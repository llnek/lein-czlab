;; Copyright © 2013-2019, Kenneth Leung. All rights reserved.
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns
  leiningen.lein-czlab

  (:require [clojure.java.io :as io]
            [leiningen.core
             [classpath :as cp]
             [utils :as cu]
             [project :as pj]
             [main :as cm]]
            [leiningen
             [jar :as jar]
             [pom :as pom]
             [javac :as lj]
             [test :as lt]]
            [clojure
             [set :as set]
             [pprint :as pp]
             [string :as cs]]
            [robert.hooke :as h])

  (:import [java.io
            File
            IOException]
           [java.nio.file
            Files
            Path
            Paths
            FileVisitResult
            SimpleFileVisitor]
           [java.nio.file.attribute BasicFileAttributes]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn- clean-dir

  "Clena out recursively a directory with native Java.
  https://docs.oracle.com/javase/tutorial/essential/io/walk.html"
  [dir]

  (let [root (io/file dir)]
    (->> (proxy [SimpleFileVisitor][]
           (visitFile [^Path file
                       ^BasicFileAttributes attrs]
             (Files/delete file)
             FileVisitResult/CONTINUE)
           (postVisitDirectory [^Path dir
                                ^IOException ex]
             (if (not= dir root)
               (Files/delete dir))
             FileVisitResult/CONTINUE))
         (Files/walkFileTree (Paths/get (.toURI root))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn- pack-lib

  "Package uberjar's jars into a separate directory."
  [project toDir]

  (let [scoped (set (pj/pom-scope-profiles project :provided))
        dft (set (pj/expand-profile project :default))
        provided (remove (set/difference dft scoped)
                         (-> project meta :included-profiles))
        project (pj/merge-profiles
                  (pj/merge-profiles project
                                     [:uberjar]) provided)
        ;;_ (pom/check-for-snapshot-deps project)
        project (update-in project
                           [:jar-inclusions]
                           concat (:uberjar-inclusions project))
        [_ jar] (first (jar/jar project nil))
        whites (select-keys project pj/whitelist-keys)
        project (-> (pj/unmerge-profiles project [:default]) (merge whites))
        deps (->> (cp/resolve-managed-dependencies
                    :dependencies :managed-dependencies project)
                  (filter #(.endsWith (.getName ^File %) ".jar")))
        jars (cons (io/file jar) deps)
        lib (io/file toDir "lib")]
    (.mkdirs lib)
    (clean-dir lib)
    (doseq [fj jars
            :let [n (.getName ^File fj)
                  t (io/file lib n)]] (io/copy fj t))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn lein-czlab

  "For czlab's internal use only:
  copies all dependent jars to out/lib."
  [project & args]

  (let [dir (second (drop-while
                      #(not= "--to-dir" %) args))
        dir (or dir (io/file (:root project)))
        dir (io/file dir)]
    (pack-lib project dir)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn- not-code?

  ""
  [^File f]

  (not (or (.endsWith (.getName f) ".java")
           (.endsWith (.getName f) ".clj"))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn- cpy-res

  ""
  [proj]

  (let [dirs (:java-source-paths proj)
        out  (:compile-path proj)
        vs (str "version=" (:version proj))
        co (remove empty?
                   (cs/split (str (:coordinate! proj)) #"/"))
        gid (first co)
        cid (cs/join "/" (drop 1 co))
        ver (cs/join "/"
                     [out
                      (or gid (:group proj))
                      (if (empty? cid)
                        (:name proj) cid) "version.properties"])]
    (cm/debug "ver file = " ver)
    (cm/debug "dirs = " dirs)
    (cm/debug "out = " out)
    (doseq [dir' dirs
            :let [dir (io/file dir')
                  cp (.getCanonicalPath dir)
                  n (inc (.length cp))]]
      (doseq [r (filter #(and (.isFile %)
                              (not-code? %)) (file-seq dir))
              :let [rp (.substring (.getCanonicalPath r) n)
                    des (io/file out rp)]]
        (cm/debug "res = " r)
        (cm/debug "des = " des)
        (when (> (.lastModified r)
                 (.lastModified des))
          (.mkdirs (.getParentFile des))
          (io/copy r des))))
    (let [v (io/file ver)]
      (.mkdirs (.getParentFile v))
      (spit v vs :encoding "utf-8"))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn hook-javac

  "The hook: run the javac task then copy all
  non code resources to out-dir."
  [task & args]

  (apply task args)
  (try (cpy-res (first args))
       (catch Throwable t (cu/error t))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn activate

  "Activate the hook"
  []

  (h/add-hook #'lj/javac #'hook-javac))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;EOF


;; Copyright (c) 2013-2017, Kenneth Leung. All rights reserved.
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns leiningen.lein-czlab

  (:require [leiningen.core.utils :as cu]
            [leiningen.core.main :as cm]
            [leiningen.javac :as lj]
            [clojure.java.io :as io]
            [clojure.string :as cs]
            [robert.hooke :as h])

  (:import [java.io File]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn lein-czlab
  "For czlab's internal use only"
  [project & args])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- notCode?
  [^File f]
  (not (or (.endsWith (.getName f) ".java")
           (.endsWith (.getName f) ".clj"))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- cpyRes
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
                        (:name proj) cid)
                      "version.properties"])]
    (cm/debug "ver file = " ver)
    (cm/debug "dirs = " dirs)
    (cm/debug "out = " out)
    (doseq [dir' dirs
            :let [dir (io/file dir')
                  cp (.getCanonicalPath dir)
                  n (inc (.length cp))]]
      (doseq [r (filter #(and (.isFile %)
                              (notCode? %)) (file-seq dir))
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
;;
(defn hookJavac
  ""
  [task & args]
  (apply task args)
  (try
    (cpyRes (first args))
    (catch Throwable t
      (cu/error t))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn activate
  ""
  []
  (h/add-hook #'lj/javac #'hookJavac))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;EOF


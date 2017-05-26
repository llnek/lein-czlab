;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defproject lein-czlab "1.0.0"

  :description "Hook: extend the build process by copying non-source-code
               resources to out.
               Task: copy all dependent jars to output-dir"

  :license {:url "http://www.eclipse.org/legal/epl-v10.html"
            :name "Eclipse Public License"}
  :url "https://github.com/llnek/lein-czlab"

  :dependencies [[commons-io/commons-io "2.5"]]


  :global-vars {*warn-on-reflection* true}
  :target-path "out/%s"
  :eval-in-leiningen true)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;EOF

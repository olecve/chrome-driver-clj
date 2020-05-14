(defproject chrome-driver-clj "0.1.3"
  :description "chrome driver downloader"
  :url "https://github.com/olecve/chrome-driver-clj"
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [com.taoensso/timbre "4.10.0"]]
  :plugins [[lein-nsorg "0.3.0"]]
  :repl-options {:init-ns chrome-driver-clj.core}
  :deploy-repositories [["releases" :clojars]
                        ["snapshots" :clojars]]
  :aliases {"nsorg" ["nsorg" "--replace"]})

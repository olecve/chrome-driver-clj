(ns chrome-driver-clj.core
  (:require [clojure.java.io :as io]
            [clojure.string :refer [includes? lower-case]]
            [taoensso.timbre :refer [info]])
  (:import (java.io File)
           (java.util.zip ZipInputStream)))

(def ^:private location-key "webdriver.chrome.driver")
(def ^:private temp-dir (System/getProperty "java.io.tmpdir"))
(def ^:private root-uri "http://chromedriver.storage.googleapis.com/")
(def ^:private latest-release-version-uri (str root-uri "LATEST_RELEASE"))

(defn- get-latest-release-version [] (slurp latest-release-version-uri))
(defn- create-driver-uri [os driver-version] (str root-uri driver-version "/chromedriver_" os ".zip"))

(defn- download [uri file]
  (with-open [in (io/input-stream uri)
              out (io/output-stream file)]
    (io/copy in out)))

(defn- unzip [absolute-path-to-zip absolute-path]
  (let [zip-stream (ZipInputStream. (io/input-stream absolute-path-to-zip))]
    (.getNextEntry zip-stream)
    (io/copy (io/input-stream zip-stream) (io/file absolute-path))
    (.closeEntry zip-stream)
    (.close zip-stream)))

(defn- file-exists? [absolute-path]
  (.exists (io/as-file absolute-path)))

(defn- os-name->file-and-os-names [os-name driver-version]
  (cond
    (includes? os-name "win") {:file-name (str "chromedriver-" driver-version ".exe") :os "win32"}
    (includes? os-name "nux") {:file-name (str "chromedriver-" driver-version) :os "linux64"}
    (includes? os-name "mac") {:file-name (str "chromedriver-" driver-version) :os "mac64"}
    :else (throw (RuntimeException. "Can't identify operating system"))))

(defn- download-driver [os driver-version zip bin]
  (info "Downloading chrome driver for" os "with version" driver-version)
  (download (create-driver-uri os driver-version) zip)
  (info "Driver is downloaded, unzipping...")
  (unzip zip bin)
  (.setExecutable (io/file bin) true)
  (info "Driver is ready"))

(defn- locate-or-download-driver [version]
  (let [os-name (-> "os.name" System/getProperty lower-case)
        {:keys [file-name os]} (os-name->file-and-os-names os-name version)
        bin (str temp-dir File/separator file-name)
        zip (str temp-dir File/separator "chromedriver_" os ".zip")
        file-exists (file-exists? bin)]
    (when-not file-exists (download-driver os version zip bin))
    {:bin         bin
     :zip         zip
     :file-exists file-exists}))

(defn init []
  (let [version (get-latest-release-version)
        paths (locate-or-download-driver version)]
    (System/setProperty location-key (:bin paths))
    (merge paths
           {:version version})))

(defn clean []
  (let [files (->> temp-dir
                   io/file
                   file-seq
                   (filter #(re-find #"chromedriver" (.getName %))))]
    (doseq [file files]
      (io/delete-file file))))

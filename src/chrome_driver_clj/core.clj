(ns chrome-driver-clj.core
  (:require [clojure.java.io :as io]
            [clojure.string :refer [includes? lower-case]])
  (:import (java.io File)))

(def ^:private driver-version "78.0.3904.70")
(def ^:private temp-dir (System/getProperty "java.io.tmpdir"))

(defn- copy [uri file]
  (with-open [in (io/input-stream uri)
              out (io/output-stream file)]
    (io/copy in out)))

(defn- download-driver [os version path-to-zip]
  (copy
    (str "http://chromedriver.storage.googleapis.com/" version "/chromedriver_" os ".zip")
    path-to-zip))

(defn- unzip [absolute-path-to-zip absolute-path]
  (let [zip-stream (java.util.zip.ZipInputStream. (io/input-stream absolute-path-to-zip))]
    (.getNextEntry zip-stream)
    (io/copy (io/input-stream zip-stream) (io/file absolute-path))))

(defn- file-exists? [absolute-path]
  (.exists (io/as-file absolute-path)))

(defn- os-name->file-and-os-names [os-name]
  (cond
    (includes? os-name "win") {:file-name (str "chromedriver-" driver-version ".exe") :os "win32"}
    (includes? os-name "nux") {:file-name (str "chromedriver-" driver-version) :os "linux64"}
    (includes? os-name "mac") {:file-name (str "chromedriver-" driver-version) :os "mac64"}
    :else (throw (Exception. "Can't recognize operating system"))))

(defn- locate-driver []
  (let [os-name (-> "os.name" System/getProperty lower-case)
        {:keys [file-name os]} (os-name->file-and-os-names os-name)
        path-to-driver (str temp-dir File/separator file-name)
        path-to-zip (str temp-dir File/separator "chromedriver_" os ".zip")]
    (when-not (file-exists? path-to-driver)
      (do
        (println (str "[ Downloading chrome driver for " os " with version " driver-version " ]"))
        (download-driver os driver-version path-to-zip)
        (println "[ Driver is downloaded, unzipping... ]")
        (unzip path-to-zip path-to-driver)
        (.setExecutable (io/file path-to-driver) true)
        (println "[ Driver is ready ]")))
    path-to-driver))

(defn init []
  (let [path-to-driver (locate-driver)]
    (System/setProperty "webdriver.chrome.driver" path-to-driver)
    path-to-driver))

(ns chrome-driver-clj.core-test
  (:require [clojure.java.io :as io]
            [chrome-driver-clj.core :as chrome-driver]
            [clojure.test :refer :all]))

(def location-key "webdriver.chrome.driver")
(defn get-path [] (System/getProperty location-key))

(defn with-clean-property-fixture [f]
  (chrome-driver/clean)
  (System/clearProperty location-key)
  (f)
  (System/clearProperty location-key)
  (chrome-driver/clean))

(use-fixtures :once with-clean-property-fixture)

(deftest init-test
  (is (nil? (get-path)))

  (let [result (chrome-driver/init)
        path (get-path)]
    (is (not (nil? path)))
    (is (= (:bin result) path))
    (is (false? (:file-exists result)))
    (is (some? (:version result)))
    (is (.exists (io/file path)))
    (is (.isFile (io/file path))))

  (let [result (chrome-driver/init)
        path (get-path)]
    (is (not (nil? path)))
    (is (= (:bin result) path))
    (is (true? (:file-exists result)))
    (is (some? (:version result)))
    (is (.exists (io/file path)))
    (is (.isFile (io/file path)))))

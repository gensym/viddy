(ns org.gensym.viddy.statistics-test
  (:require [org.gensym.viddy.statistics :as stats])
  (:use clojure.test))


(deftest rotate-keys
  (testing "Should rotate keys"
    (let [input-data {"Monday" {0.25 12, 0.5 13, 0.75 32}
                      "Tuesday" {0.25 16 0.5 42 0.75 67}
                      "Wednesday" {0.25 18 0.50 18 0.75 18}}
          expected {0.25 {"Monday" 12 "Tuesday" 16 "Wednesday" 18}
                    0.5 {"Monday" 13 "Tuesday" 42 "Wednesday" 18}
                    0.75 {"Monday" 32 "Tuesday" 67 "Wednesday" 18}}
          actual (stats/rotate-keys input-data)]
      (is (= expected actual)))))

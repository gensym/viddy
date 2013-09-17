(ns org.gensym.viddy.statistics-test
  (:require [org.gensym.viddy.statistics :as stats])
  (:use clojure.test))

(deftest should-ss
  (testing "A thing"
    (let [expected-data])
    (is (= 3 2))))

(deftest data-bucketing
  (testing "Should bucket data"
    (let [expected {0 [0 3 6 9]
                    1 [1 4 7 10]
                    2 [2 5 8 11]}]

      (is (= expected
             (stats/bucket #(mod % 3) (range 12)))))))

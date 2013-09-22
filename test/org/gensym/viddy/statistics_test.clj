(ns org.gensym.viddy.statistics-test
  (:require [org.gensym.viddy.statistics :as stats])
  (:use clojure.test))

(deftest as-percentiles
  (testing "Should split simple numbers into percentiles"
    (let [expected {0.1 9
                    0.3 29
                    0.5 49
                    0.8 79
                    0.9 89}
          inputs (range 100)
          actual (stats/percentiles [0.1 0.3 0.5 0.8 0.9] inputs)]
      (is (= expected actual))))
  (testing "Should work with small collections"
    (let [exected {0.1 1
                   0.3 1
                   0.5 1
                   0.51 2
                   0.8 2
                   0.9 2
                   1.0 2}
          inputs [1 2]
          actual (stats/percentiles [0.1 0.3 0.5 0.51 0.8 0.9 1.0] inputs)]
      (is (= exected actual))))

  (testing "Should work with a single-elemenet collection"
    (let [expected {0.1 42
                   0.5 42
                   0.9 42}]
      (is (= expected (stats/percentiles [0.1 0.5 0.9] [42])))))
  (testing "Should work with an empty collection"
    (let [expected {0.1 0
                    0.5 0
                    0.9 0}]
      (is (= expected (stats/percentiles [0.1 0.5 0.9] []))))))

(deftest series-percentiles
  (testing "Should break data down into percentiles"
    (let [input-data [
                      {:day "Monday" :value 23}
                      {:day "Monday" :value 32}
                      {:day "Tuesday" :value 99}
                      {:day "Monday" :value 12}
                      {:day "Tuesday" :value 42}
                      {:day "Monday" :value 10}
                      {:day "Monday" :value 92}
                      {:day "Wednesday" :value 18}
                      {:day "Monday" :value 13}
                      {:day "Tuesday" :value 16}
                      {:day "Tuesday" :value 67}
                      ]
          expected {"Monday" {0.25 12, 0.5 13, 0.75 32}
                    "Tuesday" {0.25 16 0.5 42 0.75 67}
                    "Wednesday" {0.25 18 0.50 18 0.75 18}}
          actual (stats/analyse-percentages
                  :day
                  :value
                  [0.25 0.5 0.75]
                  input-data)]
      (is (= expected actual)))))

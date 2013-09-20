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
      (is (= exected actual)))))

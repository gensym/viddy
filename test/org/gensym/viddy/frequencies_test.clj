(ns org.gensym.viddy.frequencies-test
  (:require [org.gensym.viddy.frequencies :as freq])
  (:use clojure.test))

(deftest creation
  (testing "Should round-trip"
    (let [input  [1 1 2 4 4 4 5 8 8 10]]
      (is (= input (freq/to-seq (freq/create input))))))
  (testing "Should work with an empty list"
    (is (= [] (freq/to-seq (freq/create []))))))

(deftest merging
  (testing "Should merge with an empty seq"
    (let [input [1 1 2 4 4 4 5 8 8 10]
          f1 (freq/create input)
          f2 (freq/create [])]
      (is (= input (freq/to-seq (freq/merge f1 f2))))))
  (testing "Should merge two seqs"
    (let [a (freq/create [1 1 3 5 5 5 5 5 5])
          b (freq/create [1 2 2 5 9 9])
          expected (freq/create [1 1 1 2 2 3 5 5 5 5 5 5 5 9 9])]
      (is (= expected (freq/merge a b))))))

(deftest percentiles
  (testing "Should split simple numbers into percentiles"
    (let [expected {0.1 9
                    0.3 29
                    0.5 49
                    0.8 79
                    0.9 89}
          inputs (freq/create (range 100))
          actual (freq/percentiles [0.1 0.3 0.5 0.8 0.9] inputs)]
      (is (= expected actual))))

  (testing "Should work with small collections"
    (let [exected {0.1 1
                   0.3 1
                   0.5 1
                   0.51 2
                   0.8 2
                   0.9 2
                   1.0 2}
          inputs (freq/create [1 2])
          actual (freq/percentiles [0.1 0.3 0.5 0.51 0.8 0.9 1.0] inputs)]
      (is (= exected actual))))

  (testing "Should work with a single-elemenet collection"
    (let [expected {0.1 42
                   0.5 42
                   0.9 42}]
      (is (= expected (freq/percentiles [0.1 0.5 0.9] (freq/create [42]))))))
  (testing "Should work with an empty collection"
    (let [expected {0.1 0
                    0.5 0
                    0.9 0}]
      (is (= expected (freq/percentiles [0.1 0.5 0.9] (freq/create [])))))))

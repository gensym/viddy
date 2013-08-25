(ns org.gensym.util.timeseries-test
  (:require [org.gensym.util.timeseries :as ts])
  (:use clojure.test))

(deftest should-filter
  (testing "Should filter redundant events"
    (let [events [{:time 1 :value "FOO"}
                  {:time 2 :value "FOO"}
                  {:time 3 :value "BAR"}
                  {:time 4 :value "BAR"}
                  {:time 5 :value "FOO"}]]

      (is (=  (list  {:time 1 :value "FOO"}
                     {:time 3 :value "BAR"}
                     {:time 5 :value "FOO"})
              (ts/filter-redundant [:time] events))))))

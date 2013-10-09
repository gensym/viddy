(ns org.gensym.viddy.timeslices-test
  (:require [org.gensym.viddy.timeslices :as ts])
  (:use clojure.test))

(deftest timeslices-test
  (testing "9:39 PM"
    (is (= "21:30"
           (ts/fifteen-minutes  #inst "2013-08-16T21:39:01.000-00:00" ))))
  (testing "4:39 AM"
        (is (= "4:30"
               (ts/fifteen-minutes  #inst "2013-08-16T04:39:01.000-00:00" )))))



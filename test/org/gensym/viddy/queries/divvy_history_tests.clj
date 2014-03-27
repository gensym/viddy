(ns org.gensym.viddy.queries.divvy-history-tests
  (require [org.gensym.viddy.queries.divvy-history :as dh]
           [org.gensym.viddy.frequencies :as freq])
  (use clojure.test))

(deftest weekdays-in-range-tests
  (testing "should return zero for weekend dates"
    (is (= 0 (dh/weekdays-in-range #inst "2014-02-08" #inst "2014-02-09"))))
  (testing "1-day weekday range should have 1 weekday"
    (is (= 1 (dh/weekdays-in-range #inst "2014-01-06" #inst "2014-01-06"))))
  (testing "1-week weekday range should have 5 weekdays"
    (is (= 5 (dh/weekdays-in-range #inst "2013-01-06" #inst "2013-01-13"))))
  (testing "greater than one week should include all weekdays"
    (is (= 8 (dh/weekdays-in-range #inst "2013-01-07" #inst "2013-01-16")))))


(deftest concat-available-bikes-weekdays-tests
  (testing "should concat something with empty"
    (let [empty {:start nil
                 :end nil
                 :data {}}
          d {:start #inst "2013-01-06"
             :end #inst "2013-01-07"
             :data {"Monday" (freq/create [1 2 3])}}]

      (do 
        (is (= d (dh/concat-available-bikes-weekdays empty d)))
        (is (= d (dh/concat-available-bikes-weekdays d empty))))))

  (testing "should concat something with no data"
    (let [empty {:start #inst "2013-01-05"
                 :end #inst "2013-01-06"
                 :data {}}
          d {:start #inst "2013-01-06"
             :end #inst "2013-01-07"
             :data {"Monday" (freq/create [1 2 3])}}
          expected {:start #inst "2013-01-05"
                    :end #inst "2013-01-07"
                    :data {"Monday" (freq/create [1 2 3])}}]
      (is (= expected (dh/concat-available-bikes-weekdays empty d)))))

  
  (testing "should concat data with same types of datapoints"
    (let [d1 {:start #inst "2013-01-06"
              :end #inst "2013-01-07"
              :data {"Monday" (freq/create [1 2 3])}}
          d2 {:start #inst "2013-01-13"
              :end #inst "2013-01-16"
              :data {"Monday" (freq/create [2 3 4])}}
          expected {:start #inst "2013-01-06"
                    :end #inst "2013-01-16"
                    :data {"Monday" (freq/create [1 2 2 3 3 4])}}]
      (is (= expected (dh/concat-available-bikes-weekdays d1 d2))))))



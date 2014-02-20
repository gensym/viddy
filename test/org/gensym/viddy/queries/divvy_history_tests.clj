(ns org.gensym.viddy.queries.divvy-history-tests
  (require [org.gensym.viddy.queries.divvy-history :as dh])
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

(comment   (testing "Should concat"

             (not (= {:data {"percentile-50"
                             {"Tuesday" 22, "Monday" 11, "Wednesday" 18},
                             "percentile-75"
                             {"Tuesday" 90, "Monday" 40, "Wednesday" 18},
                             "percentile-25"
                             {"Tuesday" 13, "Monday" 12, "Wednesday" 18}},
                      :start #inst "2013-01-06T00:00:00.000-00:00",
                      :end #inst "2013-01-17T00:00:00.000-00:00"}
                     {:start #inst "2014-01-06T00:00:00.000-00:00",
                      :end #inst "2013-01-16T00:00:00.000-00:00", :data {"percentile-25" {"Tuesday" 2185/128, "Monday" 12, "Wednesday" 18}, "percentile-75" {"Tuesday" 4197/64, "Monday" 4599/128, "Wednesday" 18}, "percentile-50" {"Tuesday" 1509/128, "Monday" 1509/256, "Wednesday" 18}}}))
))

(deftest concat-available-bikes-weekdays-tests
  (testing "should concat equivalent data"
    (let [d1 {:start #inst "2013-01-06"
              :end #inst "2013-01-07"
              :data {"percentile"  {"Monday" 12}}}
          d2 {:start #inst "2013-01-13"
              :end #inst "2013-01-16"
              :data {"percentile" {"Monday" 12}}}
          expected {:start #inst "2013-01-06"
                    :end #inst "2013-01-16"
                    :data {"percentile" {"Monday" 12}}}]
      (is (= expected (dh/concat-available-bikes-weekdays d1 d2)))))

  (comment
    (testing "should concat data with same types of datapoints"
      (let [d1 {:start #inst "2014-01-06"
                :end #inst "2013-01-07"
                :data {"percentile-25"
                       {"Monday" 12, "Tuesday" 17, "Wednesday" 18}
                       "percentile-50"
                       {"Monday" 6, "Tuesday" 12, "Wednesday" 18}
                       "percentile-75"
                       {"Monday" 36, "Tuesday" 66, "Wednesday" 18}}}
            d2 {:start #inst "2013-01-13"
                :end #inst "2013-01-16"
                :data {"percentile-25"
                       {"Monday" 12 "Tuesday" 11 "Wednesday" 18}
                       "percentile-50"
                       {"Monday" 15 "Tuesday" 30 "Wednesday" 18}
                       "percentile-75"
                       {"Monday" 42 "Tuesday" 102 "Wednesday" 18}}}
            expected {:start #inst "2013-01-06"
                      :end #inst "2013-01-17"
                      :data {"percentile-25"
                             {"Monday" 12 "Tuesday" 13 "Wednesday" 18}
                             "percentile-50"
                             {"Monday" 11 "Tuesday" 22 "Wednesday" 18}
                             "percentile-75"
                             {"Monday" 40 "Tuesday" 90 "Wednesday" 18}}}]
      
        (is (= expected (dh/concat-available-bikes-weekdays d1 d2)))))))



(ns org.gensym.viddy.queries.date-planner-test
  (require [org.gensym.viddy.queries.date-planner :as dp]
           [org.gensym.viddy.queries.time-tree :as tt])
  (:use clojure.test))

(deftest building-block-tests
  (testing "Should find next month"
    (is (= #inst "2013-02-01T00:00:00"
           (dp/next-index-boundary [1 :month]
                                   #inst "2013-01-23T10:09:23"))))
    (testing "Should align blocks of months"
      (is (= #inst "2013-03-01T00:00:00"
             (dp/next-index-boundary [2 :month]
                                     #inst "2013-01-23T10:09:23"))))
    (testing "Should return aligned if aligned"
      (is (= #inst "2013-03-01T00:00:00"
             (dp/next-index-boundary [2 :month]
                                     #inst "2013-03-01T00:00:00"))))
    (testing "The start of a year is always aligned"
      (is (= #inst "2013-01-01T00:00:00"
             (dp/next-index-boundary [9 :month]
                                     #inst "2013-01-01T00:00:00"))))
    (testing "Blocks of months need to be aligned with a correct month"
      (is (= #inst "2013-04-01T00:00:00"
             (dp/next-index-boundary [3 :month]
                                     #inst "2013-02-01T00:00:00"))))

    (testing "Blocks of months need to be aligned with a correct month 2"
      (is (= #inst "2013-04-01T00:00:00"
             (dp/next-index-boundary [3 :month]
                                     #inst "2013-02-02T00:00:00"))))
    (testing "Blocks of months need to be aligned with a correct month 3"
      (is (= #inst "2013-04-01T00:00:00"
             (dp/next-index-boundary [3 :month]
                                     #inst "2013-01-02T00:00:00"))))

    (testing "Should align to the next year (because of month block)"
      (is (= #inst "2013-01-01T00:00:00"
             (dp/next-index-boundary [10 :month]
                                     #inst "2012-11-29T00:00:01"))))

    (testing "Something already aligned to a day should remain so"
      (is (= #inst "2013-01-06T00:00:00"
             (dp/next-index-boundary [1 :day]
                                     #inst "2013-01-06T00:00:00"))))

    (testing "Should align to start of next day"
      (is (= #inst "2013-05-01T00:00:00"
             (dp/next-index-boundary [1 :day]
                                     #inst "2013-04-30T08:00:00"))))

    (testing "Should align to the start of the next block"
      (is (= #inst "2013-06-01T00:00:00"
             (dp/next-index-boundary [20 :day]
                                     #inst "2013-05-27T08:00:00"))))

    (testing "Should align to the next year"
      (is (= #inst "2013-01-01T00:00:00"
             (dp/next-index-boundary [1 :year]
                                     #inst "2012-05-01T00:00:00"))))

    (testing "Should align to the next decade"
      (is (= #inst "2010-01-01T00:00:00"
             (dp/next-index-boundary [10 :year]
                                     #inst "2002-05-01T00:00:00"))))

    (testing "Should align to the next hour"
      (is (= #inst "2013-01-01T08:00:00"
             (dp/next-index-boundary [1 :hour]
                                     #inst "2013-01-01T07:45:00"))))

    (testing "Should align to the next day (because of hour)"
      (is (= #inst "2013-01-02T00:00:00"
             (dp/next-index-boundary [1 :hour]
                                     #inst "2013-01-01T23:12:00"))))

    (testing "Should align to the next day (because of block of hours)"
      (is (= #inst "2013-01-02T00:00:00"
             (dp/next-index-boundary [10 :hour]
                                     #inst "2013-01-01T23:00:00"))))

    (testing "Should align to the next minute"
      (is (= #inst "2013-01-06T08:24:00"
             (dp/next-index-boundary [1 :minute]
                                     #inst "2013-01-06T08:23:12"))))

    (testing "Should align to the next hour (because of block of minutes)"
      (is (= #inst "2013-01-06T09:00:00"
             (dp/next-index-boundary [15 :minute]
                                     #inst "2013-01-06T08:56:00")))))



(deftest timestep-tree-tests
  (testing "Should divide a timestamp by months"
    (is (= [2 [#inst "2013-10-01T00:00:00" #inst "2013-11-12T08:15:00"]]
           (dp/divide-timestep [3 :month]
                               #inst "2013-04-01T00:00:00"
                               #inst "2013-11-12T08:15:00"))))

  (testing "Should divide a timestamp by days"
    (is (= [3 [#inst "2013-11-07T00:00:00" #inst "2013-11-12T08:15:00"]]
           (dp/divide-timestep [10 :day]
                               #inst "2013-10-08T00:00:00"
                               #inst "2013-11-12T08:15:00"))))

  (testing "Should divide a timestamp by weeks"
    (is (=  [6 [#inst "2013-11-07T00:00:00"
                #inst "2013-11-12T08:15:00"]]
            (dp/divide-timestep [2 :week]
                                #inst "2013-08-15T00:00:00"
                                #inst "2013-11-12T08:15:00"))))

  (testing "Should divide a timestamp by years"
    (is (= [2 [#inst "2013-01-01T00:00:00"
               #inst "2013-11-12T08:15:00"]]
           (dp/divide-timestep [2 :year]
                               #inst "2009-01-01T00:00:00"
                               #inst "2013-11-12T08:15:00"))))

  (testing "Should return zero in a division"
    (is (= [0 [#inst "2013-01-01T00:00:00"
               #inst "2013-11-12T08:15:00"]]
           (dp/divide-timestep [2 :year]
                               #inst "2013-01-01T00:00:00"
                               #inst "2013-11-12T08:15:00"))))

  (testing "Should not return negative"
    (is (= [0 [#inst "2013-01-01T00:00:00"
               #inst "2010-11-12T08:15:00"]]
           (dp/divide-timestep [1 :year]
                               #inst "2013-01-01T00:00:00"
                               #inst "2010-11-12T08:15:00")))))

(deftest expander-tests
  (testing "Should expand with no remainder"
    (is (= (tt/expansion [1 [2 :year]] #inst "2012-01-01" #inst "2014-01-01")
           ((dp/make-expander [2 :year]) #inst "2012-01-01" #inst "2014-01-01"))))

  (testing "Should expand with remainder before"
    (is (= (tt/expansion [1 [2 :year]] #inst "2012-01-01" #inst "2014-01-01")
           ((dp/make-expander [2 :year]) #inst "2011-01-01" #inst "2014-01-01"))))

  (testing "Should expand with a remainder after"
    (is (= (tt/expansion [1 [2 :year]] #inst "2012-01-01" #inst "2014-01-01")
           ((dp/make-expander [2 :year]) #inst "2012-01-01" #inst "2015-01-01")))))

(deftest planner-tests
  (testing "Should return zero-step planner"
    (let [planner (dp/make-planner [])]
      (is (= [[#inst "2013-12-20" #inst "2015-01-06"]]
             (planner #inst "2013-12-20" #inst "2015-01-06")))))
    (testing "Should return a 1-step planner"
             (let [planner (dp/make-planner [[1 :year]])]
      (is (= [[#inst "2013-12-20" #inst "2014-01-01"]
              [#inst "2014-01-01" [1 [1 :year]] #inst "2015-01-01"]
              [#inst "2015-01-01" #inst "2015-01-06"]]
             (planner #inst "2013-12-20" #inst "2015-01-06")))))
    (testing "Should return a multi-step planner"
      (let [planner (dp/make-planner [[1 :year]
                                      [1 :month]
                                      [1 :day]
                                      [1 :hour]
                                      [15 :minute]])]
        (is (= [[#inst "2013-12-02" [30 [1 :day]] #inst "2014-01-01"]
                [#inst "2014-01-01" [1 [1 :year]] #inst "2015-01-01"]
                [#inst "2015-01-01" [2 [1 :month]] #inst "2015-03-01"]
                [#inst "2015-03-01" [29 [1 :day]] #inst "2015-03-30"]]
               (planner #inst "2013-12-02" #inst "2015-03-30")))))) 

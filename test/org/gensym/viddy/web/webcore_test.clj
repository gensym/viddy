(ns org.gensym.viddy.web.webcore-test
  (:require [org.gensym.viddy.web.webcore :as webcore]
            [org.gensym.viddy.divvy-data :as divvy])
  (:use [clojure.test]))

(deftest available-bikes-edn-tests
  (testing "Should return a result"
    (let [bike-data [{:execution-time #inst "2013-08-28T00:38:12.455-00:00"
                      :available-bikes 3}
                     {:available-bikes 7,
                      :execution-time #inst "2013-08-16T21:39:01.000-00:00"}
                     {:execution-time #inst "2013-08-28T01:58:12.455-00:00"
                      :available-bikes 9}]
          datasource  (reify divvy/DivvyData
                        (station-info [datasource station-id] {})
                        (station-updates [datasource station-id]
                          (get {23 bike-data} station-id))
                        (current-stations [datasource] []))

          rfn (webcore/router datasource)
          response (rfn {:uri "/available_bikes/23.edn"})]

      (is (= 200 (:status response)))
      (is (= java.lang.String (type (:body response)))))))
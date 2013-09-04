(ns org.gensym.viddy.divvy-data-test
  (:require [org.gensym.viddy.divvy-data :as divvy])
  (:use clojure.test))

(deftest available-bikes-tests
  (testing "Should filter extraneous keys"
    (let [expected-data
          [{:execution-time #inst "2013-08-28T00:38:12.455-00:00"
            :available-bikes 3}
           {:available-bikes 7,
            :execution-time #inst "2013-08-16T21:39:01.000-00:00"}
           {:execution-time #inst "2013-08-28T01:58:12.455-00:00"
            :available-bikes 9}]

          from-storage  [{:execution-time #inst "2013-08-28T00:38:12.455-00:00"
                          :longitude "whocares"
                          :available-bikes 3}
                         {:available-bikes 7,
                          :available-docks 8,
                          :longitude -87.637715,
                          :latitude 41.902924,
                          :station-status "In Service",
                          :execution-time #inst "2013-08-16T21:39:01.000-00:00"}
                         {:execution-time #inst "2013-08-28T01:58:12.455-00:00"
                          :available-bikes 9}]

          datasource (reify divvy/DivvyData
                       (station-info [datasource station-id] {})
                       (station-updates [datasource station-id]
                         (get {23 from-storage} station-id))
                       (current-stations [datasource] []))]

      (is (= expected-data (divvy/available-bikes datasource 23))))))




(ns org.gensym.viddy.divvy-data-test
  (:require [org.gensym.viddy.divvy-data :as divvy])
  (:use clojure.test))

(defn- make-datasource [overrides]
  (let [defaults {:clear-caches (fn [datasource])
                  :station-info (fn [datasource station-id] {})
                  :station-updates (fn [datasource station-id from-date to-date] [])
                  :current-stations (fn [datasource] [])
                  :newest-stations (fn [datasource] [])}
        fns (merge defaults overrides)]

    (reify divvy/DivvyData
      (clear-caches [datasource] ((:clear-caches fns) datasource))
      (station-info [datasource station-id]
        ((:station-info fns) datasource station-id))
      (station-updates [datasource station-id from-date to-date]
        ((:station-updates fns) datasource station-id from-date to-date))
      (current-stations [datasource]
        ((:current-stations fns) datasource))
      (newest-stations [datasource]
        ((:newest-stations fns) datasource)))))

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

          datasource (make-datasource {:station-updates
                                       (fn [datasource station-id from-date to-date]
                                         (get {23 from-storage} station-id))})]

      (is (= expected-data (divvy/available-bikes datasource 23 #inst "2013-07-01" #inst "2013-10-01")))))

  (testing "Should filter duplicates"
    (let [expected-data
          (list
           {:execution-time #inst "2013-08-28T00:38:12.455-00:00"
            :available-bikes 3}
           {:available-bikes 7,
            :execution-time #inst "2013-08-16T21:39:01.000-00:00"}
           {:execution-time #inst "2013-08-28T01:58:12.455-00:00"
            :available-bikes 3})
          
          from-storage
          [{:execution-time #inst "2013-08-28T00:38:12.455-00:00"
            :some-key 8
            :available-bikes 3}
           {:execution-time #inst "2013-08-28T00:38:13.000-00:00"
            :some-key 9
            :available-bikes 3}
           {:available-bikes 7,
            :execution-time #inst "2013-08-16T21:39:01.000-00:00"}
           {:available-bikes 7,
            :execution-time #inst "2013-08-17T12:00:13.123-00:00"}
           {:execution-time #inst "2013-08-28T01:58:12.455-00:00"
            :available-bikes 3}]

          datasource (make-datasource {:station-updates
                                       (fn [datasource station-id from-date to-date]
                                         (get {23 from-storage} station-id))})]

      (is (= expected-data (divvy/available-bikes datasource 23 #inst "2013-07-01" #inst "2013-10-01"))))))

(deftest newest-stations-tests
  (testing "Should return newest stations"
    (let [expected-data
          (list
           {:execution-time #inst "2013-08-28T00:38:12.455-00:00"
            :station-name "Fake St. & Bogus Ave."
            :station-id 23
            :station-status "In Service"}
           {:execution-time #inst "2013-08-27T00:38:12.455-00:00"
            :station-name "Nonexistant Boulevard. & Delirious Drive"
            :station-id 23
            :station-status "Not In Service"})
          datasource (make-datasource {:newest-stations
                                       (fn [datasource] expected-data)})]

      (is (= expected-data (divvy/newest-stations datasource))))))


(deftest available-docks-tests
  (testing "Should filter extraneous keys"
    (let [expected-data
          (list {:execution-time #inst "2013-08-28T00:38:12.455-00:00"
                 :available-docks 3}
                {:available-docks 7,
                 :execution-time #inst "2013-08-16T21:39:01.000-00:00"}
                {:execution-time #inst "2013-08-28T01:58:12.455-00:00"
                 :available-docks 9})
          
          from-storage  [{:execution-time #inst "2013-08-28T00:38:12.455-00:00"
                          :longitude "whocares"
                          :available-docks 3}
                         {:available-docks 7,
                          :available-bikes 8,
                          :longitude -87.637715,
                          :latitude 41.902924,
                          :station-status "In Service",
                          :execution-time #inst "2013-08-16T21:39:01.000-00:00"}
                         {:execution-time #inst "2013-08-28T01:58:12.455-00:00"
                          :available-docks 9}]

          datasource (make-datasource {:station-updates
                                       (fn [datasource station-id from-date to-date]
                                         (get {23 from-storage} station-id))})]
      
      (is (= expected-data (divvy/available-docks datasource 23 #inst "2013-07-01" #inst "2013-10-01")))))

  (testing "Should filter duplicates"
    (let [expected-data
          (list
           {:execution-time #inst "2013-08-28T00:38:12.455-00:00"
            :available-docks 3}
           {:available-docks 7,
            :execution-time #inst "2013-08-16T21:39:01.000-00:00"}
           {:execution-time #inst "2013-08-28T01:58:12.455-00:00"
            :available-docks 3})
          
          from-storage
          [{:execution-time #inst "2013-08-28T00:38:12.455-00:00"
            :some-key 8
            :available-docks 3}
           {:execution-time #inst "2013-08-28T00:38:13.000-00:00"
            :some-key 9
            :available-docks 3}
           {:available-docks 7,
            :execution-time #inst "2013-08-16T21:39:01.000-00:00"}
           {:available-docks 7,
            :execution-time #inst "2013-08-17T12:00:13.123-00:00"}
           {:execution-time #inst "2013-08-28T01:58:12.455-00:00"
            :available-docks 3}]

          datasource (make-datasource {:station-updates
                                       (fn [datasource station-id from-date to-date]
                                         (get {23 from-storage} station-id))})]
      
      (is (= expected-data (divvy/available-docks datasource 23 #inst "2013-07-01" #inst "2013-10-01"))))))

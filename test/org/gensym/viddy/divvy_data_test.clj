(ns org.gensym.viddy.divvy-data-test
  (:require [org.gensym.viddy.divvy-data :as divvy]
            [clj-time.format :as time-format]
            [clj-time.coerce :as time-coerce])
  
  (:use clojure.test))

(defn- make-datasource [overrides]
  (let [defaults {:clear-caches (fn [datasource])
                  :station-info (fn [datasource station-id] {})
                  :station-updates (fn [datasource station-id] [])
                  :current-stations (fn [datasource] [])
                  :newest-stations (fn [datasource] [])}
        fns (merge defaults overrides)]

    (reify divvy/DivvyData
      (clear-caches [datasource] ((:clear-caches fns) datasource))
      (station-info [datasource station-id]
        ((:station-info fns) datasource station-id))
      (station-updates [datasource station-id]
        ((:station-updates fns) datasource station-id))
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
                                       (fn [datasource station-id]
                                         (get {23 from-storage} station-id))})]

      (is (= expected-data (divvy/available-bikes datasource 23)))))

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
                                       (fn [datasource station-id]
                                         (get {23 from-storage} station-id))})]

      (is (= expected-data (divvy/available-bikes datasource 23))))))

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
                                       (fn [datasource station-id]
                                         (get {23 from-storage} station-id))})]
      
      (is (= expected-data (divvy/available-docks datasource 23)))))

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
                                       (fn [datasource station-id]
                                         (get {23 from-storage} station-id))})]
      
      (is (= expected-data (divvy/available-docks datasource 23)))))


  )

(deftest percentiles-tests
  (testing "Should get percentiles by day for weekdays"
    (let [ ;; August 26, 2013 was a Monday          
          from-storage [{:execution-time #inst "2013-08-26T00:38:12.455-00:00"
                         :longitude "whocares"
                         :available-bikes 23}
                        
                        {:available-bikes 32,
                         :available-docks 999,
                         :longitude -87.637715,
                         :latitude 41.902924,
                         :station-status "In Service",
                         :execution-time #inst "2013-08-26T21:39:01.000-00:00"}

                        {:execution-time #inst "2013-08-29T01:58:12.455-00:00"
                         :available-bikes 9999}

                        {:execution-time #inst "2013-08-25T01:58:12.455-00:00"
                         :available-bikes 9999}

                        {:execution-time #inst "2013-08-27T01:58:12.455-00:00"
                         :available-bikes 99}

                        {:execution-time #inst "2013-08-26T01:58:12.455-00:00"
                         :available-bikes 12}

                        {:execution-time #inst "2013-08-27T01:58:12.455-00:00"
                         :available-bikes 42}

                        {:execution-time #inst "2013-08-26T01:58:12.455-00:00"
                         :available-bikes 10}

                        {:execution-time #inst "2013-08-26T01:58:12.455-00:00"
                         :available-bikes 92}

                        {:execution-time #inst "2013-08-28T01:58:12.455-00:00"
                         :available-bikes 18}
                        
                        {:execution-time #inst "2013-08-26T01:58:12.455-00:00"
                         :available-bikes 13}

                        {:execution-time #inst "2013-08-27T01:58:12.455-00:00"
                         :available-bikes 16}

                        {:execution-time #inst "2013-08-27T01:58:12.455-00:00"
                         :available-bikes 67}]

          expected-data {0.25 {"Monday" 12, "Tuesday" 16, "Wednesday" 18}
                         0.5  {"Monday" 13, "Tuesday" 42, "Wednesday" 18}
                         0.75 {"Monday" 32, "Tuesday" 67, "Wednesday" 18}}

          datasource (make-datasource {:station-updates
                                       (fn [datasource station-id]
                                         (get {23 from-storage} station-id))})

          actual-data (divvy/available-bikes-percentiles
                       datasource
                       23
                       :available-bikes
                       #(time-format/unparse
                         (time-format/formatter "EEEE")
                         (time-coerce/from-date (:execution-time %)))
                       #{"Monday" "Tuesday" "Wednesday"}
                       [0.25 0.5 0.75])]

      (is (= actual-data expected-data)))))


(comment (def from-storage [{:execution-time #inst "2013-08-26T00:38:12.455-00:00"
                         :longitude "whocares"
                         :available-bikes 23}
                        
                        {:available-bikes 32,
                         :available-docks 999,
                         :longitude -87.637715,
                         :latitude 41.902924,
                         :station-status "In Service",
                         :execution-time #inst "2013-08-26T21:39:01.000-00:00"}

                        {:execution-time #inst "2013-08-29T01:58:12.455-00:00"
                         :available-bikes 9999}

                        {:execution-time #inst "2013-08-25T01:58:12.455-00:00"
                         :available-bikes 9999}

                        {:execution-time #inst "2013-08-27T01:58:12.455-00:00"
                         :available-bikes 99}

                        {:execution-time #inst "2013-08-26T01:58:12.455-00:00"
                         :available-bikes 12}

                        {:execution-time #inst "2013-08-27T01:58:12.455-00:00"
                         :available-bikes 42}

                        {:execution-time #inst "2013-08-26T01:58:12.455-00:00"
                         :available-bikes 10}

                        {:execution-time #inst "2013-08-26T01:58:12.455-00:00"
                         :available-bikes 92}

                        {:execution-time #inst "2013-08-28T01:58:12.455-00:00"
                         :available-bikes 18}
                        
                        {:execution-time #inst "2013-08-26T01:58:12.455-00:00"
                         :available-bikes 13}

                        {:execution-time #inst "2013-08-27T01:58:12.455-00:00"
                         :available-bikes 16}

                        {:execution-time #inst "2013-08-27T01:58:12.455-00:00"
                         :available-bikes 67}]))

(comment (pprint (divvy/available-bikes-percentiles
                                           (make-datasource {:station-updates
                                                             (fn [datasource station-id]
                                                                (get {23 from-storage} station-id))})
                                           23
                                           :available-bikes
                                           #(time-format/unparse
                                             (time-format/formatter "EEEE")
                                             (time-coerce/from-date (:execution-time %)))
                                           #{"Monday" "Tuesday" "Wednesday"}
                                           [0.25 0.5 0.75])))

(ns org.gensym.viddy.divvy-data-test
  (:require [org.gensym.viddy.divvy-data :as divvy]
            [org.gensym.viddy.frequencies :as freq]
            [clj-time.format :as time-format]
            [clj-time.coerce :as time-coerce])
  
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

(deftest execution-time-tests
  (testing "should mark a weekday as a weekday"
    (is (divvy/weekday? {:execution-time #inst "2013-08-26T00:38:12.455-00:00"})))
  (testing "should not mark a weekend as a weekday"
    (is (not (divvy/weekday? {:execution-time #inst "2013-08-25T00:38:12.455-00:00"}))))
  (testing "should market a weekend as a weekend"
    (is (divvy/weekend? {:execution-time #inst "2013-08-25T00:38:12.455-00:00"})))
  (testing "should not market a weekday as a weekend"
    (is (not (divvy/weekend? {:execution-time #inst "2013-08-26T00:38:12.455-00:00"})))))

(deftest frequencies-tests
  (testing "Should get frequencies by day for weekdays"
    (let [ ;; August 26, 2013 was a Monday          
          from-storage [{:execution-time #inst "2013-08-26T00:38:12.455-00:00"
                         :longitude "whocares"
                         :available-bikes 13}
                        
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

                        {:execution-time #inst "2013-09-02T01:58:12.455-00:00"
                         :available-bikes 8}

                        {:execution-time #inst "2013-09-09T01:58:12.455-00:00"
                         :available-bikes 8}

                        {:execution-time #inst "2013-09-16T01:58:12.455-00:00"
                         :available-bikes 8}

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

          expected-data {"Monday" (freq/create [8 8 8 10 12 13 13 32 92])
                         "Tuesday" (freq/create [16 42 67 99])
                         "Wednesday" (freq/create [18])}

          datasource (make-datasource {:station-updates
                                       (fn [datasource station-id from-date to-date]
                                         (get {23 from-storage} station-id))})

          to-key #(time-format/unparse
                   (time-format/formatter "EEEE")
                   (time-coerce/from-date (:execution-time %)))

          actual-data (divvy/available-bikes-frequencies
                       datasource
                       23
                       #inst "2013-07-01"
                       #inst "2013-10-01"
                       (comp #{"Monday" "Tuesday" "Wednesday"} to-key)
                       :available-bikes
                       to-key)]

      (is (= actual-data expected-data)))))

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

          expected-data {"percentile-25"
                         {"Monday" 12, "Tuesday" 16, "Wednesday" 18}
                         "percentile-50"
                         {"Monday" 13, "Tuesday" 42, "Wednesday" 18}
                         "percentile-75"
                         {"Monday" 32, "Tuesday" 67, "Wednesday" 18}}

          datasource (make-datasource {:station-updates
                                       (fn [datasource station-id from-date to-date]
                                         (get {23 from-storage} station-id))})

          to-key #(time-format/unparse
                   (time-format/formatter "EEEE")
                   (time-coerce/from-date (:execution-time %)))

          actual-data (divvy/available-bikes-percentiles
                       datasource
                       23
                       #inst "2013-07-01"
                       #inst "2013-10-01"
                       (comp #{"Monday" "Tuesday" "Wednesday"} to-key)
                       :available-bikes
                       to-key
                       [0.25 0.5 0.75])]

      (is (= actual-data expected-data)))))

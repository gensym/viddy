(ns org.gensym.viddy.divvy-data
  (use [clj-time.coerce :only [from-date]]
       [clj-time.predicates :only [sunday? saturday?]])
  (require [org.gensym.util.timeseries :as timeseries]
           [org.gensym.viddy.timeslices :as ts]
           [org.gensym.viddy.statistics :as stats]))

(defprotocol DivvyData
  (clear-caches [datasource])
  (station-info [datasource station-id])
  (station-updates [datasource station-id from-date to-date])
  (current-stations [datasource])
  (newest-stations [datasource]))

(defn available-bikes [datasource station-id from-date to-date]
  (->> (station-updates datasource station-id from-date to-date)
       (map (fn [record]
              (select-keys record [:execution-time :available-bikes])))
       (timeseries/filter-redundant [:execution-time])))

(defn available-docks [datasource station-id from-date to-date]
  (->> (station-updates datasource station-id from-date to-date)
       (map (fn [record]
              (select-keys record [:execution-time :available-docks])))
       (timeseries/filter-redundant [:execution-time])))

(defn available-bikes-percentiles [datasource
                                   station-id
                                   from-date
                                   to-date
                                   include-datum?
                                   datum->value
                                   datum->key
                                   percentiles]
  (->> (station-updates datasource station-id from-date to-date)
       (filter include-datum?)
       (map (fn [rec] [(datum->key rec) (datum->value rec)]))
       (stats/analyse-percentages first second percentiles)
       (stats/rotate-keys)
       (stats/rename-keys #(str "percentile-" (int (* % 100))))))


(defn weekend? [station-update]
  (let [t (from-date (:execution-time station-update))]
    (or (saturday? t)
        (sunday? t))))

(def weekday? (comp not weekend?))

(defn available-bikes-weekdays [datasource station-id from-date to-date]
  (available-bikes-percentiles datasource
                               station-id
                               from-date
                               to-date
                               weekday?
                               :available-bikes
                               (comp ts/fifteen-minutes :execution-time)
                               [0.1 0.15 0.25 0.5 0.75 0.85 0.9]))

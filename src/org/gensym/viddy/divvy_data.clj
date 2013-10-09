(ns org.gensym.viddy.divvy-data
  (require [org.gensym.util.timeseries :as timeseries]))

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

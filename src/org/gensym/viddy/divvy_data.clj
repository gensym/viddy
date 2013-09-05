(ns org.gensym.viddy.divvy-data
  (require [org.gensym.util.timeseries :as timeseries]))

(defprotocol DivvyData
  (station-info [datasource station-id])
  (station-updates [datasource station-id])
  (current-stations [datasource]))

(defn available-bikes [datasource station-id]
  (->> (station-updates datasource station-id)
       (map (fn [record]
              (select-keys record [:execution-time :available-bikes])))
       (timeseries/filter-redundant [:execution-time])))

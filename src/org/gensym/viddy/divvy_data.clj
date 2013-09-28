(ns org.gensym.viddy.divvy-data
  (require [org.gensym.util.timeseries :as timeseries]
           [org.gensym.viddy.statistics :as stats]))

(defprotocol DivvyData
  (clear-caches [datasource])
  (station-info [datasource station-id])
  (station-updates [datasource station-id])
  (current-stations [datasource])
  (newest-stations [datasource]))

(defn available-bikes [datasource station-id]
  (->> (station-updates datasource station-id)
       (map (fn [record]
              (select-keys record [:execution-time :available-bikes])))
       (timeseries/filter-redundant [:execution-time])))

(defn available-docks [datasource station-id]
  (->> (station-updates datasource station-id)
       (map (fn [record]
              (select-keys record [:execution-time :available-docks])))
       (timeseries/filter-redundant [:execution-time])))

(defn available-bikes-percentiles [datasource
                                   station-id
                                   include-datum?
                                   datum->value
                                   datum->key
                                   percentiles]
  (->> (station-updates datasource station-id)
       (filter include-datum?)
       (map (fn [rec] [(datum->key rec) (datum->value rec)]))
       (stats/analyse-percentages first second percentiles)
       (stats/rotate-keys))

  )

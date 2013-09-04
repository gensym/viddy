(ns org.gensym.viddy.divvy-data)

(defprotocol DivvyData
  (station-info [datasource station-id])
  (station-updates [datasource station-id])
  (current-stations [datasource]))

(defn available-bikes [datasource station-id]
  (map (fn [record]
         (select-keys record [:execution-time :available-bikes]))
       (station-updates datasource station-id)))

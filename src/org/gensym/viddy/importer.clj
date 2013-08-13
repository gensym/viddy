(ns org.gensym.viddy.importer
  (:require [org.gensym.viddy.stations :as stations]
            [org.gensym.viddy.storage :as storage]
            [clojure.java.jdbc.sql :as sql]))

;; TODO - reuse a connection, rather than reconnect with every operation
(def db-spec {:connection-uri "jdbc:postgresql://localhost:5432/viddy"})

(def divvy-api->storage
  {:id "station_id"
   :docks "docks"
   :bikes "bikes"
   :latitude "latitude"
   :longitude "longitude"
   :name "station_name"
   :status "status"})

(defn mapkeys [keymap mapcoll]
  (map 
   #(reduce
    (fn [m [k v]] (assoc m (get keymap k) v)) {} %)
   mapcoll))

(defn import-current-station-status! []
  (let [current-status 
        (stations/current-station-status)]
    (if (= 200 (:status current-status))
      (let [execution-time (:execution-time current-status)
            rows (->> current-status
                      (:stations)
                      (mapkeys divvy-api->storage))]
        (storage/save-station-updates! db-spec execution-time rows)))))

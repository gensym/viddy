(ns org.gensym.viddy.importer
  (:require [org.gensym.viddy.stations :as stations]
            [org.gensym.viddy.storage :as storage]
            [clojure.tools.logging :as log]
            [clojure.java.jdbc.sql :as sql]))

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

(defn import-current-station-status! [db-spec]
  (let [current-status 
        (stations/current-station-status)]
    (if (= 200 (:status current-status))
      (do
        (log/info "Retrieved current Divvy station status")
        (let [execution-time (:execution-time current-status)
              rows (->> current-status
                        (:stations)
                        (mapkeys divvy-api->storage))]
          (storage/save-station-updates! db-spec execution-time rows)
          ;; current-stations uses data from station-additions, so it
          ;; must be updated second
          (storage/refresh-station-addtions db-spec)
          (storage/refresh-current-stations db-spec)))
      (do
        (log/error "Failed to retrieve current Divvy station status: " current-status)))))

(ns org.gensym.viddy.storage
  (:require [clojure.java.jdbc :as sql]
            [clojure.java.jdbc.sql :as sqldsl]
            [clojure.core.memoize :as memo]
            [org.gensym.viddy.divvy-data :as data]))


(comment (def db-spec {:connection-uri "jdbc:postgresql://localhost:5432/viddy"}))

(defn- date->sqldate [date]
  (-> date
      .getTime
      java.sql.Timestamp.))

(defn- replace-keys [keymap m]
  (reduce (fn [m [k v]]
            (if (contains? keymap k)
              (assoc m (get keymap k) v)
              (assoc m k v))) {} m))

(defn save-station-updates! [db-spec execution-time station-updates]
  (sql/db-transaction
   [t-con db-spec]
   (let [[{execution-id :id}] 
         (sql/insert! t-con "station_update_executions"
                      {"execution_time"
                       (date->sqldate execution-time)})]
     (apply sql/insert! t-con "station_updates"
            (map
             (fn [row]
               (assoc row "execution_id" execution-id))
             station-updates)))))

(defn station-info [db-spec station-id]
  (first
   (->>
    (sql/query db-spec
               [(str "SELECT s.station_name, s.longitude, s.latitude, "
                     "       s.status, s.bikes, s.docks "
                     "FROM current_stations s WHERE s.station_id = ?")
                station-id])
    (map
     #(replace-keys {:station_name :station-name
                     :bikes :available-bikes
                     :docks :available-docks} %)))))

(defn station-updates [db-spec station-id]
  (->>
   (sql/query db-spec
              [(str "SELECT s.bikes, s.docks, "
                    "       s.longitude, s.latitude, s.status, "
                    "       e.execution_time "
                    "FROM station_updates s, station_update_executions e "
                    "WHERE s.execution_id = e.id AND s.station_id = ? "
                    "ORDER BY e.insertion_time"),
               station-id])
   (map
    #(replace-keys {:execution_time :execution-time
                    :bikes :available-bikes
                    :docks :available-docks
                    :status :station-status} %))))

(defn current-stations [db-spec]
  (->>
   (sql/query db-spec
              ["SELECT id, station_id, station_name, bikes, docks, longitude, latitude, status, execution_time FROM current_stations"])
   (map 
    #(replace-keys {:station_id :station-id
                   :station_name :station-name
                    :execution_time :execution-time
                    :bikes :available-bikes
                    :docks :available-docks
                    :status :station-status} %))))

(defn make-divvy-data [db-spec]
  (let [current-stations
        (memo/ttl current-stations :ttl/threshold (* 60 60 1000))]
    (reify data/DivvyData
      (clear-caches [this]
        (memo/memo-clear! current-stations))
      (station-info [this station-id]
        (station-info db-spec station-id))
      (station-updates [this station-id]
        (station-updates db-spec station-id))
      (current-stations [this]
        (current-stations db-spec)))))

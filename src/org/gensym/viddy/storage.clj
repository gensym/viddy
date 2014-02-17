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

(defn refresh-current-stations [db-spec]
  (sql/execute! db-spec ["REFRESH MATERIALIZED VIEW current_stations"]))

(defn refresh-station-addtions [db-spec]
  (sql/execute! db-spec ["REFRESH MATERIALIZED VIEW station_additions"]))


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

(defn station-updates [db-spec station-id from-date to-date]
  (->>
   (sql/query db-spec
              [(str "SELECT s.bikes, s.docks, "
                    "       s.longitude, s.latitude, s.status, "
                    "       e.execution_time "
                    "FROM station_updates s, station_update_executions e "
                    "WHERE s.execution_id = e.id AND s.station_id = ? "
                    "AND e.execution_time >= ? "
                    "AND e.execution_time <= ? "
                    "ORDER BY e.insertion_time"),
               station-id
               (date->sqldate from-date)
               (date->sqldate to-date)])
   (map
    #(replace-keys {:execution_time :execution-time
                    :bikes :available-bikes
                    :docks :available-docks
                    :status :station-status} %))))

(defn current-stations [db-spec]
  (->>
   (sql/query db-spec
              [(str "SELECT id, station_id, "
                    "       station_name, bikes, "
                    "       docks, longitude, latitude, "
                    "       status, execution_time, addition_time "
                    "FROM current_stations ORDER BY station_id")])
   (map 
    #(replace-keys {:station_id :station-id
                   :station_name :station-name
                    :execution_time :execution-time
                    :bikes :available-bikes
                    :docks :available-docks
                    :status :station-status
                    :addition_time :addition-time} %))))

(defn newest-stations [db-spec]
  (->>
   (sql/query db-spec
              [(str "SELECT s.station_id AS station_id, "
                    "       a.addition_time AS addition_time, "
                    "       s.station_name AS station_name, "
                    "       s.status AS station_status "
                    "FROM station_additions a, current_stations s "
                    "WHERE s.station_id = a.station_id "
                    "ORDER BY a.addition_time DESC LIMIT 10")])
   (map
    #(replace-keys {:station_id :station-id
                    :station_name :station-name
                    :addition_time :addition-time
                    :station_status :station-status} %))))

(defn make-divvy-data [db-spec]
  (let [one-minute (* 60 1000)
        current-stations (memo/ttl current-stations :ttl/threshold one-minute)
        newest-stations (memo/ttl newest-stations :ttl/threshold one-minute)
        station-info (memo/ttl station-info :ttl/threshold one-minute)
        station-updates (memo/lru station-updates :lru/threshold 64)]
    (reify data/DivvyData
      (clear-caches [this]
        (memo/memo-clear! current-stations))
      (station-info [this station-id]
        (station-info db-spec station-id))
      (station-updates [this station-id from-date to-date]
        (station-updates db-spec station-id from-date to-date))
      (current-stations [this]
        (current-stations db-spec))
      (newest-stations [this]
        (newest-stations db-spec)))))

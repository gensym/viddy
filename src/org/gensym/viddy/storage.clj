(ns org.gensym.viddy.storage
  (:require [clojure.java.jdbc :as sql]
            [clojure.java.jdbc.sql :as sqldsl]))


(comment (def db-spec {:connection-uri "jdbc:postgresql://localhost:5432/viddy"}))

(defn- date->sqldate [date]
  (-> date
      .getTime
      java.sql.Timestamp.))

(defn replace-keys [keymap m]
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


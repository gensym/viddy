(ns org.gensym.viddy.storage
  (:require [clojure.java.jdbc :as sql]
            [clojure.java.jdbc.sql :as sqldsl]))


(comment (def db-spec {:connection-uri "jdbc:postgresql://localhost:5432/viddy"}))

(defn- date->sqldate [date]
  (-> date
      .getTime
      java.sql.Timestamp.))

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

(defn current-stations [db-spec]
  (sql/query db-spec
             ["SELECT * FROM station_updates WHERE t = (SELECT MAX(t) FROM station_updates)"]))


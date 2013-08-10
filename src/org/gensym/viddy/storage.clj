(ns org.gensym.viddy.storage
  (:require [clojure.java.jdbc :as sql]
            [clojure.java.jdbc.sql :as sqldsl]))


(defn- datetime->sqldate [datetime]
  (-> datetime
      .toDate
      .getTime
      java.sql.Timestamp.))

(defn- datetimes->sqldates [rows]
  (map
   (fn [row]
     (reduce (fn [m [k v]]
               (assoc m k
                      (if (= org.joda.time.DateTime (type v))
                        (datetime->sqldate v)
                        v)))
             {} row))
   rows))

(defn save-station-updates! [db-spec station-updates]
  (apply sql/insert!
         db-spec
         "station_updates"
         (datetimes->sqldates station-updates)))

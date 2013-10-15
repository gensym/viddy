(ns org.gensym.viddy.benchmark
  (:gen-class)
  (:require [org.gensym.viddy.dbconnection :as dbconn]
            [org.gensym.viddy.storage :as storage]
            [org.gensym.viddy.divvy-data :as data]
            [clojure.tools.logging :as log]))

(defn make-timed-divvy-data [wrapped]
  (reify data/DivvyData
    (clear-caches [this])
    (station-info [this station-id]
      (data/station-info wrapped station-id))
    (station-updates [this station-id from-date to-date]
      (let [start (java.lang.System/currentTimeMillis)
            ret (data/station-updates wrapped station-id from-date to-date)]
        (log/info "Data for station-updates took" (- (java.lang.System/currentTimeMillis) start) "ms")))
    (current-stations [this]
      (data/current-stations wrapped))
    (newest-stations [this]
      (data/newest-stations wrapped))))



(defn start [db-spec]
  (let [datasource (make-timed-divvy-data (storage/make-divvy-data db-spec))
        from #inst "2013-09-01"
        to #inst "2013-10-01"
        station-id 77]
    (loop []
      (let [start (java.lang.System/currentTimeMillis)]
        (data/available-bikes-weekdays datasource station-id from to)
        (log/info "Getting weekday data took" (- (java.lang.System/currentTimeMillis) start) "ms")
        (recur)))))

(defn -main []
  (let [db-spec (dbconn/db-spec  (System/getenv "DATABASE_URL"))]
    (set! *warn-on-reflection* true)
    (start db-spec)))



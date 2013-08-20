(ns org.gensym.viddy.workerrun
  (:gen-class)
  (:require [org.gensym.util.scheduler :as sched]
            [org.gensym.viddy.importer :as importer]
            [org.gensym.viddy.dbconnection :as dbconn]))

(defn start-schedulers [db-spec]
  (let [s (sched/make-scheduler!
           #(importer/import-current-station-status! db-spec)
           0
           (* 1000 60))]
    (fn [] (sched/shutdown! s))))


(defn start [db-spec]
  (let [schedulers (start-schedulers db-spec)]
    (fn []
      (schedulers))))

(defn -main []
  (let [db-spec (dbconn/db-spec  (System/getenv "DATABASE_URL"))]
    (start db-spec)
    (.start (Thread. #()))))


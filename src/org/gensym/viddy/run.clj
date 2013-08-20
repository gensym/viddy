(ns org.gensym.viddy.run
  (:gen-class)
  (:require [org.gensym.viddy.webrun :as webrun]
            [org.gensym.util.scheduler :as sched]
            [org.gensym.viddy.importer :as importer]
            [org.gensym.viddy.dbconnection :as dbconn]
            [org.gensym.viddy.workerrun :as workerrun]))

(defn start [port db-spec]
  (let [webserver (webrun/start port db-spec)
        schedulers (workerrun/start db-spec)]
    (fn []
      (schedulers)
      (webserver))))

(defn -main []
  (let [port (Integer/parseInt
              (or (System/getenv "PORT") "8080"))
        db-spec (dbconn/db-spec  (System/getenv "DATABASE_URL"))]
    (start port db-spec)))


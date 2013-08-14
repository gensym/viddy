(ns org.gensym.viddy.run
  (:gen-class)
  (:require [org.gensym.viddy.web.webcore :as webapp]
            [org.gensym.util.scheduler :as sched]
            [org.gensym.viddy.importer :as importer]
            [org.gensym.sample.jetty :as jetty]))

(defn start-schedulers []
  (let [s (sched/make-scheduler!
           importer/import-current-station-status!
           0
           (* 1000 60))]
    (fn [] (sched/shutdown! s))))

(defn start [port]
  (let [server (jetty/make-jetty-server (webapp/handler) port)
        schedulers (start-schedulers)]
    (.start server)
    (fn []
      (schedulers)
      (.stop server))))

(defn -main []
  (start 8080)
  (.start (Thread. #())))


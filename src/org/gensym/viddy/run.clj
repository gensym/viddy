(ns org.gensym.viddy.run
  (:gen-class)
  (:require [org.gensym.viddy.web.webcore :as webapp]
            [org.gensym.util.scheduler :as sched]
            [org.gensym.viddy.importer :as importer]
            [org.gensym.sample.jetty :as jetty]))

(defn start-schedulers [db-spec]
  (let [s (sched/make-scheduler!
           #(importer/import-current-station-status! db-spec)
           0
           (* 1000 60))]
    (fn [] (sched/shutdown! s))))

(defn start [port]
  (let [db-spec  {:connection-uri (str "jdbc:" (System/getenv "DATABASE_URL"))}
        server (jetty/make-jetty-server (webapp/handler db-spec) port)
        schedulers (start-schedulers db-spec)]
    (.start server)
    (fn []
      (schedulers)
      (.stop server))))

(defn -main []
  (let [port (Integer/parseInt
              (or (System/getenv "PORT") "8080"))]
    (start port)
    (.start (Thread. #()))))


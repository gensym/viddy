(ns org.gensym.viddy.webrun
  (:gen-class)
  (:require [org.gensym.viddy.web.webcore :as webapp]
            [org.gensym.sample.jetty :as jetty]
            [org.gensym.viddy.dbconnection :as dbconn]))

(defn start [port db-spec]
  (let [server (jetty/make-jetty-server (webapp/handler db-spec) port)]
    (.start server)
    (fn []
      (.stop server))))



(defn -main []
  (let [port (Integer/parseInt
              (or (System/getenv "PORT") "8080"))
        db-spec (dbconn/db-spec  (System/getenv "DATABASE_URL"))]
    (start port db-spec)))

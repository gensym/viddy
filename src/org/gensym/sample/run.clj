(ns org.gensym.sample.run
  (:gen-class)
  (:require [org.gensym.sample.webcore :as webapp]
            [org.gensym.sample.jetty :as jetty]))

(defn start [port]
  (let [server (jetty/make-jetty-server (webapp/handler) port)]
    (.start server)
    (fn [] (.stop server))))

(defn -main []
  (start 8080)
  (.start (Thread. #())))


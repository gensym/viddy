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


(defn- to-db-spec [uristring]
  (let [uri (java.net.URI. uristring)
        without-auth     {:subprotocol "postgresql"
                          :subname (str "//" (.getHost uri) ":"
                                        (.getPort uri)
                                        (.getPath uri))}]
    (if-let [user-info (.getUserInfo uri)]
      (let [[user password] (clojure.string/split user-info #":")]
        (merge  without-auth
                {:user user
                 :password password}))
      without-auth)))

(defn start [port]
  (let [db-spec (to-db-spec  (System/getenv "DATABASE_URL"))
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


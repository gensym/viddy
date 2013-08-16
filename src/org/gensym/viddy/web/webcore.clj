(ns org.gensym.viddy.web.webcore
  (:require
   [org.gensym.viddy.storage :as storage]
   [org.gensym.viddy.web.stations :as stations]
   [ring.middleware.params :as params]
   [ring.middleware.file :as file]
   [ring.middleware.file-info :as file-info]))


(def  dummy-content
  [{:station-id 16
    :station-name "Marshfield Ave & North Ave"
    :available-bikes 6
    :available-docks 9
    :station-status "In Service"}
   {:station-id 17
    :station-name "Wood St & Division St"
    :available-bikes   7
    :available-docks 8
    :station-status "In Service"}
   {:station-id 19
    :station-name "Loomis St & Taylor St"
    :available-bikes  12
    :available-docks 3
    :station-status "In Service"}
   {:station-id 20
    :station-name "Sheffield Ave & Kingsbury St"
    :available-bikes   9
    :available-docks 6
    :station-status "In Service"}])

(defn router [db-spec req]
  (condp = (:uri req)
    "/dynamic" {:status 200
                :headers {"Content-Type" "text/html"}
                :body (str "Hello, there. This is dynamically generated:" (+ 2 3))}
    "/stations.html" {:status 400
                      :headers {"Content-Type" "text/html"}
                      :body (apply str
                                   (stations/html-page
                                    (partial storage/current-stations db-spec)
                                    req))}
    {:status 404
     :headers {"Content-Type" "text/html"}
     :body "Oops"}))


(defn handler [db-spec]
  (->
   (params/wrap-params (partial router db-spec))
   (file/wrap-file "resources/public")
   (file-info/wrap-file-info)))

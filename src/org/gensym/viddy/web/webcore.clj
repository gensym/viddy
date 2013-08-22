(ns org.gensym.viddy.web.webcore
  (:require
   [org.gensym.viddy.storage :as storage]
   [org.gensym.viddy.web.stations :as stations]
   [org.gensym.util.webroutes :as w]
   [ring.middleware.params :as params]
   [ring.middleware.file :as file]
   [ring.middleware.file-info :as file-info]))

(defn- html-page [nodes]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (apply str nodes)})

(defn router [db-spec]
  (w/make-router
   (w/string-matcher "/stations.html"
                     (fn [req] (html-page
                               (stations/stations-html-page
                                (storage/current-stations db-spec)))))
   (w/re-matcher #"/station/(\d+)\.html"
                 (fn [req station-id]
                   (html-page station-id)))))

(defn handler [db-spec]
  (-> (params/wrap-params (router db-spec))
      (file/wrap-file "resources/public")
      (file-info/wrap-file-info)))

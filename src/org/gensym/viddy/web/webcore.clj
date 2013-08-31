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

(def dummy-data
  [
   {:execution-time #inst "2013-08-28T00:38:12.455-00:00" :available-bikes 3}
   {:execution-time #inst "2013-08-28T00:58:12.455-00:00" :available-bikes 12}
   {:execution-time #inst "2013-08-28T01:58:12.455-00:00" :available-bikes 9}
   {:execution-time #inst "2013-08-28T02:18:12.455-00:00" :available-bikes 4}
   {:execution-time #inst "2013-08-28T02:38:12.455-00:00" :available-bikes 15}
   {:execution-time #inst "2013-08-28T02:58:12.455-00:00" :available-bikes 16}
   {:execution-time #inst "2013-08-28T03:01:32.455-00:00" :available-bikes 12}]
  )

(defn router [db-spec]
  (w/make-router
   (w/string-matcher "/stations.html"
                     (fn [req] (html-page
                               (stations/stations-html-page
                                (storage/current-stations db-spec)))))
   (w/regex-matcher #"/station/(\d+)\.html"
                 (fn [req station-id]
                   (let [station-info (storage/station-info
                                       db-spec
                                       (read-string station-id))]
                     (html-page
                      (stations/station-html-page station-id
                                                  (:station-name station-info))))))
   (w/regex-matcher #"/available_bikes/(\d+)\.edn"
                    (fn [req station-id]
                      {:status 200
                       :headers {"Content-Type" "application/edn"}
                       :body (print-str dummy-data)}))


   ))

(defn handler [db-spec]
  (-> (params/wrap-params (router db-spec))
      (file/wrap-file "resources/public")
      (file-info/wrap-file-info)))

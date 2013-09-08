(ns org.gensym.viddy.web.webcore
  (:require
   [org.gensym.viddy.storage :as storage]
   [org.gensym.viddy.divvy-data :as data]
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
  [{:execution-time #inst "2013-08-28T00:38:12.455-00:00"
    :station-name "Fake St. & Bogus Ave."
    :station-id 23
    :station-status "In Service"}
   {:execution-time #inst "2013-08-27T00:38:12.455-00:00"
    :station-name "Nonexistant Boulevard. & Delirious Drive"
    :station-id 23
    :station-status "Not In Service"}
   {:execution-time #inst "2013-08-28T00:38:12.455-00:00"
    :station-name "Fake St. & Bogus Ave."
    :station-id 23
    :station-status "In Service"}
   {:execution-time #inst "2013-08-28T00:38:12.455-00:00"
    :station-name "Fake St. & Bogus Ave."
    :station-id 23
    :station-status "In Service"}
   {:execution-time #inst "2013-08-28T00:38:12.455-00:00"
    :station-name "Fake St. & Bogus Ave."
    :station-id 23
    :station-status "In Service"}
   {:execution-time #inst "2013-08-28T00:38:12.455-00:00"
    :station-name "Fake St. & Bogus Ave."
    :station-id 23
    :station-status "In Service"}
   {:execution-time #inst "2013-08-28T00:38:12.455-00:00"
    :station-name "Fake St. & Bogus Ave."
    :station-id 23
    :station-status "In Service"}
   {:execution-time #inst "2013-08-28T00:38:12.455-00:00"
    :station-name "Fake St. & Bogus Ave."
    :station-id 23
    :station-status "In Service"}
   {:execution-time #inst "2013-08-28T00:38:12.455-00:00"
    :station-name "Fake St. & Bogus Ave."
    :station-id 23
    :station-status "In Service"}])

(defn router [divvy-source]
  (w/make-router

   (w/strings-matcher ["/index.html" "/"]
                      (fn [req] (html-page
                                (stations/index-html-page dummy-data))))

   (w/string-matcher "/stations.html"
                     (fn [req] (html-page
                               (stations/stations-html-page
                                (data/current-stations divvy-source)))))
   (w/regex-matcher #"/station/(\d+)\.html"
                 (fn [req station-id]
                   (let [station-info (data/station-info
                                       divvy-source
                                       (read-string station-id))]
                     (html-page
                      (stations/station-html-page station-id
                                                  (:station-name station-info))))))
   (w/regex-matcher #"/available_bikes/(\d+)\.edn"
                    (fn [req station-id]
                      {:status 200
                       :headers {"Content-Type" "application/edn"}
                       :body (print-str
                              (data/available-bikes divvy-source
                                                    (read-string station-id)))}))
  (w/regex-matcher #"/available_docks/(\d+)\.edn"
                    (fn [req station-id]
                      {:status 200
                       :headers {"Content-Type" "application/edn"}
                       :body (print-str
                              (data/available-docks divvy-source
                                                    (read-string station-id)))}))))

(defn handler [db-spec]
  (let [datasource (storage/make-divvy-data db-spec)]
    (-> (params/wrap-params (router datasource))
        (file/wrap-file "resources/public")
        (file-info/wrap-file-info))))

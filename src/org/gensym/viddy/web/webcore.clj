(ns org.gensym.viddy.web.webcore
  (:require
   [org.gensym.viddy.storage :as storage]
   [org.gensym.viddy.queries.divvy-history :as history]
   [org.gensym.viddy.divvy-data :as data]
   [org.gensym.viddy.web.stations :as stations]
   [org.gensym.util.webroutes :as w]
   [ring.middleware.params :as params]
   [ring.middleware.file :as file]
   [ring.middleware.file-info :as file-info]
   [clojure.tools.logging :as log]
   [clj-time.core :as time]
   [clj-time.format :as time-format]))

(def date-time-format "YYYY-MM-dd'T'HH:mm:ssZ")
(def time-formatter (time-format/formatter date-time-format))


(defn- html-page [nodes]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (apply str nodes)})

(defn- date-or-default [date-str-or-nil default-thunk]
  (if (empty? date-str-or-nil)
    (default-thunk)
    (time-format/parse time-formatter date-str-or-nil)))

(defn- request-daterange [req]
  (let [to-date (date-or-default (get (:params req) "to_date") #(time/now))
        from-date (date-or-default (get (:params req) "from_date") #(time/minus- to-date (time/months 1)))]
    {:to (.toDate to-date)
     :from (.toDate from-date)}))

(defn router [divvy-source divvy-cache]
  (w/make-router

   (w/strings-matcher ["/index.html" "/"]
                      (fn [req] (html-page
                                (stations/index-html-page
                                 (data/newest-stations divvy-source)))))

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
                      (let [dates (request-daterange req)]
                        {:status 200
                         :headers {"Content-Type" "application/edn"}
                         :body (pr-str
                                (history/available-bikes
                                 divvy-cache
                                 (read-string station-id)
                                 (:from dates)
                                 (:to dates)))})))

   (w/regex-matcher #"/available_docks/(\d+)\.edn"
                    (fn [req station-id]
                      (let [dates (request-daterange req)]
                        {:status 200
                         :headers {"Content-Type" "application/edn"}
                         :body (print-str
                                (history/available-docks
                                 divvy-cache
                                 (read-string station-id)
                                 (:from dates)
                                 (:to dates)))})))

   (w/regex-matcher #"/available_bikes/weekdays/(\d+)\.edn"
                    (fn [req station-id]
                      (let [dates (request-daterange req)]
                        {:status 200
                         :headers {"Content-Type" "application/edn"}
                         :body  (pr-str
                                 (history/available-bikes-weekdays
                                  divvy-cache
                                  (read-string station-id)
                                  (:from dates)
                                  (:to dates)))})))))

(defn log-request [handler]
  (fn [request]
    (let [start (java.lang.System/currentTimeMillis)
          response (handler request)]
      (log/info "Processing request for" (:uri request) "took" (- (java.lang.System/currentTimeMillis) start) "ms")
      response)))

(defn handler [db-spec]
  (let [datasource (storage/make-divvy-data db-spec)
        datacache (history/make-data-source datasource)]
    (-> (params/wrap-params (router datasource datacache))
        (log-request)
        (file/wrap-file "resources/public")
        (file-info/wrap-file-info))))

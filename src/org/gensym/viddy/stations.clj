(ns org.gensym.viddy.stations
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]
            [clj-time.format :as time-format]))

(defn- clean-json [json-string]
  (reduce (fn [m [k v]] (assoc m k v)) {}
          (json/read-str json-string)))

(defn- divvy-string->datetime [divvy-string]
  ;; 2013-08-08 11:16:01 PM
  (time-format/parse (time-format/formatter "yyyy-MM-dd hh:mm:ss a") divvy-string))

(defn- from-divvy-api [cleaned-json]
  (let [stations (get cleaned-json "stationBeanList")
        execution-time (divvy-string->datetime (get cleaned-json "executionTime"))
        keymap {:id "id"
                :bikes "availableBikes"
                :docks "availableDocks"
                :longitude "longitude"
                :latitude "latitude"
                :status "statusValue"
                :name "stationName"
                }]

    (map (fn [station]
           (reduce (fn [m [k v]]
                     (assoc m k (get station v)))
                   {:updateTime execution-time}
                   keymap))
         stations)))

(defn current-station-status []
  (let [val (client/get "http://divvybikes.com/stations/json")
        status (:status val)]
    {:status status
     :stations (if (= 200 status)
                 (from-divvy-api
                  (clean-json (:body val)))
                 {})}))

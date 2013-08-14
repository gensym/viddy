(ns org.gensym.viddy.stations
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]
            [clj-time.format :as time-format]
            [clj-time.core :as time]))

(defn- clean-json [json-string]
  (reduce (fn [m [k v]] (assoc m k v)) {}
          (json/read-str json-string)))

;; 2013-08-08 11:16:01 PM
(def time-formatter (time-format/formatter "yyyy-MM-dd hh:mm:ss a"
                                           (time/default-time-zone)))

(defn- divvy-string->date [divvy-string]
  (->>  divvy-string
        (time-format/parse time-formatter)
        (.toDate)))

(defn- from-divvy-api [cleaned-json]
  (let [stations (get cleaned-json "stationBeanList")
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
                   {}
                   keymap))
         stations)))

(defn current-station-status []
  (let [val (client/get "http://divvybikes.com/stations/json")
        status (:status val)]
    (if (= 200 status)
      (let [cleaned-json (clean-json (:body val))]
        {:status 200
         :stations (from-divvy-api cleaned-json)
         :execution-time (divvy-string->date
                          (get cleaned-json "executionTime")) })
      {})))

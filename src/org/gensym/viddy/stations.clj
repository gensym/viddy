(ns org.gensym.viddy.stations
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]))

(defn clean-json [json-string]
  (reduce (fn [m [k v]] (assoc m k v)) {}
          (json/read-str json-string)))

(defn from-divvy-api [cleaned-json]
  (let [stations (get cleaned-json "stationBeanList")
        keymap {:id "id"
                :bikes "availableBikes"
                :docks "availableDocks"
                :longitude "longitude"
                :latitude "latitude"
                :status "statusValue"
                :name "stationName"}]
    (map (fn [station]
           (reduce (fn [m [k v]]
                     (assoc m k (get station v)))
                   {}
                   keymap))
         stations)))

(defn stuff []
  (let [val (client/get "http://divvybikes.com/stations/json")
        status (:status val)]
    {:status status
     :stations (if (= 200 status)
                 (from-divvy-api
                  (clean-json (:body val)))
                 {})}))

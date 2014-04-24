(ns org.gensym.viddy.debugging
    (use [clj-time.coerce :only [from-date]]
       [clj-time.predicates :only [sunday? saturday?]])
  (:require
   [org.gensym.viddy.queries.date-cache :as c]
   [org.gensym.viddy.frequencies :as freq]
   [org.gensym.viddy.timeslices :as ts]
   [org.gensym.viddy.divvy-data :as d]
   [org.gensym.viddy.queries.date-planner :as dp]
   [org.gensym.viddy.storage :as storage]
   [org.gensym.viddy.queries.divvy-history :as history]
   [clj-time.format :as time-format]
   [clj-time.core :as time]
   [org.gensym.viddy.dbconnection :as dbconn] :reload-all))

(def db-spec (dbconn/db-spec  (System/getenv "DATABASE_URL")))
(def datasource (storage/make-divvy-data db-spec))
(def divvy-cache (history/make-data-source datasource))

(def date-time-format "YYYY-MM-dd'T'HH:mm:ssZ")
(def time-formatter (time-format/formatter date-time-format))

(defn- date-or-default [date-str-or-nil default-thunk]
  (if (empty? date-str-or-nil)
    (default-thunk)
    (time-format/parse time-formatter date-str-or-nil)))

(defn- request-daterange [req]
  (let [to-date (date-or-default (get (:params req) "to_date") #(time/now))
        from-date (date-or-default (get (:params req) "from_date") #(time/minus- to-date (time/months 1)))]
    {:to (.toDate to-date)
     :from (.toDate from-date)}))

(defn available-docks [req station-id]
  (let [dates (request-daterange req)]
    (history/available-docks
     divvy-cache
     (read-string station-id)
     (:from dates)
     (:to dates))))

(defn available-bikes-weekdays [req station-id]
  (let [dates (request-daterange req)]
      (history/available-bikes-weekdays
              divvy-cache
              (read-string station-id)
              (:from dates)
              (:to dates))))

(defn bike-frequencies [req station-id]
  (let [dates (request-daterange req)]
    (c/produce-result (:steps divvy-cache)
                      (:cache divvy-cache)
                      :available-bikes-weekdays
                      (:from dates)
                      (:to dates)
                      [(read-string station-id)])))


(defn- dates [planstep]
  (if (= 3 (count planstep))
    [(get planstep 0) (get planstep 2)]
    [(get planstep 0) (get planstep 1)]))


(defn- produce-value! [cache key start end args]
  (let [[found found-value] (c/maybe-find-for-date-range cache key start end args)]
    (if found
      found-value
      (let [calc (c/calculate-for-date-range cache key start end args)]
        (c/store-calculation cache key start end calc args)
        calc))))


(defn doit2 []
  (let [cache  (:cache divvy-cache)
        plan [[#inst "2014-02-18T02:47:17.512-00:00"  #inst "2014-02-18T03:00:00.000-00:00"]
              [#inst "2014-02-19T00:00:00.000-00:00" [27 [1 :day]] #inst "2014-03-18T00:00:00.000-00:00"]
]
        all-dates (map dates plan)]
    

    (reductions (fn [res [start end]]
              (c/concat-calculation cache
                                    :available-bikes-weekdays
                                    res 
                                    (produce-value! cache :available-bikes-weekdays start end [349])))
            (c/empty-calculation cache  :available-bikes-weekdays)
            all-dates)))

(defn doit []
  (let [cache  (:cache divvy-cache)
        dates [[#inst "2014-02-18T02:47:17.512-00:00"
                #inst "2014-02-18T03:00:00.000-00:00"]
               [#inst "2014-02-18T03:00:00.000-00:00"
                #inst "2014-02-19T00:00:00.000-00:00"]
               [#inst "2014-02-19T00:00:00.000-00:00"
                #inst "2014-03-18T00:00:00.000-00:00"]
               [#inst "2014-03-18T00:00:00.000-00:00"
                #inst "2014-03-18T02:00:00.000-00:00"]
               [#inst "2014-03-18T02:00:00.000-00:00"
                #inst "2014-03-18T02:13:00.000-00:00"]
               [#inst "2014-03-18T02:13:00.000-00:00"
                #inst "2014-03-18T02:43:00.000-00:00"]
               [#inst "2014-03-18T02:43:00.000-00:00"
                #inst "2014-03-18T02:47:17.512-00:00"]]

]

    (map (fn [ [start end]]
           (produce-value! cache :available-bikes-weekdays start end [349]))
         dates)))

(defn weekend? [station-update]
  (let [t (from-date (:execution-time station-update))]
    (or (saturday? t)
        (sunday? t))))

(def weekday? (comp not weekend?))



(defn testit []

  (let [cache (:cache divvy-cache)
        key :available-bikes-weekdays

        start  #inst "2014-02-19T00:00:00.000-00:00"
        end  #inst "2014-03-18T00:00:00.000-00:00"
        args  [349]]


    (->> (d/station-updates datasource 349 start end)
         (filter weekday?)
         (map (fn [rec] [((comp ts/fifteen-minutes :execution-time) rec)
                        (:available-bikes rec)]))
         (map (fn [[k v]] {k (freq/create [v])}))
         (apply merge-with freq/merge-freqs {}))))

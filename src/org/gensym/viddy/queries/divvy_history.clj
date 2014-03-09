(ns org.gensym.viddy.queries.divvy-history
  (require [clojure.set :as set]
           [org.gensym.viddy.divvy-data :as d]
           [org.gensym.viddy.frequencies :as freq]
           [org.gensym.viddy.queries.date-cache :as c]
           [org.gensym.viddy.queries.pass-through-date-cache :as pt])
  (import [org.joda.time DateTimeConstants DateTime DateTimeZone Weeks Days]))

(defn calc-available-bikes [datasource start end [station-id]]
  (d/available-bikes datasource station-id start end))

(defn calc-available-docks [datasource start end [station-id]]
  (d/available-docks datasource station-id start end))

(defn calc-available-bikes-weekdays [datasource start end [station-id]]
  {:start start
   :end end
   :data (d/available-bikes-weekdays-frequencies datasource
                                                 station-id
                                                 start
                                                 end)})

(def weekdays #{DateTimeConstants/MONDAY
                DateTimeConstants/TUESDAY
                DateTimeConstants/WEDNESDAY
                DateTimeConstants/THURSDAY
                DateTimeConstants/FRIDAY})

(defn weekdays-in-range [from to]
  (let [utc (DateTimeZone/forID "UTC")
        d1 (DateTime. from utc)
        d2 (DateTime. to utc)
        weeks (Weeks/weeksBetween d1 d2)
        num-weeks (.getWeeks weeks)
        num-days (inc (.getDays (Days/daysBetween (.plusWeeks d1 num-weeks) d2)))
        days (map #(.plusDays d1 %) (range num-days))
        ret (+ (* 5 num-weeks) (count (filter weekdays  (map #(.getDayOfWeek %) days))))]
    ret))


(defn concat-available-bikes-weekdays [{s1 :start e1 :end d1 :data}
                                       {s2 :start e2 :end d2 :data}]

    (let [dur-1 (weekdays-in-range s1 e1)
          dur-2 (weekdays-in-range s2 e2)]
      {:start s1
       :end e2
       :data (reduce (fn [m k]
                       (assoc m k (freq/merge-freqs (get d1 k) (get d2 k))))
                     {}
                     (set/intersection (into #{} (keys d1))
                                       (into #{} (keys d2))))}))

(defn make-data-source [calc-data-source]
  {:steps [[1 :year]
           [1 :month]
           [1 :day]
           [1 :hour]
           [15 :minute]]
   :cache (-> (pt/empty-datacache)
              (pt/add-function :available-bikes
                               (partial calc-available-bikes calc-data-source)
                               concat
                               [])
              (pt/add-function :available-docks
                               (partial calc-available-docks calc-data-source)
                               concat
                               []))})

(defn available-bikes [ds station-id from-date to-date]
  (c/produce-result (:steps ds)
                    (:cache ds)
                    :available-bikes
                    from-date
                    to-date
                    [station-id]))

(defn available-docks [ds station-id from-date to-date]
  (c/produce-result (:steps ds)
                    (:cache ds)
                    :available-docks
                    from-date
                    to-date
                    [station-id]))

(defn available-bikes-weekdays [ds station-id from-date to-date]
  (d/available-bikes-frequencies->percentiles
   (:data
    (c/produce-result (:steps ds)
                      (:cache ds)
                      :available-bikes-weekdays
                      from-date
                      to-date
                      station-id))
   [0.1 0.15 0.25 0.5 0.75 0.85 0.9]))

(ns org.gensym.viddy.queries.divvy-history
  (require [clojure.set :as set]
           [org.gensym.viddy.divvy-data :as d]
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
   :data
   (d/available-bikes-weekdays station-id start end)})

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
          dur-2 (weekdays-in-range s2 e2)
          total-dur (+ dur-1 dur-2)
          combine-values (fn [a b]
                           (reduce (fn [m k]
                                     (let [v1 (get a k 0)
                                           v2 (get b k 0)]
                                       (assoc m k (/ (+ (* dur-1 v1)
                                                        (* dur-2 v2))
                                                     total-dur))))
                                   {}
                                   (set/union (keys a) (keys b))))]
      {:start s1
       :end e2
       :data (reduce (fn [m k]
                       (assoc m k (combine-values (get d1 k) (get d2 k))))
                     {}
                     (set/intersection (into #{} (keys d1))
                                       (into #{} (keys d2))))})  


)

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
  (:data
   (c/produce-result (:steps ds)
                     (:cache ds)
                     :available-bikes-weekdays
                     from-date
                     to-date
                     station-id)))

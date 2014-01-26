(ns org.gensym.viddy.queries.date-planner
  (require [org.gensym.viddy.queries.time-tree :as tt])
  (:import [org.joda.time
            PeriodType
            DateTime
            DateTimeZone
            Years
            Months
            Weeks
            Days]))

(defn make-time-tree [start-date end-date]
  [start-date [] end-date])

(defn start-date [time-tree]
  (first time-tree))

(defn end-date [time-tree]
  (last time-tree))

(defn left-range [time-tree]
  (if (empty? (second time-tree))
    [(start-date time-tree) (end-date time-tree)]
    [(start-date time-tree) (first (second time-tree))]))

(defn- ^DateTime next-index-year [num-years time]
  (let [utc (DateTimeZone/forID "UTC")
        dt (DateTime. time utc)
        year (.get (.year dt))
        truncated (DateTime. year 1 1 0 0 0 utc)
        offset (mod year num-years)]
    (if (and (zero? offset) (= truncated dt))
      dt
      (.plusYears truncated (- num-years offset)))))

(defn- ^DateTime next-index-month [num-months time]
  (let [utc (DateTimeZone/forID "UTC")
        dt (DateTime. time utc)
        month (.get (.monthOfYear dt))
        year (.get (.year dt))
        truncated (DateTime. year month 1 0 0 0 utc)
        offset (mod (dec month) num-months)]
    (if (and (zero? offset) (= truncated dt))
      dt
      (.plusMonths truncated
                   (min (- 12 (dec month))
                        (- num-months offset))))))

(defn- ^DateTime next-index-day [num-days time]
  (let [utc (DateTimeZone/forID "UTC")
        dt (DateTime. time utc)
        day (.get (.dayOfMonth dt))
        month (.get (.monthOfYear dt))
        year (.get (.year dt))
        truncated (DateTime. year month day 0 0 0 utc)
        offset (mod (dec day) num-days)]
    (if (= truncated dt)
      dt
      (let [maybe-too-far 
            (.plusDays truncated
                       (- num-days offset))]
        (if (> (.get (.monthOfYear maybe-too-far))
               month)
          (.withDayOfMonth maybe-too-far 1)
          maybe-too-far)))))

(defn- ^DateTime next-index-hour [num-hours time]
  (let [utc (DateTimeZone/forID "UTC")
        dt (DateTime. time utc)
        day (.get (.dayOfMonth dt))
        month (.get (.monthOfYear dt))
        year (.get (.year dt))
        hour (.get (.hourOfDay  dt))
        truncated (DateTime. year month day hour 0 0 utc)
        offset (mod hour num-hours)]
    (if (and (zero? offset) (= truncated dt))
      dt
      (.plusHours truncated
                  (min
                   (- 24 hour)
                   (- num-hours offset))))))

(defn- ^DateTime next-index-minute [num-minutes time]
  (let [utc (DateTimeZone/forID "UTC")
        dt (DateTime. time utc)
        day (.get (.dayOfMonth dt))
        month (.get (.monthOfYear dt))
        year (.get (.year dt))
        hour (.get (.hourOfDay  dt))
        minute (.get (.minuteOfHour dt))
        truncated (DateTime. year month day hour minute 0 utc)
        offset (mod hour num-minutes)]
    (if (and (zero? offset) (= truncated dt))
      dt
      (.plusMinutes truncated
                  (min
                   (- 60 minute)
                   (- num-minutes offset))))))

(defn next-index-boundary [time-unit time]
  (let [[t-q t-u] time-unit]
    (case t-u
      :year (.toDate (next-index-year t-q time))
      :month (.toDate (next-index-month t-q time))
      :day (.toDate (next-index-day t-q time))
      :hour (.toDate (next-index-hour t-q time))
      :minute (.toDate (next-index-minute t-q time)))))


(defn- ensure-range-is-positive [quotient start rem-start to]
  (if (< quotient 0)
    [0 [start to]]
    [quotient [rem-start to]]))

(defn- divide-months [divisor ^DateTime from ^DateTime to]
  (let [numerator (.getMonths (Months/monthsBetween from to))
        quotient (int (/ numerator divisor))]
    (ensure-range-is-positive quotient
                              from
                              (.plusMonths from (* quotient divisor))
                              to )))

(defn- divide-days [divisor from to]
  (let [numerator (.getDays (Days/daysBetween from to))
        quotient (int (/ numerator divisor))]
    (ensure-range-is-positive quotient
                              from
                              (.plusDays  from (* quotient divisor))
                              to)))

(defn- divide-weeks [divisor from to]
  (let [numerator (.getWeeks (Weeks/weeksBetween from to))
        quotient (int (/ numerator divisor))]
    (ensure-range-is-positive quotient
                              from
                              (.plusWeeks from (* quotient divisor))
                              to)))

(defn- divide-years [divisor from to]
  (let [numerator (.getYears (Years/yearsBetween from to))
        quotient (int (/ numerator divisor))]
    (ensure-range-is-positive quotient
                              from
                              (.plusYears from  (* quotient divisor))
                              to)))

(defn divide-timestep [[step-quantity step-unit] from to]
  (let [utc (DateTimeZone/forID "UTC")
        fdt (DateTime. from utc)
        tdt (DateTime. to utc)
        [quotient [rem-from rem-to]]
        (case step-unit
          :year (divide-years step-quantity fdt tdt)
          :month (divide-months step-quantity fdt tdt)
          :week (divide-weeks step-quantity fdt tdt)
          :day (divide-days step-quantity fdt tdt))]
    [quotient [(.toDate rem-from) (.toDate rem-to)]]))


(defn make-expander [time-unit]
  (fn [a b]
    (let [start (next-index-boundary time-unit a)
          [quotient [end _]] (divide-timestep time-unit start b)]
      (tt/expansion [quotient time-unit] start end))))

(defn make-planner [time-units]
  (fn [start end]
    (let [start-tree (tt/make-time-tree start end)]
      (->> time-units
           (map make-expander)
           (reduce (fn [time-tree expander]
                     (tt/expand-tree time-tree expander))
                   start-tree)
           (tt/nodes)))))

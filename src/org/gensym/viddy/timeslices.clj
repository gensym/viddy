(ns org.gensym.viddy.timeslices
  (:use [clj-time.coerce :only [from-date]]))

(defn fifteen-minutes [t]
  (let [ldate (.toLocalTime (from-date t))
        increments
        (int
         (/ 
          (+ (.getMinuteOfHour ldate)
             (* 60 (.getHourOfDay ldate)))
          15))]
    (str (int (/ increments 4))
         ":"
         (get ["00" "15" "30" "45"]
              (mod increments 4)))))

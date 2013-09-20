(ns org.gensym.viddy.statistics)

(defn percentile [percentile coll]
  "Percentile should be a number between 1 and zero"
  (nth coll (max 0 (dec
                    (int (Math/ceil (* percentile (count coll)))))))
)

(defn percentiles [keys coll]
  (let [coll (vec coll)]
    (reduce
     (fn [m v]
       (assoc m v (percentile v coll)))
     {} keys)))


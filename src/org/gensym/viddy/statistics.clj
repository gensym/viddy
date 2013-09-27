(ns org.gensym.viddy.statistics)

(defn percentile [percentile coll]
  "Percentile should be a number between 1 and zero"
  (let [c (count coll)]
    (if (= 0 c)
      0
      (nth coll (max 0 (dec
                        (int (Math/ceil (* percentile c)))))))))

(defn percentiles [keys coll]
  (let [coll (vec coll)]
    (reduce
     (fn [m v]
       (assoc m v (percentile v coll)))
     {} keys)))

(defn analyse-percentages [group-by-fn value-fn percentile-keys coll]
  (->> coll
       (group-by group-by-fn)
       (map (fn [[k v]] [k (percentiles percentile-keys
                                       (sort (map value-fn v)))]))
       (into {})))

(defn rotate-keys [m]
  (apply merge-with merge
         (map (fn [[k v]]
                (into {}
                      (map (fn [[kk vv]]
                             [kk {k vv}]) v)))
              m)))


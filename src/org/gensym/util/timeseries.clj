(ns org.gensym.util.timeseries)

(defn filter-redundant [ignore-keys coll]
  (reverse
   (reduce
    (fn [m v]
      (if (= (apply dissoc (first m) ignore-keys)
             (apply dissoc v ignore-keys))
        m
        (conj m v)))
    '() coll)))

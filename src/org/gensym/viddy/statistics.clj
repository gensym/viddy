(ns org.gensym.viddy.statistics
  (:require [org.gensym.viddy.frequencies :as freq]))

(defn analyse-percentages [group-by-fn value-fn percentile-keys coll]
  (->> coll
       (group-by group-by-fn)
       (map (fn [[k v]] [k (freq/percentiles percentile-keys
                                            (freq/create
                                             (sort (map value-fn v))))]))
       (into {})))

(defn rotate-keys [m]
  (apply merge-with merge
         (map (fn [[k v]]
                (into {}
                      (map (fn [[kk vv]]
                             [kk {k vv}]) v)))
              m)))

(defn rename-keys [f m]
  (into {} (map (fn [[k v]] [(f k) v]) m)))


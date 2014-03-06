(ns org.gensym.viddy.statistics
  (:require [org.gensym.viddy.frequencies :as freq]))

(defn rotate-keys [m]
  (apply merge-with merge
         (map (fn [[k v]]
                (into {}
                      (map (fn [[kk vv]]
                             [kk {k vv}]) v)))
              m)))

(defn rename-keys [f m]
  (into {} (map (fn [[k v]] [(f k) v]) m)))


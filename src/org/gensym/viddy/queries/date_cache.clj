(ns org.gensym.viddy.queries.date-cache
  (require [org.gensym.viddy.queries.date-planner :as dp]))

(defprotocol DateCache
  (maybe-find-for-date-range [datasource key start end args])
  (calculate-for-date-range [datasource key start end args])
  (store-calculation [datasource key start end calculation args])
  (concat-calculation [datasource key calc-1 calc-2])
  (empty-calculation [datasource key]))

(defn- dates [planstep]
  (if (= 3 (count planstep))
    [(get planstep 0) (get planstep 2)]
    [(get planstep 0) (get planstep 1)]))

(defn- produce-value! [cache key start end args]
  (let [[found found-value] (maybe-find-for-date-range cache key start end args)]
    (if found
      found-value
      (let [calc (calculate-for-date-range cache key start end args)]
        (store-calculation cache key start end calc args)
        calc))))

(defn- calc-plan [plan cache key args]
  (let [all-dates (map dates plan)]
    (reduce (fn [res [start end]]
              (concat-calculation cache
                                  key
                                  res 
                                  (produce-value! cache key start end args)))
            (empty-calculation cache key)
            all-dates)))

(defn produce-result [steps cache key start end args]
  (let [planner (dp/make-planner steps)]
    (calc-plan (planner start end) cache key args)))

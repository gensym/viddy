(ns org.gensym.viddy.queries.in-memory-date-cache
  (require [org.gensym.viddy.queries.date-cache :as dc]))

(defn make-datacache [calculation-fns]
  (reify dc/DateCache

    (maybe-find-for-date-range [this key start end args]
      (if (contains? this [key start end])
        [true (get this [key start end])]
        [false nil]))

    (calculate-for-date-range [this key start end args]
      ((get calculation-fns key) start end))


    (store-calculation [this key start end calculation args]
      (assoc this [key start end] calculation))

    (concat-calculation [this key a b]
      (calculation-fns key) a b)

    (empty-calculation [datasource key]
      )))

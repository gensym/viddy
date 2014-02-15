(ns org.gensym.viddy.queries.pass-through-date-cache
  (require [org.gensym.viddy.queries.date-cache :as dc]))

(defn empty-datacache []
  (let [t (fn [k] (throw (Exception. (str "function not found: " k))))]
    (reify dc/DateCache
      (maybe-find-for-date-range [cache k start end args]
        (t k))
      (calculate-for-date-range [cache k start end args]
        (t k))
      (store-calculation [cache k start end c args]
        (t k))
      (concat-calculation [cache k c1 c2]
        (t k))
      (empty-calculation [cache k]
        (t k)))))


(defn add-function [cache key calc-fn concat-fn empty-val]
  (reify dc/DateCache
    (maybe-find-for-date-range [this k start end args]
      (if (= key k)
        [false nil]
        (dc/maybe-find-for-date-range cache k start end args)))

    (calculate-for-date-range [this k start end args]
      (if (= key k)
        (calc-fn start end args)
        (dc/calculate-for-date-range cache k start end args)))

    (store-calculation [this k start end calculation args]
      (if (= key k)
        nil
        (dc/store-calculation range k start end calculation args)))

    (concat-calculation [this k c1 c2]
      (if (= key k)
        (concat-fn c1 c2)
        (dc/concat-calculation cache k c1 c2)))

    (empty-calculation [this k]
      (if (= key k)
        empty-val
        (dc/empty-calculation cache k)))))

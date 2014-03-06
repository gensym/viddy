(ns org.gensym.viddy.frequencies)

(defn- ordered-frequencies [ordered-seq]
  (reverse
   (reduce (fn [m v]
             (cond (nil? (first m)) (cons [v 1] m)
                   (= v (ffirst m)) (cons [v (inc (second (first m)))] (rest m))
                   :else (cons [v 1] m))) '() ordered-seq)))

(defn create [ordered-input-seq]
  {:count (count ordered-input-seq)
   :items (ordered-frequencies ordered-input-seq)})

(defn to-seq [ordered-frequencies]
  (mapcat (fn [[val count]] (repeat count val)) (:items ordered-frequencies)))

(defn- merge-freq-seqs [a b]
  (loop [res ()
         a a
         b b]
    (cond (empty? a) (concat (reverse res) b)
          (empty? b) (concat (reverse res) a)
          (> (ffirst a) (ffirst b)) (recur (cons (first b) res) a (rest b))
          (< (ffirst a) (ffirst b)) (recur (cons (first a) res) (rest a) b)
          (= (ffirst a) (ffirst b)) (let [v (ffirst a)
                                          c (+ (second (first a))
                                               (second (first b)))]
                                      (recur (cons [v c] res)
                                             (rest a)
                                             (rest b))) )))

(defn merge-freqs [a b]
  {:count (+ (:count a) (:count b))
   :items (merge-freq-seqs (:items a) (:items b))})


(defn percentiles [params freq-seqs]
  (let [v (into [] (to-seq freq-seqs))]
    (->> params
         (map (fn [x] (dec (int (Math/ceil (* x (:count freq-seqs)))))))
         (map vector params)
         (map (fn [[a b]] [a (get v b 0)]))
         (reduce (fn [m [a b]] (assoc m a b)) {}))))

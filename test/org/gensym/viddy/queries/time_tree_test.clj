(ns org.gensym.viddy.queries.time-tree-test
  (require [org.gensym.viddy.queries.time-tree :as tt])
  (:use clojure.test))

(defn primer [x]
  (fn [a b]
    (let [s (if (zero? (mod a x)) a (+ a (- x (rem a x))))
          q (quot (- b s) x)
          e (+ s (* q x))]
      (tt/expansion [x q] s e))))

(deftest time-tree-tests
  (testing "single expansion"
    (is (= (list [5 [5 2] 15])
           (->
            (tt/make-time-tree 5 15)
            (tt/expand-tree (primer 5))
            (tt/nodes)))))

  (testing "single expansion with start remainder"
    (is (= (list [3 5] [5 [5 2] 15])
           (-> (tt/make-time-tree 3 15)
               (tt/expand-tree (primer 5))
               (tt/nodes)))))

  (testing "single expansion with end remainder"
    (is (= (list [5 [5 2] 15] [15 17])
           (-> (tt/make-time-tree 5 17)
               (tt/expand-tree (primer 5))
               (tt/nodes)))))

  (testing "double expansion"
    (is (= (list [1 5] [5 [5 2] 15] [15 [3 1] 18])
           (-> (tt/make-time-tree 1 18)
               (tt/expand-tree (primer 5))
               (tt/expand-tree (primer 3))
               (tt/nodes)))))

  (testing "no expansion"
    (is (= (list [1 18])
           (-> (tt/make-time-tree 1 18)
               (tt/nodes))))))



(ns org.gensym.viddy.queries.time-tree
  (require [clojure.zip :as z]))


(defn- -make-node [node children]
  (assoc node :children children))

(defn- unexpanded-node [start end]
  (if (neg? (compare start end))
    {:start start :end end}
    nil))

(defn- expanded? [node]
  (:label node))

(defn- expanded-node [node expansion]
  (if (neg? (compare (:start expansion) (:end expansion)))
    (assoc expansion
      :children [(unexpanded-node (:start node) (:start expansion))
                 (unexpanded-node (:end expansion) (:end node))])
    node))

(defn- strip-node [node]
  (if (expanded? node)
    [(:start node) (:label node) (:end node)]
    [(:start node) (:end node)]))



(defn- expand-location [loc f]
  (z/edit
   loc
   (fn [node] (expanded-node node (f (:start node) (:end node))))))

(defn- make-zipper [tree]
  (z/zipper expanded?
            :children
            -make-node
            tree))

;; Begin public methods

(defn make-time-tree [from to]
  {:start from
   :end to})


(defn expansion [label start end]
  {:label label
   :start start
   :end end})

(defn expand-tree [tree f]
  "f should return the result of calling expansion"
  (loop [loc (make-zipper tree)]
    (cond (z/end? loc)             (z/root loc)
          (nil? (z/node loc)) (recur (z/next loc))
          (expanded? (z/node loc)) (recur (z/next loc))
          :else                    (recur (z/next (expand-location loc f))))))

(defn nodes [tree]
  "Return a seq of all the trees nodes, in pre-order. Expanded nodes will be of the form [start label end], while unexpanded nodes will be of the form [start end]"
  (map strip-node
   (if (expanded? tree)
     (loop [down (list tree) ;; Only expanded nodes are allowed on this
            up () ;; Only expanded nodes are allowed on this
            ret []]
       (if (empty? down)
         (if (empty? up)
           ret
           (let [curr (first up)
                 right (second (:children curr))]
             (cond (nil? right) (recur down
                                       (rest up)
                                       (conj ret curr))
                   (expanded? right) (recur (cons right down)
                                            (rest up)
                                            (conj ret curr))
                   :else (recur down
                                (rest up)
                                (conj ret curr right)))))
         (let [curr (first down)
               left (first (:children curr))]
           (cond (nil? left) (recur (rest down)
                                    (cons curr up)
                                    ret)
                 (expanded? left) (recur (cons left (rest down))
                                         (cons curr up)
                                         ret)
                 :else (recur (rest down)
                              (cons curr up)
                              (conj ret left))))))
     [tree])))

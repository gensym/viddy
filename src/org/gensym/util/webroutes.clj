(ns org.gensym.util.webroutes)

(defn string-matcher [s f]
  "f takes no params"
  (fn [req]
    (if (= s (:uri req))
      [(f req)]
      nil)))

(defn strings-matcher [string-cool f]
  "f takes no params"
  (let [s (into #{} string-cool)]
    (fn [req]
      (if (s (:uri req))
        [(f req)]
        nil))))

(defn regex-matcher [re f]
  (fn [req]
    (if-let [matches (re-matches re (:uri req))]
      [(apply f req (rest matches))]
      nil)))

(defn make-router [& matchers]
  (fn [req]
    (loop [curr (first matchers)
           matchers (rest matchers)]
      (if (nil? curr)
        {:status 404
         :headers {"Content-Type" "text/html"}
         :body "Oops"}
        (if-let [res (curr req)]
          (first res)
          (recur (first matchers)
                 (rest matchers)))))))

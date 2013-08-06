(ns org.gensym.sample.webcore
  (:require
   [ring.middleware.params :as params]
   [ring.middleware.file :as file]
   [ring.middleware.file-info :as file-info]))


(defn router [req]
  (condp = (:uri req)
    "/dynamic" {:status 200
                :headers {"Content-Type" "text/html"}
                :body (str "Hello, there. This is dynamically generated:" (+ 2 3))}
    {:status 404
     :headers {"Content-Type" "text/html"}
     :body "Oops"}))


(defn handler []
  (->
   (params/wrap-params router)
   (file/wrap-file "resources/public")
   (file-info/wrap-file-info)))

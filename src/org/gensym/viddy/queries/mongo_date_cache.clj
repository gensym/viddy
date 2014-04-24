(ns org.gensym.viddy.queries.mongo-date-cache
  (:require [monger.core :as mg])
  (:import [com.mongodb MongoOptions ServerAddress]))

(def host "localhost")

(def port 7878)

(defn connect []
  (mg/connect {:host host :port port}))


(ns org.gensym.viddy.queries.mongo-date-cache
  (:require [monger.core :as mg]
            [monger.multi.collection :as coll])
  (:import [com.mongodb MongoOptions ServerAddress]))

(def host "localhost")

(def dbname "local")

(def port 27017)

(defn connect []
  (mg/connect {:host host :port port}))



(defn insertion [db]
  (coll/insert-and-return db "documents" {:name "John" :age 23 :hobbies ["fishing" "fission"]}))


(defn doit [conn]
  (insertion (mg/get-db conn dbname)))

(defn findit [conn]
  (coll/find (mg/get-db conn dbname) "documents" {:name "John"}))

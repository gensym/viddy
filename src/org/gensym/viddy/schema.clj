(ns org.gensym.viddy.schema
  (:require [clojure.java.jdbc :as sql]))


(def db-spec {:connection-uri "jdbc:postgresql://localhost:5432/viddy"})

(defn query []
  (sql/query db-spec ["SELECT Name FROM Test WHERE Id=?", 1]))

(defn make-table []
  (sql/db-do-commands db-spec "CREATE TABLE Test2 (Id int, Addy varchar(255))"))


(defn install-db [url-string]
)

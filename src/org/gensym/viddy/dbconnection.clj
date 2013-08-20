(ns org.gensym.viddy.dbconnection)

(defn db-spec [uristring]
  (let [uri (java.net.URI. uristring)
        without-auth     {:subprotocol "postgresql"
                          :subname (str "//" (.getHost uri) ":"
                                        (.getPort uri)
                                        (.getPath uri))}]
    (if-let [user-info (.getUserInfo uri)]
      (let [[user password] (clojure.string/split user-info #":")]
        (merge  without-auth
                {:user user
                 :password password}))
      without-auth)))


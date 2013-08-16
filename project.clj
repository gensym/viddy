(defproject skelweb "0.1.0-SNAPSHOT"
  :min-lein-version "2.0.0"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [ring/ring-core "1.2.0"]
                 [ring/ring-jetty-adapter "1.2.0"]
                 [org.eclipse.jetty/jetty-servlet "7.4.2.v20110526"]
                 [enlive "1.1.1"]
                 [clj-http "0.7.6"]
                 [clj-time "0.5.1"]
                 [org.clojure/data.json "0.2.2"]
                 [org.clojure/java.jdbc "0.3.0-alpha4"]
                 [postgresql "9.1-901.jdbc4"]]
  :main org.gensym.viddy.run)

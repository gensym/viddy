(defproject skelweb "0.1.0-SNAPSHOT"
  :min-lein-version "2.0.0"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/algo.generic "0.1.1"]
                 [org.clojure/tools.logging "0.2.6"]
                 [org.clojure/core.memoize "0.5.6"]
                 [ring/ring-core "1.2.0"]
                 [ring/ring-jetty-adapter "1.2.0"]
                 [org.eclipse.jetty/jetty-servlet "7.4.2.v20110526"]
                 [enlive "1.1.1"]
                 [hiccup "1.0.4"]
                 [clj-http "0.7.6"]
                 [clj-time "0.6.0"]
                 [org.clojure/data.json "0.2.2"]
                 [org.clojure/java.jdbc "0.3.0-alpha4"]
                 [postgresql "9.1-901.jdbc4"]
                 [com.novemberain/monger "1.7.0"]]
  :warn-on-reflection false
  :jvm-opts ["-XX:+TieredCompilation" "-XX:TieredStopAtLevel=1" "-Xmx384m" "-Xss512k"]
  :java-agents [[com.newrelic.agent.java/newrelic-agent "2.21.3"]]
  :aliases { "webrun"
             ["trampoline" "run" "-m" "org.gensym.viddy.webrun"]
             "workerrun"
             ["trampoline" "run" "-m" "org.gensym.viddy.workerrun"]
             "benchmark"
             ["trampoline" "run" "-m" "org.gensym.viddy.benchmark"]}
  :main org.gensym.viddy.run)

(ns org.gensym.viddy.queries.date-cache-test
  (require [org.gensym.viddy.queries.date-cache :as dc]
           [org.gensym.viddy.queries.pass-through-date-cache :as pt])
  (use clojure.test))

(deftest date-cache-tests
  (testing "should run with pass-through cache"
    (let [expected [#inst "2013-01-17T00:00:00.000-00:00"
                    #inst "2013-02-01T00:00:00.000-00:00"
                    #inst "2014-01-01T00:00:00.000-00:00"
                    #inst "2015-01-01T00:00:00.000-00:00"
                    #inst "2015-04-01T00:00:00.000-00:00"
                    #inst "2015-04-29T00:00:00.000-00:00"
                    #inst "2015-04-29T08:00:00.000-00:00"
                    #inst "2015-04-29T08:07:00.000-00:00"
                    #inst "2015-04-29T08:52:00.000-00:00"]
          steps [[1 :year] [1 :month] [1 :day] [1 :hour] [15 :minute]]
          cache (-> (pt/empty-datacache)
                    (pt/add-function :test (fn [a b] [a]) concat []))]
      (is (= expected
             (dc/produce-result
              steps
              cache
              :test
              #inst "2013-01-17"
              #inst "2015-04-29T08:57:00"))))))

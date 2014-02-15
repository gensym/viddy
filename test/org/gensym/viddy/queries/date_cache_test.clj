(ns org.gensym.viddy.queries.date-cache-test
  (require [org.gensym.viddy.queries.date-cache :as dc]
           [org.gensym.viddy.queries.pass-through-date-cache :as pt])
  (use clojure.test))

(deftest date-cache-tests
  (testing "should run with pass-through cache"
    (let [expected ["Wed Jan 16 18:00:00 CST 2013 - Thu Jan 31 18:00:00 CST 2013"
                    "Thu Jan 31 18:00:00 CST 2013 - Tue Dec 31 18:00:00 CST 2013"
                    "Tue Dec 31 18:00:00 CST 2013 - Wed Dec 31 18:00:00 CST 2014"
                    "Wed Dec 31 18:00:00 CST 2014 - Tue Mar 31 19:00:00 CDT 2015"
                    "Tue Mar 31 19:00:00 CDT 2015 - Tue Apr 28 19:00:00 CDT 2015"
                    "Tue Apr 28 19:00:00 CDT 2015 - Wed Apr 29 03:00:00 CDT 2015"
                    "Wed Apr 29 03:00:00 CDT 2015 - Wed Apr 29 03:07:00 CDT 2015"
                    "Wed Apr 29 03:07:00 CDT 2015 - Wed Apr 29 03:52:00 CDT 2015"
                    "Wed Apr 29 03:52:00 CDT 2015 - Wed Apr 29 03:57:00 CDT 2015"]         
          steps [[1 :year] [1 :month] [1 :day] [1 :hour] [15 :minute]]
          cache (-> (pt/empty-datacache)
                    (pt/add-function :test
                                     (fn [a b [s]] [(str a s b)])
                                     concat
                                     []))]
      (is (= expected
             (dc/produce-result
              steps
              cache
              :test
              #inst "2013-01-17"
              #inst "2015-04-29T08:57:00"
              [" - "]))))))



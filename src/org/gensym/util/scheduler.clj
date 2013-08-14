(ns org.gensym.util.scheduler
  (:import [java.util.concurrent ScheduledThreadPoolExecutor TimeUnit]))

(defn make-scheduler! [f initial-delay period-milliseconds]
    "Periodically run a function. The function should execute quickly, since it will block the timer thread until it completes"
    (let [pool  (atom (ScheduledThreadPoolExecutor. 1))]
      (.scheduleAtFixedRate @pool
                            f initial-delay
                            period-milliseconds
                            TimeUnit/MILLISECONDS)
      pool))

(defn shutdown! [scheduler]
  (swap! scheduler (fn [s] (when s (.shutdown s)))))

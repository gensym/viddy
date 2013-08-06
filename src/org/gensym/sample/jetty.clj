(ns org.gensym.sample.jetty
  (:import (org.eclipse.jetty.server.handler AbstractHandler)
           (org.eclipse.jetty.server Server Request Response)
           (org.eclipse.jetty.server.nio SelectChannelConnector)
           (org.eclipse.jetty.servlet ServletContextHandler)
           (javax.servlet.http HttpServletRequest HttpServletResponse))
  (:require [ring.util.servlet :as servlet]))

(defn- proxy-http-handler [handler]
  (proxy [AbstractHandler] []
    (handle [target ^Request request http-request http-response]
      (let [request-map (servlet/build-request-map request)
            response-map (handler request-map)]
        (when response-map
          (servlet/update-servlet-response http-response response-map)
          (.setHandled request true))))))


(defn- create-server [port]
  (let [connector (doto (SelectChannelConnector.)
                    (.setPort port))
        server (doto (Server.)
                 (.addConnector connector)
                 (.setSendDateHeader true))]
    server))

(defn ^Server make-jetty-server [handler port]
  (let [^Server s (create-server port)]
    (.setHandler s  (proxy-http-handler handler))
    s))

(ns org.gensym.viddy.web.stations
  (:require  [net.cgrand.enlive-html :as html]))

(def station-row-selector [:table.stations-list :> :tbody :> [:tr (html/nth-of-type 1)]])

(html/defsnippet stations-table "templates/stations.html" station-row-selector
  [station]
  [:td.station-id] (html/content (str (:station-id station)))
  [:td.station-name] (html/content (:station-name station))
  [:td.station-status] (html/content (:station-status station))
  [:td.station-available-bikes-quantity] (html/content
                                          (str (:available-bikes station)))
  [:td.station-available-docks-quantity] (html/content
                                          (str (:available-docks station))))

(html/deftemplate html-page "templates/stations.html" [stations-fn req]
  [:table.stations-list :> :tbody] (html/content
                                    (map #(stations-table %) (stations-fn))))

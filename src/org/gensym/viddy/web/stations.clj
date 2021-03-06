(ns org.gensym.viddy.web.stations
  (:require  [net.cgrand.enlive-html :as enl]
             [hiccup.core :as hcp]))

(def station-row-selector
  [:table.stations-list :> :tbody :> [:tr (enl/nth-of-type 1)]])

(enl/defsnippet stations-table "templates/stations.html" station-row-selector
  [station]
  [:td.station-id] (enl/content (str (:station-id station)))
  [:td.station-name :> :a.station-link] (enl/do->
                                         (enl/content (:station-name station))
                                         (enl/set-attr :href
                                                       (str "/station/"
                                                            (:station-id station) ".html")))
  [:td.station-status] (enl/content (:station-status station))
  [:td.station-available-bikes-quantity] (enl/content
                                          (str (:available-bikes station)))
  [:td.station-available-docks-quantity] (enl/content
                                          (str (:available-docks station)))
  [:td.addition-time] (enl/content
                       (str (:addition-time station))))

(enl/defsnippet station-additions-table
  "templates/index.html"
  [:.recently-added-stations :> :table :> :tbody :> [:tr (enl/nth-of-type 1)]]
  [station]
  
  [:td.addition-time] (enl/content (str (:addition-time station)))
  [:td.station-name :> :a.station-link] (enl/do->
                                         (enl/content (:station-name station))
                                         (enl/set-attr :href
                                                       (str "/station/"
                                                            (:station-id station) ".html")))
  [:td.station-status] (enl/content (:station-status station)))

(enl/deftemplate stations-html-page "templates/stations.html" [stations]
  [:table.stations-list :> :tbody] (enl/content
                                    (map #(stations-table %) stations)))

(enl/deftemplate station-html-page "templates/station.html"
  [station-id station-name]
  [:title] (enl/content station-name)
  [:a#station-header] (enl/do->
                       (enl/content station-name)
                       (enl/set-attr :data-station-id station-id)))

(enl/deftemplate index-html-page "templates/index.html"
  [stations]
  [:.recently-added-stations :> :table :> :tbody]
  (enl/content  (map #(station-additions-table %) stations)))

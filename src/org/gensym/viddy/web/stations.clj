(ns org.gensym.viddy.web.stations
  (:require  [net.cgrand.enlive-html :as html]))

(def  dummy-content
  [{:station-id 16
    :station-name "Marshfield Ave & North Ave"
    :available-bikes 6
    :available-docks 9
    :station-status "In Service"}
   {:station-id 17
    :station-name "Wood St & Division St"
    :available-bikes   7
    :available-docks 8
    :station-status "In Service"}
   {:station-id 19
    :station-name "Loomis St & Taylor St"
    :available-bikes  12
    :available-docks 3
    :station-status "In Service"}
   {:station-id 20
    :station-name "Sheffield Ave & Kingsbury St"
    :available-bikes   9
    :available-docks 6
    :station-status "In Service"}])

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

(html/deftemplate html-page "templates/stations.html" [req]
  [:table.stations-list :> :tbody] (html/content
                                    (map #(stations-table %) dummy-content)))

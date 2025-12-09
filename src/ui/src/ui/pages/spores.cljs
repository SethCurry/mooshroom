(ns ui.pages.spores
  (:require [ui.api-client :as api-client]
            [cljs.core.async :refer [<!]]
            [reagent.core :as r]
            [ui.components.table :as table]
            [ui.components.chart :as chart]
            [reagent.session :as session]
            [taoensso.telemere :as t]
            [ui.components.div :as div]
            [ui.theme :as theme])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn spores-list []
  (println theme/palette)
  (let [spores (r/atom [])]
    (go (reset! spores (<! (api-client/list-spores))))
    (fn []
      [:div
       [:h1 "Spores"]
       [table/table ["ID" "Name"] (map (fn [spore]
                                         [(:id spore) [:a {:href (str "/spores/" (:id spore))} (:name spore)]])
                                       @spores)]])))

(defn spore-detail []
  (let [routing-data (session/get :route)
        spore-id (get-in routing-data [:route-params :spore-id])
        spore (r/atom {})
        sensor-data (r/atom [])]
    (go (do (reset! spore (<! (api-client/get-spore spore-id)))
            (doall (map (fn [sensor]
                          (go (swap! sensor-data conj {:id (:id sensor) :name (:name sensor) :readings (<! (api-client/get-dht-data (:id sensor)))})))
                        (:dht-sensors @spore)))))
    (fn []
      (t/log! {:level :debug :msg "Sensor data" :data {:sensor-data @sensor-data}})
      [:div
       [:h1 (str "Spore: " (:name @spore))]
       (when (not (empty? @sensor-data))
         [div/container
          [:h2 {:key "dht-sensors-header" :style {:text-align "center"}} "DHT Sensors"]
          (doall (map (fn [sensor]
                        [:div {:key (:id sensor) :style {:height "30vh" :width "25%" :display "flex" :flex-direction "column" :align-items "center" :justify-content "center"}}
                         [:h3 (:name sensor)]
                         [:div {:style {:height "25vh" :width "100%"}}
                          [chart/climate-chart-component (:id sensor) (:readings sensor)]]])
                      @sensor-data))])])))
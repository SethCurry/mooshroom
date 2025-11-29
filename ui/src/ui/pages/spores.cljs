(ns ui.pages.spores
  (:require [ui.api :as api]
            [cljs.core.async :refer [<!]]
            [reagent.core :as r]
            [ui.components.table :as table]
            [ui.components.chart :as chart]
            [reagent.session :as session])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn spores-list []
  (let [spores (r/atom [])]
    (go (reset! spores (<! (api/list-spores))))
    (fn []
      (println @spores)
      [:div
       [:h1 "Spores"]
       (table/table ["ID" "Name"] (map (fn [spore]
                                         [(:id spore) [:a {:href (str "/spores/" (:id spore))} (:name spore)]])
                                       (:spores @spores)))])))

(defn spore-detail []
  (let [routing-data (session/get :route)
        spore-id (get-in routing-data [:route-params :spore-id])
        spore (r/atom {})
        sensor-data (r/atom [])]
    (go (do (reset! spore (<! (api/get-spore spore-id)))
            (doall (map (fn [sensor] (go (swap! sensor-data concat (:readings (<! (api/get-dht-data (:id sensor)))))))
                                            (:dht_sensors @spore)))))
    (fn []
      (println @sensor-data)
      [:div
       [:h1 (str "Spore: " (:name @spore))]
       (when (not (empty? @sensor-data))
         [:div {:style {:height "50vh" :width "50vw"}}
          [chart/climate-chart-component @sensor-data]])])))
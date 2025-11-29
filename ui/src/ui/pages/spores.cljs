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
                                         [(:id spore) (:name spore)])
                                       (:spores @spores)))])))

(defn spore-detail []
  (fn [] 
  (let [routing-data (session/get :route)
        spore-id (get-in routing-data [:route-params :spore-id])]
    [:div
     [:h1 "Spore Detail"]
     [chart/rev-chartjs-component]])))
(ns ui.pages.taxonomy
  (:require [ui.components.taxonomy.taxonomy-tables :as taxonomy-tables]
            [reagent.session :as session]
            [reagent.core :as r]
            [cljs.core.async :refer [<!]]
            [ui.api-client :as api-client])
  (:require-macros [cljs.core.async.macros :refer [go]]))


(defn genera-list []
  (fn []
    [:div
     [:h1 "Genera"]
     [taxonomy-tables/genera-overview {}]]))

(defn genus-detail []
  (let [routing-data (session/get :route)
        genus-id (get-in routing-data [:route-params :genus-id])
        genus (r/atom {})]
    (go (reset! genus (<! (api-client/get-genus genus-id))))
    (fn []
      [:div
       [:h1 (str "Genus: " (:name @genus))]
       [:h2 "Species"]
       [taxonomy-tables/species-overview {:genus-id genus-id}]])))

(defn species-list []
  (fn []
    [:div
     [:h1 "Species"]
     [taxonomy-tables/species-overview {}]]))


(defn species-detail []
  (let [routing-data (session/get :route)
        species-id (get-in routing-data [:route-params :species-id])
        species (r/atom {})]
    (go (reset! species (<! (api-client/get-species species-id))))
    (fn []
      [:div
       [:h1 (str "Species: " (:name @species))]
       [:p "Genus: " [:a {:href (str "/genera/" (:id (:genus @species)))} (:name (:genus @species))]]
       [:h2 "Common Names"]
       [:ul (map (fn [common-name] [:li common-name]) (:common-names @species))]])))
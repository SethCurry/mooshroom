(ns ui.pages.enclosures
  (:require-macros [cljs.core.async.macros :refer [go]])
  
  (:require [ui.api-client :as api-client]
            [cljs.core.async :refer [<!]]
            [reagent.core :as r]
            [ui.components.table :as table]
            [ui.components.styles :as styles]))


(defn enclosures-list []
  (let [enclosures (r/atom [])]
    (go (reset! enclosures (<! (api-client/list-enclosures))))
    (fn []
      [:div {:style (->> {:gap "1rem"} styles/flex-col)}
       [:h1 "Enclosures"]
       [:a {:href "/enclosures/new" :style (->> {} styles/a-hidden)} [:span {:style (->> {:padding "0.25rem" :border-radius "0.25rem"} styles/medium-brown-bg styles/dark-brown-text)} "New Enclosure"]]
       [table/table ["ID" "Name"] (map (fn [enclosure]
                                         [(:id enclosure) [:a {:href (str "/enclosures/view/" (:id enclosure))} (:name enclosure)]])
                                       @enclosures)]])))

(defn new-enclosure []
  (let [name (r/atom "")]
    (fn []
      [:div {:style (->> {:gap "1rem"} styles/flex-col)}
       [:h1 "New Enclosure"]
       [:input {:type "text" :value @name :on-change #(reset! name (-> % .-target .-value))}]
       [:button {:on-click #(api-client/create-enclosure @name)} "Create"]])))
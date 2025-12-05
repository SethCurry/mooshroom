(ns ui.pages.enclosures
  (:require-macros [cljs.core.async.macros :refer [go]])
  
  (:require [ui.api-client :as api-client]
            [cljs.core.async :refer [<!]]
            [reagent.core :as r]
            [ui.components.table :as table]))


(defn enclosures-list []
  (let [enclosures (r/atom [])]
    (go (reset! enclosures (<! (api-client/list-enclosures))))
    (fn []
      [:div
       [:h1 "Enclosures"]
       [table/table ["ID" "Name"] (map (fn [enclosure]
                                         [(:id enclosure) [:a {:href (str "/spores/" (:id enclosure))} (:name enclosure)]])
                                       @enclosures)]])))
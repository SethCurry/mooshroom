(ns ui.components.taxonomy.taxonomy-tables
  (:require [ui.api-client :as api-client]
            [cljs.core.async :refer [<!]]
            [reagent.core :as r]
            [ui.components.table :as table])
  (:require-macros [cljs.core.async.macros :refer [go]]))


(defn genera-overview [genera-filter]
  (let [genera (r/atom [])]
    (go (reset! genera (<! (api-client/list-genera genera-filter))))
    (fn []
      [table/table ["ID" "Name"] (map (fn [genus]
                                        [(:id genus) [:a {:href (str "/genera/" (:id genus))} (:name genus)]])
                                      @genera)])))


(defn species-overview [species-filter]
  (let [species (r/atom [])]
    (go (reset! species (<! (api-client/list-species species-filter))))
    (fn []
      [table/table ["ID" "Name" "Genus"] (map (fn [sp]
                                                [(:id sp) [:a {:href (str "/species/" (:id sp))} (:name sp)] [:a {:href (str "/genera/" (:id (:genus sp)))} (:name (:genus sp))]])
                                              @species)])))
(ns ui.components.spore.spore-table
  (:require [ui.api-client :as api-client]
            [cljs.core.async :refer [<!]]
            [reagent.core :as r]
            [ui.components.table :as table])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn overview [spore-filter]
  (let [spores (r/atom [])]
    (go (reset! spores (<! (api-client/list-spores spore-filter))))
    (fn []
      [table/table ["ID" "Name"] (map (fn [spore]
                                        [(:id spore) [:a {:href (str "/spores/" (:id spore))} (:name spore)]])
                                      @spores)])))
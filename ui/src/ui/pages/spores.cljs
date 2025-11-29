(ns ui.pages.spores
  (:require [ui.api :as api]
            [cljs.core.async :refer [<!]]
            [reagent.core :as r])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn spores-list []
  (let [spores (r/atom [])]
    (go (reset! spores (<! (api/list-spores))))
    (fn []
      (println @spores)
      [:div
       [:h1 "Spores"]
       [:ul
        (doall (map (fn [spore]
                      [:li {:key (:id spore)} (:name spore)])
                    @spores))]])))

(ns ui.components.div 
  (:require
    [ui.components.styles :as styles]))

(defn vertically-centered [children]
  [:div {:style (->> {:height "100%"}
                     styles/flex-col)}
   [:div {:style {:height "100%" :flex-grow 0 :flex-shrink 1}}]
   children
   [:div {:style {:height "100%" :flex-grow 0 :flex-shrink 1}}]])
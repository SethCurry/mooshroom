(ns ui.layouts
  (:require [ui.components.colors :as colors]))

(defn base [& children]
  [:div {:style {:background-color colors/off-white :width "100%" :height "100%"}}
   [:div {:style {:height "3rem" :width "100%" :background-color colors/dark-brown :color colors/off-white}}
    [:span {:style {:font-size "2rem"}} "Mooshroom"]]
   children])
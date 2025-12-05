(ns ui.layouts
  (:require [ui.components.styles :as styles]
            [ui.components.navbar :as navbar]))

(defn base [& children]
  [:div {:style (->> {:width "100%" :height "100%"}
                     styles/light-brown-bg)}
   [navbar/navbar [["Spores" "/spores"]
                   ["Enclosures" "/enclosures"]]]
   [:div {:style {:padding "1rem"}} children]])
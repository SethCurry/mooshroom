(ns ui.layouts
  (:require [ui.components.styles :as styles]
            [ui.components.navbar :as navbar]))

(defn base [& children]
  [:div {:style (->>{:width "100%" :height "100%"}
                 styles/off-white-bg)}
   [navbar/navbar [["Home" "/"]
                   ["Spores" "/spores"]]]
   children])
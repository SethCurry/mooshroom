(ns ui.components.table
  (:require
   [ui.components.styles :as styles]))

(defn table [columns rows]
  (println "Table data: " rows)
  [:table {:style {:background-color styles/color-dark-brown}}
   [:thead
    [:tr {:style {:background-color styles/color-dark-brown :color styles/color-off-white}}
     (doall (map (fn [column]
                   [:th column])
                 columns))]]
   [:tbody
    (doall (map-indexed (fn [index row]
                          [:tr {:style {:background-color (if (even? index)
                                                            styles/color-medium-brown
                                                            styles/color-light-brown)}}
                           (doall (map (fn [column]
                                         [:td column])
                                       row))])
                        rows))]])
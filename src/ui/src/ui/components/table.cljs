(ns ui.components.table
  (:require
   [ui.components.styles :as styles]
   [reagent.core :as r]))

(defprotocol TableRow
  (get-id [this])
  (row-data [this]))

(defn table [columns rows]
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

(defn table-with-checkboxes [columns rows on-change]
  (let [selected-items (r/atom [])]
    (table (conj columns "Selected") (map #(conj % [:input {:type "checkbox" :on-change (fn [e]
                                                                                          (if (true? e.target.checked)
                                                                                           (swap! selected-items conj (first %))
                                                                                           (swap! selected-items (fn [items rm-item] (filter (fn [item] (not (= item rm-item))) items)) (first %)))
                                                                                          (on-change @selected-items))}]) rows))))
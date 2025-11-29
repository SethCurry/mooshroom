(ns ui.components.table)

(defn table [columns rows]
  [:table
   [:thead
    [:tr
     (doall (map (fn [column]
                   [:th column])
                 columns))]]
   [:tbody
    (doall (map (fn [row]
                  [:tr (doall (map (fn [column]
                                     [:td column])
                                   row))])
                rows))]])
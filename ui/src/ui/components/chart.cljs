(ns ui.components.chart
  (:require ["chart.js/auto" :as chartjs]
            [reagent.core :as r]))

(defn show-revenue-chart
  []
  (let [context (.getContext (.getElementById js/document "rev-chartjs") "2d")
        chart-data {:type "bar"
                    :data {:labels ["2012" "2013" "2014" "2015" "2016"]
                           :datasets [{:data [5 10 15 20 25]
                                       :label "Rev in MM"
                                       :backgroundColor "#90EE90"}
                                      {:data [3 6 9 12 15]
                                       :label "Cost in MM"
                                       :backgroundColor "#F08080"}]}}]
    (chartjs/Chart. context (clj->js chart-data))))

(defn rev-chartjs-component
  []
  (r/create-class
   {:component-did-mount #(show-revenue-chart)
    :display-name        "chartjs-component"
    :reagent-render      (fn []
                           [:canvas {:id "rev-chartjs" :width "700" :height "380"}])}))
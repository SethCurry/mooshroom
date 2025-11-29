(ns ui.components.chart
  (:require ["chart.js/auto" :as chartjs]
            ["chartjs-adapter-luxon" :as luxon-adapter]
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
  [labels datasets]
  (r/create-class
   {:component-did-mount #(show-revenue-chart)
    :display-name        "chartjs-component"
    :reagent-render      (fn []
                           [:canvas {:id "rev-chartjs" :width "700" :height "380"}])}))


(defn show-climate-chart
  [data]
  (let [context (.getContext (.getElementById js/document "climate-chart") "2d")
        chart-data {:type "line"
                    :options {
                              :scales {
                                       :y {
                                           :suggestedMin 0
                                           :suggestedMax 100
                                       }
                                       :x {
                                           :type "time"
                                           :adapters {
                                                      :date luxon-adapter
                                           }
                                       }
                              }
                    }
                    :data {:labels (seq (doall (map #(:timestamp %)
                                                    data)))
                           :datasets [{:data (doall (map #(:temperature %)
                                                         data))
                                       :label "Temperature (C)"
                                       :backgroundColor "#FF0000"}
                                      {:data (doall (map #(:humidity %)
                                                         data))
                                       :label "Humidity (RH %)"
                                       :backgroundColor "#4169E1"}]}}]
    (chartjs/Chart. context (clj->js chart-data))))

(defn climate-chart-component
  [data]
  (r/create-class
   {:component-did-mount #(show-climate-chart data)
    :display-name        "chartjs-climate-component"
    :reagent-render      (fn []
                           [:canvas {:id "climate-chart" :width "100%" :height "100%"}])}))

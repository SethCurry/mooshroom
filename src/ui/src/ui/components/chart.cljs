(ns ui.components.chart
  (:require ["chart.js/auto" :as chartjs]
            ["chartjs-adapter-luxon" :as luxon-adapter]
            [reagent.core :as r]
            [taoensso.telemere :as t]))

(defn show-climate-chart
  [id data]
  (t/log! {:level :debug :msg "Showing climate chart" :data {:id id :data data}})
  (let [context (.getContext (.getElementById js/document (str "climate-chart-" id)) "2d")
        chart-data {:type "line"
                    :options {:scales {:y {:suggestedMin 0
                                           :suggestedMax 100}
                                       :x {:type "time"
                                           :adapters {:date luxon-adapter}}}}
                    :data {:labels (seq (doall (map #(:time %)
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
  [id data]
  (r/create-class
   {:component-did-mount #(show-climate-chart id data)
    :display-name        "chartjs-climate-component"
    :reagent-render      (fn []
                           [:canvas {:id (str "climate-chart-" id) :width "100%" :height "100%"}])}))

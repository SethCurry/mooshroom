(ns ui.button 
  (:require
    [ui.components.styles :as styles]))

(defn link [name to]
  [:a {:href to :style (->> {}
                            styles/a-hidden)}
   [:span {:style (->> {:padding "0.25rem" :border-radius "0.25rem"}
                       styles/light-brown-bg
                       styles/dark-brown-text)} name]])
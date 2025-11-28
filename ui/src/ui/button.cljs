(ns ui.button 
  (:require
    [ui.components.styles :as styles]))

(defn link [name to]
  [:a {:href to :style (->> {}
                            styles/a-hidden)}
   [:button name]])
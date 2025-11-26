(ns ui.components.styles
  (:require [ui.components.colors :as colors]))

(defn light-brown [style]
  (assoc style :background-color colors/light-brown))
(ns ui.brower)

(defn redirect-without-history [url]
  (js/window.location.replace url))

(defn redirect [url]
  (set! js/window.location.href url))
(ns ui.components.styles
  (:require [ui.components.colors :as colors])
  (:require-macros [ui.macros :as macros]))


(macros/defcolor medium-brown "#a28f70")
(macros/defcolor light-brown "#d3bd9a")
(macros/defcolor dark-brown "#674f04")
(macros/defcolor off-white "#f5f0e8")

(defn flex-row [s]
  (assoc s :display "flex" :flex-direction "row"))

(defn flex-col [s]
  (assoc s :display "flex" :flex-direction "column"))

(defn a-hidden [s]
  (assoc s :text-decoration "none" :color "inherit"))

(defn gap-sm [s]
  (assoc s :gap "1rem"))
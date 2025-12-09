(ns ui.theme
  (:require-macros [ui.designer.theming :as theming]))

(theming/deftheme {:primary "#000000" :secondary "#ffffff"} {:font-family "Arial" :font-size "16px" :font-weight 400 :font-style "normal"})
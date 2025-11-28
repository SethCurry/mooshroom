(ns ui.components.navbar
  (:require [ui.components.styles :as styles]
            [ui.components.div :as div]
            [ui.button :as button]))

(defn navbar-link [name link-to]
  (div/vertically-centered
   (button/link name link-to)))

(defn navbar [links]
  [:div {:style (->> {:height "3rem" :width "100%"}
                     styles/dark-brown-bg
                     styles/off-white-text
                     styles/flex-row
                     styles/gap-sm)}
   (div/vertically-centered [:span {:style {:font-size "2rem"}} "Mooshroom"])
   (map #(navbar-link (first %) (second %))
        links)])
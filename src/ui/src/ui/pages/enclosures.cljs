(ns ui.pages.enclosures
  (:require-macros [cljs.core.async.macros :refer [go]])

  (:require [ui.api-client :as api-client]
            [cljs.core.async :refer [<!]]
            [reagent.core :as r]
            [ui.components.table :as table]
            [ui.components.styles :as styles]
            [ui.brower :as brower]
            [taoensso.telemere :as t]
            [reagent.session :as session]))


(defn enclosures-list []
  (let [enclosures (r/atom [])]
    (go (reset! enclosures (<! (api-client/list-enclosures))))
    (fn []
      [:div {:style (->> {:gap "1rem"} styles/flex-col)}
       [:h1 "Enclosures"]
       [:a {:href "/enclosures/new" :style (->> {} styles/a-hidden)} [:span {:style (->> {:padding "0.25rem" :border-radius "0.25rem"} styles/medium-brown-bg styles/dark-brown-text)} "New Enclosure"]]
       [table/table ["ID" "Name"] (map (fn [enclosure]
                                         [(:id enclosure) [:a {:href (str "/enclosures/view/" (:id enclosure))} (:name enclosure)]])
                                       @enclosures)]])))

(defn- on-create-enclosure-submit [name]
  (go (let [response (<! (api-client/create-enclosure name))]
        (if (= (:status response) 200)
          (brower/redirect "/enclosures")
          (t/log! {:level :error :msg "Failed to create enclosure" :data {:response response}})))))

(defn new-enclosure []
  (let [name (r/atom "")]
    (fn []
      [:div {:style (->> {:gap "1rem"} styles/flex-col)}
       [:h1 "New Enclosure"]
       [:input {:type "text" :value @name :on-change #(reset! name (-> % .-target .-value))}]
       [:button {:on-click #(on-create-enclosure-submit @name)} "Create"]])))


(defn enclosure-spores-list [enclosure-id]
  (let [spores (r/atom [])]
    (go (reset! spores (<! (api-client/list-spores {:enclosure-id enclosure-id}))))
    (fn []
      [:div
       [:h2 "Spores"]
       [table/table ["ID" "Name"] (map (fn [spore]
                                         [(:id spore) [:a {:href (str "/spores/" (:id spore))} (:name spore)]])
                                       @spores)]])))

(defn enclosure-detail []
  (let [routing-data (session/get :route)
        enclosure-id (get-in routing-data [:route-params :enclosure-id])
        enclosure (r/atom {})
        spores (r/atom [])]
    (go (reset! enclosure (:body (<! (api-client/get-enclosure enclosure-id)))))
    (go (reset! spores (<! (api-client/list-spores {:enclosure-id enclosure-id}))))
    (fn []
      [:div
       [:h1 (str "Enclosure: " (:name @enclosure))]
       [enclosure-spores-list enclosure-id]])))
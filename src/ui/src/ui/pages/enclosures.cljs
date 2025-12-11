(ns ui.pages.enclosures
  (:require-macros [cljs.core.async.macros :refer [go]])

  (:require
   [cljs.core.async :refer [<!]]
   [reagent.core :as r]
   [reagent.session :as session]
   [taoensso.telemere :as t]
   [ui.api-client :as api-client]
   [ui.brower :as brower]
   [ui.components.spore.spore-table :as spore-table]
   [ui.components.styles :as styles]
   [ui.components.table :as table]))


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

(defn- on-add-spores-submit [enclosure-id spore-ids]
  (doseq [spore-id spore-ids]
    (let [enclosure (js/parseInt enclosure-id)]
    (go (let [response (<! (api-client/update-spore spore-id {:enclosure_id enclosure}))]
          (if (= (:status response) 200)
            (t/log! {:level :debug :msg "Spore added" :data {:spore-id spore-id :enclosure-id enclosure-id}})
            (t/log! {:level :error :msg "Failed to add spores" :data {:response response}})))))))

(defn enclosure-detail []
  (let [routing-data (session/get :route)
        enclosure-id (get-in routing-data [:route-params :enclosure-id])
        enclosure (r/atom {})
        spores (r/atom [])
        selected-spores (r/atom [])]
    (go (reset! enclosure (:body (<! (api-client/get-enclosure enclosure-id)))))
    (go (reset! spores (<! (api-client/list-spores {:enclosure-id enclosure-id}))))
    (fn []
      [:div
       [:h1 (str "Enclosure: " (:name @enclosure))]
       [:h2 "Spores"]
       [(spore-table/overview {:enclosure-id enclosure-id})]
       [:h2 "Add Spores"]
       [(spore-table/selectable-overview {:enclosure-id -1} #(reset! selected-spores %))]
       [:button {:on-click #(on-add-spores-submit enclosure-id @selected-spores)} "Add"]])))
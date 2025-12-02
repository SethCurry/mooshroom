(ns ui.core
  (:require
   [reagent.core :as reagent :refer [atom]]
   [reagent.dom :as rdom]
   [clojure.string :as string]
   [accountant.core :as accountant]
   [reitit.frontend :as reitit]
   [reagent.session :as session]
   [ui.layouts :as layouts]
   [ui.pages.spores :as spores]))

;; -------------------------
;; Views

(defn get-url-hash []
  (-> js/window .-location .-hash))

(defn get-page-keyword []
  (let [url-hash (get-url-hash)
        url-without-hash (string/replace-first url-hash #"#" "")]
    (if (= url-without-hash "")
      :default
      (keyword url-without-hash))))

;; -------------------------
;; Initialize app

(def router
  (reitit/router
   [["/" :index]
    ["/spores"
     ["" :spores]
     ["/:spore-id" :spore-detail]]]))

(defn path-for [route & [params]]
  (if params
    (:path (reitit/match-by-name router route params))
    (:path (reitit/match-by-name router route))))

;; -------------------------
;; Page components

(defn spore-link [id]
  (path-for :spore-detail {:spore-id id}))

(defn home-page []
  (fn []
    [:h1 "Welcome to Mooshroom"]))

;; -------------------------
;; Translate routes -> page components

(defn page-for [route]
  (case route
    :index #'home-page
    :spores #(spores/spores-list)
    :spore-detail #(spores/spore-detail)))


;; -------------------------
;; Page mounting component

(defn current-page []
  (fn []
    (let [page (:current-page (session/get :route))]
      (layouts/base
       [page]))))

;; -------------------------
;; Initialize app

(defn mount-root []
  (rdom/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (accountant/configure-navigation!
   {:nav-handler
    (fn [path]
      (let [match (reitit/match-by-path router path)
            current-page (:name (:data  match))
            route-params (:path-params match)]
        (session/put! :route {:current-page (page-for current-page)
                              :route-params route-params})))
    :path-exists?
    (fn [path]
      (boolean (reitit/match-by-path router path)))})
  (accountant/dispatch-current!)
  (mount-root))

(ns mooshroom.server.api
  (:require [mooshroom.db :as db]
            [cheshire.core :refer [generate-string]]
            [reitit.ring :as reitit-ring]))


(defn list-spores [request]
  (let [spores (db/list-spores)
        data (generate-string spores)]
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body data}))

(defn get-spore [request]
  (let [spore-id (Integer/parseInt (get-in request [:path-params :spore-id]))
        spore (db/get-spore-by-id spore-id)
        data (generate-string spore)]
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body data}))

(def app
  (reitit-ring/ring-handler
   (reitit-ring/router ["/api/v1/spores"
                        ["" {:get {:handler list-spores}}]
                        ["/:spore-id" {:get {:handler get-spore}}]])
   (reitit-ring/routes
    (reitit-ring/create-resource-handler {:path "/" :root "/public"})
    (reitit-ring/create-default-handler))))


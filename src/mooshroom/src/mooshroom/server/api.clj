(ns mooshroom.server.api
  (:require [mooshroom.db :as db]
            [cheshire.core :refer [generate-string]]
            [reitit.ring :as reitit-ring]
            [reitit.ring.middleware.parameters :refer [parameters-middleware]]
            [reitit.ring.middleware.exception :as reitit-exception]
            [mooshroom.exceptions :as exceptions]
            [taoensso.telemere :as t]
            [mooshroom.server.api.enclosure-api :refer [view-enclosures new-enclosure get-enclosure]]
            [mooshroom.server.api.spore-api :refer [list-spores get-spore]]))


(defn get-dht-sensor-data [request]
  (let [dht-sensor-id (Integer/parseInt (get-in request [:path-params :dht_sensor_id]))
        data (db/get-dht-sensor-data-by-id dht-sensor-id)
        data (generate-string data)]
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body data}))

(def exception-middleware
  (reitit-exception/create-exception-middleware
   (merge
    reitit-exception/default-handlers
    {
     ::exceptions/not-found (fn [message exception request]
                   (t/log! {:level :error :msg "Resource not found" :data {:message message :exception exception :request request}})
                   {:status 404
                    :headers {"Content-Type" "application/json"}
                    :body (generate-string {:error "Resource not found"})})
     ::reitit-exception/default (fn [message exception request]
                           (t/log! {:level :error :msg "Error in request" :data {:message message :exception exception :request request}}))
    })))

(def app
  (reitit-ring/ring-handler
   (reitit-ring/router ["/api/v1"
                        ["/spores"
                         ["" {:get {:handler list-spores}}]
                         ["/:spore-id" {:get {:handler get-spore}}]]
                        ["/dht_sensors/:dht_sensor_id/data" {:get {:handler get-dht-sensor-data}}]
                        ["/enclosures"
                         ["" {:get {:handler view-enclosures}
                              :post {:handler new-enclosure}}]
                         ["/:enclosure-id" {:get {:handler get-enclosure}}]]]
                       {:data {:middleware [parameters-middleware exception-middleware]}})
   (reitit-ring/routes
    (reitit-ring/create-resource-handler {:path "/" :root "/public"})
    (reitit-ring/create-default-handler))))

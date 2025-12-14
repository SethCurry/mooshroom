(ns mooshroom.server.api
  (:require [cheshire.core :refer [generate-string]]
            [reitit.ring :as reitit-ring]
            [reitit.ring.middleware.parameters :refer [parameters-middleware]]
            [reitit.ring.middleware.exception :as reitit-exception]
            [mooshroom.exceptions :as exceptions]
            [taoensso.telemere :as t]
            [mooshroom.models.dht-sensors :as dht-sensors]
            [mooshroom.server.api.taxonomy-api :refer [list-genera list-species]]
            [mooshroom.server.api.enclosure-api :refer [view-enclosures new-enclosure get-enclosure]]
            [mooshroom.server.api.spore-api :refer [list-spores get-spore update-spore]]))


(defn get-dht-sensor-data [request]
  (let [dht-sensor-id (Integer/parseInt (get-in request [:path-params :dht_sensor_id]))
        data (dht-sensors/get-dht-sensor-data-by-id dht-sensor-id)
        data (generate-string data)]
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body data}))

(defn not-found-handler [exception _]
  (let [data (ex-data exception)
        resource-type (:resource-name data)
        resource-id (:resource-id data)]
    (t/log! {:level :error :msg "Resource not found" :data data})
    {:status 404
     :headers {"Content-Type" "application/json"}
     :body (generate-string {:error "Resource not found" :resource-type resource-type :resource-id resource-id})}))

(def exception-middleware
  (reitit-exception/create-exception-middleware
   (merge
    reitit-exception/default-handlers
    {::exceptions/not-found not-found-handler
     ::reitit-exception/default (fn [exception request]
                                  (t/log! {:level :error :msg "Error in request" :data {:exception exception :request request}}))})))

(def app
  (reitit-ring/ring-handler
   (reitit-ring/router ["/api/v1"
                        ["/spores"
                         ["" {:get {:handler list-spores}}]
                         ["/:spore-id" {:get {:handler get-spore}
                                        :put {:handler update-spore}}]]
                        ["/taxonomy"
                         ["/genera" {:get {:handler list-genera}}]
                         ["/species" {:get {:handler list-species}}]]
                        ["/dht_sensors/:dht_sensor_id/data" {:get {:handler get-dht-sensor-data}}]
                        ["/enclosures"
                         ["" {:get {:handler view-enclosures}
                              :post {:handler new-enclosure}}]
                         ["/:enclosure-id" {:get {:handler get-enclosure}}]]]
                       {:data {:middleware [parameters-middleware exception-middleware]}})
   (reitit-ring/routes
    (reitit-ring/create-resource-handler {:path "/" :root "/public"})
    (reitit-ring/create-default-handler))))

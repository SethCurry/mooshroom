(ns mooshroom.server.api
  (:require [mooshroom.db :as db]
            [cheshire.core :refer [generate-string]]
            [reitit.ring :as reitit-ring]
            [api.responses :refer [->SporeWithDHTSensors]]
            [mooshroom.server.api.enclosure-api :refer [view-enclosures new-enclosure get-enclosure]]))


(defn list-spores [request]
  (let [spores (db/list-spores)
        data (generate-string spores)]
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body data}))

(defn get-spore [request]
  (let [spore-id (Integer/parseInt (get-in request [:path-params :spore-id]))
        spore (db/get-spore-by-id spore-id)
        dht-sensors (db/get-dht-sensors-by-spore-id spore-id)]
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body (generate-string (->SporeWithDHTSensors spore-id (:name spore) dht-sensors))}))

(defn get-dht-sensor-data [request]
  (let [dht-sensor-id (Integer/parseInt (get-in request [:path-params :dht_sensor_id]))
        data (db/get-dht-sensor-data-by-id dht-sensor-id)
        data (generate-string data)]
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body data}))

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
                         ["/:enclosure-id" {:get {:handler get-enclosure}}]]])
   (reitit-ring/routes
    (reitit-ring/create-resource-handler {:path "/" :root "/public"})
    (reitit-ring/create-default-handler))))


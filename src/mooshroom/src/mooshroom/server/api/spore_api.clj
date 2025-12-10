(ns mooshroom.server.api.spore-api
  (:require [mooshroom.models.spores :as spores]
            [mooshroom.db :as db]
            [cheshire.core :refer [generate-string]]
            [api.responses :refer [->SporeWithDHTSensors]]
            [taoensso.telemere :as t]))

(defn list-spores [request]
  (let [params (:query-params request)
        raw-enclosure-id (get params "enclosure-id")
        enclosure-id (if (nil? raw-enclosure-id)
                       nil
                       (Integer/parseInt raw-enclosure-id))
        spores (spores/list-spores {:enclosure-id enclosure-id})
        data (generate-string spores)]
    (t/log! {:level :debug :msg "List spores" :data {:params params}})
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body data}))

(defn get-spore [request]
  (let [spore-id (Integer/parseInt (get-in request [:path-params :spore-id]))
        spore (spores/get-spore-by-id spore-id)
        dht-sensors (db/get-dht-sensors-by-spore-id spore-id)]
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body (generate-string (->SporeWithDHTSensors spore-id (:name spore) dht-sensors))}))

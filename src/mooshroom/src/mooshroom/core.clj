(ns mooshroom.core
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [cheshire.core :refer [generate-string]]
            [api.responses :refer [->Spore]]
            [mooshroom.mqtt :as mqtt]
            [reitit.ring :as reitit-ring]
            [mooshroom.db :as db]
            [taoensso.telemere :as t]
            [mooshroom.configuration :refer [config]]
            [clojurewerkz.machine-head.client :as mh])
  (:gen-class))

(defn handler [request]
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body (generate-string (->Spore 1 "Test"))})

(defn list-spores [request]
  (let [spores (db/list-spores)
        data (generate-string spores)]
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body data}))

(def app
  (reitit-ring/ring-handler
   (reitit-ring/router [["/api/v1/spores" {:get {:handler list-spores}}]])
   (reitit-ring/routes
    (reitit-ring/create-resource-handler {:path "/" :root "/public"})
    (reitit-ring/create-default-handler))))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (t/set-min-level! :debug)
  (let [conn (mqtt/start-mqtt-client [[(str (:prefix (:mqtt config)) "/spores/+/dht_data") 0 (fn [topic payload]
                                                                                               (println payload)
                                                                                               (t/log! {:level :debug :msg "saw MQTT message" :data {:topic topic :payload payload}}))]])]
    (mh/publish conn "mooshroom/spores/test/dht_data" "[1, 2, 3, 4]"))
  (run-jetty app {:port (:port (:http config))})
  (println "Hello, World!"))

(ns mooshroom.core
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [cheshire.core :refer [generate-string]]
            [api.responses :refer [->Spore]]
            [mooshroom.mqtt :as mqtt]
            [reitit.ring :as reitit-ring]
            [mooshroom.db :as db]
            [taoensso.telemere :as t]
            [mooshroom.configuration :refer [config]])
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
  (mqtt/start-mqtt-client)
  (run-jetty app {:port (:port (:http config))})
  (println "Hello, World!"))

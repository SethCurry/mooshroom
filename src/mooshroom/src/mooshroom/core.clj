(ns mooshroom.core
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [cheshire.core :refer [generate-string]]
            [api.responses :refer [->Spore]]
            [clojure.tools.cli :refer [parse-opts]]
            [mooshroom.mqtt :as mqtt]
            [reitit.ring :as reitit-ring]
            [mooshroom.db :as db]
            [taoensso.telemere :as t]
            [mooshroom.configuration :refer [config]]
            [clojurewerkz.machine-head.client :as mh])
  (:gen-class))

(defn list-spores [request]
  (let [spores (db/list-spores)
        data (generate-string spores)]
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body data}))

(defn get-spore [request]
  (let [spore-id (get-in request [:path-params :spore-id])
        spore (db/get-spore-by-id spore-id)
        data (generate-string spore)]
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body data}))

(def app
  (reitit-ring/ring-handler
   (reitit-ring/router [["/api/v1/spores" ["" {:get {:handler list-spores}}
                                           "/:spore-id" {:get {:handler get-spore}}]]])
   (reitit-ring/routes
    (reitit-ring/create-resource-handler {:path "/" :root "/public"})
    (reitit-ring/create-default-handler))))


(defn start-server
  "I don't do a whole lot ... yet."
  [_ _]
  (let [conn (mqtt/start-mqtt-client [[(str (:prefix (:mqtt config)) "/spores/+/dht_data")
                                       0
                                       (fn [topic payload]
                                         (println payload)
                                         (t/log! {:level :debug
                                                  :msg "saw MQTT message"
                                                  :data {:topic topic
                                                         :payload payload}}))]])]
    (mh/publish conn "mooshroom/spores/test/dht_data" "[1, 2, 3, 4]"))
  (run-jetty app {:port (:port (:http config))})
  (println "Hello, World!"))



(def global-options [["-v" "--log-level LOG-LEVEL" "Log level"
                      :default :info
                      :parse-fn keyword
                      :validate [#(contains? #{:debug :info :warn :error :fatal} %) "Must be a valid log level"]]])

(def commands {:server {:options []
                        :fn start-server}})

(defn -main
  "I don't do a whole lot ... yet."
  [& all-args]
  (let [cmd-name (first all-args)
        args (rest all-args)
        cmd-keyword (keyword cmd-name)
        cmd-def (get commands cmd-keyword)
        cmd-options (:options cmd-def)
        parsed-opts (parse-opts args (concat global-options cmd-options))
        opts (:options parsed-opts)
        rest-args (:arguments parsed-opts)]
    (when (nil? cmd-def)
      (println "Unknown command: " cmd-name)
      (System/exit 1))
    (t/set-min-level! (:log-level opts))
    ((:fn cmd-def) rest-args opts))
  (System/exit 0))
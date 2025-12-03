(ns mooshroom.server.core
  (:require [mooshroom.server.mqtt :as mqtt]
            [mooshroom.server.api :as api]
            [mooshroom.configuration :refer [config]]
            [ring.adapter.jetty :refer [run-jetty]]
            [clojurewerkz.machine-head.client :as mh]
            [taoensso.telemere :as t]))


(defn start-server
  [_ _]
  (let [prefix (:prefix (:mqtt config))
        conn (mqtt/start-mqtt-client [[(str prefix "/spores/+/dht_data")
                                       0
                                       mqtt/handle-dht-data]])]
    (t/log! {:level :info :msg "Connected to MQTT" :data {:prefix prefix}}))
  (run-jetty api/app {:port (:port (:http config))}))

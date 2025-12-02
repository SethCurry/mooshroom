(ns mooshroom.server.core
  (:require [mooshroom.server.mqtt :as mqtt]
            [mooshroom.server.api :as api]
            [mooshroom.configuration :refer [config]]
            [ring.adapter.jetty :refer [run-jetty]]
            [clojurewerkz.machine-head.client :as mh]
            [taoensso.telemere :as t]))


(defn start-server
  [_ _]
  (let [conn (mqtt/start-mqtt-client [[(str (:prefix (:mqtt config)) "/spores/+/dht_data")
                                       0
                                       mqtt/handle-dht-data]])]
    (t/log! {:level :info :msg "Connected to MQTT"})
    (mh/publish conn "mooshroom/spores/test/dht_data" "[1, 2, 3, 4]"))
  (run-jetty api/app {:port (:port (:http config))}))

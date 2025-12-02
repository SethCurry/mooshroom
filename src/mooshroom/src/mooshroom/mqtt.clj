(ns mooshroom.mqtt
  (:require [clojurewerkz.machine-head.client :as mh]
            [cheshire.core :refer [parse-string]]
            [taoensso.telemere :as t]))


(defn create-mqtt-handler [callback]
  (fn [^String topic _ ^bytes payload]
    (let [payload-string (String. payload "UTF-8")
          parsed (parse-string payload-string true)]
      (callback topic parsed))))

(defn start-mqtt-client []
  (let [conn (mh/connect "tcp://localhost:11883" {:opts {:username "mooshroom" :password "mooshroom"}})]
    (mh/subscribe conn {"hello" 0} (create-mqtt-handler (fn [topic payload]
                                                          (println payload)
                                                          (t/log! {:level :debug :msg "saw MQTT message" :data {:topic topic :payload payload}}))))
    (mh/publish conn "hello" "[1, 2, 3, 4]")))

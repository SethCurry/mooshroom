(ns mooshroom.mqtt
  (:require [clojurewerkz.machine-head.client :as mh]
            [cheshire.core :refer [parse-string]]
            [taoensso.telemere :as t]
            [mooshroom.configuration :refer [config]]))


(defn create-mqtt-handler [callback]
  (fn [^String topic _ ^bytes payload]
    (let [payload-string (String. payload "UTF-8")
          parsed (parse-string payload-string true)]
      (callback topic parsed))))

(defn start-mqtt-client []
  (let [conn (mh/connect (:broker (:mqtt config)) {:opts {:username (:username (:mqtt config)) :password (:password (:mqtt config))}})]
    (mh/subscribe conn {"hello" 0} (create-mqtt-handler (fn [topic payload]
                                                          (println payload)
                                                          (t/log! {:level :debug :msg "saw MQTT message" :data {:topic topic :payload payload}}))))
    (mh/publish conn "hello" "[1, 2, 3, 4]")))

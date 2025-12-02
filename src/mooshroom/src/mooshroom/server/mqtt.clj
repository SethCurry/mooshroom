(ns mooshroom.server.mqtt
  (:require [clojurewerkz.machine-head.client :as mh]
            [cheshire.core :refer [parse-string]]
            [taoensso.telemere :as t]
            [mooshroom.configuration :refer [config]]
            [clojure.string]))

(defn handle-dht-data [topic payload]
  (let [split-topic (clojure.string/split topic #"/")
        topic-len (count split-topic)
        spore-name (nth split-topic (- topic-len 2))
        dht-pin (:pin payload)
        ts (:time payload)
        humidity (:humidity payload)
        temperature (:temperature payload)]
    (t/log! {:level :debug :msg "Received DHT data" :data {:topic topic :payload payload}})))


(defn create-mqtt-handler [callback]
  (fn [^String topic _ ^bytes payload]
    (let [payload-string (String. payload "UTF-8")
          parsed (parse-string payload-string true)]
      (callback topic parsed))))

(defn start-mqtt-client
  "Creates an MQTT client and subscribes to the given topics.
   
   Handlers are formatted as [topic qos handler]"
  [handlers]
  (let [conn (mh/connect (:broker (:mqtt config)) {:opts {:username (:username (:mqtt config)) :password (:password (:mqtt config))}})]
    (doseq [[topic qos handler] handlers]
      (mh/subscribe conn {topic qos} (create-mqtt-handler handler)))
    conn))

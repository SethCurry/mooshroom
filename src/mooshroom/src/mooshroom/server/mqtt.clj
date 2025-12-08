(ns mooshroom.server.mqtt
  (:require [clojurewerkz.machine-head.client :as mh]
            [cheshire.core :refer [parse-string]]
            [taoensso.telemere :as t]
            [mooshroom.configuration :refer [config]]
            [clojure.string]
            [mooshroom.models.spores :as spores]
            [mooshroom.db :as db]))

(defn handle-dht-data [topic payload]
  (t/log! {:level :debug :msg "Received DHT data" :data {:topic topic :payload payload}})
  (let [split-topic (clojure.string/split topic #"/")
        topic-len (count split-topic)
        spore-name (nth split-topic (- topic-len 2))
        dht-pin (:pin payload)
        humidity (:humidity payload)
        temperature (:temperature payload)
        ts (java.sql.Timestamp/from (java.time.Instant/now))]
    (t/log! {:level :debug :msg "Getting or creating spore" :data {:spore-name spore-name}})
    (let [spore (spores/get-or-create-spore-by-name spore-name)]
      (t/log! {:level :debug :msg "Got or created spore" :data {:spore-id (:id spore)}})
      (let [dht-sensor (db/get-or-create-dht-sensor-by-spore-id-and-pin (:id spore) dht-pin)]
        (t/log! {:level :debug :msg "Got or created DHT sensor" :data {:dht-sensor-id (:id dht-sensor)}})
        (t/log! {:level :debug :msg "Creating DHT sensor data" :data {:topic topic :spore-name spore-name :dht-pin dht-pin :humidity humidity :temperature temperature}})
        (db/create-dht-sensor-data (:id dht-sensor) ts humidity temperature)
        (t/log! {:level :info :msg "Created DHT sensor data" :data {:dht-sensor-id (:id dht-sensor) :humidity humidity :temperature temperature}})))))

(defn create-mqtt-handler [callback]
  (fn [^String topic _ ^bytes payload]
    (let [payload-string (String. payload "UTF-8")
          parsed (parse-string payload-string true)]
      (t/log! {:level :debug :msg "Received MQTT message" :data {:topic topic :payload payload-string}})
      (try
        (callback topic parsed)
        (catch Exception e
          (t/log! {:level :error :msg "Error in callback" :data {:topic topic :payload payload :error e}}))))))

(defn connect []
  (mh/connect (:broker (:mqtt config)) {:opts {:username (:username (:mqtt config)) :password (:password (:mqtt config))}}))

(defn fake-spore [conn]
  (while true
    (do
      (mh/publish conn "mooshroom/spores/test_spore/dht_data" "{\"pin\": 1, \"temperature\": 20, \"humidity\": 40}")
      (t/log! {:level :info :msg "Published DHT data" :data {:pin 1 :temperature 20 :humidity 40}})
      (Thread/sleep 1000))))

(defn start-mqtt-client
  "Creates an MQTT client and subscribes to the given topics.
   
   Handlers are formatted as [topic qos handler]"
  [handlers]
  (let [conn (connect)]
    (doseq [[topic qos handler] handlers]
      (mh/subscribe conn {topic qos} (create-mqtt-handler handler)))
    conn))

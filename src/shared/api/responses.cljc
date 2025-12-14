(ns api.responses
  #?(:clj (:require [cheshire.core :as cheshire])))

(defn marshal-json [item]
  #?(:clj (cheshire/generate-string item)
     :cljs (.stringify js/JSON (cljs->js item))))

(defn unmarshal-json [item]
  #?(:clj (cheshire/parse-string item true)
     :cljs (js->cljs (.parse js/JSON item))))

(defrecord Spore [id name mac-address enclosure-id])

(defrecord SporeWithDHTSensors [id name mac-address dht-sensors])

(defrecord DHTSensor [id name pin])

(defrecord DHTSensorData [time temperature humidity])
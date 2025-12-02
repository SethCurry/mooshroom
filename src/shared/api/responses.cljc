(ns api.responses)

(defrecord Spore [id name])

(defrecord SporeWithDHTSensors [id name dht-sensors])

(defrecord DHTSensor [id name pin])

(defrecord DHTSensorData [time temperature humidity])
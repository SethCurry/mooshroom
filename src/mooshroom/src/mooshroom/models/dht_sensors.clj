(ns mooshroom.models.dht-sensors
  (:require [api.responses :refer [->DHTSensor ->DHTSensorData]]
            [mooshroom.db :refer [do-query]]))

(def dht-sensor-columns [:id :name :gpio_pin])

(defn- extract-dht-sensor [row]
  (->DHTSensor (:dht_sensors/id row) (:dht_sensors/name row) (:dht_sensors/gpio_pin row)))


(defn get-dht-sensors-by-spore-id [spore-id]
  (let [result (do-query {:select dht-sensor-columns
                          :from :dht_sensors
                          :where [:= :spore_id spore-id]}
                         :unmarshaller extract-dht-sensor)]
    result))

(defn get-dht-sensor-data-by-id [id]
  (let [result (do-query {:select [:time :temperature :humidity] :from :dht_data :where [:= :sensor_id id]}
                         :unmarshaller (fn [row] (->DHTSensorData (:dht_data/time row) (:dht_data/temperature row) (:dht_data/humidity row))))]
    result))

(defn get-dht-sensor-by-spore-id-and-pin [spore-id pin]
  (let [result (first (do-query {:select dht-sensor-columns
                                 :from :dht_sensors
                                 :where [:and
                                         [:= :spore_id spore-id]
                                         [:= :gpio_pin pin]]}
                                :unmarshaller extract-dht-sensor))]
    result))

(defn create-dht-sensor [name pin spore-id]
  (let [result (:id (first (do-query {:insert-into :dht_sensors
                                      :columns [:name :gpio_pin :spore_id]
                                      :values [[name pin spore-id]]
                                      :returning :id})))]
    result))

(defn get-or-create-dht-sensor-by-spore-id-and-pin [spore-id pin]
  (let [result (get-dht-sensor-by-spore-id-and-pin spore-id pin)]
    (if (nil? result)
      (create-dht-sensor (str "dht-" spore-id "_" pin) pin spore-id)
      result)))

(defn create-dht-sensor-data [dht-sensor-id ts humidity temperature]
  (let [result (first (do-query {:insert-into :dht_data
                                 :columns [:time :sensor_id :temperature :humidity]
                                 :values [[ts dht-sensor-id temperature humidity]]}))]
    result))
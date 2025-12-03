(ns mooshroom.db
  (:require [hikari-cp.core :refer [make-datasource]]
            [next.jdbc :as jdbc]
            [honey.sql :as sql]
            [taoensso.telemere :as t]
            [api.responses :refer [->Spore ->DHTSensor ->DHTSensorData]]
            [mooshroom.configuration :refer [config]]))

(def datasource-options {:auto-commit true
                         :read-only false
                         :connection-timeout 30000
                         :validation-timeout 5000
                         :idle-timeout 60000
                         :max-lifetime 1000000
                         :minimum-idle 1
                         :maximum-pool-size 10
                         :pool-name "mooshroom-pool"
                         :adapter "postgresql"
                         :register-mbeans false
                         :username (:username (:sql config))
                         :password (:password (:sql config))
                         :database-name (:database (:sql config))
                         :server-name (:host (:sql config))
                         :port-number (:port (:sql config))})

(defonce datasource (delay (make-datasource datasource-options)))

(defn raw-query [query & {:keys [unmarshaller]
                          :or {unmarshaller nil}}]
  (t/log! {:level :debug :msg "executing query" :data {:query (first query) :params (rest query)}})
  (let [rows (jdbc/execute! @datasource query)]
    (if (not (nil? unmarshaller))
      (map unmarshaller rows)
      rows)))

(defn do-query [query & {:keys [unmarshaller]
                         :or {unmarshaller nil}}]
  (let [formatted-query (sql/format query)]
    (raw-query formatted-query :unmarshaller unmarshaller)))

(defn list-spores []
  (let [results
        (doall (do-query {:select [:id :name] :from :spores}
                         :unmarshaller (fn [row] (->Spore (:spores/id row) (:spores/name row)))))
        filled-result (if (empty? results)
                        []
                        results)]
    filled-result))

(defn create-spore [name]
  (let [result (:id (first (do-query {:insert-into :spores
                                      :columns [:name]
                                      :values [[name]]
                                      :returning :id})))]
    result))

(defn get-spore-by-name [name]
  (let [result (first (do-query {:select [:id :name] :from :spores :where [:= :name name]}
                                :unmarshaller (fn [row] (->Spore (:spores/id row) (:spores/name row)))))]
    result))

(defn get-or-create-spore-by-name [name]
  (let [result (get-spore-by-name name)]
    (if (nil? result)
      (do (t/log! {:level :debug :msg "Creating spore" :data {:name name}})
          (create-spore name))
      (do (t/log! {:level :debug :msg "Got spore" :data {:id (:id result)}})
          result))))

(defn get-spore-by-id [id]
  (let [result (first (do-query {:select [:id :name] :from :spores :where [:= :id id]}
                                :unmarshaller (fn [row] (->Spore (:spores/id row) (:spores/name row)))))]
    result))

(defn get-dht-sensors-by-spore-id [spore-id]
  (let [result (do-query {:select [:id :name :gpio_pin] :from :dht_sensors :where [:= :spore_id spore-id]}
                         :unmarshaller (fn [row] (->DHTSensor (:dht_sensors/id row) (:dht_sensors/name row) (:dht_sensors/gpio_pin row))))]
    result))

(defn get-dht-sensor-data-by-id [id]
  (let [result (do-query {:select [:time :temperature :humidity] :from :dht_data :where [:= :sensor_id id]}
                         :unmarshaller (fn [row] (->DHTSensorData (:dht_data/time row) (:dht_data/temperature row) (:dht_data/humidity row))))]
    result))

(defn get-dht-sensor-by-spore-id-and-pin [spore-id pin]
  (let [result (first (do-query {:select [:id :name :gpio_pin] :from :dht_sensors :where [:and [:= :spore_id spore-id] [:= :gpio_pin pin]]}
                                :unmarshaller (fn [row] (->DHTSensor (:dht_sensors/id row) (:dht_sensors/name row) (:dht_sensors/gpio_pin row)))))]
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
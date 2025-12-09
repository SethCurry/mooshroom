(ns mooshroom.db
  (:require [hikari-cp.core :refer [make-datasource]]
            [next.jdbc :as jdbc]
            [honey.sql :as sql]
            [taoensso.telemere :as t]
            [api.responses :refer [->DHTSensor ->DHTSensorData]]
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

(defonce datasource (delay (try (make-datasource datasource-options)
                                (catch Exception e (t/log! {:level :error :msg "Error creating datasource" :data {:error e}})
                                       (throw e)))))

(defn raw-query
  "Executes a query as a string and returns the result as a sequence of maps.
   
   If :unmarshaller is provided, it will be applied to each row
   before returning the result."
  [query & {:keys [unmarshaller]
            :or {unmarshaller nil}}]
  (t/log! {:level :debug :msg "executing query" :data {:query (first query) :params (rest query)}})
  (let [rows (jdbc/execute! @datasource query)]
    (if (not (nil? unmarshaller))
      (map unmarshaller rows)
      rows)))

(defn do-query
  "Applies HoneySQL formatting to a query and executes it.
   
   If :unmarshaller is provided, it will be applied to each row
   before returning the result."
  [query & {:keys [unmarshaller]
            :or {unmarshaller nil}}]
  (let [formatted-query (sql/format query)]
    (raw-query formatted-query :unmarshaller unmarshaller)))

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
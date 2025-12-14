(ns mooshroom.db
  (:require [hikari-cp.core :refer [make-datasource]]
            [next.jdbc :as jdbc]
            [honey.sql :as sql]
            [taoensso.telemere :as t]
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
    (t/log! {:level :debug :msg "query result" :data {:rows rows}})
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

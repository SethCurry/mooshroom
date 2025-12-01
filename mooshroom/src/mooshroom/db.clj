(ns mooshroom.db
  (:require [hikari-cp.core :refer [make-datasource]]
            [next.jdbc :as jdbc]
            [honey.sql :as sql]
            [taoensso.telemere :as t]))

(def datasource-options {
                         :auto-commit true
                         :read-only false
                         :connection-timeout 30000
                         :validation-timeout 5000
                         :idle-timeout 60000
                         :max-lifetime :1000000
                         :minimum-idle 1
                         :maximum-pool-size 10
                         :pool-name "mooshroom-pool"
                         :adapter "postgresql"
                         :register-mbeans false
                         :username "mooshroom"
                         :password "mooshroom"
                         :database-name "mooshroom"
                         :server-name "localhost"
                         :server-port 5432
})

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
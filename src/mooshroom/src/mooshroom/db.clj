(ns mooshroom.db
  (:require [hikari-cp.core :refer [make-datasource]]
            [next.jdbc :as jdbc]
            [honey.sql :as sql]
            [taoensso.telemere :as t]
            [api.responses :refer [->Spore]]
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
                                      :values [name]
                                      :returning :id})))]
    result))

(defn get-spore-by-name [name]
  (let [result (first (do-query {:select [:id :name] :from :spores :where [:name name]}
                                :unmarshaller (fn [row] (->Spore (:spores/id row) (:spores/name row)))))]
    result))

(defn get-spore-by-id [id]
  (let [result (first (do-query {:select [:id :name] :from :spores :where [:id id]}
                                :unmarshaller (fn [row] (->Spore (:spores/id row) (:spores/name row)))))]
    result))
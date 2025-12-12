(ns mooshroom.models.spores
  (:require [mooshroom.db :as db]
            [taoensso.telemere :as t]
            [api.responses :refer [->Spore]]
            [mooshroom.exceptions :as exc]))

(defn list-spores
  "Lists all spores from the database."
  ([] (list-spores {}))
  ([options]
   (let [enclosure-id (:enclosure-id options)
         base-query {:select [:id :name :mac_address] :from :spores}
         query (if (nil? enclosure-id)
                 base-query
                 (conj base-query [:where [:= :enclosure_id (if (= enclosure-id -1) nil enclosure-id)]]))
         results
         (doall (db/do-query query
                             :unmarshaller (fn [row] (->Spore (:spores/id row) (:spores/name row) (:spores/mac_address row)))))
         filled-result (if (empty? results)
                         []
                         results)]
     filled-result)))

(defn create-spore
  "Creates a new spore in the database.
   Returns the ID of the created spore."
  [name mac-address]
  (let [result (:id (first (db/do-query {:insert-into :spores
                                         :columns [:name :mac_address]
                                         :values [[name mac-address]]
                                         :returning :id})))]
    result))

(defn get-spore-by-name [name]
  (let [result (first (db/do-query {:select [:id :name :mac_address] :from :spores :where [:= :name name]}
                                   :unmarshaller (fn [row] (->Spore (:spores/id row) (:spores/name row) (:spores/mac_address row)))))]
    (if (nil? result)
      (exc/throw-not-found "spore" name)
      result)))


(defn get-spore-by-mac-address [mac-address]
  (let [result (first (db/do-query {:select [:id :name :mac_address] :from :spores :where [:= :mac_address mac-address]}
                                   :unmarshaller (fn [row] (->Spore (:spores/id row) (:spores/name row) (:spores/mac_address row)))))]
    (if (nil? result)
      (exc/throw-not-found "spore" mac-address)
      result)))


(defn get-or-create-spore-by-mac-address
  "Gets a spore by name or creates a new spore if it doesn't exist.
   Returns the spore."
  [mac-address]
  (try (get-spore-by-mac-address mac-address)
       (catch Exception e
         (if (= (:type (ex-data e)) ::exc/not-found)
           (do (t/log! {:level :debug :msg "Creating spore" :data {:mac-address mac-address}})
               (create-spore mac-address mac-address))
           (throw e)))))

(defn get-spore-by-id
  "Gets a spore by ID.
   Returns the spore."
  [id]
  (let [result (first (db/do-query {:select [:id :name :mac_address] :from :spores :where [:= :id id]}
                                   :unmarshaller (fn [row] (->Spore (:spores/id row) (:spores/name row) (:spores/mac_address row)))))]
    (if (nil? result)
      (exc/throw-not-found "spore" id)
      result)))

(defn update-spore [id fields]
  (db/do-query {:update :spores :set fields :where [:= :id id]}))
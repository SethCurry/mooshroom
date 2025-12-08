(ns mooshroom.models.spores
  (:require [mooshroom.db :as db]
            [taoensso.telemere :as t]
            [api.responses :refer [->Spore]]))


(defn list-spores
  "Lists all spores from the database."
  []
  (let [results
        (doall (db/do-query {:select [:id :name] :from :spores}
                         :unmarshaller (fn [row] (->Spore (:spores/id row) (:spores/name row)))))
        filled-result (if (empty? results)
                        []
                        results)]
    filled-result))

(defn create-spore
  "Creates a new spore in the database.
   Returns the ID of the created spore."
  [name]
  (let [result (:id (first (db/do-query {:insert-into :spores
                                      :columns [:name]
                                      :values [[name]]
                                      :returning :id})))]
    result))

(defn get-spore-by-name [name]
  (let [result (first (db/do-query {:select [:id :name] :from :spores :where [:= :name name]}
                                :unmarshaller (fn [row] (->Spore (:spores/id row) (:spores/name row)))))]
    result))

(defn get-or-create-spore-by-name
  "Gets a spore by name or creates a new spore if it doesn't exist.
   Returns the spore."
  [name]
  (let [result (get-spore-by-name name)]
    (if (nil? result)
      (do (t/log! {:level :debug :msg "Creating spore" :data {:name name}})
          (create-spore name))
      (do (t/log! {:level :debug :msg "Got spore" :data {:id (:id result)}})
          result))))

(defn get-spore-by-id
  "Gets a spore by ID.
   Returns the spore."
  [id]
  (let [result (first (db/do-query {:select [:id :name] :from :spores :where [:= :id id]}
                                :unmarshaller (fn [row] (->Spore (:spores/id row) (:spores/name row)))))]
    result))

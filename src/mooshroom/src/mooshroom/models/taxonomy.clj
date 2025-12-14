(ns mooshroom.models.taxonomy
  (:require [mooshroom.db :as db]
            [taoensso.telemere :as t]))

(defrecord Genera [id name])

(defn list-genera
  "Lists all genera from the database."
  ([] (list-genera {}))
  ([options]
   (let [base-query {:select [:id :name] :from :genera}
         query base-query
         results
         (doall (db/do-query query
                             :unmarshaller (fn [row] (->Genera (:genera/id row) (:genera/name row)))))
         filled-result (if (empty? results)
                         []
                         results)]
     filled-result)))

(defn create-genus [name]
  (let [result (:id (first (db/do-query {:insert-into :genera
                                         :columns [:name]
                                         :values [[name]]
                                         :returning :id})))]
    (t/log! {:level :debug :msg "Created genus" :data {:name name :id result}})
    result))

(defrecord Species [id name genus-id])

(defn list-species
  "Lists all species from the database."
  ([] (list-species {}))
  ([{:keys [genus-id]
     :or {genus-id nil}}]
   (let [base-query {:select [:id :name :genus_id]
                     :from :species}
         query (if (nil? genus-id)
                 base-query
                 (conj base-query
                       [:where [:= :genus_id genus-id]]))
         results
         (doall (db/do-query query
                             :unmarshaller (fn [row] (->Species (:species/id row)
                                                                (:species/name row)
                                                                (:species/genus_id row)))))
         filled-result (if (empty? results)
                         []
                         results)]
     filled-result)))

(defn create-species [name genus-id]
  (let [result (:id (first (db/do-query {:insert-into :species
                                         :columns [:name :genus_id]
                                         :values [[name genus-id]]
                                         :returning :id})))]
    (t/log! {:level :debug :msg "Created species" :data {:name name :genus-id genus-id :id result}})
    result))

(defn get-species-by-id [id]
  (first (db/do-query {:select [:id :name :genus_id]
                       :from :species
                       :where [:= :id id]}
                      :unmarshaller (fn [row] (->Species (:species/id row)
                                                         (:species/name row)
                                                         (:species/genus_id row))))))
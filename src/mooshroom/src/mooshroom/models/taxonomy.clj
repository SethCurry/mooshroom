(ns mooshroom.models.taxonomy
  (:require [mooshroom.db :as db]
            [taoensso.telemere :as t]
            [mooshroom.exceptions :as exc]))

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
  (let [result (:genera/id (first (db/do-query {:insert-into :genera
                                                :columns [:name]
                                                :values [[name]]
                                                :returning :id})))]
    (t/log! {:level :debug :msg "Created genus" :data {:name name :id result}})
    result))

(defn get-genus-by-id [id]
  (first (db/do-query {:select [:id :name]
                       :from :genera
                       :where [:= :id id]}
                      :unmarshaller (fn [row] (->Genera (:genera/id row) (:genera/name row))))))

(defn get-genus-by-name [name]
  (let [result (first (db/do-query {:select [:id :name]
                                    :from :genera
                                    :where [:= :name name]}
                                   :unmarshaller (fn [row] (->Genera (:genera/id row) (:genera/name row)))))]
    (if (nil? result)
      (exc/throw-not-found "genus" name)
      result)))

(defn get-or-create-genus-by-name [name]
  (try (get-genus-by-name name)
       (catch Exception e
         (if (= (:type (ex-data e)) ::exc/not-found)
           (do (t/log! {:level :debug :msg "Creating genus" :data {:name name}})
               {:id (create-genus name) :name name})
           (throw e)))))

(defrecord Species [id name genus-id])

(defrecord SpeciesWithGenus [id name genus])

(defn list-species
  "Lists all species from the database."
  ([] (list-species {}))
  ([{:keys [genus-id]
     :or {genus-id nil}}]
   (let [base-query {:select [:species.id :species.name :genera.id :genera.name]
                     :left-join [:genera [:= :species.genus_id :genera.id]]
                     :from :species}
         query (if (nil? genus-id)
                 base-query
                 (conj base-query
                       [:where [:= :genus_id genus-id]]))
         results
         (doall (db/do-query query
                             :unmarshaller (fn [row] (->SpeciesWithGenus (:species/id row)
                                                                         (:species/name row)
                                                                         (->Genera (:genera/id row) (:genera/name row))))))
         filled-result (if (empty? results)
                         []
                         results)]
     filled-result)))

(defn create-species [name genus-id]
  (let [result (:species/id (first (db/do-query {:insert-into :species
                                                 :columns [:name :genus_id]
                                                 :values [[name genus-id]]
                                                 :returning :id})))]
    (t/log! {:level :debug :msg "Created species" :data {:name name :genus-id genus-id :id result}})
    result))

(defn get-species-by-id [id]
  (first (db/do-query {:select [:species.id :species.name :genera.id :genera.name]
                       :left-join [:genera [:= :species.genus_id :genera.id]]
                       :from :species
                       :where [:= :species.id id]}
                      :unmarshaller (fn [row] (->SpeciesWithGenus (:species/id row)
                                                                  (:species/name row)
                                                                  (->Genera (:genera/id row) (:genera/name row)))))))

(defn add-species-common-name [species-id name]
  (let [result (:id (first (db/do-query {:insert-into :species_common_names
                                         :columns [:species_id :name]
                                         :values [[species-id name]]
                                         :returning :id})))]
    (t/log! {:level :debug :msg "Added species common name" :data {:species-id species-id :name name :id result}})
    result))


(defn get-species-by-name [name]
  (let [result (first (db/do-query {:select [:id :name :genus_id]
                                    :from :species
                                    :where [:= :name name]}
                                   :unmarshaller (fn [row] (->Species (:species/id row) (:species/name row) (:species/genus_id row)))))]
    (if (nil? result)
      (exc/throw-not-found "species" name)
      result)))

(defn get-species-common-names [species-id]
  (let [result (db/do-query {:select [:name]
                             :from :species_common_names
                             :where [:= :species_id species-id]}
                            :unmarshaller (fn [row] (:species_common_names/name row)))]
    (if (empty? result)
      []
      result)))

(defn get-or-create-species-by-name [name genus-id]
  (try (get-species-by-name name)
       (catch Exception e
         (if (= (:type (ex-data e)) ::exc/not-found)
           (do (t/log! {:level :debug :msg "Creating species" :data {:name name :genus-id genus-id}})
               {:id (create-species name genus-id) :name name :genus-id genus-id})
           (throw e)))))

(defn ensure-common-name [species-id name]
  (let [common-names (get-species-common-names species-id)]
    (when-not (contains? common-names name)
      (add-species-common-name species-id name))))
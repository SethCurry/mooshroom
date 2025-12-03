(ns mooshroom.models.mushrooms
  (:require [mooshroom.db :as db]))

(defn create-genus [name]
  (let [result (:id (first (db/do-query {:insert-into :genera :columns [:name] :values [[name]] :returning :id})))]
    result))

(defn get-genus-by-id [id]
  (first (db/do-query {:select [:id :name]
                       :from :genera
                       :where [:= :genera.id id]})))

(defn create-species [name genus-id]
  (let [result (:id (first (db/do-query {:insert-into :species :columns [:name :genus_id] :values [[name genus-id]] :returning :id})))]
    result))

(defn list-species []
  (db/do-query {:select [:species.id :species.name :genera.name]
                :from :species
                :left-join [:genera [:= :species.genus_id :genera.id]]}))

(defn get-species-by-id [id]
  (first (db/do-query {:select [:species.id :species.name :genera.name]
                       :from :species
                       :left-join [:genera [:= :species.genus_id :genera.id]
                                   :species_common_names [:= :species.id :species_common_names.species_id]]
                       :where [:= :species.id id]})))
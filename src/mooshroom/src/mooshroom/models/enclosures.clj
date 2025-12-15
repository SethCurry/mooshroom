(ns mooshroom.models.enclosures
  (:require [mooshroom.db :as db]))

(def enclosure-columns [:id :name])

(defn- extract-enclosure [row]
  {:id (:enclosures/id row) :name (:enclosures/name row)})

(defn list-enclosures []
  (let [result (db/do-query {:select enclosure-columns
                             :from :enclosures}
                            :unmarshaller extract-enclosure)]
    (if (empty? result)
      []
      result)))

(defn create-enclosure [name]
  (db/do-query {:insert-into :enclosures :columns [:name] :values [[name]] :returning :id}))

(defn get-enclosure-by-id [id]
  (first (db/do-query {:select enclosure-columns
                       :from :enclosures
                       :where [:= :id id]}
                      :unmarshaller extract-enclosure)))
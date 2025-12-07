(ns mooshroom.models.enclosures
  (:require [mooshroom.db :as db]))

(defn list-enclosures []
  (let [result (db/do-query {:select [:id :name] :from :enclosures}
                            :unmarshaller (fn [row] {:id (:enclosures/id row) :name (:enclosures/name row)}))]
    (if (empty? result)
      []
      result)))

(defn create-enclosure [name]
  (db/do-query {:insert-into :enclosures :columns [:name] :values [[name]] :returning :id}))
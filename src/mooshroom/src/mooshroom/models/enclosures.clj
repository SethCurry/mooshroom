(ns mooshroom.models.enclosures
  (:require [mooshroom.db :as db]))

(defn list-enclosures []
  (db/do-query {:select [:id :name] :from :enclosures}))

(defn create-enclosure [name]
  (db/do-query {:insert-into :enclosures :columns [:name] :values [[name]] :returning :id}))
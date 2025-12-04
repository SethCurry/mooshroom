(ns mooshroom.server.api.enclosure-api
(:require [mooshroom.server.api.util :refer [make-response]]
          [mooshroom.models.enclosures :refer [list-enclosures create-enclosure]]))

(defn view-enclosures [request]
  (let [enclosures (list-enclosures)]
    (make-response 200 enclosures)))

(defn new-enclosure [request]
  (let [name (get-in request [:body :name])]
    (create-enclosure name)
    (make-response 200 {:message "Enclosure created"})))
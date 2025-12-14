(ns mooshroom.server.api.taxonomy-api
  (:require [mooshroom.models.taxonomy :as taxonomy]
            [mooshroom.server.api.util :refer [make-response]]))

(defn list-genera [request]
  (let [genera (taxonomy/list-genera)]
    (make-response 200 genera)))

(defn list-species [request]
  (let [params (:query-params request)
        raw-genus-id (get params "genus-id")
        genus-id (if (nil? raw-genus-id)
                   nil
                   (Integer/parseInt raw-genus-id))
        species (taxonomy/list-species {:genus-id genus-id})]
    (make-response 200 species)))
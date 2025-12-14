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

(defn get-genus [request]
  (let [genus-id (Integer/parseInt (get-in request [:path-params :genus-id]))
        genus (taxonomy/get-genus-by-id genus-id)]
    (make-response 200 genus)))

(defn get-species [request]
  (let [species-id (Integer/parseInt (get-in request [:path-params :species-id]))
        species (taxonomy/get-species-by-id species-id)
        common-names (taxonomy/get-species-common-names species-id)]
    (make-response 200 (assoc species :common-names common-names))))
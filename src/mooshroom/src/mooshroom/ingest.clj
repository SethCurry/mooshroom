(ns mooshroom.ingest
  (:require [mooshroom.models.taxonomy :as taxonomy]
            [cheshire.core :refer [parse-string]]))

(defn ingest-taxonomy [file]
  (let [mushrooms (parse-string (slurp file) true)]
    (doseq [mushroom mushrooms]
      (let [genus (taxonomy/get-or-create-genus-by-name (:genus mushroom))
            species (taxonomy/get-or-create-species-by-name (:species mushroom) (:id genus))]
        (doseq [common-name (:common_names mushroom)]
          (taxonomy/ensure-common-name (:id species) common-name))))))
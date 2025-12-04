(ns mooshroom.server.api.util
  (:require [cheshire.core :refer [generate-string]]))

(defn make-response [status-code data]
  {:status status-code
   :headers {"Content-Type" "application/json"}
   :body (generate-string data)})
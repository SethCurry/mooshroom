(ns mooshroom.exceptions)

(derive ::not-found ::exception)

(defn throw-exc
  ([type message]
   (throw-exc type message {}))
  ([type message fields]
   (throw (ex-info message (merge fields {:type type})))))


(defn throw-not-found [resource-name resource-id]
  (throw-exc ::not-found (format "%s %s not found" resource-name resource-id) {:resource-name resource-name :resource-id resource-id}))

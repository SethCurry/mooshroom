(ns mooshroom.exceptions)

(derive ::not-found ::exception)

(defn throw-exc
  ([type message]
   (throw-exc type message {}))
  ([type message fields]
   (throw (ex-info message (merge fields {:type type})))))
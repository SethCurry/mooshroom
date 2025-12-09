(ns ui.api-client
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]))

(defn- build-list-spores-query [{:keys [enclosure-id]
                                 :or {enclosure-id nil}}]
  (->> {}
       (#(if (nil? enclosure-id)
           %
           (assoc % :enclosure-id enclosure-id)))))

(defn list-spores
  ([] (list-spores {}))
  ([options]
   (go (let [response (<! (http/get "/api/v1/spores"
                                    {:query-params (build-list-spores-query options)}))]
         (:body response)))))

(defn get-spore [id]
  (go (let [response (<! (http/get (str "/api/v1/spores/" id)))]
        (:body response))))

(defn get-dht-data [id]
  (go (let [response (<! (http/get (str "/api/v1/dht_sensors/" id "/data")))]
        (:body response))))

(defn list-enclosures []
  (go (let [response (<! (http/get "/api/v1/enclosures"))]
        (:body response))))

(defn create-enclosure [name]
  (go (let [response (<! (http/post "/api/v1/enclosures" {:json-params {:name name}}))]
        response)))

(defn get-enclosure [id]
  (go (let [response (<! (http/get (str "/api/v1/enclosures/" id)))]
        response)))
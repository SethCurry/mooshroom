(ns ui.api-client
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            [ui.components.table :refer [TableRow]]))

(defn- build-list-spores-query [{:keys [enclosure-id]
                                 :or {enclosure-id nil}}]
  (->> {}
       (#(if (nil? enclosure-id)
           %
           (assoc % :enclosure-id enclosure-id)))))

(defrecord ListSporesItem [id name mac-address]
  TableRow
  (get-id [this] id)
  (row-data [this] [id name]))

(defn list-spores
  ([] (list-spores {}))
  ([options]
   (go (let [response (<! (http/get "/api/v1/spores"
                                    {:query-params (build-list-spores-query options)}))]
         (map (fn [item] (ListSporesItem. (:id item) (:name item) (:mac-address item))) (:body response))))))

(defn get-spore [id]
  (go (let [response (<! (http/get (str "/api/v1/spores/" id)))]
        (:body response))))

(defn update-spore [id fields]
  (go (let [response (<! (http/put (str "/api/v1/spores/" id) {:json-params fields}))]
        response)))

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
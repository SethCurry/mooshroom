(ns ui.api
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]))

(defn list-spores []
  (go (let [response (<! (http/get "/api/v1/spores"))]
        (:body response))))

(defn get-spore [id]
  (go (let [response (<! (http/get (str "/api/v1/spores/" id)))]
        (:body response))))

(defn get-dht-data [id]
  (go (let [response (<! (http/get (str "/api/v1/dht_sensors/" id "/data")))]
        (:body response))))
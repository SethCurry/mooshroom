(ns ui.api
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]))

(defn list-spores []
  (go (let [response (:body  (<! (http/get "/api/v1/spores")))]
        response)))
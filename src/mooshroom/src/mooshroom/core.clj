(ns mooshroom.core
  (:require
   [clojure.tools.cli :refer [parse-opts]]
   [taoensso.telemere :as t]
   [mooshroom.server.core :as server]
   [mooshroom.server.mqtt :as mqtt]
   [mooshroom.ingest :as ingest])
  (:gen-class))


(def global-options [["-v" "--log-level LOG-LEVEL" "Log level"
                      :default :info
                      :parse-fn keyword
                      :validate [#(contains? #{:debug :info :warn :error :fatal} %) "Must be a valid log level"]]])

(def commands {:server {:options []
                        :fn server/start-server}
               :fake-spore {:options []
                            :fn (fn [_ _] (mqtt/fake-spore (mqtt/connect)))}
               :ingest-taxonomy {:options []
                                 :fn (fn [args _] (ingest/ingest-taxonomy (first args)))}})

(defn -main
  "I don't do a whole lot ... yet."
  [& all-args]
  (let [cmd-name (first all-args)
        args (rest all-args)
        cmd-keyword (keyword cmd-name)
        cmd-def (get commands cmd-keyword)
        cmd-options (:options cmd-def)
        parsed-opts (parse-opts args (concat global-options cmd-options))
        opts (:options parsed-opts)
        opt-errors (:errors parsed-opts)
        rest-args (:arguments parsed-opts)]
    (when (not (empty? opt-errors))
      (println "Invalid CLI arguments:")
      (doseq [error opt-errors]
        (println "  " error))
      (System/exit 1))
    (when (nil? cmd-def)
      (println "Unknown command: " cmd-name)
      (System/exit 1))
    (t/set-min-level! (:log-level opts))
    ((:fn cmd-def) rest-args opts))
  (System/exit 0))
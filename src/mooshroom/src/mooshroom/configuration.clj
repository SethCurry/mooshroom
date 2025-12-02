(ns mooshroom.configuration
  (:require [clojure.java.io :as io]
            [cheshire.core :refer [parse-string]]
            [taoensso.telemere :as t]))

(defn load-config [path]
  (t/log! {:level :debug :msg "loading config" :data {:path path}})
  (let [file (io/file path)]
    (if (.exists file)
      (parse-string (slurp file) true)
      (throw (Exception. (format "Config file %s not found" path))))))

(def config (load-config "config.local.json"))
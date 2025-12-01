(ns mooshroom.core
  (:require [ring.adapter.jetty :refer [run-jetty]])
  (:gen-class))

(defn handler [request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body "<html><body><h1>Hello, World!</h1></body></html>"})

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (run-jetty handler {:port 3000})
  (println "Hello, World!"))

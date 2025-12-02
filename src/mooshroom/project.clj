(defproject mooshroom "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "https://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.12.2"]
                 [ring "1.15.3"]
                 [com.taoensso/telemere "1.1.0"]
                 [cheshire/cheshire "6.1.0"]
                 [com.github.seancorfield/next.jdbc "1.3.1070"]
                 [org.postgresql/postgresql "42.7.8"]
                 [hikari-cp/hikari-cp "3.3.0"]
                 [com.github.seancorfield/honeysql "2.7.1350"]
                 [metosin/reitit "0.9.2"]
                 [org.eclipse.paho/org.eclipse.paho.client.mqttv3 "1.2.5"]
                 [org.clojure/tools.cli "1.2.245"]
                 [clojurewerkz/machine_head "1.0.0"]]
  :source-paths ["src" "../shared"]
  :main ^:skip-aot mooshroom.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})

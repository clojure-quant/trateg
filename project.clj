(defproject trateg "0.1.2-SNAPSHOT"
  :dependencies
  [;[org.clojure/clojure "1.10.1-RC1"]
   [org.clojure/clojure "1.10.1"]
  ;[org.clojure/core.async "0.4.490"]
   [org.clojure/core.async "1.0.567"]
   [medley "1.2.0"]
   [clj-time "0.14.3"] ; (bybit)
   [cheshire "5.8.1"] ; JSON encoding
   [clj-http "3.10.0"]  ; http requests (bybit)                        
   [org.clojure/data.csv "0.1.4"]
   [net.cgrand/xforms "0.18.2"] ; transducers for timeseries
   [org.ta4j/ta4j-core "0.12"] ; ta4j java technical indicator library
   [org.pinkgorilla/throttler "1.0.1"] ; throtteling
   
   ]
  :repl-options {:init-ns ta.model.single}
  :source-paths ["src" "dev"]
  :resource-paths ["resources"])

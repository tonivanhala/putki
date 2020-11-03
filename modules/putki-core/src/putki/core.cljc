(ns putki.core
  (:require [putki.impl :as impl]
            [putki.runtime.default]
            [putki.runtime.protocol :as runtime])
  #?(:clj (:require [putki.runtime.default])))

(defn init
  [graph]
  (impl/-init graph))

(defn run!
  [runtime workflow]
  (when-let [execution (runtime/run runtime workflow)]
    {:runtime runtime
     :workflow workflow
     :execution execution}))

(defn start!
  [graph]
  (some->> graph
           init
           (run! (putki.runtime.default/local-thread-runner))))

(defn halt!
  [pipeline]
  (runtime/halt (:runtime pipeline) (:execution pipeline)))

(defn reset!
  [pipeline]
  (runtime/reset (:runtime pipeline) (:execution pipeline)))

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
  (runtime/run runtime workflow))

(defn start!
  [graph]
  (let [workflow (init graph)
        runtime (putki.runtime.default/local-thread-runner)]
    (run! runtime workflow)
    {:workflow workflow
     :runtime runtime}))

(defn halt!
  [runtime]
  (runtime/halt runtime))

(defn reset!
  [runtime]
  (runtime/reset runtime))

(defn emit
  ([pipeline data]
   (runtime/consume (:runtime pipeline) data))
  ([pipeline job-id data]
   (runtime/consume (:runtime pipeline) job-id data)))

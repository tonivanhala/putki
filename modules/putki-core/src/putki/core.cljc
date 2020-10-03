(ns putki.core
  (:require [putki.impl :as impl]
            [putki.runtime.default]
            [putki.runtime.protocol :as runtime])
  #?(:clj (:require [putki.runtime.default])))

(defn get-jobs
  "Gets Jobs of the given Workflow indexed by their id."
  [workflow]
  (get-in workflow [:workflow :jobs]))

(defn get-source-ids
  "Get Job ids of Sources in given Workflow."
  [workflow]
  (get-in workflow [:refs :sources]))

(defn get-sources
  "Get Sources of the given Workflow indexed by their job ids."
  [workflow]
  (->> workflow
       get-source-ids
       (select-keys (get-jobs workflow))))

(defn get-sink-ids
  "Get Job ids of Sinks in given Workflow."
  [workflow]
  (get-in workflow [:refs :sinks]))

(defn get-sinks
  "Get Sinks of the given Workflow indexed by their job ids."
  [workflow]
  (->> workflow
       get-sink-ids
       (select-keys (get-jobs workflow))))

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

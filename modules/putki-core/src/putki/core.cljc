(ns putki.core
  (:require [putki.impl :as impl]))

(defn get-jobs
  "Gets Jobs of the given Workflow indexed by their id."
  [workflow]
  (get-in workflow [:workflow :jobs]))

(defn init
  [graph]
  (impl/-init graph))

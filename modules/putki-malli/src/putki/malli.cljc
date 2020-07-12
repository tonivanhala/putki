(ns putki.malli
  (:require
   [malli.core :as malli]
   [putki.malli.schemas :as schemas]))

(def valid-graph?
  (malli/validator schemas/Graph))

(def valid-workflow?
  (malli/validator schemas/Workflow))

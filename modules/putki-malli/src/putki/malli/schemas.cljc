(ns putki.malli.schemas
  (:require
    [clojure.test]))

(def Id
  [:or string? keyword? uuid?])

(def Pipe
  [:map
   [:id Id]
   [:input Id]
   [:output Id]])

(def Job
  [:map
   [:id Id]
   [:action any?]])

(def Graph seqable?)

(def Workflow
  [:map
   [:refs
    [:map
     [:sources sequential?]
     [:sinks sequential?]]]
   [:workflow
    [:map
     [:pipes
      [:map-of Id Pipe]]
     [:jobs
      [:map-of Id Job]]]]])

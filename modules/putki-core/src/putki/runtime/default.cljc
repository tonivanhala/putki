(ns putki.runtime.default
  #?(:clj (:require [putki.runtime.jvm.local-thread-runner])))

(defn local-thread-runner
  []
  #?(:clj (putki.runtime.jvm.local-thread-runner/local-thread-runner)))

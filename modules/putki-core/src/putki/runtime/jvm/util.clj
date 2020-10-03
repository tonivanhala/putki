(ns putki.runtime.jvm.util)

(def ^:private +log-2+ (Math/log 2))

(defn log2
  [x]
  (/ (Math/log x) +log-2+))

(defn nproc
  []
  (.availableProcessors (Runtime/getRuntime)))

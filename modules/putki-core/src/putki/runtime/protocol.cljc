(ns putki.runtime.protocol)

(defprotocol Executor
  (run [this workflow])
  (halt [this execution])
  (reset [this execution]))

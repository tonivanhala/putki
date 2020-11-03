(ns putki.runtime.protocol)

(defprotocol Executor
  (run [this workflow])
  (halt [this])
  (reset [this]))

(defprotocol DataConsumer
  (consume
    [this data]
    [this job-id data]))

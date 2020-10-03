(ns putki.runtime.default
  (:require [putki.runtime.protocol :as protocol])
  #?(:clj (:require [putki.runtime.jvm.thread-pool]
                    [putki.runtime.jvm.queue])))

(defn local-thread-runner
  []
  (reify
    protocol/Executor
    (run [this workflow]
      (let [{:keys [execution coordination] :as _thread-pools} (putki.runtime.jvm.thread-pool/create-thread-pools)
            job-buffers (putki.runtime.jvm.queue/fifo-queue)]))
    (halt [this execution])
    (reset [this execution])))

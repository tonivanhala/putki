(ns putki.runtime.jvm.thread-pool
  (:require [putki.runtime.jvm.util :as util])
  (:import (java.util.concurrent Executors)))

(defn get-thread-counts
  []
  (let [processors (util/nproc)
        coordination (-> processors
                         double
                         util/log2
                         (- 2)
                         (Math/floor)
                         int
                         (max 1))
        execution (max 1 (- processors coordination))]
    {:execution execution
     :coordination coordination}))

(defn create-thread-pool
  [n-threads]
  (if (< 1 n-threads)
    (Executors/newFixedThreadPool n-threads)
    (Executors/newSingleThreadExecutor)))

(defn create-thread-pools
  []
  (let [{:keys [execution coordination]} (get-thread-counts)]
    {:execution (create-thread-pool execution)
     :coordination (create-thread-pool coordination)}))

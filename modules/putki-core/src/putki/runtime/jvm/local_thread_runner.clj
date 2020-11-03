(ns putki.runtime.jvm.local-thread-runner
  (:require
   [putki.runtime.protocol :as protocol]
   [putki.runtime.jvm.thread-pool]
   [putki.runtime.jvm.queue :as queue]
   [putki.util.data :as util])
  (:import (java.util.concurrent ExecutorService)))

(def +poll-interval-ms+ 500)

(defn create-buffers
  [ids]
  (reduce
   (fn [acc id]
     (assoc acc id (queue/fifo-queue)))
   {}
   ids))

(defn get-random-weighted
  "Takes one pipe buffer at random with probability weighted by
   the number of elements in each buffer."
  [buffers-by-id]
  (when-let [{:keys [total cumulative]}
             (reduce-kv
              (fn [acc id buffer]
                (let [n (count buffer)]
                  (-> acc
                      (update :total (fnil #(+ n %) 0))
                      (update :cumulative conj [n id]))))
              {}
              buffers-by-id)]
    (let [i (rand-int total)]
      (loop [cumulative' cumulative
             running-sum 0]
        (when-let [[weight buffer-id] (first cumulative')]
          (let [running-sum' (+ weight running-sum)]
            (if (and (< 0 weight) (< i running-sum'))
              buffer-id
              (recur (rest cumulative') running-sum'))))))))

(defn ->output
  [outputs item]
  (when outputs
    (let [seqs (util/wrap-into [] outputs)]
      (doseq [^queue/DataQueue s seqs]
        (queue/put-item s item)))))

(defn ^Runnable create-job
  [outputs action item]
  (fn -job-fn []
    (let [result (action item)]
      (when-not (= :putki.core/nil result)
        (->output outputs result)))))

(defn submit-job
  [^ExecutorService executor-pool outputs action item]
  (let [^Runnable job (create-job outputs action item)]
    (.submit executor-pool job)))

(defn take-and-process!
  [{:keys [jobs pipes] :as workflow}
   ^ExecutorService executor-pool
   pipe-buffers
   pipe-id]
  (when-let [item (->> pipe-id
                       (get pipe-buffers)
                       (queue/pop-item))]
    (when-not (= ::queue/nil item)
      (let [{:keys [output]} (get pipes pipe-id)
            {:keys [action]} (get jobs output)
            outputs (map
                     #(get pipe-buffers %)
                     (get-output-pipes workflow output))]
        (submit-job executor-pool outputs action item)))))

(defn start-coordination-thread!
  [{:keys [running ^ExecutorService executor-pool pipe-buffers workflow]}]
  (.start
   (Thread.
    ^Runnable
    (fn -coordination-thread []
      (loop []
        (if-let [pipe-id (get-random-weighted pipe-buffers)]
          (take-and-process! workflow executor-pool pipe-buffers pipe-id)
          (Thread/sleep +poll-interval-ms+))
        (when @running
          (recur)))))))

(defn local-thread-runner
  []
  (let [running (atom nil)]
    (reify
      protocol/Executor
      (run [_this workflow-with-refs]
        (let [workflow (:workflow workflow-with-refs)
              {:keys [execution coordination] :as _thread-counts} (putki.runtime.jvm.thread-pool/get-thread-counts)
              ^ExecutorService exec-pool (putki.runtime.jvm.thread-pool/create-thread-pool execution)
              pipe-buffers (create-buffers (-> workflow :pipes (keys)))]
          (reset! running true)
          (dotimes [n coordination]
            (start-coordination-thread! {:id (str "coordinator-" n)
                                         :executor-pool exec-pool
                                         :pipe-buffers pipe-buffers
                                         :workflow workflow
                                         :running running}))))
      (halt [this execution]
        (reset! running false))
      (reset [this execution]
        (reset! running false)
        (reset! running true)))))

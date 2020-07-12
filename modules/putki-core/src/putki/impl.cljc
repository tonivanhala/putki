(ns putki.impl
  (:require
   [putki.util.data :as util]))

(defn ensure-id
  [job]
  (update job :id (fn -set-id [id] (or id (util/gen-uuid)))))

(defn mk-pipe
  [from to]
  {:id (util/gen-uuid)
   :input (-> from :job :id)
   :output (:id to)})

(defn -init-job
  [parent job opts]
  (let [job-map (-> (if (map? job) job {:action job})
                    (merge opts)
                    ensure-id)]
    (cond-> {:job job-map}
      (some? parent) (assoc :pipe (mk-pipe parent job-map)))))

(defn job->workflow
  [{:keys [job pipe]}]
  (let [job-id (:id job)
        pipe-id (:id pipe)]
    {:workflow (cond-> (assoc-in {} [:jobs job-id] job)
                 pipe (assoc-in [:pipes pipe-id] pipe))}))

(defn -init-recur
  [parent [job & [?opts :as more]]]
  (when job
    (let [[opts siblings] (if (and (not (map? job)) (map? ?opts))
                            [?opts (rest more)]
                            [{} more])
          workflow-job (-init-job parent job opts)
          job-id (-> workflow-job :job :id)
          workflow (cond-> (job->workflow workflow-job)
                     (nil? parent) (assoc-in [:refs :sources] [job-id]))
          sibling (first siblings)
          remaining (if (sequential? sibling) ; Is child of current job?
                      (rest siblings)
                      siblings)
          is-leaf (not (sequential? sibling))]
      (cond-> (->>
               [(when (sequential? sibling)
                  (-init-recur workflow-job sibling))
                (when remaining (-init-recur parent remaining))
                workflow]
               (filter identity)
               (apply util/deep-merge))
        is-leaf (assoc-in [:refs :sinks] [job-id])))))

(defn -init
  [graph]
  (assert (sequential? graph))
  (-init-recur nil graph))

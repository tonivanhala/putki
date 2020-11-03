(ns putki.util.runtime)

(defn get-jobs
  "Gets Jobs of the given Workflow indexed by their id."
  [workflow]
  (get-in workflow [:workflow :jobs]))

(defn get-source-ids
  "Get Job ids of Sources in given Workflow."
  [workflow]
  (get-in workflow [:refs :sources]))

(defn get-sources
  "Get Sources of the given Workflow indexed by their job ids."
  [workflow]
  (->> workflow
       get-source-ids
       (select-keys (get-jobs workflow))))

(defn get-sink-ids
  "Get Job ids of Sinks in given Workflow."
  [workflow]
  (get-in workflow [:refs :sinks]))

(defn get-sinks
  "Get Sinks of the given Workflow indexed by their job ids."
  [workflow]
  (->> workflow
       get-sink-ids
       (select-keys (get-jobs workflow))))

(defn get-output-pipes
  [{:keys [pipes]} job-id]
  (seq
    (keep
       (fn -matches-job-id?
           [[pipe-id {:keys [input output]}]]
           (when (and
                   (= job-id input)
                   output)
             pipe-id))
       pipes)))

(ns putki.runtime.jvm.queue
  (:import (java.util.concurrent ArrayBlockingQueue PriorityBlockingQueue ConcurrentHashMap)
           (java.util Comparator)))

(def ^:private +default-fifo-queue-size+ 32)
(def ^:private +default-priority-queue-size+ 11)

(defprotocol
  DataQueue
  (put-item [this item])
  (peek-item [this])
  (pop-item [this])
  (clear-queue [this]))

(defn fifo-queue
  ([]
   (fifo-queue +default-fifo-queue-size+))
  ([queue-size]
   (let [queue (ArrayBlockingQueue. queue-size)]
     (reify
       DataQueue
       (put-item [_this item]
         (.offer queue {:item item}))
       (peek-item [_this]
         (if-let [item (.peek queue)]
           (:item item)
           ::nil))
       (pop-item [_this]
         (if-let [item (.poll queue)]
           (:item item)
           ::nil))
       (clear-queue [_this]
         (.clear queue))
       clojure.lang.Counted
       (count [_this]
         (.size queue))))))

(defn updateable-priority-queue
  ([]
   (updateable-priority-queue {}))
  ([{:keys [get-id get-priority initial-size replace?]
     :or {get-id :id
          get-priority :priority
          initial-size +default-priority-queue-size+
          replace? (fn [_current-item _new-item] true)}}]
   (let [-comparator (reify Comparator
                       (compare [_this one other]
                         (let [first-prio (-> one :item get-priority long)
                               second-prio (-> other :item get-priority long)]
                           (.compareTo (Long/valueOf first-prio) (Long/valueOf second-prio))))
                       (equals [this other]
                         (.equals this other)))
         queue (PriorityBlockingQueue. initial-size -comparator)
         valid-item-by-id (ConcurrentHashMap.)
         mutex (Object.)
         valid-item? (fn [item]
                       (let [id (some-> item :item get-id)
                             priority (some-> item :item get-priority)]
                         (locking mutex
                           (and
                             id
                             priority
                             (= priority (some->> id
                                                  (.get valid-item-by-id)
                                                  get-priority))))))]
     (reify
       DataQueue
       (put-item [_this item]
         (locking mutex
           (let [id (get-id item)
                 current-item (.get valid-item-by-id id)]
             (when (or (nil? current-item) (replace? current-item item))
               (.put valid-item-by-id id item)
               (.put queue {:item item})))))
       (peek-item [_this]
         (locking mutex
           (loop [item (.peek queue)]
             (cond
               (valid-item? item)
               (:item item)
               item
               (do (.poll queue)
                   (recur (.peek queue)))
               :else
               ::nil))))
       (pop-item [_this]
         (locking mutex
           (loop [item (.poll queue)]
             (cond
               (valid-item? item)
               (do
                 (.remove valid-item-by-id (-> item :item get-id))
                 (:item item))
               item
               (recur (.poll queue))
               :else
               ::nil))))
       (clear-queue [_this]
         (locking mutex
           (.clear queue)
           (.clear valid-item-by-id)))
       clojure.lang.Counted
       (count [_this]
         (.size valid-item-by-id))))))

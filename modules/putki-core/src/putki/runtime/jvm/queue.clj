(ns putki.runtime.jvm.queue
  (:import (java.util.concurrent ArrayBlockingQueue)))

(def ^:private +default-queue-size+ 32)

(defprotocol
  DataQueue
  (put-item [this item])
  (peek-item [this])
  (pop-item [this])
  (clear-queue [this]))

(defn fifo-queue
  ([]
   (fifo-queue +default-queue-size+))
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
         (.clear queue))))))

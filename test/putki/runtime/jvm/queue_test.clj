(ns putki.runtime.jvm.queue-test
  (:require [clojure.test :refer [deftest testing is]]
            [putki.runtime.jvm.queue :as q]))

(deftest fifo-queue-test
  (testing "insert and remove in fifo order"
    (let [queue (q/fifo-queue)]
      (doseq [i (range 10)]
        (q/put-item queue i))
      (is (= 10 (count queue)))
      (doseq [i (range 10)]
        (is (= i (q/pop-item queue))))
      (is (= 0 (count queue)))
      (is (= ::q/nil (q/peek-item queue)))
      (is (= ::q/nil (q/pop-item queue)))))
  (testing "insertion to full queue returns false"
    (let [queue (q/fifo-queue 5)
          results (for [i (range 10)] (q/put-item queue i))]
      (is (= (mapcat #(repeat 5 %) [true false]) results))
      (is (= 5 (count queue)))
      (doseq [i (range 5)]
        (is (= (- 5 i) (count queue)))
        (is (= i (q/pop-item queue))))
      (is (= 0 (count queue)))
      (is (= ::q/nil (q/peek-item queue)))
      (is (= ::q/nil (q/pop-item queue))))))

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

(deftest updateable-priority-queue-test
  (testing "items are returned in priority order"
    (let [queue (q/updateable-priority-queue)
          priorities [42 313 60 0 100]
          expected (sort priorities)
          items (for [p priorities]
                  {:id (str "foo_" p)
                   :priority p})]
      (doseq [item items]
        (q/put-item queue item))
      (doseq [e expected]
        (is (= {:id (str "foo_" e) :priority e} (q/pop-item queue))))
      (is (= ::q/nil (q/pop-item queue)))))
  (testing "items can be updated with new priority"
    (let [queue (q/updateable-priority-queue {:get-priority #(- (:priority %))})
          priorities [39 37 400 -5 100 90 1000 1001 1002 1003 1004 1005 1006]
          expected (sort-by - priorities)
          items (for [p priorities]
                  {:id (str "bar " p)
                   :priority p})]
      (doseq [item items]
        (q/put-item queue item))
      (is (= 13 (count queue)))
      (doseq [e (take 7 expected)]
        (is (= {:id (str "bar " e)
                :priority e}
               (q/pop-item queue))))
      (is (= 6 (count queue)))
      (q/put-item queue {:id "bar 37"
                         :priority 999})
      (q/put-item queue {:id "bar 400"
                         :priority 1})
      (is (= 6 (count queue)))
      (doseq [e [{:id "bar 37" :priority 999}
                 {:id "bar 100" :priority 100}
                 {:id "bar 90" :priority 90}
                 {:id "bar 39" :priority 39}
                 {:id "bar 400" :priority 1}
                 {:id "bar -5" :priority -5}]]
        (is (= e (q/pop-item queue))))
      (is (= ::q/nil (q/pop-item queue)))))
  (testing "can reinsert item with same id"
    (let [queue (q/updateable-priority-queue)]
      (doseq [item [{:id :foo :priority 45}
                    {:id :bar :priority 35}
                    {:id :baz :priority 66}]]
        (q/put-item queue item))
      (is (= {:id :bar :priority 35} (q/pop-item queue)))
      (is (= {:id :foo :priority 45} (q/pop-item queue)))
      (doseq [item [{:id :foo :priority 45}
                    {:id :bar :priority 35}
                    {:id :baz :priority 66}]]
        (q/put-item queue item))
      (is (= {:id :bar :priority 35} (q/pop-item queue)))
      (is (= {:id :foo :priority 45} (q/pop-item queue)))
      (is (= {:id :baz :priority 66} (q/pop-item queue)))
      (is (= ::q/nil (q/pop-item queue)))))
  (testing "custom selector fn for priority"
    (let [ids (range 20)
          prios (map #(- 10 %) ids)
          get-priority :real-priority
          queue (q/updateable-priority-queue {:get-priority get-priority})]
      (doseq [[id prio] (map vector ids prios)]
        (q/put-item queue {:id id :priority id :real-priority prio}))
      (doseq [expected (sort prios)]
        (is (= expected (some-> (q/pop-item queue) get-priority))))
      (is (= ::q/nil (q/peek-item queue)))))
  (testing "custom predicate fn for replacing item"
    (let [replace? (fn [current-item new-item]
                     (< (:priority current-item) (:priority new-item)))
          queue (q/updateable-priority-queue {:replace? replace?})]))
  (testing "can clear and reuse queue"))

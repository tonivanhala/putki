(ns putki.core-test
  (:require [clojure.test :refer [deftest testing is]]
            [malli.core :as malli]
            [putki.core :as p]
            [putki.malli :as m]
            [putki.malli.schemas :as schemas]
            [putki.test-fixtures :as fixtures]
            [putki.util.runtime :as runtime]))

(deftest init-test
  (testing "init graph"
    (let [workflow (p/init fixtures/+linear-graph+)]
      (is (m/valid-workflow? workflow))
      (is (nil? (malli/explain schemas/Workflow workflow)))
      (is (= 4 (-> workflow runtime/get-jobs count)))))
  (testing "multiple parallel pipes"
    (let [graph [identity {:id :a} [identity {:id :aa} [identity {:id :aaa}]]
                 identity {:id :b} [identity {:id :bb}]
                 identity {:id :c}
                 identity {:id :d}]
          workflow (p/init graph)]
      (is (m/valid-workflow? workflow))
      (is (= 7 (-> workflow runtime/get-jobs count)))
      (let [sources (runtime/get-sources workflow)]
        (is (= 4 (count sources)))
        (is (= #{:a :b :c :d} (-> sources keys set))))
      (let [sinks (runtime/get-sinks workflow)]
        (is (= 4 (count sinks)))
        (is (= #{:aaa :bb :c :d} (-> sinks keys set)))))))

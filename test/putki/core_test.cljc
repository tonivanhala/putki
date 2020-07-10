(ns putki.core-test
  (:require [clojure.test :refer [deftest testing is]]
            [malli.core :as malli]
            [putki.core :as p]
            [putki.malli :as m]
            [putki.malli.schemas :as schemas]
            [putki.test-fixtures :as fixtures]))

(deftest init-test
  (testing "init graph"
    (let [workflow (p/init fixtures/+linear-graph+)]
      (is (m/valid-workflow? workflow))
      (is (nil? (malli/explain schemas/Workflow workflow)))
      (is (= 4 (-> workflow p/get-jobs count))))))

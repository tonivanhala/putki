(ns putki.malli-test
  (:require
   [clojure.test :refer [deftest is]]
   [malli.core :as m]
   [putki.malli :as pm]
   [putki.malli.schemas :as schemas]
   [putki.test-fixtures :as fixtures]))

(deftest valid-graph-test
  (is (pm/valid-graph? fixtures/+linear-graph+))
  (is (nil? (m/explain schemas/Graph fixtures/+linear-graph+))))

(deftest valid-workflow-test
  (is (pm/valid-workflow? fixtures/+empty-workflow+))
  (is (nil? (m/explain schemas/Workflow fixtures/+empty-workflow+))))

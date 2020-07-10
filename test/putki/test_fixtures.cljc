(ns putki.test-fixtures)

(def +linear-graph+
  [inc
   [#(/ % 3)
    [#(Math/ceil %)
     [println]]]])

(def +empty-workflow+
  {:refs {:sources []
          :sinks []}
   :workflow {:pipes {}
              :jobs {}}})

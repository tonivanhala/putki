(ns putki.util.data
  #?(:clj (:import [java.util UUID])))

(defn deep-merge
  "Recursively merge nested maps, so that values in same path are appended
   together and nil values are dropped.
   Based on potpuri.core/deep-merge in https://github.com/metosin/potpuri."
  [& xs]
  (let [xs' (filter some? xs)]
    (cond
      (every? map? xs')
      (apply merge-with deep-merge xs')
      (every? coll? xs')
      (reduce into xs')
      (coll? (first xs'))
      (reduce conj (first xs') (rest xs'))
      (seq (rest xs'))
      (if (every? coll? (rest xs'))
        (reduce concat [(first xs')] (rest xs'))
        (reduce conj [(first xs')] (rest xs')))
      :else
      (first xs'))))

(defn gen-uuid
  []
  #?(:clj (UUID/randomUUID)
     :cljs (random-uuid)))

(defn wrap-into
  "Wrap non-collection values into given collection.
   Collections are only put into the collection (non-wrapped).
   Examples:
       (wrap-into [] :a) => [:a]
       (wrap-into [] [:a]) => [:a]
       (wrap-into #{} [:a]) => #{:a}"
  [coll v]
  (into coll (if (coll? v)
               v
               [v])))

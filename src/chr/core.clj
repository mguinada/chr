(ns chr.core
  (:require [clojure.data :as d]))

(defn- nested?
  "Returns true of that are maps contained in `changes`"
  [changes]
  (boolean (some map? (map (fn [[_ old _]] old) changes))))

(defn- dotify
  "Flattens nested data by one level using dot notation."
  [changes]
  (letfn [(tuple-flattener [[field old new :as tuple]]
            (if (map? old)
              (->> old
                   (keys)
                   (map (fn [k]
                          [(keyword (str (name field) "." (name k)))
                           (get old k)
                           (get new k)])))
              (vector tuple)))]
    (->> changes
         (map tuple-flattener)
         (mapcat flatten)
         (partition 3))))

(defn flatten-data
  "Transforms nested data into a flat representation"
  [changes]
  (loop [coll changes]
    (if-not (nested? coll)
      (mapv (partial apply vector) coll)
      (recur (dotify coll)))))

(defn diff
  "Returns a list of vectors for each difference between `m1` and `m2` maps.
   Each difference is represented by the map key, the key's value for `m1`
   followed by the key's value at `m2`"
  [m1 m2]
  (let [[only-a only-b] (d/diff m1 m2)
        diff-tuple (fn [coll [k v]]
                     (conj coll [k v (get only-b k)]))]
    (reduce diff-tuple '() only-a)))

(defn changes
  "Returns a vector describing, per field, the changes between `a` and `b`"
  [a b]
  (letfn [(into-maps [coll] (mapv (partial zipmap [:field :old :new]) coll))]
    (-> (diff a b)
        (flatten-data)
        (into-maps))))

(defn timestamp
  []
  (new java.util.Date))

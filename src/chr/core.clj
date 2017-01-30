(ns chr.core
  (:require [clojure [string :as str] [data :as d]])
  (:import [java.util Calendar TimeZone]))

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
                   (map (fn [k] [(keyword (str/join "." [(name field) (name k)]))
                                 (get-in old [k])
                                 (get-in new [k])])))
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

(defn changes
  "Returns a collection describing, per field, the changes between `a` and `b`"
  [a b]
  (let [[only-a only-b _] (d/diff a b)
        tuples  (fn [coll [k v]] (conj coll [k v (get only-b k)]))
        changes (fn [coll] (zipmap [:field :old :new] coll))]
   (->> only-a
        (reduce tuples '())
        (flatten-data)
        (map changes))))

(defn timestamp
  []
  (new java.util.Date))

(ns chr.core
  (:require [clojure [string :as str] [data :as d]])
  (:import [java.util Calendar TimeZone]))

;; Data processing

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

;; Utilities for working with time

(defn calendar
  "Get an instance of java.util.Calendar"
  []
  (doto (Calendar/getInstance)
    (.setTimeZone (TimeZone/getTimeZone "UTC"))))

(defn set-calendar
  "Sets calendar to a date"
  ([year month day hour minute secs]
   (doto (calendar)
     (.set java.util.Calendar/DAY_OF_MONTH day)
     (.set java.util.Calendar/MONTH (dec month))
     (.set java.util.Calendar/YEAR year)
     (.set java.util.Calendar/HOUR_OF_DAY hour)
     (.set java.util.Calendar/MINUTE minute)
     (.set java.util.Calendar/SECOND secs)
     (.set java.util.Calendar/MILLISECOND 0)))
  ([year month day hour minute]
   (set-calendar year month day hour minute 0))
  ([year month day hour]
   (set-calendar year month day hour 0 0))
  ([year month day]
   (set-calendar year month day 0 0 0)))

(defn time
  "Gets current time from the calendar"
  [cal]
  (.getTime cal))

(defn timestamp
  []
  (new java.util.Date))

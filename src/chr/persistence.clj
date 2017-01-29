(ns chr.persistence
  (:require [somnium.congomongo :as m]))

(def ^:private conn)

(defn connect
  [dbname]
  (m/set-connection!
   (m/make-connection dbname
                      :host "127.0.0.1"
                      :port 27017)))

(defn create!
  [collection model]
  (m/insert! collection model))

(defn mass-insert!
  [collection data]
  (dorun (m/mass-insert! collection data)))

(defn update!
  [collection data new-data]
  (m/update! collection data (merge data new-data)))

(defn drop-coll!
  [collection]
  (m/drop-coll! collection))

(defn find
  [collection id]
  (m/fetch-one collection :where {:_id id}))

(defn count
  [collection]
  (m/fetch-count collection))

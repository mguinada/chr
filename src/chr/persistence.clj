(ns chr.persistence
  (:require [somnium.congomongo :as m]))

(defn connect
  [dbname]
  (m/set-connection!
   (m/make-connection dbname
                      :host "127.0.0.1"
                      :port 27017)))

(defn fetch-one
  [collection id]
  (m/fetch-one collection :where {:_id id}))

(defn mass-insert!
  [collection data]
  (dorun (m/mass-insert! collection data)))

(defn update!
  [collection {id :_id :as data} new-data]
  (m/update! collection {:_id id} (merge data new-data)))

(def insert! m/insert!)
(def drop-coll! m/drop-coll!)
(def fetch-count m/fetch-count)
(def aggregate m/aggregate)
(def object-id m/object-id)

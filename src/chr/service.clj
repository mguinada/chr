(ns chr.service
  (:require [chr.core :as c]
            [chr.persistence :as p]))

(p/connect "challange")

(defn timestamp
  []
  (new java.util.Date))

(defn- update-and-track-changes!
  [{id :_id :as current-data} data]
  (let [changes (c/changes current-data data) t (timestamp)]
    (when-not (empty? changes)
      (do
        (p/mass-insert! :user-changes (map #(assoc % :timestamp t :user-id id) changes))
        (p/update! :users current-data data)))))

(defn update!
  [{id :_id :as data}]
  (if-let [current-data (p/find :users id)]
    (update-and-track-changes! current-data data)
    (throw (ex-info (str "Could not find element " id " at users collection")
                    {:id id :collection :users}))))

(defn save!
  [data]
  (if-let [id (:_id data)]
    (update! data)
    (p/create! :users data)))

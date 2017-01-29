(ns chr.service
  (:require [chr.core :as c]
            [chr.persistence :as p]))

(p/connect "challange")

(defn- update-and-track-changes!
  "Updates data and tracks changes between the current and new values.
   If no changes between data are detected, the database will not be hit
   and `nil` will be returned."
  [{id :_id :as current-data} data]
  (let [changes (c/changes current-data data) t (c/timestamp)]
    (when-not (empty? changes)
      (do
        (p/update! :users current-data data)
        (p/mass-insert! :user-changes (map #(assoc % :timestamp t :user-id id) changes))))))

(defn- update!
  "Fetches the current version of the data from the database and proceeds
   with the update. If the current version of the data is not found, an
   exception will the thrown."
  [{id :_id :as data}]
  (if-let [current-data (p/fetch-one :users id)]
    (update-and-track-changes! current-data data)
    (throw (ex-info (str "Could not find element " id " at users collection")
                    {:id id :collection :users}))))

(defn save!
  "Saves data to the database. If previous versions of the data are
   found at the database, an update will be triggered, if not, an insert
   will be performed"
  [data]
  (if-let [id (:_id data)]
    (update! data)
    (p/insert! :users data)))

(def fetch (partial p/fetch-one :users))

(defn changes
  "Lists changes in a user with id equal to `user-id`
   that occured in a given timespan"
  [user-id t1 t2]
  (let [aggregate-data (p/aggregate
                        :user-changes
                        {:$match
                         {:user-id user-id
                          :timestamp
                          {:$gte t1
                           :$lte t2}}}
                        {:$sort
                         {:timestamp 1}}
                        {:$group
                         {:_id "$field"
                          :new {:$last "$new"}
                          :old {:$first "$old"}
                          :field {:$first "$field"}}})]
    (->> (:result aggregate-data)
         (map #(dissoc % :_id))
         (into []))))

(ns chr.service
  (:require [chr.core :as c]
            [chr.persistence :as p]))

(p/connect "challange")

(defn coerce-obj-id
  [id]
  (if (string? id) (p/object-id id) id))

(defn parse-date
  [iso-8601]
  (-> "yyyy-MM-dd'T'HH:mm:ssX"
      (java.text.SimpleDateFormat.)
      (.parse iso-8601)))

(defn- update-and-track-changes!
  "Updates data and tracks changes between the current and new values.
   If no changes between data are detected, the database will not be hit
   and `nil` will be returned."
  [{id :_id :as current} data]
  (let [changes (c/changes current data) t (c/timestamp)
        id (:_id current)
        id (coerce-obj-id id)]
    (if-not (empty? changes)
      (do
        (p/update! :users current (assoc data :_id id))
        (p/mass-insert! :user-changes (map #(assoc % :timestamp t :user-id id) changes))
        (p/fetch-one :users id)))))

(defn- update!
  "Fetches the current version of the data from the database and proceeds
   with the update. If the current version of the data is not found, an
   exception will the thrown."
  [{id :_id :as data}]
  (if-let [current-data (p/fetch-one :users (coerce-obj-id id))]
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
                         {:user-id (coerce-obj-id user-id)
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

(ns jetcan-server.db.user
  (:require [jetcan-server.util :as util]
            [noir.util.crypt :as crypt]
            [jetcan-server.db.core :refer [db-spec]]
            [yesql.core :refer [defqueries]]
            [clj-time.coerce :refer [to-sql-time]]))


(defn load-queries! []
  (defqueries "sql/queries/user.sql"))
(load-queries!)


(defn create! [id pass name]
  (let [hash (crypt/encrypt pass)
        created (util/datetime)]
    (try
      (do
        (-create-user! db-spec
                       id
                       hash
                       name
                       false
                       (to-sql-time created))
        true)
      (catch Exception e
        (do
          (println e)
          false)))))


(defn create-admin! [id pass name]
  (let [hash (crypt/encrypt pass)
        created (util/datetime)]
    (try
      (do
        (-create-user! db-spec
                       id
                       hash
                       name
                       true
                       (to-sql-time created))
        true)
      (catch Exception e
        (do
          (println e)
          false)))))


(defn exists? [id]
  (let [result (first (-user-exists? db-spec id))]
    (result :exists)))


(defn get-profile [id]
  (let [result (-get-user-profile db-spec id)]
    (first result)))


(defn get-credentials! [id]
  (let [result (-get-user-credentials db-spec id)]
    (first result)))


(defn update! [id new-values]
  (let [name (new-values :name)]
    (do (-update-user! db-spec
                       name
                       id)
        (get-profile id))))


(defn is-admin? [id]
  (let [profile (get-profile id)]
    (= (profile :admin)
       true)))


(defn get-list []
  (let [all-users (-get-user-list db-spec)]
    (vec all-users)))


(defn update-user-disabled-status! [user-id new-status]
  (if (exists? user-id)
    (do
      (-update-user-disabled-status! db-spec
                                       new-status
                                       user-id)
        (get-profile user-id))
    nil))

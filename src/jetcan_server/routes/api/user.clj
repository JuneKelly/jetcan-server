(ns jetcan-server.routes.api.user
  (:use compojure.core)
  (:require [liberator.core :refer [defresource]]
            [jetcan-server.db.user :as user]
            [jetcan-server.db.log :as log]
            [noir.validation :as v]
            [cheshire.core :as json]
            [jetcan-server.routes.api.core :refer [get-current-user]]
            [jetcan-server.validation :refer [user-creation-errors
                                          user-update-errors]]
            [jetcan-server.util :refer [ensure-json]]))


(defn user-resource-exists?
  "Check if there is a user resource matching the id parameter"
  [context]
  (let [params (get-in context [:request :params])]
    (user/exists? (params :id))))


(defn can-access-user?
  "Check context to see if the requested user resource can
   be accessed. Returns boolean"
  [context]
  (let [current-user (get-current-user context)
        requested-user-id (get-in context [:request :route-params :id])
        can-access (= requested-user-id current-user)]
    can-access))


(defn can-edit-user?
  "Check if the user resource can be edited"
  [context]
  (let [current-user (get-current-user context)
        requested-user-id (get-in context [:request :route-params :id])
        can-access (= requested-user-id current-user)]
    (and (not (nil? current-user))
         (not (nil? requested-user-id))
         (or (= requested-user-id current-user)
             (user/is-admin? current-user)))))


(defn current-user-admin?
  "Check that a user account can be created"
  [context]
  (let [current-user (get-current-user context)]
    (and (not (nil? current-user))
         (user/is-admin? current-user))))


(defresource user-read [id]
  :available-media-types ["application/json"]
  :allowed-methods [:get]

  :authorized?
  (fn [context]
    (can-access-user? context))

  :handle-ok
  (fn [context]
    (let [user-id (get-in context [:request :route-params :id])
          user-profile (user/get-profile user-id)]
      (do
        (log/info {:event "user:access"
                   :user user-id})
        (json/generate-string user-profile)))))


(defresource user-list
  :available-media-types ["application/json"]
  :allowed-methods [:get]

  :authorized?
  current-user-admin?

  :handle-ok
  (fn [context]
    (let [all-users (user/get-list)]
      (do
        (log/info {:event "userlist:access"
                   :user (get-current-user context)})
        (json/generate-string all-users)))))


(defresource user-update [id]
  :available-media-types ["application/json"]
  :allowed-methods [:post]

  :authorized?
  can-edit-user?

  :exists?
  user-resource-exists?

  :can-post-to-missing?
  false

  :malformed?
  (fn [context]
    (let [params (get-in context [:request :params])
          method (get-in context [:request :request-method])]
      (if (= method :post)
        (let [errors (user-update-errors params)]
          (if (empty? errors)
            false
            [true (ensure-json {:errors errors})]))
        false)))

  :handle-malformed
  (fn [context]
    {:errors (:errors context)})

  :post!
  (fn [context]
    (let [params (get-in context  [:request :params])
          id (:id params)
          name (:name params)
          password (:password params)
          new-profile (user/update! id params)]
      (do
        (log/info {:event "user:update"
                   :user id})
        {:user-profile new-profile})))

  :new? ;; updates are never new resources
  false

  :respond-with-entity? true

  :multiple-representations? false

  :handle-ok
  (fn [context]
    (json/generate-string (:user-profile context))))


(defresource user-create
  :available-media-types ["application/json"]
  :allowed-methods [:post]

  :authorized?
  current-user-admin?

  :malformed?
  (fn [context]
    (let [params (get-in context [:request :params])
          method (get-in context [:request :request-method])]
      (if (= method :post)
        (let [errors (user-creation-errors params)]
          (if (empty? errors)
            false
            [true (ensure-json {:errors errors})]))
        false)))

  :handle-malformed
  (fn [context]
    {:errors (context :errors)})

  :exists?
  user-resource-exists?

  :allowed?
  (fn [context] (not (user-resource-exists? context)))

  :post!
  (fn [context]
    (let [params (get-in context  [:request :params])
          id (:id params)
          name (:name params)
          password (:password params)
          success (user/create! id
                                password
                                name)]
      (if success
        {:user-profile (user/get-profile id)}
        {:error "Could not register user"})))

  :handle-created
  (fn [context]
    (do
      (log/info {:event "user:registration"
                 :user (get-in context [:user-profile :id])})
      (json/generate-string {:userProfile (:user-profile context)}))))

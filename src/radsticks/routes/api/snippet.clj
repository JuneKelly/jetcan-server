(ns radsticks.routes.api.snippet
  (:use compojure.core)
  (:require [liberator.core :refer [defresource]]
            [radsticks.db.user :as user]
            [radsticks.db.log :as log]
            [radsticks.db.snippet :as snippet]
            [noir.validation :as v]
            [cheshire.core :as json]
            [radsticks.routes.api.common :refer [get-current-user]]
            [radsticks.util :refer [ensure-json]]))


(defn is-snippet-owner-authenticated?
  "Checks the request context to see if the currently
   authenticated user is the owner of the snippet
   resource. returns boolean"
  [context]
  (let [current-user (get-current-user context)
        snippet-id (get-in context [:request :route-params :id])
        owner (snippet/get-snippet-owner snippet-id)]
    (and (not (nil? current-user))
         (= current-user owner))))


(defn is-authenticated?
  "Check if there is a valid auth token in context"
  [context]
  (not (nil? (get-current-user context))))


(defn can-access-snippet? [context]
  (let [method (get-in context [:request :request-method])]
    (if (contains? #{:get :put :delete} method)
      (is-snippet-owner-authenticated? context)
      (is-authenticated? context))))


(defn post-malformed? [context]
  (comment "todo"))


(defn put-malformed? [context]
  (comment "todo"))


(defresource snippet [id]
  :available-media-types ["application/json"]
  :allowed-methods [:get :post :delete :put]

  :authorized?
  can-access-snippet?

  :allowed?
  (fn [context]
    (let [snippet-id (get-in context [:request :route-params :id])
          method (get-in context [:request :request-method])]
      (if (contains? #{:get :put :delete} method)
        (snippet/exists? snippet-id)
        true)))

  :exists?
  (fn [context]
    (let [snippet-id (get-in context [:request :route-params :id])]
      (snippet/exists? snippet-id)))

  :can-put-to-missing?
  false

  :malformed?
  (fn [context]
    (let [method (get-in context [:request :request-method])]
      (cond
       (= :post method)
       (post-malformed? context)
       (= :put method)
       (put-malformed? context)
       :else
       false)))

  :conflict?
  (fn [context]
    (comment "todo, with the put action"))

  :post!
  (fn [context]
    (let [params (get-in context [:request :params])
          snippet-id (snippet/create! (:user params)
                                      (:content params)
                                      (:tags params))]
      {:snippet-id snippet-id}))

  :put!
  (fn [context]
    (comment "todo"))

  :delete!
  (fn [context]
    (comment "todo"))

  :handle-ok
  (fn [context]
    (let [snippet-id (get-in context [:request :route-params :id])
          snippet-data (snippet/get-snippet snippet-id)]
      (json/generate-string snippet-data)))

  )
(ns radsticks.routes.api.auth
  (:use compojure.core)
  (:require [liberator.core :refer [defresource]]
            [noir.validation :as v]
            [radsticks.auth :as auth]
            [radsticks.db.log :as log]
            [radsticks.util :refer [ensure-json rep-map]]))


(defn get-auth-errors
  "Validate that data submitted to auth endpoint is correct."
  [params]
  (let [email (params :email)
        password (params :password)]
    (v/rule (v/has-value? email)
            [:email "email is required"])
    (v/rule (v/has-value? password)
            [:password "password is required"])
    (v/rule (string? email)
            [:email "email must be a string"])
    (v/rule (string? password)
            [:password "password must be a string"])
    (v/get-errors)))


(defresource authentication
  :available-media-types ["application/json"]
  :allowed-methods [:post]

  :malformed?
  (fn [context]
    (let [params (get-in context  [:request :params])
          errors (get-auth-errors params)]
      (if (empty? errors)
        false
        [true (ensure-json {:errors errors})])))

  :handle-malformed
  (fn [context]
    {:errors (context :errors)})

  :allowed?
  (fn [context]
    (let [params (get-in context [:request :params])
          email (params :email)
          password (params :password)
          token (auth/authenticate-user email password)]
      (if (not (nil? token))
        [true, {:payload
                {:email email, :token token}}]
        false)))

  :post!
  (fn [context] (comment "pass"))

  :handle-created
  (fn [context]
    (do
      (log/info {:event "authenticated"
                 :user (get-in context [:payload :email])})
      (context :payload))))

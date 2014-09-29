(ns jetcan-server.validation
  (:require [validateur.validation :refer :all]))


(defn snippet-errors [data]
  (let [validate (validation-set
                  (presence-of :id)
                  (presence-of :user_id)
                  (presence-of :content)
                  (presence-of :tags)
                  (presence-of :updated)
                  (presence-of :created))]
    (validate data)))


(defn snippet-creation-errors [data]
  (let [validate (validation-set
                  (presence-of :user)
                  (presence-of :content)
                  (presence-of :tags))]
    (validate data)))


(defn user-creation-errors [data]
  (let [validate (validation-set
                  (presence-of :id)
                  (validate-with-predicate :id
                                           #(string? (:id %)))
                  (presence-of :password)
                  (presence-of :name))]
    (validate data)))


(defn user-disabled-errors [data]
  (let [validate (validation-set
                  (presence-of :disabled)
                  (validate-with-predicate :disabled
                                           #(contains? #{true false} (:disabled %))))]))


(defn user-update-errors [data]
  (let [validate (validation-set
                  (presence-of :name)
                  (validate-with-predicate :name
                                           #(string? (:name %)))
                  (presence-of :id))]
    (validate data)))


(defn auth-errors [data]
  (let [validate (validation-set
                  (presence-of :id)
                  (validate-with-predicate :id
                                           #(string? (:id %)))
                  (presence-of :password)
                  (validate-with-predicate :password
                                           #(string? (:password %))))]
    (validate data)))

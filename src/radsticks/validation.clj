(ns radsticks.validation
  (:require [validateur.validation :refer :all]))


(defn get-snippet-errors [data]
  (let [validate (validation-set
                  (presence-of :id)
                  (presence-of :user_id)
                  (presence-of :content)
                  (presence-of :tags)
                  (presence-of :updated)
                  (presence-of :created))]
    (validate data)))


(defn get-snippet-creation-errors [data]
  (let [validate (validation-set
                  (presence-of :user)
                  (presence-of :content)
                  (presence-of :tags))]
    (validate data)))


(def email-regex
  #"(?i)[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]")


(defn get-user-creation-errors [data]
  (let [validate (validation-set
                  (presence-of :email)
                  (format-of :email
                             :format email-regex
                             :message "must be an email address")
                  (presence-of :password)
                  (presence-of :name))]
    (validate data)))


(defn get-user-update-errors [data]
  (let [validate (validation-set
                  (presence-of :name))]
    (validate data)))
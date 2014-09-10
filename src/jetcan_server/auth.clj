(ns jetcan-server.auth
  (require [jetcan-server.db.user :as user]
           [environ.core  :refer [env]]
           [noir.util.crypt :as crypt]
           [clj-jwt.core  :refer :all]
           [clj-jwt.key   :refer [private-key]]
           [clj-time.core :refer [now plus days months before? after?]]
           [clj-time.coerce :refer [from-long]]))


(defn- secret [] (env :secret))


;;------------------------
;; Generate a token
;;------------------------
(defn generate-token [claim]
  (-> claim
      jwt
      (sign :HS256 (secret))
      to-str))


(defn user-claim [user-id]
  (let [user-doc (user/get-profile user-id)
        expiration (plus (now) (months 3))]
    (if user-doc
      {:user-id (user-doc :id)
       :name  (user-doc :name)
       :exp expiration
       :nbf (now)}
      nil)))


(defn user-credentials-valid? [user-id password]
  (let [user-creds (user/get-credentials! user-id)]
    (and (not (nil? user-creds))
         (crypt/compare password (user-creds :password)))))


(defn authenticate-user [user-id password]
  (if (user-credentials-valid? user-id password)
    (generate-token (user-claim user-id))
    nil))


;;------------------------
;; Validate a token
;;------------------------
(defn decode-token [token-string]
  (try
    (-> token-string str->jwt)
    (catch Exception e
      nil)))


(defn get-user-user-id [decoded-token]
  (get-in decoded-token [:claims :user-id]))


(defn token-valid? [decoded-token]
  (let [exp (from-long (* 1000 (get-in decoded-token [:claims :exp])))
        nbf (from-long (* 1000 (get-in decoded-token [:claims :nbf])))
        current-time (now)]
    (and (-> decoded-token (verify (secret)))
         (not (before? exp current-time))
         (not (after? nbf current-time))
         (user/exists? (get-user-user-id decoded-token)))))


(defn validate-user [token-string]
  (let [token (decode-token token-string)]
    (if (and token (token-valid? token))
      (get-user-user-id token)
      nil)))

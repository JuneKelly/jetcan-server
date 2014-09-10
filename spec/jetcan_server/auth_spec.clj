(ns jetcan-server.auth-spec
  (:require [jetcan-server.auth :as auth]
            [jetcan-server.test-utils :as util]
            [speclj.core :refer :all]
            [clj-jwt.core  :refer :all]
            [clj-time.core :as ti]
            [clj-time.coerce :as tc]))


(defn reset-db []
  (do
    (util/reset-db!)
    (util/populate-users!)))


(describe
  "claims"

  (before
   (do (reset-db)))

  (it "should produce a map containing claims for a user"
      (let [user-id "userone@example.com"
            current-time (ti/now)
            claim (auth/user-claim user-id)]
        (should (map? claim))
        (should (contains? claim :user-id))
        (should (string? (claim :user-id)))
        (should (contains? claim :name))
        (should (string? (claim :name)))
        (should (contains? claim :exp))
        (should (= (class (claim :exp)) org.joda.time.DateTime))
        (should (contains? claim :nbf))
        (should (= (class (claim :nbf)) org.joda.time.DateTime))
        (should (ti/after? (claim :exp) current-time))))

  (it "should return nil for a bad id"
    (let [user-id "goose@example.com"
          current-time (ti/now)
          claim (auth/user-claim user-id)]
      (should (nil? claim)))))


(describe
  "token validation"

  (before
    (do (reset-db)))

  ;; known user, should succeed
  (it "should succeed for a known-good token"
    (let [token util/good-token
          result (auth/validate-user token)]
      (should (not (nil? result)))
      (should (string? result))
      (should (= "userone@example.com" result))))

  ;; known user with expired token, should fail
  (it "should fail for an expired token"
    (let [token util/expired-token
          result (auth/validate-user token)]
      (should (nil? result))
      (should (not (string? result)))
      (should (not (= "userone@example.com" result)))))

  ;; unknown user, should fail
  (it "should fail for a token for an unknown user"
    (let [token util/invalid-user-token
          result (auth/validate-user token)]
      (should (nil? result))
      (should (not (string? result)))
      (should (not (= "notauser@example.com" result)))))

  (it "should fail with an unsigned token"
      (let [token (-> (auth/user-claim "userone@example.com")
                      jwt
                      to-str)
            result (auth/validate-user token)]
        (should (nil? result))))

  (it "should fail with a token we did not sign"
      (let [token (-> (auth/user-claim "userone@example.com")
                      jwt
                      (sign :HS256 "obviously_not_our_secret")
                      to-str)
            result (auth/validate-user token)]
        (should (nil? result))))

  (it "should fail for a garbage string"
      (let [token "this.token.blows."
            result (auth/validate-user token)]
        (should (nil? result)))))


(run-specs)

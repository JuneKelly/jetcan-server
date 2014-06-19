(ns jetcan-server.api-auth-spec
  (:require [jetcan-server.test-utils :as util]
            [speclj.core :refer :all]
            [peridot.core :refer :all]
            [jetcan-server.handler :refer :all]
            [jetcan-server.db.user :as user]
            [cheshire.core :refer [generate-string
                                   parse-string]]))


(defn auth-request! [body]
  (util/api-json-request! {:route "/api/auth"
                          :method :post
                          :body body}))


(describe
  "auth api"

  (before
   (do (util/reset-db!)
       (util/populate-users!)))

  (it "should issue a token when credentials are correct"
      (let [request (auth-request! {:id "userone@example.com"
                                    :password "password1"})
            response (:response request)
            response-json (parse-string (response :body) true)]
        (should (= "application/json;charset=UTF-8"
                   (get (:headers response) "Content-Type")))
        (should (= (:status response) 201))
        (should (contains? response-json :token))
        (should (string? (response-json :token)))
        (should (< 0 (count (response-json :token))))
        (should-contain :profile response-json)
        (should (= "userone@example.com"
                   (get-in response-json [:profile :id])))))

  (it "should fail to authenticate when id is unknown"
      (let [request (auth-request! {:id "gooser@example.com"
                                    :password "lol"})
            response (:response request)]
        (should (= "text/plain"
                   (get (:headers response) "Content-Type")))
        (should (not (= (:status response) 201)))
        (should (= (:status response) 403))
        (should (= "Forbidden." (response :body)))))

  (it "should fail to authenticate when password is incorrect"
      (let [request (auth-request! {:id "userone@example.com"
                                    :password "lol"})
            response (:response request)]
        (should (= "text/plain"
                   (get (:headers response) "Content-Type")))
        (should (not (= (:status response) 201)))
        (should (= (:status response) 403))
        (should (= "Forbidden." (response :body)))))

  (it "should fail when user id is not submitted"
      (let [request (auth-request! {:derp "userone@example.com"
                                    :password "password1"})
            response (:response request)
            response-json (parse-string (response :body) true)]
        (should (= "application/json;charset=UTF-8"
                   (get (:headers response) "Content-Type")))
        (should (not (= (:status response) 201)))
        (should (= (:status response) 400))
        (should (not (contains? response-json :token)))
        (should (contains? response-json :errors))
        (should (map? (response-json :errors)))
        (should== {:id ["is invalid" "can't be blank"]}
                  (:errors response-json))))

  (it "should fail when password is not submitted"
      (let [request (auth-request! {:id "userone@example.com"
                                    :derp "password1"})
            response (:response request)
            response-json (parse-string (response :body) true)]
        (should (= "application/json;charset=UTF-8"
                   (get (:headers response) "Content-Type")))
        (should (not (= (:status response) 201)))
        (should (= (:status response) 400))
        (should (not (contains? response-json :token)))
        (should (contains? response-json :errors))
        (should (map? (response-json :errors)))
        (should== {:password ["is invalid" "can't be blank"]}
                  (:errors response-json))))

  (it "should fail when the supplied id is not a string"
      (let [request (auth-request! {:id [1 2 3]
                                    :password "password1"})
            response (:response request)
            response-json (parse-string (response :body) true)]
        (should (= "application/json;charset=UTF-8"
                   (get (:headers response) "Content-Type")))
        (should (not (= (:status response) 201)))
        (should (= (:status response) 400))
        (should (not (contains? response-json :token)))
        (should (contains? response-json :errors))
        (should (map? (response-json :errors)))
        (should== {:id ["is invalid"]}
                  (:errors response-json))))

  (it "should fail when password is not a string"
      (let [request (auth-request! {:id "userone@example.com"
                                    :password true})
            response (:response request)
            response-json (parse-string (response :body) true)]
        (should (= "application/json;charset=UTF-8"
                   (get (:headers response) "Content-Type")))
        (should (not (= (:status response) 201)))
        (should (= (:status response) 400))
        (should (not (contains? response-json :token)))
        (should (contains? response-json :errors))
        (should (map? (response-json :errors)))
        (should== {:password ["is invalid"]}
                  (:errors response-json)))))


(run-specs)

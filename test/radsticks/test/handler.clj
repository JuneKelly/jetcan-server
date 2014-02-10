(ns radsticks.test.handler
  (:use clojure.test
        peridot.core
        radsticks.handler)
  (:require [radsticks.test.test-util :as util]
            [cheshire.core :refer [generate-string
                                   parse-string]]))


(deftest test-routes
  (testing "main route"
    (let [response (:response
                     (-> (session app)
                         (request "/")))]
      (is (= (response :status) 200))))

  (testing "not-found route"
    (let [response (:response
                     (-> (session app)
                         (request "/invalid/route")))]
      (is (= (:status response) 404)))))


(deftest test-api
  (testing "auth api"
    (do
      (util/drop-database!)
      (util/populate-users!)

      ;; successful authentication
      (let [request-body
            "{\"username\":\"userone@example.com\",
              \"password\":\"password1\"}"
            request (-> (session app)
                        (content-type "application/json")
                        (request "/api/auth"
                                 :request-method :post
                                 :body request-body))
            response (:response request)
            response-json (parse-string (response :body) true)]
        (is (= "application/json;charset=UTF-8"
               (get (:headers response) "Content-Type")))
        (is (= (:status response) 201))
        (is (contains? response-json :token))
        (is (= (class (response-json :token)) java.lang.String))
        (is (< 0 (count (response-json :token))))
        (is (= "userone@example.com" (response-json :username))))

      ;; failed authentication, bad password
      (let [request-body
            "{\"username\":\"userone@example.com\",
              \"password\":\"lol\"}"
            request (-> (session app)
                        (content-type "application/json")
                        (request "/api/auth"
                                 :request-method :post
                                 :body request-body))
            response (:response request)]
        (is (= "text/plain"
               (get (:headers response) "Content-Type")))
        (is (not (= (:status response) 201)))
        (is (= (:status response) 403))
        (is (= "Forbidden." (response :body))))

      ;; malformed request, missing username
      (let [request-body
            "{\"derp\":\"userone@example.com\",
              \"password\":\"password1\"}"
            request (-> (session app)
                        (content-type "application/json")
                        (request "/api/auth"
                                 :request-method :post
                                 :body request-body))
            response (:response request)
            response-json (parse-string (response :body) true)]
        (is (= "application/json;charset=UTF-8"
               (get (:headers response) "Content-Type")))
        (is (not (= (:status response) 201)))
        (is (= (:status response) 400))
        (is (not (contains? response-json :token)))
        (is (contains? response-json :error))
        (is (= (class (response-json :error)) java.lang.String)))

      ;; malformed request, missing password
      (let [request-body
            "{\"username\":\"userone@example.com\",
              \"derp\":\"password1\"}"
            request (-> (session app)
                        (content-type "application/json")
                        (request "/api/auth"
                                 :request-method :post
                                 :body request-body))
            response (:response request)
            response-json (parse-string (response :body) true)]
        (is (= "application/json;charset=UTF-8"
               (get (:headers response) "Content-Type")))
        (is (not (= (:status response) 201)))
        (is (= (:status response) 400))
        (is (not (contains? response-json :token)))
        (is (contains? response-json :error))
        (is (= (class (response-json :error)) java.lang.String)))

      ;; malformed request, username is not string
      (let [request-body
            "{\"username\":[1,2,3],
              \"password\":\"password1\"}"
            request (-> (session app)
                        (content-type "application/json")
                        (request "/api/auth"
                                 :request-method :post
                                 :body request-body))
            response (:response request)
            response-json (parse-string (response :body) true)]
        (is (= "application/json;charset=UTF-8"
               (get (:headers response) "Content-Type")))
        (is (not (= (:status response) 201)))
        (is (= (:status response) 400))
        (is (not (contains? response-json :token)))
        (is (contains? response-json :error))
        (is (= (class (response-json :error)) java.lang.String)))

      ;; malformed request, password is not string
      (let [request-body
            "{\"username\":\"userone@example.com\",
              \"password\":true}"
            request (-> (session app)
                        (content-type "application/json")
                        (request "/api/auth"
                                 :request-method :post
                                 :body request-body))
            response (:response request)
            response-json (parse-string (response :body) true)]
        (is (= "application/json;charset=UTF-8"
               (get (:headers response) "Content-Type")))
        (is (not (= (:status response) 201)))
        (is (= (:status response) 400))
        (is (not (contains? response-json :token)))
        (is (contains? response-json :error))
        (is (= (class (response-json :error)) java.lang.String))))))

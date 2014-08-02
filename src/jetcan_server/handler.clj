(ns jetcan-server.handler
  (:require [compojure.core :refer [defroutes]]
            [jetcan-server.routes.core  :refer [api-routes home-routes]]
            [jetcan-server.db.user :as user]
            [noir.util.middleware :as middleware]
            [compojure.route :as route]
            [taoensso.timbre :as timbre]
            [com.postspectacular.rotor :as rotor]
            [selmer.parser :as parser]
            [environ.core :refer [env]]))


(defroutes app-routes
  (route/resources "/")
  (route/not-found "Not Found"))


(defn- create-default-user
  "Create a default admin user, to bootstrap the service"
  []
  (if (not (user/exists? "admin"))
    (do
      (timbre/info "Setting up default user account admin:password")
      (user/create-admin! "admin"
                          "password"
                          "Admin"))))


(defn init
  "init will be called once when
   app is deployed as a servlet on
   an app server such as Tomcat
   put any initialization code here"
  []
  (create-default-user)
  (timbre/set-config!
    [:appenders :rotor]
    {:min-level :info
     :enabled? true
     :async? false ; should be always false for rotor
     :max-message-per-msecs nil
     :fn rotor/append})

  (timbre/set-config!
    [:shared-appender-config :rotor]
    {:path "jetcan-server.log" :max-size (* 512 1024) :backlog 10})

  (if (env :selmer-dev) (parser/cache-off!))
  (timbre/info "jetcan-server started successfully"))


(defn destroy
  "destroy will be called when your application
   shuts down, put any clean up code here"
  []
  (timbre/info "jetcan-server is shutting down..."))


(defn template-error-page [handler]
  (if (env :selmer-dev)
    (fn [request]
      (try
        (handler request)
        (catch clojure.lang.ExceptionInfo ex
          (let [{:keys [type error-template] :as data} (ex-data ex)]
            (if (= :selmer-validation-error type)
              {:status 500
               :body (parser/render error-template data)}
              (throw ex))))))
    handler))


(def app (middleware/app-handler
           ;; add your application routes here
           [home-routes api-routes app-routes]
           ;; add custom middleware here
           :middleware [template-error-page]
           ;; add access rules here
           :access-rules []
           ;; serialize/deserialize the following data formats
           ;; available formats:
           ;; :json :json-kw :yaml :yaml-kw :edn :yaml-in-html
           :formats [:json-kw :edn]))

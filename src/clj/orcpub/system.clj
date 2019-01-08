(ns orcpub.system
  (:require [com.stuartsierra.component :as component]       
            [reloaded.repl :refer [init start stop go reset]] 
            [io.pedestal.http :as http]                      
            [orcpub.pedestal :as pedestal]                                       
            [orcpub.routes :as routes]
            [orcpub.datomic :as datomic]
            [environ.core :as environ])
  (:import (org.eclipse.jetty.server.handler.gzip GzipHandler)))

(def dev-service-map-overrides
  {::http/port 8890
   ;; do not block thread that starts web server
   ::http/join? false
   ;; Routes can be a function that resolve routes,
   ;;  we can use this to set the routes to be reloadable
   ::http/routes #(deref #'routes/routes)
   ;; all origins are allowed in dev mode
   ::http/allowed-origins {:creds true :allowed-origins (constantly true)}})

(def prod-service-map
  {::http/routes routes/routes
   ::http/type :jetty
   ::http/port (let [port-str (System/getenv "PORT")]
                 (if port-str (Integer/parseInt port-str)))
   ::http/join false
   ::http/resource-path "/public"
   ::http/container-options {:context-configurator (fn [c]
                                                     (let [gzip-handler (GzipHandler.)]
                                                       (.setGzipHandler c gzip-handler)
                                                       c))}})

(defn system [env]
  (component/system-map
    :conn
    (datomic/new-datomic
      (if-let [datomic-url (:datomic-url environ/env)]
        ; (str datomic-url "?aws_access_key_id=" (environ/env :datomic-access-key) "&aws_secret_key=" (environ/env :datomic-secret-key))
        (str datomic-url)
        (when true #_(= :dev env)
          (println "WARN: no :datomic-url environment variable set; using local dev")
          "datomic:free://localhost:4334/orcpub")))

    :service-map
    (cond-> (merge
              {:env env}
              prod-service-map
              (if (= :dev env) dev-service-map-overrides))
      true http/default-interceptors
      (= :dev env) http/dev-interceptors)

    :pedestal
    (component/using
      (pedestal/new-pedestal)
      [:service-map :conn])))

(reloaded.repl/set-init! #(system :prod))

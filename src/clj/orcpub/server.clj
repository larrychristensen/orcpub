(ns orcpub.server
  (:require [io.pedestal.http :as http]          
            [io.pedestal.http.route :as route]
            [io.pedestal.test :as test]
            [io.pedestal.http.ring-middlewares :as ring]
            [io.pedestal.http.body-params :as body-params]
            [io.pedestal.interceptor.error :as error-int]
            [io.pedestal.interceptor.chain :refer [terminate]]
            [com.stuartsierra.component :as component]
            [buddy.auth.protocols :as proto]
            [buddy.auth.backends :as backends]
            [buddy.sign.jwt :as jwt]
            [buddy.hashers :as hashers]
            [clojure.java.io :as io]
            [clj-time.core :refer [hours from-now]]
            [clojure.string :as s]
            [orcpub.dnd.e5.skills :as skill5e]
            [orcpub.dnd.e5.character :as char5e]
            [datomic.api :as d])
  (:import (org.apache.pdfbox.pdmodel.interactive.form PDCheckBox PDComboBox PDListBox PDRadioButton PDTextField)
           (org.apache.pdfbox.pdmodel PDDocument PDPageContentStream)
           (org.apache.pdfbox.pdmodel.graphics.image PDImageXObject)
           (java.io ByteArrayOutputStream ByteArrayInputStream)
           (org.apache.pdfbox.pdmodel.graphics.image JPEGFactory LosslessFactory)
           (org.eclipse.jetty.server.handler.gzip GzipHandler)
           (javax.imageio ImageIO)
           (java.net URL))
  (:gen-class))

(defn response [status body & {:as headers}]
  {:status status :body body :headers headers})

(def ok       (partial response 200))
(def created  (partial response 201))
(def accepted (partial response 202))

(def database (atom {"larry" {:username "larry"
                              :password "noentry4u"
                              :first-and-last-name "Larry Christensen"}}))

(def db-uri "datomic:dev://localhost:4334/orcpub")

#_(def conn (d/connect db-uri))
(def conn)

(defn make-list [nm]
  {:name  nm
   :items {}})

#_(def service
  {::http/routes #(deref #'routes)
   ::http/type :jetty
   ::http/port (let [port (System/getenv "PORT")]
                 (if (clojure.string/blank? port)
                   8890
                   (Integer/parseInt port)))
   ::http/resource-path "/public"
   ::http/container-options {:context-configurator (fn [c]
                                                     (let [gzip-handler (GzipHandler.)]
                                                       (.setGzipHandler c gzip-handler)
                                                       c))}})

(defn start []
  #_(http/start (http/create-server service)))

(defonce server (atom nil))

#_(defn start-dev
  "The entry-point for 'lein run-dev'"
  [& args]
  (println "\nCreating your [DEV] server...")
  (reset!
   server
   (-> service ;; start with production configuration
       (merge :env :dev
              ;; do not block thread that starts web server
              ::http/join? false
              ;; Routes can be a function that resolve routes,
              ;;  we can use this to set the routes to be reloadable
              ::http/routes #(deref #'routes)
              ;; all origins are allowed in dev mode
              ::http/allowed-origins {:creds true :allowed-origins (constantly true)})
       ;; Wire up interceptor chains
       http/default-interceptors
       http/dev-interceptors
       http/create-server
       http/start)))

(defn stop-dev []
  (http/stop @server))

#_(defn restart []
  (stop-dev)
  (start-dev))

(defn test-request [verb url & [body]]
  (io.pedestal.test/response-for (::http/service-fn @server) verb url :body body))

(defn -main []
  (start))


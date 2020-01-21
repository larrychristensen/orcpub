(ns user
  (:require [clojure.java.io :as io]
            [com.stuartsierra.component :as component]
            [figwheel-sidecar.repl-api :as f]
            [datomic.api :as datomic]
            [orcpub.routes :as r]
            [orcpub.system :as s]
            [orcpub.db.schema :as schema]))

(alter-var-root #'*print-length* (constantly 50))

;; user is a namespace that the Clojure runtime looks for and
;; loads if its available

;; You can place helper functions in here. This is great for starting
;; and stopping your webserver and other development services

;; The definitions in here will be available if you run "lein repl" or launch a
;; Clojure repl some other way

;; You have to ensure that the libraries you :require are listed in your dependencies

;; Once you start down this path
;; you will probably want to look at
;; tools.namespace https://github.com/clojure/tools.namespace
;; and Component https://github.com/stuartsierra/component

(defonce -server (atom nil))

(defmacro with-db
  "Convenience util to get access to the datomic conn and/or db
   objects. Call as:
   (with-db [conn db]
     (do-stuff-to db)
   You can also just do (with-db [db]) or (with-db [conn])"
  [init-vector & body]
  `(if-let [system-map# @-server]
     ; first :conn here is a DatomicComponent;
     ; the second is the actual connection object
     (let [conn# (->> system-map# :conn :conn)
           db# (datomic/db conn#)

           ; unpack the requested values:
           {:keys ~init-vector} {:conn conn#
                                 :db db#}]
       ~@body)

     ;; nothing in -server:
     (throw (IllegalStateException. "Call (start-server) first"))))

(defn- project-form
  []
  (with-open [r (java.io.PushbackReader. (io/reader "project.clj"))]
    (binding [*read-eval* false]
      (loop [form (read r)]
        (if (= (first form) 'defproject)
          form
          (recur (read r)))))))

(defn get-cljs-build
  [id]
  (let [project-config (->> (project-form)
                            (drop 1)
                            (apply hash-map))
        build (->> project-config
                   :cljsbuild
                   :builds
                   (filter #(= id (:id %)))
                   first)]
    (prn "BUILD" build)
    [build]))

(defn init-database
  ([]
   (init-database :free))
  ([mode]
   (when-not (contains? #{:free :dev :mem} mode)
     (throw (IllegalArgumentException. (str "Unknown db type " mode))))
   (let [db-uri (str "datomic" mode "://localhost:4334/orcpub")]
     (datomic/create-database db-uri)
     (let [conn (datomic/connect db-uri)]
       (datomic/transact conn schema/all-schemas)))))

(defn stop-server
  []
  (when-let [s @-server]
    (component/stop s)
    (reset! -server nil)))

(defn start-server
  []
  ; restart
  (stop-server)
  (reset! -server (component/start (s/system :dev))))

(defn verify-new-user
  "Automatically mark a user as `verified`. Useful for local testing
   since the email never gets sent."
  [username-or-email]
  (with-db [conn db]
    (let [user (r/find-user-by-username-or-email db username-or-email)
          verification-key (:orcpub.user/verification-key user)]
      (r/verify {:query-params {:key verification-key}
                 :conn conn
                 :db db}))))

(defn fig-start
  "This starts the figwheel server and watch based auto-compiler.

  Afterwards, call (cljs-repl) to connect."
  ([]
   (fig-start "dev"))
  ([build-id]
   ;; this call will only work as long as your :cljsbuild and
   ;; :figwheel configurations are at the top level of your project.clj
   ;; and are not spread across different lein profiles

   ;; otherwise you can pass a configuration into start-figwheel! manually
   (f/start-figwheel!
     {:figwheel-options {}
      :build-ids [build-id]
      :all-builds (get-cljs-build build-id)})))

(defn fig-stop
  "Stop the figwheel server and watch based auto-compiler."
  []
  (f/stop-figwheel!))

;; if you are in an nREPL environment you will need to make sure you
;; have setup piggieback for this to work
(defn cljs-repl
  "Launch a ClojureScript REPL that is connected to your build and host environment.

  (NB: Call fig-start first.)"
  []
  (f/cljs-repl))

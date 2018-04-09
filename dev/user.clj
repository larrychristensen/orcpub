(ns user
  (:require [figwheel-sidecar.repl-api :as f]
            [com.stuartsierra.component :as component]
            [datomic.api :as datomic]
            [orcpub.system :as s]
            [orcpub.db.schema :as schema]))

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

(defn get-cljs-builds
  [id]
  (let [project-config (->> "project.clj"
                            slurp
                            read-string
                            (drop 1)
                            (apply hash-map))
        build (get-in project-config
                      [:cljsbuild :builds id])]
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

(defn fig-start
  "This starts the figwheel server and watch based auto-compiler."
  []
  ;; this call will only work are long as your :cljsbuild and
  ;; :figwheel configurations are at the top level of your project.clj
  ;; and are not spread across different lein profiles

  ;; otherwise you can pass a configuration into start-figwheel! manually
  (f/start-figwheel!
   {:figwheel-options {}
    :build-ids ["web"]
    :all-builds (get-cljs-builds "web")}))

(defn fig-stop
  "Stop the figwheel server and watch based auto-compiler."
  []
  (f/stop-figwheel!))

;; if you are in an nREPL environment you will need to make sure you
;; have setup piggieback for this to work
(defn cljs-repl
  "Launch a ClojureScript REPL that is connected to your build and host environment."
  []
  (f/cljs-repl))

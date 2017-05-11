(ns orcpub.pedestal
  (:require [com.stuartsierra.component :as component]
            [io.pedestal.http :as http]
            [datomic.api :as d]))

(defn test?
  [service-map]
  (= :test (:env service-map)))

(defn db-interceptor [conn]
  {:name :db-interceptor
   :enter (fn [context]
            (let [conn (:conn conn)]
              (let [db (d/db conn)]
                (prn "CONN" conn db)
                (update context :request assoc :db db :conn conn))))})

(defrecord Pedestal [service-map conn service]
  component/Lifecycle

  (start [this]
    (if service
      this
      (cond-> service-map
        true (update ::http/interceptors conj (db-interceptor conn))
        true http/create-server
        (not (test? service-map)) http/start
        true ((partial assoc this :service)))))

  (stop [this]
    (when (and service (not (test? service-map)))
      (http/stop service))
    (assoc this :service nil)))

(defn new-pedestal
  []
  (map->Pedestal {}))

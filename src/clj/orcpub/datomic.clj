(ns orcpub.datomic
  (:require [com.stuartsierra.component :as component]
            [datomic.api :as d]
            [orcpub.db.schema :as schema]))

(defrecord DatomicComponent [uri conn]
  component/Lifecycle
  (start [this]
    (if (:conn this)
      this
      (do
        (d/create-database uri)
        (let [connection (d/connect uri)]
          (d/transact connection schema/all-schemas)
          (assoc this :conn connection)))))
  (stop [this]
    (assoc this :conn nil)))

(defn new-datomic [uri]
  (map->DatomicComponent {:uri uri}))

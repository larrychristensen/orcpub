(ns orcpub.datomic
  (:require [com.stuartsierra.component :as component]
            [datomic.api :as d]))

(defrecord DatomicComponent [uri conn]
  component/Lifecycle
  (start [this]
    (if (:conn this)
      this
      (do
        (assoc this :conn (d/connect uri)))))
  (stop [this]
    (assoc this :conn nil)))

(defn new-datomic [uri]
  (map->DatomicComponent {:uri uri}))

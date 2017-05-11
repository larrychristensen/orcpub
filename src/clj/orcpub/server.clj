(ns orcpub.server
  (:require [orcpub.system :as s]
            [com.stuartsierra.component :as component])
  (:gen-class))

(defn -main []
  (component/start (s/system :prod)))


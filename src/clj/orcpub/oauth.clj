(ns orcpub.oauth
  (:require [clj-http.client :as client]
            [cheshire.core :as json]
            [orcpub.route-map :as route-map]))

(defn base-url [protocol host & [port]]
  (str protocol "//" host (when port (str ":" port))))

(defn get-base-url [req]
  (base-url (str (name (:scheme req)) ":") ((:headers req) "host")))
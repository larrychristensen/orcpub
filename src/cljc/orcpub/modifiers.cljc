(ns orcpub.modifiers
  (:require [clojure.spec :as spec]))

(def cumulative-numeric-modifier-type ::cumulative-numeric)
(def override-modifier-type ::overriding)
(def cumulative-list-modifier-type ::cumulative-list)

(spec/def ::value some?)
(spec/def ::path-token (spec/or :key keyword?
                                :int int?))
(spec/def ::path-tokens (spec/+ ::path-token))
(spec/def ::path (spec/or :key ::path-token
                          :full-path ::path-tokens))
(spec/def ::type #{cumulative-numeric-modifier-type
                   override-modifier-type
                   cumulative-list-modifier-type})
(spec/def ::modifier (spec/keys :req [::path ::type ::value]))
(spec/def ::modifiers (spec/+ ::modifier))
(spec/def ::keywords (spec/+ keyword?))

(defn modifier [path type value]
  {:pre [(spec/valid? ::path path)
         (spec/valid? ::type type)]
   :post [(spec/valid? ::modifier %)]}
  {::path path
   ::type type
   ::value value})

(defn overriding [path value]
  (modifier path override-modifier-type value))

(defn cumulative-numeric [path bonus]
  (modifier path cumulative-numeric-modifier-type bonus))

(defn cumulative-list [path values]
  (modifier path cumulative-list-modifier-type values))

(defn modify [{:keys [orcpub.modifiers/type orcpub.modifiers/value]} current-value]
  (case type
    ::cumulative-numeric (+ value (or current-value 0))
    ::overriding value
    ::cumulative-list (concat current-value value)))

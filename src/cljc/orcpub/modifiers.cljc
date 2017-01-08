(ns orcpub.modifiers
  (:require [clojure.spec :as spec]))

(def cumulative-numeric-modifier-type ::cumulative-numeric)
(def override-modifier-type ::overriding)
(def cumulative-list-modifier-type ::cumulative-list)

(spec/def ::value any?)
(spec/def ::path-token (spec/or :key keyword?
                                :int int?))
(spec/def ::path-tokens (spec/+ ::path-token))
(spec/def ::path (spec/or :key ::path-token
                          :full-path ::path-tokens))
(spec/def ::type #{cumulative-numeric-modifier-type
                   override-modifier-type
                   cumulative-list-modifier-type})
(spec/def ::modifier (spec/keys :req [::path ::type]
                                :opt [::value]))
(spec/def ::modifiers (spec/+ ::modifier))
(spec/def ::keywords (spec/+ keyword?))

(defn bonus-str [bonus]
  (if (pos? bonus)
    (str "+" bonus)
    (str bonus)))

(defn modifier [path type value & [name]]
  (cond-> {::path path
           ::type type}
    value (assoc ::value value)
    name (assoc ::name name)))

(defn overriding [path value & [name]]
  (modifier path override-modifier-type value name))

(defn overriding-fn [])

(defn cumulative-numeric [path bonus & [name]]
  (modifier path cumulative-numeric-modifier-type bonus name))

(defn cumulative-list [path values]
  (modifier path cumulative-list-modifier-type values))

(defn modify [{:keys [orcpub.modifiers/type orcpub.modifiers/value]} current-value]
  (case type
    ::cumulative-numeric (+ value (or current-value 0))
    ::overriding value
    ::cumulative-list (concat current-value value)))

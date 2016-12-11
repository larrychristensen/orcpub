(ns orcpub.modifiers
  (:require [clojure.core.match :refer [match]]))

(def cumulative-numeric-modifier-type :cumulative-numeric)
(def override-modifier-type :overriding)
(def cumulative-list-modifier-type :cumulative-list)

(defn resistances [& values]
  {:path :resistances
   :type cumulative-list-modifier-type
   :value values})

(defn overriding [path value]
  {:path path
   :type override-modifier-type
   :value value})

(defn cumulative-numeric [path bonus]
  {:path path
   :type cumulative-numeric-modifier-type
   :value bonus})

(defn darkvision []
  {:path :darkvision
   :type override-modifier-type
   :value true})

(defn speed [value]
  (overriding :speed value))

(defn ability [ability bonus]
  (cumulative-numeric [:abilities ability] bonus))

(defn saving-throws [& abilities]
  {:path :saving-throws
   :type cumulative-list-modifier-type
   :value abilities})

(defn modify [{:keys [type value]} current-value]
  (case type
    :cumulative-numeric (+ value (or current-value 0))
    :overriding value
    :cumulative-list (concat current-value value)))

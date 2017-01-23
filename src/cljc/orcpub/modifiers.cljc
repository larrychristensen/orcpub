(ns orcpub.modifiers
  (:require [clojure.spec :as spec]
            [orcpub.entity-spec :as es]))

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

(defn mod-f [nm value fn & [qualifiers]]
  {::name nm
   ::value value
   ::fn fn
   ::qualifiers qualifiers})

(defn deferred-mod [nm deferred-fn val-fn]
  {::name nm
   ::deferred-fn deferred-fn
   ::val-fn val-fn})

(defmacro modifier [prop body & [{:keys [name value qualifiers]}]]
  `(mod-f ~name ~value (es/modifier ~prop ~body) ~qualifiers))

(defn deferred-modifier [deferred-fn & [nm val-fn]]
  (deferred-mod nm deferred-fn val-fn))

(defmacro cum-sum-mod [prop bonus & [nm value]]
  `(mod-f ~nm ~value (es/cum-sum-mod ~prop ~bonus)))

(defmacro vec-mod [prop val & [nm value]]
  `(mod-f ~nm ~value (es/vec-mod ~prop ~val)))

(defmacro set-mod [prop body & [nm value]]
  `(mod-f ~nm ~value (es/set-mod ~prop ~body)))

(defmacro map-mod [prop k v & [nm value]]
  `(mod-f ~nm ~value (es/map-mod ~prop ~k ~v)))

(defmacro fn-mod [prop func]
  `(mod-f nil nil (es/modifier ~prop (fn [] ~func))))



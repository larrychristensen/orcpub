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

(defmacro modifier [prop body & [nm value]]
  `{::name ~nm
    ::value ~value
    ::fn (es/modifier ~prop ~body)})

(defn deferred-modifier [fn & [nm value]]
  {::name nm
   ::value value
   ::deferred-fn fn})

(defmacro cum-sum-mod [prop bonus & [nm value]]
  `{::name ~nm
    ::value ~value
    ::fn (es/cum-sum-mod ~prop ~bonus)})

(defmacro vec-mod [prop val & [nm value]]
  `{::name ~nm
    ::value ~value
    ::fn (es/vec-mod ~prop ~val)})

(defmacro set-mod [prop body & [nm value]]
  `{::name ~nm
    ::value ~value
    ::fn (es/set-mod ~prop ~body)})

(defmacro map-mod [prop k v & [nm value]]
  `{::name ~nm
    ::value ~value
    ::fn (es/map-mod ~prop ~k ~v)})

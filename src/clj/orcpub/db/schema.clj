(ns orcpub.db.schema
  (:require [orcpub.entity.strict :as se]
            [orcpub.dnd.e5.character :as char5e]
            [orcpub.dnd.e5.character.equipment :as char-equip-5e]))

(defn string-prop [key]
  {:db/ident key
   :db/valueType :db.type/string
   :db/cardinality :db.cardinality/one})

(defn fulltext-prop [key]
  {:db/ident key
   :db/valueType :db.type/string
   :db/cardinality :db.cardinality/one
   :db/fulltext true})

(defn long-prop [key]
  {:db/ident key
   :db/valueType :db.type/long
   :db/cardinality :db.cardinality/one})

(defn bool-prop [key]
  {:db/ident key
   :db/valueType :db.type/boolean
   :db/cardinality :db.cardinality/one})

(defn many-ref [key]
  {:db/ident key
   :db/valueType :db.type/ref
   :db/cardinality :db.cardinality/many
   :db/isComponent true})

(def user-schema
  [{:db/ident :orcpub.user/username
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one}
   {:db/ident :orcpub.user/first-and-last-name
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one}
   {:db/ident :orcpub.user/email
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one}
   {:db/ident :orcpub.user/password
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one}
   {:db/ident :orcpub.user/send-updates?
    :db/valueType :db.type/boolean
    :db/cardinality :db.cardinality/one}
   {:db/ident :orcpub.user/verified?
    :db/valueType :db.type/boolean
    :db/cardinality :db.cardinality/one}
   {:db/ident :orcpub.user/verification-key
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one}
   {:db/ident :orcpub.user/created
    :db/valueType :db.type/instant
    :db/cardinality :db.cardinality/one}
   {:db/ident :orcpub.user/verification-sent
    :db/valueType :db.type/instant
    :db/cardinality :db.cardinality/one}
   {:db/ident :orcpub.user/password-reset-sent
    :db/valueType :db.type/instant
    :db/cardinality :db.cardinality/one}
   {:db/ident :orcpub.user/password-reset
    :db/valueType :db.type/instant
    :db/cardinality :db.cardinality/one}
   {:db/ident :orcpub.user/password-reset-key
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one}])

(def entity-schema
  [{:db/ident ::se/key
    :db/valueType :db.type/keyword
    :db/cardinality :db.cardinality/one}
   {:db/ident ::se/owner
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one}
   {:db/ident ::se/homebrew?
    :db/valueType :db.type/boolean
    :db/cardinality :db.cardinality/one}
   {:db/ident ::se/option
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one
    :db/isComponent true}
   {:db/ident ::se/options
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/many
    :db/isComponent true}
   {:db/ident ::se/values
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one
    :db/isComponent true}
   {:db/ident ::se/selections
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/many
    :db/isComponent true}
   {:db/ident ::se/int-value
    :db/valueType :db.type/long
    :db/cardinality :db.cardinality/one}
   {:db/ident ::se/string-value
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one}
   {:db/ident ::se/map-value
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one
    :db/isComponent true}])

(def entity-type-schema
  [{:db/ident ::se/type
    :db/valueType :db.type/keyword
    :db/cardinality :db.cardinality/one}
   {:db/ident ::se/game
    :db/valueType :db.type/keyword
    :db/cardinality :db.cardinality/one}
   {:db/ident ::se/game-version
    :db/valueType :db.type/keyword
    :db/cardinality :db.cardinality/one}])

(def character-schema
  (concat
   (map
    many-ref
    [::char5e/custom-equipment
     ::char5e/custom-treasure])
   (map
    long-prop
    [::char5e/str
     ::char5e/dex
     ::char5e/con
     ::char5e/int
     ::char5e/wis
     ::char5e/cha])
   (map
    fulltext-prop
    [::char5e/character-name
     ::char5e/description])
   (map
    string-prop
    [::char5e/weight
     ::char5e/faction-image-url
     ::char5e/hair
     ::char5e/player-name
     ::char5e/skin
     ::char5e/height
     ::char5e/flaws
     ::char5e/faction-image-url-failed
     ::char5e/image-url
     ::char5e/description
     ::char5e/personality-trait-1
     ::char5e/eyes
     ::char5e/age
     ::char5e/sex
     ::char5e/ideals
     ::char5e/personality-trait-2
     ::char5e/image-url-failed
     ::char5e/bonds
     ::char5e/faction-name])))

(def character-equipment-schema
  (concat
   [(string-prop ::char-equip-5e/name)
    (long-prop ::char-equip-5e/quantity)]
   (map
    bool-prop
    [::char-equip-5e/equipped?
     ::char-equip-5e/background-starting-equipment?
     ::char-equip-5e/class-starting-equipment?])))

(def all-schemas
  (concat
   user-schema
   entity-schema
   character-schema
   character-equipment-schema))

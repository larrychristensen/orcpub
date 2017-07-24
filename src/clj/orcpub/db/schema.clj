(ns orcpub.db.schema
  (:require [orcpub.modifiers :as mod]
            [orcpub.entity.strict :as se]
            [orcpub.dnd.e5.character :as char5e]
            [orcpub.dnd.e5.units :as units5e]
            [orcpub.dnd.e5.party :as party5e]
            [orcpub.dnd.e5.magic-items :as mi5e]
            [orcpub.dnd.e5.weapons :as weapon5e]
            [orcpub.dnd.e5.spells :as spells5e]
            [orcpub.dnd.e5.common :as common5e]
            [orcpub.dnd.e5.character.equipment :as char-equip-5e]))

(defn string-prop [key]
  {:db/ident key
   :db/valueType :db.type/string
   :db/cardinality :db.cardinality/one})

(defn string-prop-no-history [key]
  {:db/ident key
   :db/valueType :db.type/string
   :db/cardinality :db.cardinality/one
   :db/noHistory true})

(defn fulltext-prop [key]
  {:db/ident key
   :db/valueType :db.type/string
   :db/cardinality :db.cardinality/one
   :db/fulltext true})

(defn fulltext-prop [key]
  {:db/ident key
   :db/valueType :db.type/string
   :db/cardinality :db.cardinality/one
   :db/fulltext true
   :db/noHistory true})

(defn long-prop [key]
  {:db/ident key
   :db/valueType :db.type/long
   :db/cardinality :db.cardinality/one})

(defn prop [key]
  {:db/ident key})

(defn long-type [cfg]
  (assoc cfg :db/valueType :db.type/long))

(defn no-history [cfg]
  (assoc cfg :db/noHistory true))

(defn many [cfg]
  (assoc cfg :db/cardinality :db.cardinality/many))

(defn one [cfg]
  (assoc cfg :db/cardinality :db.cardinality/one))

(defn long-prop-no-history [key]
  {:db/ident key
   :db/valueType :db.type/long
   :db/cardinality :db.cardinality/one
   :db/noHistory true})

(defn bool-prop [key]
  {:db/ident key
   :db/valueType :db.type/boolean
   :db/cardinality :db.cardinality/one})

(defn bool-prop-no-history [key]
  {:db/ident key
   :db/valueType :db.type/boolean
   :db/cardinality :db.cardinality/one
   :db/noHistory true})

(defn kw-prop [key]
  {:db/ident key
   :db/valueType :db.type/keyword
   :db/cardinality :db.cardinality/one})

(defn kw-prop-no-history [key]
  {:db/ident key
   :db/valueType :db.type/keyword
   :db/cardinality :db.cardinality/one
   :db/noHistory true})

(defn many-ref [key]
  {:db/ident key
   :db/valueType :db.type/ref
   :db/cardinality :db.cardinality/many
   :db/isComponent true})

(defn many-ref-no-history [key]
  {:db/ident key
   :db/valueType :db.type/ref
   :db/cardinality :db.cardinality/many
   :db/isComponent true
   :db/noHistory true})

(defn ref-no-history [key]
  {:db/ident key
   :db/valueType :db.type/ref
   :db/cardinality :db.cardinality/one
   :db/isComponent true
   :db/noHistory true})

(defn many-kws [key]
  {:db/ident key
   :db/valueType :db.type/keyword
   :db/cardinality :db.cardinality/many})

(defn many-kws-no-history [key]
  {:db/ident key
   :db/valueType :db.type/keyword
   :db/cardinality :db.cardinality/many
   :db/noHistory true})

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
    :db/cardinality :db.cardinality/one}
   {:db/ident :orcpub.user/following
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/many}])

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
   {:db/ident ::se/summary
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
   [{:db/ident ::char5e/share?
    :db/valueType :db.type/boolean
     :db/cardinality :db.cardinality/one}
    {:db/ident ::char5e/prepared-spells-by-class
     :db/valueType :db.type/ref
     :db/cardinality :db.cardinality/many
     :db/isComponent true
     :db/noHistory true}
    {:db/ident ::char5e/prepared-spells
     :db/valueType :db.type/keyword
     :db/cardinality :db.cardinality/many
     :db/noHistory true}
    {:db/ident ::char5e/current-hit-points
     :db/valueType :db.type/long
     :db/cardinality :db.cardinality/one
     :db/noHistory true}
    {:db/ident ::char5e/notes
     :db/valueType :db.type/string
     :db/cardinality :db.cardinality/one
     :db/noHistory true}]
   (map
    many-ref
    [::char5e/custom-equipment
     ::char5e/custom-treasure
     ::char5e/classes])
   (map
    long-prop
    [::char5e/str
     ::char5e/dex
     ::char5e/con
     ::char5e/int
     ::char5e/wis
     ::char5e/cha
     ::char5e/level
     ::char5e/xps])
   (map
    fulltext-prop
    [::char5e/character-name
     ::char5e/description])
   (map
    string-prop
    [::char5e/race-name
     ::char5e/subrace-name
     ::char5e/class-name
     ::char5e/subclass-name
     ::char5e/weight
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

(def features-used-schema
  (concat
   [{:db/ident ::char5e/features-used
     :db/valueType :db.type/ref
     :db/isComponent true
     :db/cardinality :db.cardinality/one
     :db/noHistory true}
    (ref-no-history ::spells5e/slots-used)]
   (for [i (range 1 10)]
     (->> (common5e/slot-level-key i)
         prop
         long-type
         many
         no-history))
   (map
    (fn [unit]
      {:db/ident unit
       :db/valueType :db.type/string
       :db/cardinality :db.cardinality/many
       :db/noHistory true})
    [::units5e/minute
     ::units5e/hour
     ::units5e/turn
     ::units5e/day
     ::units5e/round
     ::units5e/rest
     ::units5e/long-rest])))

(def party-schema
  [{:db/ident ::party5e/owner
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one}
   {:db/ident ::party5e/name
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one}
   {:db/ident ::party5e/character-ids
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/many}])

(def character-equipment-schema
  (concat
   [(string-prop ::char-equip-5e/name)
    (long-prop ::char-equip-5e/quantity)]
   (map
    bool-prop
    [::char-equip-5e/equipped?
     ::char-equip-5e/background-starting-equipment?
     ::char-equip-5e/class-starting-equipment?])))

(def magic-item-schema
  (concat
   (map
    string-prop-no-history
    [::mi5e/name
     ::mi5e/owner
     ::mi5e/description
     ::mi5e/string-arg])
   (map
    many-ref-no-history
    [::mod/args
     ::mi5e/modifiers])
   (map
    many-kws-no-history
    [::mi5e/subtypes
     ::mi5e/attunement])
   (map
    long-prop-no-history
    [::mod/int-arg
     ::mi5e/magical-damage-bonus
     ::mi5e/magical-attack-bonus
     ::mi5e/magical-ac-bonus])
   (map
    kw-prop-no-history
    [::mod/key
     ::mod/keyword-arg
     ::mi5e/type
     ::mi5e/rarity])))

(def weapon-schema
  (concat
   [(ref-no-history ::weapon5e/range)
    (ref-no-history ::weapon5e/versatile)]
   (map
    kw-prop-no-history
    [::weapon5e/type
     ::weapon5e/key
     ::weapon5e/damage-type])
   (map
    long-prop-no-history
    [::weapon5e/damage-die-count
     ::weapon5e/damage-die
     ::weapon5e/min
     ::weapon5e/max])
   (map
    bool-prop-no-history
    [::weapon5e/special?
     ::weapon5e/melee?
     ::weapon5e/ranged?
     ::weapon5e/heavy?
     ::weapon5e/thrown?
     ::weapon5e/two-handed?
     ::weapon5e/finesse?
     ::weapon5e/reach?
     ::weapon5e/ammunition?])))

(def all-schemas
  (concat
   user-schema
   entity-schema
   entity-type-schema
   character-schema
   character-equipment-schema
   features-used-schema
   party-schema
   magic-item-schema
   weapon-schema))

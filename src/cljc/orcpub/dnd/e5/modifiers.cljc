(ns orcpub.dnd.e5.modifiers
  (:require [clojure.spec :as spec]
            [orcpub.modifiers :as mods]
            [orcpub.entity-spec :as es]
            [orcpub.dnd.e5.character :as char5e])
  #?(:cljs (:require-macros [orcpub.entity-spec :as es]
                            [orcpub.modifiers :as mods])))

(defn subclass [cls-key nm]
  (mods/modifier ?levels (assoc-in ?levels [cls-key :subclass] nm)))

(defn race [nm]
  (mods/modifier ?race nm))

(defn subrace [nm]
  (mods/modifier ?subrace nm))

(defn resistance [value]
  (prn "RESISTANCE VALUE" value)
  (mods/vec-mod ?resistances value))

(defn darkvision [value]
  (mods/modifier ?darkvision value))

(defn speed [value]
  (mods/modifier ?speed value "Speed" value))

(defn ability [ability bonus]
  (mods/modifier ?abilities
               (update ?abilities ability + bonus)
               (clojure.string/upper-case (name ability))
               (mods/bonus-str bonus)))

(defn abilities [abilities]
  (mods/modifier ?abilities abilities))

(defn deferred-abilities []
  (mods/deferred-modifier (fn [abilities]
                            (es/modifier ?abilities abilities))))

(defn saving-throws [& abilities]
  (mods/modifier ?savings-throws (concat (or ?savings-throws #{}) abilities)))

(defn initiative [bonus]
  (mods/cum-sum-mod ?initiative bonus))

(defn level [class-key class-nm level]
  (mods/modifier ?levels (update ?levels class-key merge {:class-name class-nm
                                                        :class-level level})))

(defn spell-slots [level num]
  (mods/map-mod ?spell-slots level num))

(defn spells-known [level spell-key]
  (mods/modifier ?spells-known
               (update ?spells-known level conj spell-key)))

(defn trait [name & [description]]
  (mods/vec-mod ?traits (cond-> {:name name}
                               description (assoc :description description))))

(defn proficiency-bonus [bonus]
  (mods/modifier ?proficiency-bonus bonus))

(defn skill-proficiency [skill-kw]
  (mods/set-mod ?skill-profs skill-kw))

(defn max-hit-points [bonus]
  (mods/cum-sum-mod ?max-hit-points bonus "HP" bonus))

(defn deferred-max-hit-points []
  (mods/deferred-modifier (fn [v] (es/cum-sum-mod ?max-hit-points v)) "HP"))

(defn skill-expertise [key]
  (mods/set-mod ?skill-expertise key))

(defn tool-proficiency [name key]
  (mods/vec-mod ?tool-proficiencies {:name name
                                     :key key}))

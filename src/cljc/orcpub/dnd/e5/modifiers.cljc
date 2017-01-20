(ns orcpub.dnd.e5.modifiers
  (:require [clojure.spec :as spec]
            [orcpub.modifiers :as mods]
            [orcpub.entity-spec :as es]
            [orcpub.dnd.e5.character :as char5e])
  #?(:cljs (:require-macros [orcpub.entity-spec :as es]
                            [orcpub.modifiers :as mods])))

(defn subclass [cls-key nm]
  (mods/modifier ?levels (assoc-in ?levels [cls-key :subclass] nm)))

(defn race2 [nm]
  (mods/modifier ?race nm))

(defn subrace2 [nm]
  (mods/modifier ?subrace nm))

(defn resistance [& value]
  (mods/vec-mod ?resistances value))

(defn darkvision2 []
  (mods/modifier ?darkvision true))

(defn speed2 [value]
  (mods/modifier ?speed value "Speed" value))

(defn ability2 [ability bonus]
  (mods/modifier ?abilities
               (update ?abilities ability + bonus)
               (clojure.string/upper-case (name ability))
               (mods/bonus-str bonus)))

(defn abilities2 [abilities]
  (mods/modifier ?abilities abilities))

(defn deferred-abilities []
  (mods/deferred-modifier (fn [abilities]
                            (es/modifier ?abilities abilities))))

(defn saving-throws2 [& abilities]
  (mods/modifier ?savings-throws (concat (or ?savings-throws #{}) abilities)))

(defn initiative2 [bonus]
  (mods/cum-sum-mod ?initiative bonus))

(defn level2 [class-key class-nm level]
  (mods/modifier ?levels (update ?levels class-key merge {:class-name class-nm
                                                        :class-level level})))

(defn spell-slots2 [level num]
  (mods/map-mod ?spell-slots level num))

(defn spells-known2 [level spell-key]
  (mods/modifier ?spells-known
               (update ?spells-known level conj spell-key)))

(defn trait2 [name & [description]]
  (mods/vec-mod ?traits (cond-> {:name name}
                               description (assoc :description description))))

(defn proficiency-bonus2 [bonus]
  (mods/modifier ?proficiency-bonus bonus))

(defn skill-proficiency [skill-kw]
  (mods/set-mod ?skill-profs skill-kw))

(defn max-hit-points2 [bonus]
  (mods/cum-sum-mod ?max-hit-points bonus "HP" bonus))

(defn deferred-max-hit-points []
  (mods/deferred-modifier (fn [v] (es/cum-sum-mod ?max-hit-points v)) "HP"))

(defn skill-expertise2 [key]
  (mods/set-mod ?skill-expertise key))

(defn tool-proficiency2 [name key]
  (mods/vec-mod ?tool-proficiencies {:name name
                                   :key key}))

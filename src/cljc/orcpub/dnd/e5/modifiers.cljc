(ns orcpub.dnd.e5.modifiers
  (:require [clojure.spec :as spec]
            [orcpub.modifiers :as mods]
            [orcpub.entity-spec :as es]
            [orcpub.dnd.e5.character :as char5e])
  #?(:cljs (:require-macros [orcpub.entity-spec :as es])))

(defn subclass [cls-key nm]
  (es/modifier ?levels (assoc-in ?levels [cls-key :subclass] nm)))

(defn race2 [nm]
  (es/modifier ?race nm))

(defn subrace2 [nm]
  (es/modifier ?subrace nm))

(defn resistance [& value]
  (es/vec-mod ?resistances value))

(defn darkvision2 []
  (es/modifier ?darkvision true))

(defn speed2 [value]
  (es/modifier ?speed value "Speed" value))

(defn ability2 [ability bonus]
  (es/modifier ?abilities
               (update ?abilities ability + bonus)
               (clojure.string/upper-case (name ability))
               (mods/bonus-str bonus)))

(defn abilities2 [abilities]
  (es/modifier ?abilities abilities))

(defn saving-throws2 [& abilities]
  (es/modifier ?savings-throws (concat (or ?savings-throws #{}) abilities)))

(defn initiative2 [bonus]
  (es/cum-sum-mod ?initiative bonus))

(defn level2 [class-key class-nm level]
  (es/modifier ?levels (update ?levels class-key merge {:class-name class-nm
                                                        :class-level level})))

(defn spell-slots2 [level num]
  (es/map-mod ?spell-slots level num))

(defn spells-known2 [level spell-key]
  (es/modifier ?spells-known
               (update ?spells-known level conj spell-key)))

(defn trait2 [name & [description]]
  (es/vec-mod ?traits (cond-> {:name name}
                               description (assoc :description description))))

(defn proficiency-bonus2 [bonus]
  (es/modifier ?proficiency-bonus bonus))

(defn skill-proficiency [skill-kw]
  (es/set-mod ?skill-profs skill-kw))

(defn max-hit-points2 [bonus]
  (es/cum-sum-mod ?max-hit-points bonus "HP" bonus))

(defn skill-expertise2 [key]
  (es/set-mod ?skill-expertise key))

(defn tool-proficiency2 [name key]
  (es/vec-mod ?tool-proficiencies {:name name
                                   :key key}))

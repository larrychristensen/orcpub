(ns orcpub.dnd.e5.modifiers
  (:require [clojure.spec :as spec]
            [orcpub.modifiers :as mods]
            [orcpub.entity-spec :as es]
            [orcpub.dnd.e5.character :as char5e])
  #?(:cljs (:require-macros [orcpub.entity-spec :as es])))

(defn race [nm]
  (mods/overriding [::char5e/race] nm))

(defn race2 [nm]
  (es/modifier ?race nm))

(defn subrace [nm]
  (mods/overriding [::char5e/subrace] nm))

(defn subrace2 [nm]
  (es/modifier ?subrace nm))

(defn resistances [& values]
  {:pre [(spec/valid? ::mods/keywords values)]}
  (mods/cumulative-list ::resistances values))

(defn resistance [& value]
  (es/vec-mod ?resistances value))

(defn darkvision []
  (mods/overriding ::char5e/darkvision true))

(defn darkvision2 []
  (es/modifier ?darkvision true))

(defn speed [value]
  (mods/overriding ::char5e/speed value "Speed"))

(defn speed2 [value]
  (es/modifier ?speed value "Speed"))

(defn ability [ability bonus]
  (mods/cumulative-numeric [::char5e/abilities ability] bonus (clojure.string/upper-case (name ability))))

(defn ability2 [ability bonus]
  (es/modifier ?abilities
               (update ?abilities ability + bonus)
               (clojure.string/upper-case (name ability))))

(defn abilities [abilities]
  (mods/overriding [::char5e/abilities] abilities))

(defn abilities2 [abilities]
  (es/modifier ?abilities abilities))

(defn saving-throws [& abilities]
  (mods/cumulative-list ::char5e/savings-throws abilities))

(defn saving-throws2 [& abilities]
  (es/set-mod ?savings-throws abilities))

(defn initiative [bonus]
  (mods/cumulative-numeric ::char5e/initiative bonus))

(defn initiative2 [bonus]
  (es/cum-sum-mod ?initiative bonus))

(defn level [class-key class-nm level]
  (mods/overriding [::char5e/levels class-key] {::char5e/class-name class-nm
                                                ::char5e/class-level level}))

(defn level2 [class-key class-nm level]
  (es/modifier ?levels (assoc ?levels class-key {::char5e/class-name class-nm
                                                               ::char5e/class-level level})))

(defn spell-slots [level num]
  (mods/cumulative-numeric [::char5e/spell-slots level] num))

(defn spell-slots2 [level num]
  (es/map-mod ?spell-slots level num))

(defn spells-known [level spell-key]
  (mods/cumulative-list [::char5e/spells-known level]
                        [spell-key]))

(defn spells-known2 [level spell-key]
  (es/modifier ?spell-slots
               (update ?spell-slots level conj spell-key)))

(defn trait [name & [description]]
  (mods/cumulative-list [::char5e/traits]
                        []))

(defn trait2 [name & [description]]
  (es/vec-mod ?traits (cond-> {:name name}
                               description (assoc :description description))))

(defn proficiency-bonus [bonus]
  (mods/cumulative-numeric ::char5e/proficiency-bonus bonus))

(defn proficiency-bonus2 [bonus]
  (es/modifier ?proficiency-bonus bonus))

(defn max-hit-points [bonus]
  (mods/cumulative-numeric ::char5e/max-hit-points bonus "HP"))

(defn max-hit-points2 [bonus]
  (es/cum-sum-mod ?max-hit-points bonus "HP"))

(defn skill-expertise [key]
  (mods/cumulative-list [::char5e/skill-expertise] [key]))

(defn skill-expertise2 [key]
  (es/set-mod ?skill-expertise key))

(defn tool-proficiency [name key]
  (mods/cumulative-list [::char5e/tool-proficiencies] [{::char5e/name name
                                                        ::char5e/key key}]))

(defn tool-proficiency2 [name key]
  (es/vec-mod ?tool-proficiencies {::char5e/name name
                                          ::char5e/key key}))

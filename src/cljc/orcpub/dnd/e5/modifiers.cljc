(ns orcpub.dnd.e5.modifiers
  (:require [clojure.spec :as spec]
            [orcpub.modifiers :as mods]
            [orcpub.entity-spec :as es]
            [orcpub.dnd.e5.character :as char5e])
  #?(:cljs (:require-macros [orcpub.entity-spec :as es])))

(defn race [nm]
  (mods/overriding [::char5e/race] nm))

(defn race2 [nm]
  (es/modifier ?char5e/race nm))

(defn subrace [nm]
  (mods/overriding [::char5e/subrace] nm))

(defn subrace2 [nm]
  (es/modifier ?char5e/subrace nm))

(defn resistances [& values]
  {:pre [(spec/valid? ::mods/keywords values)]}
  (mods/cumulative-list ::resistances values))

(defn resistances2 [& values]
  (es/vec-mod ?char5e/resistances values))

(defn darkvision []
  (mods/overriding ::char5e/darkvision true))

(defn darkvision2 []
  (es/modifier ?char5e/darkvision true))

(defn speed [value]
  (mods/overriding ::char5e/speed value "Speed"))

(defn speed2 [value]
  ^{:name "Speed"} (es/modifier ?char5e/speed value))

(meta (speed2 35))

(defn ability [ability bonus]
  (mods/cumulative-numeric [::char5e/abilities ability] bonus (clojure.string/upper-case (name ability))))

(defn abilities [abilities]
  (mods/overriding [::char5e/abilities] abilities))

(defn saving-throws [& abilities]
  (mods/cumulative-list ::char5e/savings-throws abilities))

(defn initiative [bonus]
  (mods/cumulative-numeric ::char5e/initiative bonus))

(defn level [class-key class-nm level]
  (mods/overriding [::char5e/levels class-key] {::char5e/class-name class-nm
                                                ::char5e/class-level level}))

(defn spell-slots [level num]
  (mods/cumulative-numeric [::char5e/spell-slots level] num))

(defn spells-known [level spell-key]
  (mods/cumulative-list [::char5e/spells-known level]
                        [spell-key]))

(defn trait [name & [description]]
  (mods/cumulative-list [::char5e/traits]
                        [(cond-> {:name name}
                           description (assoc :description description))]))

(defn proficiency-bonus [bonus]
  (mods/cumulative-numeric ::char5e/proficiency-bonus bonus))

(defn max-hit-points [bonus]
  (mods/cumulative-numeric ::char5e/max-hit-points bonus "HP"))

(defn skill-expertise [key]
  (mods/cumulative-list [::char5e/skill-expertise] [key]))

(defn tool-proficiency [name key]
  (mods/cumulative-list [::char5e/tool-proficiencies] [{::char5e/name name
                                                        ::char5e/key key}]))

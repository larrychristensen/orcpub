(ns orcpub.dnd.e5.modifiers
  (:require [clojure.spec :as spec]
            [orcpub.modifiers :as mods]
            [orcpub.dnd.e5.character :as char5e]))

(defn resistances [& values]
  {:pre [(spec/valid? ::mods/keywords values)]}
  (mods/cumulative-list ::resistances values))

(defn darkvision []
  (mods/overriding ::char5e/darkvision true))

(defn speed [value]
  (mods/overriding ::char5e/speed value))

(defn ability [ability bonus]
  (mods/cumulative-numeric [::char5e/abilities ability] bonus))

(defn abilities [abilities]
  (mods/overriding [::char5e/abilities] abilities))

(defn saving-throws [& abilities]
  (mods/cumulative-list ::char5e/savings-throws abilities))

(defn initiative [bonus]
  (mods/cumulative-numeric ::char5e/initiative bonus))

(defn level [class-key]
  (mods/cumulative-numeric [::char5e/levels class-key] 1))

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
  (mods/cumulative-numeric ::char5e/max-hit-points bonus))

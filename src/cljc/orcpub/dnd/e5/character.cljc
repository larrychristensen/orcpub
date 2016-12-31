(ns orcpub.dnd.e5.character
  (:require [clojure.spec :as spec]
            [orcpub.dice :as dice]))

(spec/def ::subrace string?)
(spec/def ::race string?)
(spec/def ::darkvision boolean?)
(spec/def ::speed nat-int?)
(spec/def ::character-ability (spec/int-in 1 21))
(spec/def ::initiative int?)
(spec/def ::savings-throw keyword?)
(spec/def ::savings-throws (spec/* ::savings-throw))
(spec/def ::max-hit-points nat-int?)

(spec/def ::str ::character-ability)
(spec/def ::dex ::character-ability)
(spec/def ::con ::character-ability)
(spec/def ::int ::character-ability)
(spec/def ::wis ::character-ability)
(spec/def ::cha ::character-ability)
(spec/def ::abilities (spec/keys :req [::str ::dex ::con ::int ::wis ::cha]))

(spec/def ::character (spec/keys :req [::abilities
                                       ::savings-throws
                                       ::speed
                                       ::darkvision
                                       ::initiative]))

(defn standard-ability-roll []
  (dice/dice-roll {:num 4 :sides 6 :drop-num 1}))

(spec/fdef
 standard-ability-roll
 :args nil?
 :ret ::character-ability
 :fn (spec/and (partial <= 3) (partial >= 18)))

(defn standard-ability-rolls []
  (zipmap
   [::str ::dex ::con ::int ::wis ::cha]
   (take 6 (repeatedly standard-ability-roll))))

(spec/fdef
 standard-ability-rolls
 :args nil?
 :ret ::abilities)

(defn abilities [str con dex int wis cha]
  {::str str
   ::con con
   ::dex dex
   ::int int
   ::wis wis
   ::cha cha})


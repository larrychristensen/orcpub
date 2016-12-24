(ns orcpub.dnd.e5.character
  (:require [clojure.spec :as spec]))

(spec/def ::not-negative (comp not neg?))
(spec/def ::natural-number (spec/and int? ::not-negative))

(spec/def ::darkvision boolean?)
(spec/def ::speed ::natural-number)
(spec/def ::character-ability (spec/int-in 1 21))
(spec/def ::initiative int?)
(spec/def ::savings-throw keyword?)
(spec/def ::savings-throws (spec/* ::savings-throw))

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

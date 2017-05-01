(ns orcpub.dnd.e5.character
  (:require [clojure.spec :as spec]
            [orcpub.entity-spec :as es]
            [orcpub.dice :as dice]
            [orcpub.template :as t]))

(spec/def ::armor-class nat-int?)
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

(spec/def ::abilities (spec/keys :req-un [::str ::dex ::con ::int ::wis ::cha]))

(spec/def ::character (spec/keys :req-un [::abilities
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

(def ability-keys [:str :dex :con :int :wis :cha])

(defn standard-ability-rolls []
  (zipmap
   ability-keys
   (take 6 (repeatedly standard-ability-roll))))

(spec/fdef
 standard-ability-rolls
 :args nil?
 :ret ::abilities)

(defn abilities [& as]
  (zipmap
   ability-keys
   as))

(defn alignment [built-char]
  (es/entity-val built-char :alignment))

(defn levels [built-char]
  (es/entity-val built-char :levels))

(defn background [built-char]
  (es/entity-val built-char :background))

(defn ability-values [built-char]
  (es/entity-val built-char :abilities))

(defn ability-bonuses [built-char]
  (es/entity-val built-char :ability-bonuses))

(defn base-land-speed [built-char]
  (es/entity-val built-char :speed))

(defn base-swimming-speed [built-char]
  (es/entity-val built-char :swimming-speed))

(defn base-flying-speed [built-char]
  (es/entity-val built-char :flying-speed))

(defn land-speed-with-armor [built-char]
  (es/entity-val built-char :speed-with-armor))

(defn unarmored-speed-bonus [built-char]
  (es/entity-val built-char :unarmored-speed-bonus))

(defn race [built-char]
  (es/entity-val built-char :race))

(defn subrace [built-char]
  (es/entity-val built-char :subrace))

(defn classes [built-char]
  (es/entity-val built-char :classes))

(defn darkvision [built-char]
  (es/entity-val built-char :darkvision))

(defn skill-proficiencies [built-char]
  (es/entity-val built-char :skill-profs))

(defn skill-bonuses [built-char]
  (es/entity-val built-char :skill-bonuses))

(defn tool-proficiencies [built-char]
  (es/entity-val built-char :tool-profs))

(defn weapon-proficiencies [built-char]
  (let [proficiencies (es/entity-val built-char :weapon-profs)]
    (if (and proficiencies (proficiencies :martial))
      [:simple :martial]
      proficiencies)))

(defn armor-proficiencies [built-char]
  (es/entity-val built-char :armor-profs))

(defn damage-resistances [built-char]
  (es/entity-val built-char :damage-resistances))

(defn damage-immunities [built-char]
  (es/entity-val built-char :damage-immunities))

(defn condition-immunities [built-char]
  (es/entity-val built-char :condition-immunities))

(defn languages [built-char]
  (es/entity-val built-char :languages))

(defn base-armor-class [built-char]
  (es/entity-val built-char :armor-class))

(defn armor-class-with-armor [built-char]
  (es/entity-val built-char :armor-class-with-armor))

(defn normal-armor-inventory [built-char]
  (es/entity-val built-char :armor))

(defn magic-armor-inventory [built-char]
  (es/entity-val built-char :magic-armor))

(defn all-armor-inventory [built-char]
  (merge (normal-armor-inventory built-char)
         (magic-armor-inventory built-char)))

(defn normal-weapons-inventory [built-char]
  (es/entity-val built-char :weapons))

(defn magic-weapons-inventory [built-char]
  (es/entity-val built-char :magic-weapons))

(defn all-weapons-inventory [built-char]
  (merge (normal-weapons-inventory built-char)
         (magic-weapons-inventory built-char)))

(defn normal-equipment-inventory [built-char]
  (es/entity-val built-char :equipment))

(defn magical-equipment-inventory [built-char]
  (es/entity-val built-char :magic-items))

(defn spells-known [built-char]
  (es/entity-val built-char :spells-known))

(defn spell-slots [built-char]
  (es/entity-val built-char :spell-slots))

(defn traits [built-char]
  (es/entity-val built-char :traits))

(defn attacks [built-char]
  (es/entity-val built-char :attacks))

(defn bonus-actions [built-char]
  (es/entity-val built-char :bonus-actions))

(defn reactions [built-char]
  (es/entity-val built-char :reactions))

(defn actions [built-char]
  (es/entity-val built-char :actions))

(defn max-hit-points [built-char]
  (es/entity-val built-char :max-hit-points))

(defn initiative [built-char]
  (es/entity-val built-char :initiative))

(defn proficiency-bonus [built-char]
  (es/entity-val built-char :prof-bonus))

(defn passive-perception [built-char]
  (es/entity-val built-char :passive-perception))

(defn number-of-attacks [built-char]
  (es/entity-val built-char :num-attacks))

(defn critical-hit-values [built-char]
  (es/entity-val built-char :critical))

(defn saving-throws [built-char]
  (es/entity-val built-char :saving-throws))

(defn save-bonuses [built-char]
  (es/entity-val built-char :save-bonuses))

(defn saving-throw-advantages [built-char]
  (es/entity-val built-char :saving-throw-advantage))

(defn weapon-attack-modifier [built-char weapon finesse?]
  ((es/entity-val built-char :weapon-attack-modifier)
   weapon
   finesse?))

(defn weapon-damage-modifier [built-char weapon finesse?]
  ((es/entity-val built-char :weapon-damage-modifier)
   weapon
   finesse?))

(defn age [built-char]
  (es/entity-val built-char :age))

(defn sex [built-char]
  (es/entity-val built-char :sex))

(defn height [built-char]
  (es/entity-val built-char :height))

(defn weight [built-char]
  (es/entity-val built-char :weight))

(defn skin [built-char]
  (es/entity-val built-char :skin))

(defn eyes [built-char]
  (es/entity-val built-char :eyes))

(defn hair [built-char]
  (es/entity-val built-char :hair))

(defn image-url [built-char]
  (es/entity-val built-char :image-url))

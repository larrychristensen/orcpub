(ns orcpub.dnd.e5.template-base
  (:require [orcpub.entity-spec :as es]
            [orcpub.template :as t]
            [orcpub.dnd.e5.options :as opt5e]
            [orcpub.dnd.e5.character :as char5e]
            [orcpub.dnd.e5.skills :as skill5e]
            [orcpub.dnd.e5.modifiers :as mod5e]
            [orcpub.dnd.e5.character.equipment :as char-equip5e]))

(def warlock-spell-slot-schedule
  {1 {1 1}
   2 {1 2}
   3 {2 2}
   4 {2 2}
   5 {3 2}
   6 {3 2}
   7 {4 2}
   8 {4 2}
   9 {5 2}
   10 {5 2}
   11 {5 3}
   12 {5 3}
   13 {5 3}
   14 {5 3}
   15 {5 3}
   16 {5 3}
   17 {5 4}
   18 {5 4}
   19 {5 4}
   20 {5 4}})

(def template-base
  (es/make-entity
   {?armor-class (+ 10 (?ability-bonuses ::char5e/dex))
    ?base-armor-class (+ 10 (?ability-bonuses ::char5e/dex)
                         ?natural-ac-bonus
                         ?magical-ac-bonus)
    ?levels {}
    ?ac-bonus 0
    ?natural-ac-bonus 0
    ?unarmored-ac-bonus 0
    ?unarmored-with-shield-ac-bonus 0
    ?armored-ac-bonus 0
    ?max-medium-armor-bonus 2
    ?magical-ac-bonus 0
    ?armor-stealth-disadvantage? (fn [armor]
                                   (:stealth-disadvantage? armor))
    ?armor-dex-bonus (fn [armor]
                       (let [dex-bonus (?ability-bonuses ::char5e/dex)]
                         (case (:type armor)
                           :light dex-bonus
                           :medium (min ?max-medium-armor-bonus dex-bonus)
                           0)))
    ?shield-ac-bonus (fn [shield]
                       (+ 2 (or (:magical-ac-bonus shield) 0)))
    ?unarmored-armor-class (+ ?base-armor-class ?unarmored-ac-bonus ?ac-bonus)
    ?unarmored-with-shield-armor-class (fn [shield]
                                         (+ ?base-armor-class
                                            ?unarmored-with-shield-ac-bonus
                                            ?ac-bonus
                                            (?shield-ac-bonus shield)))
    ?armor-class-with-armor-base (fn [armor & [shield]]
                                   (cond (and (nil? armor)
                                              (nil? shield)) ?unarmored-armor-class
                                         (nil? armor) (?unarmored-with-shield-armor-class shield)
                                         :else (+ (if shield (?shield-ac-bonus shield) 0)
                                                  (+ (?armor-dex-bonus armor)
                                                     (or ?armored-ac-bonus 0)
                                                     (:base-ac armor)
                                                     (:magical-ac-bonus armor)
                                                     ?ac-bonus)
                                                  ?magical-ac-bonus)))
    ?armor-class-with-armor (fn [armor & [shield]]
                              (apply +
                                     (apply max
                                            (?armor-class-with-armor-base armor shield)
                                            (map #(% armor shield) ?ac-fns))
                                     (map #(% armor shield) ?ac-bonus-fns)))
    ?ac-bonus-fns []
    ?ac-fns []
    ?abilities (reduce
                (fn [m k]
                  (let [overrides (filter
                                   (fn [{:keys [ability value]}]
                                     (= ability k))
                                   ?ability-overrides)]
                    (assoc m k (max (if (seq overrides)
                                      (apply max (map :value overrides))
                                      0)
                                    (+ (or (k ?base-abilities) 0)
                                       (or (k ?ability-increases) 0)
                                       (or (k ?level-ability-increases) 0))))))
                {}
                char5e/ability-keys)
    ?ability-bonuses (reduce-kv
                      (fn [m k v]
                        (assoc m k (opt5e/ability-bonus v)))
                      {}
                      ?abilities)
    ?ability-overrides []
    ?saving-throw-bonuses {}
    ?save-bonuses (reduce-kv
                   (fn [m k v]
                     (assoc m k (+ v
                                   (or (get ?saving-throw-bonuses k) 0)
                                   (if (and ?saving-throws (?saving-throws k)) ?prof-bonus 0))))
                   {}
                   ?ability-bonuses)
    ?total-levels (apply + (map (fn [[k {l :class-level}]] l) ?levels))
    ?class-level (fn [class-kw] (get-in ?levels [class-kw :class-level]))
    ?prof-bonus (+ (int (/ (dec ?total-levels) 4)) 2)
    ?default-skill-bonus {}
    ?skill-prof-bonuses (reduce
                         (fn [m {k :key}]
                           (assoc m k (if (k ?skill-profs)
                                        (if (k ?skill-expertise)
                                          (* 2 ?prof-bonus)
                                          ?prof-bonus)
                                        (or (?default-skill-bonus (skill5e/skill-abilities k)) 0))))
                         {}
                         skill5e/skills)
    ?skill-bonuses (reduce-kv
                    (fn [m k v]
                      (assoc m k (+ v
                                    (?ability-bonuses (skill5e/skill-abilities k))
                                    (get ?additional-skill-bonuses k 0))))
                    {}
                    ?skill-prof-bonuses)
    ?tool-bonus-fn (fn [tool-kw]
                     (* (if (?tool-profs tool-kw) ?prof-bonus 0)
                        (if (?tool-expertise tool-kw) 2 1)))
    ?tool-expertise #{}
    ?tool-profs #{}
    ?additional-skill-bonuses {}
    ?passive-perception (+ 10 (?skill-bonuses :perception))
    ?passive-investigation (+ 10 (?skill-bonuses :investigation))
    ?hit-point-level-bonus (?ability-bonuses ::char5e/con)
    ?hit-point-level-increases 0
    ?max-hit-points (max 1 (+ ?hit-point-level-increases (* ?total-levels ?hit-point-level-bonus)))
    ?initiative (?ability-bonuses ::char5e/dex)
    ?num-attacks 1
    ?critical #{20}
    ?has-weapon-prof? (fn [weapon]
                        (or (?weapon-profs :martial)
                            (?weapon-profs (:key weapon))
                            (?weapon-profs (:type weapon))
                            (?weapon-profs (:base-key weapon))))
    ?weapon-prof-bonus (fn [weapon]
                         (if (?has-weapon-prof? weapon)
                           ?prof-bonus
                           0))
    ?weapon-attack-modifier (fn [weapon finesse?]
                              (let [definitely-finesse? (and finesse?
                                                             (:finesse? weapon))]
                                (+ (?weapon-prof-bonus weapon)
                                   (or (:magical-attack-bonus weapon) 0)
                                   (or (:attack-bonus weapon) 0)
                                   (if (:melee? weapon)
                                     (+ (?ability-bonuses
                                         (if definitely-finesse? ::char5e/dex ::char5e/str))
                                        (or ?melee-attack-bonus 0))
                                     (+ (?ability-bonuses
                                         (if definitely-finesse? ::char5e/str ::char5e/dex))
                                        (or ?ranged-attack-bonus 0))))))
    ?weapon-damage-modifier (fn [weapon finesse?]
                              (let [definitely-finesse? (and finesse?
                                                             (:finesse? weapon))
                                    melee? (:melee? weapon)]
                                (+ (or (:magical-damage-bonus weapon) 0)
                                   (or (:damage-bonus weapon) 0)
                                   (?ability-bonuses
                                    (if (or (and melee? (not definitely-finesse?))
                                            (and (not melee?) definitely-finesse?))
                                      ::char5e/str
                                      ::char5e/dex)))))
    ?spell-attack-modifier-bonus 0
    ?spell-attack-modifier (fn [ability-kw]
                             (+ ?prof-bonus
                                (?ability-bonuses ability-kw)
                                ?spell-attack-modifier-bonus))
    ?spell-save-dc-bonus 0
    ?spell-save-dc (fn [ability-kw]
                     (+ 8
                        ?prof-bonus
                        (?ability-bonuses ability-kw)
                        ?spell-save-dc-bonus))
    ?spell-modifiers (reduce
                      (fn [m {:keys [ability class]}]
                        (assoc m class {:class class
                                        :ability ability
                                        :spell-save-dc (?spell-save-dc ability)
                                        :spell-attack-modifier (?spell-attack-modifier ability)}))
                      {}
                      (->> ?spells-known vals flatten))
    ?total-spellcaster-levels (apply + (map (fn [[cls-kw factor]]
                                              (-> ?levels
                                                  cls-kw
                                                  :class-level
                                                  (/ factor)
                                                  int))
                                            ?spell-slot-factors))
    ?spell-slots (merge-with
                  +
                  (cond
                    (> (count ?spell-slot-factors) 1)
                    (opt5e/total-slots
                     ?total-spellcaster-levels
                     1)
                    (= 1 (count ?spell-slot-factors))
                    (opt5e/total-slots (let [k (some-> ?spell-slot-factors first key)]
                                         (:class-level (?levels k)))
                                       (some-> ?spell-slot-factors first val))

                    :else {})
                  (if ?pact-magic?
                    (warlock-spell-slot-schedule (?class-level :warlock))))
    ?classes []
    ?reactions []
    ?actions []
    ?bonus-actions []
    ?traits []
    ?condition-immunities #{}
    ?immunities #{}
    ?damage-immunities #{}
    ?damage-resistances #{}
    ?saving-throw-advantage []
    ?saving-throws #{}
    ?spells-known (sorted-map)
    ?speed-overrides []
    ?speed 0
    ?total-speed (apply max ?speed ?speed-overrides)
    ?darkvision-bonus 0
    ?darkvision 0
    ?total-darkvision (+ ?darkvision ?darkvision-bonus)
    ?all-armor (merge ?armor ?magic-armor)
    ?equipped-armor (into {} (filter (fn [[_ v]] (::char-equip5e/equipped? v)) ?all-armor))}))

(ns orcpub.dnd.e5.template-base
  (:require [orcpub.entity-spec :as es]
            [orcpub.template :as t]
            [orcpub.common :as common]
            [orcpub.dnd.e5.options :as opt5e]
            [orcpub.dnd.e5.character :as char5e]
            [orcpub.dnd.e5.skills :as skill5e]
            [orcpub.dnd.e5.modifiers :as mod5e]
            [orcpub.dnd.e5.weapons :as weapon5e]
            [orcpub.dnd.e5.magic-items :as mi5e]
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
                         ;; Checks whether barbarian unarmored bonus exists (or is higher) than natural AC/Draconic Bloodline AC
                         (if (> ?unarmored-ac-bonus ?natural-ac-bonus ) 0 ?natural-ac-bonus)
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
                       (+ 2 (or (::mi5e/magical-ac-bonus shield) 0)))
    ?unarmored-armor-class (+ ?base-armor-class ?unarmored-ac-bonus ?ac-bonus)
    ?unarmored-with-shield-armor-class (fn [shield]
                                         (+ ?base-armor-class
                                            ?unarmored-with-shield-ac-bonus
                                            ?ac-bonus
                                            (?shield-ac-bonus shield)))
    ?dual-wield-weapon? weapon5e/light-melee-weapon?
    ?armor-class-with-armor-base (fn [armor & [shield]]
                                   (cond (and (nil? armor)
                                              (nil? shield)) ?unarmored-armor-class
                                         (nil? armor) (?unarmored-with-shield-armor-class shield)
                                         :else (+ (if shield (?shield-ac-bonus shield) 0)
                                                  (+ (?armor-dex-bonus armor)
                                                     (or ?armored-ac-bonus 0)
                                                     (:base-ac armor)
                                                     (::mi5e/magical-ac-bonus armor)
                                                     ?ac-bonus)
                                                  ?magical-ac-bonus)))
    ?armor-class-with-armor (fn [armor & [shield]]
                              (let [max-ac (apply max
                                                  (?armor-class-with-armor-base armor shield)
                                                  (map #(% armor shield) ?ac-fns))
                                    bonuses (map #(% armor shield) ?ac-bonus-fns)]
                                (apply +
                                       max-ac
                                       bonuses)))
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
    ?str-mod (?ability-bonuses ::char5e/str)
    ?dex-mod (?ability-bonuses ::char5e/dex)
    ?con-mod (?ability-bonuses ::char5e/con)
    ?int-mod (?ability-bonuses ::char5e/int)
    ?wis-mod (?ability-bonuses ::char5e/wis)
    ?cha-mod (?ability-bonuses ::char5e/cha)
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
    ?barbarian-level (?class-level :barbarian)
    ?bard-level (?class-level :bard)
    ?cleric-level (?class-level :cleric)
    ?druid-level (?class-level :druid)
    ?fighter-level (?class-level :fighter)
    ?monk-level (?class-level :monk)
    ?paladin-level (?class-level :paladin)
    ?ranger-level (?class-level :ranger)
    ?sorcerer-level (?class-level :sorcerer)
    ?warlock-level (?class-level :warlock)
    ?wizard-level (?class-level :wizard)

    ?proficiency-bonus-increase 0
    ?prof-bonus (+ (int (/ (dec ?total-levels) 4)) 2 ?proficiency-bonus-increase)
    ?default-skill-bonus {}
    ?default-skill-bonus-fns []
    ?skill-prof-bonuses (char5e/skill-prof-bonuses ?prof-bonus
                                            ?skill-profs
                                            ?skill-expertise
                                            ?default-skill-bonus-fns)
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
    ?tool-profs {}
    ?additional-skill-bonuses {}
    ?passive-perception (+ 10 (?skill-bonuses :perception))
    ?passive-investigation (+ 10 (?skill-bonuses :investigation))
    ?hit-point-level-bonus (?ability-bonuses ::char5e/con)
    ?hit-point-level-increases 0
    ?class-hit-point-level-bonus {}
    ?max-hit-points (max 1 (apply +
                                  ?hit-point-level-increases
                                  (* ?total-levels ?hit-point-level-bonus)
                                  (map
                                   (fn [[class-kw bonus]]
                                     (* bonus (?class-level class-kw)))
                                   ?class-hit-point-level-bonus)))
    ?initiative (?ability-bonuses ::char5e/dex)
    ?number-of-attacks [1]
    ?extra-attacks 0
    ?num-attacks (apply max ?extra-attacks ?number-of-attacks)
    ?critical #{20}
    ?weapon-profs #{}
    ?armor-profs #{}
    ?has-weapon-prof? (fn [weapon]
                        (or (?weapon-profs ::weapon5e/martial)
                            (?weapon-profs (::weapon5e/key weapon))
                            (?weapon-profs (:key weapon))
                            (?weapon-profs (::weapon5e/type weapon))
                            (?weapon-profs (:base-key weapon))))
    ?weapon-prof-bonus (fn [weapon]
                         (if (?has-weapon-prof? weapon)
                           ?prof-bonus
                           0))
    ?weapon-ability-modifiers [(fn [weapon finesse?]
                                 (let [definitely-finesse?
                                       (and finesse?
                                            (::weapon5e/finesse? weapon))
                                       melee? (::weapon5e/melee? weapon)]
                                   (?ability-bonuses
                                    (if (or (and melee? (not definitely-finesse?))
                                            (and (not melee?) definitely-finesse?))
                                      ::char5e/str
                                      ::char5e/dex))))]
    ?weapon-ability-modifier (fn [weapon finesse?]
                               (apply
                                max
                                (map
                                 (fn [mod-fn]
                                   (mod-fn weapon finesse?))
                                 ?weapon-ability-modifiers)))
    ?weapon-attack-modifier (fn [weapon finesse?]
                              (apply +
                                     (?weapon-prof-bonus weapon)
                                     (or (::mi5e/magical-attack-bonus weapon) 0)
                                     (or (:attack-bonus weapon) 0)
                                     (if (::weapon5e/melee? weapon)
                                       (or ?melee-attack-bonus 0)
                                       (or ?ranged-attack-bonus 0))
                                     (?weapon-ability-modifier weapon finesse?)
                                     (map
                                      #(% weapon)
                                      ?attack-modifier-fns)))
    ?best-weapon-attack-modifier (fn [weapon]
                                   (max (?weapon-attack-modifier weapon false)
                                        (?weapon-attack-modifier weapon true)))
    ?weapon-ability-damage-modifier (fn [weapon finesse? off-hand?]
                                      (if (not off-hand?)
                                        (?weapon-ability-modifier weapon finesse?)
                                        0))
    ?weapon-damage-modifier (fn [weapon finesse? & [off-hand?]]
                              (let [definitely-finesse? (and finesse?
                                                             (::weapon5e/finesse? weapon))
                                    melee? (::weapon5e/melee? weapon)]
                                (apply +
                                       (+ (or (::mi5e/magical-damage-bonus weapon) 0)
                                          (?weapon-ability-damage-modifier weapon definitely-finesse? off-hand?))
                                       ;(if melee?
                                       ;  (map
                                       ;   #(% weapon)
                                       ;   ?melee-damage-bonus-fns)
                                       ;  (map
                                       ;   #(% weapon)
                                       ;   ?ranged-damage-bonus-fns)) ;any non-melee is assumed to be ranged/finesse/dex
                                       (map
                                        #(% weapon)
                                        ?damage-bonus-fns))))
    ?best-weapon-damage-modifier (fn [weapon & [off-hand?]]
                                   (max (?weapon-damage-modifier weapon false off-hand?)
                                        (?weapon-damage-modifier weapon true off-hand?)))
    ?spell-attack-modifier-bonus 0
    ?spell-attack-modifier (fn [ability-kw]
                             (+ ?prof-bonus
                                (?ability-bonuses ability-kw)
                                ?spell-attack-modifier-bonus))
    ?spell-save-dc-bonus 0
    ?spell-save-dc (fn [ability-kw]
                     (+ 8
                        ?prof-bonus
                        (get ?ability-bonuses ability-kw 0)
                        ?spell-save-dc-bonus))
    ?spell-modifiers (reduce
                      (fn [m {:keys [ability class]}]
                        (assoc m class {:class class
                                        :ability ability
                                        :spell-save-dc (?spell-save-dc ability)
                                        :spell-attack-modifier (?spell-attack-modifier ability)}))
                      {}
                      (->> ?spells-known vals (map vals) flatten))
    ?total-spellcaster-levels (apply + (map (fn [[cls-kw factor]]
                                              (-> ?levels
                                                  cls-kw
                                                  :class-level
                                                  (/ factor)
                                                  int))
                                            ?spell-slot-factors))
    ?class-spell-slots (fn [class-kw]
                         (opt5e/total-slots
                          (?class-level class-kw)
                          (get ?spell-slot-factors class-kw)))
    ?prepare-spell-count (fn [class-name]
                           (let [class-kw (common/name-to-kw class-name)
                                 slot-factor (get ?spell-slot-factors class-kw)
                                 spell-mods ?spell-modifiers
                                 ability (some-> class-name
                                                 spell-mods
                                                 :ability)
                                 ability-mod (get ?ability-bonuses ability 0)]
                             (+ ability-mod (if-let [lvl (?class-level class-kw)]
                                              (int (/ lvl slot-factor))
                                              0))))
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
    ?flying-speed-overrides []
    ?swimming-speed-overrides []
    ?climbing-speed-overrides []
    ?speed 0
    ?flying-speed-bonus 0
    ?swimming-speed-bonus 0
    ?climbing-speed-bonus 0
    ?total-speed (apply max ?speed ?speed-overrides)
    ?flying-speed (apply max ?flying-speed-bonus ?flying-speed-overrides)
    ?swimming-speed (apply max ?swimming-speed-bonus ?swimming-speed-overrides)
    ?climbing-speed (apply max ?climbing-speed-bonus ?climbing-speed-overrides)
    ?darkvision-bonus 0
    ?darkvision 0
    ?total-darkvision (+ ?darkvision ?darkvision-bonus)
    ?all-armor (merge ?armor ?magic-armor)
    ?equipped-armor (into {} (filter (fn [[_ v]] (::char-equip5e/equipped? v)) ?all-armor))}))

(ns orcpub.dnd.e5.templates.ua-warlock-and-wizard
  (:require [orcpub.dnd.e5.options :as opt5e]
            [orcpub.dnd.e5.modifiers :as mod5e]
            [orcpub.dnd.e5.character :as char5e]
            [orcpub.modifiers :as mod]
            [orcpub.template :as t]
            [re-frame.core :refer [subscribe]]))

(def warlock-option-cfg
  {:name "Warlock"
   :subclass-level 1
   :subclass-title "Otherworldly Patron"
   :plugin? true
   :source :ua-warlock-and-wizard
   :subclasses [{:name "The Hexblade"
                 :levels {1 {:modifiers [(mod5e/armor-proficiency :medium)
                                         (mod5e/armor-proficiency :shields)
                                         (mod5e/weapon-proficiency :martial)
                                         (mod/vec-mod ?weapon-ability-modifiers
                                                      (fn [weapon finesse?]
                                                        (if (and (?has-weapon-prof weapon)
                                                                 (not (:two-handed? weapon)))
                                                          (get ?ability-bonuses ::char5e/cha)
                                                          0)))
                                         (mod5e/bonus-action
                                          {:name "Hexblade's Curse"
                                           :page 1
                                           :frequency {:units :rest}
                                           :source :ua-warlock-and-wizard})]
                             :selections [(opt5e/warlock-subclass-spell-selection [:shield :wrathful-smite])]}
                          3 {:selections [(opt5e/warlock-subclass-spell-selection [:branding-smite :magic-weapon])]}
                          5 {:selections [(opt5e/warlock-subclass-spell-selection [:blink :elemental-weapon])]}
                          6 {:selections [(mod5e/bonus-action
                                           {:name "Shadow Hound"
                                            :page 1
                                            :source :ua-warlock-and-wizard})
                                          (mod5e/damage-resistance :radiant)]}
                          7 {:selections [(opt5e/warlock-subclass-spell-selection [:phantasmal-killer :staggering-smite])]}
                          9 {:selections [(opt5e/warlock-subclass-spell-selection [:cone-of-cold :destructive-wave])]}
                          10 {:modifiers [(mod5e/trait-cfg
                                           {:name "Armor of Hexes"
                                            :page 2
                                            :source :ua-warlock-and-wizard})]}
                          14 {:modifiers [(mod5e/trait-cfg
                                           {:name "Master of Hexes"
                                            :page 2
                                            :source :ua-warlock-and-wizard})]}}}
                {:name "The Raven Queen"
                 :levels {1 {:modifiers [(mod5e/bonus-action
                                           {:name "Sentinel Raven"
                                            :page 2
                                            :source :ua-warlock-and-wizard})]
                             :selections [(opt5e/warlock-subclass-spell-selection [:false-life :sanctuary])]}
                          3 {:selections [(opt5e/warlock-subclass-spell-selection [:silence :spiritual-weapon])]}
                          5 {:selections [(opt5e/warlock-subclass-spell-selection [:feign-death :speak-with-dead])]}
                          6 {:selections [(mod5e/bonus-action
                                           {:name "Soul of the Raven"
                                            :page 2
                                            :source :ua-warlock-and-wizard})]}
                          7 {:selections [(opt5e/warlock-subclass-spell-selection [:ice-storm :locate-creature])]}
                          9 {:selections [(opt5e/warlock-subclass-spell-selection [:commune :cone-of-cold])]}
                          10 {:modifiers [(mod5e/trait-cfg
                                           {:name "Raven's Shield"
                                            :page 3
                                            :source :ua-warlock-and-wizard
                                            :summary "Advantage on death saves, immune to being frightened, and resistance to necrotic damage"})
                                          (mod5e/saving-throw-advantage [:death])
                                          (mod5e/condition-immunity :frightened)
                                          (mod5e/damage-resistance :necrotic)]}
                          14 {:modifiers [(mod5e/trait-cfg
                                           {:name "Queen's Right Hand"
                                            :page 3
                                            :source :ua-warlock-and-wizard})
                                          (mod5e/spells-known 7 :finger-of-death ::char5e/cha "Warlock" 0)]}}}]})

(defn patron-prereq [patron]
  (t/option-prereq
   (str "Your patron must be " patron)
   (fn [c] (= @(subscribe [::char5e/subclass nil c])
              patron))))

(def eldritch-invocation-selection
  (opt5e/eldritch-invocation-selection
   {:min 0
    :max 0
    :options (map
              (fn [o]
                (update o ::t/modifiers conj opt5e/ua-al-illegal))
              [(t/option-cfg
                {:name "Burning Hex"
                 :prereqs [(patron-prereq "The Hexblade")]
                 :modifiers [(mod5e/bonus-action
                              {:name "Burning Hex"
                               :page 3
                               :source :ua-warlock-and-wizard
                               :summary (str "Cause a target cursed by your Hexblade's Curse to take " (?ability-bonuses ::char5e/cha) " points of fire damage.")})]})
               (t/option-cfg
                {:name "Caiphon's Beacon"
                 :prereqs [(patron-prereq "The Great Old One")]
                 :modifiers [(mod5e/skill-proficiency :deception)
                             (mod5e/skill-proficiency :stealth)
                             (mod5e/trait-cfg
                              {:name "Caiphon's Beacon"
                               :page 3
                               :source :ua-warlock-and-wizard
                               :summary "You have advantage on attack rolls against charmed targets"})]})
               (t/option-cfg
                {:name "Chilling Hex"
                 :prereqs [(patron-prereq "The Hexblade")]
                 :modifiers [(mod5e/bonus-action
                              {:name "Chilling Hex"
                               :page 3
                               :source :ua-warlock-and-wizard
                               :summary (str "Deal " (?ability-bonuses ::char5e/cha) " points of fire damage to all of your enemies within 5 ft. of a target cursed with your Hexblade's Curse")})]})
               (t/option-cfg
                {:name "Chronicle of the Raven Queen"
                 :prereqs [(patron-prereq "The Raven Queen")
                           opt5e/pact-of-the-tome-prereq]
                 :modifiers [(mod5e/action
                              {:name "Chronicle of the Raven Queen"
                               :page 2
                               :source :ua-warlock-and-wizard
                               :duration {:units :turn}
                               :summary "Gain the ability to see through objects to 30 ft, with darkvision within that range."})]})
               (t/option-cfg
                {:name "Gift of the Depths"
                 :prereqs [(opt5e/total-levels-option-prereq 5 :warlock)]
                 :modifiers [(mod5e/swimming-speed-equal-to-walking)
                             (mod5e/spells-known 3 :water-breathing ::char5e/cha "Warlock" 0 "once per long rest")
                             (mod5e/trait-cfg
                              {:name "Gift of the Depths"
                               :page 6
                               :summary "You can breath underwater"})]})
               (t/option-cfg
                {:name "Gift of the Ever-Living Ones"
                 :prereqs [opt5e/pact-of-the-chain-prereq]
                 :modifiers [(mod5e/trait-cfg
                              {:name "Gift of the Ever-Living Ones"
                               :page 6
                               :summary "If your familiar is within 100 ft. when you regain hit points, you regain the max for rolls"})]})
               (t/option-cfg
                {:name "Grasp of Hadar"
                 :prereqs [opt5e/has-eldritch-blast-prereq]
                 :modifiers [(mod5e/trait-cfg
                              {:name "Grasp of Hadar"
                               :page 6
                               :summary "When you hit with 'eldritch blast' you can move the target up to 10 ft. toward you."})]})
               (t/option-cfg
                {:name "Improved Pact Weapon"
                 :prereqs [opt5e/pact-of-the-blade-prereq]
                 :modifiers [(mod5e/trait-cfg
                              {:name "Improved Pact Weapon"
                               :page 6
                               :summary "Use weapons summoned as spellcasting focus. If the weapon is non-magical, it counts as magic and have a +1 bonus to attack and damage."})]})
               (t/option-cfg
                {:name "Kiss of Mephistopheles"
                 :prereqs [(opt5e/total-levels-option-prereq 5 :warlock)
                           opt5e/has-eldritch-blast-prereq]
                 :modifiers [(mod5e/bonus-action
                              {:name "Kiss of Mephistopheles"
                               :page 6
                               :summary "When you hit with 'eldritch blast', cast 'fireball' centered on the target using a warlock spell slot"})]})
               (t/option-cfg
                {:name "Maddening Hex"
                 :prereqs [(opt5e/total-levels-option-prereq 5 :warlock)]
                 :modifiers [(mod5e/bonus-action
                              {:name "Maddening Hex"
                               :page 6
                               :source :ua-revised-class-options
                               :summary (str "When you hex a target, deal " (max 0 (?ability-bonuses ::char5e/cha)) " psychic damage to it and other creatures you choose within 5 ft of it.")})]})
               (t/option-cfg
                {:name "Relentless Hex"
                 :prereqs [(opt5e/total-levels-option-prereq 7 :warlock)]
                 :modifiers [(mod5e/bonus-action
                              {:name "Relentless Hex"
                               :page 6
                               :source :ua-revised-class-options
                               :summary "When you hex a target, transport up to 30 ft. to an unoccupied space within 5 ft. of it."})]})
               (t/option-cfg
                {:name "Shroud of Shadow"
                 :modifiers [(mod5e/trait-cfg
                              {:name "Eldritch Invocation: Shroud of Shadow"
                               :page 6
                               :source :ua-revised-class-options
                               :summary "cast invisibility at will"})
                             (mod5e/spells-known 2 :invisibility ::char5e/cha "Warlock" 0 "at will")]
                 :prereqs [(opt5e/total-levels-option-prereq 15 :warlock)]})
               (t/option-cfg
                {:name "Tomb of Levistus"
                 :prereqs [(opt5e/total-levels-option-prereq 5 :warlock)]
                 :modifiers [(mod5e/reaction
                              {:name "Tomb of Levistus"
                               :page 6
                               :source :ua-revised-class-options
                               :summary "When you take damage, gain 10 temp HPs. In addition, gain vulnerability to fire damage, and speed is 0, which go away at the end of your next turn."})]})
               (t/option-cfg
                {:name "Trickster's Escape"
                 :modifiers [(mod5e/trait-cfg
                              {:name "Eldritch Invocation: Trickster's Escape"
                               :page 6
                               :frequency opt5e/long-rests-1
                               :source :ua-revised-class-options
                               :summary "cast bane warlock spell slot"
                               :prereqs [(opt5e/total-levels-option-prereq 7 :warlock)]})
                             (mod5e/spells-known 4 :freedom-of-movement ::char5e/cha "Warlock" 0 "once per long rest")]})])}))

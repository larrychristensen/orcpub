(ns orcpub.dnd.e5.templates.ua-revised-class-options
  (:require [orcpub.dnd.e5.options :as opt5e]
            [orcpub.dnd.e5.modifiers :as mod5e]
            [orcpub.dnd.e5.character :as char5e]
            [orcpub.template :as t])
  #?(:cljs (:require-macros [orcpub.dnd.e5.options :as opt5e])))

(def druid-option-cfg
  {:name "Druid"
   :plugin? true
   :subclass-level 2
   :subclass-title "Druid Circle"
   :subclasses [{:name "Circle of the Shepherd"
                 :levels {2 {:modifiers [(mod5e/trait-cfg
                                          {:name "Spirit Totem"
                                           :page 1
                                           :source :ua-revised-class-options})
                                         (mod5e/trait-cfg
                                          {:name "Speech of the Woods"
                                           :page 1
                                           :source :ua-revised-class-options})]}
                          6 {:modifiers [(mod5e/trait-cfg
                                          {:name "Mighty Summoner"
                                           :page 2
                                           :source :ua-revised-class-options})]}
                          10 {:modifiers [(mod5e/trait-cfg
                                           {:name "Guardian Spirit"
                                            :page 2
                                            :source :ua-revised-class-options})]}
                          14 {:modifiers [(mod5e/trait-cfg
                                           {:name "Faithful Summons"
                                            :page 2
                                            :source :ua-revised-class-options})]}}}]})

(def fighter-option-cfg
  {:name "Fighter"
   :plugin? true
   :subclass-level 3
   :subclass-title "Martial Archetype"
   :subclasses [{:name "Cavalier"
                 :levels {3 {:selections [(t/selection-cfg
                                           {:name "Skill or Language Proficiency"
                                            :tags #{:skill-profs :profs}
                                            :options [(t/option-cfg
                                                       {:name "Skill"
                                                        :selections [(opt5e/skill-selection [:animal-handling :history :insight :performance :persuasion] 1)]})
                                                      (t/option-cfg
                                                       {:name "Language"
                                                        :selections [(opt5e/language-selection opt5e/languages 1)]})]})]
                             :modifiers [(mod5e/trait-cfg
                                          {:name "Born to the Saddle"
                                           :page 2
                                           :source :ua-revised-class-options})
                                         (mod5e/trait-cfg
                                          {:name "Combat Superiority"
                                           :page 2
                                           :source :ua-revised-class-options})]}
                          7 {:modifiers [(mod5e/trait-cfg
                                          {:name "Ferocious Charger"
                                           :page 3
                                           :source :ua-revised-class-options})]}
                          10 {:modifiers [(mod5e/trait-cfg
                                           {:name "Improved Combat Superiority"
                                            :page 3
                                            :source :ua-revised-class-options})]}
                          15 {:modifiers [(mod5e/trait-cfg
                                           {:name "Relentless"
                                            :page 3
                                            :source :ua-revised-class-options})]}}}]})

(def paladin-option-cfg
  (merge
   opt5e/paladin-base-cfg
   {:plugin? true
    :subclasses
    [{:name "Oath of Conquest"
      :modifiers [(opt5e/paladin-spell 1 :armor-of-agathys 3)
                  (opt5e/paladin-spell 1 :command 3)
                  (opt5e/paladin-spell 2 :hold-person 5)
                  (opt5e/paladin-spell 2 :spiritual-weapon 5)
                  (opt5e/paladin-spell 3 :bestow-curse 9)
                  (opt5e/paladin-spell 3 :fear 9)
                  (opt5e/paladin-spell 4 :dominate-beast 13)
                  (opt5e/paladin-spell 4 :stoneskin 13)
                  (opt5e/paladin-spell 5 :cloudkill 17)
                  (opt5e/paladin-spell 5 :dominate-person 17)
                  (mod5e/action
                   {:name "Channel Divinity: Conquering Presence"
                    :source :ua-revised-class-options
                    :page 4})
                  (mod5e/trait-cfg
                   {:name "Channel Divinity: Turn the Tide"
                    :page 4
                    :source :ua-revised-class-options})]
      :levels {7 {:modifiers [(mod5e/trait-cfg
                               {:name "Aura of Conquest"
                                :page 4
                                :source :ua-revised-class-options})]}
               15 {:modifiers [(mod5e/trait-cfg
                                {:name "Scornful Rebuke"
                                 :page 4
                                 :source :ua-revised-class-options})]}
               20 {:modifiers (conj
                               (map
                                (fn [type-kw]
                                  (mod5e/damage-resistance type-kw))
                                opt5e/damage-types)
                               (mod5e/extra-attack)
                               (mod5e/critical 19)
                               (mod5e/trait-cfg
                                {:name "Invincible Conqueror"
                                 :page 4
                                 :source :ua-revised-class-options}))}}}]}))

(def warlock-option-cfg
  {:name "Warlock"
   :subclass-level 1
   :subclass-title "Otherworldly Patron"
   :plugin? true
   :source :ua-revised-class-options
   :subclasses [{:name "The Celestial"
                 :traits [
                          {:name "Undying Nature"
                           :level 10
                           :page 140
                           :source :scag
                           :summary "don't need to breath, eat, drink, or sleep"}]
                 :levels {1 {:modifiers [(mod5e/spells-known 0 :sacred-flame ::char5e/cha "Warlock")
                                         (mod5e/spells-known 0 :burning-hands ::char5e/cha "Warlock")
                                         (mod5e/trait-cfg
                                          {:name "Healing Light"
                                           :page 6
                                           :source :ua-revised-class-options})]
                             :selections [(opt5e/warlock-subclass-spell-selection [:burning-hands :cure-wounds])]}
                          3 {:selections [(opt5e/warlock-subclass-spell-selection [:flaming-sphere :lesser-restoration])]}
                          5 {:selections [(opt5e/warlock-subclass-spell-selection [:daylight :revivify])]}
                          6 {:selections [(mod5e/trait-cfg
                                           {:name "Radiant Soul"
                                            :page 5
                                            :source :ua-revised-class-options})
                                          (mod5e/damage-resistance :radiant)]}
                          7 {:selections [(opt5e/warlock-subclass-spell-selection [:guardian-of-faith :wall-of-fire])]}
                          9 {:selections [(opt5e/warlock-subclass-spell-selection [:flame-strike :greater-restoration])]}
                          10 {:modifiers [(mod5e/trait-cfg
                                           {:name "Celestial Radiance"
                                            :page 5
                                            :source :ua-revised-class-options})]}
                          14 {:modifiers [(mod5e/trait-cfg
                                           {:name "Searing Vengeance"
                                            :page 5
                                            :source :ua-revised-class-options})]}}}]})

(def ua-revised-class-options-plugin
  {:name "Unearthed Arcana: Revised Class Options"
   :class-options? true
   :key :ua-revised-class-options
   :selections [(opt5e/class-selection
                 {:options (map
                            opt5e/class-option
                            [druid-option-cfg
                             fighter-option-cfg
                             paladin-option-cfg
                             warlock-option-cfg])})
                (opt5e/eldritch-invocation-selection
                 {:min 0
                  :max 0
                  :options (map
                            (fn [o]
                              (update o ::t/modifiers conj opt5e/ua-al-illegal))
                            [(opt5e/eldritch-invocation-option
                              {:name "Aspect of the Moon"
                               :prereqs [opt5e/pact-of-the-tome-prereq]
                               :page 5
                               :source :ua-revised-class-options
                               :summary "You don't need sleep and can't be forced to sleep."})
                             (opt5e/eldritch-invocation-option
                              {:name "Cloak of Flies"
                               :prereqs [(opt5e/total-levels-option-prereq 5 :warlock)]
                               :page 5
                               :source :ua-revised-class-options
                               :summary (str "You gain a 5 ft. radius aura that gives you advantage on Intimidation, but disadvantage on other CHA checks. Other creatures in the aura at the start of their turn take " (max 0 (?ability-bonuses ::char5e/cha)) " points of poison damage.")})
                             (opt5e/eldritch-invocation-option
                              {:name "Eldritch Smite"
                               :prereqs [(opt5e/total-levels-option-prereq 5 :warlock)
                                         opt5e/pact-of-the-blade-prereq]
                               :page 6
                               :source :ua-revised-class-options
                               :summary "When you hit with your pact weapon, you can expend a spell slot to do (X + 1)d8 extra force damage, where X is the slot level, and the target is knocked prone if it takes any of the damage and is Huge or smaller."
                               :frequency {:units :turn}})
                             (opt5e/eldritch-invocation-option
                              {:name "Frost Lance"
                               :prereqs [opt5e/has-eldritch-blast-prereq]
                               :page 6
                               :source :ua-revised-class-options
                               :frequency {:units :turn}
                               :summary "When you hit with eldritch blast, you can reduce the target's speed by 10 ft. until your next turn."
                               :trait-type :bonus-action})
                             (opt5e/eldritch-invocation-option
                              {:name "Ghostly Gaze"
                               :prereqs [(opt5e/total-levels-option-prereq 7 :warlock)]
                               :page 6
                               :source :ua-revised-class-options
                               :frequency {:units :rest}
                               :duration {:units :turn}
                               :summary "Gain the ability to see through objects to 30 ft, with darkvision within that range."
                               :trait-type :bonus-action})
                             (opt5e/eldritch-invocation-option
                              {:name "Gift of the Depths"
                               :prereqs [(opt5e/total-levels-option-prereq 5 :warlock)]
                               :page 6
                               :source :ua-revised-class-options
                               :summary "You can breath underwater"
                               :modifiers [(mod5e/swimming-speed-equal-to-walking)
                                           (mod5e/spells-known 3 :water-breathing ::char5e/cha "Warlock" 0 "once per long rest")]})
                             (opt5e/eldritch-invocation-option
                              {:name "Gift of the Ever-Living Ones"
                               :prereqs [opt5e/pact-of-the-chain-prereq]
                               :page 6
                               :source :ua-revised-class-options
                               :summary "If your familiar is within 100 ft. when you regain hit points, you regain the max for rolls"})
                             (opt5e/eldritch-invocation-option
                              {:name "Grasp of Hadar"
                               :prereqs [opt5e/has-eldritch-blast-prereq]
                               :page 6
                               :source :ua-revised-class-options
                               :frequency {:units :turn}
                               :summary "When you hit with 'eldritch blast' you can move the target up to 10 ft. toward you."})
                             (opt5e/eldritch-invocation-option
                              {:name "Improved Pact Weapon"
                               :prereqs [opt5e/pact-of-the-blade-prereq]
                               :page 6
                               :source :ua-revised-class-options
                               :summary "Use weapons summoned as spellcasting focus. If the weapon is non-magical, it counts as magic and have a +1 bonus to attack and damage."})
                             (opt5e/eldritch-invocation-option
                              {:name "Kiss of Mephistopheles"
                               :prereqs [(opt5e/total-levels-option-prereq 5 :warlock)
                                         opt5e/has-eldritch-blast-prereq]
                               :page 6
                               :source :ua-revised-class-options
                               :summary "When you hit with 'eldritch blast', cast 'fireball' centered on the target using a warlock spell slot"
                               :trait-type :bonus-action})
                             (opt5e/eldritch-invocation-option
                              {:name "Maddening Hex"
                               :prereqs [(opt5e/total-levels-option-prereq 5 :warlock)]
                               :page 6
                               :source :ua-revised-class-options
                               :summary (str "When you hex a target, deal " (max 0 (?ability-bonuses ::char5e/cha)) " psychic damage to it and other creatures you choose within 5 ft of it.")
                               :trait-type :bonus-action})
                             (opt5e/eldritch-invocation-option
                              {:name "Relentless Hex"
                               :prereqs [(opt5e/total-levels-option-prereq 7 :warlock)]
                               :page 6
                               :source :ua-revised-class-options
                               :summary "When you hex a target, transport up to 30 ft. to an unoccupied space within 5 ft. of it."
                               :trait-type :bonus-action})
                             (opt5e/eldritch-invocation-option
                              {:name "Shroud of Shadow"
                               :page 6
                               :source :ua-revised-class-options
                               :summary "cast invisibility at will"
                               :modifiers [(mod5e/spells-known 2 :invisibility ::char5e/cha "Warlock" 0 "at will")]
                               :prereqs [(opt5e/total-levels-option-prereq 15 :warlock)]})
                             (opt5e/eldritch-invocation-option
                              {:name "Tomb of Levistus"
                               :prereqs [(opt5e/total-levels-option-prereq 5 :warlock)]
                               :page 6
                               :source :ua-revised-class-options
                               :trait-type :reaction
                               :frequency {:units :rest}
                               :summary "When you take damage, gain 10 temp HPs. In addition, gain vulnerability to fire damage, and speed is 0, which go away at the end of your next turn."})
                             (opt5e/eldritch-invocation-option
                              {:name "Trickster's Escape"
                               :page 6
                               :frequency opt5e/long-rests-1
                               :source :ua-revised-class-options
                               :summary "cast bane using a warlock spell slot"
                               :prereqs [(opt5e/total-levels-option-prereq 7 :warlock)]
                               :modifiers [(mod5e/spells-known 4 :freedom-of-movement ::char5e/cha "Warlock" 0 "once per long rest")]})])})]})


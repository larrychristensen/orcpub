(ns orcpub.dnd.e5.templates.scag
  (:require [orcpub.template :as t]
            [orcpub.common :as common]
            [orcpub.entity-spec :as es]
            [orcpub.modifiers :as mod]
            [orcpub.dnd.e5.modifiers :as mod5e]
            [orcpub.dnd.e5.units :as units5e]
            [orcpub.dnd.e5.character :as char5e]
            [orcpub.dnd.e5.weapons :as weapon5e]
            [orcpub.dnd.e5.equipment :as equip5e]
            [orcpub.dnd.e5.spell-lists :as sl]
            [orcpub.dnd.e5.options :as opt5e]
            [re-frame.core :refer [subscribe]]))

(def scag-barbarian
  {:name "Barbarian"
   :plugin? true
   :subclass-level 3
   :subclass-title "Primal Path"
   :source :scag
   :subclasses [{:name "Path of the Battlerager"
                 :source :scag
                 :prereqs [(t/option-prereq
                            "Dwarves only"
                            (fn [c] (= "Dwarf" @(subscribe [::char5e/race nil c]))))]
                 :modifiers [(mod5e/bonus-action
                              {:name "Spiked Armor Attack"
                               :page 121
                               :source :scag
                               :summary "while raging, attack with spiked armor (1d4 piercing damage); grapple deals 3 piercing damage"})]
                 :levels {6 {:modifiers [(mod5e/dependent-trait
                                          {:name "Reckless Abandon"
                                           :page 121
                                           :source :scag
                                           :summary (str "gain " (?ability-bonuses ::char5e/con) " temp HPs when you Reckless Attack")})]}
                          10 {:modifiers [(mod5e/bonus-action
                                           {:name "Battlerager Charge"
                                            :page 121
                                            :source :scag
                                            :summary "Dash while raging"})]}}
                 :traits [{:name "Battlerager Armor"
                           :level 3
                           :page 121
                           :source :scag
                           :summary "can use spiked armor as a weapon"}
                          {:name "Spiked Retribution"
                           :level 14
                           :page 121
                           :source :scag
                           :summary "while raging and in spiked armor, creatures within 5 ft. that hit with melee attack take 3 damage"}]}
                {:name "Path of the Totem Warrior"
                 :levels {3 {:selections [(t/selection-cfg
                                           {:name "Totem Spirit"
                                            :tags #{:class}
                                            :options [(t/option-cfg
                                                       {:name "Elk"
                                                        :modifiers [(mod5e/used-resource :scag "Totem Spirit: Elk")
                                                                    (mod5e/trait-cfg
                                                                     {:name "Totem Spirit: Elk"
                                                                      :page 122
                                                                      :source :scag
                                                                      :summary "When raging without heavy armor, your speed increases by 15 ft."})]})
                                                      (t/option-cfg
                                                       {:name "Tiger"
                                                        :modifiers [(mod5e/used-resource :scag "Totem Spirit: Tiger")
                                                                    (mod5e/trait-cfg
                                                                     {:name "Totem Spirit: Tiger"
                                                                      :page 122
                                                                      :source :scag
                                                                      :summary "When raging, +10 ft long jump, +3 ft high jump"})]})]})]}
                          6 {:selections [(t/selection-cfg
                                           {:name "Aspect of the Beast"
                                            :tags #{:class}
                                            :options [(t/option-cfg
                                                       {:name "Elk"
                                                        :modifiers [(mod5e/used-resource :scag "Aspect of the Beast: Elk")
                                                                    (mod5e/trait-cfg
                                                                     {:name "Aspect of the Beast: Elk"
                                                                      :page 122
                                                                      :source :scag
                                                                      :summary "double travel pace"})]})
                                                      (t/option-cfg
                                                       {:name "Tiger"
                                                        :modifiers [(mod5e/used-resource :scag "Aspect of the Beast: Elk")]
                                                        :selections [(opt5e/skill-selection [:athletics :acrobatics :stealth :survival] 2)]})]})]}
                          14 {:selections [(t/selection-cfg
                                            {:name "Totemic Attunement"
                                             :tags #{:class}
                                             :options [(t/option-cfg
                                                        {:name "Elk"
                                                         :modifiers [(mod5e/used-resource :scag "Totemic Attunement: Elk")
                                                                     (mod5e/bonus-action
                                                                      {:name "Totemic Attunement: Elk"
                                                                       :page 122
                                                                       :source :scag
                                                                       :summary (str "pass through space of Large or smaller creature, if it fails a DC " (?spell-save-dc ::char5e/str) " STR save it is knocked prone and takes 1d12 " (common/mod-str (?ability-bonuses ::char5e/str)) " damage")})]})
                                                       (t/option-cfg
                                                        {:name "Tiger"
                                                         :modifiers [(mod5e/used-resource :scag "Totemic Attunement: Tiger")
                                                                     (mod5e/bonus-action
                                                                      {:name "Totemic Attunement: Tiger"
                                                                       :page 122
                                                                       :source :scag
                                                                       :summary "make an additional melee weapon attack when you move 20+ ft. in a line and make a melee weapon attack"})]})]})]}}}]})

(def scag-cleric
  {:name "Cleric"
   :plugin? true
   :subclass-level 1
   :subclass-title "Divine Domain"
   :source :scag
   :subclasses [{:name "Arcana Domain"
                 :profs {:armor {:heavy true}}
                 :modifiers [(mod5e/skill-proficiency :arcana)
                             (opt5e/cleric-spell 1 :detect-magic 1)
                             (opt5e/cleric-spell 1 :magic-missile 1)
                             (opt5e/cleric-spell 2 :magic-weapon 3)
                             (opt5e/cleric-spell 2 :nystuls-magic-aura 3)
                             (opt5e/cleric-spell 3 :dispel-magic 5)
                             (opt5e/cleric-spell 3 :magic-circle 5)
                             (opt5e/cleric-spell 4 :arcane-eye 7)
                             (opt5e/cleric-spell 4 :leomunds-secret-chest 7)
                             (opt5e/cleric-spell 5 :planar-binding 9)
                             (opt5e/cleric-spell 5 :teleportation-circle 9)]
                 :selections [(opt5e/spell-selection {:title "Wizard Cantrips"
                                                      :spellcasting-ability ::char5e/wis
                                                      :class-name "Cleric"
                                                      :num 2
                                                      :spell-keys (get-in sl/spell-lists [:wizard 0])
                                                      :exclude-ref? true})]
                 :levels {2 {:modifiers [(mod5e/action
                                          {:name "Channel Divinity: Arcane Abjuration"
                                           :page 126
                                           :source :scag
                                           :summary (str "turn celestial, elemental, fey, or fiend on failed DC " (?spell-save-dc ::char5e/wis) " WIS save.")})]}
                          8 {:modifiers [(opt5e/potent-spellcasting 126 :scag)]}
                          17 {:selections (map
                                           (fn [level]
                                             (opt5e/spell-selection {:title (str "Wizard Spell: Level " level)
                                                                     :spellcasting-ability ::char5e/wis
                                                                     :class-name "Cleric"
                                                                     :num 1
                                                                     :spell-keys (get-in sl/spell-lists [:wizard level])
                                                                     :exclude-ref? true}))
                                           [6 7 8 9])}}
                 :traits [{:level 6
                           :name "Spell Breaker"
                           :page 126
                           :souce :scag
                           :summary "when casting a healing spell on a creature, also end a spell of equal or lesser level on it"}
                          {:level 17
                           :name "Supreme Healing"
                           :summary "Instead of rolling healing, use max possible roll value." }]}]})

(def scag-fighter
  {:name "Fighter",
   :plugin? true
   :subclass-level 3
   :subclass-title "Martial Archetype"
   :source :scag
   :subclasses [{:name "Purple Dragon Knight"
                 :levels {3 {:modifiers [(mod5e/dependent-trait
                                          {:name "Rallying Cry"
                                           :page 128
                                           :source :scag
                                           :class-key :fighter
                                           :summary (str "When you Second Wind, choose up to 3 allies to regain " (?class-level :fighter) " HPs")})]}
                          7 {:modifiers [(mod5e/skill-expertise :persuasion)]
                             :selections [(opt5e/skill-selection [:persuasion :animal-handling :insight :intimidation :performance] 1)]}
                          10 {:modifiers [(mod5e/dependent-trait
                                           {:name "Inspiring Surge"
                                            :page 128
                                            :class-key :fighter
                                            :source :scag
                                            :range units5e/ft-60
                                            :summary (str "When you Action Surge, choose "
                                                          (if (>= (?class-level :fighter) 17)
                                                            2
                                                            1)
                                                          " allies to gain an attack as reaction")})]}
                          15 {:modifiers [(mod5e/trait-cfg
                                           {:page 128
                                            :source :scag
                                            :class-key :fighter
                                            :name "Bulwark"
                                            :summary "When you use Indomitable, extend the benefit to 1 ally"})]}}}]})


(def scag-monk
  (merge
   opt5e/monk-base-cfg
   {:source :scag
    :subclasses [{:name "Way of the Long Death"
                  :modifiers [(mod5e/dependent-trait
                               {:name "Touch of Death"
                                :page 130
                                :source :scag
                                :summary (str "when you reduce a creature within 5 ft. to 0 HPs, you gain " (max 1 (+ (?ability-bonuses ::char5e/wis) (?class-level :monk))) " temp HPs")})]
                  :levels {6 {:modifiers [(mod5e/action
                                           {:name "Hour of Reaping"
                                            :page 130
                                            :source :scag
                                            :duration units5e/turns-1
                                            :summary (str "creatures within 30 ft. are frightened of you on failed DC " (?spell-save-dc ::char5e/wis) " WIS save")})]}
                           11 {:modifiers [(mod5e/trait-cfg
                                            {:name "Mastery of Death"
                                             :page 131
                                             :source :scag
                                             :summary "when reduced to 0 HP, spend 1 ki point to reduce to 1 instead"})]}
                           17 {:modifiers [(mod5e/action
                                            {:name "Touch of the Long Death"
                                             :source :scag
                                             :page 131
                                             :summary (str "spend X (up to 10) ki points to deal 2Xd10 necrotic damage on failed DC " (?spell-save-dc ::char5e/wis) " CON save, half as much on successful save")})]}}}
                 {:name "Way of the Sun Soul"
                  :modifiers [(mod5e/attack
                               {:name "Radiant Sun Bolt"
                                :page 131
                                :source :scag
                                :attack-type :ranged
                                :range units5e/ft-30
                                :damage-die ?martial-arts-die
                                :damage-die-count 1
                                :damage-type :radiant
                                :damage-modifier (?ability-bonuses ::char5e/dex)})]
                  :levels {6 {:modifiers [(mod5e/bonus-action
                                           {:name "Searing Arc Strike"
                                            :page 131
                                            :source :scag
                                            :summary (str "after taking Attack action, spend 2 + X ki points to cast level X burning hands (max X of " (int (/ (?class-level :monk) 2)) ")")})]}
                           11 {:modifiers [(mod5e/action
                                            {:name "Searing Sunburst"
                                             :level 11
                                             :page 131
                                             :source :scag
                                             :summary (str "create an exploding orb, dealing 2d6 damage to creatures in a 20 ft sphere that fail a DC " (?spell-save-dc ::char5e/wis) " CON save, you can spend X ki to increase by 2Xd6 damage (up to 3 ki)")})]}
                           17 {:modifiers [(mod5e/reaction
                                            {:name "Sun Shield"
                                             :page 131
                                             :source :scag
                                             :summary (str "when hit with melee attack, deal " (+ 5 (?ability-bonuses ::char5e/wis)) " radiant damage to attacker; you also shed 30 ft. light")})]}}}]}))


(def scag-paladin
  (opt5e/subclass-plugin
   opt5e/paladin-base-cfg
   :scag
   [{:name "Oath of the Crown"
     :modifiers [(opt5e/paladin-spell 1 :command 3)
                 (opt5e/paladin-spell 1 :compelled-duel 3)
                 (opt5e/paladin-spell 2 :warding-bond 5)
                 (opt5e/paladin-spell 2 :zone-of-truth 5)
                 (opt5e/paladin-spell 3 :aura-of-vitality 9)
                 (opt5e/paladin-spell 3 :spirit-guardians 9)
                 (opt5e/paladin-spell 4 :banishment 13)
                 (opt5e/paladin-spell 4 :guardian-of-faith 13)
                 (opt5e/paladin-spell 5 :circle-of-power 17)
                 (opt5e/paladin-spell 5 :geas 17)
                 (mod5e/dependent-trait
                  {:name "Channel Divinity: Champion Challenge"
                   :source :scag
                   :page 133
                   :summary (str "creatures of your choice within 30 ft. cannot move more than 30 ft. from you on failed DC " (?spell-save-dc ::char5e/cha) " WIS save")})
                 (mod5e/bonus-action
                  {:name "Channel Divinity: Turn the Tide"
                   :page 133
                   :source :scag
                   :summary (str "creatures of your choice within 30 ft. and with half or less HPs regain 1d6 " (common/mod-str (?ability-bonuses ::char5e/cha)))})]
     :levels {7 {:modifiers [(mod5e/reaction
                              {:name "Divine Allegience"
                               :level 7
                               :page 133
                               :source :scag
                               :summary "take damage another creature would take"})]}
              15 {:modifiers [(mod5e/saving-throw-advantage [:paralyzed :stunned])
                             (mod5e/trait-cfg
                              {:name "Unyielding Spirit"
                               :page 133
                               :source :scag
                               :summary "advantage on saves against being stunned or paralyzed"})]}
              20 {:modifiers [(mod5e/action
                               {:name "Exalted Champion"
                                :page 86
                                :source :scag
                                :frequency units5e/long-rests-1
                                :duration units5e/hours-1
                                :summary "resistance to non-magical weapon slashing, bludgeoning, and piercing damage; allies within 30 ft. have advantage on death saves; you and allies have advantage on WIS saves"})]}}}]
   false))

(def scag-rogue
  {:name "Rogue"
   :subclass-level 3
   :subclass-title "Roguish Archetype"
   :subclasses [{:name "Mastermind"
                 :source "Sword Coast Adventurer's Guide"
                 :profs {:tool {:disguise-kit false
                                :forgery-kit false}
                         :tool-options {:gaming-set 1}
                         :language-options {:choose 2 :options {:any true}}}
                 :levels {3 {:modifiers [(mod5e/bonus-action
                                          {:name "Master of Tactics"
                                           :page 135
                                           :source :scag
                                           :summary "Help as bonus action"})]}
                          13 {:modifiers [(mod5e/reaction
                                           {:name "Misdirection"
                                            :page 135
                                            :source :scag
                                            :summary "when you are attacked and a creature within 5 ft is providing cover, you can have the attack target that creature instead"})]}}
                 :traits [{:name "Master of Intrigue"
                           :level 3
                           :page 135
                           :source :scag
                           :summary "unerringly mimic the speech of a creature you have heard for 1 min or more"}
                          {:name "Insightful Manipulator"
                           :level 9
                           :page 135
                           :source :scag
                           :summary "learn if superior or inferior to a creature you have observed for 1 min or more in these aspects: INT, WIS, CHA, class levels"}
                          {:name "Soul of Deceit"
                           :level 17
                           :page 135
                           :source :scag
                           :summary "your thoughts can't be read telepathically; when an attempt is made you can provide false thoughts; magical attempts to determine your truthfulness always result in true"}]}
                {:name "Swashbuckler"
                 :source "Sword Coast Adventurer's Guide"
                 :levels {3 {:modifiers [(mod/cum-sum-mod ?initiative (max 0 (?ability-bonuses ::char5e/cha)))
                                         (mod5e/dependent-trait
                                          {:name "Rakish Audacity"
                                           :level 3
                                           :page 136
                                           :source :scag
                                           :summary (str "can add CHA mod (" (common/bonus-str (?ability-bonuses ::char5e/cha)) ") to initiative; don't need advantage for Sneak Attack")})]}
                          9 {:modifiers [(mod5e/action
                                          {:name "Panache"
                                           :level 3
                                           :page 136
                                           :source :scag
                                           :duration units5e/minutes-1
                                           :summary "impose disadvantage on creature's attacks on others besides you if you success a CHA check contested by it's WIS check; it can only take opportunity attacks against you"})]}
                          13 {:modifiers [(mod5e/bonus-action
                                           {:name "Elegance Maneuver"
                                            :level 13
                                            :page 136
                                            :source :scag
                                            :summary "gain advantage to your next DEX or STR check during the turn"})]}}
                 :traits [{:name "Fancy Footwork"
                           :level 3
                           :page 135
                           :source :scag
                           :summary "when you make a melee attack the target cannot make opportunity attacks for the rest of the turn"}
                          {:name "Master Duelist"
                           :level 17
                           :page 136
                           :source :scag
                           :frequency units5e/rests-1
                           :summary "reroll a missed attack roll, this time with advantage"}]}]})

(def scag-sorcerer
  {:name "Sorcerer"
    :subclass-title "Sorcerous Origin"
    :subclass-level 1
    :subclasses [{:name "Storm Sorcery"
                  :modifiers [(mod5e/language :primordial)
                              (mod5e/bonus-action
                               {:name "Tempestuous Magic"
                                :page 137
                                :source :scag
                                :summary "before or after casting a first level spell or higher, fly up to 10 ft. without provoking opportunity attacks"})]
                  :levels {6 {:modifiers [(mod5e/damage-resistance :lightning)
                                          (mod5e/damage-resistance :thunder)
                                          (mod5e/dependent-trait
                                           {:name "Heart of the Storm"
                                            :page 137
                                            :source :scag
                                            :summary (str "When you cast 1st level spell or higher deal " (int (/ (?class-level :sorcerer) 2)) " lightning or thunder damage to creatures of your choice within 10 ft.")})
                                          (mod5e/action
                                           {:name "Storm Guide: Rain"
                                            :page 137
                                            :source :scag
                                            :summary "cause rain to stop falling in a 20 ft. radius sphere around you"})
                                          (mod5e/bonus-action
                                           {:name "Storm Guide: Wind"
                                            :page 137
                                            :source :scag
                                            :duration units5e/rounds-1
                                            :summary "if windy, choose the direction of wind within 100 foot radius sphere around you"})]}
                           14 {:modifiers [(mod5e/reaction
                                            {:name "Storm's Fury"
                                             :page 137
                                             :source :scag
                                             :summary (str "when hit with melee attack, deal " (?class-level :sorcerer) " lightning damage to attacker, if it fails a DC " (?spell-save-dc ::char5e/cha) " STR save it is pushed 20 ft. from you")})]}
                           18 {:modifiers [(mod5e/damage-immunity :lightning)
                                           (mod5e/damage-immunity :thunder)
                                           (mod5e/flying-speed-override 60)
                                           (mod5e/action
                                            {:name "Wind Soul"
                                             :page 137
                                             :source :scag
                                             :duration units5e/hours-1
                                             :frequency units5e/rests-1
                                             :summary (str "temporarily sacrifice 30 ft. of your flying speed to give 30 ft. to up to " (+ 3 (?ability-bonuses ::char5e/cha)) " other creatures")})]}}}]})

(def scag-warlock
  {:name "Warlock"
   :subclass-level 1
   :subclass-title "Otherworldly Patron"
   :subclasses [{:name "The Undying"
                 :traits [
                          {:name "Undying Nature"
                           :level 10
                           :page 140
                           :source :scag
                           :summary "don't need to breath, eat, drink, or sleep"}]
                 :levels {1 {:modifiers [(mod5e/spells-known 0 :spare-the-dying ::char5e/cha "Warlock")
                                         (mod5e/saving-throw-advantage [:disease])
                                         (mod5e/dependent-trait
                                          {:name "Among the Dead"
                                           :page 139
                                           :source :scag
                                           :summary (str "if an undead targets you, it must make a DC " (?spell-save-dc ::char5e/cha) " WIS save or choose another target")})]
                             :selections [(opt5e/warlock-subclass-spell-selection [:false-life :ray-of-sickness])]}
                          3 {:selections [(opt5e/warlock-subclass-spell-selection [:blindness-deafness :silence])]}
                          5 {:selections [(opt5e/warlock-subclass-spell-selection [:feign-death :speak-with-dead])]}
                          6 {:selections [(mod5e/dependent-trait
                                           {:name "Defy Death"
                                            :level 6
                                            :page 140
                                            :source :scag
                                            :frequency units5e/long-rests-1
                                            :summary (str "regain 1d8 " (common/bonus-str (?ability-bonuses ::char5e/con)) " HPs when you succeed on a death save or stabilize with spare the dying")})]}
                          7 {:selections [(opt5e/warlock-subclass-spell-selection [:aura-of-life :death-ward])]}
                          9 {:selections [(opt5e/warlock-subclass-spell-selection [:contagion :legend-lore])]}
                          14 {:modifiers [(mod5e/bonus-action
                                           {:name "Indestructible Life"
                                            :level 14
                                            :page 140
                                            :source :scag
                                            :summary (str "regain 1d8 " (common/bonus-str (?class-level :warlock)) " HPs and reattach severed parts")
                                            :frequency units5e/rests-1})]}}}]})

(def scag-wizard
  {:name "Wizard",
   :subclass-level 2
   :subclass-title "Arcane Tradition"
   :subclasses [{:name "Bladesinger"
                 :profs {:armor {:light false}
                         :save {::char5e/dex true ::char5e/int true}
                         :tool {:thieves-tools false}}
                 :prereqs [(t/option-prereq
                            "Elves and Half-Elves Only"
                            (fn [c] (#{"Elf" "Half-Elf"} @(subscribe [::char5e/race]))))]
                 :modifiers [(mod5e/skill-proficiency :performance)]
                 :selections [(opt5e/weapon-proficiency-selection
                               (map
                                :key
                                (filter
                                 (fn [weapon]
                                   (and (:melee? weapon)
                                        (not (:two-handed? weapon))))
                                 weapon5e/weapons))
                               1)]
                 :levels {2 {:modifiers [(mod5e/bonus-action
                                          {:level 2
                                           :name "Bladesong"
                                           :page 142
                                           :source :scag
                                           :duration units5e/minutes-1
                                           :frequency (units5e/rests 2)
                                           :summary (let [bonus (common/bonus-str (max 1 (?ability-bonuses ::char5e/int)))] (str bonus " AC; +10 speed; advantage on Acrobatics; " bonus " on concentration saves"))})]}
                          6 {:modifiers [(mod5e/num-attacks 2)
                                         (mod5e/trait-cfg
                                          {:name "Extra Attack"
                                           :page 142
                                           :source :scag
                                           :summary "you can attack twice when taking Attack action"})]}
                          10 {:modifiers [(mod5e/reaction
                                           {:name "Song of Defense"
                                            :page 142
                                            :source :scag
                                            :summary "expend an X-th level spell slot, reduce damage to you by 5X"})]}
                          14 {:modifiers [(mod5e/dependent-trait
                                           {:name "Song of Victory"
                                            :page 142
                                            :source :scag
                                            :summary (str "while bladesinging, add " (common/bonus-str (max 1 (?ability-bonuses ::char5e/int))) " to melee weapon attack damage")})]}}}]})

(def scag-classes
  [scag-barbarian
   scag-cleric
   scag-fighter
   scag-monk
   scag-paladin
   scag-rogue
   scag-sorcerer
   scag-warlock
   scag-wizard])

(def sword-coast-adventurers-guide-backgrounds
  (map
   (partial opt5e/add-sources :scag)
   [{:name "City Watch"
     :profs {:skill {:athletics true :insight true}
             :language-options {:choose 2 :options {:any true}}}
     :traits [{:name "Watcher's Eye"
               :page 145
               :summary "can easily find local watch and criminal outposts"}]
     :equipment {:manacles 1
                 :pouch 1}
     :custom-equipment {"Uniform with your rank" 1
                        "Horn to summon help" 1}
     :treasure {:gp 10}}
    {:name "Investigator"
     :profs {:skill {:investigation true :insight true}
             :language-options {:choose 2 :options {:any true}}}
     :traits [{:name "Watcher's Eye"
               :page 145
               :summary "can easily find local watch and criminal outposts"}]
     :equipment {:manacles 1
                 :pouch 1}
     :custom-equipment {"Uniform with your rank" 1
                        "Horn to summon help" 1}
     :treasure {:gp 10}}
    {:name "Clan Crafter"
     :profs {:skill {:history true :insight true}
             :tool-options {:artisans-tool 1}
             :language-options {:choose 1 :options {:any true}}}
     :traits [{:name "Respect of the Stout Folk"
               :page 145
               :summary "free room and board among shield and gold dwarves"}]
     :equipment {:pouch 1}
     :equipment-choices [opt5e/artisans-tools-choice-cfg]
     :custom-equipment {"Maker's Mark Chisel" 1}
     :treasure {:gp 5
                :gem-10-gp 1}}
    {:name "Cloistered Scholar"
     :profs {:skill {:history true}
             :skill-options {:choose 1 :options {:arcana true :nature true :religion true}}
             :language-options {:choose 2 :options {:any true}}}
     :traits [{:name "Library Access"
               :page 146
               :summary "free access to most of the library where you apprenticed"}]
     :equipment {:ink 1
                 :parchment 1
                 :pouch 1}
     :custom-equipment {"Cloister Robes" 1
                        "Quill" 1
                        "Penknife" 1
                        "Borrowed book" 1}
     :treasure {:gp 10}}
    {:name "Courtier"
     :profs {:skill {:insight true :persuasion true}
             :language-options {:choose 2 :options {:any true}}}
     :traits [{:name "Court Functionary"
               :page 147
               :summary "access to the workings of a government or court"}]
     :equipment {:clothes-fine 1
                 :pouch 1}
     :treasure {:gp 5}}
    {:name "Faction Agent"
     :profs {:skill {:insight true}
             :skill-options {:choose 1 :options {:animal-handling true :arcana true :deception true :history true :insight true :intimidation true :investigation true :medicine true :nature true :perception true :performance true :persuasion true :religion true :survival true}}
             :language-options {:choose 2 :options {:any true}}}
     :traits [{:name "Safe Haven"
               :page 148
               :summary "receive safe haven, room and board, or info from your network"}]
     :equipment {:clothes-common 1
                 :pouch 1}
     :custom-equipment {"Faction Badge/Emblem" 1
                        "Faction book" 1}
     :treasure {:gp 15}}
    {:name "Far Traveler"
     :source "Sword Coast Adventurer's Guide"
     :profs {:skill {:perception true :insight true}
             :tool-options {:musical-instrument 1}
             :language-options {:choose 1 :options {:any true}}}
     :traits [{:name "All Eyes on You"
               :page 149
               :summary "interest of scholars, nobles, and merchants"}]
     :equipment {:clothes-traveler-s 1
                 :pouch 1}
     :equipment-choices [{:name "Tool or Musical Instrument"
                          :options (zipmap (map :key (concat equip5e/artisans-tools equip5e/musical-instruments)) (repeat 1))}]
     :custom-equipment {"Poor maps of your homeland" 1}
     :custom-treasure {"Piece of jewelry from your homeland (10 GP)" 1}
     :treasure {:gp 5}}
    {:name "Inheritor"
     :source "Sword Coast Adventurer's Guide"
     :profs {:skill {:survival true}
             :skill-options {:choose 1 :options {:arcana true :history true :religion true}}
             :language-options {:choose 1 :options {:any true}}}
     :traits [{:name "Inheritance"
               :page 150
               :summary "an inherited item"}]
     :equipment {:clothes-traveler-s 1
                 :pouch 1}
     :custom-equipment {"Inheritance" 1}
     :treasure {:gp 15}}
    {:name "Knight of the Order"
     :source "Sword Coast Adventurer's Guide"
     :profs {:skill {:persuasion true}
             :skill-options {:choose 1 :options {:arcana true :history true :nature true :religion true}}
             :tool-options {:musical-instrument 1}
             :language-options {:choose 1 :options {:any true}}}
     :traits [{:name "Knightly Regard"
               :page 151
               :summary "shelter and aid from your order and supporters"}]
     :equipment {:pouch 1
                 :clothes-traveler-s 1}
     :custom-equipment {"Signet" 1
                        "Banner/seal of your rank" 1}
     :treasure {:gp 10}}
    {:name "Mercenary Veteran"
     :source "Sword Coast Adventurer's Guide"
     :profs {:skill {:athletics true :persuasion true}
             :tool {:land-vehicles true}
             :tool-options {:gaming-set 1}}
     :traits [{:name "Mercenary Life"
               :page 152
               :summary "can recall or find info about mercenary groups and can find mercenary work"}]
     :equipment {:pouch 1}
     :equipment-choices [{:name "Gaming Set"
                          :options (zipmap (map :key equip5e/gaming-sets) (repeat 1))}]
     :custom-equipment {"Uniform" 1
                        "Rank Insignia" 1}
     :treasure {:gp 10}}
    {:name "Urban Bounty Hunter"
     :source "Sword Coast Adventurer's Guide"
     :profs {:skill-options {:choose 2 :options {:deception true :insight true :persuasion true :stealth true}}}
     :selections [(opt5e/tool-selection (concat [:thieves-tools]
                                                (map :key equip5e/gaming-sets)
                                                (map :key equip5e/musical-instruments))
                                        2)]
     :traits [{:name "Ear to the Ground"
               :page 153
               :summary "you have contacts in any city that can provide info about people and places"}]
     :equipment-choices [{:name "Clothes Appropriate to Your Duties"
                          :options (zipmap (map :key equip5e/clothes) (repeat 1))}]
     :equipment {:pouch 1}
     :treasure {:gp 20}}
    {:name "Uthgardt Tribe Member"
     :source "Sword Coast Adventurer's Guide"
     :profs {:skill {:athletics true :survival true}
             :tool-options {:musical-instrument 1 :artisans-tool 1}
             :language-options {:choose 1 :options {:any true}}}
     :traits [{:name "Uthgardt Heritage"
               :page 154
               :summary "you are familiar with the wilderness of the North; can find 2X food and water when foraging; hospitality of your tribe and allies"}]
     :equipment {:hunting-trap 1
                 :pouch 1}
     :treasure {:gp 10}}
    {:name "Waterdhavian Noble"
     :source "Sword Coast Adventurer's Guide"
     :profs {:skill {:history true :persuasion true}
             :tool-options {:musical-instrument 1 :gaming-set 1}
             :language-options {:choose 1 :options {:any true}}}
     :traits [{:name "Kept in Style"
               :page 154
               :summary "2 GP per day of living expenses in the North are covered"}]
     :equipment {:clothes-fine 1
                 :purse 1}
     :custom-equipment {"Signet Ring or Brooch" 1
                        "Scroll of Pedigree" 1
                        "Skin of fine zzar or wine" 1}
     :treasure {:gp 20}}]))

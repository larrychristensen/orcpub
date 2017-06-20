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
                 :modifiers [opt5e/ua-al-illegal]
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
                 :modifiers [opt5e/ua-al-illegal]
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

(def wizard-option-cfg
  {:name "Wizard",
   :subclass-level 2
   :subclass-title "Arcane Tradition"
   :plugin? true
   :source :ua-warlock-and-wizard
   :subclasses [{:name "Lore Mastery"
                 :modifiers [opt5e/ua-al-illegal
                             (mod5e/skill-expertise :arcana)
                             (mod5e/skill-expertise :history)
                             (mod5e/skill-expertise :nature)
                             (mod5e/skill-expertise :religion)
                             (mod5e/trait-cfg
                              {:name "Lore Master"
                               :page 6
                               :source :ua-warlock-and-wizard})
                             (mod5e/trait-cfg
                              {:name "Spell Secrets"
                               :page 6
                               :source :ua-warlock-and-wizard})]
                 :levels {6 {:modifiers [(mod5e/trait-cfg
                                          {:name "Alchemical Casting"
                                           :page 6
                                           :source :ua-warlock-and-wizard})]}
                          10 {:modifiers [(mod5e/bonus-action
                                           {:name "Prodigious Memory"
                                            :page 6
                                            :source :ua-warlock-and-wizard})]}
                          14 {:modifiers [(mod5e/bonus-action
                                           {:name "Master of Magic"
                                            :page 6
                                            :source :ua-warlock-and-wizard})]}}}]})

(defn patron-prereq [patron]
  (t/option-prereq
   (str "Your patron must be " patron)
   (fn [c] (some
            (fn [[k {:keys [subclass-name]}]]
              (= patron subclass-name))
            @(subscribe [::char5e/levels nil c])))))

(def hexblade-prereq
  (patron-prereq "The Hexblade"))

(def great-old-one-prereq
  (patron-prereq "The Great Old One"))

(def raven-queen-prereq
  (patron-prereq "The Raven Queen"))

(def fiend-prereq
  (patron-prereq "The Fiend"))

(def archfey-prereq
  (patron-prereq "The Archfey"))

(def seeker-prereq
  (patron-prereq "The Seeker"))

(def eldritch-invocation-selection
  (opt5e/eldritch-invocation-selection
   {:min 0
    :max 0
    :options (map
              (fn [o]
                (update o ::t/modifiers conj opt5e/ua-al-illegal))
              [(opt5e/eldritch-invocation-option
                {:name "Aspect of the Moon (UAWW)"
                 :prereqs [archfey-prereq]
                 :page 3
                 :source :ua-warlock-and-wizard
                 :summary "You don't need sleep and can't be forced to sleep."})
               (opt5e/eldritch-invocation-option
                {:name "Burning Hex"
                 :prereqs [hexblade-prereq]
                 :page 3
                 :source :ua-warlock-and-wizard
                 :summary (str "Cause a target cursed by your Hexblade's Curse to take " (?ability-bonuses ::char5e/cha) " points of fire damage.")
                 :trait-type :bonus-action})
               (opt5e/eldritch-invocation-option
                {:name "Caiphon's Beacon"
                 :prereqs [great-old-one-prereq]
                 :page 3
                 :source :ua-warlock-and-wizard
                 :summary "You have advantage on attack rolls against charmed targets"
                 :modifiers [(mod5e/skill-proficiency :deception)
                             (mod5e/skill-proficiency :stealth)]})
               (opt5e/eldritch-invocation-option
                {:name "Chilling Hex"
                 :prereqs [hexblade-prereq]
                 :page 3
                 :source :ua-warlock-and-wizard
                 :summary (str "Deal " (?ability-bonuses ::char5e/cha) " points of fire damage to all of your enemies within 5 ft. of a target cursed with your Hexblade's Curse")
                 :trait-type :bonus-action})
               (opt5e/eldritch-invocation-option
                {:name "Chronicle of the Raven Queen"
                 :prereqs [raven-queen-prereq
                           opt5e/pact-of-the-tome-prereq]
                 :page 3
                 :source :ua-warlock-and-wizard
                 :duration {:units :turn}
                 :trait-type :bonus-action
                 :summary "Gain the ability to see through objects to 30 ft, with darkvision within that range."})
               (opt5e/eldritch-invocation-option
                {:name "Claw of Acamar"
                 :prereqs [great-old-one-prereq
                           opt5e/pact-of-the-blade-prereq]
                 :page 3
                 :summary "Create a flail with the reach property. You can expend a spell slot to do an extra 2d8 necrotic damage per slot level when you hit with it. The creature's speed also becomes 0 until the end of your next turn."
                 :source :ua-warlock-and-wizard})
               (opt5e/eldritch-invocation-option
                {:name "Cloak of Baalzebul"
                 :prereqs [fiend-prereq]
                 :page 3
                 :source :ua-warlock-and-wizard
                 :summary (str "You gain a 5 ft. radius aura that gives you advantage on Intimidation, but disadvantage on other CHA checks. Other creatures in the aura at the start of their turn take " (?ability-bonuses ::char5e/cha) " points of poison damage.")
                 :trait-type :bonus-action})
               (opt5e/eldritch-invocation-option
                {:name "Curse Bringer"
                 :prereqs [hexblade-prereq]
                 :page 6
                 :source :ua-warlock-and-wizard
                 :summary "When you hit with 'eldritch blast' you can move the target up to 10 ft. toward you."})
               (opt5e/eldritch-invocation-option
                {:name "Kiss of Mephistopheles (UAWW)"
                 :prereqs [(opt5e/total-levels-option-prereq 5 :warlock)
                           fiend-prereq
                           opt5e/has-eldritch-blast-prereq]
                 :page 6
                 :summary "When you hit with 'eldritch blast', cast 'fireball' centered on the target using a warlock spell slot"
                 :trait-type :bonus-action})
               (opt5e/eldritch-invocation-option
                {:name "Frost Lance (UAWW)"
                 :prereqs [archfey-prereq
                           opt5e/has-eldritch-blast-prereq]
                 :page 4
                 :source :ua-warlock-and-wizard
                 :frequency {:units :turn}
                 :summary "When you hit with eldritch blast, you can reduce the target's speed by 10 ft. until your next turn."
                 :trait-type :bonus-action})
               (opt5e/eldritch-invocation-option
                {:name "Gaze of Khirad"
                 :prereqs [(opt5e/total-levels-option-prereq 7 :warlock)
                           great-old-one-prereq]
                 :page 4
                 :source :ua-warlock-and-wizard
                 :duration {:units :turn}
                 :summary "Gain the ability to see through objects to 30 ft, with darkvision within that range."
                 :trait-type :action})
               (opt5e/eldritch-invocation-option
                {:name "Grasp of Hadar (UAWW)"
                 :prereqs [great-old-one-prereq
                           opt5e/has-eldritch-blast-prereq]
                 :page 4
                 :source :ua-warlock-and-wizard
                 :frequency {:units :turn}
                 :summary "When you hit with 'eldritch blast' you can move the target up to 10 ft. toward you."})
               (opt5e/eldritch-invocation-option
                {:name "Green Lord's Gift"
                 :prereqs [archfey-prereq]
                 :page 4
                 :source :ua-warlock-and-wizard
                 :summary "If your familiar is within 100 ft. when you regain hit points, you regain the max for rolls"})
               (opt5e/eldritch-invocation-option
                {:name "Improved Pact Weapon (UAWW)"
                 :prereqs [(opt5e/total-levels-option-prereq 5 :warlock)
                           opt5e/pact-of-the-blade-prereq]
                 :page 4
                 :source :ua-warlock-and-wizard
                 :summary "If your Pact of the Blade weapon is non-magical, it counts as magic and has a +1 bonus to attack and damage."})
               (opt5e/eldritch-invocation-option
                {:name "Mace of Dispater"
                 :prereqs [fiend-prereq
                           opt5e/pact-of-the-blade-prereq]
                 :page 4
                 :source :ua-warlock-and-wizard
                 :summary "When you create a mace pact weapon, it has a special property. You can expend a spell slot and it will deal an extra 2d8 force damage per spell level of the slot and can knock a Huge or smaller target prone."})
               (opt5e/eldritch-invocation-option
                {:name "Moon Bow"
                 :prereqs [archfey-prereq
                           opt5e/pact-of-the-blade-prereq]
                 :page 4
                 :source :ua-warlock-and-wizard
                 :summary "Create a longbow as your pact weapon. You can expend a spell slot and it will deal an extra 2d8 radiant damage per spell level of the slot"})
               (opt5e/eldritch-invocation-option
                {:name "Path of the Seeker"
                 :prereqs [seeker-prereq]
                 :page 4
                 :source :ua-warlock-and-wizard
                 :modifiers [(mod5e/saving-throw-advantage [:paralyzed])]
                 :summary "You ignore difficult terrain, have advantage on saves against being paralyzed, and have advantage on checks to escape rope binding, manacels, or grapples."})
               (opt5e/eldritch-invocation-option
                {:name "Raven Queen's Blessing"
                 :prereqs [raven-queen-prereq
                           opt5e/has-eldritch-blast-prereq]
                 :page 5
                 :source :ua-warlock-and-wizard
                 :summary "When you crit with 'eldritch blast', you or an ally may expend and roll an HD to regain HPs equal to the roll plus it's CON mod."})
               (opt5e/eldritch-invocation-option
                {:name "Relentless Hex (UAWW)"
                 :prereqs [(opt5e/total-levels-option-prereq 5 :warlock)
                           hexblade-prereq]
                 :page 5
                 :source :ua-warlock-and-wizard
                 :summary "You can transport up to 30 ft. to an unoccupied space within 5 ft. of a creature affected by your hex."
                 :trait-type :bonus-action})
               (opt5e/eldritch-invocation-option
                {:name "Sea Twin's Gift"
                 :prereqs [archfey-prereq]
                 :page 5
                 :source :ua-warlock-and-wizard
                 :summary "You can breath underwater"
                 :modifiers [(mod5e/swimming-speed-equal-to-walking)
                             (mod5e/spells-known 3 :water-breathing ::char5e/cha "Warlock" 0 "once per long rest")]})
               (opt5e/eldritch-invocation-option
                {:name "Seeker's Speech"
                 :prereqs [seeker-prereq]
                 :page 5
                 :source :ua-warlock-and-wizard
                 :summary "Choose two languages when you finish a long rest, you have mastered them until you finish your next long rest"})
               (opt5e/eldritch-invocation-option
                {:name "Shroud of Ulban"
                 :prereqs [(opt5e/total-levels-option-prereq 18 :warlock)
                           great-old-one-prereq]
                 :page 5
                 :source :ua-warlock-and-wizard
                 :trait-type :action
                 :summary "Turn invisible for 1 minute"})
               (opt5e/eldritch-invocation-option
                {:name "Superior Pact Weapon"
                 :prereqs [(opt5e/total-levels-option-prereq 9 :warlock)
                           opt5e/pact-of-the-blade-prereq]
                 :page 5
                 :source :ua-warlock-and-wizard
                 :summary "If your Pact of the Blade weapon is non-magical, it counts as magic and has a +2 bonus to attack and damage."})
               (opt5e/eldritch-invocation-option
                {:name "Tomb of Levistus (UAWW)"
                 :prereqs [fiend-prereq]
                 :page 5
                 :source :ua-warlock-and-wizard
                 :trait-type :reaction
                 :frequency {:units :rest}
                 :summary "When you take damage, gain 10 temp HPs. In addition, gain vulnerability to fire damage, and speed is 0, which go away at the end of your next turn."})
               (opt5e/eldritch-invocation-option
                {:name "Ultimate Pact Weapon"
                 :prereqs [(opt5e/total-levels-option-prereq 15 :warlock)
                           opt5e/pact-of-the-blade-prereq]
                 :page 5
                 :source :ua-warlock-and-wizard
                 :summary "If your Pact of the Blade weapon is non-magical, it counts as magic and has a +3 bonus to attack and damage."})])}))

(def ua-warlock-and-wizard-plugin
  {:name "Unearthed Arcana: Warlock and Wizard"
   :class-options? true
   :key :ua-warlock-and-wizard
   :selections [(opt5e/class-selection
                 {:options (map
                            opt5e/class-option
                            [warlock-option-cfg
                             wizard-option-cfg])})
                eldritch-invocation-selection]})

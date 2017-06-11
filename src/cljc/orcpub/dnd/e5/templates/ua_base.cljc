(ns orcpub.dnd.e5.templates.ua-base
  (:require [orcpub.template :as t]
            [orcpub.common :as common]
            [orcpub.modifiers :as mods]
            [orcpub.entity-spec :as es]
            [orcpub.dnd.e5.modifiers :as mod5e]
            [orcpub.dnd.e5.character :as char5e]
            [orcpub.dnd.e5.options :as opt5e]
            [orcpub.dnd.e5.weapons :as weapon5e]
            [orcpub.dnd.e5.spell-lists :as sl]
            [orcpub.dnd.e5.templates.ua-mystic :as ua-mystic]
            [orcpub.dnd.e5.templates.ua-artificer :as ua-artificer]
            [re-frame.core :refer [subscribe]]))

(defn ua-help [name url]
  [:a {:href url :target :_blank} name])

(def ua-eberron-kw :ua-eberron)

(defn dragonmark-spell-mod [kw ability-kw lvl]
  (mod5e/spells-known (opt5e/spell-level kw) kw ability-kw "Dragonmark" lvl))

(def ua-mystic-kw :ua-mystic)

(defn psionic-discipline [name page summary type]
  (let [trait-cfg {:name name
                   :page page
                   :source ua-mystic-kw
                   :summary summary}]
    (case type
      :trait (mod5e/trait-cfg trait-cfg)
      :action (mod5e/action trait-cfg)
      :reaction (mod5e/reaction trait-cfg)
      :bonus-action (mod5e/bonus-action trait-cfg))))

(defn psionic-disciplines-selection [num & [name filter-fn]]
  (t/selection-cfg
   {:name (str name (if name " ") "Psionic Disciplines")
    :tags #{:class}
    :min (or num 1)
    :max (or num 1)
    :multiselect? true
    :ref (if name
           [:class :mystic :levels :level-1 :mystic-order (common/name-to-kw name) :psionic-disciplines]
           [:class :mystic :psionic-disciplines])
    :options (map
              t/option-cfg
              (if filter-fn
                (filter filter-fn ua-mystic/psionic-disciplines)
                ua-mystic/psionic-disciplines))}))

(def ua-trio-of-subclasses-kw :ua-trio-of-subclasses)

(def ua-trio-of-subclasses-classes
  [(opt5e/subclass-plugin
    opt5e/monk-base-cfg
    ua-trio-of-subclasses-kw
    [{:name "Way of the Drunken Master"
      :modifiers [(mod5e/skill-proficiency :performance)
                  (mod5e/trait-cfg
                   {:name "Drunken Technique"
                    :page 1
                    :source ua-trio-of-subclasses-kw
                    :summary "When you use Flurry of Blows, you can Disengage and walking speed increases by 10 ft."})]
      :levels {6 {:modifiers [(mod5e/reaction
                               {:name "Tipsy Sway"
                                :page 1
                                :source ua-trio-of-subclasses-kw
                                :frequency {:units :rest}
                                :summary "When an enemy misses you with melee attack you can have the attack hit another creature within 5 ft. of you"})]}
               11 {:modifiers [(mod5e/trait-cfg
                                {:name "Drunkard's Luck"
                                 :page 1
                                 :source ua-trio-of-subclasses-kw
                                 :summary "when you make a save, you may spend a ki to gain advantage on the roll"})]}
               17 {:modifiers [(mod5e/trait-cfg
                                {:name "Intoxicated Frenzy"
                                 :page 1
                                 :source ua-trio-of-subclasses-kw
                                 :summary "When you use Flurry of Blows, you can make 3 additional attacks if each attack targets a different creature"})]}}}]
    true)
   (opt5e/subclass-plugin
    opt5e/paladin-base-cfg
    ua-trio-of-subclasses-kw
    [{:name "Oath of Redemption"
      :modifiers [(opt5e/paladin-spell 1 :shield 3)
                  (opt5e/paladin-spell 1 :sleep 3)
                  (opt5e/paladin-spell 2 :hold-person 5)
                  (opt5e/paladin-spell 2 :ray-of-enfeeblement 5)
                  (opt5e/paladin-spell 3 :counterspell 9)
                  (opt5e/paladin-spell 3 :hypnotic-pattern 9)
                  (opt5e/paladin-spell 4 :otilukes-resilient-sphere 13)
                  (opt5e/paladin-spell 4 :stoneskin 13)
                  (opt5e/paladin-spell 5 :hold-monster 17)
                  (opt5e/paladin-spell 5 :wall-of-force 17)
                  (mod5e/ac-bonus-fn
                   (fn [armor shield]
                     (if (and (nil? shield)
                              (nil? armor))
                       6
                       0)))
                  (mod5e/trait-cfg
                   {:name "Warrior of Reconciliation"
                    :source ua-trio-of-subclasses-kw
                    :page 2
                    :duration opt5e/minutes-1
                    :summary "when using a simple bludgeoning weapon and you reduce a creature to 0 HPs, the creature is charmed instead"})
                  (mod5e/bonus-action
                   {:name "Channel Divinity: Emissary of Peace"
                    :source ua-trio-of-subclasses-kw
                    :page 2
                    :summary "gain +5 to the next charisma check you make in the next minute."})
                  (mod5e/reaction
                   {:name "Channel Divinity: Rebuke the Violent"
                    :page 2
                    :source ua-trio-of-subclasses-kw
                    :range opt5e/ft-10
                    :summary (str "when an enemy deals melee damage to someone other than you, it takes radiant damage equal to the amount it dealt, half on a successful DC " (?spell-save-dc ::char5e/cha) " WIS save.")})]
      :levels {7 {:modifiers [(mod5e/reaction
                               {:name "Aura of the Guardian"
                                :page 2
                                :source ua-trio-of-subclasses-kw
                                :range opt5e/ft-10
                                :summary "magically absorb the damage an ally would take"})]}
               15 {:modifiers [(mod5e/dependent-trait
                                {:name "Protective Spirit"
                                 :page 2
                                 :source ua-trio-of-subclasses-kw
                                 :summary (str "at the end of your turn in combat, regain 1d6 + " (int (/ (?class-level :paladin) 2)) " HPs if you have less than " (int (/ ?max-hit-points 2)))})]}
               20 {:modifiers [(mod5e/trait-cfg
                                {:name "Emissary of Redemption"
                                 :page 3
                                 :source ua-trio-of-subclasses-kw
                                 :sumary "unless you attack, damage, or force it to make a save, you have resistance to all damage dealt by a creature and it takes damage equal to half that it dealt to you"})]}}}]
    true)
   
   ;; when revised ranger is added, there needs to be a corresponding subclass that gets an extra attack
   (opt5e/subclass-plugin
    opt5e/ranger-base-cfg
    ua-trio-of-subclasses-kw
    [{:name "Monster Slayer"
      :modifiers [(mod5e/spells-known 1 :protection-from-evil-and-good ::char5e/wis "Ranger")
                  (mod5e/bonus-action
                   {:name "Slayer's Eye"
                    :page 3
                    :source ua-trio-of-subclasses-kw
                    :range opt5e/ft-120
                    :summary "learn creature's vulnerabilities, immunities, and resistances, as well as special effects triggered by damage; target also takes 1d6 the first time you hit with a weapon attack"})]
      :levels {5 {:modifiers [(mod5e/spells-known 2 :zone-of-truth ::char5e/wis "Ranger")]}
               7 {:modifiers [(mod5e/trait-cfg
                               {:name "Supernatural Defense"
                                :page 3
                                :source ua-trio-of-subclasses-kw
                                :summary "when the target of your Slayer's Eye causes you to make a save, add a 1d6 to your roll"})]}
               9 {:modifiers [(mod5e/spells-known 3 :magic-circle ::char5e/wis "Ranger")]}
               11 {:modifiers [(mod5e/reaction
                                {:name "Relentless Slayer"
                                 :page 3
                                 :source ua-trio-of-subclasses-kw
                                 :range opt5e/ft-30
                                 :summary "when the target of your Slayer's Eye tries to change shape, teleport, travel to another plane, or turn gaseous, make a contested WIS check with the target, if you succeed, it fails the attempt"})]}
               13 {:modifiers [(mod5e/spells-known 4 :banishment ::char5e/wis "Ranger")]}
               15 {:modifiers [(mod5e/reaction
                                {:name "Slayer's Counter"
                                 :page 3
                                 :source ua-trio-of-subclasses-kw
                                 :summary "when the target of your Slayer's Eye forces you to make a save, make a weapon attack and, if it hits, you automatically succeed on the save"})]}
               17 {:modifiers [(mod5e/spells-known 5 :planar-binding ::char5e/wis "Ranger")]}}}]
    true)])

(def ua-trio-of-subclasses-plugin
  {:name "Unearthed Arcana: A Trio of Subclasses"
   :key ua-trio-of-subclasses-kw
   :selections [(opt5e/class-selection
                 {:options (map
                            opt5e/class-option
                            ua-trio-of-subclasses-classes)})]})

(def ua-revised-subclasses-kw :ua-revised-subclasses)

(defn kensei-weapon-selection [num]
  (t/selection-cfg
   {:name "Kensei Weapons"
    :min num
    :max num
    :multiselect? true
    :tags #{:class}
    :ref [:class :monk :levels :level-3 :monastic-tradition :way-of-the-kensei :kensei-weapons]
    :options (map
              (fn [{:keys [name key]}]
                (t/option-cfg
                 {:name name
                  :modifiers [(mod5e/weapon-proficiency key)
                              (mods/set-mod ?kensei-weapons name)]}))
              (remove
               (fn [weapon]
                 (or (:heavy? weapon)
                     (:special weapon)))
               weapon5e/weapons))}))

(def barbarian-path-of-the-ancestral-guardian
  {:name "Barbarian"
   :plugin? true
   :subclass-level 3
   :subclass-title "Primal Path"
   :source ua-revised-subclasses-kw
   :subclasses [{:name "Path of the Ancestral Guardian"
                 :source ua-revised-subclasses-kw
                 :modifiers [opt5e/ua-al-illegal
                             (mod5e/bonus-action
                              {:name "Ancestral Protectors"
                               :page 1
                               :source ua-revised-subclasses-kw
                               :duration {:units :round}
                               :summary "while raging, the first creature you hit with an attack on your turn gains disadvantage on attacks that don't target you and other creatures have reistance to the targets attacks"})]
                 :levels {6 {:modifiers [(mod5e/reaction
                                          {:name "Spirit Shield"
                                           :page 1
                                           :source ua-revised-subclasses-kw
                                           :summary (str "reduce the damage a creature takes by " (mod5e/level-val
                                                                                                   (?class-level :barbarian)
                                                                                                   {10 3
                                                                                                    14 4
                                                                                                    :default 2}) "d8")})]}}
                 :traits [{:name "Consult the Spirits"
                           :level 10
                           :page 1
                           :source ua-revised-subclasses-kw
                           :frequency {:units :rest}
                           :summary "cast 'clairvoyance' spell without a slot and with WIS as ability"}
                          {:name "Vengeful Ancestors"
                           :level 14
                           :page 1
                           :source ua-revised-subclasses-kw
                           :summary "when you use Spirit Shield to prevent damage, the attacker takes the prevented damage"}]}]})

(def bard-college-of-swords
  {:name "Bard"
   :plugin? true
   :source ua-revised-subclasses-kw
   :subclass-level 3
   :subclass-title "Bard College"
   :subclasses [{:name "College of Swords"
                 :modifiers [opt5e/ua-al-illegal
                             (mod5e/armor-proficiency :medium)
                             (mod5e/weapon-proficiency :scimitar)
                             (mod5e/action
                              {:name "Blade Flourish"
                               :page 2
                               :source ua-revised-subclasses-kw
                               :summary "make a melee weapon attack with one of the Blade Flourish options and gain +10 walking speed"})
                             (mod5e/action
                              {:name "Blade Flourish: Defensive Flourish"
                               :page 2
                               :source ua-revised-subclasses-kw
                               :duration opt5e/rounds-1
                               :summary "add a Bardic Inspiration die to your AC"})
                             (mod5e/action
                              {:name "Blade Flourish: Slashing Flourish"
                               :page 2
                               :source ua-revised-subclasses-kw
                               :range opt5e/ft-5
                               :summary "if the attack hits, do a Bardic Inspiration die worth of damage to other creatures of your choice"})
                             (mod5e/action
                              {:name "Blade Flourish: Mobile Flourish"
                               :page 2
                               :source ua-revised-subclasses-kw
                               :summary "if the attack hits, use a Bardic Inspiration to push the target 5 + the Bardic Inpiration roll ft. away, and you may use reaction to move to within 5 ft of target"})]
                 :selections [(opt5e/fighting-style-selection :bard #{:dueling :two-weapon-fighting})]
                 :levels {6 {:modifiers [(mod5e/trait-cfg
                                          {:name "Cunning Flourish"
                                           :page 2
                                           :source ua-revised-subclasses-kw
                                           :summary "attack twice when you use Blade Flourish"})]}
                          14 {:modifiers [(mod5e/trait-cfg
                                           {:name "Master's Flourish"
                                            :level 14
                                            :page 55
                                            :summary "when you use Blade Flourish, you can roll a d6 instead of expending a Bardic Inspiration"})]}}}]})

(def fighter-arcane-archer
  {:name "Fighter",
   :plugin? true
   :subclass-level 3
   :subclass-title "Martial Archetype"
   :source ua-revised-subclasses-kw
   :subclasses [{:name "Arcane Archer"
                 :levels {3 {:modifiers [opt5e/ua-al-illegal
                                         (mod5e/trait-cfg
                                          {:name "Magic Arrow"
                                           :page 3
                                           :source ua-revised-subclasses-kw
                                           :summary "when you fire a non-magical arrow, you can temporarily make it magic with +1 bonus"})
                                         (mod5e/dependent-trait
                                          {:name "Arcane Shot"
                                           :page 3
                                           :source ua-revised-subclasses-kw
                                           :frequency {:units :rest
                                                       :amount (mod5e/level-val
                                                                (?class-level :fighter)
                                                                {7 3
                                                                 10 4
                                                                 15 5
                                                                 18 6
                                                                 :default 2})}
                                           :summary "when you fire a magic arrow as part of an Attack, you can use an Arcane Shot Option"})
                                         (mod5e/dependent-trait
                                          {:name "Arcane Shot: Banishing Arrow"
                                           :page 3
                                           :source ua-revised-subclasses-kw
                                           :summary (str "If the arrow hits, banish the creature until the end of it's next turn, it's speed becomes 0 and it is incapacitated unless it succeeds on a DC " (?spell-save-dc ::char5e/int) " CHA save. " (if (>= (?class-level :fighter) 18) " The target also takes 2d6 force damage."))})
                                         (mod5e/dependent-trait
                                          {:name "Arcane Shot: Brute Bane Arrow"
                                           :page 3
                                           :source ua-revised-subclasses-kw
                                           :summary (str "If the arrow hits, deal " (if (>= (?class-level :fighter) 18) 4 2) "d6 extra necrotic damage to the target, if it fails a DC " (?spell-save-dc ::char5e/int) " CON save, it's attacks deal half damage until start of your next turn")})
                                         (mod5e/dependent-trait
                                          {:name "Arcane Shot: Bursting Arrow"
                                           :page 3
                                           :source ua-revised-subclasses-kw
                                           :summary (str "if the arrow hits, deal and extra " (if (>= (?class-level :fighter) 18) 4 2) "d6 force damage to creatures within 10 ft. of the target")})
                                         (mod5e/dependent-trait
                                          {:name "Arcane Shot: Grasping Arrow"
                                           :page 3
                                           :source ua-revised-subclasses-kw
                                           :summary (let [die-count (if (>= (?class-level :fighter) 18) 4 2)]
                                                      (str "if the arrow hits, grasping brambles deal an extra " die-count "d6 poison damage, target's speed is reduced by 10 ft. and takes " die-count "d6 slashing damage the first time it moves on each of it's turns. The brambles can be removed with a DC " (?spell-save-dc ::char5e/int) " STR save."))})
                                         (mod5e/dependent-trait
                                          {:name "Arcane Shot: Mind Scrambling Arrow"
                                           :page 4
                                           :source ua-revised-subclasses-kw
                                           :summary (str "if the arrow hits, deal an extra " (if (>= (?class-level :fighter) 18) 4 2) "d6 psychic damage, and the target must succeed on a DC " (?spell-save-dc ::char5e/int) " WIS save or cannot harm an ally of your choosing")})
                                         (mod5e/dependent-trait
                                          {:name "Arcane Shot: Piercing Arrow"
                                           :page 4
                                           :source ua-revised-subclasses-kw
                                           :summary (str "instead of an attack roll, you deal the arrows damage to all creatures within a 1 ft. X 30 ft. line plus an extra " (if (>= (?class-level :fighter) 18) 2 1) "d6 piercing damage, half damage on successful DC " (?spell-save-dc ::char5e/int) " DEX save")})
                                         (mod5e/dependent-trait
                                          {:name "Arcane Shot: Seeking Arrow"
                                           :page 4
                                           :source ua-revised-subclasses-kw
                                           :summary (str "instead of an attack roll, choose 1 creature you have seen in the past minute, it must succeed on a DC " (?spell-save-dc ::char5e/int) " DEX save or be hit by the arrow, which can move around corners, taking the arrow's damage plus an extra " (if (>= (?class-level :fighter) 18) 2 1) "d6 force damage, half on a successful save")})
                                         (mod5e/dependent-trait
                                          {:name "Arcane Shot: Shadow Arrow"
                                           :page 4
                                           :source ua-revised-subclasses-kw
                                           :duration {:units :round}
                                           :summary (str "if the arrow hits, deal an extra " (if (>= (?class-level :fighter) 18) 4 2) "d6 psychic damage and target must succeed on a DC " (?spell-save-dc ::char5e/int) " WIS save or cannot see beyond 5 ft")})]
                             :selections [(opt5e/skill-selection [:arcana :nature] 1)]}
                          7 {:modifiers [(mod5e/bonus-action
                                          {:name "Curving Shot"
                                           :page 3
                                           :source ua-revised-subclasses-kw
                                           :summary "when you miss with a magic arrow, you may reroll the attack against another target within 60 ft. of the original"})]}
                          15 {:modifiers [(mod5e/trait-cfg
                                           {:page 3
                                            :source ua-revised-subclasses-kw
                                            :name "Ever-Ready Shot"
                                            :summary "Regain a use of Arcane Shot if you roll initiative and have none"})]}}}]})

(def monk-way-of-the-kensei
  (merge
    opt5e/monk-base-cfg
    {:source ua-revised-subclasses-kw
     :plugin? true
     :subclasses [{:name "Way of the Kensei"
                   :selections [(kensei-weapon-selection 2)]
                   :modifiers [opt5e/ua-al-illegal
                               (mod5e/dependent-trait
                                {:name "Path of the Kensei"
                                 :page 4
                                 :source ua-revised-subclasses-kw
                                 :summary (str "Your kensei weapons are " (common/list-print ?kensei-weapons) ". If you make an unarmed strike Attack and you have a melee kensei weapon in hand, gain +2 AC until start of your next turn. You can use a bonus action to add 1d4 damage to your ranged kensei weapon attacks for your turn.")})]
                   :levels {6 {:selections [(kensei-weapon-selection 1)]
                               :modifiers [(mod5e/trait-cfg
                                            {:name "One with the Blade"
                                             :page 5
                                             :source ua-revised-subclasses-kw
                                             :frequency {:units :round}
                                             :summary "kensei weapons count as magical; when you hit with a kensei weapon you may spend 1 ki to add damage equal to you Martial Arts die"})]}
                            11 {:selections [(kensei-weapon-selection 1)]
                                :modifiers [(mod5e/bonus-action
                                             {:name "Sharpen the Blade"
                                              :page 5
                                              :source ua-revised-subclasses-kw
                                              :duration {:units :minute}
                                              :summary "spend X ki (max 3) to grant a kensei weapon an X bonus to attack and damage rolls"})]}
                            17 {:selections [(kensei-weapon-selection 1)]
                                :modifiers [(mod5e/trait-cfg
                                             {:name "Unerring Accuracy"
                                              :page 5
                                              :source ua-revised-subclasses-kw
                                              :frequency {:units :round}
                                              :summary "if you miss with a monk weapon, reroll the attack"})]}}}]}))

(def sorcerer-favored-soul
  {:name "Sorcerer"
   :subclass-title "Sorcerous Origin"
   :subclass-level 1
   :plugin? true
   :subclasses [{:name "Favored Soul"
                 :modifiers [opt5e/ua-al-illegal
                             (mod5e/spells-known 1 :cure-wounds ::char5e/cha "Sorcerer")
                             (mod5e/trait-cfg
                              {:name "Favored by the Gods"
                               :page 5
                               :source ua-revised-subclasses-kw
                               :frequency {:units :rest}
                               :summary "if you fail a save or miss an attack, you may roll 2d4 and add it to the missed roll"})]
                 :selections [(opt5e/subclass-cantrip-selection :sorcerer "Sorcerer" ::char5e/cha (get-in sl/spell-lists [:cleric 0]) 0)
                              (opt5e/subclass-spell-selection :sorcerer "Sorcerer" ::char5e/cha (get-in sl/spell-lists [:cleric 1]) 0)]
                 :levels {3 {:selections [(opt5e/subclass-spell-selection :sorcerer "Sorcerer" ::char5e/cha (get-in sl/spell-lists [:cleric 2]) 0)]}
                          5 {:selections [(opt5e/subclass-spell-selection :sorcerer "Sorcerer" ::char5e/cha (get-in sl/spell-lists [:cleric 3]) 0)]}
                          6 {:modifiers [(mod5e/dependent-trait
                                          {:name "Empowered Healing"
                                           :page 5
                                           :source ua-revised-subclasses-kw
                                           :summary "you may reroll healing dice once"})]}
                          7 {:selections [(opt5e/subclass-spell-selection :sorcerer "Sorcerer" ::char5e/cha (get-in sl/spell-lists [:cleric 4]) 0)]}
                          9 {:selections [(opt5e/subclass-spell-selection :sorcerer "Sorcerer" ::char5e/cha (get-in sl/spell-lists [:cleric 5]) 0)]}
                          11 {:selections [(opt5e/subclass-spell-selection :sorcerer "Sorcerer" ::char5e/cha (get-in sl/spell-lists [:cleric 6]) 0)]}
                          13 {:selections [(opt5e/subclass-spell-selection :sorcerer "Sorcerer" ::char5e/cha (get-in sl/spell-lists [:cleric 7]) 0)]}
                          15 {:selections [(opt5e/subclass-spell-selection :sorcerer "Sorcerer" ::char5e/cha (get-in sl/spell-lists [:cleric 8]) 0)]}
                          17 {:selections [(opt5e/subclass-spell-selection :sorcerer "Sorcerer" ::char5e/cha (get-in sl/spell-lists [:cleric 9]) 0)]}
                          14 {:modifiers [(mod5e/bonus-action
                                           {:name "Angelic Form"
                                            :page 5
                                            :source ua-revised-subclasses-kw
                                            :summary "gain flying speed of 30 ft."})]}
                          18 {:modifiers [(mod5e/bonus-action
                                           {:name "Unearthly Recovery"
                                            :page 5
                                            :source ua-revised-subclasses-kw
                                            :frequency {:units :long-rest}
                                            :summary (str "regain " (int (/ ?max-hit-points)) " HPs if you have that many or fewer left")})]}}}]})

(def ua-revised-classes
  [barbarian-path-of-the-ancestral-guardian
   bard-college-of-swords
   fighter-arcane-archer
   monk-way-of-the-kensei
   sorcerer-favored-soul])

(def ua-revised-subclasses-plugin
  {:name "Unearthed Arcana: Revised Subclasses"
   :key ua-revised-subclasses-kw
   :selections [(opt5e/class-selection
                 {:options (map
                            opt5e/class-option
                            ua-revised-classes)})]})

(def ua-mystic-order-of-the-awakened
  {:name "Order of the Awakened"
   :selections [(psionic-disciplines-selection 2 "Order of the Awakened" (fn [d] (= :awakened (:mystic-order d))))
                (opt5e/skill-selection [:animal-handling :deception :insight :intimidation :investigation :perception :persuasion] 2)]
   :levels {6 {:modifiers [(mod5e/dependent-trait
                            {:name "Psionic Surge"
                             :page 6
                             :source ua-mystic-kw
                             :range opt5e/ft-30
                             :summary "use psionic focus to impose disadvantage on saves against your psionic disciplines or talents"})]}
            14 {:modifiers [(mod5e/action
                             {:page 6
                              :name "Spectral Form"
                              :range opt5e/ft-30
                              :duration opt5e/minutes-10
                              :summary "gain resistance to all damage; move at half speed; can pass through creatures and objects"})]}}
   :traits [{:name "Psionic Investigation"
             :level 3
             :page 6
             :source ua-mystic-kw
             :summary "learn basic facts about an object; embed a psionic sensor in an object"}]})

(def ua-mystic-order-of-the-avatar
  {:name "Order of the Avatar"
    :selections [(psionic-disciplines-selection 2 "Order of the Avatar" (fn [d] (= :avatar (:mystic-order d))))]
    :modifiers [(mod5e/armor-proficiency :medium)
                (mod5e/armor-proficiency :shields)]
    :levels {6 {:modifiers [(mod5e/dependent-trait
                             {:name "Avatar of Healing"
                              :page 6
                              :source ua-mystic-kw
                              :range opt5e/ft-30
                              :summary (str "allies regain additional " (max 0 (?ability-bonuses ::char5e/int)) " HPs from psionic disciplines")})]}
             14 {:modifiers [(mod5e/dependent-trait
                              {:page 6
                               :name "Avatar of Speed"
                               :range opt5e/ft-30
                               :summary "an ally can Dash as bonus action"})]}}
    :traits [{:name "Avatar of Battle"
              :level 3
              :page 5
              :source ua-mystic-kw
              :range opt5e/ft-30
              :summary "give an ally +2 initiative bonus"}]})

(def ua-mystic-order-of-the-immortal
  {:name "Order of the Immortal"
   :selections [(psionic-disciplines-selection 2 "Order of the Immortal" (fn [d] (= :immortal (:mystic-order d))))]
   :modifiers [(mods/cum-sum-mod ?hit-point-level-bonus 1)
               (mod5e/ac-bonus-fn
                (fn [armor shield]
                  (if (and (nil? shield)
                           (nil? armor))
                    (?ability-bonuses ::char5e/con)
                    0)))]
   :levels {3 {:modifiers [(mod5e/dependent-trait
                            {:name "Psionic Resilience"
                             :page 7
                             :source ua-mystic-kw
                             :summary (str "at start of your turns, gain " (max 0 (?ability-bonuses ::char5e/int)) " temp HPs if you have at least 1 HP.")})]}
            6 {:modifiers [(mod5e/reaction
                            {:name "Surge of Health"
                             :page 7
                             :source ua-mystic-kw
                             :summary "when you take damage, take half instead, but lose psychic focus until you finish as rest"})]}
            14 {:modifiers [(mod5e/dependent-trait
                             {:page 7
                              :name "Immortal Will"
                              :summary (str "at end of your turn, if you have 0 HPs spend 5 psi to regain " (+ (?class-level :mystic) (?ability-bonuses ::char5e/con)) " HPs")})]}}})

(def ua-mystic-order-of-the-nomad
  {:name "Order of the Nomad"
    :selections [(psionic-disciplines-selection 2 "Order of the Nomad" (fn [d] (= :nomad (:mystic-order d))))]
    :modifiers [(mod5e/trait-cfg
                 {:name "Breadth of Knowledge"
                  :page 7
                  :source ua-mystic-kw
                  :duration opt5e/long-rests-1
                  :frequency opt5e/long-rests-1
                  :summary "gain two skill or tool proficiencies"})]
    :levels {3 {:modifiers [(mod5e/reaction
                             {:name "Memory of One Thousand Steps"
                              :page 7
                              :source ua-mystic-kw
                              :summary "when hit by attack, teleport to an unoccupied space you occupied since start of your last turn"})]}
             6 {:modifiers [(mod5e/trait-cfg
                             {:name "Superior Teleportation"
                              :page 7
                              :source ua-mystic-kw
                              :summary "+10 psionic teleportation distance"})]}
             14 {:modifiers [(mod5e/trait-cfg
                              {:page 7
                               :name "Effortless Journey"
                               :summary "forfeit up to 30 ft of you movement to teleport that distance instead"})]}}})

(def ua-mystic-order-of-the-soul-knife
  {:name "Order of the Soul Knife"
   :modifiers [(mod5e/armor-proficiency :medium)
               (mod5e/weapon-proficiency :martial)
               (mod5e/bonus-action
                {:name "Soul Knife"
                 :page 8
                 :summary "create sould knives in each of your fists that deal 1d8 psychic damage; as a bonus action gain +2 bonus to AC until start of your next turn"})]
   :levels {3 {:modifiers [(mod5e/trait-cfg
                            {:name "Psionic Resilience"
                             :page 8
                             :source ua-mystic-kw
                             :duration opt5e/minutes-10
                             :summary "spend psi to gain bonus to attack and damage: 2 psi for +1, 5 psi for +2, 7 psi for +4"})]}
            6 {:modifiers [(mod5e/trait-cfg
                            {:name "Consumptive Knife"
                             :page 8
                             :source ua-mystic-kw
                             :summary "when you slay with your soul knife, retain 2 HPs"})]}
            14 {:modifiers [(mod5e/action
                             {:page 8
                              :name "Phantom Knife"
                              :summary "make a soul knife attack against AC 10 instead of target's actual AC"})]}}})

(def ua-mystic-order-of-the-wu-jen
  {:name "Order of the Wu Jen"
   :selections [(psionic-disciplines-selection 2 "Order of the Wu Jen" (fn [d] (= :wu-jen (:mystic-order d))))]
   :levels {3 {:modifiers [(mod5e/trait-cfg
                            {:name "Elemental Attunement"
                             :page 8
                             :source ua-mystic-kw
                             :summary "Spend 1 extra psi point on a discipline to resistance"})]}
            6 {:selections [(opt5e/spell-selection {:title "Wu Jen Spells"
                                                    :ref [:class :mystic :levels :level-1 :mystic-order :order-of-the-wu-jen :spells-known]
                                                    :spellcasting-ability ::char5e/int
                                                    :class-name "Order of the Wu Jen"
                                                    :num 3
                                                    :prepend-level? true
                                                    :spell-keys (concat (get-in sl/spell-lists [:wizard 1])
                                                                        (get-in sl/spell-lists [:wizard 2])
                                                                        (get-in sl/spell-lists [:wizard 3]))})]
               :modifiers [(mod5e/trait-cfg
                            {:name "Arcane Dabbler"
                             :page 8
                             :source ua-mystic-kw
                             :summary "learn 3 wizard spells you always have prepared; spend psi to gain spell slots: 2 psi for 1st level, 3 psi for 2nd, 5 psi for 3rd, 6 psi for 4th, 7 psi for 5th"})]}
            14 {:modifiers [(mod5e/reaction
                             {:page 8
                              :name "Elemental Mastery"
                              :summary "spend 2 psi to gain immunity to damage that you take that you are resistant to"})]}}})

(def ua-mystic-subclasses
  [ua-mystic-order-of-the-avatar
   ua-mystic-order-of-the-awakened
   ua-mystic-order-of-the-immortal
   ua-mystic-order-of-the-nomad
   ua-mystic-order-of-the-soul-knife
   ua-mystic-order-of-the-wu-jen])

(def ua-artificer-plugin
  {:name "Unearthed Arcana: Artificer"
   :key :ua-artificer
   :selections [(opt5e/class-selection
                 {:options [ua-artificer/artificer-option]})]})

(def ua-mystic-plugin
  {:name "Unearthed Arcana: Mystic"
   :key ua-mystic-kw
   :selections [(opt5e/class-selection
                 {:options [(opt5e/class-option
                             {:name "Mystic",
                              :hit-die 8,
                              :ability-increase-levels [4 8 12 16 19]
                              :profs {:armor {:light false}
                                      :weapon {:simple false} 
                                      :save {::char5e/int true ::char5e/wis true}
                                      :skill-options {:choose 2 :options {:arcana true :history true :insight true :medicine true :nature true :perception true :religion true}}}
                              :multiclass-prereqs [(t/option-prereq "Requires Intelligence 13"
                                                                    (fn [c]
                                                                      (let [abilities @(subscribe [::char5e/abilities nil c])]
                                                                        (>= (::char5e/int abilities) 13))))]
                              :equipment-choices [{:name "Equipment Pack"
                                                   :options {:scholars-pack 1
                                                             :explorers-pack 1}}]
                              :modifiers [opt5e/ua-al-illegal
                                          (mods/modifier ?psi-points (mod5e/level-val
                                                                     (?class-level :mystic)
                                                                     {2 6
                                                                      3 14
                                                                      4 17
                                                                      5 27
                                                                      6 32
                                                                      7 38
                                                                      8 44
                                                                      9 57
                                                                      10 64
                                                                      18 71
                                                                      :default 4}))
                                          (mods/modifier ?psi-limit (mod5e/level-val
                                                                    (?class-level :mystic)
                                                                    {3 3
                                                                     5 5
                                                                     7 6
                                                                     9 7
                                                                     :default 2}))
                                          (mod5e/dependent-trait
                                           {:name "Psi Points"
                                            :page 2
                                            :source ua-mystic-kw
                                            :summary (str "You have " ?psi-points " psi points")})]
                              :selections [ua-mystic/psionic-talents-selection
                                           (psionic-disciplines-selection 1) 
                                           (opt5e/new-starting-equipment-selection
                                            :fighter
                                            {:name "Armor"
                                             :options [(t/option-cfg
                                                        {:name "Chain Mail"
                                                         :modifiers [(mod5e/armor :chain-mail 1)]})
                                                       (t/option-cfg
                                                        {:name "Leather Armor, Longbow, 20 Arrows"
                                                         :options [(mod5e/armor :leather 1)
                                                                   (mod5e/weapon :longbow 1)
                                                                   (mod5e/equipment :arrow 20)]})]})
                                           (opt5e/new-starting-equipment-selection
                                            :fighter
                                            {:name "Weapons"
                                             :options [(t/option-cfg
                                                        {:name "Martial Weapon and Shield"
                                                         :selections [(opt5e/new-starting-equipment-selection
                                                                       :fighter
                                                                       {:name "Martial Weapon"
                                                                        :options (opt5e/martial-weapon-options 1)})]
                                                         :modifiers [(mod5e/armor :shield 1)]})
                                                       (t/option-cfg
                                                        {:name "Two Martial Weapons"
                                                         :selections [(opt5e/new-starting-equipment-selection
                                                                       :fighter
                                                                       {:name "Martial Weapons"
                                                                        :options (opt5e/martial-weapon-options 1)
                                                                        :min 2
                                                                        :max 2})]})]})
                                           (opt5e/new-starting-equipment-selection
                                            :fighter
                                            {:name "Additional Weapons"
                                             :options [(t/option-cfg
                                                        {:name "Light Crossbow and 20 Bolts"
                                                         :modifiers [(mod5e/weapon :crossbow-light 1)
                                                                     (mod5e/equipment :crossbow-bolt 20)]})
                                                       (t/option-cfg
                                                        {:name "Two Handaxes"
                                                         :modifiers [(mod5e/weapon :handaxe 2)]})]})]
                              :levels {2 {:modifiers [(mod5e/bonus-action
                                                       {:name "Mystical Recovery"
                                                        :page 4
                                                        :source ua-mystic-kw
                                                        :summary "Regain HPs equal to the number of psi points you spend on a psionic discipline"})
                                                      (mod5e/trait-cfg
                                                       {:name "Telepathy"
                                                        :page 4
                                                        :source ua-mystic-kw
                                                        :range opt5e/ft-120
                                                        :summary "telepathically speak to a creature"})]}
                                       3 {:selections [ua-mystic/psionic-talents-selection
                                                       (psionic-disciplines-selection 1)]}
                                       4 {:modifiers [(mod5e/trait-cfg
                                                       {:name "Strength of Mind"
                                                        :page 4
                                                        :source ua-mystic-kw
                                                        :summary "Replace your WIS save with another save"})]}
                                       5 {:selections [(psionic-disciplines-selection 1)]}
                                       7 {:selections [(psionic-disciplines-selection 1)]}
                                       8 {:modifiers [(mod5e/dependent-trait
                                                       {:name "Potent Psionics"
                                                        :page 4
                                                        :source ua-mystic-kw
                                                        :frequency opt5e/rounds-1
                                                        :summary (str "deal an extra "
                                                                      (if (>= (?class-level :mystic) 14)
                                                                        2
                                                                        1)
                                                                      "d8 psychic damage on a successful attack")})]}
                                       9 {:selections [(psionic-disciplines-selection 1)]}
                                       10 {:modifiers [(mod5e/dependent-trait
                                                       {:name "Consumptive Power"
                                                        :page 4
                                                        :source ua-mystic-kw
                                                        :frequency opt5e/long-rests-1
                                                        :summary (str "activate a psionic discipline with HPs instead of psi points (affects current and max HPs)")})]
                                           :selections [ua-mystic/psionic-talents-selection]}
                                       11 {:modifiers [(mod5e/action
                                                        {:name "Psionic Mastery"
                                                         :page 5
                                                         :source ua-mystic-kw
                                                         :frequency {:units :long-rest
                                                                     :amount (mod5e/level-val
                                                                              (?class-level :mystic)
                                                                              {13 2
                                                                               15 3
                                                                               17 4
                                                                               :default 1})}
                                                         :summary (str "gain "
                                                                       (if (>= (?class-level :mystic) 15)
                                                                         11
                                                                         9)
                                                                       " special psi points to spend on disciplines that require an action or bonus action")})]}
                                       12 {:selections [(psionic-disciplines-selection 1)]}
                                       15 {:selections [(psionic-disciplines-selection 1)]}
                                       17 {:selections [ua-mystic/psionic-talents-selection]}
                                       18 {:selections [(psionic-disciplines-selection 1)]}}
                              :subclass-level 1
                              :subclass-title "Mystic Order"
                              :subclasses ua-mystic-subclasses})]})]})

(def ua-waterborne-kw :ua-waterborne)

(defn mariner-class-option [nm kw level]
  (opt5e/class-option
   {:name nm
    :plugin? true
    :source ua-waterborne-kw
    :levels {level {:selections [(opt5e/fighting-style-selection-2
                                  kw
                                  0
                                  [(t/option-cfg
                                    {:name "Mariner"
                                     :modifiers [opt5e/ua-al-illegal
                                                 (mod5e/ac-bonus-fn
                                                  (fn [armor shield]
                                                    (if (and (nil? shield)
                                                             (not (= :heavy (:type armor))))
                                                      1
                                                      0)))
                                                 (mod5e/trait-cfg
                                                  {:name "Mariner Fighting Style"
                                                   :page 2
                                                   :source ua-waterborne-kw
                                                   :summary "while not wearing heavy armor, gain +1 AC bonus and you have swimming speed and climbing speed equal to your land speed"})]})])]}}}))

(def ua-waterborne-plugin
  {:name "Unearthed Arcana: Waterborne Adventures"
   :key ua-waterborne-kw
   :selections [(opt5e/class-selection
                 {:options [(mariner-class-option "Fighter" :fighter 1)
                            (mariner-class-option "Paladin" :paladin 2)
                            (mariner-class-option "Ranger" :ranger 2)]})
                (opt5e/race-selection
                 {:options [(opt5e/race-option
                             {:name "Minotaur (Krynn)"
                              :abilities {::char5e/str 1}
                              :selections [(opt5e/ability-increase-selection [::char5e/str ::char5e/int ::char5e/wis] 1 true)]
                              :size :medium
                              :speed 30
                              :weapon-proficiencies [:horns]
                              :tool-proficiencies [:navigators-tools :water-vehicles]
                              :languages ["Common"]
                              :modifiers [opt5e/ua-al-illegal
                                          (mod5e/attack
                                           {:name "Horns"
                                            :damage-die 10
                                            :damage-die-count 1
                                            :damage-type :piercing
                                            :page 2
                                            :source ua-waterborne-kw})
                                          (mod5e/bonus-action
                                           {:name "Goring Rush"
                                            :page 2
                                            :source ua-waterborne-kw
                                            :summary "When you take Dash action, make a melee attack with horns"})
                                          (mod5e/bonus-action
                                           {:name "Hammering Horns"
                                            :page 2
                                            :source ua-waterborne-kw
                                            :summary "When you use melee Attack action, shove a creature"})]
                              :traits [{:name "Labyrinthine Recall"
                                        :page 2
                                        :source ua-waterborne-kw
                                        :summary "perfect recall of paths you've traveled"}]})]})]})

(defn dragonmark-feat [nm ability-kw least lesser greater]
  (opt5e/feat-option
   {:name (str "Dragonmark: " nm)
    :page 5
    :source ua-eberron-kw
    :summary (str "You have the " nm " dragonmark")
    :modifiers (conj
                (map
                 #(dragonmark-spell-mod % ability-kw 1)
                 least)
                (dragonmark-spell-mod lesser ability-kw 5)
                (dragonmark-spell-mod greater ability-kw 9)
                opt5e/ua-al-illegal)}))

(def ua-eberron-plugin
  {:name "Unearthed Arcana: Eberron"
   :key ua-eberron-kw
   :selections [(opt5e/race-selection
                 {:options (map
                            (fn [race] (opt5e/race-option (assoc race :source ua-eberron-kw)))
                            [{:name "Changeling"
                              :abilities {::char5e/dex 1 ::char5e/cha 1}
                              :size :medium
                              :speed 30
                              :languages ["Common"]
                              :modifiers [opt5e/ua-al-illegal
                                          (mod5e/skill-proficiency :deception)
                                          (mod5e/action
                                           {:name "Shapechanger"
                                            :page 1
                                            :source ua-eberron-kw
                                            :summary "polymorph into a Medium humanoid you have seen"})]
                              :selections [(opt5e/language-selection opt5e/languages 2)]}
                             {:name "Shifter"
                              :abilities {::char5e/dex 1}
                              :size :medium
                              :speed 30
                              :darkvision 60
                              :languages ["Common" "Sylvan"]
                              :modifiers [opt5e/ua-al-illegal
                                          (mod5e/bonus-action
                                           {:name "Shifting"
                                            :page 2
                                            :source ua-eberron-kw
                                            :duration opt5e/minutes-1
                                            :summary (str "gain "
                                                          (max 1 (+ ?total-levels (?ability-bonuses ::char5e/con)))
                                                          " temp HPs"
                                                          (if ?shifting-feature
                                                            (str " and " ?shifting-feature)))})]
                              :subraces [{:name "Beasthide"
                                          :abilities {::char5e/con 1}
                                          :modifiers [(mods/modifier ?shifting-feature "a +1 AC bonus")]}
                                         {:name "Cliffwalk"
                                          :abilities {::char5e/dex 1}
                                          :modifiers [(mods/modifier ?shifting-feature "climbing speed of 30")]}
                                         {:name "Longstride"
                                          :abilities {::char5e/dex 1}
                                          :modifiers [(mods/modifier ?shifting-feature "Dash as bonus action")]}
                                         {:name "Longtooth"
                                          :abilities {::char5e/str 1}
                                          :modifiers [(mods/modifier ?shifting-feature "1d6 bite attack")]}
                                         {:name "Razorclaw"
                                          :abilities {::char5e/dex 1}
                                          :modifiers [(mods/modifier ?shifting-feature "unarmed strike as bonus action")]}
                                         {:name "Wildhunt"
                                          :abilities {::char5e/wis 1}
                                          :modifiers [(mods/modifier ?shifting-feature "advantage on WIS checks and saves")]}]}
                             {:name "Warforged"
                              :abilities {::char5e/str 1 ::char5e/con 1}
                              :size :medium
                              :speed 30
                              :modifiers [opt5e/ua-al-illegal
                                          (mod5e/natural-ac-bonus 1)
                                          (mod5e/immunity :disease)
                                          (mod5e/trait-cfg
                                           {:name "Living Construct"
                                            :page 3
                                            :source ua-eberron-kw
                                            :summmary "immune to disease; no need to eat or breathe; instead of sleeping, go inactive but alert for 4 hours"})]
                              :languages ["Common"]
                              :selections [(opt5e/language-selection opt5e/languages 1)]}])})
                (opt5e/class-selection
                 {:options [(opt5e/class-option
                             {:name "Wizard",
                              :plugin? true
                              :source ua-eberron-kw
                              :subclass-level 2
                              :subclass-title "Arcane Tradition"
                              :subclasses [{:name "Artificer"
                                            :modifiers [opt5e/ua-al-illegal]
                                            :levels {2 {:modifiers [(mod5e/trait-cfg
                                                                     {:name "Infuse Potions"
                                                                      :page 3
                                                                      :source ua-eberron-kw
                                                                      :summary "create a potion"})
                                                                    (mod5e/trait-cfg
                                                                     {:name "Infuse Scrolls"
                                                                      :page 4
                                                                      :source ua-eberron-kw
                                                                      :summary "create a scroll"})]}
                                                     6 {:modifiers [(mod5e/trait-cfg
                                                                     {:name "Infuse Weapons and Armor"
                                                                      :page 4
                                                                      :source ua-eberron-kw
                                                                      :duration {:units :hour
                                                                                 :amount 8}
                                                                      :summary "create magic weapon or armor"})]}
                                                     10 {:modifiers [(mod5e/trait-cfg
                                                                      {:name "Superior Artificer"
                                                                       :page 4
                                                                       :source ua-eberron-kw
                                                                       :summary "infuse 1 additional weapon or armor and 1 addition potion or scroll"})]}
                                                     14 {:modifiers [(mod5e/trait-cfg
                                                                      {:name "Master Artificer"
                                                                       :page 4
                                                                       :source ua-eberron-kw
                                                                       :summary "create magic items from tables A or B of DMG"})]}}}]})]})
                (opt5e/feat-selection-2
                 {:options [(dragonmark-feat "Detection" ::char5e/wis [:detect-magic :mage-hand] :detect-thoughts :clairvoyance)
                            (dragonmark-feat "Finding" ::char5e/wis [:identify :mage-hand] :locate-object :clairvoyance)
                            (dragonmark-feat "Handling" ::char5e/wis [:druidcraft :speak-with-animals] :beast-sense :conjure-animals)
                            (dragonmark-feat "Healing" ::char5e/wis [:cure-wounds :spare-the-dying] :lesser-restoration :revivify)
                            (dragonmark-feat "Hospitality" ::char5e/wis [:friends :unseen-servant] :rope-trick :leomunds-tiny-hut)
                            (dragonmark-feat "Making" ::char5e/int [:identify :mending] :magic-weapon :fabricate)
                            (dragonmark-feat "Passage" ::char5e/int [:expeditious-retreat :light] :misty-step :teleportation-circle)
                            (dragonmark-feat "Scribing" ::char5e/int [:comprehend-languages :message] :sending :tongues)
                            (dragonmark-feat "Sentinel" ::char5e/wis [:blade-ward :compelling-duel] :blur :protection-from-energy)
                            (dragonmark-feat "Shadow" ::char5e/cha [:dancing-lights :disguise-self] :darkness :nondetection)
                            (dragonmark-feat "Storm" ::char5e/int [:fog-cloud :shocking-grasp] :gust-of-wind :sleet-storm)
                            (dragonmark-feat "Warding" ::char5e/int [:alarm :resistance] :arcane-lock :magic-circle)]})]})

(def ua-plugins
  (map
   (fn [{:keys [name key] :as plugin}]
     (assoc plugin :help (ua-help name (opt5e/source-url key))))
   (sort-by
    :name
    [ua-artificer-plugin
     ua-mystic-plugin
     ua-trio-of-subclasses-plugin
     ua-revised-subclasses-plugin
     ua-waterborne-plugin
     ua-eberron-plugin])))

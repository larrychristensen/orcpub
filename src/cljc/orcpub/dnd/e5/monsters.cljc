(ns orcpub.dnd.e5.monsters
  (:require #?(:clj [clojure.spec.alpha :as spec])
            #?(:cljs [cljs.spec.alpha :as spec])
            [orcpub.common :as common :refer [name-to-kw]]
            [clojure.string :as s]))

(spec/def ::name (spec/and string? common/starts-with-letter?))
(spec/def ::key (spec/and keyword? common/keyword-starts-with-letter?))
(spec/def ::option-pack string?)
(spec/def ::die nat-int?)
(spec/def ::die-count nat-int?)
(spec/def ::modifier number?)
(spec/def ::hit-points (spec/keys :req-un [::die ::die-count]
                                  :opt-un [::modifier]))
(spec/def ::homebrew-monster (spec/keys :req-un [::name ::key ::option-pack ::hit-points]))

(defn monster-subheader
  ([size type subtypes alignment]
   (str (if size (common/safe-capitalize-kw size))
        " "
        (common/kw-to-name type)
        (if (seq subtypes)
          (str " (" (s/join ", " (map common/kw-to-name subtypes)) ")"))
        ", "
        alignment))
  ([{:keys [size type subtypes alignment]}]
   (monster-subheader size type subtypes alignment)))


(def monster-types
  [:aberration :beast :celestial :construct :dragon :elemental :fey :fiend :giant :humanoid :monstrosity :ooze :plant :swarm-of-tiny-beasts :undead])

(def monster-size-order
  [:tiny :small :medium :large :huge :gargantuan])

(def monster-sizes
  {:huge "Huge"
   :medium "Medium"
   :gargantuan "Gargantuan"
   :tiny "Tiny"
   :large "Large"
   :small "Small"})

(def challenge-ratings {0 10, (/ 1 8) 25, (/ 1 4) 50, (/ 1 2) 100, 1 200, 2 450, 3 700, 4 1100, 5 1800, 6 2300, 7 2900, 8 3900, 9 5000, 10 5900, 11 7200, 12 8400, 13 10000, 14 11500, 15 13000, 16 15000, 17 18000, 18 20000, 19 22000, 20 25000, 21 33000, 22 41000, 23 50000, 24 62000, 25 75000, 26 90000, 27 105000, 28 120000, 29 135000, 30 155000})

(def monsters-raw [
{
 :name "Aboleth"
 :size :large
 :type :aberration
 :alignment "lawful evil"
 :armor-class 17
 :armor-notes "natural armor"
 :hit-points {:mean 135 :die-count 18 :die 10 :modifier 36}
 :speed "10 ft., swim 40 ft."

 :str 21
 :dex 9
 :con 15
 :int 18
 :wis 15
 :cha 18

 :saving-throws {:con 6, :int 8, :wis 6}

 :skills {:history 12, :perception 10}
 :senses "darkvision 120 ft., passive Perception 20"
 :languages "Deep Speech, telepathy 120 ft."
 :challenge 10

 :traits [{:name "Amphibious" :description "The aboleth can breathe air and water."}
          {:name "Mucous Cloud" :description "While underwater, the aboleth is surrounded by transformative mucus. A creature that touches the aboleth or that hits it with a melee attack while within 5 feet of it must make a DC 14 Constitution saving throw. On a failure, the creature is diseased for 1d4 hours. The diseased creature can breathe only underwater."}
          {:name "Probing Telepathy" :description "If a creature communicates telepathically with the aboleth, the aboleth learns the creature's greatest desires if the aboleth can see the creature."}]

 :actions [{:name "Multiattack" :description "The aboleth makes three tentacle attacks."}
           {:name "Tentacle" :description "Melee Weapon Attack: +9 to hit, reach 10 ft., one target. Hit: 12 (2d6 + 5) bludgeoning damage. If the target is a creature, it must succeed on a DC 14 Constitution saving throw or become diseased. The disease has no effect for 1 minute and can be removed by any magic that cures disease. After 1 minute, the diseased creature's skin becomes translucent and slimy, the creature can't regain hit points unless it is underwater, and the disease can be removed only by heal or another disease-curing spell of 6th level or higher. When the creature is outside a body of water, it takes 6 (1d12) acid damage every 10 minutes unless moisture is applied to the skin before 10 minutes have passed."}
           {:name "Tail" :description "Melee Weapon Attack: +9 to hit, reach 10 ft. one target. Hit: 15 (3d6 + 5) bludgeoning damage."}
           {:name "Enslave" :notes "3/Day" :description "The aboleth targets one creature it can see within 30 feet of it. The target must succeed on a DC 14 Wisdom saving throw or be magically charmed by the aboleth until the aboleth dies or until it is on a different plane of existence from the target. The charmed target is under the aboleth's control and can't take reactions, and the aboleth and the target can communicate telepathically with each other over any distance.
Whenever the charmed target takes damage, the target can repeat the saving throw. On a success, the effect ends. No more than once every 24 hours, the target can also repeat the saving throw when it is at least 1 mile away from the aboleth."}]

 :legendary-actions {:description "The aboleth can take 3 legendary actions, choosing from the options below. Only one legendary action option can be used at a time and only at the end of another creature's turn. The aboleth regains spent legendary actions at the start of its turn."
                     :actions [{:name "Detect" :description "The aboleth makes a Wisdom (Perception) check."}
                               {:name "Tail Swipe" :description "The aboleth makes one tail attack."}
                               {:name "Psychic Drain" :notes "Costs 2 Actions" :description "One creature charmed by the aboleth takes 10 (3d6) psychic damage, and the aboleth regains hit points equal to the damage the creature takes."}]}
}{
  :name "Deva"
  :size :medium
  :type :celestial
  :alignment "lawful good"
  :armor-class 17
  :armor-notes "natural armor"
  :hit-points {:mean 136 :die-count 16 :die 8 :modifier 64}
  :speed "30 ft., fly 90 ft."

  :str 18
  :dex 18
  :con 18
  :int 17
  :wis 20
  :cha 20

  :saving-throws {:wis 9, :cha 9}

  :skills {:insight 9, :perception 9}
  :damage-resistances "radiant; bludgeoning, piercing, and slashing from nonmagical attacks"
  :condition-immunities "charmed, exhaustion, frightened"
  :senses "darkvision 120 ft., passive Perception 19"
  :languages "all, telepathy 120 ft."
  :challenge 10

  :traits [{:name "Angelic Weapons" :description "The deva's weapon attacks are magical. When the deva hits with any weapon, the weapon deals an extra 4d8 radiant damage (included in the attack)."}
           {:name "Innate Spellcasting" :description "The deva's spellcasting ability is Charisma (spell save DC 17). 
The deva can innately cast the following spells, requiring only verbal components:
At will: detect evil and good
1/day each: commune, raise dead"}
           {:name "Magic Resistance" :description "The deva has advantage on saving throws against spells and other magical effects."}]

  :actions [{:name "Multiattack" :description "The deva makes two melee attacks."}
            {:name "Mace" :description "Melee Weapon Attack: +8 to hit, reach 5 ft., one target. Hit: 7 (1d6 + 4) bludgeoning damage plus 18 (4d8) radiant damage."}
            {:name "Healing Touch" :notes "3/Day" :description "The deva touches another creature. The target magically regains 20 (4d8 + 2) hit points and is freed from any curse, disease, poison, blindness, or deafness."}
            {:name "Change Shape" :description "The deva magically polymorphs into a humanoid or beast that has a challenge rating equal to or less than its own, or back into its true form. It reverts to its true form if it dies. Any equipment it is wearing or carrying is absorbed or borne by the new form (the deva's choice).
In a new form, the deva retains its game statistics and ability to speak, but its AC, movement modes, Strength, Dexterity, and special senses are replaced by those of the new form, and it gains any statistics and capabilities (except class features, legendary actions, and lair actions) that the new form has but that it lacks."}]
}{
  :name "Planetar"
  :size :large
  :type :celestial
  :alignment "lawful good"
  :armor-class 19
  :armor-notes "natural armor"
  :hit-points {:mean 200 :die-count 16 :die 10 :modifier 112}
  :speed "40 ft., fly 120 ft."

  :str 24
  :dex 20
  :con 24
  :int 19
  :wis 22
  :cha 25

  :saving-throws {:con 12, :wis 11, :cha 12}

  :skills {:perception 11}
  :damage-resistances "radiant; bludgeoning, piercing, and slashing from nonmagical attacks"
  :condition-immunities "charmed, exhaustion, frightened"
  :senses "truesight 120 ft., passive Perception 21"
  :languages "all, telepathy 120 ft."
  :challenge 16

  :traits [{:name "Angelic Weapons" :description "The planetar's weapon attacks are magical. When the planetar hits with any weapon, the weapon deals an extra 5d8 radiant damage (included in the attack)."}
           {:name "Divine Awareness" :description "The planetar knows if it hears a lie."}
           {:name "Innate Spellcasting" :description "The planetar's spellcasting ability is Charisma (spell save DC 20). 
The planetar can innately cast the following spells, requiring no material components:
At will: detect evil and good, invisibility (self only) 3/day each: blade barrier, dispel evil and good, flame strike, raise dead
1/day each: commune, control weather, insect plague"}
           {:name "Magic Resistance" :description "The planetar has advantage on saving throws against spells and other magical effects."}]

  :actions [{:name "Multiattack" :description "The planetar makes two melee attacks."}
            {:name "Greatsword" :description "Melee Weapon Attack: +12 to hit, reach 5 ft., one target. Hit: 21 (4d6 + 7) slashing damage plus 22 (5d8) radiant damage."}
            {:name "Healing Touch" :notes "4/Day" :description "The planetar touches another creature. The target magically regains 30 (6d8 + 3) hit points and is freed from any curse, disease, poison, blindness, or deafness."}]
}{
  :name "Solar"
  :size :large
  :type :celestial
  :alignment "lawful good"
  :armor-class 21
  :armor-notes "natural armor"
  :hit-points {:mean 243 :die-count 18 :die 10 :modifier 144}
  :speed "50 ft., fly 150 ft."

  :str 26
  :dex 22
  :con 26
  :int 25
  :wis 25
  :cha 30

  :saving-throws {:int 14, :wis 14, :cha 17}

  :skills {:perception 14}
  :damage-resistances "radiant; bludgeoning, piercing, and slashing from nonmagical attacks"
  :damage-immunities "necrotic, poison"
  :condition-immunities "charmed, exhaustion, frightened, poisoned"
  :senses "truesight 120 ft., passive Perception 24"
  :languages "all, telepathy 120 ft."
  :challenge 21

  :traits [{:name "Angelic Weapons" :description "The solar's weapon attacks are magical. When the solar hits with any weapon, the weapon deals an extra 6d8 radiant damage (included in the attack)."}
           {:name "Divine Awareness" :description "The solar knows if it hears a lie."}
           {:name "Innate Spellcasting" :description "The solar's spellcasting ability is Charisma (spell save DC 25). 
It can innately cast the following spells, requiring no material components:
At will: detect evil and good, invisibility (self only) 
3/day each: blade barrier, dispel evil and good, resurrection
1/day each: commune, control weather"}
           {:name "Magic Resistance" :description "The solar has advantage on saving throws against spells and other magical effects."}]

  :actions [{:name "Multiattack" :description "The solar makes two greatsword attacks."}
            {:name "Greatsword" :description "Melee Weapon Attack: +15 to hit, reach 5 ft., one target. Hit: 22 (4d6 + 8) slashing damage plus 27 (6d8) radiant damage."}
            {:name "Slaying Longbow" :description "Ranged Weapon Attack: +13 to hit, range 150/600 ft., one target. Hit: 15 (2d8 + 6) piercing damage plus 27 (6d8) radiant damage. If the target is a creature that has 100 hit points or fewer, it must succeed on a DC 15 Constitution saving throw or die."}
            {:name "Flying Sword" :description "The solar releases its greatsword to hover magically in an unoccupied space within 5 feet of it. If the solar can see the sword, the solar can mentally command it as a bonus action to fly up to 50 feet and either make one attack against a target or return to the solar's hands. If the hovering sword is targeted by any effect, the solar is considered to be holding it. The hovering sword falls if the solar dies."}
            {:name "Healing Touch" :notes "4/Day" :description "The solar touches another creature. The target magically regains 40 (8d8 + 4) hit points and is freed from any curse, disease, poison, blindness, or deafness."}]

  :legendary-actions {:description "The solar can take 3 legendary actions, choosing from the options below. Only one legendary action option can be used at a time and only at the end of another creature's turn. The solar regains spent legendary actions at the start of its turn."
                      :actions [{:name "Teleport" :description "The solar magically teleports, along with any equipment it is wearing or carrying, up to 120 feet to an unoccupied space it can see."}
                                {:name "Searing Burst" :notes "Costs 2 Actions" :description "The solar emits magical, divine energy. Each creature of its choice in a 10-foot radius must make a DC 23 Dexterity saving throw, taking 14 (4d6) fire damage plus 14 (4d6) radiant damage on a failed save, or half as much damage on a successful one."}
                                {:name "Blinding Gaze" :notes "Costs 3 Actions" :description "The solar targets one creature it can see within 30 feet of it. If the target can see it, the target must succeed on a DC 15 Constitution saving throw or be blinded until magic such as the lesser restoration spell removes the blindness."}]}
}{
  :name "Animated Armor"
  :size :medium
  :type :construct
  :alignment "unaligned"
  :armor-class 18
  :armor-notes "natural armor"
  :hit-points {:mean 33 :die-count 6 :die 8 :modifier 6}
  :speed "25 ft."

  :str 14
  :dex 11
  :con 13
  :int 1
  :wis 3
  :cha 1

  :damage-immunities "poison, psychic"
  :condition-immunities "blinded, charmed, deafened, exhaustion, frightened, paralyzed, petrified, poisoned"
  :senses "blindsight 60 ft. (blind beyond this radius), passive Perception 6"
  :challenge 1

  :traits [{:name "Antimagic Susceptibility" :description "The armor is incapacitated while in the area of an antimagic field. If targeted by dispel magic, the armor must succeed on a Constitution saving throw against the caster's spell save DC or fall unconscious for 1 minute."}
           {:name "False Appearance" :description "While the armor remains motionless, it is indistinguishable from a normal suit of armor."}]

  :actions [{:name "Multiattack" :description "The armor makes two melee attacks."}
            {:name "Slam" :description "Melee Weapon Attack: +4 to hit, reach 5 ft., one target. Hit: 5 (1d6 + 2) bludgeoning damage."}]
}{
  :name "Flying Sword"
  :size :small
  :type :construct
  :alignment "unaligned"
  :armor-class 17
  :armor-notes "natural armor"
  :hit-points {:mean 17 :die-count 5 :die 6}
  :speed "0 ft., fly 50 ft. (hover)"

  :str 12
  :dex 15
  :con 11
  :int 1
  :wis 5
  :cha 1

  :saving-throws {:dex 4}
  :damage-immunities "poison, psychic"
  :condition-immunities "blinded, charmed, deafened, frightened, paralyzed, petrified, poisoned"
  :senses "blindsight 60 ft. (blind beyond this radius), passive Perception 7"
  :challenge (/ 1 4)

  :traits [{:name "Antimagic Susceptibility" :description "The sword is incapacitated while in the area of an antimagic field. If targeted by dispel magic, the sword must succeed on a Constitution saving throw against the caster's spell save DC or fall unconscious for 1 minute."}
           {:name "False Appearance" :description "While the sword remains motionless and isn't flying, it is indistinguishable from a normal sword."}]

  :actions [{:name "Longsword" :description "Melee Weapon Attack: +3 to hit, reach 5 ft., one target. Hit: 5 (1d8 + 1) slashing damage."}]
}{
  :name "Rug of Smothering"
  :size :large
  :type :construct
  :alignment "unaligned"
  :armor-class 12
  :hit-points {:mean 33 :die-count 6 :die 10}
  :speed "10 ft."

  :str 17
  :dex 14
  :con 10
  :int 1
  :wis 3
  :cha 1

  :damage-immunities "poison, psychic"
  :condition-immunities "blinded, charmed, deafened, frightened, paralyzed, petrified, poisoned"
  :senses "blindsight 60 ft. (blind beyond this radius), passive Perception 6"
  :challenge 2

  :traits [{:name "Antimagic Susceptibility" :description "The rug is incapacitated while in the area of an antimagic field. If targeted by dispel magic, the rug must succeed on a Constitution saving throw against the caster's spell save DC or fall unconscious for 1 minute."}
           {:name "Damage Transfer" :description "While it is grappling a creature, the rug takes only half the damage dealt to it, and the creature grappled by the rug takes the other half."}
           {:name "False Appearance" :description "While the rug remains motionless, it is indistinguishable from a normal rug."}]

  :actions [{:name "Smother" :description "Melee Weapon Attack: +5 to hit, reach 5 ft., one Medium or smaller creature. Hit: The creature is grappled (escape DC 13). Until this grapple ends, the target is restrained, blinded, and at risk of suffocating, and the rug can't smother another target. In addition, at the start of each of the target's turns, the target takes 10 (2d6 + 3) bludgeoning damage."}]
}{
  :name "Ankheg"
  :size :large
  :type :monstrosity
  :alignment "unaligned"
  :armor-class 14
  :armor-notes "natural armor, 11 while prone"
  :hit-points {:mean 39 :die-count 6 :die 10 :modifier 6}
  :speed "30 ft., burrow 10 ft."

  :str 17
  :dex 11
  :con 13
  :int 1
  :wis 13
  :cha 6

  :senses "darkvision 60 ft., tremorsense 60 ft., passive Perception 11"
  :challenge 2
  :actions [{:name "Bite" :description "Melee Weapon Attack: +5 to hit, reach 5 ft., one target. Hit: 10 (2d6 + 3) slashing damage plus 3 (1d6) acid damage. If the target is a Large or smaller creature, it is grappled (escape DC 13). Until this grapple ends, the ankheg can bite only the grappled creature and has advantage on attack rolls to do so."}
            {:name "Acid Spray" :notes "Recharge 6" :description "The ankheg spits acid in a line that is 30 feet long and 5 feet wide, provided that it has no creature grappled. Each creature in that line must make a DC 13 Dexterity saving throw, taking 10 (3d6) acid damage on a failed save, or half as much damage on a successful one."}]
}{
  :name "Azer"
  :size :medium
  :type :elemental
  :alignment "lawful neutral"
  :armor-class 17
  :armor-notes "(natural armor, shield)"
  :hit-points {:mean 39 :die-count 6 :die 8 :modifier 12}
  :speed "30 ft."

  :str 17
  :dex 12
  :con 15
  :int 12
  :wis 13
  :cha 10

  :saving-throws {:con 4}
  :damage-immunities "fire, poison"
  :condition-immunities "poisoned"
  :senses "passive Perception 11"
  :languages "Ignan"
  :challenge 2

  :traits [{:name "Heated Body" :description "A creature that touches the azer or hits it with a melee attack while within 5 feet of it takes 5 (1d10) fire damage."}
           {:name "Heated Weapons" :description "When the azer hits with a metal melee weapon, it deals an extra 3 (1d6) fire damage (included in the attack)."}
           {:name "Illumination" :description "The azer sheds bright light in a 10-foot radius and dim light for an additional 10 feet."}]

  :actions [{:name "Warhammer" :description "Melee Weapon Attack: +5 to hit, reach 5 ft., one target. Hit: 7 (1d8 + 3) bludgeoning damage, or 8 (1d10 + 3) bludgeoning damage if used with two hands to make a melee attack, plus 3 (1d6) fire damage."}]
}{
  :name "Basilisk"
  :size :medium
  :type :monstrosity
  :alignment "unaligned"
  :armor-class 15
  :armor-notes "natural armor"
  :hit-points {:mean 52 :die-count 8 :die 8 :modifier 16}
  :speed "20 ft."

  :str 16
  :dex 8
  :con 15
  :int 2
  :wis 8
  :cha 7

  :senses "darkvision 60 ft., passive Perception 9"
  :challenge 3

  :traits [{:name "Petrifying Gaze" :description "If a creature starts its turn within 30 feet of the basilisk and the two of them can see each other, the basilisk can force the creature to make a DC 12 Constitution saving throw if the basilisk isn't incapacitated. On a failed save, the creature magically begins to turn to stone and is restrained. It must repeat the saving throw at the end of its next turn. On a success, the effect ends. On a failure, the creature is petrified until freed by the greater restoration spell or other magic.
A creature that isn't surprised can avert its eyes to avoid the saving throw at the start of its turn. If it does so, it can't see the basilisk until the start of its next turn, when it can avert its eyes again. If it looks at the
basilisk in the meantime, it must immediately make the save.
If the basilisk sees its reflection within 30 feet of it in bright light, it mistakes itself for a rival and targets itself with its gaze."}]

  :actions [{:name "Bite" :description "Melee Weapon Attack: +5 to hit, reach 5 ft., one target. Hit: 10 (2d6 + 3) piercing damage plus 7 (2d6) poison damage."}]
}{
  :name "Behir"
  :size :huge
  :type :monstrosity
  :alignment "neutral evil"
  :armor-class 17
  :armor-notes "natural armor"
  :hit-points {:mean 168 :die-count 16 :die 12 :modifier 64}
  :speed "50 ft., climb 40 ft."

  :str 23
  :dex 16
  :con 18
  :int 7
  :wis 14
  :cha 12

  :skills {:perception 6, :stealth 7}
  :damage-immunities "lightning"
  :senses "darkvision 90 ft., passive Perception 16"
  :languages "Draconic"
  :challenge 11

  :actions [{:name "Multiattack" :description "The behir makes two attacks: one with its bite and one to constrict."}
            {:name "Bite" :description "Melee Weapon Attack: +10 to hit, reach 10 ft., one target. Hit: 22 (3d10 + 6) piercing damage."}
            {:name "Constrict" :description "Melee Weapon Attack: +10 to hit, reach 5 ft., one Large or smaller creature. Hit: 17 (2d10 + 6) bludgeoning damage plus 17 (2d10 + 6) slashing damage. The target is grappled (escape DC 16) if the behir isn't already constricting a creature, and the target is restrained until this grapple ends."}
            {:name "Lightning Breath" :notes "Recharge 5–6" :description "The behir exhales a line of lightning that is 20 feet long and 5 feet wide. Each creature in that line must make a DC 16 Dexterity saving throw, taking 66 (12d10) lightning damage on a failed save, or half as much damage on a successful one."}
            {:name "Swallow" :description "The behir makes one bite attack against a Medium or smaller target it is grappling. If the attack hits, the target is also swallowed, and the grapple ends. While swallowed, the target is blinded and restrained, While swallowed, the target is blinded and restrained, it has total cover against attacks and other effects outside the behir, and it takes 21 (6d6) acid damage at the start of each of the behir's turns. A behir can have only one creature swallowed at a time. 
If the behir takes 30 damage or more on a single turn from the swallowed creature, the behir must succeed on a DC 14 Constitution saving throw at the end of that turn or regurgitate the creature, which falls prone in a space within 10 feet of the behir. If the behir dies, a swallowed creature is no longer restrained by it and can escape from the corpse by using 15 feet of movement, exiting prone."}]
}{
  :name "Bugbear"
  :size :medium
  :type :humanoid
  :subtypes #{:goblinoid}
  :alignment "chaotic evil"
  :armor-class 16
  :armor-notes "(hide armor, shield)"
  :hit-points {:mean 27 :die-count 5 :die 8 :modifier 5}
  :speed "30 ft."

  :str 15
  :dex 14
  :con 13
  :int 8
  :wis 11
  :cha 9

  :skills {:stealth 6, :survival 2}
  :senses "darkvision 60 ft., passive Perception 10"
  :languages "Common, Goblin"
  :challenge 1

  :traits [{:name "Brute" :description "A melee weapon deals one extra die of its damage when the bugbear hits with it (included in the attack)."}
           {:name "Surprise Attack" :description "If the bugbear surprises a creature and hits it with an attack during the first round of combat, the target takes an extra 7 (2d6) damage from the attack."}]

  :actions [{:name "Morningstar" :description "Melee Weapon Attack: +4 to hit, reach 5 ft., one target. Hit: 11 (2d8 + 2) piercing damage."}
            {:name "Javelin" :description "Melee or Ranged Weapon Attack: +4 to hit, reach 5 ft. or range 30/120 ft., one target. Hit: 9 (2d6 +2) piercing damage in melee or 5 (1d6 + 2) piercing damage at range."}]
}{
  :name "Bulette"
  :size :large
  :type :monstrosity
  :alignment "unaligned"
  :armor-class 17
  :armor-notes "natural armor"
  :hit-points {:mean 94 :die-count 9 :die 10 :modifier 45}
  :speed "40 ft., burrow 40 ft."

  :str 19
  :dex 11
  :con 21
  :int 2
  :wis 10
  :cha 5

  :skills {:perception 6}
  :senses "darkvision 60 ft., tremorsense 60 ft., passive Perception 16"
  :challenge 5

  :traits [{:name "Standing Leap" :description "The bulette's long jump is up to 30 feet and its high jump is up to 15 feet, with or without a running start."}]

  :actions [{:name "Bite" :description "Melee Weapon Attack: +7 to hit, reach 5 ft., one target. Hit: 30 (4d12 + 4) piercing damage."}
            {:name "Deadly Leap" :description "If the bulette jumps at least 15 feet as part of its movement, it can then use this action to land on its feet in a space that contains one or more other creatures. Each of those creatures must succeed on a DC 16 Strength or Dexterity saving throw (target's choice) or be knocked prone and take 14 (3d6 + 4) bludgeoning damage plus 14 (3d6 + 4) slashing damage. On a successful save, the creature takes only half the damage, isn't knocked prone, and is pushed 5 feet out of the bulette's space into an unoccupied space of the creature's choice. If no unoccupied space is within range, the creature instead falls prone in the bulette's space."}]
}{
  :name "Centaur"
  :size :large
  :type :monstrosity
  :alignment "neutral good"
  :armor-class 12
  :hit-points {:mean 45 :die-count 6 :die 10 :modifier 12}
  :speed "50 ft."

  :str 18
  :dex 14
  :con 14
  :int 9
  :wis 13
  :cha 11

  :skills {:athletics 6, :perception 3, :survival 3}
  :senses "passive Perception 13"
  :languages "Elvish, Sylvan"
  :challenge 2

  :traits [{:name "Charge" :description "If the centaur moves at least 30 feet straight toward a target and then hits it with a pike attack on the same turn, the target takes an extra 10 (3d6) piercing damage."}]

  :actions [{:name "Multiattack" :description "The centaur makes two attacks: one with its pike and one with its hooves or two with its longbow."}
            {:name "Pike" :description "Melee Weapon Attack: +6 to hit, reach 10 ft., one target. Hit: 9 (1d10 + 4) piercing damage."}
            {:name "Hooves" :description "Melee Weapon Attack: +6 to hit, reach 5 ft., one target. Hit: 11 (2d6 + 4) bludgeoning damage."}
            {:name "Longbow" :description "Ranged Weapon Attack: +4 to hit, range 150/600 ft., one target. Hit: 6 (1d8 + 2) piercing damage."}]
}{
  :name "Chimera"
  :size :large
  :type :monstrosity
  :alignment "chaotic evil"
  :armor-class 14
  :armor-notes "natural armor"
  :hit-points {:mean 114 :die-count 12 :die 10 :modifier 48}
  :speed "30 ft., fly 60 ft."

  :str 19
  :dex 11
  :con 19
  :int 3
  :wis 14
  :cha 10

  :skills {:perception 8}
  :senses "darkvision 60 ft., passive Perception 18"
  :languages "understands Draconic but can't speak"
  :challenge 6

  :actions [{:name "Multiattack" :description "The chimera makes three attacks: one with its bite, one with its horns, and one with its claws. When its fire breath is available, it can use the breath in place of its bite or horns."}
            {:name "Bite" :description "Melee Weapon Attack: +7 to hit, reach 5 ft., one target. Hit: 11 (2d6 + 4) piercing damage."}
            {:name "Horns" :description "Melee Weapon Attack: +7 to hit, reach 5 ft., one target. Hit: 10 (1d12 + 4) bludgeoning damage."}
            {:name "Claws" :description "Melee Weapon Attack: +7 to hit, reach 5 ft., one target. Hit: 11 (2d6 + 4) slashing damage."}
            {:name "Fire Breath" :notes "Recharge 5–6" :description "The dragon head exhales fire in a 15-foot cone. Each creature in that area must make a DC 15 Dexterity saving throw, taking 31 (7d8) fire damage on a failed save, or half as much damage on a successful one."}]
}{
  :name "Chuul"
  :size :large
  :type :aberration
  :alignment "chaotic evil"
  :armor-class 16
  :armor-notes "natural armor"
  :hit-points {:mean 93 :die-count 11 :die 10 :modifier 33}
  :speed "30 ft., swim 30 ft."

  :str 19
  :dex 10
  :con 16
  :int 5
  :wis 11
  :cha 5

  :skills {:perception 4}
  :damage-immunities "poison"
  :condition-immunities "poisoned"
  :senses "darkvision 60 ft., passive Perception 14"
  :languages "understands Deep Speech but can't speak"
  :challenge 4

  :traits [{:name "Amphibious" :description "The chuul can breathe air and water."}
           {:name "Sense Magic" :description "The chuul senses magic within 120 feet of it at will. This trait otherwise works like the detect magic spell but isn't itself magical."}]

  :actions [{:name "Multiattack" :description "The chuul makes two pincer attacks. If the chuul is grappling a creature, the chuul can also use its tentacles once."}
            {:name "Pincer" :description "Melee Weapon Attack: +6 to hit, reach 10 ft., one target. Hit: 11 (2d6 + 4) bludgeoning damage. The target is grappled (escape DC 14) if it is a Large or smaller creature and the chuul doesn't have two other creatures grappled."}
            {:name "Tentacles" :description "One creature grappled by the chuul must succeed on a DC 13 Constitution saving throw or be poisoned for 1 minute. Until this poison ends, the target is paralyzed. The target can repeat the saving throw at the end of each of its turns, ending the effect on itself on a success."}]
}{
  :name "Cloaker"
  :size :large
  :type :aberration
  :alignment "chaotic neutral"
  :armor-class 14
  :armor-notes "natural armor"
  :hit-points {:mean 78 :die-count 12 :die 10 :modifier 12}
  :speed "10 ft., fly 40 ft."

  :str 17
  :dex 15
  :con 12
  :int 13
  :wis 12
  :cha 14

  :skills {:stealth 5}
  :senses "darkvision 60 ft., passive Perception 11"
  :languages "Deep Speech, Undercommon"
  :challenge 8

  :traits [{:name "Damage Transfer" :description "While attached to a creature, the cloaker takes only half the damage dealt to it (rounded down), and that creature takes the other half."}
           {:name "False Appearance" :description "While the cloaker remains motionless without its underside exposed, it is indistinguishable from a dark leather cloak."}
           {:name "Light Sensitivity" :description "While in bright light, the cloaker has disadvantage on attack rolls and Wisdom (Perception) checks that rely on sight."}]
  :actions [{:name "Multiattack" :description "The cloaker makes two attacks: one with its bite and one with its tail."}
            {:name "Bite" :description "Melee Weapon Attack: +6 to hit, reach 5 ft., one creature. Hit: 10 (2d6 + 3) piercing damage, and if the target is Large or smaller, the cloaker attaches to it. If the cloaker has advantage against the target, the cloaker attaches to the target's head, and the target is blinded and unable to breathe while the cloaker is attached. While attached, the cloaker can make this attack only against the target and has advantage on the attack roll. The cloaker can detach itself by spending 5 feet of its movement. A creature, including the target, can take its action to detach the cloaker by succeeding on a DC 16 Strength check."}
            {:name "Tail" :description "Melee Weapon Attack: +6 to hit, reach 10 ft., one creature. Hit: 7 (1d8 + 3) slashing damage."}
            {:name "Moan" :description "Each creature within 60 feet of the cloaker that can hear its moan and that isn't an aberration must succeed on a DC 13 Wisdom saving throw or become frightened until the end of the cloaker's next turn. If a creature's saving throw is successful, the creature is immune to the cloaker's moan for the next 24 hours"}
            {:name "Phantasms" :notes "Recharges after a Short or Long Rest" :description "The cloaker magically creates three illusory duplicates of itself if it isn't in bright light. The duplicates move with it and mimic its actions, shifting position so as to make it impossible to track which cloaker is the real one. If the cloaker is ever in an area of bright light, the duplicates disappear.
Whenever any creature targets the cloaker with an attack or a harmful spell while a duplicate remains, that creature rolls randomly to determine whether it targets the cloaker or one of the duplicates. A creature is unaffected by this magical effect if it can't see or if it relies on senses other than sight.
A duplicate has the cloaker's AC and uses its saving throws. If an attack hits a duplicate, or if a duplicate fails a saving throw against an effect that deals damage, the duplicate disappears."}]
}{
  :name "Cockatrice"
  :size :small
  :type :monstrosity
  :alignment "unaligned"
  :armor-class 11
  :hit-points {:mean 27 :die-count 6 :die 6 :modifier 6}
  :speed "20 ft., fly 40 ft."

  :str 6
  :dex 12
  :con 12
  :int 2
  :wis 13
  :cha 5

  :senses "darkvision 60 ft., passive Perception 11"
  :challenge (/ 1 2)

  :actions [{:name "Bite" :description "Melee Weapon Attack: +3 to hit, reach 5 ft., one creature. Hit: 3 (1d4 + 1) piercing damage, and the target must succeed on a DC 11 Constitution saving throw against being magically petrified. On a failed save, the creature begins to turn to stone and is restrained. It must repeat the saving throw at the end of its next turn. On a success, the effect ends. On a failure, the creature is petrified for 24 hours."}]
}{
  :name "Couatl"
  :size :medium
  :type :celestial
  :alignment "lawful good"
  :armor-class 19
  :armor-notes "natural armor"
  :hit-points {:mean 97 :die-count 13 :die 8 :modifier 39}
  :speed "30 ft., fly 90 ft."

  :str 16
  :dex 20
  :con 17
  :int 18
  :wis 20
  :cha 18

  :saving-throws {:con 5, :wis 7, :cha 6}
  :damage-resistances "radiant"
  :damage-immunities "psychic; bludgeoning, piercing, and slashing from nonmagical attacks"
  :senses "truesight 120 ft., passive Perception 15"
  :languages "all, telepathy 120 ft."
  :challenge 4

  :traits [{:name "Innate Spellcasting" :description "The couatl's spellcasting ability is Charisma (spell save DC 14). 
It can innately cast the following spells, requiring only verbal components:
At will: detect evil and good, detect magic, detect thoughts
3/day each: bless, create food and water, cure wounds, lesser restoration, protection from poison, sanctuary, shield
1/day each: dream, greater restoration, scrying"}
           {:name "Magic Weapons" :description "The couatl's weapon attacks are magical"}
           {:name "Shielded Mind" :description "The couatl is immune to scrying and to any effect that would sense its emotions, read its thoughts, or detect its location."}]

  :actions [{:name "Bite" :description "Melee Weapon Attack: +8 to hit, reach 5 ft., one creature. Hit: 8 (1d6 + 5) piercing damage, and the target must succeed on a DC 13 Constitution saving throw or be poisoned for 24 hours. Until this poison ends, the target is unconscious. Another creature can use an action to shake the target awake."}
            {:name "Constrict" :description "Melee Weapon Attack: +6 to hit, reach 10 ft., one Medium or smaller creature. Hit: 10 (2d6 + 3) bludgeoning damage, and the target is grappled (escape DC 15). Until this grapple ends, the target is restrained, and the couatl can't constrict another target."}
            {:name "Change Shape" :description "The couatl magically polymorphs into a humanoid or beast that has a challenge rating equal to or less than its own, or back into its true form. It reverts to its true form if it dies. Any equipment it is wearing or carrying is absorbed or borne by the new form (the couatl's choice).
In a new form, the couatl retains its game statistics and ability to speak, but its AC, movement modes, Strength, Dexterity, and other actions are replaced by those of the new form, and it gains any statistics and capabilities (except class features, legendary actions, and lair actions) that the new form has but that it lacks. If the new form has a bite attack, the couatl can use its bite in that form."}]
}{
  :name "Darkmantle"
  :size :small
  :type :monstrosity
  :alignment "unaligned"
  :armor-class 11
  :hit-points {:mean 22 :die-count 5 :die 6 :modifier 5}
  :speed "10 ft., fly 30 ft."

  :str 16
  :dex 12
  :con 13
  :int 2
  :wis 10
  :cha 5

  :skills {:stealth 3}
  :senses "blindsight 60 ft., passive Perception 10"
  :challenge (/ 1 2)

  :traits [{:name "Echolocation" :description "The darkmantle can't use its blindsight while deafened."}
           {:name "False Appearance" :description "While the darkmantle remains motionless, it is indistinguishable from a cave formation such as a stalactite or stalagmite."}]

  :actions [{:name "Crush" :description "Melee Weapon Attack: +5 to hit, reach 5 ft., one creature. Hit: 6 (1d6 + 3) bludgeoning damage, and the darkmantle attaches to the target. If the target is Medium or smaller and the darkmantle has advantage on the attack roll, it attaches by engulfing the target's head, and the target is also blinded and unable to breathe while the darkmantle is attached in this way.
While attached to the target, the darkmantle can attack no other creature except the target but has advantage on its attack rolls. The darkmantle's speed also becomes 0, it can't benefit from any bonus to its speed, and it moves with the target.
A creature can detach the darkmantle by making a successful DC 13 Strength check as an action. On its turn, the darkmantle can detach itself from the target by using 5 feet of movement."}
            {:name "Darkness Aura" :notes "1/Day" :description "A 15-foot radius of magical darkness extends out from the darkmantle, moves with it, and spreads around corners. The darkness lasts as long as the darkmantle maintains concentration, up to 10 minutes (as if concentrating on a spell
Darkvision can't penetrate this darkness, and no natural light can illuminate it. If any of the darkness overlaps with an area of light created by a spell of 2nd level or lower, the spell creating the light is dispelled."}]
}{
  :name "Balor"
  :size :huge
  :type :fiend
  :subtypes #{:demon}
  :alignment "chaotic evil"
  :armor-class 19
  :armor-notes "natural armor"
  :hit-points {:mean 262 :die-count 21 :die 12 :modifier 126}
  :speed "40 ft., fly 80 ft."

  :str 26
  :dex 15
  :con 22
  :int 20
  :wis 16
  :cha 22

  :saving-throws {:str 14, :con 12, :wis 9, :cha 12}
  :damage-resistances "cold, lightning; bludgeoning, piercing, and slashing from nonmagical attacks"
  :damage-immunities "fire, poison"
  :condition-immunities "poisoned"
  :senses "truesight 120 ft., passive Perception 13"
  :languages "Abyssal, telepathy 120 ft."
  :challenge 19

  :traits [{:name "Death Throes" :description "When the balor dies, it explodes, and each creature within 30 feet of it must make a DC 20 Dexterity saving throw, taking 70 (20d6) fire damage on a failed save, or half as much damage on a successful one. The explosion ignites flammable objects in that area that aren't being worn or carried, and it destroys the balor's weapons."}
           {:name "Fire Aura" :description "At the start of each of the balor's turns, each creature within 5 feet of it takes 10 (3d6) fire damage, and flammable objects in the aura that aren't being worn or carried ignite. A creature that touches the balor or hits it with a melee attack while within 5 feet of it takes 10 (3d6) fire damage."}
           {:name "Magic Resistance" :description "The balor has advantage on saving throws against spells and other magical effects."}
           {:name "Magic Weapons" :description "The balor's weapon attacks are magical."}]

  :actions [{:name "Multiattack" :description "The balor makes two attacks: one with its longsword and one with its whip."}
            {:name "Longsword" :description "Melee Weapon Attack: +14 to hit, reach 10 ft., one target. Hit: 21 (3d8 + 8) slashing damage plus 13 (3d8) lightning damage. If the balor scores a critical hit, it rolls damage dice three times, instead of twice."}
            {:name "Whip" :description "Melee Weapon Attack: +14 to hit, reach 30 ft., one target. Hit: 15 (2d6 + 8) slashing damage plus 10 (3d6) fire damage, and the target must succeed on a DC 20 Strength saving throw or be pulled up to 25 feet toward the balor."}
            {:name "Teleport" :description "The balor magically teleports, along with any equipment it is wearing or carrying, up to 120 feet to an unoccupied space it can see."}]
}{
  :name "Dretch"
  :size :small
  :type :fiend
  :subtypes #{:demon}
  :alignment "chaotic evil"
  :armor-class 11
  :armor-notes "natural armor"
  :hit-points {:mean 18 :die-count 4 :die 6 :modifier 4}
  :speed "20 ft."

  :str 11
  :dex 11
  :con 12
  :int 5
  :wis 8
  :cha 3

  :damage-resistances "cold, fire, lightning"
  :damage-immunities "poison"
  :condition-immunities "poisoned"
  :senses "darkvision 60 ft., passive Perception 9"
  :languages "Abyssal, telepathy 60 ft. (works only with creatures that understand Abyssal)"
  :challenge (/ 1 4)

  :actions [{:name "Multiattack" :description "The dretch makes two attacks: one with its bite and one with its claws."}
            {:name "Bite" :description "Melee Weapon Attack: +2 to hit, reach 5 ft., one target. Hit: 3 (1d6) piercing damage."}
            {:name "Claws" :description "Melee Weapon Attack: +2 to hit, reach 5 ft., one target. Hit: 5 (2d4) slashing damage."}
            {:name "Fetid Cloud" :notes "1/Day" :description "A 10-foot radius of disgusting green gas extends out from the dretch. The gas spreads around corners, and its area is lightly obscured. It lasts for 1 minute or until a strong wind disperses it. Any creature that starts its turn in that area must succeed on a DC 11 Constitution saving throw or be poisoned until the start of its next turn. While poisoned in this way, the target can take either an action or a bonus action on its turn, not both, and can't take reactions."}]
}{
  :name "Glabrezu"
  :size :large
  :type :fiend
  :subtypes #{:demon}
  :alignment "chaotic evil"
  :armor-class 17
  :armor-notes "natural armor"
  :hit-points {:mean 157 :die-count 15 :die 10 :modifier 75}
  :speed "40 ft."

  :str 20
  :dex 15
  :con 21
  :int 19
  :wis 17
  :cha 16

  :saving-throws {:str 9, :con 9, :wis 7, :cha 7}
  :damage-resistances "cold, fire, lightning; bludgeoning, piercing, and slashing from nonmagical attacks"
  :damage-immunities "poison"
  :condition-immunities "poisoned"
  :senses "truesight 120 ft., passive Perception 13"
  :languages "Abyssal, telepathy 120 ft."
  :challenge 9

  :traits [{:name "Innate Spellcasting" :description "The glabrezu's spellcasting ability is Intelligence (spell save DC 16). 
The glabrezu can innately cast the following spells, requiring no material components:
At will: darkness, detect magic, dispel magic 1/day each: confusion, fly, power word stun"}
           {:name "Magic Resistance" :description "The glabrezu has advantage on saving throws against spells and other magical effects."}]

  :actions [{:name "Multiattack" :description "The glabrezu makes four attacks: two with its pincers and two with its fists. Alternatively, it makes two attacks with its pincers and casts one spell."}
            {:name "Pincer" :description "Melee Weapon Attack: +9 to hit, reach 10 ft., one target. Hit: 16 (2d10 + 5) bludgeoning damage. If the target is a Medium or smaller creature, it is grappled (escape DC 15). The glabrezu has two pincers, each of which can grapple only one target."}
            {:name "Fist" :description "Melee Weapon Attack: +9 to hit, reach 5 ft., one target. Hit: 7 (2d4 + 2) bludgeoning damage."}]
}{
  :name "Hezrou"
  :size :large
  :type :fiend
  :subtypes #{:demon}
  :alignment "chaotic evil"
  :armor-class 16
  :armor-notes "natural armor"
  :hit-points {:mean 136 :die-count 13 :die 10 :modifier 65}
  :speed "30 ft."

  :str 19
  :dex 17
  :con 20
  :int 5
  :wis 12
  :cha 13

  :saving-throws {:str 7, :con 8, :wis 4}
  :damage-resistances "cold, fire, lightning; bludgeoning, piercing, and slashing from nonmagical attacks"
  :damage-immunities "poison"
  :condition-immunities "poisoned"
  :senses "darkvision 120 ft., passive Perception 11"
  :languages "Abyssal, telepathy 120 ft."
  :challenge 8

  :traits [{:name "Magic Resistance" :description "The hezrou has advantage on saving throws against spells and other magical effects."}
           {:name "Stench" :description "Any creature that starts its turn within 10 feet of the hezrou must succeed on a DC 14 Constitution saving throw or be poisoned until the start of its next turn. On a successful saving throw, the creature is immune to the hezrou's stench for 24 hours."}]

  :actions [{:name "Multiattack" :description "The hezrou makes three attacks: one with its bite and two with its claws."}
            {:name "Bite" :description "Melee Weapon Attack: +7 to hit, reach 5 ft., one target. Hit: 15 (2d10 + 4) piercing damage."}
            {:name "Claw" :description "Melee Weapon Attack: +7 to hit, reach 5 ft., one target. Hit: 11 (2d6 + 4) slashing damage."}]
}{
  :name "Marilith"
  :size :large
  :type :fiend
  :subtypes #{:demon}
  :alignment "chaotic evil"
  :armor-class 18
  :armor-notes "natural armor"
  :hit-points {:mean 189 :die-count 18 :die 10 :modifier 90}
  :speed "40 ft."

  :str 18
  :dex 20
  :con 20
  :int 18
  :wis 16
  :cha 20

  :saving-throws {:str 9, :con 10, :wis 8, :cha 10}
  :damage-resistances "cold, fire, lightning; bludgeoning, piercing, and slashing from nonmagical attacks"
  :damage-immunities "poison"
  :condition-immunities "poisoned"
  :senses "truesight 120 ft., passive Perception 13"
  :languages "Abyssal, telepathy 120 ft."
  :challenge 16

  :traits [{:name "Magic Resistance" :description "The marilith has advantage on saving throws against spells and other magical effects."}
           {:name "Magic Weapons" :description "The marilith's weapon attacks are magical."}
           {:name "Reactive" :description "The marilith can take one reaction on every turn in a combat."}]

  :actions [{:name "Multiattack" :description "The marilith makes seven attacks: six with its longswords and one with its tail."}
            {:name "Longsword" :description "Melee Weapon Attack: +9 to hit, reach 5 ft., one target. Hit: 13 (2d8 + 4) slashing damage."}
            {:name "Tail" :description "Melee Weapon Attack: +9 to hit, reach 10 ft., one creature. Hit: 15 (2d10 + 4) bludgeoning damage. If the target is Medium or smaller, it is grappled (escape DC 19). Until this grapple ends, the target is restrained, the marilith can automatically hit the target with its tail, and the marilith can't make tail attacks against other targets."}
            {:name "Teleport" :description "The marilith magically teleports, along with any equipment it is wearing or carrying, up to 120 feet to an unoccupied space it can see."}]

  :reactions [{:name "Parry" :description "The marilith adds 5 to its AC against one melee attack that would hit it. To do so, the marilith must see the attacker and be wielding a melee weapon."}]
}{
  :name "Nalfeshnee"
  :size :large
  :type :fiend
  :subtypes #{:demon}
  :alignment "chaotic evil"
  :armor-class 18
  :armor-notes "natural armor"
  :hit-points {:mean 184 :die-count 16 :die 10 :modifier 96}
  :speed "20 ft., fly 30 ft."

  :str 21
  :dex 10
  :con 22
  :int 19
  :wis 12
  :cha 15

  :saving-throws {:con 11, :int 9, :wis 6, :cha 7}
  :damage-resistances "cold, fire, lightning; bludgeoning, piercing, and slashing from nonmagical attacks"
  :damage-immunities "poison"
  :condition-immunities "poisoned"
  :senses "truesight 120 ft., passive Perception 11"
  :languages "Abyssal, telepathy 120 ft."
  :challenge 13

  :traits [{:name "Magic Resistance" :description "The nalfeshnee has advantage on saving throws against spells and other magical effects."}]

  :actions [{:name "Multiattack" :description "The nalfeshnee uses Horror Nimbus if it can. It then makes three attacks: one with its bite and two with its claws."}
            {:name "Bite" :description "Melee Weapon Attack: +10 to hit, reach 5 ft., one target. Hit: 32 (5d10 + 5) piercing damage."}
            {:name "Claw" :description "Melee Weapon Attack: +10 to hit, reach 10 ft., one target. Hit: 15 (3d6 + 5) slashing damage."}
            {:name "Horror Nimbus" :notes "Recharge 5–6" :description "The nalfeshnee magically emits scintillating, multicolored light. Each creature within 15 feet of the nalfeshnee that can see the light must succeed on a DC 15 Wisdom saving throw or be frightened for 1 minute. A creature can repeat the saving throw at the end of each of its turns, ending the effect on itself on a success. If a creature's saving throw is successful or the effect ends for it, the creature is immune to the nalfeshnee's Horror Nimbus for the next 24 hours."}
            {:name "Teleport" :description "The nalfeshnee magically teleports, along with any equipment it is wearing or carrying, up to 120 feet to an unoccupied space it can see."}]
}{
  :name "Quasit"
  :size :tiny
  :type :fiend
  :subtypes #{:demon, :shapechanger}
  :alignment "chaotic evil"
  :armor-class 13
  :hit-points {:mean 7 :die-count 3 :die 4}
  :speed "40 ft."

  :str 5
  :dex 17
  :con 10
  :int 7
  :wis 10
  :cha 10

  :skills {:stealth 5}

  :damage-resistances "cold, fire, lightning; bludgeoning, piercing, and slashing from nonmagical attacks"
  :damage-immunities "poison"
  :condition-immunities "poisoned"
  :senses "darkvision 120 ft., passive Perception 10"
  :languages "Abyssal, Common"
  :challenge 1

  :traits [{:name "Shapechanger" :description "The quasit can use its action to polymorph into a beast form that resembles a bat (speed 10 ft. fly 40 ft.), a centipede (40 ft., climb 40 ft.), or a toad (40 ft., swim 40 ft.), or back into its true form. Its statistics are the same in each form, except for the speed changes noted. Any equipment it is wearing or carrying isn't transformed. It reverts to its true form if it dies."}
           {:name "Magic Resistance" :description "The quasit has advantage on saving throws against spells and other magical effects."}]

  :actions [{:name "Claws" :notes "Bite in Beast Form" :description "Melee Weapon Attack: +4 to hit, reach 5 ft., one target. Hit: 5 (1d4 + 3) piercing damage, and the target must succeed on a DC 10 Constitution saving throw or take 5 (2d4) poison damage and become poisoned for 1 minute. The target can repeat the saving throw at the end of each of its turns, ending the effect on itself on a success."}
            {:name "Scare" :notes "1/Day" :description "One creature of the quasit's choice within 20 feet of it must succeed on a DC 10 Wisdom saving throw or be frightened for 1 minute. The target can repeat the saving throw at the end of each of its turns, with disadvantage if the quasit is within line of sight, ending the effect on itself on a success."}
            {:name "Invisibility" :description "The quasit magically turns invisible until it attacks or uses Scare, or until its concentration ends (as if concentrating on a spell). Any equipment the quasit wears or carries is invisible with it."}]
}{
  :name "Vrock"
  :size :large
  :type :fiend
  :subtypes #{:demon}
  :alignment "chaotic evil"
  :armor-class 15
  :armor-notes "natural armor"
  :hit-points {:mean 104 :die-count 11 :die 10 :modifier 44}
  :speed "40 ft., fly 60 ft."

  :str 17
  :dex 15
  :con 18
  :int 8
  :wis 13
  :cha 8

  :saving-throws {:dex 5, :wis 4, :cha 2}
  :damage-resistances "cold, fire, lightning; bludgeoning, piercing, and slashing from nonmagical attacks"
  :damage-immunities "poison"
  :condition-immunities "poisoned"
  :senses "darkvision 120 ft., passive Perception 11"
  :languages "Abyssal, telepathy 120 ft."
  :challenge 6

  :traits [{:name "Magic Resistance" :description "The vrock has advantage on saving throws against spells and other magical effects."}]

  :actions [{:name "Multiattack" :description "The vrock makes two attacks: one with its beak and one with its talons."}
            {:name "Beak" :description "Melee Weapon Attack: +6 to hit, reach 5 ft., one target. Hit: 10 (2d6 + 3) piercing damage."}
            {:name "Talons" :description "Melee Weapon Attack: +6 to hit, reach 5 ft., one target. Hit: 14 (2d10 + 3) slashing damage."}
            {:name "Spores" :notes "Recharge 6" :description "A 15-foot-radius cloud of toxic spores extends out from the vrock. The spores spread around corners. Each creature in that area must succeed on a DC 14 Constitution saving throw or become poisoned. While poisoned in this way, a target takes 5 (1d10) poison damage at the start of each of its turns. A target can repeat the saving throw at the end of each of its turns, ending the effect on itself on a success. Emptying a vial of holy water on the target also ends the effect on it."}
            {:name "Stunning Screech" :notes "1/Day" :description "The vrock emits a horrific screech. Each creature within 20 feet of it that can hear it and that isn't a demon must succeed on a DC 14 Constitution saving throw or be stunned until the end of the vrock's next turn."}]
}{
  :name "Barbed Devil"
  :size :medium
  :type :fiend
  :subtypes #{:devil}
  :alignment "lawful evil"
  :armor-class 15
  :armor-notes "natural armor"
  :hit-points {:mean 110 :die-count 13 :die 8 :modifier 52}
  :speed "30 ft."

  :str 16
  :dex 17
  :con 18
  :int 12
  :wis 14
  :cha 14
                                                                                                                                                                                                                                                                                              
  :saving-throws {:str 6, :con 7, :wis 5, :cha 5}
  :skills {:deception 5, :insight 5, :perception 8}
  :damage-resistances "cold; bludgeoning, piercing, and slashing from nonmagical attacks that aren't silvered"
  :damage-immunities "fire, poison"
  :condition-immunities "poisoned"
  :senses "darkvision 120 ft., passive Perception 18"
  :languages "Infernal, telepathy 120 ft."
  :challenge 5

  :traits [{:name "Barbed Hide" :description "At the start of each of its turns, the barbed devil deals 5 (1d10) piercing damage to any creature grappling it.
Devil's Sight. Magical darkness doesn't impede the devil's darkvision."}
           {:name "Magic Resistance" :description "The devil has advantage on saving throws against spells and other magical effects."}]

  :actions [{:name "Multiattack" :description "The devil makes three melee attacks: one with its tail and two with its claws. Alternatively, it can use Hurl Flame twice."}
            {:name "Claw" :description "Melee Weapon Attack: +6 to hit, reach 5 ft., one target. Hit: 6 (1d6 + 3) piercing damage."}
            {:name "Tail" :description "Melee Weapon Attack: +6 to hit, reach 5 ft., one target. Hit: 10 (2d6 + 3) piercing damage."}
            {:name "Hurl Flame" :description "Ranged Spell Attack: +5 to hit, range 150 ft., one target. Hit: 10 (3d6) fire damage. If the target is a flammable object that isn't being worn or carried, it also catches fire."}]
}{
  :name "Bearded Devil"
  :size :medium
  :type :fiend
  :subtypes #{:devil}
  :alignment "lawful evil"
  :armor-class 13
  :armor-notes "natural armor"
  :hit-points {:mean 52 :die-count 8 :die 8 :modifier 16}
  :speed "30 ft."

  :str 16
  :dex 15
  :con 15
  :int 9
  :wis 11
  :cha 11

  :saving-throws {:str 5, :con 4, :wis 2}
  :damage-resistances "cold; bludgeoning, piercing, and slashing from nonmagical attacks that aren't silvered"
  :damage-immunities "fire, poison"
  :condition-immunities "poisoned"
  :senses "darkvision 120 ft., passive Perception 10"
  :languages "Infernal, telepathy 120 ft."
  :challenge 3

  :traits [{:name "Devil's Sight" :description "Magical darkness doesn't impede the devil's darkvision."}
           {:name "Magic Resistance" :description "The devil has advantage on saving throws against spells and other magical effects."}
           {:name "Steadfast" :description "The devil can't be frightened while it can see an allied creature within 30 feet of it."}]

  :actions [{:name "Multiattack" :description "The devil makes two attacks: one with its beard and one with its glaive."}
            {:name "Beard" :description "Melee Weapon Attack: +5 to hit, reach 5 ft., one creature. Hit: 6 (1d8 + 2) piercing damage, and the target must succeed on a DC 12 Constitution saving throw or be poisoned for 1 minute. While poisoned in this way, the target can't regain hit points. The target can repeat the saving throw at the end of each of its turns, ending the effect on itself on a success."}
            {:name "Glaive" :description "Melee Weapon Attack: +5 to hit, reach 10 ft., one target. Hit: 8 (1d10 + 3) slashing damage. If the target is a creature other than an undead or a construct, it must succeed on a DC 12 Constitution saving throw or lose 5 (1d10) hit points at the start of each of its turns due to an infernal wound. Each time the devil hits the wounded target with this attack, the damage dealt by the wound increases by 5 (1d10). Any creature can take an action to stanch the wound with a successful DC 12 Wisdom (Medicine) check. The wound also closes if the target receives magical healing."}]
}{
  :name "Bone Devil"
  :size :large
  :type :fiend
  :subtypes #{:devil}
  :alignment "lawful evil"
  :armor-class 19
  :armor-notes "natural armor"
  :hit-points {:mean 142 :die-count 15 :die 10 :modifier 60}
  :speed "40 ft., fly 40 ft."

  :str 18
  :dex 16
  :con 18
  :int 13
  :wis 14
  :cha 16

  :saving-throws {:int 5, :wis 6, :cha 7}

  :skills {:deception 7, :insight 6}
  :damage-resistances "cold; bludgeoning, piercing, and slashing from nonmagical attacks that aren't silvered"
  :damage-immunities "fire, poison"
  :condition-immunities "poisoned"
  :senses "darkvision 120 ft., passive Perception 12"
  :languages "Infernal, telepathy 120 ft."
  :challenge 9

  :traits [{:name "Devil's Sight" :description "Magical darkness doesn't impede the devil's darkvision."}
           {:name "Magic Resistance" :description "The devil has advantage on saving throws against spells and other magical effects."}]

  :actions [{:name "Multiattack" :description "The devil makes three attacks: two with its claws and one with its sting."}
            {:name "Claw" :description "Melee Weapon Attack: +8 to hit, reach 10 ft., one target. Hit: 8 (1d8 + 4) slashing damage."}
            {:name "Sting" :description "Melee Weapon Attack: +8 to hit, reach 10 ft., one target. Hit: 13 (2d8 + 4) piercing damage plus 17 (5d6) poison damage, and the target must succeed on a DC 14 Constitution saving throw or become poisoned for 1 minute. The target can repeat the saving throw at the end of each of its turns, ending the effect on itself on a success."}]
}{
  :name "Chain Devil"
  :size :medium
  :type :fiend
  :subtypes #{:devil}
  :alignment "lawful evil"
  :armor-class 16
  :armor-notes "natural armor"
  :hit-points {:mean 85 :die-count 10 :die 8 :modifier 40}
  :speed "30 ft."

  :str 18
  :dex 15
  :con 18
  :int 11
  :wis 12
  :cha 14

  :saving-throws {:con 7, :wis 4, :cha 5}
  :damage-resistances "cold; bludgeoning, piercing, and slashing from nonmagical attacks that aren't silvered"
  :damage-immunities "fire, poison"
  :condition-immunities "poisoned"
  :senses "darkvision 120 ft., passive Perception 11"
  :languages "Infernal, telepathy 120 ft."
  :challenge 8

  :traits [{:name "Devil's Sight" :description "Magical darkness doesn't impede the devil's darkvision."}
           {:name "Magic Resistance" :description "The devil has advantage on saving throws against spells and other magical effects."}]

  :actions [{:name "Multiattack" :description "The devil makes two attacks with its chains."}
            {:name "Chain" :description "Melee Weapon Attack: +8 to hit, reach 10 ft., one target. Hit: 11 (2d6 + 4) slashing damage. The target is grappled (escape DC 14) if the devil isn't already grappling a creature. Until this grapple ends, the target is restrained and takes 7 (2d6) piercing damage at the start of each of its turns."}
            {:name "Animate Chains" :notes "Recharges after a Short or Long Rest" :description "Up to four chains the devil can see within 60 feet of it magically sprout razor-edged barbs and animate under the devil's control, provided that the chains aren't being worn or carried.
Each animated chain is an object with AC 20, 20 hit points, resistance to piercing damage, and immunity to psychic and thunder damage. When the devil uses Multiattack on its turn, it can use each animated chain to make one additional chain attack. An animated chain can grapple one creature of its own but can't make attacks while grappling. An animated chain reverts to its inanimate state if reduced to 0 hit points or if the devil is incapacitated or dies."}]

  :reactions [{:name "Unnerving Mask" :description "When a creature the devil can see starts its turn within 30 feet of the devil, the devil can create the illusion that it looks like one of the creature's departed loved ones or bitter enemies. If the creature can see the devil, it must succeed on a DC 14 Wisdom saving throw or be frightened until the end of its turn."}]
}{
  :name "Erinyes"
  :size :medium
  :type :fiend
  :subtypes #{:devil}
  :alignment "lawful evil"
  :armor-class 18
  :armor-notes "(plate)"
  :hit-points {:mean 153 :die-count 18 :die 8 :modifier 72}
  :speed "30 ft., fly 60 ft."

  :str 18
  :dex 16
  :con 18
  :int 14
  :wis 14
  :cha 18

  :saving-throws {:dex 7, :con 8, :wis 6, :cha 8}
  :damage-resistances "cold; bludgeoning, piercing, and slashing from nonmagical attacks that aren't silvered"
  :damage-immunities "fire, poison"
  :condition-immunities "poisoned"
  :senses "truesight 120 ft., passive Perception 12"
  :languages "Infernal, telepathy 120 ft."
  :challenge 12

  :traits [{:name "Hellish Weapons" :description "The erinyes's weapon attacks are magical and deal an extra 13 (3d8) poison damage on a hit (included in the attacks)."}
           {:name "Magic Resistance" :description "The erinyes has advantage on saving throws against spells and other magical effects."}]

  :actions [{:name "Multiattack" :description "The erinyes makes three attacks."}
            {:name "Longsword" :description "Melee Weapon Attack: +8 to hit, reach 5 ft., one target. Hit: 8 (1d8 + 4) slashing damage, or 9 (1d10 + 4) slashing damage if used with two hands, plus 13 (3d8) poison damage."}
            {:name "Longbow" :description "Ranged Weapon Attack: +7 to hit, range 150/600 ft., one target. Hit: 7 (1d8 + 3) piercing damage plus 13 (3d8) poison damage, and the target must succeed on a DC 14 Constitution saving throw or be poisoned. The poison lasts until it is removed by the lesser restoration spell or similar magic."}]

  :reactions [{:name "Parry" :description "The erinyes adds 4 to its AC against one melee attack that would hit it. To do so, the erinyes must see the attacker and be wielding a melee weapon."}]
}{
  :name "Horned Devil"
  :size :large
  :type :fiend
  :subtypes #{:devil}
  :alignment "lawful evil"
  :armor-class 18
  :armor-notes "natural armor"
  :hit-points {:mean 148 :die-count 17 :die 10 :modifier 55}
  :speed "20 ft., fly 60 ft."

  :str 22
  :dex 17
  :con 21
  :int 12
  :wis 16
  :cha 17

  :saving-throws {:str 10, :dex 7, :wis 7, :cha 7}
  :damage-resistances "cold; bludgeoning, piercing, and slashing from nonmagical attacks not made with silvered weapons"
  :damage-immunities "fire, poison"
  :condition-immunities "poisoned"
  :senses "darkvision 120 ft., passive Perception 13"
  :languages "Infernal, telepathy 120 ft."
  :challenge 11

  :traits [{:name "Devil's Sight" :description "Magical darkness doesn't impede the devil's darkvision."}
           {:name "Magic Resistance" :description "The devil has advantage on saving throws against spells and other magical effects."}]

  :actions [{:name "Multiattack" :description "The devil makes three melee attacks: two with its fork and one with its tail. It can use Hurl Flame in place of any melee attack."}
            {:name "Fork" :description "Melee Weapon Attack: +10 to hit, reach 10 ft., one target. Hit: 15 (2d8 + 6) piercing damage."}
            {:name "Tail" :description "Melee Weapon Attack: +10 to hit, reach 10 ft., one target. Hit: 10 (1d8 + 6) piercing damage. If the target is a creature other than an undead or a construct, it must succeed on a DC 17 Constitution saving throw or lose 10 (3d6) hit points at the start of each of its turns due to an infernal wound. Each time the devil hits the wounded target with this attack, the damage dealt by the wound increases by 10 (3d6). Any creature can take an action to stanch the wound with a successful DC 12 Wisdom (Medicine) check. The wound also closes if the target receives magical healing."}
            {:name "Hurl Flame" :description "Ranged Spell Attack: +7 to hit, range 150 ft., one target. Hit: 14 (4d6) fire damage. If the target is a flammable object that isn't being worn or carried, it also catches fire."}]
}{
  :name "Ice Devil"
  :size :large
  :type :fiend
  :subtypes #{:devil}
  :alignment "lawful evil"
  :armor-class 18
  :armor-notes "natural armor"
  :hit-points {:mean 180 :die-count 19 :die 10 :modifier 76}
  :speed "40 ft."

  :str 21
  :dex 14
  :con 18
  :int 18
  :wis 15
  :cha 18

  :saving-throws {:dex 7, :con 9, :wis 7, :cha 9}
  :damage-resistances "bludgeoning, piercing, and slashing from nonmagical attacks that aren't silvered"
  :damage-immunities "cold, fire, poison"
  :condition-immunities "poisoned"
  :senses "blindsight 60 ft., darkvision 120 ft., passive Perception 12"
  :languages "Infernal, telepathy 120 ft."
  :challenge 14

  :traits [{:name "Devil's Sight" :description "Magical darkness doesn't impede the devil's darkvision."}
           {:name "Magic Resistance" :description "The devil has advantage on saving throws against spells and other magical effects."}]

  :actions [{:name "Multiattack" :description "The devil makes three attacks: one with its bite, one with its claws, and one with its tail."}
            {:name "Bite" :description "Melee Weapon Attack: +10 to hit, reach 5 ft., one target. Hit: 12 (2d6 + 5) piercing damage plus 10 (3d6) cold damage."}
            {:name "Claws" :description "Melee Weapon Attack: +10 to hit, reach 5 ft., one target. Hit: 10 (2d4 + 5) slashing damage plus 10 (3d6) cold damage."}
            {:name "Tail" :description "Melee Weapon Attack: +10 to hit, reach 10 ft., one target. Hit: 12 (2d6 + 5) bludgeoning damage plus 10 (3d6) cold damage."}
            {:name "Wall of Ice" :notes "Recharge 6" :description "The devil magically forms an opaque wall of ice on a solid surface it can see within 60 feet of it. The wall is 1 foot thick and up to 30 feet long and 10 feet high, or it's a hemispherical dome up to 20 feet in diameter.
When the wall appears, each creature in its space is pushed out of it by the shortest route. The creature chooses which side of the wall to end up on, unless the creature is incapacitated. The creature then makes a DC 17 Dexterity saving throw, taking 35 (10d6) cold damage on a failed save, or half as much damage on a successful one. 
The wall lasts for 1 minute or until the devil is incapacitated or dies. The wall can be damaged and breached; each 10-foot section has AC 5, 30 hit points, vulnerability to fire damage, and immunity to acid, cold, necrotic, poison, and psychic damage. If a section is destroyed, it leaves behind a sheet of frigid air in the space the wall occupied. Whenever a creature finishes moving through the frigid air on a turn, willingly or otherwise, the creature must make a DC 17 Constitution saving throw, taking 17 (5d6) cold damage on a failed save, or half as much damage on a successful one. The frigid air dissipates when the rest of the wall vanishes."}]
}{
  :name "Imp"
  :size :tiny
  :type :fiend
  :subtypes #{:devil, :shapechanger}
  :alignment "lawful evil"
  :armor-class 13
  :hit-points {:mean 10 :die-count 3 :die 4 :modifier 3}
  :speed "20 ft., fly 40 ft."

  :str 6
  :dex 17
  :con 13
  :int 11
  :wis 12
  :cha 14

  :skills {:deception 4, :insight 3, :persuasion 4, :stealth 5}
  :damage-resistances "cold; bludgeoning, piercing, and slashing from nonmagical attacks that aren't silvered"
  :damage-immunities "fire, poison"
  :condition-immunities "poisoned"
  :senses "darkvision 120 ft., passive Perception 11"
  :languages "Infernal, Common"
  :challenge 1

  :traits [{:name "Shapechanger" :description "The imp can use its action to polymorph into a beast form that resembles a rat (speed 20 ft.), a raven (20 ft., fly 60 ft.), or a spider (20 ft., climb 20 ft.), or back into its true form. Its statistics are the same in each form, except for the speed changes noted. Any equipment it is wearing or carrying isn't transformed. It reverts to its true form if it dies.
Devil's Sight. Magical darkness doesn't impede the imp's darkvision."}
           {:name "Magic Resistance" :description "The imp has advantage on saving throws against spells and other magical effects."}]

  :actions [{:name "Sting" :notes "Bite in Beast Form" :description "Melee Weapon Attack: +5 to hit, reach 5 ft., one target. Hit: 5 (1d4 + 3) piercing damage, and the target must make on a DC 11 Constitution saving throw, taking 10 (3d6) poison damage on a failed save, or half as much damage on a successful one."}
            {:name "Invisibility" :description "The imp magically turns invisible until it attacks or until its concentration ends (as if concentrating on a spell). Any equipment the imp wears or carries is invisible with it."}]
}{
  :name "Lemure"
  :size :medium
  :type :fiend
  :subtypes #{:devil}
  :alignment "lawful evil"
  :armor-class 7
  :hit-points {:mean 13 :die-count 3 :die 8}
  :speed "15 ft."

  :str 10
  :dex 5
  :con 11
  :int 1
  :wis 11
  :cha 3

  :damage-resistances "cold"
  :damage-immunities "fire, poison"
  :condition-immunities "charmed, frightened, poisoned"
  :senses "darkvision 120 ft., passive Perception 10"
  :languages "understands Infernal but can't speak"
  :challenge 0

  :traits [{:name "Devil's Sight" :description "Magical darkness doesn't impede the lemure's darkvision."}
           {:name "Hellish Rejuvenation" :description "A lemure that dies in the Nine Hells comes back to life with all its hit points in 1d10 days unless it is killed by a good-aligned creature with a bless spell cast on that creature or its remains are sprinkled with holy water."}]

  :actions [{:name "Fist" :description "Melee Weapon Attack: +3 to hit, reach 5 ft., one target. Hit: 2 (1d4) bludgeoning damage."}]
}{
  :name "Pit Fiend"
  :size :large
  :type :fiend
  :subtypes #{:devil}
  :alignment "lawful evil"
  :armor-class 19
  :armor-notes "natural armor"
  :hit-points {:mean 300 :die-count 24 :die 10 :modifier 168}
  :speed "30 ft., fly 60 ft."

  :str 26
  :dex 14
  :con 24
  :int 22
  :wis 18
  :cha 24

  :saving-throws {:dex 8, :con 13, :wis 10}
  :damage-resistances "cold; bludgeoning, piercing, and slashing from nonmagical attacks that aren't silvered"
  :damage-immunities "fire, poison"
  :condition-immunities "poisoned"
  :senses "truesight 120 ft., passive Perception 14"
  :languages "Infernal, telepathy 120 ft."
  :challenge 20

  :traits [{:name "Fear Aura" :description "Any creature hostile to the pit fiend that starts its turn within 20 feet of the pit fiend must make a DC 21 Wisdom saving throw, unless the pit fiend is incapacitated. On a failed save, the creature is frightened until the start of its next turn. If a creature's saving throw is successful, the creature is immune to the pit fiend's Fear Aura for the next 24 hours."}
           {:name "Magic Resistance" :description "The pit fiend has advantage on saving throws against spells and other magical effects."}
           {:name "Magic Weapons" :description "The pit fiend's weapon attacks are magical."}
           {:name "Innate Spellcasting" :description "The pit fiend's spellcasting ability is Charisma (spell save DC 21). 
The pit fiend can innately cast the following spells, requiring no material components:
At will: detect magic, fireball
3/day each: hold monster, wall of fire"}]

  :actions [{:name "Multiattack" :description "The pit fiend makes four attacks: one with its bite, one with its claw, one with its mace, and one with its tail."}
            {:name "Bite" :description "Melee Weapon Attack: +14 to hit, reach 5 ft., one target. Hit: 22 (4d6 + 8) piercing damage. The target must succeed on a DC 21 Constitution saving throw or become poisoned. While poisoned in this way, the target can't regain hit points, and it takes 21 (6d6) poison damage at the start of each of its turns. The poisoned target can repeat the saving throw at the end of each of its turns, ending the effect on itself on a success."}
            {:name "Claw" :description "Melee Weapon Attack: +14 to hit, reach 10 ft., one target. Hit: 17 (2d8 + 8) slashing damage."}
            {:name "Mace" :description "Melee Weapon Attack: +14 to hit, reach 10 ft., one target. Hit: 15 (2d6 + 8) bludgeoning damage plus 21 (6d6) fire damage."}
            {:name "Tail" :description "Melee Weapon Attack: +14 to hit, reach 10 ft., one target. Hit: 24 (3d10 + 8) bludgeoning damage."}]
}{
  :name "Plesiosaurus"
  :size :large
  :type :beast
  :alignment "unaligned"
  :armor-class 13
  :armor-notes "natural armor"
  :hit-points {:mean 68 :die-count 8 :die 10 :modifier 24}
  :speed "20 ft., swim 40 ft."

  :str 18
  :dex 15
  :con 16
  :int 2
  :wis 12
  :cha 5

  :skills {:perception 3, :stealth 4}
  :senses "passive Perception 13"
  :challenge 2

  :traits [{:name "Hold Breath" :description "The plesiosaurus can hold its breath for 1 hour."}]

  :actions [{:name "Bite" :description "Melee Weapon Attack: +6 to hit, reach 10 ft., one target. Hit: 14 (3d6 + 4) piercing damage."}]
}{
  :name "Triceratops"
  :size :huge
  :type :beast
  :alignment "unaligned"
  :armor-class 13
  :armor-notes "natural armor"
  :hit-points {:mean 95 :die-count 10 :die 12 :modifier 30}
  :speed "50 ft."

  :str 22
  :dex 9
  :con 17
  :int 2
  :wis 11
  :cha 5

  :senses "passive Perception 10"
  :challenge 5

  :traits [{:name "Trampling Charge" :description "If the triceratops moves at least 20 feet straight toward a creature and then hits it with a gore attack on the same turn, that target must succeed on a DC 13 Strength saving throw or be knocked prone.
If the target is prone, the triceratops can make one stomp attack against it as a bonus action."}]

  :actions [{:name "Gore" :description "Melee Weapon Attack: +9 to hit, reach 5 ft., one target. Hit: 24 (4d8 + 6) piercing damage."}
            {:name "Stomp" :description "Melee Weapon Attack: +9 to hit, reach 5 ft., one prone creature. Hit: 22 (3d10 + 6) bludgeoning damage."}]
}{
  :name "Tyrannosaurus Rex"
  :size :huge
  :type :beast
  :alignment "unaligned"
  :armor-class 13
  :armor-notes "natural armor"
  :hit-points {:mean 136 :die-count 13 :die 12 :modifier 52}
  :speed "50 ft."

  :str 25
  :dex 10
  :con 19
  :int 2
  :wis 12
  :cha 9

  :skills {:perception 4}
  :senses "passive Perception 14"
  :challenge 8

  :actions [{:name "Multiattack" :description "The tyrannosaurus makes two attacks: one with its bite and one with its tail. It can't make both attacks against the same target."}
            {:name "Bite" :description "Melee Weapon Attack: +10 to hit, reach 10 ft., one target. Hit: 33 (4d12 + 7) piercing damage. If the target is a Medium or smaller creature, it is grappled (escape DC 17). Until this grapple ends, the target is restrained, and the tyrannosaurus can't bite another target."}
            {:name "Tail" :description "Melee Weapon Attack: +10 to hit, reach 10 ft., one target. Hit: 20 (3d8 + 7) bludgeoning damage."}]
}{
  :name "Doppelganger"
  :size :medium
  :type :monstrosity
  :subtypes #{:shapechanger}
  :alignment "neutral"
  :armor-class 14
  :hit-points {:mean 52 :die-count 8 :die 8 :modifier 16}
  :speed "30 ft."

  :str 11
  :dex 18
  :con 14
  :int 11
  :wis 12
  :cha 14

  :skills {:deception 6, :insight 3}
  :condition-immunities "charmed"
  :senses "darkvision 60 ft., passive Perception 11"
  :languages "Common"
  :challenge 3

  :traits [{:name "Shapechanger" :description "The doppelganger can use its action to polymorph into a Small or Medium humanoid it has seen, or back into its true form. Its statistics, other than its size, are the same in each form. Any equipment it is wearing or carrying isn't transformed. It reverts to its true form if it dies."}
           {:name "Ambusher" :description "The doppelganger has advantage on attack rolls against any creature it has surprised."}
           {:name "Surprise Attack" :description "If the doppelganger surprises a creature and hits it with an attack during the first round of combat, the target takes an extra 10 (3d6) damage from the attack."}]

  :actions [{:name "Multiattack" :description "The doppelganger makes two melee attacks."}
            {:name "Slam" :description "Melee Weapon Attack: +6 to hit, reach 5 ft., one target. Hit: 7 (1d6 + 4) bludgeoning damage."}
            {:name "Read Thoughts" :description "The doppelganger magically reads the surface thoughts of one creature within 60 feet of it. The effect can penetrate barriers, but 3 feet of wood or dirt, 2 feet of stone, 2 inches of metal, or a thin sheet of lead blocks it. While the target is in range, the doppelganger can continue reading its thoughts, as long as the doppelganger's concentration isn't broken (as if concentrating on a spell). While reading the target's mind, the doppelganger has advantage on Wisdom (Insight) and Charisma (Deception, Intimidation, and Persuasion) checks against the target."}]
}{
  :name "Ancient Black Dragon"
  :size :gargantuan
  :type :dragon
  :alignment "chaotic evil"
  :armor-class 22
  :armor-notes "natural armor"
  :hit-points {:mean 367 :die-count 21 :die 20 :modifier 147}
  :speed "40 ft., fly 80 ft., swim 40 ft."

  :str 27
  :dex 14
  :con 25
  :int 16
  :wis 15
  :cha 19

  :saving-throws {:dex 9, :con 14, :wis 9, :cha 11}

  :skills {:perception 16, :stealth 9}
  :damage-immunities "acid"
  :senses "blindsight 60 ft., darkvision 120 ft., passive Perception 26"
  :languages "Common, Draconic"
  :challenge 21

  :traits [{:name "Amphibious" :description "The dragon can breathe air and water."}
           {:name "Legendary Resistance" :notes "3/Day" :description "If the dragon fails a saving throw, it can choose to succeed instead."}]

  :actions [{:name "Multiattack" :description "The dragon can use its Frightful Presence. It then makes three attacks: one with its bite and two with its claws."}
            {:name "Bite" :description "Melee Weapon Attack: +15 to hit, reach 15 ft., one target. Hit: 19 (2d10 + 8) piercing damage plus 9 (2d8) acid damage."}
            {:name "Claw" :description "Melee Weapon Attack: +15 to hit, reach 10 ft., one target. Hit: 15 (2d6 + 8) slashing damage."}
            {:name "Tail" :description "Melee Weapon Attack: +15 to hit, reach 20 ft., one target. Hit: 17 (2d8 + 8) bludgeoning damage."}
            {:name "Frightful Presence" :description "Each creature of the dragon's choice that is within 120 feet of the dragon and aware of it must succeed on a DC 19 Wisdom saving throw or become frightened for 1 minute. A creature can repeat the saving throw at the end of each of its turns, ending the effect on itself on a success. If a creature's saving throw is successful or the effect ends for it, the creature is immune to the dragon's Frightful Presence for the next 24 hours."}
            {:name "Acid Breath" :notes "Recharge 5–6" :description "The dragon exhales acid in a 90-foot line that is 10 feet wide. Each creature in that line must make a DC 22 Dexterity saving throw, taking 67 (15d8) acid damage on a failed save, or half as much damage on a successful one."}]

  :legendary-actions {:description "The dragon can take 3 legendary actions, choosing from the options below. Only one legendary action option can be used at a time and only at the end of another creature's turn. The dragon regains spent legendary actions at the start of its turn."
                      :actions [{:name "Detect" :description "The dragon makes a Wisdom (Perception) check."}
                                {:name "Tail Attack" :description "The dragon makes a tail attack."}
                                {:name "Wing Attack" :notes "Costs 2 Actions" :description "The dragon beats its wings. Each creature within 15 feet of the dragon must succeed on a DC 23 Dexterity saving throw or take 15 (2d6 + 8) bludgeoning damage and be knocked prone. The dragon can then fly up to half its flying speed."}]}
}{
  :name "Adult Black Dragon"
  :size :huge
  :type :dragon
  :alignment "chaotic evil"
  :armor-class 19
  :armor-notes "natural armor"
  :hit-points {:mean 195 :die-count 17 :die 12 :modifier 85}
  :speed "40 ft., fly 80 ft., swim 40 ft."

  :str 23
  :dex 14
  :con 21
  :int 14
  :wis 13
  :cha 17

  :saving-throws {:dex 7, :con 10, :wis 6, :cha 8}

  :skills {:perception 11, :stealth 7}
  :damage-immunities "acid"
  :senses "blindsight 60 ft., darkvision 120 ft., passive Perception 21"
  :languages "Common, Draconic"
  :challenge 14

  :traits [{:name "Amphibious" :description "The dragon can breathe air and water."}
           {:name "Legendary Resistance" :notes "3/Day" :description "If the dragon fails a saving throw, it can choose to succeed instead."}]

  :actions [{:name "Multiattack" :description "The dragon can use its Frightful Presence. It then makes three attacks: one with its bite and two with its claws."}
            {:name "Bite" :description "Melee Weapon Attack: +11 to hit, reach 10 ft., one target. Hit: 17 (2d10 + 6) piercing damage plus 4 (1d8) acid damage."}
            {:name "Claw" :description "Melee Weapon Attack: +11 to hit, reach 5 ft., one target. Hit: 13 (2d6 + 6) slashing damage."}
            {:name "Tail" :description "Melee Weapon Attack: +11 to hit, reach 15 ft., one target. Hit: 15 (2d8 + 6) bludgeoning damage."}
            {:name "Frightful Presence" :description "Each creature of the dragon's choice that is within 120 feet of the dragon and aware of it must succeed on a DC 16 Wisdom saving throw or become frightened for 1 minute. A creature can repeat the saving throw at the end of each of its turns, ending the effect on itself on a success. If a creature's saving throw is successful or the effect ends for it, the creature is immune to the dragon's Frightful Presence for the next 24 hours."}
            {:name "Acid Breath" :notes "Recharge 5–6" :description "The dragon exhales acid in a 60-foot line that is 5 feet wide. Each creature in that line must make a DC 18 Dexterity saving throw, taking 54 (12d8) acid damage on a failed save, or half as much damage on a successful one."}]

  :legendary-actions {:description "The dragon can take 3 legendary actions, choosing from the options below. Only one legendary action option can be used at a time and only at the end of another creature's turn. The dragon regains spent legendary actions at the start of its turn."
                      :actions [{:name "Detect" :description "The dragon makes a Wisdom (Perception) check."}
                                {:name "Tail Attack" :description "The dragon makes a tail attack."}
                                {:name "Wing Attack" :notes "Costs 2 Actions" :description "The dragon beats its wings. Each creature within 10 feet of the dragon must succeed on a DC 19 Dexterity saving throw or take 13 (2d6 + 6) bludgeoning damage and be knocked prone. The dragon can then fly up to half its flying speed."}]}
}{
  :name "Young Black Dragon"
  :size :large
  :type :dragon
  :alignment "chaotic evil"
  :armor-class 18
  :armor-notes "natural armor"
  :hit-points {:mean 127 :die-count 15 :die 10 :modifier 45}
  :speed "40 ft., fly 80 ft., swim 40 ft."

  :str 19
  :dex 14
  :con 17
  :int 12
  :wis 11
  :cha 15

  :saving-throws {:dex 5, :con 6, :wis 3, :cha 5}

  :skills {:perception 6, :stealth 5}
  :damage-immunities "acid"
  :senses "blindsight 30 ft., darkvision 120 ft., passive Perception 16"
  :languages "Common, Draconic"
  :challenge 7

  :traits [{:name "Amphibious" :description "The dragon can breathe air and water."}]

  :actions [{:name "Multiattack" :description "The dragon makes three attacks: one with its bite and two with its claws."}
            {:name "Bite" :description "Melee Weapon Attack: +7 to hit, reach 10 ft., one target. Hit: 15 (2d10 + 4) piercing damage plus 4 (1d8) acid damage."}
            {:name "Claw" :description "Melee Weapon Attack: +7 to hit, reach 5 ft., one target. Hit: 11 (2d6 + 4) slashing damage."}]
}{
  :name "Black Dragon Wyrmling"
  :size :medium
  :type :dragon
  :alignment "chaotic evil"
  :armor-class 17
  :armor-notes "natural armor"
  :hit-points {:mean 33 :die-count 6 :die 8 :modifier 6}
  :speed "30 ft., fly 60 ft., swim 30 ft."

  :str 15
  :dex 14
  :con 13
  :int 10
  :wis 11
  :cha 13

  :saving-throws {:dex 4, :con 3, :wis 2, :cha 3}

  :skills {:perception 4, :stealth 4}
  :damage-immunities "acid"
  :senses "blindsight 10 ft., darkvision 60 ft., passive Perception 14"
  :languages "Draconic"
  :challenge 2

  :traits [{:name "Amphibious" :description "The dragon can breathe air and water."}]

  :actions [{:name "Bite" :description "Melee Weapon Attack: +4 to hit, reach 5 ft., one target. Hit: 7 (1d10 + 2) piercing damage plus 2 (1d4) acid damage."}
            {:name "Acid Breath" :notes "Recharge 5–6" :description "The dragon exhales acid in a 15-foot line that is 5 feet wide. Each creature in that line must make a DC 11 Dexterity saving throw, taking 22 (5d8) acid damage on a failed save, or half as much damage on a successful one."}]
}{
  :name "Ancient Blue Dragon"
  :size :gargantuan
  :type :dragon
  :alignment "lawful evil"
  :armor-class 22
  :armor-notes "natural armor"
  :hit-points {:mean 481 :die-count 26 :die 20 :modifier 208}
  :speed "40 ft., burrow 40 ft., fly 80 ft."

  :str 29
  :dex 10
  :con 27
  :int 18
  :wis 17
  :cha 21

  :saving-throws {:dex 7, :con 15, :wis 10, :cha 12}

  :skills {:perception 17, :stealth 7}
  :damage-immunities "lightning"
  :senses "blindsight 60 ft., darkvision 120 ft., passive Perception 27"
  :languages "Common, Draconic"
  :challenge 23

  :traits [{:name "Legendary Resistance" :notes "3/Day" :description "If the dragon fails a saving throw, it can choose to succeed instead."}]

  :actions [{:name "Multiattack" :description "The dragon can use its Frightful Presence. It then makes three attacks: one with its bite and two with its claws."}
            {:name "Bite" :description "Melee Weapon Attack: +16 to hit, reach 15 ft., one target. Hit: 20 (2d10 + 9) piercing damage plus 11 (2d10) lightning damage."}
            {:name "Claw" :description "Melee Weapon Attack: +16 to hit, reach 10 ft., one target. Hit: 16 (2d6 + 9) slashing damage."}
            {:name "Tail" :description "Melee Weapon Attack: +16 to hit, reach 20 ft., one target. Hit: 18 (2d8 + 9) bludgeoning damage."}
            {:name "Frightful Presence" :description "Each creature of the dragon's choice that is within 120 feet of the dragon and aware of it must succeed on a DC 20 Wisdom saving throw or become frightened for 1 minute. A creature can repeat the saving throw at the end of each of its turns, ending the effect on itself on a success. If a creature's saving throw is successful or the effect ends for it, the creature is immune to the dragon's Frightful Presence for the next 24 hours."}
            {:name "Lightning Breath" :notes "Recharge 5–6" :description "The dragon exhales lightning in a 120-foot line that is 10 feet wide. Each creature in that line must make a DC 23 Dexterity saving throw, taking 88 (16d10) lightning damage on a failed save, or half as much damage on a successful one."}]

  :legendary-actions {:description "The dragon can take 3 legendary actions, choosing from the options below. Only one legendary action option can be used at a time and only at the end of another creature's turn. The dragon regains spent legendary actions at the start of its turn."
                      :actions [{:name "Detect" :description "The dragon makes a Wisdom (Perception) check."}
                                {:name "Tail Attack" :description "The dragon makes a tail attack."}
                                {:name "Wing Attack" :notes "Costs 2 Actions" :description "The dragon beats its wings. Each creature within 15 feet of the dragon must succeed on a DC 24 Dexterity saving throw or take 16 (2d6 + 9) bludgeoning damage and be knocked prone. The dragon can then fly up to half its flying speed."}]}
}{
  :name "Adult Blue Dragon"
  :size :huge
  :type :dragon
  :alignment "lawful evil"
  :armor-class 19
  :armor-notes "natural armor"
  :hit-points {:mean 225 :die-count 18 :die 12 :modifier 108}
  :speed "40 ft., burrow 30 ft., fly 80 ft."

  :str 25
  :dex 10
  :con 23
  :int 16
  :wis 15
  :cha 19

  :saving-throws {:dex 5, :con 11, :wis 7, :cha 9}

  :skills {:perception 12, :stealth 5}
  :damage-immunities "lightning"
  :senses "blindsight 60 ft., darkvision 120 ft., passive Perception 22"
  :languages "Common, Draconic"
  :challenge 16

  :traits [{:name "Legendary Resistance" :notes "3/Day" :description "If the dragon fails a saving throw, it can choose to succeed instead."}]

  :actions [{:name "Multiattack" :description "The dragon can use its Frightful Presence. It then makes three attacks: one with its bite and two with its claws."}
            {:name "Bite" :description "Melee Weapon Attack: +12 to hit, reach 10 ft., one target. Hit: 18 (2d10 + 7) piercing damage plus 5 (1d10) lightning damage."}
            {:name "Claw" :description "Melee Weapon Attack: +12 to hit, reach 5 ft., one target. Hit: 14 (2d6 + 7) slashing damage."}
            {:name "Tail" :description "Melee Weapon Attack: +12 to hit, reach 15 ft., one target. Hit: 16 (2d8 + 7) bludgeoning damage."}
            {:name "Frightful Presence" :description "Each creature of the dragon's choice that is within 120 feet of the dragon and aware of it must succeed on a DC 17 Wisdom saving throw or become frightened for 1 minute. A creature can repeat the saving throw at the end of each of its turns, ending the effect on itself on a success. If a creature's saving throw is successful or the effect ends for it, the creature is immune to the dragon's Frightful Presence for the next 24 hours."}
            {:name "Lightning Breath" :notes "Recharge 5–6" :description "The dragon exhales lightning in a 90-foot line that is 5 feet wide. Each creature in that line must make a DC 19 Dexterity saving throw, taking 66 (12d10) lightning damage on a failed save, or half as much damage on a successful one."}]

  :legendary-actions {:description "The dragon can take 3 legendary actions, choosing from the options below. Only one legendary action option can be used at a time and only at the end of another creature's turn. The dragon regains spent legendary actions at the start of its turn."
                      :actions [{:name "Detect" :description "The dragon makes a Wisdom (Perception) check."}
                                {:name "Tail Attack" :description "The dragon makes a tail attack."}
                                {:name "Wing Attack" :notes "Costs 2 Actions" :description "The dragon beats its wings. Each creature within 10 feet of the dragon must succeed on a DC 20 Dexterity saving throw or take 14 (2d6 + 7) bludgeoning damage and be knocked prone. The dragon can then fly up to half its flying speed."}]}
}{
  :name "Young Blue Dragon"
  :size :large
  :type :dragon
  :alignment "lawful evil"
  :armor-class 18
  :armor-notes "natural armor"
  :hit-points {:mean 152 :die-count 16 :die 10 :modifier 64}
  :speed "40 ft., burrow 20 ft., fly 80 ft."

  :str 21
  :dex 10
  :con 19
  :int 14
  :wis 13
  :cha 17

  :saving-throws {:dex 4, :con 8, :wis 5, :cha 7}

  :skills {:perception 9, :stealth 4}
  :damage-immunities "lightning"
  :senses "blindsight 30 ft., darkvision 120 ft., passive Perception 19"
  :languages "Common, Draconic"
  :challenge 9

  :actions [{:name "Multiattack" :description "The dragon makes three attacks: one with its bite and two with its claws."}
            {:name "Bite" :description "Melee Weapon Attack: +9 to hit, reach 10 ft., one target. Hit: 16 (2d10 + 5) piercing damage plus 5 (1d10) lightning damage."}
            {:name "Claw" :description "Melee Weapon Attack: +9 to hit, reach 5 ft., one target. Hit: 12 (2d6 + 5) slashing damage."}
            {:name "Lightning Breath" :notes "Recharge 5–6" :description "The dragon exhales lightning in an 60-foot line that is 5 feet wide. Each creature in that line must make a DC 16 Dexterity saving throw, taking 55 (10d10) lightning damage on a failed save, or half as much damage on a successful one."}]
}{
  :name "Blue Dragon Wyrmling"
  :size :medium
  :type :dragon
  :alignment "lawful evil"
  :armor-class 17
  :armor-notes "natural armor"
  :hit-points {:mean 52 :die-count 8 :die 8 :modifier 16}
  :speed "30 ft., burrow 15 ft., fly 60 ft."

  :str 17
  :dex 10
  :con 15
  :int 12
  :wis 11
  :cha 15

  :saving-throws {:dex 2, :con 4, :wis 2, :cha 4}

  :skills {:perception 4, :stealth 2}
  :damage-immunities "lightning"
  :senses "blindsight 10 ft., darkvision 60 ft., passive Perception 14"
  :languages "Draconic"
  :challenge 3

  :actions [{:name "Bite" :description "Melee Weapon Attack: +5 to hit, reach 5 ft., one target. Hit: 8 (1d10 + 3) piercing damage plus 3 (1d6) lightning damage."}
            {:name "Lightning Breath" :notes "Recharge 5–6" :description "The dragon exhales lightning in a 30-foot line that is 5 feet wide. Each creature in that line must make a DC 12 Dexterity saving throw, taking 22 (4d10) lightning damage on a failed save, or half as much damage on a successful one."}]
}{
  :name "Ancient Green Dragon"
  :size :gargantuan
  :type :dragon
  :alignment "lawful evil"
  :armor-class 21
  :armor-notes "natural armor"
  :hit-points {:mean 385 :die-count 22 :die 20 :modifier 154}
  :speed "40 ft., fly 80 ft., swim 40 ft."

  :str 27
  :dex 12
  :con 25
  :int 20
  :wis 17
  :cha 19

  :saving-throws {:dex 8, :con 14, :wis 10, :cha 11}

  :skills {:deception 11, :insight 10, :perception 17, :persuasion 11, :stealth 8}
  :damage-immunities "poison"
  :condition-immunities "poisoned"
  :senses "blindsight 60 ft., darkvision 120 ft., passive Perception 27"
  :languages "Common, Draconic"
  :challenge 22

  :traits [{:name "Amphibious" :description "The dragon can breathe air and water."}
           {:name "Legendary Resistance" :notes "3/Day" :description "If the dragon fails a saving throw, it can choose to succeed instead."}]

  :actions [{:name "Multiattack" :description "The dragon can use its Frightful Presence. It then makes three attacks: one with its bite and two with its claws."}
            {:name "Bite" :description "Melee Weapon Attack: +15 to hit, reach 15 ft., one target. Hit: 19 (2d10 + 8) piercing damage plus 10 (3d6) poison damage."}
            {:name "Claw" :description "Melee Weapon Attack: +15 to hit, reach 10 ft., one target. Hit: 22 (4d6 + 8) slashing damage."}
            {:name "Tail" :description "Melee Weapon Attack: +15 to hit, reach 20 ft., one target. Hit: 17 (2d8 + 8) bludgeoning damage."}
            {:name "Frightful Presence" :description "Each creature of the dragon's choice that is within 120 feet of the dragon and aware of it must succeed on a DC 19 Wisdom saving throw or become frightened for 1 minute. A creature can repeat the saving throw at the end of each of its turns, ending the effect on itself on a success. If a creature's saving throw is successful or the effect ends for it, the creature is immune to the dragon's Frightful Presence for the next 24 hours."}
            {:name "Poison Breath" :notes "Recharge 5–6" :description "The dragon exhales poisonous gas in a 90-foot cone. Each creature in that area must make a DC 22 Constitution saving throw, taking 77 (22d6) poison damage on a failed save, or half as much damage on a successful one."}]

  :legendary-actions {:description "The dragon can take 3 legendary actions, choosing from the options below. Only one legendary action option can be used at a time and only at the end of another creature's turn. The dragon regains spent legendary actions at the start of its turn."
                      :actions [{:name "Detect" :description "The dragon makes a Wisdom (Perception) check."}
                                {:name "Tail Attack" :description "The dragon makes a tail attack."}
                                {:name "Wing Attack" :notes "Costs 2 Actions" :description "The dragon beats its wings. Each creature within 15 feet of the dragon must succeed on a DC 23 Dexterity saving throw or take 15 (2d6 + 8) bludgeoning damage and be knocked prone. The dragon can then fly up to half its flying speed."}]}
}{
  :name "Adult Green Dragon"
  :size :huge
  :type :dragon
  :alignment "lawful evil"
  :armor-class 19
  :armor-notes "natural armor"
  :hit-points {:mean 207 :die-count 18 :die 12 :modifier 90}
  :speed "40 ft., fly 80 ft., swim 40 ft."

  :str 23
  :dex 12
  :con 21
  :int 18
  :wis 15
  :cha 17

  :saving-throws {:dex 6, :con 10, :wis 7, :cha 8}

  :skills {:deception 8, :insight 7, :perception 12}
  :persuasion 8, :stealth 6
  :damage-immunities "poison"
  :condition-immunities "poisoned"
  :senses "blindsight 60 ft., darkvision 120 ft., passive Perception 22"
  :languages "Common, Draconic"
  :challenge 15

  :traits [{:name "Amphibious" :description "The dragon can breathe air and water."}
           {:name "Legendary Resistance" :notes "3/Day" :description "If the dragon fails a saving throw, it can choose to succeed instead."}]

  :actions [{:name "Multiattack" :description "The dragon can use its Frightful Presence. It then makes three attacks: one with its bite and two with its claws."}
            {:name "Bite" :description "Melee Weapon Attack: +11 to hit, reach 10 ft., one target. Hit: 17 (2d10 + 6) piercing damage plus 7 (2d6) poison damage."}
            {:name "Claw" :description "Melee Weapon Attack: +11 to hit, reach 5 ft., one target. Hit: 13 (2d6 + 6) slashing damage."}
            {:name "Tail" :description "Melee Weapon Attack: +11 to hit, reach 15 ft., one target. Hit: 15 (2d8 + 6) bludgeoning damage."}
            {:name "Frightful Presence" :description "Each creature of the dragon's choice that is within 120 feet of the dragon and aware of it must succeed on a DC 16 Wisdom saving throw or become frightened for 1 minute. A creature can repeat the saving throw at the end of each of its turns, ending the effect on itself on a success. If a creature's saving throw is successful or the effect ends for it, the creature is immune to the dragon's Frightful Presence for the next 24 hours."}
            {:name "Poison Breath" :notes "Recharge 5–6" :description "The dragon exhales poisonous gas in a 60-foot cone. Each creature in that area must make a DC 18 Constitution saving throw, taking 56 (16d6) poison damage on a failed save, or half as much damage on a successful one."}]

  :legendary-actions {:description "The dragon can take 3 legendary actions, choosing from the options below. Only one legendary action option can be used at a time and only at the end of another creature's turn. The dragon regains spent legendary actions at the start of its turn."
                      :actions [{:name "Detect" :description "The dragon makes a Wisdom (Perception) check."}
                                {:name "Tail Attack" :description "The dragon makes a tail attack."}
                                {:name "Wing Attack" :notes "Costs 2 Actions" :description "The dragon beats its wings. Each creature within 10 feet of the dragon must succeed on a DC 19 Dexterity saving throw or take 13 (2d6 + 6) bludgeoning damage and be knocked prone. The dragon can then fly up to half its flying speed."}]}
}{
  :name "Young Green Dragon"
  :size :large
  :type :dragon
  :alignment "lawful evil"
  :armor-class 18
  :armor-notes "natural armor"
  :hit-points {:mean 136 :die-count 16 :die 10 :modifier 48}
  :speed "40 ft., fly 80 ft., swim 40 ft."

  :str 19
  :dex 12
  :con 17
  :int 16
  :wis 13
  :cha 15

  :saving-throws {:dex 4, :con 6, :wis 4, :cha 5}

  :skills {:deception 5, :perception 7, :stealth 4}
  :damage-immunities "poison"
  :condition-immunities "poisoned"
  :senses "blindsight 30 ft., darkvision 120 ft., passive Perception 17"
  :languages "Common, Draconic"
  :challenge 8

  :traits [{:name "Amphibious" :description "The dragon can breathe air and water."}]

  :actions [{:name "Multiattack" :description "The dragon makes three attacks: one with its bite and two with its claws."}
            {:name "Bite" :description "Melee Weapon Attack: +7 to hit, reach 10 ft., one target. Hit: 15 (2d10 + 4) piercing damage plus 7 (2d6) poison damage."}
            {:name "Claw" :description "Melee Weapon Attack: +7 to hit, reach 5 ft., one target. Hit: 11 (2d6 + 4) slashing damage."}
            {:name "Poison Breath" :notes "Recharge 5–6" :description "The dragon exhales poisonous gas in a 30-foot cone. Each creature in that area must make a DC 14 Constitution saving throw, taking 42 (12d6) poison damage on a failed save, or half as much damage on a successful one."}]
}{
  :name "Green Dragon Wyrmling"
  :size :medium
  :type :dragon
  :alignment "lawful evil"
  :armor-class 17
  :armor-notes "natural armor"
  :hit-points {:mean 38 :die-count 7 :die 8 :modifier 7}
  :speed "30 ft., fly 60 ft., swim 30 ft."

  :str 15
  :dex 12
  :con 13
  :int 14
  :wis 11
  :cha 13

  :saving-throws {:dex 3, :con 3, :wis 2, :cha 3}

  :skills {:perception 4, :stealth 3}
  :damage-immunities "poison"
  :condition-immunities "poisoned"
  :senses "blindsight 10 ft., darkvision 60 ft., passive Perception 14"
  :languages "Draconic"
  :challenge 2

  :traits [{:name "Amphibious" :description "The dragon can breathe air and water."}]

  :actions [{:name "Bite" :description "Melee Weapon Attack: +4 to hit, reach 5 ft., one target. Hit: 7 (1d10 + 2) piercing damage plus 3 (1d6) poison damage."}
            {:name "Poison Breath" :notes "Recharge 5-6" :description "The dragon exhales poisonous gas in a 15-foot cone. Each creature in that area must make a DC 11 Constitution saving throw, taking 21 (6d6) poison damage on a failed save, or half as much damage on a successful one."}]
}{
  :name "Ancient Red Dragon"
  :size :gargantuan
  :type :dragon
  :alignment "chaotic evil"
  :armor-class 22
  :armor-notes "natural armor"
  :hit-points {:mean 546 :die-count 28 :die 20 :modifier 252}
  :speed "40 ft., climb 40 ft., fly 80 ft."

  :str 30
  :dex 10
  :con 29
  :int 18
  :wis 15
  :cha 23

  :saving-throws {:dex 7, :con 16, :wis 9, :cha 13}

  :skills {:perception 16, :stealth 7}
  :damage-immunities "fire"
  :senses "blindsight 60 ft., darkvision 120 ft., passive Perception 26"
  :languages "Common, Draconic"
  :challenge 24

  :traits [{:name "Legendary Resistance" :notes "3/Day" :description "If the dragon fails a saving throw, it can choose to succeed instead."}]

  :actions [{:name "Multiattack" :description "The dragon can use its Frightful Presence. It then makes three attacks: one with its bite and two with its claws."}
            {:name "Bite" :description "Melee Weapon Attack: +17 to hit, reach 15 ft., one target. Hit: 21 (2d10 + 10) piercing damage plus 14 (4d6) fire damage."}
            {:name "Claw" :description "Melee Weapon Attack: +17 to hit, reach 10 ft., one target. Hit: 17 (2d6 + 10) slashing damage."}
            {:name "Tail" :description "Melee Weapon Attack: +17 to hit, reach 20 ft., one target. Hit: 19 (2d8 + 10) bludgeoning damage."}
            {:name "Frightful Presence" :description "Each creature of the dragon's choice that is within 120 feet of the dragon and aware of it must succeed on a DC 21 Wisdom saving throw or become frightened for 1 minute. A creature can repeat the saving throw at the end of each of its turns, ending the effect on itself on a success. If a creature's saving throw is successful or the effect ends for it, the creature is immune to the dragon's Frightful Presence for the next 24 hours."}
            {:name "Fire Breath" :notes "Recharge 5–6" :description "The dragon exhales fire in a 90-foot cone. Each creature in that area must make a DC 24 Dexterity saving throw, taking 91 (26d6) fire damage on a failed save, or half as much damage on a successful one."}]

  :legendary-actions {:description "The dragon can take 3 legendary actions, choosing from the options below. Only one legendary action option can be used at a time and only at the end of another creature's turn. The dragon regains spent legendary actions at the start of its turn."
                      :actions [{:name "Detect" :description "The dragon makes a Wisdom (Perception) check."}
                                {:name "Tail Attack" :description "The dragon makes a tail attack."}
                                {:name "Wing Attack" :notes "Costs 2 Actions" :description "The dragon beats its wings. Each creature within 15 feet of the dragon must succeed on a DC 25 Dexterity saving throw or take 17 (2d6 + 10) bludgeoning damage and be knocked prone. The dragon can then fly up to half its flying speed."}]}
}{
  :name "Adult Red Dragon"
  :size :huge
  :type :dragon
  :alignment "chaotic evil"
  :armor-class 19
  :armor-notes "natural armor"
  :hit-points {:mean 256 :die-count 19 :die 12 :modifier 133}
  :speed "40 ft., climb 40 ft., fly 80 ft."

  :str 27
  :dex 10
  :con 25
  :int 16
  :wis 13
  :cha 21

  :saving-throws {:dex 6, :con 13, :wis 7, :cha 11}

  :skills {:perception 13, :stealth 6}
  :damage-immunities "fire"
  :senses "blindsight 60 ft., darkvision 120 ft., passive Perception 23"
  :languages "Common, Draconic"
  :challenge 17

  :traits [{:name "Legendary Resistance" :notes "3/Day" :description "If the dragon fails a saving throw, it can choose to succeed instead."}]

  :actions [{:name "Multiattack" :description "The dragon can use its Frightful Presence. It then makes three attacks: one with its bite and two with its claws."}
            {:name "Bite" :description "Melee Weapon Attack: +14 to hit, reach 10 ft., one target. Hit: 19 (2d10 + 8) piercing damage plus 7 (2d6) fire damage."}
            {:name "Claw" :description "Melee Weapon Attack: +14 to hit, reach 5 ft., one target. Hit: 15 (2d6 + 8) slashing damage."}
            {:name "Tail" :description "Melee Weapon Attack: +14 to hit, reach 15 ft., one target. Hit: 17 (2d8 + 8) bludgeoning damage."}
            {:name "Frightful Presence" :description "Each creature of the dragon's choice that is within 120 feet of the dragon and aware of it must succeed on a DC 19 Wisdom saving throw or become frightened for 1 minute. A creature can repeat the saving throw at the end of each of its turns, ending the effect on itself on a success. If a creature's saving throw is successful or the effect ends for it, the creature is immune to the dragon's Frightful Presence for the next 24 hours."}
            {:name "Fire Breath" :notes "Recharge 5–6" :description "The dragon exhales fire in a 60-foot cone. Each creature in that area must make a DC 21 Dexterity saving throw, taking 63 (18d6) fire damage on a failed save, or half as much damage on a successful one."}]

  :legendary-actions {:description "The dragon can take 3 legendary actions, choosing from the options below. Only one legendary action option can be used at a time and only at the end of another creature's turn. The dragon regains spent legendary actions at the start of its turn."
                      :actions [{:name "Detect" :description "The dragon makes a Wisdom (Perception) check."}
                                {:name "Tail Attack" :description "The dragon makes a tail attack."}
                                {:name "Wing Attack" :notes "Costs 2 Actions" :description "The dragon beats its wings. Each creature within 10 feet of the dragon must succeed on a DC 22 Dexterity saving throw or take 15 (2d6 + 8) bludgeoning damage and be knocked prone. The dragon can then fly up to half its flying speed."}]}
}{
  :name "Young Red Dragon"
  :size :large
  :type :dragon
  :alignment "chaotic evil"
  :armor-class 18
  :armor-notes "natural armor"
  :hit-points {:mean 178 :die-count 17 :die 10 :modifier 85}
  :speed "40 ft., climb 40 ft., fly 80 ft."

  :str 23
  :dex 10
  :con 21
  :int 14
  :wis 11
  :cha 19

  :saving-throws {:dex 4, :con 9, :wis 4, :cha 8}

  :skills {:perception 8, :stealth 4}
  :damage-immunities "fire"
  :senses "blindsight 30 ft., darkvision 120 ft., passive Perception 18"
  :languages "Common, Draconic"
  :challenge 10

  :actions [{:name "Multiattack" :description "The dragon makes three attacks: one with its bite and two with its claws."}
            {:name "Bite" :description "Melee Weapon Attack: +10 to hit, reach 10 ft., one target. Hit: 17 (2d10 + 6) piercing damage plus 3 (1d6) fire damage."}
            {:name "Claw" :description "Melee Weapon Attack: +10 to hit, reach 5 ft., one target. Hit: 13 (2d6 + 6) slashing damage."}
            {:name "Fire Breath" :notes "Recharge 5–6" :description "The dragon exhales fire in a 30-foot cone. Each creature in that area must make a DC 17 Dexterity saving throw, taking 56 (16d6) fire damage on a failed save, or half as much damage on a successful one."}]
}{
  :name "Red Dragon Wyrmling"
  :size :medium
  :type :dragon
  :alignment "chaotic evil"
  :armor-class 17
  :armor-notes "natural armor"
  :hit-points {:mean 75 :die-count 10 :die 8 :modifier 30}
  :speed "30 ft., climb 30 ft., fly 60 ft."

  :str 19
  :dex 10
  :con 17
  :int 12
  :wis 11
  :cha 15

  :saving-throws {:dex 2, :con 5, :wis 2, :cha 4}

  :skills {:perception 4, :stealth 2}
  :damage-immunities "fire"
  :senses "blindsight 10 ft., darkvision 60 ft., passive Perception 14"
  :languages "Draconic"
  :challenge 4

  :actions [{:name "Bite" :description "Melee Weapon Attack: +6 to hit, reach 5 ft., one target. Hit: 9 (1d10 + 4) piercing damage plus 3 (1d6) fire damage."}
            {:name "Fire Breath" :notes "Recharge 5–6" :description "The dragon exhales fire in a 15-foot cone. Each creature in that area must make a DC 13 Dexterity saving throw, taking 24 (7d6) fire damage on a failed save, or half as much damage on a successful one."}]
}{
  :name "Ancient White Dragon"
  :size :gargantuan
  :type :dragon
  :alignment "chaotic evil"
  :armor-class 20
  :armor-notes "natural armor"
  :hit-points {:mean 333 :die-count 18 :die 20 :modifier 144}
  :speed "40 ft., burrow 40 ft., fly 80 ft., swim 40 ft."

  :str 26
  :dex 10
  :con 26
  :int 10
  :wis 13
  :cha 14

  :saving-throws {:dex 6, :con 14, :wis 7, :cha 8}

  :skills {:perception 13, :stealth 6}
  :damage-immunities "cold"
  :senses "blindsight 60 ft., darkvision 120 ft., passive Perception 23"
  :languages "Common, Draconic"
  :challenge 20

  :traits [{:name "Ice Walk" :description "The dragon can move across and climb icy surfaces without needing to make an ability check. Additionally, difficult terrain composed of ice or snow doesn't cost it extra moment."}
           {:name "Legendary Resistance" :notes "3/Day" :description "If the dragon fails a saving throw, it can choose to succeed instead."}]

  :actions [{:name "Multiattack" :description "The dragon can use its Frightful Presence. It then makes three attacks: one with its bite and two with its claws."}
            {:name "Bite" :description "Melee Weapon Attack: +14 to hit, reach 15 ft., one target. Hit: 19 (2d10 + 8) piercing damage plus 9 (2d8) cold damage."}
            {:name "Claw" :description "Melee Weapon Attack: +14 to hit, reach 10 ft., one target. Hit: 15 (2d6 + 8) slashing damage."}
            {:name "Tail" :description "Melee Weapon Attack: +14 to hit, reach 20 ft., one target. Hit: 17 (2d8 + 8) bludgeoning damage."}
            {:name "Frightful Presence" :description "Each creature of the dragon's choice that is within 120 feet of the dragon and aware of it must succeed on a DC 16 Wisdom saving throw or become frightened for 1 minute. A creature can repeat the saving throw at the end of each of its turns, ending the effect on itself on a success. If a creature's saving throw is successful or the effect ends for it, the creature is immune to the dragon's Frightful Presence for the next 24 hours."}
            {:name "Cold Breath" :notes "Recharge 5–6" :description "The dragon exhales an icy blast in a 90-foot cone. Each creature in that area must make a DC 22 Constitution saving throw, taking 72 (16d8) cold damage on a failed save, or half as much damage on a successful one."}]

  :legendary-actions {:description "The dragon can take 3 legendary actions, choosing from the options below. Only one legendary action option can be used at a time and only at the end of another creature's turn. The dragon regains spent legendary actions at the start of its turn."
                      :actions [{:name "Detect" :description "The dragon makes a Wisdom (Perception) check."}
                                {:name "Tail Attack" :description "The dragon makes a tail attack."}
                                {:name "Wing Attack" :notes "Costs 2 Actions" :description "The dragon beats its wings. Each creature within 15 feet of the dragon must succeed on a DC 22 Dexterity saving throw or take 15 (2d6 + 8) bludgeoning damage and be knocked prone. The dragon can then fly up to half its flying speed."}]}
}{
  :name "Adult White Dragon"
  :size :huge
  :type :dragon
  :alignment "chaotic evil"
  :armor-class 18
  :armor-notes "natural armor"
  :hit-points {:mean 200 :die-count 16 :die 12 :modifier 96}
  :speed "40 ft., burrow 30 ft., fly 80 ft., swim 40 ft."

  :str 22
  :dex 10
  :con 22
  :int 8
  :wis 12
  :cha 12

  :saving-throws {:dex 5, :con 11, :wis 6, :cha 6}

  :skills {:perception 11, :stealth 5}
  :damage-immunities "cold"
  :senses "blindsight 60 ft., darkvision 120 ft., passive Perception 21"
  :languages "Common, Draconic"
  :challenge 13

  :traits [{:name "Ice Walk" :description "The dragon can move across and climb icy surfaces without needing to make an ability check. Additionally, difficult terrain composed of ice or snow doesn't cost it extra moment."}
           {:name "Legendary Resistance" :notes "3/Day" :description "If the dragon fails a saving throw, it can choose to succeed instead."}]

  :actions [{:name "Multiattack" :description "The dragon can use its Frightful Presence. It then makes three attacks: one with its bite and two with its claws."}
            {:name "Bite" :description "Melee Weapon Attack: +11 to hit, reach 10 ft., one target. Hit: 17 (2d10 + 6) piercing damage plus 4 (1d8) cold damage."}
            {:name "Claw" :description "Melee Weapon Attack: +11 to hit, reach 5 ft., one target. Hit: 13 (2d6 + 6) slashing damage."}
            {:name "Tail" :description "Melee Weapon Attack: +11 to hit, reach 15 ft., one target. Hit: 15 (2d8 + 6) bludgeoning damage."}
            {:name "Frightful Presence" :description "Each creature of the dragon's choice that is within 120 feet of the dragon and aware of it must succeed on a DC 14 Wisdom saving throw or become frightened for 1 minute. A creature can repeat the saving throw at the end of each of its turns, ending the effect on itself on a success. If a creature's saving throw is successful or the effect ends for it, the creature is immune to the dragon's Frightful Presence for the next 24 hours."}
            {:name "Cold Breath" :notes "Recharge 5–6" :description "The dragon exhales an icy blast in a 60-foot cone. Each creature in that area must make a DC 19 Constitution saving throw, taking 54 (12d8) cold damage on a failed save, or half as much damage on a successful one."}]

  :legendary-actions {:description "The dragon can take 3 legendary actions, choosing from the options below. Only one legendary action option can be used at a time and only at the end of another creature's turn. The dragon regains spent legendary actions at the start of its turn."
                      :actions [{:name "Detect" :description "The dragon makes a Wisdom (Perception) check."}
                                {:name "Tail Attack" :description "The dragon makes a tail attack."}
                                {:name "Wing Attack" :notes "Costs 2 Actions" :description "The dragon beats its wings. Each creature within 10 feet of the dragon"}]}
}{
  :name "Young White Dragon"
  :size :large
  :type :dragon
  :alignment "chaotic evil"
  :armor-class 17
  :armor-notes "natural armor"
  :hit-points {:mean 133 :die-count 14 :die 10 :modifier 56}
  :speed "40 ft., burrow 20 ft., fly 80 ft., swim 40 ft."

  :str 18
  :dex 10
  :con 18
  :int 6
  :wis 11
  :cha 12

  :saving-throws {:dex 3, :con 7, :wis 3, :cha 4}

  :skills {:perception 6, :stealth 3}
  :damage-immunities "cold"
  :senses "blindsight 30 ft., darkvision 120 ft., passive Perception 16"
  :languages "Common, Draconic"
  :challenge 6

  :traits [{:name "Ice Walk" :description "The dragon can move across and climb icy surfaces without needing to make an ability check. Additionally, difficult terrain composed of ice or snow doesn't cost it extra moment."}]

  :actions [{:name "Multiattack" :description "The dragon makes three attacks: one with its bite and two with its claws."}
            {:name "Bite" :description "Melee Weapon Attack: +7 to hit, reach 10 ft., one target. Hit: 15 (2d10 + 4) piercing damage plus 4 (1d8) cold damage."}
            {:name "Claw" :description "Melee Weapon Attack: +7 to hit, reach 5 ft., one target. Hit: 11 (2d6 + 4) slashing damage."}
            {:name "Cold Breath" :notes "Recharge 5–6" :description "The dragon exhales an icy blast in a 30-foot cone. Each creature in that area must make a DC 15 Constitution saving throw, taking 45 (10d8) cold damage on a failed save, or half as much damage on a successful one."}]
}{
  :name "White Dragon Wyrmling"
  :size :medium
  :type :dragon
  :alignment "chaotic evil"
  :armor-class 16
  :armor-notes "natural armor"
  :hit-points {:mean 32 :die-count 5 :die 8 :modifier 10}
  :speed "30 ft., burrow 15 ft., fly 60 ft., swim 30 ft."

  :str 14
  :dex 10
  :con 14
  :int 5
  :wis 10
  :cha 11

  :saving-throws {:dex 2, :con 4, :wis 2, :cha 2}

  :skills {:perception 4, :stealth 2}
  :damage-immunities "cold"
  :senses "blindsight 10 ft., darkvision 60 ft., passive Perception 14"
  :languages "Draconic"
  :challenge 2

  :actions [{:name "Bite" :description "Melee Weapon Attack: +4 to hit, reach 5 ft., one target. Hit: 7 (1d10 + 2) piercing damage plus 2 (1d4) cold damage."}
            {:name "Cold Breath" :notes "Recharge 5–6" :description "The dragon exhales an icy blast of hail in a 15-foot cone. Each creature in that area must make a DC 12 Constitution saving throw, taking 22 (5d8) cold damage on a failed save, or half as much damage on a successful one."}]
}{
  :name "Ancient Brass Dragon"
  :size :gargantuan
  :type :dragon
  :alignment "chaotic good"
  :armor-class 20
  :armor-notes "natural armor"
  :hit-points {:mean 297 :die-count 17 :die 20 :modifier 119}
  :speed "40 ft., burrow 40 ft., fly 80 ft."

  :str 27
  :dex 10
  :con 25
  :int 16
  :wis 15
  :cha 19

  :saving-throws {:dex 6, :con 13, :wis 8, :cha 10}

  :skills {:history 9, :perception 14, :persuasion 10}
  :stealth 6
  :damage-immunities "fire"
  :senses "blindsight 60 ft., darkvision 120 ft., passive Perception 24"
  :languages "Common, Draconic"
  :challenge 20

  :traits [{:name "Legendary Resistance" :notes "3/Day" :description "If the dragon fails a saving throw, it can choose to succeed instead."}]

  :actions [{:name "Multiattack" :description "The dragon can use its Frightful Presence. It then makes three attacks: one with its bite and two with its claws."}
            {:name "Bite" :description "Melee Weapon Attack: +14 to hit, reach 15 ft., one target. Hit: 19 (2d10 + 8) piercing damage."}
            {:name "Claw" :description "Melee Weapon Attack: +14 to hit, reach 10 ft., one target. Hit: 15 (2d6 + 8) slashing damage."}
            {:name "Tail" :description "Melee Weapon Attack: +14 to hit, reach 20 ft., one target. Hit: 17 (2d8 + 8) bludgeoning damage."}
            {:name "Frightful Presence" :description "Each creature of the dragon's choice that is within 120 feet of the dragon and aware of it must succeed on a DC 18 Wisdom saving throw or become frightened for 1 minute. A creature can repeat the saving throw at the end of each of its turns, ending the effect on itself on a success. If a creature's saving throw is successful or the effect ends for it, the creature is immune to the dragon's Frightful Presence for the next 24 hours."}
            {:name "Breath Weapons" :notes "Recharge 5–6" :description "The dragon uses one of the following breath weapons:"}
            {:name "Fire Breath" :description "The dragon exhales fire in an 90-foot line that is 10 feet wide. Each creature in that line must make a DC 21 Dexterity saving throw, taking 56 (16d6) fire damage on a failed save, or half as much damage on a successful one."}
            {:name "Sleep Breath" :description "The dragon exhales sleep gas in a 90-foot cone. Each creature in that area must succeed on a DC 21 Constitution saving throw or fall unconscious for 10 minutes. This effect ends for a creature if the creature takes damage or someone uses an action to wake it."}
            {:name "Change Shape" :description "The dragon magically polymorphs into a humanoid or beast that has a challenge rating no higher than its own, or back into its true form. It reverts to its true form if it dies. Any equipment it is wearing or carrying is absorbed or borne by the new form (the dragon's choice).
In a new form, the dragon retains its alignment, hit points, Hit Dice, ability to speak, proficiencies, Legendary Resistance, lair actions, and Intelligence, Wisdom, and Charisma scores, as well as this action. Its statistics and capabilities are otherwise replaced by those of the new form, except any class features or legendary actions of that form."}]

  :legendary-actions {:description "The dragon can take 3 legendary actions, choosing from the options below. Only one legendary action option can be used at a time and only at the end of another creature's turn. The dragon regains spent legendary actions at the start of its turn."
                      :actions [{:name "Detect" :description "The dragon makes a Wisdom (Perception) check."}
                                {:name "Tail Attack" :description "The dragon makes a tail attack."}
                                {:name "Wing Attack" :notes "Costs 2 Actions" :description "The dragon beats its wings. Each creature within 15 feet of the dragon must succeed on a DC 22 Dexterity saving throw or take 15 (2d6 + 8) bludgeoning damage and be knocked prone. The dragon can then fly up to half its flying speed."}]}
}{
  :name "Adult Brass Dragon"
  :size :huge
  :type :dragon
  :alignment "chaotic good"
  :armor-class 18
  :armor-notes "natural armor"
  :hit-points {:mean 172 :die-count 15 :die 12 :modifier 75}
  :speed "40 ft., burrow 30 ft., fly 80 ft."

  :str 23
  :dex 10
  :con 21
  :int 14
  :wis 13
  :cha 17

  :saving-throws {:dex 5, :con 10, :wis 6, :cha 8}

  :skills {:history 7, :perception 11, :persuasion 8, :stealth 5}
  :damage-immunities "fire"
  :senses "blindsight 60 ft., darkvision 120 ft., passive Perception 21"
  :languages "Common, Draconic"
  :challenge 13

  :traits [{:name "Legendary Resistance" :notes "3/Day" :description "If the dragon fails a saving throw, it can choose to succeed instead."}]

  :actions [{:name "Multiattack" :description "The dragon can use its Frightful Presence. It then makes three attacks: one with its bite and two with its claws."}
            {:name "Bite" :description "Melee Weapon Attack: +11 to hit, reach 10 ft., one target. Hit: 17 (2d10 + 6) piercing damage."}
            {:name "Claw" :description "Melee Weapon Attack: +11 to hit, reach 5 ft., one target. Hit: 13 (2d6 + 6) slashing damage."}
            {:name "Tail" :description "Melee Weapon Attack: +11 to hit, reach 15 ft., one target. Hit: 15 (2d8 + 6) bludgeoning damage."}
            {:name "Frightful Presence" :description "Each creature of the dragon's choice that is within 120 feet of the dragon and aware of it must succeed on a DC 16 Wisdom saving throw or become frightened for 1 minute. A creature can repeat the saving throw at the end of each of its turns, ending the effect on itself on a success. If a creature's saving throw is successful or the effect ends for it, the creature is immune to the dragon's Frightful Presence for the next 24 hours."}
            {:name "Breath Weapons" :notes "Recharge 5–6" :description "The dragon uses one of the following breath weapons."}
            {:name "Fire Breath" :description "The dragon exhales fire in an 60-foot line that is 5 feet wide. Each creature in that line must make a DC 18 Dexterity saving throw, taking 45 (13d6) fire damage on a failed save, or half as much damage on a successful one."}
            {:name "Sleep Breath" :description "The dragon exhales sleep gas in a 60-foot cone. Each creature in that area must succeed on a DC 18 Constitution saving throw or fall unconscious for 10 minutes. This effect ends for a creature if the creature takes damage or someone uses an action to wake it."}]

  :legendary-actions {:description "The dragon can take 3 legendary actions, choosing from the options below. Only one legendary action option can be used at a time and only at the end of another creature's turn. The dragon regains spent legendary actions at the start of its turn."
                      :actions [{:name "Detect" :description "The dragon makes a Wisdom (Perception) check."}
                                {:name "Tail Attack" :description "The dragon makes a tail attack."}
                                {:name "Wing Attack" :notes "Costs 2 Actions" :description "The dragon beats its wings. Each creature within 10 feet of the dragon must succeed on a DC 19 Dexterity saving throw or take 13 (2d6 + 6) bludgeoning damage and be knocked prone. The dragon can then fly up to half its flying speed."}]}
}{
  :name "Young Brass Dragon"
  :size :large
  :type :dragon
  :alignment "chaotic good"
  :armor-class 17
  :armor-notes "natural armor"
  :hit-points {:mean 110 :die-count 13 :die 10 :modifier 39}
  :speed "40 ft., burrow 20 ft., fly 80 ft."

  :str 19
  :dex 10
  :con 17
  :int 12
  :wis 11
  :cha 15

  :saving-throws {:dex 3, :con 6, :wis 3, :cha 5}

  :skills {:perception 6, :persuasion 5, :stealth 3}
  :damage-immunities "fire"
  :senses "blindsight 30 ft., darkvision 120 ft., passive Perception 16"
  :languages "Common, Draconic"
  :challenge 6

  :actions [{:name "Multiattack" :description "The dragon makes three attacks: one with its bite and two with its claws."}
            {:name "Bite" :description "Melee Weapon Attack: +7 to hit, reach 10 ft., one target. Hit: 15 (2d10 + 4) piercing damage."}
            {:name "Claw" :description "Melee Weapon Attack: +7 to hit, reach 5 ft., one target. Hit: 11 (2d6 + 4) slashing damage."}
            {:name "Breath Weapons" :notes "Recharge 5–6" :description "The dragon uses one of the following breath weapons."}
            {:name "Fire Breath" :description "The dragon exhales fire in a 40-foot line that is 5 feet wide. Each creature in that line must make a DC 14 Dexterity saving throw, taking 42 (12d6) fire damage on a failed save, or half as much damage on a successful one."}
            {:name "Sleep Breath" :description "The dragon exhales sleep gas in a 30-foot cone. Each creature in that area must succeed on a DC 14 Constitution saving throw or fall unconscious for 5 minutes. This effect ends for a creature if the creature takes damage or someone uses an action to wake it."}]
}{
  :name "Brass Dragon Wyrmling"
  :size :medium
  :type :dragon
  :alignment "chaotic good"
  :armor-class 16
  :armor-notes "natural armor"
  :hit-points {:mean 16 :die-count 3 :die 8 :modifier 3}
  :speed "30 ft., burrow 15 ft., fly 60 ft."

  :str 15
  :dex 10
  :con 13
  :int 10
  :wis 11
  :cha 13

  :saving-throws {:dex 2, :con 3, :wis 2, :cha 3}

  :skills {:perception 4, :stealth 2}
  :damage-immunities "fire"
  :senses "blindsight 10 ft., darkvision 60 ft., passive Perception 14"
  :languages "Draconic"
  :challenge 1

  :actions [{:name "Bite" :description "Melee Weapon Attack: +4 to hit, reach 5 ft., one target. Hit: 7 (1d10 + 2) piercing damage."}
            {:name "Breath Weapons" :notes "Recharge 5–6" :description "The dragon uses one of the following breath weapons."}
            {:name "Fire Breath" :description "The dragon exhales fire in an 20-foot line that is 5 feet wide. Each creature in that line must make a DC 11 Dexterity saving throw, taking 14 (4d6) fire damage on a failed save, or half as much damage on a successful one."}
            {:name "Sleep Breath" :description "The dragon exhales sleep gas in a 15-foot cone. Each creature in that area must succeed on a DC 11 Constitution saving throw or fall unconscious for 1 minute. This effect ends for a creature if the creature takes damage or someone uses an action to wake it."}]
}{
  :name "Ancient Bronze Dragon"
  :size :gargantuan
  :type :dragon
  :alignment "lawful good"
  :armor-class 22
  :armor-notes "natural armor"
  :hit-points {:mean 444 :die-count 24 :die 20 :modifier 192}
  :speed "40 ft., fly 80 ft., swim 40 ft."

  :str 29
  :dex 10
  :con 27
  :int 18
  :wis 17
  :cha 21

  :saving-throws {:dex 7, :con 15, :wis 10, :cha 12}

  :skills {:insight 10, :perception 17, :stealth 7}
  :damage-immunities "lightning"
  :senses "blindsight 60 ft., darkvision 120 ft., passive Perception 27"
  :languages "Common, Draconic"
  :challenge 22

  :traits [{:name "Amphibious" :description "The dragon can breathe air and water."}
           {:name "Legendary Resistance" :notes "3/Day" :description "If the dragon fails a saving throw, it can choose to succeed instead."}]

  :actions [{:name "Multiattack" :description "The dragon can use its Frightful Presence. It then makes three attacks: one with its bite and two with its claws."}
            {:name "Bite" :description "Melee Weapon Attack: +16 to hit, reach 15 ft., one target. Hit: 20 (2d10 + 9) piercing damage."}
            {:name "Claw" :description "Melee Weapon Attack: +16 to hit, reach 10 ft., one target. Hit: 16 (2d6 + 9) slashing damage."}
            {:name "Tail" :description "Melee Weapon Attack: +16 to hit, reach 20 ft., one target. Hit: 18 (2d8 + 9) bludgeoning damage."}
            {:name "Frightful Presence" :description "Each creature of the dragon's choice that is within 120 feet of the dragon and aware of it must succeed on a DC 20 Wisdom saving throw or become frightened for 1 minute. A creature can repeat the saving throw at the end of each of its turns, ending the effect on itself on a success. If a creature's saving throw is successful or the effect ends for it, the creature is immune to the dragon's Frightful Presence for the next 24 hours."}
            {:name "Breath Weapons" :notes "Recharge 5–6" :description "The dragon uses one of the following breath weapons."}
            {:name "Lightning Breath" :description "The dragon exhales lightning in a 120-foot line that is 10 feet wide. Each creature in that line must make a DC 23 Dexterity saving throw, taking 88 (16d10) lightning damage on a failed save, or half as much damage on a successful one."}
            {:name "Repulsion Breath" :description "The dragon exhales repulsion energy in a 30-foot cone. Each creature in that area must succeed on a DC 23 Strength saving throw. On a failed save, the creature is pushed 60 feet away from the dragon."}
            {:name "Change Shape" :description "The dragon magically polymorphs into a humanoid or beast that has a challenge rating no higher than its own, or back into its true form. It reverts to its true form if it dies. Any equipment it is wearing or carrying is absorbed or borne by the new form (the dragon's choice).
In a new form, the dragon retains its alignment, hit points, Hit Dice, ability to speak, proficiencies, Legendary Resistance, lair actions, and Intelligence, Wisdom, and Charisma scores, as well as this action. Its statistics and capabilities are otherwise replaced by those of the new form, except any class features or legendary actions of that form."}]

  :legendary-actions {:description "The dragon can take 3 legendary actions, choosing from the options below. Only one legendary action option can be used at a time and only at the end of another creature's turn. The dragon regains spent legendary actions at the start of its turn."
                      :actions [{:name "Detect" :description "The dragon makes a Wisdom (Perception) check."}
                                {:name "Tail Attack" :description "The dragon makes a tail attack."}
                                {:name "Wing Attack" :notes "Costs 2 Actions" :description "The dragon beats its wings. Each creature within 15 feet of the dragon must succeed on a DC 24 Dexterity saving throw or take 16 (2d6 + 9) bludgeoning damage and be knocked prone. The dragon can then fly up to half its flying speed."}]}
}{
  :name "Adult Bronze Dragon"
  :size :huge
  :type :dragon
  :alignment "lawful good"
  :armor-class 19
  :armor-notes "natural armor"
  :hit-points {:mean 212 :die-count 17 :die 12 :modifier 102}
  :speed "40 ft., fly 80 ft., swim 40 ft."

  :str 25
  :dex 10
  :con 23
  :int 16
  :wis 15
  :cha 19

  :saving-throws {:dex 5, :con 11, :wis 7, :cha 9}

  :skills {:insight 7, :perception 12, :stealth 5}
  :damage-immunities "lightning"
  :senses "blindsight 60 ft., darkvision 120 ft., passive Perception 22"
  :languages "Common, Draconic"
  :challenge 15

  :traits [{:name "Amphibious" :description "The dragon can breathe air and water."}
           {:name "Legendary Resistance" :notes "3/Day" :description "If the dragon fails a saving throw, it can choose to succeed instead."}]

  :actions [{:name "Multiattack" :description "The dragon can use its Frightful Presence. It then makes three attacks: one with its bite and two with its claws."}
            {:name "Bite" :description "Melee Weapon Attack: +12 to hit, reach 10 ft., one target. Hit: 18 (2d10 + 7) piercing damage."}
            {:name "Claw" :description "Melee Weapon Attack: +12 to hit, reach 5 ft., one target. Hit: 14 (2d6 + 7) slashing damage."}
            {:name "Tail" :description "Melee Weapon Attack: +12 to hit, reach 15 ft., one target. Hit: 16 (2d8 + 7) bludgeoning damage."}
            {:name "Frightful Presence" :description "Each creature of the dragon's choice that is within 120 feet of the dragon and aware of it must succeed on a DC 17 Wisdom saving throw or become frightened for 1 minute. A creature can repeat the saving throw at the end of each of its turns, ending the effect on itself on a success. If a creature's saving throw is successful or the effect ends for it, the creature is immune to the dragon's Frightful Presence for the next 24 hours."}
            {:name "Breath Weapons" :notes "Recharge 5–6" :description "The dragon uses one of the following breath weapons."}
            {:name "Lightning Breath" :description "The dragon exhales lightning in a 90- foot line that is 5 feet wide. Each creature in that line must make a DC 19 Dexterity saving throw, taking 66 (12d10) lightning damage on a failed save, or half as much damage on a successful one."}
            {:name "Repulsion Breath" :description "The dragon exhales repulsion energy in a 30-foot cone. Each creature in that area must succeed on a DC 19 Strength saving throw. On a failed save, the creature is pushed 60 feet away from the dragon."}
            {:name "Change Shape" :description "The dragon magically polymorphs into a humanoid or beast that has a challenge rating no higher than its own, or back into its true form. It reverts to its true form if it dies. Any equipment it is wearing or carrying is absorbed or borne by the new form (the dragon's choice).
In a new form, the dragon retains its alignment, hit points, Hit Dice, ability to speak, proficiencies, Legendary Resistance, lair actions, and Intelligence, Wisdom, and Charisma scores, as well as this action. Its statistics and capabilities are otherwise replaced by those of the new form, except any class features or legendary actions of that form."}]

  :legendary-actions {:description "The dragon can take 3 legendary actions, choosing from the options below. Only one legendary action option can be used at a time and only at the end of another creature's turn. The dragon regains spent legendary actions at the start of its turn."
                      :actions [{:name "Detect" :description "The dragon makes a Wisdom (Perception) check."}
                                {:name "Tail Attack" :description "The dragon makes a tail attack."}
                                {:name "Wing Attack" :notes "Costs 2 Actions" :description "The dragon beats its wings. Each creature within 10 feet of the dragon must succeed on a DC 20 Dexterity saving throw or take 14 (2d6 + 7) bludgeoning damage and be knocked prone. The dragon can then fly up to half its flying speed."}]}
}{
  :name "Young Bronze Dragon"
  :size :large
  :type :dragon
  :alignment "lawful good"
  :armor-class 18
  :armor-notes "natural armor"
  :hit-points {:mean 142 :die-count 15 :die 10 :modifier 60}
  :speed "40 ft., fly 80 ft., swim 40 ft."

  :str 21
  :dex 10
  :con 19
  :int 14
  :wis 13
  :cha 17

  :saving-throws {:dex 3, :con 7, :wis 4, :cha 6}

  :skills {:insight 4, :perception 7, :stealth 3}
  :damage-immunities "lightning"
  :senses "blindsight 30 ft., darkvision 120 ft., passive Perception 17"
  :languages "Common, Draconic"
  :challenge 8

  :traits [{:name "Amphibious" :description "The dragon can breathe air and water."}]

  :actions [{:name "Multiattack" :description "The dragon makes three attacks: one with its bite and two with its claws."}
            {:name "Bite" :description "Melee Weapon Attack: +8 to hit, reach 10 ft., one target. Hit: 16 (2d10 + 5) piercing damage."}
            {:name "Claw" :description "Melee Weapon Attack: +8 to hit, reach 5 ft., one target. Hit: 12 (2d6 + 5) slashing damage."}
            {:name "Breath Weapons" :notes "Recharge 5–6" :description "The dragon uses one of the following breath weapons."}
            {:name "Lightning Breath" :description "The dragon exhales lightning in a 60- foot line that is 5 feet wide. Each creature in that line must make a DC 15 Dexterity saving throw, taking 55 (10d10) lightning damage on a failed save, or half as much damage on a successful one."}
            {:name "Repulsion Breath" :description "The dragon exhales repulsion energy in a 30-foot cone. Each creature in that area must succeed on a DC 15 Strength saving throw. On a failed save, the creature is pushed 40 feet away from the dragon."}]
}{
  :name "Bronze Dragon Wyrmling"
  :size :medium
  :type :dragon
  :alignment "lawful good"
  :armor-class 17
  :armor-notes "natural armor"
  :hit-points {:mean 32 :die-count 5 :die 8 :modifier 10}
  :speed "30 ft., fly 60 ft., swim 30 ft."

  :str 17
  :dex 10
  :con 15
  :int 12
  :wis 11
  :cha 15

  :saving-throws {:dex 2, :con 4, :wis 2, :cha 4}

  :skills {:perception 4, :stealth 2}
  :damage-immunities "lightning"
  :senses "blindsight 10 ft., darkvision 60 ft., passive Perception 14"
  :languages "Draconic"
  :challenge 2

  :traits [{:name "Amphibious" :description "The dragon can breathe air and water."}]

  :actions [{:name "Bite" :description "Melee Weapon Attack: +5 to hit, reach 5 ft., one target. Hit: 8 (1d10 + 3) piercing damage."}
            {:name "Breath Weapons" :notes "Recharge 5–6" :description "The dragon uses one of the following breath weapons."}
            {:name "Lightning Breath" :description "The dragon exhales lightning in a 40- foot line that is 5 feet wide. Each creature in that line must make a DC 12 Dexterity saving throw, taking 16 (3d10) lightning damage on a failed save, or half as much damage on a successful one."}
            {:name "Repulsion Breath" :description "The dragon exhales repulsion energy in a 30-foot cone. Each creature in that area must succeed on a DC 12 Strength saving throw. On a failed save, the creature is pushed 30 feet away from the dragon."}]
}{
  :name "Ancient Copper Dragon"
  :size :gargantuan
  :type :dragon
  :alignment "chaotic good"
  :armor-class 21
  :armor-notes "natural armor"
  :hit-points {:mean 350 :die-count 20 :die 20 :modifier 140}
  :speed "40 ft., climb 40 ft., fly 80 ft."

  :str 27
  :dex 12
  :con 25
  :int 20
  :wis 17
  :cha 19

  :saving-throws {:dex 8, :con 14, :wis 10, :cha 11}

  :skills {:deception 11, :perception 17, :stealth 8}
  :damage-immunities "acid"
  :senses "blindsight 60 ft., darkvision 120 ft., passive Perception 27"
  :languages "Common, Draconic"
  :challenge 21

  :traits [{:name "Legendary Resistance" :notes "3/Day" :description "If the dragon fails a saving throw, it can choose to succeed instead."}]

  :actions [{:name "Multiattack" :description "The dragon can use its Frightful Presence. It then makes three attacks: one with its bite and two with its claws."}
            {:name "Bite" :description "Melee Weapon Attack: +15 to hit, reach 15 ft., one target. Hit: 19 (2d10 + 8) piercing damage."}
            {:name "Claw" :description "Melee Weapon Attack: +15 to hit, reach 10 ft., one target. Hit: 15 (2d6 + 8) slashing damage."}
            {:name "Tail" :description "Melee Weapon Attack: +15 to hit, reach 20 ft., one target. Hit: 17 (2d8 + 8) bludgeoning damage."}
            {:name "Frightful Presence" :description "Each creature of the dragon's choice that is within 120 feet of the dragon and aware of it must succeed on a DC 19 Wisdom saving throw or become frightened for 1 minute. A creature can repeat the saving throw at the end of each of its turns, ending the effect on itself on a success. If a creature's saving throw is successful or the effect ends for it, the creature is immune to the dragon's Frightful Presence for the next 24 hours."}
            {:name "Breath Weapons" :notes "Recharge 5–6" :description "The dragon uses one of the following breath weapons."}
            {:name "Acid Breath" :description "The dragon exhales acid in an 90-foot line that is 10 feet wide. Each creature in that line must
In a new form, the dragon retains its alignment, hit points, Hit Dice, ability to speak, proficiencies, Legendary Resistance, lair actions, and Intelligence, Wisdom, and Charisma scores, as well as this action. Its statistics and capabilities are otherwise replaced by those of the new form, except any class features or legendary actions of that form."}]

  :legendary-actions {:description "The dragon can take 3 legendary actions, choosing from the options below. Only one legendary action option can be used at a time and only at the end of another creature's turn. The dragon regains spent legendary actions at the start of its turn."
                      :actions [{:name "Detect" :description "The dragon makes a Wisdom (Perception) check."}
                                {:name "Tail Attack" :description "The dragon makes a tail attack."}
                                {:name "Wing Attack" :notes "Costs 2 Actions" :description "The dragon beats its wings. Each creature within 15 feet of the dragon must succeed on a DC 23 Dexterity saving throw or take 15 (2d6 + 8) bludgeoning damage and be knocked prone. The dragon can then fly up to half its flying speed."}]}
}{
  :name "Adult Copper Dragon"
  :size :huge
  :type :dragon
  :alignment "chaotic good"
  :armor-class 18
  :armor-notes "natural armor"
  :hit-points {:mean 184 :die-count 16 :die 12 :modifier 80}
  :speed "40 ft., climb 40 ft., fly 80 ft."

  :str 23
  :dex 12
  :con 21
  :int 18
  :wis 15
  :cha 17

  :saving-throws {:dex 6, :con 10, :wis 7, :cha 8}

  :skills {:deception 8, :perception 12, :stealth 6}
  :damage-immunities "acid"
  :senses "blindsight 60 ft., darkvision 120 ft., passive Perception 22"
  :languages "Common, Draconic"
  :challenge 14

  :traits [{:name "Legendary Resistance" :notes "3/Day" :description "If the dragon fails a saving throw, it can choose to succeed instead."}]

  :actions [{:name "Multiattack" :description "The dragon can use its Frightful Presence. It then makes three attacks: one with its bite and two with its claws."}
            {:name "Bite" :description "Melee Weapon Attack: +11 to hit, reach 10 ft., one target. Hit: 17 (2d10 + 6) piercing damage."}
            {:name "Claw" :description "Melee Weapon Attack: +11 to hit, reach 5 ft., one target. Hit: 13 (2d6 + 6) slashing damage."}
            {:name "Tail" :description "Melee Weapon Attack: +11 to hit, reach 15 ft., one target. Hit: 15 (2d8 + 6) bludgeoning damage."}
            {:name "Frightful Presence" :description "Each creature of the dragon's choice that is within 120 feet of the dragon and aware"}
            {:name "Breath Weapons" :notes "Recharge 5–6" :description "The dragon uses one of the following breath weapons."}
            {:name "Acid Breath" :description "The dragon exhales acid in an 60-foot line that is 5 feet wide. Each creature in that line must make a DC 18 Dexterity saving throw, taking 54 (12d8) acid damage on a failed save, or half as much damage on a successful one."}
            {:name "Slowing Breath" :description "The dragon exhales gas in a 60-foot cone. Each creature in that area must succeed on a DC 18 Constitution saving throw. On a failed save, the creature can't use reactions, its speed is halved, and it can't make more than one attack on its turn. In addition, the creature can use either an action or a bonus action on its turn, but not both. These effects last for 1 minute. The creature can repeat the saving throw at the end of each of its turns, ending the effect on itself with a successful save."}]

  :legendary-actions {:description "The dragon can take 3 legendary actions, choosing from the options below. Only one legendary action option can be used at a time and only at the end of another creature's turn. The dragon regains spent legendary actions at the start of its turn."
                      :actions [{:name "Detect" :description "The dragon makes a Wisdom (Perception) check."}
                                {:name "Tail Attack" :description "The dragon makes a tail attack."}
                                {:name "Wing Attack" :notes "Costs 2 Actions" :description "The dragon beats its wings. Each creature within 10 feet of the dragon must succeed on a DC 19 Dexterity saving throw or take 13 (2d6 + 6) bludgeoning damage and be knocked prone. The dragon can then fly up to half its flying speed."}]}
}{
  :name "Young Copper Dragon"
  :size :large
  :type :dragon
  :alignment "chaotic good"
  :armor-class 17
  :armor-notes "natural armor"
  :hit-points {:mean 119 :die-count 14 :die 10 :modifier 42}
  :speed "40 ft., climb 40 ft., fly 80 ft."

  :str 19
  :dex 12
  :con 17
  :int 16
  :wis 13
  :cha 15

  :saving-throws {:dex 4, :con 6, :wis 4, :cha 5}
  :skills {:deception 5, :perception 7, :stealth 4}
  :damage-immunities "acid"
  :senses "blindsight 30 ft., darkvision 120 ft., passive Perception 17"
  :languages "Common, Draconic"
  :challenge 7
  :actions [{:name "Multiattack" :description "The dragon makes three attacks: one with its bite and two with its claws."}
            {:name "Bite" :description "Melee Weapon Attack: +7 to hit, reach 10 ft., one target. Hit: 15 (2d10 + 4) piercing damage."}
            {:name "Claw" :description "Melee Weapon Attack: +7 to hit, reach 5 ft., one target. Hit: 11 (2d6 + 4) slashing damage."}
            {:name "Breath Weapons" :notes "Recharge 5–6" :description "The dragon uses one of the following breath weapons."}
            {:name "Acid Breath" :description "The dragon exhales acid in an 40-foot line that is 5 feet wide. Each creature in that line must make a DC 14 Dexterity saving throw, taking 40 (9d8) acid damage on a failed save, or half as much damage on a successful one."}
            {:name "Slowing Breath" :description "The dragon exhales gas in a 30-foot cone. Each creature in that area must succeed on a DC 14 Constitution saving throw. On a failed save, the creature can't use reactions, its speed is halved, and it can't make more than one attack on its turn. In addition, the creature can use either an action or a bonus action on its turn, but not both. These effects last for 1 minute. The creature can repeat the saving throw at the end of each of its turns, ending the effect on itself with a successful save."}]
}{
  :name "Copper Dragon Wyrmling"
  :size :medium
  :type :dragon
  :alignment "chaotic good"
  :armor-class 16
  :armor-notes "natural armor"
  :hit-points {:mean 22 :die-count 4 :die 8 :modifier 4}
  :speed "30 ft., climb 30 ft., fly 60 ft."

  :str 15
  :dex 12
  :con 13
  :int 14
  :wis 11
  :cha 13
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          
  :saving-throws {:dex 3, :con 3, :wis 2, :cha 3}
  :skills {:perception 4, :stealth 3}
  :damage-immunities "acid"
  :senses "blindsight 10 ft., darkvision 60 ft., passive Perception 14"
  :languages "Draconic"
  :challenge 1

  :actions [{:name "Bite" :description "Melee Weapon Attack: +4 to hit, reach 5 ft., one target. Hit: 7 (1d10 + 2) piercing damage."}
            {:name "Breath Weapons" :notes "Recharge 5–6" :description "The dragon uses one of the following breath weapons."}
            {:name "Acid Breath" :description "The dragon exhales acid in an 20-foot line that is 5 feet wide. Each creature in that line must make a DC 11 Dexterity saving throw, taking 18 (4d8) acid damage on a failed save, or half as much damage on a successful one."}
            {:name "Slowing Breath" :description "The dragon exhales gas in a 15-foot cone. Each creature in that area must succeed on a DC 11 Constitution saving throw. On a failed save, the creature can't use reactions, its speed is halved, and it can't make more than one attack on its turn. In addition, the creature can use either an action or a bonus action on its turn, but not both. These effects last for 1 minute. The creature can repeat the saving throw at the end of each of its turns, ending the effect on itself with a successful save."}]
}{
  :name "Ancient Gold Dragon"
  :size :gargantuan
  :type :dragon
  :alignment "lawful good"
  :armor-class 22
  :armor-notes "natural armor"
  :hit-points {:mean 546 :die-count 28 :die 20 :modifier 252}
  :speed "40 ft., fly 80 ft., swim 40 ft."

  :str 30
  :dex 14
  :con 29
  :int 18
  :wis 17
  :cha 28

  :saving-throws {:dex 9, :con 16, :wis 10, :cha 16}

  :skills {:insight 10, :perception 17, :persuasion 16}
  :stealth 9
  :damage-immunities "fire"
  :senses "blindsight 60 ft., darkvision 120 ft., passive Perception 27"
  :languages "Common, Draconic"
  :challenge 24

  :traits [{:name "Amphibious" :description "The dragon can breathe air and water."}
           {:name "Legendary Resistance" :notes "3/Day" :description "If the dragon fails a saving throw, it can choose to succeed instead."}]

  :actions [{:name "Multiattack" :description "The dragon can use its Frightful Presence. It then makes three attacks: one with its bite and two with its claws."}
            {:name "Bite" :description "Melee Weapon Attack: +17 to hit, reach 15 ft., one target. Hit: 21 (2d10 + 10) piercing damage."}
            {:name "Claw" :description "Melee Weapon Attack: +17 to hit, reach 10 ft., one target. Hit: 17 (2d6 + 10) slashing damage."}
            {:name "Tail" :description "Melee Weapon Attack: +17 to hit, reach 20 ft., one target. Hit: 19 (2d8 + 10) bludgeoning damage."}
            {:name "Frightful Presence" :description "Each creature of the dragon's choice that is within 120 feet of the dragon and aware of it must succeed on a DC 24 Wisdom saving throw or become frightened for 1 minute. A creature can repeat the saving throw at the end of each of its turns, ending the effect on itself on a success. If a creature's saving throw is successful or the effect ends for it, the creature is immune to the dragon's Frightful Presence for the next 24 hours."}
            {:name "Breath Weapons" :notes "Recharge 5–6" :description "The dragon uses one of the following breath weapons."}
            {:name "Fire Breath" :description "The dragon exhales fire in a 90-foot cone. Each creature in that area must make a DC 24 Dexterity saving throw, taking 71 (13d10) fire damage on a failed save, or half as much damage on a successful one."}
            {:name "Weakening Breath" :description "The dragon exhales gas in a 90-foot cone. Each creature in that area must succeed on a DC 24 Strength saving throw or have disadvantage on Strength-based attack rolls, Strength checks, and Strength saving throws for 1 minute. A creature can repeat the saving throw at the end of each of its turns, ending the effect on itself on a success."}
            {:name "Change Shape" :description "The dragon magically polymorphs into a humanoid or beast that has a challenge rating no higher than its own, or back into its true form. It reverts to its true form if it dies. Any equipment it is wearing or carrying is absorbed or borne by the new form (the dragon's choice).
In a new form, the dragon retains its alignment, hit points, Hit Dice, ability to speak, proficiencies, Legendary Resistance, lair actions, and Intelligence, Wisdom, and Charisma scores, as well as this action. Its statistics and capabilities are otherwise replaced by those of the new form, except any class features or legendary actions of that form."}]

  :legendary-actions {:description "The dragon can take 3 legendary actions, choosing from the options below. Only one legendary action option can be used at a time and only at the end of another creature's turn. The dragon regains spent legendary actions at the start of its turn."
                      :actions [{:name "Detect" :description "The dragon makes a Wisdom (Perception) check."}
                                {:name "Tail Attack" :description "The dragon makes a tail attack."}
                                {:name "Wing Attack" :notes "Costs 2 Actions" :description "The dragon beats its wings. Each creature within 15 feet of the dragon must succeed on a DC 25 Dexterity saving throw or take 17 (2d6 + 10) bludgeoning damage and be knocked prone. The dragon can then fly up to half its flying speed."}]}
}{
  :name "Adult Gold Dragon"
  :size :huge
  :type :dragon
  :alignment "lawful good"
  :armor-class 19
  :armor-notes "natural armor"
  :hit-points {:mean 256 :die-count 19 :die 12 :modifier 133}
  :speed "40 ft., fly 80 ft., swim 40 ft."

  :str 27
  :dex 14
  :con 25
  :int 16
  :wis 15
  :cha 24

  :saving-throws {:dex 8, :con 13, :wis 8, :cha 13}

  :skills {:insight 8, :perception 14, :persuasion 13}
  :stealth 8
  :damage-immunities "fire"
  :senses "blindsight 60 ft., darkvision 120 ft., passive Perception 24"
  :languages "Common, Draconic"
  :challenge 17

  :traits [{:name "Amphibious" :description "The dragon can breathe air and water."}
           {:name "Legendary Resistance" :notes "3/Day" :description "If the dragon fails a saving throw, it can choose to succeed instead."}]

  :actions [{:name "Multiattack" :description "The dragon can use its Frightful Presence. It then makes three attacks: one with its bite and two with its claws."}
            {:name "Bite" :description "Melee Weapon Attack: +14 to hit, reach 10 ft., one target. Hit: 19 (2d10 + 8) piercing damage."}
            {:name "Claw" :description "Melee Weapon Attack: +14 to hit, reach 5 ft., one target. Hit: 15 (2d6 + 8) slashing damage."}
            {:name "Tail" :description "Melee Weapon Attack: +14 to hit, reach 15 ft., one target. Hit: 17 (2d8 + 8) bludgeoning damage."}
            {:name "Frightful Presence" :description "Each creature of the dragon's choice that is within 120 feet of the dragon and aware of it must succeed on a DC 21 Wisdom saving throw or become frightened for 1 minute. A creature can repeat the saving throw at the end of each of its turns, ending the effect on itself on a success. If a creature's saving throw is successful or the effect ends for it, the creature is immune to the dragon's Frightful Presence for the next 24 hours."}
            {:name "Breath Weapons" :notes "Recharge 5–6" :description "The dragon uses one of the following breath weapons."}
            {:name "Fire Breath" :description "The dragon exhales fire in a 60-foot cone. Each creature in that area must make a DC 21 Dexterity saving throw, taking 66 (12d10) fire damage on a failed save, or half as much damage on a successful one."}
            {:name "Weakening Breath" :description "The dragon exhales gas in a 60-foot cone. Each creature in that area must succeed on a DC 21 Strength saving throw or have disadvantage on Strength-based attack rolls, Strength checks, and Strength saving throws for 1 minute. A creature can repeat the saving throw at the end of each of its turns, ending the effect on itself on a success."}
            {:name "Change Shape" :description "The dragon magically polymorphs into a humanoid or beast that has a challenge rating no higher than its own, or back into its true form. It reverts to its true form if it dies. Any equipment it is wearing or carrying is absorbed or borne by the new form (the dragon's choice).
In a new form, the dragon retains its alignment, hit points, Hit Dice, ability to speak, proficiencies, Legendary Resistance, lair actions, and Intelligence, Wisdom, and Charisma scores, as well as this action. Its statistics and capabilities are otherwise replaced by those of the new form, except any class features or legendary actions of that form."}]

  :legendary-actions {:description "The dragon can take 3 legendary actions, choosing from the options below. Only one legendary action option can be used at a time and only at the end of another creature's turn. The dragon regains spent legendary actions at the start of its turn."
                      :actions [{:name "Detect" :description "The dragon makes a Wisdom (Perception) check."}
                                {:name "Tail Attack" :description "The dragon makes a tail attack."}
                                {:name "Wing Attack" :notes "Costs 2 Actions" :description "The dragon beats its wings. Each creature within 10 feet of the dragon must succeed on a DC 22 Dexterity saving throw or take 15 (2d6 + 8) bludgeoning damage and be knocked prone. The dragon can then fly up to half its flying speed."}]}
}{
  :name "Young Gold Dragon"
  :size :large
  :type :dragon
  :alignment "lawful good"
  :armor-class 18
  :armor-notes "natural armor"
  :hit-points {:mean 178 :die-count 17 :die 10 :modifier 85}
  :speed "40 ft., fly 80 ft., swim 40 ft."

  :str 23
  :dex 14
  :con 21
  :int 16
  :wis 13
  :cha 20

  :saving-throws {:dex 6, :con 9, :wis 5, :cha 9}

  :skills {:insight 5, :perception 9, :persuasion 9, :stealth 6}
  :damage-immunities "fire"
  :senses "blindsight 30 ft., darkvision 120 ft., passive Perception 19"
  :languages "Common, Draconic"
  :challenge 10

  :traits [{:name "Amphibious" :description "The dragon can breathe air and water."}]

  :actions [{:name "Multiattack" :description "The dragon makes three attacks: one with its bite and two with its claws."}
            {:name "Bite" :description "Melee Weapon Attack: +10 to hit, reach 10 ft., one target. Hit: 17 (2d10 + 6) piercing damage."}
            {:name "Claw" :description "Melee Weapon Attack: +10 to hit, reach 5 ft., one target. Hit: 13 (2d6 + 6) slashing damage."}
            {:name "Breath Weapons" :notes "Recharge 5–6" :description "The dragon uses one of the following breath weapons."}
            {:name "Fire Breath" :description "The dragon exhales fire in a 30-foot cone. Each creature in that area must make a DC 17 Dexterity saving throw, taking 55 (10d10) fire damage on a failed save, or half as much damage on a successful one."}
            {:name "Weakening Breath" :description "The dragon exhales gas in a 30-foot cone. Each creature in that area must succeed on a DC 17 Strength saving throw or have disadvantage on Strength-based attack rolls, Strength checks, and Strength saving throws for 1 minute. A creature can repeat the saving throw at the end of each of its turns, ending the effect on itself on a success."}]
}{
  :name "Gold Dragon Wyrmling"
  :size :medium
  :type :dragon
  :alignment "lawful good"
  :armor-class 17
  :armor-notes "natural armor"
  :hit-points {:mean 60 :die-count 8 :die 8 :modifier 24}
  :speed "30 ft., fly 60 ft., swim 30 ft."

  :str 19
  :dex 14
  :con 17
  :int 14
  :wis 11
  :cha 16

  :saving-throws {:dex 4, :con 5, :wis 2, :cha 5}

  :skills {:perception 4, :stealth 4}
  :damage-immunities "fire"
  :senses "blindsight 10 ft., darkvision 60 ft., passive Perception 14"
  :languages "Draconic"
  :challenge 3

  :traits [{:name "Amphibious" :description "The dragon can breathe air and water."}]

  :actions [{:name "Bite" :description "Melee Weapon Attack: +6 to hit, reach 5 ft., one target. Hit: 9 (1d10 + 4) piercing damage."}
            {:name "Breath Weapons" :notes "Recharge 5–6" :description "The dragon uses one of the following breath weapons."}
            {:name "Fire Breath" :description "The dragon exhales fire in a 15-foot cone. Each creature in that area must make a DC 13 Dexterity saving throw, taking 22 (4d10) fire damage on a failed save, or half as much damage on a successful one."}
            {:name "Weakening Breath" :description "The dragon exhales gas in a 15-foot cone. Each creature in that area must succeed on a DC 13 Strength saving throw or have disadvantage on Strength-based attack rolls, Strength checks, and Strength saving throws for 1 minute. A creature can repeat the saving throw at the end of each of its turns, ending the effect on itself on a success."}]
}{
  :name "Ancient Silver Dragon"
  :size :gargantuan
  :type :dragon
  :alignment "lawful good"
  :armor-class 22
  :armor-notes "natural armor"
  :hit-points {:mean 487 :die-count 25 :die 20 :modifier 225}
  :speed "40 ft., fly 80 ft."

  :str 30
  :dex 10
  :con 29
  :int 18
  :wis 15
  :cha 23

  :saving-throws {:dex 7, :con 16, :wis 9, :cha 13}

  :skills {:arcana 11, :history 11, :perception 16, :stealth 7}
  :damage-immunities "cold"
  :senses "blindsight 60 ft., darkvision 120 ft., passive Perception 26"
  :languages "Common, Draconic"
  :challenge 23

  :traits [{:name "Legendary Resistance" :notes "3/Day" :description "If the dragon fails a saving throw, it can choose to succeed instead."}]

  :actions [{:name "Multiattack" :description "The dragon can use its Frightful Presence. It then makes three attacks: one with its bite and two with its claws."}
            {:name "Bite" :description "Melee Weapon Attack: +17 to hit, reach 15 ft., one target. Hit: 21 (2d10 + 10) piercing damage."}
            {:name "Claw" :description "Melee Weapon Attack: +17 to hit, reach 10 ft., one target. Hit: 17 (2d6 + 10) slashing damage."}
            {:name "Tail" :description "Melee Weapon Attack: +17 to hit, reach 20 ft., one target. Hit: 19 (2d8 + 10) bludgeoning damage."}
            {:name "Frightful Presence" :description "Each creature of the dragon's choice that is within 120 feet of the dragon and aware of it must succeed on a DC 21 Wisdom saving throw or become frightened for 1 minute. A creature can repeat the saving throw at the end of each of its turns, ending the effect on itself on a success. If a creature's saving throw is successful or the effect ends for it, the creature is immune to the dragon's Frightful Presence for the next 24 hours."}
            {:name "Breath Weapons" :notes "Recharge 5–6" :description "The dragon uses one of the following breath weapons."}
            {:name "Cold Breath" :description "The dragon exhales an icy blast in a 90- foot cone. Each creature in that area must make a DC 24 Constitution saving throw, taking 67 (15d8) cold damage on a failed save, or half as much damage on a successful one."}
            {:name "Paralyzing Breath" :description "The dragon exhales paralyzing gas in a 90-foot cone. Each creature in that area must succeed on a DC 24 Constitution saving throw or be paralyzed for 1 minute. A creature can repeat the saving throw at the end of each of its turns, ending the effect on itself on a success."}
            {:name "Change Shape" :description "The dragon magically polymorphs into a humanoid or beast that has a challenge rating no higher than its own, or back into its true form. It reverts to its true form if it dies. Any equipment it is wearing or carrying is absorbed or borne by the new form (the dragon's choice).
In a new form, the dragon retains its alignment, hit points, Hit Dice, ability to speak, proficiencies, Legendary Resistance, lair actions, and Intelligence, Wisdom, and Charisma scores, as well as this action. Its statistics and capabilities are otherwise replaced by those of the new form, except any class features or legendary actions of that form."}]

  :legendary-actions {:description "The dragon can take 3 legendary actions, choosing from the options below. Only one legendary action option can be used at a time and only at the end of another creature's turn. The dragon regains spent legendary actions at the start of its turn."
                      :actions [{:name "Detect" :description "The dragon makes a Wisdom (Perception) check."}
                                {:name "Tail Attack" :description "The dragon makes a tail attack."}
                                {:name "Wing Attack" :notes "Costs 2 Actions" :description "The dragon beats its wings. Each creature within 15 feet of the dragon must succeed on a DC 25 Dexterity saving throw or take 17 (2d6 + 10) bludgeoning damage and be knocked prone. The dragon can then fly up to half its flying speed."}]}
}{
  :name "Adult Silver Dragon"
  :size :huge
  :type :dragon
  :alignment "lawful good"
  :armor-class 19
  :armor-notes "natural armor"
  :hit-points {:mean 243 :die-count 18 :die 12 :modifier 126}
  :speed "40 ft., fly 80 ft."

  :str 27
  :dex 10
  :con 25
  :int 16
  :wis 13
  :cha 21

  :saving-throws {:dex 5, :con 12, :wis 6, :cha 10}

  :skills {:arcana 8, :history 8, :perception 11, :stealth 5}
  :damage-immunities "cold"
  :senses "blindsight 60 ft., darkvision 120 ft., passive Perception 21"
  :languages "Common, Draconic"
  :challenge 16

  :traits [{:name "Legendary Resistance" :notes "3/Day" :description "If the dragon fails a saving throw, it can choose to succeed instead."}]

  :actions [{:name "Multiattack" :description "The dragon can use its Frightful Presence. It then makes three attacks: one with its bite and two with its claws."}
            {:name "Bite" :description "Melee Weapon Attack: +13 to hit, reach 10 ft., one target. Hit: 19 (2d10 + 8) piercing damage."}
            {:name "Claw" :description "Melee Weapon Attack: +13 to hit, reach 5 ft., one target. Hit: 15 (2d6 + 8) slashing damage."}
            {:name "Tail" :description "Melee Weapon Attack: +13 to hit, reach 15 ft., one target. Hit: 17 (2d8 + 8) bludgeoning damage."}
            {:name "Frightful Presence" :description "Each creature of the dragon's choice that is within 120 feet of the dragon and aware of it must succeed on a DC 18 Wisdom saving throw or become frightened for 1 minute. A creature can repeat the saving throw at the end of each of its turns, ending the effect on itself on a success. If a creature's saving throw is successful or the effect ends for it, the creature is immune to the dragon's Frightful Presence for the next 24 hours."}
            {:name "Breath Weapons" :notes "Recharge 5–6" :description "The dragon uses one of the following breath weapons."}
            {:name "Cold Breath" :description "The dragon exhales an icy blast in a 60- foot cone. Each creature in that area must make a DC 20 Constitution saving throw, taking 58 (13d8) cold damage on a failed save, or half as much damage on a successful one."}
            {:name "Paralyzing Breath" :description "The dragon exhales paralyzing gas in a 60-foot cone. Each creature in that area must succeed on a DC 20 Constitution saving throw or be paralyzed for 1 minute. A creature can repeat the saving throw at the end of each of its turns, ending the effect on itself on a success."}
            {:name "Change Shape" :description "The dragon magically polymorphs into a humanoid or beast that has a challenge rating no higher than its own, or back into its true form. It reverts to its true form if it dies. Any equipment it is wearing or carrying is absorbed or borne by the new form (the dragon's choice).
In a new form, the dragon retains its alignment, hit points, Hit Dice, ability to speak, proficiencies, Legendary Resistance, lair actions, and Intelligence, Wisdom, and Charisma scores, as well as this action. Its statistics and capabilities are otherwise replaced by those of the new form, except any class features or legendary actions of that form."}]

  :legendary-actions {:description "The dragon can take 3 legendary actions, choosing from the options below. Only one legendary action option can be used at a time and only at the end of another creature's turn. The dragon regains spent legendary actions at the start of its turn."
                      :actions [{:name "Detect" :description "The dragon makes a Wisdom (Perception) check."}
                                {:name "Tail Attack" :description "The dragon makes a tail attack."}
                                {:name "Wing Attack" :notes "Costs 2 Actions" :description "The dragon beats its wings. Each creature within 10 feet of the dragon must succeed on a DC 21 Dexterity saving throw or take 15 (2d6 + 8) bludgeoning damage and be knocked prone. The dragon can then fly up to half its flying speed."}]}
}{
  :name "Young Silver Dragon"
  :size :large
  :type :dragon
  :alignment "lawful good"
  :armor-class 18
  :armor-notes "natural armor"
  :hit-points {:mean 168 :die-count 16 :die 10 :modifier 80}
  :speed "40 ft., fly 80 ft."

  :str 23
  :dex 10
  :con 21
  :int 14
  :wis 11
  :cha 19

  :saving-throws {:dex 4, :con 9, :wis 4, :cha 8}

  :skills {:arcana 6, :history 6, :perception 8, :stealth 4}
  :damage-immunities "cold"
  :senses "blindsight 30 ft., darkvision 120 ft., passive Perception 18"
  :languages "Common, Draconic"
  :challenge 9

  :actions [{:name "Multiattack" :description "The dragon makes three attacks: one with its bite and two with its claws."}
            {:name "Bite" :description "Melee Weapon Attack: +10 to hit, reach 10 ft., one target. Hit: 17 (2d10 + 6) piercing damage."}
            {:name "Claw" :description "Melee Weapon Attack: +10 to hit, reach 5 ft., one target. Hit: 13 (2d6 + 6) slashing damage."}
            {:name "Breath Weapons" :notes "Recharge 5–6" :description "The dragon uses one of the following breath weapons."}
            {:name "Cold Breath" :description "The dragon exhales an icy blast in a 30- foot cone. Each creature in that area must make a DC 17 Constitution saving throw, taking 54 (12d8) cold damage on a failed save, or half as much damage on a successful one."}
            {:name "Paralyzing Breath" :description "The dragon exhales paralyzing gas in a 30-foot cone. Each creature in that area must succeed on a DC 17 Constitution saving throw or be paralyzed for 1 minute. A creature can repeat the saving throw at the end of each of its turns, ending the effect on itself on a success."}]
}{
  :name "Silver Dragon Wyrmling"
  :size :medium
  :type :dragon
  :alignment "lawful good"
  :armor-class 17
  :armor-notes "natural armor"
  :hit-points {:mean 45 :die-count 6 :die 8 :modifier 18}
  :speed "30 ft., fly 60 ft."

  :str 19
  :dex 10
  :con 17
  :int 12
  :wis 11
  :cha 15

  :saving-throws {:dex 2, :con 5, :wis 2, :cha 4}

  :skills {:perception 4, :stealth 2}
  :damage-immunities "cold"
  :senses "blindsight 10 ft., darkvision 60 ft., passive Perception 14"
  :languages "Draconic"
  :challenge 2

  :actions [{:name "Bite" :description "Melee Weapon Attack: +6 to hit, reach 5 ft., one target. Hit: 9 (1d10 + 4) piercing damage."}
            {:name "Breath Weapons" :notes "Recharge 5–6" :description "The dragon uses one of the following breath weapons."}
            {:name "Cold Breath" :description "The dragon exhales an icy blast in a 15- foot cone. Each creature in that area must make a DC 13 Constitution saving throw, taking 18 (4d8) cold damage on a failed save, or half as much damage on a successful one."}
            {:name "Paralyzing Breath" :description "The dragon exhales paralyzing gas in a 15-foot cone. Each creature in that area must succeed on a DC 13 Constitution saving throw or be paralyzed for 1 minute. A creature can repeat the saving throw at the end of each of its turns, ending the effect on itself on a success."}]
}{
  :name "Dragon Turtle"
  :size :gargantuan
  :type :dragon
  :alignment "neutral"
  :armor-class 20
  :armor-notes "natural armor"
  :hit-points {:mean 341 :die-count 22 :die 20 :modifier 110}
  :speed "20 ft., swim 40 ft."

  :str 25
  :dex 10
  :con 20
  :int 10
  :wis 12
  :cha 12

  :saving-throws {:dex 6, :con 11, :wis 7}
  :damage-resistances "fire"
  :senses "darkvision 120 ft., passive Perception 11"
  :languages "Aquan, Draconic"
  :challenge 17

  :traits [{:name "Amphibious" :description "The dragon turtle can breathe air and water."}]

  :actions [{:name "Multiattack" :description "The dragon turtle makes three attacks: one with its bite and two with its claws. It can make one tail attack in place of its two claw attacks."}
            {:name "Bite" :description "Melee Weapon Attack: +13 to hit, reach 15 ft., one target. Hit: 26 (3d12 + 7) piercing damage."}
            {:name "Claw" :description "Melee Weapon Attack: +13 to hit, reach 10 ft., one target. Hit: 16 (2d8 + 7) slashing damage."}
            {:name "Tail" :description "Melee Weapon Attack: +13 to hit, reach 15 ft., one target. Hit: 26 (3d12 + 7) bludgeoning damage. If the target is a creature, it must succeed on a DC 20 Strength saving throw or be pushed up to 10 feet away from the dragon turtle and knocked prone."}
            {:name "Steam Breath" :notes "Recharge 5–6" :description "The dragon turtle exhales scalding steam in a 60-foot cone. Each creature in that area must make a DC 18 Constitution saving throw, taking 52 (15d6) fire damage on a failed save, or half as much damage on a successful one. Being underwater doesn't grant resistance against this damage."}]
}{
  :name "Drider"
  :size :large
  :type :monstrosity
  :alignment "chaotic evil"
  :armor-class 19
  :armor-notes "natural armor"
  :hit-points {:mean 123 :die-count 13 :die 10 :modifier 52}
  :speed "30 ft., climb 30 ft."

  :str 16
  :dex 16
  :con 18
  :int 13
  :wis 14
  :cha 12

  :skills {:perception 5, :stealth 9}
  :senses "darkvision 120 ft., passive Perception 15"
  :languages "Elvish, Undercommon"
  :challenge 6

  :traits [{:name "Fey Ancestry" :description "The drider has advantage on saving throws against being charmed, and magic can't put the drider to sleep."}
           {:name "Innate Spellcasting" :description "The drider's innate spellcasting ability is Wisdom (spell save DC 13). 
The drider can innately cast the following spells, requiring no material components:
At will: dancing lights
1/day each: darkness, faerie fire"}
           {:name "Spider Climb" :description "The drider can climb difficult surfaces, including upside down on ceilings, without needing to make an ability check."}
           {:name "Sunlight Sensitivity" :description "While in sunlight, the drider has disadvantage on attack rolls, as well as on Wisdom (Perception) checks that rely on sight."}
           {:name "Web Walker" :description "The drider ignores movement restrictions caused by webbing."}]

  :actions [{:name "Multiattack" :description "The drider makes three attacks, either with its longsword or its longbow. It can replace one of those attacks with a bite attack."}
            {:name "Bite" :description "Melee Weapon Attack: +6 to hit, reach 5 ft., one creature. Hit: 2 (1d4) piercing damage plus 9 (2d8) poison damage."}
            {:name "Longsword" :description "Melee Weapon Attack: +6 to hit, reach 5 ft., one target. Hit: 7 (1d8 + 3) slashing damage, or 8 (1d10 + 3) slashing damage if used with two hands."}
            {:name "Longbow" :description "Ranged Weapon Attack: +6 to hit, range 150/600 ft., one target. Hit: 7 (1d8 + 3) piercing damage plus 4 (1d8) poison damage."}]
}{
  :name "Dryad"
  :size :medium
  :type :fey
  :alignment "neutral"
  :armor-class 11
  :armor-notes "(16 with barkskin)"
  :hit-points {:mean 22 :die-count 5 :die 8}
  :speed "30 ft."

  :str 10
  :dex 12
  :con 11
  :int 14
  :wis 15
  :cha 18

  :skills {:perception 4, :stealth 5}
  :senses "darkvision 60 ft., passive Perception 14"
  :languages "Elvish, Sylvan"
  :challenge 1

  :traits [{:name "Innate Spellcasting" :description "The dryad's innate spellcasting ability is Charisma (spell save DC 14). 
The dryad can innately cast the following spells, requiring no material components:
At will: druidcraft
3/day each: entangle, goodberry
1/day each: barkskin, pass without trace, shillelagh"}
           {:name "Magic Resistance" :description "The dryad has advantage on saving throws against spells and other magical effects.
Speak with Beasts and Plants. The dryad can communicate with beasts and plants as if they shared a language."}
           {:name "Tree Stride" :description "Once on her turn, the dryad can use 10 feet of her movement to step magically into one living tree within her reach and emerge from a second living tree within 60 feet of the first tree, appearing in an unoccupied space within 5 feet of the second tree. Both trees must be Large or bigger."}]

  :actions [{:name "Club" :description "Melee Weapon Attack: +2 to hit (+6 to hit with shillelagh), reach 5 ft., one target. Hit: 2 (1d4) bludgeoning damage, or 8 (1d8 + 4) bludgeoning damage with shillelagh."}
            {:name "Fey Charm" :description "The dryad targets one humanoid or beast that she can see within 30 feet of her. If the target can see the dryad, it must succeed on a DC 14 Wisdom saving throw or be magically charmed. The charmed creature regards the dryad as a trusted friend to be heeded and protected. Although the target isn't under the dryad's control, it takes the dryad's requests or actions in the most favorable way it can.
Each time the dryad or its allies do anything harmful to the target, it can repeat the saving throw, ending the effect on itself on a success. Otherwise, the effect lasts 24 hours or until the dryad dies, is on a different plane of existence from the target, or ends the effect as a bonus action. If a target's saving throw is successful, the target is immune to the dryad's Fey Charm for the next 24 hours.
The dryad can have no more than one humanoid and up to three beasts charmed at a time."}]
}{
  :name "Duergar"
  :size :medium
  :type :humanoid
  :subtypes #{:dwarf}
  :alignment "lawful evil"
  :armor-class 16
  :armor-notes "(scale mail, shield)"
  :hit-points {:mean 26 :die-count 4 :die 8 :modifier 8}
  :speed "25 ft."

  :str 14
  :dex 11
  :con 14
  :int 11
  :wis 10
  :cha 9

  :damage-resistances "poison"
  :senses "darkvision 120 ft., passive Perception 10"
  :languages "Dwarvish, Undercommon"
  :challenge 1

  :traits [{:name "Duergar Resilience" :description "The duergar has advantage on saving throws against poison, spells, and illusions, as well as to resist being charmed or paralyzed."}
           {:name "Sunlight Sensitivity" :description "While in sunlight, the duergar has disadvantage on attack rolls, as well as on Wisdom (Perception) checks that rely on sight."}]

  :actions [{:name "Enlarge" :notes "Recharges after a Short or Long Rest" :description "For 1 minute, the duergar magically increases in size, along with anything it is wearing or carrying. While enlarged, the duergar is Large, doubles its damage dice on Strength-based weapon attacks (included in the attacks), and makes Strength checks and Strength saving throws with advantage. If the duergar lacks the room to become Large, it attains the maximum size possible in the space available."}
            {:name "War Pick" :description "Melee Weapon Attack: +4 to hit, reach 5 ft., one target. Hit: 6 (1d8 + 2) piercing damage, or 11 (2d8 + 2) piercing damage while enlarged."}
            {:name "Javelin" :description "Melee or Ranged Weapon Attack: +4 to hit, reach 5 ft. or range 30/120 ft., one target. Hit: 5 (1d6 + 2) piercing damage, or 9 (2d6 + 2) piercing damage while enlarged."}
            {:name "Invisibility" :notes "Recharges after a Short or Long Rest" :description "The duergar magically turns invisible until it attacks, casts a spell, or uses its Enlarge, or until its concentration is broken, up to 1 hour (as if concentrating on a spell). Any equipment the duergar wears or carries is invisible with it."}]
}{
  :name "Air Elemental"
  :size :large
  :type :elemental
  :alignment "neutral"
  :armor-class 15
  :hit-points {:mean 90 :die-count 12 :die 10 :modifier 24}
  :speed "0 ft., fly 90 ft. (hover)"

  :str 14
  :dex 20
  :con 14
  :int 6
  :wis 10
  :cha 6

  :damage-resistances "lightning, thunder; bludgeoning, piercing, and slashing from nonmagical attacks"
  :damage-immunities "poison"
  :condition-immunities "exhaustion, grappled, paralyzed, petrified, poisoned, prone, restrained, unconscious"
  :senses "darkvision 60 ft., passive Perception 10"
  :languages "Auran"
  :challenge 5

  :traits [{:name "Air Form" :description "The elemental can enter a hostile creature's space and stop there. It can move through a space as narrow as 1 inch wide without squeezing."}]

  :actions [{:name "Multiattack" :description "The elemental makes two slam attacks."}
            {:name "Slam" :description "Melee Weapon Attack: +8 to hit, reach 5 ft., one target. Hit: 14 (2d8 + 5) bludgeoning damage."}
            {:name "Whirlwind" :notes "Recharge 4–6" :description "Each creature in the elemental's space must make a DC 13 Strength saving throw. On a failure, a target takes 15 (3d8 + 2) bludgeoning damage and is flung up 20 feet away from the elemental in a random direction and knocked prone. If a thrown target strikes an object, such as a"}]
}{
  :name "Earth Elemental"
  :size :large
  :type :elemental
  :alignment "neutral"
  :armor-class 17
  :armor-notes "natural armor"
  :hit-points {:mean 126 :die-count 12 :die 10 :modifier 60}
  :speed "30 ft., burrow 30 ft."

  :str 20
  :dex 8
  :con 20
  :int 5
  :wis 10
  :cha 5

  :damage-immunities "poison"
  :damage-vulnerabilities "thunder"
  :damage-resistances "bludgeoning, piercing, and slashing from nonmagical attacks"
  :condition-immunities "exhaustion, paralyzed, petrified, poisoned, unconscious"
  :senses "darkvision 60 ft., tremorsense 60 ft., passive Perception 10"
  :languages "Terran"
  :challenge 5

  :traits [{:name "Earth Glide" :description "The elemental can burrow through nonmagical, unworked earth and stone. While doing so, the elemental doesn't disturb the material it moves through."}
           {:name "Siege Monster" :description "The elemental deals double damage to objects and structures."}]

  :actions [{:name "Multiattack" :description "The elemental makes two slam attacks."}
            {:name "Slam" :description "Melee Weapon Attack: +8 to hit, reach 10 ft., one target. Hit: 14 (2d8 + 5) bludgeoning damage."}]
}{
  :name "Fire Elemental"
  :size :large
  :type :elemental
  :alignment "neutral"
  :armor-class 13
  :hit-points {:mean 102 :die-count 12 :die 10 :modifier 36}
  :speed "50 ft."

  :str 10
  :dex 17
  :con 16
  :int 6
  :wis 10
  :cha 7

  :damage-resistances "bludgeoning, piercing, and slashing from nonmagical attacks"
  :damage-immunities "fire, poison"
  :condition-immunities "exhaustion, grappled, paralyzed, petrified, poisoned, prone, restrained, unconscious"
  :senses "darkvision 60 ft., passive Perception 10"
  :languages "Ignan"
  :challenge 5

  :traits [{:name "Fire Form" :description "The elemental can move through a space as narrow as 1 inch wide without squeezing. A creature that touches the elemental or hits it with a melee attack while within 5 feet of it takes 5 (1d10) fire damage. In addition, the elemental can enter a hostile creature's space and stop there. The first time it enters a creature's space on a turn, that creature takes 5 (1d10) fire damage and catches fire; until someone takes an action to douse the fire, the creature takes 5 (1d10) fire damage at the start of each of its turns."}
           {:name "Illumination" :description "The elemental sheds bright light in a 30- foot radius and dim light in an additional 30 feet."}
           {:name "Water Susceptibility" :description "For every 5 feet the elemental moves in water, or for every gallon of water splashed on it, it takes 1 cold damage."}]

  :actions [{:name "Multiattack" :description "The elemental makes two touch attacks."}
            {:name "Touch" :description "Melee Weapon Attack: +6 to hit, reach 5 ft., one target. Hit: 10 (2d6 + 3) fire damage. If the target is a creature or a flammable object, it ignites. Until a creature takes an action to douse the fire, the target takes 5 (1d10) fire damage at the start of each of its turns."}]
}{
  :name "Water Elemental"
  :size :large
  :type :elemental
  :alignment "neutral"
  :armor-class 14
  :armor-notes "natural armor"
  :hit-points {:mean 114 :die-count 12 :die 10 :modifier 48}
  :speed "30 ft., swim 90 ft."

  :str 18
  :dex 14
  :con 18
  :int 5
  :wis 10
  :cha 8

  :damage-resistances "acid; bludgeoning, piercing, and slashing from nonmagical attacks"
  :damage-immunities "poison"
  :condition-immunities "exhaustion, grappled, paralyzed, petrified, poisoned, prone, restrained, unconscious"
  :senses "darkvision 60 ft., passive Perception 10"
  :languages "Aquan"
  :challenge 5

  :traits [{:name "Water Form" :description "The elemental can enter a hostile creature's space and stop there. It can move through a space as narrow as 1 inch wide without squeezing."}
           {:name "Freeze" :description "If the elemental takes cold damage, it partially freezes; its speed is reduced by 20 feet until the end of its next turn."}]

  :actions [{:name "Multiattack" :description "The elemental makes two slam attacks."}
            {:name "Slam" :description "Melee Weapon Attack: +7 to hit, reach 5 ft., one target. Hit: 13 (2d8 + 4) bludgeoning damage."}
            {:name "Whelm" :notes "Recharge 4–6" :description "Each creature in the elemental's space must make a DC 15 Strength saving throw. On a failure, a target takes 13 (2d8 + 4) bludgeoning damage. If it is Large or smaller, it is also grappled (escape DC 14). Until this grapple ends, the target is restrained and unable to breathe unless it can breathe water. If the saving throw is successful, the target is pushed out of the elemental's space.
The elemental can grapple one Large creature or up to two Medium or smaller creatures at one time. At the start of each of the elemental's turns, each target grappled by it takes 13 (2d8 + 4) bludgeoning damage. A creature within 5 feet of the elemental can pull a creature or object out of it by taking an action to make a DC 14 Strength and succeeding."}]
}{
  :name "Elf, Drow"
  :size :medium
  :type :humanoid
  :subtypes #{:elf}
  :alignment "neutral evil"
  :armor-class 15
  :armor-notes "(chain shirt)"
  :hit-points {:mean 13 :die-count 3 :die 8}
  :speed "30 ft."

  :str 10
  :dex 14
  :con 10
  :int 11
  :wis 11
  :cha 12

  :skills {:perception 2, :stealth 4}
  :senses "darkvision 120 ft., passive Perception 12"
  :languages "Elvish, Undercommon"
  :challenge (/ 1 4)

  :traits [{:name "Fey Ancestry" :description "The drow has advantage on saving throws against being charmed, and magic can't put the drow to sleep."}
           {:name "Innate Spellcasting" :description "The drow's spellcasting ability is Charisma (spell save DC 11). 
It can innately cast the following spells, requiring no material components:
At will: dancing lights
1/day each: darkness, faerie fire"}
           {:name "Sunlight Sensitivity" :description "While in sunlight, the drow has disadvantage on attack rolls, as well as on Wisdom (Perception) checks that rely on sight."}]

  :actions [{:name "Shortsword" :description "Melee Weapon Attack: +4 to hit, reach 5 ft., one target. Hit: 5 (1d6 + 2) piercing damage."}
            {:name "Hand Crossbow" :description "Ranged Weapon Attack: +4 to hit, range 30/120 ft., one target. Hit: 5 (1d6 + 2) piercing damage, and the target must succeed on a DC 13 Constitution saving throw or be poisoned for 1 hour. If the saving throw fails by 5 or more, the target is also unconscious while poisoned in this way. The target wakes up if it takes damage or if another creature takes an action to shake it awake."}]
}{
  :name "Ettercap"
  :size :medium
  :type :monstrosity
  :alignment "neutral evil"
  :armor-class 13
  :armor-notes "natural armor"
  :hit-points {:mean 44 :die-count 8 :die 8 :modifier 8}
  :speed "30 ft., climb 30 ft."

  :str 14
  :dex 15
  :con 13
  :int 7
  :wis 12
  :cha 8

  :skills {:perception 3, :stealth 4, :survival 3}
  :senses "darkvision 60 ft., passive Perception 13"
  :challenge 2

  :traits [{:name "Spider Climb" :description "The ettercap can climb difficult surfaces, including upside down on ceilings, without needing to make an ability check."}
           {:name "Web Sense" :description "While in contact with a web, the ettercap knows the exact location of any other creature in contact with the same web."}
           {:name "Web Walker" :description "The ettercap ignores movement restrictions caused by webbing."}]

  :actions [{:name "Multiattack" :description "The ettercap makes two attacks: one with its bite and one with its claws."}
            {:name "Bite" :description "Melee Weapon Attack: +4 to hit, reach 5 ft., one creature. Hit: 6 (1d8 + 2) piercing damage plus 4 (1d8) poison damage. The target must succeed on a DC 11 Constitution saving throw or be poisoned for 1 minute. The creature can repeat the saving throw at the end of also ends if the webbing is destroyed. The webbing has AC 10, 5 hit points, vulnerability to fire damage, and immunity to bludgeoning, poison, and psychic damage."}
            {:name "Claws" :description "Melee Weapon Attack: +4 to hit, reach 5 ft., one target. Hit: 7 (2d4 + 2) slashing damage."}
            {:name "Web" :notes "Recharge 5–6" :description "Ranged Weapon Attack: +4 to hit, range 30/60 ft., one Large or smaller creature. Hit: The creature is restrained by webbing. As an action, the restrained creature can make a DC 11 Strength check, escaping from the webbing on a success. The effect"}]
}{
  :name "Ettin"
  :size :large
  :type :giant
  :alignment "chaotic evil"
  :armor-class 12
  :armor-notes "natural armor"
  :hit-points {:mean 85 :die-count 10 :die 10 :modifier 30}
  :speed "40 ft."

  :str 21
  :dex 8
  :con 17
  :int 6
  :wis 10
  :cha 8

  :skills {:perception 4}
  :senses "darkvision 60 ft., passive Perception 14"
  :languages "Giant, Orc"
  :challenge 4

  :traits [{:name "Two Heads" :description "The ettin has advantage on Wisdom (Perception) checks and on saving throws against being blinded, charmed, deafened, frightened, stunned, and knocked unconscious."}
           {:name "Wakeful" :description "When one of the ettin's heads is asleep, its other head is awake."}]

  :actions [{:name "Multiattack" :description "The ettin makes two attacks: one with its battleaxe and one with its morningstar."}
            {:name "Battleaxe" :description "Melee Weapon Attack: +7 to hit, reach 5 ft., one target. Hit: 14 (2d8 + 5) slashing damage."}
            {:name "Morningstar" :description "Melee Weapon Attack: +7 to hit, reach 5 ft., one target. Hit: 14 (2d8 + 5) piercing damage."}]
}{
  :name "Shrieker"
  :size :medium
  :type :plant
  :alignment "unaligned"
  :armor-class 5
  :hit-points {:mean 13 :die-count 3 :die 8}
  :speed "0 ft."

  :str 1
  :dex 1
  :con 10
  :int 1
  :wis 3
  :cha 1

  :condition-immunities "blinded, deafened, frightened"
  :senses "blindsight 30 ft. (blind beyond this radius), passive Perception 6"
  :challenge 0

  :traits [{:name "False Appearance" :description "While the shrieker remains motionless, it is indistinguishable from an ordinary fungus."}]

  :reactions [{:name "Shriek" :description "When bright light or a creature is within 30 feet of the shrieker, it emits a shriek audible within 300 feet of it. The shrieker continues to shriek until the disturbance moves out of range and for 1d4 of the shrieker's turns afterward."}]
}{
  :name "Violet Fungus"
  :size :medium
  :type :plant
  :alignment "unaligned"
  :armor-class 5
  :hit-points {:mean 18 :die-count 4 :die 8}
  :speed "5 ft."

  :str 3
  :dex 1
  :con 10
  :int 1
  :wis 3
  :cha 1

  :condition-immunities "blinded, deafened, frightened"
  :senses "blindsight 30 ft. (blind beyond this radius), passive Perception 6"
  :challenge (/ 1 4)

  :traits [{:name "False Appearance" :description "While the violet fungus remains motionless, it is indistinguishable from an ordinary fungus."}]

  :actions [{:name "Multiattack" :description "The fungus makes 1d4 Rotting Touch attacks."}
            {:name "Rotting Touch" :description "Melee Weapon Attack: +2 to hit, reach 10ft., one creature. Hit: 4 (1d8) necrotic damage"}]
}{
  :name "Gargoyle"
  :size :medium
  :type :elemental
  :alignment "chaotic evil"
  :armor-class 15
  :armor-notes "natural armor"
  :hit-points {:mean 52 :die-count 7 :die 8 :modifier 21}
  :speed "30 ft., fly 60 ft."

  :str 15
  :dex 11
  :con 16
  :int 6
  :wis 11
  :cha 7

  :damage-resistances "bludgeoning, piercing, and slashing from nonmagical attacks that aren't adamantine"
  :damage-immunities "poison"
  :condition-immunities "exhaustion, petrified, poisoned"
  :senses "darkvision 60 ft., passive Perception 10"
  :languages "Terran"
  :challenge 2

  :traits [{:name "False Appearance" :description "While the gargoyle remains motionless, it is indistinguishable from an inanimate statue."}]

  :actions [{:name "Multiattack" :description "The gargoyle makes two attacks: one with its bite and one with its claws."}
            {:name "Bite" :description "Melee Weapon Attack: +4 to hit, reach 5 ft., one target. Hit: 5 (1d6 + 2) piercing damage."}
            {:name "Claws" :description "Melee Weapon Attack: +4 to hit, reach 5 ft., one target. Hit: 5 (1d6 + 2) slashing damage."}]
}{
  :name "Djinni"
  :size :large
  :type :elemental
  :alignment "chaotic good"
  :armor-class 17
  :armor-notes "natural armor"
  :hit-points {:mean 161 :die-count 14 :die 10 :modifier 84}
  :speed "30 ft., fly 90 ft."

  :str 21
  :dex 15
  :con 22
  :int 15
  :wis 16
  :cha 20

  :saving-throws {:dex 6, :wis 7, :cha 9}
  :damage-immunities "lightning, thunder"
  :senses "darkvision 120 ft., passive Perception 13"
  :languages "Auran"
  :challenge 11

  :traits [{:name "Elemental Demise" :description "If the djinni dies, its body disintegrates into a warm breeze, leaving behind only equipment the djinni was wearing or carrying."}
           {:name "Innate Spellcasting" :description "The djinni's innate spellcasting ability is Charisma (spell save DC 17, +9 to hit with spell attacks).
It can innately cast the following spells, requiring no material components:
At will: detect evil and good, detect magic, thunderwave
3/day each: create food and water (can create wine instead of water), tongues, wind walk
1/day each: conjure elemental (air elemental only), creation, gaseous form, invisibility, major image, plane shift"}]

  :actions [{:name "Multiattack" :description "The djinni makes three scimitar attacks."}
            {:name "Scimitar" :description "Melee Weapon Attack: +9 to hit, reach 5 ft., one target. Hit: 12 (2d6 + 5) slashing damage plus 3 (1d6) lightning or thunder damage (djinni's choice)."}
            {:name "Create Whirlwind" :description "A 5-foot-radius, 30-foot-tall cylinder of swirling air magically forms on a point the djinni can see within 120 feet of it. The whirlwind lasts as long as the djinni maintains concentration (as if concentrating on a spell). Any creature but the djinni that enters the whirlwind must succeed on a DC 18 Strength saving throw or be restrained by it. The djinni can move the whirlwind up to 60 feet as an action, and creatures restrained by the whirlwind move with it. The whirlwind ends if the djinni loses sight of it.
A creature can use its action to free a creature restrained by the whirlwind, including itself, by succeeding on a DC 18 Strength check. If the check succeeds, the creature is no longer restrained and moves to the nearest space outside the whirlwind."}]
}{
  :name "Efreeti"
  :size :large
  :type :elemental
  :alignment "lawful evil"
  :armor-class 17
  :armor-notes "natural armor"
  :hit-points {:mean 200 :die-count 16 :die 10 :modifier 112}
  :speed "40 ft., fly 60 ft."

  :str 22
  :dex 12
  :con 24
  :int 16
  :wis 15
  :cha 16

  :saving-throws {:int 7, :wis 6, :cha 7}
  :damage-immunities "fire"
  :senses "darkvision 120 ft., passive Perception 12"
  :languages "Ignan"
  :challenge 11

  :traits [{:name "Elemental Demise" :description "If the efreeti dies, its body disintegrates in a flash of fire and puff of smoke, leaving behind only equipment the efreeti was wearing or carrying."}
           {:name "Innate Spellcasting" :description "The efreeti's innate spellcasting ability is Charisma (spell save DC 15, +7 to hit with spell attacks).
It can innately cast the following spells, requiring no material components:
At will: detect magic
3/day:  enlarge/reduce, tongues
1/day each: conjure elemental (fire elemental only), gaseous form, invisibility, major image, plane shift, wall of fire"}]

  :actions [{:name "Multiattack" :description "The efreeti makes two scimitar attacks or uses its Hurl Flame twice."}
            {:name "Scimitar" :description "Melee Weapon Attack: +10 to hit, reach 5 ft., one target. Hit: 13 (2d6 + 6) slashing damage plus 7 (2d6) fire damage."}
            {:name "Hurl Flame" :description "Ranged Spell Attack: +7 to hit, range 120 ft., one target. Hit: 17 (5d6) fire damage."}]
}{
  :name "Ghost"
  :size :medium
  :type :undead
  :alignment "any alignment"
  :armor-class 11
  :hit-points {:mean 45 :die-count 10 :die 8}
  :speed "0 ft., fly 40 ft. (hover)"

  :str 7
  :dex 13
  :con 10
  :int 10
  :wis 12
  :cha 17

  :damage-resistances "acid, fire, lightning, thunder; bludgeoning, piercing, and slashing from nonmagical attacks"
  :damage-immunities "cold, necrotic, poison"
  :condition-immunities "charmed, exhaustion, frightened, grappled, paralyzed, petrified, poisoned, prone, restrained"
  :senses "darkvision 60 ft., passive Perception 11"
  :languages "any languages it knew in life"
  :challenge 4

  :traits [{:name "Ethereal Sight" :description "The ghost can see 60 feet into the Ethereal Plane when it is on the Material Plane, and vice versa."}
           {:name "Incorporeal Movement" :description "The ghost can move through other creatures and objects as if they were difficult terrain. It takes 5 (1d10) force damage if it ends its turn inside an object."}]

  :actions [{:name "Withering Touch" :description "Melee Weapon Attack: +5 to hit, reach 5 ft., one target. Hit: 17 (4d6 + 3) necrotic damage."}
            {:name "Etherealness" :description "The ghost enters the Ethereal Plane from the Material Plane, or vice versa. It is visible on the Material Plane while it is in the Border Ethereal, and vice versa, yet it can't affect or be affected by anything on the other plane."}
            {:name "Horrifying Visage" :description "Each non-undead creature within 60 feet of the ghost that can see it must succeed on a DC 13 Wisdom saving throw or be frightened for 1 minute. If the save fails by 5 or more, the target also ages 1d4 × 10 years. A frightened target can repeat the saving throw at the end of each of its turns, ending the frightened condition on itself on a success. If a target's saving throw is successful or the effect ends for it, the target is immune to this ghost's Horrifying Visage for the next 24 hours. The aging effect can be reversed with a greater restoration spell, but only within 24 hours of it occurring."}
            {:name "Possession" :notes "Recharge 6" :description "One humanoid that the ghost can see within 5 feet of it must succeed on a DC 13 Charisma saving throw or be possessed by the ghost; the ghost then disappears, and the target is incapacitated and loses control of its body. The ghost now controls the body but doesn't deprive the target of awareness. The ghost can't be targeted by any attack, spell, or other effect, except ones that turn undead, and it retains its alignment, Intelligence, Wisdom, Charisma, and immunity to being charmed and frightened. It otherwise uses the possessed target's statistics, but doesn't gain access to the target's knowledge, class features, or proficiencies.
The possession lasts until the body drops to 0 hit points, the ghost ends it as a bonus action, or the ghost is turned or forced out by an effect like the dispel evil and good spell. When the possession ends, the ghost reappears in an unoccupied space within 5 feet of the body. The target is immune to this ghost's Possession for 24 hours after succeeding on the saving throw or after the possession ends."}]
}{
  :name "Ghast"
  :size :medium
  :type :undead
  :alignment "chaotic evil"
  :armor-class 13
  :hit-points {:mean 36 :die-count 8 :die 8}
  :speed "30 ft."

  :str 16
  :dex 17
  :con 10
  :int 11
  :wis 10
  :cha 8

  :damage-resistances "necrotic"
  :damage-immunities "poison"
  :condition-immunities "charmed, exhaustion, poisoned"
  :senses "darkvision 60 ft., passive Perception 10"
  :languages "Common"
  :challenge 2

  :traits [{:name "Stench" :description "Any creature that starts its turn within 5 feet of the ghast must succeed on a DC 10 Constitution saving throw or be poisoned until the start of its next turn. On a successful saving throw, the creature is immune to the ghast's Stench for 24 hours."}
           {:name "Turning Defiance" :description "The ghast and any ghouls within 30 feet of it have advantage on saving throws against effects that turn undead."}]

  :actions [{:name "Bite" :description "Melee Weapon Attack: +3 to hit, reach 5 ft., one creature. Hit: 12 (2d8 + 3) piercing damage."}
            {:name "Claws" :description "Melee Weapon Attack: +5 to hit, reach 5 ft., one target. Hit: 10 (2d6 + 3) slashing damage. If the target is a creature other than an undead, it must succeed on a DC 10 Constitution saving throw or be paralyzed for 1 minute. The target can repeat the saving throw at the end of each of its turns, ending the effect on itself on a success."}]
}{
  :name "Ghoul"
  :size :medium
  :type :undead
  :alignment "chaotic evil"
  :armor-class 12
  :hit-points {:mean 22 :die-count 5 :die 8}
  :speed "30 ft."

  :str 13
  :dex 15
  :con 10
  :int 7
  :wis 10
  :cha 6

  :damage-immunities "poison"
  :condition-immunities "charmed, exhaustion, poisoned"
  :senses "darkvision 60 ft., passive Perception 10"
  :languages "Common"
  :challenge 1

  :actions [{:name "Bite" :description "Melee Weapon Attack: +2 to hit, reach 5 ft., one creature. Hit: 9 (2d6 + 2) piercing damage."}
            {:name "Claws" :description "Melee Weapon Attack: +4 to hit, reach 5 ft., one target. Hit: 7 (2d4 + 2) slashing damage. If the target is a creature other than an elf or undead, it must succeed on a DC 10 Constitution saving throw or be paralyzed for 1 minute. The target can repeat the saving throw at the end of each of its turns, ending the effect on itself on a success."}]
}{
  :name "Cloud Giant"
  :size :huge
  :type :giant
  :alignment "neutral good (50%) or neutral evil (50%)"
  :armor-class 14
  :armor-notes "natural armor"
  :hit-points {:mean 200 :die-count 16 :die 12 :modifier 96}
  :speed "40 ft."

  :str 27
  :dex 10
  :con 22
  :int 12
  :wis 16
  :cha 16

  :saving-throws {:con 10, :wis 7, :cha 7}

  :skills {:insight 7, :perception 7}
  :senses "passive Perception 17"
  :languages "Common, Giant"
  :challenge 9

  :traits [{:name "Innate Spellcasting" :description "The giant's innate spellcasting ability is Charisma. 
It can innately cast the following spells, requiring no material components:
At will: detect magic, fog cloud, light
3/day each: feather fall, fly, misty step, telekinesis
1/day each: control weather, gaseous form"}]

  :actions [{:name "Multiattack" :description "The giant makes two morningstar attacks."}
            {:name "Morningstar" :description "Melee Weapon Attack: +12 to hit, reach 10 ft., one target. Hit: 21 (3d8 + 8) piercing damage."}
            {:name "Rock" :description "Ranged Weapon Attack: +12 to hit, range 60/240 ft., one target. Hit: 30 (4d10 + 8) bludgeoning damage."}]
}{
  :name "Fire Giant"
  :size :huge
  :type :giant
  :alignment "lawful evil"
  :armor-class 18
  :armor-notes "(plate)"
  :hit-points {:mean 162 :die-count 13 :die 12 :modifier 78}
  :speed "30 ft."

  :str 25
  :dex 9
  :con 23
  :int 10
  :wis 14
  :cha 13

  :saving-throws {:dex 3, :con 10, :cha 5}

  :skills {:athletics 11, :perception 6}
  :damage-immunities "fire"
  :senses "passive Perception 16"
  :languages "Giant"
  :challenge 9

  :actions [{:name "Multiattack" :description "The giant makes two greatsword attacks."}
            {:name "Greatsword" :description "Melee Weapon Attack: +11 to hit, reach 10 ft., one target. Hit: 28 (6d6 + 7) slashing damage."}
            {:name "Rock" :description "Ranged Weapon Attack: +11 to hit, range 60/240 ft., one target. Hit: 29 (4d10 + 7) bludgeoning damage."}]
  }{
  :name "Frost Giant"
  :size :huge
  :type :giant
  :alignment "neutral evil"
  :armor-class 15
  :armor-notes "(patchwork armor)"
  :hit-points {:mean 138 :die-count 12 :die 12 :modifier 60}
  :speed "40 ft."

  :traits [{:name "Keen Smell" :description "The giant has advantage on Wisdom (Perception) checks that rely on smell."}]

  :str 23
  :dex 9
  :con 21
  :int 9
  :wis 10
  :cha 12

  :saving-throws {:con 8, :wis 3, :cha 4}
  :skills {:athletics 9, :perception 3}
  :damage-immunities "cold"
  :senses "passive Perception 13"
  :languages "Giant"
  :challenge 8

  :actions [{:name "Multiattack" :description "The giant makes two greataxe attacks."}
            {:name "Greataxe" :description "Melee Weapon Attack: +9 to hit, reach 10 ft., one target. Hit: 25 (3d12 + 6) slashing damage."}
            {:name "Rock" :description "Ranged Weapon Attack: +9 to hit, range 60/240 ft., one target. Hit: 28 (4d10 + 6) bludgeoning damage."}]
}{
  :name "Hill Giant"
  :size :huge
  :type :giant
  :alignment "chaotic evil"
  :armor-class 13
  :armor-notes "natural armor"
  :hit-points {:mean 105 :die-count 10 :die 12 :modifier 40}
  :speed "40 ft."

  :str 21
  :dex 8
  :con 19
  :int 5
  :wis 9
  :cha 6

  :skills {:perception 2}
  :senses "passive Perception 12"
  :languages "Giant"
  :challenge 5

  :actions [{:name "Multiattack" :description "The giant makes two greatclub attacks."}
            {:name "Greatclub" :description "Melee Weapon Attack: +8 to hit, reach 10 ft., one target. Hit: 18 (3d8 + 5) bludgeoning damage."}
            {:name "Rock" :description "Ranged Weapon Attack: +8 to hit, range 60/240 ft., one target. Hit: 21 (3d10 + 5) bludgeoning damage."}]
}{
  :name "Stone Giant"
  :size :huge
  :type :giant
  :alignment "neutral"
  :armor-class 17
  :armor-notes "natural armor"
  :hit-points {:mean 126 :die-count 11 :die 12 :modifier 55}
  :speed "40 ft."

  :str 23
  :dex 15
  :con 20
  :int 10
  :wis 12
  :cha 9

  :saving-throws {:dex 5, :con 8, :wis 4}
  :skills {:athletics 12, :perception 4}
  :senses "darkvision 60 ft., passive Perception 14"
  :languages "Giant"
  :challenge 7

  :traits [{:name "Stone Camouflage" :description "The giant has advantage on Dexterity (Stealth) checks made to hide in rocky terrain."}]

  :actions [{:name "Multiattack" :description "The giant makes two greatclub attacks."}
            {:name "Greatclub" :description "Melee Weapon Attack: +9 to hit, reach 15 ft., one target. Hit: 19 (3d8 + 6) bludgeoning damage."}
            {:name "Rock" :description "Ranged Weapon Attack: +9 to hit, range 60/240 ft., one target. Hit: 28 (4d10 + 6) bludgeoning damage. If the target is a creature, it must succeed on a DC 17 Strength saving throw or be knocked prone."}]
  :reactions [{:name "Rock Catching" :description "If a rock or similar object is hurled at the giant, the giant can, with a successful DC 10 Dexterity saving throw, catch the missile and take no bludgeoning damage from it."}]
}{
  :name "Storm Giant"
  :size :huge
  :type :giant
  :alignment "chaotic good"
  :armor-class 16
  :armor-notes "(scale mail)"
  :hit-points {:mean 230 :die-count 20 :die 12 :modifier 100}
  :speed "50 ft., swim 50 ft."

  :str 29
  :dex 14
  :con 20
  :int 16
  :wis 18
  :cha 18
                                                                                                                                                                                                                                    
  :saving-throws {:str 14, :con 10, :wis 9, :cha 9}
  :skills {:arcana 8, :athletics 14, :history 8, :perception 9}
  :damage-resistances "cold"
  :damage-immunities "lightning, thunder"
  :senses "passive Perception 19"
  :languages "Common, Giant"
  :challenge 13

  :traits [{:name "Amphibious" :description "The giant can breathe air and water."}
           {:name "Innate Spellcasting" :description "The giant's innate spellcasting ability is Charisma (spell save DC 17). 
It can innately cast the following spells, requiring no material components:
At will: detect magic, feather fall, levitate, light
3/day each: control weather, water breathing"}]

  :actions [{:name "Multiattack" :description "The giant makes two greatsword attacks."}
            {:name "Greatsword" :description "Melee Weapon Attack: +14 to hit, reach 10 ft., one target. Hit: 30 (6d6 + 9) slashing damage."}
            {:name "Rock" :description "Ranged Weapon Attack: +14 to hit, range 60/240 ft., one target. Hit: 35 (4d12 + 9) bludgeoning damage."}
            {:name "Lightning Strike" :notes "Recharge 5–6" :description "The giant hurls a magical lightning bolt at a point it can see within 500 feet of it. Each creature within 10 feet of that point must make a DC 17 Dexterity saving throw, taking 54 (12d8) lightning damage on a failed save, or half as much damage on a successful one."}]
}{
  :name "Gibbering Mouther"
  :size :medium
  :type :aberration
  :alignment "neutral"
  :armor-class 9
  :hit-points {:mean 67 :die-count 9 :die 8 :modifier 27}
  :speed "10 ft., swim 10 ft."

  :str 10
  :dex 8
  :con 16
  :int 3
  :wis 10
  :cha 6

  :condition-immunities "prone"
  :senses "darkvision 60 ft., passive Perception 10"
  :challenge 2

  :traits [{:name "Aberrant Ground" :description "The ground in a 10-foot radius around the mouther is doughlike difficult terrain. Each creature that starts its turn in that area must succeed on a DC 10 Strength saving throw or have its speed reduced to 0 until the start of its next turn."}
           {:name "Gibbering" :description "The mouther babbles incoherently while it can see any creature and isn't incapacitated. Each creature that starts its turn within 20 feet of the mouther and can hear the gibbering must succeed on a DC 10 Wisdom saving throw. On a failure, the creature can't take reactions until the start of its next turn and rolls a d8 to determine what it does during its turn. On a 1 to 4, the creature does nothing. On a 5 or 6, the creature takes no action or bonus action and uses all its movement to move in a randomly determined"}
           {:name "direction" :description "On a 7 or 8, the creature makes a melee attack against a randomly determined creature within its reach or does nothing if it can't make such an attack."}]

  :actions [{:name "Multiattack" :description "The gibbering mouther makes one bite attack and, if it can, uses its Blinding Spittle."}
            {:name "Bites" :description "Melee Weapon Attack: +2 to hit, reach 5 ft., one creature. Hit: 17 (5d6) piercing damage. If the target is Medium or smaller, it must succeed on a DC 10 Strength saving throw or be knocked prone. If the target is killed by this damage, it is absorbed into the mouther."}
            {:name "Blinding Spittle" :notes "Recharge 5–6" :description "The mouther spits a chemical glob at a point it can see within 15 feet of it. The glob explodes in a blinding flash of light on impact. Each creature within 5 feet of the flash must succeed on a DC 13 Dexterity saving throw or be blinded until the end of the mouther's next turn."}]
}{
  :name "Gnoll"
  :size :medium
  :type :humanoid
  :subtypes #{:gnoll}
  :alignment "chaotic evil"
  :armor-class 15
  :armor-notes "(hide armor, shield)"
  :hit-points {:mean 22 :die-count 5 :die 8}
  :speed "30 ft."

  :str 14
  :dex 12
  :con 11
  :int 6
  :wis 10
  :cha 7
  :senses "darkvision 60 ft., passive Perception 10"
  :languages "Gnoll"
  :challenge (/ 1 2)

  :traits [{:name "Rampage" :description "When the gnoll reduces a creature to 0 hit points with a melee attack on its turn, the gnoll can take a bonus action to move up to half its speed and make a bite attack."}]

  :actions [{:name "Bite" :description "Melee Weapon Attack: +4 to hit, reach 5 ft., one creature. Hit: 4 (1d4 + 2) piercing damage."}
            {:name "Spear" :description "Melee or Ranged Weapon Attack: +4 to hit, reach 5 ft. or range 20/60 ft., one target. Hit: 5 (1d6 + 2) piercing damage, or 6 (1d8 + 2) piercing damage if used with two hands to make a melee attack."}
            {:name "Longbow" :description "Ranged Weapon Attack: +3 to hit, range 150/600 ft., one target. Hit: 5 (1d8 + 1) piercing damage."}]
}{
  :name "Gnome, Deep (Svirfneblin)"
  :size :small
  :type :humanoid
  :subtypes #{:gnome}
  :alignment "neutral good"
  :armor-class 15
  :armor-notes "(chain shirt)"
  :hit-points {:mean 16 :die-count 3 :die 6 :modifier 6}
  :speed "20 ft."

  :str 15
  :dex 14
  :con 14
  :int 12
  :wis 10
  :cha 9

  :skills {:investigation 3, :perception 2, :stealth 4}
  :senses "darkvision 120 ft., passive Perception 12"
  :languages "Gnomish, Terran, Undercommon"
  :challenge (/ 1 2)

  :traits [{:name "Stone Camouflage" :description "The gnome has advantage on Dexterity (Stealth) checks made to hide in rocky terrain."}
           {:name "Gnome Cunning" :description "The gnome has advantage on Intelligence, Wisdom, and Charisma saving throws against magic."}
           {:name "Innate Spellcasting" :description "The gnome's innate spellcasting ability is Intelligence (spell save DC 11). 
It can innately cast the following spells, requiring no material components:
At will: nondetection (self only)
1/day each: blindness/deafness, blur, disguise self"}]

  :actions [{:name "War Pick" :description "Melee Weapon Attack: +4 to hit, reach 5 ft., one target. Hit: 6 (1d8 + 2) piercing damage."}
            {:name "Poisoned Dart" :description "Ranged Weapon Attack: +4 to hit, range 30/120 ft., one creature. Hit: 4 (1d4 + 2) piercing damage, and the target must succeed on a DC 12 Constitution saving throw or be poisoned for 1 minute. The target can repeat the saving throw at the end of each of its turns, ending the effect on itself on a success."}]
}{
  :name "Clay Golem"
  :size :large
  :type :construct
  :alignment "unaligned"
  :armor-class 14
  :armor-notes "natural armor"
  :hit-points {:mean 133 :die-count 14 :die 10 :modifier 56}
  :speed "20 ft."

  :str 20
  :dex 9
  :con 18
  :int 3
  :wis 8
  :cha 1

  :damage-immunities "acid, poison, psychic; bludgeoning,"

  :condition-immunities "charmed, exhaustion, frightened, paralyzed, petrified, poisoned"
  :senses "darkvision 60 ft., passive Perception 9"
  :languages "understands the languages of its creator but can't speak"
  :challenge 9

  :traits [{:name "Acid Absorption" :description "Whenever the golem is subjected to acid damage, it takes no damage and instead regains a number of hit points equal to the acid damage dealt."}
           {:name "Berserk" :description "Whenever the golem starts its turn with 60 hit points or fewer, roll a d6. On a 6, the golem goes berserk. On each of its turns while berserk, the golem attacks the nearest creature it can see.
If no creature is near enough to move to and attack, the golem attacks an object, with preference for an object smaller than itself. Once the golem goes berserk, it continues to do so until it is destroyed or regains all its hit points."}
           {:name "Immutable Form" :description "The golem is immune to any spell or effect that would alter its form."}
           {:name "Magic Resistance" :description "The golem has advantage on saving throws against spells and other magical effects."}
           {:name "Magic Weapons" :description "The golem's weapon attacks are magical."}]

  :actions [{:name "Multiattack" :description "The golem makes two slam attacks."}
            {:name "Slam" :description "Melee Weapon Attack: +8 to hit, reach 5 ft., one target. Hit: 16 (2d10 + 5) bludgeoning damage. If the target is a creature, it must succeed on a DC 15 Constitution saving throw or have its hit point maximum reduced by an amount equal to the damage taken. The target dies if this attack reduces its hit point maximum to 0. The reduction lasts until removed by the greater restoration spell or other magic."}
            {:name "Haste" :notes "Recharge 5–6" :description "Until the end of its next turn, the golem magically gains a +2 bonus to its AC, has advantage on Dexterity saving throws, and can use its slam attack as a bonus action."}]
}{
  :name "Goblin"
  :size :small
  :type :humanoid
  :subtypes #{:goblinoid}
  :alignment "neutral evil"
  :armor-class 15
  :armor-notes "(leather armor, shield)"
  :hit-points {:mean 7 :die-count 2 :die 6}
  :speed "30 ft."

  :str 8
  :dex 14
  :con 10
  :int 10
  :wis 8
  :cha 8

  :skills {:stealth 6}
  :senses "darkvision 60 ft., passive Perception 9"
  :languages "Common, Goblin"
  :challenge (/ 1 4)

  :traits [{:name "Nimble Escape" :description "The goblin can take the Disengage or Hide action as a bonus action on each of its turns."}]

  :actions [{:name "Scimitar" :description "Melee Weapon Attack: +4 to hit, reach 5 ft., one target. Hit: 5 (1d6 + 2) slashing damage."}
            {:name "Shortbow" :description "Ranged Weapon Attack: +4 to hit, range 80/320 ft., one target. Hit: 5 (1d6 + 2) piercing damage."}]
}{
  :name "Flesh Golem"
  :size :medium
  :type :construct
  :alignment "neutral"
  :armor-class 9
  :hit-points {:mean 93 :die-count 11 :die 8 :modifier 44}
  :speed "30 ft."

  :str 19
  :dex 9
  :con 18
  :int 6
  :wis 10
  :cha 5

  :damage-immunities "lightning, poison; bludgeoning, piercing, and slashing from nonmagical attacks that aren't adamantine"
  :condition-immunities "charmed, exhaustion, frightened, paralyzed, petrified, poisoned"
  :senses "darkvision 60 ft., passive Perception 10"
  :languages "understands the languages of its creator but can't speak"
  :challenge 5

  :traits [{:name "Berserk" :description "On each of its turns while berserk, the golem attacks the nearest creature it can see. If no creature is near enough to move to and attack, the golem attacks an object, with preference for an object smaller than itself. Once the golem goes berserk, it continues to do so until it is destroyed or regains all its hit points.
The golem's creator, if within 60 feet of the berserk golem, can try to calm it by speaking firmly and persuasively. The golem must be able to hear its creator, who must take an action to make a DC 15 Charisma (Persuasion) check. If the check succeeds, the golem ceases being berserk. If it takes damage while still at 40 hit points or fewer, the golem might go berserk again."}
           {:name "Aversion of Fire" :description "If the golem takes fire damage, it has disadvantage on attack rolls and ability checks until the end of its next turn."}
           {:name "Immutable Form" :description "The golem is immune to any spell or effect that would alter its form."}
           {:name "Lightning Absorption" :description "Whenever the golem is subjected to lightning damage, it takes no damage and instead regains a number of hit points equal to the lightning damage dealt."}
           {:name "Magic Resistance" :description "The golem has advantage on saving throws against spells and other magical effects."}
           {:name "Magic Weapons" :description "The golem's weapon attacks are magical."}]

  :actions [{:name "Multiattack" :description "The golem makes two slam attacks."}
            {:name "Slam" :description "Melee Weapon Attack: +7 to hit, reach 5 ft., one target. Hit: 13 (2d8 + 4) bludgeoning damage."}]
}{
  :name "Iron Golem"
  :size :large
  :type :construct
  :alignment "unaligned"
  :armor-class 20
  :armor-notes "natural armor"
  :hit-points {:mean 210 :die-count 20 :die 10 :modifier 100}
  :speed "30 ft."

  :str 24
  :dex 9
  :con 20
  :int 3
  :wis 11
  :cha 1

  :damage-immunities "fire, poison, psychic; bludgeoning, piercing, and slashing from nonmagical attacks that aren't adamantine"
  :condition-immunities "charmed, exhaustion, frightened, paralyzed, petrified, poisoned"
  :senses "darkvision 120 ft., passive Perception 10"
  :languages "understands the languages of its creator but can't speak"
  :challenge 16

  :traits [{:name "Fire Absorption" :description "Whenever the golem is subjected to fire damage, it takes no damage and instead regains a number of hit points equal to the fire damage dealt."}
           {:name "Immutable Form" :description "The golem is immune to any spell or effect that would alter its form."}
           {:name "Magic Resistance" :description "The golem has advantage on saving throws against spells and other magical effects."}
           {:name "Magic Weapons" :description "The golem's weapon attacks are magical."}]

  :actions [{:name "Multiattack" :description "The golem makes two melee attacks."}
            {:name "Slam" :description "Melee Weapon Attack: +13 to hit, reach 5 ft., one target. Hit: 20 (3d8 + 7) bludgeoning damage."}
            {:name "Sword" :description "Melee Weapon Attack: +13 to hit, reach 10 ft., one target. Hit: 23 (3d10 + 7) slashing damage."}
            {:name "Poison Breath" :notes "Recharge 6" :description "The golem exhales poisonous gas in a 15-foot cone. Each creature in that area must make a DC 19 Constitution saving throw, taking 45 (10d8) poison damage on a failed save, or half as much damage on a successful one."}]
}{
  :name "Stone Golem"
  :size :large
  :type :construct
  :alignment "unaligned"
  :armor-class 17
  :armor-notes "natural armor"
  :hit-points {:mean 178 :die-count 17 :die 10 :modifier 85}
  :speed "30 ft."

  :str 22
  :dex 9
  :con 20
  :int 3
  :wis 11
  :cha 1
                                                                                                                                                                                                                                                                                                  
  :damage-immunities "poison, psychic; bludgeoning, piercing, and slashing from nonmagical attacks that aren't adamantine"
  :condition-immunities "charmed, exhaustion, frightened, paralyzed, petrified, poisoned"
  :senses "darkvision 120 ft., passive Perception 10"
  :languages "understands the languages of its creator but can't speak"
  :challenge 10

  :traits [{:name "Immutable Form" :description "The golem is immune to any spell or effect that would alter its form."}
           {:name "Magic Resistance" :description "The golem has advantage on saving throws against spells and other magical effects."}
           {:name "Magic Weapons" :description "The golem's weapon attacks are magical."}]

  :actions [{:name "Multiattack" :description "The golem makes two slam attacks."}
            {:name "Slam" :description "Melee Weapon Attack: +10 to hit, reach 5 ft., one target. Hit: 19 (3d8 + 6) bludgeoning damage."}
            {:name "Slow" :notes "Recharge 5–6" :description "The golem targets one or more creatures it can see within 10 feet of it. Each target must make a DC 17 Wisdom saving throw against this magic. On a failed save, a target can't use reactions, its speed is halved, and it can't make more than one attack on its turn. In addition, the target can take either an action or a bonus action on its turn, not both. These effects last for 1 minute. A target can repeat the saving throw at the end of each of its turns, ending the effect on itself on a success."}]
}{
  :name "Gorgon"
  :size :large
  :type :monstrosity
  :alignment "unaligned"
  :armor-class 19
  :armor-notes "natural armor"
  :hit-points {:mean 114 :die-count 12 :die 10 :modifier 48}
  :speed "40 ft."

  :str 20
  :dex 11
  :con 18
  :int 2
  :wis 12
  :cha 7

  :skills {:perception 4}
  :condition-immunities "petrified"
  :senses "darkvision 60 ft., passive Perception 14"
  :challenge 5

  :traits [{:name "Trampling Charge" :description "If the gorgon moves at least 20 feet straight toward a creature and then hits it with a gore attack on the same turn, that target must succeed on a DC 16 Strength saving throw or be knocked prone. If the target is prone, the gorgon can make one attack with its hooves against it as a bonus action."}]

  :actions [{:name "Gore" :description "Melee Weapon Attack: +8 to hit, reach 5 ft., one target. Hit: 18 (2d12 + 5) piercing damage."}
            {:name "Hooves" :description "Melee Weapon Attack: +8 to hit, reach 5 ft., one target. Hit: 16 (2d10 + 5) bludgeoning damage."}
            {:name "Petrifying Breath" :notes "Recharge 5–6" :description "The gorgon exhales petrifying gas in a 30-foot cone. Each creature in that area must succeed on a DC 13 Constitution saving throw. On a failed save, a target begins to turn to stone and is restrained. The restrained target must repeat the saving throw at the end of its next turn. On a success, the effect ends on the target. On a failure, the target is petrified until freed by the greater restoration spell or other magic."}]
}{
  :name "Grick"
  :size :medium
  :type :monstrosity
  :alignment "neutral"
  :armor-class 14
  :armor-notes "natural armor"
  :hit-points {:mean 27 :die-count 6 :die 8}
  :speed "30 ft., climb 30 ft."

  :str 14
  :dex 14
  :con 11
  :int 3
  :wis 14
  :cha 5

  :damage-resistances "bludgeoning, piercing, and slashing from nonmagical attacks"
  :senses "darkvision 60 ft., passive Perception 12"
  :challenge 2

  :traits [{:name "Stone Camouflage" :description "The grick has advantage on Dexterity (Stealth) checks made to hide in rocky terrain."}]

  :actions [{:name "Multiattack" :description "The grick makes one attack with its tentacles. If that attack hits, the grick can make one beak attack against the same target."}
            {:name "Tentacles" :description "Melee Weapon Attack: +4 to hit, reach 5 ft., one target. Hit: 9 (2d6 + 2) slashing damage."}
            {:name "Beak" :description "Melee Weapon Attack: +4 to hit, reach 5 ft., one target. Hit: 5 (1d6 + 2) piercing damage."}]
}{
  :name "Griffon"
  :size :large
  :type :monstrosity
  :alignment "unaligned"
  :armor-class 12
  :hit-points {:mean 59 :die-count 7 :die 10 :modifier 21}
  :speed "30 ft., fly 80 ft."

  :str 18
  :dex 15
  :con 16
  :int 2
  :wis 13
  :cha 8
                                                                                                                                       
  :skills {:perception 5}
  :senses "darkvision 60 ft., passive Perception 15"
  :challenge 2

  :traits [{:name "Keen Sight" :description "The griffon has advantage on Wisdom (Perception) checks that rely on sight."}]

  :actions [{:name "Multiattack" :description "The griffon makes two attacks: one with its beak and one with its claws."}
            {:name "Beak" :description "Melee Weapon Attack: +6 to hit, reach 5 ft., one target. Hit: 8 (1d8 + 4) piercing damage."}
            {:name "Claws" :description "Melee Weapon Attack: +6 to hit, reach 5 ft., one target. Hit: 11 (2d6 + 4) slashing damage."}]
}{
  :name "Grimlock"
  :size :medium
  :type :humanoid
  :subtypes #{:grimlock}
  :alignment "neutral evil"
  :armor-class 11
  :hit-points {:mean 11 :die-count 2 :die 8 :modifier 2}
  :speed "30 ft."

  :str 16
  :dex 12
  :con 12
  :int 9
  :wis 8
  :cha 6

  :skills {:athletics 5, :perception 3, :stealth 3}
  :condition-immunities "blinded"
  :senses "blindsight 30 ft. or 10 ft. while deafened (blind beyond this radius), passive Perception 13"
  :languages "Undercommon"
  :challenge (/ 1 4)

  :traits [{:name "Blind Senses" :description "The grimlock can't use its blindsight while deafened and unable to smell.
Keen Hearing and Smell. The grimlock has advantage on Wisdom (Perception) checks that rely on hearing or smell."}
           {:name "Stone Camouflage" :description "The grimlock has advantage on Dexterity (Stealth) checks made to hide in rocky terrain."}]

  :actions [{:name "Spiked Bone Club" :description "Melee Weapon Attack: +5 to hit, reach 5 ft., one target. Hit: 5 (1d4 + 3) bludgeoning damage plus 2 (1d4) piercing damage."}]
}{
  :name "Green Hag"
  :size :medium
  :type :fey
  :alignment "neutral evil"
  :armor-class 17
  :armor-notes "natural armor"
  :hit-points {:mean 82 :die-count 11 :die 8 :modifier 33}
  :speed "30 ft."

  :str 18
  :dex 12
  :con 16
  :int 13
  :wis 14
  :cha 14

  :skills {:arcana 3, :deception 4, :perception 4, :stealth 3}
  :senses "darkvision 60 ft., passive Perception 14"
  :languages "Common, Draconic, Sylvan"
  :challenge 3

  :traits [{:name "Amphibious" :description "The hag can breathe air and water."}
           {:name "Innate Spellcasting" :description "The hag's innate spellcasting ability is Charisma (spell save DC 12). 
She can innately cast the following spells, requiring no material components:
At will: dancing lights, minor illusion, vicious mockery"}
           {:name "Mimicry" :description "The hag can mimic animal sounds and humanoid voices. A creature that hears the sounds can tell they are imitations with a successful DC 14 Wisdom (Insight) check."}]

  :actions [{:name "Claws" :description "Melee Weapon Attack: +6 to hit, reach 5 ft., one target. Hit: 13 (2d8 + 4) slashing damage."}
            {:name "Illusory Appearance" :description "The hag covers herself and anything she is wearing or carrying with a magical illusion that makes her look like another creature of her general size and humanoid shape. The illusion ends if the hag takes a bonus action to end it or if she dies.
The changes wrought by this effect fail to hold up to physical inspection. For example, the hag could appear to have smooth skin, but someone touching her would feel her rough flesh. Otherwise, a creature must take an action to visually inspect the illusion and succeed on a DC 20 Intelligence (Investigation) check to discern that the hag is disguised."}
            {:name "Invisible Passage" :description "The hag magically turns invisible until she attacks or casts a spell, or until her concentration ends (as if concentrating on a spell). While invisible, she leaves no physical evidence of her passage, so she can be tracked only by magic. Any equipment she wears or carries is invisible with her."}]
}{
  :name "Night Hag"
  :size :medium
  :type :fiend
  :alignment "neutral evil"
  :armor-class 17
  :armor-notes "natural armor"
  :hit-points {:mean 112 :die-count 15 :die 8 :modifier 45}
  :speed "30 ft."

  :str 18
  :dex 15
  :con 16
  :int 16
  :wis 14
  :cha 16

  :skills {:deception 7, :insight 6, :perception 6, :stealth 6}
  :damage-resistances "cold, fire; bludgeoning, piercing, and slashing from nonmagical attacks not made with silvered weapons"
  :condition-immunities "charmed"
  :senses "darkvision 120 ft., passive Perception 16"
  :languages "Abyssal, Common, Infernal, Primordial"
  :challenge 5

  :traits [{:name "Innate Spellcasting" :description "The hag's innate spellcasting ability is Charisma (spell save DC 14, +6 to hit with spell attacks). 
She can innately cast the following spells, requiring no material components:
At will: detect magic, magic missile
2/day each: plane shift (self only), ray of enfeeblement, sleep"}
           {:name "Magic Resistance" :description "The hag has advantage on saving throws against spells and other magical effects."}]

  :actions [{:name "Claws" :notes "Hag Form Only" :description "Melee Weapon Attack: +7 to hit, reach 5 ft., one target. Hit: 13 (2d8 + 4) slashing damage."}
            {:name "Change Shape" :description "The hag magically polymorphs into a Small or Medium female humanoid, or back into her"}
            {:name "true form" :description "Her statistics are the same in each form. Any equipment she is wearing or carrying isn't transformed. She reverts to her true form if she dies."}
            {:name "Etherealness" :description "The hag magically enters the Ethereal Plane from the Material Plane, or vice versa. To do so, the hag must have a heartstone in her possession."}
            {:name "Nightmare Haunting" :notes "1/Day" :description "While on the Ethereal Plane, the hag magically touches a sleeping humanoid on the Material Plane. A protection from evil and good spell cast on the target prevents this contact, as does a magic circle. As long as the contact persists, the target has dreadful visions. If these visions last for at least 1 hour, the target gains no benefit from its rest, and its hit point maximum is reduced by 5 (1d10). If this effect reduces the target's hit point maximum to 0, the target dies, and if the target was evil, its soul is trapped in the hag's soul bag. The reduction to the target's hit point maximum lasts until removed by the greater restoration spell or similar magic."}]
}{
  :name "Sea Hag"
  :size :medium
  :type :fey
  :alignment "chaotic evil"
  :armor-class 14
  :armor-notes "natural armor"
  :hit-points {:mean 52 :die-count 7 :die 8 :modifier 21}
  :speed "30 ft., swim 40 ft."

  :str 16
  :dex 13
  :con 16
  :int 12
  :wis 12
  :cha 13
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         
  :senses "darkvision 60 ft., passive Perception 11"
  :languages "Aquan, Common, Giant"
  :challenge 2

  :traits [{:name "Amphibious" :description "The hag can breathe air and water."}
           {:name "Horrific Appearance" :description "Any humanoid that starts its turn within 30 feet of the hag and can see the hag's true form must make a DC 11 Wisdom saving throw. On a failed save, the creature is frightened for 1 minute. A creature can repeat the saving throw at the end of each of its turns, with disadvantage if the hag is within line of sight, ending the effect on itself on a success. If a creature's saving throw is successful or the effect ends for it, the creature is immune to the hag's Horrific Appearance for the next 24 hours.
Unless the target is surprised or the revelation of the hag's true form is sudden, the target can avert its eyes and avoid making the initial saving throw. Until the start of its next turn, a creature that averts its eyes has disadvantage on attack rolls against the hag."}]

  :actions [{:name "Claws" :description "Melee Weapon Attack: +5 to hit, reach 5 ft., one target. Hit: 10 (2d6 + 3) slashing damage."}
            {:name "Death Glare" :description "The hag targets one frightened creature she can see within 30 feet of her. If the target can see the hag, it must succeed on a DC 11 Wisdom saving throw against this magic or drop to 0 hit points."}
            {:name "Illusory Appearance" :description "The hag covers herself and anything she is wearing or carrying with a magical illusion that makes her look like an ugly creature of her general size and humanoid shape. The effect ends if the hag takes a bonus action to end it or if she dies.
The changes wrought by this effect fail to hold up to physical inspection. For example, the hag could appear to have no claws, but someone touching her hand might feel the claws. Otherwise, a creature must take an action to visually inspect the illusion and succeed on a DC 16 Intelligence (Investigation) check to discern that the hag is disguised."}]
}{
  :name "Half-Red Dragon Veteran"
  :size :medium
  :type :humanoid
  :subtypes #{:human}
  :alignment "any alignment"
  :armor-class 18
  :armor-notes "(plate)"
  :hit-points {:mean 65 :die-count 10 :die 8 :modifier 20}
  :speed "30 ft."

  :str 16
  :dex 13
  :con 14
  :int 10
  :wis 11
  :cha 10

  :skills {:athletics 5, :perception 2}
  :damage-resistances "fire"
  :senses "blindsight 10 ft., darkvision 60 ft., passive Perception 12"
  :languages "Common, Draconic"
  :challenge 5

  :actions [{:name "Multiattack" :description "The veteran makes two longsword attacks. If it has a shortsword drawn, it can also make a shortsword attack."}
            {:name "Longsword" :description "Melee Weapon Attack: +5 to hit, reach 5 ft., one target. Hit: 7 (1d8 + 3) slashing damage, or 8 (1d10 + 3) slashing damage if used with two hands."}
            {:name "Shortsword" :description "Melee Weapon Attack: +5 to hit, reach 5 ft., one target. Hit: 6 (1d6 + 3) piercing damage."}
            {:name "Heavy Crossbow" :description "Ranged Weapon Attack: +3 to hit, range 100/400 ft., one target. Hit: 6 (1d10 + 1) piercing damage."}
            {:name "Fire Breath" :notes "Recharge 5–6" :description "The veteran exhales fire in a 15-foot cone. Each creature in that area must make a DC 15 Dexterity saving throw, taking 24 (7d6) fire damage on a failed save, or half as much damage on a successful one."}]
}{
  :name "Harpy"
  :size :medium
  :type :monstrosity
  :alignment "chaotic evil"
  :armor-class 11
  :hit-points {:mean 38 :die-count 7 :die 8 :modifier 7}
  :speed "20 ft., fly 40 ft."

  :str 12
  :dex 13
  :con 12
  :int 7
  :wis 10
  :cha 13

  :senses "passive Perception 10"
  :languages "Common"
  :challenge 1

  :actions [{:name "Multiattack" :description "The harpy makes two attacks: one with its claws and one with its club."}
            {:name "Claws" :description "Melee Weapon Attack: +3 to hit, reach 5 ft., one target. Hit: 6 (2d4 + 1) slashing damage."}
            {:name "Club" :description "Melee Weapon Attack: +3 to hit, reach 5 ft., one target. Hit: 3 (1d4 + 1) bludgeoning damage."}
            {:name "Luring Song" :description "The harpy sings a magical melody. Every humanoid and giant within 300 feet of the harpy that can hear the song must succeed on a DC 11 Wisdom saving throw or be charmed until the song ends. The harpy must take a bonus action on its subsequent turns to continue singing. It can stop singing at any time. The song ends if the harpy is incapacitated.
While charmed by the harpy, a target is incapacitated and ignores the songs of other harpies. If the charmed target is more than 5 feet away from the harpy, the target must move on its turn toward the harpy by the most direct route, trying to get within 5 feet. It doesn't avoid opportunity attacks, but before moving into damaging terrain, such as lava or a pit, and whenever it takes damage from a source other than the harpy, the target can repeat the saving throw. A charmed target can also repeat the saving throw at the end of each of its turns. If the saving throw is successful, the effect ends on it.
A target that successfully saves is immune to this harpy's song for the next 24 hours."}]
}{
  :name "Hell Hound"
  :size :medium
  :type :fiend
  :alignment "lawful evil"
  :armor-class 15
  :armor-notes "natural armor"
  :hit-points {:mean 45 :die-count 7 :die 8 :modifier 14}
  :speed "50 ft."

  :str 17
  :dex 12
  :con 14
  :int 6
  :wis 13
  :cha 6

  :skills {:perception 5}
  :damage-immunities "fire"
  :senses "darkvision 60 ft., passive Perception 15"
  :languages "understands Infernal but can't speak it"
  :challenge 3

  :traits [{:name "Keen Hearing and Smell" :description "The hound has advantage on Wisdom (Perception) checks that rely on hearing or smell."}
           {:name "Pack Tactics" :description "The hound has advantage on an attack roll against a creature if at least one of the hound's"}]

  :actions [{:name "Bite" :description "Melee Weapon Attack: +5 to hit, reach 5 ft., one target. Hit: 7 (1d8 + 3) piercing damage plus 7 (2d6) fire damage."}
            {:name "Fire Breath" :notes "Recharge 5–6" :description "The hound exhales fire in a 15-foot cone. Each creature in that area must make a DC 12 Dexterity saving throw, taking 21 (6d6) fire damage on a failed save, or half as much damage on a successful one."}]
}{
  :name "Hippogriff"
  :size :large
  :type :monstrosity
  :alignment "unaligned"
  :armor-class 11
  :hit-points {:mean 19 :die-count 3 :die 10 :modifier 3}
  :speed "40 ft., fly 60 ft."

  :str 17
  :dex 13
  :con 13
  :int 2
  :wis 12
  :cha 8

  :skills {:perception 5}
  :senses "passive Perception 15"
  :challenge 1

  :traits [{:name "Keen Sight" :description "The hippogriff has advantage on Wisdom (Perception) checks that rely on sight."}]

  :actions [{:name "Multiattack" :description "The hippogriff makes two attacks: one with its beak and one with its claws."}
            {:name "Beak" :description "Melee Weapon Attack: +5 to hit, reach 5 ft., one target. Hit: 8 (1d10 + 3) piercing damage."}
            {:name "Claws" :description "Melee Weapon Attack: +5 to hit, reach 5 ft., one target. Hit: 10 (2d6 + 3) slashing damage."}]
}{
  :name "Hobgoblin"
  :size :medium
  :type :humanoid
  :subtypes #{:goblinoid}
  :alignment "lawful evil"
  :armor-class 18
  :armor-notes "chain mail, shield"
  :hit-points {:mean 11 :die-count 2 :die 8 :modifier 2}
  :speed "30 ft."

  :str 13
  :dex 12
  :con 12
  :int 10
  :wis 10
  :cha 9

  :senses "darkvision 60 ft., passive Perception 10"
  :languages "Common, Goblin"
  :challenge (/ 1 2)

  :traits [{:name "Martial Advantage" :description "Once per turn, the hobgoblin can deal an extra 7 (2d6) damage to a creature it hits with a weapon attack if that creature is within 5 feet of an ally of the hobgoblin that isn't incapacitated."}]

  :actions [{:name "Longsword" :description "Melee Weapon Attack: +3 to hit, reach 5 ft., one target. Hit: 5 (1d8 + 1) slashing damage, or 6 (1d10 + 1) slashing damage if used with two hands."}
            {:name "Longbow" :description "Ranged Weapon Attack: +3 to hit, range 150/600 ft., one target. Hit: 5 (1d8 + 1) piercing damage."}]
}{
  :name "Homunculus"
  :size :tiny
  :type :construct
  :alignment "neutral"
  :armor-class 13
  :armor-notes "natural armor"
  :hit-points {:mean 5 :die-count 2 :die 4}
  :speed "20 ft., fly 40 ft."

  :str 4
  :dex 15
  :con 11
  :int 10
  :wis 10
  :cha 7
                                                                                                                                                 
  :damage-immunities "poison"
  :condition-immunities "charmed, poisoned"
  :senses "darkvision 60 ft., passive Perception 10"
  :languages "understands the languages of its creator but can't speak"
  :challenge 0

  :traits [{:name "Telepathic Bond" :description "While the homunculus is on the same plane of existence as its master, it can magically convey what it senses to its master, and the two can communicate  telepathically."}]

  :actions [{:name "Bite" :description "Melee Weapon Attack: +4 to hit, reach 5 ft., one creature. Hit: 1 piercing damage, and the target must succeed on a DC 10 Constitution saving throw or be poisoned for 1 minute. If the saving throw fails by 5 or more, the target is instead poisoned for 5 (1d10) minutes and unconscious while poisoned in this way."}]
}{
  :name "Hydra"
  :size :huge
  :type :monstrosity
  :alignment "unaligned"
  :armor-class 15
  :armor-notes "natural armor"
  :hit-points {:mean 172 :die-count 15 :die 12 :modifier 75}
  :speed "30 ft., swim 30 ft."

  :str 20
  :dex 12
  :con 20
  :int 2
  :wis 10
  :cha 7

  :skills {:perception 6}
  :senses "darkvision 60 ft., passive Perception 16"
  :challenge 8

  :traits [{:name "Hold Breath" :description "The hydra can hold its breath for 1 hour."}
           {:name "Multiple Heads" :description "The hydra has five heads. While it has more than one head, the hydra has advantage on saving throws against being blinded, charmed, deafened, frightened, stunned, and knocked unconscious.
Whenever the hydra takes 25 or more damage in a single turn, one of its heads dies. If all its heads die, the hydra dies.
At the end of its turn, it grows two heads for each of its heads that died since its last turn, unless it has taken fire damage since its last turn. The hydra regains 10 hit points for each head regrown in this way."}
           {:name "Reactive Heads" :description "For each head the hydra has beyond one, it gets an extra reaction that can be used only for opportunity attacks."}
           {:name "Wakeful" :description "While the hydra sleeps, at least one of its heads is awake."}]

  :actions [{:name "Multiattack" :description "The hydra makes as many bite attacks as it has heads."}
            {:name "Bite" :description "Melee Weapon Attack: +8 to hit, reach 10 ft., one target. Hit: 10 (1d10 + 5) piercing damage."}]
}{
  :name "Invisible Stalker"
  :size :medium
  :type :elemental
  :alignment "neutral"
  :armor-class 14
  :hit-points {:mean 104 :die-count 16 :die 8 :modifier 32}
  :speed "50 ft., fly 50 ft. (hover)"
                                                                                                                                          
  :str 16
  :dex 19
  :con 14
  :int 10
  :wis 15
  :cha 11

  :skills {:perception 8, :stealth 10}
  :damage-resistances "bludgeoning, piercing, and slashing from nonmagical attacks"
  :damage-immunities "poison"
  :condition-immunities "exhaustion, grappled, paralyzed, petrified, poisoned, prone, restrained, unconscious"
  :senses "darkvision 60 ft., passive Perception 18"
  :languages "Auran, understands Common but doesn't speak it"
  :challenge 6

  :traits [{:name "Invisibility" :description "The stalker is invisible."}
           {:name "Faultless Tracker" :description "The stalker is given a quarry by its summoner. The stalker knows the direction and distance to its quarry as long as the two of them are on the same plane of existence. The stalker also knows the location of its summoner."}]

  :actions [{:name "Multiattack" :description "The stalker makes two slam attacks."}
            {:name "Slam" :description "Melee Weapon Attack: +6 to hit, reach 5 ft., one target. Hit: 10 (2d6 + 3) bludgeoning damage."}]
}{
  :name "Kobold"
  :size :small
  :type :humanoid
  :subtypes #{:kobold}
  :alignment "lawful evil"
  :armor-class 12
  :hit-points {:mean 5 :die-count 2 :die 6 :modifier -2}
  :speed "30 ft."

  :str 7
  :dex 15
  :con 9
  :int 8
  :wis 7
  :cha 8

  :senses "darkvision 60 ft., passive Perception 8"
  :languages "Common, Draconic"
  :challenge (/ 1 8)

  :traits [{:name "Sunlight Sensitivity" :description "While in sunlight, the kobold has disadvantage on attack rolls, as well as on Wisdom (Perception) checks that rely on sight."}
           {:name "Pack Tactics" :description "The kobold has advantage on an attack roll against a creature if at least one of the kobold's allies is within 5 feet of the creature and the ally isn't incapacitated."}]

  :actions [{:name "Dagger" :description "Melee Weapon Attack: +4 to hit, reach 5 ft., one target. Hit: 4 (1d4 + 2) piercing damage."}
            {:name "Sling" :description "Ranged Weapon Attack: +4 to hit, range 30/120 ft., one target. Hit: 4 (1d4 + 2) bludgeoning damage."}]
}{
  :name "Kraken"
  :size :gargantuan
  :type :monstrosity
  :subtypes #{:titan}
  :alignment "chaotic evil"
  :armor-class 18
  :armor-notes "natural armor"
  :hit-points {:mean 472 :die-count 27 :die 20 :modifier 189}
  :speed "20 ft., swim 60 ft."

  :str 30
  :dex 11
  :con 25
  :int 22
  :wis 18
  :cha 20

  :saving-throws {:str 17, :dex 7, :con 14, :int 13, :wis 11}
  :damage-immunities "lightning; bludgeoning, piercing, and slashing from nonmagical attacks"
  :condition-immunities "frightened, paralyzed"
  :senses "truesight 120 ft., passive Perception 14"
  :languages "understands Abyssal, Celestial, Infernal, and Primordial but can't speak, telepathy 120 ft."
  :challenge 23

  :traits [{:name "Amphibious" :description "The kraken can breathe air and water."}
           {:name "Freedom of Movement" :description "The kraken ignores difficult terrain, and magical effects can't reduce its speed or cause it to be restrained. It can spend 5 feet of movement to escape from nonmagical restraints or being grappled."}
           {:name "Siege Monster" :description "The kraken deals double damage to objects and structures."}]

  :actions [{:name "Multiattack" :description "The kraken makes three tentacle attacks, each of which it can replace with one use of Fling."}
            {:name "Bite" :description "Melee Weapon Attack: +17 to hit, reach 5 ft., one target. Hit: 23 (3d8 + 10) piercing damage. If the target is a Large or smaller creature grappled by the kraken, that creature is swallowed, and the grapple ends.
While swallowed, the creature is blinded and restrained, it has total cover against attacks and other effects outside the kraken, and it takes 42 (12d6) acid damage at the start of each of the kraken's turns.
If the kraken takes 50 damage or more on a single turn from a creature inside it, the kraken must succeed on a DC 25 Constitution saving throw at the end of that turn or regurgitate all swallowed creatures, which fall prone in a space within 10 feet of the kraken. If the kraken dies, a swallowed creature is no longer restrained by it and can escape from the corpse using 15 feet of movement, exiting prone."}
            {:name "Tentacle" :description "Melee Weapon Attack: +17 to hit, reach 30 ft., one target. Hit: 20 (3d6 + 10) bludgeoning damage, and the target is grappled (escape DC 18). Until this grapple ends, the target is restrained. The kraken has ten tentacles, each of which can grapple one target."}
            {:name "Fling" :description "One Large or smaller object held or creature grappled by the kraken is thrown up to 60 feet in a random direction and knocked prone. If a thrown target strikes a solid surface, the target takes 3 (1d6) bludgeoning damage for every 10 feet it was thrown. If the target is thrown at another creature, that creature must succeed on a DC 18 Dexterity saving throw or take the same damage and be knocked prone."}
            {:name "Lightning Storm" :description "The kraken magically creates three bolts of lightning, each of which can strike a target the kraken can see within 120 feet of it. A target must make a DC 23 Dexterity saving throw, taking 22 (4d10) lightning damage on a failed save, or half as much damage on a successful one."}]

  :legendary-actions {:description "The kraken can take 3 legendary actions, choosing from the options below. Only one legendary action option can be used at a time and only at the end of another creature's turn. The kraken regains spent legendary actions at the start of its turn."
                      :actions [{:name "Tentacle Attack or Fling" :description "The kraken makes one tentacle attack or uses its Fling."}
                                {:name "Lightning Storm" :notes "Costs 2 Actions" :description "The kraken uses Lightning Storm."}
                                {:name "Ink Cloud" :notes "Costs 3 Actions" :description "While underwater, the kraken expels an ink cloud in a 60-foot radius. The cloud spreads around corners, and that area is heavily obscured to creatures other than the kraken. Each creature other than the kraken that ends its turn there must succeed on a DC 23 Constitution saving throw, taking 16 (3d10) poison damage on a failed save, or half as much damage on a successful one. A strong current disperses the cloud, which otherwise disappears at the end of the kraken's next turn."}]}
}{
  :name "Lamia"
  :size :large
  :type :monstrosity
  :alignment "chaotic evil"
  :armor-class 13
  :armor-notes "natural armor"
  :hit-points {:mean 97 :die-count 13 :die 10 :modifier 26}
  :speed "30 ft."

  :str 16
  :dex 13
  :con 15
  :int 14
  :wis 15
  :cha 16

  :skills {:deception 7, :insight 4, :stealth 3}
  :senses "darkvision 60 ft., passive Perception 12"
  :languages "Abyssal, Common"
  :challenge 4

  :traits [{:name "Innate Spellcasting" :description "The lamia's innate spellcasting ability is Charisma (spell save DC 13). 
It can innately cast the following spells, requiring no material components.
At will: disguise self (any humanoid form), major image
3/day each: charm person, mirror image, scrying, suggestion
1/day: geas"}]

  :actions [{:name "Multiattack" :description "The lamia makes two attacks: one with its claws and one with its dagger or Intoxicating Touch."}
            {:name "Claws" :description "Melee Weapon Attack: +5 to hit, reach 5 ft., one target. Hit: 14 (2d10 + 3) slashing damage."}
            {:name "Dagger" :description "Melee Weapon Attack: +5 to hit, reach 5 ft., one target. Hit: 5 (1d4 + 3) piercing damage."}
            {:name "Intoxicating Touch" :description "Melee Spell Attack: +5 to hit, reach 5 ft., one creature. Hit: The target is magically cursed for 1 hour. Until the curse ends, the target has disadvantage on Wisdom saving throws and all ability checks."}]
}{
  :name "Lich"
  :size :medium
  :type :undead
  :alignment "any evil alignment"
  :armor-class 17
  :armor-notes "natural armor"
  :hit-points {:mean 135 :die-count 18 :die 8 :modifier 54}
  :speed "30 ft."

  :str 11
  :dex 16
  :con 16
  :int 20
  :wis 14
  :cha 16
                                                                                                                                                                                                                                                                      
  :saving-throws {:con 10, :int 12, :wis 9}
  :skills {:arcana 18, :history 12, :insight 9, :perception 9}
  :damage-resistances "cold, lightning, necrotic"
  :damage-immunities "poison; bludgeoning, piercing, and slashing from nonmagical attacks"
  :condition-immunities "charmed, exhaustion, frightened, paralyzed, poisoned"
  :senses "truesight 120 ft., passive Perception 19"
  :languages "Common plus up to five other languages"
  :challenge 21

  :traits [{:name "Legendary Resistance" :notes "3/Day" :description "If the lich fails a saving throw, it can choose to succeed instead."}
           {:name "Rejuvenation" :description "If it has a phylactery, a destroyed lich gains a new body in 1d10 days, regaining all its hit points and becoming active again. The new body appears within 5 feet of the phylactery."}
           {:name "Spellcasting" :description "The lich is an 18th-level spellcaster. Its spellcasting ability is Intelligence (spell save DC 20, +12 to hit with spell attacks). 
The lich has the following wizard spells prepared:
Cantrips (at will): mage hand, prestidigitation, ray of frost
1st level (4 slots): detect magic, magic missile, shield, thunderwave
2nd level (3 slots): acid arrow, detect thoughts, invisibility, mirror image
3rd level (3 slots): animate dead, counterspell, dispel magic, fireball
4th level (3 slots): blight, dimension door
5th level (3 slots): cloudkill, scrying
6th level (1 slot): disintegrate, globe of invulnerability
7th level (1 slot): finger of death, plane shift
8th level (1 slot): dominate monster, power word stun
9th level (1 slot): power word kill"}
           {:name "Turn Resistance" :description "The lich has advantage on saving throws against any effect that turns undead."}]

  :actions [{:name "Paralyzing Touch" :description "Melee Spell Attack: +12 to hit, reach 5 ft., one creature. Hit: 10 (3d6) cold damage. The target must succeed on a DC 18 Constitution saving throw or be paralyzed for 1 minute. The target can repeat the saving throw at the end of each of its turns, ending the effect on itself on a success."}]

  :legendary-actions {:description "The lich can take 3 legendary actions, choosing from the options below. Only one legendary action option can be used at a time and only at the end of another creature's turn. The lich regains spent legendary actions at the start of its turn."
                      :actions [{:name "Cantrip" :description "The lich casts a cantrip."}
                                {:name "Paralyzing Touch" :notes "Costs 2 Actions" :description "The lich uses its Paralyzing Touch."}
                                {:name "Frightening Gaze" :notes "Costs 2 Actions" :description "The lich fixes its gaze on one creature it can see within 10 feet of it. The target must succeed on a DC 18 Wisdom saving throw against this magic or become frightened for 1 minute. The frightened target can repeat the saving throw at the end of each of its turns, ending the effect on itself on a success. If a target's saving throw is successful or the effect ends for it, the target is immune to the lich's gaze for the next 24 hours."}
                                {:name "Disrupt Life" :notes "Costs 3 Actions" :description "Each living creature within 20 feet of the lich must make a DC 18"}]}
}{
  :name "Lizardfolk"
  :size :medium
  :type :humanoid
  :subtypes #{:lizardfolk}
  :alignment "neutral"
  :armor-class 15
  :armor-notes "natural armor, shield"
  :hit-points {:mean 22 :die-count 4 :die 8 :modifier 4}
  :speed "30 ft., swim 30 ft."

  :str 15
  :dex 10
  :con 13
  :int 7
  :wis 12
  :cha 7

  :skills {:perception 3, :stealth 4, :survival 5}
  :senses "passive Perception 13"
  :languages "Draconic"
  :challenge (/ 1 2)
  :traits [{:name "Hold Breath" :description "The lizardfolk can hold its breath for 15 minutes."}]

  :actions [{:name "Multiattack" :description "The lizardfolk makes two melee attacks, each one with a different weapon."}
            {:name "Bite" :description "Melee Weapon Attack: +4 to hit, reach 5 ft., one target. Hit: 5 (1d6 + 2) piercing damage."}
            {:name "Heavy Club" :description "Melee Weapon Attack: +4 to hit, reach 5 ft., one target. Hit: 5 (1d6 + 2) bludgeoning damage."}
            {:name "Javelin" :description "Melee or Ranged Weapon Attack: +4 to hit, reach 5 ft. or range 30/120 ft., one target. Hit: 5 (1d6 + 2) piercing damage."}
            {:name "Spiked Shield" :description "Melee Weapon Attack: +4 to hit, reach 5 ft., one target. Hit: 5 (1d6 + 2) piercing damage."}]
}{
  :name "Werebear"
  :size :medium
  :type :humanoid
  :subtypes #{:human, :shapechanger}
  :alignment "neutral good"
  :armor-class 10
  :armor-notes "in humanoid form, 11 natural armor in bear and hybrid form"
  :hit-points {:mean 135 :die-count 18 :die 8 :modifier 54}
  :speed "30 ft. (40 ft., climb 30 ft. in bear or hybrid form)"

  :str 19
  :dex 10
  :con 17
  :int 11
  :wis 12
  :cha 12

  :skills {:perception 7}
  :damage-immunities "bludgeoning, piercing, and slashing from nonmagical attacks not made with silvered weapons"
  :senses "passive Perception 17"
  :languages "Common (can't speak in bear form)"
  :challenge 5

  :traits [{:name "Shapechanger" :description "The werebear can use its action to polymorph into a Large bear-humanoid hybrid or into a Large bear, or back into its true form, which is humanoid. Its statistics, other than its size and AC, are the same in each form. Any equipment it is wearing or carrying isn't transformed. It reverts to its true form if it dies."}
           {:name "Keen Smell" :description "The werebear has advantage on Wisdom (Perception) checks that rely on smell."}]

  :actions [{:name "Multiattack" :description "In bear form, the werebear makes two claw attacks. In humanoid form, it makes two greataxe attacks. In hybrid form, it can attack like a bear or a humanoid."}
            {:name "Bite" :notes "Bear or Hybrid Form Only" :description "Melee Weapon Attack: +7 to hit, reach 5 ft., one target. Hit: 15 (2d10 + 4) piercing damage. If the target is a humanoid, it must succeed on a DC 14 Constitution saving throw or be cursed with werebear lycanthropy."}
            {:name "Claw" :notes "Bear or Hybrid Form Only" :description "Melee Weapon Attack: +7 to hit, reach 5 ft., one target. Hit: 13 (2d8 + 4) slashing damage."}
            {:name "Greataxe" :notes "Humanoid or Hybrid Form Only" :description "Melee Weapon Attack: +7 to hit, reach 5 ft., one target. Hit: 10 (1d12 + 4) slashing damage."}]
}{
  :name "Wereboar"
  :size :medium
  :type :humanoid
  :subtypes #{:human, :shapechanger}
  :alignment "neutral evil"
  :armor-class 10
  :armor-notes "in humanoid form, 11 natural armor in boar or hybrid form"
  :hit-points {:mean 78 :die-count 12 :die 8 :modifier 24}
  :speed "30 ft. (40 ft. in boar form)"

  :str 17
  :dex 10
  :con 15
  :int 10
  :wis 11
  :cha 8

  :skills {:perception 2}
  :damage-immunities "bludgeoning, piercing, and slashing from nonmagical attacks not made with silvered weapons"
  :senses "passive Perception 12"
  :languages "Common (can't speak in boar form)"
  :challenge 4

  :traits [{:name "Shapechanger" :description "The wereboar can use its action to polymorph into a boar-humanoid hybrid or into a boar, or back into its true form, which is humanoid. Its statistics, other than its AC, are the same in each form. Any equipment it is wearing or carrying isn't transformed. It reverts to its true form if it dies."}
           {:name "Charge" :notes "Boar or Hybrid Form Only" :description "If the wereboar moves at least 15 feet straight toward a target and then hits it with its tusks on the same turn, the target takes an extra 7 (2d6) slashing damage. If the target is a creature, it must succeed on a DC 13 Strength saving throw or be knocked prone."}
           {:name "Relentless" :notes "Recharges after a Short or Long Rest" :description "If the wereboar takes 14 damage or less that would reduce it to 0 hit points, it is reduced to 1 hit point instead."}]

  :actions [{:name "Multiattack" :notes "Humanoid or Hybrid Form Only" :description "The wereboar makes two attacks, only one of which can be with its tusks."}
            {:name "Maul" :notes "Humanoid or Hybrid Form Only" :description "Melee Weapon Attack: +5 to hit, reach 5 ft., one target. Hit: 10 (2d6 + 3) bludgeoning damage."}
            {:name "Tusks" :notes "Boar or Hybrid Form Only" :description "Melee Weapon Attack: +5 to hit, reach 5 ft., one target. Hit: 10 (2d6 + 3) slashing damage. If the target is a humanoid, it must succeed on a DC 12 Constitution saving throw or be cursed with wereboar lycanthropy."}]
}{
  :name "Wererat"
  :size :medium
  :type :humanoid
  :subtypes #{:human, :shapechanger}
  :alignment "lawful evil"
  :armor-class 12
  :hit-points {:mean 33 :die-count 6 :die 8 :modifier 6}
  :speed "30 ft."

  :str 10
  :dex 15
  :con 12
  :int 11
  :wis 10
  :cha 8
                                                                                                                                                                                                                                                                                                     
  :skills {:perception 2, :stealth 4}
  :damage-immunities "bludgeoning, piercing, and slashing from nonmagical attacks not made with silvered weapons"
  :senses "darkvision 60 ft. (rat form only), passive Perception 12"
  :languages "Common (can't speak in rat form)"
  :challenge 2

  :traits [{:name "Shapechanger" :description "The wererat can use its action to polymorph into a rat-humanoid hybrid or into a giant rat, or back into its true form, which is humanoid. Its statistics, other than its size, are the same in each form. Any equipment it is wearing or carrying isn't transformed. It reverts to its true form if it dies."}
           {:name "Keen Smell" :description "The wererat has advantage on Wisdom (Perception) checks that rely on smell."}]

  :actions [{:name "Multiattack" :notes "Humanoid or Hybrid Form Only" :description "The wererat makes two attacks, only one of which can be a bite."}
            {:name "Bite" :notes "Rat or Hybrid Form Only" :description "Melee Weapon Attack:
+4 to hit, reach 5 ft., one target. Hit: 4 (1d4 + 2) piercing damage. 
If the target is a humanoid, it must succeed on a DC 11 Constitution saving throw or be cursed with wererat lycanthropy."}
            {:name "Shortsword" :notes "Humanoid or Hybrid Form Only" :description "Melee Weapon Attack: +4 to hit, reach 5 ft., one target. Hit: 5 (1d6 + 2) piercing damage."}
            {:name "Hand Crossbow" :notes "Humanoid or Hybrid Form Only" :description "Ranged Weapon Attack: +4 to hit, range 30/120 ft., one target. Hit: 5 (1d6 + 2) piercing damage."}]
}{
  :name "Weretiger"
  :size :medium
  :type :humanoid
  :subtypes #{:human, :shapechanger}
  :alignment "neutral"
  :armor-class 12
  :hit-points {:mean 120 :die-count 16 :die 8 :modifier 48}
  :speed "30 ft. (40 ft. in tiger form)"

  :str 17
  :dex 15
  :con 16
  :int 10
  :wis 13
  :cha 11

  :skills {:perception 5, :stealth 4}
  :damage-immunities "bludgeoning, piercing, and slashing from nonmagical attacks not made with silvered weapons"
  :senses "darkvision 60 ft., passive Perception 15"
  :languages "Common (can't speak in tiger form)"
  :challenge 4

  :traits [{:name "Shapechanger" :description "The weretiger can use its action to polymorph into a tiger-humanoid hybrid or into a tiger, or back into its true form, which is humanoid. Its statistics, other than its size, are the same in each form. Any equipment it is wearing or carrying isn't transformed. It reverts to its true form if it dies.
Keen Hearing and Smell. The weretiger has advantage on Wisdom (Perception) checks that rely on hearing or smell."}
           {:name "Pounce" :notes "Tiger or Hybrid Form Only" :description "If the weretiger moves at least 15 feet straight toward a creature and then hits it with a claw attack on the same turn, that target must succeed on a DC 14 Strength saving throw or be knocked prone. If the target is prone, the weretiger can make one bite attack against it as a bonus action."}]

  :actions [{:name "Multiattack" :notes "Humanoid or Hybrid Form Only" :description "In humanoid form, the weretiger makes two scimitar attacks or two longbow attacks. In hybrid form, it can attack like a humanoid or make two claw attacks."}
            {:name "Bite" :notes "Tiger or Hybrid Form Only" :description "Melee Weapon Attack: +5 to hit, reach 5 ft., one target. Hit: 8 (1d10 +"}
            {:name "piercing damage" :description "If the target is a humanoid, it must succeed on a DC 13 Constitution saving throw or be cursed with weretiger lycanthropy."}
            {:name "Claw" :notes "Tiger or Hybrid Form Only" :description "Melee Weapon Attack: +5 to hit, reach 5 ft., one target. Hit: 7 (1d8 + 3) slashing damage."}
            {:name "Scimitar" :notes "Humanoid or Hybrid Form Only" :description "Melee Weapon Attack: +5 to hit, reach 5 ft., one target. Hit: 6 (1d6 + 3) slashing damage."}
            {:name "Longbow" :notes "Humanoid or Hybrid Form Only" :description "Ranged Weapon Attack: +4 to hit, range 150/600 ft., one target. Hit: 6 (1d8 + 2) piercing damage."}]
}{
  :name "Werewolf"
  :size :medium
  :type :humanoid
  :subtypes #{:human, :shapechanger}
  :alignment "chaotic evil"
  :armor-class 11
  :armor-notes "in humanoid form, 12 natural armor in wolf or hybrid form"
  :hit-points {:mean 58 :die-count 9 :die 8 :modifier 18}
  :speed "30 ft. (40 ft. in wolf form)"

  :str 15
  :dex 13
  :con 14
  :int 10
  :wis 11
  :cha 10

  :skills {:perception 4, :stealth 3}
  :damage-immunities "bludgeoning, piercing, and slashing from nonmagical attacks not made with silvered weapons"
  :senses "passive Perception 14"
  :languages "Common (can't speak in wolf form)"
  :challenge 3

  :traits [{:name "Shapechanger" :description "The werewolf can use its action to polymorph into a wolf-humanoid hybrid or into a wolf, or back into its true form, which is humanoid. Its statistics, other than its AC, are the same in each form. Any equipment it is wearing or carrying isn't transformed. It reverts to its true form if it dies.
Keen Hearing and Smell. The werewolf has advantage on Wisdom (Perception) checks that rely on hearing or smell."}]

  :actions [{:name "Multiattack" :notes "Humanoid or Hybrid Form Only" :description "The werewolf makes two attacks: one with its bite and one with its claws or spear."}
            {:name "Bite" :notes "Wolf or Hybrid Form Only" :description "Melee Weapon Attack: +4 to hit, reach 5 ft., one target. Hit: 6 (1d8 + 2) piercing damage. If the target is a humanoid, it must succeed on a DC 12 Constitution saving throw or be cursed with werewolf lycanthropy."}
            {:name "Claws" :notes "Hybrid Form Only" :description "Melee Weapon Attack: +4 to hit, reach 5 ft., one creature. Hit: 7 (2d4 + 2) slashing damage."}
            {:name "Spear" :notes "Humanoid Form Only" :description "Melee or Ranged Weapon Attack: +4 to hit, reach 5 ft. or range 20/60 ft., one creature. Hit: 5 (1d6 + 2) piercing damage, or 6 (1d8 + 2) piercing damage if used with two hands to make a melee attack."}]
}{
  :name "Magmin"
  :size :small
  :type :elemental
  :alignment "chaotic neutral"
  :armor-class 14
  :armor-notes "natural armor"
  :hit-points {:mean 9 :die-count 2 :die 6 :modifier 2}
  :speed "30 ft."

  :str 7
  :dex 15
  :con 12
  :int 8
  :wis 11
  :cha 10

  :damage-resistances "bludgeoning, piercing, and slashing from nonmagical attacks"
  :damage-immunities "fire"
  :senses "darkvision 60 ft., passive Perception 10"
  :languages "Ignan"
  :challenge (/ 1 2)

  :traits [{:name "Death Burst" :description "When the magmin dies, it explodes in a burst of fire and magma. Each creature within 10 feet of it must make a DC 11 Dexterity saving throw, taking 7 (2d6) fire damage on a failed save, or half as much damage on a successful one. Flammable objects that aren't being worn or carried in that area are ignited."}
           {:name "Ignited Illumination" :description "As a bonus action, the magmin can set itself ablaze or extinguish its flames. While ablaze, the magmin sheds bright light in a 10-foot radius and dim light for an additional 10 feet."}]

  :actions [{:name "Touch" :description "Melee Weapon Attack: +4 to hit, reach 5 ft., one target. Hit: 7 (2d6) fire damage. If the target is a creature or a flammable object, it ignites. Until a creature takes an action to douse the fire, the target takes 3 (1d6) fire damage at the end of each of its turns."}]
}{
  :name "Manticore"
  :size :large
  :type :monstrosity
  :alignment "lawful evil"
  :armor-class 14
  :armor-notes "natural armor"
  :hit-points {:mean 68 :die-count 8 :die 10 :modifier 24}
  :speed "30 ft., fly 50 ft."

  :str 17
  :dex 16
  :con 17
  :int 7
  :wis 12
  :cha 8
                                                                                                                                                                                                                                                                                                                         
  :senses "darkvision 60 ft., passive Perception 11"
  :languages "Common"
  :challenge 3

  :traits [{:name "Tail Spike Regrowth" :description "The manticore has twenty-four tail spikes. Used spikes regrow when the manticore finishes a long rest."}]

  :actions [{:name "Multiattack" :description "The manticore makes three attacks: one with its bite and two with its claws or three with its tail spikes."}
            {:name "Bite" :description "Melee Weapon Attack: +5 to hit, reach 5 ft., one target. Hit: 7 (1d8 + 3) piercing damage."}
            {:name "Claw" :description "Melee Weapon Attack: +5 to hit, reach 5 ft., one target. Hit: 6 (1d6 + 3) slashing damage."}
            {:name "Tail Spike" :description "Ranged Weapon Attack: +5 to hit, range 100/200 ft., one target. Hit: 7 (1d8 + 3) piercing damage."}]
}{
  :name "Medusa"
  :size :medium
  :type :monstrosity
  :alignment "lawful evil"
  :armor-class 15
  :armor-notes "natural armor"
  :hit-points {:mean 127 :die-count 17 :die 8 :modifier 51}
  :speed "30 ft."

  :str 10
  :dex 15
  :con 16
  :int 12
  :wis 13
  :cha 15

  :skills {:deception 5, :insight 4, :perception 4, :stealth 5}
  :senses "darkvision 60 ft., passive Perception 14"
  :languages "Common"
  :challenge 6

  :traits [{:name "Petrifying Gaze" :description "When a creature that can see the medusa's eyes starts its turn within 30 feet of the medusa, the medusa can force it to make a DC 14 Constitution saving throw if the medusa isn't incapacitated and can see the creature. If the saving throw fails by 5 or more, the creature is instantly petrified. Otherwise, a creature that fails the save begins to turn to stone and is restrained. The restrained creature must repeat the saving throw at the end of its next turn, becoming petrified on a failure or ending the effect on a success. The petrification lasts until the creature is freed by the greater restoration spell or other magic.
Unless surprised, a creature can avert its eyes to avoid the saving throw at the start of its turn. If the creature does so, it can't see the medusa until the start of its next turn, when it can avert its eyes again. If the creature looks at the medusa in the meantime, it must immediately make the save.
If the medusa sees itself reflected on a polished surface within 30 feet of it and in an area of bright light, the medusa is, due to its curse, affected by its own gaze."}]

  :actions [{:name "Multiattack" :description "The medusa makes either three melee attacks—one with its snake hair and two with its shortsword—or two ranged attacks with its longbow."}
            {:name "Snake Hair" :description "Melee Weapon Attack: +5 to hit, reach 5 ft., one creature. Hit: 4 (1d4 + 2) piercing damage plus 14 (4d6) poison damage."}
            {:name "Shortsword" :description "Melee Weapon Attack: +5 to hit, reach 5 ft., one target. Hit: 5 (1d6 + 2) piercing damage."}
            {:name "Longbow" :description "Ranged Weapon Attack: +5 to hit, range 150/600 ft., one target. Hit: 6 (1d8 + 2) piercing damage plus 7 (2d6) poison damage."}]
}{
  :name "Dust Mephit"
  :size :small
  :type :elemental
  :alignment "neutral evil"
  :armor-class 12
  :hit-points {:mean 17 :die-count 5 :die 6}
  :speed "30 ft., fly 30 ft."

  :str 5
  :dex 14
  :con 10
  :int 9
  :wis 11
  :cha 10

  :skills {:perception 2, :stealth 4}
  :damage-vulnerabilities "fire"
  :damage-immunities "poison"
  :condition-immunities "poisoned"
  :senses "darkvision 60 ft., passive Perception 12"
  :languages "Auran, Terran"
  :challenge (/ 1 2)

  :traits [{:name "Death Burst" :description "When the mephit dies, it explodes in a burst of dust. Each creature within 5 feet of it must then succeed on a DC 10 Constitution saving throw or be blinded for 1 minute. A blinded creature can repeat the saving throw on each of its turns, ending the effect on itself on a success."}
           {:name "Innate Spellcasting" :notes "1/Day" :description "The mephit can innately cast sleep, requiring no material components. Its innate spellcasting ability is Charisma."}]

  :actions [{:name "Claws" :description "Melee Weapon Attack: +4 to hit, reach 5 ft., one creature. Hit: 4 (1d4 + 2) slashing damage."}
            {:name "Blinding Breath" :notes "Recharge 6" :description "The mephit exhales a 15- foot cone of blinding dust. Each creature in that area must succeed on a DC 10 Dexterity saving throw or be blinded for 1 minute. A creature can repeat the saving throw at the end of each of its turns, ending the effect on itself on a success."}]
}{
  :name "Ice Mephit"
  :size :small
  :type :elemental
  :alignment "neutral evil"
  :armor-class 11
  :hit-points {:mean 21 :die-count 6 :die 6}
  :speed "30 ft., fly 30 ft."

  :str 7
  :dex 13
  :con 10
  :int 9
  :wis 11
  :cha 12
                                                                                                                                                                                                                                                                                                                                                         
  :skills {:perception 2, :stealth 3}
  :damage-vulnerabilities "bludgeoning, fire"
  :damage-immunities "cold, poison"
  :condition-immunities "poisoned"
  :senses "darkvision 60 ft., passive Perception 12"
  :languages "Aquan, Auran"
  :challenge (/ 1 2)

  :traits [{:name "Death Burst" :description "When the mephit dies, it explodes in a burst of jagged ice. Each creature within 5 feet of it must make a DC 10 Dexterity saving throw, taking 4 (1d8) slashing damage on a failed save, or half as much damage on a successful one."}
           {:name "False Appearance" :description "While the mephit remains motionless, it is indistinguishable from an ordinary shard of ice."}
           {:name "Innate Spellcasting" :notes "1/Day" :description "The mephit can innately cast fog cloud, requiring no material components. Its innate spellcasting ability is Charisma."}]

  :actions [{:name "Claws" :description "Melee Weapon Attack: +3 to hit, reach 5 ft., one creature. Hit: 3 (1d4 + 1) slashing damage plus 2 (1d4) cold damage."}
            {:name "Frost Breath" :notes "Recharge 6" :description "The mephit exhales a 15- foot cone of cold air. Each creature in that area must succeed on a DC 10 Dexterity saving throw, taking 5 (2d4) cold damage on a failed save, or half as much damage on a successful one."}]
}{
  :name "Magma Mephit"
  :size :small
  :type :elemental
  :alignment "neutral evil"
  :armor-class 11
  :hit-points {:mean 22 :die-count 5 :die 6 :modifier 5}
  :speed "30 ft., fly 30 ft."
                                                                                                                                                                                                                                                                                             
  :str 8
  :dex 12
  :con 12
  :int 7
  :wis 10
  :cha 10

  :skills {:stealth 3}
  :damage-vulnerabilities "cold"
  :damage-immunities "fire, poison"
  :condition-immunities "poisoned"
  :senses "darkvision 60 ft., passive Perception 10"
  :languages "Ignan, Terran"
  :challenge (/ 1 2)

  :traits [{:name "Death Burst" :description "When the mephit dies, it explodes in a burst of lava. Each creature within 5 feet of it must make a DC 11 Dexterity saving throw, taking 7 (2d6) fire damage on a failed save, or half as much damage on a successful one."}
           {:name "False Appearance" :description "While the mephit remains motionless, it is indistinguishable from an ordinary mound of magma."}
           {:name "Innate Spellcasting" :notes "1/Day" :description "The mephit can innately cast heat metal (spell save DC 10), requiring no material components. Its innate spellcasting ability is Charisma."}]

  :actions [{:name "Claws" :description "Melee Weapon Attack: +3 to hit, reach 5 ft., one creature. Hit: 3 (1d4 + 1) slashing damage plus 2 (1d4) fire damage."}
            {:name "Fire Breath" :notes "Recharge 6" :description "The mephit exhales a 15-foot cone of fire. Each creature in that area must make a DC 11 Dexterity saving throw, taking 7 (2d6) fire damage on a failed save, or half as much damage on a successful one."}]
}{
  :name "Steam Mephit"
  :size :small
  :type :elemental
  :alignment "neutral evil"
  :armor-class 10
  :hit-points {:mean 21 :die-count 6 :die 6}
  :speed "30 ft., fly 30 ft."

  :str 5
  :dex 11
  :con 10
  :int 11
  :wis 10
  :cha 12
                                                                                                                                                                                                                                                                                 
  :damage-immunities "fire, poison"
  :condition-immunities "poisoned"
  :senses "darkvision 60 ft., passive Perception 10"
  :languages "Aquan, Ignan"
  :challenge (/ 1 4)

  :traits [{:name "Death Burst" :description "When the mephit dies, it explodes in a cloud of steam. Each creature within 5 feet of the mephit must succeed on a DC 10 Dexterity saving throw or take 4 (1d8) fire damage"}
           {:name "Innate Spellcasting" :notes "1/Day" :description "The mephit can innately cast blur, requiring no material components. Its innate spellcasting ability is Charisma."}]

  :actions [{:name "Claws" :description "Melee Weapon Attack: +2 to hit, reach 5 ft., one creature. Hit: 2 (1d4) slashing damage plus 2 (1d4) fire damage."}
            {:name "Steam Breath" :notes "Recharge 6" :description "The mephit exhales a 15- foot cone of scalding steam. Each creature in that area must succeed on a DC 10 Dexterity saving throw, taking 4 (1d8) fire damage on a failed save, or half as much damage on a successful one."}]
}{
  :name "Merfolk"
  :size :medium
  :type :humanoid
  :subtypes #{:merfolk}
  :alignment "neutral"
  :armor-class 11
  :hit-points {:mean 11 :die-count 2 :die 8 :modifier 2}
  :speed "10 ft., swim 40 ft."

  :str 10
  :dex 13
  :con 12
  :int 11
  :wis 11
  :cha 12

  :skills {:perception 2}
  :senses "passive Perception 12"
  :languages "Aquan, Common"
  :challenge (/ 1 8)

  :traits [{:name "Amphibious" :description "The merfolk can breathe air and water."}]

  :actions [{:name "Spear" :description "Melee or Ranged Weapon Attack: +2 to hit, reach 5 ft. or range 20/60 ft., one target. Hit: 3 (1d6) piercing damage, or 4 (1d8) piercing damage if used with two hands to make a melee attack."}]
}{
  :name "Merrow"
  :size :large
  :type :monstrosity
  :alignment "chaotic evil"
  :armor-class 13
  :armor-notes "natural armor"
  :hit-points {:mean 45 :die-count 6 :die 10 :modifier 12}
  :speed "10 ft., swim 40 ft."
                                                                                                                                                                                                                                           
  :str 18
  :dex 10
  :con 15
  :int 8
  :wis 10
  :cha 9

  :senses "darkvision 60 ft., passive Perception 10"
  :languages "Abyssal, Aquan"
  :challenge 2

  :traits [{:name "Amphibious" :description "The merrow can breathe air and water."}]

  :actions [{:name "Multiattack" :description "The merrow makes two attacks: one with its bite and one with its claws or harpoon."}
            {:name "Bite" :description "Melee Weapon Attack: +6 to hit, reach 5 ft., one target. Hit: 8 (1d8 + 4) piercing damage."}
            {:name "Claws" :description "Melee Weapon Attack: +6 to hit, reach 5 ft., one target. Hit: 9 (2d4 + 4) slashing damage."}
            {:name "Harpoon" :description "Melee or Ranged Weapon Attack: +6 to hit, reach 5 ft. or range 20/60 ft., one target. Hit: 11 (2d6 +"}
            {:name "piercing damage" :description "If the target is a Huge or smaller creature, it must succeed on a Strength contest against the merrow or be pulled up to 20 feet toward the merrow."}]
}{
  :name "Mimic"
  :size :medium
  :type :monstrosity
  :subtypes #{:shapechanger}
  :alignment "neutral"
  :armor-class 12
  :armor-notes "natural armor"
  :hit-points {:mean 58 :die-count 9 :die 8 :modifier 18}
  :speed "15 ft."

  :str 17
  :dex 12
  :con 15
  :int 5
  :wis 13
  :cha 8

  :skills {:stealth 5}
  :damage-immunities "acid"
  :condition-immunities "prone"
  :senses "darkvision 60 ft., passive Perception 11"
  :challenge 2

  :traits [{:name "Shapechanger" :description "The mimic can use its action to polymorph into an object or back into its true, amorphous form. Its statistics are the same in each"}
           {:name "form" :description "Any equipment it is wearing or carrying isn't transformed. It reverts to its true form if it dies."}
           {:name "Adhesive" :notes "Object Form Only" :description "The mimic adheres to anything that touches it. A Huge or smaller creature adhered to the mimic is also grappled by it (escape DC 13). Ability checks made to escape this grapple have disadvantage."}
           {:name "False Appearance" :notes "Object Form Only" :description "While the mimic remains motionless, it is indistinguishable from an ordinary object."}
           {:name "Grappler" :description "The mimic has advantage on attack rolls against any creature grappled by it."}]

  :actions [{:name "Pseudopod" :description "Melee Weapon Attack: +5 to hit, reach 5 ft., one target. Hit: 7 (1d8 + 3) bludgeoning damage. If the mimic is in object form, the target is subjected to its Adhesive trait."}
            {:name "Bite" :description "Melee Weapon Attack: +5 to hit, reach 5 ft., one target. Hit: 7 (1d8 + 3) piercing damage plus 4 (1d8) acid damage."}]
}{
  :name "Minotaur"
  :size :large
  :type :monstrosity
  :alignment "chaotic evil"
  :armor-class 14
  :armor-notes "natural armor"
  :hit-points {:mean 76 :die-count 9 :die 10 :modifier 27}
  :speed "40 ft."

  :str 18
  :dex 11
  :con 16
  :int 6
  :wis 16
  :cha 9

  :skills {:perception 7}
  :senses "darkvision 60 ft., passive Perception 17"
  :languages "Abyssal"
  :challenge 3

  :traits [{:name "Charge" :description "If the minotaur moves at least 10 feet straight toward a target and then hits it with a gore attack on the same turn, the target takes an extra 9 (2d8) piercing damage. If the target is a creature, it must succeed on a DC 14 Strength saving throw or be pushed up to 10 feet away and knocked prone."}
           {:name "Labyrinthine Recall" :description "The minotaur can perfectly recall any path it has traveled."}
           {:name "Reckless" :description "At the start of its turn, the minotaur can gain advantage on all melee weapon attack rolls it makes during that turn, but attack rolls against it have advantage until the start of its next turn."}]

  :actions [{:name "Greataxe" :description "Melee Weapon Attack: +6 to hit, reach 5 ft., one target. Hit: 17 (2d12 + 4) slashing damage."}
            {:name "Gore" :description "Melee Weapon Attack: +6 to hit, reach 5 ft., one target. Hit: 13 (2d8 + 4) piercing damage."}]
}{
  :name "Mummy"
  :size :medium
  :type :undead
  :alignment "lawful evil"
  :armor-class 11
  :armor-notes "natural armor"
  :hit-points {:mean 58 :die-count 9 :die 8 :modifier 18}
  :speed "20 ft."

  :str 16
  :dex 8
  :con 15
  :int 6
  :wis 10
  :cha 12

  :saving-throws {:wis 2}
  :damage-vulnerabilities "fire"
  :damage-resistances "bludgeoning, piercing, and slashing from nonmagical attacks"
  :damage-immunities "necrotic, poison"
  :condition-immunities "charmed, exhaustion, frightened, paralyzed, poisoned"
  :senses "darkvision 60 ft., passive Perception 10"
  :languages "the languages it knew in life"
  :challenge 3

  :actions [{:name "Multiattack" :description "The mummy can use its Dreadful Glare and makes one attack with its rotting fist."}
            {:name "Rotting Fist" :description "Melee Weapon Attack: +5 to hit, reach 5 ft., one target. Hit: 10 (2d6 + 3) bludgeoning damage plus 10 (3d6) necrotic damage. If the target is a creature, it must succeed on a DC 12 Constitution saving throw or be cursed with mummy rot. The cursed target can't regain hit points, and its hit point maximum decreases by 10 (3d6) for every 24 hours that elapse. If the curse reduces the target's hit point maximum to 0, the target dies, and its body turns to dust. The curse lasts until removed by the remove curse spell or other magic."}
            {:name "Dreadful Glare" :description "The mummy targets one creature it can see within 60 feet of it. If the target can see the mummy, it must succeed on a DC 11 Wisdom saving throw against this magic or become frightened until the end of the mummy's next turn. If the target fails the saving throw by 5 or more, it is also paralyzed for the same duration. A target that succeeds on the saving throw is immune to the Dreadful Glare of all mummies (but not mummy lords) for the next 24 hours."}]
}{
  :name "Mummy Lord"
  :size :medium
  :type :undead
  :alignment "lawful evil"
  :armor-class 17
  :armor-notes "natural armor"
  :hit-points {:mean 97 :die-count 13 :die 8 :modifier 39}
  :speed "20 ft."

  :str 18
  :dex 10
  :con 17
  :int 11
  :wis 18
  :cha 16
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            
  :saving-throws {:con 8, :int 5, :wis 9, :cha 8}
  :skills {:history 5, :religion 5}
  :damage-vulnerabilities "fire"
  :damage-immunities "necrotic, poison; bludgeoning, piercing, and slashing from nonmagical attacks"
  :condition-immunities "charmed, exhaustion, frightened, paralyzed, poisoned"
  :senses "darkvision 60 ft., passive Perception 14"
  :languages "the languages it knew in life"
  :challenge 15

  :traits [{:name "Magic Resistance" :description "The mummy lord has advantage on saving throws against spells and other magical effects."}
           {:name "Rejuvenation" :description "A destroyed mummy lord gains a new body in 24 hours if its heart is intact, regaining all its hit points and becoming active again. The new body appears within 5 feet of the mummy lord's heart."}
           {:name "Spellcasting" :description "The mummy lord is a 10th-level spellcaster. Its spellcasting ability is Wisdom (spell save DC 17, +9 to hit with spell attacks). 
The mummy lord has the following cleric spells prepared:
Cantrips (at will): sacred flame, thaumaturgy
1st level (4 slots): command, guiding bolt, shield of faith
2nd level (3 slots): hold person, silence, spiritual weapon
3rd level (3 slots): animate dead, dispel magic 4th level (3 slots): divination, guardian of faith 5th level (2 slots): contagion, insect plague
6th level (1 slot): harm"}]

  :actions [{:name "Multiattack" :description "The mummy can use its Dreadful Glare and makes one attack with its rotting fist."}
            {:name "Rotting Fist" :description "Melee Weapon Attack: +9 to hit, reach 5 ft., one target. Hit: 14 (3d6 + 4) bludgeoning damage plus 21 (6d6) necrotic damage. If the target is a creature, it must succeed on a DC 16 Constitution saving throw or be cursed with mummy rot. The cursed target can't regain hit points, and its hit point maximum decreases by 10 (3d6) for every 24 hours that elapse. If the curse reduces the target's hit point maximum to 0, the target dies, and its body turns to dust. The curse lasts until removed by the remove curse spell or other magic."}
            {:name "Dreadful Glare" :description "The mummy lord targets one creature it can see within 60 feet of it. If the target can see the mummy lord, it must succeed on a DC 16 Wisdom saving throw against this magic or become frightened until the end of the mummy's next turn. If the target fails the saving throw by 5 or more, it is also paralyzed for the same duration. A target that succeeds on the saving throw is immune to the Dreadful Glare of all mummies and mummy lords for the next 24 hours."}]

  :legendary-actions {:description "The mummy lord can take 3 legendary actions, choosing from the options below. Only one legendary action option can be used at a time and only at the end of another creature's turn. The mummy lord regains spent legendary actions at the start of its turn."
                      :actions [{:name "Attack" :description "The mummy lord makes one attack with its rotting fist or uses its Dreadful Glare."}
                                {:name "Blinding Dust" :description "Blinding dust and sand swirls magically around the mummy lord. Each creature within 5 feet of the mummy lord must succeed on a DC 16 Constitution saving throw or be blinded until the end of the creature's next turn."}
                                {:name "Blasphemous Word" :notes "Costs 2 Actions" :description "The mummy lord utters a blasphemous word. Each non-undead creature within 10 feet of the mummy lord that can hear the magical utterance must succeed on a DC 16 Constitution saving throw or be stunned until the end of the mummy lord's next turn."}
                                {:name "Channel Negative Energy" :notes "Costs 2 Actions" :description "The mummy lord magically unleashes negative energy. Creatures within 60 feet of the mummy lord, including ones behind barriers and around corners, can't regain hit points until the end of the mummy lord's next turn."}
                                {:name "Whirlwind of Sand" :notes "Costs 2 Actions" :description "The mummy lord magically transforms into a whirlwind of sand, moves up to 60 feet, and reverts to its normal form. While in whirlwind form, the mummy lord is immune to all damage, and it can't be grappled, petrified, knocked prone, restrained, or stunned. Equipment worn or carried by the mummy lord remain in its possession."}]}
}{
  :name "Guardian Naga"
  :size :large
  :type :monstrosity
  :alignment "lawful good"
  :armor-class 18
  :armor-notes "natural armor"
  :hit-points {:mean 127 :die-count 15 :die 10 :modifier 45}
  :speed "40 ft."

  :str 19
  :dex 18
  :con 16
  :int 16
  :wis 19
  :cha 18

  :saving-throws {:dex 8, :con 7, :int 7, :wis 8, :cha 8}

  :damage-immunities "poison"
  :condition-immunities "charmed, poisoned"
  :senses "darkvision 60 ft., passive Perception 14"
  :languages "Celestial, Common"
  :challenge 10

  :traits [{:name "Rejuvenation" :description "If it dies, the naga returns to life in 1d6 days and regains all its hit points. Only a wish spell can prevent this trait from functioning."}
           {:name "Spellcasting" :description "The naga is an 11th-level spellcaster. Its spellcasting ability is Wisdom (spell save DC 16, +8 to hit with spell attacks), and it needs only verbal components to cast its spells.
It has the following cleric spells prepared:
Cantrips (at will): mending, sacred flame, thaumaturgy
1st level (4 slots): command, cure wounds, shield of faith
2nd level (3 slots): calm emotions, hold person
3rd level (3 slots): bestow curse, clairvoyance
4th level (3 slots): banishment, freedom of movement
5th level (2 slots): flame strike, geas
6th level (1 slot): true seeing"}]

  :actions [{:name "Bite" :description "Melee Weapon Attack: +8 to hit, reach 10 ft., one creature. Hit: 8 (1d8 + 4) piercing damage, and the target must make a DC 15 Constitution saving throw, taking 45 (10d8) poison damage on a failed save, or half as much damage on a successful one."}
            {:name "Spit Poison" :description "Ranged Weapon Attack: +8 to hit, range 15/30 ft., one creature. Hit: The target must make a DC 15 Constitution saving throw, taking 45 (10d8) poison damage on a failed save, or half as much damage on a successful one."}]
}{
  :name "Spirit Naga"
  :size :large
  :type :monstrosity
  :alignment "chaotic evil"
  :armor-class 15
  :armor-notes "natural armor"
  :hit-points {:mean 75 :die-count 10 :die 10 :modifier 20}
  :speed "40 ft."

  :str 18
  :dex 17
  :con 14
  :int 16
  :wis 15
  :cha 16

  :saving-throws {:dex 6, :con 5, :wis 5, :cha 6}

  :damage-immunities "poison"
  :condition-immunities "charmed, poisoned"
  :senses "darkvision 60 ft., passive Perception 12"
  :languages "Abyssal, Common"
  :challenge 8

  :traits [{:name "Rejuvenation" :description "If it dies, the naga returns to life in 1d6 days and regains all its hit points. Only a wish spell can prevent this trait from functioning."}
           {:name "Spellcasting" :description "The naga is a 10th-level spellcaster. Its spellcasting ability is Intelligence (spell save DC 14, +6 to hit with spell attacks), and it needs only verbal components to cast its spells. 
It has the following wizard spells prepared:
Cantrips (at will): mage hand, minor illusion, ray of frost
1st level (4 slots): charm person, detect magic, sleep
2nd level (3 slots): detect thoughts, hold person
3rd level (3 slots): lightning bolt, water breathing
4th level (3 slots): blight, dimension door
5th level (2 slots): dominate person"}]

  :actions [{:name "Bite" :description "Melee Weapon Attack: +7 to hit, reach 10 ft., one creature. Hit: 7 (1d6 + 4) piercing damage, and the target must make a DC 13 Constitution saving throw, taking 31 (7d8) poison damage on a failed save, or half as much damage on a successful one."}]
}{
  :name "Nightmare"
  :size :large
  :type :fiend
  :alignment "neutral evil"
  :armor-class 13
  :armor-notes "natural armor"
  :hit-points {:mean 68 :die-count 8 :die 10 :modifier 24}
  :speed "60 ft., fly 90 ft."

  :str 18
  :dex 15
  :con 16
  :int 10
  :wis 13
  :cha 15

  :damage-immunities "fire"
  :senses "passive Perception 11"
  :languages "understands Abyssal, Common, and Infernal but can't speak"
  :challenge 3

  :traits [{:name "Confer Fire Resistance" :description "The nightmare can grant resistance to fire damage to anyone riding it."}
           {:name "Illumination" :description "The nightmare sheds bright light in a 10- foot radius and dim light for an additional 10 feet."}]

  :actions [{:name "Hooves" :description "Melee Weapon Attack: +6 to hit, reach 5 ft., one target. Hit: 13 (2d8 + 4) bludgeoning damage plus 7 (2d6) fire damage."}
            {:name "Ethereal Stride" :description "The nightmare and up to three willing creatures within 5 feet of it magically enter the Ethereal Plane from the Material Plane, or vice versa."}]
}{
  :name "Ogre"
  :size :large
  :type :giant
  :alignment "chaotic evil"
  :armor-class 11
  :armor-notes "hide armor"
  :hit-points {:mean 59 :die-count 7 :die 10 :modifier 21}
  :speed "40 ft."

  :str 19
  :dex 8
  :con 16
  :int 5
  :wis 7
  :cha 7
                                                                                                                                                                                                      
  :senses "darkvision 60 ft., passive Perception 8"
  :languages "Common, Giant"
  :challenge 2

  :actions [{:name "Greatclub" :description "Melee Weapon Attack: +6 to hit, reach 5 ft., one target. Hit: 13 (2d8 + 4) bludgeoning damage."}
            {:name "Javelin" :description "Melee or Ranged Weapon Attack: +6 to hit, reach 5 ft. or range 30/120 ft., one target. Hit: 11 (2d6 + 4) piercing damage."}]
}{
  :name "Oni"
  :size :large
  :type :giant
  :alignment "lawful evil"
  :armor-class 16
  :armor-notes "chain mail"
  :hit-points {:mean 110 :die-count 13 :die 10 :modifier 39}
  :speed "30 ft., fly 30 ft."

  :str 19
  :dex 11
  :con 16
  :int 14
  :wis 12
  :cha 15
                                                                                                                                                                         
  :saving-throws {:dex 3, :con 6, :wis 4, :cha 5}
  :skills {:arcana 5, :deception 8, :perception 4}
  :senses "darkvision 60 ft., passive Perception 14"
  :languages "Common, Giant"
  :challenge 7

  :traits [{:name "Innate Spellcasting" :description "The oni's innate spellcasting ability is Charisma (spell save DC 13). 
The oni can innately cast the following spells, requiring no material components:
At will: darkness, invisibility
1/day each: charm person, cone of cold, gaseous form, sleep"}
           {:name "Magic Weapons" :description "The oni's weapon attacks are magical."}
           {:name "Regeneration" :description "The oni regains 10 hit points at the start of its turn if it has at least 1 hit point."}]

  :actions [{:name "Multiattack" :description "The oni makes two attacks, either with its claws or its glaive."}
            {:name "Claw" :notes "Oni Form Only" :description "Melee Weapon Attack: +7 to hit, reach 5 ft., one target. Hit: 8 (1d8 + 4) slashing damage."}
            {:name "Glaive" :description "Melee Weapon Attack: +7 to hit, reach 10 ft., one target. Hit: 15 (2d10 + 4) slashing damage, or 9 (1d10 + 4) slashing damage in Small or Medium form."}
            {:name "Change Shape" :description "The oni magically polymorphs into a Small or Medium humanoid, into a Large giant, or back into its true form. Other than its size, its statistics are the same in each form. The only equipment that is transformed is its glaive, which shrinks so that it can be wielded in humanoid form. If the oni dies, it reverts to its true form, and its glaive reverts to its normal size."}]
}{
  :name "Black Pudding"
  :size :large
  :type :ooze
  :alignment "unaligned"
  :armor-class 7
  :hit-points {:mean 85 :die-count 10 :die 10 :modifier 30}
  :speed "20 ft., climb 20 ft."

  :str 16
  :dex 5
  :con 16
  :int 1
  :wis 6
  :cha 1

  :damage-immunities "acid, cold, lightning, slashing"
  :condition-immunities "blinded, charmed, deafened, exhaustion, frightened, prone"
  :senses "blindsight 60 ft. (blind beyond this radius), passive Perception 8"
  :challenge 4

  :traits [{:name "Amorphous" :description "The pudding can move through a space as narrow as 1 inch wide without squeezing."}
           {:name "Corrosive Form" :description "A creature that touches the pudding or hits it with a melee attack while within 5 feet of it takes 4 (1d8) acid damage. Any nonmagical weapon made of metal or wood that hits the pudding corrodes. After dealing damage, the weapon takes a permanent and cumulative −1 penalty to damage rolls. If its penalty drops to −5, the weapon is destroyed. Nonmagical ammunition made of metal or wood that hits the pudding is destroyed after dealing damage.
The pudding can eat through 2-inch-thick, nonmagical wood or metal in 1 round."}
           {:name "Spider Climb" :description "The pudding can climb difficult surfaces, including upside down on ceilings, without needing to make an ability check."}]

  :actions [{:name "Pseudopod" :description "Melee Weapon Attack: +5 to hit, reach 5 ft., one target. Hit: 6 (1d6 + 3) bludgeoning damage plus 18 (4d8) acid damage. In addition, nonmagical armor worn by the target is partly dissolved and takes a permanent and cumulative −1 penalty to the AC it offers. The armor is destroyed if the penalty reduces its AC to 10."}]

  :reactions [{:name "Split" :description "When a pudding that is Medium or larger is subjected to lightning or slashing damage, it splits into two new puddings if it has at least 10 hit points. Each new pudding has hit points equal to half the original pudding's, rounded down. New puddings are one size smaller than the original pudding."}]
}{
  :name "Gelatinous Cube"
  :size :large
  :type :ooze
  :alignment "unaligned"
  :armor-class 6
  :hit-points {:mean 84 :die-count 8 :die 10 :modifier 40}
  :speed "15 ft."

  :str 14
  :dex 3
  :con 20
  :int 1
  :wis 6
  :cha 1
                                                                                                                                                                                                                                                                                                                                                        
  :condition-immunities "blinded, charmed, deafened, exhaustion, frightened, prone"
  :senses "blindsight 60 ft. (blind beyond this radius), passive Perception 8"
  :challenge 2

  :traits [{:name "Ooze Cube" :description "The cube takes up its entire space. Other creatures can enter the space, but a creature that does so is subjected to the cube's Engulf and has disadvantage on the saving throw.
Creatures inside the cube can be seen but have total cover.
A creature within 5 feet of the cube can take an action to pull a creature or object out of the cube. Doing so requires a successful DC 12 Strength check, and the creature making the attempt takes 10 (3d6) acid damage.
The cube can hold only one Large creature or up to four Medium or smaller creatures inside it at a time."}
           {:name "Transparent" :description "Even when the cube is in plain sight, it takes a successful DC 15 Wisdom (Perception) check to spot a cube that has neither moved nor attacked. A creature that tries to enter the cube's space while unaware of the cube is surprised by the cube."}]

  :actions [{:name "Pseudopod" :description "Melee Weapon Attack: +4 to hit, reach 5 ft., one creature. Hit: 10 (3d6) acid damage."}
            {:name "Engulf" :description "The cube moves up to its speed. While doing so, it can enter Large or smaller creatures' spaces.
Whenever the cube enters a creature's space, the creature must make a DC 12 Dexterity saving throw.
On a successful save, the creature can choose to be pushed 5 feet back or to the side of the cube. A creature that chooses not to be pushed suffers the consequences of a failed saving throw.
On a failed save, the cube enters the creature's space, and the creature takes 10 (3d6) acid damage and is engulfed. The engulfed creature can't breathe, is restrained, and takes 21 (6d6) acid damage at the start of each of the cube's turns. When the cube moves, the engulfed creature moves with it.
An engulfed creature can try to escape by taking an action to make a DC 12 Strength check. On a success, the creature escapes and enters a space of its choice within 5 feet of the cube."}]
}{
  :name "Gray Ooze"
  :size :medium
  :type :ooze
  :alignment "unaligned"
  :armor-class 8
  :hit-points {:mean 22 :die-count 3 :die 8 :modifier 9}
  :speed "10 ft., climb 10 ft."

  :str 12
  :dex 6
  :con 16
  :int 1
  :wis 6
  :cha 2

  :skills {:stealth 2}
  :damage-resistances "acid, cold, fire"
  :condition-immunities "blinded, charmed, deafened, exhaustion, frightened, prone"
  :senses "blindsight 60 ft. (blind beyond this radius), passive Perception 8"
  :challenge (/ 1 2)

  :traits [{:name "Amorphous" :description "The ooze can move through a space as narrow as 1 inch wide without squeezing."}
           {:name "Corrode Metal" :description "Any nonmagical weapon made of metal that hits the ooze corrodes. After dealing damage, the weapon takes a permanent and cumulative −1 penalty to damage rolls. If its penalty drops to −5, the weapon is destroyed. Nonmagical ammunition made of metal that hits the ooze is destroyed after dealing damage.
The ooze can eat through 2-inch-thick, nonmagical metal in 1 round."}
           {:name "False Appearance" :description "While the ooze remains motionless, it is indistinguishable from an oily pool or wet rock."}]

  :actions [{:name "Pseudopod" :description "Melee Weapon Attack: +3 to hit, reach 5 ft., one target. Hit: 4 (1d6 + 1) bludgeoning damage plus 7 (2d6) acid damage, and if the target is wearing nonmagical metal armor, its armor is partly corroded and takes a permanent and cumulative −1 penalty to the AC it offers. The armor is destroyed if the penalty reduces its AC to 10."}]
}{
  :name "Ochre Jelly"
  :size :large
  :type :ooze
  :alignment "unaligned"
  :armor-class 8
  :hit-points {:mean 45 :die-count 6 :die 10 :modifier 12}
  :speed "10 ft., climb 10 ft."

  :str 15
  :dex 6
  :con 14
  :int 2
  :wis 6
  :cha 1

  :damage-resistances "acid"
  :damage-immunities "lightning, slashing"
  :condition-immunities "blinded, charmed, deafened, exhaustion, frightened, prone"
  :senses "blindsight 60 ft. (blind beyond this radius), passive Perception 8"
  :challenge 2

  :traits [{:name "Amorphous" :description "The jelly can move through a space as narrow as 1 inch wide without squeezing."}
           {:name "Spider Climb" :description "The jelly can climb difficult surfaces, including upside down on ceilings, without needing to make an ability check."}]

  :actions [{:name "Pseudopod" :description "Melee Weapon Attack: +4 to hit, reach 5 ft., one target. Hit: 9 (2d6 + 2) bludgeoning damage plus 3 (1d6) acid damage."}]

  :reactions [{:name "Split" :description "When a jelly that is Medium or larger is subjected to lightning or slashing damage, it splits into two new jellies if it has at least 10 hit points. Each new jelly has hit points equal to half the original jelly's, rounded down. New jellies are one size smaller than the original jelly."}]
}{
  :name "Orc"
  :size :medium
  :type :humanoid
  :subtypes #{:orc}
  :alignment "chaotic evil"
  :armor-class 13
  :armor-notes "hide armor"
  :hit-points {:mean 15 :die-count 2 :die 8 :modifier 6}
  :speed "30 ft."

  :str 16
  :dex 12
  :con 16
  :int 7
  :wis 11
  :cha 10

  :skills {:intimidation 2}
  :senses "darkvision 60 ft., passive Perception 10"
  :languages "Common, Orc"
  :challenge (/ 1 2)

  :traits [{:name "Aggressive" :description "As a bonus action, the orc can move up to its speed toward a hostile creature that it can see."}]

  :actions [{:name "Greataxe" :description "Melee Weapon Attack: +5 to hit, reach 5 ft., one target. Hit: 9 (1d12 + 3) slashing damage."}
            {:name "Javelin" :description "Melee or Ranged Weapon Attack: +5 to hit, reach 5 ft. or range 30/120 ft., one target. Hit: 6 (1d6 + 3) piercing damage."}]
}{
  :name "Otyugh"
  :size :large
  :type :aberration
  :alignment "neutral"
  :armor-class 14
  :armor-notes "natural armor"
  :hit-points {:mean 114 :die-count 12 :die 10 :modifier 48}

  :speed "30 ft."

  :str 16
  :dex 11
  :con 19
  :int 6
  :wis 13
  :cha 6

  :saving-throws {:con 7}
  :senses "darkvision 120 ft., passive Perception 11"
  :languages "Otyugh"
  :challenge 5

  :traits [{:name "Limited Telepathy" :description "The otyugh can magically transmit simple messages and images to any creature within 120 feet of it that can understand a language. This form of telepathy doesn't allow the receiving creature to telepathically respond."}]

  :actions [{:name "Multiattack" :description "The otyugh makes three attacks: one with its bite and two with its tentacles."}
            {:name "Bite" :description "Melee Weapon Attack: +6 to hit, reach 5 ft., one target. Hit: 12 (2d8 + 3) piercing damage. If the target is a creature, it must succeed on a DC 15 Constitution saving throw against disease or become poisoned until the disease is cured. Every 24 hours that elapse, the target must repeat the saving throw, reducing its hit point maximum by 5 (1d10) on a failure. The disease is cured on a success. The target dies if the disease reduces its hit point maximum to 0. This reduction to the target's hit point maximum lasts until the disease is cured."}
            {:name "Tentacle" :description "Melee Weapon Attack: +6 to hit, reach 10 ft., one target. Hit: 7 (1d8 + 3) bludgeoning damage plus 4 (1d8) piercing damage. If the target is Medium or smaller, it is grappled (escape DC 13) and restrained until the grapple ends. The otyugh has two tentacles, each of which can grapple one target."}
            {:name "Tentacle Slam" :description "The otyugh slams creatures grappled by it into each other or a solid surface. Each creature must succeed on a DC 14 Constitution saving throw or take 10 (2d6 + 3) bludgeoning damage and be stunned until the end of the otyugh's next turn. On a successful save, the target takes half the bludgeoning damage and isn't stunned."}]
}{
  :name "Owlbear"
  :size :large
  :type :monstrosity
  :alignment "unaligned"
  :armor-class 13
  :armor-notes "natural armor"
  :hit-points {:mean 59 :die-count 7 :die 10 :modifier 21}
  :speed "40 ft."

  :str 20
  :dex 12
  :con 17
  :int 3
  :wis 12
  :cha 7

  :skills {:perception 3}
  :senses "darkvision 60 ft., passive Perception 13"
  :challenge 3

  :traits [{:name "Keen Sight and Smell" :description "The owlbear has advantage on Wisdom (Perception) checks that rely on sight or smell."}]

  :actions [{:name "Multiattack" :description "The owlbear makes two attacks: one with its beak and one with its claws."}
            {:name "Beak" :description "Melee Weapon Attack: +7 to hit, reach 5 ft., one creature. Hit: 10 (1d10 + 5) piercing damage."}
            {:name "Claws" :description "Melee Weapon Attack: +7 to hit, reach 5 ft., one target. Hit: 14 (2d8 + 5) slashing damage."}]
}{
  :name "Pegasus"
  :size :large
  :type :celestial
  :alignment "chaotic good"
  :armor-class 12
  :hit-points {:mean 59 :die-count 7 :die 10 :modifier 21}
  :speed "60 ft., fly 90 ft."

  :str 18
  :dex 15
  :con 16
  :int 10
  :wis 15
  :cha 13
                                                                                                                                         
  :saving-throws {:dex 4, :wis 4, :cha 3}
  :skills {:perception 6}
  :senses "passive Perception 16"
  :languages "understands Celestial, Common, Elvish, and Sylvan but can't speak"
  :challenge 2

  :actions [{:name "Hooves" :description "Melee Weapon Attack: +6 to hit, reach 5 ft., one target. Hit: 11 (2d6 + 4) bludgeoning damage."}]
}{
  :name "Pseudodragon"
  :size :tiny
  :type :dragon
  :alignment "neutral good"
  :armor-class 13
  :armor-notes "natural armor"
  :hit-points {:mean 7 :die-count 2 :die 4 :modifier 2}
  :speed "15 ft., fly 60 ft."
                                                                                                                                             
  :str 6
  :dex 15
  :con 13
  :int 10
  :wis 12
  :cha 10

  :skills {:perception 3, :stealth 4}
  :senses "blindsight 10 ft., darkvision 60 ft., passive Perception 13"
  :languages "understands Common and Draconic but can't speak"
  :challenge (/ 1 4)

  :traits [{:name "Keen Senses" :description "The pseudodragon has advantage on Wisdom (Perception) checks that rely on sight, hearing, or smell."}
           {:name "Magic Resistance" :description "The pseudodragon has advantage on saving throws against spells and other magical effects."}
           {:name "Limited Telepathy" :description "The pseudodragon can magically communicate simple ideas, emotions, and images telepathically with any creature within 100 feet of it that can understand a language."}]

  :actions [{:name "Bite" :description "Melee Weapon Attack: +4 to hit, reach 5 ft., one target. Hit: 4 (1d4 + 2) piercing damage."}
            {:name "Sting" :description "Melee Weapon Attack: +4 to hit, reach 5 ft., one creature. Hit: 4 (1d4 + 2) piercing damage, and the target must succeed on a DC 11 Constitution saving throw or become poisoned for 1 hour. If the saving throw fails by 5 or more, the target falls unconscious for the same duration, or until it takes damage or another creature uses an action to shake it awake."}]
}{
  :name "Purple Worm"
  :size :gargantuan
  :type :monstrosity
  :alignment "unaligned"
  :armor-class 18
  :armor-notes "natural armor"
  :hit-points {:mean 247 :die-count 15 :die 20 :modifier 90}
  :speed "50 ft., burrow 30 ft."

  :str 28
  :dex 7
  :con 22
  :int 1
  :wis 8
  :cha 4
                                                                                                                                                                                                                                                                                                                                                                                                                     
  :saving-throws {:con 11, :wis 4}
  :senses "blindsight 30 ft., tremorsense 60 ft., passive Perception 9"
  :challenge 15

  :traits [{:name "Tunneler" :description "The worm can burrow through solid rock at half its burrow speed and leaves a 10-foot-diameter tunnel in its wake."}]

  :actions [{:name "Multiattack" :description "The worm makes two attacks: one with its bite and one with its stinger."}
            {:name "Bite" :description "Melee Weapon Attack: +9 to hit, reach 10 ft., one target. Hit: 22 (3d8 + 9) piercing damage. If the target is a Large or smaller creature, it must succeed on a DC 19 Dexterity saving throw or be swallowed by the worm. A swallowed creature is blinded and restrained, it has total cover against attacks and other effects outside the worm, and it takes 21 (6d6) acid damage at the start of each of the worm's turns.
If the worm takes 30 damage or more on a single turn from a creature inside it, the worm must succeed on a DC 21 Constitution saving throw at the end of that turn or regurgitate all swallowed creatures, which fall prone in a space within 10 feet of the worm. If the worm dies, a swallowed creature is no longer restrained by it and can escape from the corpse by using 20 feet of movement, exiting prone."}
            {:name "Tail Stinger" :description "Melee Weapon Attack: +9 to hit, reach 10 ft., one creature. Hit: 19 (3d6 + 9) piercing damage, and the target must make a DC 19 Constitution saving throw, taking 42 (12d6) poison damage on a failed save, or half as much damage on a successful one."}]
}{
  :name "Rakshasa"
  :size :medium
  :type :fiend
  :alignment "lawful evil"
  :armor-class 16
  :armor-notes "natural armor"
  :hit-points {:mean 110 :die-count 13 :die 8 :modifier 52}
  :speed "40 ft."

  :str 14
  :dex 17
  :con 18
  :int 13
  :wis 16
  :cha 20

  :skills {:deception 10, :insight 8}
  :damage-vulnerabilities "piercing from magic weapons wielded by good creatures"
  :damage-immunities "bludgeoning, piercing, and slashing from nonmagical attacks"
  :senses "darkvision 60 ft., passive Perception 13"
  :languages "Common, Infernal"
  :challenge 13

  :traits [{:name "Limited Magic Immunity" :description "The rakshasa can't be affected or detected by spells of 6th level or lower unless it wishes to be. It has advantage on saving throws against all other spells and magical effects."}
           {:name "Innate Spellcasting" :description "The rakshasa's innate spellcasting ability is Charisma (spell save DC 18, +10 to hit with spell attacks). 
The rakshasa can innately cast the following spells, requiring no material components:
At will: detect thoughts, disguise self, mage hand, minor illusion
3/day each: charm person, detect magic, invisibility, major image, suggestion
1/day each: dominate person, fly, plane shift, true seeing"}]

  :actions [{:name "Multiattack" :description "The rakshasa makes two claw attacks."}
            {:name "Claw" :description "Melee Weapon Attack: +7 to hit, reach 5 ft., one target. Hit: 9 (2d6 + 2) slashing damage, and the target is cursed if it is a creature. The magical curse takes effect whenever the target takes a short or long rest, filling the target's thoughts with horrible images and dreams. The cursed target gains no benefit from finishing a short or long rest. The curse lasts until it is lifted by a remove curse spell or similar magic."}]
}{
  :name "Remorhaz"
  :size :huge
  :type :monstrosity
  :alignment "unaligned"
  :armor-class 17
  :armor-notes "natural armor"
  :hit-points {:mean 195 :die-count 17 :die 12 :modifier 85}
  :speed "30 ft., burrow 20 ft."

  :str 24
  :dex 13
  :con 21
  :int 4
  :wis 10
  :cha 5
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        
  :damage-immunities "cold, fire"
  :senses "darkvision 60 ft., tremorsense 60 ft., passive Perception 10"
  :challenge 11

  :traits [{:name "Heated Body" :description "A creature that touches the remorhaz or hits it with a melee attack while within 5 feet of it takes 10 (3d6) fire damage."}]

  :actions [{:name "Bite" :description "Melee Weapon Attack: +11 to hit, reach 10 ft., one target. Hit: 40 (6d10 + 7) piercing damage plus 10 (3d6) fire damage. If the target is a creature, it is grappled (escape DC 17). Until this grapple ends, the target is restrained, and the remorhaz can't bite another target."}
            {:name "Swallow" :description "The remorhaz makes one bite attack against a Medium or smaller creature it is grappling. If the attack hits, that creature takes the bite's damage and is swallowed, and the grapple ends. While swallowed, the creature is blinded and restrained, it has total cover against attacks and other effects outside the remorhaz, and it takes 21 (6d6) acid damage at the start of each of the remorhaz's turns.
If the remorhaz takes 30 damage or more on a single turn from a creature inside it, the remorhaz must succeed on a DC 15 Constitution saving throw at the end of that turn or regurgitate all swallowed creatures, which fall prone in a space within 10 feet of the remorhaz. If the remorhaz dies, a swallowed creature is no longer restrained by it and can escape from the corpse using 15 feet of movement, exiting prone."}]
}{
  :name "Roc"
  :size :gargantuan
  :type :monstrosity
  :alignment "unaligned"
  :armor-class 15
  :armor-notes "natural armor"
  :hit-points {:mean 248 :die-count 16 :die 20 :modifier 80}
  :speed "20 ft., fly 120 ft."

  :str 28
  :dex 10
  :con 20
  :int 3
  :wis 10
  :cha 9
                                                                                                                                                                                                                                                                                                                                                                                                                                     
  :saving-throws {:dex 4, :con 9, :wis 4, :cha 3}
  :skills {:perception 4}
  :senses "passive Perception 14"
  :challenge 11

  :traits [{:name "Keen Sight" :description "The roc has advantage on Wisdom (Perception) checks that rely on sight."}]

  :actions [{:name "Multiattack" :description "The roc makes two attacks: one with its beak and one with its talons."}
            {:name "Beak" :description "Melee Weapon Attack: +13 to hit, reach 10 ft., one target. Hit: 27 (4d8 + 9) piercing damage."}
            {:name "Talons" :description "Melee Weapon Attack: +13 to hit, reach 5 ft., one target. Hit: 23 (4d6 + 9) slashing damage, and the target is grappled (escape DC 19). Until this grapple ends, the target is restrained, and the roc can't use its talons on another target."}]
}{
  :name "Roper"
  :size :large
  :type :monstrosity
  :alignment "neutral evil"
  :armor-class 20
  :armor-notes "natural armor"
  :hit-points {:mean 93 :die-count 11 :die 10 :modifier 33}
  :speed "10 ft., climb 10 ft."

  :str 18
  :dex 8
  :con 17
  :int 7
  :wis 16
  :cha 6

  :skills {:perception 6, :stealth 5}
  :senses "darkvision 60 ft., passive Perception 16"
  :challenge 5

  :traits [{:name "False Appearance" :description "While the roper remains motionless, it is indistinguishable from a normal cave formation, such as a stalagmite."}
           {:name "Grasping Tendrils" :description "The roper can have up to six tendrils at a time. Each tendril can be attacked (AC 20; 10 hit points; immunity to poison and psychic damage).
Destroying a tendril deals no damage to the roper, which can extrude a replacement tendril on its next turn. A tendril can also be broken if a creature takes an action and succeeds on a DC 15 Strength check against it."}
           {:name "Spider Climb" :description "The roper can climb difficult surfaces, including upside down on ceilings, without needing to make an ability check."}]

  :actions [{:name "Multiattack" :description "The roper makes four attacks with its tendrils, uses Reel, and makes one attack with its bite."}
            {:name "Bite" :description "Melee Weapon Attack: +7 to hit, reach 5 ft., one target. Hit: 22 (4d8 + 4) piercing damage."}
            {:name "Tendril" :description "Melee Weapon Attack: +7 to hit, reach 50 ft., one creature. Hit: The target is grappled (escape DC 15). Until the grapple ends, the target is restrained and has disadvantage on Strength checks and Strength saving throws, and the roper can't use the same tendril on another target."}
            {:name "Reel" :description "The roper pulls each creature grappled by it up to 25 feet straight toward it."}]
}{
    :name "Rust Monster"
    :size :medium
    :type :monstrosity
    :alignment "unaligned"
    :armor-class 14
    :armor-notes "natural armor"
    :hit-points {:mean 27 :die-count 5 :die 8 :modifier 5}
    :speed "40 ft."

    :str 13
    :dex 12
    :con 13
    :int 2
    :wis 13
    :cha 6

    :senses "darkvision 60 ft., passive Perception 11"
    :challenge (/ 1 2)

    :traits [{:name "Iron Scent" :description "The rust monster can pinpoint, by scent, the location of ferrous metal within 30 feet of it."}
             {:name "Rust Metal" :description "Any nonmagical weapon made of metal that hits the rust monster corrodes. After dealing damage, the weapon takes a permanent and cumulative −1 penalty to damage rolls. If its penalty drops to −5, the weapon is destroyed. Nonmagical ammunition made of metal that hits the rust monster is destroyed after dealing damage."}]

    :actions [{:name "Bite" :description "Melee Weapon Attack: +3 to hit, reach 5 ft., one target. Hit: 5 (1d8 + 1) piercing damage."}
              {:name "Antennae" :description "The rust monster corrodes a nonmagical ferrous metal object it can see within 5 feet of it. If the object isn't being worn or carried, the touch destroys a 1-foot cube of it. If the object is being worn or carried by a creature, the creature can make a DC 11 Dexterity saving throw to avoid the rust monster's touch.
If the object touched is either metal armor or a metal shield being worn or carried, its takes a permanent and cumulative −1 penalty to the AC it offers. Armor reduced to an AC of 10 or a shield that drops to a +0 bonus is destroyed. If the object touched is a held metal weapon, it rusts as described in the Rust Metal trait."}]
}{
  :name "Sahuagin"
  :size :medium
  :type :humanoid
  :subtypes #{:sahuagin}
  :alignment "lawful evil"
  :armor-class 12
  :armor-notes "natural armor"
  :hit-points {:mean 22 :die-count 4 :die 8 :modifier 4}
  :speed "30 ft., swim 40 ft."

  :str 13
  :dex 11
  :con 12
  :int 12
  :wis 13
  :cha 9

  :skills {:perception 5}
  :senses "darkvision 120 ft., passive Perception 15"
  :languages "Sahuagin"
  :challenge (/ 1 2)

  :traits [{:name "Blood Frenzy" :description "The sahuagin has advantage on melee attack rolls against any creature that doesn't have all its hit points."}
           {:name "Limited Amphibiousness" :description "The sahuagin can breathe air and water, but it needs to be submerged at least once every 4 hours to avoid suffocating."}
           {:name "Shark Telepathy" :description "The sahuagin can magically command any shark within 120 feet of it, using a limited telepathy."}]

  :actions [{:name "Multiattack" :description "The sahuagin makes two melee attacks: one with its bite and one with its claws or spear."}
            {:name "Bite" :description "Melee Weapon Attack: +3 to hit, reach 5 ft., one target. Hit: 3 (1d4 + 1) piercing damage."}
            {:name "Claws" :description "Melee Weapon Attack: +3 to hit, reach 5 ft., one target. Hit: 3 (1d4 + 1) slashing damage."}
            {:name "Spear" :description "Melee or Ranged Weapon Attack: +3 to hit, reach 5 ft. or range 20/60 ft., one target. Hit: 4 (1d6 + 1) piercing damage, or 5 (1d8 + 1) piercing damage if used with two hands to make a melee attack."}]
}{
  :name "Salamander"
  :size :large
  :type :elemental
  :alignment "neutral evil"
  :armor-class 15
  :armor-notes "natural armor"
  :hit-points {:mean 90 :die-count 12 :die 10 :modifier 24}
  :speed "30 ft."

  :str 18
  :dex 14
  :con 15
  :int 11
  :wis 10
  :cha 12
                                                                                                                                                                                                                                                   
  :damage-vulnerabilities "cold"
  :damage-resistances "bludgeoning, piercing, and slashing from nonmagical attacks"
  :damage-immunities "fire"
  :senses "darkvision 60 ft., passive Perception 10"
  :languages "Ignan"
  :challenge 5

  :traits [{:name "Heated Body" :description "A creature that touches the salamander or hits it with a melee attack while within 5 feet of it takes 7 (2d6) fire damage."}
           {:name "Heated Weapons" :description "Any metal melee weapon the salamander wields deals an extra 3 (1d6) fire damage on a hit (included in the attack)."}]

  :actions [{:name "Multiattack" :description "The salamander makes two attacks: one with its spear and one with its tail."}
            {:name "Spear" :description "Melee or Ranged Weapon Attack: +7 to hit, reach 5 ft. or range 20 ft./60 ft., one target. Hit: 11 (2d6 + 4) piercing damage, or 13 (2d8 + 4) piercing damage if used with two hands to make a melee attack, plus 3 (1d6) fire damage."}
            {:name "Tail" :description "Melee Weapon Attack: +7 to hit, reach 10 ft., one target. Hit: 11 (2d6 + 4) bludgeoning damage plus 7 (2d6) fire damage, and the target is grappled (escape DC 14). Until this grapple ends, the target is restrained, the salamander can automatically hit the target with its tail, and the salamander can't make tail attacks against other targets."}]
}{
  :name "Satyr"
  :size :medium
  :type :fey
  :alignment "chaotic neutral"
  :armor-class 14
  :armor-notes "leather armor"
  :hit-points {:mean 31 :die-count 7 :die 8}
  :speed "40 ft."

  :str 12
  :dex 16
  :con 11
  :int 12
  :wis 10
  :cha 14

  :skills {:perception 2, :performance 6, :stealth 5}
  :senses "passive Perception 12"
  :languages "Common, Elvish, Sylvan"
  :challenge (/ 1 2)

  :traits [{:name "Magic Resistance" :description "The satyr has advantage on saving throws against spells and other magical effects."}]

  :actions [{:name "Ram" :description "Melee Weapon Attack: +3 to hit, reach 5 ft., one target. Hit: 6 (2d4 + 1) bludgeoning damage."}
            {:name "Shortsword" :description "Melee Weapon Attack: +5 to hit, reach 5 ft., one target. Hit: 6 (1d6 + 3) piercing damage."}
            {:name "Shortbow" :description "Ranged Weapon Attack: +5 to hit, range 80/320 ft., one target. Hit: 6 (1d6 + 3) piercing damage."}]
}{
    :name "Shadow"
    :size :medium
    :type :undead
    :alignment "chaotic evil"
    :armor-class 12
    :hit-points {:mean 16 :die-count 3 :die 8 :modifier 3}
    :speed "40 ft."

    :str 6
    :dex 14
    :con 13
    :int 6
    :wis 10
    :cha 8

    :skills {:stealth 4}
    :damage-vulnerabilities "radiant"
    :damage-resistances "acid, cold, fire, lightning, thunder; bludgeoning, piercing, and slashing from nonmagical attacks"
    :damage-immunities "necrotic, poison"
    :condition-immunities "exhaustion, frightened, grappled, paralyzed, petrified, poisoned, prone, restrained"
    :senses "darkvision 60 ft., passive Perception 10"
    :challenge (/ 1 2)

    :traits [{:name "Amorphous" :description "The shadow can move through a space as narrow as 1 inch wide without squeezing."}
             {:name "Shadow Stealth" :description "While in dim light or darkness, the shadow can take the Hide action as a bonus action."}
             {:name "Sunlight Weakness" :description "While in sunlight, the shadow has disadvantage on attack rolls, ability checks, and saving throws."}]

    :actions [{:name "Strength Drain" :description "Melee Weapon Attack: +4 to hit, reach 5 ft., one creature. Hit: 9 (2d6 + 2) necrotic damage, and the target's Strength score is reduced by 1d4. The target dies if this reduces its Strength to 0. Otherwise, the reduction lasts until the target finishes a short or long rest. If a non-evil humanoid dies from this attack, a new shadow rises from the corpse 1d4 hours later."}]
}{
  :name "Shambling Mound"
  :size :large
  :type :plant
  :alignment "unaligned"
  :armor-class 15
  :armor-notes "natural armor"
  :hit-points {:mean 136 :die-count 16 :die 10 :modifier 48}
  :speed "20 ft., swim 20 ft."

  :str 18
  :dex 8
  :con 16
  :int 5
  :wis 10
  :cha 5

  :skills {:stealth 2}
  :damage-resistances "cold, fire"
  :damage-immunities "lightning"
  :condition-immunities "blinded, deafened, exhaustion"
  :senses "blindsight 60 ft. (blind beyond this radius), passive Perception 10"
  :challenge 5

  :traits [{:name "Lightning Absorption" :description "Whenever the shambling mound is subjected to lightning damage, it takes no damage and regains a number of hit points equal to the lightning damage dealt."}]

  :actions [{:name "Multiattack" :description "The shambling mound makes two slam attacks. If both attacks hit a Medium or smaller target, the target is grappled (escape DC 14), and the shambling mound uses its Engulf on it."}
            {:name "Slam" :description "Melee Weapon Attack: +7 to hit, reach 5 ft., one target. Hit: 13 (2d8 + 4) bludgeoning damage."}
            {:name "Engulf" :description "The shambling mound engulfs a Medium or smaller creature grappled by it. The engulfed target is blinded, restrained, and unable to breathe, and it must succeed on a DC 14 Constitution saving throw at the start of each of the mound's turns or take 13 (2d8 + 4) bludgeoning damage. If the mound moves, the engulfed target moves with it. The mound can have only one creature engulfed at a time."}]
}{
  :name "Shield Guardian"
  :size :large
  :type :construct
  :alignment "unaligned"
  :armor-class 17
  :armor-notes "natural armor"
  :hit-points {:mean 142 :die-count 15 :die 10 :modifier 60}
  :speed "30 ft."

  :str 18
  :dex 8
  :con 18
  :int 7
  :wis 10
  :cha 3
                                                                                                                                                                                                                                                                                                                                                                                                                                                      
  :senses "blindsight 10 ft., darkvision 60 ft., passive Perception 10"
  :damage-immunities "poison"
  :condition-immunities "charmed, exhaustion, frightened, paralyzed, poisoned"
  :languages "understands commands given in any language but can't speak"
  :challenge 7

  :traits [{:name "Bound" :description "The shield guardian is magically bound to an amulet. As long as the guardian and its amulet are on the same plane of existence, the amulet's wearer can telepathically call the guardian to travel to it, and the guardian knows the distance and direction to the amulet. If the guardian is within 60 feet of the amulet's wearer, half of any damage the wearer takes (rounded up) is transferred to the guardian."}
           {:name "Regeneration" :description "The shield guardian regains 10 hit points at the start of its turn if it has at least 1 hit point."}
           {:name "Spell Storing" :description "A spellcaster who wears the shield guardian's amulet can cause the guardian to store one spell of 4th level or lower. To do so, the wearer must cast the spell on the guardian. The spell has no effect but is stored within the guardian. When commanded to do so by the wearer or when a situation arises that was predefined by the spellcaster, the guardian casts the stored spell with any parameters set by the original caster, requiring no components. When the spell is cast or a new spell is stored, any previously stored spell is lost."}]

  :actions [{:name "Multiattack" :description "The guardian makes two fist attacks."}
            {:name "Fist" :description "Melee Weapon Attack: +7 to hit, reach 5 ft., one target. Hit: 11 (2d6 + 4) bludgeoning damage."}]

  :reactions [{:name "Shield" :description "When a creature makes an attack against the wearer of the guardian's amulet, the guardian grants a +2 bonus to the wearer's AC if the guardian is within 5 feet of the wearer."}]
}{
  :name "Skeleton"
  :size :medium
  :type :undead
  :alignment "lawful evil"
  :armor-class 13
  :armor-notes "armor scraps"
  :hit-points {:mean 13 :die-count 2 :die 8 :modifier 4}
  :speed "30 ft."

  :str 10
  :dex 14
  :con 15
  :int 6
  :wis 8
  :cha 5

  :damage-vulnerabilities "bludgeoning"
  :damage-immunities "poison"
  :condition-immunities "exhaustion, poisoned"
  :senses "darkvision 60 ft., passive Perception 9"
  :languages "understands all languages it knew in life but can't speak"
  :challenge (/ 1 4)

  :actions [{:name "Shortsword" :description "Melee Weapon Attack: +4 to hit, reach 5 ft., one target. Hit: 5 (1d6 + 2) piercing damage."}
            {:name "Shortbow" :description "Ranged Weapon Attack: +4 to hit, range 80/320 ft., one target. Hit: 5 (1d6 + 2) piercing damage."}]
}{
  :name "Warhorse  Skeleton"
  :size :large
  :type :undead
  :alignment "lawful evil"
  :armor-class 13
  :armor-notes "barding scraps"
  :hit-points {:mean 22 :die-count 3 :die 10 :modifier 6}
  :speed "60 ft."

  :str 18
  :dex 12
  :con 15
  :int 2
  :wis 8
  :cha 5

  :damage-vulnerabilities "bludgeoning"
  :damage-immunities "poison"
  :condition-immunities "exhaustion, poisoned"
  :senses "darkvision 60 ft., passive Perception 9"
  :challenge (/ 1 2)

  :actions [{:name "Hooves" :description "Melee Weapon Attack: +6 to hit, reach 5 ft., one target. Hit: 11 (2d6 + 4) bludgeoning damage."}]
}{
  :name "Minotaur Skeleton"
  :size :large
  :type :undead
  :alignment "lawful evil"
  :armor-class 12
  :armor-notes "natural armor"
  :hit-points {:mean 67 :die-count 9 :die 10 :modifier 18}
  :speed "40 ft."

  :str 18
  :dex 12
  :con 15
  :int 2
  :wis 8
  :cha 5

  :damage-vulnerabilities "bludgeoning"
  :damage-immunities "poison"
  :condition-immunities "exhaustion, poisoned"
  :senses "darkvision 60 ft., passive Perception 9"
  :languages "understands Abyssal but can't speak"
  :challenge 2

  :traits [{:name "Charge" :description "If the skeleton moves at least 10 feet straight toward a target and then hits it with a gore attack on the same turn, the target takes an extra 9 (2d8) piercing damage. If the target is a creature, it must succeed on a DC 14 Strength saving throw or be pushed up to 10 feet away and knocked prone."}]

  :actions [{:name "Greataxe" :description "Melee Weapon Attack: +6 to hit, reach 5 ft., one target. Hit: 17 (2d12 + 4) slashing damage."}
            {:name "Gore" :description "Melee Weapon Attack: +6 to hit, reach 5 ft., one target. Hit: 13 (2d8 + 4) piercing damage."}]
}{
  :name "Specter"
  :size :medium
  :type :undead
  :alignment "chaotic evil"
  :armor-class 12
  :hit-points {:mean 22 :die-count 5 :die 8}
  :speed "0 ft., fly 50 ft. (hover)"

  :str 1
  :dex 14
  :con 11
  :int 10
  :wis 10
  :cha 11

  :damage-resistances "acid, cold, fire, lightning, thunder; bludgeoning, piercing, and slashing from nonmagical attacks"
  :damage-immunities "necrotic, poison"
  :condition-immunities "charmed, exhaustion, grappled, paralyzed, petrified, poisoned, prone, restrained, unconscious"
  :senses "darkvision 60 ft., passive Perception 10"
  :languages "understands all languages it knew in life but can't speak"
  :challenge 1

  :traits [{:name "Incorporeal Movement" :description "The specter can move through other creatures and objects as if they were difficult terrain. It takes 5 (1d10) force damage if it ends its turn inside an object."}
           {:name "Sunlight Sensitivity" :description "While in sunlight, the specter has disadvantage on attack rolls, as well as on Wisdom (Perception) checks that rely on sight."}]

  :actions [{:name "Life Drain" :description "Melee Spell Attack: +4 to hit, reach 5 ft., one creature. Hit: 10 (3d6) necrotic damage. The target must succeed on a DC 10 Constitution saving throw or its hit point maximum is reduced by an amount equal to the damage taken. This reduction lasts until the creature finishes a long rest. The target dies if this effect reduces its hit point maximum to 0."}]
}{
  :name "Androsphinx"
  :size :large
  :type :monstrosity
  :alignment "lawful neutral"
  :armor-class 17
  :armor-notes "natural armor"
  :hit-points {:mean 199 :die-count 19 :die 10 :modifier 95}
  :speed "40 ft., fly 60 ft."

  :str 22
  :dex 10
  :con 20
  :int 16
  :wis 18
  :cha 23
                                                                                                                                                                                                                                                                                                                                                                                                                     
  :saving-throws {:dex 6, :con 11, :int 9, :wis 10}
  :skills {:arcana 9, :perception 10, :religion 15}
  :damage-immunities "psychic; bludgeoning, piercing, and slashing from nonmagical attacks"
  :condition-immunities "charmed, frightened"
  :senses "truesight 120 ft., passive Perception 20"
  :languages "Common, Sphinx"
  :challenge 17

  :traits [{:name "Inscrutable" :description "The sphinx is immune to any effect that would sense its emotions or read its thoughts, as well as any divination spell that it refuses. Wisdom (Insight) checks made to ascertain the sphinx's intentions or sincerity have disadvantage."}
           {:name "Magic Weapons" :description "The sphinx's weapon attacks are magical."}
           {:name "Spellcasting" :description "The sphinx is a 12th-level spellcaster. Its spellcasting ability is Wisdom (spell save DC 18, +10 to hit with spell attacks). It requires no material components to cast its spells.
The sphinx has the following cleric spells prepared:
Cantrips (at will): sacred flame, spare the dying, thaumaturgy
1st level (4 slots): command, detect evil and good, detect magic
2nd level (3 slots): lesser restoration, zone of truth
3rd level (3 slots): dispel magic, tongues
4th level (3 slots): banishment, freedom of movement 5th level (2 slots): flame strike, greater restoration 6th level (1 slot): heroes' feast"}]

  :actions [{:name "Multiattack" :description "The sphinx makes two claw attacks."}
            {:name "Claw" :description "Melee Weapon Attack: +12 to hit, reach 5 ft., one target. Hit: 17 (2d10 + 6) slashing damage."}
            {:name "Roar" :notes "3/Day" :description "The sphinx emits a magical roar. Each time it roars before finishing a long rest, the roar is louder and the effect is different, as detailed below. Each creature within 500 feet of the sphinx and able to hear the roar must make a saving throw."}
            {:name "First Roar" :description "Each creature that fails a DC 18 Wisdom saving throw is frightened for 1 minute. A frightened creature can repeat the saving throw at the end of each of its turns, ending the effect on itself on a success."}
            {:name "Second Roar" :description "Each creature that fails a DC 18 Wisdom saving throw is deafened and frightened for 1 minute. A frightened creature is paralyzed and can repeat the saving throw at the end of each of its turns, ending the effect on itself on a success."}
            {:name "Third Roar" :description "Each creature makes a DC 18 Constitution saving throw. On a failed save, a creature takes 44 (8d10) thunder damage and is knocked prone. On a successful save, the creature takes half as much damage and isn't knocked prone."}]

  :legendary-actions {:description "The sphinx can take 3 legendary actions, choosing from the options below. Only one legendary action option can be used at a time and only at the end of another creature's turn. The sphinx regains spent legendary actions at the start of its turn."
                      :actions [{:name "Claw Attack" :description "The sphinx makes one claw attack."}
                                {:name "Teleport" :notes "Costs 2 Actions" :description "The sphinx magically teleports, along with any equipment it is wearing or carrying, up to 120 feet to an unoccupied space it can see."}
                                {:name "Cast a Spell" :notes "Costs 3 Actions" :description "The sphinx casts a spell from its list of prepared spells, using a spell slot as normal."}]}
}{
  :name "Gynosphinx"
  :size :large
  :type :monstrosity
  :alignment "lawful neutral"
  :armor-class 17
  :armor-notes "natural armor"
  :hit-points {:mean 136 :die-count 16 :die 10 :modifier 48}
  :speed "40 ft., fly 60 ft."

  :str 18
  :dex 15
  :con 16
  :int 18
  :wis 18
  :cha 18

  :skills {:arcana 12, :history 12, :perception 8, :religion 8}
  :damage-resistances "bludgeoning, piercing, and slashing from nonmagical attacks"
  :damage-immunities "psychic"
  :condition-immunities "charmed, frightened"
  :senses "truesight 120 ft., passive Perception 18"
  :languages "Common, Sphinx"
  :challenge 11

  :traits [{:name "Inscrutable" :description "The sphinx is immune to any effect that would sense its emotions or read its thoughts, as well as any divination spell that it refuses. Wisdom (Insight) checks made to ascertain the sphinx's intentions or sincerity have disadvantage."}
           {:name "Spellcasting" :description "The sphinx is a 9th-level spellcaster. Its spellcasting ability is Intelligence (spell save DC 16, +8 to hit with spell attacks). It requires no material components to cast its spells. 
The sphinx has the following wizard spells prepared:
Cantrips (at will): mage hand, minor illusion, prestidigitation
1st level (4 slots): detect magic, identify, shield
2nd level (3 slots): darkness, locate object, suggestion
3rd level (3 slots): dispel magic, remove curse, tongues 
4th level (3 slots): banishment, greater invisibility
5th level (1 slot): legend lore"}]

  :actions [{:name "Multiattack" :description "The sphinx makes two claw attacks."}
            {:name "Claw" :description "Melee Weapon Attack: +8 to hit, reach 5 ft., one target. Hit: 13 (2d8 + 4) slashing damage."}
            {:name "Magic Weapons" :description "The sphinx's weapon attacks are magical."}]

  :legendary-actions {:description "The sphinx can take 3 legendary actions, choosing from the options below. Only one legendary action option can be used at a time and only at the end of another creature's turn. The sphinx regains spent legendary actions at the start of its turn."
                      :actions [{:name "Claw Attack" :description "The sphinx makes one claw attack."}
                                {:name "Teleport" :notes "Costs 2 Actions" :description "The sphinx magically teleports, along with any equipment it is wearing or carrying, up to 120 feet to an unoccupied space it can see."}
                                {:name "Cast a Spell" :notes "Costs 3 Actions" :description "The sphinx casts a spell from its list of prepared spells, using a spell slot as normal."}]}
}{
  :name "Sprite"
  :size :tiny
  :type :fey
  :alignment "neutral good"
  :armor-class 15
  :armor-notes "leather armor"
  :hit-points {:mean 2 :die-count 1 :die 4}
  :speed "10 ft., fly 40 ft."

  :str 3
  :dex 18
  :con 10
  :int 14
  :wis 13
  :cha 11

  :skills {:perception 3, :stealth 8}
  :senses "passive Perception 13"
  :languages "Common, Elvish, Sylvan"
  :challenge (/ 1 4)

  :actions [{:name "Longsword" :description "Melee Weapon Attack: +2 to hit, reach 5 ft., one target. Hit: 1 slashing damage."}
            {:name "Shortbow" :description "Ranged Weapon Attack: +6 to hit, range 40/160 ft., one target. Hit: 1 piercing damage, and the target must succeed on a DC 10 Constitution saving throw or become poisoned for 1 minute. If its saving throw result is 5 or lower, the poisoned target falls unconscious for the same duration, or until it takes damage or another creature takes an action to shake it awake."}
            {:name "Heart Sight" :description "The sprite touches a creature and magically knows the creature's current emotional state. If the target fails a DC 10 Charisma saving throw, the sprite also knows the creature's alignment. Celestials, fiends, and undead automatically fail the saving throw."}
            {:name "Invisibility" :description "The sprite magically turns invisible until it attacks or casts a spell, or until its concentration ends (as if concentrating on a spell). Any equipment the sprite wears or carries is invisible with it."}]
}{
  :name "Stirge"
  :size :tiny
  :type :beast
  :alignment "unaligned"
  :armor-class 14
  :armor-notes "natural armor"
  :hit-points {:mean 2 :die-count 1 :die 4}
  :speed "10 ft., fly 40 ft."

  :str 4
  :dex 16
  :con 11
  :int 2
  :wis 8
  :cha 6
                                                                                                                                                                                                                                                              
  :senses "darkvision 60 ft., passive Perception 9"
  :challenge (/ 1 8)

  :actions [{:name "Blood Drain" :description "Melee Weapon Attack: +5 to hit, reach 5 ft., one creature. Hit: 5 (1d4 + 3) piercing damage, and the stirge attaches to the target. While attached, the stirge doesn't attack. Instead, at the start of each of the stirge's turns, the target loses 5 (1d4 + 3) hit points due to blood loss.
The stirge can detach itself by spending 5 feet of its movement. It does so after it drains 10 hit points of blood from the target or the target dies. A creature, including the target, can use its action to detach the stirge."}]
}{
  :name "Succubus/Incubus"
  :size :medium
  :type :fiend
  :subtypes #{:shapechanger}
  :alignment "neutral evil"
  :armor-class 15
  :armor-notes "natural armor"
  :hit-points {:mean 66 :die-count 12 :die 8 :modifier 12}
  :speed "30 ft., fly 60 ft."

  :str 8
  :dex 17
  :con 13
  :int 15
  :wis 12
  :cha 20

  :skills {:deception 9, :insight 5, :perception 5}
  :persuasion 9, :stealth 7
  :damage-resistances "cold, fire, lightning, poison; bludgeoning, piercing, and slashing from nonmagical attacks"
  :senses "darkvision 60 ft., passive Perception 15"
  :languages "Abyssal, Common, Infernal, telepathy 60 ft."
  :challenge 4

  :traits [{:name "Telepathic Bond" :description "The fiend ignores the range restriction on its telepathy when communicating with a creature it has charmed. The two don't even need to be on the same plane of existence."}
           {:name "Shapechanger" :description "The fiend can use its action to polymorph into a Small or Medium humanoid, or back into its true form. Without wings, the fiend loses its flying speed. Other than its size and speed, its statistics are the same in each form. Any equipment it is wearing or carrying isn't transformed. It reverts to its true form if it dies."}]

  :actions [{:name "Claw" :notes "Fiend Form Only" :description "Melee Weapon Attack: +5 to hit, reach 5 ft., one target. Hit: 6 (1d6 + 3) slashing damage."}
            {:name "Charm" :description "One humanoid the fiend can see within 30 feet of it must succeed on a DC 15 Wisdom saving throw or be magically charmed for 1 day. The charmed target obeys the fiend's verbal or telepathic commands. If the target suffers any harm or receives a suicidal command, it can repeat the saving throw, ending the effect on a success. If the target successfully saves against the effect, or if the effect on it ends, the target is immune to this fiend's Charm for the next 24 hours.
The fiend can have only one target charmed at a time. If it charms another, the effect on the previous target ends."}
            {:name "Draining Kiss" :description "The fiend kisses a creature charmed by it or a willing creature. The target must make a DC 15
Constitution saving throw against this magic, taking 32 (5d10 + 5) psychic damage on a failed save, or half as much damage on a successful one. The target's hit point maximum is reduced by an amount equal to the damage taken. This reduction lasts until the target finishes a long rest. The target dies if this effect reduces its hit point maximum to 0."}
            {:name "Etherealness" :description "The fiend magically enters the Ethereal Plane from the Material Plane, or vice versa."}]
}{
  :name "Tarrasque"
  :size :gargantuan
  :type :monstrosity
  :subtypes #{:titan}
  :alignment "unaligned"
  :armor-class 25
  :armor-notes "natural armor"
  :hit-points {:mean 676 :die-count 33 :die 20 :modifier 330}
  :speed "40 ft."

  :str 30
  :dex 11
  :con 30
  :int 3
  :wis 11
  :cha 11
                                                                                                                                          
  :saving-throws {:int 5, :wis 9, :cha 9}
  :damage-immunities "fire, poison; bludgeoning, piercing, and slashing from nonmagical attacks"
  :condition-immunities "charmed, frightened, paralyzed, poisoned"
  :senses "blindsight 120 ft., passive Perception 10"
  :challenge 30

  :traits [{:name "Legendary Resistance" :notes "3/Day" :description "If the tarrasque fails a saving throw, it can choose to succeed instead."}
           {:name "Magic Resistance" :description "The tarrasque has advantage on saving throws against spells and other magical effects."}
           {:name "Reflective Carapace" :description "Any time the tarrasque is targeted by a magic missile spell, a line spell, or a spell that requires a ranged attack roll, roll a d6. On a 1 to 5, the tarrasque is unaffected. On a 6, the tarrasque is unaffected, and the effect is reflected back at the caster as though it originated from the tarrasque, turning the caster into the target."}
           {:name "Siege Monster" :description "The tarrasque deals double damage to objects and structures."}]

  :actions [{:name "Multiattack" :description "The tarrasque can use its Frightful Presence. It then makes five attacks: one with its bite, two with its claws, one with its horns, and one with its tail. It can use its Swallow instead of its bite."}
            {:name "Bite" :description "Melee Weapon Attack: +19 to hit, reach 10 ft., one target. Hit: 36 (4d12 + 10) piercing damage. If the target is a creature, it is grappled (escape DC 20). Until this grapple ends, the target is restrained, and the tarrasque can't bite another target."}
            {:name "Claw" :description "Melee Weapon Attack: +19 to hit, reach 15 ft., one target. Hit: 28 (4d8 + 10) slashing damage."}
            {:name "Horns" :description "Melee Weapon Attack: +19 to hit, reach 10 ft., one target. Hit: 32 (4d10 + 10) piercing damage."}
            {:name "Tail" :description "Melee Weapon Attack: +19 to hit, reach 20 ft., one target. Hit: 24 (4d6 + 10) bludgeoning damage. If the target is a creature, it must succeed on a DC 20 Strength saving throw or be knocked prone."}
            {:name "Frightful Presence" :description "Each creature of the tarrasque's choice within 120 feet of it and aware of it must succeed on a DC 17 Wisdom saving throw or become frightened for 1 minute. A creature can repeat the saving throw at the end of each of its turns, with disadvantage if the tarrasque is within line of sight, ending the effect on itself on a success. If a creature's saving throw is successful or the effect ends for it, the creature is immune to the tarrasque's Frightful Presence for the next 24 hours."}
            {:name "Swallow" :description "The tarrasque makes one bite attack against a Large or smaller creature it is grappling. If the attack hits, the target takes the bite's damage, the target is swallowed, and the grapple ends. While swallowed, the creature is blinded and restrained, it has total cover against attacks and other effects outside the tarrasque, and it takes 56 (16d6) acid damage at the start of each of the tarrasque's turns.
If the tarrasque takes 60 damage or more on a single turn from a creature inside it, the tarrasque must succeed on a DC 20 Constitution saving throw at the end of that turn or regurgitate all swallowed creatures, which fall prone in a space within 10 feet of the tarrasque. If the tarrasque dies, a swallowed creature is no longer restrained by it and can escape from the corpse by using 30 feet of movement, exiting prone."}]


  :legendary-actions {:description "The tarrasque can take 3 legendary actions, choosing from the options below. Only one legendary action option can be used at a time and only at the end of another creature's turn. The tarrasque regains spent legendary actions at the start of its turn."
                      :actions [{:name "Attack" :description "The tarrasque makes one claw attack or tail attack."}
                                {:name "Move" :description "The tarrasque moves up to half its speed."}
                                {:name "Chomp" :notes "Costs 2 Actions" :description "The tarrasque makes one bite attack or uses its Swallow."}]}
}{
  :name "Treant"
  :size :huge
  :type :plant
  :alignment "chaotic good"
  :armor-class 16
  :armor-notes "natural armor"
  :hit-points {:mean 138 :die-count 12 :die 12 :modifier 60}
  :speed "30 ft."

  :str 23
  :dex 8
  :con 21
  :int 12
  :wis 16
  :cha 12
                                                                                                                                                    
  :damage-resistances "bludgeoning, piercing"
  :damage-vulnerabilities "fire"
  :senses "passive Perception 13"
  :languages "Common, Druidic, Elvish,  Sylvan"
  :challenge 9

  :traits [{:name "False Appearance" :description "While the treant remains motionless, it is indistinguishable from a normal tree."}
           {:name "Siege Monster" :description "The treant deals double damage to objects and structures."}]

  :actions [{:name "Multiattack" :description "The treant makes two slam attacks."}
            {:name "Slam" :description "Melee Weapon Attack: +10 to hit, reach 5 ft., one target. Hit: 16 (3d6 + 6) bludgeoning damage."}
            {:name "Rock" :description "Ranged Weapon Attack: +10 to hit, range 60/180 ft., one target. Hit: 28 (4d10 + 6) bludgeoning damage."}
            {:name "Animate Trees" :notes "1/Day" :description "The treant magically animates one or two trees it can see within 60 feet of it. These trees have the same statistics as a treant, except they have Intelligence and Charisma scores of 1, they can't speak, and they have only the Slam action option. An"}]
}{
  :name "Troll"
  :size :large
  :type :giant
  :alignment "chaotic evil"
  :armor-class 15
  :armor-notes "natural armor"
  :hit-points {:mean 84 :die-count 8 :die 10 :modifier 40}
  :speed "30 ft."

  :str 18
  :dex 13
  :con 20
  :int 7
  :wis 9
  :cha 7

  :skills {:perception 2}
  :senses "darkvision 60 ft., passive Perception 12"
  :languages "Giant"
  :challenge 5

  :traits [{:name "Keen Smell" :description "The troll has advantage on Wisdom (Perception) checks that rely on smell."}
           {:name "Regeneration" :description "The troll regains 10 hit points at the start of its turn. If the troll takes acid or fire damage, this trait doesn't function at the start of the troll's next turn. The troll dies only if it starts its turn with 0 hit points and doesn't regenerate."}]

  :actions [{:name "Multiattack" :description "The troll makes three attacks: one with its bite and two with its claws."}
            {:name "Bite" :description "Melee Weapon Attack: +7 to hit, reach 5 ft., one target. Hit: 7 (1d6 + 4) piercing damage."}
            {:name "Claw" :description "Melee Weapon Attack: +7 to hit, reach 5 ft., one target. Hit: 11 (2d6 + 4) slashing damage."}]
}{
  :name "Unicorn"
  :size :large
  :type :celestial
  :alignment "lawful good"
  :armor-class 12
  :hit-points {:mean 67 :die-count 9 :die 10 :modifier 18}
  :speed "50 ft."

  :str 18
  :dex 14
  :con 15
  :int 11
  :wis 17
  :cha 16
                                                                                                                                        
  :damage-immunities "poison"
  :condition-immunities "charmed, paralyzed, poisoned"
  :senses "darkvision 60 ft., passive Perception 13"
  :languages "Celestial, Elvish, Sylvan, telepathy 60 ft."
  :challenge 5

  :traits [{:name "Charge" :description "If the unicorn moves at least 20 feet straight toward a target and then hits it with a horn attack on the same turn, the target takes an extra 9 (2d8) piercing damage. If the target is a creature, it must succeed on a DC 15 Strength saving throw or be knocked prone."}
           {:name "Innate Spellcasting" :description "The unicorn's innate spellcasting ability is Charisma (spell save DC 14). 
The unicorn can innately cast the following spells, requiring no components:
At will: detect evil and good, druidcraft, pass without trace 1/day each: calm emotions, dispel evil and good, entangle"}
           {:name "Magic Resistance" :description "The unicorn has advantage on saving throws against spells and other magical effects."}
           {:name "Magic Weapons" :description "The unicorn's weapon attacks are magical."}]

  :actions [{:name "Multiattack" :description "The unicorn makes two attacks: one with its hooves and one with its horn."}
            {:name "Hooves" :description "Melee Weapon Attack: +7 to hit, reach 5 ft., one target. Hit: 11 (2d6 + 4) bludgeoning damage."}
            {:name "Horn" :description "Melee Weapon Attack: +7 to hit, reach 5 ft., one target. Hit: 8 (1d8 + 4) piercing damage."}
            {:name "Healing Touch" :notes "3/Day" :description "The unicorn touches another creature with its horn. The target magically regains 11 (2d8 + 2) hit points. In addition, the touch removes all diseases and neutralizes all poisons afflicting the target."}
            {:name "Teleport" :notes "1/Day" :description "The unicorn magically teleports itself and up to three willing creatures it can see within 5 feet of it, along with any equipment they are wearing or carrying, to a location the unicorn is familiar with, up to 1 mile away."}]

  :legendary-actions {:description "The unicorn can take 3 legendary actions, choosing from the options below. Only one legendary action option can be used at a time and only at the end of another creature's turn. The unicorn regains spent legendary actions at the start of its turn."
                      :actions [{:name "Hooves" :description "The unicorn makes one attack with its hooves."}
                                {:name "Shimmering Shield" :notes "Costs 2 Actions" :description "The unicorn creates a shimmering, magical field around itself or another creature it can see within 60 feet of it. The target gains a +2 bonus to AC until the end of the unicorn's next turn."}
                                {:name "Heal Self" :notes "Costs 3 Actions" :description "The unicorn magically regains 11 (2d8 + 2) hit points."}]}
}{
  :name "Vampire"
  :size :medium
  :type :undead
  :subtypes #{:shapechanger}
  :alignment "lawful evil"
  :armor-class 16
  :armor-notes "natural armor"
  :hit-points {:mean 144 :die-count 17 :die 8 :modifier 68}
  :speed "30 ft."

  :str 18
  :dex 18
  :con 18
  :int 17
  :wis 15
  :cha 18
                                                                                                                                                       
  :saving-throws {:dex 9, :wis 7, :cha 9}
  :skills {:perception 7, :stealth 9}
  :damage-resistances "necrotic; bludgeoning, piercing, and slashing from nonmagical attacks"
  :senses "darkvision 120 ft., passive Perception 17"
  :languages "the languages it knew in life"
  :challenge 13

  :traits [{:name "Shapechanger" :description "If the vampire isn't in sunlight or running water, it can use its action to polymorph into a Tiny bat or a Medium cloud of mist, or back into its true form.
While in bat form, the vampire can't speak, its walking speed is 5 feet, and it has a flying speed of 30 feet. Its statistics, other than its size and speed, are unchanged. Anything it is wearing transforms with it, but nothing it is carrying does. It reverts to its true form if it dies.
While in mist form, the vampire can't take any actions, speak, or manipulate objects. It is weightless, has a flying speed of 20 feet, can hover, and can enter a hostile creature's space and stop there. In addition, if air can pass through a space, the mist can do so without squeezing, and it can't pass through water. It has advantage on Strength, Dexterity, and Constitution saving throws, and it is immune to all nonmagical damage, except the damage it takes from sunlight."}
           {:name "Legendary Resistance" :notes "3/Day" :description "If the vampire fails a saving throw, it can choose to succeed instead."}
           {:name "Misty Escape" :description "When it drops to 0 hit points outside its resting place, the vampire transforms into a cloud of mist (as in the Shapechanger trait) instead of falling unconscious, provided that it isn't in sunlight or running water. If it can't transform, it is destroyed.
While it has 0 hit points in mist form, it can't revert to its vampire form, and it must reach its resting place within 2 hours or be destroyed. Once in its resting place, it reverts to its vampire form. It is then paralyzed until it regains at least 1 hit point. After spending 1 hour in its resting place with 0 hit points, it regains 1 hit point."}
           {:name "Regeneration" :description "The vampire regains 20 hit points at the start of its turn if it has at least 1 hit point and isn't in sunlight or running water. If the vampire takes radiant damage or damage from holy water, this trait doesn't function at the start of the vampire's next turn."}
           {:name "Spider Climb" :description "The vampire can climb difficult surfaces, including upside down on ceilings, without needing to make an ability check."}
           {:name "Vampire Weaknesses" :description "The vampire has the following flaws:
Forbiddance The vampire can't enter a residence without an invitation from one of the occupants.
Harmed by Running Water. The vampire takes 20 acid damage if it ends its turn in running water.
Stake to the Heart. If a piercing weapon made of wood is driven into the vampire's heart while the vampire is incapacitated in its resting place, the vampire is paralyzed until the stake is removed.
Sunlight Hypersensitivity. The vampire takes 20 radiant damage when it starts its turn in sunlight. While in sunlight, it has disadvantage on attack rolls and ability checks."}]

  :actions [{:name "Multiattack" :notes "Vampire Form Only" :description "The vampire makes two attacks, only one of which can be a bite attack."}
            {:name "Unarmed Strike" :notes "Vampire Form Only" :description "Melee Weapon Attack: +9 to hit, reach 5 ft., one creature. Hit: 8 (1d8 + 4) bludgeoning damage. Instead of dealing damage, the vampire can grapple the target (escape DC 18)."}
            {:name "Bite" :notes "Bat or Vampire Form Only" :description "Melee Weapon Attack: +9 to hit, reach 5 ft., one willing creature, or a creature that is grappled by the vampire, incapacitated, or restrained Hit: 7 (1d6 + 4) piercing damage plus 10 (3d6) necrotic damage. The target's hit point maximum is reduced by an amount equal to the necrotic damage taken, and the vampire regains hit points equal to that amount. The reduction lasts until the target finishes a long rest. The target dies if this effect reduces its hit point maximum to 0. A humanoid slain in this way and then buried in the ground rises the following night as a vampire spawn under the vampire's control."}
            {:name "Charm" :description "The vampire targets one humanoid it can see within 30 feet of it. If the target can see the vampire, the target must succeed on a DC 17 Wisdom saving throw against this magic or be charmed by the vampire. The charmed target regards the vampire as a trusted friend to be heeded and protected. Although the target isn't under the vampire's control, it takes the vampire's requests or actions in the most favorable way it can, and it is a willing target for the vampire's bite attack.
Each time the vampire or the vampire's companions do anything harmful to the target, it can repeat the saving throw, ending the effect on itself on a success. Otherwise, the effect lasts 24 hours or until the vampire is destroyed, is on a different plane of existence than the target, or takes a bonus action to end the effect."}
            {:name "Children of the Night" :notes "(1/Day)" :description "The vampire magically calls 2d4 swarms of bats or rats, provided that the sun isn't up. While outdoors, the vampire can call 3d6 wolves instead. The called creatures arrive in 1d4 rounds, acting as allies of the vampire and obeying its spoken commands. The beasts remain for 1 hour, until the vampire dies, or until the vampire dismisses them as a bonus action."}]

  :legendary-actions {:description "The vampire can take 3 legendary actions, choosing from the options below. Only one legendary action option can be used at a time and only at the end of another creature's turn. The vampire regains spent legendary actions at the start of its turn."
                      :actions [{:name "Move" :description "The vampire moves up to its speed without provoking opportunity attacks."}
                                {:name "Unarmed Strike" :description "The vampire makes one unarmed strike."}
                                {:name "Bite" :notes "Costs 2 Actions" :description "The vampire makes one bite attack."}]}
}{
  :name "Vampire Spawn"
  :size :medium
  :type :undead
  :alignment "neutral evil"
  :armor-class 15
  :armor-notes "natural armor"
  :hit-points {:mean 82 :die-count 11 :die 8 :modifier 33}
  :speed "30 ft."

  :str 16
  :dex 16
  :con 16
  :int 11
  :wis 10
  :cha 12
                                                                                                                             
  :saving-throws {:dex 6, :wis 3}
  :skills {:perception 3, :stealth 6}
  :damage-resistances "necrotic; bludgeoning, piercing, and slashing from nonmagical attacks"
  :senses "darkvision 60 ft., passive Perception 13"
  :languages "the languages it knew in life"
  :challenge 5

  :traits [{:name "Regeneration" :description "The vampire regains 10 hit points at the start of its turn if it has at least 1 hit point and isn't in sunlight or running water. If the vampire takes radiant damage or damage from holy water, this trait doesn't function at the start of the vampire's next turn."}
           {:name "Spider Climb" :description "The vampire can climb difficult surfaces, including upside down on ceilings, without needing to make an ability check."}
           {:name "Vampire Weaknesses" :description "The vampire has the following flaws:
Forbiddance. The vampire can't enter a residence without an invitation from one of the occupants.
Harmed by Running Water. The vampire takes 20 acid damage when it ends its turn in running water.
Stake to the Heart. The vampire is destroyed if a piercing weapon made of wood is driven into its heart while it is incapacitated in its resting place.
Sunlight Hypersensitivity. The vampire takes 20 radiant damage when it starts its turn in sunlight. While in sunlight, it has disadvantage on attack rolls and ability checks."}]

  :actions [{:name "Multiattack" :description "The vampire makes two attacks, only one of which can be a bite attack."}
            {:name "Claws" :description "Melee Weapon Attack: +6 to hit, reach 5 ft., one creature. Hit: 8 (2d4 + 3) slashing damage. Instead of dealing damage, the vampire can grapple the target (escape DC 13)."}
            {:name "Bite" :description "Melee Weapon Attack: +6 to hit, reach 5 ft., one willing creature, or a creature that is grappled by the vampire, incapacitated, or restrained. Hit: 6 (1d6 + 3) piercing damage plus 7 (2d6) necrotic damage. The target's hit point maximum is reduced by an amount equal to the necrotic damage taken, and the vampire regains hit points equal to that amount. The reduction lasts until the target finishes a long rest. The target dies if this effect reduces its hit point maximum to 0."}]
}{
  :name "Wight"
  :size :medium
  :type :undead
  :alignment "neutral evil"
  :armor-class 14
  :armor-notes "studded leather"
  :hit-points {:mean 45 :die-count 6 :die 8 :modifier 18}
  :speed "30 ft."

  :str 15
  :dex 14
  :con 16
  :int 10
  :wis 13
  :cha 15

  :skills {:perception 3, :stealth 4}
  :damage-resistances "necrotic; bludgeoning, piercing, and slashing from nonmagical attacks that aren't silvered"
  :damage-immunities "poison"
  :condition-immunities "exhaustion, poisoned"
  :senses "darkvision 60 ft., passive Perception 13"
  :languages "the languages it knew in life"
  :challenge 3

  :traits [{:name "Sunlight Sensitivity" :description "While in sunlight, the wight has disadvantage on attack rolls, as well as on Wisdom (Perception) checks that rely on sight."}]

  :actions [{:name "Multiattack" :description "The wight makes two longsword attacks or two longbow attacks. It can use its Life Drain in place of one longsword attack."}
            {:name "Life Drain" :description "Melee Weapon Attack: +4 to hit, reach 5 ft., one creature. Hit: 5 (1d6 + 2) necrotic damage. The target must succeed on a DC 13 Constitution saving throw or its hit point maximum is reduced by an amount equal to the damage taken. This reduction lasts until the target finishes a long rest. The target dies if this effect reduces its hit point maximum to 0.
A humanoid slain by this attack rises 24 hours later as a zombie under the wight's control, unless the humanoid is restored to life or its body is destroyed. The wight can have no more than twelve zombies under its control at one time."}
            {:name "Longsword" :description "Melee Weapon Attack: +4 to hit, reach 5 ft., one target. Hit: 6 (1d8 + 2) slashing damage, or 7 (1d10 + 2) slashing damage if used with two hands."}
            {:name "Longbow" :description "Ranged Weapon Attack: +4 to hit, range 150/600 ft., one target. Hit: 6 (1d8 + 2) piercing damage."}]
}{
  :name "Will-o'-Wisp"
  :size :tiny
  :type :undead
  :alignment "chaotic evil"
  :armor-class 19
  :hit-points {:mean 22 :die-count 9 :die 4}
  :speed "0 ft., fly 50 ft. (hover)"

  :str 1
  :dex 28
  :con 10
  :int 13
  :wis 14
  :cha 11
                                                                                                                                                 
  :damage-immunities "lightning, poison"
  :damage-resistances "acid, cold, fire, necrotic, thunder; bludgeoning, piercing, and slashing from nonmagical attacks"
  :condition-immunities "exhaustion, grappled, paralyzed, poisoned, prone, restrained, unconscious"
  :senses "darkvision 120 ft., passive Perception 12"
  :languages "the languages it knew in life"
  :challenge 2

  :traits [{:name "Consume Life" :description "As a bonus action, the will-o'-wisp can target one creature it can see within 5 feet of it that has 0 hit points and is still alive. The target must succeed on a DC 10 Constitution saving throw against this magic or die. If the target dies, the will-o'-wisp regains 10 (3d6) hit points."}
           {:name "Ephemeral" :description "The will-o'-wisp can't wear or carry anything."}
           {:name "Incorporeal Movement" :description "The will-o'-wisp can move through other creatures and objects as if they were difficult terrain. It takes 5 (1d10) force damage if it ends its turn inside an object."}
           {:name "Variable Illumination" :description "The will-o'-wisp sheds bright light in a 5- to 20-foot radius and dim light for an additional number of feet equal to the chosen radius. The will-o'-wisp can alter the radius as a bonus action."}]

  :actions [{:name "Shock" :description "Melee Spell Attack: +4 to hit, reach 5 ft., one creature. Hit: 9 (2d8) lightning damage."}
            {:name "Invisibility" :description "The will-o'-wisp and its light magically become invisible until it attacks or uses its Consume Life, or until its concentration ends (as if concentrating on a spell)."}]
}{
  :name "Wraith"
  :size :medium
  :type :undead
  :alignment "neutral evil"
  :armor-class 13
  :hit-points {:mean 67 :die-count 9 :die 8 :modifier 27}
  :speed "0 ft., fly 60 ft. (hover)"

  :str 6
  :dex 16
  :con 16
  :int 12
  :wis 14
  :cha 15

  :damage-resistances "acid, cold, fire, lightning, thunder; bludgeoning, piercing, and slashing from nonmagical attacks that aren't silvered"
  :damage-immunities "necrotic, poison"
  :condition-immunities "charmed, exhaustion, grappled, paralyzed, petrified, poisoned, prone, restrained"
  :senses "darkvision 60 ft., passive Perception 12"
  :languages "the languages it knew in life"
  :challenge 5

  :traits [{:name "Incorporeal Movement" :description "The wraith can move through other creatures and objects as if they were difficult terrain. It takes 5 (1d10) force damage if it ends its turn inside an object."}
           {:name "Sunlight Sensitivity" :description "While in sunlight, the wraith has disadvantage on attack rolls, as well as on Wisdom (Perception) checks that rely on sight."}]

  :actions [{:name "Life Drain" :description "Melee Weapon Attack: +6 to hit, reach 5 ft., one creature. Hit: 21 (4d8 + 3) necrotic damage. The target must succeed on a DC 14 Constitution saving throw or its hit point maximum is reduced by an amount equal to the damage taken. This reduction lasts until the target finishes a long rest. The target dies if this effect reduces its hit point maximum to 0."}
            {:name "Create Specter" :description "The wraith targets a humanoid within 10 feet of it that has been dead for no longer than 1 minute and died violently. The target's spirit rises as a specter in the space of its corpse or in the nearest unoccupied space. The specter is under the wraith's control. The wraith can have no more than seven specters under its control at one time."}]
}{
  :name "Wyvern"
  :size :large
  :type :dragon
  :alignment "unaligned"
  :armor-class 13
  :armor-notes "natural armor"
  :hit-points {:mean 110 :die-count 13 :die 10 :modifier 39}
  :speed "20 ft., fly 80 ft."

  :str 19
  :dex 10
  :con 16
  :int 5
  :wis 12
  :cha 6

  :skills {:perception 4}
  :senses "darkvision 60 ft., passive Perception 14"
  :challenge 6

  :actions [{:name "Multiattack" :description "The wyvern makes two attacks: one with its bite and one with its stinger. While flying, it can use its claws in place of one other attack."}
            {:name "Bite" :description "Melee Weapon Attack: +7 to hit, reach 10 ft., one creature. Hit: 11 (2d6 + 4) piercing damage."}
            {:name "Claws" :description "Melee Weapon Attack: +7 to hit, reach 5 ft., one target. Hit: 13 (2d8 + 4) slashing damage."}
            {:name "Stinger" :description "Melee Weapon Attack: +7 to hit, reach 10 ft., one creature. Hit: 11 (2d6 + 4) piercing damage. The target must make a DC 15 Constitution saving throw, taking 24 (7d6) poison damage on a failed save, or half as much damage on a successful one."}]
}{
  :name "Xorn"
  :size :medium
  :type :elemental
  :alignment "neutral"
  :armor-class 19
  :armor-notes "natural armor"
  :hit-points {:mean 73 :die-count 7 :die 8 :modifier 42}
  :speed "20 ft., burrow 20 ft."

  :str 17
  :dex 10
  :con 22
  :int 11
  :wis 10
  :cha 11

  :skills {:perception 6, :stealth 3}
  :damage-resistances "piercing and slashing from nonmagical attacks that aren't adamantine"
  :senses "darkvision 60 ft., tremorsense 60 ft., passive Perception 16"
  :languages "Terran"
  :challenge 5

  :traits [{:name "Earth Glide" :description "The xorn can burrow through nonmagical, unworked earth and stone. While doing so, the xorn doesn't disturb the material it moves through."}
           {:name "Stone Camouflage" :description "The xorn has advantage on Dexterity (Stealth) checks made to hide in rocky terrain."}
           {:name "Treasure Sense" :description "The xorn can pinpoint, by scent, the location of precious metals and stones, such as coins and gems, within 60 feet of it."}]

  :actions [{:name "Multiattack" :description "The xorn makes three claw attacks and one bite attack."}
            {:name "Claw" :description "Melee Weapon Attack: +6 to hit, reach 5 ft., one target. Hit: 6 (1d6 + 3) slashing damage."}
            {:name "Bite" :description "Melee Weapon Attack: +6 to hit, reach 5 ft., one target. Hit: 13 (3d6 + 3) piercing damage."}]
}{
  :name "Zombie"
  :size :medium
  :type :undead
  :alignment "neutral evil"
  :armor-class 8
  :hit-points {:mean 22 :die-count 3 :die 8 :modifier 9}
  :speed "20 ft."

  :str 13
  :dex 6
  :con 16
  :int 3
  :wis 6
  :cha 5

  :saving-throws {:wis 0}
  :damage-immunities "poison"
  :condition-immunities "poisoned"
  :senses "darkvision 60 ft., passive Perception 8"
  :languages "understands the languages it knew in life but can't speak"
  :challenge (/ 1 4)

  :traits [{:name "Undead Fortitude" :description "If damage reduces the zombie to 0 hit points, it must make a Constitution saving throw with a DC of 5 + the damage taken, unless the damage is radiant or from a critical hit. On a success, the zombie drops to 1 hit point instead."}]

  :actions [{:name "Slam" :description "Melee Weapon Attack: +3 to hit, reach 5 ft., one target. Hit: 4 (1d6 + 1) bludgeoning damage."}]
}{
  :name "Ogre Zombie"
  :size :large
  :type :undead
  :alignment "neutral evil"
  :armor-class 8
  :hit-points {:mean 85 :die-count 9 :die 10 :modifier 36}
  :speed "30 ft."

  :str 19
  :dex 6
  :con 18
  :int 3
  :wis 6
  :cha 5
                                                                                                                                          
  :saving-throws {:wis 0}
  :damage-immunities "poison"
  :condition-immunities "poisoned"
  :senses "darkvision 60 ft., passive Perception 8"
  :languages "understands Common and Giant but can't speak"
  :challenge 2

  :traits [{:name "Undead Fortitude" :description "If damage reduces the zombie to 0 hit points, it must make a Constitution saving throw with a DC of 5 + the damage taken, unless the damage is radiant or from a critical hit. On a success, the zombie drops to 1 hit point instead."}]

  :actions [{:name "Morningstar" :description "Melee Weapon Attack: +6 to hit, reach 5 ft., one target. Hit: 13 (2d8 + 4) bludgeoning damage."}]
}{
  :name "Ape"
  :size :medium
  :type :beast
  :alignment "unaligned"
  :armor-class 12
  :hit-points {:mean 19 :die-count 3 :die 8 :modifier 6}
  :speed "30 ft., climb 30 ft."

  :str 16
  :dex 14
  :con 14
  :int 6
  :wis 12
  :cha 7

  :skills {:athletics 5, :perception 3}
  :senses "passive Perception 13	 	"
  :challenge (/ 1 2)

  :actions [{:name "Multiattack" :description "The ape makes two fist attacks."}
            {:name "Fist" :description "Melee Weapon Attack: +5 to hit, reach 5 ft., one target. Hit: 6 (1d6 + 3) bludgeoning damage."}
            {:name "Rock" :description "Ranged Weapon Attack: +5 to hit, range 25/50 ft., one target. Hit: 6 (1d6 + 3) bludgeoning damage."}]
}{
  :name "Awakened Shrub"
  :size :small
  :type :plant
  :alignment "unaligned"
  :armor-class 9
  :hit-points {:mean 10 :die-count 3 :die 6}
  :speed "20 ft."

  :str 3
  :dex 8
  :con 11
  :int 10
  :wis 10
  :cha 6

  :damage-vulnerabilities "fire"
  :damage-resistances "piercing"
  :senses "passive Perception 10"
  :languages "one language known by its creator"
  :challenge 0

  :traits [{:name "False Appearance" :description "While the shrub remains motionless, it is indistinguishable from a normal shrub."}]

  :actions [{:name "Rake" :description "Melee Weapon Attack: +1 to hit, reach 5 ft., one target. Hit: 1 (1d4 − 1) slashing damage."}]

  :description "An awakened shrub is an ordinary shrub given sentience and mobility by the awaken spell or similar magic."
}{
  :name "Awakened Tree"
  :size :huge
  :type :plant
  :alignment "unaligned"
  :armor-class 13
  :armor-notes "natural armor"
  :hit-points {:mean 59 :die-count 7 :die 12 :modifier 14}
  :speed "20 ft."

  :str 19
  :dex 6
  :con 15
  :int 10
  :wis 10
  :cha 7

  :damage-vulnerabilities "fire"
  :damage-resistances "bludgeoning, piercing"
  :senses "passive Perception 10"
  :languages "one language known by its creator"
  :challenge 2

  :traits [{:name "False Appearance" :description "While the tree remains motionless, it is indistinguishable from a normal tree."}]

  :actions [{:name "Slam" :description "Melee Weapon Attack: +6 to hit, reach 10 ft., one target. Hit: 14 (3d6 + 4) bludgeoning damage."}]

  :description "An awakened tree is an ordinary tree given sentience and mobility by the awaken spell or similar magic."
}{
  :name "Axe Beak"
  :size :large
  :type :beast
  :alignment "unaligned"
  :armor-class 11
  :hit-points {:mean 19 :die-count 3 :die 10 :modifier 3}
  :speed "50 ft."

  :str 14
  :dex 12
  :con 12
  :int 2
  :wis 10
  :cha 5

  :senses "passive Perception 10"
  :challenge (/ 1 4)

  :actions [{:name "Beak" :description "Melee Weapon Attack: +4 to hit, reach 5 ft., one target. Hit: 6 (1d8 + 2) slashing damage."}]

  :description "An axe beak is a tall flightless bird with strong legs and a heavy, wedge-shaped beak. It has a nasty disposition and tends to attack any unfamiliar  creature  that  wanders  too  close."
}{
  :name "Baboon"
  :size :small
  :type :beast
  :alignment "unaligned"
  :armor-class 12
  :hit-points {:mean 3 :die-count 1 :die 6}

  :str 8
  :dex 14
  :con 11
  :int 4
  :wis 12
  :cha 6

  :speed "30 ft., climb 30 ft."
  :senses "passive Perception 11"
  :challenge 0

  :traits [{:name "Pack Tactics" :description "The baboon has advantage on an attack roll against a creature if at least one of the baboon's allies is within 5 feet of the creature and the ally isn't incapacitated."}]

  :actions [{:name "Bite" :description "Melee Weapon Attack: +1 to hit, reach 5 ft., one target. Hit: 1 (1d4 − 1) piercing damage."}]
}{
  :name "Bat"
  :size :tiny
  :type :beast
  :alignment "unaligned"
  :armor-class 12
  :hit-points {:mean 1 :die-count 1 :die 6 :modifier -1}
  :speed "5 ft., fly 30 ft."

  :str 2
  :dex 15
  :con 8
  :int 2
  :wis 12
  :cha 4

  :senses "blindsight 60 ft., passive Perception 11"
  :challenge 0

  :traits [{:name "Echolocation" :description "The bat can't use its blindsight while deafened."}
           {:name "Keen Hearing" :description "The bat has advantage on Wisdom (Perception) checks that rely on hearing."}]

  :actions [{:name "Bite" :description "Melee Weapon Attack: +0 to hit, reach 5 ft., one creature. Hit: 1 piercing damage."}]
}{
  :name "Badger"
  :size :tiny
  :type :beast
  :alignment "unaligned"
  :armor-class 10
  :hit-points {:mean 3 :die-count 1 :die 4 :modifier 1}

  :speed "20 ft., burrow 5 ft."

  :str 4
  :dex 11
  :con 12
  :int 2
  :wis 12
  :cha 5

  :senses "darkvision 30 ft., passive Perception 11"
  :challenge 0

  :traits [{:name "Keen Smell" :description "The badger has advantage on Wisdom (Perception) checks that rely on smell."}]

  :actions [{:name "Bite" :description "Melee Weapon Attack: +2 to hit, reach 5 ft., one target. Hit: 1 piercing damage."}]
}{
  :name "Black Bear"
  :size :medium
  :type :beast
  :alignment "unaligned"
  :armor-class 11
  :armor-notes "natural armor"
  :hit-points {:mean 19 :die-count 3 :die 8 :modifier 6}
  :speed "40 ft., climb 30 ft."

  :str 15
  :dex 10
  :con 14
  :int 2
  :wis 12
  :cha 7

  :skills {:perception 3}
  :senses "passive Perception 13"
  :challenge (/ 1 2)

  :traits [{:name "Keen Smell" :description "The bear has advantage on Wisdom (Perception) checks that rely on smell."}]

  :actions [{:name "Multiattack" :description "The bear makes two attacks: one with its bite and one with its claws."}]
}{
  :name "Blink Dog"
  :size :medium
  :type :fey
  :alignment "lawful good"
  :armor-class 13
  :hit-points {:mean 22 :die-count 4 :die 8 :modifier 4}
  :speed "40 ft."

  :str 12
  :dex 17
  :con 12
  :int 10
  :wis 13
  :cha 11

  :skills {:perception 3, :stealth 5}
  :senses "passive Perception 13"
  :languages "Blink Dog, understands Sylvan but can't speak it"
  :challenge (/ 1 4)

  :traits [{:name "Keen Hearing and Smell" :description "The dog has advantage on Wisdom (Perception) checks that rely on hearing or smell."}]

  :actions [{:name "Bite" :description "Melee Weapon Attack: +3 to hit, reach 5 ft., one target. Hit: 4 (1d6 + 1) piercing damage."}
            {:name "Teleport" :notes "Recharge 4–6" :description "The dog magically teleports, along with any equipment it is wearing or carrying, up to 40 feet to an unoccupied space it can see. Before or after teleporting, the dog can make one bite attack."}]
}{
  :name "Blood Hawk"
  :size :small
  :type :beast
  :alignment "unaligned"
  :armor-class 12
  :hit-points {:mean 7 :die-count 2 :die 6}
  :speed "10 ft., fly 60 ft."

  :str 6
  :dex 14
  :con 10
  :int 3
  :wis 14
  :cha 5

  :skills {:perception 4}
  :senses "passive Perception 14"
  :challenge (/ 1 8)

  :traits [{:name "Keen Sight" :description "The hawk has advantage on Wisdom (Perception) checks that rely on sight."}
           {:name "Pack Tactics" :description "The hawk has advantage on an attack roll against a creature if at least one of the hawk's allies is within 5 feet of the creature and the ally isn't incapacitated."}]

  :actions [{:name "Beak" :description "Melee Weapon Attack: +4 to hit, reach 5 ft., one target. Hit: 4 (1d4 + 2) piercing damage."}]
}{
  :name "Boar"
  :size :medium
  :type :beast
  :alignment "unaligned"
  :armor-class 11
  :armor-notes "natural armor"
  :hit-points {:mean 11 :die-count 2 :die 8 :modifier 2}
  :speed "40 ft."

  :str 13
  :dex 11
  :con 12
  :int 2
  :wis 9
  :cha 5
                                                                                                                                       
  :senses "passive Perception 9"
  :challenge (/ 1 4)

  :traits [{:name "Charge" :description "If the boar moves at least 20 feet straight toward a target and then hits it with a tusk attack on the same turn, the target takes an extra 3 (1d6) slashing damage. If the target is a creature, it must succeed on a DC 11 Strength saving throw or be knocked prone."}
           {:name "Relentless" :notes "Recharges after a Short or Long Rest" :description "If the boar takes 7 damage or less that would reduce it to 0 hit points, it is reduced to 1 hit point instead."}]

  :actions [{:name "Tusk" :description "Melee Weapon Attack: +3 to hit, reach 5 ft., one target. Hit: 4 (1d6 + 1) slashing damage."}]
}{
  :name "Brown Bear"
  :size :large
  :type :beast
  :alignment "unaligned"
  :armor-class 11
  :armor-notes "natural armor"
  :hit-points {:mean 34 :die-count 4 :die 10 :modifier 12}
  :speed "40 ft., climb 30 ft."

  :str 19
  :dex 10
  :con 16
  :int 2
  :wis 13
  :cha 7

  :skills {:perception 3}
  :senses "passive Perception 13"
  :challenge 1

  :traits [{:name "Keen Smell" :description "The bear has advantage on Wisdom (Perception) checks that rely on smell."}]

  :actions [{:name "Multiattack" :description "The bear makes two attacks: one with its bite and one with its claws."}
            {:name "Bite" :description "Melee Weapon Attack: +5 to hit, reach 5 ft., one target. Hit: 8 (1d8 + 4) piercing damage."}
            {:name "Claws" :description "Melee Weapon Attack: +5 to hit, reach 5 ft., one target. Hit: 11 (2d6 + 4) slashing damage."}]
}{
  :name "Camel"
  :size :large
  :type :beast
  :alignment "unaligned"
  :armor-class 9
  :hit-points {:mean 15 :die-count 2 :die 10 :modifier 4}
  :speed "50 ft."

  :str 16
  :dex 8
  :con 14
  :int 2
  :wis 8
  :cha 5

  :skills {:perception 3}
  :senses "passive Perception 13"
  :challenge (/ 1 8)

  :actions [{:name "Bite" :description "Melee Weapon Attack: +5 to hit, reach 5 ft., one target. Hit: 2 (1d4) bludgeoning damage."}]
}{
  :name "Cat"
  :size :tiny
  :type :beast
  :alignment "unaligned"
  :armor-class 12
  :hit-points {:mean 2 :die-count 1 :die 4}
  :speed "40 ft., climb 30 ft."

  :str 3
  :dex 15
  :con 10
  :int 3
  :wis 12
  :cha 7

  :skills {:perception 3, :stealth 4}
  :senses "passive Perception 13"

  :challenge 0

  :traits [{:name "Keen Smell" :description "The cat has advantage on Wisdom (Perception) checks that rely on smell."}]

  :actions [{:name "Claws" :description "Melee Weapon Attack: +0 to hit, reach 5 ft., one target. Hit: 1 slashing damage."}]
}{
  :name "Constrictor Snake"
  :size :large
  :type :beast
  :alignment "unaligned"
  :armor-class 12
  :hit-points {:mean 13 :die-count 2 :die 10 :modifier 2}
  :speed "30 ft., swim 30 ft."

  :str 15
  :dex 14
  :con 12
  :int 1
  :wis 10
  :cha 3
                                                                                                                              
  :senses "blindsight 10 ft., passive Perception 10"
  :challenge (/ 1 4)

  :actions [{:name "Bite" :description "Melee Weapon Attack: +4 to hit, reach 5 ft., one creature. Hit: 5 (1d6 + 2) piercing damage."}
            {:name "Constrict" :description "Melee Weapon Attack: +4 to hit, reach 5 ft., one creature. Hit: 6 (1d8 + 2) bludgeoning damage, and the target is grappled (escape DC 14). Until this grapple ends, the creature is restrained, and the snake can't constrict another target."}]
}{
  :name "Crab"
  :size :tiny
  :type :beast
  :alignment "unaligned"
  :armor-class 11
  :armor-notes "natural armor"
  :hit-points {:mean 2 :die-count 1 :die 4}
  :speed "20 ft., swim 20 ft."

  :str 2
  :dex 11
  :con 10
  :int 1
  :wis 8
  :cha 2

  :skills {:stealth 2}
  :senses "blindsight 30 ft., passive Perception 9"
  :challenge 0

  :traits [{:name "Amphibious" :description "The crab can breathe air and water."}]

  :actions [{:name "Claw" :description "Melee Weapon Attack: +0 to hit, reach 5 ft., one target. Hit: 1 bludgeoning damage."}]
}{
  :name "Crocodile"
  :size :large
  :type :beast
  :alignment "unaligned"
  :armor-class 12
  :armor-notes "natural armor"
  :hit-points {:mean 19 :die-count 3 :die 10 :modifier 3}
  :speed "20 ft., swim 30 ft."

  :str 15
  :dex 10
  :con 13
  :int 2
  :wis 10
  :cha 5

  :traits [{:name "Hold Breath" :description "The crocodile can hold its breath for 15 minutes."}]

  :skills {:stealth 2}
  :senses "passive Perception 10"
  :challenge (/ 1 2)

  :actions [{:name "Bite" :description "Melee Weapon Attack: +4 to hit, reach 5 ft., one creature. Hit: 7 (1d10 + 2) piercing damage, and the target is grappled (escape DC 12). Until this grapple ends, the target is restrained, and the crocodile can't bite another target."}]
}{
  :name "Death Dog"
  :size :medium
  :type :monstrosity
  :alignment "neutral evil"
  :armor-class 12
  :hit-points {:mean 39 :die-count 6 :die 8 :modifier 12}
  :speed "40 ft."

  :str 15
  :dex 14
  :con 14
  :int 3
  :wis 13
  :cha 6

  :skills {:perception 5, :stealth 4}
  :senses "darkvision 120 ft., passive Perception 15"
  :challenge 1

  :traits [{:name "Two-Headed" :description "The dog has advantage on Wisdom (Perception) checks and on saving throws against being blinded, charmed, deafened, frightened, stunned, or knocked unconscious."}]

  :actions [{:name "Multiattack" :description "The dog makes two bite attacks."}
            {:name "Bite" :description "Melee Weapon Attack: +4 to hit, reach 5 ft., one target. Hit: 5 (1d6 + 2) piercing damage. If the target is a creature, it must succeed on a DC 12 Constitution saving throw against disease or become poisoned until the disease is cured. Every 24 hours that elapse, the creature must repeat the saving throw, reducing its hit point maximum by 5 (1d10) on a failure. This reduction lasts until the disease is cured. The creature dies if the disease reduces its hit point maximum to 0."}]

  :description "A death dog is an ugly two-headed hound that roams plains, and deserts. Hate burns in a death   dog's heart, and a taste for humanoid flesh drives it   to attack travelers and explorers. Death dog saliva carries a foul disease that causes a victim's flesh to slowly rot off the  bone."
}{
  :name "Deer"
  :size :medium
  :type :beast
  :alignment "unaligned"
  :armor-class 13
  :hit-points {:mean 4 :die-count 1 :die 8}
  :speed "50 ft."

  :str 11
  :dex 16
  :con 11
  :int 2
  :wis 14
  :cha 5
                                                                                                                                                                                                                                                                                                               
  :senses "passive Perception 12"
  :challenge 0

  :actions [{:name "Bite" :description "Melee Weapon Attack: +2 to hit, reach 5 ft., one target. Hit: 2 (1d4) piercing damage."}]
}{
  :name "Dire Wolf"
  :size :large
  :type :beast
  :alignment "unaligned"
  :armor-class 14
  :armor-notes "natural armor"
  :hit-points {:mean 37 :die-count 5 :die 10 :modifier 10}
  :speed "50 ft."

  :str 17
  :dex 15
  :con 15
  :int 3
  :wis 12
  :cha 7

  :skills {:perception 3, :stealth 4}
  :senses "passive Perception 13"
  :challenge 1

  :traits [{:name "Keen Hearing and Smell." :description "The wolf has advantage on Wisdom (Perception) checks that rely on hearing or smell."}
           {:name "Pack Tactics" :description "The wolf has advantage on an attack roll against a creature if at least one of the wolf's allies is within 5 feet of the creature and the ally isn't incapacitated."}]

  :actions [{:name "Bite" :description "Melee Weapon Attack: +5 to hit, reach 5 ft., one target. Hit: 10 (2d6 + 3) piercing damage. If the target is a creature, it must succeed on a DC 13 Strength saving throw or be knocked prone."}]
}{
  :name "Draft Horse"
  :size :large
  :type :beast
  :alignment "unaligned"
  :armor-class 10
  :hit-points {:mean 19 :die-count 3 :die 10 :modifier 3}
  :speed "40 ft."

  :str 18
  :dex 10
  :con 12
  :int 2
  :wis 11
  :cha 7

  :senses "passive Perception 10"
  :challenge (/ 1 4)

  :actions [{:name "Hooves" :description "Melee Weapon Attack: +6 to hit, reach 5 ft., one target. Hit: 9 (2d4 + 4) bludgeoning damage."}]
}{
  :name "Eagle"
  :size :small
  :type :beast
  :alignment "unaligned"
  :armor-class 12
  :hit-points {:mean 3 :die-count 1 :die 6}
  :speed "10 ft., fly 60 ft."

  :str 6
  :dex 15
  :con 10
  :int 2
  :wis 14
  :cha 7

  :skills {:perception 4}
  :senses "passive Perception 14"
  :challenge 0

  :traits [{:name "Keen Sight" :description "The eagle has advantage on Wisdom (Perception) checks that rely on sight."}]

  :actions [{:name "Talons" :description "Melee Weapon Attack: +4 to hit, reach 5 ft., one target. Hit: 4 (1d4 + 2) slashing damage."}]
}{
  :name "Elephant"
  :size :huge
  :type :beast
  :alignment "unaligned"
  :armor-class 12
  :armor-notes "natural armor"
  :hit-points {:mean 76 :die-count 8 :die 12 :modifier 24}
  :speed "40 ft."

  :str 22
  :dex 9
  :con 17
  :int 3
  :wis 11
  :cha 6
                                                                                                                                         
  :senses "passive Perception 10"
  :challenge 4

  :traits [{:name "Trampling Charge" :description "If the elephant moves at least 20 feet straight toward a creature and then hits it with a gore attack on the same turn, that target must succeed on a DC 12 Strength saving throw or be knocked prone. If the target is prone, the elephant can make one stomp attack against it as a bonus action."}]

  :actions [{:name "Gore" :description "Melee Weapon Attack: +8 to hit, reach 5 ft., one target. Hit: 19 (3d8 + 6) piercing damage."}
            {:name "Stomp" :description "Melee Weapon Attack: +8 to hit, reach 5 ft., one prone creature. Hit: 22 (3d10 + 6) bludgeoning damage."}]
}{
  :name "Elk"
  :size :large
  :type :beast
  :alignment "unaligned"
  :armor-class 10
  :hit-points {:mean 13 :die-count 2 :die 10 :modifier 2}
  :speed "50 ft."

  :str 16
  :dex 10
  :con 12
  :int 2
  :wis 10
  :cha 6

  :senses "passive Perception 10"
  :challenge (/ 1 4)

  :traits [{:name "Charge" :description "If the elk moves at least 20 feet straight toward a target and then hits it with a ram attack on"}]

  :actions [{:name "Ram" :description "Melee Weapon Attack: +5 to hit, reach 5 ft., one target. Hit: 6 (1d6 + 3) bludgeoning damage."}
            {:name "Hooves" :description "Melee Weapon Attack: +5 to hit, reach 5 ft., one prone creature. Hit: 8 (2d4 + 3) bludgeoning damage."}]
}{
  :name "Flying Snake"
  :size :tiny
  :type :beast
  :alignment "unaligned"
  :armor-class 14
  :hit-points {:mean 5 :die-count 2 :die 4}
  :speed "30 ft., fly 60 ft., swim 30 ft."

  :str 4
  :dex 18
  :con 11
  :int 2
  :wis 12
  :cha 5

  :senses "blindsight 10 ft., passive Perception 11"
  :challenge (/ 1 8)

  :trait [{:name "Flyby" :description "The snake doesn't provoke opportunity attacks when it flies out of an enemy's reach."}]

  :actions [{:name "Bite" :description "Melee Weapon Attack: +6 to hit, reach 5 ft., one target. Hit: 1 piercing damage plus 7 (3d4) poison damage."}]
  :description "A flying snake is a brightly colored, winged serpent found in remote jungles. Tribespeople and cultists sometimes domesticate flying snakes to serve as messengers that deliver scrolls wrapped in their coils."
}{
  :name "Frog"
  :size :tiny
  :type :beast
  :alignment "unaligned"
  :armor-class 11
  :hit-points {:mean 1 :die-count 1 :die 4 :modifier -1}
  :speed "20 ft., swim 20 ft."

  :str 1
  :dex 13
  :con 8
  :int 1
  :wis 8
  :cha 3

  :skills {:perception 1, :stealth 3}
  :senses "darkvision 30 ft., passive Perception 11"
  :challenge 0

  :traits [{:name "Amphibious" :description "The frog can breathe air and water."}
           {:name "Standing Leap" :description "The frog's long jump is up to 10 feet and its high jump is up to 5 feet, with or without a running start."}]

  :description "A frog has no effective attacks. It feeds on small insects and typically dwells near water, in trees, or underground. The frog's statistics can also be used to represent a toad."
}{
  :name "Giant Ape"
  :size :huge
  :type :beast
  :alignment "unaligned"
  :armor-class 12
  :hit-points {:mean 157 :die-count 15 :die 12 :modifier 60}
  :speed "40 ft., climb 40 ft."

  :str 23
  :dex 14
  :con 18
  :int 7
  :wis 12
  :cha 7

  :skills {:athletics 9, :perception 4}
  :senses "passive Perception 14"
  :challenge 7

  :actions [{:name "Multiattack" :description "The ape makes two fist attacks."}
            {:name "Fist" :description "Melee Weapon Attack: +9 to hit, reach 10 ft., one target. Hit: 22 (3d10 + 6) bludgeoning damage."}
            {:name "Rock" :description "Ranged Weapon Attack: +9 to hit, range 50/100 ft., one target. Hit: 30 (7d6 + 6) bludgeoning damage."}]
}{
  :name "Giant Badger"
  :size :medium
  :type :beast
  :alignment "unaligned"
  :armor-class 10
  :hit-points {:mean 13 :die-count 2 :die 8 :modifier 4}
  :speed "30 ft., burrow 10 ft."

  :str 13
  :dex 10
  :con 15
  :int 2
  :wis 12
  :cha 5

  :senses "darkvision 30 ft., passive Perception 11"

  :challenge (/ 1 4)

  :traits [{:name "Keen Smell" :description "The badger has advantage on Wisdom (Perception) checks that rely on smell."}]

  :actions [{:name "Multiattack" :description "The badger makes two attacks: one with its bite and one with its claws."}
            {:name "Bite" :description "Melee Weapon Attack: +3 to hit, reach 5 ft., one target. Hit: 4 (1d6 + 1) piercing damage."}
            {:name "Claws" :description "Melee Weapon Attack: +3 to hit, reach 5 ft., one target. Hit: 6 (2d4 + 1) slashing damage."}]
}{
  :name "Giant Bat"
  :size :large
  :type :beast
  :alignment "unaligned"
  :armor-class 13
  :hit-points {:mean 22 :die-count 4 :die 10}
  :speed "10 ft., fly 60 ft."

  :str 15
  :dex 16
  :con 11
  :int 2
  :wis 12
  :cha 6

  :senses "blindsight 60 ft., passive Perception 11"
  :challenge (/ 1 4)

  :traits [{:name "Echolocation" :description "The bat can't use its blindsight while deafened."}
           {:name "Keen Hearing" :description "The bat has advantage on Wisdom (Perception) checks that rely on hearing."}]

  :actions [{:name "Bite" :description "Melee Weapon Attack: +4 to hit, reach 5 ft., one creature. Hit: 5 (1d6 + 2) piercing damage."}]
}{
  :name "Giant Boar"
  :size :large
  :type :beast
  :alignment "unaligned"
  :armor-class 12
  :armor-notes "natural armor"
  :hit-points {:mean 42 :die-count 5 :die 10 :modifier 15}
  :speed "40 ft."

  :str 17
  :dex 10
  :con 16
  :int 2
  :wis 7
  :cha 5

  :senses "passive Perception 8"
  :challenge 2

  :traits [{:name "Charge" :description "If the boar moves at least 20 feet straight toward a target and then hits it with a tusk attack on the same turn, the target takes an extra 7 (2d6) slashing damage. If the target is a creature, it must succeed on a DC 13 Strength saving throw or be knocked prone."}
           {:name "Relentless" :notes "Recharges after a Short or Long Rest" :description "If the boar takes 10 damage or less that would reduce it to 0 hit points, it is reduced to 1 hit point instead."}]

  :actions [{:name "Tusk" :description "Melee Weapon Attack: +5 to hit, reach 5 ft., one target. Hit: 10 (2d6 + 3) slashing damage."}]
}{
  :name "Giant Centipede"
  :size :small
  :type :beast
  :alignment "unaligned"
  :armor-class 13
  :armor-notes "natural armor"
  :hit-points {:mean 4 :die-count 1 :die 6 :modifier 1}
  :speed "30 ft., climb 30 ft."

  :str 5
  :dex 14
  :con 12
  :int 1
  :wis 7
  :cha 3

  :senses "blindsight 30 ft., passive Perception 8"
  :challenge (/ 1 4)

  :actions [{:name "Bite" :description "Melee Weapon Attack: +4 to hit, reach 5 ft., one creature. Hit: 4 (1d4 + 2) piercing damage,and the target must succeed on a DC 11 Constitution saving throw or take 10 (3d6) poison damage. If the poison damage reduces the target to 0 hit points, the target is stable but poisoned for 1 hour, even after regaining hit points, and is paralyzed while poisoned in this way."}]
}{
  :name "Giant Constrictor Snake"
  :size :huge
  :type :beast
  :alignment "unaligned"
  :armor-class 12
  :hit-points {:mean 60 :die-count 8 :die 12 :modifier 8}
  :speed "30 ft., swim 30 ft."

  :str 19
  :dex 14
  :con 12
  :int 1
  :wis 10
  :cha 3

  :skills {:perception 2}
  :senses "blindsight 10 ft., passive Perception 12"
  :challenge 2

  :actions [{:name "Bite" :description "Melee Weapon Attack: +6 to hit, reach 10 ft., one creature. Hit: 11 (2d6 + 4) piercing damage."}
            {:name "Constrict" :description "Melee Weapon Attack: +6 to hit, reach 5 ft., one creature. Hit: 13 (2d8 + 4) bludgeoning damage, and the target is grappled (escape DC 16). Until this grapple ends, the creature is restrained, and the snake can't constrict another target."}]
}{
  :name "Giant Crab"
  :size :medium
  :type :beast
  :alignment "unaligned"
  :armor-class 15
  :armor-notes "natural armor"
  :hit-points {:mean 13 :die-count 3 :die 8}
  :speed "30 ft., swim 30 ft."

  :str 13
  :dex 15
  :con 11
  :int 1
  :wis 9
  :cha 3

  :skills {:stealth 4}
  :senses "blindsight 30 ft., passive Perception 9"
  :challenge (/ 1 8)

  :traits [{:name "Amphibious" :description "The crab can breathe air and water."}]

  :actions [{:name "Claw" :description "Melee Weapon Attack: +3 to hit, reach 5 ft., one target. Hit: 4 (1d6 + 1) bludgeoning damage, and the target is grappled (escape DC 11). The crab has two claws, each of which can grapple only one target."}]
}{
  :name "Giant Crocodile"
  :size :huge
  :type :beast
  :alignment "unaligned"
  :armor-class 14
  :armor-notes "natural armor"
  :hit-points {:mean 85 :die-count 9 :die 12 :modifier 27}
  :speed "30 ft., swim 50 ft."

  :str 21
  :dex 9
  :con 17
  :int 2
  :wis 10
  :cha 7

  :skills {:stealth 5}

  :senses "passive Perception 10"
  :challenge 5
  :traits [{:name "Hold Breath" :description "The crocodile can hold its breath for 30 minutes."}]

  :actions [{:name "Multiattack" :description "The crocodile makes two attacks: one with its bite and one with its tail."}
            {:name "Bite" :description "Melee Weapon Attack: +8 to hit, reach 5 ft., one target. Hit: 21 (3d10 + 5) piercing damage, and the target is grappled (escape DC 16). Until this grapple ends, the target is restrained, and the crocodile can't bite another target."}
            {:name "Tail" :description "Melee Weapon Attack: +8 to hit, reach 10 ft., one target not grappled by the crocodile. Hit: 14 (2d8 + 5) bludgeoning damage. If the target is a creature, it must succeed on a DC 16 Strength saving throw or be knocked prone."}]
}{
  :name "Giant Eagle"
  :size :large
  :type :beast
  :alignment "neutral good"
  :armor-class 13
  :hit-points {:mean 26 :die-count 4 :die 10 :modifier 4}
  :speed "10 ft., fly 80 ft."

  :str 16
  :dex 17
  :con 13
  :int 8
  :wis 14
  :cha 10

  :skills {:perception 4}
  :senses "passive Perception 14"
  :languages "Giant Eagle, understands Common and Auran but can't speak them"
  :challenge 1

  :traits [{:name "Keen Sight" :description "The eagle has advantage on Wisdom (Perception) checks that rely on sight."}]

  :actions [{:name "Multiattack" :description "The eagle makes two attacks: one with its beak and one with its talons."}
            {:name "Beak" :description "Melee Weapon Attack: +5 to hit, reach 5 ft., one target. Hit: 6 (1d6 + 3) piercing damage."}
            {:name "Talons" :description "Melee Weapon Attack: +5 to hit, reach 5 ft., one target. Hit: 10 (2d6 + 3) slashing damage."}]
}{
  :name "Giant Fire Beetle"
  :size :small
  :type :beast
  :alignment "unaligned"
  :armor-class 13
  :armor-notes "natural armor"
  :hit-points {:mean 4 :die-count 1 :die 6 :modifier 1}
  :speed "30 ft."

  :str 8
  :dex 10
  :con 12
  :int 1
  :wis 7
  :cha 3

  :senses "blindsight 30 ft., passive Perception 8"
  :challenge 0

  :traits [{:name "Illumination" :description "The beetle sheds bright light in a 10-foot radius and dim light for an additional 10 feet."}]

  :actions [{:name "Bite" :description "Melee Weapon Attack: +1 to hit, reach 5 ft., one target. Hit: 2 (1d6 − 1) slashing damage."}]

  :description "A giant fire beetle is a nocturnal creature that takes its name from a pair of glowing glands that give off light. Miners and adventurers prize these creatures, for a giant fire beetle's glands continue to shed light for 1d6 days after the beetle dies. Giant fire beetles are most commonly found underground and in dark forests."
}{
  :name "Giant Elk"
  :size :huge
  :type :beast
  :alignment "unaligned"
  :armor-class 14
  :armor-notes "natural armor"
  :hit-points {:mean 42 :die-count 5 :die 12 :modifier 10}
  :speed "60 ft."

  :str 19
  :dex 16
  :con 14
  :int 7
  :wis 14
  :cha 10

  :skills {:perception 4}
  :senses "passive Perception 14"
  :languages "Giant Elk, understands Common, Elvish, and Sylvan but can't speak them"
  :challenge 2

  :traits [{:name "Charge" :description "If the elk moves at least 20 feet straight toward a target and then hits it with a ram attack on the same turn, the target takes an extra 7 (2d6) damage. If the target is a creature, it must succeed on a DC 14 Strength saving throw or be knocked prone."}]

  :actions [{:name "Ram" :description "Melee Weapon Attack: +6 to hit, reach 10 ft., one target. Hit: 11 (2d6 + 4) bludgeoning damage."}
            {:name "Hooves" :description "Melee Weapon Attack: +6 to hit, reach 5 ft., one prone creature. Hit: 22 (4d8 + 4) bludgeoning damage."}]
  :description "The majestic giant elk is rare to the point that its appearance is often taken as a foreshadowing of an important event, such as the birth of a king. Legends tell of gods that take the form of giant elk when visiting the Material Plane. Many cultures therefore believe that to hunt these creatures is to invite  divine wrath."
}{
  :name "Giant Frog"
  :size :medium
  :type :beast
  :alignment "unaligned"
  :armor-class 11
  :hit-points {:mean 18 :die-count 4 :die 8}
  :speed "30 ft., swim 30 ft."

  :str 12
  :dex 13
  :con 11
  :int 2
  :wis 10
  :cha 3

  :skills {:perception 2, :stealth 3}
  :senses "darkvision 30 ft., passive Perception 12"
  :challenge (/ 1 4)

  :traits [{:name "Amphibious" :description "The frog can breathe air and water."}
           {:name "Standing Leap" :description "The frog's long jump is up to 20 feet and its high jump is up to 10 feet, with or without a running start."}]

  :actions [{:name "Bite" :description "Melee Weapon Attack: +3 to hit, reach 5 ft., one target. Hit: 4 (1d6 + 1) piercing damage, and the target is grappled (escape DC 11). Until this grapple ends, the target is restrained, and the frog can't bite another target."}
            {:name "Swallow" :description "The frog makes one bite attack against a or smaller target it is grappling. If the attack hits, the target is swallowed, and the grapple ends. The swallowed target is blinded and restrained, it has total cover against attacks and other effects outside the frog and it takes 5 (2d4) acid damage at the start of each of the frog's turns. The frog can have only one target swallowed at a time.
If the frog dies, a swallowed creature is no longer restrained by it and can escape from the corpse using 5 feet of movement, exiting prone."}]
}{
  :name "Giant Goat"
  :size :large
  :type :beast
  :alignment "unaligned"
  :armor-class 11
  :armor-notes "natural armor"
  :hit-points {:mean 19 :die-count 3 :die 10 :modifier 3}
  :speed "40 ft."

  :str 17
  :dex 11
  :con 12
  :int 3
  :wis 12
  :cha 6
                                                                                                                                                 
  :senses "passive Perception 11"
  :challenge (/ 1 2)

  :traits [{:name "Charge" :description "If the goat moves at least 20 feet straight toward a target and then hits it with a ram attack on the same turn, the target takes an extra 5 (2d4) bludgeoning damage. If the target is a creature, it must succeed on a DC 13 Strength saving throw or be knocked prone.
Sure-Footed. The goat has advantage on Strength and Dexterity saving throws made against effects that would knock it prone."}]

  :actions [{:name "Ram" :description "Melee Weapon Attack: +5 to hit, reach 5 ft., one target. Hit: 8 (2d4 + 3) bludgeoning damage."}]
}{
  :name "Giant Hyena"
  :size :large
  :type :beast
  :alignment "unaligned"
  :armor-class 12
  :hit-points {:mean 45 :die-count 6 :die 10 :modifier 12}
  :speed "50 ft."

  :str 16
  :dex 14
  :con 14
  :int 2
  :wis 12
  :cha 7

  :skills {:perception 3}
  :senses "passive Perception 13"
  :challenge 1

  :traits [{:name "Rampage" :description "When the hyena reduces a creature to 0 hit points with a melee attack on its turn, the hyena can take a bonus action to move up to half its speed and make a bite attack."}]

  :actions [{:name "Bite" :description "Melee Weapon Attack: +5 to hit, reach 5 ft., one target. Hit: 10 (2d6 + 3) piercing damage."}]
}{
  :name "Giant Lizard"
  :size :large
  :type :beast
  :alignment "unaligned"
  :armor-class 12
  :armor-notes "natural armor"
  :hit-points {:mean 19 :die-count 3 :die 10 :modifier 3}
  :speed "30 ft., climb 30 ft."

  :str 15
  :dex 12
  :con 13
  :int 2
  :wis 10
  :cha 5
                                                                                                                                        
  :senses "darkvision 30 ft., passive Perception 10"
  :challenge (/ 1 4)

  :actions [{:name "Bite" :description "Melee Weapon Attack: +4 to hit, reach 5 ft., one target. Hit: 6 (1d8 + 2) piercing damage."}]
  :description "A giant lizard can be ridden or used as a draft animal. Lizardfolk also keep them as pets, and subterranean giant lizards are used as mounts and pack animals by drow, duergar, and others."
}{
  :name "Giant Octopus"
  :size :large
  :type :beast
  :alignment "unaligned"
  :armor-class 11
  :hit-points {:mean 52 :die-count 8 :die 10 :modifier 8}
  :speed "10 ft., swim 60 ft."

  :str 17
  :dex 13
  :con 13
  :int 4
  :wis 10
  :cha 4

  :skills {:perception 4, :stealth 5}
  :senses "darkvision 60 ft., passive Perception 14"
  :challenge 1

  :traits [{:name "Hold Breath" :description "While out of water, the octopus can hold its breath for 1 hour."}
           {:name "Underwater Camouflage" :description "The octopus has advantage on Dexterity (Stealth) checks made while underwater."}
           {:name "Water Breathing" :description "The octopus can breathe only underwater."}]

  :actions [{:name "Tentacles" :description "Melee Weapon Attack: +5 to hit, reach 15 ft., one target. Hit: 10 (2d6 + 3) bludgeoning damage. If the target is a creature, it is grappled (escape DC 16). Until this grapple ends, the target is restrained, and the octopus can't use its tentacles on another target."}
            {:name "Ink Cloud" :notes "Recharges after a Short or Long Rest" :description "A 20- foot-radius cloud of ink extends all around the octopus if it is underwater. The area is heavily obscured for 1 minute, although a significant current can disperse the ink. After releasing the ink, the octopus can use the Dash action as a bonus action."}]
}{
  :name "Giant Owl"
  :size :large
  :type :beast
  :alignment "neutral"
  :armor-class 12
  :hit-points {:mean 19 :die-count 3 :die 10 :modifier 3}
  :speed "5 ft., fly 60 ft."

  :str 13
  :dex 15
  :con 12
  :int 8
  :wis 13
  :cha 10

  :skills {:perception 5, :stealth 4}
  :senses "darkvision 120 ft., passive Perception 15"
  :languages "Giant Owl, understands Common, Elvish, and Sylvan but can't speak them"
  :challenge (/ 1 4)

  :traits [{:name "Flyby" :description "The owl doesn't provoke opportunity attacks when it flies out of an enemy's reach. Keen Hearing and Sight. The owl has advantage on Wisdom (Perception) checks that rely on hearing or sight."}]

  :actions [{:name "Talons" :description "Melee Weapon Attack: +3 to hit, reach 5 ft., one target. Hit: 8 (2d6 + 1) slashing damage."}]
  :description "Giant owls often befriend fey and other sylvan creatures and are guardians of their woodland realms."
}{
  :name "Giant Poisonous Snake"
  :size :medium
  :type :beast
  :alignment "unaligned"
  :armor-class 14
  :hit-points {:mean 11 :die-count 2 :die 8 :modifier 2}
  :speed "30 ft., swim 30 ft."

  :str 10
  :dex 18
  :con 13
  :int 2
  :wis 10
  :cha 3

  :skills {:perception 2}
  :senses "blindsight 10 ft., passive Perception 12"
  :challenge (/ 1 4)

  :actions [{:name "Bite" :description "Melee Weapon Attack: +6 to hit, reach 10 ft., one target. Hit: 6 (1d4 + 4) piercing damage, and the target must make a DC 11 Constitution saving throw, taking 10 (3d6) poison damage on a failed save, or half as much damage on a successful one."}]
}{
  :name "Giant Scorpion"
  :size :large
  :type :beast
  :alignment "unaligned"
  :armor-class 15
  :armor-notes "natural armor"
  :hit-points {:mean 52 :die-count 7 :die 10 :modifier 14}
  :speed "40 ft."

  :str 15
  :dex 13
  :con 15
  :int 1
  :wis 9
  :cha 3

  :senses "blindsight 60 ft., passive Perception 9"
  :challenge 3

  :actions [{:name "Multiattack" :description "The scorpion makes three attacks: two with its claws and one with its sting."}
            {:name "Claw" :description "Melee Weapon Attack: +4 to hit, reach 5 ft., one target. Hit: 6 (1d8 + 2) bludgeoning damage, and the target is grappled (escape DC 12). The scorpion has two claws, each of which can grapple only one target."}
            {:name "Sting" :description "Melee Weapon Attack: +4 to hit, reach 5 ft., one creature. Hit: 7 (1d10 + 2) piercing damage, and the target must make a DC 12 Constitution saving throw, taking 22 (4d10) poison damage on a failed save, or half as much damage on a successful one."}]
}{
  :name "Giant Rat"
  :size :small
  :type :beast
  :alignment "unaligned"
  :armor-class 12
  :hit-points {:mean 7 :die-count 2 :die 6}
  :speed "30 ft."

  :str 7
  :dex 15
  :con 11
  :int 2
  :wis 10
  :cha 4

  :senses "darkvision 60 ft., passive Perception 10"
  :challenge (/ 1 8)

  :traits [{:name "Keen Smell" :description "The rat has advantage on Wisdom (Perception) checks that rely on smell."}
           {:name "Pack Tactics" :description "The rat has advantage on an attack roll against a creature if at least one of the rat's allies is within 5 feet of the creature and the ally isn't incapacitated."}]

  :actions [{:name "Bite" :description "Melee Weapon Attack: +4 to hit, reach 5 ft., one target. Hit: 4 (1d4 + 2) piercing damage."}]
}{
  :name "Giant Sea Horse"
  :size :large
  :type :beast
  :alignment "unaligned"
  :armor-class 13
  :armor-notes "natural armor"
  :hit-points {:mean 16 :die-count 3 :die 10}
  :speed "0 ft., swim 40 ft."

  :str 12
  :dex 15
  :con 11
  :int 2
  :wis 12
  :cha 5

  :senses "passive Perception 11"
  :challenge (/ 1 2)

  :traits [{:name "Charge" :description "If the sea horse moves at least 20 feet straight toward a target and then hits it with a ram attack on the same turn, the target takes an extra 7 (2d6) bludgeoning damage. It the target is a creature, it must succeed on a DC 11 Strength saving throw or be knocked prone."}
           {:name "Water Breathing" :description "The sea horse can breathe only underwater."}]

  :actions [{:name "Ram" :description "Melee Weapon Attack: +3 to hit, reach 5 ft., one target. Hit: 4 (1d6 + 1) bludgeoning damage."}]
  :description "Like their smaller kin, giant sea horses are shy, colorful fish with elongated bodies and curled tails. Aquatic elves train them as mounts."
}{
  :name "Giant Shark"
  :size :huge
  :type :beast
  :alignment "unaligned"
  :armor-class 13
  :armor-notes "natural armor"
  :hit-points {:mean 126 :die-count 11 :die 12 :modifier 55}
  :speed "0 ft., swim 50 ft."

  :str 23
  :dex 11
  :con 21
  :int 1
  :wis 10
  :cha 5

  :skills {:perception 3}
  :senses "blindsight 60 ft., passive Perception 13"
  :challenge 5

  :traits [{:name "Blood Frenzy" :description "The shark has advantage on melee attack rolls against any creature that doesn't have all its hit points."}
           {:name "Water Breathing" :description "The shark can breathe only underwater."}]

  :actions [{:name "Bite" :description "Melee Weapon Attack: +9 to hit, reach 5 ft., one target. Hit: 22 (3d10 + 6) piercing damage. A giant shark is 30 feet long and normally found in deep oceans. Utterly fearless, it preys on anything that crosses its path, including whales and  ships."}]
}{
  :name "Giant Spider"
  :size :large
  :type :beast
  :alignment "unaligned"
  :armor-class 14
  :armor-notes "natural armor"
  :hit-points {:mean 26 :die-count 4 :die 10 :modifier 4}
  :speed "30 ft., climb 30 ft."

  :str 14
  :dex 16
  :con 12
  :int 2
  :wis 11
  :cha 4

  :skills {:stealth 7}
  :senses "blindsight 10 ft., darkvision 60 ft., passive Perception 10"
  :challenge 1

  :traits [{:name "Spider Climb" :description "The spider can climb difficult surfaces, including upside down on ceilings, without needing to make an ability check."}
           {:name "Web Sense" :description "While in contact with a web, the spider knows the exact location of any other creature in contact with the same web."}
           {:name "Web Walker" :description "The spider ignores movement restrictions caused by webbing."}]

  :actions [{:name "Bite" :description "Melee Weapon Attack: +5 to hit, reach 5 ft., one creature. Hit: 7 (1d8 + 3) piercing damage, and the target must make a DC 11 Constitution saving throw, taking 9 (2d8) poison damage on a failed save, or half as much damage on a successful one. If the poison damage reduces the target to 0 hit points, the target is stable but poisoned for 1 hour, even after regaining hit points, and is paralyzed while poisoned in this way."}
            {:name "Web" :notes "Recharge 5–6" :description "Ranged Weapon Attack: +5 to hit, range 30/60 ft., one creature. Hit: The target is restrained by webbing. As an action, the restrained target can make a DC 12 Strength check, bursting the webbing on a success. The webbing can also be attacked and destroyed (AC 10; hp 5; vulnerability to fire damage; immunity to bludgeoning, poison, and psychic damage)."}]

  :description "To snare its prey, a giant spider spins elaborate webs or shoots sticky strands of webbing from its abdomen. Giant spiders are most commonly found underground, making their lairs on ceilings or in dark, web-filled crevices. Such lairs are often festooned with web cocoons holding past victims."
}{
  :name "Giant Toad"
  :size :large
  :type :beast
  :alignment "unaligned"
  :armor-class 11
  :hit-points {:mean 39 :die-count 6 :die 10 :modifier 6}
  :speed "20 ft., swim 40 ft."

  :str 15
  :dex 13
  :con 13
  :int 2
  :wis 10
  :cha 3
                                                                                                                                                                                                                                                                                                                        
  :senses "darkvision 30 ft., passive Perception 10"
  :challenge 1

  :traits [{:name "Amphibious" :description "The toad can breathe air and water."}
           {:name "Standing Leap" :description "The toad's long jump is up to 20 feet and its high jump is up to 10 feet, with or without a running start."}]

  :actions [{:name "Bite" :description "Melee Weapon Attack: +4 to hit, reach 5 ft., one target. Hit: 7 (1d10 + 2) piercing damage plus 5 (1d10) poison damage, and the target is grappled (escape DC 13). Until this grapple ends, the target is restrained, and the toad can't bite another target."}
            {:name "Swallow" :description "The toad makes one bite attack against a Medium or smaller target it is grappling. If the attack hits, the target is swallowed, and the grapple ends. The swallowed target is blinded and restrained, it has total cover against attacks and other effects outside the toad, and it takes 10 (3d6) acid damage at the start of each of the toad's turns. The toad can have only one target swallowed at a time.
If the toad dies, a swallowed creature is no longer restrained by it and can escape from the corpse using 5 feet of movement, exiting prone."}]
}{
  :name "Giant Vulture"
  :size :large
  :type :beast
  :alignment "neutral evil"
  :armor-class 10
  :hit-points {:mean 22 :die-count 3 :die 10 :modifier 6}
  :speed "10 ft., fly 60 ft."

  :str 15
  :dex 10
  :con 15
  :int 6
  :wis 12
  :cha 7

  :skills {:perception 3}
  :senses "passive Perception 13"
  :languages "understands Common but can't speak"
  :challenge 1

  :traits [{:name "Keen Sight and Smell" :description "The vulture has advantage on Wisdom (Perception) checks that rely on sight or smell."}
           {:name "Pack Tactics" :description "The vulture has advantage on an attack roll against a creature if at least one of the vulture's allies is within 5 feet of the creature and the ally isn't incapacitated."}]

  :actions [{:name "Multiattack" :description "The vulture makes two attacks: one with its beak and one with its talons."}
            {:name "Beak" :description "Melee Weapon Attack: +4 to hit, reach 5 ft., one target. Hit: 7 (2d4 + 2) piercing damage."}
            {:name "Talons" :description "Melee Weapon Attack: +4 to hit, reach 5 ft., one target. Hit: 9 (2d6 + 2) slashing damage."}]
  
  :description "A giant vulture has advanced intelligence and a malevolent bent. Unlike its smaller kin, it will attack a wounded creature to hasten its end. Giant vultures have been known to haunt a thirsty, starving  creature for days to enjoy its suffering."
}{
  :name "Giant Wasp"
  :size :medium
  :type :beast
  :alignment "unaligned"
  :armor-class 12
  :hit-points {:mean 13 :die-count 3 :die 8}
  :speed "10 ft., fly 50 ft."

  :str 10
  :dex 14
  :con 10
  :int 1
  :wis 10
  :cha 3

  :senses "passive Perception 10"
  :challenge (/ 1 2)

  :actions [{:name "Sting" :description "Melee Weapon Attack: +4 to hit, reach 5 ft., one creature. Hit: 5 (1d6 + 2) piercing damage, and the target must make a DC 11 Constitution saving throw, taking 10 (3d6) poison damage on a failed save, or half as much damage on a successful one. If the poison damage reduces the target to 0 hit points, the target is stable but poisoned for 1 hour, even after regaining hit points, and is paralyzed while poisoned in this way."}]
}{
  :name "Giant Weasel"
  :size :medium
  :type :beast
  :alignment "unaligned"
  :armor-class 13
  :hit-points {:mean 9 :die-count 2 :die 8}
  :speed "40 ft."

  :str 11
  :dex 16
  :con 10
  :int 4
  :wis 12
  :cha 5

  :skills {:perception 3, :stealth 5}
  :senses "darkvision 60 ft., passive Perception 13"
  :challenge 1

  :traits [{:name "Keen Hearing and Smell." :description "The weasel has advantage on Wisdom (Perception) checks that rely on hearing or smell."}]

  :actions [{:name "Bite" :description "Melee Weapon Attack: +5 to hit, reach 5 ft., one target. Hit: 5 (1d4 + 3) piercing damage."}]
}{
  :name "Giant Wolf Spider"
  :size :medium
  :type :beast
  :alignment "unaligned"
  :armor-class 13
  :hit-points {:mean 11 :die-count 2 :die 8 :modifier 2}
  :speed "40 ft., climb 40 ft."

  :str 12
  :dex 16
  :con 13
  :int 3
  :wis 12
  :cha 4

  :skills {:perception 3, :stealth 7}
  :senses "blindsight 10 ft., darkvision 60 ft., passive Perception 13"
  :challenge (/ 1 4)

  :traits [{:name "Spider Climb" :description "The spider can climb difficult surfaces, including upside down on ceilings, without needing to make an ability check."}
           {:name "Web Sense" :description "While in contact with a web, the spider knows the exact location of any other creature in contact with the same web."}
           {:name "Web Walker" :description "The spider ignores movement restrictions caused by webbing."}]

  :actions [{:name "Bite" :description "Melee Weapon Attack: +3 to hit, reach 5 ft., one creature. Hit: 4 (1d6 + 1) piercing damage, and the target must make a DC 11 Constitution saving throw, taking 7 (2d6) poison damage on a failed save, or half as much damage on a successful one. If the poison damage reduces the target to 0 hit points, the target is stable but poisoned for 1 hour, even after regaining hit points, and is paralyzed while poisoned in this way."}]
  :description "Smaller than a giant spider, a giant wolf spider hunts prey across open ground or hides in a burrow or crevice, or in a hidden cavity beneath debris."
}{
  :name "Goat"
  :size :medium
  :type :beast
  :alignment "unaligned"
  :armor-class 10
  :hit-points {:mean 4 :die-count 1 :die 8}
  :speed "40 ft."

  :str 12
  :dex 10
  :con 11
  :int 2
  :wis 10
  :cha 5
                                                                                                                                                                        
  :senses "passive Perception 10"
  :challenge 0

  :traits [{:name "Charge" :description "If the goat moves at least 20 feet straight toward a target and then hits it with a ram attack on the same turn, the target takes an extra 2 (1d4) bludgeoning damage. If the target is a creature, it must succeed on a DC 10 Strength saving throw or be knocked prone."}
           {:name "Sure-Footed" :description "The goat has advantage on Strength and Dexterity saving throws made against effects that would knock it prone."}]

  :actions [{:name "Ram" :description "Melee Weapon Attack: +3 to hit, reach 5 ft., one target. Hit: 3 (1d4 + 1) bludgeoning damage."}]
}{
  :name "Hawk"
  :size :tiny
  :type :beast
  :alignment "unaligned"
  :armor-class 13
  :hit-points {:mean 1 :die-count 1 :die 4 :modifier -1}
  :speed "10 ft., fly 60 ft."

  :str 5
  :dex 16
  :con 8
  :int 2
  :wis 14
  :cha 6

  :skills {:perception 4}
  :senses "passive Perception 14"
  :challenge 0

  :traits [{:name "Keen Sight" :description "The hawk has advantage on Wisdom (Perception) checks that rely on sight."}]

  :actions [{:name "Talons" :description "Melee Weapon Attack: +5 to hit, reach 5 ft., one target. Hit: 1 slashing damage."}]
}{
  :name "Hunter Shark"
  :size :large
  :type :beast
  :alignment "unaligned"
  :armor-class 12
  :armor-notes "natural armor"
  :hit-points {:mean 45 :die-count 6 :die 10 :modifier 12}
  :speed "0 ft., swim 40 ft."

  :str 18
  :dex 13
  :con 15
  :int 1
  :wis 10
  :cha 4

  :skills {:perception 2}
  :senses "blindsight 30 ft., passive Perception 12"
  :challenge 2

  :traits [{:name "Blood Frenzy" :description "The shark has advantage on melee attack rolls against any creature that doesn't have all its hit points."}
           {:name "Water Breathing" :description "The shark can breathe only underwater."}]

  :actions [{:name "Bite" :description "Melee Weapon Attack: +6 to hit, reach 5 ft., one target. Hit: 13 (2d8 + 4) piercing damage."}]

  :description "Smaller than a giant shark but larger and fiercer than a reef shark, a hunter shark haunts deep waters. It usually hunts alone, but multiple hunter sharks might feed in the same area. A fully grown hunter shark is 15 to 20 feet long."
}{
  :name "Hyena"
  :size :medium
  :type :beast
  :alignment "unaligned"
  :armor-class 11
  :hit-points {:mean 5 :die-count 1 :die 8 :modifier 1}
  :speed "50 ft."

  :str 11
  :dex 13
  :con 12
  :int 2
  :wis 12
  :cha 5

  :skills {:perception 3}
  :senses "passive Perception 13"
  :challenge 0

  :traits [{:name "Pack Tactics" :description "The hyena has advantage on an attack roll against a creature if at least one of the hyena's allies is within 5 feet of the creature and the ally isn't incapacitated."}]

  :actions [{:name "Bite" :description "Melee Weapon Attack: +2 to hit, reach 5 ft., one target. Hit: 3 (1d6) piercing damage."}]
}{
  :name "Jackal"
  :size :small
  :type :beast
  :alignment "unaligned"
  :armor-class 12
  :hit-points {:mean 3 :die-count 1 :die 6}
  :speed "40 ft."

  :str 8
  :dex 15
  :con 11
  :int 3
  :wis 12
  :cha 6

  :skills {:perception 3}
  :senses "passive Perception 13"
  :challenge 0

  :traits [{:name "Keen Hearing and Smell." :description "The jackal has advantage on Wisdom (Perception) checks that rely on hearing or smell."}
           {:name "Pack Tactics" :description "The jackal has advantage on an attack roll against a creature if at least one of the jackal's allies is within 5 feet of the creature and the ally isn't incapacitated."}]

  :actions [{:name "Bite" :description "Melee Weapon Attack: +1 to hit, reach 5 ft., one target. Hit: 1 (1d4 – 1) piercing damage."}]
}{
  :name "Killer Whale"
  :size :huge
  :type :beast
  :alignment "unaligned"
  :armor-class 12
  :armor-notes "natural armor"
  :hit-points {:mean 90 :die-count 12 :die 12 :modifier 12}
  :speed "0 ft., swim 60 ft."

  :str 19
  :dex 10
  :con 13
  :int 3
  :wis 12
  :cha 7

  :skills {:perception 3}
  :senses "blindsight 120 ft., passive Perception 13"
  :challenge 3

  :traits [{:name "Echolocation" :description "The whale can't use its blindsight while deafened."}
           {:name "Hold Breath" :description "The whale can hold its breath for 30 minutes."}
           {:name "Keen Hearing" :description "The whale has advantage on Wisdom (Perception) checks that rely on hearing."}]

  :actions [{:name "Bite" :description "Melee Weapon Attack: +6 to hit, reach 5 ft., one target. Hit: 21 (5d6 + 4) piercing damage."}]
}{
  :name "Lion"
  :size :large
  :type :beast
  :alignment "unaligned"
  :armor-class 12
  :hit-points {:mean 26 :die-count 4 :die 10 :modifier 4}
  :speed "50 ft."

  :str 17
  :dex 15
  :con 13
  :int 3
  :wis 12
  :cha 8

  :skills {:perception 3, :stealth 6}
  :senses "passive Perception 13"
  :challenge 1

  :traits [{:name "Keen Smell" :description "The lion has advantage on Wisdom (Perception) checks that rely on smell."}
           {:name "Pack Tactics" :description "The lion has advantage on an attack roll against a creature if at least one of the lion's allies is within 5 feet of the creature and the ally isn't incapacitated."}
           {:name "Pounce" :description "If the lion moves at least 20 feet straight toward a creature and then hits it with a claw attack on the same turn, that target must succeed on a DC 13 Strength saving throw or be knocked prone. If the target is prone, the lion can make one bite attack against it as a bonus action."}
           {:name "Running Leap" :description "With a 10-foot running start, the lion can long jump up to 25 feet."}]

  :actions [{:name "Bite" :description "Melee Weapon Attack: +5 to hit, reach 5 ft., one target. Hit: 7 (1d8 + 3) piercing damage."}
            {:name "Claw" :description "Melee Weapon Attack: +5 to hit, reach 5 ft., one target. Hit: 6 (1d6 + 3) slashing damage."}]
}{
  :name "Lizard"
  :size :tiny
  :type :beast
  :alignment "unaligned"
  :armor-class 10
  :hit-points {:mean 2 :die-count 1 :die 4}
  :speed "20 ft., climb 20 ft."

  :str 2
  :dex 11
  :con 10
  :int 1
  :wis 8
  :cha 3

  :senses "darkvision 30 ft., passive Perception 9"
  :challenge 0

  :actions [{:name "Bite" :description "Melee Weapon Attack: +0 to hit, reach 5 ft., one target. Hit: 1 piercing damage."}]
}{
  :name "Mammoth"
  :size :huge
  :type :beast
  :alignment "unaligned"
  :armor-class 13
  :armor-notes "natural armor"
  :hit-points {:mean 126 :die-count 11 :die 12 :modifier 55}
  :speed "40 ft."

  :str 24
  :dex 9
  :con 21
  :int 3
  :wis 11
  :cha 6

  :senses "passive Perception 10"
  :challenge 6

  :traits [{:name "Trampling Charge" :description "If the mammoth moves at least 20 feet straight toward a creature and then hits it with a gore attack on the same turn, that target must succeed on a DC 18 Strength saving throw or be knocked prone. If the target is prone, the mammoth can make one stomp attack against it as a bonus action."}]

  :actions [{:name "Gore" :description "Melee Weapon Attack: +10 to hit, reach 10 ft., one target. Hit: 25 (4d8 + 7) piercing damage."}
            {:name "Stomp" :description "Melee Weapon Attack: +10 to hit, reach 5 ft., one prone creature. Hit: 29 (4d10 + 7) bludgeoning damage."}]
  :description "A mammoth is an elephantine creature with thick   fur and long tusks. Stockier and fiercer than normal elephants, mammoths inhabit a wide range of climes, from subarctic to subtropical."
}{
  :name "Mastiff"
  :size :medium
  :type :beast
  :alignment "unaligned"
  :armor-class 12
  :hit-points {:mean 5 :die-count 1 :die 8 :modifier 1}
  :speed "40 ft."

  :str 13
  :dex 14
  :con 12
  :int 3
  :wis 12
  :cha 7

  :skills {:perception 3}
  :senses "passive Perception 13"
  :challenge (/ 1 8)

  :traits [{:name "Keen Hearing and Smell." :description "The mastiff has advantage on Wisdom (Perception) checks that rely on hearing or smell."}]

  :actions [{:name "Bite" :description "Melee Weapon Attack: +3 to hit, reach 5 ft., one target. Hit: 4 (1d6 + 1) piercing damage. If the target is a creature, it must succeed on a DC 11 Strength saving throw or be knocked prone."}]

  :description "Mastiffs are impressive hounds prized by humanoids for their loyalty and keen senses. Mastiffs can be trained as guard dogs, hunting dogs, and war dogs. Halflings and other Small humanoids ride them as mounts."
}{
  :name "Mule"
  :size :medium
  :type :beast
  :alignment "unaligned"
  :armor-class 10
  :hit-points {:mean 11 :die-count 2 :die 8 :modifier 2}
  :speed "40 ft."

  :str 14
  :dex 10
  :con 13
  :int 2
  :wis 10
  :cha 5

  :senses "passive Perception 10"
  :challenge (/ 1 8)

  :traits [{:name "Beast of Burden" :description "The mule is considered to be a Large animal for the purpose of determining its carrying capacity. Sure-Footed. The mule has advantage on Strength and Dexterity saving throws made against effects that would knock it prone."}]

  :actions [{:name "Hooves" :description "Melee Weapon Attack: +2 to hit, reach 5 ft., one target. Hit: 4 (1d4 + 2) bludgeoning damage."}]
}{
  :name "Octopus"
  :size :small
  :type :beast
  :alignment "unaligned"
  :armor-class 12
  :hit-points {:mean 3 :die-count 1 :die 6}
  :speed "5 ft., swim 30 ft."

  :str 4
  :dex 15
  :con 11
  :int 3
  :wis 10
  :cha 4

  :skills {:perception 2, :stealth 4}
  :senses "darkvision 30 ft., passive Perception 12"
  :challenge 0

  :traits [{:name "Hold Breath" :description "While out of water, the octopus can hold its breath for 30 minutes."}
           {:name "Underwater Camouflage" :description "The octopus has advantage on Dexterity (Stealth) checks made while underwater."}
           {:name "Water Breathing" :description "The octopus can breathe only underwater"}]

  :actions [{:name "Tentacles" :description "Melee Weapon Attack: +4 to hit, reach 5 ft., one target. Hit: 1 bludgeoning damage, and the target is grappled (escape DC 10). Until this grapple ends, the octopus can't use its tentacles on another target."}
            {:name "Ink Cloud" :notes "Recharges after a Short or Long Rest" :description "A 5- foot-radius cloud of ink extends all around the octopus if it is underwater. The area is heavily obscured for 1 minute, although a significant current can disperse the ink. After releasing the ink, the octopus can use the Dash action as a bonus action."}]
}{
  :name "Panther"
  :size :medium
  :type :beast
  :alignment "unaligned"
  :armor-class 12
  :hit-points {:mean 13 :die-count 3 :die 8}
  :speed "50 ft., climb 40 ft."

  :str 14
  :dex 15
  :con 10
  :int 3
  :wis 14
  :cha 7

  :skills {:perception 4, :stealth 6}
  :senses "passive Perception 14"
  :challenge (/ 1 4)

  :traits [{:name "Keen Smell" :description "The panther has advantage on Wisdom (Perception) checks that rely on smell."}
           {:name "Pounce" :description "If the panther moves at least 20 feet straight toward a creature and then hits it with a claw attack on the same turn, that target must succeed on a DC 12 Strength saving throw or be knocked prone. If the target is prone, the panther can make one bite attack against it as a bonus action."}]

  :actions [{:name "Bite" :description "Melee Weapon Attack: +4 to hit, reach 5 ft., one target. Hit: 5 (1d6 + 2) piercing damage."}
            {:name "Claw" :description "Melee Weapon Attack: +4 to hit, reach 5 ft., one target. Hit: 4 (1d4 + 2) slashing damage."}]
}{
  :name "Owl"
  :size :tiny
  :type :beast
  :alignment "unaligned"
  :armor-class 11
  :hit-points {:mean 1 :die-count 1 :die 4 :modifier -1}
  :speed "5 ft., fly 60 ft."

  :str 3
  :dex 13
  :con 8
  :int 2
  :wis 12
  :cha 7

  :skills {:perception 3, :stealth 3}
  :senses "darkvision 120 ft., passive Perception 13"
  :challenge 0

  :traits [{:name "Flyby" :description "The owl doesn't provoke opportunity attacks when it flies out of an enemy's reach."}
           {:name "Keen Hearing and Sight" :description "The owl has advantage on Wisdom (Perception) checks that rely on hearing or sight."}]

  :actions [{:name "Talons" :description "Melee Weapon Attack: +3 to hit, reach 5 ft., one target. Hit: 1 slashing damage."}]
}{
  :name "Phase Spider"
  :size :large
  :type :monstrosity
  :alignment "unaligned"
  :armor-class 13
  :armor-notes "natural armor"
  :hit-points {:mean 32 :die-count 5 :die 10 :modifier 5}
  :speed "30 ft., climb 30 ft."

  :str 15
  :dex 15
  :con 12
  :int 6
  :wis 10
  :cha 6

  :skills {:stealth 6}
  :senses "darkvision 60 ft., passive Perception 10"
  :challenge 3

  :traits [{:name "Ethereal Jaunt" :description "As a bonus action, the spider can magically shift from the Material Plane to the Ethereal Plane, or vice versa."}
           {:name "Spider Climb" :description "The spider can climb difficult surfaces, including upside down on ceilings, without needing to make an ability check."}
           {:name "Web Walker" :description "The spider ignores movement restrictions caused by webbing."}]

  :actions [{:name "Bite" :description "Melee Weapon Attack: +4 to hit, reach 5 ft., one creature. Hit: 7 (1d10 + 2) piercing damage, and the target must make a DC 11 Constitution saving throw, taking 18 (4d8) poison damage on a failed save, or half as much damage on a successful one. If the poison damage reduces the target to 0 hit points, the target is stable but poisoned for 1 hour, even after regaining hit points, and is paralyzed while poisoned in this way."}]

  :description "A phase spider possesses the magical ability to phase in and out of the Ethereal Plane. It seems to appear out of nowhere and quickly vanishes after attacking. Its movement on the Ethereal Plane before coming back to the Material Plane makes it seem like it can teleport."
}{
  :name "Poisonous Snake"
  :size :tiny
  :type :beast
  :alignment "unaligned"
  :armor-class 13
  :hit-points {:mean 2 :die-count 1 :die 4}
  :speed "30 ft., swim 30 ft."

  :str 2
  :dex 16
  :con 11
  :int 1
  :wis 10
  :cha 3

  :senses "blindsight 10 ft., passive Perception 10"
  :challenge (/ 1 8)

  :actions [{:name "Bite" :description "Melee Weapon Attack: +5 to hit, reach 5 ft., one target. Hit: 1 piercing damage, and the target must make a DC 10 Constitution saving throw, taking 5 (2d4) poison damage on a failed save, or half as much damage on a successful one."}]
}{
  :name "Polar Bear"
  :size :large
  :type :beast
  :alignment "unaligned"
  :armor-class 12
  :armor-notes "natural armor"
  :hit-points {:mean 42 :die-count 5 :die 10 :modifier 15}
  :speed "40 ft., swim 30 ft."

  :str 20
  :dex 10
  :con 16
  :int 2
  :wis 13
  :cha 7

  :skills {:perception 3}
  :senses "passive Perception 13"
  :challenge 2

  :traits [{:name "Keen Smell" :description "The bear has advantage on Wisdom (Perception) checks that rely on smell."}]

  :actions [{:name "Multiattack" :description "The bear makes two attacks: one with its bite and one with its claws."}
            {:name "Bite" :description "Melee Weapon Attack: +7 to hit, reach 5 ft., one target. Hit: 9 (1d8 + 5) piercing damage."}
            {:name "Claws" :description "Melee Weapon Attack: +7 to hit, reach 5 ft., one target. Hit: 12 (2d6 + 5) slashing damage."}]
}{
  :name "Pony"
  :size :medium
  :type :beast
  :alignment "unaligned"
  :armor-class 10
  :hit-points {:mean 11 :die-count 2 :die 8 :modifier 2}
  :speed "40 ft."

  :str 15
  :dex 10
  :con 13
  :int 2
  :wis 11
  :cha 7
  :senses "passive Perception 10"
  :challenge (/ 1 8)

  :actions [{:name "Hooves" :description "Melee Weapon Attack: +4 to hit, reach 5 ft., one target. Hit: 7 (2d4 + 2) bludgeoning damage."}]
}{
  :name "Quipper"
  :size :tiny
  :type :beast
  :alignment "unaligned"
  :armor-class 13
  :hit-points {:mean 1 :die-count 1 :die 4 :modifier -1}
  :speed "0 ft., swim 40 ft."

  :str 2
  :dex 16
  :con 9
  :int 1
  :wis 7
  :cha 2

  :senses "darkvision 60 ft., passive Perception 8"
  :challenge 0

  :traits [{:name "Blood Frenzy" :description "The quipper has advantage on melee attack rolls against any creature that doesn't have all its hit points."}
           {:name "Water Breathing" :description "The quipper can breathe only underwater."}]

  :actions [{:name "Bite" :description "Melee Weapon Attack: +5 to hit, reach 5 ft., one target. Hit: 1 piercing damage."}]

  :description "A quipper is a carnivorous fish with sharp teeth. Quippers can adapt to any aquatic environment, including cold subterranean lakes. They frequently gather in swarms; the statistics for a swarm of quippers appear later in this  appendix."
}{
  :name "Rat"
  :size :tiny
  :type :beast
  :alignment "unaligned"
  :armor-class 10
  :hit-points {:mean 1 :die-count 1 :die 4 :modifier -1}
  :speed "20 ft."

  :str 2
  :dex 11
  :con 9
  :int 2
  :wis 10
  :cha 4

  :senses "darkvision 30 ft., passive Perception 10"
  :challenge 0

  :traits [{:name "Keen Smell" :description "The rat has advantage on Wisdom (Perception) checks that rely on smell."}]

  :actions [{:name "Bite" :description "Melee Weapon Attack: +0 to hit, reach 5 ft., one target. Hit: 1 piercing damage."}]
}{
  :name "Raven"
  :size :tiny
  :type :beast
  :alignment "unaligned"
  :armor-class 12
  :hit-points {:mean 1 :die-count 1 :die 4 :modifier -1}
  :speed "10 ft., fly 50 ft."

  :str 2
  :dex 14
  :con 8
  :int 2
  :wis 12
  :cha 6

  :skills {:perception 3}
  :senses "passive Perception 13"
  :challenge 0

  :traits [{:name "Mimicry" :description "The raven can mimic simple sounds it has heard, such as a person whispering, a baby crying, or an animal chittering. A creature that hears the sounds can tell they are imitations with a successful DC 10 Wisdom (Insight) check."}]

  :actions [{:name "Beak" :description "Melee Weapon Attack: +4 to hit, reach 5 ft., one target. Hit: 1 piercing damage."}]
}{
  :name "Reef Shark"
  :size :medium
  :type :beast
  :alignment "unaligned"
  :armor-class 12
  :armor-notes "natural armor"
  :hit-points {:mean 22 :die-count 4 :die 8 :modifier 4}
  :speed "0 ft., swim 40 ft."

  :str 14
  :dex 13
  :con 13
  :int 1
  :wis 10
  :cha 4

  :skills {:perception 2}
  :senses "blindsight 30 ft., passive Perception 12"
  :challenge 1

  :traits [{:name "Pack Tactics" :description "The shark has advantage on an attack roll against a creature if at least one of the shark's allies is within 5 feet of the creature and the ally isn't incapacitated."}
           {:name "Water Breathing" :description "The shark can breathe only underwater."}]

  :actions [{:name "Bite" :description "Melee Weapon Attack: +4 to hit, reach 5 ft., one target. Hit: 6 (1d8 + 2) piercing damage."}]

  :description "Smaller than giant sharks and hunter sharks, reef sharks inhabit shallow waters and coral reefs, gathering in small packs to hunt. A full-grown specimen measures 6 to 10 feet long."
}{
  :name "Saber-Toothed Tiger"
  :size :large
  :type :beast
  :alignment "unaligned"
  :armor-class 12
  :hit-points {:mean 52 :die-count 7 :die 10 :modifier 14}
  :speed "40 ft."

  :str 18
  :dex 14
  :con 15
  :int 3
  :wis 12
  :cha 8

  :skills {:perception 3 :stealth 6}
  :senses "passive Perception 13"
  :challenge 2

  :traits [{:name "Keen Smell" :description "The tiger has advantage on Wisdom (Perception) checks that rely on smell."}
           {:name "Pounce" :description "If the tiger moves at least 20 feet straight toward a creature and then hits it with a claw attack on the same turn, that target must succeed on a DC 14 Strength saving throw or be knocked prone. If the target is prone, the tiger can make one bite attack against it as a bonus action."}]

  :actions [{:name "Bite" :description "Melee Weapon Attack: +6 to hit, reach 5 ft., one target. Hit: 10 (1d10 + 5) piercing damage. "}
            {:name "Claw" :description "Melee Weapon Attack: +6 to hit, reach 5 ft., one target. Hit: 12 (2d6 + 5) slashing damage."}]
}{
  :name "Rhinoceros"
  :size :large
  :type :beast
  :alignment "unaligned"
  :armor-class 11
  :armor-notes "natural armor"
  :hit-points {:mean 45 :die-count 6 :die 10 :modifier 12}
  :speed "40 ft."

  :str 21
  :dex 8
  :con 15
  :int 2
  :wis 12
  :cha 6

  :senses "passive Perception 11"
  :challenge 2

  :traits [{:name "Charge" :description "If the rhinoceros moves at least 20 feet straight toward a target and then hits it with a gore attack on the same turn, the target takes an extra 9 (2d8) bludgeoning damage. If the target is a creature, it must succeed on a DC 15 Strength saving throw or be knocked prone."}]

  :actions [{:name "Gore" :description "Melee Weapon Attack: +7 to hit, reach 5 ft., one target. Hit: 14 (2d8 + 5) bludgeoning damage."}]
}{
  :name "Riding Horse"
  :size :large
  :type :beast
  :alignment "unaligned"
  :armor-class 10
  :hit-points {:mean 13 :die-count 2 :die 10 :modifier 2}
  :speed "60 ft."

  :str 16
  :dex 10
  :con 12
  :int 2
  :wis 11
  :cha 7

  :senses "passive Perception 10"
  :challenge (/ 1 4)

  :actions [{:name "Hooves" :description "Melee Weapon Attack: +5 to hit, reach 5 ft., one target. Hit: 8 (2d4 + 3) bludgeoning damage."}]
}{
  :name "Scorpion"
  :size :tiny
  :type :beast
  :alignment "unaligned"
  :armor-class 11
  :armor-notes "natural armor"
  :hit-points {:mean 1 :die-count 1 :die 4 :modifier -1}
  :speed "10 ft."

  :str 2
  :dex 11
  :con 8
  :int 1
  :wis 8
  :cha 2

  :senses "blindsight 10 ft., passive Perception 9"
  :challenge 0

  :actions [{:name "Sting" :description "Melee Weapon Attack: +2 to hit, reach 5 ft., one creature. Hit: 1 piercing damage, and the target must make a DC 9 Constitution saving throw, taking 4 (1d8) poison damage on a failed save, or half as much damage on a successful one."}]
}{
  :name "Sea Horse"
  :size :tiny
  :type :beast
  :alignment "unaligned"
  :armor-class 11
  :hit-points {:mean 1 :die-count 1 :die 4 :modifier -1}
  :speed "0 ft., swim 20 ft."

  :str 1
  :dex 12
  :con 8
  :int 1
  :wis 10
  :cha 2

  :senses "passive Perception 10"
  :challenge 0

  :traits [{:name "Water Breathing" :description "The sea horse can breathe only underwater."}]
}{
  :name "Spider"
  :size :tiny
  :type :beast
  :alignment "unaligned"
  :armor-class 12
  :hit-points {:mean 1 :die-count 1 :die 4 :modifier -1}
  :speed "20 ft., climb 20 ft."

  :str 2
  :dex 14
  :con 8
  :int 1
  :wis 10
  :cha 2

  :skills {:stealth 4}
  :senses "darkvision 30 ft., passive Perception 10"
  :challenge 0

  :traits [{:name "Spider Climb" :description "The spider can climb difficult surfaces, including upside down on ceilings, without needing to make an ability check."}
           {:name "Web Sense" :description "While in contact with a web, the spider knows the exact location of any other creature in contact with the same web."}
           {:name "Web Walker" :description "The spider ignores movement restrictions caused by webbing."}]

  :actions [{:name "Bite" :description "Melee Weapon Attack: +4 to hit, reach 5 ft., one creature. Hit: 1 piercing damage, and the target must succeed on a DC 9 Constitution saving throw or take 2 (1d4) poison damage."}]
}{
  :name "Swarm of Bats"
  :size :medium
  :type :swarm-of-tiny-beasts
  :alignment "unaligned"
  :armor-class 12
  :hit-points {:mean 22 :die-count 5 :die 8}
  :speed "0 ft., fly 30 ft."

  :str 5
  :dex 15
  :con 10
  :int 2
  :wis 12
  :cha 4

  :damage-resistances "bludgeoning, piercing, slashing"
  :condition-immunities "charmed, frightened, grappled, paralyzed, petrified, prone, restrained, stunned"
  :senses "blindsight 60 ft., passive Perception 11"
  :challenge (/ 1 4)

  :traits [{:name "Echolocation" :description "The swarm can't use its blindsight while deafened."}
           {:name "Keen Hearing" :description "The swarm has advantage on Wisdom (Perception) checks that rely on hearing."}
           {:name "Swarm" :description "The swarm can occupy another creature's space and vice versa, and the swarm can move through any opening large enough for a Tiny bat. The swarm can't regain hit points or gain temporary hit points."}]

  :actions [{:name "Bites" :description "Melee Weapon Attack: +4 to hit, reach 0 ft., one creature in the swarm's space. Hit: 5 (2d4) piercing damage, or 2 (1d4) piercing damage if the swarm has half of its hit points or fewer."}]
}{
  :name "Swarm of Insects"
  :size :medium
  :type :swarm-of-tiny-beasts
  :alignment "unaligned"
  :armor-class 12
  :armor-notes "natural armor"
  :hit-points {:mean 22 :die-count 5 :die 8}
  :speed "20 ft., climb 20 ft."

  :str 3
  :dex 13
  :con 10
  :int 1
  :wis 7
  :cha 1

  :damage-resistances "bludgeoning, piercing, slashing"
  :condition-immunities "charmed, frightened, grappled, paralyzed, petrified, prone, restrained, stunned"
  :senses "blindsight 10 ft., passive Perception 8"
  :challenge (/ 1 2)

  :traits [{:name "Swarm" :description "The swarm can occupy another creature's space and vice versa, and the swarm can move through any opening large enough for a Tiny insect. The swarm can't regain hit points or gain temporary hit points."}]

  :actions [{:name "Bites" :description "Melee Weapon Attack: +3 to hit, reach 0 ft., one target in the swarm's space. Hit: 10 (4d4) piercing damage, or 5 (2d4) piercing damage if the swarm has half of its hit points or fewer."}]
}{
  :name "Swarm of Poisonous Snakes"
  :size :medium
  :type :swarm-of-tiny-beasts
  :alignment "unaligned"
  :armor-class 14
  :hit-points {:mean 36 :die-count 8 :die 8}
  :speed "30 ft., swim 30 ft."

  :str 8
  :dex 18
  :con 11
  :int 1
  :wis 10
  :cha 3

  :damage-resistances "bludgeoning, piercing, slashing"
  :condition-immunities "charmed, frightened, grappled, paralyzed, petrified, prone, restrained, stunned"
  :senses "blindsight 10 ft., passive Perception 10"
  :challenge 2

  :traits [{:name "Swarm" :description "The swarm can occupy another creature's space and vice versa, and the swarm can move through any opening large enough for a Tiny snake. The swarm can't regain hit points or gain temporary hit points."}]

  :actions [{:name "Bites" :description "Melee Weapon Attack: +6 to hit, reach 0 ft., one creature in the swarm's space. Hit: 7 (2d6) piercing damage, or 3 (1d6) piercing damage if the swarm has half of its hit points or fewer. The target must make a DC 10 Constitution saving throw, taking 14 (4d6) poison damage on a failed save, or half as much damage on a successful one."}]
}{
  :name "Swarm of Quippers"
  :size :medium
  :type :swarm-of-tiny-beasts
  :alignment "unaligned"
  :armor-class 13
  :hit-points {:mean 28 :die-count 8 :die 8 :modifier -8}
  :speed "0 ft., swim 40 ft."

  :str 13
  :dex 16
  :con 9
  :int 1
  :wis 7
  :cha 2

  :damage-resistances "bludgeoning, piercing, slashing"
  :condition-immunities "charmed, frightened, grappled, paralyzed, petrified, prone, restrained, stunned"
  :senses "darkvision 60 ft., passive Perception 8"
  :challenge 1

  :traits [{:name "Blood Frenzy" :description "The swarm has advantage on melee attack rolls against any creature that doesn't have all its hit points."}
           {:name "Swarm" :description "The swarm can occupy another creature's space and vice versa, and the swarm can move through any opening large enough for a Tiny quipper. The swarm can't regain hit points or gain temporary hit points."}
           {:name "Water Breathing" :description "The swarm can breathe only underwater."}]

  :actions [{:name "Bites" :description "Melee Weapon Attack: +5 to hit, reach 0 ft., one creature in the swarm's space. Hit: 14 (4d6) piercing damage, or 7 (2d6) piercing damage if the swarm has half of its hit points or fewer."}]
}{
  :name "Swarm of Rats"
  :size :medium
  :type :swarm-of-tiny-beasts
  :alignment "unaligned"
  :armor-class 10
  :hit-points {:mean 24 :die-count 7 :die 8 :modifier -7}
  :speed "30 ft."

  :str 9
  :dex 11
  :con 9
  :int 2
  :wis 10
  :cha 3

  :damage-resistances "bludgeoning, piercing, slashing"
  :condition-immunities "charmed, frightened, grappled, paralyzed, petrified, prone, restrained, stunned"
  :senses "darkvision 30 ft., passive Perception 10"
  :challenge (/ 1 4)

  :traits [{:name "Keen Smell" :description "The swarm has advantage on Wisdom (Perception) checks that rely on smell."}
           {:name "Swarm" :description "The swarm can occupy another creature's space and vice versa, and the swarm can move through any opening large enough for a Tiny rat. The swarm can't regain hit points or gain temporary hit points."}]

  :actions [{:name "Bites" :description "Melee Weapon Attack: +2 to hit, reach 0 ft., one target in the swarm's space. Hit: 7 (2d6) piercing damage, or 3 (1d6) piercing damage if the swarm has half of its hit points or fewer."}]
}{
  :name "Swarm of Ravens"
  :size :medium
  :type :swarm-of-tiny-beasts
  :alignment "unaligned"
  :armor-class 12
  :hit-points {:mean 24 :die-count 7 :die 8 :modifier -7}
  :speed "10 ft., fly 50 ft."

  :str 6
  :dex 14
  :con 8
  :int 3
  :wis 12
  :cha 6

  :skills {:perception 5}
  :damage-resistances "bludgeoning, piercing, slashing"
  :condition-immunities "charmed, frightened, grappled, paralyzed, petrified, prone, restrained, stunned"
  :senses "passive Perception 15"
  :challenge (/ 1 4)

  :traits [{:name "Swarm" :description "The swarm can occupy another creature's space and vice versa, and the swarm can move through any opening large enough for a Tiny raven. The swarm can't regain hit points or gain temporary hit points."}]

  :actions [{:name "Beaks" :description "Melee Weapon Attack: +4 to hit, reach 5 ft., one target in the swarm's space. Hit: 7 (2d6) piercing damage, or 3 (1d6) piercing damage if the swarm has half of its hit points or fewer."}]
}{
  :name "Tiger"
  :size :large
  :type :beast
  :alignment "unaligned"
  :armor-class 12
  :hit-points {:mean 37 :die-count 5 :die 10 :modifier 10}
  :speed "40 ft."

  :str 17
  :dex 15
  :con 14
  :int 3
  :wis 12
  :cha 8

  :skills {:perception 3, :stealth 6}
  :senses "darkvision 60 ft., passive Perception 13"
  :challenge 1

  :traits [{:name "Keen Smell" :description "The tiger has advantage on Wisdom (Perception) checks that rely on smell."}
           {:name "Pounce" :description "If the tiger moves at least 20 feet straight toward a creature and then hits it with a claw attack on the same turn, that target must succeed on a DC 13 Strength saving throw or be knocked prone. If the target is prone, the tiger can make one bite attack against it as a bonus action."}]

  :actions [{:name "Bite" :description "Melee Weapon Attack: +5 to hit, reach 5 ft., one target. Hit: 8 (1d10 + 3) piercing damage."}
            {:name "Claw" :description "Melee Weapon Attack: +5 to hit, reach 5 ft., one target. Hit: 7 (1d8 + 3) slashing damage."}]
}{
  :name "Vulture"
  :size :medium
  :type :beast
  :alignment "unaligned"
  :armor-class 10
  :hit-points {:mean 5 :die-count 1 :die 8 :modifier 1}
  :speed "10 ft., fly 50 ft."

  :str 7
  :dex 10
  :con 13
  :int 2
  :wis 12
  :cha 4

  :skills {:perception 3}
  :senses "passive Perception 13"
  :challenge 0

  :traits [{:name "Keen Sight and Smell" :description "The vulture has advantage on Wisdom (Perception) checks that rely on sight or smell."}
           {:name "Pack Tactics" :description "The vulture has advantage on an attack roll against a creature if at least one of the vulture's allies is within 5 feet of the creature and the ally isn't incapacitated."}]

  :actions [{:name "Beak" :description "Melee Weapon Attack: +2 to hit, reach 5 ft., one target. Hit: 2 (1d4) piercing damage."}]
}{
  :name "Warhorse"
  :size :large
  :type :beast
  :alignment "unaligned"
  :armor-class 11
  :hit-points {:mean 19 :die-count 3 :die 10 :modifier 3}
  :speed "60 ft."

  :str 18
  :dex 12
  :con 13
  :int 2
  :wis 12
  :cha 7

  :senses "passive Perception 11"
  :challenge (/ 1 2)

  :traits [{:name "Trampling Charge" :description "If the horse moves at least 20 feet straight toward a creature and then hits it with a hooves attack on the same turn, that target must succeed on a DC 14 Strength saving throw or be knocked prone. If the target is prone, the horse can make another attack with its hooves against it as a bonus action."}]

  :actions [{:name "Hooves" :description "Melee Weapon Attack: +6 to hit, reach 5 ft., one target. Hit: 11 (2d6 + 4) bludgeoning damage."}]
}{
  :name "Weasel"
  :size :tiny
  :type :beast
  :alignment "unaligned"
  :armor-class 13
  :hit-points {:mean 1 :die-count 1 :die 4 :modifier -1}
  :speed "30 ft."

  :str 3
  :dex 16
  :con 8
  :int 2
  :wis 12
  :cha 3

  :skills {:perception 3, :stealth 5}
  :senses "passive Perception 13"
  :challenge 0

  :traits [{:name "Keen Hearing and Smell." :description "The weasel has advantage on Wisdom (Perception) checks that rely on hearing or smell."}]

  :actions [{:name "Bite" :description "Melee Weapon Attack: +5 to hit, reach 5 ft., one target. Hit: 1 piercing damage."}]
}{
  :name "Winter Wolf"
  :size :large
  :type :monstrosity
  :alignment "neutral evil"
  :armor-class 13
  :armor-notes "natural armor"
  :hit-points {:mean 75 :die-count 10 :die 10 :modifier 20}
  :speed "50 ft."

  :str 18
  :dex 13
  :con 14
  :int 7
  :wis 12
  :cha 8

  :skills {:perception 5, :stealth 3}
  :damage-immunities "cold"
  :senses "passive Perception 15"
  :languages "Common, Giant, Winter Wolf"
  :challenge 3

  :traits [{:name "Keen Hearing and Smell." :description "The wolf has advantage on Wisdom (Perception) checks that rely on hearing or smell."}
           {:name "Pack Tactics" :description "The wolf has advantage on an attack roll against a creature if at least one of the wolf's allies is within 5 feet of the creature and the ally isn't incapacitated."}
           {:name "Snow Camouflage" :description "The wolf has advantage on Dexterity (Stealth) checks made to hide in snowy terrain."}]

  :actions [{:name "Bite" :description "Melee Weapon Attack: +6 to hit, reach 5 ft., one target. Hit: 11 (2d6 + 4) piercing damage. If the target is a creature, it must succeed on a DC 14 Strength saving throw or be knocked prone."}
            {:name "Cold Breath" :notes "Recharge 5–6" :description "The wolf exhales a blast of freezing wind in a 15-foot cone. Each creature in that area must make a DC 12 Dexterity saving throw, taking 18 (4d8) cold damage on a failed save, or half as much damage on a successful one."}]
  
  :description "The arctic-dwelling winter wolf is as large as a dire wolf but has snow-white fur and pale blue eyes. Frost giants use these evil creatures as guards and hunting companions, putting the wolves' deadly breath weapon to use against their foes. Winter wolves communicate with one another using growls and barks, but they speak Common and Giant well enough to follow simple conversations."
}{
  :name "Wolf"
  :size :medium
  :type :beast
  :alignment "unaligned"
  :armor-class 13
  :armor-notes "natural armor"
  :hit-points {:mean 11 :die-count 2 :die 8 :modifier 2}
  :speed "40 ft."

  :str 12
  :dex 15
  :con 12
  :int 3
  :wis 12
  :cha 6

  :skills {:perception 3, :stealth 4}
  :senses "passive Perception 13"
  :challenge (/ 1 4)

  :traits [{:name "Keen Hearing and Smell." :description "The wolf has advantage on Wisdom (Perception) checks that rely on hearing or smell."}
           {:name "Pack Tactics" :description "The wolf has advantage on attack rolls against a creature if at least one of the wolf's allies is within 5 feet of the creature and the ally isn't incapacitated."}]

  :actions [{:name "Bite" :description "Melee Weapon Attack: +4 to hit, reach 5 ft., one target. Hit: 7 (2d4 + 2) piercing damage. If the target is a creature, it must succeed on a DC 11 Strength saving throw or be knocked prone."}]
}{
  :name "Worg"
  :size :large
  :type :monstrosity
  :alignment "neutral evil"
  :armor-class 13
  :armor-notes "natural armor"
  :hit-points {:mean 26 :die-count 4 :die 10 :modifier 4}
  :speed "50 ft."

  :str 16
  :dex 13
  :con 13
  :int 7
  :wis 11
  :cha 8

  :skills {:perception 4}
  :senses "darkvision 60 ft., passive Perception 14"
  :languages "Goblin, Worg"
  :challenge (/ 1 2)

  :traits [{:name "Keen Hearing and Smell." :description "The worg has advantage on Wisdom (Perception) checks that rely on hearing or smell."}]

  :actions [{:name "Bite" :description "Melee Weapon Attack: +5 to hit, reach 5 ft., one target. Hit: 10 (2d6 + 3) piercing damage. If the target is a creature, it must succeed on a DC 13 Strength saving throw or be knocked prone."}]

  :description "A worg is an evil predator that delights in hunting and devouring creatures weaker than itself. Cunning and malevolent, worgs roam across the remote wilderness or are raised by goblins and hobgoblins. Those creatures use worgs as mounts, but a worg  will turn on its rider if it feels mistreated or malnourished. Worgs speak in their own language and Goblin, and a few learn to speak Common as well."
}{
  :name "Acolyte"
  :size :medium
  :type :humanoid
  :subtypes #{:any-race}
  :alignment "any alignment"
  :armor-class 10
  :hit-points {:mean 9 :die-count 2 :die 8}
  :speed "30 ft."

  :str 10
  :dex 10
  :con 10
  :int 10
  :wis 14
  :cha 11

  :skills {:medicine 4, :religion 2}
  :senses "passive Perception 12"
  :languages "any one language (usually Common)"
  :challenge (/ 1 4)

  :traits [{:name "Spellcasting" :description "The acolyte is a 1st-level spellcaster. Its spellcasting ability is Wisdom (spell save DC 12, +4 to hit with spell attacks). 
The acolyte has following cleric spells prepared:
Cantrips (at will): light, sacred flame, thaumaturgy
1st level (3 slots): bless, cure wounds, sanctuary"}]

  :actions [{:name "Club" :description "Melee Weapon Attack: +2 to hit, reach 5 ft., one target. Hit: 2 (1d4) bludgeoning damage."}]

  :description "Acolytes are junior members of a clergy, usually answerable to a priest. They perform a variety of functions in a temple and are granted minor spellcasting power by their deities."
}{
  :name "Archmage"
  :size :medium
  :type :humanoid
  :subtypes #{:any-race}
  :alignment "any alignment"
  :armor-class 12
  :armor-notes "15 with mage armor"
  :hit-points {:mean 99 :die-count 18 :die 8 :modifier 18}
  :speed "30 ft."

  :str 10
  :dex 14
  :con 12
  :int 20
  :wis 15
  :cha 16

  :saving-throws {:int 9, :wis 6}
  :skills {:arcana 13, :history 13}
  :damage-resistances "damage from spells; nonmagical bludgeoning, piercing, and slashing (from stoneskin)"
  :senses "passive Perception 12"
  :languages "any six languages"
  :challenge 12

  :traits [{:name "Magic Resistance" :description "The archmage has advantage on saving throws against spells and other magical effects."}
           {:name "Spellcasting" :description "The archmage is an 18th-level spellcaster. Its spellcasting ability is Intelligence (spell save DC 17, +9 to hit with spell attacks). 
The archmage can cast disguise self and invisibility at will and has the following wizard spells prepared:
Cantrips (at will): fire bolt, light, mage hand, prestidigitation,  shocking grasp
1st level (4 slots): detect magic, identify, mage armor,* magic missile
2nd level (3 slots): detect thoughts, mirror image, misty step
3rd level (3 slots): counterspell, fly, lightning bolt
4th level (3 slots): banishment, fire shield, stoneskin* 5th level (3 slots): cone of cold, scrying, wall of force 6th level (1 slot): globe of invulnerability
7th level (1 slot): teleport
8th level (1 slot): mind blank* 9th level (1 slot): time stop
*The archmage casts these spells on itself before combat."}]

  :actions [{:name "Dagger" :description "Melee or Ranged Weapon Attack: +6 to hit, reach 5 ft. or range 20/60 ft., one target. Hit: 4 (1d4 + 2) piercing damage"}]

  :description "Archmages are powerful (and usually quite old) spellcasters dedicated to the study of the arcane arts. Benevolent ones counsel kings and queens, while evil ones rule as tyrants and pursue lichdom. Those who are neither good nor evil sequester themselves in remote towers to practice their magic without interruption. An archmage typically has one or more apprentice mages, and an archmage's abode has numerous magical wards and guardians to discourage interlopers."
}{
  :name "Assassin"
  :size :medium
  :type :humanoid
  :subtypes #{:any-race}
  :alignment "any non-good alignment"
  :armor-class 15
  :armor-notes "studded leather"
  :hit-points {:mean 78 :die-count 12 :die 8 :modifier 24}
  :speed "30 ft."

  :str 11
  :dex 16
  :con 14
  :int 13
  :wis 11
  :cha 10

  :saving-throws {:dex 6, :int 4}
  :skills {:acrobatics 6, :deception 3, :perception 3, :stealth 9}
  :damage-resistances "poison"
  :senses "passive Perception 13"
  :languages "Thieves' cant plus any two languages"
  :challenge 8

  :traits [{:name "Assassinate" :description "During its first turn, the assassin has advantage on attack rolls against any creature that hasn't taken a turn. Any hit the assassin scores against a surprised creature is a critical hit."}
           {:name "Evasion" :description "If the assassin is subjected to an effect that allows it to make a Dexterity saving throw to take only half damage, the assassin instead takes no damage if it succeeds on the saving throw, and only half damage if it fails."}
           {:name "Sneak Attack" :description "Once per turn, the assassin deals an extra 14 (4d6) damage when it hits a target with a weapon attack and has advantage on the attack roll, or when the target is within 5 feet of an ally of the assassin that isn't incapacitated and the assassin doesn't have disadvantage on the attack roll."}]

  :actions [{:name "Multiattack" :description "The assassin makes two shortsword attacks."}
            {:name "Shortsword" :description "Melee Weapon Attack: +6 to hit, reach 5 ft., one target. Hit: 6 (1d6 + 3) piercing damage, and the target must make a DC 15 Constitution saving throw, taking 24 (7d6) poison damage on a failed save, or half as much damage on a successful one."}
            {:name "Light Crossbow" :description "Ranged Weapon Attack: +6 to hit, range 80/320 ft., one target. Hit: 7 (1d8 + 3) piercing damage, and the target must make a DC 15 Constitution saving throw, taking 24 (7d6) poison damage on a failed save, or half as much damage on a successful one."}]

  :description "Trained in the use of poison, assassins are remorseless killers who work for nobles, guildmasters, sovereigns, and anyone else who can afford them."
}{
  :name "Bandit"
  :size :medium
  :type :humanoid
  :subtypes #{:any-race}
  :alignment "any non-lawful alignment"
  :armor-class 12
  :armor-notes "leather armor"
  :hit-points {:mean 11 :die-count 2 :die 8 :modifier 2}
  :speed "30 ft."

  :str 11
  :dex 12
  :con 12
  :int 10
  :wis 10
  :cha 10

  :senses "passive Perception 10"
  :languages "any one language (usually Common)"
  :challenge (/ 1 8)

  :actions [{:name "Scimitar" :description "Melee Weapon Attack: +3 to hit, reach 5 ft., one target. Hit: 4 (1d6 + 1) slashing damage."}
            {:name "Light Crossbow" :description "Ranged Weapon Attack: +3 to hit, range 80 ft./320 ft., one target. Hit: 5 (1d8 + 1) piercing damage."}]

  :description "Bandits rove in gangs and are sometimes led by thugs, veterans, or spellcasters. Not all bandits are evil. Oppression, drought, disease, or famine can often drive otherwise honest folk to a life of banditry. 
Pirates are bandits of the high seas. They might be freebooters interested only in treasure and murder, or they might be privateers sanctioned by the crown to attack and plunder an enemy nation's vessels."
}{
  :name "Bandit Captain"
  :size :medium
  :type :humanoid
  :subtypes #{:any-race}
  :alignment "any non-lawful alignment"
  :armor-class 15
  :armor-notes "studded leather"
  :hit-points {:mean 65 :die-count 10 :die 8 :modifier 20}
  :speed "30 ft."

  :str 15
  :dex 16
  :con 14
  :int 14
  :wis 11
  :cha 14

  :saving-throws {:str 4, :dex 5, :wis 2}
  :skills {:athletics 4, :deception 4}
  :senses "passive Perception 10"
  :languages "any two languages"
  :challenge 2

  :actions [{:name "Multiattack" :description "The captain makes three melee attacks: two with its scimitar and one with its dagger. Or the captain makes two ranged attacks with its daggers."}
            {:name "Scimitar" :description "Melee Weapon Attack: +5 to hit, reach 5 ft., one target. Hit: 6 (1d6 + 3) slashing damage."}
            {:name "Dagger" :description "Melee or Ranged Weapon Attack: +5 to hit, reach 5 ft. or range 20/60 ft., one target. Hit: 5 (1d4 + 4)"}]

  :reactions [{:name "Parry" :description "The captain adds 2 to its AC against one melee attack that would hit it. To do so, the captain must see the attacker and be wielding a melee weapon."}]

  :description "It takes a strong personality, ruthless cunning, and a silver tongue to keep a gang of bandits in line. The bandit captain has these qualities in  spades.
In addition to managing a crew of selfish malcontents, the pirate captain is a variation of the bandit captain, with a ship to protect and command. To keep the crew in line, the captain must mete out rewards and punishment on a regular  basis.
More than treasure, a bandit captain or pirate captain craves infamy. A prisoner who appeals to the captain's vanity or ego is more likely to be treated fairly than a prisoner who does not or claims not to know anything of the captain's colorful reputation."
}{
  :name "Berserker"
  :size :medium
  :type :humanoid
  :subtypes #{:any-race}
  :alignment "any chaotic alignment"
  :armor-class 13
  :armor-notes "hide armor"
  :hit-points {:mean 67 :die-count 9 :die 8 :modifier 27}
  :speed "30 ft."

  :str 16
  :dex 12
  :con 17
  :int 9
  :wis 11
  :cha 9

  :senses "passive Perception 10"
  :languages "any one language (usually Common)"
  :challenge 2

  :traits [{:name "Reckless" :description "At the start of its turn, the berserker can gain advantage on all melee weapon attack rolls during that turn, but attack rolls against it have advantage until the start of its next turn."}]

  :actions [{:name "Greataxe" :description "Melee Weapon Attack: +5 to hit, reach 5 ft., one target. Hit: 9 (1d12 + 3) slashing damage."}]

  :description "Hailing from uncivilized lands, unpredictable berserkers come together in war parties and seek conflict wherever they can find it."
}{
  :name "Commoner"
  :size :medium
  :type :humanoid
  :subtypes #{:any-race}
  :alignment "any alignment"
  :armor-class 10
  :hit-points {:mean 4 :die-count 1 :die 8}
  :speed "30 ft."

  :str 10
  :dex 10
  :con 10
  :int 10
  :wis 10
  :cha 10

  :senses "passive Perception 10"
  :languages "any one language (usually Common)"
  :challenge 0

  :actions [{:name "Club" :description "Melee Weapon Attack: +2 to hit, reach 5 ft., one target. Hit: 2 (1d4) bludgeoning damage."}]

  :description "Commoners include peasants, serfs, slaves, servants, pilgrims, merchants, artisans, and  hermits."
}{
  :name "Cultist"
  :size :medium
  :type :humanoid
  :subtypes #{:any-race}
  :alignment "any non-good alignment"
  :armor-class 12
  :armor-notes "leather armor"
  :hit-points {:mean 9 :die-count 2 :die 8}
  :speed "30 ft."

  :str 11
  :dex 12
  :con 10
  :int 10
  :wis 11
  :cha 10

  :skills {:deception 2, :religion 2}
  :senses "passive Perception 10"
  :languages "any one language (usually Common)"
  :challenge (/ 1 8)

  :traits [{:name "Dark Devotion" :description "The cultist has advantage on saving throws against being charmed or frightened."}]

  :actions [{:name "Scimitar" :description "Melee Weapon Attack: +3 to hit, reach 5 ft., one creature. Hit: 4 (1d6 + 1) slashing damage."}]

  :description "Cultists swear allegiance to dark powers such as elemental princes, demon lords, or archdevils. Most conceal their loyalties to avoid being ostracized, imprisoned, or executed for their beliefs. Unlike evil acolytes, cultists often show signs of insanity in their beliefs and practices."
}{
  :name "Cult Fanatic"
  :size :medium
  :type :humanoid
  :subtypes #{:any-race}
  :alignment "any non-good alignment"
  :armor-class 13
  :armor-notes "leather armor"
  :hit-points {:mean 33 :die-count 6 :die 8 :modifier 6}
  :speed "30 ft."

  :str 11
  :dex 14
  :con 12
  :int 10
  :wis 13
  :cha 14

  :skills {:deception 4, :persuasion 4, :religion 2}
  :senses "passive Perception 11"
  :languages "any one language (usually Common)"
  :challenge 2

  :traits [{:name "Dark Devotion" :description "The fanatic has advantage on saving throws against being charmed or frightened."}
           {:name "Spellcasting" :description "The fanatic is a 4th-level spellcaster. Its spellcasting ability is Wisdom (spell save DC 11, +3 to hit with spell attacks). 
The fanatic has the following cleric spells prepared:
Cantrips (at will): light, sacred flame, thaumaturgy
1st level (4 slots): command, inflict wounds, shield of faith
2nd level (3 slots): hold person, spiritual weapon"}]

  :actions [{:name "Multiattack" :description "The fanatic makes two melee attacks."}
            {:name "Dagger" :description "Melee or Ranged Weapon Attack: +4 to hit, reach 5 ft. or range 20/60 ft., one creature. Hit: 4 (1d4 + 2) piercing damage."}]
  
  :description "Fanatics are often part of a cult's leadership, using their charisma and dogma to influence and prey on those of weak will. Most are interested in personal power above all else."
}{
  :name "Druid"
  :size :medium
  :type :humanoid
  :subtypes #{:any-race}
  :alignment "any alignment"
  :armor-class 11
  :armor-notes "16 with barkskin"
  :hit-points {:mean 27 :die-count 5 :die 8 :modifier 5}
  :speed "30 ft."

  :str 10
  :dex 12
  :con 13
  :int 12
  :wis 15
  :cha 11

  :skills {:medicine 4, :nature 3, :perception 4}
  :senses "passive Perception 14"
  :languages "Druidic plus any two languages"
  :challenge 2

  :traits [{:name "Spellcasting" :description "The druid is a 4th-level spellcaster. Its spellcasting ability is Wisdom (spell save DC 12, +4 to hit with spell attacks). 
It has the following druid spells prepared:
Cantrips (at will): druidcraft, produce flame, shillelagh
1st level (4 slots): entangle, longstrider, speak with animals, thunderwave
2nd level (3 slots): animal messenger, barkskin"}]

  :actions [{:name "Quarterstaff" :description "Melee Weapon Attack: +2 to hit (+4 to hit with shillelagh), reach 5 ft., one target. Hit: 3 (1d6) bludgeoning damage, 4 (1d8) bludgeoning damage if wielded with two hands, or 6 (1d8 + 2) bludgeoning damage with shillelagh."}]

  :description "Druids dwell in forests and other secluded wilderness locations, where they protect the natural world from monsters and the encroachment of civilization. Some are tribal shamans who heal the sick, pray to animal spirits, and provide spiritual guidance."
}{
  :name "Gladiator"
  :size :medium
  :type :humanoid
  :subtypes #{:any-race}
  :alignment "any alignment"
  :armor-class 16
  :armor-notes "studded leather, shield"
  :hit-points {:mean 112 :die-count 15 :die 8 :modifier 45}
  :speed "30 ft."

  :str 18
  :dex 15
  :con 16
  :int 10
  :wis 12
  :cha 15

  :saving-throws {:str 7, :dex 5, :con 6}
  :skills {:athletics 10, :intimidation 5}
  :senses "passive Perception 11"
  :languages "any one language (usually Common)"
  :challenge 5

  :traits [{:name "Brave" :description "The gladiator has advantage on saving throws against being frightened."}
           {:name "Brute" :description "A melee weapon deals one extra die of its damage when the gladiator hits with it (included in the attack)."}]

  :actions [{:name "Multiattack" :description "The gladiator makes three melee attacks or two ranged attacks."}
            {:name "Spear" :description "Melee or Ranged Weapon Attack: +7 to hit, reach 5 ft. and range 20/60 ft., one target. Hit: 11 (2d6 + 4) piercing damage, or 13 (2d8 + 4) piercing damage if used with two hands to make a melee attack."}
            {:name "Shield Bash" :description "Melee Weapon Attack: +7 to hit, reach 5 ft., one creature. Hit: 9 (2d4 + 4) bludgeoning damage. If the target is a Medium or smaller creature, it must succeed on a DC 15 Strength saving throw or be knocked prone."}]

  :reactions [{:name "Parry" :description "The gladiator adds 3 to its AC against one melee attack that would hit it. To do so, the gladiator must see the attacker and be wielding a melee weapon."}]

  :description "Gladiators battle for the entertainment of raucous crowds. Some gladiators are brutal pit fighters who treat each match as a life-‑or-‑death struggle, while others are professional duelists who command huge fees but rarely fight to the death."
}{
  :name "Guard"
  :size :medium
  :type :humanoid
  :subtypes #{:any-race}
  :alignment "any alignment"
  :armor-class 16
  :armor-notes "chain shirt, shield"
  :hit-points {:mean 11 :die-count 2 :die 8 :modifier 2}
  :speed "30 ft."

  :str 13
  :dex 12
  :con 12
  :int 10
  :wis 11
  :cha 10

  :skills {:perception 2}
  :senses "passive Perception 12"
  :languages "any one language (usually Common)"
  :challenge (/ 1 8)

  :actions [{:name "Spear" :description "Melee or Ranged Weapon Attack: +3 to hit, reach 5 ft. or range 20/60 ft., one target. Hit: 4 (1d6 + piercing damage, or 5 (1d8 + 1) piercing damage if used with two hands to make a melee attack."}]

  :description "Guards include members of a city watch, sentries in a citadel or fortified town, and the bodyguards of merchants and nobles."
}{
  :name "Knight"
  :size :medium
  :type :humanoid
  :subtypes #{:any-race}
  :alignment "any alignment"
  :armor-class 18
  :armor-notes "plate"
  :hit-points {:mean 52 :die-count 8 :die 8 :modifier 16}
  :speed "30 ft."

  :str 16
  :dex 11
  :con 14
  :int 11
  :wis 11
  :cha 15

  :saving-throws {:con 4, :wis 2}
  :senses "passive Perception 10"
  :languages "any one language (usually Common)"
  :challenge 3

  :traits [{:name "Brave" :description "The knight has advantage on saving throws against being frightened."}]

  :actions [{:name "Multiattack" :description "The knight makes two melee attacks."}
            {:name "Greatsword" :description "Melee Weapon Attack: +5 to hit, reach 5 ft., one target. Hit: 10 (2d6 + 3) slashing damage."}
            {:name "Heavy Crossbow" :description "Ranged Weapon Attack: +2 to hit, range 100/400 ft., one target. Hit: 5 (1d10) piercing damage."}
            {:name "Leadership" :notes "Recharges after a Short or Long Rest" :description "For 1 minute, the knight can utter a special command or warning whenever a nonhostile creature that it can see within 30 feet of it makes an attack roll or a saving throw. The creature can add a d4 to its roll provided it can hear and understand the knight. A creature can benefit from only one Leadership die at a time. This effect ends if the knight is incapacitated."}]
  
  :reactions [{:name "Parry" :description "The knight adds 2 to its AC against one melee attack that would hit it. To do so, the knight must see the attacker and be wielding a melee weapon."}]

  :description "Knights are warriors who pledge service to rulers, religious orders, and noble causes. A knight's alignment determines the extent to which a pledge is honored. Whether undertaking a quest or patrolling a realm, a knight often travels with an entourage that includes squires and hirelings who are commoners."
}{
  :name "Mage"
  :size :medium
  :type :humanoid
  :subtypes #{:any-race}
  :alignment "any alignment"
  :armor-class 12
  :armor-notes "15 with mage armor"
  :hit-points {:mean 40 :die-count 9 :die 8}
  :speed "30 ft."

  :str 9
  :dex 14
  :con 11
  :int 17
  :wis 12
  :cha 11

  :saving-throws {:int 6, :wis 4}
  :skills {:arcana 6, :history 6}
  :senses "passive Perception 11"
  :languages "any four languages"
  :challenge 6

  :traits [{:name "Spellcasting" :description "The mage is a 9th-level spellcaster. Its spellcasting ability is Intelligence (spell save DC 14, +6 to hit with spell attacks). 
The mage has the following wizard spells prepared:
Cantrips (at will): fire bolt, light, mage hand, prestidigitation
1st level (4 slots): detect magic, mage armor, magic missile, shield
2nd level (3 slots): misty step, suggestion
3rd level (3 slots): counterspell, fireball, fly
4th level (3 slots): greater invisibility, ice storm
5th level (1 slot): cone of cold"}]

  :actions [{:name "Dagger" :description "Melee or Ranged Weapon Attack: +5 to hit, reach 5 ft. or range 20/60 ft., one target. Hit: 4 (1d4 + 2) piercing damage."}]

  :description "Mages spend their lives in the study and practice of magic. Good-‑aligned mages offer counsel to nobles and others in power, while evil mages dwell in isolated sites to perform unspeakable experiments without interference."
}{
  :name "Noble"
  :size :medium
  :type :humanoid
  :subtypes #{:any-race}
  :alignment "any alignment"
  :armor-class 15
  :armor-notes "breastplate"
  :hit-points {:mean 9 :die-count 2 :die 8}
  :speed "30 ft."

  :str 11
  :dex 12
  :con 11
  :int 12
  :wis 14
  :cha 16

  :skills {:deception 5, :insight 4, :persuasion 5}
  :senses "passive Perception 12"
  :languages "any two languages"
  :challenge (/ 1 8)

  :actions [{:name "Rapier" :description "Melee Weapon Attack: +3 to hit, reach 5 ft., one target. Hit: 5 (1d8 + 1) piercing damage."}]

  :reactions [{:name "Parry" :description "The noble adds 2 to its AC against one melee attack that would hit it. To do so, the noble must see the attacker and be wielding a melee weapon."}]

  :description "Nobles wield great authority and influence as members of the upper class, possessing wealth and connections that can make them as powerful as monarchs and generals. A noble often travels in the company of guards, as well as servants who are commoners.
The noble's statistics can also be used to represent courtiers who aren't of noble birth."
}{
  :name "Priest"
  :size :medium
  :type :humanoid
  :subtypes #{:any-race}
  :alignment "any alignment"
  :armor-class 13
  :armor-notes "chain shirt"
  :hit-points {:mean 27 :die-count 5 :die 8 :modifier 5}
  :speed "25 ft."

  :str 10
  :dex 10
  :con 12
  :int 13
  :wis 16
  :cha 13

  :skills {:medicine 7, :persuasion 3, :religion 4}
  :senses "passive Perception 13"
  :languages "any two languages"
  :challenge 2

  :traits [{:name "Divine Eminence" :description "As a bonus action, the priest can expend a spell slot to cause its melee weapon attacks to magically deal an extra 10 (3d6) radiant damage to a target on a hit. This benefit lasts until the end of the turn. If the priest expends a spell slot of 2nd level or higher, the extra damage increases by 1d6 for each level above 1st."}
           {:name "Spellcasting" :description "The priest is a 5th-level spellcaster. Its spellcasting ability is Wisdom (spell save DC 13, +5 to hit with spell attacks). 
The priest has the following cleric spells prepared:
Cantrips (at will): light, sacred flame, thaumaturgy
1st level (4 slots): cure wounds, guiding bolt, sanctuary
2nd level (3 slots): lesser restoration, spiritual weapon
3rd level (2 slots): dispel magic, spirit guardians"}]

  :actions [{:name "Mace" :description "Melee Weapon Attack: +2 to hit, reach 5 ft., one target. Hit: 3 (1d6) bludgeoning damage."}]

  :description "Priests bring the teachings of their gods to the common folk. They are the spiritual leaders of temples and shrines and often hold positions of influence in their communities. Evil priests might work openly under a tyrant, or they might be the leaders of religious sects hidden in the shadows of good society, overseeing depraved rites.
A priest typically has one or more acolytes to help with religious ceremonies and other sacred duties."
}{
  :name "Scout"
  :size :medium
  :type :humanoid
  :subtypes #{:any-race}
  :alignment "any alignment"
  :armor-class 13
  :armor-notes "leather armor"
  :hit-points {:mean 16 :die-count 3 :die 8 :modifier 3}
  :speed "30 ft."

  :str 11
  :dex 14
  :con 12
  :int 11
  :wis 13
  :cha 11

  :skills {:nature 4, :perception 5, :stealth 6, :survival 5}
  :senses "passive Perception 15"
  :languages "any one language (usually Common)"
  :challenge (/ 1 2)

  :traits [{:name "Keen Hearing and Sight" :description "The scout has advantage on Wisdom (Perception) checks that rely on hearing or sight."}]

  :actions [{:name "Multiattack" :description "The scout makes two melee attacks or two ranged attacks."}
            {:name "Shortsword" :description "Melee Weapon Attack: +4 to hit, reach 5 ft., one target. Hit: 5 (1d6 + 2) piercing damage."}
            {:name "Longbow" :description "Ranged Weapon Attack: +4 to hit, ranged 150/600 ft., one target. Hit: 6 (1d8 + 2) piercing damage."}]

  :description "Scouts are skilled hunters and trackers who offer their services for a fee. Most hunt wild game, but a few work as bounty hunters, serve as guides, or provide  military reconnaissance."
}{
  :name "Spy"
  :size :medium
  :type :humanoid
  :subtypes #{:any-race}
  :alignment "any alignment"
  :armor-class 12
  :hit-points {:mean 27 :die-count 6 :die 8}
  :speed "30 ft."

  :str 10
  :dex 15
  :con 10
  :int 12
  :wis 14
  :cha 16

  :skills {:deception 5, :insight 4, :investigation 5, :perception 6, :persuasion 5, :sleight-of-hand 4}
  :stealth 4
  :senses "passive Perception 16"
  :languages "any two languages"
  :challenge 1

  :traits [{:name "Cunning Action" :description "On each of its turns, the spy can use a bonus action to take the Dash, Disengage, or Hide action."}
           {:name "Sneak Attack" :notes "1/Turn" :description "The spy deals an extra 7 (2d6) damage when it hits a target with a weapon attack and has advantage on the attack roll, or when the target is within 5 feet of an ally of the spy that isn't incapacitated and the spy doesn't have disadvantage on the attack roll."}]

  :actions [{:name "Multiattack" :description "The spy makes two melee attacks."}
            {:name "Shortsword" :description "Melee Weapon Attack: +4 to hit, reach 5 ft., one target. Hit: 5 (1d6 + 2) piercing damage."}
            {:name "Hand Crossbow" :description "Ranged Weapon Attack: +4 to hit, range 30/120 ft., one target. Hit: 5 (1d6 + 2) piercing damage."}]

  :description "Rulers, nobles, merchants, guildmasters, and other wealthy individuals use spies to gain the upper hand in a world of cutthroat politics. A spy is trained to secretly gather information. Loyal spies would   rather die than divulge information that could compromise them or their  employers."
}{
  :name "Thug"
  :size :medium
  :type :humanoid
  :subtypes #{:any-race}
  :alignment "any non-good alignment"
  :armor-class 11
  :armor-notes "leather armor"
  :hit-points {:mean 32 :die-count 5 :die 8 :modifier 10}
  :speed "30 ft."

  :str 15
  :dex 11
  :con 14
  :int 10
  :wis 10
  :cha 11

  :skills {:intimidation 2}
  :senses "passive Perception 10"
  :languages "any one language (usually Common)"
  :challenge (/ 1 2)

  :traits [{:name "Pack Tactics" :description "The thug has advantage on an attack roll against a creature if at least one of the thug's allies is within 5 feet of the creature and the ally isn't incapacitated."}]

  :actions [{:name "Multiattack" :description "The thug makes two melee attacks."}
            {:name "Mace" :description "Melee Weapon Attack: +4 to hit, reach 5 ft., one creature. Hit: 5 (1d6 + 2) bludgeoning damage."}
            {:name "Heavy Crossbow" :description "Ranged Weapon Attack: +2 to hit, range 100/400 ft., one target. Hit: 5 (1d10) piercing damage."}]

  :description "Thugs are ruthless enforcers skilled at intimidation and violence. They work for money and have few scruples."
}{
  :name "Tribal Warrior"
  :size :medium
  :type :humanoid
  :subtypes #{:any-race}
  :alignment "any alignment"
  :armor-class 12
  :armor-notes "hide armor"
  :hit-points {:mean 11 :die-count 2 :die 8 :modifier 2}
  :speed "30 ft."

  :str 13
  :dex 11
  :con 12
  :int 8
  :wis 11
  :cha 8

  :senses "passive Perception 10"
  :languages "any one language"
  :challenge (/ 1 8)

  :traits [{:name "Pack Tactics" :description "The warrior has advantage on an attack roll against a creature if at least one of the warrior's allies is within 5 feet of the creature and the ally isn't incapacitated."}]

  :actions [{:name "Spear" :description "Melee or Ranged Weapon Attack: +3 to hit, reach 5 ft. or range 20/60 ft., one target. Hit: 4 (1d6 + 1) piercing damage, or 5 (1d8 + 1) piercing damage if used with two hands to make a melee attack."}]

  :description "Tribal warriors live beyond civilization, most often subsisting on fishing and hunting. Each tribe acts in accordance with the wishes of its chief, who is the greatest or oldest warrior of the tribe or a tribe member blessed by the gods."
}{
  :name "Veteran"
  :size :medium
  :type :humanoid
  :subtypes #{:any-race}
  :alignment "any alignment"
  :armor-class 17
  :armor-notes "splint"
  :hit-points {:mean 58 :die-count 9 :die 8 :modifier 18}
  :speed "30 ft."

  :str 16
  :dex 13
  :con 14
  :int 10
  :wis 11
  :cha 10

  :skills {:athletics 5, :perception 2}
  :senses "passive Perception 12"
  :languages "any one language (usually Common)"
  :challenge 3

  :actions [{:name "Multiattack" :description "The veteran makes two longsword attacks. If it has a shortsword drawn, it can also make a shortsword attack."}
            {:name "Longsword" :description "Melee Weapon Attack: +5 to hit, reach 5 ft., one target. Hit: 7 (1d8 + 3) slashing damage, or 8 (1d10 + 3) slashing damage if used with two hands."}
            {:name "Shortsword" :description "Melee Weapon Attack: +5 to hit, reach 5 ft., one target. Hit: 6 (1d6 + 3) piercing damage."}
            {:name "Heavy Crossbow" :description "Ranged Weapon Attack: +3 to hit, range 100/400 ft., one target. Hit: 6 (1d10 + 1) piercing damage."}]

  :description "Veterans are professional fighters that take up arms for pay or to protect something they believe in or value. Their ranks include soldiers retired from long service and warriors who never served anyone but themselves."
}])

(def monsters (map (fn [m] (assoc m :key (name-to-kw (:name m)))) monsters-raw))
(def monster-map (reduce (fn [mp m] (assoc mp (:key m) m))
                         {}
                         monsters))
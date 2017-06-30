(ns orcpub.dnd.e5.templates.ua-mystic
  (:require [orcpub.template :as t]
            [orcpub.dnd.e5.modifiers :as mod5e]
            [orcpub.dnd.e5.character :as char5e]
            [orcpub.dnd.e5.options :as opt5e]
            [orcpub.dnd.e5.units :as units5e]))

(def ua-mystic-kw :ua-mystic)

(def psionic-disciplines
  [{:name "Adaptive Body"
    :mystic-order :immortal
    :modifiers [(mod5e/trait-cfg
                 {:name "Adaptive Body: Psychic Focus"
                  :page 10
                  :source ua-mystic-kw
                  :summary "don't need to eat, sleep, or breathe"})
                (mod5e/action
                 {:name "Environmental Adaptation"
                  :page 10
                  :source ua-mystic-kw
                  :qualifier "2 psi"
                  :duration units5e/conc-hours-1
                  :summary "you or a creature you touch can ignore effects of extreme cold or heat"})
                (mod5e/reaction
                 {:name "Adaptive Shield"
                  :page 10
                  :source ua-mystic-kw
                  :qualifier "3 psi"
                  :duration units5e/turns-1
                  :summary "when you take lighting, thunder, fire, cold, or acid damage gain resistance to that type of damage"})
                (mod5e/action
                 {:name "Energy Adaptation"
                  :page 10
                  :source ua-mystic-kw
                  :qualifier "5 psi"
                  :duration units5e/conc-hours-1
                  :summary "impart resistance to cold, acid, fire, lighting, or thunder damage"})
                (mod5e/action
                 {:name "Energy Immunity"
                  :page 10
                  :source ua-mystic-kw
                  :qualifier "7 psi"
                  :duration units5e/conc-hours-1
                  :summary "impart immunity to cold, acid, fire, lighting, or thunder damage"})]}
   {:name "Aura Sight"
    :mystic-order :awakened
    :modifiers [(mod5e/trait-cfg
                 {:name "Aura Sight: Psychic Focus"
                  :page 11
                  :source ua-mystic-kw
                  :summary "advantage on Insight checks"})
                (mod5e/bonus-action
                 {:name "Assess Foe"
                  :page 11
                  :source ua-mystic-kw
                  :qualifier "2 psi"
                  :summary "learn a creature's current HPs and its immunities, resistances, and vulnerabilities"})
                (mod5e/bonus-action
                 {:name "Read Moods"
                  :page 11
                  :source ua-mystic-kw
                  :qualifier "2 psi"
                  :summary "learn 1 word summary of the mood of up to 6 creatures"})
                (mod5e/action
                 {:name "View Aura"
                  :page 11
                  :source ua-mystic-kw
                  :qualifier "3 psi"
                  :duration units5e/conc-hours-1
                  :summary "learn effects on a creature, it's current HPs, and its mood"})
                (mod5e/bonus-action
                 {:name "Perceive the Unseen"
                  :page 11
                  :source ua-mystic-kw
                  :qualifier "5 psi"
                  :duration units5e/conc-hours-1
                  :summary "see invisible and hidden creatures"})]}
   {:name "Bestial Form"
    :mystic-order :immortal
    :modifiers [(mod5e/trait-cfg
                 {:name "Bestial Form: Psychic Focus"
                  :page 11
                  :source ua-mystic-kw
                  :summary "advantage on Wisdom checks"})
                (mod5e/trait-cfg
                 {:name "Bestial Claws"
                  :page 11
                  :source ua-mystic-kw
                  :qualifier "1-7 psi"
                  :summary "melee weapon attack, 1d10 slashing damage per psi"})
                (mod5e/bonus-action
                 {:name "Bestial Transformation: Amphibious"
                  :page 11
                  :source ua-mystic-kw
                  :qualifier "2 psi"
                  :duration units5e/conc-hours-1
                  :summary "can breathe air or water"})
                (mod5e/bonus-action
                 {:name "Bestial Transformation: Climbing"
                  :page 11
                  :source ua-mystic-kw
                  :qualifier "2 psi"
                  :duration units5e/conc-hours-1
                  :summary "gain climbing speed equal to your walking speed"})
                (mod5e/bonus-action
                 {:name "Bestial Transformation: Flight"
                  :page 11
                  :source ua-mystic-kw
                  :qualifier "5 psi"
                  :duration units5e/conc-hours-1
                  :summary "gain flying speed equal to your walking speed"})
                (mod5e/bonus-action
                 {:name "Bestial Transformation: Keen Senses"
                  :page 11
                  :source ua-mystic-kw
                  :qualifier "2 psi"
                  :duration units5e/conc-hours-1
                  :summary "advantage on Perception checks"})
                (mod5e/bonus-action
                 {:name "Bestial Transformation: Perfect Senses"
                  :page 11
                  :source ua-mystic-kw
                  :qualifier "3 psi"
                  :duration units5e/conc-hours-1
                  :summary "see invisible creatures and objects, even if blinded"})
                (mod5e/bonus-action
                 {:name "Bestial Transformation: Swimming"
                  :page 11
                  :source ua-mystic-kw
                  :qualifier "2 psi"
                  :duration units5e/conc-hours-1
                  :summary "gain swimming speed equal to your walking speed"})
                (mod5e/bonus-action
                 {:name "Bestial Transformation: Tough Hide"
                  :page 11
                  :source ua-mystic-kw
                  :qualifier "2 psi"
                  :duration units5e/conc-hours-1
                  :summary "gain +2 AC bonus"})]}
   {:name "Brute Force"
    :mystic-order :immortal
    :modifiers [(mod5e/trait-cfg
                 {:name "Brute Force: Psychic Focus"
                  :page 11
                  :source ua-mystic-kw
                  :summary "advantage on Athletics checks"})
                (mod5e/bonus-action
                 {:name "Brute Strike"
                  :page 11
                  :source ua-mystic-kw
                  :qualifier "1-7 psi"
                  :summary "gain +1d6 attack damage bonus per psi"})
                (mod5e/reaction
                 {:name "Knock Back"
                  :page 11
                  :source ua-mystic-kw
                  :qualifier "1-7 psi"
                  :summary (str "unless it succeeds on a DC " (?spell-save-dc ::char5e/int) " STR save, push a target 10 ft per psi and do 1d6 bludgeoning damage per psi if it hits an object")})
                (mod5e/trait-cfg
                 {:name "Mighty Leap"
                  :page 11
                  :source ua-mystic-kw
                  :qualifier "1-7 psi"
                  :summary "jump 20 feet per psi"})
                (mod5e/bonus-action
                 {:name "Feat of Strength"
                  :page 11
                  :source ua-mystic-kw
                  :qualifier "2 psi"
                  :summary "gain +5 bonus to STR checks until end of your next turn"})]}
   {:name "Celerity"
    :mystic-order :immortal
    :modifiers [(mod5e/trait-cfg
                 {:name "Celerity: Psychic Focus"
                  :page 12
                  :source ua-mystic-kw
                  :summary "+10 walking speed"})
                (mod5e/bonus-action
                 {:name "Rapid Step"
                  :page 12
                  :source ua-mystic-kw
                  :qualifier "1-7 psi"
                  :duration units5e/turns-1
                  :summary "+10 walking speed per psi, also applies to climbing and swimming speed if you have them"})
                (mod5e/bonus-action
                 {:name "Agile Defense"
                  :page 12
                  :source ua-mystic-kw
                  :qualifier "2 psi"
                  :summary "take Dodge action"})
                (mod5e/action
                 {:name "Blur of Motion"
                  :page 12
                  :source ua-mystic-kw
                  :qualifier "2 psi"
                  :duration units5e/turns-1
                  :summary "become invisible during your movement"})
                (mod5e/bonus-action
                 {:name "Surge of Speed"
                  :page 11
                  :source ua-mystic-kw
                  :qualifier "2 psi"
                  :duration units5e/turns-1
                  :summary "gain climbing speed equal to walking speed, don't provoke opportunity attacks"})
                (mod5e/bonus-action
                 {:name "Surge of Action"
                  :page 11
                  :source ua-mystic-kw
                  :qualifier "5 psi"
                  :duration units5e/turns-1
                  :summary "take Dash action or make a weapon attack"})]}
   {:name "Corrosive Metabolism"
    :mystic-order :immortal
    :modifiers [(mod5e/trait-cfg
                 {:name "Corrosive Metabolism: Psychic Focus"
                  :page 12
                  :source ua-mystic-kw
                  :summary "resistance to acid and poison damage"})
                (mod5e/action
                 {:name "Corrosive Touch"
                  :page 12
                  :source ua-mystic-kw
                  :qualifier "1-7 psi"
                  :summary (str "deal 1d10 acid damage per psi, half on successful DC " (?spell-save-dc ::char5e/int) " DEX save")})
                (mod5e/action
                 {:name "Venom Strike"
                  :page 12
                  :source ua-mystic-kw
                  :qualifier "1-7 psi"
                  :range units5e/ft-30
                  :summary (str "deal 1d6 per psi poison damage and target it poisoned until end of your next turn, it takes half damage and is not poisoned on successful DC " (?spell-save-dc ::char5e/int) " CON save")})
                (mod5e/reaction
                 {:name "Acid Spray"
                  :page 12
                  :source ua-mystic-kw
                  :qualifier "2 psi"
                  :summary "if you take piercing or slashing damage, creatures within 5 ft. take 2d6 acid damage"})
                (mod5e/dependent-trait
                 {:name "Breath of the Black Dragon"
                  :page 11
                  :source ua-mystic-kw
                  :qualifier "5 psi"
                  :summary (str "exhale 60 x 5 ft. line of acid dealing 6d6 acid damage, half on successful DC " (?spell-save-dc ::char5e/int) " CON save")})
                (mod5e/dependent-trait
                 {:name "Breath of the Green Dragon"
                  :page 11
                  :source ua-mystic-kw
                  :qualifier "7 psi"
                  :summary (str "exhale 90 ft. cone of poison dealing 10d6 poison damage, half on successful DC " (?spell-save-dc ::char5e/int) " CON save")})]}
   {:name "Crown of Despair"
    :mystic-order :avatar
    :modifiers [(mod5e/trait-cfg
                 {:name "Crown of Despair: Psychic Focus"
                  :page 12
                  :source ua-mystic-kw
                  :summary "advantage on Intimidation checks"})
                (mod5e/action
                 {:name "Crowned in Sorrow"
                  :page 12
                  :source ua-mystic-kw
                  :qualifier "1-7 psi"
                  :range units5e/ft-60
                  :summary (str "deal 1d8 psychic damage per psi and target can't take reactions, half as much on successful DC " (?spell-save-dc ::char5e/int) " CHA save")})
                (mod5e/action
                 {:name "Call to Inaction"
                  :page 12
                  :source ua-mystic-kw
                  :qualifier "2 psi"
                  :range units5e/ft-30
                  :duration units5e/conc-minutes-10
                  :summary (str "if you spent 1 min conversing with a creature, charm to incapacitate it unless if succeeds on a DC " (?spell-save-dc ::char5e/int) " WIS save")})
                (mod5e/action
                 {:name "Visions of Despair"
                  :page 12
                  :source ua-mystic-kw
                  :qualifier "3 psi"
                  :range units5e/ft-60
                  :summary (str "Deal 3d6 + 1d6 per additional psi psychic damage to a creature and it's speed becomes 0 unless  it succeeds on DC " (?spell-save-dc ::char5e/int) " CHA save")})
                (mod5e/action
                 {:name "Dolorous Mind"
                  :page 12
                  :source ua-mystic-kw
                  :qualifier "5 psi"
                  :duration units5e/conc-hours-1
                  :range units5e/ft-60
                  :summary (str "incapaciate a creature on failed DC " (?spell-save-dc ::char5e/int) " CHA save")})]}
   {:name "Crown of Disgust"
    :mystic-order :avatar
    :modifiers [(mod5e/trait-cfg
                 {:name "Crown of Disgust: Psychic Focus"
                  :page 13
                  :source ua-mystic-kw
                  :range units5e/ft-5
                  :summary "area around you is difficult terrain to creatures not immune to being frightened"})
                (mod5e/action
                 {:name "Eye of Horror"
                  :page 13
                  :source ua-mystic-kw
                  :qualifier "1-7 psi"
                  :range units5e/ft-60
                  :summary (str "deal 1d6 psychic damage per psi and target can't move closer to you until end of its next turn, half as much damage on successful DC " (?spell-save-dc ::char5e/int) " WIS save")})
                (mod5e/action
                 {:name "Wall of Repulsion"
                  :page 13
                  :source ua-mystic-kw
                  :qualifier "3 psi"
                  :range units5e/ft-60
                  :duration units5e/conc-minutes-10
                  :summary (str "create a 30 x 10 x 1 ft. wall, a creature must make a DC " (?spell-save-dc ::char5e/int) " WIS save to pass through")})
                (mod5e/action
                 {:name "Visions of Disgust"
                  :page 13
                  :source ua-mystic-kw
                  :qualifier "5 psi"
                  :range units5e/ft-60
                  :duration units5e/conc-minutes-1
                  :summary (str "deal 5d6 psychic damage at end of each of target's turns it takes 1d6 psychic damage for every creature within 5 ft of it, only half initial damage on successful DC " (?spell-save-dc ::char5e/int) " WIS save")})
                (mod5e/action
                 {:name "World of Horror"
                  :page 13
                  :source ua-mystic-kw
                  :qualifier "7 psi"
                  :duration units5e/conc-hours-1
                  :range units5e/ft-60
                  :summary (str "choose up to 6 creatures, they take 8d6 psychic damage and are frightened, half as much damage on failed DC " (?spell-save-dc ::char5e/int) " CHA save")})]}
   {:name "Crown of Rage"
    :mystic-order :avatar
    :modifiers [(mod5e/trait-cfg
                 {:name "Crown of Rage: Psychic Focus"
                  :page 13
                  :source ua-mystic-kw
                  :range units5e/ft-5
                  :summary "an enemy within 5 ft. of you has disadvantage on melee attacks on anyone other than you"})
                (mod5e/action
                 {:name "Primal Fury"
                  :page 13
                  :source ua-mystic-kw
                  :qualifier "1-7 psi"
                  :range units5e/ft-60
                  :summary (str "deal 1d6 psychic damage per psi and target must use reaction to move in a straight line toward it's nearest enemy unless it succeeds on a DC " (?spell-save-dc ::char5e/int) " CHA save")})
                (mod5e/action
                 {:name "Fighting Words"
                  :page 13
                  :source ua-mystic-kw
                  :qualifier "2 psi"
                  :duration units5e/conc-minutes-10
                  :summary (str "if you spent 1 minute conversing with a creature, charm it to cause it to attack another creature you describe or name unless it succeeds on a DC " (?spell-save-dc ::char5e/int) " WIS save")})
                (mod5e/bonus-action
                 {:name "Mindless Courage"
                  :page 13
                  :source ua-mystic-kw
                  :qualifier "2 psi"
                  :range units5e/ft-60
                  :summary (str "target must succeed on DC " (?spell-save-dc ::char5e/int) " WIS save or it can't move except toward it's nearest enemy")})
                (mod5e/bonus-action
                 {:name "Punishing Fury"
                  :page 14
                  :source ua-mystic-kw
                  :qualifier "5 psi"
                  :duration units5e/conc-hours-1
                  :range units5e/ft-60
                  :summary (str "target must succeed on DC " (?spell-save-dc ::char5e/int) " WIS save or, when it makes a melee attack, creatures within 5 ft. of it can use reaction to melee attack it")})]}
   {:name "Diminution"
    :mystic-order :immortal
    :modifiers [(mod5e/trait-cfg
                 {:name "Diminution: Psychic Focus"
                  :page 14
                  :source ua-mystic-kw
                  :summary "advantage on Stealth checks"})
                (mod5e/bonus-action
                 {:name "Miniature Form"
                  :page 14
                  :source ua-mystic-kw
                  :qualifier "2 psi"
                  :summary "become tiny and gain +5 to Stealth checks"})
                (mod5e/bonus-action
                 {:name "Toppling Shift"
                  :page 14
                  :source ua-mystic-kw
                  :qualifier "2 psi"
                  :range units5e/ft-5
                  :summary (str "a creature must succed on DC " (?spell-save-dc ::char5e/int) " STR save or be knocked prone")})
                (mod5e/reaction
                 {:name "Sudden Shift"
                  :page 14
                  :source ua-mystic-kw
                  :qualifier "5 psi"
                  :summary "when your are hit by attack, cause the attack to miss and move 5 ft. away without provoking opportunity attack"})
                (mod5e/bonus-action
                 {:name "Microscopic Form"
                  :page 14
                  :source ua-mystic-kw
                  :qualifier "7 psi"
                  :duration units5e/conc-hours-1
                  :range units5e/ft-60
                  :summary "Become smaller than Tiny, gain +10 to Stealth checks and +5 to AC."})]}
   {:name "Giant Growth"
    :mystic-order :immortal
    :modifiers [(mod5e/trait-cfg
                 {:name "Giant Growth: Psychic Focus"
                  :page 14
                  :source ua-mystic-kw
                  :summary "your reach increases by 5 ft"})
                (mod5e/bonus-action
                 {:name "Ogre Form"
                  :page 14
                  :source ua-mystic-kw
                  :qualifier "2 psi"
                  :duration units5e/conc-hours-1
                  :summary "+10 temp HPs, +1d4 bludgeoning damage on melee attacks, +5 ft. reach, become Large"})
                (mod5e/bonus-action
                 {:name "Giant Form"
                  :page 14
                  :source ua-mystic-kw
                  :qualifier "7 psi"
                  :duration units5e/conc-hours-1
                  :summary "+30 temp HPs, +2d6 bludgeoning damage on melee attacks, +10 ft. reach, become Huge"})]}
   {:name "Intellect Fortress"
    :mystic-order :awakened
    :modifiers [(mod5e/trait-cfg
                 {:name "Intellect Fortress: Psychic Focus"
                  :page 14
                  :source ua-mystic-kw
                  :summary "resistance to psychic damage"})
                (mod5e/reaction
                 {:name "Psychic Backlash"
                  :page 14
                  :source ua-mystic-kw
                  :qualifier "2 psi"})
                (mod5e/reaction
                 {:name "Psychic Parry"
                  :page 14
                  :source ua-mystic-kw
                  :qualifier "1-7 psi"})
                (mod5e/action
                 {:name "Psychic Redoubt"
                  :page 14
                  :source ua-mystic-kw
                  :qualifier "5 psi"})]}
   {:name "Iron Durability"
    :mystic-order :immortal
    :modifiers [(mod5e/trait-cfg
                 {:name "Iron Durability: Psychic Focus"
                  :page 15
                  :source ua-mystic-kw
                  :summary "+1 AC bonus"})
                (mod5e/reaction
                 {:name "Iron Hide"
                  :page 15
                  :source ua-mystic-kw
                  :qualifier "1-7 psi"
                  :duration units5e/turns-1
                  :summary "+1 AC bonus per psi"})
                (mod5e/bonus-action
                 {:name "Steel Hide"
                  :page 15
                  :source ua-mystic-kw
                  :qualifier "2 psi"
                  :duration units5e/turns-1
                  :summary "gain resistance to bludgeoning, piercing, and slashing damage"})
                (mod5e/action
                 {:name "Iron Resistance"
                  :page 15
                  :source ua-mystic-kw
                  :qualifier "7 psi"
                  :duration units5e/conc-hours-1
                  :summary "gain resistance to bludgeoning, piercing, and slashing damage"})]}
   {:name "Mantle of Awe"
    :mystic-order :awakened
    :modifiers [(mod5e/dependent-trait
                 {:name "Mantle of Awe: Psychic Focus"
                  :page 15
                  :source ua-mystic-kw
                  :summary (str "gain a " (max 1 (/ (?ability-bonuses ::char5e/int) 2)) " bonus to CHA checks")})
                (mod5e/action
                 {:name "Charming Presence"
                  :page 15
                  :source ua-mystic-kw
                  :qualifier "1-7 psi"})
                (mod5e/action
                 {:name "Center of Attention"
                  :page 15
                  :source ua-mystic-kw
                  :qualifier "2 psi"
                  :duration units5e/conc-hours-1})
                (mod5e/action
                 {:name "Invoke Awe"
                  :page 15
                  :source ua-mystic-kw
                  :qualifier "7 psi"
                  :duration units5e/conc-minutes-10})]}
   {:name "Mantle of Command"
    :mystic-order :avatar
    :modifiers [(mod5e/dependent-trait
                 {:name "Mantle of Command: Psychic Focus"
                  :page 15
                  :source ua-mystic-kw})
                (mod5e/bonus-action
                 {:name "Coordinated Movement"
                  :page 15
                  :source ua-mystic-kw
                  :qualifier "2 psi"})
                (mod5e/action
                 {:name "Commander's Sight"
                  :page 15
                  :source ua-mystic-kw
                  :qualifier "2 psi"
                  :duration units5e/rounds-1})
                (mod5e/action
                 {:name "Command to Strike"
                  :page 15
                  :source ua-mystic-kw
                  :qualifier "3 psi"})
                (mod5e/action
                 {:name "Strategic Mind"
                  :page 15
                  :source ua-mystic-kw
                  :qualifier "5 psi"
                  :duration units5e/conc-hours-1})
                (mod5e/action
                 {:name "Overwhelming Attack"
                  :page 16
                  :source ua-mystic-kw
                  :qualifier "7 psi"})]}
   {:name "Mantle of Courage"
    :mystic-order :avatar
    :modifiers [(mod5e/dependent-trait
                 {:name "Mantle of Courage: Psychic Focus"
                  :page 16
                  :source ua-mystic-kw})
                (mod5e/bonus-action
                 {:name "Incite Courage"
                  :page 16
                  :source ua-mystic-kw
                  :qualifier "2 psi"})
                (mod5e/bonus-action
                 {:name "Aura of Victory"
                  :page 16
                  :source ua-mystic-kw
                  :qualifier "1-7 psi"
                  :duration units5e/conc-minutes-10})
                (mod5e/bonus-action
                 {:name "Pillar of Confidence"
                  :page 16
                  :source ua-mystic-kw
                  :qualifier "6 psi"
                  :duration units5e/rounds-1})]}
   {:name "Mantle of Fear"
    :mystic-order :avatar
    :modifiers [(mod5e/dependent-trait
                 {:name "Mantle of Fear: Psychic Focus"
                  :page 16
                  :source ua-mystic-kw
                  :summary "gain advantage on Intimidation checks"})
                (mod5e/bonus-action
                 {:name "Incite Fear"
                  :page 16
                  :source ua-mystic-kw
                  :qualifier "2 psi"
                  :duration units5e/conc-hours-1})
                (mod5e/bonus-action
                 {:name "Unsettling Aura"
                  :page 16
                  :source ua-mystic-kw
                  :qualifier "3 psi"
                  :duration units5e/conc-hours-1})
                (mod5e/action
                 {:name "Incite Panic"
                  :page 16
                  :source ua-mystic-kw
                  :qualifier "5 psi"
                  :duration units5e/conc-hours-1})]}
   {:name "Mantle of Fury"
    :mystic-order :avatar
    :modifiers [(mod5e/dependent-trait
                 {:name "Mantle of Fury: Psychic Focus"
                  :page 16
                  :source ua-mystic-kw})
                (mod5e/bonus-action
                 {:name "Incite Fury"
                  :page 16
                  :source ua-mystic-kw
                  :qualifier "2 psi"
                  :duration units5e/conc-hours-1})
                (mod5e/bonus-action
                 {:name "Mindless Charge"
                  :page 16
                  :source ua-mystic-kw
                  :qualifier "2 psi"})
                (mod5e/bonus-action
                 {:name "Aura of Bloodletting"
                  :page 16
                  :source ua-mystic-kw
                  :qualifier "3 psi"
                  :duration units5e/conc-hours-1})
                (mod5e/bonus-action
                 {:name "Overwhelming Fury"
                  :page 16
                  :source ua-mystic-kw
                  :qualifier "5 psi"
                  :duration units5e/conc-hours-1})]}
   {:name "Mantle of Joy"
    :mystic-order :avatar
    :modifiers [(mod5e/dependent-trait
                 {:name "Mantle of Fury: Psychic Focus"
                  :page 17
                  :source ua-mystic-kw
                  :summary "gain advantage on Persuasion checks"})
                (mod5e/bonus-action
                 {:name "Soothing Presence"
                  :page 17
                  :source ua-mystic-kw
                  :qualifier "1-7 psi"})
                (mod5e/bonus-action
                 {:name "Conforting Aura"
                  :page 17
                  :source ua-mystic-kw
                  :qualifier "2 psi"
                  :duration units5e/conc-hours-1})
                (mod5e/bonus-action
                 {:name "Aura of Jubilation"
                  :page 17
                  :source ua-mystic-kw
                  :qualifier "3 psi"
                  :duration units5e/conc-hours-1})
                (mod5e/bonus-action
                 {:name "Beacon of Recovery"
                  :page 17
                  :source ua-mystic-kw
                  :qualifier "5 psi"})]}
   {:name "Mastery of Air"
    :mystic-order :wu-jen
    :modifiers [(mod5e/dependent-trait
                 {:name "Mastery of Air: Psychic Focus"
                  :page 17
                  :source ua-mystic-kw
                  :summary "take no falling damage and ignore difficult terrain"})
                (mod5e/trait-cfg
                 {:name "Wind Step"
                  :page 17
                  :source ua-mystic-kw
                  :qualifier "1-7 psi"})
                (mod5e/action
                 {:name "Wind Stream"
                  :page 17
                  :source ua-mystic-kw
                  :qualifier "1-7 psi"})
                (mod5e/bonus-action
                 {:name "Cloak of Air"
                  :page 17
                  :source ua-mystic-kw
                  :qualifier "3 psi"
                  :duration units5e/conc-minutes-10})
                (mod5e/bonus-action
                 {:name "Wind Form"
                  :page 17
                  :source ua-mystic-kw
                  :qualifier "5 psi"
                  :duration units5e/conc-minutes-10})
                (mod5e/action
                 {:name "Misty Form"
                  :page 17
                  :source ua-mystic-kw
                  :qualifier "6 psi"
                  :duration units5e/conc-hours-1})
                (mod5e/action
                 {:name "Animate Air"
                  :page 17
                  :source ua-mystic-kw
                  :qualifier "7 psi"
                  :duration units5e/conc-hours-1})]}
   {:name "Mastery of Fire"
    :mystic-order :wu-jen
    :modifiers [(mod5e/trait-cfg
                 {:name "Mastery of Fire: Psychic Focus"
                  :page 17
                  :source ua-mystic-kw})
                (mod5e/action
                 {:name "Combustion"
                  :page 17
                  :source ua-mystic-kw
                  :qualifier "1-7 psi"
                  :duration units5e/conc-hours-1})
                (mod5e/action
                 {:name "Rolling Flame"
                  :page 17
                  :source ua-mystic-kw
                  :qualifier "3 psi"
                  :duration units5e/conc-hours-1})
                (mod5e/action
                 {:name "Detonation"
                  :page 18
                  :source ua-mystic-kw
                  :qualifier "5 psi"})
                (mod5e/bonus-action
                 {:name "Fire Form"
                  :page 18
                  :source ua-mystic-kw
                  :qualifier "5 psi"
                  :duration units5e/conc-hours-1})
                (mod5e/action
                 {:name "Animate Fire"
                  :page 18
                  :source ua-mystic-kw
                  :qualifier "6 psi"
                  :duration units5e/conc-hours-1})]}
   {:name "Mastery of Force"
    :mystic-order :wu-jen
    :modifiers [(mod5e/trait-cfg
                 {:name "Mastery of Force: Psychic Focus"
                  :page 18
                  :source ua-mystic-kw
                  :summary "Adavantage on STR checks"})
                (mod5e/action
                 {:name "Push"
                  :page 18
                  :source ua-mystic-kw
                  :qualifier "1-7 psi"})
                (mod5e/action
                 {:name "Move"
                  :page 18
                  :source ua-mystic-kw
                  :qualifier "2-7 psi"})
                (mod5e/action
                 {:name "Inertial Armor"
                  :page 18
                  :source ua-mystic-kw
                  :qualifier "2 psi"})
                (mod5e/action
                 {:name "Telekinetic Barrier"
                  :page 18
                  :source ua-mystic-kw
                  :qualifier "3 psi"
                  :duration units5e/conc-minutes-10})
                (mod5e/action
                 {:name "Grasp"
                  :page 18
                  :source ua-mystic-kw
                  :qualifier "3 psi"
                  :duration units5e/conc-hours-1})]}
   {:name "Mastery of Ice"
    :mystic-order :wu-jen
    :modifiers [(mod5e/trait-cfg
                 {:name "Mastery of Ice: Psychic Focus"
                  :page 19
                  :source ua-mystic-kw
                  :summary "Resistance to cold damage"})
                (mod5e/action
                 {:name "Ice Spike"
                  :page 19
                  :source ua-mystic-kw
                  :qualifier "1-7 psi"})
                (mod5e/action
                 {:name "Ice Sheet"
                  :page 19
                  :source ua-mystic-kw
                  :qualifier "2 psi"})
                (mod5e/bonus-action
                 {:name "Frozen Santuary"
                  :page 19
                  :source ua-mystic-kw
                  :qualifier "3 psi"})
                (mod5e/action
                 {:name "Frozen Rain"
                  :page 19
                  :source ua-mystic-kw
                  :qualifier "5 psi"
                  :duration units5e/conc-hours-1})
                (mod5e/action
                 {:name "Ice Barrier"
                  :page 19
                  :source ua-mystic-kw
                  :qualifier "6 psi"
                  :duration units5e/conc-minutes-10})]}
   {:name "Mastery of Light and Darkness"
    :mystic-order :wu-jen
    :modifiers [(mod5e/trait-cfg
                 {:name "Mastery of Light and Darkness: Psychic Focus"
                  :page 19
                  :source ua-mystic-kw
                  :summary "Darkness within 30 ft. doesn't affect your vision"})
                (mod5e/action
                 {:name "Darkness"
                  :page 19
                  :source ua-mystic-kw
                  :qualifier "1-7 psi"})
                (mod5e/action
                 {:name "Light"
                  :page 19
                  :source ua-mystic-kw
                  :qualifier "2 psi"
                  :duration units5e/conc-hours-1})
                (mod5e/action
                 {:name "Shadow Beasts"
                  :page 19
                  :source ua-mystic-kw
                  :qualifier "3 psi"
                  :duration units5e/conc-hours-1})
                (mod5e/action
                 {:name "Radiant Beam"
                  :page 19
                  :source ua-mystic-kw
                  :qualifier "5 psi"
                  :duration units5e/conc-hours-1})]}
   {:name "Mastery of Water"
    :mystic-order :wu-jen
    :modifiers [(mod5e/trait-cfg
                 {:name "Mastery of Water: Psychic Focus"
                  :page 19
                  :source ua-mystic-kw
                  :summary "Breathe underwater; swimming speed equal to walking speed"})
                (mod5e/action
                 {:name "Dessicate"
                  :page 20
                  :source ua-mystic-kw
                  :qualifier "1-7 psi"})
                (mod5e/action
                 {:name "Watery Grasp"
                  :page 20
                  :source ua-mystic-kw
                  :qualifier "2 psi"})
                (mod5e/action
                 {:name "Water Whip"
                  :page 20
                  :source ua-mystic-kw
                  :qualifier "3 psi"})
                (mod5e/action
                 {:name "Water Breathing"
                  :page 20
                  :source ua-mystic-kw
                  :qualifier "5 psi"})
                (mod5e/action
                 {:name "Water Sphere"
                  :page 20
                  :source ua-mystic-kw
                  :qualifier "6 psi"
                  :duration units5e/conc-hours-1})
                (mod5e/action
                 {:name "Animate Water"
                  :page 20
                  :source ua-mystic-kw
                  :qualifier "7 psi"
                  :duration units5e/conc-hours-1})]}
   {:name "Mastery of Weather"
    :mystic-order :wu-jen
    :modifiers [(mod5e/trait-cfg
                 {:name "Mastery of Weather: Psychic Focus"
                  :page 20
                  :source ua-mystic-kw
                  :summary "gain resistance to thunder and lightning damage"})
                (mod5e/action
                 {:name "Cloud Steps"
                  :page 20
                  :source ua-mystic-kw
                  :qualifier "1-7 psi"
                  :duration units5e/conc-minutes-10})
                (mod5e/action
                 {:name "Hungry Lighting"
                  :page 20
                  :source ua-mystic-kw
                  :qualifier "1-7 psi"})
                (mod5e/action
                 {:name "Wall of Clouds"
                  :page 20
                  :source ua-mystic-kw
                  :qualifier "2 psi"
                  :duration units5e/conc-minutes-10})
                (mod5e/action
                 {:name "Water Breathing"
                  :page 20
                  :source ua-mystic-kw
                  :qualifier "5 psi"})
                (mod5e/action
                 {:name "Whirlwind"
                  :page 20
                  :source ua-mystic-kw
                  :qualifier "2 psi"})
                (mod5e/action
                 {:name "Lightning Leap"
                  :page 20
                  :source ua-mystic-kw
                  :qualifier "5 psi"})
                (mod5e/action
                 {:name "Wall of Thunder"
                  :page 21
                  :source ua-mystic-kw
                  :qualifier "6 psi"
                  :duration units5e/conc-minutes-10})
                (mod5e/action
                 {:name "Thunder Clap"
                  :page 21
                  :source ua-mystic-kw
                  :qualifier "7 psi"})]}
   {:name "Mastery of Wood and Earth"
    :mystic-order :wu-jen
    :modifiers [(mod5e/trait-cfg
                 {:name "Mastery of Wood and Earth: Psychic Focus"
                  :page 21
                  :source ua-mystic-kw
                  :summary "+1 AC bonus"})
                (mod5e/action
                 {:name "Animate Weapon"
                  :page 21
                  :source ua-mystic-kw
                  :qualifier "1-7 psi"})
                (mod5e/action
                 {:name "Warp Weapon"
                  :page 21
                  :source ua-mystic-kw
                  :qualifier "2 psi"})
                (mod5e/action
                 {:name "Warp Armor"
                  :page 21
                  :source ua-mystic-kw
                  :qualifier "3 psi"})
                (mod5e/action
                 {:name "Water Breathing"
                  :page 21
                  :source ua-mystic-kw
                  :qualifier "5 psi"})
                (mod5e/action
                 {:name "Wall of Wood"
                  :page 21
                  :source ua-mystic-kw
                  :qualifier "3 psi"
                  :duration units5e/conc-hours-1})
                (mod5e/bonus-action
                 {:name "Armored Form"
                  :page 21
                  :source ua-mystic-kw
                  :qualifier "6 psi"
                  :duration units5e/conc-hours-1})
                (mod5e/action
                 {:name "Animate Earth"
                  :page 21
                  :source ua-mystic-kw
                  :qualifier "7 psi"
                  :duration units5e/conc-hours-1})]}
   {:name "Nomadic Arrow"
    :mystic-order :nomad
    :modifiers [(mod5e/trait-cfg
                 {:name "Nomadic Arrow: Psychic Focus"
                  :page 21
                  :source ua-mystic-kw})
                (mod5e/bonus-action
                 {:name "Speed Dart"
                  :page 21
                  :source ua-mystic-kw
                  :qualifier "1-7 psi"})
                (mod5e/reaction
                 {:name "Seeking Missile"
                  :page 21
                  :source ua-mystic-kw
                  :qualifier "2 psi"})
                (mod5e/action
                 {:name "Faithful Archer"
                  :page 21
                  :source ua-mystic-kw
                  :qualifier "5 psi"
                  :duration units5e/conc-hours-1})]}
   {:name "Nomadic Chameleon"
    :mystic-order :nomad
    :modifiers [(mod5e/trait-cfg
                 {:name "Nomadic Chameleon: Psychic Focus"
                  :page 22
                  :source ua-mystic-kw
                  :summary "gain advantage on stealth checks"})
                (mod5e/action
                 {:name "Chameleon"
                  :page 22
                  :source ua-mystic-kw
                  :qualifier "2 psi"})
                (mod5e/bonus-action
                 {:name "Step from Sight"
                  :page 22
                  :source ua-mystic-kw
                  :qualifier "3 psi"
                  :duration units5e/conc-hours-1})
                (mod5e/bonus-action
                 {:name "Enduring Invisibility"
                  :page 22
                  :source ua-mystic-kw
                  :qualifier "7 psi"
                  :duration units5e/conc-hours-1})]}
   {:name "Nomadic Mind"
    :mystic-order :nomad
    :modifiers [(mod5e/trait-cfg
                 {:name "Nomadic Mind: Psychic Focus"
                  :page 22
                  :source ua-mystic-kw})
                (mod5e/trait-cfg
                 {:name "Wandering Mind"
                  :page 22
                  :source ua-mystic-kw
                  :qualifier "2-6 psi"
                  :duration units5e/conc-minutes-10})
                (mod5e/trait-cfg
                 {:name "Find Creature"
                  :page 22
                  :source ua-mystic-kw
                  :qualifier "2 psi"
                  :duration units5e/conc-hours-1})
                (mod5e/trait-cfg
                 {:name "Item Lore"
                  :page 22
                  :source ua-mystic-kw
                  :qualifier "3 psi"
                  :duration units5e/conc-hours-1})
                (mod5e/action
                 {:name "Psychic Speech"
                  :page 22
                  :source ua-mystic-kw
                  :qualifier "5 psi"})
                (mod5e/action
                 {:name "Wandering Eye"
                  :page 22
                  :source ua-mystic-kw
                  :qualifier "6 psi"
                  :duration units5e/conc-hours-1})
                (mod5e/action
                 {:name "Phasing Eye"
                  :page 22
                  :source ua-mystic-kw
                  :qualifier "7 psi"
                  :duration units5e/conc-hours-1})]}
   {:name "Nomadic Step"
    :mystic-order :nomad
    :modifiers [(mod5e/trait-cfg
                 {:name "Nomadic Step: Psychic Focus"
                  :page 22
                  :source ua-mystic-kw})
                (mod5e/bonus-action
                 {:name "Step of a Dozen Paces"
                  :page 22
                  :source ua-mystic-kw
                  :qualifier "1-7 psi"})
                (mod5e/action
                 {:name "Nomadic Anchor"
                  :page 23
                  :source ua-mystic-kw
                  :qualifier "1 psi"})
                (mod5e/reaction
                 {:name "Defensive Step"
                  :page 23
                  :source ua-mystic-kw
                  :qualifier "2 psi"})
                (mod5e/bonus-action
                 {:name "There and Back Again"
                  :page 23
                  :source ua-mystic-kw
                  :qualifier "2 psi"})
                (mod5e/bonus-action
                 {:name "Transposition"
                  :page 23
                  :source ua-mystic-kw
                  :qualifier "3 psi"
                  :duration units5e/conc-hours-1})
                (mod5e/action
                 {:name "Baleful Transposition"
                  :page 23
                  :source ua-mystic-kw
                  :qualifier "5 psi"})
                (mod5e/action
                 {:name "Phantom Caravan"
                  :page 23
                  :source ua-mystic-kw
                  :qualifier "6 psi"})
                (mod5e/action
                 {:name "Nomadic Gate"
                  :page 23
                  :source ua-mystic-kw
                  :qualifier "7 psi"
                  :duration units5e/conc-hours-1})]}
   {:name "Precognition"
    :mystic-order :awakened
    :modifiers [(mod5e/trait-cfg
                 {:name "Precognition: Psychic Focus"
                  :page 23
                  :source ua-mystic-kw
                  :summary "advantage on initiative rolls"})
                (mod5e/bonus-action
                 {:name "Precognitive Hunch"
                  :page 23
                  :source ua-mystic-kw
                  :qualifier "2 psi"})
                (mod5e/reaction
                 {:name "All-Around Sight"
                  :page 23
                  :source ua-mystic-kw
                  :qualifier "3 psi"})
                (mod5e/action
                 {:name "Danger Sense"
                  :page 23
                  :source ua-mystic-kw
                  :qualifier "5 psi"
                  :duration units5e/conc-hours-8})
                (mod5e/trait-cfg
                 {:name "Victory Before Battle"
                  :page 23
                  :source ua-mystic-kw
                  :qualifier "7 psi"})]}
   {:name "Psionic Restoration"
    :mystic-order :immortal
    :modifiers [(mod5e/trait-cfg
                 {:name "Psionic Restoration: Psychic Focus"
                  :page 23
                  :source ua-mystic-kw})
                (mod5e/action
                 {:name "Mend Wounds"
                  :page 23
                  :source ua-mystic-kw
                  :qualifier "1-7 psi"})
                (mod5e/action
                 {:name "Restore Health"
                  :page 23
                  :source ua-mystic-kw
                  :qualifier "3 psi"})
                (mod5e/action
                 {:name "Restore Life"
                  :page 23
                  :source ua-mystic-kw
                  :qualifier "5 psi"})
                (mod5e/action
                 {:name "Restore Vigor"
                  :page 24
                  :source ua-mystic-kw
                  :qualifier "7 psi"})]}
   {:name "Psionic Weapon"
    :mystic-order :immortal
    :modifiers [(mod5e/trait-cfg
                 {:name "Psionic Weapon: Psychic Focus"
                  :page 24
                  :source ua-mystic-kw})
                (mod5e/bonus-action
                 {:name "Etereal Weapon"
                  :page 24
                  :source ua-mystic-kw
                  :qualifier "1 psi"})
                (mod5e/bonus-action
                 {:name "Lethal Strike"
                  :page 23
                  :source ua-mystic-kw
                  :qualifier "1-7 psi"})
                (mod5e/bonus-action
                 {:name "Augmented Weapon"
                  :page 23
                  :source ua-mystic-kw
                  :qualifier "5 psi"
                  :duration units5e/conc-minutes-10})]}
   {:name "Psychic Assault"
    :mystic-order :awakened
    :modifiers [(mod5e/trait-cfg
                 {:name "Psychic Assault: Psychic Focus"
                  :page 24
                  :source ua-mystic-kw
                  :summary "+2 bonus to damage from psionic talents that deal psychic damage"})
                (mod5e/action
                 {:name "Psionic Blast"
                  :page 24
                  :source ua-mystic-kw
                  :qualifier "1-7 psi"})
                (mod5e/action
                 {:name "Ego Whip"
                  :page 24
                  :source ua-mystic-kw
                  :qualifier "3 psi"})
                (mod5e/action
                 {:name "Id Insinuation"
                  :page 24
                  :source ua-mystic-kw
                  :qualifier "5 psi"})
                (mod5e/action
                 {:name "Psychic Blast"
                  :page 24
                  :source ua-mystic-kw
                  :qualifier "6 psi"})
                (mod5e/action
                 {:name "Psychic Crush"
                  :page 24
                  :source ua-mystic-kw
                  :qualifier "7 psi"})]}
   {:name "Psychic Disruption"
    :mystic-order :awakened
    :modifiers [(mod5e/trait-cfg
                 {:name "Psychic Disruption: Psychic Focus"
                  :page 24
                  :source ua-mystic-kw
                  :summary "advantage on Deception checks"})
                (mod5e/action
                 {:name "Distracting Haze"
                  :page 24
                  :source ua-mystic-kw
                  :qualifier "1-7 psi"
                  :duration units5e/conc-minutes-1})
                (mod5e/action
                 {:name "Daze"
                  :page 25
                  :source ua-mystic-kw
                  :qualifier "3 psi"})
                (mod5e/action
                 {:name "Mind Storm"
                  :page 25
                  :source ua-mystic-kw
                  :qualifier "5 psi"})]}
   {:name "Psychic Inquisition"
    :mystic-order :awakened
    :modifiers [(mod5e/trait-cfg
                 {:name "Psychic Inquisition: Psychic Focus"
                  :page 24
                  :source ua-mystic-kw})
                (mod5e/action
                 {:name "Hammer of Inquisition"
                  :page 24
                  :source ua-mystic-kw
                  :qualifier "1-7 psi"})
                (mod5e/action
                 {:name "Forceful Query"
                  :page 25
                  :source ua-mystic-kw
                  :qualifier "2 psi"})
                (mod5e/trait-cfg
                 {:name "Ransack Mind"
                  :page 25
                  :source ua-mystic-kw
                  :qualifier "5 psi"
                  :duration units5e/conc-hours-1})
                (mod5e/trait-cfg
                 {:name "Phantom Idea"
                  :page 25
                  :source ua-mystic-kw
                  :qualifier "6 psi"
                  :duration units5e/conc-hours-1})]}
   {:name "Psychic Phantoms"
    :mystic-order :awakened
    :modifiers [(mod5e/trait-cfg
                 {:name "Psychic Phantoms: Psychic Focus"
                  :page 24
                  :source ua-mystic-kw
                  :summary "gain advantage on Deception checks"})
                (mod5e/action
                 {:name "Distracting Figment"
                  :page 24
                  :source ua-mystic-kw
                  :qualifier "1-7 psi"})
                (mod5e/action
                 {:name "Phantom Foe"
                  :page 25
                  :source ua-mystic-kw
                  :qualifier "3 psi"
                  :duration units5e/conc-minutes-1})
                (mod5e/action
                 {:name "Phantom Betrayal"
                  :page 26
                  :source ua-mystic-kw
                  :qualifier "5 psi"
                  :duration units5e/conc-minutes-1})
                (mod5e/action
                 {:name "Phantom Riches"
                  :page 26
                  :source ua-mystic-kw
                  :qualifier "7 psi"
                  :duration units5e/conc-minutes-1})]}
   {:name "Telepathic Contact"
    :mystic-order :awakened
    :modifiers [(mod5e/trait-cfg
                 {:name "Telepathic Contact: Psychic Focus"
                  :page 26
                  :source ua-mystic-kw})
                (mod5e/action
                 {:name "Extracting Query"
                  :page 26
                  :source ua-mystic-kw
                  :qualifier "2 psi"})
                (mod5e/action
                 {:name "Occluded Mind"
                  :page 26
                  :source ua-mystic-kw
                  :qualifier "2 psi"})
                (mod5e/action
                 {:name "Broken Will"
                  :page 26
                  :source ua-mystic-kw
                  :qualifier "5 psi"})
                (mod5e/action
                 {:name "Psychic Grip"
                  :page 26
                  :source ua-mystic-kw
                  :qualifier "6 psi"
                  :duration units5e/conc-minutes-1})
                (mod5e/action
                 {:name "Psychic Domination"
                  :page 26
                  :source ua-mystic-kw
                  :qualifier "7 psi"
                  :duration units5e/conc-minutes-1})]}
   {:name "Third Eye"
    :mystic-order :nomad
    :modifiers [(mod5e/trait-cfg
                 {:name "Third Eye: Psychic Focus"
                  :page 26
                  :source ua-mystic-kw
                  :summary "gain darkvision 60 ft. or +10 ft. if you already have darkvision"})
                (mod5e/bonus-action
                 {:name "Tremor Sense"
                  :page 26
                  :source ua-mystic-kw
                  :qualifier "2 psi"
                  :duration units5e/conc-minutes-1})
                (mod5e/bonus-action
                 {:name "Unwavering Eye"
                  :page 26
                  :source ua-mystic-kw
                  :qualifier "2 psi"})
                (mod5e/bonus-action
                 {:name "Piercing Sight"
                  :page 26
                  :source ua-mystic-kw
                  :qualifier "3 psi"
                  :duration units5e/conc-minutes-1})
                (mod5e/bonus-action
                 {:name "Truesight"
                  :page 26
                  :source ua-mystic-kw
                  :qualifier "5 psi"
                  :duration units5e/conc-minutes-1})]}])

(def psionic-talents
  [{:name "Beacon"
    :page 27
    :type :bonus-action
    :duration units5e/hours-1
    :summary "Create a bright light with 20 ft radius"}
   {:name "Blade Meld"
    :page 27
    :type :bonus-action
    :summary "A one-handed weapon becomes part of your hand"}
   {:name "Blind Spot"
    :modifiers [(mod5e/action
                 {:name "Blind Spot"
                  :page 27
                  :souce ua-mystic-kw
                  :duration units5e/rounds-1
                  :summary (str "Become invisible to 1 creature unless it succeeds on a DC " (?spell-save-dc ::char5e/int) " WIS save.")})]
    :summary "Become invisible to 1 creature"}
   {:name "Delusion"
    :page 27
    :type :action
    :summary "Create a sound or image in the mind of 1 creature"}
   {:name "Energy Beam"
    :modifiers [(mod5e/action
                 {:name "Energy Beam"
                  :page 27
                  :source ua-mystic-kw
                  :range units5e/ft-90
                  :summary (str "A creature must succeed on a DC "
                                (?spell-save-dc ::char5e/int)
                                " WIS save or take "
                                (mod5e/level-val
                                 (?class-level :mystic)
                                 {5 2
                                  11 3
                                  17 4
                                  :default 1})
                                "d8 thunder, cold, acid, or fire damage")})]
    :summary "target a creature with an energy beam"}
   {:name "Light Step"
    :page 27
    :type :bonus-action
    :duration units5e/turns-1
    :summary "+10 walking speed; stand up without movement cost"}
   {:name "Mind Meld"
    :page 27
    :type :bonus-action
    :duration units5e/turns-1
    :range units5e/ft-120
    :summary "communicate telepathically with on creature"}
   {:name "Mind Slam"
    :modifiers [(mod5e/action
                 {:name "Mind Slam"
                  :page 28
                  :source ua-mystic-kw
                  :range units5e/ft-60
                  :summary (str "A creature must succeed on a DC "
                                (?spell-save-dc ::char5e/int)
                                " CON save or take "
                                (mod5e/level-val
                                 (?class-level :mystic)
                                 {5 2
                                  11 3
                                  17 4
                                  :default 1})
                                "d6 force damage and be knocked prone if Large or smaller")})]
    :summary "metally slam a creature"}
   {:name "Mind Thrust"
    :modifiers [(mod5e/action
                 {:name "Mind Thrust"
                  :page 28
                  :source ua-mystic-kw
                  :range units5e/ft-120
                  :summary (str "A creature must succeed on a DC "
                                (?spell-save-dc ::char5e/int)
                                " INT save or take "
                                (mod5e/level-val
                                 (?class-level :mystic)
                                 {5 2
                                  11 3
                                  17 4
                                  :default 1})
                                "d10 psychic damage")})]
    :summary "metally slam a creature"}
   {:name "Mystic Charm"
    :modifiers [(mod5e/action
                 {:name "Mystic Charm"
                  :page 28
                  :source ua-mystic-kw
                  :range units5e/ft-120
                  :duration units5e/turns-1
                  :summary (str "A creature must succeed on a DC "
                                (?spell-save-dc ::char5e/int)
                                " CHA save or be charmed by you")})]
    :summary "charm a creature"}
   {:name "Mystic Hand"
    :page 28
    :type :action
    :duration units5e/turns-1
    :range units5e/ft-30
    :summary "manipulate or move an object"}
   {:name "Psychic Hammer"
    :modifiers [(mod5e/action
                 {:name "Psychic Hammer"
                  :page 28
                  :source ua-mystic-kw
                  :range units5e/ft-120
                  :summary (str "A creature must succeed on a DC "
                                (?spell-save-dc ::char5e/int)
                                " STR save or take "
                                (mod5e/level-val
                                 (?class-level :mystic)
                                 {5 2
                                  11 3
                                  17 4
                                  :default 1})
                                "d6 force damage and if it is Large or smaller you can move it up to 10 ft.")})]
    :summary "psychic attack"}])

(def psionic-talents-selection
  (t/selection-cfg
   {:name "Psionic Talents"
    :tags #{:class}
    :ref [:class :mystic :psionic-talents]
    :multiselect? true
    :options (map
              (fn [{:keys [name modifiers] :as cfg}]
                (t/option-cfg
                 (if modifiers
                   cfg
                   {:name name
                    :modifiers [(mod5e/trait-cfg
                                 cfg)]})))
              psionic-talents)}))

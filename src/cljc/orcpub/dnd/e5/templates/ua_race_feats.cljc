(ns orcpub.dnd.e5.templates.ua-race-feats
  (:require [orcpub.dnd.e5.modifiers :as mod5e]
            [orcpub.dnd.e5.options :as opt5e]
            [orcpub.dnd.e5.character :as char5e]
            [orcpub.dnd.e5.units :as units5e]
            [orcpub.template :as t]
            [orcpub.common :as common]
            [re-frame.core :refer [subscribe]]
            [clojure.string :as s]))

(def grudge-bearer-foe-selection
  (t/selection-cfg
   {:name "Foe"
    :tags #{:feats}
    :options (map
              (fn [[k v]]
                (t/option-cfg
                 (let [nm (if (map? v)
                            (:name v)
                            (s/capitalize (name k)))]
                   {:name nm 
                    :modifiers [(mod5e/trait-cfg
                                 {:name "Grudge Bearer Feat"
                                  :page 3
                                  :source :ua-race-feats
                                  :summary (str "Your chosen foe type is " nm ". Against them, you have advantage on attack in the first round of combat, they take opportunity attacks on you with disadvantage, and whenever you make an INT check to recall info about them you add double your prof bonus.")})]})))
              (concat
               (seq opt5e/favored-enemy-types)
               (seq opt5e/humanoid-enemies)))}))

(def ua-race-feats-plugin
  {:name "Unearthed Arcana: Feats for Races"
   :key :ua-race-feats
   :feat-options? true
   :selections [(opt5e/feat-selection-2
                 {:options (map
                            (fn [o]
                              (update o ::t/modifiers conj opt5e/ua-al-illegal))
                            [(opt5e/feat-option
                              {:name "Barbed Hide"
                               :page 1
                               :source :ua-race-feats
                               :prereqs [(opt5e/race-prereq "Tiefling")]
                               :summary "Increase CON or CHA by 1; protrude or retract barbs from your skin that deal 1d6 piercing while grappling."
                               :selections [(opt5e/ability-increase-selection [::char5e/con ::char5e/cha] 1 false)]
                               :modifiers [(mod5e/bonus-action
                                            {:name "Barbed Hide"
                                             :page 1
                                             :source :ua-race-feats
                                             :summary "Protrude or retract barbs from your skin that deal 1d6 piercing while grappling."})
                                           (opt5e/skill-prof-or-expertise :intimidation :ua-barbed-hide)]})
                             (opt5e/feat-option
                              {:name "Bountiful Luck"
                               :page 1
                               :source :ua-race-feats
                               :prereqs [(opt5e/race-prereq "Halfling")]
                               :summary "Allow and ally within 30 ft. to reroll a 1 on an attack roll, ability check, or save."})
                             (opt5e/feat-option
                              {:name "Critter Friend"
                               :page 1
                               :source :ua-race-feats
                               :summary "Can cast 'animal handling', 'speak with animals', and 'animal friendship'"
                               :prereqs [(opt5e/subrace-prereq "Gnome" "Forest Gnome")]
                               :modifiers [(opt5e/skill-prof-or-expertise :animal-handling :critter-friend)
                                           (mod5e/spells-known 1 :speak-with-animals ::char5e/int "Forest Gnome" 0 "at will")
                                           (mod5e/spells-known 1 :animal-friendship ::char5e/int "Forest Gnome" 0 "once per long rest")]})
                             (opt5e/feat-option
                              {:name "Dragon Fear"
                               :page 2
                               :source :ua-race-feats
                               :prereqs [(opt5e/race-prereq "Dragonborn")]
                               :selections [(opt5e/ability-increase-selection [::char5e/str ::char5e/cha] 1 false)]
                               :summary "Increase STR or CHA by 1. Expend a breath weapon use and roar, causing creatures of your choice within 30 ft. and can hear you to make a WIS save. On failed save they become frightened of you for 1 min. Targets that take damage can repeat the save."
                               :exclude-trait? true
                               :modifiers [(mod5e/dependent-trait
                                            {:name "Dragon Fear"
                                             :page 2
                                             :source :ua-race-feats
                                             :summary (str "Expend a breath weapon use and roar, causing creatures of your choice within 30 ft. and can hear you to make a DC " (?spell-save-dc ::char5e/cha) " WIS save. On failed save they become frightened of you for 1 min. Targets that take damage can repeat the save.")})]})
                             (opt5e/feat-option
                              {:name "Dragon Hide"
                               :page 2
                               :source :ua-race-feats
                               :prereqs [(opt5e/race-prereq "Dragonborn")]
                               :selections [(opt5e/ability-increase-selection [::char5e/str ::char5e/cha] 1 false)]
                               :exclude-trait? true
                               :summary "Increase STR or CHA by 1; deal 1d4 + STR mod slashing damage with an unarmed strike"
                               :modifiers [(mod5e/dependent-trait
                                            {:name "Dragon Hide"
                                             :page 2
                                             :source :ua-race-feats
                                             :summary (str "Deal 1d4 " (common/bonus-str (?ability-bonuses ::char5e/str)) " slashing damage with an unarmed strike")})
                                           (mod5e/ac-bonus-fn
                                            (fn [armor & [shield]]
                                              (if (and (nil? armor)
                                                       (nil? shield))
                                                1)))]})
                             (opt5e/feat-option
                              {:name "Dragon Wings"
                               :page 2
                               :source :ua-race-feats
                               :prereqs [(opt5e/race-prereq "Dragonborn")]
                               :summary "You have a flying speed of 20 ft. when you haven't exceeded carrying capacity and aren't wearing heavy armor."
                               :modifiers [(mod5e/flying-speed-override 20)]})
                             (opt5e/feat-option
                              {:name "Drow High Magic"
                               :page 2
                               :source :ua-race-feats
                               :summary "Can cast 'detect magic', 'levitate', and 'dispel magic'"
                               :prereqs [(opt5e/subrace-prereq "Elf" "Dark Elf (Drow)")]
                               :modifiers [(mod5e/spells-known 1 :detect-magic ::char5e/cha "Dark Elf" 0 "at will")
                                           (mod5e/spells-known 2 :levitate ::char5e/cha "Dark Elf" 0 "once per long rest")
                                           (mod5e/spells-known 3 :dispel-magic ::char5e/cha "Dark Elf" 0 "once per long rest")]})
                             (opt5e/feat-option
                              {:name "Dwarf Resilience"
                               :page 2
                               :source :ua-race-feats
                               :exclude-trait? true
                               :summary "Increase CON by 1; whenever you Dodge you can spend an HD, roll it, and heal that number of HPs plus your CON modifier" 
                               :prereqs [(opt5e/race-prereq "Dwarf")]
                               :modifiers [(mod5e/ability ::char5e/con 1)
                                           (mod5e/dependent-trait
                                            {:name "Dwarf Resilience"
                                             :page 1
                                             :source :ua-race-feats
                                             :summary (str "Whenever you Dodge you can spend an HD, roll it, and heal that number of HPs " (common/bonus-str (?ability-bonuses ::char5e/con)))})]})
                             (opt5e/feat-option
                              {:name "Elven Accuracy"
                               :page 2
                               :source :ua-race-feats
                               :summary "When you have advantage on attack roll, you can reroll one die"
                               :prereqs [(opt5e/race-prereq ["Elf" "Half-Elf"])]
                               :modifiers [(mod5e/ability ::char5e/dex 1)]})
                             (opt5e/feat-option
                              {:name "Everybody's Friend"
                               :page 2
                               :source :ua-race-feats
                               :prereqs [(opt5e/race-prereq ["Half-Elf"])]
                               :modifiers [(mod5e/ability ::char5e/cha 1)
                                           (opt5e/skill-prof-or-expertise :deception :everybodys-friend)
                                           (opt5e/skill-prof-or-expertise :persuasion :everybodys-friend)]})
                             (opt5e/feat-option
                              {:name "Fade Away"
                               :page 1
                               :source :ua-race-feats
                               :exclude-trait? true
                               :summary "Increase INT by 1; when you take damage, become invisible until end of your next turn."
                               :prereqs [(opt5e/race-prereq "Gnome")]
                               :modifiers [(mod5e/ability ::char5e/int 1)
                                           (mod5e/reaction
                                            {:name "Fade Away Feat"
                                             :page 2
                                             :source :ua-race-feats
                                             :frequency units5e/rests-1
                                             :summary "When you take damage, become invisible until end of your next turn."})]})
                             (opt5e/feat-option
                              {:name "Fey Teleportation"
                               :page 3
                               :source :ua-race-feats
                               :summary "Increase INT by 1; can cast 'misty step'"
                               :prereqs [(opt5e/subrace-prereq "Elf" "High Elf")]
                               :modifiers [(mod5e/ability ::char5e/int 1)
                                           (mod5e/spells-known 2 :misty-step ::char5e/int "High Elf" 0 "once per rest")]})
                             (opt5e/feat-option
                              {:name "Flames of Phlegethos"
                               :page 3
                               :source :ua-race-feats
                               :prereqs [(opt5e/race-prereq "Tiefling")]
                               :selections [(opt5e/ability-increase-selection [::char5e/int ::char5e/cha] 1 false)]
                               :summary "When you roll fire damage, may reroll a 1 once. Also, whenever you cast a spell that deals fire damage, you become surrounded by flames that shed 30 ft light and deal 1d4 fire damage to a creature within 5 ft. that hits you with melee attack."})
                             (opt5e/feat-option
                              {:name "Grudge Bearer"
                               :page 3
                               :source :ua-race-feats
                               :prereqs [(opt5e/race-prereq "Dwarf")]
                               :summary "Choose an enemy type. Against them, you have advantage on attack in the first round of combat, they take opportunity attacks on you with disadvantage, and whenever you make an INT check to recall info about them you add double your prof bonus."
                               :exclude-trait? true
                               :selections [(opt5e/ability-increase-selection [::char5e/str ::char5e/con ::char5e/wis] 1 false)
                                            grudge-bearer-foe-selection]})
                             (opt5e/feat-option
                              {:name "Human Determination"
                               :page 3
                               :source :ua-race-feats
                               :prereqs [(opt5e/race-prereq "Human")]
                               :exclude-trait? true
                               :summary "Increase an ability score by 1;"
                               :modifiers [(mod5e/trait-cfg
                                            {:name "Human Determination Feat; you may make an attack roll, ability check, or save with advantage"
                                             :page 3
                                             :source :ua-race-feats
                                             :frequency units5e/rests-1
                                             :summary "You may make an attack roll, ability check, or save with advantage."})]
                               :selections [(opt5e/ability-increase-selection char5e/ability-keys 1 false)]})
                             (opt5e/feat-option
                              {:name "Infernal Construction"
                               :page 3
                               :source :ua-race-feats
                               :prereqs [(opt5e/race-prereq "Tiefling")]
                               :summary "Increase CON by 1; resistance to cold and poison damage; and advantage on saves against being poisoned"
                               :modifiers [(mod5e/ability ::char5e/con 1)
                                           (mod5e/damage-resistance :cold)
                                           (mod5e/damage-resistance :poison)
                                           (mod5e/saving-throw-advantage [:poisoned])]})
                             (opt5e/feat-option
                              {:name "Orcish Aggression"
                               :exclude-trait? true
                               :prereqs [(opt5e/race-prereq "Half-Orc")]
                               :summary "Move up to your speed toward and enemy as a bonus action"
                               :modifiers [(mod5e/bonus-action
                                            {:name "Orcish Aggression Feat"
                                             :page 3
                                             :source :ua-race-feats
                                             :summary "Move up to your speed toward and enemy"})]})
                             (opt5e/feat-option
                              {:name "Orcish Fury"
                               :exclude-trait? true
                               :prereqs [(opt5e/race-prereq "Half-Orc")]
                               :summary "Increase STR or CON by 1; reroll one weapon damage die an additional time as extra damage; after using Relentless Endurance, make a weapon attack"
                               :selections [(opt5e/ability-increase-selection [::char5e/str ::char5e/con] 1 false)]
                               :modifiers [(mod5e/trait-cfg
                                            {:name "Orcish Fury Feat"
                                             :page 4
                                             :source :ua-race-feats
                                             :frequency units5e/rests-1
                                             :summary "Reroll one weapon damage die an additional time as extra damage"})
                                           (mod5e/reaction
                                            {:name "Orcish Fury Reaction"
                                             :page 4
                                             :source :ua-race-feats
                                             :summary "After using Relentless Endurance, make a weapon attack"})]})
                             (opt5e/feat-option
                              {:name "Prodigy"
                               :exclude-trait? true
                               :prereqs [(opt5e/race-prereq ["Half-Elf" "Human"])]
                               :summary "Increase an ability score by 1; select 1 skill proficiency, 1 tool proficiency, and 1 language proficiency"
                               :selections [(opt5e/ability-increase-selection char5e/ability-keys 1 false)
                                            (opt5e/skill-selection 1)
                                            (opt5e/tool-selection 1)
                                            (opt5e/language-selection opt5e/languages 1)]})
                             (opt5e/feat-option
                              {:name "Second Chance"
                               :exclude-trait? true
                               :prereqs [(opt5e/race-prereq "Halfling")]
                               :selections [(opt5e/ability-increase-selection [::char5e/dex ::char5e/con ::char5e/cha] 1 false)]
                               :summary "Increase DEX, CON, or CHA by 1; force a creature to reroll a hitting attack roll"
                               :modifiers [(mod5e/reaction
                                            {:name "Second Chance Feat"
                                             :page 4
                                             :source :ua-race-feats
                                             :frequency units5e/rests-1
                                             :summary "Force a creature to reroll a hitting attack roll"})]})
                             (opt5e/feat-option
                              {:name "Squat Nimbleness"
                               :exclude-trait? true
                               :summary "Increase STR or DEX by 1; gain prof or expertise in acrobatics or athletics; increase speed by 5"
                               :prereqs [(opt5e/race-prereq ["Dwarf" "Gnome" "Halfling"])]
                               :selections [(opt5e/ability-increase-selection [::char5e/dex ::char5e/str] 1 false)
                                            (opt5e/skill-or-expertise-selection 1 [:acrobatics :athletics] :squat-nimbleness)]
                               :modifiers [(mod5e/speed 5)]})
                             (opt5e/feat-option
                              {:name "Wonder Maker"
                               :page 4
                               :source :ua-race-feats
                               :summary "Increase DEX or INT by 1; double proficiency bonus with tinker's tools; additional device options for your Tinker trait: alarm, calculator, lifter, timekeeper, weather sensor"
                               :prereqs [(opt5e/subrace-prereq "Gnome" "Rock Gnome")]
                               :selections [(opt5e/ability-increase-selection [::char5e/dex ::char5e/int] 1 false)]
                               :modifiers [(mod5e/tool-expertise :tinkers-tools)]})
                             (opt5e/feat-option
                              {:name "Wood Elf Magic"
                               :page 3
                               :source :ua-race-feats
                               :summary "Choose a druid cantrip; learn 'longstrider' and 'pass without trace' spells"
                               :prereqs [(opt5e/subrace-prereq "Elf" "Wood Elf")]
                               :selections [(opt5e/druid-cantrip-selection "Wood Elf")]
                               :modifiers [(mod5e/spells-known 1 :longstrider ::char5e/wis "Wood Elf" 0 "once per rest")
                                           (mod5e/spells-known 2 :pass-without-trace ::char5e/wis "Wood Elf" 0 "once per rest")]})])})]})

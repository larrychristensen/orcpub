(ns orcpub.dnd.e5.templates.ua-cleric
  (:require [orcpub.dnd.e5.modifiers :as mod5e]
            [orcpub.dnd.e5.options :as opt5e]))

(def ua-cleric-cfg
  {:name "Cleric"
   :plugin? true
   :subclass-level 1
   :subclass-title "Divine Domain"
   :source :ua-cleric
   :subclasses [{:name "Forge Domain"
                 :source :ua-cleric
                 :modifiers [(mod5e/al-illegal "Forge Domain is not allowed")
                             (mod5e/armor-proficiency :heavy)
                             (opt5e/cleric-spell 1 :searing-smite 1)
                             (opt5e/cleric-spell 1 :shield 1)
                             (opt5e/cleric-spell 2 :heat-metal 3)
                             (opt5e/cleric-spell 2 :magic-weapon 3)
                             (opt5e/cleric-spell 3 :elemental-weapon 5)
                             (opt5e/cleric-spell 3 :protection-from-energy 5)
                             (opt5e/cleric-spell 4 :fabricate 7)
                             (opt5e/cleric-spell 4 :wall-of-fire 7)
                             (opt5e/cleric-spell 5 :animate-objects 9)
                             (opt5e/cleric-spell 5 :creation 9)]
                 :levels {8 {:modifiers [(opt5e/divine-strike "fire" 1 :ua-cleric)]}}
                 :traits [{:name "Blessings of the Forge"
                           :page 1
                           :source :ua-cleric}
                          {:name "Channel Divinity: Artisan's Blessing"
                           :page 1
                           :level 2
                           :source :ua-cleric}
                          {:name "Soul of the Forge"
                           :page 1
                           :source :ua-cleric
                           :level 6}
                          {:level 17
                           :name "Saint of the Forge"
                           :page 1
                           :source :ua-cleric}]}
                {:name "Grave Domain"
                 :source :ua-cleric
                 :modifiers [(mod5e/al-illegal "Grave Domain is not allowed")
                             (mod5e/armor-proficiency :heavy)
                             (opt5e/cleric-spell 1 :bane 1)
                             (opt5e/cleric-spell 1 :false-life 1)
                             (opt5e/cleric-spell 2 :gentle-repose 3)
                             (opt5e/cleric-spell 2 :ray-of-enfeeblement 3)
                             (opt5e/cleric-spell 3 :revivify 5)
                             (opt5e/cleric-spell 3 :vampiric-touch 5)
                             (opt5e/cleric-spell 4 :blight 7)
                             (opt5e/cleric-spell 4 :death-ward 7)
                             (opt5e/cleric-spell 5 :antilife-shell 9)
                             (opt5e/cleric-spell 5 :raise-dead 9)]
                 :levels {8 {:modifiers [(opt5e/divine-strike "necrotic" 1 :ua-cleric)]}}
                 :traits [{:name "Circle of Mortality"
                           :page 2
                           :source :ua-cleric}
                          {:name "Eyes of the Grave"
                           :page 2
                           :source :ua-cleric}
                          {:name "Channel Divinity: Path to the Grave"
                           :page 2
                           :level 2
                           :source :ua-cleric}
                          {:name "Sentinel at Death's Door"
                           :page 2
                           :source :ua-cleric
                           :level 6}
                          {:level 17
                           :name "Keeper of Souls"
                           :page 2
                           :source :ua-cleric}]}
                {:name "Protection Domain"
                 :source :ua-cleric
                 :modifiers [(mod5e/al-illegal "Protection Domain is not allowed")
                             (mod5e/armor-proficiency :heavy)
                             (opt5e/cleric-spell 1 :compelled-duel 1)
                             (opt5e/cleric-spell 1 :protection-from-evil-and-good 1)
                             (opt5e/cleric-spell 2 :aid 3)
                             (opt5e/cleric-spell 2 :protection-from-poison 3)
                             (opt5e/cleric-spell 3 :slow 5)
                             (opt5e/cleric-spell 3 :protection-from-energy 5)
                             (opt5e/cleric-spell 4 :otilukes-resilient-sphere 7)
                             (opt5e/cleric-spell 4 :guardian-of-faith 7)
                             (opt5e/cleric-spell 5 :antilife-shell 9)
                             (opt5e/cleric-spell 5 :wall-of-force 9)]
                 :levels {8 {:modifiers [(opt5e/divine-strike "radiant" 1 :ua-cleric)]}}
                 :traits [{:name "Shield of the Faithful"
                           :page 3
                           :source :ua-cleric}
                          {:name "Channel Divinity: Radiant Defense"
                           :page 3
                           :level 2
                           :source :ua-cleric}
                          {:name "Blessed Healer"
                           :page 3
                           :source :ua-cleric
                           :level 6}
                          {:level 17
                           :name "Indomitable Defense"
                           :page 3
                           :source :ua-cleric}]}]})

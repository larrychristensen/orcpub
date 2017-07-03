(ns orcpub.dnd.e5.templates.ua-gothic-heroes
  (:require [orcpub.common :as common]
            [orcpub.template :as t]
            [orcpub.modifiers :as mod]
            [orcpub.dnd.e5.modifiers :as mod5e]
            [orcpub.dnd.e5.character :as char5e]
            [orcpub.dnd.e5.options :as opt5e]
            [orcpub.dnd.e5.units :as units5e]
            [orcpub.dnd.e5.templates.ua-options :as ua-options]
            [orcpub.dnd.e5.spell-lists :as sl]))

(def fighter-option-cfg
  {:name "Fighter"
   :plugin? true
   :subclass-level 3
   :subclass-title "Martial Archetype"
   :source :ua-fighter
   :subclasses [{:name "Monster Hunter"
                 :modifiers [opt5e/ua-al-illegal]
                 :source :ua-gothic-heroes
                 :levels {3 {:selections [(t/selection-cfg
                                           {:name "Bonus Proficiencies"
                                            :tags #{:skill-profs :profs}
                                            :options [(t/option-cfg
                                                       {:name "Two Skills"
                                                        :selections [(opt5e/skill-selection
                                                                      [:arcana :history :insight :investigation :nature :perception]
                                                                      2)]})
                                                      (t/option-cfg
                                                       {:name "One Skill, One Tool"
                                                        :selections [(opt5e/skill-selection
                                                                      [:arcana :history :insight :investigation :nature :perception]
                                                                      2)
                                                                     (opt5e/tool-selection 1)]})]})
                                          (opt5e/language-selection
                                           (filter
                                            (comp #{:abyssal :celestial :infernal} :key)
                                            opt5e/languages)
                                           1)]
                             :modifiers [opt5e/ua-al-illegal
                                         (mod5e/trait-cfg
                                          {:name "Combat Superiority"
                                           :page 2
                                           :source :ua-gothic-heroes})
                                         (mod5e/trait-cfg
                                          {:name "Hunter's Mysticism"
                                           :page 2
                                           :source :ua-gothic-heroes})
                                         (mod5e/spells-known 1 :detect-magic nil "Fighter" 1 "ritual only")
                                         (mod5e/spells-known 1 :protection-from-evil-and-good nil "Fighter" 1 "once/long rest")]}
                          7 {:modifiers [(mod5e/trait-cfg
                                          {:name "Monster Slayer"
                                           :page 2
                                           :source :ua-gothic-heroes})]}
                          10 {:modifiers [(mod5e/trait-cfg
                                           {:name "Improved Combat Superiority"
                                            :page 2
                                            :source :ua-gothic-heroes})]}
                          18 {:modifiers [(mod5e/trait-cfg
                                           {:name "Relentless"
                                            :page 2
                                            :source :ua-gothic-heroes})]}}}]})

(def rogue-option-cfg
  {:name "Rogue"
   :subclass-level 3
   :subclass-title "Roguish Archetype"
   :subclasses [{:name "Inquisitive"
                 :source :ua-gothic-heroes
                 :levels {3 {:modifiers [(mod5e/trait-cfg
                                          {:name "Ear for Deceit"
                                           :page 3
                                           :source :ua-gothic-heroes})
                                         (mod5e/trait-cfg
                                          {:name "Eye for Detail"
                                           :page 3
                                           :source :ua-gothic-heroes})]}
                          13 {:modifiers [(mod5e/action
                                           {:name "Unerrign Eye"
                                            :page 3
                                            :source :ua-gothic-heroes})]}}
                 :traits [{:name "Steady Eye"
                           :level 9
                           :page 13
                           :source :ua-gothic-heroes}
                          {:name "Eye for Weakness"
                           :level 17
                           :page 3
                           :source :ua-gothic-heroes}]}]})

(def ua-gothic-heroes-plugin
  {:name "Unearthed Arcana: Gothic Heroes"
   :class-options? true
   :key :ua-gothic-heroes
   :selections [(opt5e/class-selection
                 {:options (map
                            opt5e/class-option
                            [rogue-option-cfg
                             fighter-option-cfg])})]})

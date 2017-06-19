(ns orcpub.dnd.e5.templates.ua-warlock-and-wizard
  (:require [orcpub.dnd.e5.options :as opt5e]
            [orcpub.dnd.e5.modifiers :as mod5e]
            [orcpub.dnd.e5.character :as char5e]
            [orcpub.template :as t]))

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

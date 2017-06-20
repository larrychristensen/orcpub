(ns orcpub.dnd.e5.templates.ua-fighter
  (:require [orcpub.dnd.e5.modifiers :as mod5e]
            [orcpub.dnd.e5.options :as opt5e]
            [orcpub.template :as t]
            [orcpub.dnd.e5.templates.ua-options :as ua-options]))

(defn rapid-strike [page]
  (mod5e/trait-cfg
   {:name "Rapid Strike"
    :page page
    :source :ua-fighter}))


(def fighter-option-cfg
  {:name "Fighter"
   :plugin? true
   :subclass-level 3
   :subclass-title "Martial Archetype"
   :subclasses [ua-options/arcane-archer-option-cfg
                {:name "Knight"
                 :levels {3 {:modifiers [(mod5e/trait-cfg
                                          {:name "Born to the Saddle"
                                           :page 2
                                           :source :ua-fighter})
                                         (mod5e/trait-cfg
                                          {:name "Implacable Mark"
                                           :page 2
                                           :source :ua-fighter})]}
                          7 {:modifiers [(mod5e/trait-cfg
                                          {:name "Noble Cavalry"
                                           :page 2
                                           :source :ua-fighter})]}
                          10 {:modifiers [(mod5e/trait-cfg
                                           {:name "Hold the Line"
                                            :page 2
                                            :source :ua-fighter})]}
                          15 {:modifiers [(rapid-strike 3)]}
                          18 {:modifiers [(mod5e/trait-cfg
                                           {:name "Defender's Blade"
                                            :page 3
                                            :source :ua-fighter})]}}}
                {:name "Samurai"
                 :levels {3 {:modifiers [(mod5e/trait-cfg
                                          {:name "Fighting Spirit"
                                           :page 3
                                           :source :ua-fighter})]}
                          7 {:modifiers [(mod5e/trait-cfg
                                          {:name "Elegant Courtier"
                                           :page 3
                                           :source :ua-fighter})]}
                          10 {:modifiers [(mod5e/trait-cfg
                                           {:name "Unbreakable Will"
                                            :page 3
                                            :source :ua-fighter})]}
                          15 {:modifiers [(rapid-strike 3)]}
                          18 {:modifiers [(mod5e/trait-cfg
                                           {:name "Strength Before Death"
                                            :page 3
                                            :source :ua-fighter})]}}}
                {:name "Sharpshooter"
                 :levels {3 {:modifiers [(mod5e/trait-cfg
                                          {:name "Steady Aim"
                                           :page 3
                                           :source :ua-fighter})]}
                          7 {:modifiers [(mod5e/trait-cfg
                                          {:name "Careful Eyes"
                                           :page 4
                                           :source :ua-fighter})]
                             :selections [(opt5e/skill-selection [:perception :investigation :survival] 1)]}
                          10 {:modifiers [(mod5e/trait-cfg
                                           {:name "Close Quarters Shooting"
                                            :page 3
                                            :source :ua-fighter})]}
                          15 {:modifiers [(rapid-strike 3)]}
                          18 {:modifiers [(mod5e/trait-cfg
                                           {:name "Snap Shot"
                                            :page 3
                                            :source :ua-fighter})]}}}]})

(def ua-fighter-plugin
  {:name "Unearthed Arcana: Fighter"
   :class-options? true
   :key :ua-fighter
   :selections [(opt5e/class-selection
                 {:options [(opt5e/class-option fighter-option-cfg)]})]})

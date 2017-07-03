(ns orcpub.dnd.e5.templates.ua-sorcerer
  (:require [orcpub.common :as common]
            [orcpub.modifiers :as mod]
            [orcpub.dnd.e5.modifiers :as mod5e]
            [orcpub.dnd.e5.character :as char5e]
            [orcpub.dnd.e5.options :as opt5e]
            [orcpub.dnd.e5.units :as units5e]
            [orcpub.dnd.e5.templates.ua-options :as ua-options]
            [orcpub.dnd.e5.spell-lists :as sl]))

(def favored-soul {:name "Favored Soul"
                   :modifiers [opt5e/ua-al-illegal
                               (mod5e/spells-known 1 :cure-wounds ::char5e/cha "Sorcerer")
                               (mod5e/trait-cfg
                                {:name "Favored by the Gods"
                                 :page 5
                                 :source ua-options/ua-revised-subclasses-kw
                                 :frequency units5e/rests-1
                                 :summary "if you fail a save or miss an attack, you may roll 2d4 and add it to the missed roll"})]
                   :selections [(opt5e/subclass-cantrip-selection :sorcerer "Sorcerer" ::char5e/cha (get-in sl/spell-lists [:cleric 0]) 0)
                                (opt5e/subclass-spell-selection :sorcerer "Sorcerer" ::char5e/cha (get-in sl/spell-lists [:cleric 1]) 0)]
                   :levels {3 {:selections [(opt5e/subclass-spell-selection :sorcerer "Sorcerer" ::char5e/cha (get-in sl/spell-lists [:cleric 2]) 0)]}
                            5 {:selections [(opt5e/subclass-spell-selection :sorcerer "Sorcerer" ::char5e/cha (get-in sl/spell-lists [:cleric 3]) 0)]}
                            6 {:modifiers [(mod5e/dependent-trait
                                            {:name "Empowered Healing"
                                             :page 5
                                             :source ua-options/ua-revised-subclasses-kw
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
                                              :source ua-options/ua-revised-subclasses-kw
                                              :summary "gain flying speed of 30 ft."})]}
                            18 {:modifiers [(mod5e/bonus-action
                                             {:name "Unearthly Recovery"
                                              :page 5
                                              :source ua-options/ua-revised-subclasses-kw
                                              :frequency units5e/long-rests-1
                                              :summary (str "regain " (int (/ ?max-hit-points)) " HPs if you have that many or fewer left")})]}}})

(def phoenix-sorcery {:name "Phoenix Sorcery"
                   :modifiers [opt5e/ua-al-illegal
                               (mod5e/action
                                {:name "Ignite"
                                 :page 2
                                 :source :ua-sorcerer
                                 :summary "touch a flammable object with your hand and ignite it"})
                               (mod5e/bonus-action
                                {:name "Mantle of Flame"
                                 :page 2
                                 :source :ua-sorcerer
                                 :duration units5e/minutes-1
                                 :frequency units5e/long-rests-1
                                 :summary (str "shed bright light for 30 ft., dim for 30 ft. beyond that; a creature within 5 ft. that touches you or hits with an attack takes " ?cha-mod " fire damage; on your turn, when you roll fire damage, add " (common/bonus-str ?cha-mod))})]
                   :levels {6 {:modifiers [(mod5e/reaction
                                            {:name "Phoenix Spark"
                                             :page 2
                                             :source :ua-sorcerer
                                             :frequency units5e/long-rests-1
                                             :summary (str "if reduced to 0 HPs, you are instead reduced to 1 and deal "
                                                           (+ (int (/ ?sorcerer-level 2))
                                                              ?cha-mod)
                                                           " fire damage to creatures within 10 ft. of you. If under Mantle of Flame, the damage is "
                                                           (+ (int ?sorcerer-level)
                                                              (* 2 ?cha-mod))
                                                           " and Mantle of Flame ends.")})]}
                            14 {:modifiers [(mod5e/dependent-trait
                                             {:name "Nourishing Fire"
                                              :page 2
                                              :source :ua-sorcerer
                                              :summary (str "When you cast a spell that deals fire damage, you regain slot's level + " ?cha-mod " HPs")})]}
                            18 {:modifiers [(mod5e/trait-cfg
                                             {:name "Form of the Phoenix"
                                              :page 2
                                              :source :ua-sorcerer
                                              :summary "While under effect of Mantle of Flame, you have a flying speed of 40 ft, have resistance to all damage, and your Phoenix Sparke deals and extra 20 fire damage"})]}}})

(def sea-sorcery {:name "Sea Sorcery"
                  :modifiers [opt5e/ua-al-illegal
                              (mod5e/swimming-speed-equal-to-walking)
                               (mod5e/trait-cfg
                                {:name "Soul of the Sea"
                                 :page 3
                                 :source :ua-sorcerer
                                 :summary "Can breathe underwater and have a swimming speed the same as your walking speed"})
                               (mod5e/trait-cfg
                                {:name "Curse of the Sea"
                                 :page 3
                                 :source :ua-sorcerer
                                 :duration units5e/rounds-1})]
                  :levels {6 {:modifiers [(mod5e/damage-resistance :fire)
                                          (mod5e/reaction
                                           {:name "Watery Defense"
                                            :page 3
                                            :source :ua-sorcerer
                                            :frequency units5e/rests-1})]}
                            14 {:modifiers [(mod5e/trait-cfg
                                             {:name "Shifting Form"
                                              :page 3
                                              :source :ua-sorcerer})]}
                           18 {:modifiers [(mod5e/damage-resistance :bludgeoning)
                                           (mod5e/damage-resistance :piercing)
                                           (mod5e/damage-resistance :slashing)
                                           (mod5e/trait-cfg
                                            {:name "Water Soul"
                                             :page 3
                                             :source :ua-sorcerer
                                             :summary "don't need to eat, sleep, or drink; critical hits on you become normal hits; resistance to slashing, piercing, and bludgeoning damage."})]}}})

(def stone-sorcery
  {:name "Stone Sorcery"
   :modifiers [opt5e/ua-al-illegal
               (mod5e/armor-proficiency :shields)
               (mod5e/weapon-proficiency :simple)
               (mod5e/weapon-proficiency :martial)
               (mod/cum-sum-mod ?hit-point-level-increases ?sorcerer-level)
               (mod5e/action
                {:name "Stones Durability"
                 :page 4
                 :source :ua-sorcerer})]
   :levels {3 {:selections [(opt5e/subclass-spell-selection :sorcerer "Sorcerer" ::char5e/cha [:compelling-duel :searing-smite :thunderous-smite :wrathful-smite] 0)]}
            5 {:selections [(opt5e/subclass-spell-selection :sorcerer "Sorcerer" ::char5e/cha [:branding-smite :magic-weapon] 0)]}
            6 {:modifiers [(mod5e/bonus-action
                            {:name "Stone Aegis"
                             :page 4
                             :source :ua-sorcerer})]}
            7 {:selections [(opt5e/subclass-spell-selection :sorcerer "Sorcerer" ::char5e/cha [:blinding-smite :elemental-weapon :staggering-smite] 0)]}
            14 {:modifiers [(mod5e/trait-cfg
                             {:name "Stone's Edge"
                              :page 4
                              :source :ua-sorcerer})]}
            18 {:modifiers [(mod5e/trait-cfg
                             {:name "Earth Master's Aegis"
                              :page 4
                              :source :ua-sorcerer})]}}})

(defn sorcerer-plugin [subclasses]
  {:name "Sorcerer"
   :subclass-title "Sorcerous Origin"
   :subclass-level 1
   :plugin? true
   :subclasses subclasses})

(def sorcerer-favored-soul
  (sorcerer-plugin [favored-soul]))

(def ua-sorcerer-plugin
  {:name "Unearthed Arcana: Sorcerer"
   :class-options? true
   :key :ua-sorcerer
   :selections [(opt5e/class-selection
                 {:options [(opt5e/class-option
                             (sorcerer-plugin
                              [favored-soul
                               phoenix-sorcery
                               sea-sorcery
                               stone-sorcery]))]})]})

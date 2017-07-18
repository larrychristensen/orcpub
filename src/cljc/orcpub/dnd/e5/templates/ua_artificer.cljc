(ns orcpub.dnd.e5.templates.ua-artificer
  (:require [orcpub.dnd.e5.character :as char5e]
            [orcpub.dnd.e5.character.equipment :as char-equip5e]
            [orcpub.dnd.e5.options :as opt5e]
            [orcpub.dnd.e5.modifiers :as mod5e]
            [orcpub.dnd.e5.units :as units5e]
            [orcpub.dnd.e5.equipment :as equip5e]
            [orcpub.dnd.e5.magic-items :as mi5e]
            [orcpub.modifiers :as mod]
            [orcpub.template :as t]
            [orcpub.common :as common]))

(defn wonderous-invention-selection [item-kws]
  (t/selection-cfg
   {:name "Wonderous Inventions"
    :min 1
    :max 1
    :ref [:class :artificer :wonderous-inventions]
    :multiselect? true
    :tags #{:equipment}
    :options (map
              (fn [item-kw]
                (let [{:keys [::mi5e/name ::mi5e/summary ::mi5e/description] :as item} (mi5e/other-magic-item-map item-kw)]
                  (if (nil? item)
                    (do #?(:cljs (js/console.warn "MAGIC ITEM NOT FOUND" item-kw))))
                  (t/option-cfg
                   {:name name
                    :help (or summary description)
                    :modifiers [(mod5e/magic-item item-kw
                                                  item
                                                  {::char-equip5e/quantity 1
                                                   ::char-equip5e/equipped? true})]})))
              item-kws)}))

(def alchemical-fire-mod
  (mod5e/action
   {:name "Alchemical Fire"
    :page 5
    :source :ua-artificer
    :range units5e/ft-30
    :summary (str "Hurl a vial of alchemical fire that does "
                  (mod5e/level-val
                   (?class-level :artificer)
                   {4 2
                    7 3
                    10 4
                    13 5
                    16 6
                    19 7
                    :default 1})
                  "d6 fire damage to creatures within a 5 ft. that fail a DC "
                  (?spell-save-dc :int)
                  " DEX save")}))

(def alchemical-acid-mod
  (mod5e/action
   {:name "Alchemical Acid"
    :page 5
    :source :ua-artificer
    :range units5e/ft-30
    :summary (str "Hurl a vial of acid that does "
                  (mod5e/level-val
                   (?class-level :artificer)
                   {3 2
                    5 3
                    7 4
                    9 5
                    11 6
                    13 7
                    15 8
                    17 9
                    19 10
                    :default 1}
                   )
                  "d6 acid damage to creatures within a 5 ft. that fail a DC "
                  (?spell-save-dc :int)
                  " DEX save")}))

(def alchemical-formula-selection
  (t/selection-cfg
   {:name "Alchemical Formulas"
    :min 1
    :max 1
    :multiselect? true
    :tags #{:class}
    :ref [:class :artificer :levels :level-1 :artificer-specialty :alchemist :alchemical-formulas]
    :options [(t/option-cfg
               {:name "Healing Draught"
                :modifiers [(mod5e/action
                             {:name "Healing Draught"
                              :page 5
                              :source :ua-artificer
                              :summary (str "Produce a vial of healing liquid that heals "
                                            (mod5e/level-val
                                             (?class-level :artificer)
                                             {3 2
                                              5 3
                                              7 4
                                              9 5
                                              11 6
                                              13 7
                                              15 8
                                              17 9
                                              19 10
                                              :default 1})
                                            "d8 hit points")})]})
              (t/option-cfg
               {:name "Smoke Stick"
                :modifiers [(mod5e/action
                             {:name "Smoke Stick"
                              :page 5
                              :source :ua-artificer
                              :range units5e/ft-30
                              :summary "Create a stick that produces a thick smoke within a 10 ft. radius that blocks vision"})]})
              (t/option-cfg
               {:name "Swift Step Draught"
                :modifiers [(mod5e/action
                             {:name "Swift Step Draught"
                              :page 5
                              :source :ua-artificer
                              :duration units5e/minutes-1
                              :frequency units5e/minutes-1
                              :summary "Produce a vial of liquid that increases a creature's speed by 20"})]})
              (t/option-cfg
               {:name "Tanglefoot Bag"
                :modifiers [(mod5e/action
                             {:name "Tanglefoot Bag"
                              :page 6
                              :source :ua-artificer
                              :range units5e/ft-30
                              :frequency units5e/minutes-1
                              :summary "Produce a bag that, when thrown, covers a 5 ft. radius with a sticky goo. Creatures that start their turn in the goo have their speed halved that turn."})]})
              (t/option-cfg
               {:name "Thunderstone"
                :modifiers [(mod5e/action
                             {:name "Thunderstone"
                              :page 6
                              :source :ua-artificer
                              :range units5e/ft-30
                              :summary (str "Hurl a shard. Creatures within 10 ft. of the impact point must make a DC " (?spell-save-dc :int) " CON save or be knocked prone and pushed 10 ft. away.")})]})]}))

(defn artificer-tool-prof-mods [tool-key]
  [(mod5e/tool-proficiency tool-key)
   (mod/set-mod ?tool-expertise
                tool-key
                nil
                nil
                [(>= (?class-level :artificer) 2)])])

(def artificer-option
  (opt5e/class-option
   {:name "Artificer"
    :hit-die 8
    :ability-increase-levels [4 8 12 16 18]
    :multiclass-prereqs [(opt5e/ability-prereq ::char5e/cha 13)]
    :profs {:armor {:light false :medium false}
            :weapon {:simple true}
            :save {::char5e/con true ::char5e/int true}
            :tool {:thieves-tools false}
            :skill-options {:choose 3 :options {:arcana true
                                                :deception true
                                                :history true
                                                :investigation true
                                                :medicine true
                                                :nature true
                                                :religion true
                                                :sleight-of-hand true}}}
    :weapon-choices [{:name "Weapon"
                      :options {:handaxe 1
                                :light-hammer 1
                                :simple 1}}]
    :weapons {:crossbow-light 1}
    :equipment {:crossbow-bolt 20
                :thieves-tools 1
                :dungeoneers-pack 1}
    :armor-choices [{:name "Armor"
                     :options {:scale-mail 1
                               :studded 1}}]
    :armor {:leather 1}
    :spellcaster true
    :spellcasting {:level-factor 3       
                   :spells-known {3 3
                                  4 1
                                  7 1
                                  8 1
                                  10 1
                                  11 1
                                  14 1
                                  15 1
                                  19 1
                                  20 2}
                   :known-mode :schedule
                   :ability ::char5e/int}
    :traits [{:name "Magic Item Analysis"
              :page 3
              :source :ua-artificer
              :summary "You know detect magic and identify and can cast them as rituals."}
             {:name "Infuse Magic"
              :page 4
              :level 4
              :source :ua-artificer
              :summary "Infuse non-magical items with spells"}
             {:name "Mechanical Servant"
              :page 4
              :level 6
              :source :ua-artificer
              :summary "You can create a mechanical servant"}
             {:name "Soul of Artifice"
              :page 4
              :level 20
              :source :ua-artificer
              :summary "You gain +1 to saves for each magic item you are attuned to"}]
    :selections [(opt5e/tool-proficiency-selection
                  {:num 2
                   :options (map
                             (fn [{:keys [name key]}]
                               (t/option-cfg
                                {:name name
                                 :key key
                                 :modifiers (artificer-tool-prof-mods key)}))
                             equip5e/tools)})]
    :modifiers [(mod5e/tool-proficiency :thieves-tools)
                opt5e/ua-al-illegal]
    :levels {2 {:modifiers [(mod5e/tool-expertise :thieves-tools)]
                :selections [(wonderous-invention-selection
                              [:bag-of-holding
                               :cap-of-water-breathing
                               :driftglobe
                               :goggles-of-night
                               :sending-stones])]}
             5 {:selections [(wonderous-invention-selection
                              [:alchemy-jug
                               :helm-of-comprehending-languages
                               :lantern-of-revealing
                               :ring-of-swimming
                               :robe-of-useful-items
                               :rope-of-climbing
                               :wand-of-magic-detection
                               :wand-of-secrets])]
                :modifiers [(mod5e/dependent-trait
                             {:name "Superior Attunement"
                              :page 4
                              :level 5
                              :source :ua-artificer
                              :summary (str "Can attune to "
                                            (let [level (?class-level :artificer)]
                                              (cond
                                                (< level 15) 4
                                                (< level 20) 5
                                                :else 6)))})]}
             10 {:selections [(wonderous-invention-selection
                               [:bag-of-beans
                                :chime-of-opening
                                :decanter-of-endless-water
                                :eyes-of-minute-seeing
                                :folding-boat
                                :handy-haversack])]}
             15 {:selections [(wonderous-invention-selection
                               [:boots-of-striding-and-springing
                                :bracers-of-archery
                                :brooch-of-shielding
                                :broom-of-flying
                                :hat-of-disguise
                                :slippers-of-spider-climbing])]}
             20 {:selections [(wonderous-invention-selection
                               [:eyes-of-the-eagle
                                :gem-of-brightness
                                :gloves-of-missile-snaring
                                :gloves-of-swimming-and-climbing
                                :ring-of-jumping
                                :ring-of-mind-shielding
                                :wings-of-flying])]}}
    :subclass-level 1
    :subclass-title "Artificer Specialty"
    :subclasses [{:name "Alchemist"
                  :traits [{:name "Alchemist's Satchel"
                            :page 5
                            :source :ua-artificer
                            :summary "You craft a satchel that contains your alchemical materials"}]
                  :selections [alchemical-formula-selection]
                  :modifiers [alchemical-fire-mod
                              alchemical-acid-mod]
                  :levels {3 {:selections [alchemical-formula-selection]}
                           9 {:selections [alchemical-formula-selection]}
                           14 {:selections [alchemical-formula-selection]}
                           17 {:selections [alchemical-formula-selection]}}}
                 {:name "Gunsmith"
                  :modifiers (concat
                              (artificer-tool-prof-mods :smiths-tools)
                              [(mod5e/spells-known 0 :mending ::char5e/int "Artificer")
                               (mod5e/weapon :thunder-cannon {::char-equip5e/equipped? true
                                                              ::char-equip5e/quantity 1})
                               (mod5e/weapon-proficiency :thunder-cannon)])
                  :levels {3 {:modifiers [(mod5e/action
                                           {:name "Thunder Monger"
                                            :page 6
                                            :source :ua-artificer
                                            :summary (str "Make a special Thunder Cannon attack that deals an extra "
                                                          (mod5e/level-val
                                                           (?class-level :artificer)
                                                           {5 2
                                                            7 3
                                                            9 4
                                                            11 5
                                                            13 6
                                                            15 7
                                                            17 8
                                                            19 9
                                                            :default 1})
                                                          "d6 thunder damage")})]}
                           9 {:modifiers [(mod5e/action
                                           {:name "Blast Wave"
                                            :page 6
                                            :source :ua-artificer
                                            :summary (str "Make a special Thunder Cannon attack that unleashes energy in a 15 ft. cone. Creatures in the area that fail a DC "
                                                          (?spell-save-dc :int)
                                                          " STR save take "
                                                          (mod5e/level-val
                                                           (?class-level :artificer)
                                                           {13 3
                                                            17 4
                                                            :default 2})
                                                          "d6 force damage and are pushed 10 ft.")})]}
                           14 {:modifiers [(mod5e/action
                                            {:name "Piercing Round"
                                             :page 6
                                             :source :ua-artificer
                                             :summary (str "Make a special Thunder Cannon attack that unleashes a 5 x 30 ft. lightning bolt. Creatures in the area that fail a DC "
                                                           (?spell-save-dc :int)
                                                           " DEX save take "
                                                           (mod5e/level-val
                                                            (?class-level :artificer)
                                                            {19 6
                                                             :default 4})
                                                           "d6 force damage and are pushed 10 ft.")})]}
                           17 {:modifiers [(mod5e/action
                                           {:name "Explosive Round"
                                            :page 6
                                            :source :ua-artificer
                                            :summary (str "Launch an explosive round that detonates in a 30 ft. radius. Creatures in the area that fail a DC "
                                                          (?spell-save-dc :int)
                                                          " DEX save take 4d8 fire damage")})]}}
                  :traits [{:name "Thunder Cannon"
                            :page 6
                            :source :ua-artificer
                            :summary "You forge a thunder cannon firearm"}
                           {:name "Arcane Magazine"
                            :page 6
                            :source :ua-artificer
                            :summary "You craft a leather bag that can produce 40 rounds every long rest and 10 rounds every short rest"}]}]}))

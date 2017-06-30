(ns orcpub.dnd.e5.templates.ua-revised-ranger
  (:require [orcpub.dnd.e5.options :as opt5e]
            [orcpub.dnd.e5.character :as char5e]
            [orcpub.dnd.e5.modifiers :as mod5e]
            [orcpub.dnd.e5.units :as units5e]
            [orcpub.modifiers :as mod]
            [orcpub.template :as t]
            [orcpub.common :as common]
            [clojure.string :as s]
            [re-frame.core :refer [subscribe]]))

(defn favored-enemy-selection [name types]
  (t/selection-cfg
   {:name name
    :tags #{:class}
    :order 4
    :multiselect? false
    :options (map
              (fn [enemy-kw]
                (t/option-cfg
                 {:name (s/capitalize (clojure.core/name enemy-kw))
                  :modifiers [(mod/set-mod ?revised-ranger-favored-enemies enemy-kw)]
                  :selections [(opt5e/language-selection opt5e/languages 1)]}))
              types)}))


(def revised-ranger-option
  (opt5e/class-option
   {:name "Ranger (Revised)"
    :key :revised-ranger
    :hit-die 10
    :profs {:armor {:light false :medium false :shields false}
            :weapon {:simple false :martial false}
            :save {::char5e/str true ::char5e/dex true}
            :skill-options {:choose 3 :options opt5e/ranger-skills}
            :multiclass-skill-options {:choose 1 :options opt5e/ranger-skills}}
    :multiclass-prereqs [(t/option-prereq "Requires Wisdom 13 and Dexterity 13"
                                          (fn [c]
                                            (let [abilities @(subscribe [::char5e/abilities nil c])]
                                              (and (>= (::char5e/wis abilities) 13)
                                                   (>= (::char5e/dex abilities) 13)))))]
    :ability-increase-levels [4 8 12 16 19]
    :spellcaster true
    :spellcasting {:level-factor 2
                   :known-mode :schedule
                   :spell-list :ranger
                   :spells-known {2 2
                                  3 1
                                  5 1
                                  7 1
                                  9 1
                                  11 1
                                  13 1
                                  15 1
                                  17 1
                                  19 1}
                   :ability ::char5e/wis}
    :armor-choices [{:name "Armor"
                     :options {:scale-mail 1
                               :leather 1}}]
    :equipment-choices [{:name "Equipment Pack"
                         :options {:dungeoneers-pack 1
                                   :explorers-pack 1}}]
    :weapons {:longbow 1}
    :equipment {:quiver 1
                :arrow 20}
    :modifiers [opt5e/ua-al-illegal
                (mod5e/dependent-trait
                 {:name "Favored Enemy"
                  :page 2
                  :source :ua-revised-ranger
                  :summary (str "You deal an extra "
                                (common/bonus-str
                                 (if (>= (?class-level :revised-ranger) 6)
                                   4
                                   2))
                                " damage against "
                                (common/list-print (map name ?revised-ranger-favored-enemies))
                                " creatures. You also have advantage on Survival checks to track them and INT checks to recall info about them.")})
                (mod5e/dependent-trait
                 {:name "Natural Explorer"
                  :page 2
                  :source :ua-revised-ranger
                  :summary "You ignore difficult terrain; have advantage on initiative rolls; and during first round of combat have advantage on attacks against creatures that have yet to act. When traveling an hour or more: your group isn't slowed by difficult terrain; you group can't become lost by magic; you remain alert to danger while engaged in other activities; you can move stealthily at a normal pace if alone; 2x food when foraging; when tracking you know eact number and sizes of creatures and how long ago they passed."})]
    :selections [(opt5e/new-starting-equipment-selection
                  :ranger
                  {:name "Melee Weapon"
                   :options [(t/option-cfg
                              {:name "Two Shortswords"
                               :modifiers [(mod5e/weapon :shortsword 2)]})
                             (t/option-cfg
                              {:name "Simple Melee Weapon"
                               :selections [(opt5e/new-starting-equipment-selection
                                             :ranger
                                             {:name "Simple Melee Weapon"
                                              :options (opt5e/simple-melee-weapon-options 1)
                                              :min 2
                                              :max 2})]})]})
                 (favored-enemy-selection "Favored Enemy" [:beast :fey :humanoid :monstrosity :undead])]
    :levels {2 {:selections [(opt5e/fighting-style-selection :revised-ranger #{:archery :defense :dueling :two-weapon-fighting})]}
             6 {:selections [(favored-enemy-selection "Greater Favored Enemy" [:abberation :celestial :construct :dragon :elemental :fiend :giant])]}
             8 {:modifiers [(mod5e/bonus-action
                             {:name "Fleet of Foot"
                              :page 4
                              :source :ua-revised-ranger
                              :summary "Take the Dash action"})]}
             14 {:selections [(mod5e/bonus-action
                               {:name "Vanish"
                                :page 4
                                :source :ua-revised-ranger
                                :summary "Take the Hide action; can't be tracked with magic"})]}
             20 {:modifiers [(mod5e/dependent-trait
                              {:name "Foe Slayer"
                               :frequency units5e/turns-1
                               :level 20
                               :page 92
                               :summary (str "add " (common/bonus-str (?ability-bonuses ::char5e/wis)) " to an attack or damage roll")})]}}
    :traits [{:name "Primeval Awareness"
              :level 3
              :page 4
              :source :ua-revised-ranger
              :summary "Communicate with beasts; concentrate for 1 min. to learn the numbers, directions, and distances to your favored enemies within 5 miles."}
             {:name "Hide in Plain Sight"
              :level 10
              :page 4
              :source :ua-revised-ranger}
             {:name "Feral Senses"
              :level 18
              :page 92
              :summary "no disadvantage on attacks against creature you can't see, you know location of invisible creatures within 30 ft."}]
    :subclass-level 3
    :subclass-title "Ranger Conclave"
    :subclasses [{:name "Hunter Conclave"
                  :levels {3 {:selections [(t/selection-cfg
                                            {:name "Hunter's Prey"
                                             :tags #{:class}
                                             :options [(t/option-cfg
                                                        {:name "Colossus Slayer"
                                                         :modifiers [(mod5e/trait-cfg
                                                                      {:name "Colossus Slayer"
                                                                       :page 93
                                                                       :frequency units5e/turns-1
                                                                       :summary "deal an extra d8 damage when you hit a creature that is below its HP max with a weapon attack"})]})
                                                       (t/option-cfg
                                                        {:name "Giant Killer"
                                                         :modifiers [(mod5e/reaction
                                                                      {:name "Giant Killer"
                                                                       :page 93
                                                                       :frequency units5e/turns-1
                                                                       :summary "attack a Large or larger creature within 5 ft that misses an attack against you"})]})
                                                       (t/option-cfg
                                                        {:name "Horde Breaker"
                                                         :modifiers [(mod5e/trait-cfg
                                                                      {:name "Horde Breaker"
                                                                       :page 93
                                                                       :frequency units5e/turns-1
                                                                       :summary "when you attack one creature, attack another creature within 5 feet of it with the same action"})]})]})]}
                           5 {:modifiers [(mod5e/num-attacks 2)]}
                           7 {:selections [(t/selection-cfg
                                            {:name "Defensive Tactics"
                                             :tags #{:class}
                                             :options [(t/option-cfg
                                                        {:name "Escape the Horde"
                                                         :modifiers [(mod5e/trait-cfg
                                                                      {:name "Escape the Horde"
                                                                       :frequency units5e/turns-1
                                                                       :page 93})]})
                                                       (t/option-cfg
                                                        {:name "Multiattack Defense"
                                                         :modifiers [(mod5e/trait-cfg
                                                                      {:name "Multiattack Defense"
                                                                       :frequency units5e/turns-1
                                                                       :page 93})]})
                                                       (t/option-cfg
                                                        {:name "Steel Will"
                                                         :modifiers [(mod5e/saving-throw-advantage [:frightened])]})]})]}
                           11 {:selections [(t/selection-cfg
                                             {:name "Multiattack"
                                              :tags #{:class}
                                              :options [(t/option-cfg
                                                         {:name "Volley"
                                                          :modifiers [(mod5e/action
                                                                       {:name "Volley"
                                                                        :page 93
                                                                        :summary "make a ranged attack against any creatures within a 10 ft of a point" 
                                                                        })]})
                                                        (t/option-cfg
                                                         {:name "Whirlwind Attack"
                                                          :modifiers [(mod5e/action
                                                                       {:name "Whirlwind Attack"
                                                                        :page 93
                                                                        :summary "melee attack against any creatures within 5 ft. of you"})]})]})]}
                           15 {:selections [(t/selection-cfg
                                             {:name "Superior Hunter's Defense"
                                              :tags #{:class}
                                              :options [(t/option-cfg
                                                         {:name "Evasion"
                                                          :modifiers [(mod5e/trait-cfg
                                                                       (opt5e/evasion 15 93))]})
                                                        (t/option-cfg
                                                         {:name "Stand Against the Tide"
                                                          :modifiers  [(mod5e/reaction
                                                                        {:name "Stand Against the Tide"
                                                                         :page 93
                                                                         :summary "force a creature to repeat its attack on another creature when it misses you"
                                                                         })]})
                                                        (t/option-cfg
                                                         {:name "Uncanny Dodge"
                                                          :modifiers [(opt5e/uncanny-dodge-modifier 93)]})]})]}}}
                 {:name "Beast Conclave"
                  :selections [(t/selection-cfg
                                {:name "Animal Companion"
                                 :tags #{:class}
                                 :options (map
                                           (fn [name]
                                             (t/option-cfg
                                              {:name name
                                               :modifiers [(mod5e/trait-cfg
                                                            {:name "Animal Companion"
                                                             :level 3
                                                             :page 5
                                                             :source :ua-revised-ranger
                                                             :summary (str "Your animal companion is " (if (s/starts-with? name "a") "an " "a ") name ".")})]}))
                                           ["ape" "black bear" "boar" "giant badger" "giant weasel" "mule" "panther" "wolf"])})]
                  :traits [{:name "Coordinated Attack"
                            :level 5
                            :page 6
                            :source :ua-revised-ranger
                            :summary "When you Attack on your turn, your companion can use its reaction to make a melee attack."}
                           {:name "Beasts Defense"
                            :level 7
                            :page 6
                            :source :ua-revised-ranger
                            :summary "Your companion has advantage on saves while it can see you."}
                           {:name "Storm of Claws and Fangs"
                            :level 11
                            :page 6
                            :source :ua-revised-ranger
                            :summary "Your companion can, as an action, attack each creature within 5 ft. of it."}
                           {:name "Superior Beast's Defense"
                            :level 15
                            :page 7
                            :source :ua-revised-ranger
                            :summary "When an attacker hits your companion with an attack, it can, as a reaction, halve the damage."}]}
                 {:name "Deep Stalker Conclave"
                  :levels {3 {:modifiers [(mod5e/trait-cfg
                                           {:name "Underdark Scout"
                                            :page 8
                                            :source :ua-revised-ranger
                                            :summary "During your first turn in combat, you can make an additional attack and gain +10 bonus to speed. Creatures that rely on darkvision don't benefit from it when attempting to detect you in dimness and darkness"})
                                          (mod5e/darkvision 90)
                                          (mod5e/spells-known 1 :disguise-self ::char5e/wis "Ranger (Revised)")]}
                           5 {:modifiers [(mod5e/spells-known 2 :rope-trick ::char5e/wis "Ranger (Revised)")
                                          (mod5e/num-attacks 2)]}
                           7 {:modifiers [(mod5e/saving-throws nil ::char5e/wis)]}
                           9 {:modifiers [(mod5e/spells-known 3 :glyph-of-warding ::char5e/wis "Ranger (Revised)")]}
                           11 {:modifiers [(mod5e/trait-cfg
                                           {:name "Stalker's Fury"
                                            :page 8
                                            :source :ua-revised-ranger
                                            :frequency units5e/turns-1
                                            :summary "When you miss with an attack, you can make another"})]}
                           13 {:modifiers [(mod5e/spells-known 4 :greater-invisibility ::char5e/wis "Ranger (Revised)")]}
                           15 {:modifiers [(mod5e/reaction
                                            {:name "Stalker's Dodge"
                                             :page 8
                                             :source :ua-revised-ranger
                                             :summary "Impose disadvantage on a creature's attacks against you if it doesn't have advantage"})]}
                           17 {:modifiers [(mod5e/spells-known 5 :seeming ::char5e/wis "Ranger (Revised)")]}}}]}))

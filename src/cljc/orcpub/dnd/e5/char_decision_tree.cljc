(ns orcpub.dnd.e5.char-decision-tree
  (:require [orcpub.dnd.e5.character.random :as char-rand5e]))

(defn add-race-fn [race-kw & [subrace-kw]]
  #(let [name-cfg {:race race-kw
                   :subrace subrace-kw
                   :sex (:sex %2)}]
     (cond-> %
       true (assoc-in [:orcpub.entity/options :race]
                      {:orcpub.entity/key race-kw})
       subrace-kw (assoc-in [:orcpub.entity/options
                             :race
                             :orcpub.entity/options]
                            {:subrace {:orcpub.entity/key subrace-kw}})
       true (assoc-in [:orcpub.entity/values
                       :orcpub.dnd.e5.character/character-name]
                      (:name (char-rand5e/random-name-result name-cfg))))))

(defn set-sex-fn [sex]
  #(assoc-in % [:orcpub.entity/values :orcpub.dnd.e5.character/sex] sex))

(defn set-class-fn [class]
  #(update %
          :orcpub.entity/options
          (fn [options]
            (merge
             options
             (:orcpub.entity/options class)))))

(defn set-law-fn [good neutral evil]
  (fn [c {:keys [good-or-evil]}]
    (assoc-in
     c
     [:orcpub.entity/options :alignment :orcpub.entity/key]
     (case good-or-evil
       :good good
       :neutral neutral
       :evil evil))))

(def archer
  {:orcpub.entity/options
   {:ability-scores
    {:orcpub.entity/key :standard-scores,
     :orcpub.entity/value
     {:orcpub.dnd.e5.character/str 13,
      :orcpub.dnd.e5.character/dex 15,
      :orcpub.dnd.e5.character/con 14,
      :orcpub.dnd.e5.character/int 8,
      :orcpub.dnd.e5.character/wis 12,
      :orcpub.dnd.e5.character/cha 10}},
    :class
    [{:orcpub.entity/key :fighter,
      :orcpub.entity/options
      {:levels [{:orcpub.entity/key :level-1}],
       :fighting-style [{:orcpub.entity/key :archery}],
       :skill-proficiency
       [{:orcpub.entity/key :animal-handling}
        {:orcpub.entity/key :perception}],
       :starting-equipment-additional-weapons
       {:orcpub.entity/key :two-handaxes},
       :starting-equipment-armor
       {:orcpub.entity/key :leather-armor-longbow-20-arrows},
       :starting-equipment-equipment-pack
       {:orcpub.entity/key :explorers-pack},
       :starting-equipment-weapons
       {:orcpub.entity/key :two-martial-weapons,
        :orcpub.entity/options
        {:starting-equipment-martial-weapon-1
         {:orcpub.entity/key :shortsword},
         :starting-equipment-martial-weapon-2
         {:orcpub.entity/key :shortsword}}}}}],
    :background
    {:orcpub.entity/key :custom,
     :orcpub.entity/value "Soldier",
     :orcpub.entity/options
     {:skill-proficiency
      [{:orcpub.entity/key :athletics}
       {:orcpub.entity/key :intimidation}],
      :tool-language-proficiencies
      {:orcpub.entity/key :two-tools,
       :orcpub.entity/options
       {:tool-proficiency
        [{:orcpub.entity/key :dice-set}
         {:orcpub.entity/key :land-vehicles}]}}}}}})

(def swordsman
  {:orcpub.entity/options
   {:ability-scores
    {:orcpub.entity/key :standard-scores,
     :orcpub.entity/value
     {:orcpub.dnd.e5.character/str 15,
      :orcpub.dnd.e5.character/dex 13,
      :orcpub.dnd.e5.character/con 14,
      :orcpub.dnd.e5.character/int 8,
      :orcpub.dnd.e5.character/wis 12,
      :orcpub.dnd.e5.character/cha 10}},
    :class
    [{:orcpub.entity/key :fighter,
      :orcpub.entity/options
      {:levels [{:orcpub.entity/key :level-1}],
       :fighting-style [{:orcpub.entity/key :great-weapon-fighting}],
       :skill-proficiency
       [{:orcpub.entity/key :animal-handling}
        {:orcpub.entity/key :perception}],
       :starting-equipment-additional-weapons
       {:orcpub.entity/key :light-crossbow-and-20-bolts},
       :starting-equipment-armor {:orcpub.entity/key :chain-mail},
       :starting-equipment-equipment-pack
       {:orcpub.entity/key :explorers-pack},
       :starting-equipment-weapons
       {:orcpub.entity/key :two-martial-weapons,
        :orcpub.entity/options
        {:starting-equipment-martial-weapon-2
         {:orcpub.entity/key :greatsword},
         :starting-equipment-martial-weapon-1
         {:orcpub.entity/key :longsword}}}}}],
    :background
    {:orcpub.entity/key :custom,
     :orcpub.entity/value "Soldier",
     :orcpub.entity/options
     {:skill-proficiency
      [{:orcpub.entity/key :athletics}
       {:orcpub.entity/key :intimidation}],
      :tool-language-proficiencies
      {:orcpub.entity/key :two-tools,
       :orcpub.entity/options
       {:tool-proficiency
        [{:orcpub.entity/key :dice-set}
         {:orcpub.entity/key :land-vehicles}]}}}}},
   :changed true})

(def balanced-fighter
  {:orcpub.entity/options
   {:ability-scores
    {:orcpub.entity/key :standard-scores,
     :orcpub.entity/value
     {:orcpub.dnd.e5.character/str 15,
      :orcpub.dnd.e5.character/dex 14,
      :orcpub.dnd.e5.character/con 13,
      :orcpub.dnd.e5.character/int 8,
      :orcpub.dnd.e5.character/wis 12,
      :orcpub.dnd.e5.character/cha 10}},
    :class
    [{:orcpub.entity/key :fighter,
      :orcpub.entity/options
      {:levels [{:orcpub.entity/key :level-1}],
       :fighting-style [{:orcpub.entity/key :defense}],
       :skill-proficiency
       [{:orcpub.entity/key :animal-handling}
        {:orcpub.entity/key :perception}],
       :starting-equipment-additional-weapons
       {:orcpub.entity/key :two-handaxes},
       :starting-equipment-armor
       {:orcpub.entity/key :leather-armor-longbow-20-arrows},
       :starting-equipment-equipment-pack
       {:orcpub.entity/key :explorers-pack},
       :starting-equipment-weapons
       {:orcpub.entity/key :martial-weapon-and-shield,
        :orcpub.entity/options
        {:starting-equipment-martial-weapon
         {:orcpub.entity/key :longsword}}}}}],
    :background
    {:orcpub.entity/key :custom,
     :orcpub.entity/value "Soldier",
     :orcpub.entity/options
     {:skill-proficiency
      [{:orcpub.entity/key :athletics}
       {:orcpub.entity/key :intimidation}],
      :tool-language-proficiencies
      {:orcpub.entity/key :two-tools,
       :orcpub.entity/options
       {:tool-proficiency
        [{:orcpub.entity/key :dice-set}
         {:orcpub.entity/key :land-vehicles}]}}}}}})

(def barbarian
  {:orcpub.entity/options
   {:ability-scores
    {:orcpub.entity/key :standard-scores,
     :orcpub.entity/value
     {:orcpub.dnd.e5.character/str 15,
      :orcpub.dnd.e5.character/dex 12,
      :orcpub.dnd.e5.character/con 14,
      :orcpub.dnd.e5.character/int 10,
      :orcpub.dnd.e5.character/wis 13,
      :orcpub.dnd.e5.character/cha 8}},
    :class
    [{:orcpub.entity/key :barbarian,
      :orcpub.entity/options
      {:levels [{:orcpub.entity/key :level-1}],
       :skill-proficiency
       [{:orcpub.entity/key :intimidation} {:orcpub.entity/key :nature}],
       :starting-equipment-martial-weapon {:orcpub.entity/key :greataxe},
       :starting-equipment-simple-weapon
       {:orcpub.entity/key :handaxe-2-}}}],
    :weapons
    [{:orcpub.entity/key :javelin,
      :orcpub.entity/value
      {:orcpub.dnd.e5.character.equipment/quantity 4,
       :orcpub.dnd.e5.character.equipment/equipped? true,
       :orcpub.dnd.e5.character.equipment/class-starting-equipment?
       true}}],
    :equipment
    [{:orcpub.entity/key :explorers-pack,
      :orcpub.entity/value
      {:orcpub.dnd.e5.character.equipment/quantity 1,
       :orcpub.dnd.e5.character.equipment/equipped? true,
       :orcpub.dnd.e5.character.equipment/class-starting-equipment?
       true}}],
    :background
    {:orcpub.entity/key :custom,
     :orcpub.entity/value "Outlander",
     :orcpub.entity/options
     {:skill-proficiency
      [{:orcpub.entity/key :athletics} {:orcpub.entity/key :survival}],
      :tool-language-proficiencies
      {:orcpub.entity/key :one-tool-one-language,
       :orcpub.entity/options
       {:tool-proficiency {:orcpub.entity/key :lute},
        :languages [{:orcpub.entity/key :goblin}]}}}}}})

(def bard
  {:orcpub.entity/options
   {:ability-scores
    {:orcpub.entity/key :standard-scores,
     :orcpub.entity/value
     {:orcpub.dnd.e5.character/str 8,
      :orcpub.dnd.e5.character/dex 14,
      :orcpub.dnd.e5.character/con 13,
      :orcpub.dnd.e5.character/int 12,
      :orcpub.dnd.e5.character/wis 10,
      :orcpub.dnd.e5.character/cha 15}},
    :background
    {:orcpub.entity/key :custom,
     :orcpub.entity/value "Entertainer",
     :orcpub.entity/options
     {:tool-language-proficiencies
      {:orcpub.entity/key :two-tools,
       :orcpub.entity/options
       {:tool-proficiency
        [{:orcpub.entity/key :disguise-kit}
         {:orcpub.entity/key :drum}]}}}}
    :class
    [{:orcpub.entity/key :bard,
      :orcpub.entity/options
      {:levels [{:orcpub.entity/key :level-1}],
       :bard-cantrips-known
       [{:orcpub.entity/key :dancing-lights}
        {:orcpub.entity/key :vicious-mockery}],
       :bard-spells-known
       [{:orcpub.entity/key :charm-person}
        {:orcpub.entity/key :detect-magic}
        {:orcpub.entity/key :healing-word}
        {:orcpub.entity/key :thunderwave}],
       :skill-proficiency
       [{:orcpub.entity/key :performance}
        {:orcpub.entity/key :persuasion}
        {:orcpub.entity/key :history}],
       :tool-selection--:musical-instruments
       [{:orcpub.entity/key :lyre}
        {:orcpub.entity/key :horn}
        {:orcpub.entity/key :flute}],
       :starting-equipment-equipment-pack
       {:orcpub.entity/key :entertainers-pack},
       :starting-equipment-musical-instrument {:orcpub.entity/key :lyre},
       :starting-equipment-weapon {:orcpub.entity/key :longsword}}}],
    :weapons
    [{:orcpub.entity/key :dagger,
      :orcpub.entity/value
      {:orcpub.dnd.e5.character.equipment/quantity 1,
       :orcpub.dnd.e5.character.equipment/equipped? true,
       :orcpub.dnd.e5.character.equipment/class-starting-equipment?
       true}}],
    :armor
    [{:orcpub.entity/key :leather,
      :orcpub.entity/value
      {:orcpub.dnd.e5.character.equipment/quantity 1,
       :orcpub.dnd.e5.character.equipment/equipped? true,
       :orcpub.dnd.e5.character.equipment/class-starting-equipment?
       true}}]}})

(def cleric
  {:orcpub.entity/options
   {:ability-scores
    {:orcpub.entity/key :standard-scores,
     :orcpub.entity/value
     {:orcpub.dnd.e5.character/str 14,
      :orcpub.dnd.e5.character/dex 12,
      :orcpub.dnd.e5.character/con 13,
      :orcpub.dnd.e5.character/int 8,
      :orcpub.dnd.e5.character/wis 15,
      :orcpub.dnd.e5.character/cha 10}},
    :class
    [{:orcpub.entity/key :cleric,
      :orcpub.entity/options
      {:levels
       [{:orcpub.entity/key :level-1,
         :orcpub.entity/options
         {:divine-domain {:orcpub.entity/key :life-domain}}}],
       :cleric-cantrips-known
       [{:orcpub.entity/key :spare-the-dying}
        {:orcpub.entity/key :resistance}
        {:orcpub.entity/key :guidance}],
       :skill-proficiency
       [{:orcpub.entity/key :medicine} {:orcpub.entity/key :persuasion}],
       :starting-equipment-additional-weapon
       {:orcpub.entity/key :light-crossbow-and-20-bolts},
       :starting-equipment-armor {:orcpub.entity/key :chain-mail},
       :starting-equipment-cleric-weapon {:orcpub.entity/key :mace},
       :starting-equipment-equipment-pack
       {:orcpub.entity/key :priests-pack},
       :starting-equipment-holy-symbol {:orcpub.entity/key :emblem}}}],
    :equipment
    [{:orcpub.entity/key :clothes-common,
      :orcpub.entity/value
      {:orcpub.dnd.e5.character.equipment/quantity 1,
       :orcpub.dnd.e5.character.equipment/equipped? true,
       :orcpub.dnd.e5.character.equipment/background-starting-equipment?
       true}}
     {:orcpub.entity/key :pouch,
      :orcpub.entity/value
      {:orcpub.dnd.e5.character.equipment/quantity 1,
       :orcpub.dnd.e5.character.equipment/equipped? true,
       :orcpub.dnd.e5.character.equipment/background-starting-equipment?
       true}}
     {:orcpub.entity/key :incense,
      :orcpub.entity/value
      {:orcpub.dnd.e5.character.equipment/quantity 5,
       :orcpub.dnd.e5.character.equipment/equipped? true,
       :orcpub.dnd.e5.character.equipment/background-starting-equipment?
       true}}
     {:orcpub.entity/key :vestements,
      :orcpub.entity/value
      {:orcpub.dnd.e5.character.equipment/quantity 1,
       :orcpub.dnd.e5.character.equipment/equipped? true,
       :orcpub.dnd.e5.character.equipment/background-starting-equipment?
       true}}],
    :background
    {:orcpub.entity/key :acolyte,
     :orcpub.entity/options
     {:starting-equipment-holy-symbol {:orcpub.entity/key :amulet},
      :starting-equipment-prayer-book-wheel
      {:orcpub.entity/key :prayer-book}}},
    :treasure
    [{:orcpub.entity/key :gp,
      :orcpub.entity/value
      {:orcpub.dnd.e5.character.equipment/quantity 15,
       :orcpub.dnd.e5.character.equipment/equipped? true,
       :orcpub.dnd.e5.character.equipment/background-starting-equipment?
       true}}],
    :armor
    [{:orcpub.entity/key :shield,
      :orcpub.entity/value
      {:orcpub.dnd.e5.character.equipment/quantity 1,
       :orcpub.dnd.e5.character.equipment/equipped? true,
       :orcpub.dnd.e5.character.equipment/class-starting-equipment?
       true}}],
    :languages
    [{:orcpub.entity/key :celestial} {:orcpub.entity/key :infernal}]},})

(def druid
  {:orcpub.entity/options
   {:ability-scores
    {:orcpub.entity/key :standard-scores,
     :orcpub.entity/value
     {:orcpub.dnd.e5.character/str 12,
      :orcpub.dnd.e5.character/dex 13,
      :orcpub.dnd.e5.character/con 14,
      :orcpub.dnd.e5.character/int 8,
      :orcpub.dnd.e5.character/wis 15,
      :orcpub.dnd.e5.character/cha 10}},
    :class
    [{:orcpub.entity/key :druid,
      :orcpub.entity/options
      {:levels [{:orcpub.entity/key :level-1}],
       :druid-cantrips-known
       [{:orcpub.entity/key :druidcraft}
        {:orcpub.entity/key :shillelagh}],
       :skill-proficiency
       [{:orcpub.entity/key :nature} {:orcpub.entity/key :survival}],
       :starting-equipment-druidic-focus
       {:orcpub.entity/key :wooden-staff},
       :starting-equipment-equipment-pack
       {:orcpub.entity/key :priests-pack},
       :starting-equipment-melee-weapon
       {:orcpub.entity/key :simple-melee-weapon,
        :orcpub.entity/options
        {:starting-equipment-simple-melee-weapon
         {:orcpub.entity/key :club}}},
       :starting-equipment-wooden-shield-or-simple-weapon
       {:orcpub.entity/key :wooden-shield}}}],
    :armor
    [{:orcpub.entity/key :leather,
      :orcpub.entity/value
      {:orcpub.dnd.e5.character.equipment/quantity 1,
       :orcpub.dnd.e5.character.equipment/equipped? true,
       :orcpub.dnd.e5.character.equipment/class-starting-equipment?
       true}}],
    :equipment
    [{:orcpub.entity/key :explorers-pack,
      :orcpub.entity/value
      {:orcpub.dnd.e5.character.equipment/quantity 1,
       :orcpub.dnd.e5.character.equipment/equipped? true,
       :orcpub.dnd.e5.character.equipment/class-starting-equipment?
       true}}],
    :background
    {:orcpub.entity/key :custom,
     :orcpub.entity/value "Hermit",
     :orcpub.entity/options
     {:skill-proficiency
      [{:orcpub.entity/key :medicine} {:orcpub.entity/key :religion}],
      :tool-language-proficiencies
      {:orcpub.entity/key :one-tool-one-language,
       :orcpub.entity/options
       {:tool-proficiency {:orcpub.entity/key :herbalism-kit},
        :languages [{:orcpub.entity/key :sylvan}]}}}}}})

(def monk
  {:orcpub.entity/options
   {:ability-scores
    {:orcpub.entity/key :standard-scores,
     :orcpub.entity/value
     {:orcpub.dnd.e5.character/str 13,
      :orcpub.dnd.e5.character/dex 15,
      :orcpub.dnd.e5.character/con 12,
      :orcpub.dnd.e5.character/int 8,
      :orcpub.dnd.e5.character/wis 14,
      :orcpub.dnd.e5.character/cha 10}},
    :class
    [{:orcpub.entity/key :monk,
      :orcpub.entity/options
      {:levels [{:orcpub.entity/key :level-1}],
       :skill-proficiency
       [{:orcpub.entity/key :acrobatics}
        {:orcpub.entity/key :athletics}],
       :tool-selection
       {:orcpub.entity/key :musical-instruments,
        :orcpub.entity/options
        {:tool-selection--:musical-instruments
         {:orcpub.entity/key :pan-flute}}},
       :starting-equipment-equipment-pack
       {:orcpub.entity/key :explorers-pack},
       :starting-equipment-weapon
       {:orcpub.entity/key :any-simple-weapon,
        :orcpub.entity/options
        {:starting-equipment-simple-weapon
         {:orcpub.entity/key :quarterstaff}}}}}],
    :weapons
    [{:orcpub.entity/key :dart,
      :orcpub.entity/value
      {:orcpub.dnd.e5.character.equipment/quantity 10,
       :orcpub.dnd.e5.character.equipment/equipped? true,
       :orcpub.dnd.e5.character.equipment/class-starting-equipment?
       true}}],
    :background
    {:orcpub.entity/key :custom,
     :orcpub.entity/value "Hermit",
     :orcpub.entity/options
     {:skill-proficiency
      [{:orcpub.entity/key :medicine} {:orcpub.entity/key :religion}],
      :tool-language-proficiencies
      {:orcpub.entity/key :two-tools,
       :orcpub.entity/options
       {:tool-proficiency
        [{:orcpub.entity/key :alchemists-supplies}
         {:orcpub.entity/key :herbalism-kit}]}}}}}})

(def paladin
  {:orcpub.entity/options
   {:ability-scores
    {:orcpub.entity/key :standard-scores,
     :orcpub.entity/value
     {:orcpub.dnd.e5.character/str 15,
      :orcpub.dnd.e5.character/dex 13,
      :orcpub.dnd.e5.character/con 12,
      :orcpub.dnd.e5.character/int 10,
      :orcpub.dnd.e5.character/wis 8,
      :orcpub.dnd.e5.character/cha 14}},
    :class
    [{:orcpub.entity/key :paladin,
      :orcpub.entity/options
      {:levels [{:orcpub.entity/key :level-1}],
       :skill-proficiency
       [{:orcpub.entity/key :athletics}
        {:orcpub.entity/key :intimidation}],
       :starting-equipment-equipment-pack
       {:orcpub.entity/key :explorers-pack},
       :starting-equipment-melee-weapon
       {:orcpub.entity/key :five-javelins},
       :starting-equipment-weapons
       {:orcpub.entity/key :martial-weapon-and-shield,
        :orcpub.entity/options
        {:starting-equipment-martial-weapon
         {:orcpub.entity/key :longsword}}}}}],
    :background
    {:orcpub.entity/key :custom,
     :orcpub.entity/value "Noble",
     :orcpub.entity/options
     {:skill-proficiency
      [{:orcpub.entity/key :history} {:orcpub.entity/key :persuasion}],
      :tool-language-proficiencies
      {:orcpub.entity/key :one-tool-one-language,
       :orcpub.entity/options
       {:languages [{:orcpub.entity/key :elvish}],
        :tool-proficiency {:orcpub.entity/key :dragonchess-set}}}}},
    :armor
    [{:orcpub.entity/key :chain-mail,
      :orcpub.entity/value
      {:orcpub.dnd.e5.character.equipment/quantity 1,
       :orcpub.dnd.e5.character.equipment/equipped? true,
       :orcpub.dnd.e5.character.equipment/class-starting-equipment?
       true}}]}})

(def ranger
  {:orcpub.entity/options
 {:ability-scores
  {:orcpub.entity/key :standard-scores,
   :orcpub.entity/value
   {:orcpub.dnd.e5.character/str 12,
    :orcpub.dnd.e5.character/dex 15,
    :orcpub.dnd.e5.character/con 13,
    :orcpub.dnd.e5.character/int 8,
    :orcpub.dnd.e5.character/wis 14,
    :orcpub.dnd.e5.character/cha 10}},
  :class
  [{:orcpub.entity/key :ranger,
    :orcpub.entity/options
    {:levels [{:orcpub.entity/key :level-1}],
     :favored-enemy-1 {:orcpub.entity/key :two-humanoid-races},
     :favored-enemy-race
     [{:orcpub.entity/key :orc} {:orcpub.entity/key :goblin}],
     :favored-terrain [{:orcpub.entity/key :forest}],
     :skill-proficiency
     [{:orcpub.entity/key :survival}
      {:orcpub.entity/key :nature}
      {:orcpub.entity/key :animal-handling}],
     :starting-equipment-armor {:orcpub.entity/key :scale-mail},
     :starting-equipment-equipment-pack
     {:orcpub.entity/key :explorers-pack},
     :starting-equipment-melee-weapon
     {:orcpub.entity/key :two-shortswords}}}],
  :weapons
  [{:orcpub.entity/key :longbow,
    :orcpub.entity/value
    {:orcpub.dnd.e5.character.equipment/quantity 1,
     :orcpub.dnd.e5.character.equipment/equipped? true,
     :orcpub.dnd.e5.character.equipment/class-starting-equipment?
     true}}],
  :equipment
  [{:orcpub.entity/key :quiver,
    :orcpub.entity/value
    {:orcpub.dnd.e5.character.equipment/quantity 1,
     :orcpub.dnd.e5.character.equipment/equipped? true,
     :orcpub.dnd.e5.character.equipment/class-starting-equipment?
     true}}
   {:orcpub.entity/key :arrow,
    :orcpub.entity/value
    {:orcpub.dnd.e5.character.equipment/quantity 20,
     :orcpub.dnd.e5.character.equipment/equipped? true,
     :orcpub.dnd.e5.character.equipment/class-starting-equipment?
     true}}]}})

(def rogue
  {:orcpub.entity/options
   {:ability-scores
    {:orcpub.entity/key :standard-scores,
     :orcpub.entity/value
     {:orcpub.dnd.e5.character/str 8,
      :orcpub.dnd.e5.character/dex 15,
      :orcpub.dnd.e5.character/con 10,
      :orcpub.dnd.e5.character/int 14,
      :orcpub.dnd.e5.character/wis 12,
      :orcpub.dnd.e5.character/cha 13}},
    :class
    [{:orcpub.entity/key :rogue,
      :orcpub.entity/options
      {:levels [{:orcpub.entity/key :level-1}],
       :skill-proficiency
       [{:orcpub.entity/key :stealth}
        {:orcpub.entity/key :perception}
        {:orcpub.entity/key :acrobatics}
        {:orcpub.entity/key :investigation}],
       :expertise {:orcpub.entity/key :one-skill-thieves-tools},
       :starting-equipment-melee-weapon {:orcpub.entity/key :shortsword},
       :starting-equipment-additional-weapon
       {:orcpub.entity/key :shortbow-quiver-20-arrows},
       :starting-equipment-equipment-pack
       {:orcpub.entity/key :burglers-pack}}}],
    :background
    {:orcpub.entity/key :custom,
     :orcpub.entity/value "Charlatan",
     :orcpub.entity/options
     {:skill-proficiency
      [{:orcpub.entity/key :sleight-of-hand}
       {:orcpub.entity/key :deception}],
      :tool-language-proficiencies
      {:orcpub.entity/key :two-tools,
       :orcpub.entity/options
       {:tool-proficiency
        [{:orcpub.entity/key :disguise-kit}
         {:orcpub.entity/key :forgery-kit}]}}}},
    :weapons
    [{:orcpub.entity/key :dagger,
      :orcpub.entity/value
      {:orcpub.dnd.e5.character.equipment/quantity 2,
       :orcpub.dnd.e5.character.equipment/equipped? true,
       :orcpub.dnd.e5.character.equipment/class-starting-equipment?
       true}}],
    :armor
    [{:orcpub.entity/key :leather,
      :orcpub.entity/value
      {:orcpub.dnd.e5.character.equipment/quantity 1,
       :orcpub.dnd.e5.character.equipment/equipped? true,
       :orcpub.dnd.e5.character.equipment/class-starting-equipment?
       true}}],
    :equipment
    [{:orcpub.entity/key :thieves-tools,
      :orcpub.entity/value
      {:orcpub.dnd.e5.character.equipment/quantity 1,
       :orcpub.dnd.e5.character.equipment/equipped? true,
       :orcpub.dnd.e5.character.equipment/class-starting-equipment?
       true}}],
    :skill-expertise [{:orcpub.entity/key :stealth}]}})

(def sorcerer
  {:orcpub.entity/options
   {:ability-scores
    {:orcpub.entity/key :standard-scores,
     :orcpub.entity/value
     {:orcpub.dnd.e5.character/str 8,
      :orcpub.dnd.e5.character/dex 13,
      :orcpub.dnd.e5.character/con 14,
      :orcpub.dnd.e5.character/int 12,
      :orcpub.dnd.e5.character/wis 10,
      :orcpub.dnd.e5.character/cha 15}},
    :class
    [{:orcpub.entity/key :sorcerer,
      :orcpub.entity/options
      {:levels
       [{:orcpub.entity/key :level-1,
         :orcpub.entity/options
         {:sorcerous-origin
          {:orcpub.entity/key :draconic-bloodline,
           :orcpub.entity/options
           {:draconic-ancestry-type {:orcpub.entity/key :red}}}}}],
       :sorcerer-cantrips-known
       [{:orcpub.entity/key :light}
        {:orcpub.entity/key :prestidigitation}
        {:orcpub.entity/key :ray-of-frost}
        {:orcpub.entity/key :shocking-grasp}],
       :sorcerer-spells-known
       [{:orcpub.entity/key :shield}
        {:orcpub.entity/key :magic-missile}],
       :skill-proficiency
       [{:orcpub.entity/key :arcana} {:orcpub.entity/key :deception}],
       :starting-equipment-equipment-pack
       {:orcpub.entity/key :explorers-pack},
       :starting-equipment-spellcasting-equipment
       {:orcpub.entity/key :component-pouch},
       :starting-equipment-weapon
       {:orcpub.entity/key :light-crossbow}}}],
    :weapons
    [{:orcpub.entity/key :dagger,
      :orcpub.entity/value
      {:orcpub.dnd.e5.character.equipment/quantity 2,
       :orcpub.dnd.e5.character.equipment/equipped? true,
       :orcpub.dnd.e5.character.equipment/class-starting-equipment?
       true}}],
    :background
    {:orcpub.entity/key :custom,
     :orcpub.entity/value "Hermit",
     :orcpub.entity/options
     {:skill-proficiency
      [{:orcpub.entity/key :medicine} {:orcpub.entity/key :religion}],
      :tool-language-proficiencies
      {:orcpub.entity/key :one-tool-one-language,
       :orcpub.entity/options
       {:languages [{:orcpub.entity/key :elvish}],
        :tool-proficiency {:orcpub.entity/key :herbalism-kit}}}}}}})

(def warlock
  {:orcpub.entity/options
   {:ability-scores
    {:orcpub.entity/key :standard-scores,
     :orcpub.entity/value
     {:orcpub.dnd.e5.character/str 12,
      :orcpub.dnd.e5.character/dex 13,
      :orcpub.dnd.e5.character/con 14,
      :orcpub.dnd.e5.character/int 8,
      :orcpub.dnd.e5.character/wis 10,
      :orcpub.dnd.e5.character/cha 15}},
    :class
    [{:orcpub.entity/key :warlock,
      :orcpub.entity/options
      {:levels
       [{:orcpub.entity/key :level-1,
         :orcpub.entity/options
         {:otherworldly-patron {:orcpub.entity/key :the-fiend}}}],
       :warlock-cantrips-known
       [{:orcpub.entity/key :eldritch-blast}
        {:orcpub.entity/key :chill-touch}],
       :warlock-spells-known
       [{:orcpub.entity/key :burning-hands}
        {:orcpub.entity/key :protection-from-evil-and-good}],
       :skill-proficiency
       [{:orcpub.entity/key :arcana} {:orcpub.entity/key :intimidation}],
       :starting-equipment-equipment-pack
       {:orcpub.entity/key :dungeoneers-pack},
       :starting-equipment-simple-weapon
       {:orcpub.entity/key :quarterstaff},
       :starting-equipment-spellcasting-equipment
       {:orcpub.entity/key :component-pouch},
       :starting-equipment-weapon
       {:orcpub.entity/key :light-crossbow-20-bolts}}}],
    :background
    {:orcpub.entity/key :custom,
     :orcpub.entity/value "Charlatan",
     :orcpub.entity/options
     {:skill-proficiency
      [{:orcpub.entity/key :deception}
       {:orcpub.entity/key :sleight-of-hand}],
      :tool-language-proficiencies
      {:orcpub.entity/key :two-tools,
       :orcpub.entity/options
       {:tool-proficiency
        [{:orcpub.entity/key :disguise-kit}
         {:orcpub.entity/key :forgery-kit}]}}}},
    :weapons
    [{:orcpub.entity/key :dagger,
      :orcpub.entity/value
      {:orcpub.dnd.e5.character.equipment/quantity 2,
       :orcpub.dnd.e5.character.equipment/equipped? true,
       :orcpub.dnd.e5.character.equipment/class-starting-equipment?
       true}}],
    :armor
    [{:orcpub.entity/key :leather,
      :orcpub.entity/value
      {:orcpub.dnd.e5.character.equipment/quantity 1,
       :orcpub.dnd.e5.character.equipment/equipped? true,
       :orcpub.dnd.e5.character.equipment/class-starting-equipment?
       true}}]}})

(def wizard
  {:orcpub.entity/options
   {:ability-scores
    {:orcpub.entity/key :standard-scores,
     :orcpub.entity/value
     {:orcpub.dnd.e5.character/str 8,
      :orcpub.dnd.e5.character/dex 14,
      :orcpub.dnd.e5.character/con 13,
      :orcpub.dnd.e5.character/int 15,
      :orcpub.dnd.e5.character/wis 12,
      :orcpub.dnd.e5.character/cha 10}},
    :class
    [{:orcpub.entity/key :wizard,
      :orcpub.entity/options
      {:levels [{:orcpub.entity/key :level-1}],
       :wizard-cantrips-known
       [{:orcpub.entity/key :mage-hand}
        {:orcpub.entity/key :light}
        {:orcpub.entity/key :ray-of-frost}],
       :wizard-spells-known
       [{:orcpub.entity/key :burning-hands}
        {:orcpub.entity/key :charm-person}
        {:orcpub.entity/key :feather-fall}
        {:orcpub.entity/key :mage-armor}
        {:orcpub.entity/key :magic-missile}
        {:orcpub.entity/key :sleep}],
       :skill-proficiency
       [{:orcpub.entity/key :investigation}
        {:orcpub.entity/key :insight}],
       :starting-equipment-equipment-pack
       {:orcpub.entity/key :scholars-pack},
       :starting-equipment-spellcasting-equipment
       {:orcpub.entity/key :component-pouch}}}],
    :equipment
    [{:orcpub.entity/key :spellbook,
      :orcpub.entity/value
      {:orcpub.dnd.e5.character.equipment/quantity 1,
       :orcpub.dnd.e5.character.equipment/equipped? true,
       :orcpub.dnd.e5.character.equipment/class-starting-equipment?
       true}}],
    :background
    {:orcpub.entity/key :custom,
     :orcpub.entity/value "Sage",
     :orcpub.entity/options
     {:skill-proficiency
      [{:orcpub.entity/key :arcana} {:orcpub.entity/key :history}],
      :tool-language-proficiencies
      {:orcpub.entity/key :two-languages,
       :orcpub.entity/options
       {:languages
        [{:orcpub.entity/key :elvish}
         {:orcpub.entity/key :dwarvish}]}}}}}})

(def questions
  [

   {:key :good-or-evil
    :question "Is your character a hero or a villain?"
    :answers [{:answer "Hero"
               :tag :good}
              {:answer "Villan"
               :tag :evil}
              {:answer "Both or neither (things aren't so black and white)"
               :tag :neutral}]}

   {:key :law-or-chaos
    :question "How does your character feel about following laws, rules, norms, etc?"
    :answers [{:answer "Rules should always be followed"
               :tag :lawful
               :update-fn (set-law-fn :lawful-good :lawful-neutral :lawful-evil)}
              {:answer "In many circumstances rules need to be ignored"
               :tag :neutral
               :update-fn (set-law-fn :neutral-good :neutral :neutral-evil)}
              {:answer "I don't care about rules"
               :tag :chaotic
               :update-fn (set-law-fn :chaotic-good :chaotic-neutral :chaotic-evil)}]}

   {:key :sex
    :question "Is your character male, female, or neither"
    :answers [{:answer "Male"
               :tag :male
               :update-fn (set-sex-fn "male")}
              {:answer "Female"
               :tag :female
               :update-fn (set-sex-fn "female")}
              {:answer "Other"
               :tag :androgenous
               :update-fn (set-sex-fn "other")}]}

   {:key :lotr-character
    :question "As far as your race goes, which Lord of the Rings character is your character most like?"
    :answers [{:answer "Aragorn, Eowyn, or Bard (Human)"
               :tag :human
               :update-fn (add-race-fn :human :tethyrian)}
              {:answer "Thorin or Gimli"
               :tag :dwarf
               :update-fn (fn [c] (assoc-in c
                                            [:orcpub.entity/options :race]
                                            {:orcpub.entity/key :dwarf,
                                             :orcpub.entity/options
                                             {:subrace {:orcpub.entity/key :hill-dwarf},
                                              :tool-proficiency {:orcpub.entity/key :smiths-tools}}}))}
              {:answer "Legolas, Galadriel, Arwen, or Elrond"
               :tag :elf
               :update-fn (add-race-fn :elf :high-elf)}
              {:answer "Bilbo, Mrs. Proudfoot, or Frodo"
               :tag :halfling
               :update-fn (add-race-fn :halfling :lightfoot)}
              {:answer "An orc"
               :tag :half-orc
               :update-fn (add-race-fn :half-orc)}
              {:answer "Smaug"
               :tag :dragonborn
               :update-fn (add-race-fn :dragonborn)}
              {:answer "Sauron"
               :tag :tiefling
               :update-fn (add-race-fn :tiefling)}
              {:answer "What's Lord of the Rings?"
               :tag :whats-lotr}]}

   {:key :human-or-exotic
    :filter #(= :whats-lotr (:lotr-character %))
    :question "Is your character human or a more exotic race?"
    :answers [{:answer "Usual"
               :tag :human
               :update-fn (add-race-fn :human :tethyrian)}
              {:answer "Exotic"
               :tag :exotic}]}

   {:key :race-description
    :filter #(= :exotic (:human-or-exotic %))
    :question "Which best describes your character?"
    :answers [{:answer "Similar to a human, but more slender and graceful and with magnificient pointy ears (an elf)"
               :tag :elf
               :update-fn (add-race-fn :elf :high-elf)}
              {:answer "Similar to a human, but shorter, stouter, and is likely to have a glorious beard (a dwarf)"
               :tag :dwarf
               :update-fn (add-race-fn :dwarf :hill-dwarf)}
              {:answer "Kinda like if you shrunk a human an gave him lush hairy feet (a hobbit)"
               :tag :halfling
               :update-fn (add-race-fn :halfling :lightfoot-halfling)}
              {:answer "Like if a dragon were to shrink to a size and shape similar to a human (a dragonborn)"
               :tag :dragonborn
               :update-fn (add-race-fn :dragonborn)}
              {:answer "Half human, half devil (a tiefling)"
               :tag :tiefling
               :update-fn (add-race-fn :tiefling)}
              {:answer "Half human, half big ugly green monster (a half-orc)"
               :tag :half-orc
               :update-fn (add-race-fn :half-orc)}]}

   {:key :combat-abilities
    :question "In battle, what abilities do your character prefer to rely on?"
    :answers [{:answer "Weapons and might (a warrior)"
               :tag :warrior}
              {:answer "Spellcasting"
               :tag :spellcaster}
              {:answer "Stealth and cunning (like a burglar or thief)"
               :tag :rogue
               :update-fn (set-class-fn rogue)}]}

   {:key :spellcaster-type
    :filter #(= :spellcaster (:combat-abilities %))
    :question "How did your character gain his/her spellcasting abilities?"
    :answers [{:answer "From long hours of study"
               :tag :wizard
               :update-fn (set-class-fn wizard)}
              {:answer "From a god"
               :tag :cleric
               :update-fn (set-class-fn cleric)}
              {:answer "From the power of song"
               :tag :bard
               :update-fn (set-class-fn bard)}
              {:answer "From a dragon ancestor"
               :tag :sorcerer
               :update-fn (set-class-fn sorcerer)}
              {:answer "From the forces of nature"
               :tag :druid
               :update-fn (set-class-fn druid)}
              {:answer "From a pact with a devil"
               :tag :warlock
               :update-fn (set-class-fn warlock)}]}

   {:key :warrior-type
    :filter #(= :warrior (:combat-abilities %))
    :question "What type of warrior is your character?"
    :answers [{:answer "A warrior of impeccable training an skill"
               :tag :fighter}
              {:answer "A tribal warrior of primal instinct"
               :tag :barbarian
               :update-fn (set-class-fn barbarian)}
              {:answer "A warrior of duty and devotion, aided by godly magic"
               :tag :paladin
               :update-fn (set-class-fn paladin)}
              {:answer "A mystical warrior, skilled with martial arts, armed or unarmed"
               :tag :monk
               :update-fn (set-class-fn monk)}
              {:answer "An independent warrior of the wilderness"
               :tag :ranger
               :update-fn (set-class-fn ranger)}]}

   {:key :fighter-type
    :filter #(= :fighter (:warrior-type %))
    :question "Does your character prefer to fight up close or from afar?"
    :answers [{:answer "My character likes to look an enemy in the eye in combat"
               :tag :melee-fighter
               :update-fn (set-class-fn swordsman)}
              {:answer "My character is a sharpshooter"
               :tag :ranged-fighter
               :update-fn (set-class-fn archer)}
              {:answer "My character likes to be well balanced"
               :tag :balanced-fighter
               :update-fn (set-class-fn balanced-fighter)}]}])

(defn add-answer [v {:keys [key]} {:keys [answer tag update-fn]}]
  (cond-> v
      true (update :answers assoc key tag)
      true (update :tags (fn [t] (if t (conj tag) #{tag})))
      update-fn (update :char update-fn (:answers v))))

(defn next-question [{:keys [answers tags]}]
  (first
   (sequence
    (comp
     (map
      (fn [{:keys [key filter] :as q}]
        (let [f (fn [_]
                  (nil? (key answers)))]
          (assoc
           q
           :filter
           (if filter
             (fn [x]
               (and (f x)
                    (filter x)))
             f)))))
     (filter #((:filter %) answers)))
    questions)))

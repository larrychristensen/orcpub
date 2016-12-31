(ns orcpub.entity-test
  (:require [clojure.test :refer :all]
            [clojure.spec :as spec]
            [clojure.spec.test :as stest]
            [orcpub.entity :as entity]
            [orcpub.template :as t]
            [orcpub.dnd.e5.modifiers :as modifiers]
            [orcpub.dnd.e5.character :as char5e]))

(def abilities
  [{:name "Strength"
    :path :str
    :type :integer}
   {:name "Dexterity"
    :path :dex
    :type :integer}
   {:name "Constitution"
    :path :con
    :type :integer}
   {:name "Intelligence"
    :path :int
    :type :integer}
   {:name "Wisdom"
    :path :wis
    :type :integer}
   {:name "Charisma"
    :path :cha
    :type :integer}])

(def feats
  [{:name "Fire Resister"
    :modifiers [(modifiers/resistances :fire)]}
   {:name "Elemental Adept"
    :restrictions [{:path [:spellcaster?]
                    :value true}]}
   {:name "Grappler"
    :restrictions [{:path [:abilities :str]
                    :min 13}]
    :traits [{:name "When grappling a creature, you have advantage on attack rolls against it"}]}
   {:name "Durable"
    :modifiers [(modifiers/ability :con 1)]}
   {:name "Athlete"}])

(defn total-levels [character]
  (apply + (map val (:levels character))))

(defn at-least-level? [level]
  (fn [character]
    (>= (total-levels character) level)))

(def wizard-cantrips
  [:acid-splash :blade-ward :light :true-strike])

(def wizard-1st-level-spells
  [:alarm :burning-hands :charm-person :grease :identity :shield])

(def wizard-2nd-level-spells
  [:alarm :burning-hands])


(def elf
  {:name "Elf"
   :key :elf
   :selections {:subrace {:name "Subrace"
                          :min 1
                          :max 1
                          :options [{:name "High Elf"
                                     :key :high-elf
                                     :selections {:cantrips {:name "Cantrips"
                                                             :max 1
                                                             :options wizard-cantrips}}}
                                    {:name "Dark Elf (Drow)"
                                     :key :dark-elf
                                     :modifiers [(modifiers/spells-known 0 :dancing-lights)
                                                 (assoc
                                                  (modifiers/spells-known 1 :faerie-fire)
                                                  :prereqs
                                                  [(at-least-level? 3)])
                                                 (assoc
                                                  (modifiers/spells-known 2 :darkness)
                                                  :prereqs
                                                  [(at-least-level? 5)])]}]}}})

(def dwarf
  {:name "Dwarf"
   :modifiers [(modifiers/ability :con 2)
               (modifiers/speed 25)
               (modifiers/resistances :poison)
               (modifiers/darkvision)]
   :selections [{:name "Subrace"
                 :min 1
                 :max 1
                 :options [{:name "Hill Dwarf"
                            :modifiers [(modifiers/ability :wis 1)]}]}]})

(def arcane-traditions
  [{:name "Arcane Tradition"
    :min 1
    :max 1
    :options [{:name "School of Evocation"
               :modifiers [(modifiers/trait "Evocation Savant")
                           (modifiers/trait "Sculpt Spells")]}]}])

(def ability-score-improvement
  {:name "Ability Score Improvement"
   :max 1
   :options (concat
             feats
             (map
              (fn [ability]
                {:name (:name ability)
                 :modifiers [(modifiers/ability (:path ability) 1)]})
              abilities))})

(def wizard-levels
  [{:modifiers [(modifiers/level :wizard)
                (modifiers/spell-slots 1 2)
                (modifiers/proficiency-bonus 2)
                (modifiers/trait "Arcane Recovery")]
    :selections [{:name "Cantrips Known"
                  :max 3
                  :options wizard-cantrips}]}
   {:modifiers [(modifiers/level :wizard)
                (modifiers/spell-slots 1 1)
                (modifiers/trait "Arcane Recovery")]
    :selections [{:name "Arcane Tradition"
                  :path :arcane-tradition
                  :key :arcane-tradition
                  :min 1
                  :max 1
                  :options :arcane-tradition}]}
   {:modifiers [(modifiers/level :wizard)
                (modifiers/spell-slots 1 1)
                (modifiers/spell-slots 2 2)]
    :selections [ability-score-improvement]}])

(defn has-spell-slots? [spell-level]
  (fn [character]
    (let [slots (get-in character [:spell-slots spell-level])]
      (and slots (pos? slots)))))

(def wizard
  {:name "Wizard"
   :key :wizard
   :modifiers [(modifiers/saving-throws :int :wis)]
   ::selections {:level {:name "Level"
                         :path :level
                         :min 1
                         :max 20
                         :options wizard-levels}
                 :spells-1 {:name "1st Level Spells Known"
                            :path [:spells-known 1]
                            :options wizard-1st-level-spells}
                 :spells-2 {:name "2nd Level Spells Known"
                            :path [:spells-known 2]
                            :prereqs [(has-spell-slots? 2)]
                            :options wizard-2nd-level-spells}}})

(spec/def ::name string?)
(spec/def ::key keyword)

(def dnd-5e-attributes
  (concat [{:name "Name"
            :key :name}
           {:name "Race"
            :key :race
            :type :integer}
           {:name "Subrace"
            :key :subrace}
           {:name "Background"
            :key :background}
           {:name "Feats"
            :key :feats
            :type :list}]
          abilities))

(defn ability-modifier [ability]
  (fn [character]
    (int (/ (- 10 (ability character)) 2))))

(def dnd-5e-derived-attributes
  [{:name "Strength Modifier"
    :key :str-modifier
    :value (ability-modifier :str)}
   {:name "Dexterity Modifier"
    :key :dex-modifier
    :value (ability-modifier :dex)}
   {:name "Constitution Modifier"
    :key :con-modifier
    :value (ability-modifier :con)}
   {:name "Intelligence Modifier"
    :key :int-modifier
    :value (ability-modifier :int)}
   {:name "Wisdom Modifier"
    :key :wis-modifier
    :value (ability-modifier :wis)}
   {:name "Charisma Modifier"
    :key :cha-modifier
    :value (ability-modifier :cha)}
   {:name "Initiative"
    :key :initiative
    :value :dex-modifier}])

(def classes
  {:name "Class"
   :key :class
   :min 1
   :max 1
   ::options {::wizard wizard}})

(def races
  {:race "Race"
   :key :race
   :min 1
   :max 1
   ::options {::elf elf
              ::dwarf dwarf}})

(def dnd-5e-char-template
  {::attributes dnd-5e-attributes
   ::derived-attributes dnd-5e-derived-attributes
   ::selections {::class classes
                 ::races races}})

(get-in dnd-5e-char-template [::selections ::class ::options ::wizard])

(def example-character-2
  {::character-type :dnd-5e
   ::abilities {:str 9 :dex 10 :con 12 :int 5 :wis 18 :cha 12}
   ::options {:race {:key :elf
                    :options {:subrace {:high-elf {:options {:cantrip :light}}}}}
             :classes [{:key :wizard
                        :options {:skills [:arcana :history]
                                  :levels {1 {:options {:cantrips [{:key :acid-splash}
                                                                 {:key :true-strike}]
                                                      :spells [{:key :alarm}
                                                               {:key :burning-hands}
                                                               {:key :charm-person}
                                                               {:key :grease}
                                                               {:key :identify}
                                                               {:key :shield}]}}
                                           2 {:options {:subclass {:key :school-of-evocation}
                                                      :hit-points {:key :roll
                                                                   :value 1}}}
                                           3 {:options {:ability-increase {:key :str}
                                                      :hit-points {:key :mean
                                                                   :value 4}
                                                      :spells [{:key :sleep}
                                                               {:key :silent-image}]}}}}}
                       {:name :rogue
                        :options {:subclass {:key :thief}
                                  :levels [{:key 1
                                            :options {:expertise [{:key :stealth}
                                                                  {:key :thieves-tools}]}}]}}]}})

(def example-character-3
  {::character-type :dnd-5e
   ::abilities {:str 9 :dex 10 :con 12 :int 5 :wis 18 :cha 12}
   ::options {:race {::key :elf
                     ::options {:subrace {::key :high-elf
                                          ::options {:cantrip :light}}}}
              :classes [{::key :wizard
                         ::options {:skills [:arcana :history]
                                    :levels [{::key 1
                                              ::options {:cantrips [:acid-splash
                                                                    :true-strike]
                                                         :spells [:alarm
                                                                  :burning-hands
                                                                  :charm-person
                                                                  :grease
                                                                  :identify
                                                                  :shield]}}
                                             {::key 2
                                              ::options {:subclass :school-of-evocation
                                                         :hit-points {::key :roll
                                                                      :value 1}}}
                                             {::key 3
                                              ::options {:ability-increase :str
                                                         :hit-points {::key :mean
                                                                      :value 4}
                                                         :spells [:sleep
                                                                  :silent-image]}}]}}]}})

(def example-character-4
  {::options {:race {::key :elf
                     ::options {:subrace {::key :high-elf
                                          ::options {:cantrip {::key :light}}}}}
              :levels [{::key :wizard-1
                        ::options {:cantrips [{::key :acid-splash}
                                              {::key :true-strike}]
                                   :spells [{::key :alarm}
                                            {::key :burning-hands}
                                            {::key :charm-person}
                                            {::key :grease}
                                            {::key :identify}
                                            {::key :shield}]}}
                       {::key :rogue-1-multiclass
                        ::options {:expertise [{::key :stealth}
                                               {::key :thieves-tools}]}}
                       {::key :wizard-2
                        ::options {:subclass {::key :school-of-evocation}
                                   :hit-points {::key :roll
                                                :value 1}}}]}})

(declare flatten-options-aux)

(defn flatten-sequential [path value]
  (map
   (fn [v]
     (if (keyword? v)
       (conj path v)
       (let [new-path (conj path (::key v))]
         (if (seq (::options v))
           (flatten-options-aux new-path v)
           new-path))))
   value))

(defn flatten-option [path [option-key option-value]]
  (let [new-path (conj path option-key)]
    (cond (map? option-value) (flatten-options-aux (conj new-path (::key option-value)) option-value)
          (sequential? option-value) (flatten-sequential new-path option-value)
          (keyword? option-value) (conj new-path option-value))))

(defn flatten-options-aux [path entity]
  (let [options (::options entity)]
    (if (seq options)
      (map
       (partial flatten-option path)
       options))))

(defn flatten-options [entity]
  (flatten-options-aux [] entity))

(declare add-paths-aux)

(defn add-paths-to-sequence [path value]
  (map
   (fn [v]
     (if (keyword? v)
       {::path (conj path v)
        ::key v}
       (let [new-path (conj path (::key v))]
         (if (seq (::options v))
           (add-paths-aux new-path v)
           new-path))))
   value))

(defn add-paths-to-option [path [option-key option-value]]
  (let [new-path (conj path option-key)]
    [option-key
     (cond (map? option-value)
           (let [option-path (conj new-path (::key option-value))]
             (add-paths-aux option-path (assoc option-value ::path option-path)))
           (sequential? option-value) (add-paths-to-sequence new-path option-value)
           (keyword? option-value) {::path (conj new-path option-value)
                                    ::key option-value})]))

(defn add-paths-aux [path entity]
  (update entity ::options
          #(into {} (map (partial add-paths-to-option path) (seq %)))))

(defn add-paths [entity]
  (add-paths-aux [] entity))

(defn build-entity [entity template]
  entity)

(defn char-option [key & [options]]
  (cond-> {}
    key (assoc ::entity/key key)
    options (assoc ::entity/options options)))

(def character
  {::entity/options {:ability-score-variant {::entity/key :standard-rolls
                                             ::entity/value (char5e/abilities 12 13 14 15 16 17)}
                     :race {::entity/key :elf
                            ::entity/options {:subrace {::entity/key :high-elf
                                                        ::entity/options {:cantrip {::entity/key :light}}}}}
                     :levels [{::entity/key :wizard-1
                               ::entity/options {:cantrips-known {::entity/key :acid-splash}
                                                 :hit-points {::entity/key :max}}}
                              {::entity/key :wizard-2
                               ::entity/options {:arcane-tradition {::entity/key :school-of-evocation}
                                                 :hit-points {::entity/key :roll
                                                              ::entity/value 3}}}]}})

(spec/explain-data ::entity/raw-entity character)

(def wizard-cantrip-options
  (map
   (fn [key]
     {::t/key key
      ::t/name (name key)
      ::t/modifiers [(modifiers/spells-known 0 key)]})
   wizard-cantrips))

(def arcane-tradition-options
  [(t/option
    "School of Evocation"
    nil
    [(modifiers/trait "Evocation Savant")
     (modifiers/trait "Sculpt Spells")])])

(def template
  {::t/selections
   [(t/selection
     "Ability Score Variant"
     [(t/option
       "Standard Rolls"
       []
       [(modifiers/abilities nil)])])
    (t/selection
     "Race"
     [(t/option
       "Elf"
       [(t/selection
         "Subrace"
         [(t/option
           "High Elf"
           [(t/selection
             "Cantrip"
             wizard-cantrip-options)]
           [(modifiers/ability ::char5e/int 1)])])]
       [(modifiers/ability ::char5e/dex 2)])])
    (t/selection
     "Levels"
     [(t/option
       "Wizard 1"
       [(t/selection
         "Cantrips Known"
         wizard-cantrip-options)
        (t/selection
         "Hit Points"
         [(t/option
           "Max"
           []
           [(modifiers/max-hit-points 6)])])]
       [(modifiers/saving-throws ::char5e/int ::char5e/wis)
        (modifiers/level :wizard)])
      (t/option
       "Wizard 2"
       [(t/selection
         "Arcane Tradition"
         arcane-tradition-options)
        (t/selection
         "Hit Points"
         [(t/option
           "Roll"
           []
           [(modifiers/max-hit-points nil)])])]
       [(modifiers/level :wizard)])])]})

(clojure.pprint/pprint (t/make-modifier-map template))

(spec/explain-data ::t/template template)

(stest/instrument `t/make-modifier-map)

(deftest test-build
  (let [built (entity/build character (t/make-modifier-map template))]
    (is (= (::char5e/spells-known built) {0 [:light :acid-splash]}))
    (is (= (::char5e/savings-throws built) [::char5e/int ::char5e/wis]))
    (is (= (::char5e/traits built) [{:name "Evocation Savant"} {:name "Sculpt Spells"}]))
    (is (= (::char5e/abilities built) (char5e/abilities 12 13 16 16 16 17)))
    (is (= (::char5e/max-hit-points built) 9))
    (is (= (::char5e/levels built) {:wizard 2}))))


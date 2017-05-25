(ns orcpub.entity-test
  (:require [clojure.test :refer :all]
            [clojure.spec :as spec]
            [clojure.spec.test :as stest]
            [orcpub.entity.strict :as e]
            [orcpub.entity :as entity]
            [orcpub.entity-spec :as es]
            [orcpub.template :as t]
            [orcpub.dnd.e5.modifiers :as modifiers]
            [orcpub.dnd.e5.character :as char5e]
            [orcpub.dnd.e5.template :as t5e]
            [orcpub.dnd.e5.character.equipment :as equip]))

(def character {::entity/options
                {:race
                 {::entity/key :elf,
                  ::entity/options {:subrace {:orcpub.entity/key :wood-elf}}}
                 :weapons
                 [{::entity/key :javelin,
                   ::entity/value
                   {::equip/quantity 4,
                    ::equip/equipped? true,
                    ::equip/class-starting-equipment? true}}]}
                ::entity/values
                {::char5e/eyes "green"
                 ::char5e/custom-equipment [{::equip/name "Scroll of Pedigree"
                                     ::equip/equipped? true
                                             ::equip/background-starting-equipment? true}]}})

(def strict-character {::e/selections [{::e/key :race
                                        ::e/option {::e/key :elf
                                                    ::e/selections [{::e/key :subrace
                                                                     ::e/option {::e/key :wood-elf}}]}}
                                       {::e/key :weapons
                                        ::e/options [{::e/key :javelin
                                                      ::e/map-value {::equip/quantity 4
                                                                     ::equip/equipped? true
                                                                     ::equip/class-starting-equipment? true}}]}]
                       ::e/values {::char5e/eyes "green"
                                   ::char5e/custom-equipment [{::equip/name "Scroll of Pedigree"
                                                               ::equip/equipped? true
                                                               ::equip/background-starting-equipment? true}]}})

(deftest test-to-strict
  (stest/instrument `entity/to-strict)
  (is (= (entity/to-strict character) strict-character))
  (stest/unstrument `entity/to-strict))

(deftest test-from-strict
  (stest/instrument `entity/from-strict)
  (is (= (entity/from-strict strict-character) character))
  (stest/unstrument `entity/from-strict))

(deftest test-template-option-map
  (let [selections [(assoc
                     (t/selection-cfg
                      {:name "Selection X"
                       :options [(t/option-cfg
                                  {:name "Option 1"})
                                 (t/option-cfg
                                  {:name "Option 2"})]})
                     ::entity/path
                     [:selection-x])
                    (assoc
                     (t/selection-cfg
                      {:name "Selection Y"
                       :options [(t/option-cfg
                                  {:name "Option 3"})
                                 (t/option-cfg
                                  {:name "Option 4"})]})
                     ::entity/path
                     [:selection-y])]
        expected {[:selection-x :option-1] (t/option-cfg
                                            {:name "Option 1"})
                  [:selection-x :option-2] (t/option-cfg
                                            {:name "Option 2"})
                  [:selection-y :option-3] (t/option-cfg
                                            {:name "Option 3"})
                  [:selection-y :option-4] (t/option-cfg
                                            {:name "Option 4"})}]
    (is (= (entity/make-template-option-map selections) expected))))

(def arcane-trickster
  {:orcpub.entity/options
   {:class
    [{:orcpub.entity/key :rogue,
      :orcpub.entity/options
      {:levels
       [{:orcpub.entity/key :level-1}
        {:orcpub.entity/key :level-2}
        {:orcpub.entity/key :level-3,
         :orcpub.entity/options
         {:roguish-archetype
          {:orcpub.entity/key :arcane-trickster,
           :orcpub.entity/options
           {:enchantment-or-illusion-spells-known
            [{:orcpub.entity/key :charm-person}],
            :cantrips-known
            [{:orcpub.entity/key :acid-splash}
             {:orcpub.entity/key :blade-ward}]}}}}]}}]}})

(deftest test-make-path-map
  (let [path-map (entity/make-path-map arcane-trickster)]
    (is (= path-map
           {:class
            {:rogue
             {:levels
              {:level-1 {},
               :level-2 {}
               :level-3
               {:roguish-archetype
                {:arcane-trickster
                 {:enchantment-or-illusion-spells-known {:charm-person {}},
                  :cantrips-known {:acid-splash {}, :blade-ward {}}}}}}}}}))))

(deftest get-all-selections-aux-2
  (let [selections (entity/get-all-selections-aux-2 t5e/template (entity/make-path-map arcane-trickster))
        ref-set (into #{} (map ::t/ref) selections)]
    (is (ref-set [:class
                  :rogue
                  :levels
                  :level-3
                  :roguish-archetype
                  :arcane-trickster
                  :enchantment-or-illusion-spells-known]))))

(deftest make-template-option-map
  (let [selections (entity/get-all-selections-aux-2 t5e/template (entity/make-path-map arcane-trickster))
        template-option-map (entity/make-template-option-map selections)]
    (is (template-option-map [:class
                              :rogue
                              :levels
                              :level-3
                              :roguish-archetype
                              :arcane-trickster
                              :enchantment-or-illusion-spells-known
                              :charm-person]))))

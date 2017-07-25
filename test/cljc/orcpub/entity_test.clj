(ns orcpub.entity-test
  (:require [clojure.test :refer :all]
            [clojure.spec.alpha :as spec]
            [clojure.spec.test.alpha :as stest]
            [clojure.data :refer [diff]]
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
  (let [selections (entity/get-all-selections-aux-2
                    (t5e/template
                     (t5e/template-selections nil nil nil))
                    (entity/make-path-map arcane-trickster))
        ref-set (into #{} (map ::t/ref) selections)]
    (is (ref-set [:class
                  :rogue
                  :levels
                  :level-3
                  :roguish-archetype
                  :arcane-trickster
                  :enchantment-or-illusion-spells-known]))))

(deftest make-template-option-map
  (let [selections (entity/get-all-selections-aux-2
                    (t5e/template
                     (t5e/template-selections nil nil nil))
                    (entity/make-path-map arcane-trickster))
        template-option-map (entity/make-template-option-map selections)]
    (is (template-option-map [:class
                              :rogue
                              :levels
                              :level-3
                              :roguish-archetype
                              :arcane-trickster
                              :enchantment-or-illusion-spells-known
                              :charm-person]))))

(defn strict-round-trip [strict]
  (-> strict entity/from-strict entity/to-strict))

(deftest test-round-trip
  (let [strict '{:db/id 17592186054525, :orcpub.entity.strict/values {:db/id 17592186054622, :orcpub.dnd.e5.character/character-name "Sanchito", :orcpub.dnd.e5.character/player-name "Larry"}}]
    (is (= strict (strict-round-trip strict)))))

(deftest test-round-trip-2
  (let [strict {:db/id 17592186054624, :orcpub.entity.strict/selections [{:db/id 17592186054625, :orcpub.entity.strict/key :ability-scores, :orcpub.entity.strict/option {:db/id 17592186054626, :orcpub.entity.strict/key :standard-scores, :orcpub.entity.strict/map-value {:db/id 17592186054627, :orcpub.dnd.e5.character/str 15, :orcpub.dnd.e5.character/dex 14, :orcpub.dnd.e5.character/con 13, :orcpub.dnd.e5.character/int 12, :orcpub.dnd.e5.character/wis 10, :orcpub.dnd.e5.character/cha 8}}}]}]
    (is (= strict (strict-round-trip strict)))))

(deftest test-round-trip-3
  (let [strict '{:db/id 17592186054624, :orcpub.entity.strict/selections [{:db/id 17592186054625, :orcpub.entity.strict/key :ability-scores, :orcpub.entity.strict/option {:db/id 17592186054626, :orcpub.entity.strict/key :standard-scores, :orcpub.entity.strict/map-value {:db/id 17592186054627, :orcpub.dnd.e5.character/str 15, :orcpub.dnd.e5.character/dex 14, :orcpub.dnd.e5.character/con 13, :orcpub.dnd.e5.character/int 12, :orcpub.dnd.e5.character/wis 10, :orcpub.dnd.e5.character/cha 8}}} {:db/id 17592186054628, :orcpub.entity.strict/key :class, :orcpub.entity.strict/options [{:db/id 17592186054629, :orcpub.entity.strict/key :barbarian, :orcpub.entity.strict/selections [{:db/id 17592186054630, :orcpub.entity.strict/key :levels, :orcpub.entity.strict/options [{:db/id 17592186054631, :orcpub.entity.strict/key :level-1}]}]}]} {:db/id 17592186054632, :orcpub.entity.strict/key :weapons, :orcpub.entity.strict/options [{:db/id 17592186054633, :orcpub.entity.strict/key :javelin, :orcpub.entity.strict/map-value {:db/id 17592186054634, :orcpub.dnd.e5.character.equipment/quantity 4, :orcpub.dnd.e5.character.equipment/equipped? true, :orcpub.dnd.e5.character.equipment/class-starting-equipment? true}}]} {:db/id 17592186054635, :orcpub.entity.strict/key :equipment, :orcpub.entity.strict/options [{:db/id 17592186054636, :orcpub.entity.strict/key :explorers-pack, :orcpub.entity.strict/map-value {:db/id 17592186054637, :orcpub.dnd.e5.character.equipment/quantity 1, :orcpub.dnd.e5.character.equipment/equipped? true, :orcpub.dnd.e5.character.equipment/class-starting-equipment? true}}]}]}]
    (is (= strict (strict-round-trip strict)))))

(deftest test-round-trip-4
  (let [strict {:db/id 17592186055139, :orcpub.entity.strict/selections [{:db/id 17592186055143, :orcpub.entity.strict/key :class, :orcpub.entity.strict/options [{:db/id 17592186055144, :orcpub.entity.strict/key :barbarian, :orcpub.entity.strict/selections [{:db/id 17592186055145, :orcpub.entity.strict/key :levels, :orcpub.entity.strict/options [{:db/id 17592186055146, :orcpub.entity.strict/key :level-1} {:db/id 17592186055147, :orcpub.entity.strict/key :level-2, :orcpub.entity.strict/selections [{:db/id 17592186055148, :orcpub.entity.strict/key :hit-points, :orcpub.entity.strict/option {:db/id 17592186055149, :orcpub.entity.strict/key :roll, :orcpub.entity.strict/int-value 6}}]}]}]}]} {:db/id 17592186055150, :orcpub.entity.strict/key :weapons, :orcpub.entity.strict/options [{:db/id 17592186055151, :orcpub.entity.strict/key :javelin, :orcpub.entity.strict/map-value {:db/id 17592186055152, :orcpub.dnd.e5.character.equipment/quantity 4, :orcpub.dnd.e5.character.equipment/equipped? true, :orcpub.dnd.e5.character.equipment/class-starting-equipment? true}}]} {:db/id 17592186055153, :orcpub.entity.strict/key :equipment, :orcpub.entity.strict/options [{:db/id 17592186055154, :orcpub.entity.strict/key :explorers-pack, :orcpub.entity.strict/map-value {:db/id 17592186055155, :orcpub.dnd.e5.character.equipment/quantity 1, :orcpub.dnd.e5.character.equipment/equipped? true, :orcpub.dnd.e5.character.equipment/class-starting-equipment? true}}]}]}]
    (is (= strict (strict-round-trip strict)))))

(deftest test-round-trip-5
  (let [strict {:db/id 17592186055191, :orcpub.entity.strict/selections [{:db/id 17592186055192, :orcpub.entity.strict/key :ability-scores, :orcpub.entity.strict/option {:db/id 17592186055193, :orcpub.entity.strict/key :standard-scores, :orcpub.entity.strict/map-value {:db/id 17592186055194, :orcpub.dnd.e5.character/str 15, :orcpub.dnd.e5.character/dex 14, :orcpub.dnd.e5.character/con 13, :orcpub.dnd.e5.character/int 12, :orcpub.dnd.e5.character/wis 10, :orcpub.dnd.e5.character/cha 8}}} {:db/id 17592186055195, :orcpub.entity.strict/key :class, :orcpub.entity.strict/options [{:db/id 17592186055196, :orcpub.entity.strict/key :barbarian, :orcpub.entity.strict/selections [{:db/id 17592186055197, :orcpub.entity.strict/key :levels, :orcpub.entity.strict/options [{:db/id 17592186055198, :orcpub.entity.strict/key :level-1} {:db/id 17592186055199, :orcpub.entity.strict/key :level-2, :orcpub.entity.strict/selections [{:db/id 17592186055200, :orcpub.entity.strict/key :hit-points, :orcpub.entity.strict/option {:db/id 17592186055201, :orcpub.entity.strict/key :roll, :orcpub.entity.strict/int-value 11}}]} {:db/id 17592186055202, :orcpub.entity.strict/key :level-3, :orcpub.entity.strict/selections [{:db/id 17592186055203, :orcpub.entity.strict/key :hit-points, :orcpub.entity.strict/option {:db/id 17592186055204, :orcpub.entity.strict/key :manual-entry, :orcpub.entity.strict/int-value 33}}]}]}]}]} {:db/id 17592186055205, :orcpub.entity.strict/key :weapons, :orcpub.entity.strict/options [{:db/id 17592186055206, :orcpub.entity.strict/key :javelin, :orcpub.entity.strict/map-value {:db/id 17592186055207, :orcpub.dnd.e5.character.equipment/quantity 4, :orcpub.dnd.e5.character.equipment/equipped? true, :orcpub.dnd.e5.character.equipment/class-starting-equipment? true}}]} {:db/id 17592186055208, :orcpub.entity.strict/key :equipment, :orcpub.entity.strict/options [{:db/id 17592186055209, :orcpub.entity.strict/key :explorers-pack, :orcpub.entity.strict/map-value {:db/id 17592186055210, :orcpub.dnd.e5.character.equipment/quantity 1, :orcpub.dnd.e5.character.equipment/equipped? true, :orcpub.dnd.e5.character.equipment/class-starting-equipment? true}}]}]}]
    (is (= strict (strict-round-trip strict)))))

(deftest test-round-trip--warlock
  (let [strict {:db/id 17592186056112, :orcpub.entity.strict/selections [{:db/id 17592186056113, :orcpub.entity.strict/key :ability-scores, :orcpub.entity.strict/option {:db/id 17592186056114, :orcpub.entity.strict/key :standard-scores, :orcpub.entity.strict/map-value {:db/id 17592186056115, :orcpub.dnd.e5.character/str 15, :orcpub.dnd.e5.character/dex 14, :orcpub.dnd.e5.character/con 13, :orcpub.dnd.e5.character/int 12, :orcpub.dnd.e5.character/wis 10, :orcpub.dnd.e5.character/cha 8}}} {:db/id 17592186056116, :orcpub.entity.strict/key :class, :orcpub.entity.strict/options [{:db/id 17592186056117, :orcpub.entity.strict/key :warlock, :orcpub.entity.strict/selections [{:db/id 17592186056118, :orcpub.entity.strict/key :levels, :orcpub.entity.strict/options [{:db/id 17592186056119, :orcpub.entity.strict/key :level-1}]}]}]} {:db/id 17592186056123, :orcpub.entity.strict/key :equipment} {:db/id 17592186056127, :orcpub.entity.strict/key :weapons, :orcpub.entity.strict/options [{:db/id 17592186056128, :orcpub.entity.strict/key :dagger, :orcpub.entity.strict/map-value {:db/id 17592186056129, :orcpub.dnd.e5.character.equipment/quantity 2, :orcpub.dnd.e5.character.equipment/equipped? true, :orcpub.dnd.e5.character.equipment/class-starting-equipment? true}}]} {:db/id 17592186056130, :orcpub.entity.strict/key :armor, :orcpub.entity.strict/options [{:db/id 17592186056131, :orcpub.entity.strict/key :leather, :orcpub.entity.strict/map-value {:db/id 17592186056132, :orcpub.dnd.e5.character.equipment/quantity 1, :orcpub.dnd.e5.character.equipment/equipped? true, :orcpub.dnd.e5.character.equipment/class-starting-equipment? true}}]}]}]
    (is (= strict (strict-round-trip strict)))))

(deftest to-strict--homebrew-paths
  (let [non-strict {::entity/options {:race
                                      {:orcpub.entity/key :human,
                                       :orcpub.entity/options
                                       {:subrace
                                        {:orcpub.entity/key :custom,
                                         :orcpub.entity/value "Sancho"}}}}
                    ::entity/homebrew-paths {[:race] true
                                             [:race :human :subrace] true}}
        expected {::e/selections [{::e/key :race
                                   ::e/homebrew? true
                                   ::e/option {::e/key :human
                                               ::e/selections [{::e/key :subrace
                                                                ::e/homebrew? true
                                                                ::e/option {::e/key :custom
                                                                            ::e/string-value "Sancho"}}]}}]}]
    (is (= expected (entity/to-strict non-strict)))
    (is (= non-strict (entity/from-strict (entity/to-strict non-strict))))))

(deftest get-entity-path
  (let [warlock-entity {:orcpub.entity/options
                        {:ability-scores
                         {:orcpub.entity/key :standard-scores,
                          :orcpub.entity/value
                          {:orcpub.dnd.e5.character/str 15,
                           :orcpub.dnd.e5.character/dex 14,
                           :orcpub.dnd.e5.character/con 13,
                           :orcpub.dnd.e5.character/int 12,
                           :orcpub.dnd.e5.character/wis 10,
                           :orcpub.dnd.e5.character/cha 8}},
                         :class
                         [{:orcpub.entity/key :warlock,
                           :orcpub.entity/options
                           {:levels
                            [{:orcpub.entity/key :level-1}
                             {:orcpub.entity/key :level-2}
                             {:orcpub.entity/key :level-3,
                              :orcpub.entity/options
                              {:pact-boon {:orcpub.entity/key :pact-of-the-tome}}}],
                            :eldritch-invocations
                            [{:orcpub.entity/key :book-of-ancient-secrets}]}}],
                         :optional-content
                         [{:orcpub.entity/key :vgm}
                          {:orcpub.entity/key :scag}
                          {:orcpub.entity/key :ee}
                          {:orcpub.entity/key :dmg}
                          {:orcpub.entity/key :cos}],
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
                            true}}]}}]
    (is (= (entity/get-entity-path
            t5e/template
            warlock-entity
            [:class :warlock :eldritch-invocations :book-of-ancient-secrets :book-of-ancient-secrets-rituals])
           [:orcpub.entity/options :class 0 :orcpub.entity/options :eldritch-invocations 0 :orcpub.entity/options :book-of-ancient-secrets-rituals]))))

(deftest remove-empty-fields
  (let [entity-1 {:orcpub.entity.strict/values
                {:x :y
                 :orcpub.dnd.e5.features-used
                 {:orcpub.dnd.e5.units/long-rest #{}}
                 :orcpub.dnd.e5.character/prepared-spells-by-class
                 {"Cleric" #{}}}}
        entity-2 {:x :y
                  :orcpub.dnd.e5.character/prepared-spells-by-class
                  {"Cleric" #{:cure-wounds :healing-word}}}]
    (is (= {:orcpub.entity.strict/values {:x :y}}
           (entity/remove-empty-fields entity-1)))
    (is (= entity-2
           (entity/remove-empty-fields entity-2))))

  (testing "removes empty sequences"
    (let [entity-1 {:key :x
                    :options []}]
      (is (= {:key :x} (entity/remove-empty-fields entity-1)))))

  (testing "removes empty maps from sequences"
    (let [entity-1 '{:key :x
                     :options [{} {} {} {} {} {} {}]}
          expected '{:key :x}]
      (is (= expected (entity/remove-empty-fields entity-1))))
    
    (let [entity-1 '{:selections
                    [{:key :class,
                      :options
                      [{:key :artificer,
                        :selections
                        [{:key :wonderous-inventions,
                          :homebrew? true,
                          :options [{} {} {} {} {} {} {}]}]}]}]}
          expected '{:selections
                    [{:key :class,
                      :options
                      [{:key :artificer,
                        :selections
                        [{:key :wonderous-inventions,
                          :homebrew? true}]}]}]}]
      (is (= expected (entity/remove-empty-fields entity-1))))))

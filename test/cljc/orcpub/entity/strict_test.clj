(ns orcpub.entity.strict-test
  (:require [clojure.spec.alpha :as spec]
            [clojure.test :refer [deftest is]]
            [orcpub.entity.strict :as e]
            [orcpub.dnd.e5.character.equipment :as equip]))

(def entity {:db/id 17592186485120,
                :orcpub.entity.strict/owner "znax",
                :orcpub.entity.strict/values
                {:orcpub.dnd.e5.character/weight "110 lbs",
                 :orcpub.dnd.e5.character/player-name "Patrick",
                 :orcpub.dnd.e5.character/height "5'",
                 :orcpub.dnd.e5.character/flaws
                 "I have trouble keeping my true feelings hidden. My sharp tongue lands me in trouble.",
                 :orcpub.dnd.e5.character/image-url
                 "http://s-media-cache-ak0.pinimg.com/736x/17/8e/ce/178eced7cbf2068a7dd8f633adffb86b.jpg",
                 :orcpub.dnd.e5.character/personality-trait-1
                 "I don't pay attention to the risks in a situation. Never tell me the odds.",
                 :orcpub.dnd.e5.character/age "16",
                 :orcpub.dnd.e5.character/sex "M",
                 :orcpub.dnd.e5.character/ideals
                 "I'm loyal to my friends, not to any ideals, and everyone else can take a trip down the Styx for all I care.",
                 :db/id 17592186485208,
                 :orcpub.dnd.e5.character/custom-equipment
                 [{:db/id 17592186485209,
                   :orcpub.dnd.e5.character.equipment/name "Uniform with your rank",
                   :orcpub.dnd.e5.character.equipment/quantity 1,
                   :orcpub.dnd.e5.character.equipment/equipped? true,
                   :orcpub.dnd.e5.character.equipment/background-starting-equipment?
                   true}
                  {:db/id 17592186485210,
                   :orcpub.dnd.e5.character.equipment/name "Horn to summon help",
                   :orcpub.dnd.e5.character.equipment/quantity 1,
                   :orcpub.dnd.e5.character.equipment/equipped? true,
                   :orcpub.dnd.e5.character.equipment/background-starting-equipment?
                   true}],
                 :orcpub.dnd.e5.character/personality-trait-2
                 "I would rather make a new friend than a new enemy.",
                 :orcpub.dnd.e5.character/bonds
                 "I have a older sister in trouble because of me. I will do anything to protect her.",
                 :orcpub.dnd.e5.character/character-name "Zeneris Wilmar",
                 :orcpub.dnd.e5.character/faction-name "The Lords' Alliance"},
                :orcpub.entity.strict/selections
                [{:db/id 17592186485121,
                  :orcpub.entity.strict/key :weapons,
                  :orcpub.entity.strict/options
                  [{:db/id 17592186485122,
                    :orcpub.entity.strict/key :crossbow-hand,
                    :orcpub.entity.strict/map-value
                    {:db/id 17592186485123,
                     :orcpub.dnd.e5.character.equipment/quantity 1,
                     :orcpub.dnd.e5.character.equipment/equipped? true}}]}
                 {:db/id 17592186485124,
                  :orcpub.entity.strict/key :skill-expertise,
                  :orcpub.entity.strict/options
                  [{:db/id 17592186485125, :orcpub.entity.strict/key :stealth}
                   {:db/id 17592186485126, :orcpub.entity.strict/key :perception}]}
                 {:db/id 17592186485127,
                  :orcpub.entity.strict/key :race,
                  :orcpub.entity.strict/option
                  {:db/id 17592186485128,
                   :orcpub.entity.strict/key :human,
                   :orcpub.entity.strict/selections
                   [{:db/id 17592186485129,
                     :orcpub.entity.strict/key :subrace,
                     :orcpub.entity.strict/option
                     {:db/id 17592186485130, :orcpub.entity.strict/key :shou}}
                    {:db/id 17592186485131,
                     :orcpub.entity.strict/key :variant,
                     :orcpub.entity.strict/option
                     {:db/id 17592186485132,
                      :orcpub.entity.strict/key :variant-human,
                      :orcpub.entity.strict/selections
                      [{:db/id 17592186485133,
                        :orcpub.entity.strict/key :asi,
                        :orcpub.entity.strict/options
                        [{:db/id 17592186485134,
                          :orcpub.entity.strict/key :orcpub.dnd.e5.character/dex}
                         {:db/id 17592186485135,
                          :orcpub.entity.strict/key
                          :orcpub.dnd.e5.character/cha}]}]}}]}}
                 {:db/id 17592186485136,
                  :orcpub.entity.strict/key :ability-scores,
                  :orcpub.entity.strict/option
                  {:db/id 17592186485137,
                   :orcpub.entity.strict/key :point-buy,
                   :orcpub.entity.strict/map-value
                   {:db/id 17592186485138,
                    :orcpub.dnd.e5.character/str 8,
                    :orcpub.dnd.e5.character/dex 15,
                    :orcpub.dnd.e5.character/con 14,
                    :orcpub.dnd.e5.character/int 10,
                    :orcpub.dnd.e5.character/wis 12,
                    :orcpub.dnd.e5.character/cha 13}}}
                 {:db/id 17592186485139,
                  :orcpub.entity.strict/key :alignment,
                  :orcpub.entity.strict/option
                  {:db/id 17592186485140, :orcpub.entity.strict/key :lawful-neutral}}
                 {:db/id 17592186485141,
                  :orcpub.entity.strict/key :background,
                  :orcpub.entity.strict/option
                  {:db/id 17592186485142, :orcpub.entity.strict/key :investigator}}
                 {:db/id 17592186485143, :orcpub.entity.strict/key :equipment}
                 {:db/id 17592186485144, :orcpub.entity.strict/key :treasure}
                 {:db/id 17592186485145,
                  :orcpub.entity.strict/key :feats,
                  :orcpub.entity.strict/options
                  [{:db/id 17592186485146,
                    :orcpub.entity.strict/key :magic-initiate,
                    :orcpub.entity.strict/selections
                    [{:db/id 17592186485147,
                      :orcpub.entity.strict/key :spell-class,
                      :orcpub.entity.strict/option
                      {:db/id 17592186485148,
                       :orcpub.entity.strict/key :wizard,
                       :orcpub.entity.strict/selections
                       [{:db/id 17592186485149,
                         :orcpub.entity.strict/key :level-1-spell,
                         :orcpub.entity.strict/option
                         {:db/id 17592186485150,
                          :orcpub.entity.strict/key :find-familiar}}
                        {:db/id 17592186485151,
                         :orcpub.entity.strict/key :cantrip,
                         :orcpub.entity.strict/options
                         [{:db/id 17592186485152,
                           :orcpub.entity.strict/key :mage-hand}
                          {:db/id 17592186485153,
                           :orcpub.entity.strict/key :minor-illusion}]}]}}]}
                   {:db/id 17592186485154,
                    :orcpub.entity.strict/key :crossbow-expert}]}
                 {:db/id 17592186485155,
                  :orcpub.entity.strict/key :class,
                  :orcpub.entity.strict/options
                  [{:db/id 17592186485156,
                    :orcpub.entity.strict/key :rogue,
                    :orcpub.entity.strict/selections
                    [{:db/id 17592186485157,
                      :orcpub.entity.strict/key :levels,
                      :orcpub.entity.strict/options
                      [{:db/id 17592186485158, :orcpub.entity.strict/key :level-1}
                       {:db/id 17592186485159,
                        :orcpub.entity.strict/key :level-2,
                        :orcpub.entity.strict/selections
                        [{:db/id 17592186485160,
                          :orcpub.entity.strict/key :hit-points,
                          :orcpub.entity.strict/option
                          {:db/id 17592186485161,
                           :orcpub.entity.strict/key :average,
                           :orcpub.entity.strict/int-value 5}}]}
                       {:db/id 17592186485162,
                        :orcpub.entity.strict/key :level-3,
                        :orcpub.entity.strict/selections
                        [{:db/id 17592186485163,
                          :orcpub.entity.strict/key :hit-points,
                          :orcpub.entity.strict/option
                          {:db/id 17592186485164,
                           :orcpub.entity.strict/key :average,
                           :orcpub.entity.strict/int-value 5}}
                         {:db/id 17592186485165,
                          :orcpub.entity.strict/key :roguish-archetype,
                          :orcpub.entity.strict/option
                          {:db/id 17592186485166,
                           :orcpub.entity.strict/key :swashbuckler}}]}
                       {:db/id 17592186485167,
                        :orcpub.entity.strict/key :level-4,
                        :orcpub.entity.strict/selections
                        [{:db/id 17592186485168,
                          :orcpub.entity.strict/key :hit-points,
                          :orcpub.entity.strict/option
                          {:db/id 17592186485169,
                           :orcpub.entity.strict/key :average,
                           :orcpub.entity.strict/int-value 5}}
                         {:db/id 17592186485170,
                          :orcpub.entity.strict/key :asi-or-feat,
                          :orcpub.entity.strict/option
                          {:db/id 17592186485171, :orcpub.entity.strict/key :feat}}]}
                       {:db/id 17592186485172,
                        :orcpub.entity.strict/key :level-5,
                        :orcpub.entity.strict/selections
                        [{:db/id 17592186485173,
                          :orcpub.entity.strict/key :hit-points,
                          :orcpub.entity.strict/option
                          {:db/id 17592186485174,
                           :orcpub.entity.strict/key :average,
                           :orcpub.entity.strict/int-value 5}}]}]}
                     {:db/id 17592186485175,
                      :orcpub.entity.strict/key :expertise,
                      :orcpub.entity.strict/option
                      {:db/id 17592186485176, :orcpub.entity.strict/key :two-skills}}
                     {:db/id 17592186485177,
                      :orcpub.entity.strict/key :starting-equipment-additional-weapon,
                      :orcpub.entity.strict/option
                      {:db/id 17592186485178, :orcpub.entity.strict/key :shortsword}}
                     {:db/id 17592186485179,
                      :orcpub.entity.strict/key :starting-equipment-equipment-pack,
                      :orcpub.entity.strict/option
                      {:db/id 17592186485180,
                       :orcpub.entity.strict/key :burglars-pack}}
                     {:db/id 17592186485181,
                      :orcpub.entity.strict/key :starting-equipment-melee-weapon,
                      :orcpub.entity.strict/option
                      {:db/id 17592186485182,
                       :orcpub.entity.strict/key :shortsword}}]}
                   {:db/id 17592186485183,
                    :orcpub.entity.strict/key :fighter,
                    :orcpub.entity.strict/selections
                    [{:db/id 17592186485184,
                      :orcpub.entity.strict/key :levels,
                      :orcpub.entity.strict/options
                      [{:db/id 17592186485185,
                        :orcpub.entity.strict/key :level-1,
                        :orcpub.entity.strict/selections
                        [{:db/id 17592186485186,
                          :orcpub.entity.strict/key :hit-points,
                          :orcpub.entity.strict/option
                          {:db/id 17592186485187,
                           :orcpub.entity.strict/key :average,
                           :orcpub.entity.strict/int-value 6}}]}]}
                     {:db/id 17592186485188,
                      :orcpub.entity.strict/key :fighting-style,
                      :orcpub.entity.strict/options
                      [{:db/id 17592186485189,
                        :orcpub.entity.strict/key :archery}]}]}]}
                 {:db/id 17592186485190,
                  :orcpub.entity.strict/key :skill-profs,
                  :orcpub.entity.strict/options
                  [{:db/id 17592186485191, :orcpub.entity.strict/key :stealth}
                   {:db/id 17592186485192, :orcpub.entity.strict/key :acrobatics}
                   {:db/id 17592186485193, :orcpub.entity.strict/key :investigation}
                   {:db/id 17592186485194, :orcpub.entity.strict/key :perception}
                   {:db/id 17592186485195, :orcpub.entity.strict/key :persuasion}]}
                 {:db/id 17592186485196,
                  :orcpub.entity.strict/key :armor,
                  :orcpub.entity.strict/options
                  [{:db/id 17592186485197,
                    :orcpub.entity.strict/key :studded,
                    :orcpub.entity.strict/map-value
                    {:db/id 17592186485198,
                     :orcpub.dnd.e5.character.equipment/quantity 1,
                     :orcpub.dnd.e5.character.equipment/equipped? true}}]}
                 {:db/id 17592186485199,
                  :orcpub.entity.strict/key :languages,
                  :orcpub.entity.strict/options
                  [{:db/id 17592186485200, :orcpub.entity.strict/key :orc}
                   {:db/id 17592186485201, :orcpub.entity.strict/key :gnomish}
                   {:db/id 17592186485202, :orcpub.entity.strict/key :elvish}]}
                 {:db/id 17592186485203,
                  :orcpub.entity.strict/key :optional-content,
                  :orcpub.entity.strict/options
                  [{:db/id 17592186485204, :orcpub.entity.strict/key :scag}]}
                 {:db/id 17592186485205,
                  :orcpub.entity.strict/key :magic-weapons,
                  :orcpub.entity.strict/options
                  [{:db/id 17592186485206,
                    :orcpub.entity.strict/key :sword-of-wounding-shortsword,
                    :orcpub.entity.strict/map-value
                    {:db/id 17592186485207,
                     :orcpub.dnd.e5.character.equipment/quantity 1,
                     :orcpub.dnd.e5.character.equipment/equipped? true}}]}
                 {:db/id 17592186485385, :orcpub.entity.strict/key :magic-armor}
                 {:db/id 17592186485386,
                  :orcpub.entity.strict/key :weapons,
                  :orcpub.entity.strict/options
                  [{:db/id 17592186485122,
                    :orcpub.entity.strict/key :crossbow-hand,
                    :orcpub.entity.strict/map-value
                    {:db/id 17592186485123,
                     :orcpub.dnd.e5.character.equipment/quantity 1,
                     :orcpub.dnd.e5.character.equipment/equipped? true}}]}
                 {:db/id 17592186485387, :orcpub.entity.strict/key :other-magic-items}
                 {:db/id 17592186485388,
                  :orcpub.entity.strict/key :armor,
                  :orcpub.entity.strict/options
                  [{:db/id 17592186485197,
                    :orcpub.entity.strict/key :studded,
                    :orcpub.entity.strict/map-value
                    {:db/id 17592186485198,
                     :orcpub.dnd.e5.character.equipment/quantity 1,
                     :orcpub.dnd.e5.character.equipment/equipped? true}}]}
                 {:db/id 17592186485389,
                  :orcpub.entity.strict/key :magic-weapons,
                  :orcpub.entity.strict/options
                  [{:db/id 17592186485206,
                    :orcpub.entity.strict/key :sword-of-wounding-shortsword,
                    :orcpub.entity.strict/map-value
                    {:db/id 17592186485207,
                     :orcpub.dnd.e5.character.equipment/quantity 1,
                     :orcpub.dnd.e5.character.equipment/equipped? true}}]}
                 {:db/id 17592186485448,
                  :orcpub.entity.strict/key :weapons,
                  :orcpub.entity.strict/options
                  [{:db/id 17592186485122,
                    :orcpub.entity.strict/key :crossbow-hand,
                    :orcpub.entity.strict/map-value
                    {:db/id 17592186485123,
                     :orcpub.dnd.e5.character.equipment/quantity 1,
                     :orcpub.dnd.e5.character.equipment/equipped? true}}]}
                 {:db/id 17592186485449,
                  :orcpub.entity.strict/key :armor,
                  :orcpub.entity.strict/options
                  [{:db/id 17592186485197,
                    :orcpub.entity.strict/key :studded,
                    :orcpub.entity.strict/map-value
                    {:db/id 17592186485198,
                     :orcpub.dnd.e5.character.equipment/quantity 1,
                     :orcpub.dnd.e5.character.equipment/equipped? true}}]}
                 {:db/id 17592186485450,
                  :orcpub.entity.strict/key :magic-weapons,
                  :orcpub.entity.strict/options
                  [{:db/id 17592186485206,
                    :orcpub.entity.strict/key :sword-of-wounding-shortsword,
                    :orcpub.entity.strict/map-value
                    {:db/id 17592186485207,
                     :orcpub.dnd.e5.character.equipment/quantity 1,
                     :orcpub.dnd.e5.character.equipment/equipped? true}}]}
                 {:db/id 17592186485454,
                  :orcpub.entity.strict/key :weapons,
                  :orcpub.entity.strict/options
                  [{:db/id 17592186485122,
                    :orcpub.entity.strict/key :crossbow-hand,
                    :orcpub.entity.strict/map-value
                    {:db/id 17592186485123,
                     :orcpub.dnd.e5.character.equipment/quantity 1,
                     :orcpub.dnd.e5.character.equipment/equipped? true}}]}
                 {:db/id 17592186485455,
                  :orcpub.entity.strict/key :armor,
                  :orcpub.entity.strict/options
                  [{:db/id 17592186485197,
                    :orcpub.entity.strict/key :studded,
                    :orcpub.entity.strict/map-value
                    {:db/id 17592186485198,
                     :orcpub.dnd.e5.character.equipment/quantity 1,
                     :orcpub.dnd.e5.character.equipment/equipped? true}}]}
                 {:db/id 17592186485456,
                  :orcpub.entity.strict/key :magic-weapons,
                  :orcpub.entity.strict/options
                  [{:db/id 17592186485206,
                    :orcpub.entity.strict/key :sword-of-wounding-shortsword,
                    :orcpub.entity.strict/map-value
                    {:db/id 17592186485207,
                     :orcpub.dnd.e5.character.equipment/quantity 1,
                     :orcpub.dnd.e5.character.equipment/equipped? true}}]}]})

(def entity-2 {:orcpub.entity.strict/selections
                  [{:db/id 17592186485127,
                    :orcpub.entity.strict/key :race,
                    :orcpub.entity.strict/option
                    {:db/id 17592186485128,
                     :orcpub.entity.strict/key :human,
                     :orcpub.entity.strict/selections
                     [{:db/id 17592186485129,
                       :orcpub.entity.strict/key :subrace,
                       :orcpub.entity.strict/option
                       {:db/id 17592186485130, :orcpub.entity.strict/key :shou}}
                      {:db/id 17592186485131,
                       :orcpub.entity.strict/key :variant,
                       :orcpub.entity.strict/option
                       {:db/id 17592186485132,
                        :orcpub.entity.strict/key :variant-human,
                        :orcpub.entity.strict/selections
                        [{:db/id 17592186485133,
                          :orcpub.entity.strict/key :asi,
                          :orcpub.entity.strict/options
                          [{:db/id 17592186485134,
                            :orcpub.entity.strict/key :orcpub.dnd.e5.character/dex}
                           {:db/id 17592186485135,
                            :orcpub.entity.strict/key
                            :orcpub.dnd.e5.character/cha}]}
                         {:db/id 17592186485133,
                          :orcpub.entity.strict/key :asi,
                          :orcpub.entity.strict/options
                          [{:db/id 17592186485134,
                            :orcpub.entity.strict/key :orcpub.dnd.e5.character/dex}
                           {:db/id 17592186485135,
                            :orcpub.entity.strict/key
                            :orcpub.dnd.e5.character/cha}]}]}}]}}]})

(def entity-3 {:orcpub.entity.strict/selections
               [{:db/id 17592186485127,
                 :orcpub.entity.strict/key :race,
                 :orcpub.entity.strict/option
                 {:db/id 17592186485128,
                  :orcpub.entity.strict/key :human,
                  :orcpub.entity.strict/selections
                  [{:db/id 17592186485129,
                    :orcpub.entity.strict/key :subrace,
                    :orcpub.entity.strict/option
                    {:db/id 17592186485130, :orcpub.entity.strict/key :shou}}
                   {:db/id 17592186485131,
                    :orcpub.entity.strict/key :variant,
                    :orcpub.entity.strict/option
                    {:db/id 17592186485132,
                     :orcpub.entity.strict/key :variant-human,
                     :orcpub.entity.strict/selections
                     [{:db/id 17592186485133,
                       :orcpub.entity.strict/key :asi,
                       :orcpub.entity.strict/options
                       [{:db/id 17592186485134,
                         :orcpub.entity.strict/key :orcpub.dnd.e5.character/dex}
                        {:db/id 17592186485135,
                         :orcpub.entity.strict/key
                         :orcpub.dnd.e5.character/cha}]}]}}]}}]})

(deftest has-duplicate-selections?
  (is (e/has-duplicate-selections? entity))
  (is (e/has-duplicate-selections? entity-2))
  (is (not (e/has-duplicate-selections? entity-3))))


(deftest valid-spec
  (let [entity {::e/selections [{::e/key :race
                                 ::e/option {::e/key :elf
                                             ::e/selections [{::e/key :subrace
                                                              ::e/option {::e/key :wood-elf}}]}}
                                {::e/key :weapons
                                 ::e/options [{::e/key :javelin
                                               ::e/map-value {::equip/quantity 4
                                                              ::equip/equipped? true
                                                              ::equip/class-starting-equipment? true}}]}]
                ::e/values {::eyes "green"
                            ::custom-equipment [{::equip/name "Scroll of Pedigree"
                                                 ::equip/equipped? true
                                                 ::equip/background-starting-equipment? true}]}}]
    (is (spec/valid? ::e/entity entity))
    (is (spec/valid? ::e/entity entity-3))
    (is (not (spec/valid? ::e/entity entity-2)))))

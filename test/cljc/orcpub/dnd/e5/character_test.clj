(ns orcpub.dnd.e5.character-test
  (:require [clojure.test :refer [is deftest testing]]
            [clojure.spec :as spec]
            [orcpub.dnd.e5.character :as char]
            [orcpub.dnd.e5.character.equipment :as equip]
            [orcpub.entity :as entity]))

(deftest equipment-to-namespaced
  (let [item {:name "Juancho"
              :quantity 2
              :equipped? true
              :background-starting-equipment? true
              :class-starting-equipment? false}
        expected-item {::equip/name "Juancho"
                       ::equip/quantity 2
                       ::equip/equipped? true
                       ::equip/background-starting-equipment? true
                       ::equip/class-starting-equipment? false}]
    (is (= (equip/to-namespaced item) expected-item))))

(deftest add-ability-namespaces
  (let [character {:orcpub.entity/options
                   {:ability-scores
                    {:orcpub.entity/key :standard-roll,
                     :orcpub.entity/value
                     {:str 15,
                      :dex 14,
                      :con 13,
                      :int 12,
                      :wis 10,
                      :cha 8}}}}
        expected {:orcpub.entity/options
                  {:ability-scores
                   {:orcpub.entity/key :standard-roll,
                    :orcpub.entity/value
                    {:orcpub.dnd.e5.character/str 15,
                     :orcpub.dnd.e5.character/dex 14,
                     :orcpub.dnd.e5.character/con 13,
                     :orcpub.dnd.e5.character/int 12,
                     :orcpub.dnd.e5.character/wis 10,
                     :orcpub.dnd.e5.character/cha 8}}}}]
    (is (= (char/add-ability-namespaces character) expected))))

(deftest unnamespaced-character
  (let [character {:orcpub.entity/options
                   {:magic-armor
                    [{:orcpub.entity/key :animated-shield,
                      :orcpub.entity/value {:quantity 1, :equipped? true}}]}}
        character-2 {:orcpub.entity/values
                     {:custom-treasure [],
                      :custom-equipment
                      [{:name "Scoll of Pedigree",
                        :quantity 1,
                        :equipped? true,
                        :background-starting-equipment? true}], 
                      :character-name "Hcak"}}
        character-3 {:orcpub.entity/options
                     {:ability-scores
                      {:orcpub.entity/key :standard-roll,
                       :orcpub.entity/value
                       {:str 15,
                        :dex 14,
                        :con 13,
                        :int 12,
                        :wis 10,
                        :cha 8}}}}
        namespaced {:orcpub.entity/options
                    {:magic-armor
                     [{:orcpub.entity/key :animated-shield,
                       :orcpub.entity/value {::equip/quantity 1,
                                             ::equip/:equipped? true}}]
                     :ability-scores
                     {:orcpub.entity/key :standard-roll,
                      :orcpub.entity/value
                      {:orcpub.dnd.e5.character/str 15
                       :orcpub.dnd.e5.character/dex 14,
                       :orcpub.dnd.e5.character/con 13,
                       :orcpub.dnd.e5.character/int 12,
                       :orcpub.dnd.e5.character/wis 10,
                       :orcpub.dnd.e5.character/cha 8}}}
                    :orcpub.entity/values
                     {::char/custom-treasure [],
                      ::char/custom-equipment
                      [{:name "Scoll of Pedigree",
                        :quantity 1,
                        :equipped? true,
                        :background-starting-equipment? true}], 
                      ::char/character-name "Hcak"}}]
    (is (spec/valid? ::char/unnamespaced-character character))
    (is (spec/valid? ::char/unnamespaced-character character-2))
    (is (spec/valid? ::char/unnamespaced-character character-3))
    (is (not (spec/valid? ::char/unnamespaced-character namespaced)))))

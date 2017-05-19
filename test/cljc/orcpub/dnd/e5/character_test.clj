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
        namespaced {:orcpub.entity/options
                    {:magic-armor
                     [{:orcpub.entity/key :animated-shield,
                       :orcpub.entity/value {::equip/quantity 1,
                                             ::equip/:equipped? true}}]}
                    :orcpub.entity/values
                     {::char/custom-treasure [],
                      ::char/custom-equipment
                      [{:name "Scoll of Pedigree",
                        :quantity 1,
                        :equipped? true,
                        :background-starting-equipment? true}], 
                      ::char/character-name "Hcak"}}]
    (clojure.pprint/pprint (spec/explain-data ::char/unnamespaced-character character))
    (clojure.pprint/pprint (spec/explain-data ::char/unnamespaced-character character-2))
    (is (spec/valid? ::char/unnamespaced-character character))
    (is (spec/valid? ::char/unnamespaced-character character-2))
    (is (not (spec/valid? ::char/unnamespaced-character namespaced)))))

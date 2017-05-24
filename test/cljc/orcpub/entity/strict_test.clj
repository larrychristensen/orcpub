(ns orcpub.entity.strict-test
  (:require [clojure.spec :as spec]
            [clojure.test :refer [deftest is]]
            [orcpub.entity.strict :as e]
            [orcpub.dnd.e5.character.equipment :as equip]))


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
    (is (spec/valid? ::e/entity entity))))

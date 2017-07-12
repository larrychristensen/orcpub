(ns orcpub.dnd.e5.magic-items-test
  (:require [clojure.test :refer [testing deftest is]]
            [orcpub.dnd.e5.magic-items :as mi]
            [orcpub.dnd.e5.character :as char]
            [orcpub.modifiers :as mod]))

(deftest test-to-internal-item
  (testing "Ability override modifier"
    (let [item {::mi/modifiers [{::mod/key :ability-override
                                 ::mod/args [{::mod/keyword-arg ::char/str}
                                             {::mod/int-arg 1}]}]}
          expected-item {::mi/internal-modifiers {:ability {::char/str {:value 1
                                                                        :type :becomes-at-least}}}}
          internal-item (mi/to-internal-item item)]
      (is (= internal-item expected-item))))
  (testing "Ability bonus modifier"
    (let [item {::mi/modifiers [{::mod/key :ability
                                 ::mod/args [{::mod/keyword-arg ::char/str}
                                             {::mod/int-arg 1}]}]}
          expected-item {::mi/internal-modifiers {:ability {::char/str {:value 1
                                                                        :type :increases-by}}}}
          internal-item (mi/to-internal-item item)]
      (is (= internal-item expected-item))))
  (testing "Save modifier"
    (let [item {::mi/modifiers [{::mod/key :saving-throw-bonus
                                 ::mod/args [{::mod/keyword-arg ::char/str}
                                             {::mod/int-arg 1}]}]}
          expected-item {::mi/internal-modifiers {:save {::char/str {:value 1}}}}
          internal-item (mi/to-internal-item item)]
      (is (= internal-item expected-item))))
  (testing "Resistance modifier"
    (let [item {::mi/modifiers [{::mod/key :damage-resistance
                                 ::mod/args [{::mod/keyword-arg :fire}]}
                                {::mod/key :damage-resistance
                                 ::mod/args [{::mod/keyword-arg :necrotic}]}]}
          expected-item {:orcpub.dnd.e5.magic-items/internal-modifiers {:damage-resistance {:fire true, :necrotic true}}}
          internal-item (mi/to-internal-item item)]
      (is (= internal-item expected-item))))
  (testing "Speed modifier"
    (let [item {::mi/modifiers [{::mod/key :flying-speed-equal-to-walking}
                                {::mod/key :swimming-speed-override
                                 ::mod/args [{::mod/int-arg 10}]}]}
          expected-item {:orcpub.dnd.e5.magic-items/internal-modifiers {:flying-speed {:type :equals-walking-speed}, :swimming-speed {:type :becomes-at-least :value 10}}}
          internal-item (mi/to-internal-item item)]
      (is (= internal-item expected-item)))))

(deftest test-from-internal-item
  (testing "Default ability modifier"
    (let [internal-item {::mi/internal-modifiers {:ability {::char/str {:value 1}}}}
          expected-item {::mi/modifiers [{::mod/key :ability-override
                                          ::mod/args [{::mod/keyword-arg ::char/str}
                                                      {::mod/int-arg 1}]}]}
          item (mi/from-internal-item internal-item)]
      (is (= item expected-item))))
  (testing "Ability bonus modifier"
    (let [internal-item {::mi/internal-modifiers {:ability {::char/str {:value 1
                                                                        :type :increases-by}}}}
          expected-item {::mi/modifiers [{::mod/key :ability
                                          ::mod/args [{::mod/keyword-arg ::char/str}
                                                      {::mod/int-arg 1}]}]}
          item (mi/from-internal-item internal-item)]
      (is (= item expected-item))))
  (testing "Ability override modifier"
    (let [internal-item {::mi/internal-modifiers {:ability {::char/str {:value 1
                                                                        :type :becomes-at-least}}}}
          expected-item {::mi/modifiers [{::mod/key :ability-override
                                          ::mod/args [{::mod/keyword-arg ::char/str}
                                                      {::mod/int-arg 1}]}]}
          item (mi/from-internal-item internal-item)]
      (is (= item expected-item))))
  (testing "Save modifier"
    (let [internal-item {::mi/internal-modifiers {:save {::char/str {:value 1}}}}
          expected-item {::mi/modifiers [{::mod/key :saving-throw-bonus
                                          ::mod/args [{::mod/keyword-arg ::char/str}
                                                      {::mod/int-arg 1}]}]}
          item (mi/from-internal-item internal-item)]
      (is (= item expected-item))))
  (testing "Resistance modifier"
    (let [internal-item {:orcpub.dnd.e5.magic-items/internal-modifiers {:damage-resistance {:fire true, :necrotic true}}}
          expected-item {::mi/modifiers [{::mod/key :damage-resistance
                                          ::mod/args [{::mod/keyword-arg :fire}]}
                                         {::mod/key :damage-resistance
                                          ::mod/args [{::mod/keyword-arg :necrotic}]}]}
          item (mi/from-internal-item internal-item)]
      (is (= item expected-item))))
  (testing "Speed modifier"
    (let [internal-item {:orcpub.dnd.e5.magic-items/internal-modifiers {:flying-speed {:type :equals-walking-speed}, :swimming-speed {:value 10}}}
          expected-item {::mi/modifiers [{::mod/key :flying-speed-equal-to-walking}
                                         {::mod/key :swimming-speed-override
                                          ::mod/args [{::mod/int-arg 10}]}]}
          item (mi/from-internal-item internal-item)]
      (is (= item expected-item)))))

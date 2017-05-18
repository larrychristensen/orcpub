(ns orcpub.dnd.e5.character-test
  (:require [clojure.test :refer [is deftest testing]]
           [orcpub.dnd.e5.character.equipment :as equip]))

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



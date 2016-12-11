(ns orcpub.entity-test
  (:require [clojure.test :refer :all]
            [orcpub.entity :as entity]
            [orcpub.modifiers :as modifiers]))

(def example-races
  {:dwarf {:option-type :race
           :name "Dwarf"
           :modifiers [(modifiers/ability :con 2)
                       (modifiers/overriding :speed 25)
                       (modifiers/resistances :poison)
                       (modifiers/darkvision)]}})

(def example-feats
  {:fire-resister {:option-type :feat
                   :name "Fire Resister"
                   :modifiers [(modifiers/resistances :fire)]}})

(def example-subraces
  {:hill-dwarf {:option-type :subrace
                :name "Hill Dwarf"
                :restrictions {[:race] (= :dwarf)}
                :modifiers [(modifiers/ability :wis 1)]}})

(def example-classes
  {:wizard {:option-type :class
            :name "Wizard"
            :modifiers [(modifiers/saving-throws :int :wis)]}})

(def example-character
  {:character-type :dnd-5e
   :abilities {:str 9 :dex 10 :con 12 :int 5 :wis 18 :cha 12}
   :options [[:race :dwarf]
             [:subrace :hill-dwarf]
             [:class :wizard]
             [:feat :fire-resister]]})

(def all-options
  {:race example-races
   :subrace example-subraces
   :feat example-feats
   :class example-classes})

(deftest build-dnd-5e-char
  (let [built-char (entity/build example-character
                                 all-options)]
    (is (= 25 (:speed built-char)))
    (is (= 19 (get-in built-char [:abilities :wis])))
    (is (:darkvision built-char))
    (is (= [:poison :fire] (:resistances built-char)))
    (is (= [:int :wis] (:saving-throws built-char)))))

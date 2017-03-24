(ns orcpub.entity-spec-test
  (:require [clojure.test :refer :all]
            [orcpub.dnd.e5.character :as char5e]
            [orcpub.dnd.e5.modifiers :as mod5e]
            [orcpub.modifiers :as modifiers]
            [orcpub.entity-spec :as es]
            [orcpub.entity :as entity]))

(deftest test-defentity
  (let [e (es/make-entity {?x (+ 1 2)
                        ?y (+ 5 ?x)})]
    (is (= 3 (es/entity-val e :x)))
    (is (= 8 (es/entity-val e :y)))))

(def skills [{:key :athletics
              :ability :str}
             {:key :acrobatics
              :ability :dex}
             {:key :perception
              :ability :wis}])

(def skill-abilities
  (into {} (map (juxt :key :ability)) skills))

(def char1
  (es/make-entity
   {?ability-bonuses (reduce-kv
                      (fn [m k v]
                        (assoc m k (int (/ (- v 10) 2))))
                      {}
                      ?abilities)
    ?total-levels (apply + (vals ?levels))
    ?prof-bonus (+ (int (/ (dec ?total-levels) 4)) 2)
    ?skill-profs #{:athletics :perception}
    ?skill-prof-bonuses (into {}
                              (map (fn [{k :key}]
                                     [k (if (?skill-profs k) ?prof-bonus 0)]))
                              skills)
    ?skill-bonuses (reduce-kv
                    (fn [m k v]
                      (assoc m k (+ v (or (?ability-bonuses (skill-abilities k)) 0))))
                    {}
                    ?skill-prof-bonuses)}))

(def modifiers2
  [(modifiers/modifier ?abilities {:str 18 :dex 12 :con 14 :int 15 :wis 17 :cha 19})
   (modifiers/modifier ?skill-expertise (conj (or ?skill-expertise #{}) :perception))
   (modifiers/modifier ?skill-prof-bonuses (reduce-kv
                         (fn [m k v]
                                        (* 2 v)
                           (assoc m k (if (?skill-expertise k)
                                        v)))
                         {}
                         ?skill-prof-bonuses))
   (modifiers/modifier ?abilities (update ?abilities :wis + 2))])

(deftest test-modifier
  (is (every? ::modifiers/fn modifiers2))
  (let [modified (modifiers/apply-modifiers char1 modifiers2)]
    (is (= {:str 18 :dex 12 :con 14 :int 15 :wis 19 :cha 19} (es/entity-val modified :abilities)))
    (is (= {:str 4 :dex 1 :con 2 :int 2 :wis 4 :cha 4} (es/entity-val modified :ability-bonuses)))
    (is (= #{:athletics :perception} (es/entity-val modified :skill-profs)))
    (is (= #{:perception} (es/entity-val modified :skill-expertise)))))

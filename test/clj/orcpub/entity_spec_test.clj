(ns orcpub.entity-spec-test
  (:require [clojure.test :refer :all]
            [orcpub.dnd.e5.character :as char5e]
            [orcpub.dnd.e5.modifiers :as mod5e]
            [orcpub.entity-spec :refer [make-entity q modifier modifiers apply-modifiers]]))

(deftest test-defentity
  (let [e (make-entity {?char5e/x (+ 1 2)
                        ?char5e/y (+ 5 ?char5e/x)})]
    (is (= 3 (q e ?char5e/x)))
    (is (= 8 (q e ?char5e/y)))))

(def skills [{:key :athletics
              :ability :str}
             {:key :acrobatics
              :ability :dex}
             {:key :perception
              :ability :wis}])

(def skill-abilities
  (into {} (map (juxt :key :ability)) skills))

(def char1
  (make-entity
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
                      (prn "KV" m k v (or (?ability-bonuses (skill-abilities k)) 0))
                      (assoc m k (+ v (or (?ability-bonuses (skill-abilities k)) 0))))
                    {}
                    ?skill-prof-bonuses)}))

(def modifiers2
  (modifiers
   (?abilities {:str 18 :dex 12 :con 14 :int 15 :wis 17 :cha 19})
   (?skill-expertise (conj (or ?skill-expertise #{}) :perception))
   (?skill-prof-bonuses (reduce-kv
                         (fn [m k v]
                           (assoc m k (if (?skill-expertise k)
                                        (* 2 v)
                                        v)))
                         {}
                         ?skill-prof-bonuses))
   (?abilities (update ?abilities :wis + 2))))

(deftest test-modifier
  (let [modified (apply-modifiers char1 modifiers2)]
    (prn (:abilities (apply-modifiers char1 modifiers2)))
    (is (= {:str 18 :dex 12 :con 14 :int 15 :wis 19 :cha 19} (q modified ?abilities)))
    (is (= {:str 4 :dex 1 :con 2 :int 2 :wis 4 :cha 4} (q modified ?ability-bonuses)))
    (is (= #{:athletics :perception} (q modified ?skill-profs)))
    (is (= #{:perception} (q modified ?skill-expertise)))
    (is (= {:athletics 3 :acrobatics 0 :perception 6} (q modified ?skill-prof-bonuses)))
    (is (= 4 (:rogue (q char1 ?levels))))
    (is (= {:athletics 7 :acrobatics 1 :perception 10} (q modified ?skill-bonuses)))
    (is (= 10 (:perception (q modified ?skill-bonuses))))))

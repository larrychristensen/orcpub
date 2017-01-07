(ns orcpub.entity-spec-test
  (:require [clojure.test :refer :all]
            [orcpub.entity-spec :refer [make-entity q modifier modifiers apply-modifiers]]))

(deftest test-defentity
  (let [e (make-entity {?x (+ 1 2)
                        ?y (+ 5 ?x)})]
    (is (= 3 (q e ?x)))
    (is (= 8 (q e ?y)))))

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
   {?levels {:wizard 1
             :rogue 4}
    ?abilities {:str 18 :dex 12 :con 14 :int 15 :wis 17 :cha 19}
    ?ability-bonuses (reduce-kv
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
    ?skill-bonuses (into {}
                         (map
                          (fn [[k v]]
                            [k (+ v (?ability-bonuses (skill-abilities k)))]))
                         ?skill-prof-bonuses)}))

(def modifiers2
  (modifiers
   (?skill-expertise (conj (or ?skill-expertise #{}) :perception))
   (?skill-prof-bonuses (reduce-kv
                         (fn [m k v]
                           (assoc m k (if (?skill-expertise k)
                                        (* 2 v)
                                        v)))
                         {}
                         ?skill-prof-bonuses))
   (?abilities (update ?abilities :wis + 2))))

(deftest test-defentity--character
  (is (= 4 (:rogue (q char1 ?levels))))
  (is (= {:athletics 7 :acrobatics 1 :perception 6} (q char1 ?skill-bonuses))))

(deftest test-modifier
  (let [modified (apply-modifiers char1 modifiers2)]
    (is (= 10 (:perception (q modified ?skill-bonuses))))))

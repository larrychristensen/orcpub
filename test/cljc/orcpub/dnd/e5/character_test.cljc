(ns orcpub.dnd.e5.character-test
  (:require [clojure.spec.alpha :as spec]
            [clojure.spec.alpha.test :as stest]
            [clojure.test :refer [deftest testing is]]
            [clojure.test.check :as tc]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.clojure-test :refer [defspec]]
            [orcpub.dnd.e5.character :as char5e]))

(def valid-char
  {:abilities (char5e/abilities 12 13 1 20 14 12) 
   :savings-throws [:int :con]
   :speed 35
   :darkvision false
   :initiative 12})

(deftest test-character-spec
  (testing "valid character"
    (is (not (spec/explain-data ::char5e/character valid-char))))
  (testing "missing ability"
    (is (spec/explain-data ::char5e/character (update valid-char ::char5e/abilities dissoc ::char5e/dex)))))

(defspec non-negative-speeds-are-valid
  100
  (prop/for-all [speed gen/int]
                (let [updated-char (assoc valid-char ::char5e/speed speed)]
                  (= (not (neg? speed))
                     (spec/valid? ::char5e/character updated-char)))))

(stest/instrument `char5e/standard-ability-rolls)
(char5e/standard-ability-rolls)
(stest/summarize-results (stest/check `char5e/standard-ability-rolls))

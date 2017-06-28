(ns orcpub.template-test
  (:require [clojure.spec.alpha :as spec]
            [clojure.spec.alpha.test :as stest]
            [clojure.spec.alpha.gen :as sgen]
            [clojure.test :refer [deftest testing is]]
            [clojure.test.check :as tc]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.clojure-test :refer [defspec]]
            [orcpub.template :as template]
            [orcpub.modifiers :as modifiers]))

(deftest make-modifer-map
  (testing "one level"
    (let [modifiers-1 (sgen/sample (spec/gen ::modifiers/modifier) 2)
          modifiers-2 (sgen/sample (spec/gen ::modifiers/modifier) 3)
          template {::template/selections [{::template/name ""
                                            ::template/key :x
                                            ::template/options [{::template/name ""
                                                                 ::template/key :y
                                                                 ::template/modifiers modifiers-1}
                                                                {::template/name ""
                                                                 ::template/key :v
                                                                 ::template/modifiers modifiers-2}]}]}
          modifiers-map (template/make-modifier-map template)]
      (is (= modifiers-1 (get-in modifiers-map [:x :y ::template/modifiers])))
      (is (= modifiers-2 (get-in modifiers-map [:x :v ::template/modifiers])))))
  (testing "two levels"
    (let [modifiers-1 (sgen/sample (spec/gen ::modifiers/modifier) 2)
          modifiers-2 (sgen/sample (spec/gen ::modifiers/modifier) 3)
          modifiers-3 (sgen/sample (spec/gen ::modifiers/modifier) 5)
          template
          {::template/selections [{::template/name ""
                                   ::template/key :x
                                   ::template/options [{::template/name ""
                                                        ::template/key :y
                                                        ::template/modifiers modifiers-1
                                                        ::template/selections [{::template/name ""
                                                                                ::template/key :z
                                                                                ::template/options [{::template/name ""
                                                                                                     ::template/key :a
                                                                                                     ::template/modifiers modifiers-3}]}]}
                                                       {::template/name ""
                                                        ::template/key :v
                                                        ::template/modifiers modifiers-2}]}]}
          modifiers-map (template/make-modifier-map template)]
      (is (= modifiers-1 (get-in modifiers-map [:x :y ::template/modifiers])))
      (is (= modifiers-2 (get-in modifiers-map [:x :v ::template/modifiers])))
      (is (= modifiers-3 (get-in modifiers-map [:x :y :z :a ::template/modifiers]))))))

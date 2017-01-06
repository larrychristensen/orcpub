(ns orcpub.entity-spec-test
  (:require [clojure.test :refer :all]
            [orcpub.entity-spec :refer :all]))

(deftest test-defentity
  (let [e (make-entity {?x (+ 1 2)
                        ?y (+ 5 ?x)})]
    (is (= 3 (q e ?x)))
    (is (= 8 (q e ?y)))))

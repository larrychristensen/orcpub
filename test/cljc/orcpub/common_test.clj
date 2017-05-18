(ns orcpub.common-test
  (:require [clojure.test :refer [is deftest]]
            [clojure.spec.test.alpha :as stest]
            [orcpub.common :as common]))

(deftest test-add-namespaces-to-keys
  (stest/instrument `common/add-namespaces-to-keys)
  (is (thrown? Exception (common/add-namespaces-to-keys "a.b.c" {::x 1})))
  (is (= {:a.b.c/x 1
          :a.b.c/y "sdlk"} (common/add-namespaces-to-keys "a.b.c" {:x 1 :y "sdlk"})))
  (stest/instrument `common/add-namespaces-to-keys))

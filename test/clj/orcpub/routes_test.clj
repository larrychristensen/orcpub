(ns orcpub.routes-test
  (:require [orcpub.routes :as routes]
            [clojure.test :refer [deftest is]]))

(deftest test-db-ids
  (let [e-1 {:db/id 1
             :x {:db/id 2
                 :y [{:db/id 3
                      :z {:db/id 4}}]}}]
    (is (= (routes/db-ids e-1) #{1 2 3 4}))
    (is (= (routes/db-ids e-1 (routes/diff-branch #{1 2})) #{1 2 3}))))

(deftest test-remove-orphan-ids
  (let [e-1 {:db/id 1
             :s "sere"
             :v 12324
             :x {:db/id 2
                 :y [{:db/id 3
                      :s "xx"
                      :z {:db/id 4}}]
                 :yy [{:db/id 5
                       :v 34
                       :zz {:db/id 6
                            :v 78
                            :zzz [{:db/id 7
                                   :s "String"}]}}]}}
        e-2 {:db/id 1
             :s "sere"
             :v 12324
             :x {:y [{:db/id 3
                      :s "xx"
                      :z {:db/id 4}}]
                 :yy [{:db/id 5
                       :v 34
                       :zz {:db/id 6
                            :v 78
                            :zzz [{:db/id 7
                                   :s "String"}]}}]}}
        e-3 {:db/id 1
             :s "sere"
             :v 12324
             :x {:db/id 2
                 :y [{:s "xx"
                      :z {:db/id 4}}]
                 :yy [{:db/id 5
                       :v 34
                       :zz {:v 78
                            :zzz [{:db/id 7
                                   :s "String"}]}}]}}]
    (is (= (routes/remove-orphan-ids e-1) e-1))
    (is (= (routes/remove-orphan-ids e-2) {:db/id 1
                                           :s "sere"
                                           :v 12324
                                           :x {:y [{:s "xx"
                                                    :z {}}]
                                               :yy [{:v 34
                                                     :zz {:v 78
                                                          :zzz [{:s "String"}]}}]}}))
    (is (= (routes/remove-orphan-ids e-3) {:db/id 1
                                           :s "sere"
                                           :v 12324
                                           :x {:db/id 2
                                               :y [{:s "xx"
                                                    :z {}}]
                                               :yy [{:db/id 5
                                                     :v 34
                                                     :zz {:v 78
                                                          :zzz [{:s "String"}]}}]}}))))

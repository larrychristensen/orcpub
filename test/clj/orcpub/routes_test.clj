(ns orcpub.routes-test
  (:require [orcpub.routes :as routes]
            [orcpub.dnd.e5.magic-items :as mi]
            [orcpub.dnd.e5.character :as char]
            [orcpub.modifiers :as mod]
            [orcpub.entity :as entity]
            [clojure.test :refer [deftest is testing]]
            [orcpub.errors :as errors]
            [io.pedestal.http :as http]
            [io.pedestal.test :refer [response-for]]
            [orcpub.db.schema :as schema]
            [datomic.api :as d]
            [datomock.core :as dm]
            [clojure.set :refer [intersection]])
  (:import [java.util UUID]))

#_(def service
  (::http/service-fn (http/create-servlet {::http/routes routes/routes
                                           ::http/type :jetty
                                           ::http/port 8080})))

#_(deftest test-index
  (let [response (response-for service :get "/")]
    (prn "RESPONSE" response)
    (is (= (:status response)
           200))))

(defmacro with-conn [conn-binding & body]
  `(let [uri# (str "datomic:mem:orcpub-test-" (UUID/randomUUID))
         ~conn-binding (do (d/create-database uri#)
                           (d/connect uri#))]
     (try ~@body
          (finally (d/delete-database uri#)))))

(deftest test-save-entity
  (with-conn conn
    (let [mocked-conn (dm/fork-conn conn)]
      @(d/transact mocked-conn schema/all-schemas)
      @(d/transact mocked-conn [{:orcpub.user/username "testy"
                                 :orcpub.user/email "test@test.com"}
                                {:orcpub.user/username "testy-2"
                                 :orcpub.user/email "test-2@test.com"}])

      (testing "Save new entity"
        (let [entity {::mi/name "Cool Item"}
              saved-entity (routes/save-entity mocked-conn "testy" entity ::mi/owner)]
          (is (= "testy" (::mi/owner saved-entity)))
          (is (int? (:db/id saved-entity)))
          (is (= "Cool Item" (::mi/name saved-entity)))))
      
      (testing "Create and update entity"
        (let [entity {::mi/modifiers [{::mod/key :saving-throw-bonus
                                         ::mod/args [{::mod/keyword-arg ::char/str}
                                                     {::mod/int-arg 1}]}]}
              saved-entity (routes/save-entity mocked-conn "testy" entity ::mi/owner)]
          (is (= "testy" (::mi/owner saved-entity)))
          (is (int? (:db/id saved-entity)))
          (is (int? (get-in saved-entity [::mi/modifiers 0 ::mod/args 1 :db/id])))
          (is (= saved-entity (routes/save-entity mocked-conn "testy" saved-entity ::mi/owner)))))

      (testing "Update other user's entity"
        (let [entity {::mi/modifiers [{::mod/key :saving-throw-bonus
                                       ::mod/args [{::mod/keyword-arg ::char/str}
                                                   {::mod/int-arg 1}]}]}
              saved-entity (routes/save-entity mocked-conn "testy" entity ::mi/owner)
              saved-entity-2 (routes/save-entity mocked-conn "testy-2" entity ::mi/owner)]
          (is (not= saved-entity saved-entity-2))
          (is (thrown? Throwable (routes/save-entity mocked-conn "testy" (update saved-entity :db/id (:db/id saved-entity-2)) ::mi/owner)))))

      (testing "Removal of orphans"
        (let [entity {::mi/modifiers [{::mod/key :saving-throw-bonus
                                       ::mod/args [{::mod/keyword-arg ::char/str}
                                                   {::mod/int-arg 1}]}]}
              saved-entity (routes/save-entity mocked-conn "testy" entity ::mi/owner)
              root-id (:db/id saved-entity)
              child-ids (disj (entity/db-ids saved-entity) root-id)
              update-entity (assoc-in saved-entity [::mi/modifiers 0 :db/id] nil)
              updated-entity (routes/save-entity mocked-conn "testy" update-entity ::mi/owner)
              updated-entity-ids (entity/db-ids updated-entity)]
          (is (= root-id (:db/id updated-entity)))
          (is (empty? (intersection updated-entity-ids child-ids)))))

      (testing "Removal of non-children ids"
        (let [entity {::mi/modifiers [{::mod/key :saving-throw-bonus
                                       ::mod/args [{::mod/keyword-arg ::char/str}
                                                   {::mod/int-arg 1}]}]}
              saved-entity (routes/save-entity mocked-conn "testy" entity ::mi/owner)
              root-id (:db/id saved-entity)
              child-ids (disj (entity/db-ids saved-entity) root-id)
              saved-entity-2 (routes/save-entity mocked-conn "testy-2" entity ::mi/owner)
              update-entity (assoc-in saved-entity [::mi/modifiers 0 :db/id] (:db/id saved-entity-2))
              updated-entity (routes/save-entity mocked-conn "testy" update-entity ::mi/owner)
              updated-entity-ids (entity/db-ids updated-entity)]
          (is (= root-id (:db/id updated-entity)))
          (is (empty? (intersection updated-entity-ids child-ids)))
          (is (not (updated-entity-ids (:db/id saved-entity-2)))))))))

(deftest test-db-ids
  (let [e-1 {:db/id 1
             :x {:db/id 2
                 :y [{:db/id 3
                      :z {:db/id 4}}]}}]
    (is (= (entity/db-ids e-1) #{1 2 3 4}))
    (is (= (entity/db-ids e-1 (routes/diff-branch #{1 2})) #{1 2 3}))))

(deftest test-remove-specific-ids
  (let [e {:db/id 2
           :y {:db/id 3
               :z {:db/id 4}
               :x [{:db/id 5} {:db/id 6 :xx 2}]}}]
    (is (= (entity/remove-specific-ids e #{4 6})
           {:db/id 2
            :y {:db/id 3
                :z {}
                :x [{:db/id 5} {:xx 2}]}}))))

(deftest test-remove-ids
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
        e-2 {:s "sere"
             :v 12324
             :x {:y [{:s "xx"
                      :z {}}]
                 :yy [{:v 34
                       :zz {:v 78
                            :zzz [{:s "String"}]}}]}}]
    (is (= e-2 (entity/remove-ids e-1)))))

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
    (is (= (entity/remove-orphan-ids e-1) e-1))
    (is (= (entity/remove-orphan-ids e-2) {:db/id 1
                                           :s "sere"
                                           :v 12324
                                           :x {:y [{:s "xx"
                                                    :z {}}]
                                               :yy [{:v 34
                                                     :zz {:v 78
                                                          :zzz [{:s "String"}]}}]}}))
    (is (= (entity/remove-orphan-ids e-3) {:db/id 1
                                           :s "sere"
                                           :v 12324
                                           :x {:db/id 2
                                               :y [{:s "xx"
                                                    :z {}}]
                                               :yy [{:db/id 5
                                                     :v 34
                                                     :zz {:v 78
                                                          :zzz [{:s "String"}]}}]}}))))

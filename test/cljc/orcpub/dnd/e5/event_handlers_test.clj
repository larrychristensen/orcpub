(ns orcpub.dnd.e5.event-handlers-test
  (:require [clojure.test :refer [deftest is]]
            [orcpub.entity :as entity]
            [orcpub.entity.strict :as se]
            [orcpub.dnd.e5.event-handlers :as eh]))

(def character
  {:orcpub.entity/options
   {:class
    [{:orcpub.entity/key :barbarian,
      :db/id 17592186055267,
      :orcpub.entity/options
      {:levels
       [{:orcpub.entity/key :level-1, :db/id 17592186055269}
        {:orcpub.entity/key :level-2,
         :db/id 17592186055270,
         :orcpub.entity/options
         {:hit-points
          {:orcpub.entity/key :roll,
           :db/id 17592186055272,
           :orcpub.entity/value 7}}}
        {:orcpub.entity/key :level-3,
         :db/id 17592186055273,
         :orcpub.entity/options
         {:hit-points
          {:orcpub.entity/key :roll,
           :db/id 17592186055275,
           :orcpub.entity/value 5}}}
        {:orcpub.entity/key :level-4,
         :db/id 17592186055288
         :orcpub.entity/options
         {:hit-points
          {:orcpub.entity/key :manual-entry
           :db/id 17592186055290,
           :orcpub.entity/value 1}}}]}}]}
   :db/id 17592186055262})

(defn level-key [level]
  (keyword (str "level-" level)))

(defn test-set-level [c new-level]
  (let [updated (eh/set-class-level c [:set-class-level 0 new-level])
        levels (-> updated
                   ::entity/options
                   :class
                   (get 0)
                   ::entity/options
                   :levels)]
    (is (= (level-key new-level)
           (-> levels
               last
               ::entity/key)))
    (is (= (map level-key (range 1 (inc new-level)))
           (map ::entity/key levels)))
    updated))

(deftest set-class-level--add-level
  (test-set-level character 5))

(deftest set-class-level--add-multiple-levels
  (test-set-level character 20))

(deftest set-class-level--remove-level
  (test-set-level character 3))

(deftest set-class-level--level-1
  (test-set-level character 1))

(deftest set-class-level--same-level
  (test-set-level character 4))

(def levels-strict-path [::se/selections 0 ::se/options 0 ::se/selections 0])

(defn get-levels-id [e]
  (-> e
      (get-in [::entity/options :class 0 ::entity/options :levels])
      meta
      :db/id))

(deftest test-set-level--round-trip
  (let [strict {:db/id 17592186055262, :orcpub.entity.strict/selections [{:db/id 17592186055266, :orcpub.entity.strict/key :class, :orcpub.entity.strict/options [{:db/id 17592186055267, :orcpub.entity.strict/key :barbarian, :orcpub.entity.strict/selections [{:db/id 17592186055268, :orcpub.entity.strict/key :levels, :orcpub.entity.strict/options [{:db/id 17592186055269, :orcpub.entity.strict/key :level-1} {:db/id 17592186055270, :orcpub.entity.strict/key :level-2, :orcpub.entity.strict/selections [{:db/id 17592186055271, :orcpub.entity.strict/key :hit-points, :orcpub.entity.strict/option {:db/id 17592186055272, :orcpub.entity.strict/key :roll, :orcpub.entity.strict/int-value 7}}]} {:db/id 17592186055273, :orcpub.entity.strict/key :level-3, :orcpub.entity.strict/selections [{:db/id 17592186055274, :orcpub.entity.strict/key :hit-points, :orcpub.entity.strict/option {:db/id 17592186055275, :orcpub.entity.strict/key :roll, :orcpub.entity.strict/int-value 5}}]} {:db/id 17592186055288, :orcpub.entity.strict/key :level-4, :orcpub.entity.strict/selections [{:db/id 17592186055289, :orcpub.entity.strict/key :hit-points, :orcpub.entity.strict/option {:db/id 17592186055290, :orcpub.entity.strict/key :manual-entry, :orcpub.entity.strict/int-value 1}}]}]}]}]}]}
        non-strict (entity/from-strict strict)
        updated (test-set-level non-strict 7)
        back-to-strict (entity/to-strict updated)
        without-new-levels (update-in back-to-strict
                                      (conj levels-strict-path ::se/options)
                                      (fn [levels]
                                        (with-meta
                                          (vec (take 4 levels))
                                          (meta levels))))
        levels-id (get-in strict (conj levels-strict-path :db/id))]
    (is (some? levels-id))
    (is (= (get-levels-id non-strict)
           levels-id))
    (is (= (get-levels-id updated)
           levels-id))
    (is (= (get-in back-to-strict (conj levels-strict-path :db/id))
           levels-id))
    (is (= (get-in without-new-levels (conj levels-strict-path :db/id))
           levels-id))
    (is (= (:db/id strict) (:db/id without-new-levels)))
    (is (= strict without-new-levels))))

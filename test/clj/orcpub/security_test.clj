(ns orcpub.security-test
  (:require [clojure.test :refer [testing deftest is]]
            [clj-time.core :as t :refer [hours minutes seconds millis ago now]]
            [orcpub.security :as s]
            [clojure.set :as sets]))

(defn attempts-set [attempts]
  (into
   (sorted-set-by s/compare-dates)
   attempts))

(defn attempts-for-dates [dates]
  (into
   (sorted-set-by s/compare-dates)
   (map
    (fn [date]
      {:date date})
    dates)))

(deftest test-compare-dates
  (is (= 1 (s/compare-dates
            {:date (-> 10 seconds ago)}
            {:date (-> 11 seconds ago)})))
  (is (= 1 (s/compare-dates
            {:date (-> 10 seconds ago)}
            {:date (-> 11 minutes ago)})))
  (is (= -1 (s/compare-dates
            {:date (-> 10 seconds ago)}
            {:date (-> 9 seconds ago)})))
  (is (= -1 (s/compare-dates
            {:date (-> 10 minutes ago)}
            {:date (-> 9 seconds ago)}))))

(deftest test-too-many-attempts-for-username?
  (let [attempts {"larry" (attempts-for-dates (map #(-> % seconds ago) (range 5)))
                  "larry-2" (attempts-for-dates (map #(-> % seconds ago) (range 6)))
                  "redorc" (attempts-for-dates (map #(-> % seconds ago) (range 4)))
                  "redorc-2" (sets/union
                              (attempts-for-dates (map #(-> % seconds ago) (range 1 5)))
                              (attempts-for-dates (map #(-> % hours ago) (range 1 6))))}]
    (is (s/too-many-attempts-for-username-aux "larry" attempts))
    (is (s/too-many-attempts-for-username-aux "larry-2" attempts))
    (is (not (s/too-many-attempts-for-username-aux "redorc" attempts)))
    (is (= 5 (-> "redorc-2" attempts (subseq < {:date (-> 1 minutes ago)}) count)))
    (is (= 4 (-> "redorc-2" attempts (subseq > {:date (-> 1 minutes ago)}) count)))
    (is (not (s/too-many-attempts-for-username-aux "redorc-2" attempts)))
    (is (not (s/too-many-attempts-for-username-aux "larry-3" attempts)))))

(deftest test-multiple-account-access?
  (let [attempts {"1.2.3.4" (attempts-set
                             (map
                              (fn [i]
                                {:date (-> i seconds ago)
                                 :user (str "user-" i)})
                              (range 5)))
                  "1.2.3.5" (attempts-set
                             (map
                              (fn [i]
                                {:date (-> i seconds ago)
                                 :user (str "user-" i)})
                              (range 6)))
                  "1.2.3.6" (attempts-set
                             (map
                              (fn [i]
                                {:date (-> i seconds ago)
                                 :user (str "user-" i)})
                              (range 4)))}
        attempts-2 (update
                    attempts
                    "1.2.3.4"
                    sets/union
                    (attempts-set
                     (map
                      (fn [i]
                        {:date (-> i hours ago)
                         :user (str "user-" i)})
                      (range 1 10))))]
    (is (s/multiple-account-access-aux "1.2.3.4" attempts))
    (is (s/multiple-account-access-aux "1.2.3.5" attempts))
    (is (not (s/multiple-account-access-aux "1.2.3.6" attempts)))
    (is (not (s/multiple-account-access-aux "1.2.3.6" attempts-2)))
    (is (not (s/multiple-account-access-aux "1.2.3.8" attempts-2)))))

(deftest test-multiple-ip-attempts-to-same-account? []
  (let [attempts {"user-1" (attempts-set
                            (map
                             (fn [i]
                               {:date (-> i millis ago)
                                :ip (str i)})
                             (range 3)))
                  "user-2" (attempts-set
                            (map
                             (fn [i]
                               {:date (-> i millis ago)
                                :ip (str i)})
                             (range 4)))
                  "user-3" (attempts-set
                            (map
                             (fn [i]
                               {:date (-> i millis ago)
                                :ip (str i)})
                             (range 2)))}]
    (is (s/multiple-ip-attempts-to-same-account-aux
         "user-1"
         attempts))
    (is (s/multiple-ip-attempts-to-same-account-aux
         "user-1"
         (update
          attempts
          "user-1"
          (fn [as]
            (sets/union
             as
             (attempts-set
              (map
               (fn [s]
                 (assoc s :ip "0"))
               as)))))))
    (is (not
         (s/multiple-ip-attempts-to-same-account-aux
          "user-3"
          (update
           attempts
           "user-3"
           (fn [as]
            (sets/union
             as
             (attempts-set
              (map
               (fn [s]
                 (assoc s :ip "0"))
               as))))))))
    (is (s/multiple-ip-attempts-to-same-account-aux
         "user-2"
         attempts))
    (is (not (s/multiple-ip-attempts-to-same-account-aux
              "user-3"
              attempts)))
    (is (not (s/multiple-ip-attempts-to-same-account-aux
              "user-4"
              attempts)))))

(deftest test-add-and-remove-old
  (let [attempts {"user-1" (attempts-set
                            (map
                             (fn [i]
                               {:ip (str i)
                                :user "user-1"
                                :date (-> i minutes ago)})
                             (range 1 10)))
                  "user-2" (attempts-set
                            (map
                             (fn [i]
                               {:ip (str i)
                                :user "user-2"
                                :date (-> (+ i 10) minutes ago)})
                             (range 1 10)))
                  "user-3" (attempts-set
                            (map
                             (fn [i]
                               {:ip (str i)
                                :user "user-3"
                                :date (-> i minutes ago)})
                             (range 1 20)))}
        result (s/add-and-remove-old
                "user-1"
                {:ip "x"
                 :user "user-1"
                 :date (now)}
                attempts
                (-> 10 minutes ago))]
    (is (-> "user-2" result nil?))
    (is (= 10 (-> "user-1" result count)))
    (is (= 9 (-> "user-3" result count)))))

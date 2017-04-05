(ns orcpub.dice
  (:require [clojure.spec :as spec]
            [orcpub.common :as common]))

(defn die-roll [sides]
  (inc (rand-int sides)))

(defn dice-roll [{:keys [num sides drop-num modifier]}]
  (apply +
         (or modifier 0)
         (drop (or drop-num 0)
               (sort
                (take num (repeatedly #(die-roll 6)))))))

(defn dice-string [die die-count modifier]
  (str die "d" die-count (common/mod-str modifier)))

(defn die-mean [die]
  (int (Math/ceil (/ (apply + (range 1 (inc die))) die))))

(spec/def ::num pos-int?)
(spec/def ::sides pos-int?)
(spec/def ::drop-num pos-int?)
(spec/def ::modifier pos-int?)
(spec/def ::roll-args (spec/keys :req-un [::num ::sides]
                                 :opt-un [::drop-num ::modifier]))
(spec/fdef
 dice-roll
 :args (spec/cat :x ::roll-args)
 :ret pos-int?)

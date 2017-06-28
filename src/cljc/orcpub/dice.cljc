(ns orcpub.dice
  (:require [clojure.spec.alpha :as spec]
            [orcpub.common :as common]))

(defn die-roll [sides]
  (inc (rand-int sides)))

(defn roll-n [num sides]
  (take num (repeatedly #(die-roll sides))))

(defn dice-roll [{:keys [num sides drop-num modifier]}]
  (apply +
         (or modifier 0)
         (drop (or drop-num 0)
               (sort (roll-n num sides)))))

(defn dice-string [die die-count modifier]
  (str die "d" die-count (common/mod-str modifier)))

(defn die-mean [die]
  (int (Math/ceil (/ (apply + (range 1 (inc die))) die))))

(def dice-regex #"(\d+)?d(\d+)\s?([+-])?\s?(\d+)?")

(defn parse-int [s]
  (if s
    #?(:cljs (js/parseInt s))
    #?(:clj (Integer/valueOf s))))

(defn dice-roll-text [dice-text]
  (if-let [[_ num-str sides-str plus-minus-str mod-str :as match]
           (re-matches dice-regex dice-text)]
    (let [num (or (parse-int num-str) 1)
          sides (or (parse-int sides-str) )
          plus-minus (if (= "-" plus-minus-str)
                       -1
                       1)
          raw-mod (or (parse-int mod-str) 0) 
          mod (* raw-mod plus-minus)
          rolls (roll-n num sides)
          total (apply + mod rolls)]
      {:total total
       :rolls rolls
       :mod mod
       :raw-mod raw-mod
       :plus-minus plus-minus})))

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

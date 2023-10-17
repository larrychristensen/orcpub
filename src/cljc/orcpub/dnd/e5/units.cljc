(ns orcpub.dnd.e5.units)

(defn units [type amount]
  {:units type
   :amount amount})

(defn ft [amount]
  (units ::ft amount))

(def ft-5 (ft 5))

(def ft-10 (ft 10))

(def ft-30 (ft 30))

(def ft-60 (ft 60))

(def ft-90 (ft 90))

(def ft-120 (ft 120))

(defn turns [amount]
  (units ::turn amount))

(def turns-1 (turns 1))

(def rounds-1 (units ::round 1))

(defn minutes [amount]
  (units ::minute amount))

(def minutes-1 (minutes 1))

(def minutes-10 (minutes 10))

(defn hours [amount]
  (units ::hour amount))

(def hours-1 (hours 1))

(def hours-2 (hours 2))

(def hours-8 (hours 8))

(def days-1 (units ::day 1))

(defn conc-minutes [amount]
  {:units ::minute
   :concentration true
   :amount amount})

(def conc-minutes-1 (conc-minutes 1))

(def conc-minutes-10 (conc-minutes 10))

(defn conc-hours [amount]
  {:units ::hour
   :concentration true
   :amount amount})

(def conc-hours-1 (conc-hours 1))

(def conc-hours-8 (conc-hours 8))

(defn rests [amount]
  (units ::rest amount))

(def rests-1 (rests 1))

(defn long-rests [amount]
  (units ::long-rest amount))

(def long-rests-1 (long-rests 1))

(defn short-rests [amount]
  (units ::short-rest amount))

(def short-rests-1 (short-rests 1))
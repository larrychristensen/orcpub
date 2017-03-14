(ns orcpub.common
  (:require [clojure.string :as s]))

(defn name-to-kw [name]
  (-> name
      clojure.string/lower-case
      (clojure.string/replace #"'" "")
      (clojure.string/replace #"\W" "-")
      keyword))

(defn map-by-key [values]
  (zipmap (map :key values) values))

(defmacro ptime [message body]
  `(do (prn ~message)
       (time ~body)))

(defn bonus-str [val]
  (str (if (pos? val) "+") val))

(defn mod-str [val]
  (cond (pos? val) (str " + " val)
        (neg? val) (str " - " (int (Math/abs val)))
        :else ""))

(defn map-vals [val-fn m]
  (reduce-kv
   (fn [m2 k v]
     (assoc m2 k (val-fn k v)))
   {}
   m))

(defn list-print [list]
  (case (count list)
    0 ""
    1 (str (first list))
    2 (s/join "and" list)
    (str
     (s/join ", " (butlast list))
     ", and "
     (last list))))

(defn round-up [num]
  (int (Math/ceil (double num))))

(defn sentensize [desc]
  (str
   (s/upper-case (subs desc 0 1))
   (subs desc 1)
   (if (not (s/ends-with? desc "."))
     ".")))

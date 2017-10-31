(ns orcpub.common
  (:require [clojure.string :as s]
            #?(:clj [clojure.spec.alpha :as spec])
            #?(:cljs [cljs.spec.alpha :as spec])))

(def dot-char "•")

(defn- name-to-kw-aux [name ns]
  (if (string? name)
    (as-> name $
        (s/lower-case $)
        (s/replace $ #"'" "")
        (s/replace $ #"\W" "-")
        (s/replace $ #"\-+" "-")
        (keyword ns $))))

(def memoized-name-to-kw (memoize name-to-kw-aux))

(defn name-to-kw [name & [ns]]
  (memoized-name-to-kw name ns))

(defn kw-to-name [kw & [capitalize?]]
  (if (keyword? kw)
    (as-> kw $
      (name $)
      (s/split $ #"\-")
      (if capitalize? (map s/capitalize $) $)
      (s/join " " $))))

(defn map-by [by values]
  (zipmap (map by values) values))

(defn map-by-key [values]
  (map-by :key values))

(defn map-by-id [values]
  (map-by :db/id values))

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

(defn list-print [list & [preceding-last]]
  (let [preceding-last (or preceding-last "and")]
    (case (count list)
      0 ""
      1 (str (first list))
      2 (s/join (str " " preceding-last " ") list)
      (str
       (s/join ", " (butlast list))
       (str ", " preceding-last " ")
       (last list)))))

(defn round-up [num]
  (int (Math/ceil (double num))))

(defn warn [message]
  #?(:cljs (js/console.warn message))
  #?(:clj (prn "WARNING: " message)))

(defn safe-name [kw]
  (if (keyword? kw)
    (name kw)
    (warn (str "non-keyword value passed to safe-name: " kw))))

(defn sentensize [desc]
  (if desc
    (str
     (s/upper-case (subs desc 0 1))
     (subs desc 1)
     (if (not (s/ends-with? desc "."))
       "."))))

(def add-keys-xform
  (map
   #(assoc % :key (name-to-kw (:name %)))))

(defn add-keys [vals]
  (into [] add-keys-xform vals))

(defn remove-first [f v]
  (concat
   (take-while (complement f) v)
   (rest (drop-while (complement f) v))))

(defn add-namespaces-to-keys [ns-str item]
  (into {}
        (map
         (fn [x]
           (let [[k v] x]
             [(if (simple-keyword? k)
                (keyword ns-str (name k))
                k)
              v]))
         item)))

(spec/fdef add-namespaces-to-keys
           :args (spec/cat :ns-str string? :item (spec/map-of keyword? any?))
           :ret (spec/map-of qualified-keyword? any?)
           :fn #(and (= (count (-> % :args :item))
                        (count (-> % :ret)))
                     (= (set (-> % :args :item keys))
                        (set (->> % :ret keys (map (fn [k] (keyword (name k)))))))))

(defn ordinal [i]
  (case i
    1 "1st"
    2 "2nd"
    3 "3rd"
    (str i "th")))

(defn starts-with-letter? [nm]
  (re-matches #"^[a-zA-Z].*" nm))

(defn keyword-starts-with-letter? [kw]
  (and (keyword? kw)
       (-> kw name starts-with-letter?)))

(defn remove-at-index [v index]
  (vec
   (keep-indexed
    (fn [i item]
      (if (not= i index)
        item))
    v)))

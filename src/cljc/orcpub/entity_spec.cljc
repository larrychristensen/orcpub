(ns orcpub.entity-spec
  (:require [clojure.string :as s]))

(defn entity-val [entity k]
  (let [v (entity k)]
    (if (fn? v)
      (v entity)
      v)))

(defn ref-sym-to-kw [sym]
  (keyword (subs (str sym) 1)))

(defmacro q [entity query]
  `(entity-val
    ~entity
    ~(ref-sym-to-kw query)))

(defn ref-to-kw [s entity]
  (if (and (symbol? s)
           (.startsWith (str s) "?"))
    `(entity-val ~entity ~(ref-sym-to-kw s))
    s))

(defn replace-refs [entity body]
  (cond
    (map? body)
    (into
     {}
     (reduce-kv
      (fn [m k v]
        (assoc m (ref-to-kw k entity) (replace-refs entity v)))
      {}
      body))
    (vector? body)
    (mapv #(replace-refs entity %) body)
    (sequential? body)
    (map #(replace-refs entity %) body)
    :else (ref-to-kw body entity)))

(defmacro make-entity [body]
  (reduce-kv
   (fn [m k v]
     (let [arg (gensym "e")
           replaced (replace-refs arg v)]
       (assoc
        m
        (ref-sym-to-kw k)
        (concat `(fn [~arg])
                [replaced]))))
   {}
   `~body))

(defmacro modifier [k body & [nm value]]
  (let [arg (gensym "e")
        replaced (replace-refs arg body)]
    `(with-meta
      ~(concat
        `(fn [~arg])
        `((update ~arg ~(ref-sym-to-kw k) (fn [_#] ~replaced))))
       {:name ~nm
        :value ~value})))

(defmacro vec-mod [q val & [nm value]]
  `(modifier ~q (conj (or ~q []) ~val) ~nm ~value))

(defmacro set-mod [q val & [nm value]]
  `(modifier ~q (conj (or ~q #{}) ~val) ~nm ~value))

(defmacro map-mod [q key val & [nm value]]
  `(modifier ~q (assoc ~q ~key ~val) ~nm ~value))

(defmacro cum-sum-mod [q bonus & [nm value]]
  `(modifier ~q (+ ~q ~bonus) ~nm ~value))

(defmacro modifiers [& mods]
  (mapv
   (fn [mod]
     (cons `modifier mod))
   mods))

(defn apply-modifiers [entity modifiers]
  (reduce
   (fn [e mod] 
     (mod e))
   entity
   modifiers))

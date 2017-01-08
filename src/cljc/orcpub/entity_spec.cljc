(ns orcpub.entity-spec
  (:require [clojure.string :as s]))

(defn entity-val [entity k]
  (let [v (entity k)]
    (if (fn? v)
      (v entity)
      v)))

(defn ref-sym-to-kw [sym nsx]
  (if nsx
    (keyword nsx (subs (str sym) 1))
    (keyword (subs (str sym) 1))))

(defmacro q [entity query & [nsx]]
  `(entity-val
    ~entity
    ~(ref-sym-to-kw query nsx)))

(defn ref-to-kw [s entity & [nsx]]
  (if (and (symbol? s)
           (.startsWith (str s) "?"))
    `(entity-val ~entity ~(ref-sym-to-kw s nsx))
    s))

(defn replace-refs [entity body & [nsx]]
  (cond
    (map? body)
    (into
     {}
     (reduce-kv
      (fn [m k v]
        (assoc m (ref-to-kw k entity) (replace-refs entity v nsx)))
      {}
      body))
    (vector? body)
    (mapv #(replace-refs entity % nsx) body)
    (sequential? body)
    (map #(replace-refs entity % nsx) body)
    :else (ref-to-kw body entity)))

(defmacro make-entity [body & [nsx]]
  (reduce-kv
   (fn [m k v]
     (let [arg (gensym "e")
           replaced (replace-refs arg v nsx)]
       (assoc
        m
        (ref-sym-to-kw k nsx)
        (concat `(fn [~arg])
                [replaced]))))
   {}
   `~body))

(defmacro modifier [k body & [nm nsx]]
  (let [arg (gensym "e")
        replaced (replace-refs arg body nsx)]
    `(with-meta
      ~(concat
        `(fn [~arg])
        `((update ~arg ~(ref-sym-to-kw k nsx) (fn [_#] ~replaced))))
       {:name ~nm})))

(defmacro vec-mod [q val & [nm]]
  `(modifier ~q (conj (or ~q []) ~val) ~nm))

(defmacro set-mod [q val & [nm]]
  `(modifier ~q (conj (or ~q #{}) ~val) ~nm))

(defmacro map-mod [q key val & [nm]]
  `(modifier ~q (assoc ~q ~key ~val) ~nm))

(defmacro cum-sum-mod [q bonus & [nm]]
  `(modifier ~q (+ ~q ~bonus) ~nm))

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

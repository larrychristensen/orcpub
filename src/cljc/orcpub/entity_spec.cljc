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

(defmacro modifier [k body]
  (let [arg (gensym "e")
        replaced (replace-refs arg body)]
    (concat
       `(fn [~arg])
       `((update ~arg ~(ref-sym-to-kw k) (fn [_#] ~replaced))))))

(defmacro vec-mod [k val]
  `(modifier ~k (conj (or ~k []) ~val)))

(defmacro set-mod [k val]
  `(modifier ~k (conj (or ~k #{}) ~val)))

(defmacro map-mod [k key val]
  `(modifier ~k (assoc ~k ~key ~val)))

(defmacro cum-sum-mod [k bonus]
  `(modifier ~k (+ ~k ~bonus)))

(defmacro modifiers [& mods]
  (mapv
   (fn [mod]
     (cons `modifier mod))
   mods))

(defn apply-modifiers [entity modifiers]
  (reduce
   (fn [e mod]
     (prn "MOD " mod)
     (mod e))
   entity
   modifiers))

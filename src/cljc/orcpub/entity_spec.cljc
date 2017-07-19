(ns orcpub.entity-spec
  (:require [clojure.string :as s]
            [clojure.set :as sets]))

(defn entity-val [entity k]
  (let [v (entity k)
        entity-fn? (:entity-fn? (meta v))]
    (if entity-fn?
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
           (s/starts-with? (str s) "?"))
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

(defn deps [k body]
  (let [nodes (tree-seq coll? seq body)]
    (into
     #{}
     (comp
      (filter
       #(and (symbol? %)
             (not= k %)
             (s/starts-with? (name %) "?")))
      (map ref-sym-to-kw))
     nodes)))

(defmacro dependencies [k body]
  (deps k body))

(defmacro entity-dependencies [body]
  (reduce-kv
   (fn [m k v]
     (let [kw (ref-sym-to-kw k)]
       (assoc
        m
        kw
       (deps k v))))
   {}
   `~body))

(defmacro make-entity [body]
  (reduce-kv
   (fn [m k v]
     (let [arg (gensym "e")
           replaced (replace-refs arg v)
           kw (ref-sym-to-kw k)]
       (assoc
        (update m ::deps (fn [d] (update d kw #(sets/union % (deps k v)))))
        kw
        `(with-meta
           ~(concat `(fn [~arg])
                    [replaced])
           {:entity-fn? true}))))
   {}
   `~body))

(defmacro condition [body]
  (let [arg (gensym "e")
        replaced (replace-refs arg body)]
    `(fn [~arg] ~replaced)))

(defmacro conditions [conds]
  (mapv
   (fn [cond]
     `(condition ~cond))
   conds))

(defmacro modifier [k body]
  (let [arg (gensym "e")
        replaced (replace-refs arg body)]
    `(with-meta
       ~(concat
        `(fn [~arg])
        `((update ~arg ~(ref-sym-to-kw k) (fn [_#] ~replaced))))
       {:entity-fn? true})))

(defmacro vec-mod [k val]
  `(modifier ~k (conj (or ~k []) ~val)))

(defmacro set-mod [k val]
  `(modifier ~k (conj (or ~k #{}) ~val)))

(defmacro map-mod [k key val]
  `(modifier ~k (assoc ~k ~key ~val)))

(defn default-to-zero [k]
  (if (number? k)
    k
    0))

(defmacro cum-sum-mod [k bonus]
  `(modifier ~k (+ (default-to-zero ~k) ~bonus)))

(defmacro modifiers [& mods]
  (mapv
   (fn [mod]
     (cons `modifier mod))
   mods))

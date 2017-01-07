(ns orcpub.entity-spec)

(defn entity-val [entity k]
  (let [v (entity k)]
    (if (fn? v)
      (v entity)
      v)))

(defn ref-sym-to-kw [s]
  (keyword (subs (name s) 1)))

(defmacro q [entity query]
  `(entity-val
    ~entity
    ~(ref-sym-to-kw query)))

(defn ref-to-kw [s entity]
  (if (and (symbol? s)
           (.startsWith (name s) "?"))
    `(entity-val ~entity ~(ref-sym-to-kw s))
    s))

(defn replace-refs-2 [entity body]
  (cond
    (map? body)
    (into
     {}
     (reduce-kv
      (fn [m k v]
        (assoc m (ref-to-kw k entity) (replace-refs-2 entity v)))
      {}
      body))
    (vector? body)
    (mapv (partial replace-refs-2 entity) body)
    (sequential? body)
    (map (partial replace-refs-2 entity) body)
    :else (ref-to-kw body entity)))

(defmacro replace-refs [entity body]
  (:x entity))

(defmacro make-entity [body]
  (reduce-kv
   (fn [m k v]
     (let [arg (gensym "e")
           replaced (replace-refs-2 arg v)]
       (assoc
        m
        (ref-sym-to-kw k)
        (concat `(fn [~arg]) [replaced]))))
   {}
   `~body))

(defmacro modifier [k body]
  (let [arg (gensym "e")
        replaced (replace-refs-2 arg body)]
    (concat `(fn [~arg]) `((update ~arg ~(ref-sym-to-kw k) (fn [_#] ~replaced))))))

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

(def char3
  (make-entity
   {?x (+ 1 2)
    ?y (+ 5 ?x)}))

(def char4
  {:x (fn [c] (+ 1 2))
   :y (fn [c] (+ 5 ((:x c) c)))})

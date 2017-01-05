(ns orcpub.entity-spec)

(defn entity-val [k entity]
  (let [v (entity k)]
    (if (fn? v)
      (v entity)
      v)))

(defn ref-sym-to-kw [s]
  (keyword (subs (name s) 1)))

(defn ref-to-kw [s entity]
  (if (and (symbol? s)
           (.startsWith (name s) "?"))
    `(entity-val ~(ref-sym-to-kw s) ~entity)
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

(defmacro defentity [nm body]
  `(def ~nm
     ~(reduce-kv
       (fn [m k v]
         (let [replaced (replace-refs-2 'e v)]
           (assoc
            m
            (ref-sym-to-kw k)
            (concat '(fn [e]) [replaced]))))
       {}
       body)))

(defentity char1
  {?levels {:wizard 1
            :rogue 4}
   ?abilities {:str 18 :dex 12 :con 14 :int 15 :wis 17 :cha 19}
   ?ability-bonuses (reduce-kv
                     (fn [m k v]
                       (assoc m k (int (/ (- v 10) 2))))
                     {}
                     ?abilities)
   ?total-levels (apply + (vals ?levels))
   ?prof-bonus (+ (int (/ (dec ?total-levels) 4)) 2)})

(defentity char3
  {?x (+ 1 2)
   ?y (+ 5 ?x)})

(def char4
  {:x (fn [c] (+ 1 2))
   :y (fn [c] (+ 5 ((:x c) c)))})

(def char2
  {:levels (fn [c]
             {:wizard 1
              :rogue 4})
   :abilities (fn [c]
                {:str 18 :dex 12 :con 14 :int 15 :wis 17 :cha 19})
   :ability-bonuses (fn [c]
                      (reduce-kv
                       (fn [m k v]
                         (assoc m k (int (/ (- v 10) 2))))
                       {}
                       (entity-val :abilities c)))
   :total-levels (fn [c] (apply + (vals (entity-val :levels c))))
   :prof-bonus (fn [c] (+ (int (/ (dec (entity-val :total-levels c)) 4)) 2))})

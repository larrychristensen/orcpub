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

(def skills [{:key :athletics
              :ability :str}
             {:key :acrobatics
              :ability :dex}
             {:key :perception
              :ability :wis}])

(def skill-abilities
  (into {} (map (juxt :key :ability)) skills))

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
   ?prof-bonus (+ (int (/ (dec ?total-levels) 4)) 2)
   ?skill-profs #{:athletics :perception}
   ?skill-prof-bonuses (into {}
                             (map (fn [{k :key}]
                                    [k (if (?skill-profs k) ?prof-bonus 0)]))
                             skills)
   ?skill-bonuses (into {}
                        (map
                         (fn [[k v]]
                           [k (+ v (?ability-bonuses (skill-abilities k)))]))
                        ?skill-prof-bonuses)})

(defmacro modifier [k body]
  (let [arg (gensym "e")
        replaced (replace-refs-2 arg body)]
    (concat `(fn [~arg]) `((update ~arg ~(ref-sym-to-kw k) (fn [_#] ~replaced))))))

(def modifiers2
  [(modifier ?skill-expertise (conj ?skill-expertise :perception))
   (modifier ?abilities (update ?abilities :dex + 2))])

(def char3
  (make-entity
   {?x (+ 1 2)
    ?y (+ 5 ?x)}))

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

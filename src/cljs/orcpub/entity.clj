(ns orcpub.entity)

(defn entity-val [k entity]
  (let [v (entity k)]
    (if (fn? v)
      (v entity)
      v)))

(defmacro defentity [name body]
  (+ 1 2))

(defentity char1
  {::ability-bonuses (fn [char] (reduce-kv
                                 (fn [m k v]
                                   (assoc m k (int (/ (- v 10) 2))))
                                 {}
                                 (entity-val ::abilities char)))
   ::skill-prof-bonuses (fn [char] (map (fn [skill]) ))
   ::total-levels (fn [char] (apply + (vals (::levels char))))
   ::prof-bonus (fn [char] (+ (/ (dec (::total-levels char)) 4) 2))})

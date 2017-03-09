(ns orcpub.pdf-spec
  (:require [clojure.string :as s]
            [orcpub.common :as common]
            [orcpub.entity-spec :as es]
            [orcpub.dnd.e5.character :as char5e]
            [orcpub.dnd.e5.options :as opt5e]
            [orcpub.dnd.e5.spells :as spells]))

(defn entity-vals [built-char kws]
  (reduce
   (fn [vs kw]
     (let [[to from] (if (keyword? kw) [kw kw] kw)]
       (assoc vs to (es/entity-val built-char from))))
   {}
   kws))

(defn class-string [levels]
  (s/join
   " / "
   (map
    (fn [[cls-k {:keys [class-name class-level subclass]}]]
      (str class-name " (" class-level ")"))
    levels)))

(defn ability-related-bonuses [suffix vals]
  (into {}
        (map
         (fn [[k v]]
           [(keyword (str (name k) "-" suffix)) (common/bonus-str v)])
         vals)))

(defn ability-bonuses [built-char]
  (ability-related-bonuses "mod" (char5e/ability-bonuses built-char)))

(defn save-bonuses [built-char]
  (ability-related-bonuses "save" (es/entity-val built-char :save-bonuses)))

(defn skill-fields [built-char]
  (let [skill-bonuses (es/entity-val built-char :skill-bonuses)
        skill-profs (es/entity-val built-char :skill-profs)]
    (reduce
     (fn [m {k :key}]
       (-> m
           (assoc k (common/bonus-str (skill-bonuses k)))
           (assoc (keyword (str (name k) "-check")) (boolean (k skill-profs)))))
     {}
     opt5e/skills)))

(defn total-length [traits]
  (reduce + (map
             (fn [{:keys [name description]}]
               (+ (count name) (count description)))
             traits)))

(defn traits-string [traits]
  (s/join
   "\n\n"
   (map
    (fn [{:keys [name description]}]
      (str name ". " description))
    traits)))

(defn traits-fields [built-char]
  (let [traits (es/entity-val built-char :traits)
        total-len (total-length traits)
        half-len (/ total-len 2)
        half-traits (reduce
                     (fn [ht t]
                       (if (> (total-length ht) half-len)
                         (reduced ht)
                         (conj ht t)))
                     []
                     traits)
        other-half-traits (drop (count half-traits) traits)]
    {:features-and-traits (traits-string traits)
     :features-and-traits-2 (traits-string other-half-traits)}))

(defn equipment-fields [built-char]
  {:equipment (s/join
    "; "
    (map
     (fn [[kw count]]
       (str (:name (opt5e/equipment-map kw)) " (" count ")"))
     (es/entity-val built-char :equipment)))})

(defn spellcasting-fields [built-char]
  (let [spells-known (es/entity-val built-char :spells-known)
        spell-attack-modifier-fn (es/entity-val built-char :spell-attack-modifier)
        spell-save-dc-fn (es/entity-val built-char :spell-save-dc)
        all-abilities (vec
                       (set
                        (flatten
                         (map
                          (fn [[_ spells]]
                            (map :ability spells))
                          spells-known))))
        _ (prn "ALL_ABILITITE" all-abilities)
        num-abilities (count all-abilities)]
    (apply
     merge
     (flatten
      (for [[level spells] spells-known
            :let [by-ability (group-by :ability spells)]
            ability-index (range num-abilities)
            :let [ability (all-abilities ability-index)
                  ability-items (vec (by-ability ability))]
            item-index (range (count ability-items))
            :let [item (ability-items item-index)
                  suffix (str "-" (inc ability-index))]]
        (do
          (prn level ability-index ability item-index (:key item))
          [{(keyword (str "spellcasting-ability" suffix)) (:name (opt5e/abilities-map ability))}
           {(keyword (str "spell-save-dc" suffix)) (spell-save-dc-fn ability)}
           {(keyword (str "spell-attack-bonus" suffix)) (spell-attack-modifier-fn ability)}
           {(keyword (str "spells-" level "-" (inc item-index) suffix))
            (:name (spells/spell-map (:key item)))}]))))))

(defn make-spec [built-char]
  (let [race (es/entity-val built-char :race)
        subrace (es/entity-val built-char :subrace)
        abilities (char5e/ability-values built-char)
        saving-throws (set (es/entity-val built-char :saving-throws))
        unarmored-armor-class (es/entity-val built-char :armor-class)
        ac-with-armor-fn (es/entity-val built-char :armor-class-with-armor)
        equipped-armor (es/entity-val built-char :armor)
        has-shield? (:shield equipped-armor)
        armored-armor-classes (map
                               (fn [[kw _]]
                                 (ac-with-armor-fn (opt5e/armor-map kw)))
                               (dissoc equipped-armor :shield))
        unshielded-armor-classes (conj armored-armor-classes unarmored-armor-class)
        armor-classes (if has-shield? (map (partial + 2) unshielded-armor-classes) unshielded-armor-classes)
        max-armor-class (apply max armor-classes)
        levels (char5e/levels built-char)
        total-hit-dice (apply + (map :class-level (vals levels)))]
    (prn (es/entity-val built-char :spells-known))
    (merge
     {:race (str race (if subrace (str "/" subrace)))
      :class-level (class-string levels)
      :background (char5e/background built-char)
      :prof-bonus (common/bonus-str (es/entity-val built-char :prof-bonus))
      :ac max-armor-class
      :hd-total total-hit-dice
      :initiative (common/bonus-str (es/entity-val built-char :initiative))
      :speed (es/entity-val built-char :speed)
      :hp-max (es/entity-val built-char :max-hit-points)
      :passive (es/entity-val built-char :passive-perception)}
     (skill-fields built-char)
     abilities
     (ability-bonuses built-char)
     (save-bonuses built-char)
     (reduce
      (fn [saves key]
        (assoc saves (keyword (str (name key) "-save-check")) (boolean (key saving-throws))))
      {}
      char5e/ability-keys)
     (traits-fields built-char)
     (equipment-fields built-char)
     (spellcasting-fields built-char))))

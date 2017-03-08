(ns orcpub.pdf-spec
  (:require [clojure.string :as s]
            [orcpub.common :as common]
            [orcpub.entity-spec :as es]
            [orcpub.dnd.e5.character :as char5e]
            [orcpub.dnd.e5.options :as opt5e]))

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
    (merge
     {:race (str race (if subrace (str "/" subrace)))
      :class-level (class-string levels)
      :background (char5e/background built-char)
      :prof-bonus (common/bonus-str (es/entity-val built-char :prof-bonus))
      :ac max-armor-class
      :hd-total total-hit-dice
      :initiative (common/bonus-str (es/entity-val built-char :initiative))
      :speed (es/entity-val built-char :speed)}
     abilities
     (ability-bonuses built-char)
     (save-bonuses built-char)
     (reduce
      (fn [saves key]
        (assoc saves (keyword (str (name key) "-save-check")) (boolean (key saving-throws))))
      {}
      char5e/ability-keys))))

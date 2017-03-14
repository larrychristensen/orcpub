(ns orcpub.pdf-spec
  (:require [clojure.string :as s]
            [orcpub.common :as common]
            [orcpub.entity-spec :as es]
            [orcpub.dnd.e5.character :as char5e]
            [orcpub.dnd.e5.options :as opt5e]
            [orcpub.dnd.e5.spells :as spells]
            [orcpub.dnd.e5.display :as disp5e]))

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
    (fn [{:keys [name description summary page source]}]
      (str name ". " (or summary description) (if page (str " (" (or source "PHB ") page ")"))))
    traits)))

(defn traits-fields [built-char]
  (let [traits (sort-by :name (es/entity-val built-char :traits))
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
    {:features-and-traits (traits-string half-traits)
     :features-and-traits-2 (traits-string other-half-traits)}))

(defn attacks-string [attacks]
  (s/join
   "\n\n"
   (map
    (fn [attack]
      (str (:name attack) ". " (disp5e/attack-description attack)))
    attacks)))

(defn equipment-fields [built-char]
  (let [equipment (es/entity-val built-char :equipment)]
    {:equipment (s/join
                 "; "
                 (map
                  (fn [[kw count]]
                    (str (:name (opt5e/equipment-map kw)) " (" count ")"))
                  equipment))}))

(def level-max-spells
  {0 8
   1 12
   2 13
   3 13
   4 13
   5 9
   6 9
   7 9
   8 7
   9 7})

(defn make-page-map [spells-known]
  (reduce-kv
   (fn [m k s]
     (let [by-ability (group-by :ability s)]
       (reduce-kv
        (fn [am a a-s]
          (assoc-in am [a k] (sort-by (comp :name spells/spell-map :key) a-s)))
        m
        by-ability)))
   {}
   spells-known))

(defn make-pages [spells]
  (let [page-map (make-page-map spells)]
    (mapcat
     (fn [[ability levels]]
       (let [ability-classes (into
                              (sorted-set)
                              (mapcat
                               (fn [[_ spells]]
                                 (map :class spells))
                               levels))
             split-levels (common/map-vals
                           (fn [level spells]
                             (vec (partition-all (level-max-spells level) spells)))
                           levels)
             num-pages (apply max
                              (map (fn [[level parts]]
                                     (count parts))
                                   split-levels))]
         (for [page-index (range num-pages)]
           {:ability ability
            :classes ability-classes
            :spells (map (fn [level]
                           {:level level
                            :spells (get-in split-levels [level page-index])})
                         (range 10))})))
     page-map)))

(defn spell-page-fields [spells save-dc-fn attack-mod-fn]
  (let [spell-pages (make-pages spells)]
    (apply
     merge
     (flatten
      (map-indexed
       (fn [i {:keys [ability classes spells]}]
         (let [suffix (str "-" (inc i))]
           [{(keyword (str "spellcasting-class" suffix)) (s/join ", " classes)
             (keyword (str "spellcasting-ability" suffix)) (:name (opt5e/abilities-map ability))
             (keyword (str "spell-save-dc" suffix)) (save-dc-fn ability)
             (keyword (str "spell-attack-bonus" suffix)) (common/bonus-str (attack-mod-fn ability))}
            (map
             (fn [{:keys [level spells]}]
               (map-indexed
                (fn [spell-index spell]
                  {(keyword (str "spells-" level "-" (inc spell-index) suffix))
                   (:name (spells/spell-map (:key spell)))})
                spells))
             spells)]))
       spell-pages)))))

(defn spellcasting-fields [built-char]
  (let [spells-known (es/entity-val built-char :spells-known)
        spell-attack-modifier-fn (es/entity-val built-char :spell-attack-modifier)
        spell-save-dc-fn (es/entity-val built-char :spell-save-dc)]
    (spell-page-fields spells-known spell-save-dc-fn spell-attack-modifier-fn)))

(defn profs-paragraph [profs prof-map title]
  (if (seq profs)
    (str
     title
     " Proficiencies: "
     (s/join "; " (sort (map :name profs))))))

(defn other-profs-field [built-char]
  (let [tool-profs (es/entity-val built-char :tool-profs)
        weapon-profs (es/entity-val built-char :weapon-profs)
        armor-profs (es/entity-val built-char :armor-profs)
        languages (es/entity-val built-char :languages)]
    (s/join
     "\n\n"
     (remove
      nil?
      [(profs-paragraph tool-profs opt5e/tools-map "Tool")
       (profs-paragraph weapon-profs opt5e/weapons-map "Weapon")
       (profs-paragraph armor-profs opt5e/armor-map "Armor")
       (profs-paragraph languages opt5e/language-map "Language")]))))

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
        total-hit-dice (s/join
                        " / "
                        (map
                         (fn [{:keys [class-level hit-die]}] (str class-level "D" hit-die))
                         (vals levels)))]
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
      :passive (es/entity-val built-char :passive-perception)
      :other-profs (other-profs-field built-char)
      :personality-traits (s/join "\n\n" [(es/entity-val built-char :personality-trait-1) (es/entity-val built-char :personality-trait-2)])
      :ideals (es/entity-val built-char :ideals)
      :bonds (es/entity-val built-char :bonds)
      :flaws (es/entity-val built-char :flaws)
      :backstory (es/entity-val built-char :description)
      :character-name (es/entity-val built-char :character-name)
      :player-name (es/entity-val built-char :player-name)
      :attacks-and-spellcasting (attacks-string (es/entity-val built-char :attacks))}
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

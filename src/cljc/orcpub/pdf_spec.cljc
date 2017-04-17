(ns orcpub.pdf-spec
  (:require [clojure.string :as s]
            [orcpub.common :as common]
            [orcpub.entity-spec :as es]
            [orcpub.dice :as dice]
            [orcpub.dnd.e5.character :as char5e]
            [orcpub.dnd.e5.options :as opt5e]
            [orcpub.dnd.e5.spells :as spells]
            [orcpub.dnd.e5.display :as disp5e]
            [orcpub.dnd.e5.weapons :as weapon5e]
            [orcpub.dnd.e5.armor :as armor5e]
            [orcpub.dnd.e5.equipment :as equip5e]
            [orcpub.dnd.e5.magic-items :as mi5e]
            [orcpub.dnd.e5.skills :as skill5e]))

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
     skill5e/skills)))

(defn total-length [traits]
  (reduce + (map
             (fn [{:keys [name description]}]
               (+ (count name) (count description)))
             traits)))

(defn trait-string [nm desc]
  (str nm ". " (common/sentensize desc)))

(defn traits-string [traits]
  (s/join
   "\n\n"
   (map
    (fn [{:keys [name description summary page source] :as trait}]
      (trait-string name (disp5e/action-description trait)))
    traits)))

(defn actions-string [title actions]
  (if (seq actions)
    (str
     title
     "\n"
     (s/join
      "\n\n"
      (map
       (fn [action]
         (trait-string (:name action) (common/sentensize (disp5e/action-description action))))
       actions)))))

(defn traits-fields [built-char]
  (let [bonus-actions (sort-by :name (es/entity-val built-char :bonus-actions))
        actions (sort-by :name (es/entity-val built-char :actions))
        reactions (sort-by :name (es/entity-val built-char :reactions))
        traits (sort-by :name (es/entity-val built-char :traits))
        traits-str (traits-string traits)
        actions? (or (seq bonus-actions)
                     (seq actions)
                     (seq reactions))]
    {:features-and-traits (if actions?
                            (s/join
                             "\n\n"
                             (remove nil?
                                     [(actions-string "----------Bonus Actions----------" bonus-actions)
                                      (actions-string "---------------Actions--------------" actions)
                                      (actions-string "-------------Reactions-------------" reactions)
                                      (if traits "(additional features & traits on page 2)")]))
                            traits-str)
     :features-and-traits-2 (if actions? traits-str)}))

(defn attack-string [attack]
  (str "- " (trait-string (:name attack) (disp5e/attack-description attack))))

(defn attacks-string [attacks]
  (s/join
   "\n\n"
   (map
    attack-string
    attacks)))

(defn equipment-fields [built-char]
  (let [equipment (es/entity-val built-char :equipment)
        armor (es/entity-val built-char :armor)
        magic-armor (es/entity-val built-char :magic-armor)
        magic-items (es/entity-val built-char :magic-items)
        weapons (es/entity-val built-char :weapons)
        magic-weapons (es/entity-val built-char :magic-weapons)
        all-equipment (merge equipment magic-items armor magic-armor)
        treasure (es/entity-val built-char :treasure)
        treasure-map (into {} (map (fn [[kw {qty :quantity}]] [kw qty]) treasure))
        unequipped-items (filter
                          (fn [[kw {:keys [equipped? quantity]}]]
                            (and (not equipped?)
                                 (pos? quantity)))
                          (merge all-equipment weapons magic-weapons))]
    (prn "AL EQUS" unequipped-items)
    (merge
     (select-keys treasure-map [:cp :sp :ep :gp :pp])
     {:equipment (s/join
                  "; "
                  (map
                   (fn [[kw {count :quantity}]]
                     (str (:name (mi5e/all-equipment-map kw)) " (" count ")"))
                   (filter
                    (fn [[kw {:keys [equipped? quantity]}]] (and equipped? (pos? quantity)))
                    all-equipment)))
      :treasure (s/join
                  "; "
                  (map
                   (fn [[kw {count :quantity}]]
                     (str (:name (mi5e/all-equipment-map kw)) " (" count ")"))
                   unequipped-items))})))

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

(defn spell-page-fields [spells spell-slots save-dc-fn attack-mod-fn]
  (let [spell-pages (make-pages spells)]
    (apply
     merge
     (flatten
      (map-indexed
       (fn [i {:keys [ability classes spells]}]
         (let [suffix (str "-" (inc i))
               class-header {(keyword (str "spellcasting-class" suffix)) (s/join ", " classes)}]
           [(if ability
              (merge
               class-header
               {(keyword (str "spellcasting-ability" suffix)) (:name (opt5e/abilities-map ability))
                (keyword (str "spell-save-dc" suffix)) (save-dc-fn ability)
                (keyword (str "spell-attack-bonus" suffix)) (common/bonus-str (attack-mod-fn ability))})
              class-header)
            (map
             (fn [{:keys [level spells]}]
               (conj
                (map-indexed
                 (fn [spell-index spell]
                   {(keyword (str "spells-" level "-" (inc spell-index) suffix))
                    (str (:name (spells/spell-map (:key spell))) (let [qualifier (:qualifier spell)]
                                                                   (if qualifier
                                                                     (str " (" qualifier ")"))))})
                 spells)
                {(keyword (str "spell-slots-" level suffix))
                 (spell-slots level)}))
             spells)]))
       spell-pages)))))

(defn spellcasting-fields [built-char]
  (let [spells-known (es/entity-val built-char :spells-known)
        spell-attack-modifier-fn (es/entity-val built-char :spell-attack-modifier)
        spell-save-dc-fn (es/entity-val built-char :spell-save-dc)
        spell-slots (es/entity-val built-char :spell-slots)]
    (spell-page-fields spells-known spell-slots spell-save-dc-fn spell-attack-modifier-fn)))

(defn profs-paragraph [profs prof-map title]
  (if (seq profs)
    (str
     title
     " Proficiencies: "
     (s/join "; " (map (fn [p]
                         (let [prof (prof-map p)]
                           (if prof
                             (:name prof)
                             (s/capitalize (name p)))))
                       (sort profs))))))

(defn other-profs-field [built-char]
  (let [tool-profs (es/entity-val built-char :tool-profs)
        weapon-profs (es/entity-val built-char :weapon-profs)
        armor-profs (es/entity-val built-char :armor-profs)
        languages (es/entity-val built-char :languages)]
    (s/join
     "\n\n"
     (remove
      nil?
      [(profs-paragraph tool-profs equip5e/tools-map "Tool")
       (profs-paragraph weapon-profs weapon5e/weapons-map "Weapon")
       (profs-paragraph armor-profs armor5e/armor-map "Armor")
       (profs-paragraph languages opt5e/language-map "Language")]))))

(defn damage-str [die die-count mod damage-type]
  (str (dice/dice-string die-count die mod)
       (if damage-type (str " " (name damage-type)))))

(defn attacks-and-spellcasting-fields [built-char]
  (let [all-weapons (mi5e/equipped-items-details (char5e/all-weapons-inventory built-char) mi5e/all-weapons-map)
        weapon-fields (mapcat
                       (fn [{:keys [name damage-die damage-die-count damage-type] :as weapon}]
                         (let [versatile (:versatile weapon)
                               normal-damage-modifier (char5e/weapon-damage-modifier built-char weapon false)
                               finesse-damage-modifier (char5e/weapon-damage-modifier built-char weapon true)
                               normal {:name (:name weapon)
                                       :attack-bonus (char5e/weapon-attack-modifier built-char weapon false)
                                       :damage (damage-str damage-die damage-die-count normal-damage-modifier damage-type)}]
                           (remove
                            nil?
                            [normal
                             (if (:versatile weapon)
                               {:name (str (:name weapon) " (two-handed)")
                                :attack-bonus (char5e/weapon-attack-modifier built-char weapon false)
                                :damage (damage-str (:damage-die versatile) (:damage-die-count versatile) normal-damage-modifier damage-type)})
                             (if (:finesse? weapon)
                               {:name (str (:name weapon) " (finesse)")
                                :attack-bonus (char5e/weapon-attack-modifier built-char weapon true)
                                :damage (damage-str damage-die damage-die-count finesse-damage-modifier damage-type)})])))
                       all-weapons)
        first-3-weapons (take 3 weapon-fields)
        rest-weapons (drop 3 weapon-fields)]
    (apply merge
     {:attacks-and-spellcasting (s/join "\n"
                                        (concat (map
                                                 attack-string
                                                 (es/entity-val built-char :attacks))
                                                (map
                                                 (fn [{:keys [name attack-bonus damage]}]
                                                   (str "- " name ". " (common/bonus-str attack-bonus) ", " damage))
                                                 rest-weapons)))}
     (map-indexed
      (fn [i {:keys [name attack-bonus damage]}]
        {(keyword (str "weapon-name-" (inc i))) name
         (keyword (str "weapon-attack-bonus-" (inc i))) (common/bonus-str attack-bonus)
         (keyword (str "weapon-damage-" (inc i))) damage})
      first-3-weapons))))

(defn make-spec [built-char]
  (let [race (char5e/race built-char)
        subrace (char5e/subrace built-char)
        abilities (char5e/ability-values built-char)
        saving-throws (set (char5e/saving-throws built-char))
        unarmored-armor-class (char5e/base-armor-class built-char)
        ac-with-armor-fn (char5e/armor-class-with-armor built-char)
        all-armor-inventory (mi5e/equipped-armor-details (char5e/all-armor-inventory built-char))
        equipped-armor (armor5e/non-shields all-armor-inventory)
        equipped-shields (armor5e/shields all-armor-inventory)
        all-armor-classes (for [armor (conj equipped-armor nil)
                                shield (conj equipped-shields nil)]
                            (ac-with-armor-fn armor shield))
        max-armor-class (apply max all-armor-classes)
        levels (char5e/levels built-char)
        total-hit-dice (s/join
                        " / "
                        (map
                         (fn [{:keys [class-level hit-die]}] (str class-level "D" hit-die))
                         (vals levels)))]
    (merge
     {:race (str race (if subrace (str "/" subrace)))
      :alignment (char5e/alignment built-char)
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
      :player-name (es/entity-val built-char :player-name)}
     (attacks-and-spellcasting-fields built-char)
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

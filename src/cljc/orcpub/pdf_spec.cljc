(ns orcpub.pdf-spec
  (:require [clojure.string :as s]
            [orcpub.common :as common]
            [orcpub.entity-spec :as es]
            [orcpub.dice :as dice]
            [orcpub.dnd.e5.character :as char5e]
            [orcpub.dnd.e5.character.equipment :as char-equip5e]
            [orcpub.dnd.e5.options :as opt5e]
            [orcpub.dnd.e5.spells :as spells]
            [orcpub.dnd.e5.display :as disp5e]
            [orcpub.dnd.e5.weapons :as weapon5e]
            [orcpub.dnd.e5.armor :as armor5e]
            [orcpub.dnd.e5.equipment :as equip5e]
            [orcpub.dnd.e5.magic-items :as mi5e]
            [orcpub.dnd.e5.skills :as skill5e]
            [re-frame.core :refer [subscribe]]))

(defn entity-vals [built-char kws]
  (reduce
   (fn [vs kw]
     (let [[to from] (if (keyword? kw) [kw kw] kw)]
       (assoc vs to (es/entity-val built-char from))))
   {}
   kws))

(defn class-string [classes levels]
  (s/join
   " / "
   (map
    (fn [cls-key]
      (let [{:keys [class-name class-level subclass]} (levels cls-key)]
        (str class-name " (" class-level ")")))
    classes)))

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
        skill-profs (char5e/skill-proficiencies built-char)]
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

(defn vec-trait [nm items]
  (if (seq items) (str nm ": " (s/join ", " items))))
 
(defn keyword-vec-trait [nm keywords]
  (vec-trait nm (map name keywords)))

(defn resistance-strings [resistances]
  (map
   (fn [{:keys [value qualifier]}]
     (str (name value)
          (if qualifier
            (str "(" qualifier ")"))))
   resistances))

(defn features-and-traits-header [built-char]
  (let [darkvision (char5e/darkvision built-char)
        damage-resistances (char5e/damage-resistances built-char)
        damage-immunities (char5e/damage-immunities built-char)
        condition-immunities (char5e/condition-immunities built-char)
        immunities (char5e/immunities built-char)
        number-of-attacks (char5e/number-of-attacks built-char)
        crit-values (char5e/critical-hit-values built-char)]
    (s/join
     "\n"
     (remove nil?
             [(if (and darkvision (pos? darkvision)) (str "Darkvision: " darkvision " ft."))
              (if (> number-of-attacks 1) (str "Number of Attacks: " number-of-attacks))
              (if (> (count crit-values) 1) (str "Critical Hits: " (char5e/crit-values-str built-char)))
              (vec-trait "Damage Resistances" (resistance-strings damage-resistances))
              (vec-trait "Damage Immunities" (resistance-strings damage-immunities))
              (vec-trait "Condition Immunities" (resistance-strings condition-immunities))
              (vec-trait "Immunities" (resistance-strings immunities))]))))

(defn traits-fields [built-char]
  (let [bonus-actions (sort-by :name (es/entity-val built-char :bonus-actions))
        actions (sort-by :name (es/entity-val built-char :actions))
        reactions (sort-by :name (es/entity-val built-char :reactions))
        traits (sort-by :name (es/entity-val built-char :traits))
        traits-str (traits-string traits)
        actions? (or (seq bonus-actions)
                     (seq actions)
                     (seq reactions))
        header (features-and-traits-header built-char)]
    {:features-and-traits (str
                           (if (not (s/blank? header)) (str header "\n\n"))
                           (if actions?
                             (s/join
                              "\n\n"
                              (remove nil?
                                      [(actions-string "----------Bonus Actions----------" bonus-actions)
                                       (actions-string "---------------Actions--------------" actions)
                                       (actions-string "-------------Reactions-------------" reactions)
                                       (if traits "(additional features & traits on page 2)")]))
                             traits-str))
     :features-and-traits-2 (if actions? traits-str)}))

(defn attack-string [attack]
  (str "- " (trait-string (:name attack) (disp5e/attack-description attack))))

(defn attacks-string [attacks]
  (s/join
   "\n\n"
   (map
    attack-string
    attacks)))

(def coin-keys [:cp :sp :ep :gp :pp])

(defn equipment-fields [built-char]
  (let [equipment (es/entity-val built-char :equipment)
        armor (es/entity-val built-char :armor)
        magic-armor (es/entity-val built-char :magic-armor)
        magic-items (es/entity-val built-char :magic-items)
        weapons (es/entity-val built-char :weapons)
        magic-weapons (es/entity-val built-char :magic-weapons)
        custom-equipment (into {}
                               (map
                                (juxt ::char-equip5e/name identity)
                                (char5e/custom-equipment built-char)))
        custom-treasure (into {}
                               (map
                                (juxt ::char-equip5e/name identity)
                                (char5e/custom-treasure built-char)))
        all-equipment (concat equipment custom-equipment custom-treasure magic-items armor magic-armor)
        treasure (es/entity-val built-char :treasure)
        treasure-map (into {} (map (fn [[kw {qty ::char-equip5e/quantity}]] [kw qty]) treasure))
        unequipped-items (filter
                          (fn [[kw {:keys [::char-equip5e/equipped? ::char-equip5e/quantity]}]]
                            (and (not equipped?)
                                 (pos? quantity)))
                          (merge all-equipment weapons magic-weapons))]
    (merge
     (select-keys treasure-map coin-keys)
     {:equipment (s/join
                  "; "
                  (map
                   (fn [[kw {count ::char-equip5e/quantity}]]
                     (str (disp5e/equipment-name mi5e/all-equipment-map kw) " (" count ")"))
                   (filter
                    (fn [[kw {:keys [::char-equip5e/equipped? ::char-equip5e/quantity]}]] (and equipped? (pos? quantity)))
                    (concat
                     all-equipment))))
      :treasure (s/join
                  "; "
                  (map
                   (fn [[kw {count ::char-equip5e/quantity}]]
                     (str (disp5e/equipment-name mi5e/all-equipment-map kw) " (" count ")"))
                   (concat
                    treasure
                    unequipped-items)))})))

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

(defn filter-prepared [spell-cfgs
                       lvl
                       prepares-spells
                       prepared-spells-by-class]
  (filter
   (fn [{:keys [key class always-prepared?]}]
     (char5e/spell-prepared? {:hide-unprepared? true
                              :always-prepared? always-prepared?
                              :lvl lvl
                              :key key
                              :class class
                              :prepares-spells prepares-spells
                              :prepared-spells-by-class prepared-spells-by-class}))
   spell-cfgs))

(defn make-page-map [spells-known
                     print-prepared-spells?
                     prepares-spells
                     prepared-spells-by-class]
  (reduce-kv
   (fn [m k s]
     (let [spell-cfgs (vals s)
           filtered (if print-prepared-spells?
                      (filter-prepared spell-cfgs
                                       k
                                       prepares-spells
                                       prepared-spells-by-class)
                      spell-cfgs)
           by-ability (group-by :ability filtered)]
       (reduce-kv
        (fn [am a a-s]
          (assoc-in am [a k] (sort-by (comp :name spells/spell-map :key) a-s)))
        m
        by-ability)))
   {}
   spells-known))

(defn make-pages [spells
                  print-prepared-spells
                  prepares-spells
                  prepared-spells-by-class]
  (let [page-map (make-page-map spells
                                print-prepared-spells
                                prepares-spells
                                prepared-spells-by-class)]
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

(defn make-spell-card-info [spells-known
                            save-dc-fn
                            attack-mod-fn
                            print-prepared-spells?
                            prepares-spells
                            prepared-spells-by-class]
  (let [flat-spells (char5e/flat-spells spells-known)]
    (reduce
     (fn [m {:keys [key ability qualifier class]}]
       (-> m
           (assoc-in [:spell-save-dcs class] (save-dc-fn ability))
           (assoc-in [:spell-attack-mods class] (attack-mod-fn ability))))
     {:spells-known (reduce
                     (fn [m [k v]]
                       (assoc m k (if print-prepared-spells?
                                    (filter-prepared (vals v)
                                                     k
                                                     prepares-spells
                                                     prepared-spells-by-class)
                                    (vals v))))
                     {}
                     spells-known)}
     flat-spells)))

(defn spell-page-fields [spells
                         spell-slots
                         save-dc-fn
                         attack-mod-fn
                         print-prepared-spells?
                         prepares-spells
                         prepared-spells-by-class]
  (let [spell-pages (make-pages spells
                                print-prepared-spells?
                                prepares-spells
                                prepared-spells-by-class)]
    (apply
     merge
     (make-spell-card-info spells
                           save-dc-fn
                           attack-mod-fn
                           print-prepared-spells?
                           prepares-spells
                           prepared-spells-by-class)
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

(defn spellcasting-fields [built-char print-prepared-spells?]
  (let [spells-known (char5e/spells-known built-char)
        spell-attack-modifier-fn (char5e/spell-attack-modifier-fn built-char)
        spell-save-dc-fn (char5e/spell-save-dc-fn built-char)
        spell-slots (char5e/spell-slots built-char)
        prepares-spells (char5e/prepares-spells built-char)
        prepared-spells-by-class (char5e/prepared-spells-by-class built-char)]
    
    (spell-page-fields spells-known
                       spell-slots
                       spell-save-dc-fn
                       spell-attack-modifier-fn
                       print-prepared-spells?
                       prepares-spells
                       prepared-spells-by-class)))

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
  (let [tool-profs (char5e/tool-proficiencies built-char)
        weapon-profs (char5e/weapon-proficiencies built-char)
        armor-profs (char5e/armor-proficiencies built-char)
        languages (char5e/languages built-char)]
    (s/join
     "\n\n"
     (remove
      nil?
      [(profs-paragraph (map first tool-profs) equip5e/tools-map "Tool")
       (profs-paragraph weapon-profs weapon5e/weapons-map "Weapon")
       (profs-paragraph armor-profs armor5e/armor-map "Armor")
       (profs-paragraph languages opt5e/language-map "Language")]))))

(defn damage-str [die die-count mod damage-type]
  (str (dice/dice-string die-count die mod)
       (if damage-type (str " " (name damage-type)))))

(defn sort-weapons [main-hand-weapon-kw off-hand-weapon-kw weapons]
  (sort-by
   (fn [{:keys [key]}]
     (cond
       (= main-hand-weapon-kw key) 0
       (= off-hand-weapon-kw key) 1
       :else 2))
   weapons))

(defn weapon-attack-string [{:keys [name] :as weapon} weapon-damage-modifier weapon-attack-modifier off-hand-weapon? main-hand-weapon? main-weapon-handedness]
  (str
   common/dot-char
   " "
   (or name (::mi5e/name weapon))
   ". "
   (disp5e/weapon-attack-description
    weapon
    weapon-damage-modifier
    weapon-attack-modifier
    off-hand-weapon?
    main-hand-weapon?
    main-weapon-handedness)))

(defn attacks-and-spellcasting-fields [id built-char]
  (let [weapons-map @(subscribe [::mi5e/all-weapons-map id])
        all-weapons-inventory @(subscribe [::char5e/carried-weapons id])
        main-hand-weapon-kw @(subscribe [::char5e/main-hand-weapon id])
        off-hand-weapon-kw @(subscribe [::char5e/off-hand-weapon id])
        main-weapon-handedness @(subscribe [::char5e/main-weapon-handedness id])
        weapon-details (mi5e/item-details
                        all-weapons-inventory
                        weapons-map)
        all-weapons (sort-weapons main-hand-weapon-kw off-hand-weapon-kw weapon-details)
        weapon-attack-modifier @(subscribe [::char5e/best-weapon-attack-modifier-fn id])
        weapon-damage-modifier @(subscribe [::char5e/best-weapon-damage-modifier-fn id])]
    {:attacks-and-spellcasting (s/join "\n"
                                       (concat (map
                                                attack-string
                                                (es/entity-val built-char :attacks))
                                               (mapcat
                                                (fn [{:keys [key name] :as weapon}]
                                                  (let [main-hand-weapon? (= key main-hand-weapon-kw)
                                                        off-hand-weapon? (= key off-hand-weapon-kw)
                                                        attack-str-fn #(weapon-attack-string
                                                                        weapon
                                                                        weapon-damage-modifier
                                                                        weapon-attack-modifier
                                                                        %1
                                                                        %2
                                                                        main-weapon-handedness)]
                                                    (if (and main-hand-weapon?
                                                             (= main-hand-weapon-kw off-hand-weapon-kw))
                                                      [(attack-str-fn
                                                        false
                                                        true)
                                                       (attack-str-fn
                                                        true
                                                        false)]
                                                      [(attack-str-fn
                                                        off-hand-weapon?
                                                        main-hand-weapon?)])))
                                                all-weapons)))}))

(defn speed [built-char]
  (let [speed (char5e/base-land-speed built-char)
        speed-with-armor (char5e/land-speed-with-armor built-char)
        unarmored-speed-bonus (char5e/unarmored-speed-bonus built-char)
        equipped-armor (char5e/normal-armor-inventory built-char)
        unarmored-speed (+ (or unarmored-speed-bonus 0)
                           (if speed-with-armor
                             (speed-with-armor nil)
                             speed))]
    (if (not= unarmored-speed speed)
      (str unarmored-speed "/" speed)
      speed)))


(defn make-spec [built-char
                 id
                 {:keys [print-character-sheet?
                         print-spell-cards?
                         print-prepared-spells?] :as options}]
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
        current-ac @(subscribe [::char5e/current-armor-class id])
        levels (char5e/levels built-char)
        classes (char5e/classes built-char)
        character-name (char5e/character-name built-char)
        total-hit-dice (s/join
                        " / "
                        (map
                         (fn [{:keys [class-level hit-die]}] (str class-level "d" hit-die))
                         (vals levels)))]
    (merge
     {:race (str race (if subrace (str "/" subrace)))
      :alignment (char5e/alignment built-char)
      :class-level (class-string classes levels)
      :background (char5e/background built-char)
      :prof-bonus (common/bonus-str (es/entity-val built-char :prof-bonus))
      :ac current-ac
      :hd-total total-hit-dice
      :initiative (common/bonus-str (es/entity-val built-char :initiative))
      :speed (speed built-char)
      :hp-max (es/entity-val built-char :max-hit-points)
      :passive (es/entity-val built-char :passive-perception)
      :other-profs (other-profs-field built-char)
      :personality-traits (s/join "\n\n" [(char5e/personality-trait-1 built-char) (char5e/personality-trait-2 built-char)])
      :ideals (char5e/ideals built-char)
      :bonds (char5e/bonds built-char)
      :flaws (char5e/flaws built-char)
      :backstory (char5e/description built-char)
      :character-name character-name
      :character-name-2 character-name
      :xp (char5e/xps built-char)
      :player-name (char5e/player-name built-char)
      :age (char5e/age built-char)
      :height (char5e/height built-char)
      :weight (char5e/weight built-char)
      :eyes (char5e/eyes built-char)
      :skin (char5e/skin built-char)
      :hair (char5e/hair built-char)
      :image-url (char5e/image-url built-char)
      :image-url-failed (char5e/image-url-failed built-char)
      :faction-image-url (char5e/faction-image-url built-char)
      :faction-image-url-failed (char5e/faction-image-url-failed built-char)
      :faction-name (char5e/faction-name built-char)
      :print-character-sheet? print-character-sheet?
      :print-spell-cards? print-spell-cards?}
     (attacks-and-spellcasting-fields id built-char)
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
     (spellcasting-fields built-char print-prepared-spells?))))

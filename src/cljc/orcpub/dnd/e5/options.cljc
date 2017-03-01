(ns orcpub.dnd.e5.options
  (:require [clojure.string :as s]
            [orcpub.common :as common]
            [orcpub.template :as t]
            [orcpub.entity :as entity]
            [orcpub.entity-spec :as es]
            [orcpub.modifiers :as mods]
            [orcpub.dnd.e5.character :as character]
            [orcpub.dnd.e5.modifiers :as modifiers]
            [orcpub.dnd.e5.spells :as spells]
            [orcpub.dnd.e5.spell-lists :as sl]))

(def skills [{:name "Acrobatics"
              :key :acrobatics
              :ability :dex}
             {:name "Animal Handling"
              :key :animal-handling
              :ability :wis}
             {:name "Arcana"
              :key :arcana
              :ability :int}
             {:name "Athletics"
              :key :athletics
              :ability :str}
             {:name "Deception"
              :key :deception
              :ability :cha}
             {:name "History"
              :key :history
              :ability :int}
             {:name "Insight"
              :key :insight
              :ability :wis}
             {:name "Intimidation"
              :key :intimidation
              :ability :cha}
             {:name "Investigation"
              :key :investigation
              :ability :int}
             {:name "Medicine"
              :key :medicine
              :ability :wis}
             {:name "Nature"
              :key :nature
              :ability :int}
             {:name "Perception"
              :key :perception
              :ability :wis}
             {:name "Performance"
              :key :performance
              :ability :cha}
             {:name "Persuasion"
              :key :persuasion
              :ability :cha}
             {:name "Religion"
              :key :religion
              :ability :int}
             {:name "Sleight of Hand"
              :key :sleight-of-hand
              :ability :dex}
             {:name "Stealth"
              :key :stealth
              :ability :dex}
             {:name "Survival"
              :key :survival
              :ability :wis}])

(def skills-map (common/map-by-key skills))

(def musical-instruments
  [{:key :bagpipes
    :name "Bagpipes"}
   {:key :drum
    :name "Drum"}
   {:key :dulcimer
    :name "Dulcimer"}
   {:key :flute
    :name "Flute"}
   {:key :lute
    :name "Lute"}
   {:key :lyre
    :name "Lyre"}
   {:key :horn
    :name "Horn"}
   {:key :pan-flute
    :name "Pan Flute"}
   {:key :shawm
    :name "Shawm"}
   {:key :viol
    :name "Viol"}])

(def artisans-tools
  [{:name "Alchemist's Supplies", :key :alchemists-supplies}
   {:name "Brewer's Supplies", :key :brewers-supplies}
   {:name "Calligrapher's Supplies", :key :calligraphers-supplies}
   {:name "Carpenter's Tools", :key :carpenters-tools}
   {:name "Cartographer's Tools", :key :cartographers-tools}
   {:name "Cobbler's Tools", :key :cobblers-tools}
   {:name "Cook's Utensils", :key :cooks-utensils}
   {:name "Glassblower's Tools", :key :glassblowers-tools}
   {:name "Jeweler's Tools", :key :jewelers-tools}
   {:name "Leatherworker's Tools", :key :leatherworkers-tools}
   {:name "Mason's Tools", :key :masons-tools}
   {:name "Painter's Supplies", :key :painters-supplies}
   {:name "Potter's Tools", :key :potters-tools}
   {:name "Smith's Tools", :key :smiths-tools}
   {:name "Tinker's Tools", :key :tinkers-tools}
   {:name "Weaver's Tools", :key :weavers-tools}
   {:name "Woodcarver's Tools", :key :woodcarvers-tools}])

(def misc-tools
  [{:name "Disguise Kit"
    :key :disguise-kit}
   {:name "Forgery Kit"
    :key :forgery-kit}
   {:name "Herbalism Kit"
    :key :herbalism-kit}
   {:name "Navigator's Tools"
    :key :navigators-tools}
   {:name "Poisoner's Tools"
    :key :poisoners-tools}
   {:name "Thieves' Tools"
    :key :thieves-tools}])

(def gaming-sets
  [{:name "Dice Set"
    :key :dice-set}
   {:name "Dragonchess Set"
    :key :dragonchess-set}
   {:name "Playing Card Set"
    :key :playing-card-set}
   {:name "Three-Dragon Ante Set"
    :key :three-dragon-ante-set}])

(def vehicles
  [{:name "Water Vehicles"
    :key :water-vehicles}
   {:name "Land Vehicles"
    :key :land-vehicles}])

(def tools
  (concat
   musical-instruments
   artisans-tools
   misc-tools
   gaming-sets
   vehicles))

(def tools-map
  (merge
   {:artisans-tool {:name "Artisans Tools"
                    :values artisans-tools}
    :musical-instrument {:name "Musical Instruments"
                         :values musical-instruments}
    :gaming-set {:name "Gaming Set"
                 :values gaming-sets}}
   (zipmap (map :key tools) tools)))

(def add-keys-xform
  (map
   #(assoc % :key (common/name-to-kw (:name %)))))

(def ammunition
  (into
   []
   add-keys-xform
   [{:name "Bolt"}
    {:name "Arrow"}]))

(def packs
  (into
   []
   add-keys-xform
   [{:name "Burgler's Pack"}
    {:name "Diplomat's Pack"}
    {:name "Dungeoneer's Pack"}
    {:name "Entertainer's Pack"}
    {:name "Explorer's Pack"}
    {:name "Priest's Pack"}
    {:name "Scholar's Pack"}]))

(def equipment
  (concat
   packs
   tools
   ammunition
   [{:name "Ancane Focus"
     :key :arcane-focus}
    {:name "Component Pouch"
     :key :component-pouch}
    {:name "Holy Symbol"
     :key :holy-symbol}]))

(def equipment-map
  (merge
   {:pack {:name "Equipment Packs"
           :values packs}}
   (zipmap (map :key equipment) equipment)))

(def armor
  [{:name "Shield"
    :key :shield}
   {:name "Padded",
    :type :light,
    :base-ac 11,
    :stealth-disadvantage? true,
    :weight 8,
    :key :padded}
   {:name "Leather",
    :type :light,
    :base-ac 11,
    :weight 10,
    :key :leather}
   {:name "Studded",
    :type :light,
    :base-ac 12,
    :weight 13,
    :key :studded}
   {:name "Hide",
    :type :medium,
    :base-ac 12,
    :max-dex-mod 2,
    :weight 12,
    :key :hide}
   {:name "Chain Shirt",
    :type :medium,
    :base-ac 13,
    :max-dex-mod 2,
    :weight 20,
    :key :chain-shirt}
   {:name "Scale mail",
    :type :medium,
    :base-ac 14,
    :max-dex-mod 2,
    :stealth-disadvantage? true,
    :weight 45,
    :key :scale-mail}
   {:name "Breastplate",
    :type :medium,
    :base-ac 14,
    :max-dex-mod 2,
    :weight 20,
    :key :breastplate}
   {:name "Half plate",
    :type :medium,
    :base-ac 15,
    :max-dex-mod 2,
    :stealth-disadvantage? true,
    :weight 40,
    :key :half-plate}
   {:name "Ring mail",
    :type :heavy,
    :base-ac 14,
    :max-dex-mod 0,
    :stealth-disadvantage? true,
    :weight 40,
    :key :ring-mail}
   {:name "Chain mail",
    :type :heavy,
    :base-ac 16,
    :max-dex-mod 0,
    :min-str 13,
    :stealth-disadvantage? true,
    :weight 55,
    :key :chain-mail}
   {:name "Splint",
    :type :heavy,
    :base-ac 17,
    :max-dex-mod 0,
    :min-str 15,
    :stealth-disadvantage? true,
    :weight 60,
    :key :splint}
   {:name "Plate",
    :type :heavy,
    :base-ac 18,
    :max-dex-mod 0,
    :min-str 15,
    :stealth-disadvantage? true,
    :weight 65,
    :key :plate}])

(def armor-map
  (zipmap (map :key armor) armor))

(def weapons
  [{:name "Crossbow, light",
    :damage-type :piercing,
    :damage-die 8,
    :type :simple,
    :damage-die-count 1,
    :ranged true,
    :range {:min 80, :max 320},
    :key :crossbow--light}
   {:ranged true,
    :key :dart,
    :name "Dart",
    :damage-die-count 1,
    :type :simple,
    :damage-type :piercing,
    :thrown true,
    :finesse true,
    :damage-die 4,
    :range {:min 20, :max 60}}
   {:name "Shortbow",
    :damage-type :piercing,
    :damage-die 6,
    :type :simple,
    :damage-die-count 1,
    :ranged true,
    :range {:min 80, :max 320},
    :key :shortbow}
   {:name "Sling",
    :damage-type :bludgeoning,
    :damage-die 4,
    :type :simple,
    :damage-die-count 1,
    :ranged true,
    :range {:min 30, :max 120},
    :key :sling}
   {:name "Club",
    :damage-type :bludgeoning,
    :damage-die 4,
    :damage-die-count 1,
    :type :simple,
    :melee true,
    :key :club}
   {:melee true,
    :key :dagger,
    :name "Dagger",
    :damage-die-count 1,
    :type :simple,
    :damage-type :piercing,
    :thrown true,
    :finesse true,
    :damage-die 4,
    :range {:min 20, :max 60}}
   {:name "Greatclub",
    :damage-type :bludgeoning,
    :damage-die 8,
    :damage-die-count 1,
    :type :simple,
    :melee true,
    :key :greatclub}
   {:name "Handaxe",
    :damage-type :slashing,
    :damage-die 6,
    :damage-die-count 1,
    :type :simple,
    :melee true,
    :thrown true,
    :range {:min 20, :max 60},
    :key :handaxe}
   {:name "Javelin",
    :damage-type :piercing,
    :damage-die 6,
    :damage-die-count 1,
    :type :simple,
    :melee true,
    :thrown true,
    :range {:min 30, :max 120},
    :key :javelin}
   {:name "Light hammer",
    :damage-type :bludgeoning,
    :damage-die 4,
    :damage-die-count 1,
    :type :simple,
    :melee true,
    :thrown true,
    :range {:min 20, :max 60},
    :key :light-hammer}
   {:name "Mace",
    :damage-type :bludgeoning,
    :type :simple,
    :damage-die 6,
    :damage-die-count 1,
    :melee true,
    :key :mace}
   {:name "Quarterstaff",
    :damage-type :bludgeoning,
    :type :simple,
    :damage-die 6,
    :damage-die-count 1,
    :versatile {:damage-die 8, :damage-die-count 1},
    :melee true,
    :key :quarterstaff}
   {:name "Sickle",
    :damage-type :slashing,
    :damage-die 4,
    :type :simple,
    :damage-die-count 1,
    :melee true,
    :key :sickle}
   {:melee true,
    :versatile {:damage-die 8, :damage-die-count 1},
    :key :spear,
    :name "Spear",
    :damage-die-count 1,
    :type :simple,
    :damage-type :piercing,
    :thrown true,
    :damage-die 6,
    :range {:min 20, :max 60}}
   {:name "Unarmed Strike",
    :damage-type :bludgeoning,
    :damage-die 1,
    :type :simple,
    :damage-die-count 1,
    :melee true,
    :unarmed true,
    :key :unarmed-strike}
   {:name "Battleaxe",
    :damage-type :slashing,
    :damage-die 8,
    :type :martial,
    :damage-die-count 1,
    :versatile {:damage-die 10, :damage-die-count 1},
    :melee true,
    :key :battleaxe}
   {:name "Flail",
    :damage-type :bludgeoning,
    :damage-die 8,
    :type :martial,
    :damage-die-count 1,
    :melee true,
    :key :flail}
   {:name "Glaive",
    :damage-type :slashing,
    :damage-die 10,
    :type :martial,
    :damage-die-count 1,
    :melee true,
    :heavy true,
    :reach true,
    :key :glaive}
   {:name "Greataxe",
    :damage-type :slashing,
    :damage-die 12,
    :type :martial,
    :damage-die-count 1,
    :heavy true,
    :melee true,
    :key :greataxe}
   {:name "Greatsword",
    :damage-type :slashing,
    :damage-die 6,
    :type :martial,
    :damage-die-count 2,
    :heavy true,
    :melee true,
    :key :greatsword}
   {:name "Halberd",
    :damage-type :slashing,
    :damage-die 10,
    :type :martial,
    :damage-die-count 1,
    :melee true,
    :heavy true,
    :reach true,
    :key :halberd}
   {:name "Lance",
    :damage-type :piercing,
    :damage-die 12,
    :type :martial,
    :damage-die-count 1,
    :melee true,
    :reach true,
    :key :lance}
   {:name "Longsword",
    :damage-type :slashing,
    :damage-die 8,
    :type :martial,
    :damage-die-count 1,
    :versatile {:damage-die 10, :damage-die-count 1},
    :melee true,
    :key :longsword}
   {:name "Maul",
    :damage-type :bludgeoning,
    :damage-die 6,
    :type :martial,
    :damage-die-count 2,
    :heavy true,
    :melee true,
    :key :maul}
   {:name "Morningstar",
    :damage-type :piercing,
    :damage-die 8,
    :type :martial,
    :damage-die-count 1,
    :melee true,
    :key :morningstar}
   {:name "Pike",
    :damage-type :piercing,
    :damage-die 10,
    :type :martial,
    :damage-die-count 1,
    :melee true,
    :heavy true,
    :reach true,
    :key :pike}
   {:name "Rapier",
    :damage-type :piercing,
    :damage-die 8,
    :type :martial,
    :damage-die-count 1,
    :finesse true,
    :melee true,
    :key :rapier}
   {:name "Scimitar",
    :damage-type :slashing,
    :damage-die 6,
    :type :martial,
    :damage-die-count 1,
    :finesse true,
    :melee true,
    :key :scimitar}
   {:name "Shortsword",
    :damage-type :piercing,
    :damage-die 6,
    :type :martial,
    :finesse true,
    :damage-die-count 1,
    :melee true,
    :key :shortsword}
   {:melee true,
    :versatile {:damage-die 8, :damage-die-count 1},
    :key :trident,
    :name "Trident",
    :damage-die-count 1,
    :type :martial,
    :damage-type :piercing,
    :thrown true,
    :damage-die 6,
    :range {:min 20, :max 60}}
   {:name "War pick",
    :damage-type :piercing,
    :damage-die 8,
    :type :martial,
    :damage-die-count 1,
    :melee true,
    :key :war-pick}
   {:name "Warhammer",
    :damage-type :bludgeoning,
    :damage-die 8,
    :type :martial,
    :damage-die-count 1,
    :versatile {:damage-die 10, :damage-die-count 1},
    :melee true,
    :key :warhammer}
   {:name "Whip",
    :damage-type :slashing,
    :damage-die 4,
    :type :martial,
    :damage-die-count 1,
    :melee true,
    :finesse true,
    :reach true,
    :key :whip}
   {:name "Blowgun",
    :damage-type :piercing,
    :damage-die 1,
    :type :martial,
    :damage-die-count 1,
    :ranged true,
    :range {:min 25, :max 100},
    :key :blowgun}
   {:name "Crossbow, hand",
    :damage-type :piercing,
    :damage-die 6,
    :type :martial,
    :damage-die-count 1,
    :ranged true,
    :range {:min 30, :max 120},
    :key :crossbow--hand}
   {:name "Crossbow, heavy",
    :damage-type :piercing,
    :damage-die 10,
    :type :martial,
    :damage-die-count 1,
    :ranged true,
    :heavy true,
    :range {:min 100, :max 400},
    :key :crossbow--heavy}
   {:name "Longbow",
    :damage-type :piercing,
    :damage-die 8,
    :type :martial,
    :damage-die-count 1,
    :ranged true,
    :heavy true,
    :range {:min 150, :max 600},
    :key :longbow}
   {:name "Net",
    :type :martial,
    :ranged true,
    :thrown true,
    :range {:min 5, :max 15},
    :key :net}])

(def weapons-map
  (zipmap (map :key weapons) weapons))

(defn weapons-of-type [weapons type]
  (filter #(= type (:type %)) weapons))

(defn martial-weapons [weapons]
  (weapons-of-type weapons :martial))

(defn simple-weapons [weapons]
  (weapons-of-type weapons :simple))

(def skill-abilities
  (into {} (map (juxt :key :ability)) skills))

(defn skill-option [skill]
  (t/option
   (:name skill)
   (:key skill)
   nil
   [(modifiers/skill-proficiency (:key skill))]))

(defn weapon-proficiency-option [{:keys [name key]}]
  (t/option
   name
   key
   nil
   [(modifiers/weapon-proficiency name key)]))

(defn tool-option [tool]
  (t/option
   (:name tool)
   (:key tool)
   nil
   [(modifiers/tool-proficiency (:name tool) (:key tool))]))

(defn weapon-option [weapon & [num]]
  (t/option
   (:name weapon)
   (:key weapon)
   nil
   [(modifiers/weapon (:key weapon) (or num 1))]))

(defn weapon-options [weapons & [num]]
  (map
   #(weapon-option % num)
   weapons))

(defn simple-melee-weapon-options [num]
  (weapon-options
   (filter
    #(and (= :simple (:type %)) (:melee %))
    weapons)
   num))

(defn martial-weapon-options [num]
  (weapon-options
   (filter
    #(= :martial (:type %))
    weapons)
   num))

(defn skill-options [skills]
  (map
   skill-option
   skills))

(defn weapon-proficiency-options [weapons]
  (map
   weapon-proficiency-option
   weapons))

(defn tool-options [tools]
  (map
   tool-option
   tools))

(defn ability-increase-selection [abilities num & [different?]]
  (assoc
   (t/selection
    "Ability Score Increase"
    (into
     []
     (map
      (fn [ability]
        (t/option
         (s/upper-case (name ability))
         ability
         []
         [(modifiers/ability ability 1)])))
     abilities)
    num
    num)
   ::t/simple? (> num 1)))

(defn min-ability [ability-kw min-value]
  (fn [c] (>= (ability-kw (es/entity-val c :abilities)) min-value)))

(defn ability-prereq [ability-kw min-value]
  {::t/label (str (s/upper-case (name ability-kw)) " " min-value " or higher")
   ::t/prereq-fn (min-ability ability-kw min-value)})

(defn prereq [label prereq-fn]
  {::t/label label
   ::t/prereq-fn prereq-fn})

(defn armor-prereq [armor-kw]
  (prereq (str "proficiency with " (name armor-kw) " armor")
             (fn [c] (let [prof-keys (set (map :key (:armor-profs c)))]
                       (boolean (prof-keys armor-kw))))))

(def languages
  [{:name "Common"
    :key :common}
   {:name "Dwarvish"
    :key :dwarvish}
   {:name "Elvish"
    :key :elvish}
   {:name "Giant"
    :key :giant}
   {:name "Gnomish"
    :key :gnomish}
   {:name "Goblin"
    :key :goblin}
   {:name "Halfling"
    :key :halfling}
   {:name "Orc"
    :key :orc}
   {:name "Abyssal"
    :key :abyssal}
   {:name "Celestial"
    :key :celestial}
   {:name "Draconic"
    :key :draconic}
   {:name "Deep Speech"
    :key :deep-speech}
   {:name "Infernal"
    :key :infernal}
   {:name "Primordial"
    :key :primordial}
   {:name "Sylvan"
    :key :sylval}
   {:name "Undercommon"
    :key :undercommon}])

(def language-map
  (zipmap (map :key languages) languages))

(def elemental-disciplines
  [{:name "Breath of Winter"
    :level 17}
   {:name "Clench of the North Wind"
    :level 6}
   {:name "Eternal Mountain Defense"
    :level 17}
   {:name "Fangs of the Fire Snake"}
   {:name "Fist of Four Thunders"}
   {:name "Fist of Unbroken Air"}
   {:name "Flames of the Phoenix"
    :level 11}
   {:name "Gong of the Summit"
    :level 6}
   {:name "Mist Stance"
    :level 11}
   {:name "Ride the Wind"
    :level 11}
   {:name "River of Hungry Flame"
    :level 17}
   {:name "Rush of the Gale Spirits"}
   {:name "Shape of the Flowing River"}
   {:name "Sweeping Cinder Strike"}
   {:name "Water Whip"}
   {:name "Wave of Rolling Earth"
    :level 17}])

(defn monk-elemental-disciplines []
  (t/selection
   "Elemental Disciplines"
   (mapv
    (fn [{:keys [name level]}]
      (t/option
       name
       (common/name-to-kw name)
       []
       [(modifiers/trait (str "Elemental Discipline: " name))]
       (if level [(fn [c] (>= (es/entity-val c :total-levels) level))])))
    elemental-disciplines)))

(defn language-option [{:keys [name key]}]
  (t/option
   name
   key
   nil
   [(modifiers/language name key)]))

(defn key-to-name [key]
  (s/join " " (map s/capitalize (s/split (name key) #"-"))))

(defn spell-options [spells level spellcasting-ability]
  (map
   (fn [key]
     (t/option
      (:name (spells/spell-map key))
      key
      []
      [(modifiers/spells-known level key spellcasting-ability)]))
   (sort spells)))

(defn spell-level-title [level]
  (if (zero? level) "Cantrip" (str "Level " level " Spell")))

(defn spell-selection
  ([class-key level spellcasting-ability num]
   (spell-selection class-key level (get-in sl/spell-lists [class-key level]) spellcasting-ability num))
  ([class-key level spell-keys spellcasting-ability num]
   (t/selection
    (spell-level-title level)
    (spell-options spell-keys level spellcasting-ability)
    num
    num)))

(defn spell-slot-schedule [level-factor]
  (case level-factor
    1 {1 {1 2}
       2 {1 1}
       3 {1 1
          2 2}
       4 {2 1}
       5 {3 2}
       6 {3 1}
       7 {4 1}
       8 {4 1}
       9 {4 1
          5 1}
       10 {5 1}
       11 {6 1}
       13 {7 1}
       15 {8 1}
       17 {9 1}
       18 {5 1}
       19 {6 1}
       20 {7 1}}
    2 {2 {1 2}
       3 {1 1}
       5 {1 1
          2 2}
       7 {2 1}
       9 {3 1}
       11 {3 1}
       13 {4 1}
       15 {4 1}
       17 {4 1
           5 1}
       19 {5 1}}
    3 {3 {1 2}
       4 {1 1}
       7 {1 1
          2 2}
       10 {2 1}
       13 {3 2}
       16 {3 1}
       19 {4 1}}))

(defn total-slots [level level-factor]
  (let [schedule (spell-slot-schedule level-factor)]
    (reduce
     (fn [m lvl]
       (merge-with + m (schedule lvl)))
     {}
     (range 1 (inc level)))))

(defn raw-bard-magical-secrets [level]
  (let [spell-slots (total-slots level 1)]
    (vec
     (for [i (range 2)]
       (t/selection
        (str "Magical Secrets " (inc i))
        (map
         (fn [[lvl _]]
           (t/option
            (spell-level-title lvl)
            (keyword (str lvl))
            [(spell-selection
              :bard
              lvl
              (reduce-kv
               (fn [s _ lvls]
                 (clojure.set/union s (lvls lvl)))
               #{}
               sl/spell-lists)
              :cha
              1)]
            []))
         spell-slots))))))

(defn bard-magical-secrets [min-level]
  (map
   (fn [s] (assoc s ::t/prereq-fn
                  (fn [built-char]
                    (let [bard-levels (-> (es/entity-val built-char :levels) :bard :class-level)]
                      (>= bard-levels min-level)))))
   (raw-bard-magical-secrets min-level)))

(defn cantrip-selections [class-key ability cantrips-known]
  (reduce
   (fn [m [k v]]
     (assoc m k [(spell-selection class-key 0 ability v)]))
   {}
   cantrips-known))

(defn apply-spell-restriction [spell-keys restriction]
  (if restriction
    (filter
     (fn [spell-key]
       (restriction (spells/spell-map spell-key)))
     spell-keys)
    spell-keys))

(defn spells-known-selections [{:keys [class-key
                                       level-factor
                                       spells-known
                                       known-mode
                                       spell-list
                                       spells
                                       ability] :as cfg}]
  (reduce
   (fn [m [cls-lvl v]]
     (let [[num restriction] (if (number? v) [v] ((juxt :num :restriction) v))
           slots (total-slots cls-lvl level-factor)
           all-spells (select-keys
                       (or spells (sl/spell-lists (or spell-list class-key)))
                       (keys slots))
           acquire? (= :acquire known-mode)]
       (assoc m cls-lvl
              [(t/selection
                "Spells Known"
                (vec
                 (flatten
                  (map
                   (fn [[lvl spell-keys]]
                     (map
                      (fn [spell-key]
                        (let [spell (spells/spell-map spell-key)]
                          (t/option
                           (str lvl " - " (:name spell))
                           spell-key
                           []
                           [(modifiers/spells-known lvl spell-key ability)])))
                      (apply-spell-restriction spell-keys restriction)))
                   all-spells)))
                num
                (if (not acquire?) num)
                acquire?
                (if acquire?
                  (fn [s o v] {::entity/key nil})))])))
   {}
   spells-known))

(defn spellcasting-template [{:keys [class-key
                                     level-factor
                                     cantrips-known
                                     spells-known
                                     known-mode
                                     ability] :as cfg}]
  (let [spell-selections (spells-known-selections cfg)
        cantrip-selections (cantrip-selections class-key ability cantrips-known)]
    {:selections (merge-with
                  concat
                  cantrip-selections
                  spell-selections)}))

(defn magic-initiate-option [class-key spellcasting-ability spell-lists]
  (t/option
   (name class-key)
   class-key
   [(t/selection
     "Cantrip"
     (spell-options (get-in spell-lists [class-key 0]) 0 spellcasting-ability)
     2 2)
    (t/selection
     "1st Level Spell"
     (spell-options (get-in spell-lists [class-key 1]) 1 spellcasting-ability)
     1 1)]
   []))

(defn ritual-caster-option [class-key spellcasting-ability spell-lists]
  (t/option
   (name class-key)
   class-key
   [(t/selection
     "1st Level Ritual"
     (spell-options (filter (fn [spell-kw] (:ritual (spells/spell-map spell-kw))) (get-in spell-lists [class-key 1])) 1 spellcasting-ability)
     2
     2)]
   []))

(defn language-selection [langs num]
  (t/selection
   "Languages"
   (map
    (fn [lang]
      (language-option lang))
    langs)
   num
   num))

(defn maneuver-option [name]
  (t/option
   name
   (common/name-to-kw name)
   nil
   [(modifiers/trait (str name " Maneuver"))]))

(defn proficiency-help [num singular plural]
  (str "Select additional " (if (> num 1) plural singular) " for which you are proficient."))

(defn skill-selection
  ([num]
   (skill-selection (map :key skills) num))
  ([options num]
   (t/selection-cfg
    {:name "Skill Proficiency"
     :help (proficiency-help num "a skill" "skills")
     :options (skill-options
      (filter
       (comp (set options) :key)
       skills))
     :min num
     :max num})))

(defn tool-selection
  ([num]
   (t/selection-cfg
    {:name "Tool Proficiency"
     :help (proficiency-help num "a tool" "tools")
     :options (tool-options tools)
     :min num
     :max num}))
  ([options num]
   (t/selection-cfg
    {:name "Tool Proficiency"
     :help (proficiency-help num "a tool" "tools")
     :options (tool-options
               (filter
                (comp (set options) :key)
                tools))
     :min num
     :max num})))

(defn weapon-proficiency-selection
  ([num]
   (t/selection-cfg
    {:name "Weapon Proficiency"
     :help (proficiency-help num "a weapon" "weapons")
     :options (weapon-proficiency-options weapons)
     :min num
     :max num}))
  ([options num]
   (t/selection-cfg
    {:name "Tool Proficiency"
     :help (proficiency-help num "a weapon" "weapons")
     :options (weapon-proficiency-options
               (filter
                (comp (set options) :key)
                tools))
     :min num
     :max num})))

(defn skilled-selection [title]
  (t/selection
   title
   [(t/select-option
     "Skill"
     [(skill-selection 1)])
    (t/select-option
     "Tool"
     [(tool-selection 1)])]))

(def maneuver-options
  [(maneuver-option "Commander's Strike")
   (maneuver-option "Disarming Attack")
   (maneuver-option "Distracting Strike")
   (maneuver-option "Evasive Footwork")
   (maneuver-option "Feinting Attack")
   (maneuver-option "Goading Attack")
   (maneuver-option "Lunging Attack")
   (maneuver-option "Manuevering Attack")
   (maneuver-option "Menacing Attack")
   (maneuver-option "Parry")
   (maneuver-option "Precision Attack")
   (maneuver-option "Pushing Attack")
   (maneuver-option "Rally")
   (maneuver-option "Riposte")
   (maneuver-option "Sweeping Attack")
   (maneuver-option "Trip Attack")])

(def can-cast-spell-prereq
  {::t/label "spellcasting ability"
   ::t/prereq-fn (fn [c] (some (fn [[k v]] (seq v)) (:spells-known c)))})

(def feat-options
  [(t/option
    "Alert"
    :alert
    nil
    [(modifiers/initiative 5)
     (modifiers/trait "Alert Feat")])
   (t/option
    "Athlete"
    :athlete
    [(ability-increase-selection [:str :dex] 1 false)]
    [(modifiers/trait "Athlete Feat")])
   (t/option
    "Actor"
    :actor
    []
    [(modifiers/ability :cha 1)
     (modifiers/trait "Actor Feat")])
   (t/option
    "Charger"
    :charger
    []
    [(modifiers/trait "Charger Feat")])
   (t/option
    "Crossbow Expert"
    :crossbow-expert
    []
    [(modifiers/trait "Crossbow Expert Feat")])
   (t/option
    "Defensive Duelist"
    :defensive-duelist
    []
    [(modifiers/trait "Defensive Duelist Feat")]
    [(ability-prereq :dex 13)])
   (t/option
    "Dual Wielder"
    :dual-wielder
    []
    [(modifiers/trait "Dual Wielder Feat")])
   (t/option
    "Dungeon Delver"
    :dungeon-delver
    []
    [(modifiers/trait "Dungeon Delver Feat")
     (modifiers/resistance :trap)])
   (t/option
    "Durable"
    :durable
    []
    [(modifiers/trait "Durable Feat")
     (modifiers/ability :con 1)])
   (t/option
    "Elemental Adept"
    :elemental-adept
    []
    [(modifiers/trait "Elemental Adept Feat")]
    [can-cast-spell-prereq])
   (t/option
    "Grappler"
    :grappler
    []
    [(modifiers/trait "Grappler Feat")]
    [(ability-prereq :str 13)])
   (t/option
    "Great Weapon Master"
    :great-weapon-master
    []
    [(modifiers/trait "Great Weapon Master Feat")])
   (t/option
    "Healer"
    :healer
    []
    [(modifiers/trait "Healer Feat")
     (modifiers/action "Healer Feat Action")])
   (t/option
    "Heavily Armored"
    :heavily-armored
    []
    [(modifiers/heavy-armor-proficiency)
     (modifiers/ability :str 1)]
    [(armor-prereq :medium)])
   (t/option
    "Heavy Armor Master"
    :heavy-armor-master
    []
    [(modifiers/ability :str 1)
     (modifiers/trait "Heavy Armor Master Feat")]
    [(armor-prereq :heavy)])
   (t/option
    "Inspiring Leader"
    :inspiring-leader
    []
    [(modifiers/trait "Inspiring Leader Feat")]
    [(ability-prereq :cha 13)])
   (t/option
    "Keen Mind"
    :keen-mind
    []
    [(modifiers/ability :int 1)
     (modifiers/trait "Keen Mind Feat")])
   (t/option
    "Lightly Armored"
    :lightly-armored
    [(ability-increase-selection [:str :dex] 1 false)]
    [(modifiers/light-armor-proficiency)])
   (t/option
    "Linguist"
    :linguist
    [(language-selection languages 3)]
    [(modifiers/ability :int 1)])
   (t/option
    "Lucky"
    :lucky
    nil
    [(modifiers/trait "Lucky Feat")])
   (t/option
    "Mage Slayer"
    :mage-slayer
    nil
    [(modifiers/trait "Mage Slayer Feat")])
   (t/option
    "Magic Initiate"
    :magic-initiate
    [(t/selection
      "Spell Class"
      [(magic-initiate-option :bard :cha sl/spell-lists)
       (magic-initiate-option :cleric :wis sl/spell-lists)
       (magic-initiate-option :druid :wis sl/spell-lists)
       (magic-initiate-option :sorcerer :cha sl/spell-lists)
       (magic-initiate-option :warlock :cha sl/spell-lists)
       (magic-initiate-option :wizard :int sl/spell-lists)])]
    [])
   (t/option
    "Martial Adept"
    :martial-adept
    [(t/selection
      "Martial Maneuvers"
      maneuver-options
      2 2)]
    [(modifiers/trait "Martial Adept Feat")])
   (t/option
    "Medium Armor Master"
    :medium-armor-master
    []
    [(modifiers/trait "Medium Armor Master Feat")
     (mods/modifier ?max-medium-armor-bonus 3)
     (mods/fn-mod ?armor-stealth-disadvantage?
                  (fn [armor]
                    (if (= :medium (:type armor))
                      false
                      (?armor-stealth-disadvantage? armor))))]
    [(armor-prereq :medium)])
   (t/option
    "Mobile"
    :mobile
    []
    [(modifiers/speed 10)
     (modifiers/trait "Mobile Feat")])
   (t/option
    "Moderately Armored"
    :moderately-armored
    [(ability-increase-selection [:str :dex] 1 false)]
    [(modifiers/medium-armor-proficiency)
     (modifiers/shield-armor-proficiency)]
    [(armor-prereq :light)])
   (t/option
    "Mounted Combatant"
    :mounted-combatant
    []
    [(modifiers/trait "Mounted Combatant Feat")])
   (t/option
    "Observant"
    :observant
    [(ability-increase-selection [:int :wis] 1 false)]
    [(modifiers/trait "Observant Feat")
     (modifiers/passive-perception 5)
     (modifiers/passive-investigation 5)])
   (t/option
    "Polearm Master"
    :polearm-master
    []
    [(modifiers/trait "Polearm Master Feat")])
   (t/option
    "Resilient"
    :resilient
    [(t/selection
      "Ability"
      (map
       (fn [ability-key]
         (t/option
          (s/upper-case (name ability-key))
          ability-key
          []
          [(modifiers/ability ability-key 1)
           (modifiers/saving-throws ability-key)]))
       character/ability-keys))]
    [])
   (t/select-option
    "Ritual Caster"
    [(t/selection
      "Spell Class"
      [(ritual-caster-option :bard :cha sl/spell-lists)
       (ritual-caster-option :cleric :wis sl/spell-lists)
       (ritual-caster-option :druid :wis sl/spell-lists)
       (ritual-caster-option :sorcerer :cha sl/spell-lists)
       (ritual-caster-option :warlock :cha sl/spell-lists)
       (ritual-caster-option :wizard :int sl/spell-lists)])]
    [{::t/label "Intelligence or Wisdom 13 or higher"
      ::t/prereq-fn (fn [{{:keys [wis int]} :abilities}]
                      (or (>= wis 13)
                          (>= int 13)))}])
   (t/option
    "Savage Attacker"
    :savage-attacker
    []
    [(modifiers/trait "Savage Attacker Feat")])
   (t/option
    "Sentinal"
    :sentinal
    []
    [(modifiers/trait "Sentinal Feat")])
   (t/mod-option
    "Sharpshooter"
    [(modifiers/trait "Sharpshooter Feat")])
   (t/mod-option
    "Shield Master"
    [(modifiers/trait "Shield Master Feat")])
   (t/select-option
    "Skilled"
    [(skilled-selection "Skill/Tool 1")
     (skilled-selection "Skill/Tool 2")
     (skilled-selection "Skill/tool 3")])
   (t/mod-option
    "Skulker"
    [(modifiers/trait "Skulker Feat")]
    [(ability-prereq :dex 13)])
   (t/mod-option
    "Spell Sniper"
    [(modifiers/trait "Spell Sniper Feat")]
    [can-cast-spell-prereq])
   (t/option
    "Tavern Brawler"
    :tavern-brawler
    [(ability-increase-selection [:str :dex] 1 false)]
    [(modifiers/weapon-proficiency "Improvised Weapons" :improvised)
     (modifiers/trait "Tavern Brawler Feat")])
   (t/mod-option
    "Tough"
    [(mods/modifier ?hit-point-level-bonus (+ 2 ?hit-point-level-bonus))])
   (t/mod-option
    "War Caster"
    [(modifiers/trait "War Caster Feat")])
   (t/select-option
    "Weapon Master"
    [(ability-increase-selection [:str :dex] 1 false)
     (weapon-proficiency-selection 4)])])


(def fighting-style-options
  [(t/option
    "Archery"
    :archery
    []
    [(modifiers/ranged-attack-bonus 2)
     (modifiers/trait "Archery Fighting Style" "You gain a +2 bonus to attack rolls you make with ranged weapons.")])
   (t/option
    "Defense"
    :defense
    []
    [(modifiers/armored-ac-bonus 1)
     (modifiers/trait "Defense Fighting Style" "While you are wearing armor, you gain a +1 bonus to AC.")])
   (t/option
    "Dueling"
    :dueling
    []
    [(modifiers/trait "Dueling Fighting Style" "When you are wielding a melee weapon in one hand and no other weapons, you gain a +2 bonus to damage rolls with that weapon.")])
   (t/option
    "Great Weapon Fighting"
    :great-weapon-fighting
    []
    [(modifiers/trait "Great Weapon Fighting Style" "When you roll a 1 or 2 on a damage die for an attack you make with a melee weapon that you are wielding with two hands, you can reroll the die and must use the new roll, even if the new roll is a 1 or a 2. The weapon must have the two-handed or versatile property for you to gain this benefit.")])
   (t/option
    "Protection"
    :protection
    []
    [(modifiers/trait "Protection Fighting Style" "When a creature you can see attacks a target other than you that is within 5 feet of you, you can use your reaction to impose disadvantage on the attack roll. You must be wielding a shield.")])
   (t/option
    "Two Weapon Fighting"
    :two-weapon-fighting
    []
    [(modifiers/trait "Two Weapon Fighting" "When you engage in two-weapon fighting, you can add your ability modifier to the damage of the second attack.")])])

(defn fighting-style-selection [character-ref & [restrictions]]
  (t/selection
   "Fighting Style"
   (if restrictions
     (filter
      (fn [o]
        (restrictions (::t/key o)))
      fighting-style-options)
     fighting-style-options)))

(defn feat-selection [num]
  (t/selection
   (if (= 1 num) "Feat" "Feats")
   feat-options
   num
   num))

(defn ability-score-improvement-selection []
  (t/selection
   "Ability Score Improvement/Feat"
   [(t/option
     "Ability Score Improvement"
     :ability-score-improvement
     [(ability-increase-selection character/ability-keys 2 false)]
     [])
    (t/option
     "Feat"
     :feat
     [(t/selection
       "Feat"
       feat-options)]
     [])]))

(defn expertise-selection [num]
  (t/selection
   "Skill Expertise"
   (mapv
    (fn [skill]
      (assoc
       (t/option
        (:name skill)
        (:key skill)
        nil
        [(modifiers/skill-expertise (:key skill))])
       ::t/prereq-fn
       (fn [built-char]
         (let [skill-profs (es/entity-val built-char :skill-profs)]
           (and skill-profs (skill-profs (:key skill)))))))
    skills)
   num
   num))

(def rogue-expertise-selection
  (t/selection
   "Expertise"
   [(t/option
     "Two Skills"
     :two-skills
     [(expertise-selection 2)]
     [])
    (t/option
     "One Skill/Theives Tools"
     :one-skill-thieves-tools
     [(expertise-selection 1)]
     [(modifiers/tool-proficiency "Thieves Tools" :thieves-tools)])]))

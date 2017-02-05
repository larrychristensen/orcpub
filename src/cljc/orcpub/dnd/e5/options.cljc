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
  [{:name "smith's tools"
     :key :smiths-tools}
    {:name "brewer's supplies"
     :key :brewers-supplies}
    {:name "mason's tools"
     :key :masons-tools}])

(def tools
  (concat
   musical-instruments
   artisans-tools))

(def tools-map
  (merge
   {:artisans-tool {:name "Artisans Tools"
                    :values artisans-tools}
    :musical-instrument {:name "Musical Instruments"
                         :values musical-instruments}}
   (zipmap (map :key tools) tools)))

(def add-keys-xform
  (map
   #(assoc % :key (common/name-to-kw (:name %)))))

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
   tools))

(def equipment-map
  (merge
   {:pack {:name "Equipment Packs"
           :values packs}}
   tools-map
   (zipmap (map :key packs) packs)))

(def armor
  [{:name "Padded",
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

(defn skill-options [skills]
  (map
   skill-option
   skills))

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

(defn language-option [{:keys [name key]}]
  (t/option
   name
   key
   nil
   [(modifiers/language name key)]))

(def wizard-cantrips
  [:acid-splash :blade-ward :light :true-strike])

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
   spells))

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
                                       ability] :as cfg}]
  (case known-mode
    :schedule (reduce
               (fn [m [k v]]
                 (let [[cls-lvl restriction] (if (number? v) [v] ((juxt :num :restriction) v))
                       slots (total-slots cls-lvl level-factor)]
                   (assoc m k (if (> (count slots) 1)
                                [(t/selection
                                  "Spell"
                                  (mapv
                                   (fn [[lvl _]]
                                     (let [spell-keys (get-in sl/spell-lists [class-key lvl])
                                           final-spell-keys (apply-spell-restriction spell-keys restriction)]
                                       (t/option
                                        (spell-level-title lvl)
                                        (keyword (str lvl))
                                        [(spell-selection class-key lvl final-spell-keys ability cls-lvl)]
                                        [])))
                                   slots))]
                                [(spell-selection class-key 1 (apply-spell-restriction (get-in sl/spell-lists [class-key 1]) restriction) ability cls-lvl)]))))
               {}
               spells-known)
    {}))

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

(def wizard-cantrip-options
  (map
   (fn [key]
     {::t/key key
      ::t/name (key-to-name key)
      ::t/modifiers [(modifiers/spells-known 0 key :int)]})
   wizard-cantrips))

(defn wizard-cantrip-selection [num]
  (t/selection "Cantrips Known" wizard-cantrip-options num num))

(def wizard-spells-1
  [:mage-armor :magic-missile :magic-mouth :shield])

(def wizard-spell-options-1
  (map
   (fn [key]
     {::t/key key
      ::t/name (key-to-name key)
      ::t/modifiers [(modifiers/spells-known 1 key :int)]})
   wizard-spells-1))

(defn wizard-spell-selection-1 []
  (assoc (t/selection*
          "1st Level Spells Known"
          (fn [selection spells-known]
            {::entity/key :shield})
          wizard-spell-options-1)
         ::t/key
         :spells-known))

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
    [{::t/label "spellcasting ability"
      ::t/prereq-fn (fn [c] (some (fn [[k v]] (seq v)) (:spells-known c)))}])
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
    [])])

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

(defn skill-selection
  ([num]
   (skill-selection (map :key skills) num))
  ([options num]
   (t/selection
    "Skill Proficiency"
    (skill-options
     (filter
      (comp (set options) :key)
      skills))
    num
    num)))

(defn tool-selection [options num]
  (t/selection
   "Tool Proficiency"
   (tool-options
    (filter
     (comp (set options) :key)
     tools))
   num
   num))

(def expertise-selection
  (t/selection
   "Skill Expertise"
   (map
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
   2
   2))

(def rogue-expertise-selection
  (t/selection
   "Expertise"
   [(t/option
     "Two Skills"
     :two-skills
     [expertise-selection]
     [])
    (t/option
     "One Skill/Theives Tools"
     :one-skill-thieves-tools
     [(t/selection
       "Skills"
       [(t/option
         "Athletics"
         :athletics
         nil
         [(modifiers/skill-expertise :athletics)])
        (t/option
         "Acrobatics"
         :acrobatics
         nil
         [(modifiers/skill-expertise :acrobatics)])])]
     [(modifiers/tool-proficiency "Thieves Tools" :thieves-tools)])]))

(ns orcpub.dnd.e5.spell-subs
  (:require [re-frame.core :refer [reg-sub reg-sub-raw dispatch subscribe]]
            [orcpub.common :as common]
            [orcpub.template :as t]
            [orcpub.modifiers :as mod]
            [orcpub.dnd.e5 :as e5]
            [orcpub.dnd.e5.backgrounds :as bg5e]
            [orcpub.dnd.e5.races :as races5e]
            [orcpub.dnd.e5.feats :as feats5e]
            [orcpub.dnd.e5.modifiers :as mod5e]
            [orcpub.dnd.e5.magic-items :as mi5e]
            [orcpub.dnd.e5.character :as char5e]
            [orcpub.dnd.e5.weapons :as weapon5e]
            [orcpub.dnd.e5.spells :as spells5e]
            [orcpub.dnd.e5.spell-lists :as sl5e]
            [orcpub.dnd.e5.armor :as armor5e]
            [orcpub.dnd.e5.template :as t5e]
            [orcpub.dnd.e5.equipment :as equipment5e]
            [orcpub.dnd.e5.options :as opt5e]
            [orcpub.route-map :as routes]
            [orcpub.dnd.e5.events :as events]
            [reagent.ratom :as ra]
            [clojure.string :as s]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(reg-sub
 ::e5/plugins
 (fn [db _]
   (get db :plugins)))

(reg-sub
 ::e5/plugin-vals
 :<- [::e5/plugins]
 (fn [plugins]
   (vals plugins)))

(reg-sub
 ::bg5e/plugin-backgrounds
 :<- [::e5/plugin-vals]
 (fn [plugins _]
   (apply concat (map (comp vals ::e5/backgrounds) plugins))))

(reg-sub
 ::races5e/plugin-races
 :<- [::e5/plugin-vals]
 (fn [plugins _]
   (apply concat (map (comp vals ::e5/races) plugins))))

(reg-sub
 ::races5e/plugin-subraces
 :<- [::e5/plugin-vals]
 (fn [plugins _]
   (map
    (fn [subrace]
      (assoc subrace :modifiers (opt5e/plugin-modifiers (:props subrace))))
    (apply concat (map (comp vals ::e5/subraces) plugins)))))

(reg-sub
 ::feats5e/plugin-feats
 :<- [::e5/plugin-vals]
 (fn [plugins _]
   (apply concat (map (comp vals ::e5/feats) plugins))))

(def acolyte-bg
  {:name "Acolyte"
   :help "Your life has been devoted to serving a god or gods."
   :profs {:skill {:insight true, :religion true}
           :language-options {:choose 2 :options {:any true}}}
   :equipment {:clothes-common 1
               :pouch 1
               :incense 5
               :vestements 1}
   :selections [(opt5e/new-starting-equipment-selection
                 nil
                 {:name "Holy Symbol"
                  :options (map
                            #(opt5e/starting-equipment-option % 1)
                            equipment5e/holy-symbols)})
                ]
   :equipment-choices [{:name "Prayer Book/Wheel"
                        :options {:prayer-book 1
                                  :prayer-wheel 1}}]
   :treasure {:gp 15}
   :traits [{:name "Shelter the Faithful"
             :page 127
             :summary "You and your companions can expect free healing at an establishment of your faith."}]})

(reg-sub
 ::bg5e/backgrounds
 :<- [::bg5e/plugin-backgrounds]
 (fn [plugin-backgrounds]
   (cons
    acolyte-bg
    plugin-backgrounds)))

(def elf-weapon-training-mods
  (opt5e/weapon-prof-modifiers [:longsword :shortsword :shortbow :longbow]))

(defn sunlight-sensitivity [page & [source]]
  {:name "Sunlight Sensitivity"
   :summary "Disadvantage on attack and perception rolls in direct sunlight"
   :source (or source :phb)
   :page 24})

(def mask-of-the-wild-mod
  (mod5e/trait-cfg
   {:name "Mask of the Wild"
    :page 24
    :summary "Hide when lightly obscured by natural phenomena."}))

(defn high-elf-cantrip-selection [spell-lists spells-map]
  (opt5e/spell-selection
   spell-lists
   spells-map
   {:class-key :wizard
    :level 0
    :exclude-ref? true
    :spellcasting-ability ::char5e/int
    :class-name "High Elf"
    :num 1}))

#_(def drow-magic-mods
  [(mod5e/spells-known 0 :dancing-lights ::char5e/cha "Dark Elf")
   (mod5e/spells-known 1 :faerie-fire ::char5e/cha "Dark Elf" 3)
   (mod5e/spells-known 2 :darkness ::char5e/cha "Dark Elf" 5)])

(defn elf-option-cfg [spell-lists spells-map]
  {:name "Elf"
   :key :elf
   :help "Elves are graceful, magical creatures, with a slight build."
   :abilities {::char5e/dex 2}
   :size :medium
   :speed 30
   :languages ["Elvish" "Common"]
   :darkvision 60
   :modifiers [(mod5e/saving-throw-advantage [:charmed])
               (mod5e/immunity :magical-sleep)
               (mod5e/skill-proficiency :perception)]
   :subraces
   [{:name "High Elf"
     :abilities {::char5e/int 1}
     :selections [(high-elf-cantrip-selection spell-lists spells-map)
                  (opt5e/language-selection opt5e/languages 1)]
     :modifiers [elf-weapon-training-mods]}
    #_{:name "Wood Elf"
     :abilities {::char5e/wis 1}
     :modifiers [(mod5e/speed 5)
                 mask-of-the-wild-mod
                 elf-weapon-training-mods]}
    #_{:name "Dark Elf (Drow)"
     :abilities {::char5e/cha 1}
     :traits [(sunlight-sensitivity 24)]
     :modifiers (conj drow-magic-mods
                      (mod5e/weapon-proficiency :rapier)
                      (mod5e/weapon-proficiency :shortsword)
                      (mod5e/weapon-proficiency :crossbow-hand)
                      (mod5e/darkvision 120))}]
   :traits [{:name "Fey Ancestry"
             :page 23
             :summary "advantage on charmed saves and immune to sleep magic"}
            {:name "Trance"
             :page 23
             :summary "Trance 4 hrs. instead of sleep 8"}]})

(def dwarf-option-cfg
  {:name "Dwarf",
   :key :dwarf
   :help "Dwarves are short and stout and tend to be skilled warriors and craftmen in stone and metal."
   :abilities {::char5e/con 2},
   :size :medium
   :speed 25,
   :darkvision 60
   :languages ["Dwarvish" "Common"]
   :weapon-proficiencies [:handaxe :battleaxe :light-hammer :warhammer]
   :selections [(opt5e/tool-selection [:smiths-tools :brewers-supplies :masons-tools] 1)]
   :traits [{:name "Dwarven Resilience"
             :summary "Advantage on poison saves, resistance to poison damage"
             :page 20},
            {:name "Stonecunning"
             :summary "2X prof bonus on stonework-related history checks"
             :page 20}]
   :subraces [{:name "Hill Dwarf",
               :abilities {::char5e/wis 1}
               :modifiers [(mod/modifier ?hit-point-level-bonus (+ 1 ?hit-point-level-bonus))]}
              #_{:name "Mountain Dwarf"
               :abilities {::char5e/str 2}
               :armor-proficiencies [:light :medium]}]
   :modifiers [(mod5e/damage-resistance :poison)
               (mod5e/saving-throw-advantage [:poisoned])]})

(def halfling-option-cfg
  {:name "Halfling"
   :key :halfling
   :help "Halflings are small and nimble, half the height of a human, but fairly stout. They are cheerful and practical."
   :abilities {::char5e/dex 2}
   :size :small
   :speed 25
   :languages ["Halfling" "Common"]
   :modifiers [(mod5e/saving-throw-advantage [:frightened])]
   :subraces
   [{:name "Lightfoot"
     :abilities {::char5e/cha 1}
     :traits [{:name "Naturally Stealthy"
               :page 28
               :summary "Hide behind creatures larger than you"}]}
    #_{:name "Stout"
     :abilities {::char5e/con 1}
     :modifiers [(mod5e/damage-resistance :poison)
                 (mod5e/saving-throw-advantage [:poisoned])]}]
   :traits [{:name "Lucky"
             :page 28
             :summary "Reroll 1s on d20"}
            {:name "Halfling Nimbleness"
             :page 28
             :summary "move through the space of larger creatures"}
            {:name "Brave"
             :page 28
             :summary "you have advantage on saves against being frightened"}]})

(defn human-option-cfg [spell-lists spells-map]
  {:name "Human"
   :key :human
   :help "Humans are physically diverse and highly adaptable. They excel in nearly every profession."
   :size :medium
   :speed 30
   :languages ["Common"]
   :subraces
   [{:name "Calishite"}
    {:name "Chondathan"}
    {:name "Damaran"}
    {:name "Illuskan"}
    {:name "Mulan"}
    {:name "Rashemi"}
    {:name "Shou"}
    {:name "Tethyrian"}
    {:name "Turami"}]
   :selections [(opt5e/language-selection opt5e/languages 1)
                (t/selection-cfg
                 {:name "Variant"
                  :tags #{:subrace}
                  :options [(t/option-cfg
                             {:name "Standard Human"
                              :modifiers [(mod5e/race-ability ::char5e/str 1)
                                          (mod5e/race-ability ::char5e/con 1)
                                          (mod5e/race-ability ::char5e/dex 1)
                                          (mod5e/race-ability ::char5e/int 1)
                                          (mod5e/race-ability ::char5e/wis 1)
                                          (mod5e/race-ability ::char5e/cha 1)]})
                            (t/option-cfg
                             {:name "Variant Human"
                              :selections [(opt5e/feat-selection spell-lists spells-map 1)
                                           (opt5e/skill-selection 1)
                                           (opt5e/ability-increase-selection char5e/ability-keys 2 true)]})]})]})

(defn draconic-ancestry-option [{:keys [name breath-weapon]}]
  (t/option-cfg
   {:name name
    :modifiers [(mod5e/damage-resistance (:damage-type breath-weapon))
                (mod/modifier ?draconic-ancestry-breath-weapon breath-weapon)]}))

(def draconic-ancestries
  [{:name "Black"
    :breath-weapon {:damage-type :acid
                    :area-type :line
                    :line-width 5
                    :line-length 30
                    :save ::char5e/dex}}
   {:name "Blue"
    :breath-weapon {:damage-type :lightning
                    :area-type :line
                    :line-width 5
                    :line-length 30
                    :save ::char5e/dex}}
   {:name "Brass"
    :breath-weapon {:damage-type :fire
                    :area-type :line
                    :line-width 5
                    :line-length 30
                    :save ::char5e/dex}}
   {:name "Bronze"
    :breath-weapon {:damage-type :lightning
                    :area-type :line
                    :line-width 5
                    :line-length 30
                    :save ::char5e/dex}}
   {:name "Copper"
    :breath-weapon {:damage-type :acid
                    :area-type :line
                    :line-width 5
                    :line-length 30
                    :save ::char5e/dex}}
   {:name "Gold"
    :breath-weapon {:damage-type :fire
                    :area-type :cone
                    :length 15
                    :save ::char5e/dex}}
   {:name "Green"
    :breath-weapon {:damage-type :poison
                    :area-type :cone
                    :length 15
                    :save ::char5e/con}}
   {:name "Red"
    :breath-weapon {:damage-type :fire
                    :area-type :cone
                    :length 15
                    :save ::char5e/dex}}
   {:name "Silver"
    :breath-weapon {:damage-type :cold
                    :area-type :cone
                    :length 15
                    :save ::char5e/con}}
   {:name "White"
    :breath-weapon {:damage-type :cold
                    :area-type :cone
                    :length 15
                    :save ::char5e/con}}])

(def dragonborn-option-cfg
  {:name "Dragonborn"
   :key :dragonborn
   :help "Kin to dragons, dragonborn resemble humanoid dragons, without wings or tail and standing erect. They tend to make excellent warriors."
   :abilities {::char5e/str 2 ::char5e/cha 1}
   :size :medium
   :speed 30
   :languages ["Draconic" "Common"]
   :modifiers [(mod5e/attack
                (let [breath-weapon ?draconic-ancestry-breath-weapon
                      damage-type (:damage-type breath-weapon)]
                  (merge
                   breath-weapon
                   {:name "Breath Weapon"
                    :summary (if damage-type
                               (s/capitalize (name damage-type)))
                    :attack-type :area
                    :damage-die 6
                    :page 34
                    :damage-die-count (condp <= ?total-levels
                                        16 5
                                        11 4
                                        6 3
                                        2)
                    :save-dc (?spell-save-dc ::char5e/con)})))]
   :selections [(t/selection-cfg
                 {:name "Draconic Ancestry"
                  :tags #{:subrace}
                  :options (map
                            draconic-ancestry-option
                            draconic-ancestries)})]})


(def gnome-option-cfg
  {:name "Gnome"
   :key :gnome
   :help "Gnomes are small, intelligent humanoids who live life with the utmost of enthusiasm."
   :abilities {::char5e/int 2}
   :size :small
   :speed 25
   :darkvision 60
   :languages ["Gnomish" "Common"]
   :modifiers [(mod5e/saving-throw-advantage [:magic] [::char5e/int ::char5e/wis ::char5e/cha])]
   :traits [{:name "Gnome Cunning"
             :page 37
             :summary "Advantage on INT, WIS, and CHA saves against magic"}]
   :subraces
   [{:name "Rock Gnome"
     :abilities {::char5e/con 1}
     :modifiers [(mod5e/tool-proficiency :tinkers-tools)]
     :traits [{:name "Artificer's Lore"
               :page 37
               :summary "Add 2X prof bonus on magical, alchemical, or technological item-related history checks."}
              {:name "Tinker"
               :page 37
               :summary "Construct tiny clockwork devices."}]}
    #_{:name "Forest Gnome"
     :abilities {::char5e/dex 1}
     :modifiers [(mod5e/spells-known 0 :minor-illusion ::char5e/int "Forest Gnome")]
     :traits [{:name "Speak with Small Beasts"
               :page 37
               :summary "Communicate with Small or smaller beasts."}]}]})

(def half-elf-option-cfg
  {:name "Half-Elf"
   :key :half-elf
   :help "Half-elves are charismatic, and bear a resemblance to both their elvish and human parents and share many of the traits of each."
   :abilities {::char5e/cha 2}
   :size :medium
   :speed 30
   :darkvision 60
   :languages ["Common" "Elvish"]
   :selections [(opt5e/ability-increase-selection (disj (set char5e/ability-keys) ::char5e/cha) 2 true)
                (assoc
                 (opt5e/skill-selection 2)
                 ::t/ref
                 [:race :half-elf :skill-proficiency])
                (opt5e/language-selection opt5e/languages 1)]
   :modifiers [(mod5e/saving-throw-advantage [:charmed])]
   :traits [{:name "Fey Ancestry"
             :page 39
             :summary "advantage on charmed saves and immune to sleep magic"}]})

(def half-orc-option-cfg
  {:name "Half-Orc"
   :key :half-orc
   :help "Half-orcs are strong and bear an unmistakable resemblance to their orcish parent. They tend to make excellent warriors, especially Barbarians."
   :abilities {::char5e/str 2 ::char5e/con 1}
   :size :medium
   :speed 30
   :darkvision 60
   :languages ["Common" "Orc"]
   :modifiers [(mod5e/skill-proficiency :intimidation)]
   :traits [{:name "Relentless Endurance"
             :page 41
             :summary "Drop to 1 hp instead of being reduced to 0."}
            {:name "Savage Attacks"
             :page 41
             :summary "On critical hit, add additional damage dice roll"}]})

(def tiefling-option-cfg
  {:name "Tiefling"
   :key :tiefling
   :help "Tieflings bear the distinct marks of their infernal ancestry: horns, a tail, pointed teeth, and solid-colored eyes. They are smart and charismatic."
   :abilities {::char5e/int 1 ::char5e/cha 2}
   :size :medium
   :speed 30
   :darkvision 60
   :languages ["Common" "Infernal"]
   :modifiers [(mod5e/trait-cfg
                {:name "Hellish Resistance"
                 :page 43
                 :summary "Resistance to fire damage"})
               (mod5e/dependent-trait
                {:name "Infernal Legacy"
                 :page 43
                 :summary (str "You know thaumaturgy and can cast "
                               (common/list-print
                                (let [lvl ?total-levels]
                                  (cond-> []
                                    (>= lvl 3) (conj "Hellish Rebuke")
                                    (>= lvl 5) (conj "Darkness"))))
                               " once per day. CHA is the spellcasting ability.")})
               (mod5e/damage-resistance :fire)
               (mod5e/spells-known 0 :thaumaturgy ::char5e/cha "Tiefling")
               (mod5e/spells-known 1 :hellish-rebuke ::char5e/cha "Tiefling" 3)
               (mod5e/spells-known 2 :darkness ::char5e/cha "Tiefling" 5)]})

(reg-sub
 ::races5e/plugin-subraces-map
 :<- [::races5e/plugin-subraces]
 (fn [plugin-subraces]
   (group-by :race plugin-subraces)))

(reg-sub
 ::races5e/races
 :<- [::races5e/plugin-races]
 :<- [::races5e/plugin-subraces-map]
 :<- [::spells5e/spell-lists]
 :<- [::spells5e/spells-map]
 (fn [[plugin-races subraces-map spell-lists spells-map]]
   (map
    (fn [{:keys [key] :as race}]
      (if (subraces-map key)
        (update race :subraces concat (subraces-map key))
        race))
    (concat
     [dwarf-option-cfg
      (elf-option-cfg spell-lists spells-map)
      halfling-option-cfg
      (human-option-cfg spell-lists spells-map)
      dragonborn-option-cfg
      gnome-option-cfg
      half-elf-option-cfg
      half-orc-option-cfg
      tiefling-option-cfg]
     plugin-races))))

(reg-sub
 ::races5e/race-map
 :<- [::races5e/races]
 (fn [races]
   (common/map-by-key races)))

(reg-sub
 ::races5e/race
 :<- [::races5e/race-map]
 (fn [race-map [_ key]]
   (race-map key)))

(reg-sub
 ::feats5e/feats
 :<- [::feats5e/plugin-feats]
 (fn [plugin-feats]
   plugin-feats))

(reg-sub
 ::spells5e/plugin-spells
 :<- [::e5/plugin-vals]
 (fn [plugins _]
   (apply concat (map (comp vals ::e5/spells) plugins))))

(reg-sub
 ::spells5e/spells
 :<- [::spells5e/plugin-spells]
 (fn [plugin-spells]
   (concat
    spells5e/spells
    plugin-spells)))

(reg-sub
 ::spells5e/spells-map
 :<- [::spells5e/spells]
 (fn [spells]
   (reduce
    (fn [m {:keys [name key level] :as spell}]
      (assoc m (or key (common/name-to-kw name)) spell))
    {}
    spells)))

(defn merge-spell-lists [& spell-lists]
  (apply
   merge-with
   concat
   spell-lists))

(reg-sub
 ::spells5e/plugin-spell-lists
 :<- [::spells5e/plugin-spells]
 (fn [plugin-spells _]
   (reduce
    (fn [lists {:keys [key level spell-lists]}]
      (reduce-kv
       (fn [l k v]
         (update-in l [k level] conj key))
       lists
       spell-lists))
    {}
    plugin-spells)))

(reg-sub
 ::spells5e/spell-lists
 :<- [::spells5e/plugin-spell-lists]
 (fn [plugin-spell-lists]
   (merge-with
    merge-spell-lists
    sl5e/spell-lists
    plugin-spell-lists)))

(reg-sub
 ::spells5e/spellcasting-classes
 (fn []
   (map
    (fn [kw]
      {:key kw
       :name (common/kw-to-name kw)})
    [:bard :cleric :druid :paladin :ranger :sorcerer :warlock :wizard])))

(defn spell-option [spells-map [_ spell-key ability-key class-name]]
   (let [spell (spells-map spell-key)
         level (:level spell)]
     (t/option-cfg
      {:name (str level " - " (:name spell))
       :key spell-key
       :modifiers [(mod5e/spells-known
                    (:level spell)
                    spell-key
                    ability-key
                    class-name)]})))

(reg-sub
 ::spells5e/spell-option
 :<- [::spells5e/spells-map]
 spell-option)

(reg-sub
 ::spells5e/spell-options
 :<- [::spells5e/spells-map]
 :<- [::spells5e/spell-lists]
 (fn [[spells-map spell-lists] [_ ability-key class-name levels]]
   (apply concat
          (sequence
           (comp
            (map spell-lists)
            (map (fn [spell-key]
                   (spell-option spells-map [nil spell-key ability-key class-name]))))
           levels))))

(reg-sub
 ::spells5e/builder-item
 (fn [db _]
   (::spells5e/builder-item db)))

(reg-sub
 ::bg5e/builder-item
 (fn [db _]
   (::bg5e/builder-item db)))

(reg-sub
 ::races5e/builder-item
 (fn [db _]
   (::races5e/builder-item db)))

(reg-sub
 ::races5e/subrace-builder-item
 (fn [db _]
   (::races5e/subrace-builder-item db)))

(reg-sub
 ::feats5e/builder-item
 (fn [db _]
   (::feats5e/builder-item db)))

(ns orcpub.dnd.e5.options
  (:require [clojure.string :as s]
            [clojure.set :as sets]
            [orcpub.common :as common]
            [orcpub.template :as t]
            [orcpub.entity :as entity]
            [orcpub.dice :as dice]
            [orcpub.entity-spec :as es]
            [orcpub.modifiers :as mods]
            [orcpub.dnd.e5.character :as character]
            [orcpub.dnd.e5.character.equipment :as char-equip]
            [orcpub.dnd.e5.modifiers :as modifiers]
            [orcpub.dnd.e5.weapons :as weapons]
            [orcpub.dnd.e5.armor :as armor]
            [orcpub.dnd.e5.spells :as spells]
            [orcpub.dnd.e5.equipment :as equipment]
            [orcpub.dnd.e5.spell-lists :as sl]
            [orcpub.dnd.e5.display :as disp]
            [orcpub.dnd.e5.skills :as skills]
            [orcpub.dnd.e5.event-handlers :as eh]
            [re-frame.core :refer [dispatch subscribe]])
  #?(:cljs (:require-macros [orcpub.dnd.e5.modifiers :as modifiers])))

#?(:cljs (enable-console-print!))

(def ft-5 {:units :feet
           :amount 5})

(def ft-10 {:units :feet
           :amount 10})

(def ft-30 {:units :feet
            :amount 30})

(def ft-60 {:units :feet
            :amount 60})

(def ft-90 {:units :feet
            :amount 90})

(def ft-120 {:units :feet
             :amount 120})

(def turns-1 {:units :turn})

(def rounds-1 {:units :round})

(def minutes-1 {:units :minute})

(def minutes-10 {:units :minute
                 :amount 10})

(def hours-1 {:units :hour})

(def conc-minutes-1 {:units :minute
                     :concentration true})

(def conc-minutes-10 {:units :minute
                      :amount 10
                      :concentration true})

(def conc-hours-1 {:units :hour
              :concentration true})

(def rests-1 {:units :rest})

(def long-rests-1 {:units :long-rest})

(def abilities
  [{:key ::character/str
    :name "Strength"}
   {:key ::character/con
    :name "Constitution"}
   {:key ::character/dex
    :name "Dexterity"}
   {:key ::character/int
    :name "Intelligence"}
   {:key ::character/wis
    :name "Wisdom"}
   {:key ::character/cha
    :name "Charisma"}])

(def abilities-map
  (common/map-by-key abilities))

(def conditions
  [{:name "Blinded"}
   {:name "Charmed"}
   {:name "Deafened"}
   {:name "Frightened"}
   {:name "Grappled"}
   {:name "Incapacitated"}
   {:name "Invisible"}
   {:name "Paralyzed"}
   {:name "Petrified"}
   {:name "Poisoned"}
   {:name "Prone"}
   {:name "Restrained"}
   {:name "Stunned"}
   {:name "Unconscious"}])

(def damage-types
  [:acid
   :bludgeoning
   :cold
   :fire
   :force
   :lightning
   :necrotic
   :piercing
   :poison
   :psychic
   :radiant
   :slashing
   :thunder])

(def conditions-map
  (common/map-by-key (common/add-keys conditions)))

(defn skill-option [skill]
  (t/option-cfg
   {:name (:name skill)
    :icon (:icon skill)
    :key (:key skill)
    :help (:description skill)
    :prereqs [(t/option-prereq
               "You already have this skill"
               (fn [c]
                 (let [skill-profs @(subscribe [::character/skill-profs nil c])]
                   (not (get skill-profs (:key skill))))))]
    :modifiers [(modifiers/skill-proficiency (:key skill))]}))

(defn weapon-proficiency-option [{:keys [name key]}]
  (t/option-cfg
   {:name name
    :modifiers [(modifiers/weapon-proficiency key)]}))

(defn tool-option [tool]
  (t/option-cfg
   {:name (:name tool)
    :key (:key tool)
    :icon (:icon tool)
    :modifiers [(modifiers/tool-proficiency (:key tool))]}))

(defn weapon-option [weapon & [num]]
  (t/option-cfg
   {:name (:name weapon)
    :key (:key weapon)
    :help (:description weapon)
    :modifiers [(modifiers/weapon (:key weapon) {::char-equip/equipped? true
                                                 ::char-equip/quantity (or num 1)})]}))

(defn weapon-options [weapons & [num]]
  (map
   #(weapon-option % num)
   weapons))

(defn simple-melee-weapon-options [num]
  (weapon-options
   (filter
    #(and (= :simple (:type %)) (:melee? %))
    weapons/weapons)
   num))

(defn martial-weapon-options [num]
  (weapon-options
   (filter
    #(= :martial (:type %))
    weapons/weapons)
   num))

(defn simple-weapon-options [num]
  (weapon-options
   (filter
    #(= :simple (:type %))
    weapons/weapons)
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

(defn ability-bonus [ability-value]
  (- (int (/ ability-value 2)) 5))

(defn ability-bonus-str [ability-value]
  (common/bonus-str (ability-bonus ability-value)))

(defn get-raw-abilities [character]
  (get-in character [::entity/options :ability-scores ::entity/value]))

(defn ability-increase-selection-2 [{:keys [ability-keys num-increases min max different? modifier-fns]}]
  (t/selection-cfg
   {:name "Ability Score Improvement"
    :key :asi
    :min (or num-increases min)
    :max (or num-increases max)
    :tags #{:ability-scores}
    :different? different?
    :multiselect? true
    :options (map
              (fn [k]
                (t/option-cfg
                 {:name (:name (abilities-map k))
                  :key k
                  :modifiers (conj (map #(% k) modifier-fns)
                                   (modifiers/level-ability-increase k 1))}))
              (or ability-keys
                  character/ability-keys))}))

(defn ability-increase-selection [ability-keys num-increases & [different? modifier-fns]]
  (ability-increase-selection-2 {:ability-keys ability-keys
                                 :num-increases num-increases
                                 :different? different?
                                 :modifier-fns modifier-fns}))

(defn ability-increase-option [num-increases different? ability-keys]
  (t/option-cfg
   {:name "Ability Score Improvement"
    :key :ability-score-improvement
    :selections [(ability-increase-selection ability-keys num-increases different?)]
    :modifiers [(modifiers/deferred-ability-increases)]}))

(defn min-ability [ability-kw min-value]
  (fn [c] (>= (ability-kw @(subscribe [::character/abilities nil c])) min-value)))

(defn ability-prereq [ability-kw min-value]
  (t/option-prereq (str "Requires " (s/upper-case (name ability-kw)) " " min-value " or higher")
                   (min-ability ability-kw min-value)))

(defn armor-prereq [armor-kw]
  (t/option-prereq (str "Requires proficiency with " (name armor-kw) " armor")
                   (fn [c] (let [prof-keys @(subscribe [::character/armor-profs nil c])]
                             (boolean (and prof-keys (prof-keys armor-kw)))))))

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
    :key :sylvan}
   {:name "Undercommon"
    :key :undercommon}])

(def language-keys (map :key languages))

(def language-map
  (zipmap language-keys languages))

(def elemental-disciplines
  [(t/option-cfg
    {:name "Breath of Winter"
     :modifiers [(modifiers/action
                  {:name "Breath of Winter"
                   :level 17
                   :page 81
                   :summary "spend 6 ki to cast cone of cold"})]})
   (t/option-cfg
    {:name "Clench of the North Wind"
     :modifiers [(modifiers/action
                  {:name "Clench of the North Wind"
                   :page 81
                   :level 6
                   :summary "spend 3 ki to cast hold person"})]})
   (t/option-cfg
    {:name "Eternal Mountain Defense"
     :modifiers [(modifiers/action
                  {:name "Eternal Mountain Defense"
                   :level 17
                   :page 81
                   :summary "spend 5 ki to cast stoneskin on yourself"})]})
   (t/option-cfg
    {:name "Fangs of the Fire Snake"
     :modifiers [(modifiers/trait-cfg
                  {:name "Fangs of the Fire Snake"
                   :page 81
                   :summary "spend 1 ki point when you use Attack action to increase your unarmed strike reach by 10 ft. You unarmed strike deals fire damage and if you spend 1 more ki it deals an extra 2d10 damage"})]})
   (t/option-cfg
    {:name "Fist of Four Thunders"
     :modifiers [(modifiers/action
                  {:name "Fist of Four Thunders"
                   :page 81
                   :summary "spend 2 ki to cast thunderwave"})]})
   (t/option-cfg
    {:name "Fist of Unbroken Air"
     :modifiers [(modifiers/action
                  {:name "Fist of Unbroken Air"
                   :page 81
                   :summary (str "spend 2 + X ki, a creature within 30 ft. takes 3d10 + Xd10 damage on failed DC " (?spell-save-dc ::character/wis) " STR save, is pushed up to 20 ft., and is knocked prone. On successful save it just takes half damage.")})]})
   (t/option-cfg
    {:name "Flames of the Phoenix"
     :modifiers [(modifiers/action
                  {:name "Flames of the Phoenix"
                   :level 11
                   :page 81
                   :summary "spend 4 ki to cast fireball"})]})
   (t/option-cfg
    {:name "Gong of the Summit"
     :modifiers [(modifiers/action
                  {:name "Gong of the Summit"
                   :page 81
                   :level 6
                   :summary "spend 3 ki to cast shatter"})]})
   (t/option-cfg
    {:name "Mist Stance"
     :modifiers [(modifiers/action
                  {:name "Mist Stance"
                   :page 81
                   :level 11
                   :summary "spend 4 ki to cast gaseous form on yourself"})]})
   (t/option-cfg
    {:name "Ride the Wind"
     :modifiers [(modifiers/action
                  {:name "Ride the Wind"
                   :page 81
                   :level 11
                   :summary "spend 4 ki to cast fly on yourself"})]})
   (t/option-cfg
    {:name "River of Hungry Flame"
     :modifiers [(modifiers/action
                  {:name "River of Hungry Flame"
                   :page 81
                   :level 17
                   :summary "spend 5 ki to cast wall of fire"})]})
   (t/option-cfg
    {:name "Rush of the Gale Spirits"
     :modifiers [(modifiers/action
                  {:name "Rush of the Gale Spirits"
                   :page 81
                   :summary "spend 2 ki to cast gust of wind"})]})
   (t/option-cfg
    {:name "Shape of the Flowing River"
     :modfiers [(modifiers/action
                 {:name "Shape of the Flowing River"
                  :page 81
                  :summary "spend 1 ki to transform ice to water, and vice versa, reshape ice"})]})
   (t/option-cfg
    {:name "Sweeping Cinder Strike"
     :modifiers [(modifiers/action
                  {:name "Sweeping Cinder Strike"
                   :page 81
                   :summary "spend 2 ki to cast burning hands"})]})
   (t/option-cfg
    {:name "Water Whip"
     :modifiers [(modifiers/bonus-action
                  {:name "Water Whip"
                   :page 81
                   :summary (str "spend 2 + X ki, a creature within 30 ft. takes 3d10 + Xd10 damage on failed DC " (?spell-save-dc ::character/wis) " DEX save, is pulled up to 25 ft. or knocked prone. On successful save it just takes half damage.")})]})
   (t/option-cfg
    {:name "Wave of Rolling Earth"
     :modifiers [(modifiers/action
                  {:name "Wave of Rolling Earth"
                   :level 17
                   :page 81
                   :summary "spend 6 ki to cast wall of stone"})]})])

(defn monk-elemental-disciplines []
  (t/selection-cfg
   {:name "Elemental Disciplines"
    :tags #{:class}
    :ref [:class :monk :elemental-disciplines]
    :multiselect? true
    :options elemental-disciplines}))

(defn language-option [{:keys [name key]}]
  (t/option-cfg
   {:name name
    :modifiers [(modifiers/language key)]
    :prereqs [(t/option-prereq
               "You already have this language"
               (fn [c] (not (get @(subscribe [::character/languages nil c]) key))))]}))

(defn key-to-name [key]
  (s/join " " (map s/capitalize (s/split (name key) #"-"))))

(defn spell-field [name value]
  [:div.m-b-2
   [:span.f-w-b (str name ": ")]
   [:span.f-w-n value]])

(defn spell-help [{:keys [school casting-time range duration description summary source page]}]
  [:div
   [:div.m-b-5
    (spell-field "School" school)
    (spell-field "Casting Time" casting-time)
    (spell-field "Range" range)
    (spell-field "Duration" duration)]
   [:div.f-w-n (if (or description summary)
                 (doall
                  (map-indexed
                   (fn [i p]
                     ^{:key i} [:p.m-t-5 p])
                   (s/split (or description summary) #"\n"))))]
   (if source
     (let [{:keys [abbr url]} (disp/sources source)]
       [:div.f-w-n
        [:span "(see"]
        [:a.m-l-5 {:href url :target :_blank} abbr]
        [:span.m-l-5 (str "page " page)]
        [:span " for more details)"]]))])

(defn spell-option [spellcasting-ability class-name key & [prepend-level? qualifier]]
  (let [{:keys [name level source] :as spell} (spells/spell-map key)]
    (t/option-cfg
     {:name (if prepend-level? (str level " - " name) name)
      :key key
      :help (spell-help spell)
      :prereqs [(t/option-prereq
                 "You already know this spell"
                 (fn [c] (let [spells-known @(subscribe [::character/spells-known nil c])]
                           (or (not spells-known)
                               (not (some #(= key (:key %))
                                          (spells-known level)))))))
                (t/option-prereq
                 "You aren't using this source"
                 (fn [c] (or (nil? source)
                             (= :phb source)
                             (get @(subscribe [::character/option-sources nil c]) source)))
                 true)]
      :modifiers [(modifiers/spells-known level key spellcasting-ability class-name nil qualifier)]})))

(def memoized-spell-option (memoize spell-option))

(defn spell-options [spells spellcasting-ability class-name & [prepend-level? qualifier]]
  (map
   #(memoized-spell-option spellcasting-ability class-name % prepend-level? qualifier)
   (sort spells)))

(defn spell-level-title [class-name level]
  (str class-name (if (and level (zero? level)) " Cantrips Known" (str " Spells Known" (if level (str " " level))))))

(defn spell-selection [{:keys [title class-key level spellcasting-ability class-name num prepend-level? spell-keys options min max exclude-ref? ref]}]
  (let [title (or title (spell-level-title class-name level))
        kw (common/name-to-kw title)
        ref (or ref (if (not exclude-ref?) [:class class-key kw]))]
     (t/selection-cfg
      {:name title
       :key kw
       :ref ref
       :order (if (and level (zero? level)) 0 1)
       :multiselect? true
       :options (or options
                    (spell-options
                     (or spell-keys (get-in sl/spell-lists [class-key level]))
                     spellcasting-ability
                     class-name
                     prepend-level?))
       :min (or min num)
       :max (or max num)
       :tags #{:spells}})))

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
       19 {4 1}}
    {}))

(defn total-slots [level level-factor]
  (let [schedule (spell-slot-schedule level-factor)]
    (reduce
     (fn [m lvl]
       (merge-with + m (schedule lvl)))
     {}
     (range 1 (inc level)))))

(defn spell-tags [cls-key-nm level]
  #{:spells (keyword (str cls-key-nm "-spells")) (keyword (str "level-" level))})

(defn bard-magical-secrets [min-level]
  (let [max-level (key (last (total-slots min-level 1)))
        spells-by-level (group-by :level spells/spells)
        filtered-spells-by-level (select-keys spells-by-level (range 1 (inc max-level)))]
    (t/selection-cfg
     {:name "Bard Magical Secrets"
      :tags #{:spells}
      :min 2
      :max 2
      :ref [:class :bard :magical-secrets]
      :options (mapcat
                (fn [[lvl spells]]
                  (map
                   (fn [{:keys [name] :as spell}]
                     (let [key (or (:key spell) (common/name-to-kw name))]
                       (spell-option ::character/cha "Bard" key true)))
                   spells))
                filtered-spells-by-level)})))

(defn cantrip-selections [class-key class-name ability cantrips-known]
  (reduce
   (fn [m [k v]]
     (assoc m k [(spell-selection {:class-key class-key
                                   :level 0
                                   :spellcasting-ability ability
                                   :class-name class-name
                                   :num v})]))
   {}
   cantrips-known))

(defn apply-spell-restriction [spell-keys restriction]
  (if restriction
    (filter
     (fn [spell-key]
       (restriction (spells/spell-map spell-key)))
     spell-keys)
    spell-keys))

(defn class-key-name [cls-key cls-nm]
  (if cls-key
    (name cls-key)
    (common/name-to-kw cls-nm)))

(defn spell-selection-key [cls-key-nm]
  (keyword (str cls-key-nm "-spells-known")))


(defn spells-known-selections [{:keys [class-key
                                       level-factor
                                       spells-known
                                       known-mode
                                       spell-list
                                       spells
                                       ability
                                       slot-schedule] :as cfg}
                               cls-cfg]
  (reduce
   (fn [m [cls-lvl v]]
     (let [[num restriction] (if (number? v) [v] ((juxt :num :restriction) v))
           slots (or (if slot-schedule (slot-schedule cls-lvl)) (total-slots cls-lvl level-factor))
           all-spells (select-keys
                       (or spells (sl/spell-lists (or spell-list class-key)))
                       (keys slots))
           acquire? (= :acquire known-mode)]
       (let [options (flatten
                      (map
                       (fn [[lvl spell-keys]]
                         (map
                          (fn [spell-key]
                            (let [spell (spells/spell-map spell-key)]
                              #?@(:cljs
                                  [(if (nil? spell) (js/console.warn (str "No spell found for key: " spell-key)))
                                   (if (nil? (:name spell)) (js/console.warn (str "Spell is missing name: " spell-key)))])
                              (spell-option
                               ability
                               (:name cls-cfg)
                               spell-key
                               true)))
                          (apply-spell-restriction spell-keys restriction)))
                       all-spells))]
         (assoc m cls-lvl
                [(let [cls-key-nm (class-key-name (:key cls-cfg) (:name cls-cfg))
                       kw (spell-selection-key cls-key-nm)
                       cls-nm (:name cls-cfg)]
                   (spell-selection
                    {:class-key class-key
                     :class-name cls-nm
                     :min num
                     :max (if (not acquire?) num)
                     :options options}))]))))
   {}
   spells-known))

(defn spellcasting-template [{:keys [class-key
                                     level-factor
                                     cantrips-known
                                     spells-known
                                     known-mode
                                     ability] :as cfg}
                             cls-cfg]
  (let [spell-selections (spells-known-selections cfg cls-cfg)
        cantrip-selections (cantrip-selections class-key (:name cls-cfg) ability cantrips-known)]
    {:selections (merge-with
                  concat
                  cantrip-selections
                  spell-selections)}))

(defn magic-initiate-option [class-key class-name spellcasting-ability spell-lists]
  (t/option-cfg
   {:name (name class-key)
    :selections [(t/selection-cfg
                  {:name "Cantrip"
                   :order 1
                   :tags #{:spells}
                   :options (spell-options (get-in spell-lists [class-key 0]) spellcasting-ability class-name)
                   :min 2
                   :max 2})
                 (t/selection-cfg
                  {:name "Level 1 Spell"
                   :order 2
                   :tags #{:spells}
                   :options (spell-options (get-in spell-lists [class-key 1]) spellcasting-ability class-name)
                   :min 1
                   :max 1})]}))

(defn ritual-spell? [spell]
  (:ritual spell))

(defn ritual-caster-option [class-key class-name spellcasting-ability spell-lists]
  (t/option-cfg
   {:name (name class-key)
    :key class-key
    :selections [(t/selection-cfg
                  {:name "Level 1 Ritual"
                   :tags #{:spells}
                   :options (spell-options
                             (filter (fn [spell-kw] (ritual-spell? (spells/spell-map spell-kw))) (get-in spell-lists [class-key 1]))
                             spellcasting-ability
                             class-name
                             false
                             "Ritual Only")
                   :min 2
                   :max 2})]}))

(defn spell-sniper-option [class-key class-name spellcasting-ability spell-lists]
  (let [options (spell-options (filter (fn [spell-kw] (:attack-roll? (spells/spell-map spell-kw))) (get-in spell-lists [class-key 0])) spellcasting-ability class-name)]
    (t/option-cfg
     {:name (name class-key)
      :key class-key
      :prereqs [(t/option-prereq
                 "There are no attack cantrips for this class with the 'Option Sources' you have selected"
                 (fn [_] (seq options)))]
      :selections [(t/selection-cfg
                    {:name "Attack Cantrip"
                     :tags #{:spells}
                     :options options})]})))

(defn language-selection
  ([langs num]
  (t/selection-cfg
   {:name "Languages"
    :options (map
     (fn [lang]
       (language-option lang))
     langs)
    :ref [:languages]
    :multiselect? true
    :tags #{:profs :language-profs}
    :min num
    :max num}))
  ([language-options]
   (let [{lang-num :choose lang-options :options} language-options
         lang-kws (if (:any lang-options)
                    (map :key languages)
                    (keys lang-options))]
     (language-selection (map language-map lang-kws) lang-num))))

(defn any-language-selection [num]
  (language-selection languages num))

(defn maneuver-option [name & [desc]]
  (t/option-cfg
   {:name name
    :modifiers [(modifiers/trait (str name " Maneuver")
                      desc)]}))

(defn mod-maneuver-option [name mods]
  (t/option-cfg
   {:name name
    :modifiers mods}))

(defn proficiency-help [num singular plural]
  (str "Select additional " (if (> num 1) plural singular) " for which you are proficient."))

(defn skill-selection-2 [{:keys [options num min max order key prereq-fn]}]
  (t/selection-cfg
   {:name "Skill Proficiency"
    :key key
    :order (or order 0)
    :help (proficiency-help (or num min) "a skill" "skills")
    :options (let [key-set (set options)]
               (skill-options
                (filter
                 (comp key-set :key)
                 skills/skills)))
    :min (or min num)
    :max (or max num)
    :multiselect? true
    ;;:ref [:skill-profs]
    :tags #{:skill-profs :profs}
    :prereq-fn prereq-fn}))

(defn skill-prof-or-expertise [skill-kw source]
  [(modifiers/skill-proficiency skill-kw source)
   (modifiers/skill-expertise skill-kw [(some
                                     (fn [[k v]]
                                       (not= k source))
                                     (?skill-profs skill-kw))])])

(defn skill-or-expertise-selection [num skill-kws option-source]
  (t/selection-cfg
   {:name "Skill Proficiency"
    :order 0
    :tags #{:skill-profs :profs}
    :options (map
              (fn [skill-kw]
                (let [{:keys [name icon]} (skills/skills-map skill-kw)]
                  (t/option-cfg
                   {:name name
                    :icon icon
                    :modifiers [(skill-prof-or-expertise skill-kw option-source)]})))
              [:acrobatics :athletics])}))

(defn skill-selection
  ([num]
   (skill-selection-2 {:num num
                       :options (map :key skills/skills)}))
  ([options num & [order key prereq-fn]]
   (skill-selection-2 {:options options
                       :num num
                       :order order
                       :key key
                       :prereq-fn prereq-fn})))

(defn tool-proficiency-selection-2 [{:keys [num min max] :as cfg}]
  (t/selection-cfg
   (merge
    {:name "Tool Proficiency"
     :help (proficiency-help (or num min) "a tool" "tools")
     :multiselect 2
     :tags #{:tool-profs :profs}}
    (if num {:min num :max num})
    cfg)))

(defn tool-proficiency-selection [cfg]
  (tool-proficiency-selection-2
   cfg))

(defn tool-selection
  ([num]
   (tool-proficiency-selection
    {:options (tool-options equipment/tools)
     :num num}))
  ([options num]
   (tool-proficiency-selection
    {:options (tool-options
               (filter
                (comp (set options) :key)
                equipment/tools))
     :num num})))


(defn weapon-proficiency-selection
  ([num]
   (t/selection-cfg
    {:name "Weapon Proficiency"
     :help (proficiency-help num "a weapon" "weapons")
     :options (weapon-proficiency-options weapons/weapons)
     :min num
     :max num
     :tags #{:weapon-profs :profs}}))
  ([options num]
   (t/selection-cfg
    {:name "Weapon Proficiency"
     :help (proficiency-help num "a weapon" "weapons")
     :options (weapon-proficiency-options
               (filter
                (comp (set options) :key)
                weapons/weapons))
     :min num
     :max num
     :tags #{:weapon-profs :profs}})))

(defn skilled-selection [title]
  (t/selection-cfg
   {:name title
    :tags #{:profs}
    :options [(t/option-cfg
              {:name "Skill"
               :selections [(skill-selection 1)]})
             (t/option-cfg
              {:name "Tool"
               :selections [(tool-selection 1)]})]}))

(def maneuver-options
  [(maneuver-option "Commander's Strike"
                    "When you take Attack action, forgo one attack, expend a superiority die, give a creature an immediate reaction attack, adding superiority die to damage")
   (maneuver-option "Disarming Attack"
                    "When you hit with a weapon attack, expend a superiority die and force the target to drop an item of your choice on failed STR save")
   (maneuver-option "Distracting Strike"
                    "When you hit with a weapon attack, expend a superiority die, add die to damage, give advantage to next attack roll by someone else against the creature")
   (maneuver-option "Evasive Footwork"
                    "Add superiority die to AC when moving")
   (mod-maneuver-option
    "Feinting Attack"
    [(modifiers/bonus-action
      {:name "Feinting Attack Maneuver"
       :page 74
       :summary "feint attack on a creature and gain advantage on next attack against it, adding superiority die to damage"})])
   (mod-maneuver-option
    "Goading Attack"
    [(modifiers/dependent-trait
      {:name "Goading Attack Maneuver"
       :page 74
       :summary (str "add superiority die to a successful attack's damage, if target fails DC " ?maneuver-save-dc " WIS save, the next attack it makes must be against you or have disadvantage")})])
   (maneuver-option "Lunging Attack"
                    "increase melee attack reach by 5 ft., add superiority die to damage")
   (maneuver-option "Manuevering Attack"
                    "add superiority die to a successful attack's damage, choose a friendly creature that can move half it's speed as a reaction without opportunity attack from attack target")
   (mod-maneuver-option
    "Menacing Attack"
    [(modifiers/dependent-trait
      {:name "Menacing Attack Maneuver"
       :page 74
       :summary (str "add superiority die to a successful attack's damage, if target fails DC " ?maneuver-save-dc " WIS save, it becomes frightened of you until your next turn")})])
   (mod-maneuver-option
    "Parry"
    [(modifiers/reaction
      {:name "Parry Maneuver"
       :page 74
       :summary (str "reduce melee attack damage dealt to you by superiority die roll " (common/mod-str (?ability-bonuses ::character/dex)))})])
   (maneuver-option "Precision Attack"
                    "add superiority die to weapon attack roll")
   (mod-maneuver-option
    "Pushing Attack"
    [(modifiers/dependent-trait
      {:name "Pushing Attack Maneuver"
       :page 74
       :summary (str "add superiority die to a successful attack's damage, if target is Large or smaller and fails a DC " ?maneuver-save-dc " STR save, it is pushed 15 ft. away")})])
   (mod-maneuver-option
    "Rally"
    [(modifiers/bonus-action
      {:name "Rally Maneuver"
       :page 74
       :summary (str "give superiority die "
                     (common/mod-str (?ability-bonuses ::character/cha))
                     " temp HPs to a friendly creature")})])
   (mod-maneuver-option
    "Riposte"
    [(modifiers/reaction
      {:name "Riposte Maneuver"
       :page 74
       :summary "if a creature misses you with a melee attack, attack as a reaction and add superiority die to damage"})])
   (maneuver-option "Sweeping Attack"
                    "if you hit a creature with an attack roll, choose another creature within 5 ft., if the roll would hit the creature, it takes superiority die worth of damage")
   (mod-maneuver-option
    "Trip Attack"
    [(modifiers/dependent-trait
      {:name "Trip Attack Maneuver"
       :page 74
       :summary (str "add superiority die to successful attack's damage, if target fails a DC " ?maneuver-save-dc " STR save, it is knocked prone")})])])

(def can-cast-spell-prereq
  (t/option-prereq "Requires the ability to cast at least one spell."
                   (fn [c] (some (fn [[k v]] (seq v)) @(subscribe [::character/spells-known nil c])))))

(defn does-not-have-feat-prereq [kw]
  {::t/label "You already have this feat."
   ::t/prereq-fn (fn [c] (let [feats @(subscribe [::character/feats nil c])]
                           (not (and feats (feats kw)))))})

(defn feat-option [cfg & [multiselect?]]
  (let [kw (common/name-to-kw (:name cfg))
        summary (:summary cfg)]
    (t/option-cfg
     (cond-> cfg
       true (assoc :key kw :help summary)
       (not (:exclude-trait? cfg)) (update :modifiers
                                           conj
                                           (modifiers/trait-cfg
                                            {:name (str (:name cfg) " Feat")
                                             :page (:page cfg)
                                             :source (:source cfg)
                                             :summary summary}))
       true (update :modifiers
                    conj
                    (mods/set-mod ?feats kw))
       (not multiselect?) (update :prereqs conj (does-not-have-feat-prereq kw))))))

(def charge-summary "when you Dash, you can make 1 melee attack or shove as a bonus action; if you move 10 ft. before taking this bonus action you gain +5 damage to attack or shove 10 ft.")

(def defensive-duelist-summary "when you are hit with a melee attack, you can add your prof bonus to AC for the attack if you are wielding a finesse weapon you are proficient with")

(def feat-options
  [(feat-option
    {:name "Alert"
     :icon "look-at"
     :page 165
     :summary "+5 initiative; can't be surprised; creatures don't gain advantage on attacks against you for being hidden"
     :modifiers [(modifiers/initiative 5)]})
   (feat-option
    {:name "Athlete"
     :icon "weight-lifting-up"
     :page 165
     :summary "increase STR or DEX by 1; standing up only uses 5 ft movement; climbing doesn't cost extra movement; make running long or high jump after moving only 5 ft."
     :selections [(ability-increase-selection [::character/str ::character/dex] 1 false)]})
   (feat-option
    {:name "Actor"
     :icon "drama-masks"
     :page 165
     :summary "increase CHA by 1; advantage on Deception and Performance when trying to pass as someone else; mimic the speech of a person you have heard"
     :modifiers [(modifiers/ability ::character/cha 1)]})
   (feat-option
    {:name "Charger"
     :icon "charging-bull"
     :page 165
     :summary charge-summary
     :modifiers [(modifiers/bonus-action
                  {:name "Charge"
                   :page 165
                   :summary charge-summary})]})
   (feat-option
    {:name "Crossbow Expert"
     :icon "crossbow"
     :page 165
     :summary "ignore loading property of crossbows you are proficient with; don't have disadvantage from being within 5 ft of hostile creature; when you Attack with 1 hand weapon, you can attack with a hand crossbow as bonus action"
     :modifiers [(modifiers/bonus-action
                  {:name "Crossbow Expert"
                   :page 165
                   :summary "when you Attack with 1 hand weapon, you can attack with a hand crossbow"})]})
   (feat-option
    {:name "Defensive Duelist"
     :icon "spinning-sword"
     :page 165
     :exclude-trait? true
     :summary defensive-duelist-summary
     :modifiers [(modifiers/reaction
                  {:name "Defensive Duelist"
                   :page 165
                   :summary defensive-duelist-summary})]
     :prereqs [(ability-prereq ::character/dex 13)]})
   (feat-option
    {:name "Dual Wielder"
     :icon "rogue"
     :page 165
     :summary "+1 AC bonus when wielding two melee weapons; two-weapon fighting with any one-handed melee weapon"})
   (feat-option
    {:name "Dungeon Delver"
     :icon "dungeon-gate"
     :page 166
     :summary "advantage to detect secret doors; advantage on saves against and resistance to trap damage; search for traps at normal pace"
     :modifiers [(modifiers/damage-resistance :trap)
                 (modifiers/saving-throw-advantage [:traps])]})
   (feat-option
    {:name "Durable"
     :icon "hospital-cross"
     :page 166
     :exclude-trait? true
     :summary "increase CON by 1; when you roll Hit Die to regain HPs, the min points regained is 2X your CON modifier"
     :modifiers [(modifiers/ability ::character/con 1)
                 (modifiers/dependent-trait
                  {:name "Durable"
                   :page 166
                   :summary (str "when you roll Hit Die to regain HPs, the min points regained is " (* 2 (?ability-bonuses ::character/con)))})]})
   (feat-option
    {:name "Elemental Adept"
     :icon "wind-hole"
     :page 166
     :summary "select a damage type, your spells ignore resistance to that type and min damage die roll is 2"
     :prereqs [can-cast-spell-prereq]}
    true)
   (feat-option
    {:name "Grappler"
     :icon "muscle-up"
     :page 167
     :summary "advantage on attacks against creature you grapple; can use an action to pin the creature"
     :modifiers [(modifiers/action
                  {:name "Grappler"
                   :page 167
                   :summary "restrain a creature you are grappling"})]
     :prereqs [(ability-prereq ::character/str 13)]})
   (feat-option
    {:name "Great Weapon Master"
     :icon "broadsword"
     :page 167
     :summary "When you critical or reduce a creature to 0 HPs with melee weapon, make one melee weapon attack as bonus action. When you melee Attack with heavy weapon, you can take -5 on attack to deal +10 damage."
     :modifiers [(modifiers/bonus-action
                  {:name "Great Weapon Master"
                   :page 167
                   :summary "When you critical or reduce a creature to 0 HPs with melee weapon, make one melee weapon attack"})]})
   (feat-option
    {:name "Healer"
     :icon "medical-pack-alt"
     :page 167
     :summary "When you stabilize with healer's kit, the creature regains 1 HP; use a healer's kit to restore 1d6 + 4 + creature's max hit dice HPs"
     :modifiers [(modifiers/action
                  {:name "Healer Feat"
                   :page 167
                   :summary "use a healer's kit to restore 1d6 + 4 + creature's max hit dice HPs"})]})
   (feat-option
    {:name "Heavily Armored"
     :icon "lamellar"
     :summary "increase STR by 1; proficiency in heavy armor"
     :page 167
     :modifiers [(modifiers/heavy-armor-proficiency)
                 (modifiers/ability ::character/str 1)]
     :prereqs [(armor-prereq :medium)]})
   (feat-option
    {:name "Heavy Armor Master"
     :icon "gauntlet"
     :page 167
     :summary "increase STR by 1; when wearing heavy armor, slashing, piercing, and bludgeoning damage from non-magical weapons is 3 less"
     :modifiers [(modifiers/ability ::character/str 1)]
     :prereqs [(armor-prereq :heavy)]})
   (feat-option
    {:name "Inspiring Leader"
     :icon "public-speaker"
     :page 167
     :summary "increase CHA by 1; give 6 friendly creatures within 30 ft. temp HPs equal to you CHA mod + your level"
     :prereqs [(ability-prereq ::character/cha 13)]})
   (feat-option
    {:name "Keen Mind"
     :icon "brain"
     :page 167
     :summary "increase INT by 1; always know which direction is north; know hours before sunset or sunrise; recall anything heard or seen within a month"
     :modifiers [(modifiers/ability ::character/int 1)]})
   (feat-option
    {:name "Lightly Armored"
     :icon "scale-mail"
     :page 167
     :summary "increase STR or DEX by 1; proficiency in light armor"
     :selections [(ability-increase-selection [::character/str ::character/dex] 1 false)]
     :modifiers [(modifiers/light-armor-proficiency)]})
   (feat-option
    {:name "Linguist"
     :icon "lips"
     :page 167
     :summary "increase INT by 1; learn 3 languages; create written ciphers"
     :selections [(language-selection languages 3)]
     :modifiers [(modifiers/ability ::character/int 1)]})
   (feat-option
    {:name "Lucky"
     :icon "clover"
     :page 167
     :summary "3 luck points, which you can use to roll an additional d20 when rolling an attack, save, or ability check, and choose which one to use"})
   (feat-option
    {:name "Mage Slayer"
     :icon "zeus-sword"
     :page 168
     :summary "use reaction to attack a caster within 5 ft.; impose disadvantage to a caster's concentration check when you attack; advantage on saves against spells cast within 5ft."
     :modifiers [(modifiers/reaction
                  {:name "Mage Slayer"
                   :range {:units :feet
                           :amount 5}
                   :page 168
                   :summary "attack a creature that casts a spell"})]})
   (feat-option
    {:name "Magic Initiate"
     :icon "magic-palm"
     :page 168
     :summary "gain 2 cantrips and 1 1st level spell from a chosen class"
     :selections [(t/selection-cfg
                   {:name "Spell Class"
                    :order 0
                    :tags #{:spells}
                    :options [(magic-initiate-option :bard "Bard" ::character/cha sl/spell-lists)
                              (magic-initiate-option :cleric "Cleric" ::character/wis sl/spell-lists)
                              (magic-initiate-option :druid "Druid" ::character/wis sl/spell-lists)
                              (magic-initiate-option :sorcerer "Sorcerer" ::character/cha sl/spell-lists)
                              (magic-initiate-option :warlock "Warlock" ::character/cha sl/spell-lists)
                              (magic-initiate-option :wizard "Wizard" ::character/int sl/spell-lists)]})]})
   (feat-option
    {:name "Martial Adept"
     :icon "visored-helm"
     :page 168
     :summary "learn two Battle Master martial maneuvers using 1 d6 superiority die"
     :selections [(t/selection-cfg
                   {:name "Martial Maneuvers"
                    :tags #{:class}
                    :options maneuver-options
                    :min 2
                    :max 2})]})
   (feat-option
    {:name "Medium Armor Master"
     :icon "bracers"
     :page 168
     :summary "medium armor doesn't give disadvantage to Stealth; max DEX bonus to AC is 3 for medium armor"
     :modifiers [(mods/modifier ?max-medium-armor-bonus 3)
                 (mods/fn-mod ?armor-stealth-disadvantage?
                              (fn [armor]
                                (if (= :medium (:type armor))
                                  false
                                  (?armor-stealth-disadvantage? armor))))]
     :prereqs [(armor-prereq :medium)]})
   (feat-option
    {:name "Mobile"
     :icon "move"
     :page 168
     :summary "speed increases by 10 ft.; Dash through difficult terrain doesn't cost extra movement; don't provoke opportunity attacks from a creature you made a melee attack against"
     :modifiers [(modifiers/speed 10)]})
   (feat-option
    {:name "Moderately Armored"
     :icon "shoulder-armor"
     :page 168
     :summary "increase STR or DEX by 1; gain proficiency with shields and medium armor"
     :selections [(ability-increase-selection [::character/str ::character/dex] 1 false)]
     :modifiers [(modifiers/medium-armor-proficiency)
                 (modifiers/shield-armor-proficiency)]
     :prereqs [(armor-prereq :light)]})
   (feat-option
    {:name "Mounted Combatant"
     :icon "cavalry"
     :page 168
     :summary "while mounted: advantage on attacks against unmounted creatures smaller than mount, force attack on mount to target you; mount takes no damage on sucessful DEX saves and half on failed"})
   (feat-option
    {:name "Observant"
     :icon "surrounded-eye"
     :page 168
     :summary "increase INT or WIS by 1; read lips; +5 bonus to passive Perception and passive Investigation"
     :selections [(ability-increase-selection [::character/int ::character/wis] 1 false)]
     :modifiers [(modifiers/passive-perception 5)
                 (modifiers/passive-investigation 5)]})
   (feat-option
    {:name "Polearm Master"
     :icon "halberd"
     :page 168
     :exclude-trait? true
     :summary "bonus attack with opposite end of quarterstaff, glaive, or halberd; opportunity attacks have the reach of glaive, pike, halberd, or quarterstaff"
     :modifiers [(modifiers/bonus-action
                  {:name "Polearm Master"
                   :page 168
                   :summary "when you make an Attack with a glaive, quarterstaff, or halberd, make an additionaal melee attack with the other end of the weapon, dealing d4 bludgeoning damage"})]})
   (feat-option
    {:name "Resilient"
     :icon "dodging"
     :page 168
     :summary "increase ability by 1 and gain proficiency in saves with that ability"
     :selections [(ability-increase-selection character/ability-keys 1 false [(fn [k] (modifiers/saving-throws nil k))])]})
   (feat-option
    {:name "Ritual Caster"
     :icon "gift-of-knowledge"
     :page 169
     :summary "choose a spellcaster class and learn 2 rituals from that class"
     :selections [(t/selection-cfg
                   {:name "Ritual Caster: Spell Class"
                    :tags #{:spells}
                    :options [(ritual-caster-option :bard "Bard" ::character/cha sl/spell-lists)
                              (ritual-caster-option :cleric "Cleric" ::character/wis sl/spell-lists)
                              (ritual-caster-option :druid "Druid" ::character/wis sl/spell-lists)
                              (ritual-caster-option :sorcerer "Sorcerer" ::character/cha sl/spell-lists)
                              (ritual-caster-option :warlock "Warlock" ::character/cha sl/spell-lists)
                              (ritual-caster-option :wizard "Wizard" ::character/int sl/spell-lists)]})]
     :prereqs [(t/option-prereq "Requires Intelligence or Wisdom 13 or higher"
                                (fn [c]
                                  (let [{:keys [::character/wis ::character/int] :as abilities} @(subscribe [::character/abilities nil c])]
                                    (or (and wis (>= wis 13))
                                        (and int (>= int 13))))))]})
   (feat-option
    {:name "Savage Attacker"
     :icon "saber-slash"
     :page 169
     :summary "reroll melee weapon attack damage and use either total"})
   (feat-option
    {:name "Sentinal"
     :icon "guards"
     :page 169
     :summary "reduce target's speed to 0 when you hit with opportunity attack; opportunity attacks even when target Disengages; use reaction to make a weapon attack against a creature within 5 ft. that attacks another target"})
   (feat-option
    {:name "Sharpshooter"
     :icon "bullseye"
     :page 170
     :summary "no disadvantage for long range; ignore half and 3/4 cover; take -5 to ranged attack to gain +10 on damage"})
   (feat-option
    {:name "Shield Master"
     :icon "attached-shield"
     :page 170
     :summary "when Attacking use bonus action to shove; add shield's AC bonus to saves that target just you; take no damage on a sucessful save"
     :modifiers [(modifiers/bonus-action
                  {:name "Shield Master: Shove"
                   :page 170
                   :summary "make a shove with shield when taking the Attack action"})]})
   (feat-option
    {:name "Skilled"
     :icon "juggler"
     :page 170
     :summary "proficiency in three skills and/or tools"
     :selections [(skilled-selection "Skill/Tool 1")
                  (skilled-selection "Skill/Tool 2")
                  (skilled-selection "Skill/tool 3")]})
   (feat-option
    {:name "Skulker"
     :icon "ghost-ally"
     :page 170
     :summary "hide when lightly obscured; when hiding, missing an attack doesn't reveal you; no disadvantage on Perception checks in dim light"
     :prereqs [(ability-prereq ::character/dex 13)]})
   (feat-option
    {:name "Spell Sniper"
     :icon "laser-precision"
     :page 170
     :summary "attack spells have double range; ignore half and 3/4 cover; learn a cantrip that requires an attack roll"
     :prereqs [can-cast-spell-prereq]
     :selections [(t/selection-cfg
                   {:name "Spell Sniper: Spell Class"
                    :tags #{:spells}
                    :options [(spell-sniper-option :bard "Bard" ::character/cha sl/spell-lists)
                              (spell-sniper-option :cleric "Cleric" ::character/wis sl/spell-lists)
                              (spell-sniper-option :druid "Druid" ::character/wis sl/spell-lists)
                              (spell-sniper-option :sorcerer "Sorcerer" ::character/cha sl/spell-lists)
                              (spell-sniper-option :warlock "Warlock" ::character/cha sl/spell-lists)
                              (spell-sniper-option :wizard "Wizard" ::character/int sl/spell-lists)]})]})
   (feat-option
    {:name "Tavern Brawler"
     :icon "broken-bottle"
     :page 170
     :summary "increase STR or CON by 1; improvised weapon proficiency; d4 damage on unarmed strike; grapple as bonus action"
     :selections [(ability-increase-selection [::character/str ::character/con] 1 false)]
     :modifiers [(modifiers/weapon-proficiency :improvised)
                 (modifiers/bonus-action
                  {:name "Tavern Brawler: Grapple"
                   :page 170
                   :summary "attempt grapple when you hit with improvised weapon or unarmed strike"})]})
   (feat-option
    {:name "Tough"
     :icon "defensive-wall"
     :page 170
     :summary "2 extra HPs per level"
     :modifiers [(mods/modifier ?hit-point-level-bonus (+ 2 ?hit-point-level-bonus))]})
   (feat-option
    {:name "War Caster"
     :icon "deadly-strike"
     :page 170
     :summary "adv. on CON saves for spell concentration; somatic components with weapons or shield in hand; cast spell as opporunity attack"
     :prereqs [can-cast-spell-prereq]})
   (feat-option
    {:name "Weapon Master"
     :icon "sword-slice"
     :page 170
     :summary "increase STR or DEX by 1; proficiency with 4 weapons"
     :selections [(ability-increase-selection [::character/str ::character/dex] 1 false)
                  (weapon-proficiency-selection 4)]})])

(def fighting-style-options
  [(t/option-cfg
    {:name "Archery"
     :modifiers [(modifiers/ranged-attack-bonus 2)
      (modifiers/trait-cfg
       {:name "Archery Fighting Style"
        :page 72
        :description "You gain a +2 bonus to attack rolls you make with ranged weapons."})]})
   (t/option-cfg
    {:name "Defense"
     :modifiers [(modifiers/armored-ac-bonus 1)
      (modifiers/trait-cfg
       {:name "Defense Fighting Style"
        :page 72
        :description "While you are wearing armor, you gain a +1 bonus to AC."})]})
   (t/option-cfg
    {:name "Dueling"
     :modifiers [(modifiers/trait-cfg
       {:name "Dueling Fighting Style"
        :page 72
        :description "When you are wielding a melee weapon in one hand and no other weapons, you gain a +2 bonus to damage rolls with that weapon."})]})
   (t/option-cfg
    {:name "Great Weapon Fighting"
     :modifiers [(modifiers/trait-cfg
       {:name "Great Weapon Fighting Style"
        :page 72
        :description "When you roll a 1 or 2 on a damage die for an attack you make with a melee weapon that you are wielding with two hands, you can reroll the die and must use the new roll, even if the new roll is a 1 or a 2. The weapon must have the two-handed or versatile property for you to gain this benefit."})]})
   (t/option-cfg
    {:name "Protection"
     :modifiers [(modifiers/reaction
       {:name "Protection Fighting Style"
        :page 72
        :description "When a creature you can see attacks a target other than you that is within 5 feet of you, you can use your reaction to impose disadvantage on the attack roll. You must be wielding a shield."})]})
   (t/option-cfg
    {:name"Two Weapon Fighting"
     :modifiers [(modifiers/trait-cfg
       {:name "Two Weapon Fighting"
        :description "When you engage in two-weapon fighting, you can add your ability modifier to the damage of the second attack."})]})])

(defn fighting-style-selection-2 [class-kw num options]
  (t/selection-cfg
   {:name "Fighting Style"
    :tags #{:class}
    :ref [:class class-kw :fighting-style]
    :multiselect? true
    :min num
    :max num
    :options options}))

(defn fighting-style-selection [class-kw & [restrictions additional-options]]
  (fighting-style-selection-2
   class-kw
   1
   (if restrictions
     (filter
      (fn [o]
        (restrictions (::t/key o)))
      fighting-style-options)
     fighting-style-options)))

(defn feat-selection [num]
  (t/selection-cfg
   {:name "Feats"
    :options feat-options
    :multiselect? true
    :tags #{:feats}
    :ref [:feats]
    :show-if-zero? true
    :min num
    :max num}))

(defn ability-score-improvement-selection [cls lvl]
  (t/selection-cfg
   {:name "Ability Score Improvement or Feat"
    :key :asi-or-feat
    :tags #{:ability-scores}
    :options [(ability-increase-option 2 false character/ability-keys)
              (t/option-cfg
               {:name "Feat"
                :selections [(feat-selection 1)]})]}))

(defn expertise-selection [num & [key]]
  (t/selection-cfg
   {:name "Skill Expertise"
    :key (or key :skill-expertise)
    :order 2
    :options (map
              (fn [{:keys [name key icon]}]
                (t/option-cfg
                 {:name name
                  :key key
                  :icon icon
                  :modifiers [(modifiers/skill-expertise key)]
                  :prereqs [(t/option-prereq (str "Requires proficiency in " name)
                                             (fn [built-char]
                                               (let [skill-profs @(subscribe [::character/skill-profs nil built-char])]
                                                 (and skill-profs (skill-profs key)))))]}))
              skills/skills)
    :min num
    :max num
    :multiselect? true
    :ref [:skill-expertise]
    :tags #{:profs :expertise}}))

(def rogue-expertise-selection
  (t/selection-cfg
   {:name "Expertise"
    :tags #{:profs :skill-profs :expertise}
    :order 1
    :options [(t/option-cfg
               {:name "Two Skills"
                :selections [(expertise-selection 2 :two-skills)]})
              (t/option-cfg
               {:name "One Skill/Theives Tools"
                :selections [(expertise-selection 1 :one-skill-thieves-tools)]
                :modifiers [(modifiers/tool-proficiency :thieves-tools)
                            (modifiers/tool-expertise :thieves-tools)]})]}))

(defn cleric-spell [spell-level spell-key min-level]
  (modifiers/spells-known spell-level spell-key ::character/wis "Cleric" min-level "Domain" :cleric))


(defn potent-spellcasting [page & [source]]
  (modifiers/dependent-trait
   {:level 8
    :page page
    :source source
    :summary (str "Add "
                  (common/bonus-str (?ability-bonuses ::character/wis))
                  " to damage from cantrips you cast")
    :name "Potent Spellcasting"}))

(def monk-base-cfg
  {:name "Monk"
   :subclass-level 3
   :subclass-title "Monastic Tradition"})

(def paladin-base-cfg
  {:name "Paladin"
   :subclass-level 3
   :subclass-title "Sacred Oath"})

(def ua-al-illegal (modifiers/al-illegal "Unearthed Arcana options are not allowed"))

(defn subclass-plugin [class-base-cfg source subclasses ua-al-illegal?]
  (merge
   class-base-cfg
   {:source source
    :plugin? true
    :subclasses (if ua-al-illegal?
                  (map
                   (fn [subclass]
                     (update subclass :modifiers conj ua-al-illegal))
                   subclasses)
                  subclasses)}))

(defn paladin-spell [spell-level key min-level]
  (modifiers/spells-known spell-level key ::character/cha "Paladin" min-level nil :paladin))

(defn subclass-spell-selection [class-key class-name ability spells num]
  (spell-selection
   {:class-key class-key
    :spell-keys spells
    :spellcasting-ability ability
    :class-name class-name
    :num num
    :prepend-level? true}))

(defn subclass-cantrip-selection [class-key class-name ability spells num]
  (spell-selection
   {:class-key class-key
    :level 0
    :spellcasting-ability ability
    :class-name class-name
    :spell-keys spells
    :num num}))

(defn warlock-subclass-spell-selection [spells]
  (subclass-spell-selection :warlock "Warlock" ::character/cha spells 0))

(defn traits-modifiers [traits & [class-key source]]
  (map
   (fn [trait]
     (modifiers/trait-cfg (merge {:source source
                                  :class-key class-key}
                                 trait)))
   traits))

(defn armor-prof-modifiers [armor-proficiencies & [cls-kw]]
  (map
   (fn [armor-prof]
     (let [[armor-kw first-class?] (if (keyword? armor-prof) [armor-prof false] armor-prof)]
       (modifiers/armor-proficiency armor-kw first-class? cls-kw)))
   armor-proficiencies))

(defn tool-prof-modifiers [tool-proficiencies & [cls-kw]]
  (map
   (fn [tool-prof]
     (let [[tool-kw first-class?] (if (keyword? tool-prof) [tool-prof false] tool-prof)]
       (modifiers/tool-proficiency tool-kw first-class? cls-kw)))
   tool-proficiencies))

(defn weapon-prof-modifiers [weapon-proficiencies & [cls-kw]]
  (map
   (fn [weapon-prof]
     (let [[weapon-kw first-class?] (if (keyword? weapon-prof) [weapon-prof false] weapon-prof)]
       (if (#{:simple :martial} weapon-kw)
         (modifiers/weapon-proficiency weapon-kw first-class? cls-kw)
         (modifiers/weapon-proficiency weapon-kw first-class? cls-kw))))
   weapon-proficiencies))


(defn subrace-option [source
                      {:keys [name
                              abilities
                              size
                              speed
                              subrace-options
                              armor-proficiencies
                              weapon-proficiencies
                              modifiers
                              selections
                              traits
                              source]}]
  (t/option-cfg
   {:name name
    :selections selections
    :modifiers (concat
                [(modifiers/subrace name)]
                modifiers
                (armor-prof-modifiers armor-proficiencies)
                (weapon-prof-modifiers weapon-proficiencies)
                (map
                 (fn [[k v]]
                   (modifiers/subrace-ability k v))
                 abilities)
                (traits-modifiers traits nil source)
                (if source [(modifiers/used-resource source name)]))}))

(defn ability-modifiers [abilities]
  (map
   (fn [[k v]]
     (modifiers/ability k v))
   abilities))

(defn darkvision-modifiers [range]
  [(modifiers/darkvision range)])

(defn feat-selection-2 [cfg]
  (t/selection-cfg
   (merge
    {:name "Feats"
     :ref [:feats]
     :show-if-zero? true
     :tags #{:feats}
     :multiselect? true}
    cfg)))

(def homebrew-spell-selection
  (spell-selection
   {:class-key :homebrew
    :class-name "Homebrew"
    :ref [:optional-content :homebrew :spells-known]
    :min 0
    :max nil
    :spell-keys (keys spells/spell-map)}))

(def homebrew-tool-prof-selection
  (tool-proficiency-selection-2
   {:min 0
    :max nil
    :multiselect? true
    :ref [:tool-profs]
    :options (tool-options equipment/tools)}))

(def homebrew-skill-prof-selection
  (skill-selection-2 {:min 0
                      :max nil
                      :options (map :key skills/skills)}))

(def homebrew-ability-increase-selection
  (ability-increase-selection-2
   {:min 0}))

(def homebrew-feat-selection
  (feat-selection-2
   {:min 0
    :max nil
    :options feat-options}))

(def homebrew-al-illegal
  (modifiers/al-illegal "Homebrew options are not allowed"))

(defn none-option [path]
  (t/option-cfg
   {:name "<none>"
    :key :none
    :order 1001
    :prereqs [(t/option-prereq
               nil
               (fn [_] @(subscribe [:homebrew? path]))
               true)]}))

(defn custom-option-builder [name-sub name-event]
  [:div.m-t-10
   [:span "Name"]
   [:input.input
    {:value @(subscribe name-sub)
     :on-change (fn [e] (dispatch (conj name-event (.. e -target -value))))}]])

(defn custom-subrace-builder []
  (custom-option-builder
   [:custom-subrace-name]
   [:set-custom-subrace]))

(defn custom-subrace-option [path]
  (t/option-cfg
   {:name "Custom"
    :icon "beer-stein"
    :ui-fn custom-subrace-builder
    :help "Homebrew subrace. This allows you to use a subrace that is not on the list. This will allow unrestricted access to skill and tool proficiencies, racial ability increases, and feats."
    :modifiers [(modifiers/deferred-subrace)
                homebrew-al-illegal]
    :order 1000
    :prereqs [(t/option-prereq
               nil
               (fn [_] @(subscribe [:homebrew? path]))
               true)]
    :selections [homebrew-skill-prof-selection
                 homebrew-tool-prof-selection
                 homebrew-ability-increase-selection
                 homebrew-feat-selection]}))

(defn custom-race-builder []
  (custom-option-builder
   [:custom-race-name]
   [:set-custom-race]))

(defn subrace-selection [source subraces path]
  (let [subrace-path (conj path :subrace)]
    (t/selection-cfg
     {:name "Subrace"
      :tags #{:subrace}
      :options (conj
                (if (seq subraces)
                  (map
                   (partial subrace-option source)
                   (if source
                     (map (fn [sr] (assoc sr :source source)) subraces)
                     subraces))
                  [(none-option subrace-path)])
                (custom-subrace-option subrace-path))})))

(def custom-race-option
  (t/option-cfg
   {:name "Custom"
    :icon "beer-stein"
    :ui-fn custom-race-builder
    :help "Homebrew race. This allows you to use a race that is not on the list. This will allow unrestricted access to skill and tool proficiencies, racial ability increases, and feats."
    :modifiers [(modifiers/deferred-race)
                homebrew-al-illegal]
    :prereqs [(t/option-prereq
               nil
               (fn [_] @(subscribe [:homebrew? [:race]]))
               true)]
    :order 1000
    :selections [(subrace-selection nil nil [:race :custom])
                 homebrew-skill-prof-selection
                 homebrew-tool-prof-selection
                 homebrew-ability-increase-selection
                 homebrew-feat-selection]}))

(defn custom-background-builder []
  (custom-option-builder
   [:custom-background-name]
   [:set-custom-background]))

(def custom-background-option
  (t/option-cfg
   {:name "Custom"
    :ui-fn custom-background-builder
    :order 1000
    :modifiers [(modifiers/deferred-background)]
    :selections [(skill-selection 2)
                 (t/selection-cfg
                  {:name "Tool / Language Proficiencies"
                   :tags #{:profs}
                   :options [(t/option-cfg
                              {:name "Two Tools"
                               :selections [(tool-selection 2)]})
                             (t/option-cfg
                              {:name "One Tool / One Language"
                               :selections [(tool-selection 1)
                                            (any-language-selection 1)]})
                             (t/option-cfg
                              {:name "Two Languages"
                               :selections [(any-language-selection 2)]})]})]}))

(defn race-option [{:keys [name
                           icon
                           key
                           help
                           abilities
                           size
                           speed
                           darkvision
                           subraces
                           modifiers
                           selections
                           traits
                           source
                           languages
                           language-options
                           armor-proficiencies
                           weapon-proficiencies
                           tool-proficiencies
                           source
                           plugin?]}]
  (let [key (or key (common/name-to-kw name))]
    (t/option-cfg
     {:name name
      :icon icon
      :key key
      :help help
      :selections (concat
                   (if (seq subraces)
                     [(subrace-selection source subraces [:race key])])
                   (if (seq language-options) [(language-selection language-options)])
                   selections)
      :modifiers (concat
                  (if (not plugin?)
                    (remove
                     nil?
                     [(modifiers/race name)
                      (if size (modifiers/size size))
                      (if speed (modifiers/speed speed))]))
                  (if darkvision
                    (darkvision-modifiers darkvision))
                  (map
                   (fn [language]
                     (modifiers/language (common/name-to-kw language)))
                   languages)
                  (map
                   (fn [[k v]]
                     (modifiers/race-ability k v))
                   abilities)
                  modifiers
                  (tool-prof-modifiers tool-proficiencies)
                  (traits-modifiers traits nil source)
                  (armor-prof-modifiers armor-proficiencies)
                  (weapon-prof-modifiers weapon-proficiencies)
                  (if source [(modifiers/used-resource source name)]))})))

(defn add-sources [source background]
  (-> background
      (assoc :source source)
      (update :traits (fn [traits] (map (fn [t] (assoc t :source source)) traits)))))

(def artisans-tools-choice-cfg
  {:name "Artisan's Tool"
   :options (zipmap (map :key equipment/artisans-tools) (repeat 1))})

(defn class-starting-equipment-entity-options [key items]
  (eh/starting-equipment-entity-options ::char-equip/class-starting-equipment? key items))

(defn tool-prof-selection-aux [tool num & [key prereq-fn]]
  (t/selection-cfg
   {:name (str "Tool Proficiency: " (:name tool))
    :key (if key (keyword (str (name key) "--" (common/name-to-kw (:name tool)))))
    :help (str "Select " (s/lower-case (:name tool)) " for which you are proficient.")
    :options (map
              (fn [{:keys [name key icon]}]
                (t/option-cfg
                 {:name name
                  :key key
                  :icon icon
                  :modifiers [(modifiers/tool-proficiency key)]}))
              (:values tool))
    :min num
    :max num
    :prereq-fn prereq-fn
    :tags #{:tool-profs :profs}}))

(defn tool-prof-selection [tool-options & [key prereq-fn]]
  (let [[first-key first-num] (-> tool-options first)
        first-option (equipment/tools-map first-key)]
    (if (and (= 1 (count tool-options))
             (seq (:values first-option)))
      (tool-prof-selection-aux first-option first-num key prereq-fn)
      (t/selection-cfg
       {:name "Tool Proficiencies"
        :key key
        :options (map
                  (fn [[k num]]
                    (let [tool (equipment/tools-map k)]
                      (if (:values tool)
                        (t/option-cfg
                         {:name (:name tool)
                          :selections [(tool-prof-selection-aux tool num key prereq-fn)]})
                        (t/option-cfg
                         {:name (:name tool)
                          :key (:key tool)
                          :icon (:icon tool)
                          :modifiers [(modifiers/tool-proficiency (:key tool))]}))))
                  tool-options)
        :prereq-fn prereq-fn
        :tags #{:profs :tool-profs}}))))

(defn first-class? [class-kw & [classes]]
  (fn [c] (= class-kw (first (or classes @(subscribe [::character/classes nil c]))))))

(defn new-starting-equipment-selection [class-kw {:keys [name options] :as cfg}]
  (t/selection-cfg
   (merge
    cfg
    {:name (str "Starting Equipment: " name)
     :tags #{:equipment :starting-equipment}
     :order 1
     :options (conj options
                    (t/option-cfg
                     {:name "<none>"
                      :key :none}))
     :prereq-fn (if class-kw (first-class? class-kw))})))

(defn simple-weapon-selection [num class-kw]
  (new-starting-equipment-selection
   class-kw
   {:name "Simple Weapon"
    :tags #{:starting-equipment}
    :options (weapon-options (weapons/simple-weapons weapons/weapons))
    :min num
    :max num
    :prereq-fn (first-class? class-kw)}))

(defn weapon-option-2 [class-kw [k num]]
  (case k
    :simple (t/option-cfg
             {:name "Any Simple Weapon"
              :selections [(simple-weapon-selection num class-kw)]})
    :martial (t/option-cfg
              {:name "Any Martial Weapon"
               :selections [(new-starting-equipment-selection
                             class-kw
                             {:name "Martial Weapon"
                              :options (weapon-options (weapons/martial-weapons weapons/weapons))
                              :min num
                              :max num})]})
    (t/option-cfg
     {:name (-> k weapons/weapons-map :name (str (if (> num 1) (str " (" num ")") "")))
      :modifiers [(modifiers/weapon k num)]})))

(defn class-options [class-kw option-fn choices help]
  (map
   (fn [{:keys [name options]}]
     (new-starting-equipment-selection
      class-kw
      {:name name
       :help help
       :options (map
                 option-fn
                 options)}))
   choices))

(defn class-weapon-options [weapon-choices class-kw]
  (class-options class-kw (partial weapon-option-2 class-kw) weapon-choices "Select a weapon to begin your adventuring career with."))

(defn armor-option [[k num]]
  (t/option-cfg
     {:name (-> k armor/armor-map :name)
      :modifiers [(modifiers/armor k num)]}))

(defn class-armor-options [armor-choices class-kw]
  (class-options class-kw armor-option armor-choices "Select armor to begin your adventuring career with."))

(defn equipment-option [class-kw [k num]]
  (let [equipment (equipment/equipment-map k)]
    (if (:values equipment)
      (t/option-cfg
       {:name (:name equipment)
        :selections [(t/selection-cfg
                      {:name (:name equipment)
                       :tags #{:equipment :starting-equipment}
                       :options (map
                                 #(equipment-option class-kw %)
                                 (zipmap (map :key (:values equipment)) (repeat num)))
                       :prereq-fn (first-class? class-kw)})]})
      (t/option-cfg
       {:name (-> equipment :name (str (if (> num 1) (str " (" num ")") "")))
        :modifiers (if (:items equipment)
                     (map
                      (fn [[kw num]]
                        (modifiers/equipment kw num))
                      (:items equipment))
                     [(modifiers/equipment k num)])}))))

(defn class-equipment-options [equipment-choices class-kw]
  (class-options class-kw (partial equipment-option class-kw) equipment-choices "Select equipment to start your adventuring career with."))

(defn background-skills-cfg [background-nm skill-kws]
  {:modifiers (map
               (fn [skill-kw]
                 (modifiers/skill-proficiency skill-kw
                                              background-nm
                                              [(not (get ?skill-profs skill-kw))]))
               skill-kws)
   :selections (map
                (fn [skill-kw]
                  (skill-selection (map :key skills/skills)
                                   1
                                   0
                                   nil
                                   (fn [c]
                                     (let [skill-profs @(subscribe [::character/skill-profs nil c])
                                           skill-sources (get skill-profs skill-kw)
                                           passes? (and skill-sources
                                                        (not (skill-sources background-nm)))]
                                       passes?))))
                skill-kws)})

(defn background-option [{:keys [name
                                 help
                                 page
                                 profs
                                 selections
                                 modifiers
                                 weapon-choices
                                 weapons
                                 equipment
                                 custom-equipment
                                 equipment-choices
                                 armor
                                 armor-choices
                                 treasure
                                 custom-treasure
                                 traits
                                 source]
                          :as background}]
  (let [kw (common/name-to-kw name)
        {:keys [skill skill-options tool-options tool language-options]
         armor-profs :armor weapon-profs :weapon} profs
        {skill-num :choose options :options} skill-options
        skill-kws (if (:any options) (map :key skills/skills) (keys options))]
    (t/option-cfg
     (merge-with
      concat
      (background-skills-cfg name (keys skill))
      {:name name
       :key kw
       :help help
       :page page
       :select-fn (fn [_ _]
                    (dispatch [:add-background-starting-equipment background]))
       :selections (concat
                    selections
                    (if (seq tool-options) [(tool-prof-selection tool-options)])
                    (class-weapon-options weapon-choices nil)
                    (class-armor-options armor-choices nil)
                    (class-equipment-options equipment-choices nil)
                    (if (seq skill-kws) [(skill-selection skill-kws skill-num)])
                    (if (seq language-options) [(language-selection language-options)]))
       :modifiers (concat
                   [(modifiers/background name)]
                   (traits-modifiers traits)
                   modifiers
                   (armor-prof-modifiers (keys armor-profs))
                   (weapon-prof-modifiers (keys weapon-profs))
                   (tool-prof-modifiers (keys tool)))}))))

(defn total-levels-prereq [level & [class-key]]
  (fn [c] (>= (if class-key
                (@(subscribe [::character/class-level-fn nil c]) class-key)
                @(subscribe [::character/total-levels nil c]))
              level)))

(defn total-levels-prereq-2 [level & [class-key]]
  (fn [c]
    (and c
         (character/class-level-fn c)
         level
         (>= (or (if class-key
                   ((character/class-level-fn c) class-key)
                   (character/total-levels c))
                 0)
             (or level 0)))))


(defn total-levels-option-prereq [level & [class-key]]
  (t/option-prereq
   (str "You must have at least " level " " (name class-key) " levels")
   (total-levels-prereq level class-key)))

(defn add-mod-total-levels-prereq [lvl cls modifier]
  (if (sequential? modifier)
    (map
     add-mod-total-levels-prereq
     modifier)
    (update
     modifier
     ::mods/conditions
     conj
     (total-levels-prereq-2 lvl (:key cls)))))

(defn subclass-option [cls
                       {:keys [name
                               source
                               profs
                               selections
                               spellcasting
                               modifiers
                               level-modifiers
                               traits
                               prereqs
                               levels]
                        :as subcls}]
  (let [kw (common/name-to-kw name)
        {:keys [armor weapon save skill-options tool-options tool language-options]} profs
        {skill-num :choose options :options} skill-options
        {level-factor :level-factor} spellcasting
        skill-kws (if (:any options) (map :key skills/skills) (keys options))
        armor-profs (keys armor)
        weapon-profs (keys weapon)
        tool-profs (keys tool)
        spellcasting-template (spellcasting-template
                               (assoc
                                spellcasting
                                :class-key
                                (or (:spell-list spellcasting) kw))
                               subcls)
        spell-selections (mapcat
                          (fn [[lvl selections]]
                            (map
                             (fn [selection]
                               (assoc selection ::t/prereq-fn (fn [c] (let [total-levels @(subscribe [::character/total-levels nil c])]
                                                                        (>= lvl total-levels)))))
                             selections))
                          (:selections spellcasting-template))
        level-selections (mapcat
                          (fn [[lvl {selections :selections}]]
                            (map
                             (fn [selection]
                               (assoc
                                selection
                                ::t/prereq-fn
                                (total-levels-prereq lvl (:key cls))))
                             selections))
                          levels)
        level-modifiers (mapcat
                         (fn [[lvl {modifiers :modifiers}]]
                           (map
                            (partial add-mod-total-levels-prereq lvl cls)
                            modifiers))
                         levels)]
    (t/option-cfg
     {:name name
      :prereqs prereqs
      :selections (map
                   (fn [selection]
                     (update selection ::t/tags sets/union #{(:key cls) kw}))
                   (concat
                    selections
                    level-selections
                    spell-selections
                    (if (seq tool-options) [(tool-prof-selection tool-options)])
                    (if (seq skill-kws) [(skill-selection skill-kws skill-num)])
                    (if (seq language-options) [(language-selection language-options)])))
      :modifiers (concat
                  modifiers
                  level-modifiers
                  [(modifiers/subclass (:key cls) kw)
                   (modifiers/subclass-name (:key cls) name)]
                  (if (:known-mode spellcasting)
                    [(modifiers/spells-known-mode name (:known-mode spellcasting))])
                  (armor-prof-modifiers armor-profs)
                  (weapon-prof-modifiers weapon-profs)
                  (tool-prof-modifiers tool-profs)
                  (traits-modifiers traits (:key cls))
                  (if level-factor [(modifiers/spell-slot-factor (:key cls) level-factor)])
                  (if source [(modifiers/used-resource source name)]))})))

(defn level-key [index]
  (keyword (str "level-" index)))

(defn level-name [index]
  (str "Level " index))

(defn subclass-level-option [{:keys [name
                                     levels] :as subcls}
                             kw
                             spellcasting-template
                             i]
  (let [selections (some-> levels (get i) :selections)]
    (t/option-cfg
     {:name (level-name i)
      :key (level-key i)
      :order i
      :selections (concat
                   selections      
                   (some-> spellcasting-template :selections (get i)))
      :modifiers (some-> levels (get i) :modifiers)})))

(defn al-illegal-hit-points-mod [reason]
  (modifiers/al-illegal (str reason " The only legal option is 'Average'.")))

(defn hit-points-selection [die class-nm level]
  (t/selection-cfg
   {:name (str "Hit Points: " class-nm " " level)
    :key :hit-points
    :require-value? true
    :help "Select the method with which to determine this level's hit points."
    :tags #{:class}
    :options [{::t/name "Manual Entry"
               ::t/key :manual-entry
               ::t/help "This option allows you to manually type in the value for this level's hit points. Use this if you want to roll dice yourself or if you already have a character with known hit points for this level."
               ::t/modifiers [(modifiers/deferred-max-hit-points)
                              (al-illegal-hit-points-mod "Manual entry for hit points is not legal.")]}
              {::t/name (str "Roll (1D" die ")")
               ::t/key :roll
               ::t/help "This option rolls virtual dice for you and sets that value for this level's hit points. It could pay off with a high roll, but you might also roll a 1."
               ::t/modifiers [(modifiers/deferred-max-hit-points)
                              (al-illegal-hit-points-mod "Rolling for hit points is not legal.")]}
              (let [average (dice/die-mean die)]
                (t/option-cfg
                 {:name "Average"
                  :key :average
                  :help (str "This option just gives you the average value (" average ") for the die roll (1D" die "). Choose this option if you're not feeling lucky.")
                  :modifiers [(modifiers/max-hit-points average)]}))]}))

(defn custom-subclass-builder [path]
  (custom-option-builder
   [:custom-subclass-name path]
   [:set-custom-subclass path]))

(defn custom-subclass-option [cls-key level-key subclass-selection-key]
  (let [path [:class cls-key :levels level-key subclass-selection-key]]
    (t/option-cfg
     {:name "Custom"
      :icon "beer-stein"
      :ui-fn #(custom-subclass-builder path)
      :help "Homebrew subclass. This allows you to use a subclass that is not on the list. This will allow unrestricted access to skill and tool proficiencies and feats."
      :prereqs [(t/option-prereq
                 nil
                 (fn [_] @(subscribe [:homebrew? path]))
                 true)]
      :order 1000
      :modifiers [(modifiers/deferred-subclass-name cls-key)
                  homebrew-al-illegal]
      :selections [homebrew-skill-prof-selection
                   homebrew-tool-prof-selection
                   homebrew-feat-selection]})))

(defn level-option [{:keys [name
                            plugin?
                            hit-die
                            profs
                            levels
                            traits
                            spellcasting
                            ability-increase-levels
                            subclass-title
                            subclass-help
                            subclass-level
                            subclasses
                            source] :as cls}
                    kw
                    spellcasting-template
                    i]
  (let [ability-inc-set (set ability-increase-levels)
        level-kw (level-key i)]
    (t/option-cfg
     {:name (level-name i)
      :key level-kw
      :order i
      :selections (map
                   (fn [selection]
                     (update selection ::t/tags sets/union #{:level level-kw}))
                   (concat
                    (some-> levels (get i) :selections)
                    (some-> spellcasting-template :selections (get i))
                    (if (= i subclass-level)
                      (let [subclass-selection-key (common/name-to-kw subclass-title)]
                        [(t/selection-cfg
                          {:name subclass-title
                           :key subclass-selection-key
                           :help subclass-help
                           :tags #{:subclass}
                           :order 2
                           :options (conj
                                     (map
                                      #(subclass-option (assoc cls :key kw) %)
                                      (if source (map (fn [sc] (assoc sc :source source)) subclasses) subclasses))
                                     (custom-subclass-option kw level-kw subclass-selection-key))})]))
                    (if (and (not plugin?) (ability-inc-set i))
                      [(ability-score-improvement-selection name i)])
                    (if (not plugin?)
                      [(assoc
                        (hit-points-selection hit-die name i)
                        ::t/prereq-fn
                        (fn [c] (or (not (= kw (first @(subscribe [::character/classes nil c]))))
                                    (> i 1))))])))
      :modifiers (concat
                  (some-> levels (get i) :modifiers)
                  (traits-modifiers
                   (filter
                    (fn [{level :level :or {level 1}}]
                      (= level i))
                    traits)
                   kw)
                  (if (and (not plugin?)
                           (= i 1))
                    [(mods/cum-sum-mod
                      ?hit-point-level-increases
                      hit-die
                      nil
                      nil
                      [(= kw (first ?classes))])])
                  (if (not plugin?)
                    [(modifiers/level kw name i hit-die)]))})))



(defn class-skill-selection [{skill-num :choose options :options skill-select-order :order} key prereq-fn]
  (let [skill-kws (if (:any options) (map :key skills/skills) (keys options))]
    (skill-selection skill-kws skill-num skill-select-order key prereq-fn)))

(defn class-help-field [name value]
  [:div.m-t-5
    [:span.f-w-b (str name ":")]
   [:span.m-l-10 value]])


(defn class-help [hd saves weapon-profs armor-profs]
  [:div
   (class-help-field "Hit Die" (str "d" hd))
   (class-help-field "Saving Throw Proficiencies" (s/join ", " (map (comp s/upper-case name) saves)))
   (class-help-field "Weapon Proficiencies" (s/join ", " (map (comp name key) weapon-profs)))
   (class-help-field "Armor Proficiencies" (s/join ", " (map (comp name key) armor-profs)))])

(defn class-option [{:keys [name
                            key
                            help
                            hit-die
                            plugin?
                            profs
                            levels
                            ability-increase-levels
                            subclass-title
                            subclass-level
                            subclasses
                            selections
                            modifiers
                            source
                            weapon-choices
                            weapons
                            equipment
                            equipment-choices
                            armor
                            armor-choices
                            spellcasting
                            multiclass-prereqs]
                     :as cls}]
  (let [kw (or key (common/name-to-kw name))
        {:keys [save skill-options multiclass-skill-options tool-options multiclass-tool-options tool]
         armor-profs :armor weapon-profs :weapon} profs
        {level-factor :level-factor} spellcasting
        save-profs (keys save)
        spellcasting-template (spellcasting-template (assoc spellcasting :class-key kw) cls)
        first-class? (fn [c] (let [first-class (first @(subscribe [::character/classes nil c]))]
                               (= kw first-class)))]
    (t/option-cfg
     {:name name
      :key kw
      :help [:div.p-t-5.p-l-10.p-r-10
             (class-help hit-die save-profs weapon-profs armor-profs)
             [:div.m-t-10 help]]
      :prereqs multiclass-prereqs
      :selections (map
                   (fn [selection]
                     (update selection ::t/tags sets/union #{kw}))
                   (concat
                    selections
                    (if (seq tool-options)
                      [(tool-prof-selection tool-options :tool-selection first-class?)])
                    (if (seq multiclass-tool-options)
                      [(tool-prof-selection multiclass-tool-options :multiclass-tool-selection (fn [c] (not= kw (first (:classes c)))))])
                    (if weapon-choices (class-weapon-options weapon-choices kw))
                    (if armor-choices (class-armor-options armor-choices kw))
                    (if equipment-choices (class-equipment-options equipment-choices kw))
                    (if skill-options
                      [(class-skill-selection skill-options :skill-proficiency first-class?)])
                    (if multiclass-skill-options
                      [(class-skill-selection multiclass-skill-options :multiclass-skill-proficiency (complement first-class?))])
                    [(t/selection-cfg
                      {:name (str name " Levels")
                       :key :levels
                       :help "These are your levels in the containing class. You can add levels by clicking the 'Add Levels' button below."
                       :new-item-text "Level Up (Add a Level)"
                       :new-item-fn (fn [selection options current-values]
                                      {::entity/key (-> current-values count inc level-key)})
                       :tags #{kw}
                       :options (map
                                 (partial level-option cls kw spellcasting-template)
                                 (range 1 21))
                       :min 1
                       :sequential? true
                       :multiselect? true
                       :max nil})]))
      :associated-options (remove
                           nil?
                           [(class-starting-equipment-entity-options :weapons weapons)
                            (class-starting-equipment-entity-options :armor armor)
                            (class-starting-equipment-entity-options :equipment equipment)])
      :modifiers (concat
                  modifiers
                  (if (= :all (:known-mode spellcasting))
                    (let [spell-list (sl/spell-lists kw)]
                      (mapcat
                       (fn [[lvl spell-keys]]
                         (map
                          (fn [spell-key]
                            (modifiers/spells-known-cfg lvl
                                                        {:class-key kw
                                                         :key spell-key
                                                         :class name
                                                         :ability (:ability spellcasting)}
                                                        1
                                                        [(let [slots (?class-spell-slots kw)]
                                                           (slots lvl))]))
                          spell-keys))
                       spell-list)))
                  (if armor-profs (armor-prof-modifiers armor-profs kw))
                  (if weapon-profs (weapon-prof-modifiers weapon-profs kw))
                  (if tool (tool-prof-modifiers tool kw))
                  (if level-factor [(modifiers/spell-slot-factor kw level-factor)])
                  (if (and source (not plugin?))
                    [(modifiers/used-resource source name)])
                  (if (:known-mode spellcasting)
                    [(modifiers/spells-known-mode name (:known-mode spellcasting))])
                  (remove
                   nil?
                   [(modifiers/cls kw)
                    (if save-profs (apply modifiers/saving-throws kw save-profs))]))})))

(defn source-url [source]
  (some-> source disp/sources :url))

(defn spell-level [spell-kw]
  (some-> spells/spell-map spell-kw :level))

(def ranger-base-cfg
  {:name "Ranger"
   :subclass-level 3
   :subclass-title "Ranger Archetype"})

(defn background-selection [cfg]
  (t/selection-cfg
   (merge
    {:name "Background"
     :tags #{:background}}
    cfg)))

(defn class-selection [cfg]
  (t/selection-cfg
   (merge
    {:name "Class"
     :order 0
     :tags #{:class}
     :multiselect? true
     :min 1
     :max nil}
    cfg)))

(defn race-selection [cfg]
  (t/selection-cfg
   (merge
    {:name "Race"
     :order 0
     :help "Race determines your appearance and helps shape your culture and background. It also affects you ability scores, size, speed, languages, and many other crucial inherent traits."
     :tags #{:race}}
    cfg)))

(def ranger-skills {:animal-handling true :athletics true :insight true :investigation true :nature true :perception true :stealth true :survival true})

(defn evasion [level page]
  {:name "Evasion"
   :page page
   :level level
   :summary "when you succeed on a DEX save to take half damage, you take none, if you fail, you take half"})

(defn uncanny-dodge-modifier [page]
  (modifiers/reaction
   {:name "Uncanny Dodge"
    :page page
    :summary "halve the damage from an attacker you can see that hits you"}))

(defn divine-strike [damage-desc page & [source]]
  (modifiers/dependent-trait
   {:level 8
    :name "Divine Strike"
    :page page
    :source source
    :frequency turns-1
    :summary (str "Add "
                  (if (>= (?class-level :cleric) 14) 2 1)
                  "d8 "
                  damage-desc
                  " damage to a successful weapon attack's damage")}))

(def favored-enemy-types
  {:aberration [:deep-speech :undercommon :grell :slaad]
   :beast [:giant-elk :giant-eagle :giant-owl]
   :celestial language-keys
   :construct [:modron]
   :dragon [:aquan :draconic :sylvan]
   :elemental [:auran :terran :ignan :aquan]
   :fey [:draconic :elvish :sylvan :abyssal :infernal :primoridial :aquan :giant]
   :fiend language-keys
   :giant [:giant :orc :undercommon]
   :monstrosity [:draconic :sylvan :elvish :hook-horror :abyssal :celestial :infernal :primordial :aquan :sphynx :umber-hulk :yeti :winter-wolf :goblin :worg]
   :ooze []
   :plant [:druidic :elvish :sylvan]
   :undead language-keys})

(def humanoid-enemies
  {:bugbear [:goblin]
   :bullywug [:bullywug]
   :githyanki [:gith]
   :gitzerai [:gith]
   :gnoll [:gnoll :abyssal]
   :goblin [:goblin]
   :grimlock [:undercommon]
   :hobgoblin [:goblin]
   :kobold [:draconic]
   :koa-toa [:undercommon]
   :lizardfolk [:draconic :abyssal]
   :merfolk [:aquan]
   :orc [:orc]
   :thri-kreen [:thri-kreen]
   :troglodyte [:troglodyte]
   :yuan-ti-pureblood {:name "Yuan-Ti Pureblood"
                       :languages [:abyssal :draconic]}})

(defn druid-cantrip-selection [class-nm]
  (t/selection-cfg
   {:name "Druid Cantrip"
    :tags #{:spells}
    :options (spell-options (get-in sl/spell-lists [:druid 0]) ::character/wis class-nm)}))

(defn eldritch-invocation-selection [cfg]
  (t/selection-cfg
   (merge
    {:name "Eldritch Invocations"
     :multiselect? true
     :ref [:class :warlock :eldritch-invocations]
     :tags #{:spells}}
    cfg)))

(def pact-of-the-tome-name "Pact Boon: Pact of the Tome")
(def pact-of-the-chain-name "Pact Boon: Pact of the Chain")
(def pact-of-the-blade-name "Pact Boon: Pact of the Blade")

(defn has-trait-with-name-prereq [name]
  (t/option-prereq
   (str "You must have " name)
   (fn [c] (some #(= name (:name %)) @(subscribe [::character/traits nil c])))))

(def pact-of-the-tome-prereq
  (has-trait-with-name-prereq pact-of-the-tome-name))

(def pact-of-the-blade-prereq
  (has-trait-with-name-prereq pact-of-the-blade-name))

(def pact-of-the-chain-prereq
  (has-trait-with-name-prereq pact-of-the-chain-name))

(def has-eldritch-blast-prereq
  (t/option-prereq
   "You must know the edritch blast spell"
   (fn [c] (some #(= :eldritch-blast (:key %))
                 (get @(subscribe [::character/spells-known nil c]) 0)))))

(defn deep-gnome-option-cfg [key source page]
  {:name "Gnome"
   :plugin? true
   :subraces
   [{:name (str "Deep Gnome (" (s/upper-case (name source)) ")")
     :key key
     :abilities {::character/dex 1}
     :modifiers [(modifiers/darkvision 120)]
     :source source
     :traits [{:name "Stone Camouflage"
               :source source
               :page page
               :summary "Advantage on hide checks in rocky terrain"}]}]})

(defmacro eldritch-invocation-option [{:keys [name summary source page prereqs modifiers trait-type frequency range]}]
  `(t/option-cfg
    {:name ~name
     :prereqs ~prereqs
     :modifiers (conj
                 ~modifiers
                 (~(case trait-type
                    :action `modifiers/action
                    :bonus-action `modifiers/bonus-action
                    :reaction `modifiers/reaction
                    `modifiers/dependent-trait)
                  {:name (str "Eldritch Invocation: " ~name)
                   :page ~page
                   :source ~source
                   :summary ~summary
                   :frequency ~frequency
                   :range ~range}))}))

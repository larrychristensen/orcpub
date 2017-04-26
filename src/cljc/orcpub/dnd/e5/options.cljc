(ns orcpub.dnd.e5.options
  (:require [clojure.string :as s]
            [orcpub.common :as common]
            [orcpub.template :as t]
            [orcpub.entity :as entity]
            [orcpub.entity-spec :as es]
            [orcpub.modifiers :as mods]
            [orcpub.dnd.e5.character :as character]
            [orcpub.dnd.e5.modifiers :as modifiers]
            [orcpub.dnd.e5.weapons :as weapons]
            [orcpub.dnd.e5.spells :as spells]
            [orcpub.dnd.e5.equipment :as equipment]
            [orcpub.dnd.e5.spell-lists :as sl]
            [orcpub.dnd.e5.display :as disp]
            [orcpub.dnd.e5.skills :as skills])
  #?(:cljs (:require-macros [orcpub.dnd.e5.modifiers :as modifiers])))

(def abilities
  [{:key :str
    :name "Strength"}
   {:key :con
    :name "Constitution"}
   {:key :dex
    :name "Dexterity"}
   {:key :int
    :name "Intelligence"}
   {:key :wis
    :name "Wisdom"}
   {:key :cha
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

(def conditions-map
  (common/map-by-key (common/add-keys conditions)))

(defn skill-option [skill]
  (t/option-cfg
   {:name (:name skill)
    :key (:key skill)
    :help (:description skill)
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
    :options [(modifiers/tool-proficiency (:key tool))]}))

(defn weapon-option [weapon & [num]]
  (t/option-cfg
   {:name (:name weapon)
    :key (:key weapon)
    :help (:description weapon)
    :modifiers [(modifiers/weapon (:key weapon) {:equipped? true
                                                 :quantity (or num 1)})]}))

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

(defn get-raw-abilities [app-state]
  (get-in (:character @app-state) [::entity/options :ability-scores ::entity/value]))

(defn abilities-improvement-component [num-increases different? ability-keys path built-template app-state built-char]
  (let [abilities (es/entity-val built-char :abilities)
        abilities-vec (vec abilities)
        increases-path (entity/get-option-value-path built-template (:character @app-state) path)
        full-path (concat [:character] increases-path)
        ability-increases (or (get-in @app-state full-path)
                              (zipmap character/ability-keys (repeat 0)))
        num-increased (apply + (vals ability-increases))
        num-remaining (- num-increases num-increased)
        allowed-abilities (set ability-keys)]
    [:div
     [:div
      [:div.m-t-5
       [:span.f-w-n "Increases Remaining: "]
       [:span.f-w-b num-remaining]
       (if different? [:div.f-w-n.i.m-t-5 (str num-increases " different abilities")])]]
     [:div.flex.justify-cont-s-b
      (doall
       (map-indexed
        (fn [i [k v]]
          (let [ability-disabled? (not (allowed-abilities k))
                increase-disabled? (or ability-disabled?
                                       (zero? num-remaining)
                                       (and different? (pos? (ability-increases k)))
                                       (>= (abilities k) 20))
                decrease-disabled? (or ability-disabled?
                                       (not (pos? (ability-increases k))))]
            ^{:key k}
           [:div.m-t-10.t-a-c
            {:class-name (if ability-disabled? "opacity-5 cursor-disabled")}
            [:div.uppercase (name k)]
            [:div.f-s-18.f-w-b v]
            [:div.f-6-12.f-w-n (ability-bonus-str v)]
            [:div.f-s-16
             [:i.fa.fa-minus-circle.orange
              {:class-name (if decrease-disabled? "opacity-5 cursor-disabled")
               :on-click (fn [] (if (not decrease-disabled?) (swap! app-state assoc-in full-path (update ability-increases k dec))))}]
             [:i.fa.fa-plus-circle.orange.m-l-5
              {:class-name (if increase-disabled? "opacity-5 cursor-disabled")
               :on-click (fn [] (if (not increase-disabled?) (swap! app-state assoc-in full-path (update ability-increases k inc))))}]]]))
        abilities-vec))]]))

(defn ability-increase-selection [ability-keys num-increases & [different?]]
  (t/selection-cfg
   {:name "Ability Score Improvement"
    :key :asi
    :min num-increases
    :max num-increases
    :tags #{:ability-scores}
    :different? different?
    :options (map
              (fn [k]
                (t/option-cfg
                 {:name (:name (abilities-map k))
                  :key k
                  :modifiers [(modifiers/level-ability-increase k 1)]}))
              ability-keys)}))

(defn ability-increase-option [num-increases different? ability-keys]
  (t/option-cfg
   {:name "Ability Score Improvement"
    :key :ability-score-improvement
    :selections [(ability-increase-selection ability-keys num-increases different?)]
    ;;:ui-fn (fn [path built-template app-state built-char] (abilities-improvement-component num-increases different? ability-keys path built-template app-state built-char))
    :modifiers [(modifiers/deferred-ability-increases)]}))

(defn min-ability [ability-kw min-value]
  (fn [c] (>= (ability-kw (es/entity-val c :abilities)) min-value)))

(defn ability-prereq [ability-kw min-value]
  (t/option-prereq (str "Requires " (s/upper-case (name ability-kw)) " " min-value " or higher")
                   (min-ability ability-kw min-value)))

(defn armor-prereq [armor-kw]
  (t/option-prereq (str "proficiency with " (name armor-kw) " armor")
                   (fn [c] (let [prof-keys (:armor-profs c)]
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
    :key :sylval}
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
     :modifiers [(modifiers/trait
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
                   :summary (str "spend 2 + X ki, a creature within 30 ft. takes 3d10 + Xd10 damage on failed DC " (?spell-save-dc :wis) " STR save, is pushed up to 20 ft., and is knocked prone. On successful save it just takes half damage.")})]})
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
                   :summary (str "spend 2 + X ki, a creature within 30 ft. takes 3d10 + Xd10 damage on failed DC " (?spell-save-dc :wis) " DEX save, is pulled up to 25 ft. or knocked prone. On successful save it just takes half damage.")})]})
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
    :prereqs [(t/option-prereq "You already have this language" (fn [c] (not (get (es/entity-val c :languages) key))))]}))

(def class-names
  {:barbarian "Barbarian"
   :bard "Bard"
   :cleric "Cleric"
   :druid "Druid"
   :fighter "Fighter"
   :monk "Monk"
   :paladin "Paladin"
   :ranger "Ranger"
   :rogue "Rogue"
   :sorcerer "Sorcerer"
   :wizard "Wizard"
   :warlock "Warlock"})

(defn key-to-name [key]
  (s/join " " (map s/capitalize (s/split (name key) #"-"))))

(defn spell-field [name value]
  [:div.m-b-2
   [:span.f-w-b (str name ": ")]
   [:span.f-w-n value]])

(defn spell-help [{:keys [school casting-time range duration description source page]}]
  [:div
   [:div.m-b-5
    (spell-field "School" school)
    (spell-field "Casting Time" casting-time)
    (spell-field "Range" range)
    (spell-field "Duration" duration)]
   [:div.f-w-n (if description
                 (doall
                  (map-indexed
                   (fn [i p]
                     ^{:key i} [:p.m-t-5 p])
                   (s/split description #"\n")))
                 (if source
                   (let [{:keys [abbr url]} (disp/sources source)]
                     [:div
                      [:span "See"]
                      [:a.m-l-5 {:href url} abbr]
                      [:span.m-l-5 (str "page " page)]])))]])

(defn spell-option [spellcasting-ability class-name key & [prepend-level? qualifier]]
  (let [{:keys [name level source] :as spell} (spells/spell-map key)]
    (t/option-cfg
     {:name (if prepend-level? (str level " - " name) name)
      :key key
      :help (spell-help spell)
      :prereqs [(t/option-prereq
                 "You already know this spell"
                 (fn [c] (let [spells-known (es/entity-val c :spells-known)]
                           (or (not spells-known)
                               (not (some #(= key (:key %))
                                          (spells-known level)))))))
                (t/option-prereq
                 "You aren't using this source"
                 (fn [c] (or (nil? source)
                             (= :phb source)
                             (get (es/entity-val c :option-sources) source)))
                 true)]
      :modifiers [(modifiers/spells-known level key spellcasting-ability class-name nil qualifier)]})))

(def memoized-spell-option (memoize spell-option))

(defn spell-options [spells spellcasting-ability class-name & [prepend-level? qualifier]]
  (map
   #(memoized-spell-option spellcasting-ability class-name % prepend-level? qualifier)
   (sort spells)))

(defn spell-level-title [class-name level]
  (str class-name (if (zero? level) " Cantrips Known" (str " Spells Known" (if level (str " " level))))))

(defn spell-selection [{:keys [title class-key level spellcasting-ability class-name num prepend-level? spell-keys options min max exclude-ref?]}]
  (let [title (or title (spell-level-title class-name level))
        kw (common/name-to-kw title)
        ref (if (not exclude-ref?) [:class class-key kw])]
     (t/selection-cfg
      {:name title
       :key kw
       :ref ref
       :order (if (zero? level) 0 1)
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
       19 {4 1}}))

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
      :options (mapcat
                (fn [[lvl spells]]
                  (map
                   (fn [{:keys [name] :as spell}]
                     (let [key (or (:key spell) (common/name-to-kw name))]
                       (spell-option :cha "Bard" key true)))
                   spells))
                filtered-spells-by-level)})))

(defn cantrip-selections [class-key ability cantrips-known]
  (reduce
   (fn [m [k v]]
     (assoc m k [(spell-selection {:class-key class-key
                                   :level 0
                                   :spellcasting-ability ability
                                   :class-name (class-names class-key)
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
           slots (or slot-schedule (total-slots cls-lvl level-factor))
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
                               (class-names class-key)
                               spell-key
                               true)))
                          (apply-spell-restriction spell-keys restriction)))
                       all-spells))]
         (assoc m cls-lvl
                [(let [cls-key-nm (class-key-name (:key cls-cfg) (:name cls-cfg))
                       kw (spell-selection-key cls-key-nm)]
                   (spell-selection
                    {:class-key class-key
                     :class-name (:name cls-cfg)
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
        cantrip-selections (cantrip-selections class-key ability cantrips-known)]
    {:selections (merge-with
                  concat
                  cantrip-selections
                  spell-selections)}))

(defn magic-initiate-option [class-key spellcasting-ability spell-lists]
  (t/option-cfg
   {:name (name class-key)
    :selections [(t/selection-cfg
                  {:name "Cantrip"
                   :order 1
                   :tags #{:spells}
                   :options (spell-options (get-in spell-lists [class-key 0]) spellcasting-ability (class-names class-key))
                   :min 2
                   :max 2})
                 (t/selection-cfg
                  {:name "1st Level Spell"
                   :order 2
                   :tags #{:spells}
                   :options (spell-options (get-in spell-lists [class-key 1]) spellcasting-ability (class-names class-key))
                   :min 1
                   :max 1})]}))

(defn ritual-spell? [spell]
  (:ritual spell))

(defn ritual-caster-option [class-key spellcasting-ability spell-lists]
  (t/option-cfg
   {:name (name class-key)
    :key class-key
    :selections [(t/selection-cfg
                  {:name "1st Level Ritual"
                   :tags #{:spells}
                   :options (spell-options (filter (fn [spell-kw] (ritual-spell? (spells/spell-map spell-kw))) (get-in spell-lists [class-key 1])) spellcasting-ability (class-names class-key))
                   :min 2
                   :max 2})]}))

(defn spell-sniper-option [class-key spellcasting-ability spell-lists]
  (t/option-cfg
   {:name (name class-key)
    :key class-key
    :selections [(t/selection-cfg
                  {:name "Attack Cantrip"
                   :tags #{:spells}
                   :options (spell-options (filter (fn [spell-kw] (:attack-roll? (spells/spell-map spell-kw))) (get-in spell-lists [class-key 0])) spellcasting-ability (class-names class-key))})]}))

(defn language-selection [langs num]
  (t/selection-cfg
   {:name "Languages"
    :options (map
     (fn [lang]
       (language-option lang))
     langs)
    :ref :languages
    :multiselect? true
    :tags #{:profs :language-profs}
    :min num
    :max num}))

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

(defn skill-selection
  ([num]
   (skill-selection (map :key skills/skills) num))
  ([options num & [order key prereq-fn]]
   (t/selection-cfg
    {:name "Skill Proficiency"
     :key key
     :order (or order 0)
     :help (proficiency-help num "a skill" "skills")
     :options (skill-options
               (filter
                (comp (set options) :key)
                skills/skills))
     :min num
     :max num
     :multiselect? true
     :ref :skill-profs
     :tags #{:skill-profs :profs}
     :prereq-fn prereq-fn})))

(defn tool-selection
  ([num]
   (t/selection-cfg
    {:name "Tool Proficiency"
     :help (proficiency-help num "a tool" "tools")
     :options (tool-options equipment/tools)
     :min num
     :max num
     :tags #{:tool-profs :profs}}))
  ([options num]
   (t/selection-cfg
    {:name "Tool Proficiency"
     :help (proficiency-help num "a tool" "tools")
     :options (tool-options
               (filter
                (comp (set options) :key)
                equipment/tools))
     :min num
     :max num
     :tags #{:tool-profs :profs}})))


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
   (maneuver-option "Feinting Attack"
                    "feint attack on a creature and gain advantage on next attack against it, adding superiority die to damage")
   (maneuver-option "Goading Attack"
                    "add superiority die to a successful attack's damage, if target fails WIS save, the next attack it makes must be against you or have disadvantage")
   (maneuver-option "Lunging Attack"
                    "increase melee attack reach by 5 ft., add superiority die to damage")
   (maneuver-option "Manuevering Attack"
                    "add superiority die to a successful attack's damage, choose a friendly creature that can move half it's speed as a reaction without opportunity attack from attack target")
   (maneuver-option "Menacing Attack"
                    "add superiority die to a successful attack's damage, if target fails WIS save, it becomes frightened of you until your next turn")
   (mod-maneuver-option
    "Parry"
    [(modifiers/reaction
      {:name "Parry Maneuver"
       :page 74
       :summary (str "reduce melee attack damage dealt to you by superiority die roll " (common/mod-str (?ability-bonuses :dex)))})])
   (maneuver-option "Precision Attack"
                    "add superiority die to weapon attack roll")
   (maneuver-option "Pushing Attack"
                    "add superiority die to a successful attack's damage, if target is Large or smaller and fails an STR save, it is pushed 15 ft. away")
   (mod-maneuver-option
    "Rally"
    [(modifiers/bonus-action
      {:name "Rally Maneuver"
       :page 74
       :summary (str "give superiority die "
                     (common/mod-str (?ability-bonuses :cha))
                     " temp HPs to a friendly creature")})])
   (mod-maneuver-option
    "Riposte"
    [(modifiers/reaction
      {:name "Riposte Maneuver"
       :page 74
       :summary "if a creature misses you with a melee attack, attack as a reaction and add superiority die to damage"})])
   (maneuver-option "Sweeping Attack"
                    "if you hit a creature with an attack roll, choose another creature within 5 ft., if the roll would hit the creature, it takes superiority die worth of damage")
   (maneuver-option "Trip Attack"
                    "add superiority die to successful attack's damage, if target fails STR save, it is knocked prone")])

(def can-cast-spell-prereq
  (t/option-prereq "Requires spellcasting ability."
                   (fn [c] (some (fn [[k v]] (seq v)) (es/entity-val c :spells-known)))))

(defn does-not-have-feat-prereq [kw]
  {::t/label "You already have this feat."
   ::t/prereq-fn (fn [c] (let [feats (es/entity-val c :feats)]
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
     :selections [(ability-increase-selection [:str :dex] 1 false)]})
   (feat-option
    {:name "Actor"
     :icon "drama-masks"
     :page 165
     :summary "increase CHA by 1; advantage on Deception and Performance when trying to pass as someone else; mimic the speech of a person you have heard"
     :modifiers [(modifiers/ability :cha 1)]})
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
     :prereqs [(ability-prereq :dex 13)]})
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
     :modifiers [(modifiers/ability :con 1)
                 (modifiers/dependent-trait
                  {:name "Durable"
                   :page 166
                   :summary (str "when you roll Hit Die to regain HPs, the min points regained is " (* 2 (?ability-bonuses :con)))})]})
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
     :prereqs [(ability-prereq :str 13)]})
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
                 (modifiers/ability :str 1)]
     :prereqs [(armor-prereq :medium)]})
   (feat-option
    {:name "Heavy Armor Master"
     :icon "gauntlet"
     :page 167
     :summary "increase STR by 1; when wearing heavy armor, slashing, piercing, and bludgeoning damage from non-magical weapons is 3 less"
     :modifiers [(modifiers/ability :str 1)]
     :prereqs [(armor-prereq :heavy)]})
   (feat-option
    {:name "Inspiring Leader"
     :icon "public-speaker"
     :page 167
     :summary "increase CHA by 1; give 6 friendly creatures within 30 ft. temp HPs equal to you CHA mod + your level"
     :prereqs [(ability-prereq :cha 13)]})
   (feat-option
    {:name "Keen Mind"
     :icon "brain"
     :page 167
     :summary "increase INT by 1; always know which direction is north; know hours before sunset or sunrise; recall anything heard or seen within a month"
     :modifiers [(modifiers/ability :int 1)]})
   (feat-option
    {:name "Lightly Armored"
     :icon "scale-mail"
     :page 167
     :summary "increase STR or DEX by 1; proficiency in light armor"
     :selections [(ability-increase-selection [:str :dex] 1 false)]
     :prereqs [(modifiers/light-armor-proficiency)]})
   (feat-option
    {:name "Linguist"
     :icon "lips"
     :page 167
     :summary "increase INT by 1; learn 3 languages; create written ciphers"
     :selections [(language-selection languages 3)]
     :prereqs [(modifiers/ability :int 1)]})
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
                    :options [(magic-initiate-option :bard :cha sl/spell-lists)
                              (magic-initiate-option :cleric :wis sl/spell-lists)
                              (magic-initiate-option :druid :wis sl/spell-lists)
                              (magic-initiate-option :sorcerer :cha sl/spell-lists)
                              (magic-initiate-option :warlock :cha sl/spell-lists)
                              (magic-initiate-option :wizard :int sl/spell-lists)]})]})
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
     :selections [(ability-increase-selection [:str :dex] 1 false)]
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
     :selections [(ability-increase-selection [:int :wis] 1 false)]
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
     :selections [(ability-increase-selection character/ability-keys 1 false)]})
   (feat-option
    {:name "Ritual Caster"
     :icon "gift-of-knowledge"
     :page 169
     :summary "choose a spellcaster class and learn 2 rituals from that class"
     :selections [(t/selection-cfg
                   {:name "Ritual Caster: Spell Class"
                    :tags #{:spells}
                    :options [(ritual-caster-option :bard :cha sl/spell-lists)
                              (ritual-caster-option :cleric :wis sl/spell-lists)
                              (ritual-caster-option :druid :wis sl/spell-lists)
                              (ritual-caster-option :sorcerer :cha sl/spell-lists)
                              (ritual-caster-option :warlock :cha sl/spell-lists)
                              (ritual-caster-option :wizard :int sl/spell-lists)]})]
     :prereqs [(t/option-prereq "Intelligence or Wisdom 13 or higher"
                                (fn [c]
                                  (let [{:keys [:wis :int] :as abilities} (es/entity-val c :abilities)]
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
     :prereqs [(ability-prereq :dex 13)]})
   (feat-option
    {:name "Spell Sniper"
     :icon "laser-precision"
     :page 170
     :summary "attack spells have double range; ignore half and 3/4 cover; learn a cantrip that requires an attack roll"
     :prereqs [can-cast-spell-prereq]
     :selections [(t/selection-cfg
                   {:name "Spell Sniper: Spell Class"
                    :tags #{:spells}
                    :options [(spell-sniper-option :bard :cha sl/spell-lists)
                              (spell-sniper-option :cleric :wis sl/spell-lists)
                              (spell-sniper-option :druid :wis sl/spell-lists)
                              (spell-sniper-option :sorcerer :cha sl/spell-lists)
                              (spell-sniper-option :warlock :cha sl/spell-lists)
                              (spell-sniper-option :wizard :int sl/spell-lists)]})]})
   (feat-option
    {:name "Tavern Brawler"
     :icon "broken-bottle"
     :page 170
     :summary "increase STR or CON by 1; improvised weapon proficiency; d4 damage on unarmed strike; grapple as bonus action"
     :selections [(ability-increase-selection [:str :dex] 1 false)]
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
     :selections [(ability-increase-selection [:str :dex] 1 false)
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

(defn fighting-style-selection [& [restrictions]]
  (t/selection-cfg
   {:name "Fighting Style"
    :tags #{:class}
    :options (if restrictions
               (filter
                (fn [o]
                  (restrictions (::t/key o)))
                fighting-style-options)
               fighting-style-options)}))

(defn feat-selection [num]
  (t/selection-cfg
   {:name (if (= 1 num) "Feat" "Feats")
    :options feat-options
    :multiselect? true
    :tags #{:feats}
    :ref :feats
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
                                               (let [skill-profs (es/entity-val built-char :skill-profs)]
                                                 (and skill-profs (skill-profs key)))))]}))
              skills/skills)
    :min num
    :max num
    :multiselect? true
    :ref :skill-expertise
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
                :modifiers [(modifiers/tool-proficiency :thieves-tools)]})]}))

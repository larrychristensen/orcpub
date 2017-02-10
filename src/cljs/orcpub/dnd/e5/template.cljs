(ns orcpub.dnd.e5.template
  (:require [orcpub.entity :as entity]
            [orcpub.entity-spec :as es]
            [orcpub.template :as t]
            [orcpub.dice :as dice]
            [orcpub.modifiers :as mod]
            [orcpub.common :as common]
            [orcpub.dnd.e5.character :as char5e]
            [orcpub.dnd.e5.modifiers :as mod5e]
            [orcpub.dnd.e5.options :as opt5e]
            [orcpub.dnd.e5.spell-lists :as sl]))

(def character
  {::entity/options {:ability-scores {::entity/key :standard-roll
                                      ::entity/value (char5e/abilities 12 13 14 15 16 17)}
                     :race {::entity/key :elf
                            ::entity/options {:subrace {::entity/key :high-elf
                                                        ::entity/options {:cantrip {::entity/key :light}}}}}
                     :class [{::entity/key :wizard
                              ::entity/options {:levels [{::entity/key :1
                                                          ::entity/options {:cantrips-known [{::entity/key :acid-splash}]
                                                                            :spells-known [{::entity/key :mage-armor} {::entity/key :magic-missile}]}}
                                                         {::entity/key :2
                                                          ::entity/options {:arcane-tradition {::entity/key :school-of-evocation}
                                                                            :hit-points {::entity/key :roll
                                                                                         ::entity/value 3}}}]}}]}})

(defn get-raw-abilities [character-ref]
  (get-in @character-ref [::entity/options :ability-scores ::entity/value]))

(defn swap-abilities [character-ref i other-i k v]
  (fn [e]
    (swap! character-ref
           update-in
           [::entity/options :ability-scores ::entity/value]
           (fn [a]
             (let [a-vec (vec a)
                   other-index (mod other-i (count a-vec))
                   [other-k other-v] (a-vec other-index)]
               (assoc a k other-v other-k v))))
    (.stopPropagation e)))

(defn abilities-standard [character-ref]
  [:div
    {:style {:display :flex
             :justify-content :space-between}}
    (let [abilities (get-raw-abilities character-ref)
          abilities-vec (vec abilities)]
      (map-indexed
       (fn [i [k v]]
         ^{:key k}
         [:div {:style {:margin-top "10px"
                        :margin-bottom "10px"
                        :text-align :center}}
          [:div {:style {:text-transform :uppercase}} (name k)]
          [:div {:style {:font-size "18px"}} v]
          [:div
           [:i.fa.fa-chevron-circle-left
            {:style {:font-size "16px"}
             :on-click (swap-abilities character-ref i (dec i) k v)}]
           [:i.fa.fa-chevron-circle-right
            {:style {:margin-left "5px" :font-size "16px"}
             :on-click (swap-abilities character-ref i (inc i) k v)}]]])
       abilities-vec))])

(defn abilities-roller [character-ref reroll-fn]
  [:div
   (abilities-standard character-ref)
   [:button.form-button
    {:on-click reroll-fn}
    "Re-Roll"]])

(declare template-selections)

(defn roll-hit-points [die character-ref path]
  (let [value-path (entity/get-option-value-path
                    {::t/selections (template-selections character-ref)}
                    @character-ref
                    path)]
    (swap! character-ref #(assoc-in % value-path (dice/die-roll die)))))

(defn hit-points-roller [die character-ref path]
  [:div
   [:button.form-button
    {:style {:margin-top "10px"}
     :on-click #(roll-hit-points die character-ref path)}
    "Re-Roll"]])

(defn traits-modifiers [traits & [include-level?]]
  (map
   (fn [{:keys [name description level]}]
     (if include-level?
       (mod5e/trait name description level)
       ;; for subclasses we need to do level checks, for most everything else we do not
       (mod5e/trait name description)))
   traits))

(defn armor-prof-modifiers [armor-proficiencies]
  (map
   (fn [armor-kw]
        (mod5e/armor-proficiency (clojure.core/name armor-kw) armor-kw))
   armor-proficiencies))

(defn tool-prof-modifiers [tool-proficiencies]
  (map
   (fn [tool-kw]
        (mod5e/tool-proficiency (clojure.core/name tool-kw) tool-kw))
   tool-proficiencies))

(defn weapon-prof-modifiers [weapon-proficiencies]
  (map
   (fn [weapon-kw]
     (if (#{:simple :martial} weapon-kw)
       (mod5e/weapon-proficiency (str (name weapon-kw) " weapons") weapon-kw)
       (mod5e/weapon-proficiency (-> weapon-kw opt5e/weapons-map :name) weapon-kw)))
   weapon-proficiencies))

(defn subrace-option [{:keys [name
                              abilities
                              size
                              speed
                              subrace-options
                              armor-proficiencies
                              weapon-proficiencies
                              modifiers
                              selections
                              traits]}
                      character-ref]
  (let [option (t/option
   name
   (common/name-to-kw name)
   selections
   (vec
    (concat
     [(mod5e/subrace name)]
     modifiers
     (armor-prof-modifiers armor-proficiencies)
     (weapon-prof-modifiers weapon-proficiencies)
     (map
      (fn [[k v]]
        (mod5e/ability k v))
      abilities)
     (traits-modifiers traits))))]
    option))

(defn ability-modifiers [abilities]
  (map
   (fn [[k v]]
     (mod5e/ability k v))
   abilities))

(defn race-option [{:keys [name
                           abilities
                           size
                           speed
                           subraces
                           modifiers
                           selections
                           traits
                           languages
                           armor-proficiencies
                           weapon-proficiencies]}]
  (t/option
   name
   (common/name-to-kw name)
   (concat
    (if subraces
      [(t/selection
        "Subrace"
        (map subrace-option subraces))])
    selections)
   (vec
    (concat
     [(mod5e/race name)
      (mod5e/size size)
      (mod5e/speed speed)]
     (map
      (fn [language]
        (mod5e/language language (common/name-to-kw language)))
      languages)
     (map
      (fn [[k v]]
        (mod5e/ability k v))
      abilities)
     modifiers
     (traits-modifiers traits)
     (armor-prof-modifiers armor-proficiencies)
     (weapon-prof-modifiers weapon-proficiencies)))))

(def elf-weapon-training-mods
  (weapon-prof-modifiers [:longsword :shortsword :shortbow :longbow]))

(def elf-option
  (race-option
   {:name "Elf"
    :abilities {:dex 2}
    :size :medium
    :speed 30
    :languages ["Elvish" "Common"]
    :subraces
    [{:name "High Elf"
      :abilities {:int 1}
      :selections [(opt5e/wizard-cantrip-selection 1)
                   (opt5e/language-selection opt5e/languages 1)]
      :modifiers [elf-weapon-training-mods]}
     {:name "Wood Elf"
      :abilities {:cha 1}
      :traits [{:name "Mask of the Wild"}]
      :modifiers [(mod5e/speed 35)
                  elf-weapon-training-mods]}
     {:name "Dark Elf (Drow)"
      :abilities {:cha 1}
      :traits [{:name "Sunlight Sensitivity"}
               {:name "Drow Magic"}]
      :modifiers [(mod5e/darkvision 120)
                  (mod5e/spells-known 0 :dancing-lights :cha)
                  (mod5e/spells-known 1 :faerie-fire :cha 3)
                  (mod5e/spells-known 2 :darkness :cha 5)]}]
    :traits [{:name "Fey Ancestry" :description "You have advantage on saving throws against being charmed and magic can't put you to sleep"}
             {:name "Trance" :description "Elves don't need to sleep. Instead, they meditate deeply, remaining semiconscious, for 4 hours a day. (The Common word for such meditation is 'trance.') While meditating, you can dream after a fashion; such dreams are actually mental exercises that have become re exive through years of practice. After resting in this way, you gain the same bene t that a human does from 8 hours of sleep."}
             {:name "Darkvision" :description "Accustomed to twilit forests and the night sky, you have superior vision in dark and dim conditions. You can see in dim light within 60 feet of you as if it were bright light, and in darkness as if it were dim light. You can't discern color in darkness, only shades of gray."}]}))

(def dwarf-option
  (race-option
   {:name "Dwarf",
    :abilities {:con 2},
    :size :medium
    :speed 25,
    :languages ["Dwarvish" "Common"]
    :weapon-proficiencies [:handaxe :battleaxe :light-hammer :warhammer]
    :traits [{:name "Dwarven Resilience",
              :description "You have advantage on saving throws against poison, and you have resistance against poison damage"},
             {:name "Stonecunning"
              :description "Whenever you make an Intelligence (History) check related to the origin of stonework you are considered proficient in the History skill and add double your proficiency bonus to the check, instead of your normal proficiency bonus"}
             {:name "Darkvision" :description "Accustomed to twilit forests and the night sky, you have superior vision in dark and dim conditions. You can see in dim light within 60 feet of you as if it were bright light, and in darkness as if it were dim light. You can't discern color in darkness, only shades of gray."}]
    :subraces [{:name "Hill Dwarf",
                :abilities {:wis 1}
                :selections [(opt5e/tool-selection [:smiths-tools :brewers-supplies :masons-tools] 1)]
                :modifiers [(mod/modifier ?hit-point-level-bonus (+ 1 ?hit-point-level-bonus))]}
               {:name "Mountain Dwarf"
                :abilities {:str 2}
                :armor-proficiencies [:light :medium]}]
    :modifiers [(mod5e/darkvision 60)
                (mod5e/resistance :poison)]}))

(def halfling-option
  (race-option
   {:name "Halfling"
    :abilities {:dex 2}
    :size :small
    :speed 25
    :languages ["Halfling" "Common"]
    :subraces
    [{:name "Lightfoot"
      :abilities {:cha 1}
      :traits [{:name "Naturally Stealthy" :description "You can attempt to hide even when you are obscured only by a creature that is at least one size larger than you."}]}
     {:name "Stout"
      :abilities {:con 1}
      :traits [{:name "Stout Resilience"}]}]
    :traits [{:name "Lucky" :description "When you roll a 1 on the d20 for an attack roll, ability check, or saving throw, you can reroll the die and must use the new roll."}
             {:name "Brave" :description "You have advantage on saving throws against being frightened."}
             {:name "Halfling Nimbleness" :description "You can move through the space of any creature that is of a size larger than yours."}]}))

(def human-option
  (race-option
   {:name "Human"
    ;; abilities are tied to variant selection below
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
                 (t/selection
                  "Variant"
                  [(t/option
                    "Standard Human"
                    :standard
                    []
                    [(ability-modifiers {:str 1 :con 1 :dex 1 :int 1 :wis 1 :cha 1})])
                   (t/option
                    "Variant Human"
                    :variant
                    [(opt5e/feat-selection 1)
                     (opt5e/skill-selection 1)
                     (opt5e/ability-increase-selection char5e/ability-keys 2 true)]
                    [])])]}))

(defn draconic-ancestry-option [{:keys [name damage-type breath-weapon]}]
  (t/option
   name
   (common/name-to-kw name)
   []
   [(mod5e/resistance damage-type)
    (mod5e/trait "Breath Weapon Details" breath-weapon)]))

(def dragonborn-option
  (race-option
   {:name "Dragonborn"
    :abilities {:str 2 :cha 1}
    :size :medium
    :speed 30
    :languages ["Draconic" "Common"]
    :selections [(t/selection
                  "Draconic Ancestry"
                  (map
                   draconic-ancestry-option
                   [{:name "Black"
                     :damage-type :acid
                     :breath-weapon "5 by 30 ft. line (Dex. save)"}
                    {:name "Blue"
                     :damage-type :lightning
                     :breath-weapon "5 by 30 ft. line (Dex. save)"}
                    {:name "Brass"
                     :damage-type :fire
                     :breath-weapon "5 by 30 ft. line (Dex. save)"}
                    {:name "Bronze"
                     :damage-type :lightning
                     :breath-weapon "5 by 30 ft. line (Dex. save)"}
                    {:name "Copper"
                     :damage-type :acid
                     :breath-weapon "5 by 30 ft. line (Dex. save)"}
                    {:name "Gold"
                     :damage-type :fire
                     :breath-weapon "15 ft cone (Dex. save)"}
                    {:name "Green"
                     :damage-type :poison
                     :breath-weapon "15 ft cone (Con. save)"}
                    {:name "Red"
                     :damage-type :fire
                     :breath-weapon "15 ft cone (Dex. save)"}
                    {:name "Silver"
                     :damage-type :cold
                     :breath-weapon "15 ft cone (Con. save)"}
                    {:name "White"
                     :damage-type :cold
                     :breath-weapon "15 ft cone (Con. save)"}]))]
    :traits [{:name "Breath Weapon" :description "You can use your action to 
exhale destructive energy. Your draconic ancestry 
determines the size, shape, and damage type of the 
exhalation.
When you use your breath weapon, each creature 
in the area of the exhalation must make a saving 
throw, the type of which is determined by your 
draconic ancestry. The DC for this saving throw 
equals 8 + your Constitution modifier + your 
proficiency bonus. A creature takes 2d6 damage on a 
failed save, and half as much damage on a successful one. The damage increases to 3d6 at 6th level, 4d6 at 
11th level, and 5d6 at 16th level.
After you use your breath weapon, you can't use it 
again until you complete a short or long rest."}]}))

(def gnome-option
  (race-option
   {:name "Gnome"
    :abilities {:int 2}
    :size :small
    :speed 25
    :darkvision 60
    :languages ["Gnomish" "Common"]
    :subraces
    [{:name "Rock Gnome"
      :abilities {:con 1}
      :modifiers [(mod5e/tool-proficiency "Tinker's Tools" :tinkers-tools)]
      :traits [{:name "Artificer's Lore" :description "Whenever you make an Intelligence (History) check related to magic items, alchemical objects, or technological devices, you can add twice your proficiency bonus, instead of any proficiency bonus you normally apply."}
               {:name "Tinker" :description "You have proficiency with artisan's tools 
(tinker's tools). Using those tools, you can spend 1 
hour and 10 gp worth of materials to construct a 
Tiny clockwork device (AC 5, 1 hp). The device 
ceases to function after 24 hours (unless you spend 
1 hour repairing it to keep the device functioning), 
or when you use your action to dismantle it; at that 
time, you can reclaim the materials used to create it. 
You can have up to three such devices active at a 
time.
When you create a device, choose one of the 
following options:
Clockwork Toy. This toy is a clockwork animal, 
monster, or person, such as a frog, mouse, bird, 
dragon, or soldier. When placed on the ground, the 
toy moves 5 feet across the ground on each of your 
turns in a random direction. It makes noises as 
appropriate to the creature it represents.
Fire Starter. The device produces a miniature flame, 
which you can use to light a candle, torch, or 
campfire. Using the device requires your action.
Music Box. When opened, this music box plays a 
single song at a moderate volume. The box stops 
playing when it reaches the song's end or when it
is closed."}]}
     {:name "Forest Gnome"
      :abilities {:dex 1}
      :modifiers [(mod5e/spells-known 0 :minor-illusion :int)]
      :traits [{:name "Speak with Small Beasts"}]}]
    :traits [{:name "Gnome Cunning" :description "You have advantage on all 
Intelligence, Wisdom, and Charisma saving throws against magic."}]
    :modifiers [(mod5e/darkvision 60)]}))

(def half-elf-option
  (race-option
   {:name "Half Elf"
    :abilities {:cha 2}
    :size :medium
    :speed 30
    :languages ["Common"]
    :selections [(opt5e/ability-increase-selection (disj (set char5e/ability-keys) :cha) 1 false)
                 (opt5e/skill-selection 2)
                 (opt5e/language-selection opt5e/languages 1)]
    :modifiers [(mod5e/darkvision 60)]
    :traits [{:name "Fey Ancestry" :description "You have advantage on saving 
throws against being charmed, and magic can't put 
you to sleep."}]}))

(def half-orc-option
  (race-option
   {:name "Half Orc"
    :abilities {:str 2 :con 1}
    :size :medium
    :speed 30
    :languages ["Common" "Orc"]
    :modifiers [(mod5e/darkvision 60)
                (mod5e/skill-proficiency :intimidation)]
    :traits [{:name "Relentless Endurance" :description "When you are reduced to 0 
hit points but not killed outright, you can drop to 1 
hit point instead. You can't use this feature again 
until you finish a long rest."}
                      {:name "Savage Attacks" :description "When you score a critical hit with 
a melee weapon attack, you can roll one of the 
weapon's damage dice one additional time and add it 
to the extra damage of the critical hit."}]}))

(def tiefling-option
  (race-option
   {:name "Tiefling"
    :abilities {:int 1 :cha 2}
    :size :medium
    :speed 30
    :languages ["Common" "Infernal"]
    :modifiers [(mod5e/darkvision 60)
                  (mod5e/spells-known 0 :thaumaturgy :cha)
                  (mod5e/spells-known 1 :hellish-rebuke :cha 3)
                  (mod5e/spells-known 2 :darkness :cha 5)]
    :traits [{:name "Relentless Endurance" :description "When you are reduced to 0 
hit points but not killed outright, you can drop to 1 
hit point instead. You can't use this feature again 
until you finish a long rest."}
                      {:name "Savage Attacks" :description "When you score a critical hit with 
a melee weapon attack, you can roll one of the 
weapon's damage dice one additional time and add it 
to the extra damage of the critical hit."}]}))

(defn die-mean [die]
  (int (Math/ceil (/ (apply + (range 1 (inc die))) die))))

(defn hit-points-selection [character-ref die]
  (t/selection
   "Hit Points"
   [{::t/name "Roll"
     ::t/key :roll
     ::t/ui-fn #(hit-points-roller die character-ref %)
     ::t/select-fn #(roll-hit-points die character-ref %)
     ::t/modifiers [(mod5e/deferred-max-hit-points)]}
    (t/option
     "Average"
     :average
     nil
     [(mod5e/max-hit-points (die-mean die))])]))

(defn tool-prof-selection [tool-options]
  (t/selection
   "Tool Proficiencies"
   (map
    (fn [[k num]]
      (let [tool (opt5e/tools-map k)]
        (if (:values tool)
          (t/option
           (:name tool)
           k
           [(t/selection
             (:name tool)
             (map
              (fn [{:keys [name key]}]
                (t/option
                 name
                 key
                 []
                 [(mod5e/tool-proficiency name key)]))
              (:values tool))
             num
             num)]
           [])
          (t/option
           (:name tool)
           (:key tool)
           []
           [(mod5e/tool-proficiency (:name tool) (:key tool))]))))
    tool-options)))

(defn subclass-level-option [{:keys [name
                                     levels] :as subcls}
                             kw
                             character-ref
                             spellcasting-template
                             i]
  (let [selections (some-> levels (get i) :selections)]
    (if (= name "Way of the Four Elements")
      (js/console.log "SELECTIONS" selections))
    (t/option
     (str i)
     (keyword (str i))
     (concat
      selections      
      (some-> spellcasting-template :selections (get i)))
     (some-> levels (get i) :modifiers))))

(defn subclass-option [cls
                       {:keys [name
                               profs
                               selections
                               spellcasting
                               modifiers
                               level-modifiers
                               traits]
                        :as subcls}
                       character-ref]
  (let [kw (common/name-to-kw name)
        {:keys [armor weapon save skill-options tool-options tool]} profs
        {skill-num :choose options :options} skill-options
        skill-kws (if (:any options) (map :key opt5e/skills) (keys options))
        armor-profs (keys armor)
        weapon-profs (keys weapon)
        tool-profs (keys tool)
        spellcasting-template (opt5e/spellcasting-template
                               (assoc
                                spellcasting
                                :class-key
                                (or (:spell-list spellcasting) kw)))]
    (assoc
     (t/option
      name
      kw
      (concat
       selections
       (if (seq tool-options) [(tool-prof-selection tool-options)])
       (if (seq skill-kws) [(opt5e/skill-selection skill-kws skill-num)]))
      (concat
       modifiers
       (armor-prof-modifiers armor-profs)
       (weapon-prof-modifiers weapon-profs)
       (tool-prof-modifiers tool-profs)
       (traits-modifiers traits true)))
     ::t/plugins [{::t/path [:class (:key cls)]
                   ::t/selections [(t/sequential-selection
                                    "Levels"
                                    (fn [selection options current-values]
                                      {::entity/key (-> current-values count inc str keyword)})
                                    (vec
                                     (map
                                      (partial subclass-level-option subcls kw character-ref spellcasting-template)
                                      (range 1 21))))]}])))

(defn level-option [{:keys [name
                            hit-die
                            profs
                            levels
                            traits
                            spellcasting
                            ability-increase-levels
                            subclass-title
                            subclass-level
                            subclasses] :as cls}
                    kw
                    character-ref
                    spellcasting-template
                    i]
  (let [ability-inc-set (set ability-increase-levels)]
    (t/option
     (str i)
     (keyword (str i))
     (concat
      (some-> levels (get i) :selections)
      (some-> spellcasting-template :selections (get i))
      (if (= i subclass-level)
        [(t/selection-with-key
          subclass-title
          :subclass
          (map
           #(subclass-option (assoc cls :key kw) % character-ref)
           subclasses))])
      (if (ability-inc-set i)
        [(opt5e/ability-score-improvement-selection)])
      (if (> i 1)
        [(hit-points-selection character-ref hit-die)]))
     (concat
      (if (= :all (:known-mode spellcasting))
        (let [slots (opt5e/total-slots i (:level-factor spellcasting))
              prev-level-slots (opt5e/total-slots (dec i) (:level-factor spellcasting))
              new-slots (apply dissoc slots (keys prev-level-slots))]
          (if (seq new-slots)
            (let [lvl (key (first new-slots))]
              (map
               (fn [kw]
                 (mod5e/spells-known lvl kw (:ability spellcasting)))
               (get-in sl/spell-lists [kw lvl]))))))
      (some-> levels (get i) :modifiers)
      (traits-modifiers
       (filter
        (fn [{level :level :or {level 1}}]
          (= level i))
        traits))
      (if (= i 1) [(mod5e/max-hit-points hit-die)])
      [(mod5e/level kw name i)]))))


(defn equipment-option [[k num]]
  (let [equipment (opt5e/equipment-map k)]
    (if (:values equipment)
      (t/option
       (:name equipment)
       k
       [(t/selection
         (:name equipment)
         (map
          equipment-option
          (zipmap (map :key (:values equipment)) (repeat num))))]
       [])
      (t/option
       (-> k opt5e/equipment-map :name (str (if (> num 1) (str " (" num ")") "")))
       k
       []
       [(mod5e/equipment k num)]))))

(defn weapon-option [[k num]]
  (case k
    :simple (t/option
             "Any Simple Weapon"
             :any-simple
             [(t/selection
               "Simple Weapon"
               (opt5e/weapon-options (opt5e/simple-weapons opt5e/weapons)))]
             [])
    :martial (t/option
              "Any Martial Weapon"
              :any-martial
              [(t/selection
                "Martial Weapon"
                (opt5e/weapon-options (opt5e/martial-weapons opt5e/weapons)))]
              [])
    (t/option
     (-> k opt5e/weapons-map :name (str (if (> num 1) (str " (" num ")") "")))
     k
     []
     [(mod5e/weapon k num)])))

(defn armor-option [[k num]]
  (t/option
     (-> k opt5e/armor-map :name)
     k
     []
     [(mod5e/armor k num)]))

(defn class-options [option-fn choices]
  (map
   (fn [{:keys [name options]}]
     (t/selection
      name
      (mapv
       option-fn
       options)))
   choices))

(defn class-weapon-options [weapon-choices]
  (class-options weapon-option weapon-choices))

(defn class-armor-options [armor-choices]
  (class-options armor-option armor-choices))

(defn class-equipment-options [equipment-choices]
  (class-options equipment-option equipment-choices))


(defn class-option [{:keys [name
                            hit-die
                            profs
                            levels
                            ability-increase-levels
                            subclass-title
                            subclass-level
                            subclasses
                            selections
                            modifiers
                            weapon-choices
                            weapons
                            equipment
                            equipment-choices
                            armor
                            armor-choices
                            spellcasting]
                     :as cls}
                    character-ref]
  (let [kw (common/name-to-kw name)
        {:keys [save skill-options tool-options tool]
         armor-profs :armor weapon-profs :weapon} profs
        {skill-num :choose options :options} skill-options
        skill-kws (if (:any options) (map :key opt5e/skills) (keys options))
        save-profs (keys save)
        spellcasting-template (opt5e/spellcasting-template (assoc spellcasting :class-key kw))]
    (t/option
     name
     kw
     (concat
      selections
      (if (seq tool-options) [(tool-prof-selection tool-options)])
      (class-weapon-options weapon-choices)
      (class-armor-options armor-choices)
      (class-equipment-options equipment-choices)
      [(opt5e/skill-selection skill-kws skill-num)
       (t/sequential-selection
        "Levels"
        (fn [selection options current-values]
          {::entity/key (-> current-values count inc str keyword)})
        (vec
         (map
          (partial level-option cls kw character-ref spellcasting-template)
          (range 1 21))))])
     (concat
      modifiers
      (armor-prof-modifiers (keys armor-profs))
      (weapon-prof-modifiers (keys weapon-profs))
      (tool-prof-modifiers (keys tool))
      (mapv
       (fn [[k num]]
         (mod5e/weapon k num))
       weapons)
      (mapv
       (fn [[k num]]
         (mod5e/armor k num))
       armor)
      (mapv
       (fn [[k num]]
         (mod5e/equipment k num))
       equipment)
      [(apply mod5e/saving-throws save-profs)]))))


(defn barbarian-option [character-ref]
  (class-option
   {:name "Barbarian"
    :hit-die 12
    :ability-increase-levels [4 8 12 16 19]
    :profs {:armor {:light true :medium true :shields true}
            :weapon {:simple true :martial true}
            :save {:str true :con true}
            :skill-options {:choose 2 :options {:animal-handling true :athletics true :intimidation true :nature true :perception true :survival true}}}
    :weapon-choices [{:name "Martial Weapon"
                      :options {:greataxe 1
                                :martial 1}}
                     {:name "Simple Weapon"
                      :options {:handaxe 2
                                :simple 1}}]
    :weapons {:javelin 4}
    :equipment {:explorers-pack 1}
    :levels {5 {:modifiers [(mod5e/extra-attack)]}}
    :traits [{:name "Rage"
              :description "In battle, you fight with primal ferocity. On your turn, 
you can enter a rage as a bonus action.
While raging, you gain the following benefits if you 
aren't wearing heavy armor:
* You have advantage on Strength checks and 
Strength saving throws.
* When you make a melee weapon attack using 
Strength, you gain a bonus to the damage roll that 
increases as you gain levels as a barbarian, as 
shown in the Rage Damage column of the 
Barbarian table.
* You have resistance to bludgeoning, piercing, and 
slashing damage.
If you are able to cast spells, you can't cast them or 
concentrate on them while raging.
Your rage lasts for 1 minute. It ends early if you 
are knocked unconscious or if your turn ends and 
you haven't attacked a hostile creature since your 
last turn or taken damage since then. You can also 
end your rage on your turn as a bonus action.
Once you have raged the number of times shown 
for your barbarian level in the Rages column of the 
Barbarian table, you must finish a long rest before 
you can rage again."}
             {:name "Unarmored Defense"
              :description "While you are not wearing any armor, your Armor 
Class equals 10 + your Dexterity modifier + your 
Constitution modifier. You can use a shield and still 
gain this benefit."}
             {:name "Reckless Attack"
              :level 2
              :description "Starting at 2nd level, you can throw aside all concern 
for defense to attack with fierce desperation. When 
you make your first attack on your turn, you can 
decide to attack recklessly. Doing so gives you 
advantage on melee weapon attack rolls using 
Strength during this turn, but attack rolls against 
you have advantage until your next turn."}
             {:name "Danger Sense"
              :level 2
              :description "At 2nd level, you gain an uncanny sense of when 
things nearby aren't as they should be, giving you an 
edge when you dodge away from danger.
You have advantage on Dexterity saving throws 
against effects that you can see, such as traps and 
spells. To gain this benefit, you can't be blinded, 
deafened, or incapacitated."}
             {:name "Extra Attack"
              :level 5
              :description "Beginning at 5th level, you can attack twice, instead 
of once, whenever you take the Attack action on your 
turn."}
             {:name "Fast Movement"
              :level 5
              :description "Starting at 5th level, your speed increases by 10 feet 
while you aren't wearing heavy armor."}
             {:name "Feral Instinct"
              :level 7
              :description "By 7th level, your instincts are so honed that you 
have advantage on initiative rolls.
Additionally, if you are surprised at the beginning 
of combat and aren't incapacitated, you can act 
normally on your first turn, but only if you enter 
your rage before doing anything else on that turn."}
             {:name "Brutal Critical"
              :level 9
              :description "Beginning at 9th level, you can roll one additional 
weapon damage die when determining the extra 
damage for a critical hit with a melee attack.
This increases to two additional dice at 13th level 
and three additional dice at 17th level."}
             {:name "Relentless Rage"
              :level 11
              :description "Starting at 11th level, your rage can keep you 
fighting despite grievous wounds. If you drop to 0 hit 
points while you're raging and don't die outright, 
you can make a DC 10 Constitution saving throw. If 
you succeed, you drop to 1 hit point instead.
Each time you use this feature after the first, the 
DC increases by 5. When you finish a short or long 
rest, the DC resets to 10."}
             {:name "Persistent Rage"
              :level 15
              :description "Beginning at 15th level, your rage is so fierce that it 
ends early only if you fall unconscious or if you 
choose to end it."}
             {:name "Indomitable Might"
              :level 18
              :description "Beginning at 18th level, if your total for a Strength 
check is less than your Strength score, you can use 
that score in place of the total."}
             {:name "Primal Champion"
              :level 20
              :description "At 20th level, you embody the power of the wilds. 
Your Strength and Constitution scores increase by 4. 
Your maximum for those scores is now 24."}]
    :subclass-level 3
    :subclass-title "Primal Path"
    :subclasses [{:name "Path of the Beserker"
                  :traits [{:name "Frenzy"
                            :level 3
                            :description "Starting when you choose this path at 3rd level, you 
can go into a frenzy when you rage. If you do so, for 
the duration of your rage you can make a single 
melee weapon attack as a bonus action on each of 
your turns after this one. When your rage ends, you 
suffer one level of exhaustion (as described in 
appendix A)."}
                           {:name "Mindless Rage"
                            :level 6
                            :description "Beginning at 6th level, you can't be charmed or 
frightened while raging. If you are charmed or 
frightened when you enter your rage, the effect is 
suspended for the duration of the rage."}
                           {:name "Intimidating Presence"
                            :level 10
                            :description "Beginning at 10th level, you can use your action to 
frighten someone with your menacing presence. 
When you do so, choose one creature that you can 
see within 30 feet of you. If the creature can see or 
hear you, it must succeed on a Wisdom saving throw 
(DC equal to 8 + your proficiency bonus + your 
Charisma modifier) or be frightened of you until the 
end of your next turn. On subsequent turns, you can 
use your action to extend the duration of this effect 
on the frightened creature until the end of your next 
turn. This effect ends if the creature ends its turn out 
of line of sight or more than 60 feet away from you.
If the creature succeeds on its saving throw, you 
can't use this feature on that creature again for 24 
hours."}
                           {:name "Retaliation"
                            :level 14
                            :description "Starting at 14th level, when you take damage from a 
creature that is within 5 feet of you, you can use your 
reaction to make a melee weapon attack against that 
creature."}]}
                 {:name "Path of the Totem Warrior"
                  :traits [{:name "Spirit Seeker"
                            :level 3}
                           {:name "Totem Spirit"
                            :level 3}
                           {:name "Aspect of the Beast"
                            :level 6}
                           {:name "Spirit Walker"
                            :level 10}
                           {:name "Totemic Attunement"
                            :level 14}]}
                 {:name "Path of the Battlerager"
                  :source "Sword Coast Adventurer's Guide"
                  :traits [{:name "Battlerager Armor"
                            :level 3}
                           {:name "Reckless Abandon"
                            :level 6}
                           {:name "Battlerager Charge"
                            :level 10}
                           {:name "Spiked Retribution"
                            :level 14}]}]}
   character-ref))

(defn bard-option [character-ref]
  (class-option
   {:name "Bard"
    :hit-die 8
    :ability-increase-levels [4 8 12 16 19]
    :profs {:armor {:light true}
            :weapon {:simple true :crossbow--hand true :longsword true :rapier true :shortsword true}
            :save {:dex true :cha true}
            :skill-options {:choose 3 :options {:any true}}
            :tool-options {:musical-instrument 3}}
    :weapon-choices [{:name "Weapon"
                      :options {:rapier 1
                                :longsword 1
                                :simple 1}}]
    :weapons {:dagger 1}
    :equipment-choices [{:name "Equipment Pack"
                         :options {:diplomats-pack 1
                                   :entertainers-pack 1}}
                        {:name "Musical Instrument"
                         :options {:lute 1
                                   :musical-instrument 1}}]
    :armor {:leather 1}
    :spellcaster true
    :spellcasting {:level-factor 1
                   :cantrips-known {1 2 4 1 10 1}
                   :spells-known {1 4
                                  2 1
                                  3 1
                                  4 1
                                  5 1
                                  6 1
                                  7 1
                                  8 1
                                  9 1
                                  10 2
                                  11 1
                                  13 1
                                  14 2
                                  15 1
                                  17 1
                                  18 2}
                   :known-mode :schedule
                   :ability :cha}
    :levels {2 {:modifiers [(mod/modifier ?default-skill-bonus (let [b (int (/ ?prof-bonus 2))]
                                                                 (zipmap char5e/ability-keys (repeat b))))]}
             3 {:selections [(opt5e/expertise-selection 2)]}
             10 {:selections (concat [opt5e/expertise-selection]
                                     (opt5e/raw-bard-magical-secrets 10))}
             14 {:selections (opt5e/raw-bard-magical-secrets 14)}
             18 {:selections (opt5e/raw-bard-magical-secrets 18)}}
    :traits [{:name "Bardic Inspiration"
              :description "You can inspire others through stirring words or 
music. To do so, you use a bonus action on your turn 
to choose one creature other than yourself within 60 
feet of you who can hear you. That creature gains 
one Bardic Inspiration die, a d6.
Once within the next 10 minutes, the creature can 
roll the die and add the number rolled to one ability 
check, attack roll, or saving throw it makes. The 
creature can wait until after it rolls the d20 before 
deciding to use the Bardic Inspiration die, but must 
decide before the GM says whether the roll succeeds 
or fails. Once the Bardic Inspiration die is rolled, it is 
lost. A creature can have only one Bardic Inspiration 
die at a time.
You can use this feature a number of times equal 
to your Charisma modifier (a minimum of once). You 
regain any expended uses when you finish a long 
rest.
Your Bardic Inspiration die changes when you 
reach certain levels in this class. The die becomes a 
d8 at 5th level, a d10 at 10th level, and a d12 at 15th 
level."}
             {:name "Jack of All Trades"
              :level 2
              :description "Starting at 2nd level, you can add half your 
proficiency bonus, rounded down, to any ability 
check you make that doesn't already include your 
proficiency bonus."}
             {:name "Song of Rest"
              :level 2
              :description "Beginning at 2nd level, you can use soothing music 
or oration to help revitalize your wounded allies 
during a short rest. If you or any friendly creatures 
who can hear your performance regain hit points at 
the end of the short rest by spending one or more 
Hit Dice, each of those creatures regains an extra 
1d6 hit points.
The extra hit points increase when you reach 
certain levels in this class: to 1d8 at 9th level, to 
1d10 at 13th level, and to 1d12 at 17th level."}
             {:name "Expertise"
              :level 3
              :description "At 3rd level, choose two of your skill proficiencies. 
Your proficiency bonus is doubled for any ability 
check you make that uses either of the chosen 
proficiencies.
At 10th level, you can choose another two skill 
proficiencies to gain this benefit."}
             {:name "Font of Inspiration"
              :level 5
              :description "Beginning when you reach 5th level, you regain all of 
your expended uses of Bardic Inspiration when you 
finish a short or long rest"}
             {:name "Countercharm"
              :level 6
              :description "At 6th level, you gain the ability to use musical notes 
or words of power to disrupt mind-influencing 
effects. As an action, you can start a performance 
that lasts until the end of your next turn. During that 
time, you and any friendly creatures within 30 feet 
of you have advantage on saving throws against 
being frightened or charmed. A creature must be 
able to hear you to gain this benefit. The 
performance ends early if you are incapacitated or 
silenced or if you voluntarily end it (no action 
required)."}
             {:name "Magical Secrets"
              :level 10
              :description "By 10th level, you have plundered magical 
knowledge from a wide spectrum of disciplines. 
Choose two spells from any class, including this one. 
A spell you choose must be of a level you can cast, as 
shown on the Bard table, or a cantrip.
The chosen spells count as bard spells for you and 
are included in the number in the Spells Known 
column of the Bard table.
You learn two additional spells from any class at 
14th level and again at 18th level."}
             {:name "Superior Inspiration"
              :level 20
              :description "At 20th level, when you roll initiative and have no 
uses of Bardic Inspiration left, you regain one use."}]
    :subclass-level 3
    :subclass-title "Bard College"
    :subclasses [{:name "College of Lore"
                  :profs {:skill-options {:choose 3 :options {:any true}}}
                  :selections (opt5e/bard-magical-secrets 6)
                  :traits [{:name "Cutting Wounds"
                            :level 3
                            :description "Also at 3rd level, you learn how to use your wit to 
distract, confuse, and otherwise sap the confidence 
and competence of others. When a creature that you 
can see within 60 feet of you makes an attack roll, an 
ability check, or a damage roll, you can use your 
reaction to expend one of your uses of Bardic 
Inspiration, rolling a Bardic Inspiration die and 
subtracting the number rolled from the creature's 
roll. You can choose to use this feature after the 
creature makes its roll, but before the GM 
determines whether the attack roll or ability check 
succeeds or fails, or before the creature deals its 
damage. The creature is immune if it can't hear you 
or if it's immune to being charmed."}
                           {:name "Additional Magical Secrets"
                            :level 6
                            :description "At 6th level, you learn two spells of your choice from 
any class. A spell you choose must be of a level you 
can cast, as shown on the Bard table, or a cantrip. 
The chosen spells count as bard spells for you but don't count against the number of bard spells you 
know."}
                           {:name "Peerless Skill"
                            :level 14
                            :description "Starting at 14th level, when you make an ability 
check, you can expend one use of Bardic Inspiration. 
Roll a Bardic Inspiration die and add the number 
rolled to your ability check. You can choose to do so 
after you roll the die for the ability check, but before 
the GM tells you whether you succeed or fail."}]}
                 {:name "College of Valor"
                  :profs {:armor {:medium true
                                  :shields true}
                          :weapon {:martial true}}
                  :levels {6 {:modifiers [(mod5e/extra-attack)]}}
                  :traits [{:name "Combat Inspiration"
                            :level 3}
                           {:name "Extra Attack"
                            :level 6}
                           {:name "Battle Magic"
                            :level 14}]}]}
   character-ref))

(defn blessings-of-knowledge-skill [skill-name]
  (let [skill-kw (common/name-to-kw skill-name)]
    (t/option
     skill-name
     skill-kw
     []
     [(mod5e/skill-proficiency skill-kw)
      (mod5e/skill-expertise skill-kw)])))

(defn cleric-option [character-ref]
  (class-option
   {:name "Cleric",
    :spellcasting {:level-factor 1
                   :cantrips-known {1 3 4 4 10 5}
                   :known-mode :all
                   :ability :wis}
    :spellcaster true
    :hit-die 8,
    :ability-increase-levels [4 8 12 16 19]
    :profs {:armor {:light true :medium true :shields true}
            :weapon {:simple true}
            :save {:wis true :cha true}
            :skill-options {:choose 2 :options {:history true :insight true :medicine true :persuasion true :religion true}}}
    :equipment-choices [{:name "Equipment Pack"
                         :options {:priests-pack 1
                                   :explorers-pack 1}}]
    :weapon-choices [{:name "Cleric Weapon"
                      :options {:mace 1
                                :warhammer 1}}]
    :armor-choices [{:name "Armor"
                     :options {:scale-mail 1
                               :leather 1
                               :chain-mail 1}}]
    :equipment {:shield 1
                :holy-symbol 1}
    :selections [(t/selection
                  "Additional Weapon"
                  [(t/option
                    "Light Crossbow and 20 Bolts"
                    :light-crossbow
                    []
                    [(mod5e/weapon :crossbow--light 1)
                     (mod5e/equipment :bolt 20)])
                   (weapon-option [:simple 1])])]
    :traits [{:level 2 :name "Channel Divinity: Turn Undead" :description "As an action, you present your holy symbol and speak a prayer censuring the undead. Each undead that can see or hear you within 30 feet of you must make a Wisdom saving throw. If the creature fails its saving throw, it is turned for 1 minute or until it takes any damage.\nA turned creature must spend its turns trying to move as far away from you as it can, and it can't willingly move to a space within 30 feet of you. It also can't take reactions. For its action, it can use only the Dash action or try to escape from an effect that prevents it from moving. If there's nowhere to move, the creature can use the Dodge action."}
             {:level 5 :name "Destroy Undead" :description "When an undead fails its saving throw against your Turn Undead feature, the creature is instantly destroyed if its challenge rating is at or below a certain threshold, as shown in the Destroy Undead table."}
             {:level 10 :name "Divine Intervention" :description "You can call on your deity to intervene on your behalf when your need is great.\nImploring your deity's aid requires you to use your action. Describe the assistance you seek, and roll percentile dice. If you roll a number equal to or lower than your cleric level, your deity intervenes. The DM chooses the nature of the intervention; the e ect of any cleric spell or cleric domain spell would be appropriate.\nIf your deity intervenes, you can't use this feature again for 7 days. Otherwise, you can use it again after you  nish a long rest.\nAt 20th level, your call for intervention succeeds automatically, no roll required."}]
    :subclass-level 1
    :subclass-title "Divine Domain"
    :subclasses [{:name "Life Domain"
                  :profs {:armor {:heavy true}}
                  :modifiers [(mod5e/spells-known 1 :bless :wis 1)
                              (mod5e/spells-known 1 :cure-wounds :wis 1)
                              (mod5e/spells-known 2 :lesser-restoration :wis 3)
                              (mod5e/spells-known 2 :spiritual-weapon :wis 3)
                              (mod5e/spells-known 3 :beacon-of-hope :wis 5)
                              (mod5e/spells-known 3 :revivify :wis 5)
                              (mod5e/spells-known 4 :death-ward :wis 7)
                              (mod5e/spells-known 4 :guardian-of-faith :wis 7)
                              (mod5e/spells-known 5 :mass-cure-wounds :wis 9)
                              (mod5e/spells-known 5 :raise-dead :wis 9)]
                  :traits [{:level 1
                            :name "Disciple of Life"
                            :description "Also starting at 1st level, your healing spells are more e ective. Whenever you use a spell of 1st level or higher to restore hit points to a creature, the creature regains additional hit points equal to 2 + the spell's level."}
                           {:level 2
                            :name "Channel Divinity: Preserve Life"
                            :description "Starting at 2nd level, you can use your Channel Divinity to heal the badly injured.\nAs an action, you present your holy symbol and evoke healing energy that can restore a number of hit points equal to  ve times your cleric level. Choose any creatures within 30 feet of you, and divide those hit points among them. This feature can restore a creature to no more than half of its hit point maximum. You can't use this feature on an undead or a construct."}
                           {:level 6
                            :name "Blessed Healer"
                            :description "Beginning at 6th level, the healing spells you cast on others heal you as well. When you cast a spell of 1st level or higher that restores hit points to a creature other than you, you regain hit points equal to 2 + the spell's level."}
                           {:level 8
                            :name "Divine Strike"
                            :description "At 8th level, you gain the ability to infuse your weapon strikes with divine energy. Once on each of your turns when you hit a creature with a weapon attack, you can cause the attack to deal an extra 1d8 radiant damage to the target. When you reach 14th level, the extra damage increases to 2d8."}
                           {:level 17
                            :name "Supreme Healing"
                            :description "Starting at 17th level, when you would normally roll one or more dice to restore hit points with a spell, you instead use the highest number possible for each die. For example, instead of restoring 2d6 hit points to a creature, you restore 12."}]}
                 {:name "Knowledge Domain"
                  :modifiers [(mod5e/spells-known 1 :command :wis 1)
                              (mod5e/spells-known 1 :identify :wis 1)
                              (mod5e/spells-known 2 :augury :wis 3)
                              (mod5e/spells-known 2 :suggestion :wis 3)
                              (mod5e/spells-known 3 :nondetection :wis 5)
                              (mod5e/spells-known 3 :speak-with-dead :wis 5)
                              (mod5e/spells-known 4 :arcane-eye :wis 7)
                              (mod5e/spells-known 4 :confusion :wis 7)
                              (mod5e/spells-known 5 :legend-lore :wis 9)
                              (mod5e/spells-known 5 :scrying :wis 9)]
                  :selections [(opt5e/language-selection opt5e/languages 2)
                               (t/selection
                                "Blessings of Knowledge Skills"
                                (map
                                 blessings-of-knowledge-skill
                                 ["Arcana" "History" "Nature" "Religion"])
                                2
                                2)]
                  :traits [{:level 1
                            :name "Blessings of Knowledge"}
                           {:level 2
                            :name "Channel Divinity: Knowledge of the Ages"}
                           {:level 6
                            :name "Channel Divinity: Read Thoughts"}
                           {:level 8
                            :name "Potent Spellcasting"}
                           {:level 17
                            :name "Visions of the Past"}]}
                 {:name "Light Domain"
                  :modifiers [(mod5e/spells-known 0 :light :wis 1)
                              (mod5e/spells-known 1 :burning-hands :wis 1)
                              (mod5e/spells-known 1 :faerie-fire :wis 1)
                              (mod5e/spells-known 2 :flaming-sphere :wis 3)
                              (mod5e/spells-known 2 :scorching-ray :wis 3)
                              (mod5e/spells-known 3 :daylight :wis 5)
                              (mod5e/spells-known 3 :fireball :wis 5)
                              (mod5e/spells-known 4 :guardian-of-faith :wis 7)
                              (mod5e/spells-known 4 :wall-of-fire :wis 7)
                              (mod5e/spells-known 5 :flame-strike :wis 9)
                              (mod5e/spells-known 5 :scrying :wis 9)]
                  :traits [{:level 1
                            :name "Warding Flame"}
                           {:level 2
                            :name "Channel Divinity: Radiance of the Dawn"}
                           {:level 6
                            :name "Improved Flare"}
                           {:level 8
                            :name "Potent Spellcasting"}
                           {:level 17
                            :name "Corona of Light"}]}
                 {:name "Nature Domain"
                  :profs {:armor {:heavy true}
                          :skill-options {:choose 1 :options {:animal-handling true :nature true :survival true}}}
                  :modifiers [(mod5e/spells-known 1 :animal-friendship :wis 1)
                              (mod5e/spells-known 1 :speak-with-animals :wis 1)
                              (mod5e/spells-known 2 :barkskin :wis 3)
                              (mod5e/spells-known 2 :spike-growth :wis 3)
                              (mod5e/spells-known 3 :plant-growth :wis 5)
                              (mod5e/spells-known 3 :wind-wall :wis 5)
                              (mod5e/spells-known 4 :dominate-beast :wis 7)
                              (mod5e/spells-known 4 :grasping-vine :wis 7)
                              (mod5e/spells-known 5 :insect-plague :wis 9)
                              (mod5e/spells-known 5 :tree-stride :wis 9)]
                  :selections [(t/selection
                                "Druid Cantrip"
                                (opt5e/spell-options (get-in sl/spell-lists [:druid 0]) 0 :wis))]
                  :traits [{:name "Channel Divinity: Charm Animals and Plants"
                            :level 2}
                           {:name "Dampen Elements"
                            :level 6}
                           {:name "Divine Strike"
                            :level 8}
                           {:name "Master of Nature"
                            :level 17}]}
                 {:name "Tempest Domain"
                  :profs {:armor {:heavy true}
                          :weapon {:martial true}}
                  :modifiers [(mod5e/spells-known 1 :fog-cloud :wis 1)
                              (mod5e/spells-known 1 :thunderwave :wis 1)
                              (mod5e/spells-known 2 :gust-of-wind :wis 3)
                              (mod5e/spells-known 2 :shatter :wis 3)
                              (mod5e/spells-known 3 :call-lightning :wis 5)
                              (mod5e/spells-known 3 :sleet-storm :wis 5)
                              (mod5e/spells-known 4 :control-water :wis 7)
                              (mod5e/spells-known 4 :ice-storm :wis 7)
                              (mod5e/spells-known 5 :destructive-wave :wis 9)
                              (mod5e/spells-known 5 :insect-plague :wis 9)]
                  :traits [{:name "Wrath of the Storm"}
                           {:name "Channel Divinity: Destructive Wrath"
                            :level 2}
                           {:name "Thunderbolt Strike"
                            :level 6}
                           {:name "Divine Strike"
                            :level 8}
                           {:name "Stormborn"
                            :level 17}]}
                 {:name "Trickery Domain"
                  :modifiers [(mod5e/spells-known 1 :charm-person :wis 1)
                              (mod5e/spells-known 1 :disguise-self :wis 1)
                              (mod5e/spells-known 2 :mirror-image :wis 3)
                              (mod5e/spells-known 2 :pass-without-trace :wis 3)
                              (mod5e/spells-known 3 :blink :wis 5)
                              (mod5e/spells-known 3 :dispel-magic :wis 5)
                              (mod5e/spells-known 4 :dimension-door :wis 7)
                              (mod5e/spells-known 4 :polymorph :wis 7)
                              (mod5e/spells-known 5 :dominate-person :wis 9)
                              (mod5e/spells-known 5 :modify-memory :wis 9)]
                  :traits [{:name "Blessing of the Trickster"
                            :level 1}
                           {:name "Channel Divinity: Invoke Duplicity"
                            :level 2}
                           {:name "Channel Divinity: Cloak of Shadows"
                            :level 6}
                           {:name "Divine Strike"
                            :level 8}
                           {:name "Improved Duplicity"
                            :level 17}]}
                 {:name "War Domain"
                  :profs {:armor {:heavy true}
                          :weapon {:martial true}}
                  :modifiers [(mod5e/spells-known 1 :divine-favor :wis 1)
                              (mod5e/spells-known 1 :shield-of-faith :wis 1)
                              (mod5e/spells-known 2 :magic-weapon :wis 3)
                              (mod5e/spells-known 2 :spiritual-weapon :wis 3)
                              (mod5e/spells-known 3 :crusaders-mantle :wis 5)
                              (mod5e/spells-known 3 :spirits-guardians :wis 5)
                              (mod5e/spells-known 4 :freedom-of-movement :wis 7)
                              (mod5e/spells-known 4 :stoneskin :wis 7)
                              (mod5e/spells-known 5 :flame-strike :wis 9)
                              (mod5e/spells-known 5 :hold-monster :wis 9)]
                  :traits [{:name "War Priest"
                            :level 1}
                           {:name "Channel Divinity: Guided Strike"
                            :level 2}
                           {:name "Channel Divinity: War God's Blessing"
                            :level 6}
                           {:name "Divine Strike"
                            :level 8}
                           {:name "Avatar of Battle"
                            :level 17}]}
                 {:name "Arcana Domain"
                  :source "Sword Coast Adventurer's Guide"
                  :traits [{:name "Arcane Initiate"}
                           {:name "Channel Divinity: Arcane Abjuration"
                            :level 2}
                           {:name "Spell Breaker"
                            :level 6}
                           {:name "Potent Spellcasting"
                            :level 8}
                           {:name "Arcane Mastery"}]}]}
   character-ref))

(defn druid-option [character-ref]
  (class-option
   {:name "Druid"
    :hit-die 8
    :spellcaster true
    :spellcasting {:level-factor 1
                   :cantrips-known {:1 2 :4 3 :10 4}
                   :known-mode :all
                   :ability :wis}
    :ability-increase-levels [4 6 8 12 14 16 19]
    :profs {:armor {:light true :medium true :shields true}
            :weapon {:club true :dagger true :dart true :javelin true :mace true :quarterstaff true :scimitar true :sickle true :sling true :spear true}
            :tool {:herbalism-kit true}
            :save {:int true :wis true}
            :skill-options {:choose 2 :options {:arcana true :animal-handling true :insight true :medicine true :nature true :perception true :religion true :survival true}}}
    :armor {:leather 1}
    :equipment {:explorers-pack 1
                :druidic-focus 1}
    :modifiers [(mod5e/language "Druidic" :druidic)]
    :selections [(t/selection
                  "Wooden Shield or Simple Weapon"
                  [(t/option
                    "Wooden Shield"
                    :shield
                    []
                    [(mod5e/armor :shield 1)])
                   (weapon-option [:simple 1])])
                 (t/selection
                  "Melee Weapon"
                  [(t/option
                    "Scimitar"
                    :scimitar
                    []
                    [(mod5e/weapon :scimitar 1)])
                   (t/option
                    "Simple Melee Weapon"
                    :simple-melee
                    [(t/selection
                      "Simple Melee Weapon"
                      (opt5e/simple-melee-weapon-options 1))]
                    [])])]
    :traits [{:name "Druidic"
              :description "You know Druidic, the secret language of druids. You 
can speak the language and use it to leave hidden 
messages. You and others who know this language 
automatically spot such a message. Others spot the 
message's presence with a successful DC 15 Wisdom 
(Perception) check but can't decipher it without 
magic."}
             {:name "Wild Shape"
              :description "Starting at 2nd level, you can use your action to 
magically assume the shape of a beast that you have 
seen before. You can use this feature twice. You 
regain expended uses when you finish a short or 
long rest.
Your druid level determines the beasts you can 
transform into, as shown in the Beast Shapes table. 
At 2nd level, for example, you can transform into any 
beast that has a challenge rating of 1/4 or lower that 
doesn't have a flying or swimming speed. (see the Players Handbook for further details)"}
             {:name "Timeless Body"
              :level 18
              :description "Starting at 18th level, the primal magic that you 
wield causes you to age more slowly. For every 10 
years that pass, your body ages only 1 year."}
             {:name "Beast Spells"
              :level 18
              :description "Beginning at 18th level, you can cast many of your 
druid spells in any shape you assume using Wild 
Shape. You can perform the somatic and verbal 
components of a druid spell while in a beast shape, 
but you aren't able to provide material components."}
             {:name "Archdruid"
              :level 20
              :description "At 20th level, you can use your Wild Shape an 
unlimited number of times.
Additionally, you can ignore the verbal and 
somatic components of your druid spells, as well as 
any material components that lack a cost and aren't 
consumed by a spell. You gain this benefit in both 
your normal shape and your beast shape from Wild 
Shape."}]
    :subclass-level 2
    :subclass-title "Druid Circle"
    :subclasses [{:name "Circle of the Land"
                  :selections [(t/selection
                                "Bonus Cantrip"
                                (opt5e/spell-options (get-in sl/spell-lists [:druid 0]) 0 :wis))
                               (t/selection
                                "Land Type"
                                [(t/option
                                  "Arctic"
                                  :arctic
                                  []
                                  [(mod5e/spells-known 3 :slow :wis 5)
                                   (mod5e/spells-known 5 :cone-of-cold :wis 9)])
                                 (t/option
                                  "Coast"
                                  :coast
                                  []
                                  [(mod5e/spells-known 2 :mirror-image :wis 3)
                                   (mod5e/spells-known 2 :misty-step :wis 3)])
                                 (t/option
                                  "Desert"
                                  :desert
                                  []
                                  [(mod5e/spells-known 2 :blur :wis 3)
                                   (mod5e/spells-known 2 :silence :wis 3)
                                   (mod5e/spells-known 3 :create-food-and-water :wis 5)])
                                 (t/option
                                  "Forest"
                                  :forest
                                  []
                                  [(mod5e/spells-known 2 :spider-climb :wis 3)
                                   (mod5e/spells-known 4 :divination :wis 7)])
                                 (t/option
                                  "Grassland"
                                  :grassland
                                  []
                                  [(mod5e/spells-known 2 :invisibility :wis 3)
                                   (mod5e/spells-known 3 :haste :wis 5)
                                   (mod5e/spells-known 4 :divination :wis 7)
                                   (mod5e/spells-known 5 :dream :wis 9)])
                                 (t/option
                                  "Mountain"
                                  :mountain
                                  []
                                  [(mod5e/spells-known 2 :spider-climb :wis 3)
                                   (mod5e/spells-known 3 :lightning-bolt :wis 5)])
                                 (t/option
                                  "Swamp"
                                  :swamp
                                  []
                                  [(mod5e/spells-known 2 :darkness :wis 3)
                                   (mod5e/spells-known 2 :melfs-acid-arrow :wis 3)
                                   (mod5e/spells-known 3 :stinking-cloud :wis 5)])
                                 (t/option
                                  "Underdark"
                                  :underdark
                                  []
                                  [(mod5e/spells-known 2 :spider-climb :wis 3)
                                   (mod5e/spells-known 2 :web :wis 3)
                                   (mod5e/spells-known 3 :stinking-cloud :wis 5)
                                   (mod5e/spells-known 3 :gaseous-form :wis 5)
                                   (mod5e/spells-known 4 :greater-invisibility :wis 7)
                                   (mod5e/spells-known 5 :cloudkill :wis 9)])])]
                  :modifiers []
                  :traits [{:name "Natural Recovery"
                            :level 2
                            :description "Starting at 2nd level, you can regain some of your 
magical energy by sitting in meditation and 
communing with nature. During a short rest, you 
choose expended spell slots to recover. The spell 
slots can have a combined level that is equal to or 
less than half your druid level (rounded up), and 
none of the slots can be 6th level or higher. You can't 
use this feature again until you finish a long rest.
For example, when you are a 4th-level druid, you 
can recover up to two levels worth of spell slots. You 
can recover either a 2nd-level slot or two 1st-level 
slots"}
                           {:name "Land's Stride"
                            :level 6
                            :description "Starting at 6th level, moving through nonmagical 
difficult terrain costs you no extra movement. You 
can also pass through nonmagical plants without 
being slowed by them and without taking damage 
from them if they have thorns, spines, or a similar 
hazard.
In addition, you have advantage on saving throws 
against plants that are magically created or 
manipulated to impede movement, such those 
created by the entangle spell."}
                           {:name "Nature's Ward"
                            :level 10
                            :description "When you reach 10th level, you can't be charmed or 
frightened by elementals or fey, and you are immune 
to poison and disease."}
                           {:name "Nature's Santuary"
                            :level 14
                            :description "When you reach 14th level, creatures of the natural 
world sense your connection to nature and become 
hesitant to attack you. When a beast or plant 
creature attacks you, that creature must make a 
Wisdom saving throw against your druid spell save 
DC. On a failed save, the creature must choose a 
different target, or the attack automatically misses. 
On a successful save, the creature is immune to this 
effect for 24 hours.
The creature is aware of this effect before it makes 
its attack against you."}]}
                 {:name "Circle of the Moon"
                  :traits [{:name "Combat Wild Shape"
                            :level 2}
                           {:name "Circle Forms"
                            :level 2}
                           {:name "Primal Strike"
                            :level 6}
                           {:name "Elemental Wild Shape"
                            :level 10}
                           {:name "Thousand Forms"
                            :level 14}]}]}
   character-ref))

(defn eldritch-knight-spell? [s]
  (let [school (:school s)]
    (or (= school "evocation")
        (= school "abjuration"))))

(defn arcane-trickster-spell? [s]
  (let [school (:school s)]
    (or (= school "enchantment")
        (= school "illusion"))))

(defn total-levels-prereq [level]
  (fn [c] (>= (es/entity-val c :total-levels) level)))

(defn add-level-prereq [template-obj level]
  (assoc
   template-obj
   ::t/prereq-fn
   (total-levels-prereq level)))

(defn fighter-option [character-ref]
  (class-option
   {:name "Fighter",
    :hit-die 10,
    :ability-increase-levels [4 8 12 16 19]
    :profs {:armor {:light true :medium true :heavy true :shields true}
            :weapon {:simple true :martial true} 
            :save {:str true :con true}
            :skill-options {:choose 2 :options {:acrobatics true :animal-handling true :athletics true :history true :insight true :intimidation true :perception true :survival true}}}
    :equipment-choices [{:name "Equipment Pack"
                         :options {:dungeoneers-pack 1
                                   :explorers-pack 1}}]
    :traits [{:name "Second Wind" :description "You have a limited well of stamina that you can draw on to protect yourself from harm. On your turn, you can use a bonus action to regain hit points equal to 1d10 + your fighter level.\nOnce you use this feature, you must  nish a short or long rest before you can use it again."}
             {:level 2 :name "Action Surge" :description "You can push yourself beyond your normal limits for a moment. On your turn, you can take one additional action on top of your regular action and a possible bonus action.\nOnce you use this feature, you must  nish a short or long rest before you can use it again. Starting at 17th level, you can use it twice before a rest, but only once on the same turn."}
             {:level 5 :name "Extra Attack" :description "You can attack twice, instead of once, whenever you take the Attack action on your turn.\nThe number of attacks increases to three when you reach 11th level in this class and to four when you reach 20th level in this class."}
             {:level 9 :name "Indomitable" :description "You can reroll a saving throw that you fail. If you do so, you must use the new roll, and you can't use this feature again until you  nish a long rest.\nYou can use this feature twice between long rests starting at 13th level and three times between long rests starting at 17th level."}]
    :subclass-level 3
    :subclass-title "Martial Archetype"
    :levels {5 {:modifiers [(mod5e/extra-attack)]}}
    :selections [(opt5e/fighting-style-selection character-ref)
                 (t/selection
                  "Armor"
                  [(t/option
                    "Chain Mail"
                    :chain-mail
                    []
                    [(mod5e/armor :chain-mail 1)])
                   (t/option
                    "Leather Armor, Longbow, 20 Arrows"
                    :leather
                    []
                    [(mod5e/armor :leather 1)
                     (mod5e/weapon :longbow 1)
                     (mod5e/equipment :arrow 20)])])
                 (t/selection
                  "Weapons"
                  [(t/option
                    "Martial Weapon and Shield"
                    :martial-and-shield
                    [(t/selection
                      "Martial Weapon"
                      (opt5e/martial-weapon-options 1))]
                    [(mod5e/armor :shield 1)])
                   (t/option
                    "Two Martial Weapons"
                    :two-martial
                    [(t/selection
                      "Martial Weapons"
                      (opt5e/martial-weapon-options 1)
                      2
                      2)]
                    [])])
                 (t/selection
                  "Additional Weapons"
                  [(t/option
                    "Light Crossbow and 20 Bolts"
                    :light-crossbow
                    []
                    [(mod5e/weapon :crossbow--light 1)
                     (mod5e/equipment :bolt 20)])
                   (t/option
                    "Two Handaxes"
                    :two-handaxes
                    []
                    [(mod5e/weapon :handaxe 2)])])]
    :subclasses [{:name "Champion"
                  :selections [(add-level-prereq
                                (opt5e/fighting-style-selection character-ref)
                                10)]
                  :levels {3 {:modifiers [(mod5e/critical 19)]}
                           7 {:modifiers [(mod/modifier ?default-skill-bonus (let [b (int (/ ?prof-bonus 2))] {:str b :dex b :con b}))]}
                           15 {:modifiers [(mod5e/critical 18)]}}
                  :traits [{:level 3
                            :name "Improved Critical"
                            :description "Your weapon attacks score a critical hit on a roll of 19 or 20."}
                           {:level 7
                            :name "Remarkable Athlete"
                            :description "You can add half your proficiency bonus (round up) to any Strength, Dexterity, or Constitution check you make that doesn't already use your proficiency bonus.\nIn addition, when you make a running long jump, the distance you can cover increases by a number of feet equal to your Strength modifier."}
                           {:level 10
                            :name "Additional Fighting Style"
                            :description "You can choose a second option from the Fighting Style class feature."}
                           {:level 15
                            :name "Superior Critical"
                            :description "Your weapon attacks score a critical hit on a roll of 18-20."}
                           {:level 18
                            :name "Survivor"
                            :description "You attain the pinnacle of resilience in battle. At the start of each of your turns, you regain hit points equal to 5 + your Constitution modifier if you have no more than half of your hit points left. You don't gain this bene t if you have 0 hit points."}]}
                 {:name "Battle Master"
                  :selections [(t/selection
                                "Martial Maneuvers"
                                opt5e/maneuver-options
                                3 3)
                               (opt5e/tool-selection (map :key opt5e/artisans-tools) 1)]
                  :traits [{:name "Combat Superiority"
                            :level 3}
                           {:name "Student of War"
                            :level 3}
                           {:name "Know Your Enemy"
                            :level 7}
                           {:name "Improved Combat Superiority"
                            :level 10}
                           {:name "Relentless"
                            :level 15}]}
                 {:name "Eldritch Knight"
                  :spellcaster true
                  :spellcasting {:level-factor 3
                                 :spell-list :wizard
                                 :cantrips-known {3 2 10 3}
                                 :known-mode :schedule
                                 :spells-known {3 {:num 3
                                                   :restriction eldritch-knight-spell?}
                                                4 {:num 1
                                                   :restriction eldritch-knight-spell?}
                                                7 {:num 1
                                                   :restriction eldritch-knight-spell?}
                                                8 1
                                                10 {:num 1
                                                    :restriction eldritch-knight-spell?}
                                                11 {:num 1
                                                    :restriction eldritch-knight-spell?}
                                                13 {:num 1
                                                    :restriction eldritch-knight-spell?}
                                                14 1
                                                16 {:num 1
                                                    :restriction eldritch-knight-spell?}
                                                19 {:num 1
                                                    :restriction eldritch-knight-spell?}
                                                20 1}
                                 :ability :int}
                  
                  :traits [{:name "Weapon Bond"
                            :level 3}
                           {:name "War Magic"
                            :level 7}
                           {:name "Eldritch Strike"
                            :level 10}
                           {:name "Arcane Charge"
                            :level 15}
                           {:name "Improved War Magic"
                            :level 18}]}
                 {:name "Purple Dragon Knight"
                  :source "Sword Coast Adventurer's Guide"
                  :traits [{:name "Rallying Cry"
                            :level 3}
                           {:name "Royal Envoy"
                            :level 7}
                           {:name "Inspiring Surge"
                            :level 10}
                           {:name "Bulwark"
                            :level 15}]}]}
   character-ref))

(defn monk-option [character-ref]
  (class-option
   {:name "Monk"
    :hit-die 8
    :ability-increase-levels [4 8 10 16 19]
    :unarmored-abilities [:wis]
    :martial-arts {1 4, 2 4, 3 4, 4 4, 5 6, 6 6, 7 6, 8 6, 9 6, 10 6, 11 8, 12 8, 13 8, 14 8, 15 8, 16 8, 17 10, 18 10, 19 10, 20 10}
    :profs {:armor {:light true}   
            :weapon {:simple true :shortsword true}
            :save {:dex true :str true}
            :skill-options {:choose 2 :options {:acrobatics true :athletics true :history true :insight true :religion true :stealth true}}}
    :equipment-choices [{:name "Equipment Pack"
                         :options {:dungeoneers-pack 1
                                   :explorers-pack 1}}]
    :weapon-choices [{:name "Weapon"
                      :options {:shortsword 1
                                :simple 1}}]
    :modifiers [(mod/modifier ?armor-class (+ (?ability-bonuses :wis) ?armor-class))]
    :levels {5 {:modifiers [(mod5e/extra-attack)]}
             10 {:modifiers [(mod5e/immunity :poison)
                             (mod5e/immunity :disease)]}
             13 {:modifiers (map
                             (fn [{:keys [name key]}]
                               (mod5e/language name key))
                             opt5e/languages)}
             14 {:modifiers [(mod5e/saving-throws char5e/ability-keys)]}}
    :equipment {:dart 10}
    :traits [{:name "Ki"
              :level 2
              :description "Starting at 2nd level, your training allows you to 
harness the mystic energy of ki. Your access to this 
energy is represented by a number of ki points. Your 
monk level determines the number of points you 
have, as shown in the Ki Points column of the Monk 
table.
You can spend these points to fuel various ki 
features. You start knowing three such features: 
Flurry of Blows, Patient Defense, and Step of the 
Wind. You learn more ki features as you gain levels 
in this class.
When you spend a ki point, it is unavailable until 
you finish a short or long rest, at the end of which 
you draw all of your expended ki back into yourself. 
You must spend at least 30 minutes of the rest 
meditating to regain your ki points.
Some of your ki features require your target to 
make a saving throw to resist the feature's effects. 
The saving throw DC is calculated as follows:
Ki save DC = 8 + your proficiency bonus +
your Wisdom modifier"}
             {:name "Flurry of Blows"
              :level 2
              :description "Immediately after you take the Attack action on your 
turn, you can spend 1 ki point to make two unarmed 
strikes as a bonus action."}
             {:name "Patient Defense"
              :level 2
              :description "You can spend 1 ki point to take the Dodge action as 
a bonus action on your turn."}
             {:name "Step of the Wind"
              :level 2
              :description "You can spend 1 ki point to take the Disengage or 
Dash action as a bonus action on your turn, and your 
jump distance is doubled for the turn."}
             {:name "Unarmored Movement"
              :level 2
              :description "Starting at 2nd level, your speed increases by 10 feet 
while you are not wearing armor or wielding a 
shield. This bonus increases when you reach certain 
monk levels, as shown in the Monk table.
At 9th level, you gain the ability to move along 
vertical surfaces and across liquids on your turn 
without falling during the move."}
             {:name "Deflect Missiles"
              :level 3
              :description "Starting at 3rd level, you can use your reaction to 
deflect or catch the missile when you are hit by a 
ranged weapon attack. When you do so, the damage 
you take from the attack is reduced by 1d10 + your 
Dexterity modifier + your monk level.
If you reduce the damage to 0, you can catch the 
missile if it is small enough for you to hold in one 
hand and you have at least one hand free. If you 
catch a missile in this way, you can spend 1 ki point 
to make a ranged attack with the weapon or piece of 
ammunition you just caught, as part of the same 
reaction. You make this attack with proficiency, 
regardless of your weapon proficiencies, and the
missile counts as a monk weapon for the attack, 
which has a normal range of 20 feet and a long range 
of 60 feet."}
             {:name "Slow Fall"
              :level 4
              :description "Beginning at 4th level, you can use your reaction 
when you fall to reduce any falling damage you take 
by an amount equal to five times your monk level."}
             {:name "Extra Attack"
              :level 5
              :description "Beginning at 5th level, you can attack twice, instead 
of once, whenever you take the Attack action on your 
turn."}
             {:name "Stunning Strike"
              :level 5
              :description "Starting at 5th level, you can interfere with the flow 
of ki in an opponent's body. When you hit another 
creature with a melee weapon attack, you can spend 
1 ki point to attempt a stunning strike. The target 
must succeed on a Constitution saving throw or be 
stunned until the end of your next turn."}
             {:name "Ki-Empowered Strikes"
              :level 6
              :description "Starting at 6th level, your unarmed strikes count as 
magical for the purpose of overcoming resistance 
and immunity to nonmagical attacks and damage"}
             {:name "Evasion"
              :level 7
              :description "At 7th level, your instinctive agility lets you dodge 
out of the way of certain area effects, such as a blue 
dragon's lightning breath or a fireball spell. When 
you are subjected to an effect that allows you to 
make a Dexterity saving throw to take only half 
damage, you instead take no damage if you succeed 
on the saving throw, and only half damage if you fail."}
             {:name "Stillness of Mind"
              :level 7
              :description "Starting at 7th level, you can use your action to end 
one effect on yourself that is causing you to be 
charmed or frightened."}
             {:name "Purity of Body"
              :level 10
              :description "At 10th level, your mastery of the ki flowing through 
you makes you immune to disease and poison."}
             {:name "Tongue of the Sun and Moon"
              :level 13
              :description "Starting at 13th level, you learn to touch the ki of 
other minds so that you understand all spoken 
languages. Moreover, any creature that can 
understand a language can understand what you say."}
             {:name "Diamond Soul"
              :level 14
              :description "Beginning at 14th level, your mastery of ki grants 
you proficiency in all saving throws.
Additionally, whenever you make a saving throw 
and fail, you can spend 1 ki point to reroll it and take 
the second result."}
             {:name "Timeless Body"
              :level 15
              :description "At 15th level, your ki sustains you so that you suffer 
none of the frailty of old age, and you can't be aged 
magically. You can still die of old age, however. In 
addition, you no longer need food or water."}
             {:name "Empty Body"
              :level 18
              :description "Beginning at 18th level, you can use your action to 
spend 4 ki points to become invisible for 1 minute. 
During that time, you also have resistance to all 
damage but force damage.
Additionally, you can spend 8 ki points to cast the 
astral projection spell, without needing material 
components. When you do so, you can't take any 
other creatures with you."}
             {:name "Perfect Self"
              :level 20
              :description "At 20th level, when you roll for initiative and have 
no ki points remaining, you regain 4 ki points."}]
    :subclass-level 3
    :subclass-title "Monastic Tradition"
    :subclasses [{:name "Way of the Open Hand"
                  :traits [{:name "Open Hand Technique"
                            :level 3
                            :description "Starting when you choose this tradition at 3rd level, 
you can manipulate your enemy's ki when you 
harness your own. Whenever you hit a creature with 
one of the attacks granted by your Flurry of Blows, 
you can impose one of the following effects on that 
target:
* It must succeed on a Dexterity saving throw or be 
knocked prone.
* It must make a Strength saving throw. If it fails, 
you can push it up to 15 feet away from you. 
* It can't take reactions until the end of your next 
turn."}
                           {:name "Wholeness of Body"
                            :level 6
                            :description "At 6th level, you gain the ability to heal yourself. As 
an action, you can regain hit points equal to three 
times your monk level. You must finish a long rest 
before you can use this feature again."}
                           {:name "Tranquility"
                            :level 11
                            :description "Beginning at 11th level, you can enter a special 
meditation that surrounds you with an aura of peace. 
At the end of a long rest, you gain the effect of a 
sanctuary spell that lasts until the start of your next 
long rest (the spell can end early as normal). The 
saving throw DC for the spell equals 8 + your 
Wisdom modifier + your proficiency bonus."}
                           {:name "Quivering Palm"
                            :level 17
                            :description "At 17th level, you gain the ability to set up lethal 
vibrations in someone's body. When you hit a 
creature with an unarmed strike, you can spend 3 ki 
points to start these imperceptible vibrations, which 
last for a number of days equal to your monk level. 
The vibrations are harmless unless you use your 
action to end them. To do so, you and the target 
must be on the same plane of existence. When you 
use this action, the creature must make a 
Constitution saving throw. If it fails, it is reduced to 0 
hit points. If it succeeds, it takes 10d10 necrotic 
damage.
You can have only one creature under the effect of 
this feature at a time. You can choose to end the 
vibrations harmlessly without using an action."}]}
                 {:name "Way of Shadow"
                  :traits [{:name "Shadow Arts"
                            :level 3}
                           {:name "Shadow Step"
                            :level 6}
                           {:name "Cloak of Shadows"
                            :level 11}
                           {:name "Opportunist"
                            :level 17}]}
                 {:name "Way of the Four Elements"
                  :levels {3 {:selections [(opt5e/monk-elemental-disciplines)]}
                           6 {:selections [(opt5e/monk-elemental-disciplines)]}
                           11 {:selections [(opt5e/monk-elemental-disciplines)]}
                           17 {:selections [(opt5e/monk-elemental-disciplines)]}}
                  :traits [{:name "Disciple of the Elements"
                            :level 3}
                           {:name "Elemental Discipline: Elemental Attunement"}]}
                 {:name "Way of the Long Death"
                  :source "Sword Coast Adventurer's Guide"
                  :traits [{:name "Touch of Death"
                            :level 3}
                           {:name "Hour of Reaping"
                            :level 6}
                           
                           {:name "Mastery of Death"
                            :level 11}
                           {:name "Touch of the Long Death"
                            :level 17}]}
                 {:name "Way of the Sun Soul"
                  :source "Sword Coast Adventurer's Guide"
                  :traits [{:name "Radiant Sun Bolt"
                            :level 2}
                           {:name "Searing Arc Strike"
                            :level 6}
                           {:name "Searing Sunburst"
                            :level 11}
                           {:name "Sun Shield"
                            :level 17}]}
                 {:name "Way of the Kensei"
                  :source "Unearthed Arcana: Monk"
                  :traits [{:name "Path of the Kensei"
                            :level 3}
                           {:name "One with the Blade"
                            :level 6}
                           {:name "Sharpen the Blade"
                            :level 11}
                           {:name "Unerring Accuracy"}]}
                 {:name "Way of the Tranquility"
                  :source "Unearthed Arcana: Monk"
                  :traits [{:name "Path of Tranquility"
                            :level 3}
                           {:name "Healing Hands"
                            :level 3}
                           {:name "Emissary of Peace"
                            :level 6}
                           {:name "Douse the Flames of War"
                            :level 11}
                           {:name "Anger of a Gentle Soul"
                            :level 17}]}]}
   character-ref))

(defn paladin-option [character-ref]
  (class-option
   {:name "Paladin"
    :spellcaster true
    :spellcasting {:level-factor 2
                   :known-mode :all
                   :ability :cha}
    :hit-die 10
    :ability-increase-levels [4 8 12 16 19]
    :profs {:armor {:light true :medium true :heavy true :shields true}
            :weapon {:simple true :martial true}
            :save {:wis true :cha true}
            :skill-options {:choose 2 :options {:athletics true :insight true :intimidation true :medicine true :persuasion true :religion true}}}
    :equipment-choices [{:name "Equipment Pack"
                         :options {:priests-pack 1
                                   :explorers-pack 1}}]
    :armor {:chain-mail 1}
    :levels {2 {:selections [(opt5e/fighting-style-selection character-ref #{:defense :dueling :great-weapon-fighting :protection})]}
             3 {:modifiers [(mod5e/immunity :disease)]}
             5 {:modifiers [(mod5e/extra-attack)]}}
    :selections [(t/selection
                  "Weapons"
                  [(t/option
                    "Martial Weapon and Shield"
                    :martial-and-shield
                    [(t/selection
                      "Martial Weapon"
                      (opt5e/martial-weapon-options 1))]
                    [(mod5e/armor :shield 1)])
                   (t/option
                    "Two Martial Weapons"
                    :two-martial
                    [(t/selection
                      "Martial Weapons"
                      (opt5e/martial-weapon-options 1)
                      2
                      2)]
                    [])])
                 (t/selection
                  "Melee Weapon"
                  [(t/option
                    "Five Javelins"
                    :javelins
                    []
                    [(mod5e/weapon :javelin 5)])
                   (t/option
                    "Simple Melee Weapon"
                    :simple-melee
                    [(t/selection
                      "Simple Melee Weapon"
                      (opt5e/simple-melee-weapon-options 1))]
                    [])])]
    :traits [{:name "Divine Sense"
              :description "The presence of strong evil registers on your senses 
like a noxious odor, and powerful good rings like 
heavenly music in your ears. As an action, you can 
open your awareness to detect such forces. Until the 
end of your next turn, you know the location of any 
celestial, fiend, or undead within 60 feet of you that 
is not behind total cover. You know the type 
(celestial, fiend, or undead) of any being whose 
presence you sense, but not its identity (the vampire 
Count Strahd von Zarovich, for instance). Within the 
same radius, you also detect the presence of any 
place or object that has been consecrated or 
desecrated, as with the hallow spell.
You can use this feature a number of times equal 
to 1 + your Charisma modifier. When you finish a 
long rest, you regain all expended uses."}
             {:name "Lay on Hands"
              :description "Your blessed touch can heal wounds. You have a 
pool of healing power that replenishes when you 
take a long rest. With that pool, you can restore a 
total number of hit points equal to your paladin level 
× 5.
As an action, you can touch a creature and draw 
power from the pool to restore a number of hit 
points to that creature, up to the maximum amount 
remaining in your pool.
Alternatively, you can expend 5 hit points from 
your pool of healing to cure the target of one disease 
or neutralize one poison affecting it. You can cure 
multiple diseases and neutralize multiple poisons 
with a single use of Lay on Hands, expending hit 
points separately for each one.
This feature has no effect on undead and 
constructs."}
             {:name "Divine Smite"
              :level 2
              :description "Starting at 2nd level, when you hit a creature with a 
melee weapon attack, you can expend one spell slot 
to deal radiant damage to the target, in addition to 
the weapon's damage. The extra damage is 2d8 for a 
1st-level spell slot, plus 1d8 for each spell level 
Not for resale. Permission granted to print or photocopy this document for personal use only. System Reference Document 5.0 32
higher than 1st, to a maximum of 5d8. The damage 
increases by 1d8 if the target is an undead or a fiend."}
             {:name "Divine Health"
              :level 3
              :description "By 3rd level, the divine magic flowing through you 
makes you immune to disease."}
             {:name "Extra Attack"
              :level 5
              :description "Beginning at 5th level, you can attack twice, instead 
of once, whenever you take the Attack action on your 
turn."}
             {:name "Aura of Protection"
              :level 6
              :description "Starting at 6th level, whenever you or a friendly 
creature within 10 feet of you must make a saving 
throw, the creature gains a bonus to the saving 
throw equal to your Charisma modifier (with a 
minimum bonus of +1). You must be conscious to 
grant this bonus.
At 18th level, the range of this aura increases to 30 
feet."}
             {:name "Aura of Courage"
              :level 10
              :description "Starting at 10th level, you and friendly creatures 
within 10 feet of you can't be frightened while you 
are conscious.
At 18th level, the range of this aura increases to 30 
feet."}
             {:name "Improved Divine Smite"
              :level 11
              :description "By 11th level, you are so suffused with righteous 
might that all your melee weapon strikes carry 
divine power with them. Whenever you hit a 
creature with a melee weapon, the creature takes an 
extra 1d8 radiant damage. If you also use your 
Divine Smite with an attack, you add this damage to 
the extra damage of your Divine Smite."}
             {:name "Cleansing Touch"
              :level 14
              :description "Beginning at 14th level, you can use your action to 
end one spell on yourself or on one willing creature 
that you touch.
You can use this feature a number of times equal 
to your Charisma modifier (a minimum of once). You 
regain expended uses when you finish a long rest."}]
    :subclass-level 3
    :subclass-title "Sacred Oath"
    :subclasses [{:name "Oath of Devotion"
                  :modifiers [(mod5e/spells-known 1 :protection-from-evil-and-good :wis 3)
                              (mod5e/spells-known 1 :sanctuary :wis 3)
                              (mod5e/spells-known 2 :lesser-restoration :wis 5)
                              (mod5e/spells-known 2 :zone-of-truth :wis 5)
                              (mod5e/spells-known 3 :beacon-of-hope :wis 9)
                              (mod5e/spells-known 3 :dispel-magic :wis 9)
                              (mod5e/spells-known 4 :freedom-of-movement :wis 13)
                              (mod5e/spells-known 4 :guardian-of-faith :wis 13)
                              (mod5e/spells-known 5 :commune :wis 17)
                              (mod5e/spells-known 5 :flame-strike :wis 17)]
                  :traits [{:name "Channel Divinity"
                            :level 3
                            :description "When you take this oath at 3rd level, you gain the 
following two Channel Divinity options.
Sacred Weapon. As an action, you can imbue one 
weapon that you are holding with positive energy, 
using your Channel Divinity. For 1 minute, you add 
your Charisma modifier to attack rolls made with 
that weapon (with a minimum bonus of +1). The 
weapon also emits bright light in a 20-foot radius 
and dim light 20 feet beyond that. If the weapon is 
not already magical, it becomes magical for the 
duration.
You can end this effect on your turn as part of any 
other action. If you are no longer holding or carrying 
this weapon, or if you fall unconscious, this effect 
ends.
Turn the Unholy. As an action, you present your 
holy symbol and speak a prayer censuring fiends 
and undead, using your Channel Divinity. Each fiend 
or undead that can see or hear you within 30 feet of 
you must make a Wisdom saving throw. If the 
creature fails its saving throw, it is turned for 1 
minute or until it takes damage.
A turned creature must spend its turns trying to 
move as far away from you as it can, and it can't 
willingly move to a space within 30 feet of you. It 
also can't take reactions. For its action, it can use 
only the Dash action or try to escape from an effect 
that prevents it from moving. If there's nowhere to 
move, the creature can use the Dodge action."}
                           {:name "Aura of Devotion"
                            :level 7
                            :description "Starting at 7th level, you and friendly creatures 
within 10 feet of you can't be charmed while you are 
conscious.
At 18th level, the range of this aura increases to 30 
feet."}
                           {:name "Purity of Spirit"
                            :level 15
                            :description "Beginning at 15th level, you are always under the 
effects of a protection from evil and good spell."}
                           {:name "Holy Nimbus"
                            :level 20
                            :description "At 20th level, as an action, you can emanate an aura 
of sunlight. For 1 minute, bright light shines from 
you in a 30-foot radius, and dim light shines 30 feet 
beyond that.
Whenever an enemy creature starts its turn in the 
bright light, the creature takes 10 radiant damage.
In addition, for the duration, you have advantage 
on saving throws against spells cast by fiends or 
undead.
Once you use this feature, you can't use it again 
until you finish a long rest."}]}
                 {:name "Oath of the Ancients"
                  :modifiers [(mod5e/spells-known 1 :ensnaring-strike :wis 3)
                              (mod5e/spells-known 1 :speak-with-animals :wis 3)
                              (mod5e/spells-known 2 :misty-step :wis 5)
                              (mod5e/spells-known 2 :moonbeam :wis 5)
                              (mod5e/spells-known 3 :plant-growth :wis 9)
                              (mod5e/spells-known 3 :protection-from-energy :wis 9)
                              (mod5e/spells-known 4 :ice-storm :wis 13)
                              (mod5e/spells-known 4 :stoneskin :wis 13)
                              (mod5e/spells-known 5 :commune-with-nature :wis 17)
                              (mod5e/spells-known 5 :tree-stride :wis 17)]
                  :traits [{:name "Channel Divinity: Nature's Wrath"
                            :level 3}
                           {:name "Channel Divinity: Turn the Faithless"
                            :level 3}
                           {:name "Aura of Warding"
                            :level 7}
                           {:name "Undying Sentinal"
                            :level 15}
                           {:name "Elder Champion"
                            :level 20}]}
                 {:name "Oath of Vengeance"
                  :modifiers [(mod5e/spells-known 1 :bane :wis 3)
                              (mod5e/spells-known 1 :hunters-mark :wis 3)
                              (mod5e/spells-known 2 :hold-person :wis 5)
                              (mod5e/spells-known 2 :misty-step :wis 5)
                              (mod5e/spells-known 3 :haste :wis 9)
                              (mod5e/spells-known 3 :protection-from-energy :wis 9)
                              (mod5e/spells-known 4 :banishment :wis 13)
                              (mod5e/spells-known 4 :dimension-door :wis 13)
                              (mod5e/spells-known 5 :hold-monster :wis 17)
                              (mod5e/spells-known 5 :scrying :wis 17)]
                  :traits [{:name "Channel Divinity: Abjure Enemy"
                            :level 3}
                           {:name "Channel Divinity: Vow of Eternity"
                            :level 3}
                           {:name "Relentless Avenger"
                            :level 7}
                           {:name "Soul of Vengeance"
                            :level 15}
                           {:name "Avenging Angel"
                            :level 20}]}]}
   character-ref))

(defn ranger-option [character-ref]
  (class-option
   {:name "Ranger"
    :hit-die 10
    :profs {:armor {:light true :medium true}
            :weapon {:simple true :martial true}
            :save {:str true :dex true}
            :skill-options {:choose 3 :options {:animal-handling true :athletics true :insight true :investigation true :nature true :perception true :stealth true :survival true}}}
    :ability-increase-levels [4 8 10 16 19]
    :spellcaster true
    :spellcasting {:level-factor 2
                   :known-mode :schedule
                   :spells-known {2 2
                                  3 1
                                  5 1
                                  7 1
                                  9 1
                                  11 1
                                  13 1
                                  15 1
                                  17 1
                                  19 1}
                   :ability :wis}
    :armor-choices [{:name "Armor"
                     :options {:scale-mail 1
                               :leather 1}}]
    :equipment-choices [{:name "Equipment Pack"
                         :options {:dungeoneers-pack 1
                                   :explorers-pack 1}}]
    :weapons {:longbow 1}
    :equipment {:quiver 1
                :arrows 20}
    :selections [(t/selection
                  "Melee Weapon"
                  [(t/option
                    "Two Shortswords"
                    :shortswords
                    []
                    [(mod5e/weapon :shortsword 2)])
                   (t/option
                    "Simple Melee Weapon"
                    :simple-melee
                    [(t/selection
                      "Simple Melee Weapon"
                      (opt5e/simple-melee-weapon-options 1)
                      2
                      2)]
                    [])])]
    :levels {2 {:selections [(opt5e/fighting-style-selection character-ref #{:archery :defense :dueling :two-weapon-fighting})]}
             5 {:modifiers [(mod5e/extra-attack)]}}
    :traits [{:name "Primeval Awareness"
              :level 3
              :description "Beginning at 3rd level, you can use your action and 
expend one ranger spell slot to focus your 
awareness on the region around you. For 1 minute 
per level of the spell slot you expend, you can sense 
whether the following types of creatures are present 
within 1 mile of you (or within up to 6 miles if you 
are in your favored terrain): aberrations, celestials, 
dragons, elementals, fey, fiends, and undead. This 
feature doesn't reveal the creatures' location or 
number."}
             {:name "Extra Attack"
              :level 5
              :description "Beginning at 5th level, you can attack twice, instead 
of once, whenever you take the Attack action on your 
turn."}
             {:name "Land's Stride"
              :level 8
              :description "Starting at 8th level, moving through nonmagical 
difficult terrain costs you no extra movement. You 
can also pass through nonmagical plants without 
being slowed by them and without taking damage 
from them if they have thorns, spines, or a similar 
hazard.
In addition, you have advantage on saving throws 
against plants that are magically created or 
manipulated to impede movement, such those 
created by the entangle spell"}
             {:name "Hide in Plain Sight"
              :level 10
              :description "Starting at 10th level, you can spend 1 minute 
creating camouflage for yourself. You must have 
access to fresh mud, dirt, plants, soot, and other 
naturally occurring materials with which to create 
your camouflage.
Once you are camouflaged in this way, you can try 
to hide by pressing yourself up against a solid 
surface, such as a tree or wall, that is at least as tall 
and wide as you are. You gain a +10 bonus to 
Dexterity (Stealth) checks as long as you remain 
there without moving or taking actions. Once you 
move or take an action or a reaction, you must 
camouflage yourself again to gain this benefit."}
             {:name "Vanish"
              :level 14
              :description "Starting at 14th level, you can use the Hide action as 
a bonus action on your turn. Also, you can't be 
tracked by nonmagical means, unless you choose to 
leave a trail."}
             {:name "Feral Senses"
              :level 18
              :description "At 18th level, you gain preternatural senses that help 
you fight creatures you can't see. When you attack a 
creature you can't see, your inability to see it doesn't 
impose disadvantage on your attack rolls against it.
You are also aware of the location of any invisible 
creature within 30 feet of you, provided that the 
creature isn't hidden from you and you aren't 
blinded or deafened."}
             {:name "Foe Slayer"
              :level 20
              :description "At 20th level, you become an unparalleled hunter of 
your enemies. Once on each of your turns, you can 
add your Wisdom modifier to the attack roll or the 
damage roll of an attack you make against one of 
your favored enemies. You can choose to use this 
feature before or after the roll, but before any effects 
of the roll are applied."}
             {:name "Natural Explorer"
              :description "You are particularly familiar with one type of natural 
environment and are adept at traveling and 
surviving in such regions. Choose one type of 
favored terrain: arctic, coast, desert, forest, 
grassland, mountain, or swamp. When you make an 
Intelligence or Wisdom check related to your 
favored terrain, your proficiency bonus is doubled if 
you are using a skill that you're proficient in.
While traveling for an hour or more in your 
favored terrain, you gain the following benefits:
* Difficult terrain doesn't slow your group's travel.
* Your group can't become lost except by magical 
means.
* Even when you are engaged in another activity 
while traveling (such as foraging, navigating, or 
tracking), you remain alert to danger.
* If you are traveling alone, you can move stealthily 
at a normal pace.
* When you forage, you find twice as much food as 
you normally would.
* While tracking other creatures, you also learn 
their exact number, their sizes, and how long ago 
they passed through the area.
You choose additional favored terrain types at 6th 
and 10th level."}]
    :subclass-level 3
    :subclass-title "Ranger Archetype"
    :subclasses [{:name "Hunter"
                  :levels {3 {:selections [(t/selection
                                            "Hunter's Prey"
                                            [(t/option
                                              "Colossus Slayer"
                                              :colossus-slayer
                                              []
                                              [(mod5e/trait "Colossus Slayer" "Your tenacity can wear down the 
most potent foes. When you hit a creature with a 
weapon attack, the creature takes an extra 1d8 
damage if it's below its hit point maximum. You can 
deal this extra damage only once per turn.")])
                                             (t/option
                                              "Giant Killer"
                                              :giant-killer
                                              []
                                              [(mod5e/trait "Giant Killer" "When a Large or larger creature 
within 5 feet of you hits or misses you with an attack, 
you can use your reaction to attack that creature 
immediately after its attack, provided that you can 
see the creature.")])
                                             (t/option
                                              "Horde Breaker"
                                              :horde-breaker
                                              []
                                              [(mod5e/trait "Horde Breaker" "Once on each of your turns when 
you make a weapon attack, you can make another 
attack with the same weapon against a different 
creature that is within 5 feet of the original target 
and within range of your weapon")])])]}
                           7 {:selections [(t/selection
                                            "Defensive Tactics"
                                            [(t/option
                                              "Escape the Horde"
                                              :escape-the-horde
                                              []
                                              [(mod5e/trait "Escape the Horde" "Opportunity attacks against 
you are made with disadvantage.")])
                                             (t/option
                                              "Multiattack Defense"
                                              :multiattack-defense
                                              []
                                              [(mod5e/trait "Multiattack Defense" "When a creature hits you 
with an attack, you gain a +4 bonus to AC against all 
subsequent attacks made by that creature for the 
rest of the turn.")])
                                             (t/option
                                              "Steel Will"
                                              :steel-will
                                              []
                                              [(mod5e/trait "Steel Will" "You have advantage on saving throws 
against being frightened.")])])]}
                           11 {:selections [(t/selection
                                            "Multiattack"
                                            [(t/option
                                              "Volley"
                                              :volley
                                              []
                                              [(mod5e/trait "Volley" "You can use your action to make a ranged 
attack against any number of creatures within 10 
feet of a point you can see within your weapon's 
range. You must have ammunition for each target, as 
normal, and you make a separate attack roll for each 
target.")])
                                             (t/option
                                              "Whirlwind Attack"
                                              :whirlwind-attack
                                              []
                                              [(mod5e/trait "Whirlwind Attack" "You can use your action to
make a melee attack against any number of 
creatures within 5 feet of you, with a separate attack 
roll for each target.")])])]}
                           15 {:selections [(t/selection
                                            "Superior Hunter's Defense"
                                            [(t/option
                                              "Evasion"
                                              :evasion
                                              []
                                              [(mod5e/trait "Evasion" "When you are subjected to an effect, such 
as a red dragon's fiery breath or a lightning bolt spell, 
that allows you to make a Dexterity saving throw to 
take only half damage, you instead take no damage if 
you succeed on the saving throw, and only half 
damage if you fail.")])
                                             (t/option
                                              "Stand Against the Tide"
                                              :stand-against-the-tide
                                              []
                                              [(mod5e/trait "Stand Against the Tide" "When a hostile creature 
misses you with a melee attack, you can use your 
reaction to force that creature to repeat the same 
attack against another creature (other than itself) of 
your choice.")])
                                             (t/option
                                              "Uncanny Dodge"
                                              :uncanny-dodge
                                              []
                                              [(mod5e/trait "Uncanny Dodge" "When an attacker that you can 
see hits you with an attack, you can use your 
reaction to halve the attack's damage against you.")])])]}}}
                 {:name "Beast Master"
                  :traits [{:name "Ranger's Companion"
                            :level 3}
                           {:name "Exceptional Training"
                            :level 7}
                           {:name "Bestial Fury"
                            :level 11}
                           {:name "Share Spells"
                            :level 15}]}]}
   character-ref))

(defn rogue-option [character-ref]
  (class-option
   {:name "Rogue",
    :hit-die 8
    :ability-increase-levels [4 8 12 16 19]
    :expertise true
    :profs {:armor {:light true}
            :weapon {:simple true :crossbow--hand true :longsword true :rapier true :shortsword true}
            :save {:dex true :int true}
            :tool {:thieves-tools true}
            :skill-options {:choose 4 :options {:acrobatics true :athletics true :deception true :insight true :intimidation true :investigation true :perception true :performance true :persuasion true :sleight-of-hand true :stealth true}}}
    :weapon-choices [{:name "Melee Weapon"
                      :options {:rapier 1
                                :shortsword 1}}]
    :armor {:leather 1}
    :weapons {:dagger 2}
    :equipment {:thieves-tools 1}
    :equipment-choices [{:name "Equipment Pack"
                         :options {:burglers-pack 1
                                   :dungeoneers-pack 1
                                   :explorers-pack 1}}]
    :selections [(t/selection
                  "Additional Weapon"
                  [(t/option
                    "Shortbow, Quiver, 20 Arrows"
                    :shortbow
                    []
                    [(mod5e/weapon :shortbow 5)
                     (mod5e/equipment :quiver 1)
                     (mod5e/equipment :arrow 20)])
                   (t/option
                    "Shortsword"
                    :shortsword
                    []
                    [(mod5e/weapon :shortsword 1)])])
                 opt5e/rogue-expertise-selection]
    :traits [{:name "Sneak Attack" :description "You know how to strike subtly and exploit a foe's distraction. Once per turn, you can deal an extra 1d6 damage to one creature you hit with an attack if you have advantage on the attack roll. The attack must use a finesse or a ranged weapon.\nYou don't need advantage on the attack roll if another enemy of the target is within 5 feet of it, that enemy isn't incapacitated, and you don't have disadvantage on the attack roll.\nThe amount of the extra damage increases as you gain levels in this class, as shown in the Sneak Attack column of the Rogue table."}
             {:name "Thieves' Cant" :description "During your rogue training you learned thieves' cant, a secret mix of dialect, jargon, and code that allows you to hide messages in seemingly normal conversation. Only another creature that knows thieves' cant understands such messages. It takes four times longer to convey such a message than it does to speak the same idea plainly.\nIn addition, you understand a set of secret signs and symbols used to convey short, simple messages, such as whether an area is dangerous or the territory of a thieves' guild, whether loot is nearby, or whether the people in an area are easy marks or will provide a safe house for thieves on the run."}
             {:level 2 :name "Cunning Action" :description "Your quick thinking and agility allow you to move and act quickly. You can take a bonus action on each of your turns in combat. This action can be used only to take the Dash, Disengage, or Hide action."}
             {:level 5 :name "Uncanny Dodge" :description "When an attacker that you can see hits you with an attack, you can use your reaction to halve the attack's damage against you."}
             {:level 7 :name "Evasion" :description "You can nimbly dodge out of the way of certain area effects, such as a red dragon's fiery breath or an ice storm spell. When you are subjecte to an effect that allows you to make a Dexterity saving throw to take only half damage, you instead take no damage if you succeed on the saving throw, and only half damage if you fail."}
             {:level 11 :name "Reliable Talent" :description "You have refined your chosen skills until they approach perfection. Whenever you make an ability check that lets you add your pro ciency bonus, you can treat a d20 roll of 9 or lower as a 10."}
             {:level 14 :name "Blindsense" :description "If you are able to hear, you are aware of the location of any hidden or invisible creature within 10 feet of you."}
             {:level 18 :name "Elusive" :description "You are so evasive that attackers rarely gain the upper hand against you. No attack roll has advantage against you while you aren't incapacitated."}
             {:level 20 :name "Stroke of Luck" :description "You have an uncanny knack for succeeding when you need to. If your attack misses a target within range, you can turn the miss into a hit. Alternatively, if you fail an ability check, you can treat the d20 roll as a 20.\nOnce you use this feature, you can't use it again until you  nish a short or long rest."}]
    :subclass-level 3
    :subclass-title "Roguish Archetype"
    :subclasses [{:name "Thief"
                  :traits [{:level 3 :name "Fast Hands" :description "You can use the bonus action granted by your Cunning Action to make a Dexterity (Sleight of Hand) check, use your thieves' tools to disarm a trap or open a lock, or take the Use an Object action."}
                           {:level 3 :name "Second-Story Work" :description "You gain the ability to climb faster than normal; climbing no longer costs you extra movement.\nIn addition, when you make a running jump, the distance you cover increases by a number of feet equal to your Dexterity modifier."}
                           {:level 9 :name "Supreme Sneak" :description "You have advantage on a Dexterity (Stealth) check if you move no more than half your speed on the same turn."}
                           {:level 13 :name "Use Magic Device" :description "You have learned enough about the workings of magic that you can improvise the use of items even when they are not intended for you. You ignore all class, race, and level requirements on the use of
magic items."}
                           {:level 17 :name "Thief's Reflexes" :description "You have learned enough about the workings of magic that you can improvise the use of items even when they are not intended for you. You ignore all class, race, and level requirements on the use of
magic items."}]}
                 {:name "Assassin"
                  :profs {:tool {:disguise-kit true :poisoners-kit true}}
                  :traits [{:name "Assassinate"
                            :level 3}
                           {:name "Infiltration Expertise"
                            :level 9}
                           {:name "Impostor"
                            :level 13}
                           {:name "Death Strike"
                            :level 17}]}
                 {:name "Arcane Trickster"
                  :spellcaster true
                  :spellcasting {:level-factor 3
                                 :spell-list :wizard
                                 :cantrips-known {3 2 10 3}
                                 :known-mode :schedule
                                 :spells-known {3 {:num 3
                                                   :restriction arcane-trickster-spell?}
                                                4 {:num 1
                                                   :restriction arcane-trickster-spell?}
                                                7 {:num 1
                                                   :restriction arcane-trickster-spell?}
                                                8 1
                                                10 {:num 1
                                                    :restriction arcane-trickster-spell?}
                                                11 {:num 1
                                                    :restriction arcane-trickster-spell?}
                                                13 {:num 1
                                                    :restriction arcane-trickster-spell?}
                                                14 1
                                                16 {:num 1
                                                    :restriction arcane-trickster-spell?}
                                                19 {:num 1
                                                    :restriction arcane-trickster-spell?}
                                                20 1}
                                 :ability :int}
                  :modifiers [(mod5e/spells-known 0 :mage-hand :int)]
                  :traits [{:name "Mage Hand Legerdemain"
                            :level 3}
                           {:name "Magical Ambush"
                            :level 9}
                           {:name "Versatile Trickster"
                            :level 13}
                           {:name "Spell Thief"
                            :level 17}]}
                 {:name "Mastermind"
                  :source "Sword Coast Adventurer's Guide"
                  :traits [{:name "Master of Intrigue"
                            :level 3}
                           {:name "Master of Tactics"
                            :level 3}
                           {:name "Insightful Manipulator"
                            :level 9}
                           {:name "Misdirection"
                            :level 13}
                           {:name "Soul of Deceit"
                            :level 17}]}
                 {:name "Swashbuckler"
                  :source "Sword Coast Adventurer's Guide"
                  :traits [{:name "Fancy Footwork"
                            :level 3}
                           {:name "Rakish Audacity"
                            :level 3}
                           {:name "Panache"
                            :level 3}
                           {:name "Elegance Maneuver"
                            :level 13}
                           {:name "Master Duelist"
                            :level 17}]}]}
   character-ref))

(defn reroll-abilities [character-ref]
  (fn []
    (swap! character-ref
           #(assoc-in %
                      [::entity/options :ability-scores ::entity/value]
                      (char5e/standard-ability-rolls)))))

(defn set-standard-abilities [character-ref]
  (fn []
    (swap! character-ref
           (fn [c] (assoc-in c
                             [::entity/options :ability-scores]
                             {::entity/key :standard-scores
                              ::entity/value (char5e/abilities 15 14 13 12 10 8)})))))

(def arcane-tradition-options
  [(t/option
    "School of Evocation"
    :school-of-evocation
    nil
    [(mod5e/subclass :wizard "School of Evocation")
     (mod5e/trait "Evocation Savant")
     (mod5e/trait "Sculpt Spells")])])

(defn template-selections [character-ref]
  [(t/selection
    "Ability Scores"
    [{::t/name "Standard Roll"
      ::t/key :standard-roll
      ::t/ui-fn #(abilities-roller character-ref (reroll-abilities character-ref))
      ::t/select-fn (reroll-abilities character-ref)
      ::t/modifiers [(mod5e/deferred-abilities)]}
     {::t/name "Standard Scores"
      ::t/key :standard-scores
      ::t/ui-fn #(abilities-standard character-ref)
      ::t/select-fn (set-standard-abilities character-ref)
      ::t/modifiers [(mod5e/deferred-abilities)]}])
   (t/selection
    "Race"
    [dwarf-option
     elf-option
     halfling-option
     human-option
     dragonborn-option
     gnome-option
     half-elf-option
     half-orc-option
     tiefling-option])
   (t/selection+
    "Class"
    (fn [selection classes]
      (let [current-classes (into #{}
                                  (map ::entity/key)
                                  (get-in @character-ref
                                          [::entity/options :class]))]
        {::entity/key (->> selection
                           ::t/options
                           (map ::t/key)
                           (some #(if (-> % current-classes not) %)))
         ::entity/options {:levels [{::entity/key :1}]}}))
    [(barbarian-option character-ref)
     (bard-option character-ref)
     (cleric-option character-ref)
     (druid-option character-ref)
     (fighter-option character-ref)
     (monk-option character-ref)
     (paladin-option character-ref)
     (ranger-option character-ref)
     (rogue-option character-ref)])])

(def template-base
  (es/make-entity
   {?armor-class (+ 10 (?ability-bonuses :dex))
    ?max-medium-armor-bonus 2
    ?armor-stealth-disadvantage? (fn [armor]
                                  (:stealth-disadvantage? armor))
    ?armor-dex-bonus (fn [armor]
                       (let [dex-bonus (?ability-bonuses :dex)]
                         (case (:type armor)
                           :light dex-bonus
                           :medium (max ?max-medium-armor-bonus dex-bonus)
                           0)))
    ?armor-class-with-armor (fn [armor]
                              (+ (?armor-dex-bonus armor) (:base-ac armor)))
    ?ability-bonuses (reduce-kv
                      (fn [m k v]
                        (assoc m k (int (/ (- v 10) 2))))
                      {}
                      ?abilities)
    ?total-levels (apply + (map (fn [[k {l :class-level}]] l) ?levels))
    ?prof-bonus (+ (int (/ (dec ?total-levels) 4)) 2)
    ?default-skill-bonus {}
    ?skill-prof-bonuses (reduce
                         (fn [m {k :key}]
                           (assoc m k (if (k ?skill-profs)
                                        (if (k ?skill-expertise)
                                          (* 2 ?prof-bonus)
                                          ?prof-bonus)
                                        (or (?default-skill-bonus (opt5e/skill-abilities k)) 0))))
                         {}
                         opt5e/skills)
    ?skill-bonuses (reduce-kv
                    (fn [m k v]
                      (assoc m k (+ v (?ability-bonuses (opt5e/skill-abilities k)))))
                    {}
                    ?skill-prof-bonuses)
    ?passive-perception (+ 10 (?skill-bonuses :perception))
    ?passive-investigation (+ 10 (?skill-bonuses :investigation))
    ?hit-point-level-bonus (?ability-bonuses :con)
    ?hit-point-level-increases 0
    ?max-hit-points (+ ?hit-point-level-increases (* ?total-levels ?hit-point-level-bonus))
    ?initiative (?ability-bonuses :dex)
    ?num-attacks 1
    ?critical #{20}}))

(defn template [character-ref]
  {::t/base template-base
   ::t/selections (template-selections character-ref)})

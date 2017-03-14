(ns orcpub.dnd.e5.template
  (:require [clojure.string :as s]
            [orcpub.entity :as entity]
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
  {::entity/options {#_:ability-scores #_{::entity/key :standard-roll
                                      ::entity/value (char5e/abilities 15 14 13 12 10 8)}
                     :class [{::entity/key :barbarian
                              ::entity/options {:levels [{::entity/key :1}]}}]}})

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
  [:div.flex.justify-cont-s-b
    (let [abilities (get-raw-abilities character-ref)
          abilities-vec (vec abilities)]
      (map-indexed
       (fn [i [k v]]
         ^{:key k}
         [:div.m-t-10.m-b-10.t-a-c
          [:div.uppercase (name k)]
          [:div.f-s-18 v]
          [:div.f-s-16
           [:i.fa.fa-chevron-circle-left.orange
            {:on-click (swap-abilities character-ref i (dec i) k v)}]
           [:i.fa.fa-chevron-circle-right.orange.m-l-5
            {:on-click (swap-abilities character-ref i (inc i) k v)}]]])
       abilities-vec))])

(defn abilities-roller [character-ref reroll-fn]
  [:div
   (abilities-standard character-ref)
   [:button.form-button
    {:on-click reroll-fn}
    "Re-Roll"]])

(defn abilities-entry [character-ref]
  [:div.flex
   (let [abilities (get-raw-abilities character-ref)
         abilities-vec (vec abilities)]
     (map-indexed
      (fn [i k]
        ^{:key k}
        [:div.m-t-10.t-a-c.p-1 
         [:div.uppercase (name k)]
         [:input.input.f-s-18
          {:value (k abilities)
           :on-change (fn [e] (let [value (.-value (.-target e))
                                    new-v (if (not (s/blank? value))
                                            (js/parseInt value))]
                                (swap! character-ref assoc-in [::entity/options :ability-scores ::entity/value k] new-v)))}]])
      char5e/ability-keys))])

(declare template-selections)

(defn roll-hit-points [die character-ref path]
  (let [value-path (entity/get-option-value-path
                    {::t/selections (template-selections character-ref)}
                    @character-ref
                    path)]
    (swap! character-ref #(assoc-in % value-path (dice/die-roll die)))))

(defn hit-points-roller [die character-ref path]
  [:div
   [:button.form-button.m-t-10
    {:on-click #(roll-hit-points die character-ref path)}
    "Re-Roll"]])

(defn hit-points-entry [character-ref path]
  (let [value-path (entity/get-option-value-path
                    {::t/selections (template-selections character-ref)}
                    @character-ref
                    path)
        value (get-in @character-ref value-path)]
    [:div
     [:input.input
      {:value value
       :type :number
       :on-change (fn [e] (let [value (.-value (.-target e))
                               new-v (if (not (s/blank? value))
                                       (js/parseInt value))]
                           (swap! character-ref assoc-in value-path new-v)))}]]))

(defn traits-modifiers [traits & [include-level?]]
  (map
   (fn [trait]
     (mod5e/trait-cfg trait))
   traits))

(defn armor-prof-modifiers [armor-proficiencies & [cls-kw]]
  (map
   (fn [armor-prof]
     (let [[armor-kw first-class?] (if (keyword? armor-prof) [armor-prof false] armor-prof)]
       (mod5e/armor-proficiency (clojure.core/name armor-kw) armor-kw first-class? cls-kw)))
   armor-proficiencies))

(defn tool-prof-modifiers [tool-proficiencies & [cls-kw]]
  (map
   (fn [tool-prof]
     (let [[tool-kw first-class?] (if (keyword? tool-prof) [tool-prof false] tool-prof)]
       (mod5e/tool-proficiency (:name (opt5e/tools-map tool-kw)) tool-kw first-class? cls-kw)))
   tool-proficiencies))

(defn weapon-prof-modifiers [weapon-proficiencies & [cls-kw]]
  (map
   (fn [weapon-prof]
     (let [[weapon-kw first-class?] (if (keyword? weapon-prof) [weapon-prof false] weapon-prof)]
       (if (#{:simple :martial} weapon-kw)
         (mod5e/weapon-proficiency (str (name weapon-kw) " weapons") weapon-kw first-class? cls-kw)
         (mod5e/weapon-proficiency (-> weapon-kw opt5e/weapons-map :name) weapon-kw first-class? cls-kw))))
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

(defn darkvision-modifiers [range]
  [(mod5e/darkvision range)])

(defn race-option [{:keys [name
                           help
                           abilities
                           size
                           speed
                           darkvision
                           subraces
                           modifiers
                           selections
                           traits
                           languages
                           language-options
                           armor-proficiencies
                           weapon-proficiencies]}]
  (t/option-cfg
   {:name name
    :help help
    :selections (vec
                 (concat
                  (if subraces
                    [(t/selection
                      "Subrace"
                      (vec (map subrace-option subraces)))])
                  (if language-options
                    (let [{lang-num :choose lang-options :options} language-options
                          lang-kws (if (:any lang-options)
                                     (map :key opt5e/languages)
                                     (keys lang-options))]
                      [(opt5e/language-selection (map opt5e/language-map lang-kws) lang-num)]))
                  selections))
    :modifiers (vec
                (concat
                 [(mod5e/race name)
                  (mod5e/size size)
                  (mod5e/speed speed)]
                 (if darkvision
                   (darkvision-modifiers darkvision))
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
                 (weapon-prof-modifiers weapon-proficiencies)))}))

(def elf-weapon-training-mods
  (weapon-prof-modifiers [:longsword :shortsword :shortbow :longbow]))

(def elf-option
  (race-option
   {:name "Elf"
    :help "Elves are graceful, magical creatures, with a slight build."
    :abilities {:dex 2}
    :size :medium
    :speed 30
    :languages ["Elvish" "Common"]
    :darkvision 60
    :subraces
    [{:name "High Elf"
      :abilities {:int 1}
      :selections [(opt5e/spell-selection :wizard 0 :int "High Elf" 1)
                   (opt5e/language-selection opt5e/languages 1)]
      :modifiers [elf-weapon-training-mods]}
     {:name "Wood Elf"
      :abilities {:cha 1}
      :traits [{:name "Mask of the Wild"
                :page 24
                :summary "Hide when lightly obscured by natural phenomena."}]
      :modifiers [(mod5e/speed 5)
                  elf-weapon-training-mods]}
     {:name "Dark Elf (Drow)"
      :abilities {:cha 1}
      :traits [{:name "Sunlight Sensitivity"
                :summary "Disadvantage on attack and perception rolls in direct sunlight"
                :page 24}]
      :modifiers [(mod5e/darkvision 120)
                  (mod5e/spells-known 0 :dancing-lights :cha "Dark Elf")
                  (mod5e/spells-known 1 :faerie-fire :cha "Dark Elf" 3)
                  (mod5e/spells-known 2 :darkness :cha "Dark Elf" 5)]}]
    :traits [{:name "Fey Ancestry"
              :page 23
              :description "You have advantage on saving throws against being charmed and magic can't put you to sleep"}
             {:name "Trance"
              :page 23
              :summary "Trance 4 hrs. instead of sleep 8"
              :description "Elves don't need to sleep. Instead, they meditate deeply, remaining semiconscious, for 4 hours a day. (The Common word for such meditation is 'trance.') While meditating, you can dream after a fashion; such dreams are actually mental exercises that have become re exive through years of practice. After resting in this way, you gain the same beneit that a human does from 8 hours of sleep."}]}))

(def dwarf-option
  (race-option
   {:name "Dwarf",
    :help "Dwarves are short and stout and tend to be skilled warriors and craftmen in stone and metal."
    :abilities {:con 2},
    :size :medium
    :speed 25,
    :darkvision 60
    :languages ["Dwarvish" "Common"]
    :weapon-proficiencies [:handaxe :battleaxe :light-hammer :warhammer]
    :traits [{:name "Dwarven Resilience"
              :summary "Advantage on poison saves, resistance to poison damage"
              :page 20
              :description "You have advantage on saving throws against poison, and you have resistance against poison damage"},
             {:name "Stonecunning"
              :summary "2X prof bonus on stonework-related history checks"
              :page 20
              :description "Whenever you make an Intelligence (History) check related to the origin of stonework you are considered proficient in the History skill and add double your proficiency bonus to the check, instead of your normal proficiency bonus"}]
    :subraces [{:name "Hill Dwarf",
                :abilities {:wis 1}
                :selections [(opt5e/tool-selection [:smiths-tools :brewers-supplies :masons-tools] 1)]
                :modifiers [(mod/modifier ?hit-point-level-bonus (+ 1 ?hit-point-level-bonus))]}
               {:name "Mountain Dwarf"
                :abilities {:str 2}
                :armor-proficiencies [:light :medium]}]
    :modifiers [(mod5e/resistance :poison)]}))

(def halfling-option
  (race-option
   {:name "Halfling"
    :help "Halflings are small and nimble, half the height of a human, but fairly stout. They are cheerful and practical."
    :abilities {:dex 2}
    :size :small
    :speed 25
    :languages ["Halfling" "Common"]
    :subraces
    [{:name "Lightfoot"
      :abilities {:cha 1}
      :traits [{:name "Naturally Stealthy"
                :page 28
                :summary "Hide behind creatures larger than you"
                :description "You can attempt to hide even when you are obscured only by a creature that is at least one size larger than you."}]}
     {:name "Stout"
      :abilities {:con 1}
      :modifiers [(mod5e/resistance :poison)]
      :traits [{:name "Stout Resilience"
                :page 28
                :summary "Advantage on poison saves, resistance to poison damage"}]}]
    :traits [{:name "Lucky"
              :page 28
              :summary "Reroll 1s on d20"
              :description "When you roll a 1 on the d20 for an attack roll, ability check, or saving throw, you can reroll the die and must use the new roll."}
             {:name "Brave"
              :page 28
              :description "You have advantage on saving throws against being frightened."}
             {:name "Halfling Nimbleness"
              :page 28
              :description "You can move through the space of any creature that is of a size larger than yours."}]}))

(def human-option
  (race-option
   {:name "Human"
    :help "Humans are physically diverse and highly adaptable. They excel in nearly every profession."
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

(defn draconic-ancestry-option [{:keys [name breath-weapon]}]
  (t/option
   name
   (common/name-to-kw name)
   []
   [(mod5e/resistance (:damage-type breath-weapon))
    (mod/modifier ?draconic-ancestry-breath-weapon breath-weapon)]))

(def dragonborn-option
  (race-option
   {:name "Dragonborn"
    :help "Kin to dragons, dragonborn resemble humanoid dragons, without wings or tail and standing erect. They tend to make excellent fighters."
    :abilities {:str 2 :cha 1}
    :size :medium
    :speed 30
    :languages ["Draconic" "Common"]
    :modifiers [(mod5e/attack
                 (merge
                  ?draconic-ancestry-breath-weapon
                  {:name "Breath Weapon"
                   :damage-die 6
                   :page 34
                   :damage-die-count (condp <= ?total-levels
                                       16 5
                                       11 4
                                       6 3
                                       2)
                   :save-dc (+ 8 (:con ?ability-bonuses) ?prof-bonus)}))]
    :selections [(t/selection
                  "Draconic Ancestry"
                  (map
                   draconic-ancestry-option
                   [{:name "Black"
                     :breath-weapon {:damage-type :acid
                                     :area-type :line
                                     :line-width 5
                                     :line-length 30
                                     :save :dex}}
                    {:name "Blue"
                     :breath-weapon {:damage-type :lightning
                                     :area-type :line
                                     :line-width 5
                                     :line-length 30
                                     :save :dex}}
                    {:name "Brass"
                     :breath-weapon {:damage-type :fire
                                     :area-type :line
                                     :line-width 5
                                     :line-length 30
                                     :save :dex}}
                    {:name "Bronze"
                     :breath-weapon {:damage-type :lightning
                                     :area-type :line
                                     :line-width 5
                                     :line-length 30
                                     :save :dex}}
                    {:name "Copper"
                     :breath-weapon {:damage-type :acid
                                     :area-type :line
                                     :line-width 5
                                     :line-length 30
                                     :save :dex}}
                    {:name "Gold"
                     :breath-weapon {:damage-type :fire
                                     :area-type :cone
                                     :length 15
                                     :save :dex}}
                    {:name "Green"
                     :breath-weapon {:damage-type :poison
                                     :area-type :cone
                                     :length 15
                                     :save :con}}
                    {:name "Red"
                     :breath-weapon {:damage-type :fire
                                     :area-type :cone
                                     :length 15
                                     :save :dex}}
                    {:name "Silver"
                     :breath-weapon {:damage-type :cold
                                     :area-type :cone
                                     :length 15
                                     :save :con}}
                    {:name "White"
                     :breath-weapon {:damage-type :cold
                                     :area-type :cone
                                     :length 15
                                     :save :con}}]))]}))


(def gnome-option
  (race-option
   {:name "Gnome"
    :help "Gnomes are small, intelligent humanoids who live life with the utmost of enthusiasm."
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
               {:name "Tinker" :description "You have proficiency with artisan's tools (tinker's tools). Using those tools, you can spend 1 hour and 10 gp worth of materials to construct a Tiny clockwork device (AC 5, 1 hp). The device ceases to function after 24 hours (unless you spend 1 hour repairing it to keep the device functioning), or when you use your action to dismantle it; at that time, you can reclaim the materials used to create it. You can have up to three such devices active at a time.
When you create a device, choose one of the following options:
Clockwork Toy. This toy is a clockwork animal, monster, or person, such as a frog, mouse, bird, dragon, or soldier. When placed on the ground, the toy moves 5 feet across the ground on each of your turns in a random direction. It makes noises as appropriate to the creature it represents.
Fire Starter. The device produces a miniature flame, which you can use to light a candle, torch, or campfire. Using the device requires your action. Music Box. When opened, this music box plays a single song at a moderate volume. The box stops playing when it reaches the song's end or when it is closed."}]}
     {:name "Forest Gnome"
      :abilities {:dex 1}
      :modifiers [(mod5e/spells-known 0 :minor-illusion :int "Forest Gnome")]
      :traits [{:name "Speak with Small Beasts"}]}]
    :traits [{:name "Gnome Cunning" :description "You have advantage on all Intelligence, Wisdom, and Charisma saving throws against magic."}]}))

(def half-elf-option
  (race-option
   {:name "Half-Elf"
    :help "Half-elves are charismatic, and bear a resemblance to both their elvish and human parents and share many of the traits of each."
    :abilities {:cha 2}
    :size :medium
    :speed 30
    :languages ["Common" "Elvish"]
    :selections [(opt5e/ability-increase-selection (disj (set char5e/ability-keys) :cha) 2 false)
                 (opt5e/skill-selection 2)
                 (opt5e/language-selection opt5e/languages 1)]
    :traits [{:name "Fey Ancestry" :description "You have advantage on saving throws against being charmed, and magic can't put you to sleep."}]}))

(def half-orc-option
  (race-option
   {:name "Half-Orc"
    :help "Half-orcs are strong and bear an unmistakable resemblance to their orcish parent. They tend to make excellent warriors, especially Barbarians."
    :abilities {:str 2 :con 1}
    :size :medium
    :speed 30
    :languages ["Common" "Orc"]
    :modifiers [(mod5e/skill-proficiency :intimidation)]
    :traits [{:name "Relentless Endurance" :description "When you are reduced to 0 hit points but not killed outright, you can drop to 1 hit point instead. You can't use this feature again until you finish a long rest."}
                      {:name "Savage Attacks" :description "When you score a critical hit with a melee weapon attack, you can roll one of the weapon's damage dice one additional time and add it to the extra damage of the critical hit."}]}))

(def aasimar-option
  (race-option
   {:name "Aasimar"
    :abilities {:cha 2}
    :size :medium
    :speed 30
    :darkvision 60
    :source "Volo's Guide to Monsters"
    :languages ["Common" "Celestial"]
    :modifiers [(mod5e/resistance :necrotic)
                (mod5e/resistance :radiant)
                (mod5e/spells-known 0 :light :cha "Aasimar")]
    :traits [{:name "Celestial Resistance"}
             {:name "Healing Hands"}
             {:name "Light Bearer"}]
    :subraces [{:name "Protector Aasimar"
                :abilities {:wis 1}
                :traits [{:name "Radiant Soul"
                          :level 3}]}
               {:name "Scourge Aasimar"
                :abilities {:con 1}
                :traits [{:name "Radiant Consumption"
                          :level 3}]}
               {:name "Fallen Aasimar"
                :abilities {:str 1}
                :traits [{:name "Necrotic Shroud"
                          :level 3}]}]}))

(def firbolg-option
  (race-option
   {:name "Firbolg"
    :abilities {:wis 2 :str 1}
    :size :medium
    :speed 30
    :source "Volo's Guide to Monsters"
    :languages ["Common" "Elvish" "Giant"]
    :modifiers [(mod5e/spells-known 1 :detect-magic :wis "Firbolg")
                (mod5e/spells-known 1 :disguise-self :wis "Firbolg")]
    :traits [{:name "Firbolg Magic"}
             {:name "Hidden Step"}
             {:name "Powerful Build"}
             {:name "Speech of Beast and Leaf"}]}))

(def goliath-option
  (race-option
   {:name "Goliath"
    :abilities {:str 2 :con 1}
    :size :medium
    :speed 30
    :languages ["Common" "Giant"]
    :profs {:skill {:athletics true}}
    :source "Volo's Guide to Monsters"
    :traits [{:name "Stone's Endurance"}
             {:name "Mountain Born"}
             {:name "Powerful Build"}]}))

(def kenku-option
  (race-option
   {:name "Kenku"
    :abilities {:dex 2 :wis 1}
    :size :medium
    :speed 30
    :source "Volo's Guide to Monsters"
    :languages ["Common" "Auran"]
    :profs {:skill-options {:choose 2 :options {:acrobatics true :deception true :stealth true :sleight-of-hand true}}}
    :traits [{:name "Expert Forgery"}
             {:name "Mimicry"}]}))

(def lizardfolk-option
  (race-option
   {:name "Lizardfolk"
    :abilities {:con 2 :wis 1}
    :size :medium
    :speed 30
    :source "Volo's Guide to Monsters"
    :languages ["Common" "Draconic"]
    :modifiers [(mod5e/swimming-speed 30)
                (mod/modifier ?armor-class (+ 3 ?armor-class) "Unarmored AC" (mod/bonus-str 3))
                (mod/modifier ?armor-class-with-armor (fn [armor] (max ?armor-class (?armor-class-with-armor armor))))]
    :profs {:skill-options {:choose 2 :options {:animal-handling true :nature true :stealth true :perception true :survival true}}}
    :traits [{:name "Bite"}
             {:name "Cunning Artisan"}
             {:name "Hold Breath"}
             {:name "Natural Armor"}
             {:name "Hungry Jaws"}]}))

(def tabaxi-option
  (race-option
   {:name "Tabaxi"
    :abilities {:dex 2 :cha 1}
    :size :medium
    :speed 30
    :darkvision 60
    :source "Volo's Guide to Monsters"
    :modifiers [(mod5e/climbing-speed 20)]
    :language-options {:choose 1 :options {:any true}}
    :profs {:skill {:perception true :stealth true}}
    :traits [{:name "Feline Agility"}
             {:name "Cat's Claws"}
             {:name "Cat's Talent"}]}))

(def triton-option
  (race-option
   {:name "Triton"
    :abilities {:str 1 :con 1 :cha 1}
    :size :medium
    :speed 30
    :source "Volo's Guide to Monsters"
    :languages ["Common" "Primordial"]
    :modifiers [(mod5e/swimming-speed 30)
                (mod5e/spells-known 1 :fog-cloud :cha "Triton")
                (mod5e/spells-known 2 :gust-of-wind :cha "Triton" 3)
                (mod5e/spells-known 3 :wall-of-water :cha "Triton" 5)
                (mod5e/resistance :cold)]
    :traits [{:name "Amphibious"}
             {:name "Control Air and Water"}
             {:name "Emissary of the Sea"}
             {:name "Guardians of the Depths"}]}))

(def bugbear-option
  (race-option
   {:name "Bugbear"
    :abilities {:str 2 :dex 1}
    :size "Medium"
    :speed 30
    :darkvision 60
    :source "Volo's Guide to Monsters"
    :modifiers [(mod5e/skill-proficiency :stealth)]
    :languages ["Common" "Goblin"]
    :traits [{:name "Long Limbed"}
             {:name "Powerful Build"}
             {:name "Sneaky"}
             {:name "Surprise Attack"}]}))

(def goblin-option
  (race-option
   {:name "Goblin"
    :abilities {:dex 2 :con 1}
    :size "Medium"
    :speed 30
    :darkvision 60
    :source "Volo's Guide to Monsters"
    :languages ["Common" "Goblin"]
    :traits [{:name "Fury of the Small"}
             {:name "Nimble Escape"}]}))

(def hobgoblin-option
  (race-option
   {:name "Hobgoblin"
    :abilities {:con 2 :int 1}
    :size "Medium"
    :speed 30
    :darkvision 60
    :source "Volo's Guide to Monsters"
    :selections [(t/selection
                  "Martial Weapon Proficiencies"
                  (opt5e/weapon-proficiency-options (opt5e/martial-weapons opt5e/weapons))
                  2
                  2)]
    :modifiers [(mod5e/light-armor-proficiency)]
    :traits [{:name "Martial Training"}
             {:name "Saving Face"}]}))

(def kobold-option
  (race-option
   {:name "Kobold"
    :abilities {:dex 2 :str -2}
    :size "Medium"
    :speed 30
    :darkvision 60
    :source "Volo's Guide to Monsters"
    :languages ["Common" "Draconic"]
    :traits [{:name "Grovel, Cower, and Beg"}
             {:name "Pack Tactics"}
             {:name "Sunlight Sensitivity"}]}))

(def orc-option
  (race-option
   {:name "Orc"
    :abilities {:str 2 :con 1 :int -2}
    :size "Medium"
    :speed 30
    :darkvision 60
    :source "Volo's Guide to Monsters"
    :modifiers [(mod5e/skill-proficiency :intimidation)]
    :languages ["Common" "Orc"]
    :traits [{:name "Aggressive"}
             {:name "Menacing"}
             {:name "Powerful Build"}]}))

(def yuan-ti-option
  (race-option
   {:name "Yuan-Ti"
    :abilities {:cha 2 :int 1}
    :size "Medium"
    :speed 30
    :darkvision 60
    :source "Volo's Guide to Monsters"
    :modifiers [(mod5e/spells-known 0 :poison-spray :cha "Yuan-Ti")
                (mod5e/spells-known 1 :animal-friendship :cha "Yuan-Ti" 1 "unlimited uses, can only target snakes")
                (mod5e/spells-known 2 :suggestion :cha "Yuan-Ti" 3 "one use per long rest")
                (mod5e/immunity :poison)
                (mod5e/condition-immunity :poisoned)]
    :languages ["Common" "Abyssal" "Draconic"]
    :traits [{:name "Innate Spellcasting"}
             {:name "Magic Resistance"}
             {:name "Poison Immunity"}]}))

(def tiefling-option
  (race-option
   {:name "Tiefling"
    :help "Tieflings bear the distinct marks of their infernal ancestry: horns, a tail, pointed teeth, and solid-colored eyes. They are smart and charismatic."
    :abilities {:int 1 :cha 2}
    :size :medium
    :speed 30
    :darkvision 60
    :languages ["Common" "Infernal"]
    :modifiers [
                (mod5e/spells-known 0 :thaumaturgy :cha "Tiefling")
                (mod5e/spells-known 1 :hellish-rebuke :cha "Tiefling" 3)
                (mod5e/spells-known 2 :darkness :cha "Tiefling" 5)]
    :traits [{:name "Relentless Endurance" :description "When you are reduced to 0 hit points but not killed outright, you can drop to 1 hit point instead. You can't use this feature again until you finish a long rest."}
                      {:name "Savage Attacks" :description "When you score a critical hit with a melee weapon attack, you can roll one of the weapon's damage dice one additional time and add it to the extra damage of the critical hit."}]}))

(defn die-mean [die]
  (int (Math/ceil (/ (apply + (range 1 (inc die))) die))))

(defn hit-points-selection [character-ref die]
  (t/selection-cfg
   {:name "Hit Points"
    :help "Select the method with which to determine this level's hit points."
    :options [{::t/name "Manual Entry"
               ::t/key :manual-entry
               ::t/help "This option allows you to manually type in the value for this level's hit points. Use this if you want to roll dice yourself or if you already have a character with known hit points for this level."
               ::t/ui-fn #(hit-points-entry character-ref %)
               ::t/modifiers [(mod5e/deferred-max-hit-points)]}
              {::t/name (str "Roll (1D" die ")")
               ::t/key :roll
               ::t/help "This option rolls virtual dice for you and sets that value for this level's hit points. It could pay off with a high roll, but you might also roll a 1."
               ::t/ui-fn #(hit-points-roller die character-ref %)
               ::t/select-fn #(roll-hit-points die character-ref %)
               ::t/modifiers [(mod5e/deferred-max-hit-points)]}
              (let [average (die-mean die)]
                (t/option-cfg
                 {:name "Average"
                  :key :average
                  :help (str "This option just gives you the average value (" average ") for the die roll (1D" die "). Choose this option if you're not feeling lucky.")
                  :modifiers [(mod5e/max-hit-points average)]}))]}))

(defn tool-prof-selection-aux [tool num & [key prereq-fn]]
  (t/selection-cfg
   {:name (str "Tool Proficiency: " (:name tool))
    :key (if key (keyword (str (name key) "--" (common/name-to-kw (:name tool)))))
    :help (str "Select " (s/lower-case (:name tool)) " for which you are proficient.")
    :options (mapv
              (fn [{:keys [name key]}]
                (t/option
                 name
                 key
                 []
                 [(mod5e/tool-proficiency name key)]))
              (:values tool))
    :min num
    :max num
    :prereq-fn prereq-fn}))

(defn tool-prof-selection [tool-options & [key prereq-fn]]
  (let [[first-key first-num] (-> tool-options first)
        first-option (opt5e/tools-map first-key)]
    (if (and (= 1 (count tool-options))
             (seq (:values first-option)))
      (tool-prof-selection-aux first-option first-num key prereq-fn)
      (t/selection-cfg
       {:name "Tool Proficiencies"
        :key key
        :options (map
                  (fn [[k num]]
                    (let [tool (opt5e/tools-map k)]
                      (if (:values tool)
                        (t/option
                         (:name tool)
                         k
                         [(tool-prof-selection-aux tool num key prereq-fn)]
                         [])
                        (t/option
                         (:name tool)
                         (:key tool)
                         []
                         [(mod5e/tool-proficiency (:name tool) (:key tool))]))))
                  tool-options)
        :prereq-fn prereq-fn}))))

(defn subclass-level-option [{:keys [name
                                     levels] :as subcls}
                             kw
                             character-ref
                             spellcasting-template
                             i]
  (let [selections (some-> levels (get i) :selections)]
    (t/option
     (str i)
     (keyword (str i))
     (vec
      (concat
       selections      
       (some-> spellcasting-template :selections (get i))))
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
                                (or (:spell-list spellcasting) kw)))
        option (t/option
                name
                kw
                (vec
                 (concat
                  selections
                  (if (seq tool-options) [(tool-prof-selection tool-options)])
                  (if (seq skill-kws) [(opt5e/skill-selection skill-kws skill-num)])))
                (vec
                 (concat
                  modifiers
                  (armor-prof-modifiers armor-profs)
                  (weapon-prof-modifiers weapon-profs)
                  (tool-prof-modifiers tool-profs)
                  (traits-modifiers traits true))))]
    (if spellcasting-template
      (assoc
       option
       ::t/plugins [{::t/path [:class (:key cls)]
                     ::t/selections [(t/sequential-selection
                                      "Levels"
                                      (fn [selection options current-values]
                                        {::entity/key (-> current-values count inc str keyword)})
                                      (vec
                                       (map
                                        (partial subclass-level-option subcls kw character-ref spellcasting-template)
                                        (range 1 21))))]}])
      option)))

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
                            subclasses] :as cls}
                    kw
                    character-ref
                    spellcasting-template
                    i]
  (let [ability-inc-set (set ability-increase-levels)]
    (t/option
     (str i)
     (keyword (str i))
     (vec
      (concat
       (some-> levels (get i) :selections)
       (some-> spellcasting-template :selections (get i))
       (if (= i subclass-level)
         [(t/selection-cfg
           {:name subclass-title
            :key :subclass
            :help subclass-help
            :options (mapv
                      #(subclass-option (assoc cls :key kw) % character-ref)
                      subclasses)})])
       (if (and (not plugin?) (ability-inc-set i))
         [(opt5e/ability-score-improvement-selection)])
       (if (and (not plugin?) (> i 1))
         [(hit-points-selection character-ref hit-die)])))
     (vec
      (concat
       (if (= :all (:known-mode spellcasting))
         (let [slots (opt5e/total-slots i (:level-factor spellcasting))
               prev-level-slots (opt5e/total-slots (dec i) (:level-factor spellcasting))
               new-slots (apply dissoc slots (keys prev-level-slots))]
           (if (seq new-slots)
             (let [lvl (key (first new-slots))]
               (map
                (fn [kw]
                  (mod5e/spells-known lvl kw (:ability spellcasting) name))
                (get-in sl/spell-lists [kw lvl]))))))
       (some-> levels (get i) :modifiers)
       (traits-modifiers
        (filter
         (fn [{level :level :or {level 1}}]
           (= level i))
         traits))
       (if (and (not plugin?) (= i 1)) [(mod5e/max-hit-points hit-die)])
       [(mod5e/level kw name i hit-die)])))))


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

(defn simple-weapon-selection [num]
  (t/selection
   "Simple Weapon"
   (opt5e/weapon-options (opt5e/simple-weapons opt5e/weapons))
   num
   num))

(defn weapon-option [[k num]]
  (case k
    :simple (t/option
             "Any Simple Weapon"
             :any-simple
             [(simple-weapon-selection num)]
             [])
    :martial (t/option
              "Any Martial Weapon"
              :any-martial
              [(t/selection
                "Martial Weapon"
                (opt5e/weapon-options (opt5e/martial-weapons opt5e/weapons))
                num
                num)]
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

(defn class-options [option-fn choices help]
  (map
   (fn [{:keys [name options]}]
     (t/selection-cfg
      {:name (str "Starting Equipment: " name)
       :help help
       :options (mapv
                 option-fn
                 options)}))
   choices))

(defn class-weapon-options [weapon-choices]
  (class-options weapon-option weapon-choices "Select a weapon to begin your adventuring career with."))

(defn class-armor-options [armor-choices]
  (class-options armor-option armor-choices "Select armor to begin your adventuring career with."))

(defn class-equipment-options [equipment-choices]
  (class-options equipment-option equipment-choices "Select equipment to start your adventuring career with."))

(defn class-skill-selection [{skill-num :choose options :options skill-select-order :order} key prereq-fn]
  (let [skill-kws (if (:any options) (map :key opt5e/skills) (keys options))]
    (opt5e/skill-selection skill-kws skill-num skill-select-order key prereq-fn)))

(defn class-option [{:keys [name
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
        {:keys [save skill-options multiclass-skill-options tool-options multiclass-tool-options tool]
         armor-profs :armor weapon-profs :weapon} profs
        save-profs (keys save)
        spellcasting-template (opt5e/spellcasting-template (assoc spellcasting :class-key kw))]
    (t/option-cfg
     {:name name
      :key kw
      :help help
      :selections (vec
                   (concat
                    selections
                    (if (seq tool-options)
                      [(tool-prof-selection tool-options :tool-selection (fn [c] (= kw (first (:classes c)))))])
                    (if (seq multiclass-tool-options)
                      [(tool-prof-selection multiclass-tool-options :multiclass-tool-selection (fn [c] (not= kw (first (:classes c)))))])
                    (class-weapon-options weapon-choices)
                    (class-armor-options armor-choices)
                    (class-equipment-options equipment-choices)
                    [(class-skill-selection skill-options :skill-proficiency (fn [c] (= kw (first (:classes c)))))
                     (class-skill-selection multiclass-skill-options :multiclass-skill-proficiency (fn [c] (not= kw (first (:classes c)))))
                     (t/selection-cfg
                      {:name "Levels"
                       :help "These are your levels in the containing class. You can add levels by clicking the 'Add Levels' button below."
                       :new-item-text "Level Up (Add a Level)"
                       :new-item-fn (fn [selection options current-values]
                                      {::entity/key (-> current-values count inc str keyword)})
                       :options (vec
                                 (map
                                  (partial level-option cls kw character-ref spellcasting-template)
                                  (range 1 21)))
                       :min 1
                       :sequential? true
                       :max nil})]))
      :modifiers (vec
                  (concat
                   modifiers
                   (armor-prof-modifiers armor-profs kw)
                   (weapon-prof-modifiers weapon-profs kw)
                   (tool-prof-modifiers tool kw)
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
                   [(mod5e/class kw)
                    (apply mod5e/saving-throws kw save-profs)]))})))


(defn barbarian-option [character-ref]
  (class-option
   {:name "Barbarian"
    :hit-die 12
    :ability-increase-levels [4 8 12 16 19]
    :profs {:armor {:light true :medium true :shields false}
            :weapon {:simple false :martial false}
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
    :modifiers [(mod/modifier ?armor-class (+ (?ability-bonuses :con) ?armor-class) nil nil [(= :barbarian (first ?classes))])]
    :levels {5 {:modifiers [(mod5e/extra-attack)]}}
    :traits [{:name "Rage"
              :description "In battle, you fight with primal ferocity. On your turn, you can enter a rage as a bonus action. While raging, you gain the following benefits if you aren't wearing heavy armor:
* You have advantage on Strength checks and Strength saving throws.
* When you make a melee weapon attack using Strength, you gain a bonus to the damage roll that increases as you gain levels as a barbarian, as shown in the Rage Damage column of the Barbarian table.
* You have resistance to bludgeoning, piercing, and slashing damage.
If you are able to cast spells, you can't cast them or concentrate on them while raging. Your rage lasts for 1 minute. It ends early if you are knocked unconscious or if your turn ends and you haven't attacked a hostile creature since your last turn or taken damage since then. You can also end your rage on your turn as a bonus action. Once you have raged the number of times shown for your barbarian level in the Rages column of the Barbarian table, you must finish a long rest before you can rage again."}
             {:name "Unarmored Defense"
              :description "While you are not wearing any armor, your Armor Class equals 10 + your Dexterity modifier + your Constitution modifier. You can use a shield and still gain this benefit."}
             {:name "Reckless Attack"
              :level 2
              :description "Starting at 2nd level, you can throw aside all concern for defense to attack with fierce desperation. When you make your first attack on your turn, you can decide to attack recklessly. Doing so gives you advantage on melee weapon attack rolls using Strength during this turn, but attack rolls against you have advantage until your next turn."}
             {:name "Danger Sense"
              :level 2
              :description "At 2nd level, you gain an uncanny sense of when things nearby aren't as they should be, giving you an edge when you dodge away from danger. You have advantage on Dexterity saving throws against effects that you can see, such as traps and spells. To gain this benefit, you can't be blinded, deafened, or incapacitated."}
             {:name "Extra Attack"
              :level 5
              :description "Beginning at 5th level, you can attack twice, instead of once, whenever you take the Attack action on your turn."}
             {:name "Fast Movement"
              :level 5
              :description "Starting at 5th level, your speed increases by 10 feet while you aren't wearing heavy armor."}
             {:name "Feral Instinct"
              :level 7
              :description "By 7th level, your instincts are so honed that you have advantage on initiative rolls. Additionally, if you are surprised at the beginning of combat and aren't incapacitated, you can act normally on your first turn, but only if you enter your rage before doing anything else on that turn."}
             {:name "Brutal Critical"
              :level 9
              :description "Beginning at 9th level, you can roll one additional weapon damage die when determining the extra damage for a critical hit with a melee attack. This increases to two additional dice at 13th level and three additional dice at 17th level."}
             {:name "Relentless Rage"
              :level 11
              :description "Starting at 11th level, your rage can keep you fighting despite grievous wounds. If you drop to 0 hit points while you're raging and don't die outright, you can make a DC 10 Constitution saving throw. If you succeed, you drop to 1 hit point instead. Each time you use this feature after the first, the DC increases by 5. When you finish a short or long rest, the DC resets to 10."}
             {:name "Persistent Rage"
              :level 15
              :description "Beginning at 15th level, your rage is so fierce that it ends early only if you fall unconscious or if you choose to end it."}
             {:name "Indomitable Might"
              :level 18
              :description "Beginning at 18th level, if your total for a Strength check is less than your Strength score, you can use that score in place of the total."}
             {:name "Primal Champion"
              :level 20
              :description "At 20th level, you embody the power of the wilds. Your Strength and Constitution scores increase by 4. Your maximum for those scores is now 24."}]
    :subclass-level 3
    :subclass-title "Primal Path"
    :subclass-help "Your primal path shapes the nature of your barbarian rage and gives you additional features."
    :subclasses [{:name "Path of the Beserker"
                  :traits [{:name "Frenzy"
                            :level 3
                            :description "Starting when you choose this path at 3rd level, you can go into a frenzy when you rage. If you do so, for the duration of your rage you can make a single melee weapon attack as a bonus action on each of your turns after this one. When your rage ends, you suffer one level of exhaustion (as described in appendix A)."}
                           {:name "Mindless Rage"
                            :level 6
                            :description "Beginning at 6th level, you can't be charmed or frightened while raging. If you are charmed or frightened when you enter your rage, the effect is suspended for the duration of the rage."}
                           {:name "Intimidating Presence"
                            :level 10
                            :description "Beginning at 10th level, you can use your action to frighten someone with your menacing presence. When you do so, choose one creature that you can see within 30 feet of you. If the creature can see or hear you, it must succeed on a Wisdom saving throw (DC equal to 8 + your proficiency bonus + your Charisma modifier) or be frightened of you until the end of your next turn. On subsequent turns, you can use your action to extend the duration of this effect on the frightened creature until the end of your next turn. This effect ends if the creature ends its turn out of line of sight or more than 60 feet away from you. If the creature succeeds on its saving throw, you can't use this feature on that creature again for 24 hours."}
                           {:name "Retaliation"
                            :level 14
                            :description "Starting at 14th level, when you take damage from a creature that is within 5 feet of you, you can use your reaction to make a melee weapon attack against that creature."}]}
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
                            :level 14}]}]}
   character-ref))

(defn bard-option [character-ref]
  (class-option
   {:name "Bard"
    :hit-die 8
    :ability-increase-levels [4 8 12 16 19]
    :profs {:armor {:light false}
            :weapon {:simple true :crossbow-hand true :longsword true :rapier true :shortsword true}
            :save {:dex true :cha true}
            :skill-options {:choose 3 :options {:any true}}
            :multiclass-skill-options {:choose 1 :options {:any true}}
            :tool-options {:musical-instrument 3}
            :multiclass-tool-options {:musical-instrument 1}}
    :weapon-choices [{:name "Weapon"
                      :options {:rapier 1
                                :longsword 1
                                :simple 1}}]
    :weapons {:dagger 1}
    :equipment-choices [{:name "Equipment Pack"
                         :options {:diplomats-pack 1
                                   :entertainers-pack 1}}
                        {:name "Musical Instrument"
                         :options (zipmap (map :key opt5e/musical-instruments) (repeat 1))}]
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
             10 {:selections (conj (opt5e/raw-bard-magical-secrets 10) (opt5e/expertise-selection 2))}
             14 {:selections (opt5e/raw-bard-magical-secrets 14)}
             18 {:selections (opt5e/raw-bard-magical-secrets 18)}}
    :traits [{:name "Bardic Inspiration"
              :description "You can inspire others through stirring words or music. To do so, you use a bonus action on your turn to choose one creature other than yourself within 60 feet of you who can hear you. That creature gains one Bardic Inspiration die, a d6.
Once within the next 10 minutes, the creature can roll the die and add the number rolled to one ability check, attack roll, or saving throw it makes. The creature can wait until after it rolls the d20 before deciding to use the Bardic Inspiration die, but must decide before the GM says whether the roll succeeds or fails. Once the Bardic Inspiration die is rolled, it is lost. A creature can have only one Bardic Inspiration die at a time.
You can use this feature a number of times equal to your Charisma modifier (a minimum of once). You regain any expended uses when you finish a long rest.
Your Bardic Inspiration die changes when you reach certain levels in this class. The die becomes a d8 at 5th level, a d10 at 10th level, and a d12 at 15th level."}
             {:name "Jack of All Trades"
              :level 2
              :description "Starting at 2nd level, you can add half your proficiency bonus, rounded down, to any ability check you make that doesn't already include your proficiency bonus."}
             {:name "Song of Rest"
              :level 2
              :description "Beginning at 2nd level, you can use soothing music or oration to help revitalize your wounded allies during a short rest. If you or any friendly creatures who can hear your performance regain hit points at the end of the short rest by spending one or more Hit Dice, each of those creatures regains an extra 1d6 hit points.
The extra hit points increase when you reach certain levels in this class: to 1d8 at 9th level, to 1d10 at 13th level, and to 1d12 at 17th level."}
             {:name "Expertise"
              :level 3
              :description "At 3rd level, choose two of your skill proficiencies. Your proficiency bonus is doubled for any ability check you make that uses either of the chosen proficiencies. At 10th level, you can choose another two skill proficiencies to gain this benefit."}
             {:name "Font of Inspiration"
              :level 5
              :description "Beginning when you reach 5th level, you regain all of 
your expended uses of Bardic Inspiration when you 
finish a short or long rest"}
             {:name "Countercharm"
              :level 6
              :description "At 6th level, you gain the ability to use musical notes or words of power to disrupt mind-influencing effects. As an action, you can start a performance that lasts until the end of your next turn. During that time, you and any friendly creatures within 30 feet of you have advantage on saving throws against being frightened or charmed. A creature must be able to hear you to gain this benefit. The performance ends early if you are incapacitated or silenced or if you voluntarily end it (no action required)."}
             {:name "Magical Secrets"
              :level 10
              :description "By 10th level, you have plundered magical knowledge from a wide spectrum of disciplines. Choose two spells from any class, including this one. A spell you choose must be of a level you can cast, as shown on the Bard table, or a cantrip. The chosen spells count as bard spells for you and are included in the number in the Spells Known column of the Bard table. You learn two additional spells from any class at 14th level and again at 18th level."}
             {:name "Superior Inspiration"
              :level 20
              :description "At 20th level, when you roll initiative and have no uses of Bardic Inspiration left, you regain one use."}]
    :subclass-level 3
    :subclass-title "Bard College"
    :subclass-help "Your bard college is a loose association that preserves bardic traditions and affords additional features"
    :subclasses [{:name "College of Lore"
                  :profs {:skill-options {:choose 3 :options {:any true}}}
                  :selections (opt5e/bard-magical-secrets 6)
                  :traits [{:name "Cutting Wounds"
                            :level 3
                            :description "Also at 3rd level, you learn how to use your wit to distract, confuse, and otherwise sap the confidence and competence of others. When a creature that you can see within 60 feet of you makes an attack roll, an ability check, or a damage roll, you can use your reaction to expend one of your uses of Bardic Inspiration, rolling a Bardic Inspiration die and subtracting the number rolled from the creature's roll. You can choose to use this feature after the creature makes its roll, but before the GM determines whether the attack roll or ability check succeeds or fails, or before the creature deals its damage. The creature is immune if it can't hear you or if it's immune to being charmed."}
                           {:name "Additional Magical Secrets"
                            :level 6
                            :description "At 6th level, you learn two spells of your choice from any class. A spell you choose must be of a level you can cast, as shown on the Bard table, or a cantrip. The chosen spells count as bard spells for you but don't count against the number of bard spells you know."}
                           {:name "Peerless Skill"
                            :level 14
                            :description "Starting at 14th level, when you make an ability check, you can expend one use of Bardic Inspiration. Roll a Bardic Inspiration die and add the number rolled to your ability check. You can choose to do so after you roll the die for the ability check, but before the GM tells you whether you succeed or fail."}]}
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

(defn cleric-spell [spell-level spell-key min-level]
  (mod5e/spells-known spell-level spell-key :wis "Cleric" min-level))

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
    :profs {:armor {:light false :medium false :shields false}
            :weapon {:simple true}
            :save {:wis true :cha true}
            :skill-options {:choose 2 :options {:history true :insight true :medicine true :persuasion true :religion true}}}
    :equipment-choices [{:name "Equipment Pack"
                         :options {:priests-pack 1
                                   :explorers-pack 1}}
                        {:name "Holy Symbol"
                         :options {:holy-symbol 1}}]
    :weapon-choices [{:name "Cleric Weapon"
                      :options {:mace 1
                                :warhammer 1}}]
    :armor-choices [{:name "Armor"
                     :options {:scale-mail 1
                               :leather 1
                               :chain-mail 1}}]
    :armor {:shield 1}
    :selections [(t/selection
                  "Additional Weapon"
                  [(t/option
                    "Light Crossbow and 20 Bolts"
                    :light-crossbow
                    []
                    [(mod5e/weapon :crossbow-light 1)
                     (mod5e/equipment :crossbow-bolt 20)])
                   (weapon-option [:simple 1])])]
    :traits [{:level 2 :name "Channel Divinity: Turn Undead" :description "As an action, you present your holy symbol and speak a prayer censuring the undead. Each undead that can see or hear you within 30 feet of you must make a Wisdom saving throw. If the creature fails its saving throw, it is turned for 1 minute or until it takes any damage.\nA turned creature must spend its turns trying to move as far away from you as it can, and it can't willingly move to a space within 30 feet of you. It also can't take reactions. For its action, it can use only the Dash action or try to escape from an effect that prevents it from moving. If there's nowhere to move, the creature can use the Dodge action."}
             {:level 5 :name "Destroy Undead" :description "When an undead fails its saving throw against your Turn Undead feature, the creature is instantly destroyed if its challenge rating is at or below a certain threshold, as shown in the Destroy Undead table."}
             {:level 10 :name "Divine Intervention" :description "You can call on your deity to intervene on your behalf when your need is great.\nImploring your deity's aid requires you to use your action. Describe the assistance you seek, and roll percentile dice. If you roll a number equal to or lower than your cleric level, your deity intervenes. The DM chooses the nature of the intervention; the e ect of any cleric spell or cleric domain spell would be appropriate.\nIf your deity intervenes, you can't use this feature again for 7 days. Otherwise, you can use it again after you  nish a long rest.\nAt 20th level, your call for intervention succeeds automatically, no roll required."}]
    :subclass-level 1
    :subclass-title "Divine Domain"
    :subclasses [{:name "Life Domain"
                  :profs {:armor {:heavy true}}
                  :modifiers [(cleric-spell 1 :bless 1)
                              (cleric-spell 1 :cure-wounds 1)
                              (cleric-spell 2 :lesser-restoration 3)
                              (cleric-spell 2 :spiritual-weapon 3)
                              (cleric-spell 3 :beacon-of-hope 5)
                              (cleric-spell 3 :revivify 5)
                              (cleric-spell 4 :death-ward 7)
                              (cleric-spell 4 :guardian-of-faith 7)
                              (cleric-spell 5 :mass-cure-wounds 9)
                              (cleric-spell 5 :raise-dead 9)]
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
                  :modifiers [(cleric-spell 1 :command 1)
                              (cleric-spell 1 :identify 1)
                              (cleric-spell 2 :augury 3)
                              (cleric-spell 2 :suggestion 3)
                              (cleric-spell 3 :nondetection 5)
                              (cleric-spell 3 :speak-with-dead 5)
                              (cleric-spell 4 :arcane-eye 7)
                              (cleric-spell 4 :confusion 7)
                              (cleric-spell 5 :legend-lore 9)
                              (cleric-spell 5 :scrying 9)]
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
                  :modifiers [(cleric-spell 0 :light 1)
                              (cleric-spell 1 :burning-hands 1)
                              (cleric-spell 1 :faerie-fire 1)
                              (cleric-spell 2 :flaming-sphere 3)
                              (cleric-spell 2 :scorching-ray 3)
                              (cleric-spell 3 :daylight 5)
                              (cleric-spell 3 :fireball 5)
                              (cleric-spell 4 :guardian-of-faith 7)
                              (cleric-spell 4 :wall-of-fire 7)
                              (cleric-spell 5 :flame-strike 9)
                              (cleric-spell 5 :scrying 9)]
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
                  :modifiers [(cleric-spell 1 :animal-friendship 1)
                              (cleric-spell 1 :speak-with-animals 1)
                              (cleric-spell 2 :barkskin 3)
                              (cleric-spell 2 :spike-growth 3)
                              (cleric-spell 3 :plant-growth 5)
                              (cleric-spell 3 :wind-wall 5)
                              (cleric-spell 4 :dominate-beast 7)
                              (cleric-spell 4 :grasping-vine 7)
                              (cleric-spell 5 :insect-plague 9)
                              (cleric-spell 5 :tree-stride 9)]
                  :selections [(t/selection
                                "Druid Cantrip"
                                (opt5e/spell-options (get-in sl/spell-lists [:druid 0]) 0 :wis "Druid"))]
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
                  :modifiers [(cleric-spell 1 :fog-cloud 1)
                              (cleric-spell 1 :thunderwave 1)
                              (cleric-spell 2 :gust-of-wind 3)
                              (cleric-spell 2 :shatter 3)
                              (cleric-spell 3 :call-lightning 5)
                              (cleric-spell 3 :sleet-storm 5)
                              (cleric-spell 4 :control-water 7)
                              (cleric-spell 4 :ice-storm 7)
                              (cleric-spell 5 :destructive-wave 9)
                              (cleric-spell 5 :insect-plague 9)]
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
                  :modifiers [(cleric-spell 1 :charm-person 1)
                              (cleric-spell 1 :disguise-self 1)
                              (cleric-spell 2 :mirror-image 3)
                              (cleric-spell 2 :pass-without-trace 3)
                              (cleric-spell 3 :blink 5)
                              (cleric-spell 3 :dispel-magic 5)
                              (cleric-spell 4 :dimension-door 7)
                              (cleric-spell 4 :polymorph 7)
                              (cleric-spell 5 :dominate-person 9)
                              (cleric-spell 5 :modify-memory 9)]
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
                  :modifiers [(cleric-spell 1 :divine-favor 1)
                              (cleric-spell 1 :shield-of-faith 1)
                              (cleric-spell 2 :magic-weapon 3)
                              (cleric-spell 2 :spiritual-weapon 3)
                              (cleric-spell 3 :crusaders-mantle 5)
                              (cleric-spell 3 :spirits-guardians 5)
                              (cleric-spell 4 :freedom-of-movement 7)
                              (cleric-spell 4 :stoneskin 7)
                              (cleric-spell 5 :flame-strike 9)
                              (cleric-spell 5 :hold-monster 9)]
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

(defn druid-spell [spell-level spell-key min-level]
  (mod5e/spells-known spell-level spell-key :wis "Druid" min-level))

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
    :profs {:armor {:light false :medium false :shields false}
            :weapon {:club true :dagger true :dart true :javelin true :mace true :quarterstaff true :scimitar true :sickle true :sling true :spear true}
            :tool {:herbalism-kit true}
            :save {:int true :wis true}
            :skill-options {:choose 2 :options {:arcana true :animal-handling true :insight true :medicine true :nature true :perception true :religion true :survival true}}}
    :armor {:leather 1}
    :equipment-choices [{:name "Equipment Pack"
                         :options {:priests-pack 1
                                   :explorers-pack 1}}
                        {:name "Druidic Focus"
                         :options {:druidic-focus 1}}]
    :equipment {:explorers-pack 1}
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
              :description "You know Druidic, the secret language of druids. You can speak the language and use it to leave hidden messages. You and others who know this language automatically spot such a message. Others spot the message's presence with a successful DC 15 Wisdom (Perception) check but can't decipher it without magic."}
             {:name "Wild Shape"
              :description "Starting at 2nd level, you can use your action to magically assume the shape of a beast that you have seen before. You can use this feature twice. You regain expended uses when you finish a short or long rest. Your druid level determines the beasts you can transform into, as shown in the Beast Shapes table. At 2nd level, for example, you can transform into any beast that has a challenge rating of 1/4 or lower that doesn't have a flying or swimming speed. (see the Players Handbook for further details)"}
             {:name "Timeless Body"
              :level 18
              :description "Starting at 18th level, the primal magic that you wield causes you to age more slowly. For every 10 years that pass, your body ages only 1 year."}
             {:name "Beast Spells"
              :level 18
              :description "Beginning at 18th level, you can cast many of your druid spells in any shape you assume using Wild Shape. You can perform the somatic and verbal components of a druid spell while in a beast shape, but you aren't able to provide material components."}
             {:name "Archdruid"
              :level 20
              :description "At 20th level, you can use your Wild Shape an unlimited number of times. Additionally, you can ignore the verbal and somatic components of your druid spells, as well as any material components that lack a cost and aren't consumed by a spell. You gain this benefit in both your normal shape and your beast shape from Wild Shape."}]
    :subclass-level 2
    :subclass-title "Druid Circle"
    :subclasses [{:name "Circle of the Land"
                  :selections [(t/selection
                                "Bonus Cantrip"
                                (opt5e/spell-options (get-in sl/spell-lists [:druid 0]) 0 :wis "Druid"))
                               (t/selection
                                "Land Type"
                                [(t/option
                                  "Arctic"
                                  :arctic
                                  []
                                  [(druid-spell 3 :slow 5)
                                   (druid-spell 5 :cone-of-cold 9)])
                                 (t/option
                                  "Coast"
                                  :coast
                                  []
                                  [(druid-spell 2 :mirror-image 3)
                                   (druid-spell 2 :misty-step 3)])
                                 (t/option
                                  "Desert"
                                  :desert
                                  []
                                  [(druid-spell 2 :blur 3)
                                   (druid-spell 2 :silence 3)
                                   (druid-spell 3 :create-food-and-water 5)])
                                 (t/option
                                  "Forest"
                                  :forest
                                  []
                                  [(druid-spell 2 :spider-climb 3)
                                   (druid-spell 4 :divination 7)])
                                 (t/option
                                  "Grassland"
                                  :grassland
                                  []
                                  [(druid-spell 2 :invisibility 3)
                                   (druid-spell 3 :haste 5)
                                   (druid-spell 4 :divination 7)
                                   (druid-spell 5 :dream 9)])
                                 (t/option
                                  "Mountain"
                                  :mountain
                                  []
                                  [(druid-spell 2 :spider-climb 3)
                                   (druid-spell 3 :lightning-bolt 5)])
                                 (t/option
                                  "Swamp"
                                  :swamp
                                  []
                                  [(druid-spell 2 :darkness 3)
                                   (druid-spell 2 :melfs-acid-arrow 3)
                                   (druid-spell 3 :stinking-cloud 5)])
                                 (t/option
                                  "Underdark"
                                  :underdark
                                  []
                                  [(druid-spell 2 :spider-climb 3)
                                   (druid-spell 2 :web 3)
                                   (druid-spell 3 :stinking-cloud 5)
                                   (druid-spell 3 :gaseous-form 5)
                                   (druid-spell 4 :greater-invisibility 7)
                                   (druid-spell 5 :cloudkill 9)])])]
                  :modifiers []
                  :traits [{:name "Natural Recovery"
                            :level 2
                            :description "Starting at 2nd level, you can regain some of your magical energy by sitting in meditation and communing with nature. During a short rest, you choose expended spell slots to recover. The spell slots can have a combined level that is equal to or less than half your druid level (rounded up), and none of the slots can be 6th level or higher. You can't use this feature again until you finish a long rest.For example, when you are a 4th-level druid, you can recover up to two levels worth of spell slots. You can recover either a 2nd-level slot or two 1st-level slots"}
                           {:name "Land's Stride"
                            :level 6
                            :description "Starting at 6th level, moving through nonmagical difficult terrain costs you no extra movement. You can also pass through nonmagical plants without being slowed by them and without taking damage from them if they have thorns, spines, or a similar hazard.
In addition, you have advantage on saving throws against plants that are magically created or manipulated to impede movement, such those created by the entangle spell."}
                           {:name "Nature's Ward"
                            :level 10
                            :description "When you reach 10th level, you can't be charmed or frightened by elementals or fey, and you are immune to poison and disease."}
                           {:name "Nature's Santuary"
                            :level 14
                            :description "When you reach 14th level, creatures of the natural world sense your connection to nature and become hesitant to attack you. When a beast or plant creature attacks you, that creature must make a Wisdom saving throw against your druid spell save DC. On a failed save, the creature must choose a different target, or the attack automatically misses. On a successful save, the creature is immune to this effect for 24 hours.
The creature is aware of this effect before it makes its attack against you."}]}
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
    :profs {:armor {:light false :medium false :heavy true :shields false}
            :weapon {:simple false :martial false} 
            :save {:str true :con true}
            :skill-options {:choose 2 :options {:acrobatics true :animal-handling true :athletics true :history true :insight true :intimidation true :perception true :survival true}}}
    :equipment-choices [{:name "Equipment Pack"
                         :options {:dungeoneers-pack 1
                                   :explorers-pack 1}}]
    :traits [{:name "Second Wind" :description "You have a limited well of stamina that you can draw on to protect yourself from harm. On your turn, you can use a bonus action to regain hit points equal to 1d10 + your fighter level.\nOnce you use this feature, you must  nish a short or long rest before you can use it again."}
             {:level 2 :name "Action Surge" :description "You can push yourself beyond your normal limits for a moment. On your turn, you can take one additional action on top of your regular action and a possible bonus action.\nOnce you use this feature, you must finish a short or long rest before you can use it again. Starting at 17th level, you can use it twice before a rest, but only once on the same turn."}
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
                    [(mod5e/weapon :crossbow-light 1)
                     (mod5e/equipment :crossbow-bolt 20)])
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
            :weapon {:simple false :shortsword false}
            :save {:dex true :str true}
            :skill-options {:choose 2 :options {:acrobatics true :athletics true :history true :insight true :religion true :stealth true}}}
    :equipment-choices [{:name "Equipment Pack"
                         :options {:dungeoneers-pack 1
                                   :explorers-pack 1}}]
    :weapon-choices [{:name "Weapon"
                      :options {:shortsword 1
                                :simple 1}}]
    :modifiers [(mod/modifier ?armor-class
                              (+ (?ability-bonuses :wis) ?armor-class)
                              nil
                              nil
                              [(= :monk (first ?classes))])
                (mod/modifier ?armor-class-with-armor
                              (fn [armor & [shield?]]
                                (if (and (nil? armor) (not shield?))
                                  ?armor-class
                                  (+ (if shield? 2 0)
                                     (?armor-dex-bonus armor)
                                     (or (:base-ac armor) 10))))
                              nil
                              nil
                              [(= :monk (first ?classes))])]
    :levels {2 {:modifiers [(mod5e/unarmored-speed-bonus 10)]}
             5 {:modifiers [(mod5e/extra-attack)]}
             6 {:modifiers [(mod5e/unarmored-speed-bonus 5)]}
             10 {:modifiers [(mod5e/immunity :poison)
                             (mod5e/immunity :disease)
                             (mod5e/unarmored-speed-bonus 5)]}
             13 {:modifiers (map
                             (fn [{:keys [name key]}]
                               (mod5e/language name key))
                             opt5e/languages)}
             14 {:modifiers [(mod5e/saving-throws char5e/ability-keys)
                             (mod5e/unarmored-speed-bonus 5)]}
             18 {:modifiers [(mod5e/unarmored-speed-bonus 5)]}}
    :equipment {:dart 10}
    :traits [{:name "Ki"
              :level 2
              :description "Starting at 2nd level, your training allows you to harness the mystic energy of ki. Your access to this energy is represented by a number of ki points. Your monk level determines the number of points you have, as shown in the Ki Points column of the Monk table.
You can spend these points to fuel various ki features. You start knowing three such features: Flurry of Blows, Patient Defense, and Step of the Wind. You learn more ki features as you gain levels in this class.
When you spend a ki point, it is unavailable until you finish a short or long rest, at the end of which you draw all of your expended ki back into yourself. You must spend at least 30 minutes of the rest meditating to regain your ki points.
Some of your ki features require your target to make a saving throw to resist the feature's effects. The saving throw DC is calculated as follows:
Ki save DC = 8 + your proficiency bonus + your Wisdom modifier"}
             {:name "Flurry of Blows"
              :level 2
              :description "Immediately after you take the Attack action on your turn, you can spend 1 ki point to make two unarmed strikes as a bonus action."}
             {:name "Patient Defense"
              :level 2
              :description "You can spend 1 ki point to take the Dodge action as a bonus action on your turn."}
             {:name "Step of the Wind"
              :level 2
              :description "You can spend 1 ki point to take the Disengage or Dash action as a bonus action on your turn, and your jump distance is doubled for the turn."}
             {:name "Unarmored Movement"
              :level 2
              :description "Starting at 2nd level, your speed increases by 10 feet while you are not wearing armor or wielding a shield. This bonus increases when you reach certain monk levels, as shown in the Monk table.
At 9th level, you gain the ability to move along vertical surfaces and across liquids on your turn without falling during the move."}
             {:name "Deflect Missiles"
              :level 3
              :description "Starting at 3rd level, you can use your reaction to deflect or catch the missile when you are hit by a ranged weapon attack. When you do so, the damage you take from the attack is reduced by 1d10 + your Dexterity modifier + your monk level.
If you reduce the damage to 0, you can catch the missile if it is small enough for you to hold in one hand and you have at least one hand free. If you catch a missile in this way, you can spend 1 ki point to make a ranged attack with the weapon or piece of ammunition you just caught, as part of the same reaction. You make this attack with proficiency, regardless of your weapon proficiencies, and the missile counts as a monk weapon for the attack, which has a normal range of 20 feet and a long range of 60 feet."}
             {:name "Slow Fall"
              :level 4
              :description "Beginning at 4th level, you can use your reaction when you fall to reduce any falling damage you take by an amount equal to five times your monk level."}
             {:name "Extra Attack"
              :level 5
              :description "Beginning at 5th level, you can attack twice, instead of once, whenever you take the Attack action on your turn."}
             {:name "Stunning Strike"
              :level 5
              :description "Starting at 5th level, you can interfere with the flow of ki in an opponent's body. When you hit another creature with a melee weapon attack, you can spend 1 ki point to attempt a stunning strike. The target must succeed on a Constitution saving throw or be stunned until the end of your next turn."}
             {:name "Ki-Empowered Strikes"
              :level 6
              :description "Starting at 6th level, your unarmed strikes count as magical for the purpose of overcoming resistance and immunity to nonmagical attacks and damage"}
             {:name "Evasion"
              :level 7
              :description "At 7th level, your instinctive agility lets you dodge out of the way of certain area effects, such as a blue dragon's lightning breath or a fireball spell. When you are subjected to an effect that allows you to make a Dexterity saving throw to take only half damage, you instead take no damage if you succeed on the saving throw, and only half damage if you fail."}
             {:name "Stillness of Mind"
              :level 7
              :description "Starting at 7th level, you can use your action to end one effect on yourself that is causing you to be charmed or frightened."}
             {:name "Purity of Body"
              :level 10
              :description "At 10th level, your mastery of the ki flowing through you makes you immune to disease and poison."}
             {:name "Tongue of the Sun and Moon"
              :level 13
              :description "Starting at 13th level, you learn to touch the ki of other minds so that you understand all spoken languages. Moreover, any creature that can understand a language can understand what you say."}
             {:name "Diamond Soul"
              :level 14
              :description "Beginning at 14th level, your mastery of ki grants you proficiency in all saving throws.Additionally, whenever you make a saving throw and fail, you can spend 1 ki point to reroll it and take the second result."}
             {:name "Timeless Body"
              :level 15
              :description "At 15th level, your ki sustains you so that you suffer none of the frailty of old age, and you can't be aged magically. You can still die of old age, however. In addition, you no longer need food or water."}
             {:name "Empty Body"
              :level 18
              :description "Beginning at 18th level, you can use your action to spend 4 ki points to become invisible for 1 minute. During that time, you also have resistance to all damage but force damage.Additionally, you can spend 8 ki points to cast the astral projection spell, without needing material components. When you do so, you can't take any other creatures with you."}
             {:name "Perfect Self"
              :level 20
              :description "At 20th level, when you roll for initiative and have no ki points remaining, you regain 4 ki points."}]
    :subclass-level 3
    :subclass-title "Monastic Tradition"
    :subclasses [{:name "Way of the Open Hand"
                  :traits [{:name "Open Hand Technique"
                            :level 3
                            :description "Starting when you choose this tradition at 3rd level, you can manipulate your enemy's ki when you harness your own. Whenever you hit a creature with one of the attacks granted by your Flurry of Blows, you can impose one of the following effects on that target:
* It must succeed on a Dexterity saving throw or be knocked prone.
* It must make a Strength saving throw. If it fails, you can push it up to 15 feet away from you. 
* It can't take reactions until the end of your next turn."}
                           {:name "Wholeness of Body"
                            :level 6
                            :description "At 6th level, you gain the ability to heal yourself. As an action, you can regain hit points equal to three times your monk level. You must finish a long rest before you can use this feature again."}
                           {:name "Tranquility"
                            :level 11
                            :description "Beginning at 11th level, you can enter a special meditation that surrounds you with an aura of peace. At the end of a long rest, you gain the effect of a sanctuary spell that lasts until the start of your next long rest (the spell can end early as normal). The saving throw DC for the spell equals 8 + your Wisdom modifier + your proficiency bonus."}
                           {:name "Quivering Palm"
                            :level 17
                            :description "At 17th level, you gain the ability to set up lethal vibrations in someone's body. When you hit a creature with an unarmed strike, you can spend 3 ki points to start these imperceptible vibrations, which last for a number of days equal to your monk level. The vibrations are harmless unless you use your action to end them. To do so, you and the target must be on the same plane of existence. When you use this action, the creature must make a Constitution saving throw. If it fails, it is reduced to 0 hit points. If it succeeds, it takes 10d10 necrotic damage.
You can have only one creature under the effect of this feature at a time. You can choose to end the vibrations harmlessly without using an action."}]}
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

(defn paladin-spell [spell-level key min-level]
  (mod5e/spells-known spell-level key :wis "Paladin" min-level))

(defn paladin-option [character-ref]
  (class-option
   {:name "Paladin"
    :spellcaster true
    :spellcasting {:level-factor 2
                   :known-mode :all
                   :ability :cha}
    :hit-die 10
    :ability-increase-levels [4 8 12 16 19]
    :profs {:armor {:light false :medium false :heavy true :shields false}
            :weapon {:simple false :martial false}
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
              :description "The presence of strong evil registers on your senses like a noxious odor, and powerful good rings like heavenly music in your ears. As an action, you can open your awareness to detect such forces. Until the end of your next turn, you know the location of any celestial, fiend, or undead within 60 feet of you that is not behind total cover. You know the type (celestial, fiend, or undead) of any being whose presence you sense, but not its identity (the vampire Count Strahd von Zarovich, for instance). Within the same radius, you also detect the presence of any place or object that has been consecrated or desecrated, as with the hallow spell.You can use this feature a number of times equal to 1 + your Charisma modifier. When you finish a long rest, you regain all expended uses."}
             {:name "Lay on Hands"
              :description "Your blessed touch can heal wounds. You have a pool of healing power that replenishes when you take a long rest. With that pool, you can restore a total number of hit points equal to your paladin level × 5.
As an action, you can touch a creature and draw power from the pool to restore a number of hit points to that creature, up to the maximum amount remaining in your pool.
Alternatively, you can expend 5 hit points from your pool of healing to cure the target of one disease or neutralize one poison affecting it. You can cure multiple diseases and neutralize multiple poisons with a single use of Lay on Hands, expending hit points separately for each one.This feature has no effect on undead and constructs."}
             {:name "Divine Smite"
              :level 2
              :description "Starting at 2nd level, when you hit a creature with a melee weapon attack, you can expend one spell slot to deal radiant damage to the target, in addition to the weapon's damage. The extra damage is 2d8 for a 1st-level spell slot, plus 1d8 for each spell level higher than 1st, to a maximum of 5d8. The damage increases by 1d8 if the target is an undead or a fiend."}
             {:name "Divine Health"
              :level 3
              :description "By 3rd level, the divine magic flowing through you makes you immune to disease."}
             {:name "Extra Attack"
              :level 5
              :description "Beginning at 5th level, you can attack twice, instead of once, whenever you take the Attack action on your turn."}
             {:name "Aura of Protection"
              :level 6
              :description "Starting at 6th level, whenever you or a friendly creature within 10 feet of you must make a saving throw, the creature gains a bonus to the saving throw equal to your Charisma modifier (with a minimum bonus of +1). You must be conscious to grant this bonus.
At 18th level, the range of this aura increases to 30 feet."}
             {:name "Aura of Courage"
              :level 10
              :description "Starting at 10th level, you and friendly creatures within 10 feet of you can't be frightened while you are conscious.
At 18th level, the range of this aura increases to 30 feet."}
             {:name "Improved Divine Smite"
              :level 11
              :description "By 11th level, you are so suffused with righteous might that all your melee weapon strikes carry divine power with them. Whenever you hit a creature with a melee weapon, the creature takes an extra 1d8 radiant damage. If you also use your Divine Smite with an attack, you add this damage to the extra damage of your Divine Smite."}
             {:name "Cleansing Touch"
              :level 14
              :description "Beginning at 14th level, you can use your action to end one spell on yourself or on one willing creature that you touch.
You can use this feature a number of times equal to your Charisma modifier (a minimum of once). You regain expended uses when you finish a long rest."}]
    :subclass-level 3
    :subclass-title "Sacred Oath"
    :subclasses [{:name "Oath of Devotion"
                  :modifiers [(paladin-spell 1 :protection-from-evil-and-good 3)
                              (paladin-spell 1 :sanctuary 3)
                              (paladin-spell 2 :lesser-restoration 5)
                              (paladin-spell 2 :zone-of-truth 5)
                              (paladin-spell 3 :beacon-of-hope 9)
                              (paladin-spell 3 :dispel-magic 9)
                              (paladin-spell 4 :freedom-of-movement 13)
                              (paladin-spell 4 :guardian-of-faith 13)
                              (paladin-spell 5 :commune 17)
                              (paladin-spell 5 :flame-strike 17)]
                  :traits [{:name "Channel Divinity"
                            :level 3
                            :description "When you take this oath at 3rd level, you gain the following two Channel Divinity options.
Sacred Weapon. As an action, you can imbue one weapon that you are holding with positive energy, using your Channel Divinity. For 1 minute, you add your Charisma modifier to attack rolls made with that weapon (with a minimum bonus of +1). The weapon also emits bright light in a 20-foot radius and dim light 20 feet beyond that. If the weapon is not already magical, it becomes magical for the duration.
You can end this effect on your turn as part of any other action. If you are no longer holding or carrying this weapon, or if you fall unconscious, this effect ends.
Turn the Unholy. As an action, you present your holy symbol and speak a prayer censuring fiends and undead, using your Channel Divinity. Each fiend or undead that can see or hear you within 30 feet of you must make a Wisdom saving throw. If the creature fails its saving throw, it is turned for 1 minute or until it takes damage.
A turned creature must spend its turns trying to move as far away from you as it can, and it can't willingly move to a space within 30 feet of you. It also can't take reactions. For its action, it can use only the Dash action or try to escape from an effect that prevents it from moving. If there's nowhere to move, the creature can use the Dodge action."}
                           {:name "Aura of Devotion"
                            :level 7
                            :description "Starting at 7th level, you and friendly creatures within 10 feet of you can't be charmed while you are conscious.
At 18th level, the range of this aura increases to 30 feet."}
                           {:name "Purity of Spirit"
                            :level 15
                            :description "Beginning at 15th level, you are always under the effects of a protection from evil and good spell."}
                           {:name "Holy Nimbus"
                            :level 20
                            :description "At 20th level, as an action, you can emanate an aura of sunlight. For 1 minute, bright light shines from you in a 30-foot radius, and dim light shines 30 feet beyond that.
Whenever an enemy creature starts its turn in the bright light, the creature takes 10 radiant damage.
In addition, for the duration, you have advantage on saving throws against spells cast by fiends or undead.
Once you use this feature, you can't use it again until you finish a long rest."}]}
                 {:name "Oath of the Ancients"
                  :modifiers [(paladin-spell 1 :ensnaring-strike 3)
                              (paladin-spell 1 :speak-with-animals 3)
                              (paladin-spell 2 :misty-step 5)
                              (paladin-spell 2 :moonbeam 5)
                              (paladin-spell 3 :plant-growth 9)
                              (paladin-spell 3 :protection-from-energy 9)
                              (paladin-spell 4 :ice-storm 13)
                              (paladin-spell 4 :stoneskin 13)
                              (paladin-spell 5 :commune-with-nature 17)
                              (paladin-spell 5 :tree-stride 17)]
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
                  :modifiers [(paladin-spell 1 :bane 3)
                              (paladin-spell 1 :hunters-mark 3)
                              (paladin-spell 2 :hold-person 5)
                              (paladin-spell 2 :misty-step 5)
                              (paladin-spell 3 :haste 9)
                              (paladin-spell 3 :protection-from-energy 9)
                              (paladin-spell 4 :banishment 13)
                              (paladin-spell 4 :dimension-door 13)
                              (paladin-spell 5 :hold-monster 17)
                              (paladin-spell 5 :scrying 17)]
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

(def ranger-skills {:animal-handling true :athletics true :insight true :investigation true :nature true :perception true :stealth true :survival true})

(defn ranger-option [character-ref]
  (class-option
   {:name "Ranger"
    :hit-die 10
    :profs {:armor {:light false :medium false :shields false}
            :weapon {:simple false :martial false}
            :save {:str true :dex true}
            :skill-options {:choose 3 :options ranger-skills}
            :multiclass-skill-options {:choose 1 :options ranger-skills}}
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
              :description "Beginning at 3rd level, you can use your action and expend one ranger spell slot to focus your awareness on the region around you. For 1 minute per level of the spell slot you expend, you can sense whether the following types of creatures are present within 1 mile of you (or within up to 6 miles if you are in your favored terrain): aberrations, celestials, dragons, elementals, fey, fiends, and undead. This feature doesn't reveal the creatures' location or number."}
             {:name "Extra Attack"
              :level 5
              :description "Beginning at 5th level, you can attack twice, instead of once, whenever you take the Attack action on your turn."}
             {:name "Land's Stride"
              :level 8
              :description "Starting at 8th level, moving through nonmagical difficult terrain costs you no extra movement. You can also pass through nonmagical plants without being slowed by them and without taking damage from them if they have thorns, spines, or a similar hazard.
In addition, you have advantage on saving throws against plants that are magically created or manipulated to impede movement, such those created by the entangle spell"}
             {:name "Hide in Plain Sight"
              :level 10
              :description "Starting at 10th level, you can spend 1 minute creating camouflage for yourself. You must have access to fresh mud, dirt, plants, soot, and other naturally occurring materials with which to create your camouflage.
Once you are camouflaged in this way, you can try to hide by pressing yourself up against a solid surface, such as a tree or wall, that is at least as tall and wide as you are. You gain a +10 bonus to Dexterity (Stealth) checks as long as you remain there without moving or taking actions. Once you move or take an action or a reaction, you must camouflage yourself again to gain this benefit."}
             {:name "Vanish"
              :level 14
              :description "Starting at 14th level, you can use the Hide action as a bonus action on your turn. Also, you can't be tracked by nonmagical means, unless you choose to leave a trail."}
             {:name "Feral Senses"
              :level 18
              :description "At 18th level, you gain preternatural senses that help you fight creatures you can't see. When you attack a creature you can't see, your inability to see it doesn't impose disadvantage on your attack rolls against it.You are also aware of the location of any invisible creature within 30 feet of you, provided that the creature isn't hidden from you and you aren't blinded or deafened."}
             {:name "Foe Slayer"
              :level 20
              :description "At 20th level, you become an unparalleled hunter of your enemies. Once on each of your turns, you can add your Wisdom modifier to the attack roll or the damage roll of an attack you make against one of your favored enemies. You can choose to use this feature before or after the roll, but before any effects of the roll are applied."}
             {:name "Natural Explorer"
              :description "You are particularly familiar with one type of natural environment and are adept at traveling and surviving in such regions. Choose one type of favored terrain: arctic, coast, desert, forest, grassland, mountain, or swamp. When you make an Intelligence or Wisdom check related to your favored terrain, your proficiency bonus is doubled if you are using a skill that you're proficient in.
While traveling for an hour or more in your favored terrain, you gain the following benefits:
* Difficult terrain doesn't slow your group's travel.
* Your group can't become lost except by magical means.
* Even when you are engaged in another activity while traveling (such as foraging, navigating, or tracking), you remain alert to danger.
* If you are traveling alone, you can move stealthily at a normal pace.
* When you forage, you find twice as much food as you normally would.
* While tracking other creatures, you also learn their exact number, their sizes, and how long ago they passed through the area.
You choose additional favored terrain types at 6th and 10th level."}]
    :subclass-level 3
    :subclass-title "Ranger Archetype"
    :subclasses [{:name "Hunter"
                  :levels {3 {:selections [(t/selection
                                            "Hunter's Prey"
                                            [(t/option
                                              "Colossus Slayer"
                                              :colossus-slayer
                                              []
                                              [(mod5e/trait "Colossus Slayer" "Your tenacity can wear down the most potent foes. When you hit a creature with a weapon attack, the creature takes an extra 1d8 damage if it's below its hit point maximum. You can deal this extra damage only once per turn.")])
                                             (t/option
                                              "Giant Killer"
                                              :giant-killer
                                              []
                                              [(mod5e/trait "Giant Killer" "When a Large or larger creature within 5 feet of you hits or misses you with an attack, you can use your reaction to attack that creature immediately after its attack, provided that you can see the creature.")])
                                             (t/option
                                              "Horde Breaker"
                                              :horde-breaker
                                              []
                                              [(mod5e/trait "Horde Breaker" "Once on each of your turns when you make a weapon attack, you can make another attack with the same weapon against a different creature that is within 5 feet of the original target and within range of your weapon")])])]}
                           7 {:selections [(t/selection
                                            "Defensive Tactics"
                                            [(t/option
                                              "Escape the Horde"
                                              :escape-the-horde
                                              []
                                              [(mod5e/trait "Escape the Horde" "Opportunity attacks against you are made with disadvantage.")])                                             (t/option
                                              "Multiattack Defense"
                                              :multiattack-defense
                                              []
                                              [(mod5e/trait "Multiattack Defense" "When a creature hits you with an attack, you gain a +4 bonus to AC against all subsequent attacks made by that creature for the rest of the turn.")])
                                             (t/option
                                              "Steel Will"
                                              :steel-will
                                              []
                                              [(mod5e/trait "Steel Will" "You have advantage on saving throws against being frightened.")])])]}
                           11 {:selections [(t/selection
                                            "Multiattack"
                                            [(t/option
                                              "Volley"
                                              :volley
                                              []
                                              [(mod5e/trait "Volley" "You can use your action to make a ranged attack against any number of creatures within 10 feet of a point you can see within your weapon's range. You must have ammunition for each target, as normal, and you make a separate attack roll for each target.")])
                                             (t/option
                                              "Whirlwind Attack"
                                              :whirlwind-attack
                                              []
                                              [(mod5e/trait "Whirlwind Attack" "You can use your action to make a melee attack against any number of creatures within 5 feet of you, with a separate attack roll for each target.")])])]}
                           15 {:selections [(t/selection
                                            "Superior Hunter's Defense"
                                            [(t/option
                                              "Evasion"
                                              :evasion
                                              []
                                              [(mod5e/trait "Evasion" "When you are subjected to an effect, such as a red dragon's fiery breath or a lightning bolt spell, that allows you to make a Dexterity saving throw to take only half damage, you instead take no damage if you succeed on the saving throw, and only half damage if you fail.")])
                                             (t/option
                                              "Stand Against the Tide"
                                              :stand-against-the-tide
                                              []
                                              [(mod5e/trait "Stand Against the Tide" "When a hostile creature misses you with a melee attack, you can use your reaction to force that creature to repeat the same attack against another creature (other than itself) of your choice.")])
                                             (t/option
                                              "Uncanny Dodge"
                                              :uncanny-dodge
                                              []
                                              [(mod5e/trait "Uncanny Dodge" "When an attacker that you can see hits you with an attack, you can use your reaction to halve the attack's damage against you.")])])]}}}
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

(def rogue-skills {:acrobatics true :athletics true :deception true :insight true :intimidation true :investigation true :perception true :performance true :persuasion true :sleight-of-hand true :stealth true})

(defn rogue-option [character-ref]
  (class-option
   {:name "Rogue",
    :hit-die 8
    :ability-increase-levels [4 8 12 16 19]
    :expertise true
    :profs {:armor {:light false}
            :weapon {:simple true :crossbow-hand true :longsword true :rapier true :shortsword true}
            :save {:dex true :int true}
            :tool {:thieves-tools false}
            :skill-options {:order 0 :choose 4 :options rogue-skills}
            :multiclass-skill-options {:order 0 :choose 1 :options rogue-skills}}
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
                 (assoc
                  opt5e/rogue-expertise-selection
                  ::t/order
                  1)]
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
                  :modifiers [(mod5e/spells-known 0 :mage-hand :int "Arcane Trickster")]
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

(defn sorcerer-option [character-ref]
  (class-option
   {:name "Sorcerer"
    :spellcasting {:level-factor 1
                   :cantrips-known {1 4 4 1 10 1}
                   :known-mode :schedule
                   :spells-known {1 2
                                  2 1
                                  3 1
                                  4 1
                                  5 1
                                  6 1
                                  7 1
                                  8 1
                                  9 1
                                  10 1
                                  11 1
                                  13 1
                                  15 1
                                  17 1}
                   :ability :cha}
    :spellcaster true
    :hit-die 6
    :ability-increase-levels [4 8 12 16 19]
    :profs {:weapon {:dagger true :dart true :sling true :quarterstaff true :crossbow-light true}
            :save {:con true :cha true}
            :skill-options {:choose 2 :options {:arcana true :deception true :insight true :intimidation true :persuasion true :religion true}}}
    :selections [(t/selection
                  "Weapon"
                  [(t/option
                    "Light Crossbow"
                    :crossbow
                    []
                    [(mod5e/weapon :crossbow-light 1)
                     (mod5e/equipment :crossbow-bolt 20)])
                   (weapon-option [:simple 1])])]
    :equipment-choices [{:name "Equipment Pack"
                         :options {:dungeoneers-pack 1
                                   :explorers-pack 1}}
                        {:name "Spellcasting Equipment"
                         :options {:component-pouch 1
                                   :arcane-focus 1}}]
    :weapons {:dagger 1}
    :traits [{:name "Flexible Casting"
              :level 2
              :description "You can use your sorcery points to gain additional spell slots, or sacrifice spell slots to gain additional sorcery points. You learn other ways to use your sorcery points as you reach higher levels.Creating Spell Slots. You can transform unexpended sorcery points into one spell slot as a bonus action on your turn. The Creating Spell Slots table shows the cost of creating a spell slot of a given level. You can create spell slots no higher in level than 5th.
Any spell slot you create with this feature vanishes when you finish a long rest."}
             {:name "Careful Spell"
              :level 3
              :description "When you cast a spell that forces other creatures to make a saving throw, you can protect some of those creatures from the spell's full force. To do so, you spend 1 sorcery point and choose a number of those creatures up to your Charisma modifier (minimum of one creature). A chosen creature automatically succeeds on its saving throw against the spell."}
             {:name "Distant Spell"
              :level 3
              :description "When you cast a spell that has a range of 5 feet or greater, you can spend 1 sorcery point to double the range of the spell.
When you cast a spell that has a range of touch, you can spend 1 sorcery point to make the range of the spell 30 feet."}
             {:name "Empowered Spell"
              :level 3
              :description "When you roll damage for a spell, you can spend 1 sorcery point to reroll a number of the damage dice up to your Charisma modifier (minimum of one). You must use the new rolls.
You can use Empowered Spell even if you have already used a different Metamagic option during the casting of the spell."}
             {:name "Extended Spell"
              :level 3
              :description "When you cast a spell that has a duration of 1 minute or longer, you can spend 1 sorcery point to double its duration, to a maximum duration of 24 hours."}
             {:name "Heightened Spell"
              :level 3
              :description "When you cast a spell that forces a creature to make a saving throw to resist its effects, you can spend 3 sorcery points to give one target of the spell disadvantage on its first saving throw made against the spell."}
             {:name "Quickened Spell"
              :level 3
              :description "When you cast a spell that has a casting time of 1 action, you can spend 2 sorcery points to change the casting time to 1 bonus action for this casting."}
             {:name "Subtle Spell"
              :level 3
              :description "When you cast a spell, you can spend 1 sorcery point to cast it without any somatic or verbal components."}
             {:name "Twinned Spell"
              :level 3
              :description "When you cast a spell that targets only one creature and doesn't have a range of self, you can spend a number of sorcery points equal to the spell's level to target a second creature in range with the same spell (1 sorcery point if the spell is a cantrip).To be eligible, a spell must be incapable of targeting more than one creature at the spell's current level. For example, magic missile and scorching ray aren't eligible, but ray of frost and chromatic orb are"}]
    :subclass-title "Sorcerous Origin"
    :subclass-level 1
    :subclasses [{:name "Draconic Ancestry"
                  :modifiers [(mod/modifier ?hit-point-level-bonus (+ 1 ?hit-point-level-bonus))]
                  :selections [(t/selection
                                "Draconic Ancestry Type"
                                [(t/option
                                  "Black"
                                  :black
                                  []
                                  [])
                                 (t/option
                                  "Blue"
                                  :blue
                                  []
                                  [])
                                 (t/option
                                  "Brass"
                                  :brass
                                  []
                                  [])
                                 (t/option
                                  "Bronze"
                                  :bronze
                                  []
                                  [])
                                 (t/option
                                  "Copper"
                                  :copper
                                  []
                                  [])
                                 (t/option
                                  "Gold"
                                  :gold
                                  []
                                  [])
                                 (t/option
                                  "Green"
                                  :green
                                  []
                                  [])
                                 (t/option
                                  "Red"
                                  :red
                                  []
                                  [])
                                 (t/option
                                  "Silver"
                                  :silver
                                  []
                                  [])
                                 (t/option
                                  "White"
                                  :white
                                  []
                                  [])])]
                  :traits [{:name "Draconic Resilience"
                            :description "As magic flows through your body, it causes physical traits of your dragon ancestors to emerge. At 1st level, your hit point maximum increases by 1 and increases by 1 again whenever you gain a level in this class.
Additionally, parts of your skin are covered by a thin sheen of dragon-like scales. When you aren't wearing armor, your AC equals 13 + your Dexterity modifier."}
                           {:name "Elemental Affinity"
                            :level 6
                            :description "Starting at 6th level, when you cast a spell that deals damage of the type associated with your draconic ancestry, you can add your Charisma modifier to one damage roll of that spell. At the same time, you can spend 1 sorcery point to gain resistance to that damage type for 1 hour."}
                           {:name "Dragon Wings"
                            :level 14
                            :description "At 14th level, you gain the ability to sprout a pair of dragon wings from your back, gaining a flying speed equal to your current speed. You can create these wings as a bonus action on your turn. They last until you dismiss them as a bonus action on your turn.
You can't manifest your wings while wearing armor unless the armor is made to accommodate them, and clothing not made to accommodate your wings might be destroyed when you manifest them."}
                           {:name "Draconic Presence"
                            :level 18
                            :description "Beginning at 18th level, you can channel the dread presence of your dragon ancestor, causing those around you to become awestruck or frightened. As an action, you can spend 5 sorcery points to draw on this power and exude an aura of awe or fear (your choice) to a distance of 60 feet. For 1 minute or until you lose your concentration (as if you were casting a concentration spell), each hostile creature that starts its turn in this aura must succeed on a Wisdom saving throw or be charmed (if you chose awe) or frightened (if you chose fear) until the aura ends. A creature that succeeds on this saving throw is immune to your aura for 24 hours."}]}
                 {:name "Wild Magic"
                  :traits [{:name "Wild Magic Surge"
                            :level 1}
                           {:name "Tides of Chaos"
                            :level 1}
                           {:name "Bend Luck"
                            :level 6}
                           {:name "Controlled Chaos"
                            :level 14}
                           {:name "Spell Bombardment"
                            :level 18}]}
                 {:name "Storm Sorcery"
                  :source "Sword Coast Adventurer's Guide"
                  :traits [{:name "Tempestuous Magic"}
                           {:name "Heart of the Storm"
                            :level 6}
                           {:name "Storm Guide"
                            :level 6}
                           {:name "Storm's Fury"
                            :level 14}
                           {:name "Wind Soul"
                            :level 18}]}]}
   character-ref))

(def pact-of-the-tome-name "Pact Boon: Pact of the Tome")
(def pact-of-the-chain-name "Pact Boon: Pact of the Chain")
(def pact-of-the-blade-name "Pact Boon: Pact of the Blade")

(def has-eldritch-blast-prereq
  (fn [c] (some #(= :eldritch-blast (:key %))
                   (get (es/entity-val c :spells-known) 0))))

(def pact-boon-options
  [(t/option
    "Pact of the Chain"
    :pact-of-the-chain
    []
    [(mod5e/trait pact-of-the-chain-name
                  "You learn the find familiar spell and can cast it as a ritual. The spell doesn’t count against your number of spells known.
When you cast the spell, you can choose one of the normal forms for your familiar or one of the following special forms: imp, pseudodragon, quasit, or sprite.
Additionally, when you take the Attack action, you can forgo one of your own attacks to allow your familiar to make one attack of its own with its reaction.")])
   (t/option
    "Pact of the Blade"
    :pact-of-the-blade
    []
    [(mod5e/trait pact-of-the-blade-name
                  "You can use your action to create a pact weapon in your empty hand. You can choose the form that this melee weapon takes each time you create it. You are proficient with it while you wield it. This weapon counts as magical for the purpose of overcoming resistance and immunity to nonmagical attacks and damage.
Your pact weapon disappears if it is more than 5 feet away from you for 1 minute or more. It also disappears if you use this feature again, if you dismiss the weapon (no action required), or if you die.
You can transform one magic weapon into your pact weapon by performing a special ritual while you hold the weapon. You perform the ritual over the course of 1 hour, which can be done during a short rest. You can then dismiss the weapon, hunting it into an extradimensional space, and it appears whenever you create your pact weapon thereafter. You can’t affect an artifact or a sentient weapon in this way. The weapon ceases being your pact weapon if you die, if you perform the 1-hour ritual on a different weapon, or if you use a 1-hour ritual to break your bond to it. The weapon appears at your feet if it is in the extradimensional space when the bond breaks.")])
   (t/option
    "Pact of the Tome"
    :pact-of-the-tome
    []
    [(mod5e/trait pact-of-the-tome-name
                  "Your patron gives you a grimoire called a Book of Shadows. When you gain this feature, choose three cantrips from any class’s spell list (the three needn’t be from the same list). While the book is on your person, you can cast those cantrips at will. They don’t count against your number of cantrips known. If they don’t appear on the warlock spell list, they are nonetheless warlock spells for you.
If you lose your Book of Shadows, you can perform a 1-hour ceremony to receive a replacement from your patron. This ceremony can be performed during a short or long rest, and it destroys the previous book. The book turns to ash when you die.")])])


(defn wizard-option [character-ref]
  (class-option
   {:name "Wizard",
    :spellcasting {:level-factor 1
                   :cantrips-known {1 3 4 1 10 1}
                   :known-mode :acquire
                   :spells-known (zipmap (range 1 21) (repeat 2))
                   :ability :int}
    :spellcaster true
    :hit-die 6
    :ability-increase-levels [4 8 12 16 19]
    :equipment-choices [{:name "Equipment Pack"
                         :options {:scholars-pack 1
                                   :explorers-pack 1}}
                        {:name "Spellcasting Equipment"
                         :options {:component-pouch 1
                                   :arcane-focus 1}}]
    :profs {:weapon {:dagger true :dart true :sling true :quarterstaff true :crossbow-light true}
            :save {:int true :wis true}
            :skill-options {:choose 2 :options {:arcana true :history true :insight true :investigation true :medicine true :religion true}}}
    :traits [{:level 18 :name "Spell Mastery" :description "You have achieved such mastery over certain spells that you can cast them at will. Choose a 1st-level wizard spell and a 2nd-level wizard spell that are in your spellbook. You can cast those spells at their lowest level without expending a spell slot when you have them prepared. If you want to cast either spell at a higher level, you must expend a spell slot as normal.\nBy spending 8 hours in study, you can exchange one or both of the spells you chose for different spells of the same levels."}
             {:level 20 :name "Signature Spells" :description "You gain mastery over two powerful spells and can cast them with little effort. Choose two 3rd-level wizard spells in your spellbook as your signature spells. You always have these spells prepared, they don't count against the number of spells you have prepared, and you can cast each of them once at 3rd level without expending a spell slot. When you do so, you can't do so again until you  nish a short or long rest.\nIf you want to cast either spell at a higher level, you must expend a spell slot as normal."}]
    :subclass-level 2
    :subclass-title "Arcane Tradition"
    :subclasses [{:name "School of Evocation"
                  :traits [{:level 2
                            :name "Evocation Savant"
                            :description "Beginning when you select this school at 2nd level, the gold and time you must spend to copy an evocation spell into your spellbook is halved."}
                           {:level 2
                            :name "Sculpt Spells"
                            :description "Beginning at 2nd level, you can create pockets of relative safety within the e ects of your evocation spells. When you cast an evocation spell that a ects other creatures that you can see, you can choose a number of them equal to 1 + the spell's level. The chosen creatures automatically succeed on their saving throws against the spell, and they take no damage if they would normally take half damage on a successful save."}
                           {:level 6
                            :name "Potent Cantrip"
                            :description "Starting at 6th level, your damaging cantrips affect even creatures that avoid the brunt of the effect. When a creature succeeds on a saving throw against your cantrip, the creature takes half the cantrip's damage (if any) but su ers no additional e ect from the cantrip."}
                           {:level 10
                            :name "Empowered Evocation"
                            :description "Beginning at 10th level, you can add your Intelligence modi er to one damage roll of any wizard evocation spell you cast."}
                           {:level 14
                            :name "Overchannel"
                            :description "Starting at 14th level, you can increase the power of your simpler spells. When you cast a wizard spell of 1st through 5th level that deals damage, you can deal maximum damage with that spell.\nThe first time you do so, you suffer no adverse effect. If you use this feature again before you  nish a long rest, you take 2d12 necrotic damage for each level of the spell, immediately after you cast it. Each time you use this feature again before finishing a long rest, the necrotic damage per spell level increases by 1d12. This damage ignores resistance and immunity."}]}
                 {:name "School of Abjuration"
                  :traits [{:name "Abjuration Savant"
                            :level 2}
                           {:name "Arcane Ward"
                            :level 2}
                           {:name "Projected Ward"
                            :level 6}
                           {:name "Improved Abjuration"
                            :level 10}
                           {:name "Spell Resistance"
                            :level 14}]}
                 {:name "School of Conjuration"
                  :traits [{:name "Conjuration Savant"
                            :level 2}
                           {:name "Minor Conjuration"
                            :level 2}
                           {:name "Benign Transposition"
                            :level 6}
                           {:name "Focused Conjuration"
                            :level 10}
                           {:name "Durable Summons"
                            :level 14}]}
                 {:name "School of Divination"
                  :traits [{:name "Divination Savant"
                            :level 2}
                           {:name "Protent"
                            :level 2}
                           {:name "Expert Divination"
                            :level 6}
                           {:name "The Third Eye"
                            :level 10}
                           {:name "Greater Portent"
                            :level 14}]}
                 {:name "School of Enchantment"
                  :traits [{:name "Enchantment Savant"
                            :level 2}
                           {:name "Hypnotic Gaze"
                            :level 2}
                           {:name "Instinctive Charm"
                            :level 6}
                           {:name "Split Enchantment"
                            :level 10}
                           {:name "Alter Memories"
                            :level 14}]}
                 {:name "School of Illusion"
                  :selections [(opt5e/spell-selection :wizard 0 :int 1 "Wizard")]
                  :traits [{:name "Illusion Savant"
                            :level 2}
                           {:name "Improved Minor Illusion"
                            :level 2}
                           {:name "Malleable Illusions"
                            :level 6}
                           {:name "Illusory Self"
                            :level 10}
                           {:name "Illusory Reality"
                            :level 14}]}
                 {:name "School of Necromancy"
                  :modifiers [(mod5e/resistance :necrotic)]
                  :traits [{:name "Necromancy Savant"
                            :level 2}
                           {:name "Grim Harvest"
                            :level 2}
                           {:name "Undead Thralls"
                            :level 6}
                           {:name "Inured to Undeath"
                            :level 10}
                           {:name "Command Undead"
                            :level 14}]}
                 {:name "School of Transmutation"
                  :modifiers [(mod5e/spells-known 4 :polymorph :int "Wizard")]
                  :traits [{:name "Transmutation Savant"
                            :level 2}
                           {:name "Minor Alchemy"
                            :level 2}
                           {:name "Transmuter's Stone"
                            :level 6}
                           {:name "Shapechanger"
                            :level 10}
                           {:name "Master Transmuter"
                            :level 14}]}
                 {:name "Bladesinger"
                  :source "Sword Coast Adventurer's Guide"
                  :traits [{:name "Training in War and Song"
                            :level 2}
                           {:name "Bladesong"
                            :level 2}
                           {:name "Extra Attack"
                            :level 6}
                           {:name "Song of Defense"
                            :level 10}
                           {:name "Song of Victory"
                            :level 14}]}]}
   character-ref))

(defn has-trait-with-name-prereq [name]
  (fn [c] (some #(= name (:name %)) (es/entity-val c :traits))))

(def eldritch-invocation-options
  [(t/option
    "Agonizing Blast"
    :agonizing-blast
    []
    [(mod5e/trait "Eldritch Invocation: Agonizing Blast")]
    [has-eldritch-blast-prereq])
   (t/option
    "Armor of Shadows"
    :armor-of-shadows
    []
    [(mod5e/trait "Eldritch Invocation: Armor of Shadows"
                  "You can cast mage armor on yourself at will, without expending a spell slot or material components.")])
   (t/option
    "Ascendant Step"
    :ascendant-step
    []
    [(mod5e/trait "Eldritch Invocation: Ascendant Step"
                  "You can cast levitate on yourself at will, without expending a spell slot or material components.")]
    [(total-levels-prereq 9)])
   (t/option
    "Beast Speech"
    :beast-speech
    []
    [(mod5e/trait "Eldritch Invocation: Beast Speech"
                  "You can cast speak with animals at will, without expending a spell slot")])
   (t/option
    "Beguiling Influence"
    :beguiling-influence
    []
    [(mod5e/skill-proficiency :deception)
     (mod5e/skill-proficiency :persuasion)])
   (t/option
    "Bewitching Whispers"
    :bewitching-whispers
    []
    [(mod5e/trait "Eldritch Invocation: Bewitching Whispers"
                  "You can cast compulsion once using a warlock spell slot. You can’t do so again until you finish a long rest.")])
   (t/option
    "Book of Ancient Secrets"
    :book-of-ancient-secrets
    []
    [(mod5e/trait "Eldritch Invocation: Book of Ancient Secrets"
                  "You can now inscribe magical rituals in your Book of Shadows. Choose two 1st-level spells that have the ritual tag from any class’s spell list (the two needn’t be from the same list). The spells appear in the book and don’t count against the number of spells you know. With your Book of Shadows in hand, you can cast the chosen spells as rituals. You can’t cast the spells except as rituals, unless you’ve learned them by some other means. You can also cast a warlock spell you know as a ritual if it has the ritual tag.
On your adventures, you can add other ritual spells to your Book of Shadows. When you find such a spell, you can add it to the book if the spell’s level is equal to or less than half your warlock level (rounded up) and if you can spare the time to transcribe the spell. For each level of the spell, the transcription process takes 2 hours and costs 50 gp for the rare inks needed to inscribe it.")]
    [(has-trait-with-name-prereq pact-of-the-tome-name)])
   (t/option
    "Chains of Carceri"
    :chains-of-carceri
    []
    [(mod5e/trait "Eldritch Invocation: Chains of Carceri"
                  "You can cast hold monster at will—targeting a celestial, fiend, or elemental—without expending a spell slot or material components. You must finish a ong rest before you can use this invocation on the same creature again.")]
    [(has-trait-with-name-prereq pact-of-the-chain-name)
     (total-levels-prereq 15)])
   (t/option
    "Devil's Sight"
    :devils-sight
    []
    [(mod5e/darkvision 120 1)
     (mod5e/trait "Eldritch Invocation: Devil's Sight"
                  "You can see normally in darkness, both magical and nonmagical, to a distance of 120 feet.")])
   (t/option
    "Dreadful Word"
    :dreadful-word
    []
    [(mod5e/trait "Eldritch Invocation: Dreadful Word"
                  "You can cast confusion once using a warlock spell slot. You can’t do so again until you finish a long rest")]
    [(total-levels-prereq 7)])
   (t/option
    "Eldritch Sight"
    :eldritch-sight
    []
    [(mod5e/trait "Eldritch Invocation: Eldritch Sight"
                  "You can cast detect magic at will, without expending a spell slot.")])
   (t/option
    "Eldritch Spear"
    :eldritch-spear
    []
    [(mod5e/trait "Eldritch Invocation: Eldritch Spear"
                  "When you cast eldritch blast, its range is 300 feet.")]
    [has-eldritch-blast-prereq])
   (t/option
    "Eyes of the Rune Keeper"
    :eyes-of-the-rune-keeper
    []
    [(mod5e/trait "Eldritch Invocation: Eyes of the Rune Keeper"
                  "You can read all writing.")])
   (t/option
    "Fiendish Vigor"
    :fiendish-vigor
    []
    [(mod5e/trait "Eldritch Invocation: Fiendish Vigor"
                  "You can cast false life on yourself at will as a 1st-level spell, without expending a spell slot or material components")])
   (t/option
    "Gaze of Two Minds"
    :gaze-of-two-minds
    []
    [(mod5e/trait "Eldritch Invocation: Gaze of Two Minds"
                  "You can use your action to touch a willing humanoid and perceive through its senses until the end of your next turn. As long as the creature is on the same plane of existence as you, you can use your action on subsequent turns to maintain this connection, extending the duration until the end of your next turn. While perceiving through the other creature’s senses, you benefit from any special senses possessed by that creature, and you are blinded and deafened to your own surroundings.")])
   (t/option
    "Lifedrinker"
    :lifedrinker
    []
    [(mod5e/trait "Eldritch Invocation: Lifedrinker"
                  "When you hit a creature with your pact weapon, the creature takes extra necrotic damage equal to your Charisma modifier (minimum 1).")]
    [(total-levels-prereq 12)
     (has-trait-with-name-prereq pact-of-the-blade-name)])
   (t/option
    "Mask of Many Faces"
    :mask-of-many-faces
    []
    [(mod5e/trait "Eldritch Invocation: Mask of Many Faces"
                  "You can cast disguise self at will, without expending a spell slot.")])
   (t/option
    "Master of Myriad Forms"
    :master-of-myriad-forms
    []
    [(mod5e/trait "Eldritch Invocation: Master of Myriad Forms"
                  "You can cast alter self at will, without expending a spell slot.")]
    [(total-levels-prereq 15)])
   (t/option
    "Minions of Chaos"
    :minions-of-chaos
    []
    [(mod5e/trait "Eldritch Invocation: Minions of Chaos"
                  "You can cast conjure elemental once using a warlock spell slot. You can’t do so again until you finish a 
long rest.")]
    [(total-levels-prereq 9)])
   (t/option
    "Mire the Mind"
    :mire-the-mind
    []
    [(mod5e/trait "Eldritch Invocation: Mire the Mind"
                  "You can cast slow once using a warlock spell slot. You can’t do so again until you finish a long rest.")]
    [(total-levels-prereq 5)])
   (t/option
    "Misty Visions"
    :misty-visions
    []
    [(mod5e/trait "Eldritch Invocation: Misty Visions"
                  "You can cast silent image at will, without expending a spell slot or material components.")])
   (t/option
    "One with Shadows"
    :one-with-shadows
    []
    [(mod5e/trait "Eldritch Invocation: One with Shadows"
                  "When you are in an area of dim light or darkness, you can use your action to become invisible until you move or take an action or a reaction")]
    [(total-levels-prereq 5)])
   (t/option
    "Otherworldly Leap"
    :otherworldly-leap
    []
    [(mod5e/trait "Eldritch Invocation: Otherworldly Leap"
                  "You can cast jump on yourself at will, without expending a spell slot or material components.")]
    [(total-levels-prereq 9)])
   (t/option
    "Repelling Blast"
    :repelling-blast
    []
    [(mod5e/trait "Eldritch Invocation: Repelling Blast"
                  "When you hit a creature with eldritch blast, you can push the creature up to 10 feet away from you in a straight line.")]
    [has-eldritch-blast-prereq])
   (t/option
    "Sculptor of Flesh"
    :sculptor-of-flesh
    []
    [(mod5e/trait "Eldritch Invocation: Sculptor of Flesh"
                  "You can cast polymorph once using a warlock spell slot. You can’t do so again until you finish a long rest.")]
    [(total-levels-prereq 7)])
   (t/option
    "Sign of Ill Omen"
    :sign-of-ill-omen
    []
    [(mod5e/trait "Eldritch Invocation: Sign of Ill Omen"
                  "You can cast bestow curse once using a warlock spell slot. You can’t do so again until you finish a long rest.")]
    [(total-levels-prereq 7)])
   (t/option
    "Thief of Five Fates"
    :thief-of-five-fates
    []
    [(mod5e/trait "Eldritch Invocation: Thief of Five Fates"
                  "You can cast bane once using a warlock spell slot. You can’t do so again until you finish a long rest.")]
    [(total-levels-prereq 7)])
   (t/option
    "Thirsting Blade"
    :thirsting-blade
    []
    [(mod5e/trait "Eldritch Invocation: Thirsting Blade"
                  "You can attack with your pact weapon twice, instead of once, whenever you take the Attack action on your turn.")]
    [(total-levels-prereq 5)
     (has-trait-with-name-prereq pact-of-the-blade-name)])
   (t/option
    "Visions of Distant Realms"
    :visions-of-distant-realms
    []
    [(mod5e/trait "Eldritch Invocation: Visions of Distant Realms"
                  "You can cast arcane eye at will, without expending a spell slot.")]
    [(total-levels-prereq 15)])
   (t/option
    "Voice of the Chain Master"
    :voice-of-the-chain-master
    []
    [(mod5e/trait "Eldritch Invocation: Voice of the Chain Master"
                  "You can communicate telepathically with your familiar and perceive through your familiar’s senses as long as you are on the same plane of existence.
Additionally, while perceiving through your familiar’s senses, you can also speak through your familiar in your own voice, even if your familiar is normally incapable of speech.")]
    [(has-trait-with-name-prereq pact-of-the-chain-name)])
   (t/option
    "Whispers of the Grave"
    :whispers-of-the-grave
    []
    [(mod5e/trait "Eldritch Invocation: Whispers of the Grave"
                  "You can cast speak with dead at will, without expending a spell slot")]
    [(total-levels-prereq 9)])
   (t/option
    "Witch Sight"
    :witch-sight
    []
    [(mod5e/trait "Eldritch Invocation: Witch Sight"
                  "You can see the true form of any shapechanger or creature concealed by illusion or transmutation magic while the creature is within 30 feet of you and within line of sight.")]
    [(total-levels-prereq 15)])])

(def warlock-spells-known
  {1 2
   2 1
   3 1
   4 1
   5 1
   6 1
   7 1
   8 1
   9 1
   11 1
   13 1
   15 1
   17 1
   19 1})

(defn eldritch-invocation-selection [& [num]]
  (t/selection-cfg
   {:name "Eldritch Invocations"
    :options eldritch-invocation-options
    :min (or num 0)
    :max (or num 0)
    :simple? true}))

(defn warlock-option [character-ref]
  (class-option
   {:name "Warlock"
    :spellcasting {:level-factor 1
                   :cantrips-known {1 2 4 1 10 1}
                   :spells-known warlock-spells-known
                   :known-mode :schedule
                   :ability :cha},
    :spellcaster true
    :hit-die 8
    :ability-increase-levels [4 8 12 16 19]
    :profs {:armor {:light false}
            :weapon {:simple false}
            :save {:wis true :cha true}
            :skill-options {:choose 2 :options {:arcana true :deception true :history true :intimidation true :investigation true :nature true :religion true}}}
    :selections [(t/selection
                  "Weapon"
                  [(t/option
                    "Light Crossbow"
                    :crossbow
                    []
                    [(mod5e/weapon :crossbow-light 1)
                     (mod5e/equipment :crossbow-bolt 20)])
                   (weapon-option [:simple 1])])
                 (simple-weapon-selection 1)]
    :equipment-choices [{:name "Equipment Pack"
                         :options {:scholars-pack 1
                                   :dungeoneers-pack 1}}
                        {:name "Spellcasting Equipment"
                         :options {:component-pouch 1
                                   :arcane-focus 1}}]
    :weapons {:dagger 2}
    :armor {:leather 1}
    :levels {2 {:selections [(eldritch-invocation-selection 2)]}
             3 {:modifiers [(mod5e/spells-known 1 :find-familiar :cha "Warlock")]
                :selections [(t/selection
                             "Pact Boon"
                             pact-boon-options)]}
             5 {:selections [(eldritch-invocation-selection)]}
             8 {:selections [(eldritch-invocation-selection)]}
             12 {:selections [(eldritch-invocation-selection)]}
             15 {:selections [(eldritch-invocation-selection)]}
             18 {:selections [(eldritch-invocation-selection)]}}
    :traits [{:name "Mystic Arcanum"
              :level 11
              :description "At 11th level, your patron bestows upon you a magical secret called an arcanum. Choose one 6th level spell from the warlock spell list as this arcanum.
You can cast your arcanum spell once without expending a spell slot. You must finish a long rest before you can do so again.
At higher levels, you gain more warlock spells of your choice that can be cast in this way: one 7th level spell at 13th level, one 8th-level spell at 15th level, and one 9th-level spell at 17th level. You regain all uses of your Mystic Arcanum when you finish a long rest."}
             {:name "Eldrich Master"
              :level 20
              :description "At 20th level, you can draw on your inner reserve of mystical power while entreating your patron to regain expended spell slots. You can spend 1 minute entreating your patron for aid to regain all your expended spell slots from your Pact Magic feature. Once you regain spell slots with this feature, you must finish a long rest before you can do so again."}]
    :subclass-level 1
    :subclass-title "Otherworldly Patron"
    :subclasses [{:name "The Fiend"
                  :spellcasting {:known-mode :schedule
                                 :spells-known warlock-spells-known
                                 :level-factor 1
                                 :spells {1 #{:burning-hands :command}
                                          2 #{:blindness-deafness :scorching-ray}
                                          3 #{:fireball :stinking-cloud}
                                          4 #{:fire-shield :wall-of-fire}
                                          5 #{:flame-strike :hallow}}}
                  :traits [{:name "Dark One's Blessing"
                            :description "Starting at 1st level, when you reduce a hostile creature to 0 hit points, you gain temporary hit points equal to your Charisma modifier + your warlock level (minimum of 1)."}
                           {:name "Dark One's Own Luck"
                            :level 6
                            :description "Starting at 6th level, you can call on your patron to alter fate in your favor. When you make an ability check or a saving throw, you can use this feature to Not for resale. Permission granted to print or photocopy this document for personal use only. System Reference Document 5.0 51 add a d10 to your roll. You can do so after seeing the initial roll but before any of the roll's effects occur.
Once you use this feature, you can't use it again until you finish a short or long rest."}
                           {:name "Fiendish Resilience"
                            :level 10
                            :description "Starting at 10th level, you can choose one damage type when you finish a short or long rest. You gain resistance to that damage type until you choose a different one with this feature. Damage from magical weapons or silver weapons ignores this resistance."}
                           {:name "Hurl Through Hell"
                            :level 14
                            :description "Starting at 14th level, when you hit a creature with an attack, you can use this feature to instantly transport the target through the lower planes. The creature disappears and hurtles through a nightmare landscape.
At the end of your next turn, the target returns to the space it previously occupied, or the nearest unoccupied space. If the target is not a fiend, it takes 10d10 psychic damage as it reels from its horrific experience.
Once you use this feature, you can't use it again until you finish a long rest."}]}
                 {:name "The Archfey"
                  :spellcasting {:known-mode :schedule
                                 :spells-known warlock-spells-known
                                 :level-factor 1
                                 :spells {1 #{:faerie-fire :sleep}
                                          2 #{:calm-emotions :phantasmal-force}
                                          3 #{:blink :plant-growth}
                                          4 #{:dominate-beast :greater-invisibility}
                                          5 #{:dominate-person :seeming}}}
                  :traits [{:name "Fey Presence"
                            :level 1}
                           {:name "Misty Escape"
                            :level 6}
                           {:name "Beguiling Defenses"
                            :level 10}
                           {:name "Dark Delerium"
                            :level 14}]}
                 {:name "The Great Old One"
                  :spellcasting {:known-mode :schedule
                                 :spells-known warlock-spells-known
                                 :level-factor 1
                                 :spells {1 #{:dissonant-whispers :tashas-hideous-laughter}
                                          2 #{:detect-thoughts :phantasmal-force}
                                          3 #{:clairvoyance :sending}
                                          4 #{:dominate-beast :evards-black-tentacles}
                                          5 #{:dominate-person :telekinesis}}}
                  :traits [{:name "Awakened Mind"
                            :level 1}
                           {:name "Entropic Ward"
                            :level 6}
                           {:name "Thought Shield"
                            :level 10}
                           {:name "Create Thrall"
                            :level 14}]}]}
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

(def backgrounds [{:name "Acolyte"
                   :help "Your life has been devoted to serving a god or gods."
                   :profs {:skill {:insight true, :religion true}
                           :language-options {:choose 2 :options {:any true}}}
                   :traits [{:name "Shelter the Faithful"
                             :summary "You and your companions can expect free healing at an establishment of your faith."
                             :description "As an acolyte, you command the respect of those who share your faith, and you can perform the religious ceremonies of your deity. You and your adventuring companions can expect to receive free healing and care at a temple, shrine, or other established presence of your faith, though you must provide any material components needed for spells. Those who share your religion will support you (but only you) at a modest lifestyle.
You might also have ties to a specific temple dedicated to your chosen deity or pantheon, and you have a residence there. This could be the temple where you used to serve, if you remain on good terms with it, or a temple where you have found a new home. While near your temple, you can call upon the priests for assistance, provided the assistance you ask for is not hazardous and you remain in good standing with your temple."}]
                   :personality ["I idolize a particular hero of my faith, and constantly refer to that person's deeds and example."
                                 "I can find common ground between the fiercest enemies, empathizing with them and always working toward peace."
                                 "I see omens in every event and action. The gods try to speak to us, we just need to listen"
                                 "Nothing can shake my optimistic attitude."
                                 "I quote (or misquote) sacred texts and proverbs in almost every situation."
                                 "I am tolerant (or intolerant) of other faiths and respect (or condemn) the worship of other gods."
                                 "I've enjoyed fine food, drink, and high society among my temple's elite. Rough living grates on me."
                                 "I've spent so long in the temple that I have little practical experience dealing with people in the outside world."]
                   :ideal ["Tradition. The ancient traditions of worship and sacrifice must be preserved and upheld. (Lawful)"
                           "Charity. I always try to help those in need, no matter what the personal cost. (Good)"
                           "Change. We must help bring about the changes the gods are constantly working in the world. (Chaotic)"
                           "Power. I hope to one day rise to the top of my faith's religious hierarchy. (Lawful)"
                           "Faith. I trust that my deity will guide my actions. I have faith that if I work hard, things will go well. (Lawful)"
                           "Aspiration. I seek to prove myself worthy of my god's favor by matching my actions against his or her teachings. (Any)"]
                   :bond ["I would die to recover an ancient relic of my faith that was lost long ago."
                          "I will someday get revenge on the corrupt temple hierarchy who branded me a heretic."
                          "I owe my life to the priest who took me in when my parents died."
                          "Everything I do is for the common people."
                          "I will do anything to protect the temple where I served."
                          "I seek to preserve a sacred text that my enemies consider heretical and seek to destroy."]
                   :flaw ["I judge others harshly, and myself even more severely."
                          "I put too much trust in those who wield power within my temple's hierarchy."
                          "My piety sometimes leads me to blindly trust those that profess faith in my god."
                          "I am inflexible in my thinking."
                          "I am suspicious of strangers and expect the worst of them."
                          "Once I pick a goal, I become obsessed with it to the detriment of everything else in my life."]},
                  {:name "Criminal"
                   :help "You have a history of criminal activity."
                   :profs {:skill {:deception true, :stealth true}
                           :tool {:thieves-tools true}
                           :tool-options {:gaming-set 1}}}
                  {:name "Folk Hero"
                   :help "You are regarded as a hero by the people of your home village."
                   :profs {:skill {:animal-handling true :survival true}
                           :tool {:land-vehicles true}
                           :tool-options {:artisans-tool 1}}}
                  {:name "Noble"
                   :help "You are of noble birth."
                   :profs {:skill {:history true :persuasion true}
                           :tool-options {:gaming-set 1}
                           :language-options {:choose 1 :options {:any true}}}}
                  {:name "Sage"
                   :help "You spent your life studying lore."
                   :profs {:skill {:arcana true :history true}
                           :language-options {:choose 2 :options {:any true}}}}
                  {:name "Soldier"
                   :help "You have spent your living by the sword."
                   :profs {:skill {:athletics true :intimidation true}
                           :tool {:land-vehicles true}
                           :tool-options {:gaming-set 1}}}
                  {:name "Charlatan"
                   :help "You have a history of being able to work people to your advantage."
                   :profs {:skill {:deception true :sleight-of-hand true}
                           :tool {:disguise-kit true :forgery-kit true}}}
                  {:name "Entertainer"
                   :help "You have a history of entertaining people."
                   :profs {:skill {:acrobatics true :performance true}
                           :tool {:disguise-kit true}
                           :tool-options {:musical-instrument 1}}}
                  {:name "Guild Artisan"
                   :help "You are an artisan and a member of a guild in a particular field."
                   :profs {:skill {:insight true :persuasion true}
                           :tool-options {:artisans-tool 1}}}
                  {:name "Hermit"
                   :help "You have lived a secluded life."
                   :profs {:skill {:medicine true :religion true}
                           :tool {:herbalism-kit true}}}
                  {:name "Outlander"
                   :help "You were raised in the wilds."
                   :profs {:skill {:athletics true :survival true}
                           :tool-options {:musical-instrument 1}}}
                  {:name "Sailor"
                   :help "You were a member of a crew for a seagoing vessel."
                   :profs {:skill {:athletics true :perception true}
                           :tool {:navigators-tools true :water-vehices true}}}
                  {:name "Urchin"
                   :help "You were a poor orphan living on the streets."
                   :profs {:skill {:sleight-of-hand true :stealth true}
                           :tool {:disguise-kit true :thieves-tools true}}}])

(defn background-option [{:keys [name
                                 help
                                 page
                                 profs
                                 selections
                                 modifiers
                                 weapon-choices
                                 weapons
                                 equipment
                                 equipment-choices
                                 armor
                                 armor-choices
                                 traits]
                          :as cls}
                         character-ref]
  (let [kw (common/name-to-kw name)
        {:keys [skill skill-options tool-options tool language-options]
         armor-profs :armor weapon-profs :weapon} profs
        {skill-num :choose options :options} skill-options
        {lang-num :choose lang-options :options} language-options
        lang-kws (if (:any lang-options) (map :key opt5e/languages) (keys lang-options))
        skill-kws (if (:any options) (map :key opt5e/skills) (keys options))]
    (t/option-cfg
     {:name name
      :key kw
      :help help
      :page page
      :selections (vec
                   (concat
                    selections
                    (if (seq tool-options) [(tool-prof-selection tool-options)])
                    (class-weapon-options weapon-choices)
                    (class-armor-options armor-choices)
                    (class-equipment-options equipment-choices)
                    (if (seq skill-kws) [(opt5e/skill-selection skill-kws skill-num)])
                    (if (seq lang-kws) [(opt5e/language-selection (map opt5e/language-map lang-kws) lang-num)])))
      :modifiers (vec
                  (concat
                   [(mod5e/background name)]
                   (traits-modifiers traits)
                   modifiers
                   (armor-prof-modifiers (keys armor-profs))
                   (weapon-prof-modifiers (keys weapon-profs))
                   (tool-prof-modifiers (keys tool))
                   (mapv
                    (fn [skill-kw]
                      (mod5e/skill-proficiency skill-kw))
                    (keys skill))
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
                    equipment)))})))

(defn volos-guide-to-monsters-selections [character-ref]
  [(t/selection
    "Race"
    [aasimar-option
     firbolg-option
     goliath-option
     lizardfolk-option
     tabaxi-option
     triton-option
     bugbear-option
     goblin-option
     hobgoblin-option
     kobold-option
     orc-option
     yuan-ti-option])])

(def sword-coast-adventurers-guide-backgrounds
  [{:name "City Watch"
    :source "Sword Coast Adventurer's Guide"
    :profs {:skill {:athletics true :insight true}}}
   {:name "Clan Crafter"
    :source "Sword Coast Adventurer's Guide"
    :profs {:skill {:history true :insight true}
            :tool-options {:artisans-tool true}}}
   {:name "Cloistered Scholar"
    :source "Sword Coast Adventurer's Guide"
    :profs {:skill {:history true}
            :skill-options {:choose 1 :options {:arcana true :nature true :religion true}}}}
   {:name "Courtier"
    :source "Sword Coast Adventurer's Guide"
    :profs {:skill {:insight true :persuasion true}}}
   {:name "Faction Agent"
    :source "Sword Coast Adventurer's Guide"
    :profs {:skill {:insight true}
            :skill-options {:choose 1 :options {:animal-handling true :arcana true :deception true :history true :insight true :intimidation true :investigation true :medicine true :nature true :perception true :performance true :persuasion true :religion true :survival true}}}}
   {:name "Far Traveler"
    :source "Sword Coast Adventurer's Guide"
    :profs {:skill {:perception true :insight true}
            :tool-options {:musical-instrument 1}}}
   {:name "Inheritor"
    :source "Sword Coast Adventurer's Guide"
    :profs {:skill {:survival true}
            :skill-options {:choose 1 :options {:arcana true :history true :religion true}}}}
   {:name "Knight of the Order"
    :source "Sword Coast Adventurer's Guide"
    :profs {:skill {:persuasion true}
            :skill-options {:choose 1 :options {:arcana true :history true :nature true :religion true}}
            :tool-options {:musical-instrument 1}}}
   {:name "Mercenary Veteran"
    :source "Sword Coast Adventurer's Guide"
    :profs {:skill {:athletics true :persuasion true}
            :tool {:land-vehicles true}
            :tool-options {:gaming-set 1}}}
   {:name "Urban Bounty Hunter"
    :source "Sword Coast Adventurer's Guide"
    :profs {:skill-options {:choose 2 :options {:deception true :insight true :persuasion true :stealth true}}
            :tool-options {:gaming-set 1 :musical-instrument 1 :thieves-tools 1}}}
   {:name "Uthgardt Tribe Member"
    :source "Sword Coast Adventurer's Guide"
    :profs {:skill {:athletics true :survival true}
            :tool-options {:musical-instrument 1 :artisans-tool 1}}}
   {:name "Waterdhavian Noble"
    :source "Sword Coast Adventurer's Guide"
    :profs {:skill {:history true :persuasion true}
            :tool-options {:musical-instrument 1 :gaming-set 1}}}])

(defn scag-classes [character-ref]
  [{:name "Barbarian"
    :plugin? true
    :subclass-level 3
    :subclass-title "Primal Path"
    :subclasses [{:name "Path of the Battlerager"
                  :source "Sword Coast Adventurer's Guide"
                  :traits [{:name "Battlerager Armor"
                            :level 3}
                           {:name "Reckless Abandon"
                            :level 6}
                           {:name "Battlerager Charge"
                            :level 10}
                           {:name "Spiked Retribution"
                            :level 14}]}]}])

(defn sword-coast-adventurers-guide-selections [character-ref]
  [(t/selection
    "Background"
    (mapv
     #(background-option % character-ref)
     sword-coast-adventurers-guide-backgrounds))
   (t/selection
    "Class"
    (mapv
     #(class-option % character-ref)
     (scag-classes character-ref)))])

(defn ability-item [name abbr desc]
  [:li.m-t-5 [:span.f-w-b.m-r-5 (str name " (" abbr ")")] desc])

(defn inventory-selection [item-type-name items modifier-fn]
  (t/selection-cfg
   {:name item-type-name
    :min 0
    :max nil
    :sequential? false
    :quantity? true
    :collapsible? true
    :new-item-fn (fn [selection selected-items]
                   {::entity/key (-> items first :key)})
    :options (mapv
              (fn [{:keys [name key]}]
                (t/option-cfg
                 {:name name
                  :key key
                  :modifiers [(modifier-fn key)]}))
              items)}))

(defn template-selections [character-ref]
  [(t/selection-cfg
    {:name "Base Ability Scores"
     :key :ability-scores
     :help [:div
            [:p "Ability scores are your major character traits and affect nearly all aspects of play. These scores range from 1 to 20 for player characters and DO NOT include racial or other bonuses."]
            [:ul.m-t-10.p-l-15.list-style-disc
             (ability-item "Strength" "STR" "measures your physical power")
             (ability-item "Dexterity" "DEX" "measures your agility and nimbleness")
             (ability-item "Constitution" "CON" "measures your physical health")
             (ability-item "Intelligence" "INT" "measures your memory and reasoning abilities")
             (ability-item "Wisdom" "WIS" "measures your connection to your environment, how observant you are.")
             (ability-item "Charisma" "CHA" "measures how well you interact with others.")]]
     :options [{::t/name "Manual Entry"
                ::t/key :manual-entry
                ::t/help "This option allows you to manually type in the value for each ability. Use this if you want to roll dice yourself or if you already have a character with known ability values."
                ::t/ui-fn #(abilities-entry character-ref)
                ::t/modifiers [(mod5e/deferred-abilities)]}
               {::t/name "Standard Roll"
                ::t/key :standard-roll
                ::t/help "This option rolls the dice for you. You can rearrange the values using the left and right arrow buttons."
                ::t/ui-fn #(abilities-roller character-ref (reroll-abilities character-ref))
                ::t/select-fn (reroll-abilities character-ref)
                ::t/modifiers [(mod5e/deferred-abilities)]}
               {::t/name "Standard Scores"
                ::t/key :standard-scores
                ::t/help "If you aren't feeling lucky, use this option, which gives you a standard set of scores. You can reassign the values using the left and right arrow buttons."
                ::t/ui-fn #(abilities-standard character-ref)
                ::t/select-fn (set-standard-abilities character-ref)
                ::t/modifiers [(mod5e/deferred-abilities)]}]})
   (t/selection-cfg
    {:name "Race"
     :help "Race determines your appearance and helps shape your culture and background. It also affects you ability scores, size, speed, languages, and many other crucial inherent traits."
     :options [dwarf-option
               elf-option
               halfling-option
               human-option
               dragonborn-option
               gnome-option
               half-elf-option
               half-orc-option
               tiefling-option]})
   (t/selection-cfg
    {:name "Background"
     :help "Background broadly describes your character origin. It also affords you two skill proficiencies and possibly proficiencies with tools or languages."
     :options (map
               background-option
               backgrounds)})
   (t/selection-cfg
    {:name "Class"
     :help [:div
            [:p "Class is your adventuring vocation. It determines many of your special talents, including weapon, armor, skill, saving throw, and tool proficiencies. It also provides starting equipment options. When you gain levels, you gain them in a particular class."]
            [:p.m-t-10 "Select your class using the selector at the top of the 'Class' section. Multiclassing is uncommon, but you may multiclass by clicking the 'Add Class' button at the end of the 'Class' section."]]
     :max nil
     :sequential? false
     :new-item-fn (fn [selection classes]
                    (let [current-classes (into #{}
                                                (map ::entity/key)
                                                (get-in @character-ref
                                                        [::entity/options :class]))]
                      {::entity/key (->> selection
                                         ::t/options
                                         (map ::t/key)
                                         (some #(if (-> % current-classes not) %)))
                       ::entity/options {:levels [{::entity/key :1}]}}))
     :options [(barbarian-option character-ref)
               (bard-option character-ref)
               (cleric-option character-ref)
               (druid-option character-ref)
               (fighter-option character-ref)
               (monk-option character-ref)
               (paladin-option character-ref)
               (ranger-option character-ref)
               (rogue-option character-ref)
               (sorcerer-option character-ref)
               (warlock-option character-ref)
               (wizard-option character-ref)]})
   (inventory-selection "Weapons" opt5e/weapons mod5e/deferred-weapon)
   (inventory-selection "Armor" opt5e/armor mod5e/deferred-armor)
   (inventory-selection "Equipment" opt5e/equipment mod5e/deferred-equipment)])


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
                           :medium (min ?max-medium-armor-bonus dex-bonus)
                           0)))
    ?armor-class-with-armor (fn [armor & [shield?]]
                              (+ (if shield? 2 0)
                                 (if (nil? armor)
                                   ?armor-class
                                   (+ (?armor-dex-bonus armor)
                                      (:base-ac armor)))))
    ?abilities (reduce
                (fn [m k]
                  (assoc m k (+ (or (k ?base-abilities) 12)
                                (or (k ?ability-increases) 0))))
                {}
                char5e/ability-keys)
    ?ability-bonuses (reduce-kv
                      (fn [m k v]
                        (assoc m k (int (/ (- v 10) 2))))
                      {}
                      ?abilities)
    ?save-bonuses (reduce-kv
                   (fn [m k v]
                     (assoc m k (+ v (if (?saving-throws k) ?prof-bonus 0))))
                   {}
                   ?ability-bonuses)
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
    ?critical #{20}
    ?spell-attack-modifier (fn [ability-kw]
                             (+ ?prof-bonus (?ability-bonuses ability-kw)))
    ?spell-save-dc (fn [ability-kw]
                     (+ 8 ?prof-bonus (?ability-bonuses ability-kw)))}))

(defn template [character-ref]
  {::t/base template-base
   ::t/selections (template-selections character-ref)})

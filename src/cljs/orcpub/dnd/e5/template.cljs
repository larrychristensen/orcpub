(ns orcpub.dnd.e5.template
  (:require [orcpub.entity :as entity]
            [orcpub.entity-spec :as es]
            [orcpub.template :as t]
            [orcpub.dice :as dice]
            [orcpub.modifiers :as mod]
            [orcpub.common :as common]
            [orcpub.dnd.e5.character :as char5e]
            [orcpub.dnd.e5.modifiers :as mod5e]
            [orcpub.dnd.e5.options :as opt5e]))

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
                    path)]
    (swap! character-ref #(assoc-in % value-path (dice/die-roll die)))))

(defn hit-points-roller [die character-ref path]
  [:div
   [:button.form-button
    {:style {:margin-top "10px"}
     :on-click #(roll-hit-points die character-ref path)}
    "Re-Roll"]])

(defn subrace-option [{:keys [name
                              abilities
                              size
                              speed
                              subrace-options
                              armor-proficiencies
                              weapon-proficiencies
                              modifiers
                              selections
                              traits]}]
  (let [option (t/option
   name
   (common/name-to-kw name)
   selections
   (vec
    (concat
     [(mod5e/subrace name)]
     modifiers
     (map
      (fn [armor-kw]
        (prn "ARMRO KW" armor-kw)
        (mod5e/armor-proficiency (clojure.core/name armor-kw) armor-kw))
      armor-proficiencies)
     (map
      (fn [weapon-nm]
        (mod5e/weapon-proficiency weapon-nm (common/name-to-kw weapon-nm)))
      weapon-proficiencies)
     (map
      (fn [[k v]]
        (mod5e/ability k v))
      abilities)
     (map
      (fn [{:keys [name description]}]
        (mod5e/trait name description))
      traits))))]
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
     (map
      (fn [{:keys [name description]}]
        (mod5e/trait name description))
      traits)
     (map
      (fn [armor-kw]
        (prn "ARMRO KW" armor-kw)
        (mod5e/armor-proficiency (clojure.core/name armor-kw) armor-kw))
      armor-proficiencies)
     (map
      (fn [weapon-nm]
        (mod5e/weapon-proficiency weapon-nm (common/name-to-kw weapon-nm)))
      weapon-proficiencies)))))

(def elf-weapon-training-mods
  [(mod5e/weapon-proficiency "longsword" :longsword)
   (mod5e/weapon-proficiency "shortword" :shortsword)
   (mod5e/weapon-proficiency "shortbow" :shortbow)
   (mod5e/weapon-proficiency "longbow" :longbow)])

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
    :weapon-proficiencies ["handaxe" "battleaxe" "light hammer" "warhammer"]
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

(defn class-option [{:keys [name
                            hit-die
                            profs
                            levels
                            ability-increase-levels]}
                    character-ref]
  (let [kw (common/name-to-kw name)
        {skill-num :choose options :options} (:skill-options profs)
        skill-kws (keys options)
        ability-inc-set (set ability-increase-levels)]
    (t/option
     name
     kw
     [(opt5e/skill-selection skill-kws skill-num)
      (t/sequential-selection
       "Levels"
       (fn [selection options current-values]
         {::entity/key (-> current-values count inc str keyword)})
       (vec
        (map
         (fn [i]
           (t/option
            (str i)
            (keyword (str i))
            (concat
             (some-> levels (get i) :selections)
             (if (ability-inc-set i)
               [(opt5e/ability-score-improvement-selection)])
             (if (> i 1)
               [(hit-points-selection character-ref hit-die)]))
            (concat
             (some-> levels (get i) :modifiers)
             (if (= i 1) [(mod5e/max-hit-points hit-die)])
             [(mod5e/level kw name i)])))
         (range 1 21))))]
     [])))

(defn barbarian-option [character-ref]
  (class-option
   {:name "Barbarian"
    :hit-die 12
    :ability-increase-levels [4 8 12 16 19]
    :profs {:armor {:light true :medium true :shields true}
            :weapon {:simple true :martial true}
            :save {:str true :con true}
            :skill-options {:choose 2 :options {:animal-handling true :athletics true :intimidation true :nature true :perception true :survival true}}}
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
     (t/option
      "Wizard"
      :wizard
      [(opt5e/skill-selection [:arcana :history :insight :investigation :medicine :religion] 2)
       (t/sequential-selection
        "Levels"
        (fn [selection options current-values]
          {::entity/key (-> current-values count inc str keyword)})
        [(t/option
          "1"
          :1
          [(opt5e/wizard-cantrip-selection 3)
           (opt5e/wizard-spell-selection-1)]
          [(mod5e/saving-throws :int :wis)
           (mod5e/level :wizard "Wizard" 1)
           (mod5e/max-hit-points 6)])
         (t/option
          "2"
          :2
          [(t/selection
            "Arcane Tradition"
            arcane-tradition-options)
           (hit-points-selection character-ref 6)]
          [(mod5e/level :wizard "Wizard" 2)])
         (t/option
          "3"
          :3
          [(opt5e/wizard-spell-selection-1)
           (hit-points-selection character-ref 6)]
          [(mod5e/level :wizard "Wizard" 3)])
         (t/option
          "4"
          :4
          [(opt5e/ability-score-improvement-selection)
           (hit-points-selection character-ref 6)]
          [(mod5e/level :wizard "Wizard" 4)])])]
      [])
     (t/option
      "Rogue"
      :rogue
      [(t/sequential-selection
        "Levels"
        (fn [selection levels]
          {::entity/key (-> levels count str keyword)})
        [(t/option
          "1"
          :1
          [(opt5e/expertise-selection)]
          [(mod5e/saving-throws :dex :int)
           (mod5e/level :rogue "Rogue" 1)
           (mod5e/max-hit-points 8)])
         (t/option
          "2"
          :2
          [(t/selection
            "Roguish Archetype"
            arcane-tradition-options)
           (t/selection
            "Hit Points"
            [(t/option
              "Average"
              :average
              []
              [(mod5e/max-hit-points 5)])])]
          [(mod5e/level :rogue "Rogue" 2)])
         (t/option
          "3"
          :3
          []
          [(mod5e/level :rogue "rogue" 3)])])]
      [])])])

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
    ?skill-prof-bonuses (reduce
                         (fn [m {k :key}]
                           (assoc m k (if (k ?skill-profs)
                                        (if (k ?skill-expertise)
                                          (* 2 ?prof-bonus)
                                          ?prof-bonus) 0)))
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
    ?initiative (?ability-bonuses :dex)}))

(defn template [character-ref]
  {::t/base template-base
   ::t/selections (template-selections character-ref)})

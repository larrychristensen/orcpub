(ns orcpub.dnd.e5.template
  (:require [clojure.string :as s]
            [clojure.set :as sets]
            [orcpub.entity :as entity]
            [orcpub.entity-spec :as es]
            [orcpub.template :as t]
            [orcpub.dice :as dice]
            [orcpub.modifiers :as mod]
            [orcpub.common :as common]
            [orcpub.dnd.e5.character :as char5e]
            [orcpub.dnd.e5.modifiers :as mod5e]
            [orcpub.dnd.e5.options :as opt5e]
            [orcpub.dnd.e5.units :as units5e]
            [orcpub.dnd.e5.weapons :as weapon5e]
            [orcpub.dnd.e5.equipment :as equip5e]
            [orcpub.dnd.e5.character.equipment :as char-equip5e]
            [orcpub.dnd.e5.armor :as armor5e]
            [orcpub.dnd.e5.spell-lists :as sl]
            [orcpub.dnd.e5.spells :as spells]
            [orcpub.dnd.e5.magic-items :as mi]
            [orcpub.dnd.e5.skills :as skill5e]
            [orcpub.dnd.e5.display :as disp5e]
            [orcpub.dnd.e5.template-base :as t-base]
            [orcpub.dnd.e5.templates.scag :as scag]
            [orcpub.dnd.e5.templates.ua-base :as ua]
            [re-frame.core :refer [subscribe dispatch]]))


(def character
  {::entity/options {:ability-scores {::entity/key :standard-scores
                                      ::entity/value (char5e/abilities 15 14 13 12 10 8)}
                     :class [{::entity/key :barbarian
                              ::entity/options {:levels [{::entity/key :level-1}]}}]
                     :optional-content
                     [{:orcpub.entity/key :vgm}
                      {:orcpub.entity/key :scag}
                      {:orcpub.entity/key :ee}
                      {:orcpub.entity/key :dmg}
                      {:orcpub.entity/key :cos}]}})

(defn set-ability! [app-state ability-key ability-value]
  (swap! app-state
         assoc-in
         [:character ::entity/options :ability-scores ::entity/value ability-key]
         ability-value))

(defn swap-abilities [app-state i other-i k v]
  (fn []
    (swap! app-state
           update-in
           [:character ::entity/options :ability-scores ::entity/value]
           (fn [a]
             (let [a-vec (vec a)
                   other-index (mod other-i (count a-vec))
                   [other-k other-v] (a-vec other-index)]
               (assoc a k other-v other-k v))))))

(def ability-icons
  {::char5e/str "strong"
   ::char5e/con "caduceus"
   ::char5e/dex "body-balance"
   ::char5e/int "read"
   ::char5e/wis "meditation"
   ::char5e/cha "aura"})

(defn ability-icon [k size theme]
  [:img {:class-name (str "h-" size " w-" size)
         :src (str (if (= "light-theme" theme) "/image/black/" "/image/") (ability-icons k) ".svg")}])

(defn ability-modifier [v]
  [:div.f-6-12.f-w-n.h-24
   [:div.t-a-c.f-s-10.opacity-5
    "mod"]
   [:div.m-t--1
    (opt5e/ability-bonus-str v)]])

(defn ability-component [k v i app-state controls]
  [:div.m-t-10.t-a-c
   (ability-icon k 24)
   [:div.uppercase (name k)]
   [:div.f-s-18.f-w-b v]
   (ability-modifier v)
   controls])

(defn abilities-standard [app-state]
  [:div.flex.justify-cont-s-b
    (let [abilities (or (opt5e/get-raw-abilities app-state) (char5e/abilities 15 14 13 12 10 8))
          abilities-vec (vec abilities)]
      (doall
       (map-indexed
        (fn [i [k v]]
          ^{:key k}
          [ability-component k v i app-state
           [:div.f-s-16
            [:i.fa.fa-chevron-circle-left.orange
             {:on-click (swap-abilities app-state i (dec i) k v)}]
            [:i.fa.fa-chevron-circle-right.orange.m-l-5
             {:on-click (swap-abilities app-state i (inc i) k v)}]]])
        abilities-vec)))])

(defn abilities-roller [app-state reroll-fn]
  [:div
   (abilities-standard app-state)
   [:button.form-button.m-t-5
    {:on-click (fn [e]
                 (reroll-fn)
                 (.stopPropagation e))}
    "Re-Roll"]])

(def score-costs
  {8 0
   9 1
   10 2
   11 3
   12 4
   13 5
   14 7
   15 9})

(def point-buy-points 27)


(defn point-buy-abilities [app-state]
  (let [abilities (or (opt5e/get-raw-abilities app-state)
                      (char5e/abilities 8 8 8 8 8 8))
        abilities-vec (vec (map (fn [[a v]] [a (-> v (min 15) (max 8))]) abilities))
        points-used (apply + (map (comp score-costs second) abilities-vec))
        points-remaining (- point-buy-points points-used)]
    [:div
     [:div.m-t-5
      [:span.f-w-n "Points Remaining: "]
      [:span.f-w-b points-remaining]]
     [:div.flex.justify-cont-s-b
      (doall
       (map-indexed
        (fn [i [k v]]
          (let [increase-disabled? (or (>= v 15) (<= points-remaining 0))
                decrease-disabled? (or (<= v 8) (>= points-remaining point-buy-points))]
            ^{:key k}
            [ability-component k v i app-state
             [:div.f-s-16
              [:i.fa.fa-minus-circle.orange
               {:class-name (if decrease-disabled? "opacity-5 cursor-disabled")
                :on-click (fn [_] (if (not decrease-disabled?) (set-ability! app-state k (dec v))))}]
              [:i.fa.fa-plus-circle.orange.m-l-5
               {:class-name (if increase-disabled? "opacity-5 cursor-disabled")
                :on-click (fn [_] (if (not increase-disabled?) (set-ability! app-state k (inc v))))}]]]))
        abilities-vec))]]))

(declare template-selections)

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

(def high-elf-cantrip-selection
  (opt5e/spell-selection
   {:class-key :wizard
    :level 0
    :exclude-ref? true
    :spellcasting-ability ::char5e/int
    :class-name "High Elf"
    :num 1}))

(def drow-magic-mods
  [(mod5e/spells-known 0 :dancing-lights ::char5e/cha "Dark Elf")
   (mod5e/spells-known 1 :faerie-fire ::char5e/cha "Dark Elf" 3)
   (mod5e/spells-known 2 :darkness ::char5e/cha "Dark Elf" 5)])

(def elf-option-cfg
  {:name "Elf"
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
     :selections [high-elf-cantrip-selection
                  (opt5e/language-selection opt5e/languages 1)]
     :modifiers [elf-weapon-training-mods]}
    {:name "Wood Elf"
     :abilities {::char5e/wis 1}
     :modifiers [(mod5e/speed 5)
                 mask-of-the-wild-mod
                 elf-weapon-training-mods]}
    {:name "Dark Elf (Drow)"
     :abilities {::char5e/cha 1}
     :traits [(sunlight-sensitivity 24)]
     :modifiers (conj drow-magic-mods (mod5e/darkvision 120))}]
   :traits [{:name "Fey Ancestry"
             :page 23
             :summary "advantage on charmed saves and immune to sleep magic"}
            {:name "Trance"
             :page 23
             :summary "Trance 4 hrs. instead of sleep 8"}]})

(def dwarf-option-cfg
  {:name "Dwarf",
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
              {:name "Mountain Dwarf"
               :abilities {::char5e/str 2}
               :armor-proficiencies [:light :medium]}]
   :modifiers [(mod5e/damage-resistance :poison)
               (mod5e/saving-throw-advantage [:poisoned])]})

(def halfling-option-cfg
  {:name "Halfling"
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
    {:name "Stout"
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

(def human-option-cfg
  {:name "Human"
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
                              :selections [(opt5e/feat-selection 1)
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
    {:name "Forest Gnome"
     :abilities {::char5e/dex 1}
     :modifiers [(mod5e/spells-known 0 :minor-illusion ::char5e/int "Forest Gnome")]
     :traits [{:name "Speak with Small Beasts"
               :page 37
               :summary "Communicate with Small or smaller beasts."}]}]})

(def half-elf-option-cfg
  {:name "Half-Elf"
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

(def aasimar-option-cfg
  {:name "Aasimar"
   :abilities {::char5e/cha 2}
   :size :medium
   :speed 30
   :darkvision 60
   :source :vgm
   :languages ["Common" "Celestial"]
   :modifiers [(mod5e/damage-resistance :necrotic)
               (mod5e/damage-resistance :radiant)
               (mod5e/spells-known 0 :light ::char5e/cha "Aasimar")]
   :traits [{:name "Healing Hands"
             :page 105
             :summary "Heal a creature a number of hit points equal to your level"}]
   :subraces [{:name "Protector Aasimar"
               :abilities {::char5e/wis 1}
               :modifiers [(mod5e/dependent-trait
                            {:name "Radiant Soul"
                             :level 3
                             :page 105
                             :source :vgm
                             :summary (str "For 1 minute, sprout wings (30 ft. flying speed) and deal "
                                           ?total-levels
                                           " extra radiant damage.") })]}
              {:name "Scourge Aasimar"
               :abilities {::char5e/con 1}
               :modifiers [(mod5e/dependent-trait
                            {:name "Radiant Consumption"
                             :level 3
                             :page 105
                             :source :vgm
                             :summary (let [level ?total-levels]
                                        (str "For 1 minute, deal "
                                             (common/round-up (/ level 2))
                                             " radiant damage to each creature within 10 ft. and deal an additional " level " radiant damage to one target you deal damage to with a spell or attack"))})]}
              {:name "Fallen Aasimar"
               :abilities {::char5e/str 1}
               :modifiers [(mod5e/dependent-trait
                            {:name "Necrotic Shroud"
                             :level 3
                             :page 105
                             :source :vgm
                             :summary (str "For 1 minute, creatures within 10 ft. must succeed on a DC " (?spell-save-dc ::char5e/cha) " cha save or be frightened of you. During that time also deal an additional " ?total-levels " necrotic damage to one target you deal damage to with a spell or attack.")})]}]})

(defn powerful-build [page]
  {:name "Powerful Build"
   :page page
   :source :vgm
   :summary "Count as Large for purposes of determining weight you can carry, push, drag, or lift."})

(def firbolg-option-cfg
  {:name "Firbolg"
   :abilities {::char5e/wis 2 ::char5e/str 1}
   :size :medium
   :speed 30
   :source :vgm
   :languages ["Common" "Elvish" "Giant"]
   :modifiers [(mod5e/spells-known 1 :detect-magic ::char5e/wis "Firbolg")
               (mod5e/spells-known 1 :disguise-self ::char5e/wis "Firbolg" 1 "only to seem 3 ft. shorter")
               (mod5e/bonus-action
                {:name "Hidden Step"
                 :duration units5e/rounds-1
                 :frequency units5e/rests-1
                 :page 107
                 :source :vgm
                 :summary "Turn invisible"})]
   :traits [(powerful-build 107)
            {:name "Speech of Beast and Leaf"
             :page 107
             :source :vgm
             :summary "Beast and plants can understand you and you have advantage on Charisma checks to influence them."}]})

(defn goliath-option-cfg [source page]
  {:name (str "Goliath (" (s/upper-case (name source)) ")")
   :key (if (= :vgm source) :goliath :goliath-ee)
   :abilities {::char5e/str 2 ::char5e/con 1}
   :size :medium
   :speed 30
   :languages ["Common" "Giant"]
   :source source
   :modifiers [(mod5e/skill-proficiency :athletics)
               (mod5e/reaction
                {:name "Stone's Endurance"
                 :frequency units5e/rests-1
                 :page page
                 :source source
                 :summary (str "Reduce damage taken by 1d12 + " (::char5e/con ?ability-bonuses))})]
   
   :traits [{:name "Mountain Born"
             :page page
             :summary "Adapted to high altitude and cold climates."}
            (powerful-build page)]})

(def kenku-option-cfg
  {:name "Kenku"
   :abilities {::char5e/dex 2 ::char5e/wis 1}
   :size :medium
   :speed 30
   :source :vgm
   :languages ["Common" "Auran"]
   :modifiers [(mod5e/dependent-trait
                {:name "Mimicry"
                 :page 111
                 :source :vgm
                 :summary (str "Mimic sounds you've heard, creatures disbelieve it with an insight check opposed to your deception check (1d20" (common/mod-str (:deception ?skill-bonuses)) ").")})]
   :selections [(opt5e/skill-selection [:acrobatics :deception :stealth :sleight-of-hand] 2)]
   :profs {:skill-options {:choose 2 :options {:acrobatics true :deception true :stealth true :sleight-of-hand true}}}
   :traits [{:name "Expert Forgery"
             :page 111
             :summary "Advantage on checks to duplicate existing objects"}]})

(def lizardfolk-option-cfg
  {:name "Lizardfolk"
   :abilities {::char5e/con 2 ::char5e/wis 1}
   :size :medium
   :speed 30
   :source :vgm
   :languages ["Common" "Draconic"]
   :selections [(opt5e/skill-selection [:animal-handling :nature :perception :stealth :survival] 2)]
   :modifiers [(mod5e/swimming-speed 30)
               (mod/modifier ?natural-ac-bonus 3)
               (mod/modifier ?armor-class-with-armor
                             (fn [armor & [shield]]
                               (max (+ ?base-armor-class
                                       (if shield (?shield-ac-bonus shield) 0))
                                    (?armor-class-with-armor armor shield))))
               (mod5e/bonus-action
                {:name "Hungry Jaws"
                 :page 113
                 :source :vgm
                 :frequency units5e/rests-1
                 :summary (str "Special attack with your bite. If you hit, you gain "
                               (max 1 (::char5e/con ?ability-bonuses))
                               " temp. hit points")})
               (mod5e/attack
                {:name "Bite"
                 :page 113
                 :source :vgm
                 :attack-type :melee
                 :damage-type :piercing
                 :damage-die 6
                 :damage-die-count 1
                 :damage-modifier (::char5e/str ?ability-bonuses)})]
   :profs {:skill-options {:choose 2 :options {:animal-handling true :nature true :stealth true :perception true :survival true}}}
   :traits [{:name "Cunning Artisan"
             :page 113
             :summary "Craft certain weapons and armor from creature remains."}
            {:name "Hold Breath"
             :page 113
             :summary "Up to 15 min."}]})

(def tabaxi-option-cfg
  {:name "Tabaxi"
   :abilities {::char5e/dex 2 ::char5e/cha 1}
   :size :medium
   :speed 30
   :darkvision 60
   :source :vgm
   :languages ["Common"]
   :modifiers [(mod5e/climbing-speed 20)
               (mod5e/skill-proficiency :perception)
               (mod5e/skill-proficiency :stealth)
               (mod5e/attack
                {:name "Cat's Claws"
                 :page 115
                 :source :vgm
                 :attack-type :melee
                 :damage-type :slashing
                 :damage-die 4
                 :damage-die-count 1
                 :damage-modifier (::char5e/str ?ability-bonuses)})]
   :language-options {:choose 1 :options {:any true}}
   :traits [{:name "Feline Agility"
             :page 115
             :summary "Double speed when moving on your turn in combat."}]})

(def triton-option-cfg
  {:name "Triton"
   :abilities {::char5e/str 1 ::char5e/con 1 ::char5e/cha 1}
   :size :medium
   :speed 30
   :source :vgm
   :languages ["Common" "Primordial"]
   :modifiers [(mod5e/swimming-speed 30)
               (mod5e/spells-known 1 :fog-cloud ::char5e/cha "Triton")
               (mod5e/spells-known 2 :gust-of-wind ::char5e/cha "Triton" 3)
               (mod5e/spells-known 3 :wall-of-water ::char5e/cha "Triton" 5)
               (mod5e/damage-resistance :cold)]
   :traits [{:name "Amphibious"
             :page 118
             :summary "Breath water and air"}
            {:name "Emissary of the Sea"
             :page 118
             :summary "Water-breathing beasts can understand your words."}
            {:name "Guardians of the Depths"
             :page 118
             :summary "No negative effects from deep, underwater environment."}]})

(def bugbear-option-cfg
  {:name "Bugbear"
   :abilities {::char5e/str 2 ::char5e/dex 1}
   :size "Medium"
   :speed 30
   :darkvision 60
   :source :vgm
   :modifiers [(mod5e/skill-proficiency :stealth)]
   :languages ["Common" "Goblin"]
   :traits [{:name "Long Limbed"
             :page 119
             :summary "5 ft. additional reach to melee attacks on your turn"}
            (powerful-build 119)
            {:name "Surprise Attack"
             :page 119
             :summary "Extra 2d6 damage when hitting a surprised creature."}]})

(def goblin-option-cfg
  {:name "Goblin"
   :abilities {::char5e/dex 2 ::char5e/con 1}
   :size "Medium"
   :speed 30
   :darkvision 60
   :source :vgm
   :languages ["Common" "Goblin"]
   :modifiers [(mod5e/dependent-trait
                {:name "Fury of the Small"
                 :page 119
                 :summary (str "Extra " ?total-levels " damage to a larger creature (use once per long or short rest)")})
               (mod5e/bonus-action
                {:name "Nimble Escape"
                 :page 119
                 :summary "Disengage or Hide action as a bonus action."})]})

(def hobgoblin-option-cfg
  {:name "Hobgoblin"
   :abilities {::char5e/con 2 ::char5e/int 1}
   :size "Medium"
   :speed 30
   :darkvision 60
   :source :vgm
   :languages ["Common" "Goblin"]
   :selections [(t/selection-cfg
                 {:name "Martial Weapon Proficiencies"
                  :tags #{:weapon-profs :profs}
                  :options (opt5e/weapon-proficiency-options (weapon5e/martial-weapons weapon5e/weapons))
                  :min 2
                  :max 2})]
   :modifiers [(mod5e/light-armor-proficiency)]
   :traits [{:name "Saving Face"
             :page 119
             :summary "Add a bonus to a missed roll for each ally you can see, up to 5."}]})

(def kobold-option-cfg
  {:name "Kobold"
   :abilities {::char5e/dex 2 ::char5e/str -2}
   :size "Medium"
   :speed 30
   :darkvision 60
   :source :vgm
   :languages ["Common" "Draconic"]
   :traits [{:name "Grovel, Cower, and Beg"
             :page 119
             :summary "Cower to give allies advantage on attacks against enemies within 10 ft."}
            {:name "Pack Tactics"
             :page 119
             :summary "Advantage on attacks if an ally is within 5 ft. of the target."}
            (sunlight-sensitivity 119)]})

(def orc-option-cfg
  {:name "Orc"
   :abilities {::char5e/str 2 ::char5e/con 1 ::char5e/int -2}
   :size "Medium"
   :speed 30
   :darkvision 60
   :source :vgm
   :modifiers [(mod5e/skill-proficiency :intimidation)
               (mod5e/bonus-action
                {:name "Aggressive"
                 :page 120
                 :summary (str "Move up to " ?speed " feet toward and enemy you can see or hear")})]
   :languages ["Common" "Orc"]
   :traits [(powerful-build 120)]})

(def yuan-ti-option-cfg
  {:name "Yuan-Ti Pureblood"
   :abilities {::char5e/cha 2 ::char5e/int 1}
   :size "Medium"
   :speed 30
   :darkvision 60
   :source :vgm
   :modifiers [(mod5e/spells-known 0 :poison-spray ::char5e/cha "Yuan-Ti")
               (mod5e/spells-known 1 :animal-friendship ::char5e/cha "Yuan-Ti" 1 "unlimited uses, can only target snakes")
               (mod5e/spells-known 2 :suggestion ::char5e/cha "Yuan-Ti" 3 "one use per long rest")
               (mod5e/damage-immunity :poison)
               (mod5e/condition-immunity :poisoned)
               (mod5e/saving-throw-advantage [:magic])]
   :languages ["Common" "Abyssal" "Draconic"]})

(def tiefling-option-cfg
  {:name "Tiefling"
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

(defn al-illegal-flying-mod [name]
  (mod5e/al-illegal (str name " gains flying speed a 1st level, which is not legal")))

(def aarakocra-option-cfg
  {:name "Aarakocra"
   :abilities {::char5e/dex 2 ::char5e/wis 1}
   :size :medium
   :speed 25
   :languages ["Common" "Aarakocra" "Auran"]
   :source :ee
   :modifiers [(mod5e/flying-speed-override 50)
               (al-illegal-flying-mod "Aarakocra")
               (mod5e/attack
                {:name "Talons"
                 :source :ee
                 :page 3
                 :damage-die 4
                 :damage-die-count 1
                 :damage-type :slashing
                 :damage-modifier (?ability-bonuses ::char5e/str)})]})

(def ee-gnome-option-cfg
  (opt5e/deep-gnome-option-cfg :deep-gnome-ee :ee 7))

(def genasi-option-cfg
  {:name "Genasi"
   :source :ee
   :abilities {::char5e/con 2}
   :size "Medium"
   :speed 30
   :languages ["Common" "Primordial"]
   :subraces [{:name "Air Genasi"
               :abilities {::char5e/dex 1}
               :modifiers [(mod5e/spells-known 2 :levitate ::char5e/con "Air Genasi" nil "once/long rest")]
               :traits [{:name "Unending Breath"
                         :page 9
                         :source :ee
                         :summary "hold breath indefinitely"}
                        {:name "Mingle with the Wind"
                         :page 9
                         :source :ee
                         :frequency units5e/long-rests-1
                         :summary "cast levitate without material components"}]}
              {:name "Earth Genasi"
               :abilities {::char5e/str 1}
               :modifiers [(mod5e/spells-known 2 :pass-without-trace ::char5e/con "Earth Genasi" nil "once/long rest")]
               :traits [{:name "Earth Walk"
                         :page 9
                         :source :ee
                         :summary "walk on difficult earth or stone terrain without extra movement"}
                        {:name "Merge with Stone"
                         :page 9
                         :source :ee
                         :frequency units5e/long-rests-1
                         :summary "cast pass without trace without material components"}]}
              {:name "Fire Genasi"
               :abilities {::char5e/int 1}
               :modifiers [(mod5e/darkvision 60)
                           (mod5e/damage-resistance :fire)
                           (mod5e/spells-known 0 :produce-flame ::char5e/con "Fire Genasi")
                           (mod5e/spells-known 1 :burning-hands ::char5e/con "Fire Genasi" 3 "once/long rest")]
               :traits [{:name "Fire Resistance"
                         :page 10
                         :source :ee
                         :summary "resistance to fire damage"}
                        {:name "Reach to the Blaze"
                         :page 10
                         :source :ee
                         :frequency units5e/long-rests-1
                         :summary "have 'produce flame' cantrip; at level 3 can cast burning hands"}]}
              {:name "Water Genasi"
               :abilities {::char5e/wis 1}
               :modifiers [(mod5e/damage-resistance :acid)
                           (mod5e/swimming-speed 30)
                           (mod5e/spells-known 0 :shape-water ::char5e/con "Water Genasi")
                           (mod5e/spells-known 1 :create-or-destroy-water ::char5e/con "Water Genasi" 3 "once/long rest")]
               :traits [{:name "Acid Resistance"
                         :page 10
                         :source :ee
                         :summary "resistant to acid damage"}
                        {:name "Amphibious"
                         :page 10
                         :source :ee
                         :summary "breathe in air and water"}
                        {:name "Swim"
                         :page 10
                         :source :ee
                         :summary "swimming speed of 30 ft."}
                        {:name "Call to the Wave"
                         :page 10
                         :source :ee
                         :frequency units5e/long-rests-1
                         :summary "have 'shape water' cantrip; at level 3 can cast create or destroy water as 2nd level"}]}]})

(defn class-level [levels class-kw]
  (get-in levels [class-kw :class-level]))

(defn extra-attack-trait [page]
  (mod5e/trait-cfg
   {:name "Extra Attack"
    :page page
    :summary "Attack twice when taking Attack action"}))

(def barbarian-option
  (opt5e/class-option
   {:name "Barbarian"
    :hit-die 12
    :ability-increase-levels [4 8 12 16 19]
    :profs {:armor {:light true :medium true :shields false}
            :weapon {:simple false :martial false}
            :save {::char5e/str true ::char5e/con true}
            :skill-options {:choose 2 :options {:animal-handling true :athletics true :intimidation true :nature true :perception true :survival true}}}
    :multiclass-prereqs [(opt5e/ability-prereq ::char5e/str 13)]
    :weapon-choices [{:name "Martial Weapon"
                      :options {:greataxe 1
                                :martial 1}}
                     {:name "Simple Weapon"
                      :options {:handaxe 2
                                :simple 1}}]
    :weapons {:javelin 4}
    :equipment {:explorers-pack 1}
    :modifiers [(mod/vec-mod ?unarmored-defense :barbarian)
                (mod/cum-sum-mod ?unarmored-ac-bonus (?ability-bonuses ::char5e/con)
                                 nil
                                 nil
                                 [(= :barbarian (first ?unarmored-defense))])
                (mod/cum-sum-mod ?unarmored-with-shield-ac-bonus (?ability-bonuses ::char5e/con)
                                 nil
                                 nil
                                 [(= :barbarian (first ?unarmored-defense))])
                (mod5e/bonus-action
                 {:name "Rage"
                  :page 48
                  :duration units5e/minutes-1
                  :frequency (units5e/rests (condp <= (?class-level :barbarian)
                                            17 6
                                            12 5
                                            6 4
                                            3 3
                                            2))
                  :summary (str "Advantage on Strength checks and saves; melee damage bonus "
                                (common/bonus-str (condp <= (?class-level :barbarian)
                                                    16 4
                                                    9 3
                                                    2))
                                "; resistance to bludgeoning, piercing, and slashing damage")})]
    :levels {5 {:modifiers [(extra-attack-trait 49)
                            (mod5e/num-attacks 2)
                            (mod/modifier ?speed-with-armor (fn [armor] (if (not= :heavy (:type armor))
                                                                            (+ 10 ?speed)
                                                                            ?speed)))
                            (mod5e/dependent-trait
                             {:name "Fast Movement"
                              :page 49
                              :summary (str "Your speed increases to " (+ 10 ?speed) " when not heavily armored")})]}
             9 {:modifiers [(mod5e/dependent-trait
                             {:name "Brutal Critical"
                              :page 49
                              :summary (let [die-count (condp <= (?class-level :barbarian)
                                                         17 "three"
                                                         13 "two"
                                                         "one")]
                                         (str die-count
                                              " additional damage "
                                              (if (= "one" die-count)
                                                "die"
                                                "dice")
                                              " for melee criticals"))})]}
             18 {:modifiers [(mod5e/dependent-trait
                              {:name "Indomitable Might"
                               :level 18
                               :page 49
                               :summary (let [str-score (::char5e/str ?abilities)]
                                          (str "Min strength check value is " str-score))})]}
             20 {:modifiers [(mod5e/ability ::char5e/str 4)
                            (mod5e/ability ::char5e/con 4)]}}
    :traits [{:name "Reckless Attack"
              :level 2
              :page 48
              :summary "Advantage on attacks using Strength, attacks against you have advantage as well."}
             {:name "Danger Sense"
              :level 2
              :page 48
              :summary "Advantage on DEX saves against effects you can see."}
             {:name "Feral Instinct"
              :level 7
              :page 49
              :summary "Advantage on initiative, surprise doesn't keep you from attacking if you enter rage"}
             {:name "Relentless Rage"
              :level 11
              :page 49
              :summary "If raging, are reduced to 0 HP, aren't killed, and make a DC 10 save (+5 for each time you've used this feature between rests), you go to 1 HP instead."}
             {:name "Persistent Rage"
              :level 15
              :page 49
              :summary "rage only ends early if you choose to end it or you fall unconscious"}]
    :subclass-level 3
    :subclass-title "Primal Path"
    :subclass-help "Your primal path shapes the nature of your barbarian rage and gives you additional features."
    :subclasses [{:name "Path of the Beserker"
                  :levels {10 {:modifiers [(mod5e/action
                                            {:name "Intimidating Presence"
                                             :level 10
                                             :page 49
                                             :summary (str "Frighten (Wisdom save DC " (?spell-save-dc ::char5e/cha) ") a creature with 30 ft.")})]}
                           14 {:modifiers [(mod5e/reaction
                                           {:name "Retaliation"
                                            :page 49
                                            :level 14
                                            :summary "Make a melee weapon attack against a creature within 5 ft. that deals damage to you."})]}}
                  :traits [{:name "Frenzy"
                            :level 3
                            :page 49
                            :summary "You can frenzy when you rage, affording you a single melee weapon attack as a bonus action on each turn until the rage ends. When the rage ends, you suffer 1 level of exhaustrion"}
                           {:name "Mindless Rage"
                            :level 6
                            :page 49
                            :summary "Can't be charmed or frightened while raging."}]}
                 {:name "Path of the Totem Warrior"
                  :levels {3 {:modifiers [(mod5e/spells-known 2 :beast-sense nil "Barbarian" 1 "ritual only")
                                          (mod5e/spells-known 1 :speak-with-animals nil "Barbarian" 1 "ritual only")]
                              :selections [(t/selection-cfg
                                            {:name "Totem Spirit"
                                             :tags #{:class}
                                             :order 2
                                             :options [(t/option-cfg
                                               {:name "Bear"
                                                :modifiers [(mod5e/trait-cfg
                                                             {:name "Totem Spirit: Bear"
                                                              :page 50
                                                              :summary "While raging, you have resistance to all damage but psychic damage"})]})
                                              (t/option-cfg
                                               {:name "Eagle"
                                                :modifiers [(mod5e/trait-cfg
                                                             {:name "Totem Spirit: Eagle"
                                                              :page 50
                                                              :summary "While raging and not wearing heavy armor, opportunity attacks against you have disadvantage, and you can Dash as a bonus action."})]})
                                              (t/option-cfg
                                               {:name "Wolf"
                                                :modifiers [(mod5e/trait-cfg
                                                             {:name "Totem Spirit: Wolf"
                                                              :page 50
                                                              :summary "While raging, allies have advantage against enemies within 5 ft."})]})]})]}
                           6 {:selections [(t/selection-cfg
                                            {:name "Aspect of the Beast"
                                             :tags #{:class}
                                             :order 3
                                             :options [(t/option-cfg
                                               {:name "Bear"
                                                :modifiers [(mod5e/trait-cfg
                                                             {:name "Aspect of the Beast: Bear"
                                                              :page 50
                                                              :summary "2X carrying capacity, advantage lift, push, pull, or break Strength checks."})]})
                                              (t/option-cfg
                                               {:name "Eagle"
                                                :modifiers [(mod5e/trait-cfg
                                                             {:name "Aspect of the Beast: Eagle"
                                                              :page 50
                                                              :summary "See clearly up to a mile as if no more than 100 ft., no disadvantage on perception checks in dim light."})]})
                                              (t/option-cfg
                                               {:name "Wolf"
                                                :modifiers [(mod5e/trait-cfg
                                                             {:name "Aspect of the Beast: Wolf"
                                                              :page 50
                                                              :summary "Track at fast pace, stealthy at normal pace"})]})]})]}
                           10 {:modifiers [(mod5e/spells-known 5 :commune-with-nature nil "Barbarian" 1 "ritual only")]}
                           14 {:selections [(t/selection-cfg
                                             {:name "Totemic Attunement"
                                              :tags #{:class}
                                              :order 4
                                              :options [(t/option-cfg
                                                        {:name "Bear"
                                                         :modifiers [(mod5e/trait-cfg
                                                                      {:name "Totemic Attunement: Bear"
                                                                       :page 50
                                                                       :summary "While raging, hostile creatures within 5 ft. have disadvantage on attack rolls against anyone but you."})]})
                                                       (t/option-cfg
                                                        {:name "Eagle"
                                                         :modifiers [(mod5e/trait-cfg
                                                                      {:name "Totemic Attunement: Eagle"
                                                                       :page 50
                                                                       :summary "While raging, you gain flying speed equal to your walking speed, falling if you end your turn in the air."})]})
                                                       (t/option-cfg
                                                        {:name "Wolf"
                                                         :modifiers [(mod5e/trait-cfg
                                                                      {:name "Totemic Attunement: Wolf"
                                                                       :page 50
                                                                       :summary "While raging, if you hit a Large or smaller creature, you can use a bonus action to knock it prone."})]})]})]}}}]}))

(defn bardic-inspiration-die [levels]
  (condp <= (class-level levels :bard)
    15 12
    10 10
    5 8
    6))

(def musical-instrument-choice-cfg
  {:name "Musical Instrument"
   :options (zipmap (map :key equip5e/musical-instruments) (repeat 1))})

(def bard-option
  (opt5e/class-option
   {:name "Bard"
    :hit-die 8
    :ability-increase-levels [4 8 12 16 19]
    :multiclass-prereqs [(opt5e/ability-prereq ::char5e/cha 13)]
    :profs {:armor {:light false}
            :weapon {:simple true :crossbow-hand true :longsword true :rapier true :shortsword true}
            :save {::char5e/dex true ::char5e/cha true}
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
                        musical-instrument-choice-cfg]
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
                                  11 1
                                  13 1
                                  15 1
                                  17 1}
                   :known-mode :schedule
                   :ability ::char5e/cha}
    :modifiers [(mod5e/bonus-action
                 {:name "Bardic Inspiration"
                  :page 53
                  :summary (str "Inspire another creature with a 1d"
                                (bardic-inspiration-die ?levels)
                                " that it can, within the next 10 min., add to a d20 roll")})]
    :levels {2 {:modifiers [(mod/vec-mod ?default-skill-bonus-fns
                                         (fn [_]
                                            (int (/ ?prof-bonus 2))))
                            (mod/cum-sum-mod ?initiative (int (/ ?prof-bonus 2)))
                            (mod5e/dependent-trait
                             {:name "Jack of All Trades"
                              :page 54
                              :summary (str (common/bonus-str (int (/ ?prof-bonus 2))) " to ability checks that don't already include your proficiency bonus")})
                            (mod5e/dependent-trait
                             {:name "Song of Rest"
                              :page 54
                              :level 2
                              :summary (str "With a song, you and friendly creatures gain 1d"
                                            (mod5e/level-val
                                             (?class-level :bard)
                                             {9 8
                                              13 10
                                              17 12
                                              :default 6})
                                            " additional healing at the end of a short rest")})]}
             3 {:selections [(opt5e/expertise-selection 2)]}
             6 {:modifiers [(mod5e/action
                             {:name "Countercharm"
                              :level 6
                              :page 54
                              :summary "performance during your turn that gives you and friendly creatures within 30 ft. advantage on frightened or charmed saves."})]}
             10 {:selections (conj [(opt5e/bard-magical-secrets 10)]
                                   (opt5e/expertise-selection 2))}
             14 {:selections [(opt5e/bard-magical-secrets 14)]}
             18 {:selections [(opt5e/bard-magical-secrets 18)]}}
    :traits [{:name "Font of Inspiration"
              :level 5
              :page 54
              :summary "regain all uses of Bardic Inspiration at the end of a rest"}
             {:name "Superior Inspiration"
              :level 20
              :page 54
              :summary "regain 1 use of Bardic Inspiration if you have none remaining when rolling initiative"
              }]
    
    :subclass-level 3
    :subclass-title "Bard College"
    :subclass-help "Your bard college is a loose association that preserves bardic traditions and affords additional features"
    :subclasses [{:name "College of Lore"
                  :profs {:skill-options {:choose 3 :options {:any true}}}
                  :modifiers [(mod5e/reaction
                               {:name "Cutting Words"
                                :level 3
                                :page 54
                                :summary (str "expend a use of Bardic Inspiration to subtract 1d"
                                              (bardic-inspiration-die ?levels)
                                              " from an attack, ability, or damage roll made by a creature within 60 ft.")})]
                  
                  :levels {6 {:selections [(opt5e/bard-magical-secrets 6)]}
                           14 {:modifiers [(mod5e/dependent-trait
                                            {:name "Peerless Skill"
                                             :level 14
                                             :page 55
                                             :summary (str "expend one use of Bardic Inspiration to add 1d"
                                                           (bardic-inspiration-die ?levels)
                                                           " to an ability check")})]}}}
                 {:name "College of Valor"
                  :profs {:armor {:medium true
                                  :shields true}
                          :weapon {:martial true}}
                  :levels {3 {:modifiers [(mod5e/trait-cfg
                                           {:name "Combat Inspiration"
                                            :page 55
                                            :summary "a creature can add your bardic inspiration to a damage roll or to it's AC against an attack"})]}
                           6 {:modifiers [(extra-attack-trait 55)
                                          (mod5e/num-attacks 2)]}
                           14 {:modifiers [(mod5e/bonus-action
                                           {:name "Battle Magic"
                                            :page 55
                                            :summary "make a weapon attack when you use your action to cast a bard spell"})]}}}]}))

(defn blessings-of-knowledge-skill [skill-name]
  (let [skill-kw (common/name-to-kw skill-name)]
    (t/option-cfg
     {:name skill-name
      :key skill-kw
      :modifiers [(mod5e/skill-proficiency skill-kw)
                  (mod5e/skill-expertise skill-kw)]})))

(defn starting-equipment-option [equipment num]
  (t/option-cfg
   {:name (:name equipment)
    :key (:key equipment)
    :modifiers [(mod5e/equipment (:key equipment) num)]}))

(def spell-level-to-cleric-level
  {1 1
   2 3
   3 5
   4 7
   5 9})

(def cleric-option
  (opt5e/class-option
   {:name "Cleric",
    :spellcasting {:level-factor 1
                   :cantrips-known {1 3 4 1 10 1}
                   :known-mode :all
                   :ability ::char5e/wis
                   :prepares-spells? true}
    :multiclass-prereqs [(opt5e/ability-prereq ::char5e/wis 13)]
    :spellcaster true
    :hit-die 8,
    :ability-increase-levels [4 8 12 16 19]
    :profs {:armor {:light false :medium false :shields false}
            :weapon {:simple true}
            :save {::char5e/wis true ::char5e/cha true}
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
    :armor {:shield 1}
    :selections [(opt5e/new-starting-equipment-selection
                  :cleric
                  {:name "Additional Weapon"
                   :options [(t/option-cfg
                              {:name "Light Crossbow and 20 Bolts"
                               :modifiers [(mod5e/weapon :crossbow-light 1)
                                           (mod5e/equipment :crossbow-bolt 20)]})
                             (t/option-cfg
                              {:name "Simple Weapon"
                               :selections [(opt5e/new-starting-equipment-selection
                                             :cleric
                                             {:name "Simple Weapon"
                                              :options (opt5e/simple-weapon-options 1)
                                              :min 1
                                              :max 1})]})]})
                 (opt5e/new-starting-equipment-selection
                  :cleric
                  {:name "Holy Symbol"
                   :options (map
                             #(starting-equipment-option % 1)
                             equip5e/holy-symbols)})]
    :levels {2 {:modifiers [(mod5e/dependent-trait
                             {:page 59
                              :name "Channel Divinity"
                              :summary "Channel divine power using Turn Undead or one of your domain Channel Divinity options."
                              :frequency (units5e/rests (mod5e/level-val
                                                       (?class-level :cleric)
                                                       {6 2
                                                        18 3
                                                        :default 1}))})
                            (mod5e/action
                             {:page 59
                              :name "Channel Divinity: Turn Undead"
                              :summary (str "undead within 30 feet must make a DC "
                                            (?spell-save-dc ::char5e/wis)
                                            " Wisdom save or be turned for 1 min. or until damaged")})]}
             5 {:modifiers [(mod5e/dependent-trait
                             {:level 5
                              :name "Destroy Undead"
                              :page 59
                              :summary (str "Destroy CR "
                                            (let [level (?class-level :cleric)]
                                              (mod5e/level-val
                                               level
                                               {5 "1/2"
                                                8 1
                                                11 2
                                                14 3
                                                17 4}))
                                            " or less creatures who fail turn save.")})]}
             
             10 {:modifiers [(mod5e/dependent-trait
                              {:name "Divine Intervention"
                               :page 59
                               :summary (str
                                         "You call for aid from your deity, succeeding "
                                         (if (= 20 (?class-level :cleric))
                                           "automatically"
                                           "if you make a percentile roll less than or equal to your cleric level"))})]}}
    :subclass-level 1
    :subclass-title "Divine Domain"
    :subclasses [{:name "Life Domain"
                  :profs {:armor {:heavy true}}
                  :modifiers [(opt5e/cleric-spell 1 :bless 1)
                              (opt5e/cleric-spell 1 :cure-wounds 1)
                              (opt5e/cleric-spell 2 :lesser-restoration 3)
                              (opt5e/cleric-spell 2 :spiritual-weapon 3)
                              (opt5e/cleric-spell 3 :beacon-of-hope 5)
                              (opt5e/cleric-spell 3 :revivify 5)
                              (opt5e/cleric-spell 4 :death-ward 7)
                              (opt5e/cleric-spell 4 :guardian-of-faith 7)
                              (opt5e/cleric-spell 5 :mass-cure-wounds 9)
                              (opt5e/cleric-spell 5 :raise-dead 9)]
                  :levels {2 {:modifiers [(mod5e/action
                                           {:name "Channel Divinity: Preserve Life"
                                            :summary (str "Distribute "
                                                          (* 5 (?class-level :cleric))
                                                          " HPs healing among any creatures within 30 ft., each can be restored to at most 1/2 their HP max")})]}
                           8 {:modifiers [(opt5e/divine-strike "radiant" 60)]}}
                  :traits [{:level 1
                            :name "Disciple of Life"
                            :page 60
                            :summary "1st level or greater healing spells increase healing by 2 + spell's level HPs"}
                           {:level 6
                            :name "Blessed Healer"
                            :page 60
                            :summary "When you cast spells that heal a creature other than you, you regain 2 + spell's level HPs"}
                           {:level 17
                            :name "Supreme Healing"
                            :summary "Instead of rolling healing, use max possible roll value." }]}
                 {:name "Knowledge Domain"
                  :modifiers [(opt5e/cleric-spell 1 :command 1)
                              (opt5e/cleric-spell 1 :identify 1)
                              (opt5e/cleric-spell 2 :augury 3)
                              (opt5e/cleric-spell 2 :suggestion 3)
                              (opt5e/cleric-spell 3 :nondetection 5)
                              (opt5e/cleric-spell 3 :speak-with-dead 5)
                              (opt5e/cleric-spell 4 :arcane-eye 7)
                              (opt5e/cleric-spell 4 :confusion 7)
                              (opt5e/cleric-spell 5 :legend-lore 9)
                              (opt5e/cleric-spell 5 :scrying 9)]
                  :selections [(opt5e/language-selection opt5e/languages 2)
                               (t/selection-cfg
                                {:name "Blessings of Knowledge Skills"
                                 :tags #{:profs :skill-profs}
                                 :options (map
                                           blessings-of-knowledge-skill
                                           ["Arcana" "History" "Nature" "Religion"])
                                 :min 2
                                 :max 2})]
                  :levels {2 {:modifiers [(mod5e/action
                                           {:page 59
                                            :summary "Become proficient in a tool or skill for 10 mins."
                                            :name "Channel Divinity: Knowledge of the Ages"})]}
                           6 {:modifiers [(mod5e/action
                                           {:page 59
                                            :name "Channel Divinity: Read Thoughts"
                                            :summary (str "a creature within 60 ft. must make a DC "
                                                          (?spell-save-dc ::char5e/wis)
                                                          " Wisdom save or you can read it's thoughts for 1 min, use an action to end the effect and cast 'suggestion' without using a slot and with no save")})]}
                           8 {:modifiers [(opt5e/potent-spellcasting 60)]}}
                  :traits [
                           {:level 17
                            :page 60
                            :name "Visions of the Past"
                            :summary "Learn the history of an object you hold or area you are in"}]}
                 {:name "Light Domain"
                  :modifiers [(opt5e/cleric-spell 0 :light 1)
                              (opt5e/cleric-spell 1 :burning-hands 1)
                              (opt5e/cleric-spell 1 :faerie-fire 1)
                              (opt5e/cleric-spell 2 :flaming-sphere 3)
                              (opt5e/cleric-spell 2 :scorching-ray 3)
                              (opt5e/cleric-spell 3 :daylight 5)
                              (opt5e/cleric-spell 3 :fireball 5)
                              (opt5e/cleric-spell 4 :guardian-of-faith 7)
                              (opt5e/cleric-spell 4 :wall-of-fire 7)
                              (opt5e/cleric-spell 5 :flame-strike 9)
                              (opt5e/cleric-spell 5 :scrying 9)
                              (mod5e/reaction
                               {:name "Warding Flare"
                                :page 61
                                :summary "impose disadvantage on an attack roll against you"
                                :frequency (units5e/long-rests
                                            (max 1 (?ability-bonuses ::char5e/wis)))})]
                  :levels {2 {:modifiers [(mod5e/action
                                           {:level 2
                                            :class-key :cleric
                                            :name "Channel Divinity: Radiance of the Dawn"
                                            :page 61
                                            :range {:plural :feet
                                                    :amount 30}
                                            :summary (str "Dispel magical darkness and deal 2d10 + "
                                                          (?class-level :cleric)
                                                          " radiant damage (half on successful DC "
                                                          (?spell-save-dc ::char5e/wis)
                                                          " Constitution save) to hostile creatures")})]}
                           6 {:modifiers [(mod5e/reaction
                                           {:level 6
                                            :name "Improved Flare"
                                            :page 61
                                            :summary "use warding flare when another creature within 30 ft. is attacked"})]}
                           8 {:modifiers [(opt5e/potent-spellcasting 61)]}
                           17 {:modifiers [(mod5e/action
                                            {:level 17
                                             :page 61
                                             :name "Corona of Light"
                                             :summary "emit bright light for 60 ft. and 30 beyond that, enemies in the bright light have disadvantage on saves against spells that deal radiant or fire damage"})]}}}
                 {:name "Nature Domain"
                  :profs {:armor {:heavy true}
                          :skill-options {:choose 1 :options {:animal-handling true :nature true :survival true}}}
                  :modifiers [(opt5e/cleric-spell 1 :animal-friendship 1)
                              (opt5e/cleric-spell 1 :speak-with-animals 1)
                              (opt5e/cleric-spell 2 :barkskin 3)
                              (opt5e/cleric-spell 2 :spike-growth 3)
                              (opt5e/cleric-spell 3 :plant-growth 5)
                              (opt5e/cleric-spell 3 :wind-wall 5)
                              (opt5e/cleric-spell 4 :dominate-beast 7)
                              (opt5e/cleric-spell 4 :grasping-vine 7)
                              (opt5e/cleric-spell 5 :insect-plague 9)
                              (opt5e/cleric-spell 5 :tree-stride 9)]
                  :levels {2 {:modifiers [(mod5e/action
                                           {:name "Channel Divinity: Charm Animals and Plants"
                                            :level 2
                                            :page 62
                                            :range {:plural :feet
                                                    :amount 30}
                                            :summary (str "charm beasts and plant creatures unless they succeed on a DC "
                                                          (?spell-save-dc ::char5e/wis)
                                                          " Wisdom save")})]}
                           6 {:modifiers [(mod5e/reaction
                                           {:name "Dampen Elements"
                                            :level 6
                                            :page 62
                                            :range {:plural :feet
                                                    :amount 30}
                                            :summary "to a creature that takes fire, cold, acid, lighting, or thunder damage, grant resistance to that damage"})]}
                           8 {:modifiers [(opt5e/divine-strike "cold, fire, or lighting" 62)]}
                           17 {:modifiers [(mod5e/bonus-action
                                            {:name "Master of Nature"
                                             :level 17
                                             :page 62
                                             :summary "command creatures charmed with your Charm Animals and Plants"})]}}
                  :selections [(opt5e/druid-cantrip-selection "Cleric")]}
                 {:name "Tempest Domain"
                  :profs {:armor {:heavy true}
                          :weapon {:martial true}}
                  :modifiers [(opt5e/cleric-spell 1 :fog-cloud 1)
                              (opt5e/cleric-spell 1 :thunderwave 1)
                              (opt5e/cleric-spell 2 :gust-of-wind 3)
                              (opt5e/cleric-spell 2 :shatter 3)
                              (opt5e/cleric-spell 3 :call-lightning 5)
                              (opt5e/cleric-spell 3 :sleet-storm 5)
                              (opt5e/cleric-spell 4 :control-water 7)
                              (opt5e/cleric-spell 4 :ice-storm 7)
                              (opt5e/cleric-spell 5 :destructive-wave 9)
                              (opt5e/cleric-spell 5 :insect-plague 9)
                              (mod5e/reaction
                               {:name "Wrath of the Storm"
                                :page 62
                                :frequency (units5e/long-rests
                                            (max 1 (?ability-bonuses ::char5e/wis)))
                                :summary (str "When a creature within 5 ft. hits you, you deal 2d8 lightning or thunder damage to them (half that on successful DC "
                                              (?spell-save-dc ::char5e/wis)
                                              " Dexterity save).")})]
                  :levels {2 {:modifiers [(mod5e/trait-cfg
                                           {:name "Channel Divinity: Destructive Wrath"
                                            :page 62
                                            :level 2
                                            :summary "Rather than roll lighting or thunder damage, deal max damage"})]}
                           8 {:modifiers [(opt5e/divine-strike "thunder" 62)]}
                           17 {:modifiers [(mod5e/flying-speed-equal-to-walking)]}}
                  :traits [{:name "Thunderbolt Strike"
                            :page 62
                            :level 6
                            :summary "Push a Large or smaller creature up to 10 ft. when you deal lightning damage to it"}
                           {:name "Stormborn"
                            :page 62
                            :level 17
                            :summary "Flying speed equal to your walking speed"}]}
                 {:name "Trickery Domain"
                  :modifiers [(opt5e/cleric-spell 1 :charm-person 1)
                              (opt5e/cleric-spell 1 :disguise-self 1)
                              (opt5e/cleric-spell 2 :mirror-image 3)
                              (opt5e/cleric-spell 2 :pass-without-trace 3)
                              (opt5e/cleric-spell 3 :blink 5)
                              (opt5e/cleric-spell 3 :dispel-magic 5)
                              (opt5e/cleric-spell 4 :dimension-door 7)
                              (opt5e/cleric-spell 4 :polymorph 7)
                              (opt5e/cleric-spell 5 :dominate-person 9)
                              (opt5e/cleric-spell 5 :modify-memory 9)
                              (mod5e/action
                               {:name "Blessing of the Trickster"
                                :page 63
                                :duration units5e/hours-1
                                :summary "Give another creature advantage on stealth checks"})]
                  :levels {2 {:modifiers [(mod5e/action
                                           {:name "Channel Divinity: Invoke Duplicity"
                                            :level 2
                                            :page 63
                                            :summary "create illusion of yourself for 1 min. or concentration. Move it 30 ft. as a bonus action, cast spells as if in illusion's space, gain advantage on attacks on a creature both you and the illusion are within 5 ft. of"})]}
                           6 {:modifiers [(mod5e/action
                                           {:name "Channel Divinity: Cloak of Shadows"
                                            :level 6
                                            :page 63
                                            :summary "become invisible until end of your next turn"})]}
                           8 {:modifiers [(opt5e/divine-strike "poison" 63)]}
                           17 {:modifiers [(mod5e/action
                                            {:name "Improved Duplicity"
                                             :level 17
                                             :page 63
                                             :summary "when you use Invoke Duplicity, create up to 4 duplicates"})]}}}
                 {:name "War Domain"
                  :profs {:armor {:heavy true}
                          :weapon {:martial true}}
                  :modifiers [(opt5e/cleric-spell 1 :divine-favor 1)
                              (opt5e/cleric-spell 1 :shield-of-faith 1)
                              (opt5e/cleric-spell 2 :magic-weapon 3)
                              (opt5e/cleric-spell 2 :spiritual-weapon 3)
                              (opt5e/cleric-spell 3 :crusaders-mantle 5)
                              (opt5e/cleric-spell 3 :spirit-guardians 5)
                              (opt5e/cleric-spell 4 :freedom-of-movement 7)
                              (opt5e/cleric-spell 4 :stoneskin 7)
                              (opt5e/cleric-spell 5 :flame-strike 9)
                              (opt5e/cleric-spell 5 :hold-monster 9)
                              (mod5e/bonus-action
                               {:name "War Priest"
                                :level 1
                                :page 63
                                :frequency (units5e/long-rests
                                            (max 1 (?ability-bonuses ::char5e/wis)))
                                :summary "make one extra weapon attack when you use the Attack action"})]
                  :levels {6 {:modifiers [(mod5e/reaction
                                           {:name "Channel Divinity: War God's Blessing"
                                            :level 6
                                            :page 63
                                            :summary "+10 to an attack roll made by a creature within 30 ft."})]}
                           8 {:modifiers [(opt5e/divine-strike nil 63)]}}
                  :traits [{:name "Channel Divinity: Guided Strike"
                            :page 63
                            :level 2
                            :summary "+10 to an attack roll"}
                           {:name "Avatar of Battle"
                            :page 63
                            :level 17
                            :summary "from non-magical weapons, resistance to slashing, bludgeoning, and piercing damage"}]}]}))

(defn druid-spell [spell-level spell-key min-level]
  (mod5e/spells-known-cfg spell-level
                      {:key spell-key
                       :ability ::char5e/wis
                       :class "Druid"
                       :class-key :druid
                       :always-prepared? true}
                      min-level
                      nil))

(defn lands-stride [level]
  {:name "Land's Stride"
   :level level
   :page 69
   :summary "moving through nonmagical difficult terrain costs no extra movement, pass through nonmagical plants without being slowed by them and without taking damage from them"})

(def druid-option
  (opt5e/class-option
   {:name "Druid"
    :hit-die 8
    :spellcaster true
    :spellcasting {:level-factor 1
                   :cantrips-known {1 2 4 1 10 1}
                   :known-mode :all
                   :ability ::char5e/wis
                   :prepares-spells? true}
    :multiclass-prereqs [(opt5e/ability-prereq ::char5e/wis 13)]
    :ability-increase-levels [4 8 12 16 19]
    :profs {:armor {:light false :medium false :shields false}
            :weapon {:club true :dagger true :dart true :javelin true :mace true :quarterstaff true :scimitar true :sickle true :sling true :spear true}
            :tool {:herbalism-kit true}
            :save {::char5e/int true ::char5e/wis true}
            :skill-options {:choose 2 :options {:arcana true :animal-handling true :insight true :medicine true :nature true :perception true :religion true :survival true}}}
    :armor {:leather 1}
    :equipment-choices [{:name "Equipment Pack"
                         :options {:priests-pack 1
                                   :explorers-pack 1}}
                        {:name "Druidic Focus"
                         :options {:druidic-focus 1}}]
    :equipment {:explorers-pack 1}
    :modifiers [(mod5e/language :druidic)]
    :levels {2 {:modifiers [(mod/modifier
                             ?wild-shape-cr
                             (mod5e/level-val
                              (?class-level :druid)
                              {1 "1/4"
                               4 "1/2"
                               8 "1"}))
                            (mod/modifier
                             ?wild-shape-limitation
                             (mod5e/level-val
                              (?class-level :druid)
                              {1 "no flying or swimming speed"
                               4 "no flying speed"
                               8 nil}))
                            (mod5e/action
                             {:name "Wild Shape"
                              :page 66
                              :frequency (units5e/rests 2)
                              :duration (units5e/hours (int (/ (?class-level :druid) 2)))
                              :summary (str "You can transform into a beast you have seen with CR "
                                            ?wild-shape-cr
                                            (if ?wild-shape-limitation (str " and " ?wild-shape-limitation)))})]}}
    :selections [(opt5e/new-starting-equipment-selection
                  :druid
                  {:name "Wooden Shield or Simple Weapon"
                   :options [(t/option-cfg
                              {:name "Wooden Shield"
                               :modifiers [(mod5e/armor :shield 1)]})
                             (t/option-cfg
                              {:name "Simple Weapon"
                               :selections [(opt5e/new-starting-equipment-selection
                                             :druid
                                             {:name "Simple Weapon"
                                              :options (opt5e/simple-weapon-options 1)
                                              :min 1
                                              :max 1})]})]})
                 (opt5e/new-starting-equipment-selection
                  :druid
                  {:name "Melee Weapon"
                   :options [(t/option-cfg
                              {:name "Scimitar"
                               :modifiers [(mod5e/weapon :scimitar 1)]})
                             (t/option-cfg
                              {:name "Simple Melee Weapon"
                               :selections [(opt5e/new-starting-equipment-selection
                                             :druid
                                             {:name "Simple Melee Weapon"
                                              :options (opt5e/simple-melee-weapon-options 1)})]})]})]
    :traits [{:name "Druidic"
              :page 66
              :summary "You can speak Druidic and use it to leave hidden message and automatically spot messages left by others"}
             {:name "Timeless Body"
              :level 18
              :page 67
              :summary "age slowly"}
             {:name "Beast Spells"
              :level 18
              :page 67
              :summary "while in Wild Shape, can perform druid spells' somatic and verbal components"}
             {:name "Archdruid"
              :level 20
              :page 67
              :summary "Wild Shape unlimited times, ignore verbal and somatic spell components, ignore material components with no cost and aren't consumed by spell"}]
    :subclass-level 2
    :subclass-title "Druid Circle"
    :subclasses [{:name "Circle of the Land"
                  :selections [(opt5e/spell-selection
                                {:class-key :druid
                                 :level 0
                                 :spellcasting-ability ::char5e/wis
                                 :class-name "Druid"
                                 :num 1})
                               (t/selection-cfg
                                {:name "Land Type"
                                 :tags #{:class}
                                 :options [(t/option-cfg
                                   {:name "Arctic"
                                    :modifiers [(druid-spell 3 :slow 5)
                                                (druid-spell 5 :cone-of-cold 9)]})
                                  (t/option-cfg
                                   {:name "Coast"
                                    :modifiers [(druid-spell 2 :mirror-image 3)
                                              (druid-spell 2 :misty-step 3)]})
                                  (t/option-cfg
                                   {:name "Desert"
                                    :modifiers [(druid-spell 2 :blur 3)
                                              (druid-spell 2 :silence 3)
                                              (druid-spell 3 :create-food-and-water 5)]})
                                  (t/option-cfg
                                   {:name "Forest"
                                    :modifiers [(druid-spell 2 :spider-climb 3)
                                              (druid-spell 4 :divination 7)]})
                                  (t/option-cfg
                                   {:name "Grassland"
                                    :modifiers [(druid-spell 2 :invisibility 3)
                                              (druid-spell 3 :haste 5)
                                              (druid-spell 4 :divination 7)
                                              (druid-spell 5 :dream 9)]})
                                  (t/option-cfg
                                   {:name "Mountain"
                                    :modifiers [(druid-spell 2 :spider-climb 3)
                                                (druid-spell 3 :lightning-bolt 5)
                                                (druid-spell 5 :passwall 9)]})
                                  (t/option-cfg
                                   {:name "Swamp"
                                    :modifiers [(druid-spell 2 :darkness 3)
                                                (druid-spell 2 :melfs-acid-arrow 3)
                                                (druid-spell 3 :stinking-cloud 5)
                                                (druid-spell 3 :water-walk 5)]})
                                  (t/option-cfg
                                   {:name "Underdark"
                                    :modifiers [(druid-spell 2 :spider-climb 3)
                                                (druid-spell 2 :web 3)
                                                (druid-spell 3 :stinking-cloud 5)
                                                (druid-spell 3 :gaseous-form 5)
                                                (druid-spell 4 :greater-invisibility 7)
                                                (druid-spell 5 :cloudkill 9)]})]})]
                  :modifiers []
                  :levels {2 {:modifiers [(mod5e/dependent-trait
                                           {:name "Natural Recovery"
                                            :level 2
                                            :page 68
                                            :summary (str "During short rest, recover "
                                                          (common/round-up (/ (?class-level :druid) 2))
                                                          " spell slots less than 6th level")})]}
                           6 {:modifiers [(mod5e/saving-throw-advantage ["plants magically created or manipulated to impede movement"])]}
                           10 {:modifiers [(mod5e/damage-immunity :poison)
                                           (mod5e/condition-immunity :poisoned)
                                           (mod5e/condition-immunity :charmed "by elementals or fey")
                                           (mod5e/condition-immunity :frightened "by elementals or fey")
                                           (mod5e/immunity :disease)
                                           (mod5e/trait-cfg
                                            {:name "Nature's Ward"
                                             :page 69
                                             :summary "immune to being charmed by fey or elementals; immune to poison and disease"})]}
                           14 {:modifiers [(mod5e/dependent-trait
                                            {:name "Nature's Santuary"
                                             :level 14
                                             :page 69
                                             :summary (str "beasts or plant creatures must make a DC "
                                                           (?spell-save-dc ::char5e/wis)
                                                           " Wisdom save or they cannot attack you.")})]}}
                  :traits [(lands-stride 6)]}
                 {:name "Circle of the Moon"
                  :levels {2 {:modifiers [(mod5e/bonus-action
                                           {:name "Combat Wild Shape"
                                            :page 69
                                            :summary "can Wild Shape as bonus action instead of action, while transformed expend a spell slot and gain 1d8 HP per slot level"})
                                          (mod/modifier
                                           ?wild-shape-cr
                                           (max 1 (int (/ (?class-level :druid) 3))))]}
                           10 {:modifiers [(mod5e/bonus-action
                                            {:name "Elemental Wild Shape"
                                             :level 10
                                             :page 69
                                             :summary "expend two Wild Shape uses to transform into an air, earth, fire, or water elemental"})]}}
                  :traits [{:name "Primal Strike"
                            :level 6
                            :page 69
                            :summary "Your beast form attacks count as magical"}
                           {:name "Thousand Forms"
                            :page 69
                            :summary "cast alter self at will"
                            :level 14}]}]}))

(defn eldritch-knight-spell? [s]
  (let [school (:school s)]
    (or (= school "evocation")
        (= school "abjuration"))))

(defn arcane-trickster-spell? [s]
  (let [school (:school s)]
    (or (= school "enchantment")
        (= school "illusion"))))


(defn subclass-wizard-spell-selection [title ref class-key class-name num spell-levels & [filter-fn]]
  (opt5e/spell-selection {:title title
                          :class-key class-key
                          :ref ref
                          :spellcasting-ability ::char5e/int
                          :class-name class-name
                          :num num
                          :prepend-level? true
                          :spell-keys (let [spell-keys
                                            (mapcat
                                             (fn [lvl] (get-in sl/spell-lists [:wizard lvl]))
                                             spell-levels)]
                                        (if filter-fn
                                          (filter
                                           (fn [spell-key]
                                             (filter-fn (spells/spell-map spell-key)))
                                           spell-keys)
                                          spell-keys))}))

(defn eldritch-knight-ref [subpath]
  (concat
   [:class :fighter :levels :level-3 :martial-archetype :eldritch-knight]
   subpath))

(defn arcane-trickster-ref [subpath]
  (concat
   [:class :rogue :levels :level-3 :roguish-archetype :arcane-trickster]
   subpath))

(defn eldritch-knight-spell-selection [num spell-levels]
  (subclass-wizard-spell-selection "Eldritch Knight Abjuration or Evocation Spells"
                                   (eldritch-knight-ref [:abjuration-or-evocation-spells-known])
                                   :fighter
                                   "Eldritch Knight"
                                   num
                                   spell-levels
                                   eldritch-knight-spell?))

(defn arcane-trickster-spell-selection [num spell-levels]
  (subclass-wizard-spell-selection "Arcane Trickster Enchantment or Illusion Spells"
                                   (arcane-trickster-ref [:enchantment-or-illusion-spells-known])
                                   :rogue
                                   "Arcane Trickster"
                                   num
                                   spell-levels
                                   arcane-trickster-spell?))

(defn eldritch-knight-any-spell-selection [num spell-levels]
  (subclass-wizard-spell-selection "Eldritch Knight Spells: Any School"
                                   (eldritch-knight-ref [:spells-known-any-school])
                                   :fighter
                                   "Eldritch Knight"
                                   num
                                   spell-levels))

(defn arcane-trickster-any-spell-selection [num spell-levels]
  (subclass-wizard-spell-selection "Arcane Trickster Spells: Any School"
                                   (arcane-trickster-ref [:spells-known-any-school])
                                   :rogue
                                   "Arcane Trickster"
                                   num
                                   spell-levels))

(defn eldritch-knight-cantrip [num]
  (opt5e/spell-selection {:class-key :fighter
                          :level 0
                          :ref (eldritch-knight-ref [:cantrips-known])
                          :spellcasting-ability ::char5e/int
                          :class-name "Eldritch Knight"
                          :num num
                          :spell-keys (get-in sl/spell-lists [:wizard 0])}))

(defn arcane-trickster-cantrip [num]
  (opt5e/spell-selection {:class-key :rogue
                          :level 0
                          :ref (arcane-trickster-ref [:cantrips-known])
                          :spellcasting-ability ::char5e/int
                          :class-name "Arcane Trickster"
                          :num num
                          :spell-keys (get-in sl/spell-lists [:wizard 0])}))

(def eldritch-knight-cfg
  {:name "Eldritch Knight"
   :spellcasting {:level-factor 3}
   :modifiers [(mod5e/bonus-action
                {:name "Summon Bonded Weapon"
                 :page 75
                 :summary "If on the same plane of existence, instantly teleport a bonded weapon into your hand"})]
   :levels {3 {:selections [(eldritch-knight-cantrip 2)
                            (eldritch-knight-spell-selection 2 [1])
                            (eldritch-knight-any-spell-selection 1 [1])]}
            4 {:selections [(eldritch-knight-spell-selection 1 [1])]}
            7 {:selections [(eldritch-knight-spell-selection 1 [1 2])]
               :modifiers [(mod5e/bonus-action
                            {:name "War Magic"
                             :page 75
                             :summary "make a weapon attack if you used your action to cast a cantrip"})]}
            8 {:selections [(eldritch-knight-any-spell-selection 1 [1 2])]}
            10 {:selections [(eldritch-knight-cantrip 1)
                             (eldritch-knight-spell-selection 1 [1 2])]}
            11 {:selections [(eldritch-knight-spell-selection 1 [1 2])]}
            13 {:selections [(eldritch-knight-spell-selection 1 [1 2 3])]}
            14 {:selections [(eldritch-knight-any-spell-selection 1 [1 2 3])]}
            16 {:selections [(eldritch-knight-spell-selection 1 [1 2 3])]}
            18 {:modifiers [(mod5e/bonus-action
                             {:name "Improved War Magic"
                              :page 75
                              :summary "make a weapon attack if you used your action to cast a spell"})]}
            19 {:selections [(eldritch-knight-spell-selection 1 [1 2 3 4])]}
            20 {:selections [(eldritch-knight-any-spell-selection 1 [1 2 3 4])]}}
   :traits [{:name "Weapon Bond"
             :level 3
             :page 75
             :summary "Bond with up to two weapons (see Summon Bonded Weapon)"}
            {:name "Eldritch Strike"
             :page 75
             :level 10
             :summary "a creature has disadvantage on next saving throw against a spell you cast before the end of your next turn if you hit it with a weapon attack"}
            {:name "Arcane Charge"
             :level 15
             :page 75
             :summary "teleport up to 30 ft. when you use Action Surge"}]})

(defn martial-maneuvers-selection [num]
  (t/selection-cfg
   {:name "Martial Maneuvers"
    :options opt5e/maneuver-options
    :ref [:class :fighter :levels :level-3 :martial-archetype :battle-master :martial-maneuvers]
    :tags #{:class}
    :min num
    :max num}))


(def fighter-option
  (opt5e/class-option
   {:name "Fighter",
    :hit-die 10,
    :ability-increase-levels [4 6 8 12 14 16 19]
    :profs {:armor {:light false :medium false :heavy true :shields false}
            :weapon {:simple false :martial false} 
            :save {::char5e/str true ::char5e/con true}
            :skill-options {:choose 2 :options {:acrobatics true :animal-handling true :athletics true :history true :insight true :intimidation true :perception true :survival true}}}
    :multiclass-prereqs [(t/option-prereq "Requires Strength 13 or Dexterity 13"
                                          (fn [c]
                                            (let [abilities @(subscribe [::char5e/abilities nil c])]
                                              (or (>= (::char5e/str abilities) 13)
                                                  (>= (::char5e/dex abilities) 13)))))]
    :equipment-choices [{:name "Equipment Pack"
                         :options {:dungeoneers-pack 1
                                   :explorers-pack 1}}]
    :modifiers [(mod5e/bonus-action
                 {:name "Second Wind"
                  :page 72
                  :frequency units5e/rests-1
                  :summary (str "regain 1d10 "
                                (common/mod-str (?class-level :fighter))
                                " HPs")})]
    :levels {2 {:modifiers [(mod5e/action
                             {:level 2
                              :name "Action Surge"
                              :page 72
                              :frequency (units5e/rests (if (>= (?class-level :fighter) 17)
                                                        2
                                                        1))
                              :summary "take an extra action"})]}
             5 {:modifiers [(mod5e/num-attacks 2)]}
             9 {:modifiers [(mod5e/dependent-trait
                             {:level 9
                              :name "Indomitable"
                              :page 72
                              :frequency (units5e/long-rests
                                          (mod5e/level-val
                                           (?class-level :fighter)
                                           {13 2
                                            17 3
                                            :default 1}))
                              :summary "reroll a save if you fail"})]}
             11 {:modifiers [(mod5e/num-attacks 3)]}
             20 {:modifiers [(mod5e/num-attacks 4)]}}
    :subclass-level 3
    :subclass-title "Martial Archetype"
    :selections [(opt5e/fighting-style-selection :fighter)
                 (opt5e/new-starting-equipment-selection
                  :fighter
                  {:name "Armor"
                   :options [(t/option-cfg
                              {:name "Chain Mail"
                               :modifiers [(mod5e/armor :chain-mail 1)]})
                             (t/option-cfg
                              {:name "Leather Armor, Longbow, 20 Arrows"
                               :modifiers [(mod5e/armor :leather 1)
                                           (mod5e/weapon :longbow 1)
                                           (mod5e/equipment :arrow 20)]})]})
                 (opt5e/new-starting-equipment-selection
                  :fighter
                  {:name "Weapons"
                   :options [(t/option-cfg
                              {:name "Martial Weapon and Shield"
                               :selections [(opt5e/new-starting-equipment-selection
                                             :fighter
                                             {:name "Martial Weapon"
                                              :options (opt5e/martial-weapon-options 1)})]
                               :modifiers [(mod5e/armor :shield 1)]})
                             (t/option-cfg
                              {:name "Two Martial Weapons"
                               :selections [(opt5e/new-starting-equipment-selection
                                             :fighter
                                             {:name "Martial Weapon 1"
                                              :options (opt5e/martial-weapon-options 1)
                                              :min 1
                                              :max 1})
                                            (opt5e/new-starting-equipment-selection
                                             :fighter
                                             {:name "Martial Weapon 2"
                                              :options (opt5e/martial-weapon-options 1)
                                              :min 1
                                              :max 1})]})]})
                 (opt5e/new-starting-equipment-selection
                  :fighter
                  {:name "Additional Weapons"
                   :options [(t/option-cfg
                              {:name "Light Crossbow and 20 Bolts"
                               :modifiers [(mod5e/weapon :crossbow-light 1)
                                           (mod5e/equipment :crossbow-bolt 20)]})
                             (t/option-cfg
                              {:name "Two Handaxes"
                               :modifiers [(mod5e/weapon :handaxe 2)]})]})]
    :subclasses [{:name "Champion"
                  :levels {3 {:modifiers [(mod5e/critical 19)]}
                           7 {:modifiers [(mod/vec-mod ?default-skill-bonus-fns
                                                       (fn [ability-kw]
                                                         (if (#{::char5e/str
                                                                ::char5e/dex
                                                                ::char5e/con}
                                                              ability-kw)
                                                           (common/round-up (/ ?prof-bonus 2))
                                                           0)))
                                          (mod/cum-sum-mod ?initiative (common/round-up (/ ?prof-bonus 2)))
                                          (mod5e/dependent-trait
                                           {:level 7
                                            :name "Remarkable Athlete"
                                            :page 72
                                            :summary (str "+"
                                                          (common/round-up (/ ?prof-bonus 2))
                                                          " to STR, DEX, or CON checks that don't already include prof bonus; running long jump increases by "
                                                          (?ability-bonuses ::char5e/str)
                                                          " ft.")})]}
                           10 {:selections [(opt5e/fighting-style-selection :fighter)]}
                           15 {:modifiers [(mod5e/critical 18)]}
                           18 {:modifiers [(mod5e/dependent-trait
                                            {:page 73
                                             :name "Survivor"
                                             :summary (str "At start of your turns, if you have at most half of your "
                                                           #_(int (/ ?max-hit-points 2))
                                                           " HPs left, regain "
                                                           (+ 5 (?ability-bonuses ::char5e/con)) " HPs")})]}}}
                 {:name "Battle Master"
                  :selections [(martial-maneuvers-selection 3)
                               (opt5e/tool-selection (map :key equip5e/artisans-tools) 1)]
                  :modifiers [(mod/modifier ?maneuver-save-dc (max (?spell-save-dc ::char5e/dex)
                                                                   (?spell-save-dc ::char5e/str)))
                              (mod5e/dependent-trait
                               {:name "Combat Superiority"
                                :page 73
                                :level 3
                                :summary (let [[num-maneuvers num-dice die]
                                               (mod5e/level-val
                                                (?class-level :fighter)
                                                {7 [5 5 8]
                                                 10 [7 5 10]
                                                 15 [9 6 10]
                                                 18 [9 6 12]
                                                 :default [3 4 8]})]
                                           (str "You know "
                                                num-maneuvers
                                                " martial maneuvers, have "
                                                num-dice
                                                " superiority dice (d"
                                                die
                                                "s), and maneuver save DC of "
                                                ?maneuver-save-dc))})]
                  :levels {7 {:selections [(martial-maneuvers-selection 2)]}
                           10 {:selections [(martial-maneuvers-selection 2)]}
                           15 {:selections [(martial-maneuvers-selection 2)]}}
                  :traits [{:name "Know Your Enemy"
                            :level 7
                            :page 73
                            :class-key :fighter
                            :summary "Study a creature outside combat for 1 min. to learn if it is superior, inferior, or equal in STR, DEX, CON, AC, current HP, total levels, fighter levels"}
                           {:name "Relentless"
                            :level 15
                            :page 74
                            :class-key :fighter
                            :summary "you regain 1 superiority die when you roll iniative and have no remaining superiority dice"}]}
                 eldritch-knight-cfg]}))

(defn monk-weapon? [{:keys [key type melee? heavy? two-handed?]}]
  (or (= key :shortsword)
      (and (= type :simple)
           melee?
           (not heavy?)
           (not two-handed?))))

(def monk-option
  (opt5e/class-option
   (merge
    opt5e/monk-base-cfg
    {:hit-die 8
     :ability-increase-levels [4 8 12 16 19]
     :unarmored-abilities [::char5e/wis]
     :profs {:weapon {:simple false :shortsword false}
             :save {::char5e/dex true ::char5e/str true}
             :tool-options {:musical-instrument 1 :artisans-tool 1}
             :skill-options {:choose 2 :options {:acrobatics true :athletics true :history true :insight true :religion true :stealth true}}}
     :multiclass-prereqs [(t/option-prereq "Requires Wisdom 13 and Dexterity 13"
                                           (fn [c]
                                             (let [abilities @(subscribe [::char5e/abilities nil c])]
                                               (and (>= (::char5e/wis abilities) 13)
                                                    (>= (::char5e/dex abilities) 13)))))]
     :equipment-choices [{:name "Equipment Pack"
                          :options {:dungeoneers-pack 1
                                    :explorers-pack 1}}]
     :weapon-choices [{:name "Weapon"
                       :options {:shortsword 1
                                 :simple 1}}]
     :modifiers [(mod/vec-mod ?weapon-ability-modifiers
                              (fn [weapon finesse?]
                                (if (monk-weapon? weapon)
                                  (get ?ability-bonuses ::char5e/dex)
                                  0)))
                 (mod/vec-mod ?unarmored-defense :monk)
                 (mod/cum-sum-mod ?unarmored-ac-bonus
                                  (?ability-bonuses ::char5e/wis)
                                  nil
                                  nil
                                  [(= :monk (first ?unarmored-defense))])
                 (mod/modifier ?martial-arts-die (mod5e/level-val
                                                  (?class-level :monk)
                                                  {5 6
                                                   11 8
                                                   17 10
                                                   :default 4}))
                 (mod5e/attack
                  {:name "Martial Arts"
                   :damage-die ?martial-arts-die
                   :damage-die-count 1
                   :damage-modifier (max (?ability-bonuses ::char5e/str) (?ability-bonuses ::char5e/dex))
                   :summary "Unarmed strike or monk weapon"})
                 (mod5e/bonus-action
                  {:name "Martial Arts"
                   :page 78
                   :summary "Make an extra unarmed strike when you take Attack action"})]
     :levels {2 {:modifiers [(mod5e/unarmored-speed-bonus 10)
                             (mod5e/dependent-trait
                              {:name "Ki"
                               :page 78
                               :level 2
                               :summary (str "You have " (?class-level :monk) " ki points")})
                             (mod5e/bonus-action
                              {:name "Flurry of Blows"
                               :page 78
                               :level 2
                               :summary "After you take Attack action, spend 1 ki to make 2 unarmed strikes"})
                             (mod5e/bonus-action
                              {:name "Patient Defense"
                               :page 78
                               :summary "Spend 1 ki point to take the Dodge action"})
                             (mod5e/bonus-action
                              {:name "Step of the Wind"
                               :page 78
                               :summary "Spend 1 ki point to take the Disengage or Dash action and jump distance is doubled for the turn"})]}
              3 {:modifiers [(mod5e/reaction
                              {:name "Deflect Missiles"
                               :page 78
                               :summary (str "When hit by a ranged attack, reduce the damage by 1d10 " (common/mod-str (+ (?ability-bonuses ::char5e/dex) (?class-level :monk))) ". If you reduce it to 0, you can catch the missile and use it in a ranged attack as a monk weapon with range 20/60")})]}
              4 {:modifiers [(mod5e/reaction
                              {:name "Slow Fall"
                               :page 78
                               :level 4
                               :summary (str "reduce falling damage by " (* 5  (?class-level :monk)))})]}
              5 {:modifiers [(mod5e/num-attacks 2)
                             (mod5e/dependent-trait
                              {:name "Stunning Strike"
                               :page 79
                               :level 5
                               :summary (str "when you hit a creature with melee attack, spend 1 ki point to stun the creature if it fails a DC " (?spell-save-dc ::char5e/wis) " CON save")})]}
              6 {:modifiers [(mod5e/unarmored-speed-bonus 5)]}
              7 {:modifiers [(mod5e/action
                              {:name "Stillness of Mind"
                               :page 79
                               :summary "end one effect causing you to be charmed or frightened"})]}
              10 {:modifiers [(mod5e/damage-immunity :poison)
                              (mod5e/immunity :disease)
                              (mod5e/unarmored-speed-bonus 5)]}
              13 {:modifiers (map
                              (fn [{:keys [name key]}]
                                (mod5e/language key))
                              opt5e/languages)}
              14 {:modifiers [(apply mod5e/saving-throws nil char5e/ability-keys)
                              (mod5e/unarmored-speed-bonus 5)]}
              18 {:modifiers [(mod5e/unarmored-speed-bonus 5)
                              (mod5e/action
                               {:name "Empty Body: Invisibility"
                                :level 18
                                :page 79
                                :duration units5e/minutes-1
                                :summary "spend 4 ki points to become invisible and have resistance to all damage but force damage"})
                              (mod5e/action
                               {:name "Empty Body: Astral Projection"
                                :page 79
                                :level 18
                                :summary "use 8 ki points to cast the astral projection spell"})]}}
     :weapons {:dart 10}
     :traits [{:name "Ki-Empowered Strikes"
               :page 79
               :level 6
               :summary "your unarmed strikes count as magical"}
              (opt5e/evasion 7 79)
              {:name "Tongue of the Sun and Moon"
               :page 79
               :level 13
               :summary "you understand all languages and can communicate with any creature that can understand a language"}
              {:name "Diamond Soul"
               :level 14
               :page 79
               :summary "you are proficient in all saves. You can spend 1 ki point to reroll failed saves."}
              {:name "Timeless Body"
               :page 79
               :level 15
               :summary "you can't be aged magically and you need no food or water"}
             
              {:name "Perfect Self"
               :page 79
               :level 20
               :summary "regain 4 ki when you have none and roll initiative"}]
     :subclasses [{:name "Way of the Open Hand"
                   :modifiers [(mod5e/dependent-trait
                                {:name "Open Hand Technique"
                                 :page 79
                                 :summary (str "when you hit with Flurry of Blows, you impose one of the effects on the target: 1) must make a DC "(?spell-save-dc ::char5e/wis) " DEX save or be knocked prone. 2) make a DC " (?spell-save-dc ::char5e/wis) " STR save or be pushed 15 ft. 3) can't take reactions until end of your next turn")})]
                   :levels {6 {:modifiers [(mod5e/action
                                            {:name "Wholeness of Body"
                                             :page 79
                                             :level 6
                                             :frequency units5e/long-rests-1
                                             :summary (str "heal yourself " (* 3 (?class-level :monk)) " HPs")})]}
                            11 {:modifiers [(mod5e/dependent-trait
                                             {:name "Tranquility"
                                              :page 80
                                              :level 11
                                              :summary (str "gain effects of sanctuary spell (save DC " (?spell-save-dc ::char5e/wis) ") between rests")})]}
                            17 {:modifiers [(mod5e/dependent-trait
                                             {:name "Quivering Palm"
                                              :level 17
                                              :page 80
                                              :summary (str "when you hit a creature with unarmed strike, set up vibrations that last " (?class-level :monk) " days. Use an action to end the vibrations, reducing the target to 0 HPs on failed DC " (?spell-save-dc ::char5e/wis) " CON save. It takes 10d10 necrotic damage on successful save.")})]}}}
                  {:name "Way of Shadow"
                   :modifiers [(mod5e/spells-known 0 :minor-illusion ::char5e/wis "Monk (Way of Shadow)")
                               (mod5e/action
                                {:name "Shadow Arts"
                                 :page 80
                                 :summary "spend 2 ki to cast 'darkness', 'darkvision', 'pass without trace', or 'silence' spell"})]
                   :levels {6 {:modifiers [(mod5e/bonus-action
                                             {:name "Shadow Step"
                                              :page 80
                                              :summary "teleport 60 ft. and gain advantage on first melee attack before end of turn"})]}
                            11 {:modifiers [(mod5e/action
                                             {:name "Cloak of Shadows"
                                              :level 11
                                              :page 80
                                              :summary "become invisible"})]}
                            17 {:modifiers [(mod5e/reaction
                                             {:name "Opportunist"
                                              :page 80
                                              :level 17
                                              :summary "when a creature within 5 ft. is hit by attack from someone else, make a melee attack"})]}}}
                  {:name "Way of the Four Elements"
                   :modifiers [(mod5e/dependent-trait
                                {:name "Disciple of the Elements"
                                 :page 80
                                 :summary (str "You learn elemental disciplines with spell save DC " (?spell-save-dc ::char5e/wis) "."
                                               (if (>= (?class-level :monk) 5)
                                                 (str " You can increase the level of elemental discipline spells you cast by 1 for each additional ki point you spend, up to " (mod5e/level-val (?class-level :monk)
                                                                                                                                                                                                 {9 4 13 5 17 6 :default 3}))))})]
                   :levels {3 {:selections [(opt5e/monk-elemental-disciplines)]}
                            6 {:selections [(opt5e/monk-elemental-disciplines)]}
                            11 {:selections [(opt5e/monk-elemental-disciplines)]}
                            17 {:selections [(opt5e/monk-elemental-disciplines)]}}
                   :traits [{:name "Elemental Attunement"
                             :page 81
                             :summary "create minor elemental effect"}]}]})))


(def paladin-option
  (opt5e/class-option
   (merge
    opt5e/paladin-base-cfg
    {:name "Paladin"
     :spellcaster true
     :spellcasting {:level-factor 2
                    :known-mode :all
                    :ability ::char5e/cha
                    :prepares-spells? true}
     :hit-die 10
     :ability-increase-levels [4 8 12 16 19]
     :profs {:armor {:light false :medium false :heavy true :shields false}
             :weapon {:simple false :martial false}
             :save {::char5e/wis true ::char5e/cha true}
             :skill-options {:choose 2 :options {:athletics true :insight true :intimidation true :medicine true :persuasion true :religion true}}}
     :multiclass-prereqs [(t/option-prereq "Requires Strength 13 or Charisma 13"
                                           (fn [c]
                                             (let [abilities @(subscribe [::char5e/abilities nil c])]
                                               (and (>= (::char5e/str abilities) 13)
                                                    (>= (::char5e/cha abilities) 13)))))]
     :equipment-choices [{:name "Equipment Pack"
                          :options {:priests-pack 1
                                    :explorers-pack 1}}]
     :armor {:chain-mail 1}
     :levels {2 {:selections [(opt5e/fighting-style-selection :paladin #{:defense :dueling :great-weapon-fighting :protection})]}
              3 {:modifiers [(mod5e/immunity :disease)
                             (mod5e/trait-cfg
                              {:name "Divine Health"
                               :page 85
                               :summary "immune to disease"})]}
              5 {:modifiers [(mod5e/num-attacks 2)]}
              6 {:modifiers (conj
                             (map
                              #(mod/modifier ?saving-throw-bonuses
                                             (merge-with +
                                                         ?saving-throw-bonuses
                                                         {% (get ?ability-bonuses ::char5e/cha 0)}))
                              char5e/ability-keys)
                             (mod5e/dependent-trait
                              {:name "Aura of Protection"
                               :page 85
                               :summary (str "you and friendly creatures within " ?paladin-aura " ft. have a " (common/bonus-str (max 1 (?ability-bonuses ::char5e/cha))) " bonus to saves")}))}
              10 {:modifiers [(mod5e/dependent-trait
                               {:name "Aura of Courage"
                                :page 85
                                :summary (str (str "you and friendly creatures within " ?paladin-aura " ft. can't be frightened"))})]}
              14 {:modifiers [(mod5e/action
                               {:name "Cleansing Touch"
                                :page 85
                                :frequency (units5e/long-rests (?ability-bonuses ::char5e/cha))
                                :summary "end a spell on yourself or willing creature"})]}}
     :modifiers [(mod/modifier ?paladin-aura (if (< (?class-level :paladin) 18) 10 30))
                 (mod5e/action
                  {:name "Divine Sense"
                   :page 84
                   :frequency (units5e/long-rests
                               (inc (?ability-bonuses ::char5e/cha)))
                   :summary "within 60 ft., detect presense of undead, celestial, or fiend. Also detect consecrated or desecrated object or place"})
                 (mod5e/action
                  {:name "Lay on Hands"
                   :page 84
                   :frequency units5e/long-rests-1
                   :summary (str "you have a healing pool of " (* 5 (?class-level :paladin)) " HPs, with it you can heal a creature or expend 5 points to cure disease or neutralize poison")})
                 (mod5e/dependent-trait
                  {:name "Channel Divinity"
                   :page 85
                   :level 3
                   :frequency units5e/rests-1
                   :summary "your oath provides specific options"})]
     :selections [(opt5e/new-starting-equipment-selection
                   :paladin
                   {:name "Weapons"
                    :options [(t/option-cfg
                               {:name "Martial Weapon and Shield"
                                :selections [(opt5e/new-starting-equipment-selection
                                              :paladin
                                              {:name "Martial Weapon"
                                               :options (opt5e/martial-weapon-options 1)})]
                                :modifiers [(mod5e/armor :shield 1)]})
                              (t/option-cfg
                               {:name "Two Martial Weapons"
                                :selections [(opt5e/new-starting-equipment-selection
                                             :paladin
                                             {:name "Martial Weapon 1"
                                              :options (opt5e/martial-weapon-options 1)
                                              :min 1
                                              :max 1})
                                            (opt5e/new-starting-equipment-selection
                                             :paladin
                                             {:name "Martial Weapon 2"
                                              :options (opt5e/martial-weapon-options 1)
                                              :min 1
                                              :max 1})]})]})
                  (opt5e/new-starting-equipment-selection
                   :paladin
                   {:name "Melee Weapon"
                    :options [(t/option-cfg
                               {:name "Five Javelins"
                                :modifiers [(mod5e/weapon :javelin 5)]})
                              (t/option-cfg
                               {:name "Simple Melee Weapon"
                                :selections [(opt5e/new-starting-equipment-selection
                                              :paladin
                                              {:name "Simple Melee Weapon"
                                               :options (opt5e/simple-melee-weapon-options 1)})]})]})]
     :traits [{:name "Divine Smite"
               :level 2
               :page 85
               :summary "when you hit with melee weapon attack, you can expend 1 X-th level spell slot to deal extra (X+1)d8 radiant damage, up to 5d8. Additional d8 on fiend or undead."}
              {:name "Improved Divine Smite"
               :level 11
               :page 85
               :summary "whenever you hit with melee weapon, you deal an extra d8 radiant damage"}]
     :subclass-level 3
     :subclass-title "Sacred Oath"
     :subclasses [{:name "Oath of Devotion"
                   :modifiers [(opt5e/paladin-spell 1 :protection-from-evil-and-good 3)
                               (opt5e/paladin-spell 1 :sanctuary 3)
                               (opt5e/paladin-spell 2 :lesser-restoration 5)
                               (opt5e/paladin-spell 2 :zone-of-truth 5)
                               (opt5e/paladin-spell 3 :beacon-of-hope 9)
                               (opt5e/paladin-spell 3 :dispel-magic 9)
                               (opt5e/paladin-spell 4 :freedom-of-movement 13)
                               (opt5e/paladin-spell 4 :guardian-of-faith 13)
                               (opt5e/paladin-spell 5 :commune 17)
                               (opt5e/paladin-spell 5 :flame-strike 17)
                               (mod5e/action
                                {:name "Channel Divinity: Sacred Weapon"
                                 :page 86
                                 :duration units5e/minutes-1
                                 :summary (str "make a weapon magical, with a " (common/bonus-str (max 1 (?ability-bonuses ::char5e/cha))) " attack bonus and magical light (20 ft./20 ft.)")})
                               (mod5e/action
                                {:name "Channel Divinity: Turn the Unholy"
                                 :page 86
                                 :duration units5e/minutes-1
                                 :summary (str "each undead or fiend within 30 ft. must make a DC " (?spell-save-dc ::char5e/cha) " WIS save or be turned for 1 min.")})]
                   :levels {7 {:modifiers [(mod5e/dependent-trait
                                            {:name "Aura of Devotion"
                                             :page 86
                                             :summary (str "you and friendly creatures within " ?paladin-aura " ft. can't be charmed")})]}
                            20 {:modifiers [(mod5e/action
                                             {:name "Holy Nimbus"
                                              :page 86
                                              :frequency units5e/long-rests-1
                                              :duration units5e/minutes-1
                                              :summary "you emanate a bright light with 30 ft radius, an enemy that starts its turn there takes 10 radiant damage. You also have advantage on saves against spells cast by fiends and undead"})]}}
                   :traits [{:name "Purity of Spirit"
                             :level 15
                             :page 86
                             :summary "always under effects of protection from evil and good spell"}]}
                  {:name "Oath of the Ancients"
                   :modifiers [(opt5e/paladin-spell 1 :ensnaring-strike 3)
                               (opt5e/paladin-spell 1 :speak-with-animals 3)
                               (opt5e/paladin-spell 2 :misty-step 5)
                               (opt5e/paladin-spell 2 :moonbeam 5)
                               (opt5e/paladin-spell 3 :plant-growth 9)
                               (opt5e/paladin-spell 3 :protection-from-energy 9)
                               (opt5e/paladin-spell 4 :ice-storm 13)
                               (opt5e/paladin-spell 4 :stoneskin 13)
                               (opt5e/paladin-spell 5 :commune-with-nature 17)
                               (opt5e/paladin-spell 5 :tree-stride 17)
                               (mod5e/action
                                {:name "Channel Divinity: Nature's Wrath"
                                 :level 3
                                 :page 87
                                 :summary (str "restrain a creature with vines on a failed DC " (?spell-save-dc ::char5e/cha) " STR or DEX save. It makes the save every turn until freed.")})
                               (mod5e/action
                                {:name "Channel Divinity: Turn the Faithless"
                                 :level 3
                                 :page 87
                                 :duration units5e/minutes-1
                                 :summary "turn and reveal the true form of fey and fiends within 30 ft."})]
                   :levels {7 {:modifiers [(mod5e/dependent-trait
                                            {:name "Aura of Warding"
                                             :level 7
                                             :page 87
                                             :summary (str "you and friendly creatures within " ?paladin-aura " have resistance to spell damage")})]}}
                   :traits [{:name "Undying Sentinal"
                             :level 15
                             :page 87
                             :frequency units5e/long-rests-1
                             :summary "when you are reduced to 0 HP without being killed, you drop to 1 instead"}
                            {:name "Elder Champion"
                             :level 20
                             :page 87
                             :frequency units5e/long-rests-1
                             :duration units5e/minutes-1
                             :summary "undergo a tranformation where you 1) regain 10 HPs at start of your turns 2) can cast spells with casting time action as bonus action 3) enemies within 10 ft. have disadvantage on saves against your Channel Divinity and spells"}]}
                  {:name "Oath of Vengeance"
                   :modifiers [(opt5e/paladin-spell 1 :bane 3)
                               (opt5e/paladin-spell 1 :hunters-mark 3)
                               (opt5e/paladin-spell 2 :hold-person 5)
                               (opt5e/paladin-spell 2 :misty-step 5)
                               (opt5e/paladin-spell 3 :haste 9)
                               (opt5e/paladin-spell 3 :protection-from-energy 9)
                               (opt5e/paladin-spell 4 :banishment 13)
                               (opt5e/paladin-spell 4 :dimension-door 13)
                               (opt5e/paladin-spell 5 :hold-monster 17)
                               (opt5e/paladin-spell 5 :scrying 17)
                               (mod5e/action
                                {:name "Channel Divinity: Abjure Enemy"
                                 :level 3
                                 :page 88
                                 :duration units5e/minutes-1
                                 :summary (str "a creature of your choosing within 60 ft. must succeed on a DC " (?spell-save-dc ::char5e/cha) " WIS save or be frightened and have a speed of 0, speed is halved on successful save")})
                               (mod5e/bonus-action
                                {:name "Channel Divinity: Vow of Eternity"
                                 :level 3
                                 :page 88
                                 :duration units5e/minutes-1
                                 :summary "gain advantage on attacks against a creature"})]
                   :levels {15 {:modifiers [(mod5e/reaction
                                             {:name "Soul of Vengeance"
                                              :level 15
                                              :page 88
                                              :summary "when a creature under you Vow of Enmity attacks, make a melee weapon attack against it"})]}
                            20 {:modifiers [(mod5e/action
                                             {:name "Avenging Angel"
                                              :level 20
                                              :page 88
                                              :duration units5e/hours-1
                                              :frequency units5e/long-rests-1
                                              :summary (str "transform, gain flying speed of 60 ft., emanate a 30 ft. aura and creatures within it must succeed on a DC " (?spell-save-dc ::char5e/cha) " WIS or be frightened for 1 min and attacks against them have advantage")})]}}
                   :traits [{:name "Relentless Avenger"
                             :level 7
                             :page 88
                             :summary "when you hit with opportunity attack, you can also move up to half your speed after the attack without provoking opportunity attacks"}]}]})))

(defn favored-enemy-option [[enemy-type info]]
  (let [vec-info? (sequential? info)
        languages (if vec-info? info (:languages info))
        name (if vec-info? (common/kw-to-name enemy-type) (:name info))]
    (t/option-cfg
     {:name name
      :selections (if (> (count languages) 1)
                    [(opt5e/language-selection
                      (map
                       (fn [lang]
                         (or (opt5e/language-map lang) {:key lang :name (s/capitalize (common/kw-to-name lang))}))
                       languages)
                      1)])
      :modifiers (remove
                  nil?
                  [(if (= 1 (count languages))
                     (mod5e/language (first languages)))
                   (mod/set-mod ?ranger-favored-enemies enemy-type)])})))

(defn favored-enemy-selection [order]
  (t/selection-cfg
   {:name (str "Favored Enemy " order)
    :tags #{:class}
    :order 3
    :options [(t/option-cfg
               {:name "Type"
                :selections [(t/selection-cfg
                              {:name "Favored Enemy Type"
                               :tags #{:class}
                               :order 4
                               :multiselect? true
                               :ref [:class :ranger :favored-enemy-type]
                               :options (map
                                         favored-enemy-option
                                         opt5e/favored-enemy-types)})]})
              (t/option-cfg
               {:name "Two Humanoid Races"
                :selections [(t/selection-cfg
                              {:name "Favored Enemy Humanoid Race"
                               :tags #{:class}
                               :order 4
                               :min 2
                               :max 2
                               :multiselect? true
                               :ref [:class :ranger :favored-enemy-race]
                               :options (map
                                         favored-enemy-option
                                         opt5e/humanoid-enemies)})]})]}))

(defn favored-terrain-selection [order]
  (t/selection-cfg
   {:name "Favored Terrain"
    :tags #{:class}
    :order 5
    :ref [:class :ranger :favored-terrain]
    :multiselect? true
    :options (map
     (fn [terrain]
       (t/option-cfg
        {:name (common/kw-to-name terrain)
         :modifiers [(mod/set-mod ?ranger-favored-terrain terrain)]}))
     [:arctic :coast :desert :forest :grassland :mountain :swamp :underdark])}))

(def ranger-option
  (opt5e/class-option
   (merge
    opt5e/ranger-base-cfg
    {:hit-die 10
     :profs {:armor {:light false :medium false :shields false}
             :weapon {:simple false :martial false}
             :save {::char5e/str true ::char5e/dex true}
             :skill-options {:choose 3 :options opt5e/ranger-skills}
             :multiclass-skill-options {:choose 1 :options opt5e/ranger-skills}}
     :multiclass-prereqs [(t/option-prereq "Requires Wisdom 13 and Dexterity 13"
                                           (fn [c]
                                             (let [abilities @(subscribe [::char5e/abilities nil c])]
                                               (and (>= (::char5e/wis abilities) 13)
                                                    (>= (::char5e/dex abilities) 13)))))]
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
                    :ability ::char5e/wis}
     :armor-choices [{:name "Armor"
                      :options {:scale-mail 1
                                :leather 1}}]
     :equipment-choices [{:name "Equipment Pack"
                          :options {:dungeoneers-pack 1
                                    :explorers-pack 1}}]
     :weapons {:longbow 1}
     :equipment {:quiver 1
                 :arrow 20}
     :modifiers [(mod5e/dependent-trait
                  {:name "Favored Enemy"
                   :page 91
                   :summary (str "You have advantage on survival checks to track " (common/list-print (map #(common/kw-to-name % false) ?ranger-favored-enemies)) " creatures and on INT checks to recall info about them")})
                 (mod5e/dependent-trait
                  {:name "Natural Explorer"
                   :page 91
                   :summary (let [favored-terrain ?ranger-favored-terrain
                                  one-terrain? (= 1 (count favored-terrain))]
                              (str "your favored terrain " (if one-terrain? "type is" "types are") " " (if (seq favored-terrain) (common/list-print (map #(common/kw-to-name % false) ?ranger-favored-terrain)) "not selected") ". Related to the terrain type" (if (not one-terrain?) "s") ": 2X proficiency bonus for INT and WIS checks for which you are proficient, difficult terrain doesn't slow your group, always alert for danger, can move stealthily alone at normal pace, 2x food when foraging, while tracking learn exact number, size, and when they passed through"))})]
     :selections [(opt5e/new-starting-equipment-selection
                   :ranger
                   {:name "Melee Weapon"
                    :options [(t/option-cfg
                               {:name "Two Shortswords"
                                :modifiers [(mod5e/weapon :shortsword 2)]})
                              (t/option-cfg
                               {:name "Simple Melee Weapon"
                                :selections [(opt5e/new-starting-equipment-selection
                                              :ranger
                                              {:name "Simple Melee Weapon"
                                               :options (opt5e/simple-melee-weapon-options 1)
                                               :min 2
                                               :max 2})]})]})
                  (favored-enemy-selection 1)
                  (favored-terrain-selection 1)]
     :levels {2 {:selections [(opt5e/fighting-style-selection :ranger #{:archery :defense :dueling :two-weapon-fighting})]}
              3 {:modifiers [(mod5e/action
                              {:name "Primeval Awareness"
                               :level 3
                               :page 92
                               :summary (str "spend an X-level spell slot, for X minutes, you sense the types of creatures within 1 mile" (if (seq ?ranger-favored-terrain) (str "(6 if " (common/list-print (map #(common/kw-to-name % false) ?ranger-favored-terrain))) ")") )})]}
              5 {:modifiers [(mod5e/num-attacks 2)]}
              6 {:selections [(favored-enemy-selection 2)
                              (favored-terrain-selection 2)]}
              10 {:selections [(favored-terrain-selection 3)]}
              14 {:selections [(favored-enemy-selection 3)]}
              20 {:modifiers [(mod5e/dependent-trait
                               {:name "Foe Slayer"
                                :frequency units5e/turns-1
                                :level 20
                                :page 92
                                :summary (str "add " (common/bonus-str (?ability-bonuses ::char5e/wis)) " to an attack or damage roll") })]}}
     :traits [(lands-stride 8)
              {:name "Hide in Plain Sight"
               :level 10
               :page 92
               :summary "spend 1 minute camouflaging yourself to gain +10 to Stealth checks when you don't move"}
              {:name "Vanish"
               :level 14
               :page 92
               :summary "Hide action as a bonus action. You also can't be non-magically tracked"}
              {:name "Feral Senses"
               :level 18
               :page 92
               :summary "no disadvantage on attacks against creature you can't see, you know location of invisible creatures within 30 ft."}]
     :subclasses [{:name "Hunter"
                   :levels {3 {:selections [(t/selection-cfg
                                             {:name "Hunter's Prey"
                                              :tags #{:class}
                                              :options [(t/option-cfg
                                                         {:name "Colossus Slayer"
                                                          :modifiers [(mod5e/trait-cfg
                                                                       {:name "Colossus Slayer"
                                                                        :page 93
                                                                        :frequency units5e/turns-1
                                                                        :summary "deal an extra d8 damage when you hit a creature that is below its HP max with a weapon attack"})]})
                                                        (t/option-cfg
                                                         {:name "Giant Killer"
                                                          :modifiers [(mod5e/reaction
                                                                       {:name "Giant Killer"
                                                                        :page 93
                                                                        :frequency units5e/turns-1
                                                                        :summary "attack a Large or larger creature within 5 ft that misses an attack against you"})]})
                                                        (t/option-cfg
                                                         {:name "Horde Breaker"
                                                          :modifiers [(mod5e/trait-cfg
                                                                       {:name "Horde Breaker"
                                                                        :page 93
                                                                        :frequency units5e/turns-1
                                                                        :summary "when you attack one creature, attack another creature within 5 feet of it with the same action"})]})]})]}
                            7 {:selections [(t/selection-cfg
                                             {:name "Defensive Tactics"
                                              :tags #{:class}
                                              :options [(t/option-cfg
                                                         {:name "Escape the Horde"
                                                          :modifiers [(mod5e/trait-cfg
                                                                       {:name "Escape the Horde"
                                                                        :frequency units5e/turns-1
                                                                        :page 93})]})
                                                        (t/option-cfg
                                                         {:name "Multiattack Defense"
                                                          :modifiers [(mod5e/trait-cfg
                                                                       {:name "Multiattack Defense"
                                                                        :frequency units5e/turns-1
                                                                        :page 93})]})
                                                        (t/option-cfg
                                                         {:name "Steel Will"
                                                          :modifiers [(mod5e/saving-throw-advantage [:frightened])]})]})]}
                            11 {:selections [(t/selection-cfg
                                              {:name "Multiattack"
                                               :tags #{:class}
                                               :options [(t/option-cfg
                                                          {:name "Volley"
                                                           :modifiers [(mod5e/action
                                                                        {:name "Volley"
                                                                         :page 93
                                                                         :summary "make a ranged attack against any creatures within a 10 ft of a point" 
                                                                         })]})
                                                         (t/option-cfg
                                                          {:name "Whirlwind Attack"
                                                           :modifiers [(mod5e/action
                                                                        {:name "Whirlwind Attack"
                                                                         :page 93
                                                                         :summary "melee attack against any creatures within 5 ft. of you"})]})]})]}
                            15 {:selections [(t/selection-cfg
                                              {:name "Superior Hunter's Defense"
                                               :tags #{:class}
                                               :options [(t/option-cfg
                                                          {:name "Evasion"
                                                           :modifiers [(mod5e/trait-cfg
                                                                        (opt5e/evasion 15 93))]})
                                                         (t/option-cfg
                                                          {:name "Stand Against the Tide"
                                                           :modifiers  [(mod5e/reaction
                                                                         {:name "Stand Against the Tide"
                                                                          :page 93
                                                                          :summary "force a creature to repeat its attack on another creature when it misses you"
                                                                          })]})
                                                         (t/option-cfg
                                                          {:name "Uncanny Dodge"
                                                           :modifiers [(opt5e/uncanny-dodge-modifier 93)]})]})]}}}
                  {:name "Beast Master"
                   :selections [(t/selection-cfg
                                 {:name "Ranger's Companion"
                                  :tags #{:class}
                                  :options (map
                                            (fn [monster-name]
                                              (t/option-cfg
                                               {:name monster-name
                                                :modifiers [(mod5e/action
                                                             {:name "Ranger's Companion"
                                                              :page 93
                                                              :summary (str "You have a " monster-name " as your companion, you can command it to Attack, Dash, Disengage, Dodge, or Help")})]}))
                                            ["Stirge" "Baboon" "Bat" "Badger" "Blood Hawk" "Boar" "Cat" "Crab" "Deer" "Eagle" "Flying Snake" "Frog" "Giant Badger" "Giant Centipede" "Giant Crab" "Giant Fire Beetle" "Giant Frog" "Giant Poisonous Snake" "Giant Rat" "Giant Wolf Spider" "Goat" "Hawk" "Hyena" "Jackal" "Lizard" "Mastiff" "Mule" "Octopus" "Panther" "Owl" "Poisonous Snake" "Pony" "Quipper" "Rat" "Raven" "Scorpion" "Sea Horse" "Spider" "Vulture" "Weasel" "Wolf"])})]
                   :levels {7 {:modifiers [(mod5e/bonus-action
                                            {:name "Exceptional Training"
                                             :page 93
                                             :level 7
                                             :summary "when your companion doesn't attack, you can command it to take the Dash, Disengage, Dodge, or Help action"})]}}
                   :traits [{:name "Bestial Fury"
                             :level 11
                             :page 93
                             :summary "your companion attacks twice when it takes the Attack action"}
                            {:name "Share Spells"
                             :level 15
                             :page 93
                             :summary "when you target yourself with a spell you can also affect your companion if within 30 ft."}]}]})))

(def rogue-skills {:acrobatics true :athletics true :deception true :insight true :intimidation true :investigation true :perception true :performance true :persuasion true :sleight-of-hand true :stealth true})

(def rogue-option
  (opt5e/class-option
   {:name "Rogue",
    :hit-die 8
    :ability-increase-levels [4 8 10 12 16 19]
    :expertise true
    :profs {:armor {:light false}
            :weapon {:simple true :crossbow-hand true :longsword true :rapier true :shortsword true}
            :save {::char5e/dex true ::char5e/int true}
            :tool {:thieves-tools false}
            :skill-options {:order 0 :choose 4 :options rogue-skills}
            :multiclass-skill-options {:order 0 :choose 1 :options rogue-skills}}
    :multiclass-prereqs [(opt5e/ability-prereq ::char5e/dex 13)]
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
    :modifiers [(mod5e/dependent-trait
                 {:name "Sneak Attack"
                  :page 96
                  :frequency units5e/turns-1
                  :summary (str (common/round-up (/ (?class-level :rogue) 2)) "d6 extra damage on attack where you have advantage or another enemy of creature is within 5 ft.")
})]
    :levels {2 {:modifiers [(mod5e/bonus-action
                             {:level 2
                              :name "Cunning Action"
                              :page 96
                              :frequency units5e/turns-1
                              :summary "Dash, Disengage or Hide"
})]}
             5 {:modifiers [(opt5e/uncanny-dodge-modifier 96)]}
             6 {:selections [(assoc
                              opt5e/rogue-expertise-selection
                              ::t/order
                              1)]}
             15 {:modifiers [(mod5e/saving-throws nil ::char5e/wis)]}}
    :selections [(opt5e/new-starting-equipment-selection
                  :rogue
                  {:name "Additional Weapon"
                   :options [(t/option-cfg
                              {:name "Shortbow, Quiver, 20 Arrows"
                               :modifiers [(mod5e/weapon :shortbow 5)
                                           (mod5e/equipment :quiver 1)
                                           (mod5e/equipment :arrow 20)]})
                             (t/option-cfg
                              {:name "Shortsword"
                               :modifiers [(mod5e/weapon :shortsword 1)]})]})
                 (assoc
                  opt5e/rogue-expertise-selection
                  ::t/order
                  1)]
    :traits [{:name "Thieves' Cant"
              :page 96
              :summary "convey secret messages hidden in normal conversation"
}
             (opt5e/evasion 7 96)
             {:level 11
              :name "Reliable Talent"
              :page 96
              :summary "when you make an ability check with proficiency, treat a roll less than 10 as a 10"
}
             {:level 14
              :name "Blindsense"
              :page 96
              :summary "know location of hidden or invisible creatures within 10 ft."
}
             {:level 18
              :name "Elusive"
              :page 96
              :summary "attack rolls only have disadvantage on you if you are incapacitated"
}
             {:level 20
              :name "Stroke of Luck"
              :page 97
              :frequency units5e/rests-1
              :summary "turn missed attack into a hit or a failed ability check roll as 20"
}]
    :subclass-level 3
    :subclass-title "Roguish Archetype"
    :subclasses [{:name "Thief"
                  :modifiers [(mod5e/bonus-action
                               {:level 3
                                :name "Fast Hands"
                                :page 96
                                :summary "use your Cunning Action to make Sleight of Hand checks, use thieves' tools, or take Use and Object action"
})
                              (mod5e/dependent-trait
                               {:level 3
                                :name "Second-Story Work"
                                :page 97
                                :summary (str "climbing costs no extra movement, your running jump distance increases by " (?ability-bonuses ::char5e/dex) " ft.")
})]
                  :traits [{:level 9
                            :name "Supreme Sneak"
                            :page 97
                            :summary "advantage on Stealth checks if you move no more than half your speed"}
                           {:level 13
                            :name "Use Magic Device"
                            :page 97
                            :summary "ignore race, class, level requirements to use magic items"}
                           {:level 17
                            :name "Thief's Reflexes"
                            :page 97
                            :summary "when not surprised, take 2 turns in first round of combat, one at your normal initiative and the next at your initiative minus 10"}]}
                 {:name "Assassin"
                  :profs {:tool {:disguise-kit true :poisoners-kit true}}
                  :levels {17 {:modifiers [(mod5e/dependent-trait
                                            {:name "Death Strike"
                                             :level 17
                                             :page 97
                                             :summary (str "double damage against a surpised creature if it fails a DC " (?spell-save-dc ::char5e/dex) " CON save")})]}}
                  :traits [{:name "Assassinate"
                            :level 3
                            :page 97
                            :summary "advantage on attack against creatures that haven't taken a turn yet. Hits against surprised creatures are critical"}
                           {:name "Infiltration Expertise"
                            :level 9
                            :page 97
                            :summary "spend 25 gp and 7 days to establish a false identity"}
                           {:name "Impostor"
                            :level 13
                            :page 97
                            :summary "accurately mimic the behavior, speech, and writing of another person"}]}
                 {:name "Arcane Trickster"
                  :spellcasting {:level-factor 3}
                  :modifiers [(mod5e/spells-known 0 :mage-hand ::char5e/int "Arcane Trickster")]
                  :levels {3 {:selections [(arcane-trickster-cantrip 2)
                                           (arcane-trickster-spell-selection 2 [1])
                                           (arcane-trickster-any-spell-selection 1 [1])]}
                           4 {:selections [(arcane-trickster-spell-selection 1 [1])]}
                           7 {:selections [(arcane-trickster-spell-selection 1 [1 2])]}
                           8 {:selections [(arcane-trickster-any-spell-selection 1 [1 2])]}
                           10 {:selections [(arcane-trickster-cantrip 1)
                                            (arcane-trickster-spell-selection 1 [1 2])]}
                           11 {:selections [(arcane-trickster-spell-selection 1 [1 2])]}
                           13 {:selections [(arcane-trickster-spell-selection 1 [1 2 3])]
                               :modifiers [(mod5e/bonus-action
                                            {:name "Versatile Trickster"
                                             :level 13
                                             :page 98
                                             :summary "use mage hand to gain advantage on attack rolls against a creature within 5 ft. of the hand"})]}
                           14 {:selections [(arcane-trickster-any-spell-selection 1 [1 2 3])]}
                           16 {:selections [(arcane-trickster-spell-selection 1 [1 2 3])]}
                           17 {:modifiers [(mod5e/reaction
                                            {:name "Spell Thief"
                                             :level 17
                                             :page 98
                                             :summary (str "steal a spell for 8 hours if it is cast on you and the spellcaster fails a DC " (?spell-save-dc ::char5e/int) " save with its spellcasting ability")})]}
                           19 {:selections [(arcane-trickster-spell-selection 1 [1 2 3 4])]}
                           20 {:selections [(arcane-trickster-any-spell-selection 1 [1 2 3 4])]}}
                  :traits [{:name "Mage Hand Legerdemain"
                            :level 3
                            :page 98
                            :summary "when you cast mage hand, you can make it invisible and perform Sleight of Hand tasks"}
                           {:name "Magical Ambush"
                            :level 9
                            :page 98
                            :summary "creatures have disadvantage on saves against your spells (only on the turn you cast them) if you are hidden from them"}]}]}))

(defn metamagic-selection [num]
  (t/selection-cfg
   {:name "Metamagic"
    :tags #{:class}
    :min num
    :max num
    :ref [:class :sorcerer :metamagic]
    :options [(t/option-cfg
               {:name "Careful Spell"
                :modifiers [(mod5e/dependent-trait
                             {:name "Careful Spell"
                              :page 102
                              :class-key :sorcerer
                              :summary (str "When you cast a spell that requires a save, spend 1 sorcery pt. to allow up to " (?ability-bonuses ::char5e/cha) " creatures to automatically succeed")})]})
              (t/option-cfg
               {:name "Distant Spell"
                :modifiers [(mod5e/trait-cfg
                             {:name "Distant Spell"
                              :page 102
                              :summary "spend 1 sorcery pt. double the range of a spell with range 5 ft. or greater or make the range of a touch spell 30 ft."})]})
              (t/option-cfg
               {:name "Empowered Spell"
                :modifiers [(mod5e/dependent-trait
                             {:name "Empowered Spell"
                              :page 102
                              :summary (str "spend 1 sorcery pt. to reroll up to " (?ability-bonuses ::char5e/cha) " spell damage dice")})]})
              (t/option-cfg
               {:name "Extended Spell"
                :modifiers [(mod5e/trait-cfg
                             {:name "Extended Spell"
                              :page 102
                              :summary "spend 1 sorcery pt. to double the duration of a spell to a max 24 hrs."})]})
              (t/option-cfg
               {:name "Heightened Spell"
                :modifiers [(mod5e/trait-cfg
                             {:name "Heightened Spell"
                              :page 102
                              :summary "when you cast a spell with a save to resist it's effects, spend 3 sorcery pts. to give one target disadvantage on its first save against it"})]})
              (t/option-cfg
               {:name "Quickened Spell"
                :modifiers [(mod5e/trait-cfg
                             {:name "Quickened Spell"
                              :page 102
                              :summary "spend 2 sorcery pts. to convert a casting of a spell with 1 action casting time to 1 bonus-action"})]})
              (t/option-cfg
               {:name "Subtle Spell"
                :modifiers [(mod5e/trait-cfg
                             {:name "Subtle Spell"
                              :page 102
                              :summary "spend 1 sorcery pt. to cast a spell without somatic or verbal components"})]})
              (t/option-cfg
               {:name "Twinned Spell"
                :modifiers [(mod5e/trait-cfg
                             {:name "Twinned Spell"
                              :page 102
                              :summary "spend X sorcery pts. (min 1) to target two creatures with a single target spell, where X is the spell level"})]})]}))

(def sorcerer-option
  (opt5e/class-option
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
                   :ability ::char5e/cha}
    :multiclass-prereqs [(opt5e/ability-prereq ::char5e/cha 13)]
    :spellcaster true
    :hit-die 6
    :ability-increase-levels [4 8 12 16 19]
    :profs {:weapon {:dagger true :dart true :sling true :quarterstaff true :crossbow-light true}
            :save {::char5e/con true ::char5e/cha true}
            :skill-options {:choose 2 :options {:arcana true :deception true :insight true :intimidation true :persuasion true :religion true}}}
    :selections [(opt5e/new-starting-equipment-selection
                  :sorcerer
                  {:name "Weapon"
                   :options [(t/option-cfg
                              {:name "Light Crossbow"
                               :modifiers [(mod5e/weapon :crossbow-light 1)
                                           (mod5e/equipment :crossbow-bolt 20)]})
                             (t/option-cfg
                              {:name "Simple Weapon"
                               :selections [(opt5e/new-starting-equipment-selection
                                             :sorcerer
                                             {:name "Simple Weapon"
                                              :options (opt5e/simple-weapon-options 1)
                                              :min 1
                                              :max 1})]})]})]
    :levels {2 {:modifiers [(mod5e/dependent-trait
                             {:name "Sorcery Points"
                              :level 2
                              :page 101
                              :summary (str "You have " (?class-level :sorcerer) " sorcery points")})
                            (mod5e/bonus-action
                             {:name "Flexible Casting"
                              :level 2
                              :page 101
                              :summary "you can convert sorcery points into spell slots (level - point cost: 1st - 2, 2nd - 3, 3rd - 5, 4th - 6, 5th - 7). You can also convert spell slots into sorcery points equal to the slot's level"})]}
             3 {:selections [(metamagic-selection 2)]}
             10 {:selections [(metamagic-selection 1)]}
             17 {:selections [(metamagic-selection 1)]}}
    :equipment-choices [{:name "Equipment Pack"
                         :options {:dungeoneers-pack 1
                                   :explorers-pack 1}}
                        {:name "Spellcasting Equipment"
                         :options {:component-pouch 1
                                   :arcane-focus 1}}]
    :weapons {:dagger 2}
    :subclass-title "Sorcerous Origin"
    :subclass-level 1
    :subclasses [{:name "Draconic Bloodline"
                  :modifiers [(mod/modifier ?hit-point-level-bonus (+ 1 ?hit-point-level-bonus))
                              (mod/modifier ?natural-ac-bonus 3)
                              (mod5e/language :draconic)]
                  :selections [(t/selection-cfg
                                {:name "Draconic Ancestry Type"
                                 :tags #{:class}
                                 :options (map
                                           (fn [{:keys [name] :as ancestry}]
                                             (t/option-cfg
                                              {:name name
                                               :modifiers [(mod/modifier ?sorcerer-draconic-ancestry ancestry)]}))
                                           draconic-ancestries)})]
                  :traits [{:name "Draconic Resilience"
                            :page 102
                            :summary "+1 HP/level, unarmored AC 13 + DEX modifier"}]
                  :levels {6 {:modifiers [(mod5e/dependent-trait
                                           {:name "Elemental Affinity"
                                            :page 102
                                            :summary (str "Add CHA mod to one damage roll of a spell that deals "
                                                          (if ?sorcerer-draconic-ancestry
                                                            (str (common/safe-name
                                                                  (get-in
                                                                   ?sorcerer-draconic-ancestry
                                                                   [:breath-weapon :damage-type]))
                                                                 " damage")
                                                            "damage of type associated with your draconic ancestry")
                                                          ", you may also spend 1 sorcery pt. to gain resistance to that damage type for an hr.")})]}
                           14 {:modifiers [(mod5e/bonus-action
                                            {:name "Dragon Wings"
                                             :page 103
                                             :summary "Sprout wings and gain flying speed equal to land speed"})]}
                           18 {:modifiers [(mod5e/action
                                            {:name "Draconic Presence"
                                             :page 103
                                             :summary (str "Spend 5 sorcery pts. and create an aura that causes hostile creatures that start their turn within it to be charmed or afraid if they fail a DC " (?spell-save-dc ::char5e/cha) " Wisdom save.")})]}}}
                 {:name "Wild Magic"
                  :levels {6 {:modifiers [(mod5e/reaction
                                           {:name "Bend Luck"
                                            :level 6
                                            :page 103
                                            :summary "spend 2 sorcery pts. to add or subtract 1d4 from a creature's d20 roll"})]}}
                  :traits [{:name "Wild Magic Surge"
                            :level 1
                            :summary "Do a d20 check when casting sorcerer spells, on a 1 roll on the Wild Magic Surge table"
                            :page 103}
                           {:name "Tides of Chaos"
                            :level 1
                            :summary "Gain advantage on a roll"
                            :page 103
                            :frequency units5e/long-rests-1}
                           {:name "Controlled Chaos"
                            :level 14
                            :page 103
                            :summary "When rolling on Wild Magic Surge table, roll twice and use either roll"}
                           {:name "Spell Bombardment"
                            :level 18
                            :page 103
                            :summary "When you roll a die for spell damage, roll max rolls an additional time"
                            :frequency units5e/turns-1}]}]}))

(defn spell-school-savant [school page]
  {:level 2
   :name (str (s/capitalize school) " Savant")
   :page page
   :description (str "time and money to copy an " school " spell is halved")})

(defn spell-in-spells-known? [known level spell-key]
  (and known (some #(= spell-key (:key %)) (known level))))

(defn spell-mastery-selection [level]
  (t/selection-cfg
   {:name (str "Spell Mastery Level " level " Spell")
    :tags #{:spells}
    :options (map
              (fn [spell-kw]
                (let [{:keys [name] :as spell} (spells/spell-map spell-kw)]
                  (t/option-cfg
                   {:name name
                    :help (opt5e/spell-help spell)
                    :modifiers [(mod/set-mod ?spell-mastery name)]
                    :prereqs [(t/option-prereq
                               nil
                               (fn [c]
                                 (let [spells-known @(subscribe [::char5e/spells-known nil c])]
                                   (get-in spells-known [level ["Wizard" spell-kw]])))
                               true)]})))
              (get-in sl/spell-lists [:wizard level]))}))

(defn signature-spells-selection []
  (t/selection-cfg
   {:name "Signature Spells"
    :tags #{:spells}
    :min 2
    :max 2
    :options (map
              (fn [spell-kw]
                (let [{:keys [name] :as spell} (spells/spell-map spell-kw)]
                  (t/option-cfg
                   {:name name
                    :help (opt5e/spell-help spell)
                    :modifiers [(mod/set-mod ?signature-spells name)]
                    :prereqs [(t/option-prereq
                               nil
                               (fn [c]
                                 (let [spells-known @(subscribe [::char5e/spells-known nil c])]
                                   (get-in spells-known [3 ["Wizard" spell-kw]])))
                               true)]})))
              (get-in sl/spell-lists [:wizard 3]))}))

(def wizard-option
  (opt5e/class-option
   {:name "Wizard",
    :spellcasting {:level-factor 1
                   :cantrips-known {1 3 4 1 10 1}
                   :known-mode :acquire
                   :spells-known (zipmap (range 1 21) (cons 6 (repeat 2)))
                   :ability ::char5e/int
                   :prepares-spells? true}
    :spellcaster true
    :multiclass-prereqs [(opt5e/ability-prereq ::char5e/int 13)]
    :hit-die 6
    :ability-increase-levels [4 8 12 16 19]
    :equipment-choices [{:name "Equipment Pack"
                         :options {:scholars-pack 1
                                   :explorers-pack 1}}
                        {:name "Spellcasting Equipment"
                         :options {:component-pouch 1
                                   :arcane-focus 1}}]
    :equipment {:spellbook 1}
    :profs {:weapon {:dagger true :dart true :sling true :quarterstaff true :crossbow-light true}
            :save {::char5e/int true ::char5e/wis true}
            :skill-options {:choose 2 :options {:arcana true :history true :insight true :investigation true :medicine true :religion true}}}
    :modifiers [(mod5e/dependent-trait
                 {:name "Arcane Recovery"
                  :page 115
                  :frequency units5e/days-1
                  :summary (str "When you finish a short rest, regain spell slots totalling no more than " (int (/ ?wizard-level 2)) ", and each must be 5th level or lower.")})]
    :levels {18 {:selections [(spell-mastery-selection 1)
                              (spell-mastery-selection 2)]
                 :modifiers [(mod5e/dependent-trait
                              {:name "Spell Mastery"
                               :page 115
                               :summary (str (if (seq ?spell-mastery)
                                               (str "Cast " (common/list-print ?spell-mastery))
                                               "Choose a 1st and 2nd level spell, cast those")
                                             " at lowest level without expending a slot if you have them prepared")})]}
             20 {:selections [(signature-spells-selection)]
                 :modifiers [(mod5e/dependent-trait
                              {:name "Signature Spells"
                               :page 115
                               :summary (str (if (seq ?signature-spells)
                                               (str "Your signature spells are " (common/list-print ?signature-spells))
                                               "Choose two 3rd level spells")
                                             ", you always have them prepared and can cast them once without expending a slot")})]}}
    :subclass-level 2
    :subclass-title "Arcane Tradition"
    :subclasses [{:name "School of Evocation"
                  :levels {10 {:modifiers [(mod5e/dependent-trait
                                            {:level 10
                                             :name "Empowered Evocation"
                                             :page 117
                                             :summary (str "add your INT mod (" (?ability-bonuses ::char5e/int) ") to one damage roll of evocation spell you cast")})]}}
                  :traits [(spell-school-savant "evocation" 117)
                           {:level 2
                            :name "Sculpt Spells"
                            :page 117
                            :summary "can choose up to 1 + spell's level creatures to automatically save against your evocation spells and take no damage"}
                           {:level 6
                            :name "Potent Cantrip"
                            :page 117
                            :summary "creature take half damage on sucessful saves against your cantrips"}
                           {:level 14
                            :name "Overchannel"
                            :page 118
                            :summary "deal max damage with evocation spells 1st-5th level. You take necrotic damage if you use this feature more than once per long rest"}]}
                 {:name "School of Abjuration"
                  :modifiers [(mod5e/dependent-trait
                               {:name "Arcane Ward"
                                :page 115
                                :summary (str "magical ward with HP max " (+ (?class-level :wizard) (?ability-bonuses ::char5e/int)) ", casting X-th level abjuration spells restores 2X HPs to it")})]
                  :levels {6 {:modifiers [(mod5e/reaction
                                           {:name "Projected Ward"
                                            :page 115
                                            :range units5e/ft-30
                                            :summary "shield a creature using your ward"})]}
                           10 {:modifiers [(mod5e/dependent-trait
                                            {:name "Improved Abjuration"
                                             :page 115
                                             :summary (str "add your proficiency bonus (" (common/bonus-str ?prof-bonus) ") to ability checks required by abjuration spells")})]}
                           14 {:modifiers [(mod5e/saving-throw-advantage [:spells])]}}
                  :traits [(spell-school-savant "abjuration" 115)
                           {:name "Spell Resistance"
                            :level 14
                            :page 116
                            :summary "advantage on saves against spells, resistance to damage from spells"}]}
                 {:name "School of Conjuration"
                  :levels {6 {:modifiers [(mod5e/action
                                           {:name "Benign Transposition"
                                            :page 116
                                            :range units5e/ft-30
                                            :summary "Teleport to unoccupied space or swap spaces with willing Small or Medium creature"})]}}
                  :traits [(spell-school-savant "conjuration" 116)
                           {:name "Minor Conjuration"
                            :level 2
                            :page 116
                            :duration units5e/hours-1
                            :summary "conjure an inanimate object 3 ft per side or less and 15 lbs or less, it radiates dim light to 5 ft."}
                           {:name "Focused Conjuration"
                            :level 10
                            :page 116
                            :summary "concentration on conjuration spells cannot be broken by taking damage"}
                           {:name "Durable Summons"
                            :level 14
                            :summary "creatures you conjure have 30 temp hit points"}]}
                 {:name "School of Divination"
                  :levels {10 {:modifiers [(mod5e/action
                                            {:name "The Third Eye"
                                             :level 10
                                             :page 117
                                             :frequency units5e/long-rests-1
                                             :summary "gain one: 1) darkvision 60 ft., 2) see etherial plane 60 ft. 3) read any language 4) see invisible within 10 ft."})]}}
                  :traits [(spell-school-savant "divination" 116)
                           {:name "Portent"
                            :level 2
                            :frequency units5e/long-rests-1
                            :summary "roll 2 d20s after long rest, can replace rolls you or a creature you can see make with these"}
                           {:name "Expert Divination"
                            :level 6
                            :page 117
                            :summary "when you cast divination spell 2nd level or higher, regain a spell slot of lower level (max 5th level)"}
                           {:name "Greater Portent"
                            :level 14
                            :page 117
                            :summary "roll 3 d20s for your Portent feature"}]}
                 {:name "School of Enchantment"
                  :levels {2 {:modifiers [(mod5e/action
                                           {:name "Hypnotic Gaze"
                                            :level 2
                                            :page 117
                                            :range units5e/ft-5
                                            :summary (str "charm a creature until end of your next turn unless it succeeds on a DC " (?spell-save-dc ::char5e/int) " WIS save, it is incapacitated and dazed")})]}
                           6 {:modifiers [(mod5e/reaction
                                           {:name "Instinctive Charm"
                                            :page 117
                                            :range units5e/ft-30
                                            :frequency units5e/long-rests-1
                                            :summary (str "redirect a creature's attack against you to the creature closest to it, not including you, if it fails a DC " (?spell-save-dc ::char5e/int) " WIS save")})]}
                           14 {:modifiers [(mod5e/dependent-trait
                                            {:name "Alter Memories"
                                             :level 14
                                             :page 117
                                             :summary (str "make a creature unaware of your charm on it, can also use your action to erase up to " (inc (?ability-bonuses ::char5e/cha)) " hours from it's memory if it fails a DC " (?spell-save-dc ::char5e/int) " INT check")})]}}
                  :traits [(spell-school-savant "enchantment" 117)
                           {:name "Split Enchantment"
                            :level 10
                            :page 117
                            :summary "target 2 creatures with an enchantment spell that normally targets 1"}]}
                 {:name "School of Illusion"
                  :modifiers [(mod5e/spells-known-cfg 0
                                                      {:key :minor-illusion
                                                       :ability ::char5e/int
                                                       :class "Wizard"
                                                       :illusionist-cantrip? true}
                                                      0
                                                      [(not (spell-in-spells-known? ?spells-known 0 :minor-illusion))])]
                  :selections [(t/selection-cfg
                                {:name "Illusionist Cantrip"
                                 :order 0
                                 :tags (opt5e/spell-tags :wizard 0)
                                 :options (opt5e/spell-options (get-in sl/spell-lists [:wizard 0]) ::char5e/int "Wizard")
                                 :prereq-fn (fn [c]
                                              (let [spells-known @(subscribe [::char5e/spells-known nil c])
                                                    passes? (or (nil? spells-known)
                                                                (some
                                                                 (fn [s]
                                                                   (and (= :minor-illusion (:key s))
                                                                        (not (:illusionist-cantrip? s))))
                                                                 (vals (spells-known 0))))]
                                                passes?))})]
                  :levels {6 {:modifiers [(mod5e/action
                                           {:name "Malleable Illusions"
                                            :page 118
                                            :summary "change the nature of an illusion you cast"})]}
                           10 {:modifiers [(mod5e/reaction
                                            {:name "Illusory Self"
                                             :page 118
                                             :frequency units5e/rests-1
                                             :summary "attacker hits an illusion of you instead of you"})]}
                           14 {:modifiers [(mod5e/bonus-action
                                            {:name "Illusory Reality"
                                             :page 119
                                             :summary "make an illusory object real for 1 minute"})]}}
                  :traits [(spell-school-savant "illusion" 118)
                           {:name "Improved Minor Illusion"
                            :page 118
                            :summary "can create a sound and an image with the same casting of minor illusion"}]}
                 {:name "School of Necromancy"
                  :levels {10 {:modifiers [(mod5e/damage-resistance :necrotic)]}
                           14 {:modifiers [(mod5e/action
                                            {:name "Command Undead"
                                             :page 119
                                             :summary (str "bring undead under your control unless it succeeds on a DC " (?spell-save-dc ::char5e/int) " CHA save")})]}}
                  :traits [(spell-school-savant "necromancy" 118)
                           {:name "Grim Harvest"
                            :level 2
                            :page 118
                            :frequency units5e/turns-1
                            :summary "when you kill a creature with a spell, you regain HPs equal to 2X the spell level or 3X the spell level for necromancy spells"}
                           {:name "Undead Thralls"
                            :page 119
                            :level 6
                            :summary (str "target one additional corpse or pile of bones for animate dead; whenever you create an undead it's HP max is increased by your wizard level amount and it adds your prof bonus to weapon damage rolls")}
                           {:name "Inured to Undeath"
                            :level 10
                            :summary "resistant to necrotic damage; your HP max cannot be reduced"}]}
                 {:name "School of Transmutation"
                  :modifiers [(mod5e/spells-known 4 :polymorph ::char5e/int "Wizard")]
                  :levels {14 {:modifiers [(mod5e/action
                                            {:name "Master Transmuter"
                                             :page 119
                                             :summary "expend your transmuter's stone for 1 effect: 1) convert a non-magical object into another non-magical object, 2) remove curses, poisons, diseases and damage from a creature 3) raise dead 4) reduce a willing creature's apparent age by 3d10"})]}}
                  :traits [(spell-school-savant "transmutation" 119)
                           {:name "Minor Alchemy"
                            :level 2
                            :page 119
                            :duration units5e/hours-1
                            :summary "transform an object of one substance to another substance"}
                           {:name "Transmuter's Stone"
                            :level 6
                            :page 119
                            :summary "create a stone with 1 benefit: 1) darkvision 60 ft. 2) +10 speed 3) prof in CON saves 4) resistance to acid, fire, cold, lightning, or thunder damage"}
                           {:name "Shapechanger"
                            :level 10
                            :page 119
                            :frequency units5e/long-rests-1
                            :summary "cast polymorph without a spell slot to turn into a CR 1 or less beast"}]}]}))

(def pact-boon-options
  [(t/option-cfg
    {:name "Pact of the Chain"
     :modifiers [(mod5e/spells-known 1 :find-familiar ::char5e/cha "Warlock")
                 (mod5e/trait-cfg
                  {:name opt5e/pact-of-the-chain-name
                   :page 107
                   :summary "Can cast find familiar as a ritual, use your attack action to give your familiar an attack as a reaction"})]})
   (t/option-cfg
    {:name "Pact of the Blade"
     :modifiers [(mod5e/trait-cfg
                  {:name opt5e/pact-of-the-blade-name
                   :page 107
                   :summary "summon a magical weapon"})]})
   (t/option-cfg
    {:name "Pact of the Tome"
     :selections [(t/selection-cfg
                   {:name "Book of Shadows Cantrips"
                    :tags #{:spells}
                    :min 3
                    :max 3
                    :options (opt5e/spell-options (into
                                                   #{}
                                                   (mapcat
                                                    (fn [[cls-kw spells-by-level]]
                                                      (spells-by-level 0))
                                                    sl/spell-lists))
                                                  ::char5e/cha
                                                  "Warlock"
                                                  false
                                                  "uses Book of Shadows")})]
     :modifiers [(mod5e/trait-cfg
                  {:name opt5e/pact-of-the-tome-name
                   :page 108
                   :summary "you have a spellbook with 3 extra cantrips"})]})])


(def eldritch-invocation-options
  [(t/option-cfg
    {:name "Agonizing Blast"
     :modifiers [(mod5e/dependent-trait
                  {:name "Eldritch Invocation: Agonizing Blast"
                   :page 110
                   :summary (str "add " (?ability-bonuses ::char5e/cha) " to eldritch blast spell damage")})]
     :prereqs [opt5e/has-eldritch-blast-prereq]})
   (t/option-cfg
    {:name "Armor of Shadows"
     :modifiers [(mod5e/trait-cfg
                  {:name "Eldritch Invocation: Armor of Shadows"
                   :page 110
                   :summary "cast mage armor on yourself at will"})
                 (mod5e/spells-known 1 :mage-armor ::char5e/cha "Warlock" 0 "at will")]})
   (t/option-cfg
    {:name "Ascendant Step"
     :modifiers [(mod5e/trait-cfg
                  {:name "Eldritch Invocation: Ascendant Step"
                   :page 110
                   :summary "cast levitate on yourself at will"})
                 (mod5e/spells-known 2 :levitate ::char5e/cha "Warlock" 0 "at will")]
     :prereqs [(opt5e/total-levels-option-prereq 9 :warlock)]})
   (t/option-cfg
    {:name "Beast Speech"
     :modifiers [(mod5e/trait-cfg
                  {:name "Eldritch Invocation: Beast Speech"
                   :page 110
                   :summary "can cast speak with animals at will"})
                 (mod5e/spells-known 1 :speak-with-animals ::char5e/cha "Warlock" 0 "at will")]})
   (t/option-cfg
    {:name "Beguiling Influence"
     :modifiers [(mod5e/trait-cfg
                  {:name "Eldritch Invocation: Beguiling Influence"
                   :page 110
                   :summary "proficiency in deception and persuasion"})
                 (mod5e/skill-proficiency :deception)
                 (mod5e/skill-proficiency :persuasion)]})
   (t/option-cfg
    {:name "Bewitching Whispers"
     :modifiers [(mod5e/trait-cfg
                  {:name "Eldritch Invocation: Bewitching Whispers"
                   :page 110
                   :frequency units5e/long-rests-1
                   :summary "cast compulsion once using warlock spell slot"})
                 (mod5e/spells-known 4 :compulsion ::char5e/cha "Warlock" 0 "once per long rest")]})
   (t/option-cfg
    {:name "Book of Ancient Secrets"
     :modifiers [(mod5e/trait-cfg
                  {:name "Eldritch Invocation: Book of Ancient Secrets"
                   :page 110
                   :summary "inscribe and cast rituals"})]
     :selections [(t/selection-cfg
                   {:name "Book of Ancient Secrets Rituals"
                    :tags #{:spells}
                    :multiselect? true
                    :options (opt5e/spell-options
                              (map
                               (fn [s] (or (:key s)
                                           (common/name-to-kw (:name s))))
                               (filter
                                (fn [s] (and (= 1 (:level s)) (opt5e/ritual-spell? s)))
                                spells/spells))
                              ::char5e/cha
                              "Warlock"
                              false
                              "Book of Ancient Secrets Ritual")
                    :min 2
                    :max 2})]
     :prereqs [opt5e/pact-of-the-tome-prereq]})
   (t/option-cfg
    {:name "Chains of Carceri"
     :modifiers [(mod5e/trait-cfg
                  {:name "Eldritch Invocation: Chains of Carceri"
                   :page 110
                   :frequency units5e/long-rests-1
                   :summary "cast hold monster at will on celestials, fiends, or elementals"})
                 (mod5e/spells-known 5 :hold-monster ::char5e/cha "Warlock" 0 "at will")]
     :prereqs [opt5e/pact-of-the-chain-prereq
               (opt5e/total-levels-option-prereq 15 :warlock)]})
   (t/option-cfg
    {:name "Devil's Sight"
     :modifiers [(mod5e/darkvision 120 1)
                 (mod5e/trait-cfg
                  {:name "Eldritch Invocation: Devil's Sight"
                   :page 110
                   :range units5e/ft-120
                   :summary "see normally in magical and nonmagical darkness"})]})
   (t/option-cfg
    {:name "Dreadful Word"
     :modifiers [(mod5e/trait-cfg
                  {:name "Eldritch Invocation: Dreadful Word"
                   :page 110
                   :summary "use warlock spell slot to cast confusion"
                   :frequency units5e/long-rests-1})
                 (mod5e/spells-known 4 :confusion ::char5e/cha "Warlock" 0 "once per long rest")]
     :prereqs [(opt5e/total-levels-option-prereq 7 :warlock)]})
   (t/option-cfg
    {:name "Eldritch Sight"
     :modifiers [(mod5e/trait-cfg
                  {:name "Eldritch Invocation: Eldritch Sight"
                   :page 110
                   :summary "cast detect magic at will"})
                 (mod5e/spells-known 1 :detect-magic ::char5e/cha "Warlock" 0 "at will")]})
   (t/option-cfg
    {:name "Eldritch Spear"
     :modifiers [(mod5e/trait-cfg
                  {:name "Eldritch Invocation: Eldritch Spear"
                   :page 111
                   :summary "eldrich blast with range 300 ft."})]
     :prereqs [opt5e/has-eldritch-blast-prereq]})
   (t/option-cfg
    {:name "Eyes of the Rune Keeper"
     :modifiers [(mod5e/trait-cfg
                  {:name "Eldritch Invocation: Eyes of the Rune Keeper"
                   :page 111
                   :summary "read any writing."})]})
   (t/option-cfg
    {:name "Fiendish Vigor"
     :modifiers [(mod5e/trait-cfg
                  {:name "Eldritch Invocation: Fiendish Vigor"
                   :page 111
                   :summary "cast false life at will"})
                 (mod5e/spells-known 1 :false-life ::char5e/cha "Warlock" 0 "at will")]})
   (t/option-cfg
    {:name "Gaze of Two Minds"
     :modifiers [(mod5e/trait "Eldritch Invocation: Gaze of Two Minds"
                   "You can use your action to touch a willing humanoid and perceive through its senses until the end of your next turn. As long as the creature is on the same plane of existence as you, you can use your action on subsequent turns to maintain this connection, extending the duration until the end of your next turn. While perceiving through the other creature’s senses, you benefit from any special senses possessed by that creature, and you are blinded and deafened to your own surroundings.")]})
   (t/option-cfg
    {:name "Lifedrinker"
     :modifiers [(mod5e/dependent-trait
                  {:name "Eldritch Invocation: Lifedrinker"
                   :page 111
                   :summary (str "extra " (max 1 (?ability-bonuses ::char5e/cha)) " necrotic damage with your pact weapon")})]
     :prereqs [(opt5e/total-levels-option-prereq 12 :warlock)
               opt5e/pact-of-the-blade-prereq]})
   (t/option-cfg
    {:name "Mask of Many Faces"
     :modifiers [(mod5e/trait-cfg
                  {:name "Eldritch Invocation: Mask of Many Faces"
                   :page 111
                   :summary "cast disguise self at will"})
                 (mod5e/spells-known 1 :disguise-self ::char5e/cha "Warlock" 0 "at will")]})
   (t/option-cfg
    {:name "Master of Myriad Forms"
     :modifiers [(mod5e/trait-cfg
                  {:name "Eldritch Invocation: Master of Myriad Forms"
                   :page 111
                   :summary "cast alter self at will"})
                 (mod5e/spells-known 2 :alter-self ::char5e/cha "Warlock" 0 "at will")]
     :prereqs [(opt5e/total-levels-option-prereq 15 :warlock)]})
   (t/option-cfg
    {:name "Minions of Chaos"
     :modifiers [(mod5e/trait-cfg
                  {:name "Eldritch Invocation: Minions of Chaos"
                   :page 111
                   :frequency units5e/long-rests-1
                   :summary "cast conjure elemental using warlock spell slot
long rest."})
                 (mod5e/spells-known 5 :conjure-elemental ::char5e/cha "Warlock" 0 "once per rest")]
     :prereqs [(opt5e/total-levels-option-prereq 9 :warlock)]})
   (t/option-cfg
    {:name "Mire the Mind"
     :modifiers [(mod5e/trait-cfg
                  {:name "Eldritch Invocation: Mire the Mind"
                   :page 111
                   :frequency units5e/long-rests-1
                   :summary "cast slow using warlock spell slot"})
                 (mod5e/spells-known 3 :slow ::char5e/cha "Warlock" 0 "at will")]
     :prereqs [(opt5e/total-levels-option-prereq 5 :warlock)]})
   (t/option-cfg
    {:name "Misty Visions"
     :modifiers [(mod5e/trait-cfg
                  {:name "Eldritch Invocation: Misty Visions"
                   :page 111
                   :summary "cast silent image at will"})
                 (mod5e/spells-known 1 :silent-image ::char5e/cha "Warlock" 0 "at will")]})
   (t/option-cfg
    {:name "One with Shadows"
     :modifiers [(mod5e/action
                  {:name "Eldritch Invocation: One with Shadows"
                   :page 111
                   :summary "in dim light or darkness, become invisible"})]
     :prereqs [(opt5e/total-levels-option-prereq 5 :warlock)]})
   (t/option-cfg
    {:name "Otherworldly Leap"
     :modifiers [(mod5e/trait-cfg
                  {:name "Eldritch Invocation: Otherworldly Leap"
                   :page 111
                   :summary "cast jump on yourself at will"})
                 (mod5e/spells-known 1 :jump ::char5e/cha "Warlock" 0 "at will")]
     :prereqs [(opt5e/total-levels-option-prereq 9 :warlock)]})
   (t/option-cfg
    {:name "Repelling Blast"
     :modifiers [(mod5e/trait-cfg
                  {:name "Eldritch Invocation: Repelling Blast"
                   :page 111
                   :summary "push the creature 10 ft when you cast eldritch blast"})]
     :prereqs [opt5e/has-eldritch-blast-prereq]})
   (t/option-cfg
    {:name "Sculptor of Flesh"
     :modifiers [(mod5e/trait-cfg
                  {:name "Eldritch Invocation: Sculptor of Flesh"
                   :page 111
                   :frequency units5e/long-rests-1
                   :summary "cast polymorph using a warlock spell slot"})
                 (mod5e/spells-known 4 :polymorph ::char5e/cha "Warlock" 0 "once per long rest")]
     :prereqs [(opt5e/total-levels-option-prereq 7 :warlock)]})
   (t/option-cfg
    {:name "Sign of Ill Omen"
     :modifiers [(mod5e/trait-cfg
                  {:name "Eldritch Invocation: Sign of Ill Omen"
                   :page 111
                   :frequency units5e/long-rests-1
                   :summary "cast bestow curse using warlock spell slot"})
                 (mod5e/spells-known 3 :bestow-curse ::char5e/cha "Warlock" 0 "once per long rest")]
     :prereqs [(opt5e/total-levels-option-prereq 5 :warlock)]})
   (t/option-cfg
    {:name "Thief of Five Fates"
     :modifiers [(mod5e/trait-cfg
                  {:name "Eldritch Invocation: Thief of Five Fates"
                   :page 111
                   :frequency units5e/long-rests-1
                   :summary "cast bane warlock spell slot"})
                 (mod5e/spells-known 1 :bane ::char5e/cha "Warlock" 0 "once per long rest")]})
   (t/option-cfg
    {:name "Thirsting Blade"
     :modifiers [(mod5e/trait-cfg
                  {:name "Eldritch Invocation: Thirsting Blade"
                   :page 111
                   :summary "when using Attack action, attack with pact blade twice"})]
     :prereqs [(opt5e/total-levels-option-prereq 5 :warlock)
               opt5e/pact-of-the-blade-prereq]})
   (t/option-cfg
    {:name "Visions of Distant Realms"
     :modifiers [(mod5e/trait-cfg
                  {:name "Eldritch Invocation: Visions of Distant Realms"
                   :page 111
                   :summary "cast arcane eye at will"})
                 (mod5e/spells-known 4 :arcane-eye ::char5e/cha "Warlock" 0 "at will")]
     :prereqs [(opt5e/total-levels-option-prereq 15 :warlock)]})
   (t/option-cfg
    {:name "Voice of the Chain Master"
     :modifiers [(mod5e/trait-cfg
                  {:name "Eldritch Invocation: Voice of the Chain Master"
                   :page 111
                   :summary "communicate telepathically with, perceive through, and speak through your familiar"})]
     :prereqs [opt5e/pact-of-the-chain-prereq]})
   (t/option-cfg
    {:name "Whispers of the Grave"
     :modifiers [(mod5e/trait-cfg
                  {:name "Eldritch Invocation: Whispers of the Grave"
                   :page 111
                   :summary "cast speak with dead at will"})
                 (mod5e/spells-known 3 :speak-with-dead ::char5e/cha "Warlock" 0 "at will")]
     :prereqs [(opt5e/total-levels-option-prereq 9 :warlock)]})
   (t/option-cfg
    {:name "Witch Sight"
     :modifiers [(mod5e/trait-cfg
                  {:name "Eldritch Invocation: Witch Sight"
                   :range units5e/ft-30
                   :page 111
                   :summary "see the true form of a creature"})]
     :prereqs [(opt5e/total-levels-option-prereq 15 :warlock)]})])


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
  (opt5e/eldritch-invocation-selection
   {:options eldritch-invocation-options
    :min (or num 1)
    :max (or num 1)}))

(defn mystic-arcanum-selection [spell-level]
  (t/selection-cfg
   {:name (str "Mystic Arcanum: Spell Level " spell-level)
    :tags #{:spells}
    :options (opt5e/spell-options
              (get-in sl/spell-lists [:warlock spell-level])
              ::char5e/cha
              "Warlock"
              false
              "uses Mystic Arcanum")}))

(def warlock-option
  (opt5e/class-option
   {:name "Warlock"
    :spellcasting {:cantrips-known {1 2 4 1 10 1}
                   :spells-known warlock-spells-known
                   :slot-schedule t-base/warlock-spell-slot-schedule
                   :known-mode :schedule
                   :pact-magic? true
                   :ability ::char5e/cha}
    :multiclass-prereqs [(opt5e/ability-prereq ::char5e/cha 13)]
    :spellcaster true
    :hit-die 8
    :ability-increase-levels [4 8 12 16 19]
    :profs {:armor {:light false}
            :weapon {:simple false}
            :save {::char5e/wis true ::char5e/cha true}
            :skill-options {:choose 2 :options {:arcana true :deception true :history true :intimidation true :investigation true :nature true :religion true}}}
    :modifiers [(mod/modifier ?pact-magic? true)]
    :selections [(opt5e/new-starting-equipment-selection
                  :warlock
                  {:name "Weapon"
                   :options [(t/option-cfg
                              {:name "Light Crossbow & 20 Bolts"
                               :modifiers [(mod5e/weapon :crossbow-light 1)
                                           (mod5e/equipment :crossbow-bolt 20)]})
                             (t/option-cfg
                              {:name "Simple Weapon"
                               :selections [(opt5e/new-starting-equipment-selection
                                             :warlock
                                             {:name "Simple Weapon"
                                              :options (opt5e/simple-weapon-options 1)
                                              :min 1
                                              :max 1})]})]})
                 (opt5e/simple-weapon-selection 1 :warlock)]
    :equipment-choices [{:name "Equipment Pack"
                         :options {:scholars-pack 1
                                   :dungeoneers-pack 1}}
                        {:name "Spellcasting Equipment"
                         :options {:component-pouch 1
                                   :arcane-focus 1}}]
    :weapons {:dagger 2}
    :armor {:leather 1}
    :levels {2 {:selections [(eldritch-invocation-selection 2)]}
             3 {:selections [(t/selection-cfg
                              {:name "Pact Boon"
                               :tags #{:class}
                               :options pact-boon-options})]}
             5 {:selections [(eldritch-invocation-selection)]}
             7 {:selections [(eldritch-invocation-selection)]}
             9 {:selections [(eldritch-invocation-selection)]}
             11 {:selections [(mystic-arcanum-selection 6)]
                 :modifiers [(mod5e/dependent-trait
                              {:name "Mystic Arcanum"
                               :level 11
                               :page 108
                               :summary "You gain a 6th level spell you can cast without expending a slot, more at higher levels"
                               :frequency (units5e/long-rests
                                           (mod5e/level-val
                                            (?class-level :warlock)
                                            {13 2
                                             15 3
                                             17 4
                                             :default 1}))})]}
             12 {:selections [(eldritch-invocation-selection)]}
             13 {:selections [(mystic-arcanum-selection 7)]}
             15 {:selections [(eldritch-invocation-selection)
                              (mystic-arcanum-selection 8)]}
             17 {:selections [(mystic-arcanum-selection 9)]}
             18 {:selections [(eldritch-invocation-selection)]}}
    :traits [{:name "Eldrich Master"
              :level 20
              :page 108
              :summary "Regain all Pact Magic spell slots"
              :frequency units5e/long-rests-1}]
    :subclass-level 1
    :subclass-title "Otherworldly Patron"
    :subclasses [{:name "The Fiend"
                  :traits [{:name "Dark One's Own Luck"
                            :level 6
                            :page 109
                            :summary "add d10 to an attack or save roll"
                            :frequency units5e/rests-1}
                           {:name "Fiendish Resilience"
                            :level 10
                            :page 109
                            :summary "resistance to a chosen damage type"}
                           {:name "Hurl Through Hell"
                            :level 14
                            :page 109
                            :summary "deal 10d10 psychic damage when you hit with an attack"
                            :frequency units5e/rests-1}]
                  :levels {1 {:modifiers [(mod5e/dependent-trait
                                           {:name "Dark One's Blessing"
                                            :page 109
                                            :summary (str "gain " (+ (?class-level :warlock)
                                                                     (?ability-bonuses ::char5e/cha)) " temp HPs when you reduce a hostile creature to 0 HPs")})]
                              :selections [(opt5e/warlock-subclass-spell-selection [:burning-hands :command])]}
                           3 {:selections [(opt5e/warlock-subclass-spell-selection [:blindness-deafness :scorching-ray])]}
                           5 {:selections [(opt5e/warlock-subclass-spell-selection [:fireball :stinking-cloud])]}
                           7 {:selections [(opt5e/warlock-subclass-spell-selection [:fire-shield :wall-of-fire])]}
                           9 {:selections [(opt5e/warlock-subclass-spell-selection [:flame-strike :hallow])]}}}
                 {:name "The Archfey"
                  :modifiers [(mod5e/action
                               {:name "Fey Presence"
                                :page 109
                                :summary (str "charm or frighten creatures in a 10 ft cube from you unless the succeed on a DC " (?spell-save-dc ::char5e/cha) " WIS save.")
                                :duration units5e/turns-1
                                :frequency units5e/rests-1})]
                  :levels {1 {:selections [(opt5e/warlock-subclass-spell-selection [:faerie-fire :sleep])]}
                           3 {:selections [(opt5e/warlock-subclass-spell-selection [:calm-emotions :phantasmal-force])]}
                           5 {:selections [(opt5e/warlock-subclass-spell-selection [:blink :plant-growth])]}
                           6 {:modifiers [(mod5e/reaction
                                           {:name "Misty Escape"
                                            :page 109
                                            :frequency units5e/rests-1
                                            :duration units5e/rounds-1
                                            :summary "when you take damage, turn invisible and teleport up to 60 ft."})]}
                           7 {:selections [(opt5e/warlock-subclass-spell-selection [:dominate-beast :greater-invisibility])]}
                           9 {:selections [(opt5e/warlock-subclass-spell-selection [:dominate-person :seeming])]}
                           10 {:modifiers [(mod5e/condition-immunity :charmed)
                                           (mod5e/reaction
                                            {:name "Beguiling Defenses"
                                             :page 109
                                             :duration units5e/minutes-1
                                             :summary (str "when a creature attempts to charm you, you can turn it back on them with a spell save DC " (?spell-save-dc ::char5e/cha) " WIS save")})]}
                           14 {:modifiers [(mod5e/action
                                            {:name "Dark Delerium"
                                             :page 109
                                             :summary (str "charm or frighten a creature within 60 ft., spell save DC " (?spell-save-dc ::char5e/cha) "WIS save")
                                             :frequency units5e/rests-1})]}}}
                 {:name "The Great Old One"
                  :levels {1 {:selections [(opt5e/warlock-subclass-spell-selection [:dissonant-whispers :tashas-hideous-laughter])]}
                           3 {:selections [(opt5e/warlock-subclass-spell-selection [:detect-thoughts :phantasmal-force])]}
                           5 {:selections [(opt5e/warlock-subclass-spell-selection [:clairvoyance :sending])]}
                           6 {:modifiers [(mod5e/reaction
                                           {:name "Entropic Ward"
                                            :page 110
                                            :frequency units5e/rests-1
                                            :summary "impose disadvantage on an attack roll against you, if it misses, gain advantage on your next attack roll against the attacker"})]}
                           7 {:selections [(opt5e/warlock-subclass-spell-selection [:dominate-beast :evards-black-tentacles])]}
                           9 {:selections [(opt5e/warlock-subclass-spell-selection [:dominate-person :telekinesis])]}
                           10 {:modifiers [(mod5e/damage-resistance :psychic)]}}
                  :traits [{:name "Awakened Mind"
                            :level 1
                            :page 110
                            :summary "speak telepathically to a creature"
                            :range units5e/ft-30}
                           {:name "Thought Shield"
                            :level 10
                            :page 110
                            :summary "your thoughts can't be read; resistance to psychic damage; when a creature deals psychic damage to you it takes the same amount"}
                           {:name "Create Thrall"
                            :level 14
                            :page 110
                            :summary "charm incapacitated creature, it becomes charmed by you, you can communicate with it telepathically"}]}]}))

(def arcane-tradition-options
  [(t/option-cfg
    {:name "School of Evocation"
     :modifiers [(mod5e/subclass :wizard "School of Evocation")
                 (mod5e/trait "Evocation Savant")
                 (mod5e/trait "Sculpt Spells")]})])

(defn criminal-background [nm]
  {:name nm
   :help "You have a history of criminal activity."
   :traits [{:name "Criminal Contact"
             :page 129
             :summary "You have a contact into a network of criminals"}]
   :profs {:skill {:deception true, :stealth true}
           :tool {:thieves-tools true}
           :tool-options {:gaming-set 1}}
   :equipment {:crowbar 1
               :clothes-common 1
               :pouch 1}
   :treasure {:gp 15}})

(def ships-passage-trait-cfg
  {:name "Ship's Passage"
   :page 139
   :summary "You are able to secure free passage on a sailing ship"})

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
                            #(starting-equipment-option % 1)
                            equip5e/holy-symbols)})
                ]
   :equipment-choices [{:name "Prayer Book/Wheel"
                        :options {:prayer-book 1
                                  :prayer-wheel 1}}]
   :treasure {:gp 15}
   :traits [{:name "Shelter the Faithful"
             :page 127
             :summary "You and your companions can expect free healing at an establishment of your faith."}]})

(def charlatan-bg
  {:name "Charlatan"
   :help "You have a history of being able to work people to your advantage."
   :traits [{:name "False Identity"
             :page 128
             :summary "you have a false identity; you can forge documents"}]
   :profs {:skill {:deception true :sleight-of-hand true}
           :tool {:disguise-kit true :forgery-kit true}}
   :equipment {:clothes-fine 1
               :disguise-kit 1
               :pouch 1}
   :treasure {:gp 15}})

(def entertainer-bg
  {:name "Entertainer"
   :help "You have a history of entertaining people."
   :traits [{:name "By Popular Demand"
             :page 130
             :summary "you are able to find a place to perform, in which you will recieve free food and lodging"}]
   :profs {:skill {:acrobatics true :performance true}
           :tool {:disguise-kit true}
           :tool-options {:musical-instrument 1}}
   :equipment-choices [musical-instrument-choice-cfg]
   :equipment {:costume 1
               :pouch 1}
   :treasure {:gp 15}})

(def gladiator-bg
  {:name "Gladiator"
   :help "You have a history of gladiatorial entertainment."
   :traits [{:name "By Popular Demand"
             :page 130
             :summary "you are able to find a place to perform, in which you will recieve free food and lodging"}]
   :profs {:skill {:acrobatics true :performance true}
           :tool {:disguise-kit true}
           :tool-options {:musical-instrument 1}}
   :selections [(opt5e/new-starting-equipment-selection
                 nil
                 {:name "Gladiator Weapon"
                  :options (opt5e/weapon-options weapon5e/weapons)})]
   :equipment {:costume 1
               :pouch 1}
   :treasure {:gp 15}})

(def folk-hero-bg
  {:name "Folk Hero"
   :help "You are regarded as a hero by the people of your home village."
   :traits [{:name "Rustic Hospitality"
             :page 131
             :summary "find a place to rest, hide, or recuperate among commoners"}]
   :profs {:skill {:animal-handling true :survival true}
           :tool {:land-vehicles true}
           :tool-options {:artisans-tool 1}}
   :equipment-choices [opt5e/artisans-tools-choice-cfg]
   :equipment {:shovel 1
               :pot-iron 1
               :clothes-common 1
               :pouch 1}
   :treasure {:gp 10}})

(def guild-artisan-bg
  {:name "Guild Artisan"
   :help "You are an artisan and a member of a guild in a particular field."
   :traits [{:name "Guild Membership"
             :page 133
             :summary "fellow guild members will provide you with food and lodging; you have powerful political connections through your guild"}]
   :profs {:skill {:insight true :persuasion true}
           :tool-options {:artisans-tool 1}
           :language-options {:choose 1 :options {:any true}}}
   :equipment-choices [opt5e/artisans-tools-choice-cfg]
   :equipment {:clothes-traveler-s 1
               :pouch 1}
   :treasure {:gp 15}})

(def guild-merchant-bg
  {:name "Guild Merchant"
   :help "You are member of a guild of merchants"
   :traits [{:name "Guild Membership"
             :page 133
             :summary "fellow guild members will provide you with food and lodging; you have powerful political connections through your guild"}]
   :profs {:skill {:insight true :persuasion true}
           :language-options {:choose 1 :options {:any true}}}
   :selections [(t/selection-cfg
                 {:name "Proficiency: Navigator's Tools or Language"
                  :tags #{:profs}
                  :options [(t/option-cfg
                             {:name "Navigator's Tools"
                              :modifiers [(mod5e/tool-proficiency :navigators-tools)]})
                            (t/option-cfg
                             {:name "Language"
                              :selections [(opt5e/language-selection opt5e/languages 1)]})]})]
   :equipment {:clothes-traveler-s 1
               :pouch 1
               :mule 1
               :cart 1}
   :treasure {:gp 15}})

(def hermit-bg
  {:name "Hermit"
   :help "You have lived a secluded life."
   :traits [{:name "Discovery"
             :page 134
             :summary "You have made a powerful and unique discovery"}]
   :profs {:skill {:medicine true :religion true}
           :tool {:herbalism-kit true}
           :language-options {:choose 1 :options {:any true}}}
   :equipment {:case-map-or-scroll 1
               :clothes-common 1
               :herbalism-kit 1}
   :custom-equipment {"Winter Blanket" 1
                      "Notes from studies/prayers" 1}
   :treasure {:gp 5}})


(def noble-bg
  {:name "Noble"
   :help "You are of noble birth."
   :traits []
   :profs {:skill {:history true :persuasion true}
           :tool-options {:gaming-set 1}
           :language-options {:choose 1 :options {:any true}}}
   :selections [(t/selection-cfg
                 {:name "Noble Feature"
                  :tags #{:background}
                  :options [(t/option-cfg
                             {:name "Position of Privilege"
                              :modifiers [(mod5e/trait-cfg
                                           {:name "Position of Privilege"
                                            :page 135
                                            :summary "you are welcome in high society and common folk try to accomodate you"})]})
                            (t/option-cfg
                             {:name "Retainers"
                              :modifiers [(mod5e/trait-cfg
                                           {:name "Retainers"
                                            :page 136
                                            :summary "You have 3 commoner retainers"})]})]})]
   :equipment {:clothes-fine 1
               :signet-ring 1
               :purse 1}
   :custom-equipment {"Scroll of Pedigree" 1}
   :treasure {:gp 25}})

(def knight-bg
  {:name "Knight"
   :help "You are a knight."
   :traits [{:name "Retainers"
             :page 136
             :summary "You have 2 commoner retainers and 1 noble squire"}]
   :profs {:skill {:history true :persuasion true}
           :tool-options {:gaming-set 1}
           :language-options {:choose 1 :options {:any true}}}
   :equipment {:clothes-fine 1
               :signet-ring 1
               :purse 1}
   :custom-equipment {"Scroll of Pedigree" 1
                      "Emblem of Chivalry" 1}
   :treasure {:gp 25}})

(def outlander-bg
  {:name "Outlander"
   :help "You were raised in the wilds."
   :traits [{:name "Wanderer"
             :page 136
             :summary "Your memory of maps, geography, settlements, and terrain is excellent. You can find fresh food and water for you and 5 other people."}]
   :profs {:skill {:athletics true :survival true}
           :tool-options {:musical-instrument 1}
           :language-options {:choose 1 :options {:any true}}}
   :equipment {:staff 1
               :clothes-traveler-s 1
               :pouch 1
               :hunting-trap 1}
   :custom-equipment {"Trophy from Animal You Killed" 1}
   :treasure {:gp 10}})

(def sage-bg
  {:name "Sage"
   :help "You spent your life studying lore."
   :traits [{:name "Researcher"
             :page 139
             :summary "If you don't know a piece of info you often know where to find it"}]
   :profs {:skill {:arcana true :history true}
           :language-options {:choose 2 :options {:any true}}}
   :equipment {:ink 1
               :clothes-common 1
               :pouch 1
               :knife-small 1}
   :custom-equipment {"Quill" 1
                      "Letter with question from dead colleague" 1}
   :treasure {:gp 10}})

(def sailor-bg
  {:name "Sailor"
   :help "You were a member of a crew for a seagoing vessel."
   :traits [ships-passage-trait-cfg]
   :profs {:skill {:athletics true :perception true}
           :tool {:navigators-tools true :water-vehicles true}}
   :weapons {:club 1}
   :equipment {:rope-silk 1
               :clothes-common 1
               :pouch 1}
   :custom-equipment {"Belaying Pin" 1
                      "Lucky Charm" 1}
   :treasure {:gp 10}})


(def pirate-bg
  {:name "Pirate"
   :help "You were a member of a crew for a seagoing vessel."
   :profs {:skill {:athletics true :perception true}
           :tool {:navigators-tools true :water-vehicles true}}
   :weapons {:club 1}
   :equipment {:rope-silk 1
               :clothes-common 1
               :pouch 1}
   :selections [(t/selection-cfg
                 {:name "Feature"
                  :tags #{:background}
                  :options [(t/option-cfg
                             {:name "Ship's Passage"
                              :modifiers [(mod5e/trait-cfg
                                           ships-passage-trait-cfg)]})
                            (t/option-cfg
                             {:name "Bad Reputation"
                              :modifiers [(mod5e/trait-cfg
                                           {:name "Bad Reputation"
                                            :page 139
                                            :summary "People in a civilized settlement are afraid of you and will let you get away with minor crimes"})]})]})]
   :custom-equipment {"Belaying Pin" 1
                      "Lucky Charm" 1}
   :treasure {:gp 10}})

(def soldier-bg
  {:name "Soldier"
   :help "You have spent your living by the sword."
   :traits [{:name "Military Rank"
             :page 140
             :summary "Where recognized, your previous rank provides influence among military"}]
   :profs {:skill {:athletics true :intimidation true}
           :tool {:land-vehicles true}
           :tool-options {:gaming-set 1}}
   :equipment {:clothes-common 1
               :pouch 1}
   :equipment-choices [{:name "Dice or Cards"
                        :options {:dice-set 1
                                  :playing-card-set 1}}]
   :custom-equipment {"Insignia of Rank" 1
                      "Trophy from Fallen Enemy" 1}
   :treasure {:gp 10}})

(def urchin-bg
  {:name "Urchin"
   :help "You were a poor orphan living on the streets."
   :traits [{:name "City Streets"
             :page 141
             :summary "You can travel twice your normal speed between city locations"}]
   :profs {:skill {:sleight-of-hand true :stealth true}
           :tool {:disguise-kit true :thieves-tools true}}
   :equipment {:knife-small 1
               :clothes-common 1
               :pouch 1}
   :custom-equipment {"Map of city you grew up in" 1
                      "Pet mouse" 1
                      "Token to remember your parents" 1}
   :treasure {:gp 10}})

(def backgrounds [acolyte-bg
                  charlatan-bg
                  (criminal-background "Criminal")
                  (criminal-background "Spy")
                  entertainer-bg
                  gladiator-bg
                  folk-hero-bg
                  guild-artisan-bg
                  guild-merchant-bg
                  hermit-bg
                  noble-bg
                  knight-bg
                  outlander-bg
                  sage-bg
                  sailor-bg
                  pirate-bg
                  soldier-bg
                  urchin-bg])

(def volos-guide-to-monsters-selections
  [(t/selection-cfg
    {:name "Race"
     :tags #{:race}
     :options (map
               (fn [race]
                 (opt5e/race-option (assoc race :source :vgm)))
               [aasimar-option-cfg
                firbolg-option-cfg
                (goliath-option-cfg :vgm 109)
                kenku-option-cfg
                lizardfolk-option-cfg
                tabaxi-option-cfg
                triton-option-cfg
                bugbear-option-cfg
                goblin-option-cfg
                hobgoblin-option-cfg
                kobold-option-cfg
                orc-option-cfg
                yuan-ti-option-cfg])})])

(def cos-backgrounds
  (map
   (partial opt5e/add-sources :cos)
   [{:name "Haunted One"
     :help "You have been subjected to an unimaginable horror"
     :profs {:skill-options {:choose 2 :options {:arcana true :investigation true :religion true :survival true}}
             :language-options {:choose 2 :options {:abyssal true :celestial true :deep-speech true :draconic true :infernal true :primordial true :sylvan true :undercommon true}}}
     :equipment {:monster-hunters-pack 1}
     :traits [{:name "Heart of Darkness"
               :page 209
               :source :cos
               :summary "Commoners do their utmost to help you, even fighting along side you"}]}]))

(def dmg-classes
  [{:name "Cleric"
    :plugin? true
    :subclass-level 1
    :subclass-title "Divine Domain"
    :source :dmg
    :subclasses [{:name "Death Domain"
                  :modifiers [(mod5e/al-illegal "Death Domain is not allowed")
                              (mod5e/weapon-proficiency :martial)
                              (opt5e/cleric-spell 1 :false-life 1)
                              (opt5e/cleric-spell 1 :ray-of-sickness 1)
                              (opt5e/cleric-spell 2 :blindness-deafness 3)
                              (opt5e/cleric-spell 2 :ray-of-enfeeblement 3)
                              (opt5e/cleric-spell 3 :animate-dead 5)
                              (opt5e/cleric-spell 3 :vampiric-touch 5)
                              (opt5e/cleric-spell 4 :blight 7)
                              (opt5e/cleric-spell 4 :death-ward 7)
                              (opt5e/cleric-spell 5 :antilife-shell 9)
                              (opt5e/cleric-spell 5 :cloudkill 9)
                              (mod5e/trait-cfg
                               {:name "Reaper"
                                :page 96
                                :source :dmg
                                :summary "Your necromancy cantrips that only target 1 creature can instead target two within 5 ft of each other"})]
                  :selections [(opt5e/spell-selection {:title "Necromancy Cantrip"
                                                       :spellcasting-ability ::char5e/wis
                                                       :class-name "Cleric"
                                                       :num 1
                                                       :spell-keys (map :key (filter #(and (= "necromancy" (:school %))
                                                                                           (= 0 (:level %))) spells/spells))
                                                       :exclude-ref? true})]
                  :levels {2 {:modifiers [(mod5e/action
                                           {:name "Channel Divinity: Touch of Death"
                                            :page 97
                                            :source :dmg
                                            :summary (str "when you hit with melee weapon attack, deal an extra " (+ 5 (* 2 (?class-level :cleric))) " necrotic damage")})]}
                           6 {:modifiers [(mod5e/trait-cfg
                                           {:name "Inescapable Destruction"
                                            :page 97
                                            :source :dmg
                                            :summary "Your cleric spells and Channel Divinity ignore necrotic damage resistance"})]}
                           8 {:modifiers [(opt5e/divine-strike "necrotic" 97 :dmg)]}}
                  :traits [{:level 17
                            :name "Improved Reaper"
                            :summary "If you cast a necromancy spell that targets only 1 creature, you can instead target two within 5 ft. of each other"}]}]}
   (opt5e/subclass-plugin
    opt5e/paladin-base-cfg
    :dmg
    [{:name "Oathbreaker"
      :modifiers [(mod5e/al-illegal "Oathbreaker is not allowed")
                  (opt5e/paladin-spell 1 :hellish-rebuke 3)
                  (opt5e/paladin-spell 1 :inflict-wounds 3)
                  (opt5e/paladin-spell 2 :crown-of-madness 5)
                  (opt5e/paladin-spell 2 :darkness 5)
                  (opt5e/paladin-spell 3 :animate-dead 9)
                  (opt5e/paladin-spell 3 :bestow-curse 9)
                  (opt5e/paladin-spell 4 :blight 13)
                  (opt5e/paladin-spell 4 :confusion 13)
                  (opt5e/paladin-spell 5 :contagion 17)
                  (opt5e/paladin-spell 5 :dominate-person 17)
                  (mod5e/action
                   {:name "Channel Divinity: Control Undead"
                    :source :dmg
                    :page 97
                    :summary (str "Control an undead creature of CR " (?class-level :paladin) " or less if it fails a DC " (?spell-save-dc ::char5e/cha) " WIS save")})
                  (mod5e/action
                   {:name "Channel Divinity: Dreadful Aspect"
                    :page 97
                    :source :dmg
                    :summary (str "creatures of your choice within 30 ft. must succeed on a DC " (?spell-save-dc ::char5e/cha) " WIS save or be frightened of you")})]
      :levels {7 {:modifiers [(mod5e/dependent-trait
                               {:name "Aura of Hate"
                                :page 97
                                :source :dmg
                                :summary (str "you and friends within " (if (>= (?class-level :paladin) 18) 30 10) " ft. gain a " (common/bonus-str (max 1 (?ability-bonuses ::char5e/cha))) " bonus to melee weapon attack damage")})]}
               15 {:modifiers [(mod5e/damage-resistance :bludgeoning "nonmagical weapons")
                               (mod5e/damage-resistance :piercing "nonmagical weapons")
                               (mod5e/damage-resistance :slashing "nonmagical weapons")]}
               20 {:modifiers [(mod5e/action
                                {:name "Dread Lord"
                                 :page 97
                                 :source :dmg
                                 :frequency units5e/long-rests-1
                                 :duration units5e/minutes-1
                                 :range units5e/ft-30
                                 :summary (str "create an aura that: reduces bright light to dim; frightened enemies within aura take 4d10 psychic damage; creatures that rely on sight have disadvantage on attack rolls agains you and allies within aura; as a bonus action make a melee spell attack on a creature within aura that deals 3d10 " (common/mod-str (?ability-bonuses ::char5e/cha)) "necrotic damage")})]}}}]
    true)])

(def elemental-evil-selections
  [(opt5e/feat-selection-2
    {:options [(opt5e/svirfneblin-magic-feat :ee 7)]})
   (opt5e/race-selection
    {:options (map
               opt5e/race-option
               [aarakocra-option-cfg
                ee-gnome-option-cfg
                genasi-option-cfg
                (goliath-option-cfg :ee 11)])})])

(def keen-senses-option
  (t/option-cfg
   {:name "Keen Senses"
    :modifiers [(mod5e/skill-proficiency :perception)]}))

(defn elf-parentage-option [name trait-options]
  (t/option-cfg
   {:name name
    :selections [(t/selection-cfg
                  {:name "Elf Trait"
                   :tags #{:race}
                   :order 3
                   :options trait-options})]}))

(def elf-weapon-training-option
  (t/option-cfg
   {:name "Elf Weapon Training"
    :modifiers elf-weapon-training-mods}))

(defn high-elf-parentage-option [name]
  (elf-parentage-option name
                        [keen-senses-option
                         elf-weapon-training-option
                         (t/option-cfg
                          {:name "High Elf Cantrip"
                           :selections [high-elf-cantrip-selection]})]))

(def scag-human-option-cfg
  {:name "Human"
   :plugin? true
   :subraces
   [{:name "Arkaiun"}
    {:name "Bedine"}
    {:name "Ffolk"}
    {:name "Gur"}
    {:name "Halruaan"}
    {:name "Imaskari"}
    {:name "Nar"}
    {:name "Shaaran"}
    {:name "Tuigan"}
    {:name "Ulutiun"}]})

(def scag-deep-gnome-cfg
  (opt5e/deep-gnome-option-cfg :deep-gnome-scag :scag 115))

(def scag-dwarf-option-cfg
  {:name "Dwarf",
   :plugin? true
   :subraces [{:name "Gray Dwarf (Duerger)",
               :abilities {::char5e/str 1}
               :modifiers [(mod5e/darkvision 120)
                           (mod5e/language :undercommon)
                           (mod5e/trait-cfg
                            {:name "Duergar Resilience"
                             :page 104
                             :source :scag
                             :summary "Advantage on saves against being charmed or paralyzed and against illusions."})
                           (mod5e/trait-cfg
                            {:name "Duergar Magic"
                             :page 104
                             :source :scag
                             :summary "At 3rd level, can cast enlarge/reduce (enlarge only); at 5th level can cast invisibility. Each can be cast without material components and once per long rest."})
                           (sunlight-sensitivity 104 :scag)
                           (mod5e/saving-throw-advantage [:charmed])
                           (mod5e/saving-throw-advantage [:paralyzed])
                           (mod5e/saving-throw-advantage [:illusions])
                           (mod5e/spells-known 2 :enlarge-reduce ::char5e/int "Duergar" 3 "enlarge only, once per long rest")
                           (mod5e/spells-known 2 :invisibility ::char5e/int "Duergar" 5 "once per long rest")]}]})

(def scag-half-elf-option-cfg
  {:name "Half-Elf"
   :plugin? true
   :selections [(t/selection-cfg
                 {:name "Half-Elf Variant"
                  :tags #{:race}
                  :order 1
                  :options [(t/option-cfg
                             {:name "Standard"
                              :help [:div "This is the standard half-elf presented in the Player's Handbook"]})
                            (t/option-cfg
                             {:name "Sword Coast Variant"
                              :help "The half-elf variants presented in the Sword Coast Adventurer's Guide"
                              :selections [(assoc
                                            (opt5e/skill-selection -2)
                                            ::t/ref
                                            [:race :half-elf :skill-proficiency])
                                           (t/selection-cfg
                                            {:name "Elf Parentage"
                                             :tags #{:race}
                                             :order 2
                                             :options [(elf-parentage-option
                                                        "Wood Elf"
                                                        [keen-senses-option
                                                         elf-weapon-training-option
                                                         (t/option-cfg
                                                          {:name "Fleet of Foot"
                                                           :modifiers [(mod5e/speed 5)]})
                                                         (t/option-cfg
                                                          {:name "Mask of the Wild"
                                                           :modifiers [mask-of-the-wild-mod]})])
                                                       (high-elf-parentage-option "Moon Elf")
                                                       (high-elf-parentage-option "Sun Elf")
                                                       (elf-parentage-option
                                                        "Drow"
                                                        [keen-senses-option
                                                         (t/option-cfg
                                                          {:name "Drow Magic"
                                                           :modifiers drow-magic-mods})])
                                                       (elf-parentage-option
                                                        "Aquatic Elf"
                                                        [keen-senses-option
                                                         (t/option-cfg
                                                          {:name "Swimming Speed"
                                                           :modifiers [(mod5e/swimming-speed 30)]})])]})]})]})]})

(defn tiefling-spell-removal-modifier [level spell-key]
  (mod/modifier ?spells-known
                (update
                 ?spells-known
                 level
                 dissoc
                 ["Tiefling" spell-key])))

(def scag-tiefling-option-cfg
  {:name "Tiefling"
   :plugin? true
   :selections [(t/selection-cfg
                 {:name "Tiefling Variant"
                  :tags #{:race}
                  :order 1
                  :options [(t/option-cfg
                             {:name "Standard"
                              :help [:div "This is the standard tiefling presented in the Player's Handbook"]})
                            (t/option-cfg
                             {:name "Sword Coast Variant"
                              :help "The tiefling variants presented in the Sword Coast Adventurer's Guide"
                              :selections [(t/selection-cfg
                                            {:name "Ability Scores"
                                             :tags #{:race}
                                             :order 2
                                             :options [(t/option-cfg
                                                        {:name "Standard"
                                                         :help "The 'Ability Score Increase' trait from the Player's Handbook"})
                                                       (t/option-cfg
                                                        {:name "Feral"
                                                         :help "The ability increases from the Sword Coast Adventurer's Guide"
                                                         :modifiers [(mod5e/race-ability ::char5e/dex 2)
                                                                     (mod5e/race-ability ::char5e/cha -2)]})]})
                                           (t/selection-cfg
                                            {:name "Variant Features"
                                             :tags #{:race}
                                             :order 3
                                             :options [(t/option-cfg
                                                        {:name "None"
                                                         :help "Keep the other standard tiefling traits"})
                                                       (t/option-cfg
                                                        {:name "Devil's Tongue"
                                                         :modifiers [(mod5e/spells-known 0 :vicious-mockery ::char5e/cha "Tiefling")
                                                                     (mod5e/spells-known 1 :charm-person ::char5e/cha "Tiefling" 3)
                                                                     (mod5e/spells-known 2 :enthrall ::char5e/cha "Tiefling" 5)
                                                                     (tiefling-spell-removal-modifier 0 :thaumaturgy)
                                                                     (tiefling-spell-removal-modifier 1 :hellish-rebuke)
                                                                     (tiefling-spell-removal-modifier 2 :darkness)]})
                                                       (t/option-cfg
                                                        {:name "Hellfire"
                                                         :modifiers [(mod5e/spells-known 1 :burning-hands ::char5e/cha "Tiefling" 3)
                                                                     (tiefling-spell-removal-modifier 1 :hellish-rebuke)]})
                                                       (t/option-cfg
                                                        {:name "Winged"
                                                         :modifiers [(mod5e/flying-speed-override 30)
                                                                     (al-illegal-flying-mod "Winged Tiefling Variant")
                                                                     (tiefling-spell-removal-modifier 0 :thaumaturgy)
                                                                     (tiefling-spell-removal-modifier 1 :hellish-rebuke)
                                                                     (tiefling-spell-removal-modifier 2 :darkness)]})]})]})]})]})

(def scag-halfling-option-cfg
  {:name "Halfling"
   :plugin? true
   :subraces
   [{:name "Ghostwise"
     :abilities {::char5e/wis 1}
     :source :scag
     :traits [{:name "Silent Speech"
               :source :scag
               :page 110
               :range units5e/ft-30
               :summary "Speak telepathically to 1 creature who understands your language"}]}]})

(def sword-coast-adventurers-guide-selections
  [(opt5e/feat-selection-2
    {:options [(opt5e/svirfneblin-magic-feat :scag 115)]})
   (opt5e/background-selection
    {:options (map
               opt5e/background-option
               scag/sword-coast-adventurers-guide-backgrounds)})
   (opt5e/race-selection
    {:options (map
               (fn [race] (opt5e/race-option (if (not (:plugin? race))
                                               (assoc race :source :scag)
                                               race)))
               [scag-half-elf-option-cfg
                scag-tiefling-option-cfg
                scag-halfling-option-cfg
                scag-deep-gnome-cfg
                scag-human-option-cfg
                scag-dwarf-option-cfg])})
   (opt5e/class-selection
    {:options (map
               (fn [cfg] (opt5e/class-option (assoc cfg :plugin? true :source :scag)))
               scag/scag-classes)})])

(def cos-selections
  [(opt5e/background-selection
    {:options (map
               opt5e/background-option
               cos-backgrounds)})])

(def eladrin-elf-option
  {:name "Elf"
   :source :dmg
   :plugin? true
   :subraces
   [{:name "Eladrin Elf"
     :source :dmg
     :abilities {::char5e/int 1}
     :modifiers [elf-weapon-training-mods
                 (mod5e/spells-known 2 :misty-step ::char5e/int "Eladrin Elf" 1 "Once per rest")]}]})

(def dmg-selections
  [(opt5e/class-selection
    {:options (map
               (fn [cfg] (opt5e/class-option (assoc cfg :plugin? true :source :dmg)))
               dmg-classes)})
   (opt5e/race-selection
    {:options [(opt5e/race-option eladrin-elf-option)]})])

(defn ability-item [name abbr desc]
  [:li.m-t-5 [:span.f-w-b.m-r-5 (str name " (" abbr ")")] desc])

(defn inventory-help [description page source]
  [:div
   [:div description]
   (if page (let [{:keys [abbr url]} (disp5e/get-source source)]
              [:span
               [:span "see"]
               [:a {:href url :target :_blank}
                abbr]
               [:span page]]))])

(defn magic-item-details [{:keys [key :db/id ::mi/name ::mi/description ::mi/summary ::mi/page ::mi/source]}]
  {:key (or key id)
   :name name
   :description (or description summary)
   :page page
   :source source})

(defn magic-item-selection [item-type-name icon options modifier-fn & [converter]]
  (t/selection-cfg
   {:name item-type-name
    :min 0
    :max nil
    :multiselect? true
    :sequential? false
    :icon icon
    :new-item-fn (fn [selection selected-items _ key]
                   {::entity/key key
                    ::entity/value 1})
    :tags #{:equipment}
    :options options}))

(defn inventory-selection [item-type-name icon items modifier-fn & [converter]]
  (t/selection-cfg
   {:name item-type-name
    :min 0
    :max nil
    :multiselect? true
    :sequential? false
    :icon icon
    :new-item-fn (fn [selection selected-items _ key]
                   {::entity/key key
                    ::entity/value 1})
    :tags #{:equipment}
    :options (map
              (fn [item]
                (let [{:keys [name key description page source]} (if converter (converter item) item)]
                  (t/option-cfg
                   {:name name
                    :key key
                    :help (if (or description
                                  page)
                            (inventory-help description page source))
                    :modifiers [(modifier-fn key item)]})))
              items)}))

(defn amazon-frame [link]
  [:iframe {:style {:width "120px" :height "240px"}
            :margin-width 0
            :margin-height 0
            :scrolling :no
            :frame-border 0
            :src link}])

(defn content-list [options]
  [:ul.m-t-5
   (doall
    (map
     (fn [nm]
       ^{:key nm}
       [:li.p-2 (str common/dot-char " " nm)])
     options))])

(def scag-amazon-frame
  (amazon-frame "//ws-na.amazon-adsystem.com/widgets/q?ServiceVersion=20070822&OneJS=1&Operation=GetAdHtml&MarketPlace=US&source=ac&ref=tf_til&ad_type=product_link&tracking_id=orcpub-20&marketplace=amazon&region=US&placement=0786965800&asins=0786965800&linkId=f35402a86dd0851190d952228fab36e9&show_border=false&link_opens_in_new_window=true&price_color=ffffff&title_color=f0a100&bg_color=2c3445"))

(def volos-amazon-frame
  (amazon-frame "//ws-na.amazon-adsystem.com/widgets/q?ServiceVersion=20070822&OneJS=1&Operation=GetAdHtml&MarketPlace=US&source=ac&ref=tf_til&ad_type=product_link&tracking_id=orcpub-20&marketplace=amazon&region=US&placement=0786966017&asins=0786966017&linkId=8c552e7b980d7d944bd12dec57e002e8&show_border=false&link_opens_in_new_window=true&price_color=ffffff&title_color=f0a100&bg_color=2c3445"))

(def phb-amazon-frame
  (amazon-frame "//ws-na.amazon-adsystem.com/widgets/q?ServiceVersion=20070822&OneJS=1&Operation=GetAdHtml&MarketPlace=US&source=ac&ref=qf_sp_asin_til&ad_type=product_link&tracking_id=orcpub-20&marketplace=amazon&region=US&placement=0786965606&asins=0786965606&linkId=3b5b686390559c31dbc3c20d20f37ec4&show_border=false&link_opens_in_new_window=true&price_color=ffffff&title_color=f0a100&bg_color=2c3445"))

(def dmg-amazon-frame
  (amazon-frame "//ws-na.amazon-adsystem.com/widgets/q?ServiceVersion=20070822&OneJS=1&Operation=GetAdHtml&MarketPlace=US&source=ac&ref=tf_til&ad_type=product_link&tracking_id=orcpub-20&marketplace=amazon&region=US&placement=0786965622&asins=0786965622&linkId=01922a9aafc4ea52eb90aed12bbeac04&show_border=false&link_opens_in_new_window=true&price_color=ffffff&title_color=f0a100&bg_color=2c3445"))

(def cos-amazon-frame
  (amazon-frame "//ws-na.amazon-adsystem.com/widgets/q?ServiceVersion=20070822&OneJS=1&Operation=GetAdHtml&MarketPlace=US&source=ac&ref=qf_sp_asin_til&ad_type=product_link&tracking_id=orcpub-20&marketplace=amazon&region=US&placement=0786965983&asins=0786965983&linkId=91dfcae14b0c8ecd3795eaf375104ca5&show_border=false&link_opens_in_new_window=true&price_color=ffffff&title_color=f0a100&bg_color=2c3445"))

(defn amazon-frame-help [frame content]
  [:div.flex.m-t-10
   [:div.flex-grow-1.p-r-5
    content]
   frame])



(def srd-link
  [:a.orange {:href "/SRD-OGL_V5.1.pdf" :target "_blank"} "the 5e SRD"])


(def plugins
  (map
   (fn [{:keys [key] :as plugin}]
     (assoc plugin :url (opt5e/source-url key)))
   (concat
    [{:name "Sword Coast Adventurer's Guide"
      :key :scag
      :race-options? true
      :class-options? true
      :background-options? true
      :selections sword-coast-adventurers-guide-selections
      :help (amazon-frame-help scag-amazon-frame
                               [:span "Incudes too many new, exciting subraces, race variants, subclasses, and backgrounds to list, as well as a ton of other info to help you create in-depth characters in the Sword Coast or elsewhere."])}
     {:name "Volo's Guide to Monsters"
      :key :vgm
      :race-options? true
      :selections volos-guide-to-monsters-selections
      :help (amazon-frame-help volos-amazon-frame
                               [:div
                                "Full of great monster race options, including"
                                (content-list ["Aasimar" "Firbolg" "Goliath" "Kenku" "Lizardfolk" "Tabaxi" "Triton" "Bugbear" "Goblin" "Hobgoblin" "Kobold" "Orc" "Yuan-Ti Pureblood"])])}
     {:name "Elemental Evil Player's Companion"
      :key :ee
      :race-options? true
      :class-options? true
      :spell-options? true
      :selections elemental-evil-selections
      :help [:div "Race and spell options from the " [:a {:href "https://media.wizards.com/2015/downloads/dnd/EE_PlayersCompanion.pdf" :target :_blank} "player's companion to Prince's of the Apocalypse"]]}
     {:name "Dungeon Master's Guide"
      :key :dmg
      :class-options? true
      :race-options? true
      :selections dmg-selections
      :help (amazon-frame-help dmg-amazon-frame
                               [:span "Includes Eladrin Elf and villainous class options, including Cleric: Death Domain and Paladin: Oathbreaker, neither of which are Adventurer's League legal."])}
     {:name "Curse of Strahd"
      :background-options? true
      :key :cos
      :selections cos-selections
      :help (amazon-frame-help cos-amazon-frame
                               [:span "Includes the Haunted One background"])}]
    ua/ua-plugins)))

(def homebrew-plugin
  (t/option-cfg
   {:name "Homebrew"
    :key :homebrew
    :icon "beer-stein"
    :modifiers [opt5e/homebrew-al-illegal
                (mod/set-mod ?option-sources :homebrew)]
    :selections [opt5e/homebrew-tool-prof-selection
                 opt5e/homebrew-skill-prof-selection
                 (opt5e/ability-increase-selection-2
                  {:min 0})
                 opt5e/homebrew-feat-selection
                 opt5e/homebrew-spell-selection]
    :help "This removes all restrictions and allows you to build your character however you want. Homebrew is not legal in the Adventurer's League."}))

(def optional-content-selection
  (t/selection-cfg
   {:name "Optional Content"
    :tags #{:optional-content}
    :help (amazon-frame-help phb-amazon-frame
                             [:span
                              "Base options are from the Player's Handbook, although descriptions are either from the "
                              srd-link
                              " or are OrcPub summaries. See the Player's Handbook for in-depth, official rules and descriptions."])
    :options (conj
              (map
               #(t/option-cfg
                 (merge-with
                  concat
                  {:modifiers [(mod/set-mod ?option-sources (:key %))]}
                  ;; don't want selections to show up
                  (select-keys % [:name :key :help :icon :modifiers])))
               plugins)
              homebrew-plugin)
    :multiselect? true
    :min 0
    :max nil}))


(def plugin-map
  (merge
   {:homebrew {:name "Homebrew"}}
   (into {} (map (juxt :key identity)
                 plugins))))

(defn al-illegal-abilities-mod [reason]
  (mod5e/al-illegal (str reason " The only legal options are 'Point Buy' or 'Standard Scores'")))

(def ability-scores-help
  [:div
   [:p "Ability scores are your major character traits and affect nearly all aspects of play. These scores range from 1 to 20 for player characters and DO NOT include racial or other bonuses."]
   [:ul.m-t-10.m-l-5
    (ability-item "Strength" "STR" "measures your physical power")
    (ability-item "Dexterity" "DEX" "measures your agility and nimbleness")
    (ability-item "Constitution" "CON" "measures your physical health")
    (ability-item "Intelligence" "INT" "measures your memory and reasoning abilities")
    (ability-item "Wisdom" "WIS" "measures your connection to your environment, how observant you are.")
    (ability-item "Charisma" "CHA" "measures how well you interact with others.")]])

(def point-buy-help
  [:div
   [:div "This allows you fine-grained control to customize your scores. You get 27 points to assign to the different scores, each value has a cost determined by the table below:"]
   [:table.m-t-10
    [:thead
     [:tr.f-w-b
      [:th.p-r-5 "Score"]
      [:th.p-r-10 "Cost"]
      [:th.p-r-5 "Score"]
      [:th "Cost"]]]
    [:tbody
     [:tr
      [:td 8]
      [:td 0]
      [:td 12]
      [:td 4]]
     [:tr
      [:td 9]
      [:td 1]
      [:td 13]
      [:td 5]]
     [:tr
      [:td 10]
      [:td 2]
      [:td 14]
      [:td 7]]
     [:tr
      [:td 11]
      [:td 3]
      [:td 15]
      [:td 9]]]]])

(defn custom-race-builder []
  [:div.m-t-10
   [:span "Name"]
   [:input.input
    {:value @(subscribe [:custom-race-name])
     :on-change (fn [e] (dispatch [:set-custom-race (.. e -target -value)]))}]])

(def base-class-options
  [barbarian-option
   bard-option
   cleric-option
   druid-option
   fighter-option
   monk-option
   paladin-option
   ranger-option
   rogue-option
   sorcerer-option
   warlock-option
   wizard-option])

(defn template-selections [magic-weapon-options magic-armor-options other-magic-item-options]
  [optional-content-selection
   (t/selection-cfg
    {:name "Base Ability Scores"
     :key :ability-scores
     :require-value? true
     :order 0
     :tags #{:ability-scores}
     :help ability-scores-help
     :options [{::t/name "Manual Entry"
                ::t/key :manual-entry
                ::t/help "This option allows you to manually type in the value for each ability. Use this if you want to roll dice yourself or if you already have a character with known ability values."
                ::t/modifiers [(mod5e/deferred-abilities)
                               (al-illegal-abilities-mod "You may not choose 'Manual Entry' for abilities.")]}
               {::t/name "Point Buy"
                ::t/key :point-buy
                ::t/help point-buy-help
                ::t/modifiers [(mod5e/deferred-abilities)]}
               {::t/name "Standard Roll"
                ::t/key :standard-roll
                ::t/help "This option rolls the dice for you. You can rearrange the values using the left and right arrow buttons."
                ::t/modifiers [(mod5e/deferred-abilities)
                               (al-illegal-abilities-mod "You may not choose 'Standard Roll' for your abilities.")]}
               {::t/name "Standard Scores"
                ::t/key :standard-scores
                ::t/help "If you aren't feeling lucky, use this option, which gives you a standard set of scores. You can reassign the values using the left and right arrow buttons."
                ::t/modifiers [(mod5e/deferred-abilities)]}]})
   (t/selection-cfg
    {:name "Alignment"
     :tags #{:description :background}
     :options (map
               (fn [alignment]
                 (t/option-cfg
                  {:name alignment
                   :modifiers [(mod5e/alignment alignment)]}))
               opt5e/alignment-titles)})
   (opt5e/race-selection
    {:options (conj
               (map
                opt5e/race-option
                [dwarf-option-cfg
                 elf-option-cfg
                 halfling-option-cfg
                 human-option-cfg
                 dragonborn-option-cfg
                 gnome-option-cfg
                 half-elf-option-cfg
                 half-orc-option-cfg
                 tiefling-option-cfg])
               opt5e/custom-race-option)})
   (opt5e/background-selection
    {:help "Background broadly describes your character origin. It also affords you two skill proficiencies and possibly proficiencies with tools or languages."
     :options (conj
               (map
                opt5e/background-option
                backgrounds)
               opt5e/custom-background-option)})
   (opt5e/feat-selection-2
    {:options opt5e/feat-options
     :show-if-zero? true
     :min 0
     :max 0})
   (opt5e/class-selection
    {:help [:div
            [:p "Class is your adventuring vocation. It determines many of your special talents, including weapon, armor, skill, saving throw, and tool proficiencies. It also provides starting equipment options. When you gain levels, you gain them in a particular class."]
            [:p.m-t-10 "Select your class using the selector at the top of the 'Class' section. Multiclassing is uncommon, but you may multiclass by clicking the 'Add Class' button at the end of the 'Class' section."]]
     :max nil
     :sequential? false
     :options base-class-options})
   (inventory-selection "Treasure" "cash" equip5e/treasure mod5e/deferred-treasure)
   (inventory-selection "Weapons" "plain-dagger" weapon5e/weapons mod5e/deferred-weapon)
   (magic-item-selection "Magic Weapons" "lightning-bow" magic-weapon-options mod5e/deferred-magic-weapon magic-item-details)
   (inventory-selection "Armor" "breastplate" armor5e/armor mod5e/deferred-armor)
   (magic-item-selection "Magic Armor" "magic-shield" magic-armor-options mod5e/deferred-magic-armor magic-item-details)
   (inventory-selection "Equipment" "backpack" equip5e/equipment mod5e/deferred-equipment)
   (magic-item-selection "Other Magic Items" "orb-wand" other-magic-item-options mod5e/deferred-magic-item magic-item-details)])

(defn template [selections]
  {::t/base t-base/template-base
   ::t/selections selections})

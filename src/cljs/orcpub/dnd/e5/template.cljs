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
            [orcpub.dnd.e5.weapons :as weapon5e]
            [orcpub.dnd.e5.equipment :as equip5e]
            [orcpub.dnd.e5.armor :as armor5e]
            [orcpub.dnd.e5.spell-lists :as sl]
            [orcpub.dnd.e5.spells :as spells]
            [orcpub.dnd.e5.magic-items :as mi])
  #_(:require-macros [orcpub.dnd.e5.options :as opt5e]
                     [orcpub.dnd.e5.modifiers :as mod5e]))

(enable-console-print!)


(def character
  {::entity/options {:ability-scores {::entity/key :standard-scores
                                      ::entity/value (char5e/abilities 15 14 13 12 10 8)}
                     :class [{::entity/key :barbarian
                              ::entity/options {:levels [{::entity/key :level-1}]}}]}})

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
  {:str "strong"
   :con "caduceus"
   :dex "body-balance"
   :int "read"
   :wis "meditation"
   :cha "aura"})

(defn ability-icon [k size]
  [:img {:class-name (str "h-" size " w-" size)
         :src (str "image/" (ability-icons k) ".svg")}])

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

(defn abilities-entry [app-state]
  [:div.flex.m-l--10.m-r--10
   (let [abilities (or (opt5e/get-raw-abilities app-state) (char5e/abilities 15 14 13 12 10 8))
         abilities-vec (vec abilities)]
     (doall
      (map-indexed
       (fn [i k]
         ^{:key k}
         [:div.m-t-10.t-a-c.p-1 
          [:div.uppercase (name k)]
          (ability-icon k 24)
          [:input.input.f-s-18.m-b-5.t-a-c.p-l-0
           {:value (k abilities)
            :on-change (fn [e] (let [value (.-value (.-target e))
                                     new-v (if (not (s/blank? value))
                                             (js/parseInt value))]
                                 (swap! app-state assoc-in [:character ::entity/options :ability-scores ::entity/value k] new-v)))}]
          (ability-modifier (k abilities))])
       char5e/ability-keys)))])

(declare template-selections)

(defn roll-hit-points [die app-state value-path]
  (let [new-val (dice/die-roll die)]
    (swap! app-state assoc-in (concat [:character] value-path) new-val)))

(defn hit-points-roller [die app-state path built-template]
  (let [value-path (entity/get-option-value-path
                    built-template
                    (:character @app-state)
                    path)
        value (get-in (:character @app-state) value-path)]
    [:div
     [:div.f-s-16.m-t-10 (str "Value: " value)]
     [:button.form-button.m-t-10
      {:on-click (fn [e]
                   (roll-hit-points die app-state value-path)
                   (.stopPropagation e))}
      "Re-Roll"]]))

(defn hit-points-entry [app-state path built-template]
  (let [value-path (entity/get-option-value-path
                    built-template
                    (:character @app-state)
                    path)
        value (get-in (:character @app-state) value-path)]
    [:div
     [:input.input
      {:value value
       :type :number
       :on-change (fn [e] (let [value (.-value (.-target e))
                               new-v (if (not (s/blank? value))
                                       (js/parseInt value))]
                           (swap! app-state assoc-in (concat [:character] value-path) new-v)))}]]))

(defn traits-modifiers [traits & [include-level? source]]
  (map
   (fn [trait]
     (mod5e/trait-cfg (assoc trait :source source)))
   traits))

(defn armor-prof-modifiers [armor-proficiencies & [cls-kw]]
  (map
   (fn [armor-prof]
     (let [[armor-kw first-class?] (if (keyword? armor-prof) [armor-prof false] armor-prof)]
       (mod5e/armor-proficiency armor-kw first-class? cls-kw)))
   armor-proficiencies))

(defn tool-prof-modifiers [tool-proficiencies & [cls-kw]]
  (map
   (fn [tool-prof]
     (let [[tool-kw first-class?] (if (keyword? tool-prof) [tool-prof false] tool-prof)]
       (mod5e/tool-proficiency tool-kw first-class? cls-kw)))
   tool-proficiencies))

(defn weapon-prof-modifiers [weapon-proficiencies & [cls-kw]]
  (map
   (fn [weapon-prof]
     (let [[weapon-kw first-class?] (if (keyword? weapon-prof) [weapon-prof false] weapon-prof)]
       (if (#{:simple :martial} weapon-kw)
         (mod5e/weapon-proficiency weapon-kw first-class? cls-kw)
         (mod5e/weapon-proficiency weapon-kw first-class? cls-kw))))
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
                              traits]}
                      app-state]
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
        (mod5e/subrace-ability k v))
      abilities)
     (traits-modifiers traits false source))))]
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
                           source
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
                    [(t/selection-cfg
                      {:name "Subrace"
                       :tags #{:subrace}
                       :options (vec (map (partial subrace-option source) subraces))})])
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
                    (mod5e/language (common/name-to-kw language)))
                  languages)
                 (map
                  (fn [[k v]]
                    (mod5e/race-ability k v))
                  abilities)
                 modifiers
                 (traits-modifiers traits false source)
                 (armor-prof-modifiers armor-proficiencies)
                 (weapon-prof-modifiers weapon-proficiencies)))}))

(def elf-weapon-training-mods
  (weapon-prof-modifiers [:longsword :shortsword :shortbow :longbow]))

(defn sunlight-sensitivity [page]
  {:name "Sunlight Sensitivity"
   :summary "Disadvantage on attack and perception rolls in direct sunlight"
   :page 24})

(def elf-option
  (race-option
   {:name "Elf"
    :help "Elves are graceful, magical creatures, with a slight build."
    :abilities {:dex 2}
    :size :medium
    :speed 30
    :languages ["Elvish" "Common"]
    :darkvision 60
    :modifiers [(mod5e/saving-throw-advantage [:charmed])
                (mod5e/immunity :magical-sleep)]
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
      :traits [(sunlight-sensitivity 24)]
      :modifiers [(mod5e/darkvision 120)
                  (mod5e/spells-known 0 :dancing-lights :cha "Dark Elf")
                  (mod5e/spells-known 1 :faerie-fire :cha "Dark Elf" 3)
                  (mod5e/spells-known 2 :darkness :cha "Dark Elf" 5)]}]
    :traits [{:name "Fey Ancestry"
              :page 23
              :summary "advantage on charmed saves and immune to sleep magic"}
             {:name "Trance"
              :page 23
              :summary "Trance 4 hrs. instead of sleep 8"}]}))

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
              :page 20},
             {:name "Stonecunning"
              :summary "2X prof bonus on stonework-related history checks"
              :page 20}]
    :subraces [{:name "Hill Dwarf",
                :abilities {:wis 1}
                :selections [(opt5e/tool-selection [:smiths-tools :brewers-supplies :masons-tools] 1)]
                :modifiers [(mod/modifier ?hit-point-level-bonus (+ 1 ?hit-point-level-bonus))]}
               {:name "Mountain Dwarf"
                :abilities {:str 2}
                :armor-proficiencies [:light :medium]}]
    :modifiers [(mod5e/damage-resistance :poison)
                (mod5e/saving-throw-advantage [:poisoned])]}))

(def halfling-option
  (race-option
   {:name "Halfling"
    :help "Halflings are small and nimble, half the height of a human, but fairly stout. They are cheerful and practical."
    :abilities {:dex 2}
    :size :small
    :speed 25
    :languages ["Halfling" "Common"]
    :modifiers [(mod5e/saving-throw-advantage [:frightened])]
    :subraces
    [{:name "Lightfoot"
      :abilities {:cha 1}
      :traits [{:name "Naturally Stealthy"
                :page 28
                :summary "Hide behind creatures larger than you"}]}
     {:name "Stout"
      :abilities {:con 1}
      :modifiers [(mod5e/damage-resistance :poison)
                  (mod5e/saving-throw-advantage [:poisoned])]}]
    :traits [{:name "Lucky"
              :page 28
              :summary "Reroll 1s on d20"}
             {:name "Halfling Nimbleness"
              :page 28
              :summary "move through the space of larger creatures"}]}))

(def human-option
  (race-option
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
                   :options [(t/option
                              "Standard Human"
                              :standard
                              []
                              [(mod5e/race-ability :str 1)
                               (mod5e/race-ability :con 1)
                               (mod5e/race-ability :dex 1)
                               (mod5e/race-ability :int 1)
                               (mod5e/race-ability :wis 1)
                               (mod5e/race-ability :cha 1)])
                             (t/option
                              "Variant Human"
                              :variant
                              [(opt5e/feat-selection 1)
                               (opt5e/skill-selection 1)
                               (opt5e/ability-increase-selection char5e/ability-keys 2 true)]
                              [])]})]}))

(defn draconic-ancestry-option [{:keys [name breath-weapon]}]
  (t/option
   name
   (common/name-to-kw name)
   []
   [(mod5e/damage-resistance (:damage-type breath-weapon))
    (mod/modifier ?draconic-ancestry-breath-weapon breath-weapon)]))

(def draconic-ancestries
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
                    :save :con}}])

(def dragonborn-option
  (race-option
   {:name "Dragonborn"
    :help "Kin to dragons, dragonborn resemble humanoid dragons, without wings or tail and standing erect. They tend to make excellent warriors."
    :abilities {:str 2 :cha 1}
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
                     :save-dc (?spell-save-dc :con)})))]
    :selections [(t/selection-cfg
                  {:name "Draconic Ancestry"
                   :tags #{:subrace}
                   :options (mapv
                             draconic-ancestry-option
                             draconic-ancestries)})]}))


(def gnome-option
  (race-option
   {:name "Gnome"
    :help "Gnomes are small, intelligent humanoids who live life with the utmost of enthusiasm."
    :abilities {:int 2}
    :size :small
    :speed 25
    :darkvision 60
    :languages ["Gnomish" "Common"]
    :modifiers [(mod5e/saving-throw-advantage [:magic] [:int :wis :cha])]
    :subraces
    [{:name "Rock Gnome"
      :abilities {:con 1}
      :modifiers [(mod5e/tool-proficiency :tinkers-tools)]
      :traits [{:name "Artificer's Lore"
                :page 37
                :summary "Add 2X prof bonus on magical, alchemical, or technological item-related history checks."}
               {:name "Tinker"
                :page 37
                :summary "Construct tiny clockwork devices."}]}
     {:name "Forest Gnome"
      :abilities {:dex 1}
      :modifiers [(mod5e/spells-known 0 :minor-illusion :int "Forest Gnome")]
      :traits [{:name "Speak with Small Beasts"
                :page 37
                :summary "Communicate with Small or smaller beasts."}]}]}))

(def half-elf-option
  (race-option
   {:name "Half-Elf"
    :help "Half-elves are charismatic, and bear a resemblance to both their elvish and human parents and share many of the traits of each."
    :abilities {:cha 2}
    :size :medium
    :speed 30
    :languages ["Common" "Elvish"]
    :selections [(opt5e/ability-increase-selection (disj (set char5e/ability-keys) :cha) 2 true)
                 (opt5e/skill-selection 2)
                 (opt5e/language-selection opt5e/languages 1)]
    :modifiers [(mod5e/saving-throw-advantage [:charmed])]}))

(def half-orc-option
  (race-option
   {:name "Half-Orc"
    :help "Half-orcs are strong and bear an unmistakable resemblance to their orcish parent. They tend to make excellent warriors, especially Barbarians."
    :abilities {:str 2 :con 1}
    :size :medium
    :speed 30
    :languages ["Common" "Orc"]
    :modifiers [(mod5e/skill-proficiency :intimidation)]
    :traits [{:name "Relentless Endurance"
              :page 41
              :summary "Drop to 1 hp instead of being reduced to 0."}
             {:name "Savage Attacks"
              :page 41
              :summary "On critical hit, add additional damage dice roll"}]}))

(def aasimar-option
  (race-option
   {:name "Aasimar"
    :abilities {:cha 2}
    :size :medium
    :speed 30
    :darkvision 60
    :source :vgm
    :languages ["Common" "Celestial"]
    :modifiers [(mod5e/damage-resistance :necrotic)
                (mod5e/damage-resistance :radiant)
                (mod5e/spells-known 0 :light :cha "Aasimar")]
    :traits [{:name "Healing Hands"
              :page 105
              :summary "Heal a creature a number of hit points equal to your level"}]
    :subraces [{:name "Protector Aasimar"
                :abilities {:wis 1}
                :modifiers [(mod5e/dependent-trait
                             {:name "Radiant Soul"
                              :level 3
                              :page 105
                              :source :vgm
                              :summary (str "For 1 minute, sprout wings (30 ft. flying speed) and deal "
                                            ?total-levels
                                            " extra radiant damage.") })]}
               {:name "Scourge Aasimar"
                :abilities {:con 1}
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
                :abilities {:str 1}
                :modifiers [(mod5e/dependent-trait
                             {:name "Necrotic Shroud"
                              :level 3
                              :page 105
                              :source :vgm
                              :summary (str "For 1 minute, creatures within 10 ft. must succeed on a DC " (?spell-save-dc :cha) " cha save or be frightened of you. During that time also deal an additional " ?total-levels " necrotic damage to one target you deal damage to with a spell or attack.")})]}]}))

(defn powerful-build [page]
  {:name "Powerful Build"
   :page page
   :source :vgm
   :summary "Count as Large for purposes of determining weight you can carry, push, drag, or lift."})

(def firbolg-option
  (race-option
   {:name "Firbolg"
    :abilities {:wis 2 :str 1}
    :size :medium
    :speed 30
    :source :vgm
    :languages ["Common" "Elvish" "Giant"]
    :modifiers [(mod5e/spells-known 1 :detect-magic :wis "Firbolg")
                (mod5e/spells-known 1 :disguise-self :wis "Firbolg" 1 "only to seem 3 ft. shorter")
                (mod5e/bonus-action
                 {:name "Hidden Step"
                  :duration {:units :round}
                  :frequency {:units :rest}
                  :page 107
                  :source :vgm
                  :summary "Turn invisible"})]
    :traits [(powerful-build 107)
             {:name "Speech of Beast and Leaf"
              :page 107
              :source :vgm
              :summary "Beast and plants can understand you and you have advantage on Charisma checks to influence them."}]}))

(def goliath-option
  (race-option
   {:name "Goliath"
    :abilities {:str 2 :con 1}
    :size :medium
    :speed 30
    :languages ["Common" "Giant"]
    :profs {:skill {:athletics true}}
    :source :vgm
    :modifiers [(mod5e/reaction
                 {:name "Stone's Endurance"
                  :frequency {:units :rest}
                  :page 109
                  :source :vgm
                  :summary (str "Reduce damage taken by 1d12 + " (:con ?ability-bonuses))})]
    
    :traits [{:name "Mountain Born"
              :page 109
              :summary "Adapted to high altitude and cold climates."}
             (powerful-build 109)]}))

(def kenku-option
  (race-option
   {:name "Kenku"
    :abilities {:dex 2 :wis 1}
    :size :medium
    :speed 30
    :source :vgm
    :languages ["Common" "Auran"]
    :modifiers [(mod5e/dependent-trait
                 {:name "Mimicry"
                  :page 111
                  :source :vgm
                  :summary (str "Mimic sounds you've heard, creatures disbelieve it with an insight check opposed to your deception check (1d20" (common/mod-str (:deception ?skill-bonuses)) ").")})]
    :profs {:skill-options {:choose 2 :options {:acrobatics true :deception true :stealth true :sleight-of-hand true}}}
    :traits [{:name "Expert Forgery"
              :page 111
              :summary "Advantage on checks to duplicate existing objects"}]}))

(def lizardfolk-option
  (race-option
   {:name "Lizardfolk"
    :abilities {:con 2 :wis 1}
    :size :medium
    :speed 30
    :source :vgm
    :languages ["Common" "Draconic"]
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
                  :frequency {:units :rest}
                  :summary (str "Special attack with your bite. If you hit, you gain "
                                (max 1 (:con ?ability-bonuses))
                                " temp. hit points")})
                (mod5e/attack
                 {:name "Bite"
                  :page 113
                  :source :vgm
                  :attack-type :melee
                  :damage-type :piercing
                  :damage-die 6
                  :damage-die-count 1
                  :damage-modifier (:str ?ability-bonuses)})]
    :profs {:skill-options {:choose 2 :options {:animal-handling true :nature true :stealth true :perception true :survival true}}}
    :traits [{:name "Cunning Artisan"
              :page 113
              :summary "Craft certain weapons and armor from creature remains."}
             {:name "Hold Breath"
              :page 113
              :summary "Up to 15 min."}]}))

(def tabaxi-option
  (race-option
   {:name "Tabaxi"
    :abilities {:dex 2 :cha 1}
    :size :medium
    :speed 30
    :darkvision 60
    :source :vgm
    :modifiers [(mod5e/climbing-speed 20)
                (mod5e/attack
                 {:name "Cat's Claws"
                  :page 115
                  :source :vgm
                  :attack-type :melee
                  :damage-type :slashing
                  :damage-die 4
                  :damage-die-count 1
                  :damage-modifier (:str ?ability-bonuses)})]
    :language-options {:choose 1 :options {:any true}}
    :profs {:skill {:perception true :stealth true}}
    :traits [{:name "Feline Agility"
              :page 115
              :summary "Double speed when moving on your turn in combat."}]}))

(def triton-option
  (race-option
   {:name "Triton"
    :abilities {:str 1 :con 1 :cha 1}
    :size :medium
    :speed 30
    :source :vgm
    :languages ["Common" "Primordial"]
    :modifiers [(mod5e/swimming-speed 30)
                (mod5e/spells-known 1 :fog-cloud :cha "Triton")
                (mod5e/spells-known 2 :gust-of-wind :cha "Triton" 3)
                (mod5e/spells-known 3 :wall-of-water :cha "Triton" 5)
                (mod5e/damage-resistance :cold)]
    :traits [{:name "Amphibious"
              :page 118
              :summary "Breath water and air"}
             {:name "Emissary of the Sea"
              :page 118
              :summary "Water-breathing beasts can understand your words."}
             {:name "Guardians of the Depths"
              :page 118
              :summary "No negative effects from deep, underwater environment."}]}))

(def bugbear-option
  (race-option
   {:name "Bugbear"
    :abilities {:str 2 :dex 1}
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
              :summary "Extra 2d6 damage when hitting a surprised creature."}]}))

(def goblin-option
  (race-option
   {:name "Goblin"
    :abilities {:dex 2 :con 1}
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
                  :summary "Disengage or Hide action as a bonus action."})]}))

(def hobgoblin-option
  (race-option
   {:name "Hobgoblin"
    :abilities {:con 2 :int 1}
    :size "Medium"
    :speed 30
    :darkvision 60
    :source :vgm
    :selections [(t/selection
                  "Martial Weapon Proficiencies"
                  (opt5e/weapon-proficiency-options (weapon5e/martial-weapons weapon5e/weapons))
                  2
                  2)]
    :modifiers [(mod5e/light-armor-proficiency)]
    :traits [{:name "Saving Face"
              :page 119
              :summary "Add a bonus to a missed roll for each ally you can see, up to 5."}]}))

(def kobold-option
  (race-option
   {:name "Kobold"
    :abilities {:dex 2 :str -2}
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
             (sunlight-sensitivity 119)]}))

(def orc-option
  (race-option
   {:name "Orc"
    :abilities {:str 2 :con 1 :int -2}
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
    :traits [(powerful-build 120)]}))

(def yuan-ti-option
  (race-option
   {:name "Yuan-Ti Pureblood"
    :abilities {:cha 2 :int 1}
    :size "Medium"
    :speed 30
    :darkvision 60
    :source :vgm
    :modifiers [(mod5e/spells-known 0 :poison-spray :cha "Yuan-Ti")
                (mod5e/spells-known 1 :animal-friendship :cha "Yuan-Ti" 1 "unlimited uses, can only target snakes")
                (mod5e/spells-known 2 :suggestion :cha "Yuan-Ti" 3 "one use per long rest")
                (mod5e/damage-immunity :poison)
                (mod5e/condition-immunity :poisoned)
                (mod5e/saving-throw-advantage [:magic])]
    :languages ["Common" "Abyssal" "Draconic"]}))

(def tiefling-option
  (race-option
   {:name "Tiefling"
    :help "Tieflings bear the distinct marks of their infernal ancestry: horns, a tail, pointed teeth, and solid-colored eyes. They are smart and charismatic."
    :abilities {:int 1 :cha 2}
    :size :medium
    :speed 30
    :darkvision 60
    :languages ["Common" "Infernal"]
    :modifiers [(mod5e/spells-known 0 :thaumaturgy :cha "Tiefling")
                (mod5e/spells-known 1 :hellish-rebuke :cha "Tiefling" 3)
                (mod5e/spells-known 2 :darkness :cha "Tiefling" 5)]}))

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
               ::t/ui-fn #(hit-points-entry %3 % %2)
               ::t/modifiers [(mod5e/deferred-max-hit-points)]}
              {::t/name (str "Roll (1D" die ")")
               ::t/key :roll
               ::t/help "This option rolls virtual dice for you and sets that value for this level's hit points. It could pay off with a high roll, but you might also roll a 1."
               ::t/ui-fn #(hit-points-roller die %3 % %2)
               ::t/select-fn #(roll-hit-points die %2 %)
               ::t/modifiers [(mod5e/deferred-max-hit-points)]}
              (let [average (dice/die-mean die)]
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
              (fn [{:keys [name key icon]}]
                (t/option-cfg
                 {:name name
                  :key key
                  :icon icon
                  :modifiers [(mod5e/tool-proficiency key)]}))
              (:values tool))
    :min num
    :max num
    :prereq-fn prereq-fn
    :tags #{:tool-profs :profs}}))

(defn tool-prof-selection [tool-options & [key prereq-fn]]
  (let [[first-key first-num] (-> tool-options first)
        first-option (equip5e/tools-map first-key)]
    (if (and (= 1 (count tool-options))
             (seq (:values first-option)))
      (tool-prof-selection-aux first-option first-num key prereq-fn)
      (t/selection-cfg
       {:name "Tool Proficiencies"
        :key key
        :options (map
                  (fn [[k num]]
                    (let [tool (equip5e/tools-map k)]
                      (if (:values tool)
                        (t/option
                         (:name tool)
                         k
                         [(tool-prof-selection-aux tool num key prereq-fn)]
                         [])
                        (t/option-cfg
                         {:name (:name tool)
                          :key (:key tool)
                          :icon (:icon tool)
                          :modifiers [(mod5e/tool-proficiency (:key tool))]}))))
                  tool-options)
        :prereq-fn prereq-fn}))))

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
      :selections (vec
                   (concat
                    selections      
                    (some-> spellcasting-template :selections (get i))))
      :modifiers (some-> levels (get i) :modifiers)})))

(defn total-levels-prereq [level]
  (fn [c] (>= (es/entity-val c :total-levels) level)))

(defn add-level-prereq [template-obj level]
  (assoc
   template-obj
   ::t/prereq-fn
   (total-levels-prereq level)))

(defn subclass-option [cls
                       {:keys [name
                               profs
                               selections
                               spellcasting
                               modifiers
                               level-modifiers
                               traits
                               levels]
                        :as subcls}]
  (let [kw (common/name-to-kw name)
        {:keys [armor weapon save skill-options tool-options tool]} profs
        {skill-num :choose options :options} skill-options
        skill-kws (if (:any options) (map :key opt5e/skills) (keys options))
        armor-profs (keys armor)
        weapon-profs (keys weapon)
        tool-profs (keys tool)
        #_spellcasting-template #_(opt5e/spellcasting-template
                               (assoc
                                spellcasting
                                :class-key
                                (or (:spell-list spellcasting) kw))
                               subcls)
        #_spell-selections #_(mapcat
                          (fn [[lvl selections]]
                            (if (= kw :eldritch-knight) (prn "LEVEL" lvl))
                            (map
                             (fn [selection]
                               (assoc selection ::t/prereq-fn (fn [c] (let [total-levels (es/entity-val c :total-levels)]
                                                                        (prn "TOTAL LEVELS" total-levels lvl)
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
                                (total-levels-prereq lvl)))
                             selections))
                          levels)
        level-modifiers (mapcat
                         (fn [[lvl {modifiers :modifiers}]]
                           (map
                            (fn [modifier]
                              (update
                               modifier
                               ::mod/conditions
                               conj
                               (total-levels-prereq lvl)))
                            modifiers))
                         levels)]
    (t/option
     name
     kw
     (map
      (fn [selection]
        (update selection ::t/tags sets/union #{(:key cls) kw}))
      (concat
       selections
       level-selections
       (if (seq tool-options) [(tool-prof-selection tool-options)])
       (if (seq skill-kws) [(opt5e/skill-selection skill-kws skill-num)])))
     (vec
      (concat
       modifiers
       level-modifiers
       [(mod5e/subclass (:key cls) kw)]
       (armor-prof-modifiers armor-profs)
       (weapon-prof-modifiers weapon-profs)
       (tool-prof-modifiers tool-profs)
       (traits-modifiers traits true))))
    #_(if spellcasting-template
      (assoc
       option
       ::t/plugins [{::t/path [:class (:key cls)]
                     ::t/selections [(t/sequential-selection
                                      "Levels"
                                      (fn [selection options current-values]
                                        {::entity/key (-> current-values count inc str keyword)})
                                      (vec
                                       (map
                                        (partial subclass-level-option subcls kw spellcasting-template)
                                        (range 1 21))))]}])
      option)))

(defn first-class? [class-kw]
  (fn [c] (= class-kw (first (es/entity-val c :classes)))))

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
                    spellcasting-template
                    i]
  (let [ability-inc-set (set ability-increase-levels)
        level-kw (level-key i)]
    (t/option-cfg
     {:name (level-name i)
      :key level-kw
      :order i
      :selections (mapv
                   (fn [selection]
                     (update selection ::t/tags sets/union #{:level level-kw}))
                   (concat
                    (some-> levels (get i) :selections)
                    (some-> spellcasting-template :selections (get i))
                    (if (= i subclass-level)
                      [(t/selection-cfg
                        {:name subclass-title
                         :key (common/name-to-kw subclass-title)
                         :help subclass-help
                         :tags #{:subclass}
                         :options (mapv
                                   #(subclass-option (assoc cls :key kw) %)
                                   subclasses)})])
                    (if (and (not plugin?) (ability-inc-set i))
                      [(opt5e/ability-score-improvement-selection name i)])
                    (if (not plugin?)
                      [(assoc
                        (hit-points-selection hit-die name i)
                        ::t/prereq-fn
                        (fn [c] (or (not (= kw (first (es/entity-val c :classes))))
                                    (> i 1))))])))
      :modifiers (vec
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
                   (if (and (not plugin?) (= i 1)) [(assoc
                                                     (mod5e/max-hit-points hit-die)
                                                     ::mod/conditions
                                                     [(first-class? kw)])])
                   [(mod5e/level kw name i hit-die)]))})))


(defn equipment-option [[k num]]
  (let [equipment (equip5e/equipment-map k)]
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
       (-> k equip5e/equipment-map :name (str (if (> num 1) (str " (" num ")") "")))
       k
       []
       [(mod5e/equipment k num)]))))

(defn simple-weapon-selection [num]
  (t/selection-cfg
   {:name "Starting Equipment: Simple Weapon"
    :tags #{:starting-equipment}
    :options (opt5e/weapon-options (weapon5e/simple-weapons weapon5e/weapons))
    :min num
    :max num}))

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
              [(t/selection-cfg
                {:name "Starting Equipment: Martial Weapon"
                 :tags #{:starting-equipment}
                 :options (opt5e/weapon-options (weapon5e/martial-weapons weapon5e/weapons))
                 :min num
                 :max num})]
              [])
    (t/option
     (-> k weapon5e/weapons-map :name (str (if (> num 1) (str " (" num ")") "")))
     k
     []
     [(mod5e/weapon k num)])))

(defn armor-option [[k num]]
  (t/option
     (-> k armor5e/armor-map :name)
     k
     []
     [(mod5e/armor k num)]))

(defn class-options [option-fn choices help]
  (map
   (fn [{:keys [name options]}]
     (t/selection-cfg
      {:name (str "Starting Equipment: " name)
       :help help
       :order 0
       :tags #{:starting-equipment}
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
                            source
                            weapon-choices
                            weapons
                            equipment
                            equipment-choices
                            armor
                            armor-choices
                            spellcasting]
                     :as cls}]
  (let [kw (common/name-to-kw name)
        {:keys [save skill-options multiclass-skill-options tool-options multiclass-tool-options tool]
         armor-profs :armor weapon-profs :weapon} profs
        save-profs (keys save)
        spellcasting-template (opt5e/spellcasting-template (assoc spellcasting :class-key kw) cls)]
    (t/option-cfg
     {:name name
      :key kw
      :help help
      :selections (mapv
                   (fn [selection]
                     (update selection ::t/tags sets/union #{kw}))
                   (concat
                    selections
                    (if (seq tool-options)
                      [(tool-prof-selection tool-options :tool-selection (fn [c] (= kw (first (:classes c)))))])
                    (if (seq multiclass-tool-options)
                      [(tool-prof-selection multiclass-tool-options :multiclass-tool-selection (fn [c] (not= kw (first (:classes c)))))])
                    (if weapon-choices (class-weapon-options weapon-choices))
                    (if armor-choices (class-armor-options armor-choices))
                    (if equipment-choices (class-equipment-options equipment-choices))
                    (if skill-options
                      [(class-skill-selection skill-options :skill-proficiency (fn [c] (= kw (first (:classes c)))))])
                    (if multiclass-skill-options
                      [(class-skill-selection multiclass-skill-options :multiclass-skill-proficiency (fn [c] (not= kw (first (:classes c)))))])
                    [(t/selection-cfg
                      {:name (str name " Levels")
                       :key :levels
                       :help "These are your levels in the containing class. You can add levels by clicking the 'Add Levels' button below."
                       :new-item-text "Level Up (Add a Level)"
                       :new-item-fn (fn [selection options current-values]
                                      {::entity/key (-> current-values count inc level-key)})
                       :tags #{kw}
                       :options (vec
                                 (map
                                  (partial level-option cls kw spellcasting-template)
                                  (range 1 21)))
                       :min 1
                       :sequential? true
                       :max nil})]))
      :modifiers (vec
                  (concat
                   modifiers
                   (if armor-profs (armor-prof-modifiers armor-profs kw))
                   (if weapon-profs (weapon-prof-modifiers weapon-profs kw))
                   (if tool (tool-prof-modifiers tool kw))
                   (if weapons
                     (mapv
                      (fn [[k num]]
                        (mod5e/weapon k num))
                      weapons))
                   (if armor
                     (mapv
                      (fn [[k num]]
                        (mod5e/armor k num))
                      armor))
                   (if equipment
                     (mapv
                      (fn [[k num]]
                        (mod5e/equipment k num))
                      equipment))
                   (remove
                    nil?
                    [(mod5e/cls kw)
                     (if save-profs (apply mod5e/saving-throws kw save-profs))])))})))


(defn class-level [levels class-kw]
  (get-in levels [class-kw :class-level]))

(def barbarian-option
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
    :modifiers [(mod/vec-mod ?unarmored-defense :barbarian)
                (mod/cum-sum-mod ?unarmored-ac-bonus (?ability-bonuses :con)
                                 nil
                                 nil
                                 [(= :barbarian (first ?unarmored-defense))])
                (mod/cum-sum-mod ?unarmored-with-shield-ac-bonus (?ability-bonuses :con)
                                 nil
                                 nil
                                 [(= :barbarian (first ?unarmored-defense))])
                (mod5e/bonus-action
                 (let [barbarian-level (class-level ?levels :barbarian)
                       attack-bonus (condp <= barbarian-level
                                      16 4
                                      9 3
                                      2)]
                   {:name "Rage"
                    :page 48
                    :duration {:units :minute}
                    :frequency {:units :rest
                                :amount (condp <= barbarian-level
                                          17 6
                                          12 5
                                          6 4
                                          3 3
                                          2)}
                    :summary (str "Advantage on Strength checks and saves; melee attack bonus " (common/bonus-str attack-bonus) "; resistance to bludgeoning, piercing, and slashing damage")}))]
    :levels {5 {:modifiers [(mod5e/extra-attack)
                            (mod/modifier ?speed-with-armor (fn [armor] (if (not= :heavy (:type armor))
                                                                            (+ 10 ?speed)
                                                                            ?speed)))]}
             9 {:modifiers [(mod5e/dependent-trait
                             {:name "Brutal Critical"
                              :page 49
                              :summary (let [die-count (condp <= (class-level ?levels :barbarian)
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
                               :summary (let [str-score (:str ?abilities)]
                                          (str "Min strength check value is " str-score))})]}
             20 {:modifiers [(mod5e/ability :str 4)
                            (mod5e/ability :con 4)]}}
    :traits [{:name "Reckless Attack"
              :level 2
              :page 48
              :summary "Advantage on attacks using Strength, attack against you have advantage as well."}
             {:name "Danger Sense"
              :level 2
              :page 48
              :summary "Advantage on Dexterity saves against effects you can see."}
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
                                             :summary (str "Frighten (Wisdom save DC " (?spell-save-dc :cha) ") a creature with 30 ft.")})]}
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

(def bard-option
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
                         :options (zipmap (map :key equip5e/musical-instruments) (repeat 1))}]
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
    :modifiers [(mod5e/bonus-action
                 {:name "Bardic Inspiration"
                  :page 53
                  :summary (str "Inspire another creature with a 1d"
                                (bardic-inspiration-die ?levels)
                                " that it can, within the next 10 min., add to a d20 roll")})]
    :levels {2 {:modifiers [(mod/modifier ?default-skill-bonus (let [b (int (/ ?prof-bonus 2))]
                                                                 (zipmap char5e/ability-keys (repeat b))))
                            (mod5e/dependent-trait
                             {:name "Song of Rest"
                              :page 54
                              :level 2
                              :summary (str "With a song, you and friendly creatures gain 1d"
                                            (mod5e/level-val
                                             (class-level ?levels :bard)
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
                               {:name "Cutting Wounds"
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
                  :levels {6 {:modifiers [(mod5e/extra-attack)]}
                           14 {:modifiers [(mod5e/bonus-action
                                           {:name "Battle Magic"
                                            :page 55
                                            :summary "make a weapon attack when you use your action to cast a bard spell"})]}}}]}))

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

(defn potent-spellcasting [page]
  (mod5e/dependent-trait
   {:level 8
    :page page
    :summary (str "Add "
                  (common/bonus-str (?ability-bonuses :wis))
                  " to damage from cantrips you cast")
    :name "Potent Spellcasting"}))

(defn divine-strike [damage-desc page]
  (mod5e/dependent-trait
   {:level 8
    :name "Divine Strike"
    :page 60
    :summary (str "Once each of your turns, add "
                  (if (>= (?class-level :cleric) 14) 2 1)
                  "d8 "
                  damage-desc
                  " damage to a successful weapon attack's damage")}))

(defn starting-equipment-option [equipment num]
  (t/option-cfg
   {:name (:name equipment)
    :key (:key equipment)
    :modifiers [(mod5e/equipment (:key equipment) num)]}))

(defn starting-equipment-selection [{:keys [name options] :as cfg}]
  (t/selection-cfg
   (merge cfg
          {:name (str "Starting Equipment: " name)
           :tags #{:equipment :starting-equipment}
           :order 0
           :options (conj options
                          (t/option-cfg
                           {:name "<none>"
                            :key :none}))})))


(def cleric-option
  (class-option
   {:name "Cleric",
    :spellcasting {:level-factor 1
                   :cantrips-known {1 3 4 1 10 1}
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
                                   :explorers-pack 1}}]
    :weapon-choices [{:name "Cleric Weapon"
                      :options {:mace 1
                                :warhammer 1}}]
    :armor-choices [{:name "Armor"
                     :options {:scale-mail 1
                               :leather 1
                               :chain-mail 1}}]
    :armor {:shield 1}
    :selections [(starting-equipment-selection
                  {:name "Additional Weapon"
                   :options [(t/option-cfg
                              {:name "Light Crossbow and 20 Bolts"
                               :modifiers [(mod5e/weapon :crossbow-light 1)
                                           (mod5e/equipment :crossbow-bolt 20)]})
                             (weapon-option [:simple 1])]})
                 (starting-equipment-selection
                  {:name "Holy Symbol"
                   :options (mapv
                             starting-equipment-option
                             equip5e/holy-symbols)})]
    :levels {2 {:modifiers [(mod5e/action
                             {:level 2
                              :page 59
                              :name "Channel Divinity: Turn Undead"
                              :summary (str "undead within 30 feet must make a DC "
                                            (?spell-save-dc :wis)
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
                                         (if (= 20 (class-level ?levels :cleric))
                                           "automatically"
                                           "if you make a percentile roll less than or equal to your cleric level"))})]}}
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
                  :levels {2 {:modifiers [(mod5e/action
                                           {:name "Channel Divinity: Preserve Life"
                                            :summary (str "Distribute "
                                                          (* 5 (?class-level :cleric))
                                                          " HPs healing among any creatures within 30 ft., each can be restored to at most 1/2 their HP max")})]}
                           8 {:modifiers [(divine-strike "radiant" 60)]}}
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
                               (t/selection-cfg
                                {:name "Blessings of Knowledge Skills"
                                 :tags #{:class}
                                 :options (map
                                           blessings-of-knowledge-skill
                                           ["Arcana" "History" "Nature" "Religion"])
                                 :min 2
                                 :max 2})]
                  :levels {6 {:modifiers [(mod5e/action
                                           {:level 6
                                            :page 59
                                            :name "Channel Divinity: Read Thoughts"
                                            :summary (str "a creature within 60 ft. must make a DC "
                                                          (?spell-save-dc :wis)
                                                          " Wisdom save or you can read it's thoughts for 1 min, use an action to end the effect and cast suggestion without using a slot and with no save")})]}
                           8 {:modifiers [(potent-spellcasting 60)]}}
                  :traits [{:level 2
                            :page 59
                            :summary "Become proficient in a tool or skill for 10 mins."
                            :name "Channel Divinity: Knowledge of the Ages"}
                           {:level 17
                            :page 60
                            :name "Visions of the Past"
                            :summary "Learn the history of an object you hold or area you are in"}]}
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
                              (cleric-spell 5 :scrying 9)
                              (mod5e/reaction
                               {:name "Warding Flare"
                                :page 61
                                :summary "impose disadvantage on an attack roll"
                                :frequency {:units :long-rest
                                            :amount (max 1 (?ability-bonuses :wis))}})]
                  :levels {2 {:modifiers [(mod5e/action
                                           {:level 2
                                            :name "Channel Divinity: Radiance of the Dawn"
                                            :page 61
                                            :range {:plural :feet
                                                    :amount 30}
                                            :summary (str "Dispel magical darkness and deal 2d10 + "
                                                          (?class-level :cleric)
                                                          " radiant damage (half on successful DC "
                                                          (?spell-save-dc :wis)
                                                          " Constitution save) to hostile creatures")})]}
                           6 {:modifiers [(mod5e/reaction
                                           {:level 6
                                            :name "Improved Flare"
                                            :page 61
                                            :summary "use warding flare when another creature within 30 ft. is attacked"})]}
                           8 {:modifiers [(potent-spellcasting 61)]}
                           17 {:modifiers [(mod5e/action
                                            {:level 17
                                             :page 61
                                             :name "Corona of Light"
                                             :summary "emit bright light for 60 ft. and 30 beyond that, enemies in the bright light have disadvantage on saves against spells that deal radiant or fire damage"})]}}}
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
                  :levels {2 {:modifiers [(mod5e/action
                                           {:name "Channel Divinity: Charm Animals and Plants"
                                            :level 2
                                            :page 62
                                            :range {:plural :feet
                                                    :amount 30}
                                            :summary (str "charm beasts and plant creatures unless they succeed on a DC "
                                                          (?spell-save-dc :wis)
                                                          " Wisdom save")})]}
                           6 {:modifiers [(mod5e/reaction
                                           {:name "Dampen Elements"
                                            :level 6
                                            :page 62
                                            :range {:plural :feet
                                                    :amount 30}
                                            :summary "to a creature that takes fire, cold, acid, lighting, or thunder damage, grant resistance to that damage"})]}
                           8 {:modifiers [(divine-strike "cold, fire, or lighting" 62)]}
                           17 {:modifiers [(mod5e/bonus-action
                                            {:name "Master of Nature"
                                             :level 17
                                             :page 62
                                             :summary "command creatures charmed with your Charm Animals and Plants"})]}}
                  :selections [(t/selection-cfg
                                {:name "Druid Cantrip"
                                 :tags #{:spells}
                                 :options (opt5e/spell-options (get-in sl/spell-lists [:druid 0]) 0 :wis "Druid")})]}
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
                              (cleric-spell 5 :insect-plague 9)
                              (mod5e/reaction
                               {:name "Wrath of the Storm"
                                :page 62
                                :frequency {:units :long-rest
                                            :amount (?ability-bonuses :wis)}
                                :summary (str "When a creature within 5 ft. hits you, you deal 2d8 lightning or thunder damage to them (half that on successful DC "
                                              (?spell-save-dc :wis)
                                              " Dexterity save).")})]
                  :levels {2 {:modifiers [(mod5e/trait-cfg
                                           {:name "Channel Divinity: Destructive Wrath"
                                            :page 62
                                            :level 2
                                            :summary "Rather than roll lighting or thunder damage, deal max damage"})]}
                           8 {:modifiers [(divine-strike "thunder" 62)]}}
                  :traits [{:name "Thunderbolt Strike"
                            :page 62
                            :level 6
                            :summary "Push a Large or smaller creature up to 10 ft. when you deal lightning damage to it"}
                           {:name "Stormborn"
                            :page 62
                            :summary "Flying speed equal to your walking speed"}]}
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
                              (cleric-spell 5 :modify-memory 9)
                              (mod5e/action
                               {:name "Blessing of the Trickster"
                                :page 63
                                :duration {:units :hour}
                                :summary "Give another creature advantage on stealth checks"})]
                  :levels {2 {:modifiers [(mod5e/action
                                           {:name "Channel Divinity: Invoke Duplicity"
                                            :level 2
                                            :page 63
                                            :summary "create illusion of yourself for 1 min. or concentration. Move it 30 ft. as a bonus action, cast spells as if in illusion's space, gain advantage on a creature both you and the illusion are within 5 ft. of"})]}
                           6 {:modifiers [(mod5e/action
                                           {:name "Channel Divinity: Cloak of Shadows"
                                            :level 6
                                            :page 63
                                            :summary "become invisible until end of your next turn"})]}
                           8 {:modifiers [(divine-strike "poison" 63)]}
                           17 {:modifiers [(mod5e/action
                                            {:name "Improved Duplicity"
                                             :level 17
                                             :page 63
                                             :summary "when you use Invoke Duplicity, create up to 4 duplicates"})]}}}
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
                              (cleric-spell 5 :hold-monster 9)
                              (mod5e/bonus-action
                               {:name "War Priest"
                                :level 1
                                :page 63
                                :frequency {:units :long-rest
                                            :amount (?ability-bonuses :wis)}
                                :summary "make one extra weapon attack when you use the Attack action"})]
                  :levels {6 {:modifiers [(mod5e/reaction
                                           {:name "Channel Divinity: War God's Blessing"
                                            :level 6
                                            :page 63
                                            :summary "+10 to an attack roll made by a creature within 30 ft."})]}
                           8 {:modifiers [(divine-strike nil 63)]}}
                  :traits [
                           {:name "Channel Divinity: Guided Strike"
                            :page 63
                            :level 2
                            :summary "+10 to an attack roll"}
                           
                           {:name "Avatar of Battle"
                            :page 63
                            :level 17
                            :summary "from non-magical weapons, resistance to slashing, bludgeoning, and piercing damage"}]}]}))

(defn druid-spell [spell-level spell-key min-level]
  (mod5e/spells-known spell-level spell-key :wis "Druid" min-level))

(defn lands-stride [level]
  {:name "Land's Stride"
   :level level
   :page 69
   :summary "moving through nonmagical difficult terrain costs no extra movement, pass through nonmagical plants without being slowed by them and without taking damage from them"})

(def druid-option
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
                              :frequency {:units :rest
                                          :amount 2}
                              :duration {:units :hour
                                         :amount (int (/ (?class-level :druid) 2))}
                              :summary (str "You can transform into a beast you have seen with CR "
                                            ?wild-shape-cr
                                            (if ?wild-shape-limitation (str " and " ?wild-shape-limitation)))})]}}
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
                                           (mod5e/condition-immunity :charmed "only by elementals or fey")
                                           (mod5e/condition-immunity :frightened "only by elementals or fey")
                                           (mod5e/immunity :disease)]}
                           14 {:modifiers [(mod5e/dependent-trait
                                            {:name "Nature's Santuary"
                                             :level 14
                                             :page 69
                                             :summary (str "beast or plant creatures must make a DC "
                                                           (?spell-save-dc :wis)
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
                  :traits [
                           
                           {:name "Primal Strike"
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

(def eldritch-knight-cfg
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
   :modifiers [(mod5e/bonus-action
                {:name "Summon Bonded Weapon"
                 :page 75
                 :summary "If on the same plane of existence, instantly teleport a bonded weapon into your hand"})]
   :levels {3 {:selections [(assoc
                             (opt5e/spell-selection
                              :wizard
                              1
                              :int
                              "Eldritch Knight"
                              3)
                             ::t/name "Eldritch Knight: Level 1 Spell"
                             ::t/tags #{:spells})]}
            7 {:modifiers [(mod5e/bonus-action
                            {:name "War Magic"
                             :page 75
                             :summary "make a weapon attack if you used your action to cast a cantrip"})]}
            18 {:modifiers [(mod5e/bonus-action
                             {:name "Improved War Magic"
                              :page 75
                              :summary "make a weapon attack if you used your action to cast a spell"})]}}
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

(def fighter-option
  (class-option
   {:name "Fighter",
    :hit-die 10,
    :ability-increase-levels [4 6 8 12 16 19]
    :profs {:armor {:light false :medium false :heavy true :shields false}
            :weapon {:simple false :martial false} 
            :save {:str true :con true}
            :skill-options {:choose 2 :options {:acrobatics true :animal-handling true :athletics true :history true :insight true :intimidation true :perception true :survival true}}}
    :equipment-choices [{:name "Equipment Pack"
                         :options {:dungeoneers-pack 1
                                   :explorers-pack 1}}]
    :modifiers [(mod5e/bonus-action
                 {:name "Second Wind"
                  :page 72
                  :frequency {:units :rest}
                  :summary (str "regain 1d10 "
                                (common/mod-str (?class-level :fighter))
                                " HPs")})
                (mod5e/action
                 {:level 2
                  :name "Action Surge"
                  :page 72
                  :frequency {:units :rest
                              :amount (if (>= (?class-level :fighter) 17)
                                        2
                                        1)}
                  :summary "take an extra action"})]
    :levels {5 {:modifiers [(mod5e/extra-attack)]}
             9 {:modifiers [(mod5e/dependent-trait
                             {:level 9
                              :name "Indomitable"
                              :page 72
                              :frequency {:units :long-rest
                                          :amount (mod5e/level-val
                                                   (?class-level :fighter)
                                                   {13 2
                                                    17 3
                                                    :default 1})}
                              :summary "reroll a save if you fail"})]}
             10 {:modifers [(opt5e/fighting-style-selection)]}
             11 {:modifiers [(mod5e/extra-attack)]}
             20 {:modifiers [(mod5e/extra-attack)]}}
    :subclass-level 3
    :subclass-title "Martial Archetype"
    :selections [(opt5e/fighting-style-selection)
                 (t/selection
                  "Starting Equipment: Armor"
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
                  "Starting Equipment: Weapons"
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
                  "Starting Equipment: Additional Weapons"
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
                                (opt5e/fighting-style-selection)
                                10)]
                  :levels {3 {:modifiers [(mod5e/critical 19)]}
                           7 {:modifiers [(mod/modifier ?default-skill-bonus (let [b (int (/ ?prof-bonus 2))] {:str b :dex b :con b}))
                                          (mod5e/dependent-trait
                                           {:level 7
                                            :name "Remarkable Athlete"
                                            :page 72
                                            :summary (str "+"
                                                          (common/round-up (/ ?prof-bonus 2))
                                                          " to STR, DEX, or CON checks that don't already include prof bonus; running long jump increases by "
                                                          (?ability-bonuses :str)
                                                          " ft.")})]}
                           15 {:modifiers [(mod5e/critical 18)]}
                           18 {:modifiers [(mod5e/dependent-trait
                                            {:level 18
                                             :page 73
                                             :name "Survivor"
                                             :summary (str "At start of your turns, if you have at most "
                                                           (int (/ ?max-hit-points 2))
                                                           " HPs left, regain "
                                                           (+ 5 (?ability-bonuses :con)) " HPs")})]}}
                  :traits []}
                 {:name "Battle Master"
                  :selections [(t/selection
                                "Martial Maneuvers"
                                opt5e/maneuver-options
                                3 3)
                               (opt5e/tool-selection (map :key equip5e/artisans-tools) 1)]
                  :modifiers [(mod/modifier ?maneuver-save-dc (max (?spell-save-dc :dex)
                                                                   (?spell-save-dc :str)))
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
                  :traits [{:name "Know Your Enemy"
                            :level 7
                            :page 73
                            :summary "Study a creature outside combat for 1 min. to learn if it is superior, inferior, or equal in STR, DEX, CON, AC, current HP, total levels, fighter levels"}
                           {:name "Relentless"
                            :level 15
                            :page 74
                            :summary "you regain 1 superiority die when you roll iniative and have no remaining superiority dice"}]}
                 eldritch-knight-cfg]}))

(defn evasion [level page]
  {:name "Evasion"
   :page page
   :level level
   :summary "when you succeed on a DEX save to take half damage, you take none, if you fail, you take half"})

(def monk-option
  (class-option
   {:name "Monk"
    :hit-die 8
    :ability-increase-levels [4 8 10 16 19]
    :unarmored-abilities [:wis]
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
    :modifiers [(mod/vec-mod ?unarmored-defense :monk)
                (mod/cum-sum-mod ?unarmored-ac-bonus
                                 (?ability-bonuses :wis)
                                 nil
                                 nil
                                 [(= :monk (first ?unarmored-defense))])
                (mod5e/attack
                 (let [die (mod5e/level-val
                                       (?class-level :monk)
                                       {5 6
                                        11 8
                                        17 10
                                        :default 4})]
                   {:name "Martial Arts"
                    :damage-die die
                    :damage-die-count 1
                    :damage-modifier (max (?ability-bonuses :str) (?ability-bonuses :dex))
                    :summary "Unarmed strike or monk weapon"}))
                (mod5e/bonus-action
                 {:name "Martial Arts"
                  :page 78
                  :summary "Make an extra unarmed strike when you take Attack action"})]
    :levels {2 {:modifiers [(mod5e/unarmored-speed-bonus 10)
                            (mod5e/dependent-trait
                             {:name "Ki"
                              :page 78
                              :level 2
                              :summary (str "You have " ?total-levels " ki points")})
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
                              :summary (str "When hit by a ranged attack, reduce the damage by 1d10 " (common/mod-str (+ (?ability-bonuses :dex) (?class-level :monk))) ". If you reduce it to 0, you can catch the missile and use it in a ranged attack as a monk weapon with range 20/60")})]}
             4 {:modifiers [(mod5e/reaction
                             {:name "Slow Fall"
                              :page 78
                             :level 4
                             :summary (str "reduce falling damage by " (* 5  (?class-level :monk)))})]}
             5 {:modifiers [(mod5e/extra-attack)
                            (mod5e/dependent-trait
                             {:name "Stunning Strike"
                              :page 79
                              :level 5
                              :summary (str "when you hit a creature with melee attack, spend 1 ki point to stun the creature if it fails a DC " (?spell-save-dc :wis) " CON save")})]}
             6 {:modifiers [(mod5e/unarmored-speed-bonus 5)]}
             7 {:modifiers [(mod5e/action
                             {:name "Stillness of Mind"
                              :page 79
                              :summary "end one effect causing you to be charmed or frightened"})]}
             10 {:modifiers [(mod5e/damage-immunity :poison)
                             (mod5e/damage-immunity :disease)
                             (mod5e/unarmored-speed-bonus 5)]}
             13 {:modifiers (map
                             (fn [{:keys [name key]}]
                               (mod5e/language key))
                             opt5e/languages)}
             14 {:modifiers [(mod5e/saving-throws char5e/ability-keys)
                             (mod5e/unarmored-speed-bonus 5)]}
             18 {:modifiers [(mod5e/unarmored-speed-bonus 5)
                             (mod5e/action
                              {:name "Empty Body: Invisibility"
                               :level 18
                               :page 79
                               :duration {:units :minute}
                               :summary "spend 4 ki points to become invisible and have resistance to all damage but force damage"})
                             (mod5e/action
                              {:name "Empty Body: Astral Projection"
                               :page 79
                               :level 18
                               :summary "use 8 ki points to cast the astral projection spell"})]}}
    :equipment {:dart 10}
    :traits [{:name "Ki-Empowered Strikes"
              :page 79
              :level 6
              :summary "your unarmed strikes count as magical"}
             (evasion 7 79)
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
    :subclass-level 3
    :subclass-title "Monastic Tradition"
    :subclasses [{:name "Way of the Open Hand"
                  :modifiers [(mod5e/dependent-trait
                               {:name "Open Hand Technique"
                                :page 79
                                :summary (str "when you hit with Flurry of Blows, you impose one of the effects on the target: 1) must make a DC "(?spell-save-dc :wis) " DEX save or be knocked prone. 2) make a DC " (?spell-save-dc :wis) " STR save or be pushed 15 ft. 3) can't take reactions until end of your next turn")})]
                  :levels {6 {:modifiers [(mod5e/action
                                           {:name "Wholeness of Body"
                                            :page 79
                                            :level 6
                                            :frequency {:units :long-rest}
                                            :summary (str "heal yourself " (* 3 (?class-level :monk)) " HPs")})]}
                           11 {:modifiers [(mod5e/dependent-trait
                                            {:name "Tranquility"
                                             :page 80
                                             :level 11
                                             :summary (str "gain effects of sanctuary spell (save DC " (?spell-save-dc :wis) ") between rests")})]}
                           17 {:modifiers [(mod5e/dependent-trait
                                            {:name "Quivering Palm"
                                             :level 17
                                             :page 80
                                             :summary (str "when you hit a creature with unarmed strike, set up vibrations that last " (?class-level :monk) " days. Use an action to end the vibrations, reducing the target to 0 HPs on failed DC " (?spell-save-dc :wis) " CON save. It takes 10d10 necrotic damage on successful save.")})]}}}
                 {:name "Way of Shadow"
                  :modifiers [(mod5e/spells-known 0 :minor-illusion :wis "Monk (Way of Shadow)")
                              (mod5e/action
                               {:name "Shadow Arts"
                                :page 80
                                :summary "spend 2 ki to cast darkness, darkvision, pass without trace, or silence spells"})]
                  :levels {6 {:modfifiers [(mod5e/bonus-action
                                            {:name "Shadow Step"
                                             :page 80
                                             :summary "teleport 60 and gain advantage on first melee attack before end of turn"})]}
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
                  :levels {3 {:selections [(opt5e/monk-elemental-disciplines)]}
                           6 {:selections [(opt5e/monk-elemental-disciplines)]}
                           11 {:selections [(opt5e/monk-elemental-disciplines)]}
                           17 {:selections [(opt5e/monk-elemental-disciplines)]}}
                  :traits [{:name "Elemental Attunement"
                            :page 81
                            :summary "create minor elemental effect"}]}
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
                            :level 17}]}]}))

(defn paladin-spell [spell-level key min-level]
  (mod5e/spells-known spell-level key :wis "Paladin" min-level))

(def paladin-option
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
    :levels {2 {:selections [(opt5e/fighting-style-selection #{:defense :dueling :great-weapon-fighting :protection})]}
             3 {:modifiers [(mod5e/damage-immunity :disease)]}
             5 {:modifiers [(mod5e/extra-attack)]}}
    :modifiers [(mod/modifier ?paladin-aura (if (< (?class-level :paladin) 18) 10 30))
                (mod5e/action
                 {:name "Divine Sense"
                  :page 84
                  :frequency {:units :long-rest
                              :amount (inc (?ability-bonuses :cha))}
                  :summary "within 60 ft., detect presense of undead, celestial, or fiend. Also detect consecrated or desecrated object or place"})
                (mod5e/action
                 {:name "Lay on Hands"
                  :page 84
                  :frequency {:units :long-rest}
                  :summary (str "you have a healing pool of " (* 5 (?class-level :paladin)) " HPs, with it you can heal a creature or expend 5 points to cure disease or neutralize poison")})
                (mod5e/dependent-trait
                 {:name "Aura of Protection"
                  :level 6
                  :page 85
                  :summary (str "you and friendly creatures within " ?paladin-aura " ft. have a " (common/bonus-str (max 1 (?ability-bonuses :cha))) " bonus to saves")})
                (mod5e/dependent-trait
                 {:name "Aura of Courage"
                  :level 10
                  :page 85
                  :summary (str (str "you and friendly creatures within " ?paladin-aura " ft. can't be frightened"))})
                (mod5e/action
                 {:name "Cleansing Touch"
                  :level 14
                  :page 85
                  :frequency {:units :long-rest
                              :amount (?ability-bonuses :cha)}
                  :summary "end a spell on yourself or willing creature"})
                (mod5e/dependent-trait
                 {:name "Channel Divinity"
                  :page 85
                  :level 3
                  :frequency {:units :rest}
                  :summary "your oath provides specific options"})]
    :selections [(t/selection
                  "Starting Equipment: Weapons"
                  [(t/option-cfg
                    {:name "Martial Weapon and Shield"
                     :selections [(t/selection
                                   "Martial Weapon"
                                   (opt5e/martial-weapon-options 1))]
                     :modifiers [(mod5e/armor :shield 1)]})
                   (t/option-cfg
                    {:name "Two Martial Weapons"
                     :selections [(t/selection
                                   "Martial Weapons"
                                   (opt5e/martial-weapon-options 1)
                                   2
                                   2)]})])
                 (t/selection
                  "Starting Equipment: Melee Weapon"
                  [(t/option-cfg
                    {:name "Five Javelins"
                     :modifiers [(mod5e/weapon :javelin 5)]})
                   (t/option-cfg
                    {:name "Simple Melee Weapon"
                     :selections [(t/selection
                                   "Simple Melee Weapon"
                                   (opt5e/simple-melee-weapon-options 1))]})])]
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
                  :modifiers [(paladin-spell 1 :protection-from-evil-and-good 3)
                              (paladin-spell 1 :sanctuary 3)
                              (paladin-spell 2 :lesser-restoration 5)
                              (paladin-spell 2 :zone-of-truth 5)
                              (paladin-spell 3 :beacon-of-hope 9)
                              (paladin-spell 3 :dispel-magic 9)
                              (paladin-spell 4 :freedom-of-movement 13)
                              (paladin-spell 4 :guardian-of-faith 13)
                              (paladin-spell 5 :commune 17)
                              (paladin-spell 5 :flame-strike 17)
                              (mod5e/action
                               {:name "Channel Divinity: Sacred Weapon"
                                :page 86
                                :duration {:units :minute}
                                :summary (str "make a weapon magical, with a " (common/bonus-str (max 1 (?ability-bonuses :cha))) " attack bonus and magical light (20 ft./20 ft.)")})
                              (mod5e/action
                               {:name "Channel Divinity: Turn the Unholy"
                                :page 86
                                :duration {:units :minute}
                                :summary (str "each undead or fiend within 30 ft. must make a DC " (?spell-save-dc :cha) " WIS save or be turned for 1 min.")})]
                  :levels {7 {:modifiers [(mod5e/dependent-trait
                                           {:name "Aura of Devotion"
                                            :level 7
                                            :page 86
                                            :summary (str "you and friendly creatures within " ?paladin-aura " ft. can't be charmed")})]}
                           20 {:modifiers [(mod5e/action
                                            {:name "Holy Nimbus"
                                             :level 20
                                             :page 86
                                             :frequency {:units :long-rest}
                                             :duration {:units :minute}
                                             :summary "you emanate a bright light with 30 ft radius, an enemy that starts its turn there takes 10 radiant damage. You also have advantage on saves against spells cast by fiends and undead"})]}}
                  :traits [{:name "Purity of Spirit"
                            :level 15
                            :page 86
                            :summary "always under effects of protection from evil and good spell"}]}
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
                              (paladin-spell 5 :tree-stride 17)
                              (mod5e/action
                               {:name "Channel Divinity: Nature's Wrath"
                                :level 3
                                :page 87
                                :summary (str "restrain a creature with vines on a failed DC " (?spell-save-dc :cha) " STR or DEX save. It makes the save every turn until freed.")})
                              (mod5e/action
                               {:name "Channel Divinity: Turn the Faithless"
                                :level 3
                                :page 87
                                :duration {:units :minute}
                                :summary "turn and reveal the true form of fey and fiends within 30 ft."})]
                  :levels {7 {:modifiers [(mod5e/dependent-trait
                                           {:name "Aura of Warding"
                                            :level 7
                                            :page 87
                                            :summary (str "you and friendly creatures within " ?paladin-aura " have resistance to spell damage")})]}}
                  :traits [{:name "Undying Sentinal"
                            :level 15
                            :page 87
                            :frequency {:units :long-rest}
                            :summary "when you are reduced to 0 HP without being killed, you drop to 1 instead"}
                           {:name "Elder Champion"
                            :level 20
                            :page 87
                            :frequency {:units :long-rest}
                            :duration {:units :minute}
                            :summary "undergo a tranformation where you 1) regain 10 HPs at start of your turns 2) can cast spells with casting time action as bonus action 3) enemies within 10 ft. have disadvantage on saves against your Channel Divinity and spells"}]}
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
                              (paladin-spell 5 :scrying 17)
                              (mod5e/action
                               {:name "Channel Divinity: Abjure Enemy"
                                :level 3
                                :page 88
                                :duration {:units :minute}
                                :summary (str "a creature of your choosing within 60 ft. must succeed on a DC " (?spell-save-dc :cha) " WIS save or be frightened and have a speed of 0, speed is halved on successful save")})
                              (mod5e/bonus-action
                               {:name "Channel Divinity: Vow of Eternity"
                                :level 3
                                :page 88
                                :duration {:units :minute}
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
                                             :duration {:units :hour}
                                             :frequency {:units :long-rest}
                                             :summary (str "transform, gain flying speed of 60 ft., emanate a 30 ft. aura and creatures within it must succeed on a DC " (?spell-save-dc :cha) " WIS or be frightened for 1 min and attacks against them have advantage")})]}}
                  :traits [{:name "Relentless Avenger"
                            :level 7
                            :page 88
                            :summary "when you hit with opportunity attack, you can also move up to half your speed after the attack without provoking opportunity attacks"}]}]}))

(def ranger-skills {:animal-handling true :athletics true :insight true :investigation true :nature true :perception true :stealth true :survival true})

(def favored-enemy-types
  {:aberration [:deep-speech :undercommon :grell :slaad]
   :beast [:giant-elk :giant-eagle :giant-owl]
   :celestial opt5e/language-keys
   :construct [:modron]
   :dragon [:aquan :common :draconic :sylvan]
   :elemental [:auran :terran :ignan :aquan]
   :fey [:common :draconic :elvish :sylvan :abyssal :infernal :primoridial :aquan :giant]
   :fiend opt5e/language-keys
   :giant [:giant :orc :undercommon :common]
   :monstrosity [:common :draconic :sylvan :elvish :hook-horror :abyssal :celestial :infernal :primordial :aquan :sphynx :umber-hulk :yeti :winter-wolf :goblin :worg]
   :ooze []
   :plant [:common :druidic :elvish :sylvan]
   :undead opt5e/language-keys})

(def humanoid-enemies
  {:bugbear [:common :goblin]
   :bullywug [:bullywug]
   :githyanki [:gith]
   :gitzerai [:gith]
   :gnoll [:gnoll :abyssal]
   :goblin [:common :goblin]
   :grimlock [:undercommon]
   :hobgoblin [:common :goblin]
   :kobold [:common :draconic]
   :koa-toa [:undercommon]
   :lizardfolk [:draconic :abyssal]
   :merfolk [:aquan :common]
   :orc [:common :orc]
   :thri-kreen [:thri-kreen]
   :troglodyte [:troglodyte]
   :yuan-ti-pureblood {:name "Yuan-Ti Pureblood"
                       :languages [:abyssal :common :draconic]}})

(defn favored-enemy-option [[enemy-type info]]
  (let [vec-info? (sequential? info)
        languages (if vec-info? info (:languages info))
        name (if vec-info? (common/kw-to-name enemy-type) (:name info))]
    (t/option-cfg
     {:name name
      :selections [(opt5e/language-selection
                    (map
                     (fn [lang]
                       (or (opt5e/language-map lang) {:key lang :name (common/kw-to-name lang)}))
                     languages)
                    1)]
      :modifiers [(mod/set-mod ?ranger-favored-enemies enemy-type)]})))

(def favored-enemy-selection
  (t/selection
   "Favored Enemy"
   [(t/option-cfg
     {:name "Type"
      :selections [(t/selection
                    "Type"
                    (mapv
                     favored-enemy-option
                     favored-enemy-types))]})
    (t/option-cfg
     {:name "Two Humanoid Races"
      :selections [(t/selection
                    "Humanoid Race 1"
                    (mapv
                     favored-enemy-option
                     humanoid-enemies))
                   (t/selection
                    "Humanoid Race 2"
                    (mapv
                     favored-enemy-option
                     humanoid-enemies))]})]))

(def favored-terrain-selection
  (t/selection
   "Favored Terrain"
   (mapv
    (fn [terrain]
      (t/option-cfg
       {:name (common/kw-to-name terrain)
        :modifiers [(mod/set-mod ?ranger-favored-terrain terrain)]}))
    [:arctic :coast :desert :forest :grassland :mountain :swamp :underdark])))

(defn uncanny-dodge-modifier [page]
  (mod5e/reaction
   {:name "Uncanny Dodge"
    :page page
    :summary "halve the damage from an attacker you can see that hits you"}))

(def ranger-option
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
    :selections [(t/selection
                  "Melee Weapon"
                  [(t/option-cfg
                    {:name "Two Shortswords"
                     :modifiers [(mod5e/weapon :shortsword 2)]})
                   (t/option-cfg
                    {:name "Simple Melee Weapon"
                     :selections [(t/selection
                                   "Simple Melee Weapon"
                                   (opt5e/simple-melee-weapon-options 1)
                                   2
                                   2)]})])
                 favored-enemy-selection
                 favored-terrain-selection]
    :levels {2 {:selections [(opt5e/fighting-style-selection #{:archery :defense :dueling :two-weapon-fighting})]}
             3 {:modifiers [(mod5e/action
                             {:name "Primeval Awareness"
                              :level 3
                              :page 92
                              :summary (str "spend an X-level spell slot, for X minutes, you sense the types of creatures within 1 mile" (if (seq ?ranger-favored-terrain) (str "(6 if " (common/list-print (map #(common/kw-to-name % false) ?ranger-favored-terrain))) ")") )})]}
             5 {:modifiers [(mod5e/extra-attack)]}
             6 {:selections [favored-enemy-selection
                             favored-terrain-selection]}
             10 {:selections [favored-terrain-selection]}
             14 {:selections [favored-enemy-selection]}
             20 {:modifiers [(mod5e/dependent-trait
                              {:name "Foe Slayer"
                               :frequency {:units :turn}
                               :level 20
                               :page 92
                               :summary (str "add " (common/bonus-str (?ability-bonuses :wis)) " to an attack or damage roll") })]}}
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
    :subclass-level 3
    :subclass-title "Ranger Archetype"
    :subclasses [{:name "Hunter"
                  :levels {3 {:selections [(t/selection
                                            "Hunter's Prey"
                                            [(t/option-cfg
                                              {:name "Colossus Slayer"
                                               :modifiers [(mod5e/trait-cfg
                                                            {:name "Colossus Slayer"
                                                             :page 93
                                                             :frequency {:units :turn}
                                                             :summary "deal an extra d8 damage when you hit a creature that is below its HP max with a weapon attack"})]})
                                             (t/option-cfg
                                              {:name "Giant Killer"
                                               :modifiers [(mod5e/reaction
                                                            {:name "Giant Killer"
                                                             :page 93
                                                             :summary "attack a Large or larger creature within 5 ft that misses an attack against you"})]})
                                             (t/option-cfg
                                              {:name "Horde Breaker"
                                               :modifiers [(mod5e/trait-cfg
                                                            {:name "Horde Breaker"
                                                             :page 93
                                                             :frequency {:units :turn}
                                                             :summary "when you attack one creature, attack another creature within 5 feet of it with the same action"})]})])]}
                           7 {:selections [(t/selection
                                            "Defensive Tactics"
                                            [(t/option-cfg
                                              {:name "Escape the Horde"
                                               :modifiers [(mod5e/trait
                                                            {:name "Escape the Horde"
                                                             :page 93})]})
                                             (t/option-cfg
                                              {:name "Multiattack Defense"
                                               :modifiers [(mod5e/trait
                                                            {:name "Multiattack Defense"
                                                             :page 93})]})
                                             (t/option-cfg
                                              {:name "Steel Will"
                                               :modifiers [(mod5e/saving-throw-advantage [:frightened])]})])]}
                           11 {:selections [(t/selection
                                            "Multiattack"
                                            [(t/option-cfg
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
                                                             :summary "melee attack against any creatures within 5 ft. of you"})]})])]}
                           15 {:selections [(t/selection
                                            "Superior Hunter's Defense"
                                            [(t/option-cfg
                                              {:name "Evasion"
                                               :modifiers [(mod5e/trait-cfg
                                                            (evasion 15 93))]})
                                             (t/option-cfg
                                              {:name "Stand Against the Tide"
                                               :modifiers  [(mod5e/reaction
                                                             {:name "Stand Against the Tide"
                                                              :page 93
                                                              :summary "force a creature to repeat its attack on another creature when it misses you"
})]})
                                             (t/option-cfg
                                              {:name "Uncanny Dodge"
                                               :modifiers [(uncanny-dodge-modifier 93)]})])]}}}
                 {:name "Beast Master"
                  :selections [(t/selection
                                "Ranger's Companion"
                                (mapv
                                 (fn [monster-name]
                                   (t/option-cfg
                                    {:name monster-name
                                     :modifiers [(mod5e/action
                                                  {:name "Ranger's Companion"
                                                   :page 93
                                                   :summary (str "You have a " monster-name " as your companion, you can command it to Attack, Dash, Disengage, Dodge, or Help")})]}))
                                 ["Stirge" "Baboon" "Bat" "Badger" "Blood Hawk" "Boar" "Cat" "Crab" "Deer" "Eagle" "Flying Snake" "Frog" "Giant Badger" "Giant Centipede" "Giant Crab" "Giant Fire Beetle" "Giant Frog" "Giant Poisonous Snake" "Giant Rat" "Giant Wolf Spider" "Goat" "Hawk" "Hyena" "Jackal" "Lizard" "Mastiff" "Mule" "Octopus" "Panther" "Owl" "Poisonous Snake" "Pony" "Quipper" "Rat" "Raven" "Scorpion" "Sea Horse" "Spider" "Vulture" "Weasel" "Wolf"]))]
                  :levels {7 {:modifiers [(mod5e/bonus-action
                                           {:name "Exceptional Training"
                                            :page 93
                                            :level 7
                                            :summary "when your companion doesn't attack, you can command it to take the Dash, Disengage, Dodge, or Help action"})]}}
                  :traits [
                           {:name "Bestial Fury"
                            :level 11
                            :page 93
                            :summary "your companion attacks twice when it takes the Attack action"}
                           {:name "Share Spells"
                            :level 15
                            :page 93
                            :summary "when you target yourself with a spell you can also affect your companion if within 30 ft."}]}]}))

(def rogue-skills {:acrobatics true :athletics true :deception true :insight true :intimidation true :investigation true :perception true :performance true :persuasion true :sleight-of-hand true :stealth true})

(def rogue-option
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
    :modifiers [(mod5e/dependent-trait
                 {:name "Sneak Attack"
                  :page 96
                  :frequency {:units :turn}
                  :summary (str (common/round-up (/ (?class-level :rogue) 2)) "d6 extra damage on attack where you have advantage or another enemy of creature is within 5 ft.")
})]
    :levels {2 {:modifiers [(mod5e/bonus-action
                             {:level 2
                              :name "Cunning Action"
                              :page 96
                              :frequency {:units :turn}
                              :summary "Dash, Disengage or Hide"
})]}
             5 {:modifiers [(uncanny-dodge-modifier 96)]}}
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
    :traits [{:name "Thieves' Cant"
              :page 96
              :summary "convey secret messages hidden in normal conversation"
}
             (evasion 7 96)
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
              :frequency {:units :rest}
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
                                :summary (str "climbing costs no extra movement, your running jump distance increases by " (?ability-bonuses :dex) " ft.")
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
                                             :summary (str "double damage against a surpised creature if it fails a DC " (?spell-save-dc :dex) " CON save")})]}}
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
                  :levels {13 {:modifiers [(mod5e/bonus-action
                                            {:name "Versatile Trickster"
                                             :level 13
                                             :page 98
                                             :summary "use mage hand to gain advantage on attack rolls against a creature within 5 ft. of the hand"})]}
                           17 {:modifiers [(mod5e/reaction
                                            {:name "Spell Thief"
                                             :level 17
                                             :page 98
                                             :summary (str "steal a spell for 8 hours if it is cast on you and the spellcaster fails a DC " (?spell-save-dc :int) " save with its spellcasting ability")})]}}
                  :traits [{:name "Mage Hand Legerdemain"
                            :level 3
                            :page 98
                            :summary "when you cast mage hand, you can make it invisible and perform Sleight of Hand tasks"}
                           {:name "Magical Ambush"
                            :level 9
                            :page 98
                            :summary "creatures have disadvantage on saves against your spells (only on the turn you cast them) if you are hidden from them"}]}]}))

(defn metamagic-selection [num]
  (t/selection
   "Metamagic"
   [(t/option-cfg
     {:name "Careful Spell"
      :modifiers [(mod5e/dependent-trait
                   {:name "Careful Spell"
                    :page 102
                    :summary (str "When you cast a spell that requires a save, spend 1 sorcery pt. to allow up to " (?ability-bonuses :cha) " creatures to automatically succeed")})]})
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
                    :summary (str "spend 1 sorcery pt. to reroll up to " (?ability-bonuses :cha) " spell damage dice")})]})
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
                   :summary "spend X sorcery pts. (min 1) to target two creatures with a single target spell, where X is the spell level"})]})]
   num
   num))

(def sorcerer-option
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
    :selections [(t/selection-cfg
                  {:name "Starting Equipment: Weapon"
                   :tags #{:equipment :starting-equipment}
                   :options [(t/option
                              "Light Crossbow"
                              :crossbow
                              []
                              [(mod5e/weapon :crossbow-light 1)
                               (mod5e/equipment :crossbow-bolt 20)])
                             (weapon-option [:simple 1])]})]
    :modifiers [(mod/modifier ?natural-ac-bonus 3)]
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
    :weapons {:dagger 1}
    :subclass-title "Sorcerous Origin"
    :subclass-level 1
    :subclasses [{:name "Draconic Bloodline"
                  :modifiers [(mod/modifier ?hit-point-level-bonus (+ 1 ?hit-point-level-bonus))]
                  :selections [(t/selection-cfg
                                {:name "Draconic Ancestry Type"
                                 :tags #{:class}
                                 :options (mapv
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
                                            :summary (str "Add CHA mod to one damage roll of a spell that deals " (common/safe-name (get-in ?sorcerer-draconic-ancestry [:breath-weapon :damage-type]))  " damage, you may also spend 1 sorcery pt. to gain resistance to that damage type for an hr.")})]}
                           14 {:modifiers [(mod5e/bonus-action
                                            {:name "Dragon Wings"
                                             :page 103
                                             :summary "Sprout wings and gain flying speed equal to land speed"})]}
                           18 {:modifiers [(mod5e/action
                                            {:name "Draconic Presence"
                                             :page 103
                                             :summary (str "Spend 5 sorcery pts. and create an aura that causes hostile creatures that start their turn within it to be charmed or afraid if they fail a DC " (?spell-save-dc :cha) " Wisdom save.")})]}}}
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
                            :frequency {:units :long-rest}}
                           {:name "Controlled Chaos"
                            :level 14
                            :page 103
                            :summary "When rolling on Wild Magic Surge table, roll twice and use either roll"}
                           {:name "Spell Bombardment"
                            :level 18
                            :page 103
                            :summary "When you roll a die for spell damage, roll max rolls an additional time"
                            :frequency {:units :turn}}]}]}))

(def wizard-option
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
    :traits [{:level 18 :name "Spell Mastery"}
             {:level 20 :name "Signature Spells"}]
    :subclass-level 2
    :subclass-title "Arcane Tradition"
    :subclasses [{:name "School of Evocation"
                  :traits [{:level 2
                            :name "Evocation Savant"}
                           {:level 2
                            :name "Sculpt Spells"}
                           {:level 6
                            :name "Potent Cantrip"}
                           {:level 10
                            :name "Empowered Evocation"}
                           {:level 14
                            :name "Overchannel"}]}
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
                  :modifiers [(mod5e/damage-resistance :necrotic)]
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
                            :level 14}]}]}))

(defn has-trait-with-name-prereq [name]
  (fn [c] (some #(= name (:name %)) (es/entity-val c :traits))))

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
    [(mod5e/trait-cfg
      {:name pact-of-the-chain-name
       :page 107
       :summary "Can cast find familiar as a ritual, use your attack action to give your familiar an attack as a reaction"})])
   (t/option
    "Pact of the Blade"
    :pact-of-the-blade
    []
    [(mod5e/trait-cfg
      {:name pact-of-the-blade-name
       :page 107
       :summary "summon a magical weapon"})])
   (t/option
    "Pact of the Tome"
    :pact-of-the-tome
    [(t/selection-cfg
      {:name "Book of Shadows Cantrips"
       :tags #{:spells}
       :options (opt5e/spell-options (into
                                      #{}
                                      (mapcat
                                       (fn [[cls-kw spells-by-level]]
                                         (spells-by-level 0))
                                       sl/spell-lists))
                                     0
                                     :cha
                                     "Warlock"
                                     "uses Book of Shadows")})]
    [(mod5e/trait-cfg
      {:name pact-of-the-tome-name
       :page 108
       :summary "you have a spellbook with 3 extra cantrips"})])])


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
    :tags #{:spells}
    :simple? true}))

(defn mystic-arcanum-selection [spell-level]
  (t/selection-cfg
   {:name (str "Mystic Arcanum: Spell Level " spell-level)
    :tags #{:spells}
    :options (opt5e/spell-options
              (get-in sl/spell-lists [:warlock spell-level])
              spell-level
              :cha
              "Warlock"
              "uses Mystic Arcanum")}))

(def warlock-option
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
    :selections [(t/selection-cfg
                  {:name "Starting Equipment: Weapon"
                   :tags #{:equipment :starting-equipment}
                   :options [(t/option
                              "Light Crossbow"
                              :crossbow
                              []
                              [(mod5e/weapon :crossbow-light 1)
                               (mod5e/equipment :crossbow-bolt 20)])
                             (weapon-option [:simple 1])]})
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
                :selections [(t/selection-cfg
                              {:name "Pact Boon"
                               :tags #{:class}
                               :options pact-boon-options})]}
             5 {:selections [(eldritch-invocation-selection)]}
             8 {:selections [(eldritch-invocation-selection)]}
             11 {:selections [(mystic-arcanum-selection 6)]
                 :modifiers [(mod5e/dependent-trait
                              {:name "Mystic Arcanum"
                               :level 11
                               :page 108
                               :summary "You gain a 6th level spell you can cast without expending a slot, more at higher levels"
                               :frequency {:units :long-rest
                                           :amount (mod5e/level-val
                                                    (?class-level :warlock)
                                                    {13 2
                                                     15 3
                                                     17 4
                                                     :default 1})}})]}
             12 {:selections [(eldritch-invocation-selection)]}
             13 {:selections [(mystic-arcanum-selection 7)]}
             15 {:selections [(eldritch-invocation-selection)
                              (mystic-arcanum-selection 8)]}
             17 {:selections [(mystic-arcanum-selection 9)]}
             18 {:selections [(eldritch-invocation-selection)]}}
    :traits [{:name "Eldrich Master"
              :level 20}]
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
                  :traits [{:name "Dark One's Blessing"}
                           {:name "Dark One's Own Luck"
                            :level 6}
                           {:name "Fiendish Resilience"
                            :level 10}
                           {:name "Hurl Through Hell"
                            :level 14}]}
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
                            :level 14}]}]}))

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
                             :summary "You and your companions can expect free healing at an establishment of your faith."}]
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
                          :as cls}]
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

(defn volos-guide-to-monsters-selections [app-state]
  [(t/selection-cfg
    {:name "Race"
     :tags #{:race}
     :options [aasimar-option
               firbolg-option
               goliath-option
               kenku-option
               lizardfolk-option
               tabaxi-option
               triton-option
               bugbear-option
               goblin-option
               hobgoblin-option
               kobold-option
               orc-option
               yuan-ti-option]})])


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

(defn scag-classes [app-state]
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
                            :level 14}]}]}
   {:name "Rogue"
    :plugin? true
    :subclass-level 3
    :subclass-title "Roguish Archetypes"
    :subclasses [{:name "Mastermind"
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
                            :level 17}]}]}])

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
     :tags #{:class}}
    cfg)))

(defn sword-coast-adventurers-guide-selections [app-state]
  [(background-selection
    {:options (mapv
               background-option
               sword-coast-adventurers-guide-backgrounds)})
   (class-selection
    {:options (mapv
               class-option
               (scag-classes app-state))})])

(defn ability-item [name abbr desc]
  [:li.m-t-5 [:span.f-w-b.m-r-5 (str name " (" abbr ")")] desc])

(defn inventory-selection [item-type-name icon items modifier-fn]
  (t/selection-cfg
   {:name item-type-name
    :min 0
    :max nil
    :sequential? false
    :quantity? true
    :collapsible? true
    :icon icon
    :new-item-fn (fn [selection selected-items _ key]
                   {::entity/key key
                    ::entity/value 1})
    :tags #{:equipment}
    :options (mapv
              (fn [{:keys [name key description]}]
                (t/option-cfg
                 {:name name
                  :key key
                  :help description
                  :modifiers [(modifier-fn key)]}))
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

(defn amazon-frame-help [frame content]
  [:div.flex.m-t-10
   [:div.flex-grow-1.p-r-5
    content]
   frame])

(def srd-link
  [:a.orange {:href "SRD-OGL_V5.1.pdf" :target "_blank"} "the 5e SRD"])

(def plugins
  [{:name "Sword Coast Adventurer's Guide"
    :key :sword-coast-adventurers-guide
    :url "https://www.amazon.com/gp/product/0786965800/ref=as_li_tl?ie=UTF8&tag=orcpub-20&camp=1789&creative=9325&linkCode=as2&creativeASIN=0786965800&linkId=9b93efa0fc7239ebbf005d0b17367233"
    :help (amazon-frame-help scag-amazon-frame
                             [:span "Incudes too many new, exciting subraces, race variants, subclasses, and backgrounds to list, as well as a ton of other info to help you create in-depth characters in the Sword Coast or elsewhere."])}
   {:name "Volo's Guide to Monsters"
    :key :volos-guide-to-monsters
    :url "https://www.amazon.com/gp/product/0786966017/ref=as_li_tl?ie=UTF8&tag=orcpub-20&camp=1789&creative=9325&linkCode=as2&creativeASIN=0786966017&linkId=506a1b33174f884dcec5db8c6c07ad31"
    :help (amazon-frame-help volos-amazon-frame
                             [:div
                              "Full of great monster race options, including"
                              (content-list ["Aasimar" "Firbolg" "Goliath" "Kenku" "Lizardfolk" "Tabaxi" "Triton" "Bugbear" "Goblin" "Hobgoblin" "Kobold" "Orc" "Yuan-Ti Pureblood"])])}])

(def optional-content-selection
  (t/selection-cfg
   {:name "Optional Content"
    :tags #{:optional-content}
    :help (amazon-frame-help phb-amazon-frame
                             [:span
                              "Base options are from the Player's Handbook, although descriptions are either from the "
                              srd-link
                              " or are OrcPub summaries. See the Player's Handbook for in-depth, official rules and descriptions."])
    :options (mapv
              #(t/option-cfg
               (select-keys % [:name :key :help]))
              plugins)
    :multiselect? true
    :min 0
    :max nil}))

(def plugin-map
  (into {} (map (juxt :key identity) plugins)))

(def template-selections
  [optional-content-selection
   (t/selection-cfg
    {:name "Base Ability Scores"
     :key :ability-scores
     :require-value? true
     :order 0
     :tags #{:ability-scores}
     :help [:div
            [:p "Ability scores are your major character traits and affect nearly all aspects of play. These scores range from 1 to 20 for player characters and DO NOT include racial or other bonuses."]
            [:ul.m-t-10.m-l-5
             (ability-item "Strength" "STR" "measures your physical power")
             (ability-item "Dexterity" "DEX" "measures your agility and nimbleness")
             (ability-item "Constitution" "CON" "measures your physical health")
             (ability-item "Intelligence" "INT" "measures your memory and reasoning abilities")
             (ability-item "Wisdom" "WIS" "measures your connection to your environment, how observant you are.")
             (ability-item "Charisma" "CHA" "measures how well you interact with others.")]]
     :options [{::t/name "Manual Entry"
                ::t/key :manual-entry
                ::t/help "This option allows you to manually type in the value for each ability. Use this if you want to roll dice yourself or if you already have a character with known ability values."
                ::t/modifiers [(mod5e/deferred-abilities)]}
               {::t/name "Point Buy"
                ::t/key :point-buy
                ::t/help [:div
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
                             [:td 9]]]]]
                ::t/modifiers [(mod5e/deferred-abilities)]}
               {::t/name "Standard Roll"
                ::t/key :standard-roll
                ::t/help "This option rolls the dice for you. You can rearrange the values using the left and right arrow buttons."
                ::t/modifiers [(mod5e/deferred-abilities)]}
               {::t/name "Standard Scores"
                ::t/key :standard-scores
                ::t/help "If you aren't feeling lucky, use this option, which gives you a standard set of scores. You can reassign the values using the left and right arrow buttons."
                ::t/modifiers [(mod5e/deferred-abilities)]}]})
   (t/selection-cfg
    {:name "Alignment"
     :tags #{:description :background}
     :options (mapv
               (fn [alignment]
                 (t/option-cfg
                  {:name alignment
                   :modifiers [(mod5e/alignment alignment)]}))
               ["Lawful Good" "Lawful Neutral" "Lawful Evil" "Neutral Good" "Neutral" "Neutral Evil" "Chaotic Good" "Chaotic Neutral" "Chaotic Evil"])})
   (t/selection-cfg
    {:name "Race"
     :order 0
     :help "Race determines your appearance and helps shape your culture and background. It also affects you ability scores, size, speed, languages, and many other crucial inherent traits."
     :tags #{:race}
     :options [dwarf-option
               elf-option
               halfling-option
               human-option
               dragonborn-option
               gnome-option
               half-elf-option
               half-orc-option
               tiefling-option]})
   (background-selection
    {:help "Background broadly describes your character origin. It also affords you two skill proficiencies and possibly proficiencies with tools or languages."
     :options (map
               background-option
               backgrounds)})
   (t/selection-cfg
    {:name "Feats"
     :ref :feats
     :tags #{:feats}
     :options opt5e/feat-options
     :multiselect? true
     :min 0
     :max 0})
   (class-selection
    {:help [:div
            [:p "Class is your adventuring vocation. It determines many of your special talents, including weapon, armor, skill, saving throw, and tool proficiencies. It also provides starting equipment options. When you gain levels, you gain them in a particular class."]
            [:p.m-t-10 "Select your class using the selector at the top of the 'Class' section. Multiclassing is uncommon, but you may multiclass by clicking the 'Add Class' button at the end of the 'Class' section."]]
     :max nil
     :sequential? false
     :options [barbarian-option
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
               wizard-option]})
   (inventory-selection "Treasure" "cash" equip5e/treasure mod5e/deferred-treasure)
   (inventory-selection "Weapons" "plain-dagger" weapon5e/weapons mod5e/deferred-weapon)
   (inventory-selection "Magic Weapons" "lightning-bow" mi/magic-weapons mod5e/deferred-magic-weapon)
   (inventory-selection "Armor" "breastplate" armor5e/armor mod5e/deferred-armor)
   (inventory-selection "Magic Armor" "magic-shield" mi/magic-armor mod5e/deferred-magic-armor)
   (inventory-selection "Equipment" "backpack" equip5e/equipment mod5e/deferred-equipment)
   (inventory-selection "Other Magic Items" "orb-wand" mi/other-magic-items mod5e/deferred-magic-item)])


(def template-base
  (es/make-entity
   {?armor-class (+ 10 (?ability-bonuses :dex))
    ?base-armor-class (+ 10 (?ability-bonuses :dex) ?natural-ac-bonus)
    ?natural-ac-bonus 0
    ?unarmored-ac-bonus 0
    ?unarmored-with-shield-ac-bonus 0
    ?armored-ac-bonus 0
    ?max-medium-armor-bonus 2
    ?armor-stealth-disadvantage? (fn [armor]
                                   (:stealth-disadvantage? armor))
    ?armor-dex-bonus (fn [armor]
                       (let [dex-bonus (?ability-bonuses :dex)]
                         (case (:type armor)
                           :light dex-bonus
                           :medium (min ?max-medium-armor-bonus dex-bonus)
                           0)))
    ?shield-ac-bonus (fn [shield]
                    (+ 2 (or (:magical-ac-bonus shield) 0)))
    ?unarmored-armor-class (+ ?base-armor-class ?unarmored-ac-bonus)
    ?unarmored-with-shield-armor-class (fn [shield]
                                         (+ ?base-armor-class
                                            ?unarmored-with-shield-ac-bonus
                                            (?shield-ac-bonus shield)))
    ?armor-class-with-armor (fn [armor & [shield]]
                              (cond (and (nil? armor)
                                         (nil? shield)) ?unarmored-armor-class
                                    (nil? armor) (?unarmored-with-shield-armor-class shield)
                                    :else (+ (if shield (?shield-ac-bonus shield) 0)
                                             (+ (?armor-dex-bonus armor)
                                                (or ?armored-ac-bonus 0)
                                                (:base-ac armor)
                                                (:magical-ac-bonus armor)))))
    ?abilities (reduce
                (fn [m k]
                  (assoc m k (+ (or (k ?base-abilities) 0)
                                (or (k ?ability-increases) 0)
                                (or (k ?level-ability-increases) 0))))
                {}
                char5e/ability-keys)
    ?ability-bonuses (reduce-kv
                      (fn [m k v]
                        (assoc m k (opt5e/ability-bonus v)))
                      {}
                      ?abilities)
    ?save-bonuses (reduce-kv
                   (fn [m k v]
                     (assoc m k (+ v (if (and ?saving-throws (?saving-throws k)) ?prof-bonus 0))))
                   {}
                   ?ability-bonuses)
    ?total-levels (apply + (map (fn [[k {l :class-level}]] l) ?levels))
    ?class-level (fn [class-kw] (get-in ?levels [class-kw :class-level]))
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
    ?max-hit-points (max 1 (+ ?hit-point-level-increases (* ?total-levels ?hit-point-level-bonus)))
    ?initiative (?ability-bonuses :dex)
    ?num-attacks 1
    ?critical #{20}
    ?has-weapon-prof? (fn [weapon]
                        (or (?weapon-profs (:key weapon))
                            (?weapon-profs (:type weapon))))
    ?weapon-prof-bonus (fn [weapon]
                         (if (?has-weapon-prof? weapon)
                           ?prof-bonus
                           0))
    ?weapon-attack-modifier (fn [weapon finesse?]
                              (let [definitely-finesse? (and finesse?
                                                             (:finesse? weapon))]
                                (+ (?weapon-prof-bonus weapon)
                                   (or (:magical-attack-bonus weapon) 0)
                                   (or (:attack-bonus weapon) 0)
                                   (if (:melee? weapon)
                                     (+ (?ability-bonuses
                                         (if definitely-finesse? :dex :str))
                                        (or ?melee-attack-bonus 0))
                                     (+ (?ability-bonuses
                                         (if definitely-finesse? :str :dex))
                                        (or ?ranged-attack-bonus 0))))))
    ?weapon-damage-modifier (fn [weapon finesse?]
                              (let [definitely-finesse? (and finesse?
                                                             (:finesse? weapon))
                                    melee? (:melee? weapon)]
                                (+ (or (:magical-damage-bonus weapon) 0)
                                   (or (:damage-bonus weapon) 0)
                                   (?ability-bonuses
                                    (if (or (and melee? (not definitely-finesse?))
                                            (and (not melee?) definitely-finesse?))
                                      :str
                                      :dex)))))
    ?spell-attack-modifier (fn [ability-kw]
                             (+ ?prof-bonus (?ability-bonuses ability-kw)))
    ?spell-save-dc (fn [ability-kw]
                     (+ 8 ?prof-bonus (?ability-bonuses ability-kw)))}))


(def template
  {::t/base template-base
   ::t/selections template-selections})

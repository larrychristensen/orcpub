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

(def tools
  [{:name "smith's tools"
    :key :smiths-tools}
   {:name "brewer's supplies"
    :key :brewers-supplies}
   {:name "mason's tools"
    :key :masons-tools}])

(def weapons
  [{:name "battleaxe"
    :key :battleaxe}
   {:name "handaxe"
    :key :handaxe}
   {:name "light hammer"
    :key :light-hammer}
   {:name "warhammer"
    :key :warhammer}])

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

(defn skill-options [skills]
  (map
   skill-option
   skills))

(defn tool-options [tools]
  (map
   tool-option
   tools))

(defn ability-increase-selection [abilities num]
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
   num))

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
    :key :giant}])

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

(def wizard-cantrip-options
  (map
   (fn [key]
     {::t/key key
      ::t/name (key-to-name key)
      ::t/modifiers [(modifiers/spells-known 0 key)]})
   wizard-cantrips))

(defn wizard-cantrip-selection [num]
  (t/selection "Cantrips Known" wizard-cantrip-options num num))

(def wizard-spells-1
  [:mage-armor :magic-missile :magic-mouth :shield])

(defn spell-options [spells level]
  (map
   (fn [key]
     {::t/key key
      ::t/name (key-to-name key)
      ::t/modifiers [(modifiers/spells-known level key)]})
   spells))

(def wizard-spell-options-1
  (map
   (fn [key]
     {::t/key key
      ::t/name (key-to-name key)
      ::t/modifiers [(modifiers/spells-known 1 key)]})
   wizard-spells-1))

(defn wizard-spell-selection-1 []
  (assoc (t/selection*
          "1st Level Spells Known"
          (fn [selection spells-known]
            {::entity/key :shield})
          wizard-spell-options-1)
         ::t/key
         :spells-known))

(defn magic-initiate-option [class-key spell-lists]
  (t/option
   (name class-key)
   class-key
   [(t/selection
     "Cantrip"
     (spell-options (get-in spell-lists [class-key 0]) 0)
     2 2)
    (t/selection
     "1st Level Spell"
     (spell-options (get-in spell-lists [class-key 1]) 1)
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
    [(ability-increase-selection [:str :dex] 1)]
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
    [(ability-increase-selection [:str :dex] 1)]
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
      [(magic-initiate-option :bard sl/spell-lists)
       (magic-initiate-option :cleric sl/spell-lists)
       (magic-initiate-option :druid sl/spell-lists)
       (magic-initiate-option :sorcerer sl/spell-lists)
       (magic-initiate-option :warlock sl/spell-lists)
       (magic-initiate-option :wizard sl/spell-lists)])]
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
    [(ability-increase-selection [:str :dex] 1)]
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
    [(ability-increase-selection [:int :wis] 1)]
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

(defn ability-score-improvement-selection []
  (t/selection
   "Ability Score Improvement/Feat"
   [(t/option
     "Ability Score Improvement"
     :ability-score-improvement
     [(ability-increase-selection character/ability-keys 2)]
     [])
    (t/option
     "Feat"
     :feat
     [(t/selection
       "Feat"
       feat-options)]
     [])]))

(defn skill-selection [options num]
  (t/selection
   "Skills"
   (skill-options
    (filter
     (comp (set options) :key)
     skills))
   num
   num))

(defn tool-selection [options num]
  (t/selection
   "Tool Proficiency"
   (tool-options
    (filter
     (comp (set options) :key)
     tools))
   num
   num))

(defn expertise-selection []
  (t/selection
   "Expertise"
   [(t/option
     "Two Skills"
     :two-skills
     [(t/selection
       "Skills"
       (map
        (fn [skill]
          (t/option
           (:name skill)
           (:key skill)
           nil
           [(modifiers/skill-expertise (:key skill))]))
        skills)
       2
       2)]
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

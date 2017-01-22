(ns orcpub.dnd.e5.options
  (:require [clojure.string :as s]
            [orcpub.template :as t]
            [orcpub.entity :as entity]
            [orcpub.entity-spec :as es]
            [orcpub.dnd.e5.character :as character]
            [orcpub.dnd.e5.modifiers :as modifiers]))

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
   "Abilities"
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
    [{::t/label "Dex 13 or higher"
      ::t/prereq-fn (min-ability :dex 13)}])])

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

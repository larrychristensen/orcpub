(ns orcpub.dnd.e5.options
  (:require [clojure.string :as s]
            [orcpub.template :as t]
            [orcpub.entity :as entity]
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

(def skill-abilities
  (into {} (map (juxt :key :ability)) skills))

(defn skill-option [skill]
  (t/option
   (:name skill)
   (:key skill)
   nil
   [(modifiers/skill-proficiency (:key skill))]))

(defn skill-options [skills]
  (map
   skill-option
   skills))

(def wizard-cantrips
  [:acid-splash :blade-ward :light :true-strike])

(defn key-to-name [key]
  (s/join " " (map s/capitalize (s/split (name key) #"-"))))

(def wizard-cantrip-options
  (map
   (fn [key]
     {::t/key key
      ::t/name (key-to-name key)
      ::t/modifiers [(modifiers/spells-known2 0 key)]})
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
      ::t/modifiers [(modifiers/spells-known2 1 key)]})
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
     [(t/selection
       "Abilities"
       (into
        []
        (map
         (fn [ability]
           (t/option
            (s/upper-case (name ability))
            ability
            []
            [(modifiers/ability2 ability 1)])))
        character/ability-keys)
       2
       2)]
     [])
    (t/option
     "Feat"
     :feat
     [(t/selection
       "Feat"
       (map
        (fn [ability]
          (let [option (t/option
                        (s/upper-case (name ability))
                        ability
                        nil
                        [(modifiers/ability2 ability 1)])]
            option))
        character/ability-keys))]
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
           [(modifiers/skill-expertise2 (:key skill))]))
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
         [(modifiers/skill-expertise2 :athletics)])
        (t/option
         "Acrobatics"
         :acrobatics
         nil
         [(modifiers/skill-expertise2 :acrobatics)])])]
     [(modifiers/tool-proficiency2 "Thieves Tools" :thieves-tools)])]))

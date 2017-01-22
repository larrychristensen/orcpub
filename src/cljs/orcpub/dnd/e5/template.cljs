(ns orcpub.dnd.e5.template
  (:require [orcpub.entity :as entity]
            [orcpub.entity-spec :as es]
            [orcpub.template :as t]
            [orcpub.dice :as dice]
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
                                                                                         ::entity/value 3}}}
                                                         {::entity/key :3}
                                                         {::entity/key :4
                                                          ::entity/options {:ability-score-improvement-feat {::entity/key :ability-score-improvement
                                                                                                             ::entity/options {:abilities [{::entity/key :cha}]}}}}]}}]}})

(defn get-raw-abilities [character-ref]
  (get-in @character-ref [::entity/options :ability-scores ::entity/value]))

(defn abilities-standard [character-ref]
  [:div
   [:div
    {:style {:display :flex
             :justify-content :space-between}}
    (for [[k v] (get-raw-abilities character-ref)]
      ^{:key k} [:div {:style {:margin-top "10px"
                               :text-align :center}}
                 [:div {:style {:text-transform :uppercase}} (name k)]
                 [:div {:style {:font-size "18px"}} v]
                 [:div
                  [:i.fa.fa-chevron-circle-left {:style {:font-size "16px"}}]
                  [:i.fa.fa-chevron-circle-right {:style {:margin-left "5px" :font-size "16px"}}]]])]])

(defn abilities-roller [character-ref reroll-fn]
  [:div
   [:div
    {:style {:display :flex
             :justify-content :space-between}}
    (for [[k v] (get-in @character-ref [::entity/options :ability-scores ::entity/value])]
      ^{:key k}
      [:div {:style {:margin-top "10px"
                     :margin-bottom "10px"
                     :text-align :center}}
       [:div {:style {:text-transform :uppercase}} (name k)]
       [:div {:style {:font-size "18px"}} v]
       [:div
        [:i.fa.fa-chevron-circle-left {:style {:font-size "16px"}}]
        [:i.fa.fa-chevron-circle-right {:style {:margin-left "5px" :font-size "16px"}}]]])]
   [:button.form-button
    {:on-click reroll-fn}
    "Re-Roll"]])

(declare template-selections)

(defn hit-points-roller [die character-ref path]
  [:div
   [:button.form-button
    {:style {:margin-top "10px"}
     :on-click
     (fn []
       (swap! character-ref #(assoc-in % (entity/get-option-value-path {::t/selections (template-selections character-ref)} path) (dice/die-roll die))))}
    "Re-Roll"]])


(def dwarf-option
  (t/option
   "Dwarf"
   :dwarf
   [(t/selection
     "Subrace"
     [(t/option
       "Hill Dwarf"
       :hill-dwarf
       [(opt5e/tool-selection [:smiths-tools :brewers-supplies :masons-tools] 1)]
       [(mod5e/subrace "Hill Dwarf")
        (mod5e/ability :wis 1)])
      (t/option
       "Mountain Dwarf"
       :mountain-dwarf
       []
       [(mod5e/subrace "Mountain Dwarf")
        (mod5e/ability :str 2)
        (mod5e/light-armor-proficiency)
        (mod5e/medium-armor-proficiency)])])]
   [(mod5e/race "Dwarf")
    (mod5e/ability :con 2)
    (mod5e/speed 25)
    (mod5e/darkvision 60)
    (mod5e/resistance :poison)
    (mod5e/weapon-proficiency "handaxe" :handaxe)
    (mod5e/weapon-proficiency "battleaxe" :battleaxe)
    (mod5e/weapon-proficiency "light hammer" :light-hammer)
    (mod5e/weapon-proficiency "warhammer" :warhammer)]))

(def elf-option
  (t/option
   "Elf"
   :elf
   [(t/selection
     "Subrace"
     [(t/option
       "High Elf"
       :high-elf
       [(opt5e/wizard-cantrip-selection 1)]
       [(mod5e/subrace "High Elf")
        (mod5e/ability :int 1)])
      (t/option
       "Wood Elf"
       :wood-elf
       []
       [(mod5e/subrace "Wood Elf")
        (mod5e/ability :wis 1)])])]
   [(mod5e/race "Elf")
    (mod5e/ability :dex 2)]))

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
      ::t/modifiers [(mod5e/deferred-abilities)]}
     {::t/name "Standard Scores"
      ::t/key :standard-scores
      ::t/ui-fn #(abilities-standard character-ref)
      ::t/select-fn (set-standard-abilities character-ref)
      ::t/modifiers [(mod5e/deferred-abilities)]}])
   (t/selection
    "Race"
    [elf-option
     dwarf-option])
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
    [(t/option
      "Wizard"
      :wizard
      [(opt5e/skill-selection [:arcana :history :insight :investigation :medicine :religion] 2)
       (t/sequential-selection
        "Levels"
        (fn [selection levels]
          {::entity/key (-> levels count inc str keyword)})
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
           (t/selection
            "Hit Points"
            [{::t/name "Roll"
              ::t/key :roll
              ::t/ui-fn #(hit-points-roller 6 character-ref %)
              ::t/modifiers [(mod5e/deferred-max-hit-points)]}
             (t/option
              "Average"
              :average
              nil
              [(mod5e/max-hit-points 4)])])]
          [(mod5e/level :wizard "Wizard" 2)])
         (t/option
          "3"
          :3
          [(opt5e/wizard-spell-selection-1)]
          [(mod5e/level :wizard "Wizard" 3)])
         (t/option
          "4"
          :4
          [(opt5e/ability-score-improvement-selection)]
          [(mod5e/level :wizard "Wizard" 3)])])]
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
    ?max-hit-points (* ?total-levels (?ability-bonuses :con))
    ?initiative (?ability-bonuses :dex)}))

(defn template [character-ref]
  {::t/base template-base
   ::t/selections (template-selections character-ref)})

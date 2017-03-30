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
            [orcpub.dnd.e5.spell-lists :as sl])
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

(def skills [{:name "Acrobatics"
              :key :acrobatics
              :ability :dex
              :icon "body-balance"
              :description "Your Dexterity (Acrobatics) check covers your attempt to stay on your feet in a tricky situation, such as when you’re trying to run across a sheet of ice, balance on a tightrope, or stay upright on a rocking ship’s deck. The GM might also call for a Dexterity (Acrobatics) check to see if you can perform acrobatic stunts, including dives, rolls, somersaults, and flips."}
             {:name "Animal Handling"
              :key :animal-handling
              :ability :wis
              :icon "horse-head"
              :description "When there is any question whether you can calm down a domesticated animal, keep a mount from getting spooked, or intuit an animal’s intentions, the GM might call for a Wisdom (Animal Handling) check. You also make a Wisdom (Animal Handling) check to control your mount when you attempt a risky maneuver"}
             {:name "Arcana"
              :key :arcana
              :ability :int
              :icon "spell-book"
              :description "Your Intelligence (Arcana) check measures your ability to recall lore about spells, magic items, eldritch symbols, magical traditions, the planes of existence, and the inhabitants of those planes."}
             {:name "Athletics"
              :key :athletics
              :ability :str
              :icon "jump-across"
              :description "Your Strength (Athletics) check covers difficult situations you encounter while climbing, jumping, or swimming."}
             {:name "Deception"
              :key :deception
              :ability :cha
              :icon "double-face-mask"
              :description "Your Charisma (Deception) check determines whether you can convincingly hide the truth, either verbally or through your actions. This deception can encompass everything from misleading others through ambiguity to telling outright lies. Typical situations include trying to fasttalk
a guard, con a merchant, earn money through gambling, pass yourself off in a disguise, dull someone’s suspicions with false assurances, or maintain a straight face while telling a blatant lie."}
             {:name "History"
              :key :history
              :ability :int
              :icon "ancient-ruins"
              :description "Your Intelligence (History) check measures your ability to recall lore about historical events, legendary people, ancient kingdoms, past disputes, recent wars, and lost civilizations"}
             {:name "Insight"
              :key :insight
              :ability :wis
              :icon "think"
              :description "Your Wisdom (Insight) check decides whether you can determine the true intentions of a creature, such as when searching out a lie or predicting someone’s next move. Doing so involves
gleaning clues from body language, speech habits, and changes in mannerisms."}
             {:name "Intimidation"
              :key :intimidation
              :ability :cha
              :icon "confrontation"
              :description "When you attempt to influence someone through overt threats, hostile actions, and physical violence, the GM might ask you to make a Charisma (Intimidation) check. Examples include trying to pry information out of a prisoner, convincing street thugs to back down from a confrontation, or using the edge of a broken bottle to convince a sneering vizier to reconsider a decision."}
             {:name "Investigation"
              :key :investigation
              :ability :int
              :icon "sherlock-holmes"
              :description "When you look around for clues and make deductions based on those clues, you make an Intelligence (Investigation) check. You might deduce the location of a hidden object, discern from the appearance of a wound what kind of weapon dealt it, or determine the weakest point in a tunnel that could cause it to collapse. Poring through ancient scrolls in search of a hidden fragment of knowledge might also call for an Intelligence (Investigation) check."}
             {:name "Medicine"
              :key :medicine
              :ability :wis
              :icon "medical-pack"
              :description "A Wisdom (Medicine) check lets you try to stabilize a dying companion or diagnose an illness."}
             {:name "Nature"
              :key :nature
              :ability :int
              :icon "falling-leaf"
              :description "Your Intelligence (Nature) check measures your ability to recall lore about terrain, plants and animals, the weather, and natural cycles."}
             {:name "Perception"
              :key :perception
              :ability :wis
              :icon "awareness"
              :description "Your Wisdom (Perception) check lets you spot, hear, or otherwise detect the presence of something. It measures your general awareness of your surroundings and the keenness of your senses. For example, you might try to hear a conversation through a closed door, eavesdrop under an open window, or hear monsters moving stealthily in the forest. Or you might try to spot things that are obscured or easy to miss, whether they are orcs lying in ambush on a road, thugs hiding in the shadows of an alley, or candlelight under a closed secret door."}
             {:name "Performance"
              :key :performance
              :ability :cha
              :icon "guitar"
              :description "Your Charisma (Performance) check determines how well you can delight an audience with music, dance, acting, storytelling, or some other form of entertainment."}
             {:name "Persuasion"
              :key :persuasion
              :ability :cha
              :icon "convince"
              :description "When you attempt to influence someone or a group of people with tact, social graces, or good nature, the GM might ask you to make a Charisma (Persuasion) check. Typically, you use persuasion when acting in good faith, to foster friendships, make cordial requests, or exhibit proper etiquette. Examples of persuading others include convincing a chamberlain to let your party see the king, negotiating peace between warring tribes, or inspiring a crowd of townsfolk."}
             {:name "Religion"
              :key :religion
              :ability :int
              :icon "church"
              :description "Your Intelligence (Religion) check measures your ability to recall lore about deities, rites and prayers, religious hierarchies, holy symbols, and the practices of secret cults"}
             {:name "Sleight of Hand"
              :key :sleight-of-hand
              :ability :dex
              :icon "snatch"
              :description "Whenever you attempt an act of legerdemain or manual trickery, such as planting something on someone else or concealing an object on your person, make a Dexterity (Sleight of Hand)
check. The GM might also call for a Dexterity (Sleight of Hand) check to determine whether you can lift a coin purse off another person or slip something out of another person’s pocket"}
             {:name "Stealth"
              :key :stealth
              :ability :dex
              :icon "invisible"
              :description "Make a Dexterity (Stealth) check when you attempt to conceal yourself from enemies, slink past guards, slip away without being noticed, or sneak up on someone without being seen or heard."}
             {:name "Survival"
              :key :survival
              :ability :wis
              :icon "footsteps"
              :description "The GM might ask you to make a Wisdom (Survival) check to follow tracks, hunt wild game, guide your group through frozen wastelands, identify signs that owlbears live nearby, predict the weather, or avoid quicksand and other natural hazards."}])

(def skills-map (common/map-by-key skills))

(def skill-abilities
  (into {} (map (juxt :key :ability)) skills))

(defn skill-option [skill]
  (t/option-cfg
   {:name (:name skill)
    :key (:key skill)
    :help (:description skill)
    :modifiers [(modifiers/skill-proficiency (:key skill))]}))

(defn weapon-proficiency-option [{:keys [name key]}]
  (t/option
   name
   key
   nil
   [(modifiers/weapon-proficiency key)]))

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
    :modifiers [(modifiers/weapon (:key weapon) (or num 1))]}))

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

(defn ability-increase-option [num-increases different? ability-keys]
  (t/option-cfg
   {:name "Ability Score Improvement"
    :key :ability-score-improvement
    :ui-fn (fn [path built-template app-state built-char] (abilities-improvement-component num-increases different? ability-keys path built-template app-state built-char))
    :modifiers [(modifiers/deferred-ability-increases)]}))

(defn ability-increase-selection [abilities num & [different?]]
  (t/selection-cfg
   {:name "Ability Score Increase"
    :options [(ability-increase-option num different? abilities)]}))

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
          (fn [c] (let [prof-keys (:armor-profs c)]
                    (boolean (prof-keys armor-kw))))))

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
  (t/selection
   "Elemental Disciplines"
   elemental-disciplines))

(defn language-option [{:keys [name key]}]
  (t/option
   name
   key
   nil
   [(modifiers/language key)]))

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

(defn spell-help [spell]
  [:div
   [:div.m-b-5
    (spell-field "School" (:school spell))
    (spell-field "Casting Time" (:casting-time spell))
    (spell-field "Range" (:range spell))
    (spell-field "Duration" (:duration spell))]
   [:div.f-w-n (:description spell)]])

(defn spell-option [level spellcasting-ability class-name key]
  (let [{:keys [name] :as spell} (spells/spell-map key)]
    (t/option-cfg
     {:name name
      :key key
      :help (spell-help spell)
      :modifiers [(modifiers/spells-known level key spellcasting-ability class-name)]})))

(def memoized-spell-option (memoize spell-option))

(defn spell-options [spells level spellcasting-ability class-name]
  (map
   (partial memoized-spell-option level spellcasting-ability class-name)
   (sort spells)))

(defn spell-level-title [level]
  (if (zero? level) "Cantrip" (str "Level " level " Spell")))

(defn spell-selection
  ([class-key level spellcasting-ability class-name num]
   (spell-selection class-key level (get-in sl/spell-lists [class-key level]) spellcasting-ability class-name num))
  ([class-key level spell-keys spellcasting-ability class-name num]
   (t/selection-cfg
    {:name (spell-level-title level)
     :options (spell-options spell-keys level spellcasting-ability class-name)
     :min num
     :max num
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

(defn raw-bard-magical-secrets [level]
  (let [spell-slots (total-slots level 1)]
    (vec
     (for [i (range 2)]
       (t/selection
        (str "Magical Secrets " (inc i))
        (map
         (fn [[lvl _]]
           (t/option
            (spell-level-title lvl)
            (keyword (str lvl))
            [(spell-selection
              :bard
              lvl
              (reduce-kv
               (fn [s _ lvls]
                 (clojure.set/union s (lvls lvl)))
               #{}
               sl/spell-lists)
              :cha
              (class-names :bard)
              1)]
            []))
         spell-slots))))))

(defn bard-magical-secrets [min-level]
  (map
   (fn [s] (assoc s ::t/prereq-fn
                  (fn [built-char]
                    (let [bard-levels (-> (es/entity-val built-char :levels) :bard :class-level)]
                      (>= bard-levels min-level)))))
   (raw-bard-magical-secrets min-level)))

(defn cantrip-selections [class-key ability cantrips-known]
  (reduce
   (fn [m [k v]]
     (assoc m k [(spell-selection class-key 0 ability (class-names class-key) v)]))
   {}
   cantrips-known))

(defn apply-spell-restriction [spell-keys restriction]
  (if restriction
    (filter
     (fn [spell-key]
       (restriction (spells/spell-map spell-key)))
     spell-keys)
    spell-keys))

(defn spells-known-selections [{:keys [class-key
                                       level-factor
                                       spells-known
                                       known-mode
                                       spell-list
                                       spells
                                       ability] :as cfg}
                               cls-cfg]
  (prn "CLS_CFG" cls-cfg)
  (reduce
   (fn [m [cls-lvl v]]
     (let [[num restriction] (if (number? v) [v] ((juxt :num :restriction) v))
           slots (total-slots cls-lvl level-factor)
           all-spells (select-keys
                       (or spells (sl/spell-lists (or spell-list class-key)))
                       (keys slots))
           acquire? (= :acquire known-mode)]
       (assoc m
              cls-lvl
              (map
               (fn [[lvl spell-keys]]
                 (let [cls-key-nm (if (:key cls-cfg)
                                    (name (:key cls-cfg))
                                    (common/name-to-kw (:name cls-cfg)))
                       kw (keyword (str cls-key-nm "-spells-known-" lvl))]
                   (t/selection-cfg
                    {:name (str "Spells Known: " (:name cls-cfg) " Level " lvl)
                     :key kw
                     :ref kw
                     :min num
                     :max (if (not acquire?) num)
                     :tags #{:spells (keyword (str cls-key-nm "-spells")) (keyword (str "spells-" lvl))}
                     :options (map
                               (fn [spell-key]
                                 (let [spell (spells/spell-map spell-key)]
                                   (t/option-cfg
                                    {:name (:name spell)
                                     :key spell-key
                                     :help (spell-help spell)
                                     :modifiers [(modifiers/spells-known lvl spell-key ability (class-names class-key))]})))
                               (apply-spell-restriction spell-keys restriction))})))
               all-spells))
       
       #_(let [options (vec
                      (flatten
                       (map
                        (fn [[lvl spell-keys]]
                          (map
                           (fn [spell-key]
                             (let [spell (spells/spell-map spell-key)]
                               (t/option-cfg
                                {:name (str lvl " - " (:name spell))
                                 :key spell-key
                                 :help (spell-help spell)
                                 :modifiers [(modifiers/spells-known lvl spell-key ability (class-names class-key))]})))
                           (apply-spell-restriction spell-keys restriction)))
                        all-spells)))]
         (assoc m cls-lvl
                [(t/selection-cfg
                  {:name "Spells Known"
                   :options options
                   :min num
                   :max (if (not acquire?) num)
                   :new-item-fn (fn [selection selected-items _ key]
                                  {::entity/key key})
                   :tags #{:spells}})]))))
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
  (t/option
   (name class-key)
   class-key
   [(t/selection
     "Cantrip"
     (spell-options (get-in spell-lists [class-key 0]) 0 spellcasting-ability (class-names class-key))
     2 2)
    (t/selection
     "1st Level Spell"
     (spell-options (get-in spell-lists [class-key 1]) 1 spellcasting-ability (class-names class-key))
     1 1)]
   []))

(defn ritual-caster-option [class-key spellcasting-ability spell-lists]
  (t/option
   (name class-key)
   class-key
   [(t/selection
     "1st Level Ritual"
     (spell-options (filter (fn [spell-kw] (:ritual (spells/spell-map spell-kw))) (get-in spell-lists [class-key 1])) 1 spellcasting-ability (class-names class-key))
     2
     2)]
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

(defn maneuver-option [name & [desc]]
  (t/option
   name
   (common/name-to-kw name)
   nil
   [(modifiers/trait (str name " Maneuver")
                     desc)]))

(defn mod-maneuver-option [name mods]
  (t/option
   name
   (common/name-to-kw name)
   nil
   mods))

(defn proficiency-help [num singular plural]
  (str "Select additional " (if (> num 1) plural singular) " for which you are proficient."))

(defn skill-selection
  ([num]
   (skill-selection (map :key skills) num))
  ([options num & [order key prereq-fn]]
   (t/selection-cfg
    {:name "Skill Proficiency"
     :key key
     :order order
     :help (proficiency-help num "a skill" "skills")
     :options (skill-options
               (filter
                (comp (set options) :key)
                skills))
     :min num
     :max num
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
  (t/selection
   title
   [(t/select-option
     "Skill"
     [(skill-selection 1)])
    (t/select-option
     "Tool"
     [(tool-selection 1)])]))

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
  {::t/label "spellcasting ability"
   ::t/prereq-fn (fn [c] (some (fn [[k v]] (seq v)) (:spells-known c)))})

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
    [(ability-increase-selection [:str :dex] 1 false)]
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
     (modifiers/damage-resistance :trap)])
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
    [can-cast-spell-prereq])
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
    [(ability-increase-selection [:str :dex] 1 false)]
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
      [(magic-initiate-option :bard :cha sl/spell-lists)
       (magic-initiate-option :cleric :wis sl/spell-lists)
       (magic-initiate-option :druid :wis sl/spell-lists)
       (magic-initiate-option :sorcerer :cha sl/spell-lists)
       (magic-initiate-option :warlock :cha sl/spell-lists)
       (magic-initiate-option :wizard :int sl/spell-lists)])]
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
    [(ability-increase-selection [:str :dex] 1 false)]
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
    [(ability-increase-selection [:int :wis] 1 false)]
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
           (modifiers/saving-throws nil ability-key)]))
       character/ability-keys))]
    [])
   (t/select-option
    "Ritual Caster"
    [(t/selection
      "Spell Class"
      [(ritual-caster-option :bard :cha sl/spell-lists)
       (ritual-caster-option :cleric :wis sl/spell-lists)
       (ritual-caster-option :druid :wis sl/spell-lists)
       (ritual-caster-option :sorcerer :cha sl/spell-lists)
       (ritual-caster-option :warlock :cha sl/spell-lists)
       (ritual-caster-option :wizard :int sl/spell-lists)])]
    [{::t/label "Intelligence or Wisdom 13 or higher"
      ::t/prereq-fn (fn [{{:keys [wis int]} :abilities}]
                      (or (>= wis 13)
                          (>= int 13)))}])
   (t/option
    "Savage Attacker"
    :savage-attacker
    []
    [(modifiers/trait "Savage Attacker Feat")])
   (t/option
    "Sentinal"
    :sentinal
    []
    [(modifiers/trait "Sentinal Feat")])
   (t/mod-option
    "Sharpshooter"
    [(modifiers/trait "Sharpshooter Feat")])
   (t/mod-option
    "Shield Master"
    [(modifiers/trait "Shield Master Feat")])
   (t/select-option
    "Skilled"
    [(skilled-selection "Skill/Tool 1")
     (skilled-selection "Skill/Tool 2")
     (skilled-selection "Skill/tool 3")])
   (t/mod-option
    "Skulker"
    [(modifiers/trait "Skulker Feat")]
    [(ability-prereq :dex 13)])
   (t/mod-option
    "Spell Sniper"
    [(modifiers/trait "Spell Sniper Feat")]
    [can-cast-spell-prereq])
   (t/option
    "Tavern Brawler"
    :tavern-brawler
    [(ability-increase-selection [:str :dex] 1 false)]
    [(modifiers/weapon-proficiency :improvised)
     (modifiers/trait "Tavern Brawler Feat")])
   (t/mod-option
    "Tough"
    [(mods/modifier ?hit-point-level-bonus (+ 2 ?hit-point-level-bonus))])
   (t/mod-option
    "War Caster"
    [(modifiers/trait "War Caster Feat")])
   (t/select-option
    "Weapon Master"
    [(ability-increase-selection [:str :dex] 1 false)
     (weapon-proficiency-selection 4)])])


(def fighting-style-options
  [(t/option
    "Archery"
    :archery
    []
    [(modifiers/ranged-attack-bonus 2)
     (modifiers/trait-cfg
      {:name "Archery Fighting Style"
       :page 72
       :description "You gain a +2 bonus to attack rolls you make with ranged weapons."})])
   (t/option
    "Defense"
    :defense
    []
    [(modifiers/armored-ac-bonus 1)
     (modifiers/trait-cfg
      {:name "Defense Fighting Style"
       :page 72
       :description "While you are wearing armor, you gain a +1 bonus to AC."})])
   (t/option
    "Dueling"
    :dueling
    []
    [(modifiers/trait-cfg
      {:name "Dueling Fighting Style"
       :page 72
       :description "When you are wielding a melee weapon in one hand and no other weapons, you gain a +2 bonus to damage rolls with that weapon."})])
   (t/option
    "Great Weapon Fighting"
    :great-weapon-fighting
    []
    [(modifiers/trait-cfg
      {:name "Great Weapon Fighting Style"
       :page 72
       :description "When you roll a 1 or 2 on a damage die for an attack you make with a melee weapon that you are wielding with two hands, you can reroll the die and must use the new roll, even if the new roll is a 1 or a 2. The weapon must have the two-handed or versatile property for you to gain this benefit."})])
   (t/option
    "Protection"
    :protection
    []
    [(modifiers/reaction
      {:name "Protection Fighting Style"
       :page 72
       :description "When a creature you can see attacks a target other than you that is within 5 feet of you, you can use your reaction to impose disadvantage on the attack roll. You must be wielding a shield."})])
   (t/option
    "Two Weapon Fighting"
    :two-weapon-fighting
    []
    [(modifiers/trait-cfg
      {:name "Two Weapon Fighting"
       :description "When you engage in two-weapon fighting, you can add your ability modifier to the damage of the second attack."})])])

(defn fighting-style-selection [& [restrictions]]
  (t/selection
   "Fighting Style"
   (if restrictions
     (filter
      (fn [o]
        (restrictions (::t/key o)))
      fighting-style-options)
     fighting-style-options)))

(defn feat-selection [num]
  (t/selection
   (if (= 1 num) "Feat" "Feats")
   feat-options
   num
   num))

(defn ability-score-improvement-selection []
  (t/selection
   "Ability Score Improvement or Feat"
   [(ability-increase-option 2 false character/ability-keys)
    (t/option
     "Feat"
     :feat
     [(t/selection
       "Feat"
       feat-options)]
     [])]))

(defn expertise-selection [num & [key]]
  (t/selection-cfg
   {:name "Skill Expertise"
    :key (or key :skill-expertise)
    :options (mapv
              (fn [skill]
                (assoc
                 (t/option
                  (:name skill)
                  (:key skill)
                  nil
                  [(modifiers/skill-expertise (:key skill))])
                 ::t/prereq-fn
                 (fn [built-char]
                   (let [skill-profs (es/entity-val built-char :skill-profs)]
                     (and skill-profs (skill-profs (:key skill)))))))
              skills)
    :min num
    :max num
    :multiselect? true
    :tags #{:profs :skill-profs :expertise}}))

(def rogue-expertise-selection
  (t/selection-cfg
   {:name "Expertise"
    :tags #{:profs :skill-profs :expertise}
    :options [(t/option
               "Two Skills"
               :two-skills
               [(expertise-selection 2 :two-skills)]
               [])
              (t/option
               "One Skill/Theives Tools"
               :one-skill-thieves-tools
               [(expertise-selection 1 :one-skill-thieves-tools)]
               [(modifiers/tool-proficiency :thieves-tools)])]}))

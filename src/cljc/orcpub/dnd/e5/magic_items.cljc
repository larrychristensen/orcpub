(ns orcpub.dnd.e5.magic-items
  (:require [clojure.spec.alpha :as spec]
            [orcpub.common :as common]
            [orcpub.modifiers :as mod]
            [orcpub.entity :as entity]
            [orcpub.dnd.e5.armor :as armor5e]
            [orcpub.dnd.e5.weapons :as weapons5e]
            [orcpub.dnd.e5.equipment :as equip5e]
            [orcpub.dnd.e5.character :as char5e]
            [orcpub.dnd.e5.damage-types :as damage-types5e]
            [orcpub.dnd.e5.character.equipment :as char-equip5e]
            [orcpub.dnd.e5.modifiers :as mod5e]
            [orcpub.dnd.e5.units :as units5e]
            [clojure.string :as s]
            [clojure.set :refer [intersection difference]])
  #?(:cljs (:require-macros [orcpub.dnd.e5.modifiers :as mod5e])))

(spec/def ::name string?)
(spec/def ::type keyword?)
(spec/def ::rarity keyword?)
(spec/def ::description string?)
(spec/def ::magical-attack-bonus int?)
(spec/def ::magical-damage-bonus int?)
(spec/def ::modifiers (spec/coll-of ::mod/mod-cfg))
(spec/def ::subtypes (spec/coll-of keyword?))
(spec/def ::attunement (spec/coll-of keyword?))

(def name-key ::name)
(def item-type-key ::type)
(def item-subtype-key ::item-subtype)
(def description-key ::description)
(def summary-key ::summary)
(def magical-attack-bonus-key ::magical-attack-bonus)
(def magical-damage-bonus-key ::magical-damage-bonus)
(def subtypes-key ::subtypes)
(def attunement-key ::attunement)
(def modifiers-key ::modifiers)

(spec/def ::magic-item
  (spec/keys :req [::name]
             :opt [::type
                   ::rarity
                   ::description
                   ::modifiers
                   ::magical-attack-bonus
                   ::magical-damage-bonus
                   ::owner
                   ::subtypes
                   ::attunement]))

(spec/def ::internal-magic-item
  (spec/keys :opt [::name
                   ::type
                   ::rarity
                   ::description
                   ::magical-attack-bonus
                   ::magical-damage-bonus
                   ::attunement]))

(def toggle-mod-keys
  #{:damage-resistance
    :damage-vulnerability
    :damage-immunity
    :condition-immunity})

(def ability-mod-keys
  #{:ability
    :ability-override})

(def speed-mod-keys
  #{:speed
    :speed-override
    :flying-speed-bonus
    :flying-speed-override
    :flying-speed-equal-to-walking
    :swimming-speed
    :swimming-speed-override
    :swimming-speed-equal-to-walking
    :climbing-speed
    :climbing-speed-override
    :climbing-speed-equal-to-walking})

(defn add-internal-speed [mod-map speed-type mod-type value]
  (let [cfg {:type mod-type}]
    (assoc mod-map speed-type (if value (assoc cfg :value value) cfg))))

(defn to-internal-modifiers [modifiers]
  (reduce
   (fn [mod-map {:keys [::mod/key ::mod/args]}]
     (let [[arg-1 arg-2] (mod/raw-args args)]
       (cond
         (toggle-mod-keys key) (assoc-in mod-map [key arg-1] true)
         (ability-mod-keys key) (assoc-in mod-map
                                          [:ability arg-1]
                                          {:value arg-2
                                           :type (if (= :ability key)
                                                   :increases-by
                                                   :becomes-at-least)})
         (= key :saving-throw-bonus) (assoc-in mod-map
                                               [:save arg-1]
                                               {:value arg-2})
         :else (case key
                 :speed (add-internal-speed mod-map :speed :increases-by arg-1)
                 :speed-override (add-internal-speed mod-map :speed :becomes-at-least arg-1)
                 :flying-speed-bonus (add-internal-speed mod-map :flying-speed :increases-by arg-1)
                 :flying-speed-override (add-internal-speed mod-map :flying-speed :becomes-at-least arg-1)
                 :flying-speed-equal-to-walking (add-internal-speed mod-map :flying-speed :equals-walking-speed nil)
                 :swimming-speed (add-internal-speed mod-map :swimming-speed :increases-by arg-1)
                 :swimming-speed-override (add-internal-speed mod-map :swimming-speed :becomes-at-least arg-1)
                 :swimming-speed-equal-to-walking (add-internal-speed mod-map :swimming-speed :equals-walking-speed nil)
                 :climbing-speed (add-internal-speed mod-map :climbing-speed :increases-by arg-1)
                 :climbing-speed-override (add-internal-speed mod-map :climbing-speed :becomes-at-least arg-1)
                 :climbing-speed-equal-to-walking (add-internal-speed mod-map :climbing-speed :equals-walking-speed nil)))))
   {}
   modifiers))

(defn to-internal-item [{:keys [::modifiers ::subtypes] :as item}]
  (cond-> item
    (seq modifiers) (assoc ::internal-modifiers (to-internal-modifiers (::modifiers item)))
    true (dissoc ::modifiers)
    true (update ::attunement set)
    (seq subtypes) (update ::subtypes #(into #{} %))
    true entity/remove-empty-fields))

(defn mod-args [args]
  (map
   (fn [arg]
     (cond
       (string? arg) {::mod/string-arg arg}
       (keyword? arg) {::mod/keyword-arg arg}
       (int? arg) {::mod/int-arg arg}))
   args))

(defn mod-cfg [key & args]
  (cond-> {::mod/key key}
    (seq args) (assoc ::mod/args (mod-args args))))

(defn toggle-mods [kw value-map]
  (sequence
   (comp
    (filter val)
    (map #(mod-cfg kw (key %))))
   value-map))

(defn default-int [value]
  (if (int? value)
    value
    0))

(defn ability-mods [items]
  (map
   (fn [[ability-kw {:keys [value type]}]]
     (if (= type :increases-by)
       (mod-cfg :ability ability-kw (default-int value))
       (mod-cfg :ability-override ability-kw (default-int value))))
   items))

(defn speed-mod-fn [{:keys [increases-by becomes-at-least equals-walking-speed]}]
  (fn [{:keys [type value]}]
    (case type
      :increases-by (mod-cfg increases-by (default-int value))
      :equals-walking-speed (mod-cfg equals-walking-speed)
      (mod-cfg becomes-at-least (default-int value)))))

(def speed-mod
  (speed-mod-fn
   {:increases-by :speed
    :becomes-at-least :speed-override}))

(def flying-speed-mod
  (speed-mod-fn
   {:increases-by :flying-speed-bonus
    :becomes-at-least :flying-speed-override
    :equals-walking-speed :flying-speed-equal-to-walking}))

(def swimming-speed-mod
  (speed-mod-fn
   {:increases-by :swimming-speed
    :becomes-at-least :swimming-speed-override
    :equals-walking-speed :swimming-speed-equal-to-walking}))

(def climbing-speed-mod
  (speed-mod-fn
   {:increases-by :climbing-speed
    :becomes-at-least :climbing-speed-override
    :equals-walking-speed :climbing-speed-equal-to-walking}))

(defn save-mods [items]
  (map
   (fn [[ability-kw {:keys [value]}]]
     (mod-cfg :saving-throw-bonus ability-kw (default-int value)))
   items))

(defn from-internal-modifiers [modifiers]
  (reduce
   (fn [mod-vec [k v]]
     (concat mod-vec
             (cond
               (toggle-mod-keys k) (toggle-mods k v)
               (= :ability k) (ability-mods v)
               (= :save k) (save-mods v)
               (= :speed k) [(speed-mod v)]
               (= :flying-speed k) [(flying-speed-mod v)]
               (= :swimming-speed k) [(swimming-speed-mod v)]
               (= :climbing-speed k) [(climbing-speed-mod v)])))
   []
   modifiers))

(defn from-internal-item [item]
  (-> item
      (assoc ::modifiers (from-internal-modifiers (::internal-modifiers item)))
      (select-keys [:db/id
                    ::name
                    ::type
                    ::subtypes
                    ::rarity
                    ::description
                    ;::attunementb ;typo?
                    ::attunement
                    ::magical-damage-bonus
                    ::magical-attack-bonus
                    ::magical-ac-bonus
                    ::modifiers
                    ::weapons5e/type
                    ::weapons5e/damage-type
                    ::weapons5e/damage-die-count
                    ::weapons5e/damage-die
                    ::weapons5e/range
                    ::weapons5e/versatile
                    ::weapons5e/special?
                    ::weapons5e/melee?
                    ::weapons5e/ranged?
                    ::weapons5e/heavy?
                    ::weapons5e/thrown?
                    ::weapons5e/two-handed?
                    ::weapons5e/finesse?
                    ::weapons5e/reach?
                    ::weapons5e/ammunition?])
      entity/remove-empty-fields))

(defn sword? [w]
  (= :sword (::weapons5e/subtype w)))

(defn staff? [w]
  (= :staff (::weapons5e/subtype w)))

(defn axe? [w]
  (= :axe (::weapons5e/subtype w)))

(defn bow? [w]
  (or (= :longbow (::weapons5e/subtype w))
      (= :shortbow (::weapons5e/subtype w)))
  )

(defn slashing-sword? [w]
 (and (= :slashing (::weapons5e/damage-type w))
      (sword? w)))

(defn javelin? [w]
  (= :javelin (:key w)))

(defn mace? [w]
  (= :mace (:key w)))

(defn ammunition? [i] (= :ammunition (::weapons5e/type i)))

(def weapon-not-ammunition? (complement ammunition?))

(defn heavy-metal-armor? [a]
   (and (#{:medium :heavy} (:type a))
           (not= :hide (:key a))))

(defn not-shield? [a] (#{:light :medium :heavy} (:type a)))

(defn bonus-name-fn [name-kw bonus]
  (fn [item] (str (name-kw item) " +" bonus)))

(defn plus-1-name [name-kw] (bonus-name-fn name-kw 1))
(defn plus-2-name [name-kw] (bonus-name-fn name-kw 2))
(defn plus-3-name [name-kw] (bonus-name-fn name-kw 3))

(defn horn-of-valhalla [name rarity die & [requirement]]
  {
   name-key (str name " Horn of Valhalla")
   ::type :wondrous-item
   ::rarity rarity
   ::description (str "You can use an action to blow this horn. In response, "
                     die "d4 + " die " warrior spirits from the Valhalla appear within 60 feet of you. They use the statistics of a berserker.
They return to Valhalla after 1 hour or when they drop to 0 hit points. Once you use the horn, it can’t be used again until 7 days have passed."
                     (if requirement
                       (str "
You must have proficiency with all "
                            requirement
                            ". If you blow the horn without meeting this requirement, the summoned berserkers attack you. If you meet the requirement, they are friendly to you and your companions and follow your commands.")))
   })

(defn potion-of-giant-strength [name strength rarity]
  {
   name-key (str "Potion of " name " Strength")
   ::type :potion
   ::rarity rarity
   ::description (str "When you drink this potion, your Strength score becomes " strength " for 1 hour. The potion has no effect on you if your Strength is equal to or greater than " strength ".
This potion’s transparent liquid has floating in it a sliver of fingernail from a giant of the appropriate type. The potion of frost giant strength and the potion of stone giant strength have the same effect.")})

(defn figurine-of-wondrous-power [rarity name description]
  {
   name-key (str "Figurine of Wondrous Power: " name)
   ::type :wondrous-item
   ::rarity rarity
   ::description (str "A figurine of wondrous power is a statuette of a beast small enough to fit in a pocket. If you use an action to speak the command word and throw the figurine to a point on the ground within 60 feet of you, the figurine becomes a living creature. If the space where the creature would appear is occupied by other creatures or objects, or if there isn’t enough space for the creature, the figurine doesn’t become a creature.
The creature is friendly to you and your companions. It understands your languages and obeys your spoken commands. If you issue no
commands, the creature defends itself but takes no other actions.
The creature exists for a duration specific to each figurine. At the end of the duration, the creature reverts to its figurine form. It reverts to a figurine early if it drops to 0 hit points or if you use an action to speak the command word again while touching it. When the creature becomes a figurine again, its property can’t be used again until a certain amount of time has passed, as specified in the figurine’s description.
" description)})

(defn belt-of-giant-strength-mod [value]
  (mod/vec-mod ?ability-overrides
               {:ability :orcpub.dnd.e5.character/str :value
                (min 30 (+ value (if (and ?giants-bane-gauntlet ?giants-bane-hammer) 4 0)))}))

(defn dragon-scale-mail [color-nm resistance-kw]
  {name-key (str "Dragon Scale Mail, " color-nm)
   ::magical-ac-bonus 1
   ::type :armor
   ::item-subtype :scale-mail

   ::rarity :very-rare

   ::attunement [:any]
   ::modifiers [(mod5e/damage-resistance resistance-kw)
               (mod5e/saving-throw-advantage ["'Frightful Presence' spell" "breath weapons of dragons"])
               (mod5e/action
                {:name "Dragon Scale Mail"
                 :page 165
                 :source :dmg
                 :frequency units5e/days-1
                 :summary "know location of closest dragon within 30 miles"})]
   ::description (str "Dragon scale mail is made of the scales of one kind of dragon. Sometimes dragons collect their cast-off scales and gift them to humanoids. Other times, hunters carefully skin and preserve the hide of a dead dragon. In either case, dragon scale mail is highly valued.
While wearing this armor, you gain a +1 bonus to AC, you have advantage on saving throws against the Frightful Presence and breath weapons of dragons, and you have resistance to " (name resistance-kw) " damage.
Additionally, you can focus your senses as an action to magically discern the distance and
direction to the closest dragon within 30 miles of you that is of the same type as the armor. This special action can’t be used again until the next dawn."
                     )})

(defn
  ^{:doc "Generic function for creating magic items with + bonuses.
   Use :sp-atk-mod for spell-attack-modifier bonuses.
   Use :sp-dc-mod for spell DC bonuses"
   ; :test (fn [] ())
   ; :arglists ([name ])
    :user/comment "This 'cleans' up item definitions... but could make them harder to read if it was applied all the way around."}

  caster-bonus-item [name bonus type rarity attunement modv description]
  (let  [full-name (str name " +" bonus)]
    {name-key full-name
     ::type type
     ::rarity rarity
     ::attunement (if (vector? attunement) attunement [attunement]) ;array should be passed not just one keyword
     ::modifiers [(for [i modv]
                    (cond (= i :sp-atk-mod) (mod5e/spell-attack-modifier-bonus bonus)
                          (= i :sp-dc-mod) (mod5e/spell-save-dc-bonus bonus)))]
     ::decription description}))

(defn rod-of-the-pact-keeper [bonus]
  {name-key (str "Rod of the Pact Keeper +" bonus)
   ::type :rod
   ::rarity :uncommon
   ::attunement [:warlock]
   ::modifiers [(mod5e/spell-save-dc-bonus bonus)
               (mod5e/spell-attack-modifier-bonus bonus)
               (mod5e/bonus-action
                {:name "Rod of the Pact Keeper"
                 :page 197
                 :source :dmg
                 :frequency units5e/long-rests-1
                 :summary "Regain a warlock spell slot"})]
   ::summary (str (common/bonus-str bonus)
                   " to spell attack rolls and saving throw DCs for your warlock spells")
   })

(defn ioun-stone [name rarity description & modifiers]
  (let [full-name (str "Ioun Stone (" name ")")]
    {
     name-key full-name
     ::type :wondrous-item
     ::rarity rarity
     ::modifiers (conj
                 modifiers
                 (mod5e/trait-cfg
                  {:name full-name
                   :page 177
                   :source :dmg
                   :summary description}))
     ::attunement [:any]
     ::description (str "An Ioun stone is named after Ioun, a god of knowledge and prophecy revered on some worlds. Many types of Ioun stone exist, each type a distinct combination of shape and color.
When you use an action to toss one of these stones into the air, the stone orbits your head at a distance of 1d3 feet and confers a benefit to you. Thereafter, another creature must use an action to grasp or net the stone to separate it from you, either by making a successful attack roll against AC 24 or a successful DC 24 Dexterity (Acrobatics) check. You can use an action to seize and stow the stone, ending its effect.
A stone has AC 24, 10 hit points, and resistance to all damage. It is considered to be an object that is being worn while it orbits your head." description)
     }))

(def armors-of-resistance
  (map
   (fn [damage-type]
     {
      name-key (str "Armor of Resistance (" (s/capitalize (name damage-type)) ")")
      ::type :armor
      ::item-subtype not-shield?
      ::rarity :rare

      ::attunement [:any]
      ::modifiers [(mod5e/damage-resistance damage-type)]
      ::description "You have resistance to one type of damage while you wear this armor. The GM chooses the type or determines it randomly from the options below."
      })
   damage-types5e/damage-types))

(def vulnerable-types #{:bludgeoning :piercing :slashing})

(def armors-of-vulnerability
  (map
   (fn [damage-type]
     (let [other-types (disj vulnerable-types damage-type)]
       {
        name-key (str "Armor of Vulnerability (" (s/capitalize (name damage-type)) ")")
        ::type :armor
        ::item-subtype :plate
        ::rarity :rare

        ::modifiers (conj
                    (map
                     (fn [other-type]
                       (mod5e/damage-vulnerability other-type))
                     other-types)
                    (mod5e/damage-resistance damage-type))
        ::attunement [:any]
        ::description (str "While wearing this armor, you have resistance to "
                          (name damage-type)
                          " damage.
Curse. This armor is cursed, a fact that is revealed only when an identify spell is cast on the armor or you attune to it. Attuning to the armor curses you until you are targeted by the remove curse spell or similar magic; removing the armor fails to end the curse. While cursed, you have vulnerability to "
                          (common/list-print other-types)
                          " damage.")
        }))
   vulnerable-types))

(def rings-of-resistance
  (map
   (fn [damage-type]
     {
      name-key (str "Ring of Resistance (" (s/capitalize (name damage-type)) ")")
      ::type :ring

      ::rarity :rare

      ::modifiers [(mod5e/damage-resistance damage-type)]
      ::attunement [:any]
      ::description (str "You have resistance to " (name damage-type) " damage.")
      })
   damage-types5e/damage-types))

(def raw-magic-items
  (concat
   armors-of-resistance
   armors-of-vulnerability
   rings-of-resistance
   [{
     name-key "Adamantine Armor"
     ::type :armor
     ::item-subtype heavy-metal-armor?
     ::rarity :uncommon
     ::description "This suit of armor is reinforced with adamantine, one of the hardest substances in existence. While you’re wearing it, any critical hit against you becomes a normal hit."
     }{
     name-key "Alchemy Jug"
     ::type :wondrous-item
     ::rarity :uncommon
     ::modifiers [(mod5e/action
                  {:name "Alchemy Jug: Create Liquid"
                   :page 150
                   :source :dmg
                   :frequncy units5e/days-1
                   :summary "Create acid, poison, beer, honey, or mayonnaise in the jug."})
                 (mod5e/action
                  {:name "Alchemy Jug: Pour Liquid"
                   :page 150
                   :source :dmg
                   :summary "Pour out liquid created in the jug."})]
     ::summary "Jug that creates acid, poison, beer, honey, or mayonnaise"}
    {
     name-key "Ammunition, +1"
     :name-fn (plus-1-name :name)
     ::type :weapon
     ::item-subtype ammunition?
     ::rarity :uncommon
     ::magical-attack-bonus 1
     ::magical-damage-bonus 1
     ::description "You have a +1 bonus to attack and damage rolls made with this piece of magic ammunition. Once it hits a target, the ammunition is no longer magical."
     }{
     name-key "Ammunition, +2"
     :name-fn (plus-2-name :name)
     ::type :weapon
     ::item-subtype ammunition?
     ::rarity :rare
     ::magical-attack-bonus 2
     ::magical-damage-bonus 2
     ::description "You have a +2 bonus to attack and damage rolls made with this piece of magic ammunition. Once it hits a target, the ammunition is no longer magical."
     }{
     name-key "Ammunition, +3"
     :name-fn (plus-3-name :name)
     ::type :weapon
     ::item-subtype ammunition?
     ::rarity :very-rare
     ::magical-attack-bonus 3
     ::magical-damage-bonus 3
     ::description "You have a +3 bonus to attack and damage rolls made with this piece of magic ammunition. The bonus is determined by the rarity of the ammunition. Once it hits a target, the ammunition is no longer magical."
     }{
     name-key "Amulet of Health"
     ::type :wondrous-item
     ::rarity :rare
     ::attunement [:any]
     ::modifiers [(mod5e/ability-override ::char5e/con 19)]
     ::description "Your Constitution score is 19 while you wear this amulet. It has no effect on you if your Constitution is already 19 or higher."
     }{
     name-key "Amulet of Proof against Detection and Location"
     ::type :wondrous-item
     ::rarity :uncommon

     ::attunement [:any]
     ::description "While wearing this amulet, you are hidden from divination magic. You can’t be targeted by such magic or perceived through magical scrying sensors."
     }{
     name-key "Amulet of the Planes"
     ::type :wondrous-item
     ::rarity :very-rare

     ::attunement [:any]
     ::description "While wearing this amulet, you can use an action to name a location that you are familiar with on another plane of existence. Then make a DC 15 Intelligence check. On a successful check, you cast the plane shift spell. On a failure, you and each creature and object within 15 feet of you travel to a random destination. Roll a d100. On a 1–60, you travel to a random location on the plane you named. On a 61–100, you travel to a randomly determined plane of existence."
     }
    {
     name-key "Animated Shield"
     ::type :armor
     ::item-subtype :shield
     ::rarity :very-rare

     ::attunement [:any]
     ::description "While holding this shield, you can speak its command word as a bonus action to cause it to animate. The shield leaps into the air and hovers in your space to protect you as if you were wielding it, leaving your hands free. The shield remains animated for 1 minute, until you use a bonus action to end this effect, or until you are incapacitated or die, at which point the shield falls to the ground or into your hand if you have one free."
     }
    {
     name-key "Apparatus of the Crab"
     ::type :wondrous-item
     ::rarity :legendary
     ::description "This item first appears to be a Large sealed iron barrel weighing 500 pounds. The barrel has a hidden catch, which can be found with a successful DC 20 Intelligence (Investigation) check. Releasing the catch unlocks a hatch at one end of the barrel, allowing two Medium or smaller creatures to crawl inside. Ten levers are set in a row at the far end, each in a neutral position, able to move either up or down. When certain levers are used, the apparatus transforms to resemble a giant lobster.
The apparatus of the Crab is a Large object with the following statistics:
Armor Class: 20
Hit Points: 200
Speed: 30 ft., swim 30 ft. (or 0 ft. for both if the legs and tail aren’t extended)
Damage Immunities: poison, psychic
To be used as a vehicle, the apparatus requires one pilot. While the apparatus’s hatch is closed, the compartment is airtight and watertight. The compartment holds enough air for 10 hours of breathing, divided by the number of breathing creatures inside.
The apparatus floats on water. It can also go underwater to a depth of 900 feet. Below that, the vehicle takes 2d6 bludgeoning damage per minute from pressure.
A creature in the compartment can use an action to move as many as two of the apparatus’s levers up or down. After each use, a lever goes back to its neutral position. Each lever, from left to right, functions as shown in the Apparatus of the Crab Levers table."
     }{
     name-key "Armor, +1"
     :name-fn (plus-1-name :name)
     ::magical-ac-bonus 1
     ::type :armor
     ::item-subtype not-shield?
     ::rarity :rare
     ::description "You have a +1 bonus to AC while wearing this armor."
     }{
     name-key "Armor, +2"
     ::magical-ac-bonus 2
     :name-fn (plus-2-name :name)
     ::type :armor
     ::item-subtype not-shield?
     ::rarity :very-rare
     ::description "You have a +2 bonus to AC while wearing this armor."
     }{
     name-key "Armor, +3"
     ::magical-ac-bonus 3
     :name-fn (plus-3-name :name)
     ::type :armor
     ::item-subtype not-shield?
     ::rarity :legendary
     ::description "You have a +3 bonus to AC while wearing this armor."
     }{
     name-key "Armor of Invulnerability"
     ::type :armor
     ::item-subtype :plate
     ::rarity :legendary

     ::attunement [:any]
     ::modifiers [(mod5e/damage-resistance :nonmagical)]
     ::description "You have resistance to nonmagical damage while you wear this armor. Additionally, you can use an action to make yourself immune to nonmagical damage for 10 minutes or until you are no longer wearing the armor. Once this special action is used, it can’t be used again until the next dawn."
     }{
     name-key "Arrow-Catching Shield"
     ::type :armor
     ::item-subtype :shield
     ::rarity :rare
     ::attunement [:any]
     ::description "You gain a +2 bonus to AC against ranged attacks while you wield this shield. This bonus is in addition to the shield’s normal bonus to AC. In addition, whenever an attacker makes a ranged attack against a target within 5 feet of you, you can use your reaction to become the target of the attack instead."
     }{
     name-key "Arrow of Slaying"
     ::type :weapon
     ::item-subtype :arrow
     ::rarity :very-rare
     ::description "An arrow of slaying is a magic weapon meant to slay a particular kind of creature. Some are more focused than others; for example, there are both arrows of dragon slaying and arrows of blue dragon slaying. If a creature belonging to the type, race, or group associated with an arrow of slaying takes damage from the arrow, the creature must make a DC 17 Constitution saving throw, taking an extra 6d10 piercing damage on a failed save, or half as much extra damage on a successful one.
Once an arrow of slaying deals its extra damage to a creature, it becomes a nonmagical arrow.
Other types of magic ammunition of this kind exist, such as bolts of slaying meant for a crossbow, though arrows are most common."
     }{
     name-key "Bag of Beans"
     ::type :wondrous-item
     ::rarity :rare
     ::description "Inside this heavy cloth bag are 3d4 dry beans. The bag weighs 1/2 pound plus 1/4 pound for each bean it contains.
If you dump the bag’s contents out on the ground, they explode in a 10-foot radius, extending from the beans. Each creature in the area, including you, must make a DC 15 Dexterity saving throw, taking 5d4 fire damage on a failed save, or half as much damage on a successful one. The fire ignites flammable objects in the area that aren’t being worn or carried.
If you remove a bean from the bag, plant it in dirt or sand, and then water it, the bean produces an effect 1 minute later from the ground where it was planted. The GM can choose an effect from the following table, determine it randomly, or create an effect."
     }{
     name-key "Bag of Devouring"
     ::type :wondrous-item
     ::rarity :very-rare
     ::description "This bag superficially resembles a bag of holding but is a feeding orifice for a gigantic extradimensional creature. Turning the bag inside out closes the orifice.
The extradimensional creature attached to the bag can sense whatever is placed inside the bag. Animal or vegetable matter placed wholly in the bag is devoured and lost forever. When part of a living creature is placed in the bag, as happens when someone reaches inside it, there is a 50 percent chance that the creature is pulled inside the bag. A creature inside the bag can use its action to try to escape with a successful DC 15 Strength check. Another creature can use its action to reach into the bag to pull a creature out, doing so with a successful DC 20 Strength check (provided it isn’t pulled inside the bag first). Any creature that starts its turn inside the bag is devoured, its body destroyed.
Inanimate objects can be stored in the bag, which can hold a cubic foot of such material. However, once each day, the bag swallows any objects inside it and spits them out into another plane of existence. The GM determines the time and plane.
If the bag is pierced or torn, it is destroyed, and anything contained within it is transported to a random location on the Astral Plane."
     }{
     name-key "Bag of Holding"
     ::type :wondrous-item
     ::rarity :uncommon
     ::description "This bag has an interior space considerably larger than its outside dimensions, roughly 2 feet in diameter at the mouth and 4 feet deep. The bag can hold up to 500 pounds, not exceeding a volume of 64 cubic feet. The bag weighs 15 pounds, regardless of
its contents. Retrieving an item from the bag requires an action.
If the bag is overloaded, pierced, or torn, it ruptures and is destroyed, and its contents are scattered in the Astral Plane. If the bag is turned inside out, its contents spill forth, unharmed, but the bag must be put right before it can be used again. Breathing creatures inside the bag can survive up to a number of minutes equal to 10 divided by the number of creatures (minimum 1 minute), after which time they begin to suffocate.
Placing a bag of holding inside an extradimensional space created by a handy haversack, portable hole, or similar item instantly destroys both items and opens a gate to the Astral Plane. The gate originates where the one item was placed inside the other. Any creature within 10 feet of the gate is sucked through it to a random location on the Astral Plane. The gate then closes. The gate is one-way only and can’t be reopened."
     }{
     name-key "Bag of Tricks"
     ::type :wondrous-item
     ::rarity :uncommon
     ::description "This ordinary bag, made from gray, rust, or tan cloth, appears empty. Reaching inside the bag, however, reveals the presence of a small, fuzzy object. The bag weighs 1/2 pound.
You can use an action to pull the fuzzy object from the bag and throw it up to 20 feet. When the object
lands, it transforms into a creature you determine by rolling a d8 and consulting the table that corresponds to the bag’s color. The creature vanishes at the next dawn or when it is reduced to 0 hit points.
The creature is friendly to you and your companions, and it acts on your turn. You can use a bonus action to command how the creature moves and what action it takes on its next turn, or to give it general orders, such as to attack your enemies. In the absence of such orders, the creature acts in a fashion appropriate to its nature.
Once three fuzzy objects have been pulled from the bag, the bag can’t be used again until the next dawn."
     }{
     name-key "Bead of Force"
     ::type :wondrous-item
     ::rarity :rare
     ::description "This small black sphere measures 3/4 of an inch in diameter and weighs an ounce. Typically, 1d4 + 4 beads of force are found together.
You can use an action to throw the bead up to 60 feet. The bead explodes on impact and is destroyed. Each creature within a 10-foot radius of where the bead landed must succeed on a DC 15 Dexterity saving throw or take 5d4 force damage. A sphere of transparent force then encloses the area for 1 minute. Any creature that failed the save and is completely within the area is trapped inside this sphere. Creatures that succeeded on the save, or are partially within the area, are pushed away from the center of the sphere until they are no longer inside it. Only breathable air can pass through the sphere’s wall. No attack or other effect can.
An enclosed creature can use its action to push against the sphere’s wall, moving the sphere up to half the creature’s walking speed. The sphere can be picked up, and its magic causes it to weigh only 1 pound, regardless of the weight of creatures inside."
     }{name-key "Belt of Dwarvenkind"
       ::type :wondrous-item
       ::rarity :varies
       ::attunement [:any]
       ::modifiers [(mod5e/saving-throw-advantage ["poison"])
                    (mod5e/darkvision 60)
                    (mod5e/language :dwarvish)
                    (mod5e/ability ::char5e/con 2)]
       ::description "While wearing this belt, you gain the following benefits:
• Your Constitution score increases by 2, to a maximum of 20.
• You have advantage on Charisma (Persuasion) checks made to interact with dwarves.
In addition, while attuned to the belt, you have a 50 percent chance each day at dawn of growing a full beard if you’re capable of growing one, or a visibly thicker beard if you already have one.
If you aren’t a dwarf, you gain the following additional benefits while wearing the belt:
• You have advantage on saving throws against poison, and you have resistance against poison damage.
• You have darkvision out to a range of 60 feet.
• You can speak, read, and write Dwarvish. (requires attunement)"}
{
     name-key "Belt of Hill Giant Strength"

     ::type :wondrous-item

     ::rarity :rare

     ::attunement [:any]
     ::modifiers [(belt-of-giant-strength-mod 21)]
     ::description "While wearing this belt, your Strength score changes to 21. If your Strength is already equal to or greater than 21, the item has no effect on you."
     }
    {
     name-key "Belt of Stone Giant Strength"

     ::type :wondrous-item

     ::rarity :very-rare

     ::attunement [:any]
     ::modifiers [(belt-of-giant-strength-mod 23)]
     ::description "While wearing this belt, your Strength score changes to 23. If your Strength is already equal to or greater than 23, the item has no effect on you."
     }
    {
     name-key "Belt of Frost Giant Strength"

     ::type :wondrous-item

     ::rarity :very-rare

     ::attunement [:any]
     ::modifiers [(belt-of-giant-strength-mod 23)]

     ::description "While wearing this belt, your Strength score changes to 23. If your Strength is already equal to or greater than 23, the item has no effect on you."
     }
    {
     name-key "Belt of Fire Giant Strength"

     ::type :wondrous-item

     ::rarity :very-rare

     ::attunement [:any]
     ::modifiers [(belt-of-giant-strength-mod 25)]
     ::description "While wearing this belt, your Strength score changes to 25. If your Strength is already equal to or greater than 25, the item has no effect on you."
     }
    {
     name-key "Belt of Cloud Giant Strength"

     ::type :wondrous-item

     ::rarity :legendary

     ::attunement [:any]
     ::modifiers [(belt-of-giant-strength-mod 27)]
     ::description "While wearing this belt, your Strength score changes to 27. If your Strength is already equal to or greater than 27, the item has no effect on you."
     }
    {
     name-key "Belt of Storm Giant Strength"

     ::type :wondrous-item

     ::rarity :legendary

     ::attunement [:any]
     ::modifiers [(belt-of-giant-strength-mod 29)]
     ::description "While wearing this belt, your Strength score changes to 29. If your Strength is already equal to or greater than 29, the item has no effect on you."
     }
    {
     name-key "Berserker Axe"
     ::type :weapon
     ::item-subtype axe?
     ::rarity :rare
     ::magical-attack-bonus 1
     ::magical-damage-bonus 1
     ::attunement [:any]
     ::description "You gain a +1 bonus to attack and damage rolls made with this magic weapon. In addition, while you are attuned to this weapon, your hit point maximum increases by 1 for each level you have attained.
Curse. This axe is cursed, and becoming attuned to it extends the curse to you. As long as you remain cursed, you are unwilling to part with the axe, keeping it within reach at all times. You also have disadvantage on attack rolls with weapons other
than this one, unless no foe is within 60 feet of you that you can see or hear.
Whenever a hostile creature damages you while the axe is in your possession, you must succeed on a DC 15 Wisdom saving throw or go berserk. While berserk, you must use your action each round to attack the creature nearest to you with the axe. If you can make extra attacks as part of the Attack action, you use those extra attacks, moving to attack the next nearest creature after you fell your current target. If you have multiple possible targets, you attack one at random. You are berserk until you start your turn with no creatures within 60 feet of you that you can see or hear."
     }{
     name-key "Boots of Elvenkind"
     ::type :wondrous-item
     ::rarity :uncommon
     ::description "While you wear these boots, your steps make no sound, regardless of the surface you are moving across. You also have advantage on Dexterity (Stealth) checks that rely on moving silently."
     }{
     name-key "Boots of Levitation"
     ::type :wondrous-item
     ::rarity :rare

     ::attunement [:any]
     ::description "While you wear these boots, you can use an action to cast the levitate spell on yourself at will."
     ::modifiers [(mod5e/action
                  {:name "Levitate"
                   :source :dmg
                   :page 155
                   :summary "cast levitate at will"})]}
    {
     name-key "Boots of Speed"
     ::type :wondrous-item
     ::rarity :rare

     ::attunement [:any]
     ::modifiers [(mod5e/bonus-action
                  {:name "Boots of Speed"
                   :source :dmg
                   :page 155
                   :frequency units5e/long-rests-1
                   :duration units5e/minutes-10
                   :summary "Activate Boots of Speed and double your walking speed and opportunity attacks against you have disadvantage"})]

     ::description "While you wear these boots, you can use a bonus action and click the boots’ heels together. If you do, the boots double your walking speed, and any creature that makes an opportunity attack against you has disadvantage on the attack roll. If you click your heels together again, you end the effect.
When the boots’ property has been used for a total of 10 minutes, the magic ceases to function until you finish a long rest."}
    {
     name-key "Boots of Striding and Springing"
     ::type :wondrous-item
     ::rarity :uncommon

     ::attunement [:any]
     ::modifiers [(mod5e/speed-override 30)]
     ::description "While you wear these boots, your walking speed becomes 30 feet, unless your walking speed is higher, and your speed isn’t reduced if you are encumbered or wearing heavy armor. In addition, you can jump three times the normal distance, though you can’t jump farther than your remaining movement would allow."
     }{
     name-key "Boots of the Winterlands"
     ::type :wondrous-item
     ::rarity :uncommon

     ::attunement [:any]
     ::modifiers [(mod5e/damage-resistance :cold)]
     ::description "These furred boots are snug and feel quite warm. While you wear them, you gain the following benefits:
• You have resistance to cold damage.
• You ignore difficult terrain created by ice or snow.
• You can tolerate temperatures as low as −50 degrees Fahrenheit without any additional protection. If you wear heavy clothes, you can tolerate temperatures as low as −100 degrees Fahrenheit."
     }{
     name-key "Bowl of Commanding Water Elementals"
     ::type :wondrous-item
     ::rarity :rare
     ::description "While this bowl is filled with water, you can use an action to speak the bowl’s command word and summon a water elemental, as if you had cast the conjure elemental spell. The bowl can’t be used this way again until the next dawn.
The bowl is about 1 foot in diameter and half as deep. It weighs 3 pounds and holds about 3 gallons."
     }{
     name-key "Bracers of Archery"
     ::type :wondrous-item
     ::rarity :uncommon

     ::attunement [:any]
     ::modifiers [(mod5e/weapon-proficiency :longbow)
                  (mod5e/weapon-proficiency :shortbow)
                  (mod5e/weapon-damage-bonus-mod #{:longbow :shortbow} 2)]
     ::description "While wearing these bracers, you have proficiency with the longbow and shortbow, and you gain a +2 bonus to damage rolls on ranged attacks made with such weapons."
     }{
     name-key "Bracers of Defense"
     ::type :wondrous-item
     ::rarity :rare

     ::attunement [:any]
     ::modifiers [(mod5e/unarmored-ac-bonus 2)]
     ::description "While wearing these bracers, you gain a +2 bonus to AC if you are wearing no armor and using no shield."
     }{
     name-key "Brazier of Commanding Fire Elementals"
     ::type :wondrous-item
     ::rarity :rare
     ::description "While a fire burns in this brass brazier, you can use an action to speak the brazier’s command word and summon a fire elemental, as if you had cast the conjure elemental spell. The brazier can’t be used this way again until the next dawn.
The brazier weighs 5 pounds."
     }{
     name-key "Brooch of Shielding"
     ::type :wondrous-item
     ::rarity :uncommon

     ::attunement [:any]
     ::modifiers [(mod5e/damage-resistance :force) (mod5e/damage-immunity :magic-missile)]
     ::description "While wearing this brooch, you have resistance to force damage, and you have immunity to damage from the magic missile spell."
     }{
     name-key "Broom of Flying"
     ::type :wondrous-item
     ::rarity :uncommon
     ::description "This wooden broom, which weighs 3 pounds, functions like a mundane broom until you stand astride it and speak its command word. It then hovers beneath you and can be ridden in the air. It has a flying speed of 50 feet. It can carry up to 400 pounds, but its flying speed becomes 30 feet while carrying over 200 pounds. The broom stops hovering when you land.
You can send the broom to travel alone to a destination within 1 mile of you if you speak the command word, name the location, and are familiar with that place. The broom comes back to you when you speak another command word, provided that the broom is still within 1 mile of you."
     }{
     name-key "Candle of Invocation"
     ::type :wondrous-item
     ::rarity :very-rare

     ::attunement [:any]
     ::description "This slender taper is dedicated to a deity and shares that deity’s alignment. The candle’s alignment can be detected with the detect evil and good spell. The GM chooses the god and associated alignment or determines the alignment randomly
The candle’s magic is activated when the candle is lit, which requires an action. After burning for 4 hours, the candle is destroyed. You can snuff it out early for use at a later time. Deduct the time it burned in increments of 1 minute from the candle’s total burn time.
While lit, the candle sheds dim light in a 30-foot radius. Any creature within that light whose alignment matches that of the candle makes attack rolls, saving throws, and ability checks with advantage. In addition, a cleric or druid in the light whose alignment matches the candle’s can cast 1stlevel
spells he or she has prepared without expending spell slots, though the spell’s effect is as if cast with a 1st-level slot.
Alternatively, when you light the candle for the first time, you can cast the gate spell with it. Doing so destroys the candle."
     }
    {
     name-key "Cap of Water Breathing"
     ::type :wondrous-item
     ::rarity :uncommon
     ::modifiers [(mod5e/action
                  {:name "Cap of Water Breathing"
                   :page 157
                   :source :dmg
                   :summary "Create a bubble of air around you head in which you can breathe normally"})]
     ::summary "Breathe underwater"}
    {
     name-key "Cape of the Mountebank"
     ::type :wondrous-item
     ::rarity :rare
     ::modifiers [(mod5e/action
                  {:name "Cape of the Mountebank"
                   :page 157
                   :source :dmg
                   :frequency units5e/days-1
                   :summary "Cast 'dimension door'"})]
     ::description "This cape smells faintly of brimstone. While wearing it, you can use it to cast the dimension door spell as an action. This property of the cape can’t be used again until the next dawn.
When you disappear, you leave behind a cloud of smoke, and you appear in a similar cloud of smoke at your destination. The smoke lightly obscures the space you left and the space you appear in, and it dissipates at the end of your next turn. A light or stronger wind disperses the smoke."
     }{
     name-key "Carpet of Flying"
     ::type :wondrous-item
     ::rarity :very-rare
     ::description "You can speak the carpet’s command word as an action to make the carpet hover and fly. It moves according to your spoken directions, provided that you are within 30 feet of it.
Four sizes of carpet of flying exist. The GM chooses the size of a given carpet or determines it randomly.
A carpet can carry up to twice the weight shown on the table, but it flies at half speed if it carries more than its normal capacity."
     }{
     name-key "Censer of Controlling Air Elementals"
     ::type :wondrous-item
     ::rarity :rare
     ::description "While incense is burning in this censer, you can use an action to speak the censer’s command word and summon an air elemental, as if you had cast the conjure elemental spell. The censer can’t be used this way again until the next dawn.
This 6-inch-wide, 1-foot-high vessel resembles a chalice with a decorated lid. It weighs 1 pound."
     }{
     name-key "Chime of Opening"
     ::type :wondrous-item
     ::rarity :rare
     ::description "This hollow metal tube measures about 1 foot long and weighs 1 pound. You can strike it as an action, pointing it at an object within 120 feet of you that can be opened, such as a door, lid, or lock. The chime
issues a clear tone, and one lock or latch on the object opens unless the sound can’t reach the object. If no locks or latches remain, the object itself opens.
The chime can be used ten times. After the tenth time, it cracks and becomes useless."
     }{
     name-key "Circlet of Blasting"
     ::type :wondrous-item
     ::rarity :uncommon
     ::modifiers [(mod5e/action
                  {:name "Circlet of Blasting"
                   :page 158
                   :source :dmg
                   :frequency units5e/days-1
                   :summary "cast 'scorching ray' with +5 attack bonus"})]
     ::description "While wearing this circlet, you can use an action to cast the scorching ray spell with it. When you make the spell’s attacks, you do so with an attack bonus of +5. The circlet can’t be used this way again until the next dawn."
     }{
     name-key "Cloak of Arachnida"
     ::type :wondrous-item

     ::rarity :very-rare

     ::attunement [:any]
     ::modifiers [(mod5e/damage-resistance :poison)
                 (mod5e/action
                  {:name "Cloak of Arachnidia"
                   :page 158
                   :source :dmg
                   :frequency units5e/days-1
                   :summary "cast 'web' with save DC 13 and 2X area"})]
     ::description "This fine garment is made of black silk interwoven with faint silvery threads. While wearing it, you gain the following benefits:
• You have resistance to poison damage.
• You have a climbing speed equal to your walking speed.
• You can move up, down, and across vertical surfaces and upside down along ceilings, while leaving your hands free.
• You can’t be caught in webs of any sort and can move through webs as if they were difficult terrain.
• You can use an action to cast the web spell (save DC 13). The web created by the spell fills twice its normal area. Once used, this property of the cloak can’t be used again until the next dawn."
     }{
     name-key "Cloak of Displacement"
     ::type :wondrous-item

     ::rarity :rare

     ::attunement [:any]
     ::description "While you wear this cloak, it projects an illusion that makes you appear to be standing in a place near your actual location, causing any creature to have disadvantage on attack rolls against you. If you take damage, the property ceases to function until the start of your next turn. This property is suppressed while you are incapacitated, restrained, or otherwise unable to move."
     }{
     name-key "Cloak of Elvenkind"
     ::type :wondrous-item

     ::rarity :uncommon

     ::attunement [:any]
     ::modifiers [(mod5e/action
                  {:name "Cloak of Elvenkind"
                   :page 158
                   :source :dmg
                   :summary "Pull hood up and gain advantage on Stealth and others have disadvantage on Perception checks to see you"})]
     ::description "While you wear this cloak with its hood up, Wisdom (Perception) checks made to see you have disadvantage, and you have advantage on Dexterity (Stealth) checks made to hide, as the cloak’s color
shifts to camouflage you. Pulling the hood up or down requires an action."
     }
    {
     name-key "Cloak of Invisibility"
     ::type :wondrous-item

     ::rarity :legendary

     ::attunement [:any]
     ::modifiers [(mod5e/action
                  {:name "Cloak of Invisibility"
                   :page 158
                   :source :dmg
                   :duration units5e/hours-2
                   :frequency units5e/days-1
                   :summary "Pull hood up and become invisible"})]
     ::description "While you wear this cloak with its hood up, Wisdom (Perception) checks made to see you have disadvantage, and you have advantage on Dexterity (Stealth) checks made to hide, as the cloak’s color
shifts to camouflage you. Pulling the hood up or down requires an action."}
    {
     name-key "Cloak of Protection"
     ::type :wondrous-item

     ::rarity :uncommon
     ::magical-ac-bonus 1
     ::modifiers (map #(mod5e/saving-throw-bonus % 1) char5e/ability-keys)
     ::attunement [:any]
     ::description "You gain a +1 bonus to AC and saving throws while you wear this cloak."
     }{
     name-key "Cloak of the Bat"
     ::type :wondrous-item

     ::rarity :rare

     ::attunement [:any]
     ::modifiers [(mod5e/action
                  {:name "Cloak of the Bat"
                   :page 159
                   :source :dmg
                   :frequency units5e/days-1
                   :summary "'polymorph' into a bat"})]
     ::description "While wearing this cloak, you have advantage on Dexterity (Stealth) checks. In an area of dim light or darkness, you can grip the edges of the cloak with both hands and use it to fly at a speed of 40 feet. If you ever fail to grip the cloak’s edges while flying in this way, or if you are no longer in dim light or darkness, you lose this flying speed.
While wearing the cloak in an area of dim light or darkness, you can use your action to cast polymorph
on yourself, transforming into a bat. While you are in the form of the bat, you retain your Intelligence, Wisdom, and Charisma scores. The cloak can’t be used this way again until the next dawn."
     }{
     name-key "Cloak of the Manta Ray"
     ::type :wondrous-item
     ::rarity :uncommon
     ::modifiers [(mod5e/swimming-speed 60)]
     ::description "While wearing this cloak with its hood up, you can breathe underwater, and you have a swimming speed of 60 feet. Pulling the hood up or down requires an action."
     }
    {
     name-key "Crystal Ball"
     ::type :wondrous-item
     ::rarity :very-rare
     ::attunement [:any]
     ::description "This crystal ball is about 6 inches in diameter. While touching it, you can cast the scrying spell (save DC 17) with it."}
    {
     name-key "Crystal Ball of Mind Reading"
     ::type :wondrous-item
     ::rarity :legendary
     ::attunement [:any]
     ::description "While touching it, you can cast the scrying spell (save DC 17) with it. You can use an action to cast the detect thoughts spell (save DC 17) while you are scrying with the crystal ball, targeting creatures you can see within 30 feet of the spell’s sensor. You don’t need to concentrate on this detect thoughts to maintain it during its duration, but it ends if scrying ends."}
    {
     name-key "Crystal Ball of Telepathy"
     ::type :wondrous-item
     ::rarity :legendary
     ::attunement [:any]
     ::description "While touching it, you can cast the scrying spell (save DC 17) with it. While scrying with the crystal ball, you can communicate telepathically with creatures you can see within 30 feet of the spell’s sensor. You can also use an action to cast the suggestion spell (save DC 17) through the sensor on
one of those creatures. You don’t need to concentrate on this suggestion to maintain it during its duration, but it ends if scrying ends. Once used, the suggestion power of the crystal ball can’t be used again until the next dawn."}
    {
     name-key "Crystal Ball of True Seeing"
     ::type :wondrous-item
     ::rarity :legendary
     ::attunement [:any]
     ::description "While touching it, you can cast the scrying spell (save DC 17) with it. While scrying with the crystal ball, you have truesight with a radius of 120 feet centered on the spell’s sensor."}
    {
     name-key "Cube of Force"
     ::type :wondrous-item

     ::rarity :rare

     ::attunement [:any]
     ::description "This cube is about an inch across. Each face has a distinct marking on it that can be pressed. The cube starts with 36 charges, and it regains 1d20 expended charges daily at dawn.
You can use an action to press one of the cube’s faces, expending a number of charges based on the chosen face, as shown in the Cube of Force Faces table. Each face has a different effect. If the cube has insufficient charges remaining, nothing happens. Otherwise, a barrier of invisible force springs into existence, forming a cube 15 feet on a side. The barrier is centered on you, moves with you, and lasts for 1 minute, until you use an action to press the cube’s sixth face, or the cube runs out of charges. You can change the barrier’s effect by pressing a different face of the cube and expending the requisite number of charges, resetting the duration.
If your movement causes the barrier to come into contact with a solid object that can’t pass through the cube, you can’t move any closer to that object as long as the barrier remains.
The cube loses charges when the barrier is targeted by certain spells or comes into contact with certain spell or magic item effects, as shown in the table below."
     }{
     name-key "Cubic Gate"
     ::type :wondrous-item
     ::rarity :legendary
     ::description "This cube is 3 inches across and radiates palpable magical energy. The six sides of the cube are each keyed to a different plane of existence, one of which is the Material Plane. The other sides are linked to planes determined by the GM.
You can use an action to press one side of the cube to cast the gate spell with it, opening a portal to the plane keyed to that side. Alternatively, if you use an action to press one side twice, you can cast the plane shift spell (save DC 17) with the cube and transport the targets to the plane keyed to that side.
The cube has 3 charges. Each use of the cube expends 1 charge. The cube regains 1d3 expended charges daily at dawn."
     }{
     name-key "Dagger of Venom"
     ::type :weapon
     ::item-subtype :dagger
     ::rarity :rare
     ::magical-attack-bonus 1
     ::magical-damage-bonus 1
     ::description "You gain a +1 bonus to attack and damage rolls made with this magic weapon.
You can use an action to cause thick, black poison to coat the blade. The poison remains for 1 minute or until an attack using this weapon hits a creature. That creature must succeed on a DC 15 Constitution saving throw or take 2d10 poison damage and become poisoned for 1 minute. The dagger can’t be used this way again until the next dawn."
     }{
     name-key "Dancing Sword"
     ::type :weapon
     ::item-subtype sword?

     ::rarity :very-rare

     ::attunement [:any]
     ::modifiers [(mod5e/bonus-action
                  {:name "Dancing Sword"
                   :page 161
                   :source :dmg
                   :summary "Cause the sword to fly up to 30 ft. and attack"})]
     ::description "You can use a bonus action to toss this magic sword into the air and speak the command word. When you do so, the sword begins to hover, flies up to 30 feet, and attacks one creature of your choice within 5 feet of it. The sword uses your attack roll and ability score modifier to damage rolls.
While the sword hovers, you can use a bonus action to cause it to fly up to 30 feet to another spot within 30 feet of you. As part of the same bonus action, you can cause the sword to attack one creature within 5 feet of it.
After the hovering sword attacks for the fourth time, it flies up to 30 feet and tries to return to your hand. If you have no hand free, it falls to the ground
at your feet. If the sword has no unobstructed path to you, it moves as close to you as it can and then falls to the ground. It also ceases to hover if you
grasp it or move more than 30 feet away from it."
     }{
     name-key "Decanter of Endless Water"
     ::type :wondrous-item
     ::rarity :uncommon
     ::description "This stoppered flask sloshes when shaken, as if it contains water. The decanter weighs 2 pounds.
You can use an action to remove the stopper and speak one of three command words, whereupon an amount of fresh water or salt water (your choice) pours out of the flask. The water stops pouring out at the start of your next turn. Choose from the following options:
• “Stream” produces 1 gallon of water.
• “Fountain” produces 5 gallons of water.
• “Geyser” produces 30 gallons of water that gushes forth in a geyser 30 feet long and 1 foot wide. As a bonus action while holding the decanter, you can aim the geyser at a creature you can see within 30 feet of you. The target must succeed on a DC 13 Strength saving throw or take 1d4 bludgeoning damage and fall prone. Instead of a creature, you can target an object that isn’t being worn or carried and that weighs no more than 200 pounds. The object is either knocked over or pushed up to 15 feet away from you."
     }{
     name-key "Deck of Illusions"
     ::type :wondrous-item
     ::rarity :uncommon
     ::description "This box contains a set of parchment cards. A full deck has 34 cards. A deck found as treasure is usually missing 1d20 − 1 cards.
The magic of the deck functions only if cards are drawn at random (you can use an altered deck of playing cards to simulate the deck). You can use an action to draw a card at random from the deck and throw it to the ground at a point within 30 feet of you.
An illusion of one or more creatures forms over the thrown card and remains until dispelled. An illusory creature appears real, of the appropriate size, and behaves as if it were a real creature except that it can do no harm. While you are within 120 feet of the illusory creature and can see it, you can use an action to move it magically anywhere within 30 feet of its card. Any physical interaction with the illusory creature reveals it to be an illusion, because objects pass through it. Someone who uses an action to visually inspect the creature identifies it as illusory
with a successful DC 15 Intelligence (Investigation) check. The creature then appears translucent.
The illusion lasts until its card is moved or the illusion is dispelled. When the illusion ends, the image on its card disappears, and that card can’t be used again."
     }{
     name-key "Deck of Many Things"
     ::type :wondrous-item
     ::rarity :legendary
     ::description "Usually found in a box or pouch, this deck contains a number of cards made of ivory or vellum. Most (75 percent) of these decks have only thirteen cards, but the rest have twenty-two.
Before you draw a card, you must declare how many cards you intend to draw and then draw them randomly (you can use an altered deck of playing cards to simulate the deck). Any cards drawn in excess of this number have no effect. Otherwise, as
soon as you draw a card from the deck, its magic takes effect. You must draw each card no more than 1 hour after the previous draw. If you fail to draw the chosen number, the remaining number of cards fly from the deck on their own and take effect all at once.
Once a card is drawn, it fades from existence. Unless the card is the Fool or the Jester, the card reappears in the deck, making it possible to draw the same card twice.
Balance. Your mind suffers a wrenching alteration, causing your alignment to change. Lawful becomes chaotic, good becomes evil, and vice versa. If you are true neutral or unaligned, this card has no effect on you.
Comet. If you single-handedly defeat the next hostile monster or group of monsters you encounter, you gain experience points enough to gain one level. Otherwise, this card has no effect.
Donjon. You disappear and become entombed in a state of suspended animation in an extradimensional sphere. Everything you were wearing and carrying stays behind in the space you occupied when you disappeared. You remain imprisoned until you are found and removed from the sphere. You can’t be located by any divination magic, but a wish spell can reveal the location of your prison. You draw no more cards.
Euryale. The card’s medusa-like visage curses you. You take a −2 penalty on saving throws while cursed in this way. Only a god or the magic of The Fates card can end this curse.
The Fates. Reality’s fabric unravels and spins anew, allowing you to avoid or erase one event as if it never happened. You can use the card’s magic as soon as you draw the card or at any other time before you die.
Flames. A powerful devil becomes your enemy. The devil seeks your ruin and plagues your life, savoring your suffering before attempting to slay you. This enmity lasts until either you or the devil dies.
Fool. You lose 10,000 XP, discard this card, and draw from the deck again, counting both draws as one of your declared draws. If losing that much XP would cause you to lose a level, you instead lose an amount that leaves you with just enough XP to keep your level.
Gem. Twenty-five pieces of jewelry worth 2,000 gp each or fifty gems worth 1,000 gp each appear at your feet.
Idiot. Permanently reduce your Intelligence by 1d4 + 1 (to a minimum score of 1). You can draw one additional card beyond your declared draws.
Jester. You gain 10,000 XP, or you can draw two additional cards beyond your declared draws.
Key. A rare or rarer magic weapon with which you are proficient appears in your hands. The GM chooses the weapon.
Knight. You gain the service of a 4th-level fighter who appears in a space you choose within 30 feet of you. The fighter is of the same race as you and serves you loyally until death, believing the fates have drawn him or her to you. You control this character.
Moon. You are granted the ability to cast the wish
spell 1d3 times.
Rogue. A nonplayer character of the GM’s choice becomes hostile toward you. The identity of your new enemy isn’t known until the NPC or someone else reveals it. Nothing less than a wish spell or divine intervention can end the NPC’s hostility toward you.
Ruin. All forms of wealth that you carry or own, other than magic items, are lost to you. Portable property vanishes. Businesses, buildings, and land you own are lost in a way that alters reality the least. Any documentation that proves you should own something lost to this card also disappears.
Skull. You summon an avatar of death—a ghostly humanoid skeleton clad in a tattered black robe and carrying a spectral scythe. It appears in a space of
the GM’s choice within 10 feet of you and attacks you, warning all others that you must win the battle alone. The avatar fights until you die or it drops to 0 hit points, whereupon it disappears. If anyone tries to help you, the helper summons its own avatar of death. A creature slain by an avatar of death can’t be restored to life
Star. Increase one of your ability scores by 2. The score can exceed 20 but can’t exceed 24.
Sun. You gain 50,000 XP, and a wondrous item (which the GM determines randomly) appears in your hands.
Talons. Every magic item you wear or carry disintegrates. Artifacts in your possession aren’t destroyed but do vanish.
Throne. You gain proficiency in the Persuasion skill, and you double your proficiency bonus on checks made with that skill. In addition, you gain rightful ownership of a small keep somewhere in the
world. However, the keep is currently in the hands of monsters, which you must clear out before you can claim the keep as yours.
Vizier. At any time you choose within one year of drawing this card, you can ask a question in meditation and mentally receive a truthful answer to that question. Besides information, the answer helps you solve a puzzling problem or other dilemma. In other words, the knowledge comes with wisdom on how to apply it.
The Void. This black card spells disaster. Your soul is drawn from your body and contained in an object in a place of the GM’s choice. One or more powerful beings guard the place. While your soul is trapped in this way, your body is incapacitated. A wish spell can’t restore your soul, but the spell reveals the location of the object that holds it. You draw no more cards."
     }{
     name-key "Defender"
     ::type :weapon
     ::item-subtype sword?

     ::rarity :legendary

     ::attunement [:any]
     ::magical-attack-bonus 3
     ::magical-damage-bonus 3
     ::description "You gain a +3 bonus to attack and damage rolls made with this magic weapon.
The first time you attack with the sword on each of your turns, you can transfer some or all of the sword’s bonus to your Armor Class, instead of using the bonus on any attacks that turn. For example, you could reduce the bonus to your attack and damage rolls to +1 and gain a +2 bonus to AC. The adjusted bonuses remain in effect until the start of your next turn, although you must hold the sword to gain a bonus to AC from it."
     }{
     name-key "Demon Armor"
     ::magical-ac-bonus 1
     ::type :armor
     ::item-subtype :plate

     ::rarity :very-rare
     ::attunement [:any]
     ::modifiers [(mod5e/language :abyssal)
                 (mod5e/attack
                  {:name "Demon Armor Gauntlets"
                   :damage-die 8
                   :damage-die-count 1
                   :damage-modifier 1
                   :attack-bonus 1
                   :damage-type :slashing})]
     ::description "While wearing this armor, you gain a +1 bonus to AC, and you can understand and speak Abyssal. In addition, the armor’s clawed gauntlets turn unarmed strikes with your hands into magic weapons that deal slashing damage, with a +1 bonus to attack rolls and damage rolls and a damage die of 1d8.
Curse. Once you don this cursed armor, you can’t doff it unless you are targeted by the remove curse
spell or similar magic. While wearing the armor, you have disadvantage on attack rolls against demons and on saving throws against their spells and special abilities."
     }{
     name-key "Dimensional Shackles"
     ::type :wondrous-item
     ::rarity :rare
     ::description "You can use an action to place these shackles on an incapacitated creature. The shackles adjust to fit a creature of Small to Large size. In addition to serving as mundane manacles, the shackles prevent a creature bound by them from using any method of extradimensional movement, including teleportation or travel to a different plane of existence. They don’t prevent the creature from passing through an interdimensional portal.
You and any creature you designate when you use the shackles can use an action to remove them. Once every 30 days, the bound creature can make a DC 30 Strength (Athletics) check. On a success, the creature breaks free and destroys the shackles."
     }
    (dragon-scale-mail "Black" :acid)
    (dragon-scale-mail "Blue" :lightning)
    (dragon-scale-mail "Brass" :fire)
    (dragon-scale-mail "Bronze" :lightning)
    (dragon-scale-mail "Copper" :acid)
    (dragon-scale-mail "Gold" :fire)
    (dragon-scale-mail "Green" :poison)
    (dragon-scale-mail "Red" :fire)
    (dragon-scale-mail "Silver" :cold)
    (dragon-scale-mail "White" :cold)
    {
     name-key "Dragon Slayer"
     ::type :weapon
     ::item-subtype sword?
     ::rarity :rare
     ::magical-attack-bonus 1
     ::magical-damage-bonus 1
     ::description "You gain a +1 bonus to attack and damage rolls made with this magic weapon.
When you hit a dragon with this weapon, the dragon takes an extra 3d6 damage of the weapon’s type. For the purpose of this weapon, “dragon”
refers to any creature with the dragon type, including dragon turtles and wyverns."
     }
    {
     name-key "Driftglobe"
     ::type :wondrous-item
     ::rarity :uncommon
     ::modifiers [(mod5e/trait-cfg
                  {:name "Driftglobe Emanation (light)"
                   :page 166
                   :source :dmg
                   :summary "Cause driftglobe to emanate light as from the light spell"})
                 (mod5e/trait-cfg
                  {:name "Driftglobe Emanation (daylight)"
                   :page 166
                   :source :dmg
                   :frequency units5e/days-1
                   :summary "Cause driftglobe to emanate light as from the daylight spell"})
                 (mod5e/action
                  {:name "Driftglobe Hover"
                   :page 166
                   :source :dmg
                   :summary "Cause driftglobe to hover"})]
     ::summary "Emanates light as with daylight or light spell and can hover"}
    {
     name-key "Dust of Disappearance"
     ::type :wondrous-item
     ::rarity :uncommon
     ::description "Found in a small packet, this powder resembles very fine sand. There is enough of it for one use. When you use an action to throw the dust into the air, you and each creature and object within 10 feet of you become invisible for 2d4 minutes. The duration is the same for all subjects, and the dust is consumed
when its magic takes effect. If a creature affected by the dust attacks or casts a spell, the invisibility ends for that creature."
     }{
     name-key "Dust of Dryness"
     ::type :wondrous-item
     ::rarity :uncommon
     ::description "This small packet contains 1d6 + 4 pinches of dust. You can use an action to sprinkle a pinch of it over water. The dust turns a cube of water 15 feet on a side into one marble-sized pellet, which floats or rests near where the dust was sprinkled. The pellet’s weight is negligible.
Someone can use an action to smash the pellet against a hard surface, causing the pellet to shatter and release the water the dust absorbed. Doing so ends that pellet’s magic.
An elemental composed mostly of water that is exposed to a pinch of the dust must make a DC 13 Constitution saving throw, taking 10d6 necrotic damage on a failed save, or half as much damage on a successful one."
     }{
     name-key "Dust of Sneezing and Choking"
     ::type :wondrous-item
     ::rarity :uncommon
     ::description "Found in a small container, this powder resembles very fine sand. It appears to be dust of disappearance, and an identify spell reveals it to be such. There is enough of it for one use.
When you use an action to throw a handful of the dust into the air, you and each creature that needs to breathe within 30 feet of you must succeed on a DC 15 Constitution saving throw or become unable to breathe, while sneezing uncontrollably. A creature affected in this way is incapacitated and suffocating. As long as it is conscious, a creature can repeat the saving throw at the end of each of its turns, ending the effect on it on a success. The lesser restoration
spell can also end the effect on a creature.}{"
     }{
     name-key "Dwarven Plate"
     ::type :armor
     ::item-subtype :plate
     ::rarity :very-rare
     ::magical-ac-bonus 2
     ::modifiers [(mod5e/reaction
                  {:name "Dwarven Plate"
                   :page 167
                   :source :dmg
                   :summary "if an effect moves you against your will along the ground, you can use your reaction to reduce the distance you are moved by up to 10 feet"})]
     ::description "While wearing this armor, you gain a +2 bonus to AC. In addition, if an effect moves you against your will along the ground, you can use your reaction to reduce the distance you are moved by up to 10 feet."
     }{
     name-key "Dwarven Thrower"
     ::type :weapon
     ::item-subtype :warhammer

     ::rarity :very-rare

     ::attunement [:dwarf]
     ::magical-attack-bonus 3
     ::magical-damage-bonus 3
     ::description "You gain a +3 bonus to attack and damage rolls made with this magic weapon. It has the thrown property with a normal range of 20 feet and a long range of 60 feet. When you hit with a ranged attack using this weapon, it deals an extra 1d8 damage or, if the target is a giant, 2d8 damage. Immediately after the attack, the weapon flies back to your hand."
     }{
     name-key "Efficient Quiver"
     ::type :wondrous-item
     ::rarity :uncommon
     ::description "Each of the quiver’s three compartments connects to an extradimensional space that allows the quiver to hold numerous items while never weighing more than 2 pounds. The shortest compartment can hold up to sixty arrows, bolts, or similar objects. The midsize compartment holds up to eighteen javelins or similar objects. The longest compartment holds up to six long objects, such as bows, quarterstaffs, or spears.
You can draw any item the quiver contains as if doing so from a regular quiver or scabbard."
     }{
     name-key "Efreeti Bottle"
     ::type :wondrous-item
     ::rarity :very-rare
     ::description "This painted brass bottle weighs 1 pound. When you use an action to remove the stopper, a cloud of thick smoke flows out of the bottle. At the end of your turn, the smoke disappears with a flash of harmless fire, and an efreeti appears in an unoccupied space within 30 feet of you.
The first time the bottle is opened, the GM rolls to determine what happens."
     }{
     name-key "Elemental Gem"
     ::type :wondrous-item
     ::rarity :uncommon
     ::description "This gem contains a mote of elemental energy. When you use an action to break the gem, an elemental is summoned as if you had cast the conjure elemental
spell, and the gem’s magic is lost. The type of gem determines the elemental summoned by the spell."
     }{
     name-key "Elven Chain"
     ::type :armor
     ::item-subtype :chain-shirt
     ::rarity :rare
     ::magical-ac-bonus 1
     ::modifiers [(mod5e/armor-proficiency :elven-chain)]
     ::description "You gain a +1 bonus to AC while you wear this armor. You are considered proficient with this armor even if you lack proficiency with medium armor."
     }{
     name-key "Eversmoking Bottle"
     ::type :wondrous-item
     ::rarity :uncommon
     ::description "Smoke leaks from the lead-stoppered mouth of this brass bottle, which weighs 1 pound. When you use an action to remove the stopper, a cloud of thick smoke pours out in a 60-foot radius from the bottle. The cloud’s area is heavily obscured. Each minute the bottle remains open and within the cloud, the radius increases by 10 feet until it reaches its maximum radius of 120 feet.
The cloud persists as long as the bottle is open. Closing the bottle requires you to speak its command word as an action. Once the bottle is closed, the cloud disperses after 10 minutes. A moderate wind (11 to 20 miles per hour) can also disperse the smoke after 1 minute, and a strong wind (21 or more miles per hour) can do so after 1 round."
     }{
     name-key "Eyes of Charming"
     ::type :wondrous-item

     ::rarity :uncommon

     ::attunement [:any]
     ::modifiers [(mod5e/action
                  {:name "Eyes of Charming"
                   :page 168
                   :source :dmg
                   :frequency units5e/days-1
                   :range units5e/ft-30
                   :summary "cast 'charm person'"})]
     ::description "These crystal lenses fit over the eyes. They have 3 charges. While wearing them, you can expend 1 charge as an action to cast the charm person spell
(save DC 13) on a humanoid within 30 feet of you, provided that you and the target can see each other. The lenses regain all expended charges daily at dawn."
     }{
     name-key "Eyes of Minute Seeing"
     ::type :wondrous-item
     ::rarity :uncommon
     ::description "These crystal lenses fit over the eyes. While wearing them, you can see much better than normal out to a range of 1 foot. You have advantage on Intelligence (Investigation) checks that rely on sight while searching an area or studying an object within that range."
     }{
     name-key "Eyes of the Eagle"
     ::type :wondrous-item

     ::rarity :uncommon

     ::attunement [:any]
     ::description "These crystal lenses fit over the eyes. While wearing them, you have advantage on Wisdom (Perception) checks that rely on sight. In conditions of clear visibility, you can make out details of even extremely distant creatures and objects as small as 2 feet across."
     }{
     name-key "Feather Token"
     ::type :wondrous-item
     ::rarity :rare
     ::description "This tiny object looks like a feather. Different types of feather tokens exist, each with a different singleuse
effect. The GM chooses the kind of token or determines it randomly.
Anchor. You can use an action to touch the token to a boat or ship. For the next 24 hours, the vessel can’t be moved by any means. Touching the token to the vessel again ends the effect. When the effect ends, the token disappears.
Bird. You can use an action to toss the token 5 feet into the air. The token disappears and an enormous, multicolored bird takes its place. The bird has the statistics of a roc, but it obeys your simple commands and can’t attack. It can carry up to 500 pounds while flying at its maximum speed (16 miles an hour for a maximum of 144 miles per day, with a one-hour rest for every 3 hours of flying), or 1,000 pounds at half that speed. The bird disappears after flying its maximum distance for a day or if it drops to 0 hit points. You can dismiss the bird as an action.
Fan. If you are on a boat or ship, you can use an action to toss the token up to 10 feet in the air. The
token disappears, and a giant flapping fan takes its place. The fan floats and creates a wind strong enough to fill the sails of one ship, increasing its speed by 5 miles per hour for 8 hours. You can dismiss the fan as an action.
Swan Boat. You can use an action to touch the token to a body of water at least 60 feet in diameter. The token disappears, and a 50-foot-long, 20-footwide
boat shaped like a swan takes its place. The boat is self-propelled and moves across water at a speed of 6 miles per hour. You can use an action while on the boat to command it to move or to turn up to 90 degrees. The boat can carry up to thirty-two Medium or smaller creatures. A Large creature counts as four Medium creatures, while a Huge creature counts as nine. The boat remains for 24 hours and then disappears. You can dismiss the boat as an action.
Tree. You must be outdoors to use this token. You can use an action to touch it to an unoccupied space on the ground. The token disappears, and in its place a nonmagical oak tree springs into existence. The tree is 60 feet tall and has a 5-foot-diameter trunk, and its branches at the top spread out in a 20-foot radius.
Whip. You can use an action to throw the token to a point within 10 feet of you. The token disappears, and a floating whip takes its place. You can then use a bonus action to make a melee spell attack against a creature within 10 feet of the whip, with an attack bonus of +9. On a hit, the target takes 1d6 + 5 force damage.
As a bonus action on your turn, you can direct the whip to fly up to 20 feet and repeat the attack against a creature within 10 feet of it. The whip disappears after 1 hour, when you use an action to dismiss it, or when you are incapacitated or die."
       }
    (figurine-of-wondrous-power
     :rare
     "Bronze Griffon"
     "This bronze statuette is of a griffon rampant. It can become a griffon for up to 6 hours. Once it has been used, it can’t be used again until 5 days have passed.")
    (figurine-of-wondrous-power
     :rare
     "Ebony Fly"
     "This ebony statuette is carved in the likeness of a horsefly. It can become a giant fly for up to 12 hours and can be ridden as a mount. Once it has been used, it can’t be used again until 2 days have passed.")
    (figurine-of-wondrous-power
     :rare
     "Golden Lions"
     "These gold statuettes of lions are always created in pairs. You can use one figurine or both simultaneously. Each can become a lion for up to 1 hour. Once a lion has been used, it can’t be used again until 7 days have passed.")
    (figurine-of-wondrous-power
     :rare
     "Ivory Goats"
     "These ivory statuettes of goats are always created in sets of three. Each goat looks unique and functions differently from the others. Their properties are as follows:
• The goat of traveling can become a Large goat with the same statistics as a riding horse. It has 24 charges, and each hour or portion thereof it spends in beast form costs 1 charge. While it has charges, you can use it as often as you wish. When it runs out of charges, it reverts to a figurine and can’t be used again until 7 days have passed, when it regains all its charges.
• The goat of travail becomes a giant goat for up to 3 hours. Once it has been used, it can’t be used again until 30 days have passed.
• The goat of terror becomes a giant goat for up to 3 hours. The goat can’t attack, but you can remove its horns and use them as weapons. One horn becomes a +1 lance, and the other becomes a +2 longsword. Removing a horn requires an action, and the weapons disappear and the horns return when the goat reverts to figurine form. In addition, the goat radiates a 30-foot-radius aura of terror while you are riding it. Any creature hostile to you that starts its turn in the aura must succeed on a DC 15 Wisdom saving throw or be frightened of the goat for 1 minute, or until the goat reverts to figurine form. The frightened creature can repeat the saving throw at the end of each of its turns, ending the effect on itself on a success. Once it successfully saves against the effect, a creature is immune to the goat’s aura for the next 24 hours. Once the figurine has been used, it can’t be used again until 15 days have passed.")
    (figurine-of-wondrous-power
     :rare
     "Marble Elephant"
     "This marble statuette is about 4 inches high and long. It can become an elephant for up to 24 hours. Once it has been used, it can’t be used again until 7 days have passed.")
    (figurine-of-wondrous-power
     :very-rare
     "Obsidian Steed"
     "This polished obsidian horse can become a nightmare for up to 24 hours. The nightmare fights only to defend itself. Once it has been used, it can’t be used again until 5 days have passed.
If you have a good alignment, the figurine has a 10 percent chance each time you use it to ignore your orders, including a command to revert to figurine form. If you mount the nightmare while it is ignoring your orders, you and the nightmare are instantly transported to a random location on the plane of Hades, where the nightmare reverts to figurine form.")
    (figurine-of-wondrous-power
     :very-rare
     "Onyx Dog"
     "This onyx statuette of a dog can become a mastiff for up to 6 hours. The mastiff has an Intelligence of 8 and can speak Common. It also has darkvision out to a range of 60 feet and can see invisible creatures and objects within that range. Once it has been used, it can’t be used again until 7 days have passed.")
    (figurine-of-wondrous-power
     :rare
     "Serpentine Owl"
     "This serpentine statuette of an owl can become a giant owl for up to 8 hours. Once it has been used, it can’t be used again until 2 days have passed. The owl can telepathically communicate with you at any range if you and it are on the same plane of existence.")
    (figurine-of-wondrous-power
     :uncommon
     "Silver Raven"
     "This silver statuette of a raven can become a raven for up to 12 hours. Once
it has been used, it can’t be used again until 2 days have passed. While in raven form, the figurine allows you to cast the animal messenger spell on it at will.")
    {
     name-key "Flame Tongue"
     ::type :weapon
     ::item-subtype sword?

     ::rarity :rare

     ::attunement [:any]
     ::description "You can use a bonus action to speak this magic sword’s command word, causing flames to erupt from the blade. These flames shed bright light in a 40-foot radius and dim light for an additional 40 feet. While the sword is ablaze, it deals an extra 2d6 fire damage to any target it hits. The flames last until you use a bonus action to speak the command word again or until you drop or sheathe the sword."
     }{
     name-key "Folding Boat"
     ::type :wondrous-item
     ::rarity :rare
     ::description "This object appears as a wooden box that measures 12 inches long, 6 inches wide, and 6 inches deep. It weighs 4 pounds and floats. It can be opened to store items inside. This item also has three command words, each requiring you to use an action to speak it.
One command word causes the box to unfold into a boat 10 feet long, 4 feet wide, and 2 feet deep. The boat has one pair of oars, an anchor, a mast, and a lateen sail. The boat can hold up to four Medium creatures comfortably.
The second command word causes the box to unfold into a ship 24 feet long, 8 feet wide, and 6 feet deep. The ship has a deck, rowing seats, five sets of oars, a steering oar, an anchor, a deck cabin, and a mast with a square sail. The ship can hold fifteen Medium creatures comfortably.
When the box becomes a vessel, its weight becomes that of a normal vessel its size, and anything that was stored in the box remains in the boat.
The third command word causes the folding boat
to fold back into a box, provided that no creatures are aboard. Any objects in the vessel that can’t fit inside the box remain outside the box as it folds. Any objects in the vessel that can fit inside the box do so."
     }{
     name-key "Frost Brand"
     ::type :weapon
     ::item-subtype sword?

     ::rarity :very-rare

     ::attunement [:any]
     ::description "When you hit with an attack using this magic sword, the target takes an extra 1d6 cold damage. In addition, while you hold the sword, you have resistance to fire damage.
In freezing temperatures, the blade sheds bright light in a 10-foot radius and dim light for an additional 10 feet.
When you draw this weapon, you can extinguish all nonmagical flames within 30 feet of you. This property can be used no more than once per hour."
     }{
     name-key "Gauntlets of Ogre Power"
     ::type :wondrous-item

     ::rarity :uncommon

     ::attunement [:any]
     ::modifiers [(mod5e/ability-override ::char5e/str 19)
                 (mod/modifier ?giants-bane-gauntlet true)]
     ::description "Your Strength score is 19 while you wear these gauntlets. They have no effect on you if your Strength is already 19 or higher."
     }{
     name-key "Gem of Brightness"
     ::type :wondrous-item
     ::rarity :uncommon
     ::description "This prism has 50 charges. While you are holding it, you can use an action to speak one of three command words to cause one of the following effects:
• The first command word causes the gem to shed bright light in a 30-foot radius and dim light for an additional 30 feet. This effect doesn’t expend a charge. It lasts until you use a bonus action to repeat the command word or until you use another function of the gem.
• The second command word expends 1 charge and causes the gem to fire a brilliant beam of light at one creature you can see within 60 feet of you. The creature must succeed on a DC 15 Constitution saving throw or become blinded for 1 minute. The creature can repeat the saving throw at the end of each of its turns, ending the effect on itself on a success.
• The third command word expends 5 charges and causes the gem to flare with blinding light in a 30-
foot cone originating from it. Each creature in the cone must make a saving throw as if struck by the beam created with the second command word.
When all of the gem’s charges are expended, the gem becomes a nonmagical jewel worth 50 gp."
     }{
     name-key "Gem of Seeing"
     ::type :wondrous-item

     ::rarity :rare

     ::attunement [:any]
     ::description "This gem has 3 charges. As an action, you can speak the gem’s command word and expend 1 charge. For the next 10 minutes, you have truesight out to 120 feet when you peer through the gem.
The gem regains 1d3 expended charges daily at dawn."
     }{
     name-key "Giant Slayer"
     ::type :weapon
     ::item-subtype (fn [w] (or (axe? w) (sword? w)))
     ::rarity :rare
     ::magical-attack-bonus 1
     ::magical-damage-bonus 1
     ::description "You gain a +1 bonus to attack and damage rolls made with this magic weapon.
When you hit a giant with it, the giant takes an extra 2d6 damage of the weapon’s type and must succeed on a DC 15 Strength saving throw or fall prone. For the purpose of this weapon, “giant” refers to any creature with the giant type, including ettins and trolls."
     }{
     name-key "Glamoured Studded Leather"
     ::type :armor
     ::item-subtype :studded
     ::rarity :rare
     ::magical-ac-bonus 1
     ::modifiers [(mod5e/bonus-action
                  {:name "Glamoured Studded Leather"
                   :page 172
                   :source :dmg
                   :summary "change the armor to assume the appearance of normal clothing or some other armor"})]
     ::description "While wearing this armor, you gain a +1 bonus to AC. You can also use a bonus action to speak the armor’s command word and cause the armor to assume the appearance of a normal set of clothing or some other kind of armor. You decide what it looks like, including color, style, and accessories, but the armor retains its normal bulk and weight. The illusory appearance lasts until you use this property again or remove the armor."
     }{
     name-key "Gloves of Missile Snaring"
     ::type :wondrous-item

     ::rarity :uncommon

     ::attunement [:any]
     ::modifiers [(mod5e/reaction
                  {:name "Gloves of Missile Snaring"
                   :page 172
                   :source :dmg
                   :summary (str "when hit by a ranged weapon attack, reduce the damage by 1d10 + DEX mod")})]
     ::description "These gloves seem to almost meld into your hands when you don them. When a ranged weapon attack hits you while you’re wearing them, you can use your reaction to reduce the damage by 1d10 + your Dexterity modifier, provided that you have a free hand. If you reduce the damage to 0, you can catch the missile if it is small enough for you to hold in that hand."
     }{
     name-key "Gloves of Swimming and Climbing"
     ::type :wondrous-item

     ::rarity :uncommon

     ::attunement [:any]
     ::description "While wearing these gloves, climbing and swimming don’t cost you extra movement, and you gain a +5 bonus to Strength (Athletics) checks made to climb or swim."
     } {
     name-key "Gloves of Thievery"
     ::type :wondrous-item
     ::rarity :uncommon
     ::modifiers [(mod5e/skill-bonus :sleight-of-hand 5)]
     :page 172
     ::description "+5 to Sleight of Hand and lock pick checks"}
    {
     name-key "Goggles of Night"
     ::type :wondrous-item
     ::rarity :uncommon
     ::modifiers [(mod5e/darkvision-bonus 60)]
     ::description "While wearing these dark lenses, you have darkvision out to a range of 60 feet. If you already have darkvision, wearing the goggles increases its range by 60 feet."
     }{
     name-key "Hammer of Thunderbolts"
     ::type :weapon
     ::item-subtype :maul
     ::rarity :legendary
     ::magical-attack-bonus 1
     ::magical-damage-bonus 1
     ::attunement [:any]
     ::modifiers [(mod/modifier ?giants-bane-hammer true)]
     ::description "You gain a +1 bonus to attack and damage rolls made with this magic weapon.
Giant’s Bane. You must be wearing a belt of giant strength (any variety) and gauntlets of ogre power to attune to this weapon. The attunement ends if you take off either of those items. While you are attuned to this weapon and holding it, your Strength score increases by 4 and can exceed 20, but not 30. When you roll a 20 on an attack roll made with this weapon against a giant, the giant must succeed on a DC 17 Constitution saving throw or die.
The hammer also has 5 charges. While attuned to it, you can expend 1 charge and make a ranged weapon attack with the hammer, hurling it as if it had the thrown property with a normal range of 20 feet and a long range of 60 feet. If the attack hits, the hammer unleashes a thunderclap audible out to 300 feet. The target and every creature within 30 feet of it must succeed on a DC 17 Constitution saving throw or be stunned until the end of your next turn. The hammer regains 1d4 + 1 expended charges daily at dawn."
     }{
     name-key "Handy Haversack"
     ::type :wondrous-item
     ::rarity :rare
     ::description "This backpack has a central pouch and two side pouches, each of which is an extradimensional space. Each side pouch can hold up to 20 pounds of material, not exceeding a volume of 2 cubic feet. The large central pouch can hold up to 8 cubic feet or 80 pounds of material. The backpack always weighs 5 pounds, regardless of its contents.
Placing an object in the haversack follows the normal rules for interacting with objects. Retrieving an item from the haversack requires you to use an action. When you reach into the haversack for a specific item, the item is always magically on top.
The haversack has a few limitations. If it is overloaded, or if a sharp object pierces it or tears it, the haversack ruptures and is destroyed. If the haversack is destroyed, its contents are lost forever, although an artifact always turns up again somewhere. If the haversack is turned inside out, its contents spill forth, unharmed, and the haversack must be put right before it can be used again. If a breathing creature is placed within the haversack, the creature can survive for up to 10 minutes, after which time it begins to suffocate.
Placing the haversack inside an extradimensional space created by a bag of holding, portable hole, or similar item instantly destroys both items and opens a gate to the Astral Plane. The gate originates where the one item was placed inside the other. Any creature within 10 feet of the gate is sucked through it and deposited in a random location on the Astral Plane. The gate then closes. The gate is one-way only and can’t be reopened."
     }{
     name-key "Hat of Disguise"
     ::type :wondrous-item

     ::rarity :uncommon

     ::attunement [:any]
     ::description "While wearing this hat, you can use an action to cast the disguise self spell from it at will. The spell ends if the hat is removed."
     }{
     name-key "Headband of Intellect"
     ::type :wondrous-item

     ::rarity :uncommon

     ::attunement [:any]
     ::modifiers [(mod5e/ability-override ::char5e/int 19)]
     ::description "Your Intelligence score is 19 while you wear this headband. It has no effect on you if your Intelligence is already 19 or higher."
     }{
     name-key "Helm of Brilliance"
     ::type :wondrous-item

     ::rarity :very-rare

     ::attunement [:any]
     ::description "This dazzling helm is set with 1d10 diamonds, 2d10 rubies, 3d10 fire opals, and 4d10 opals. Any gem pried from the helm crumbles to dust. When all the gems are removed or destroyed, the helm loses its magic.
You gain the following benefits while wearing it:
• You can use an action to cast one of the following spells (save DC 18), using one of the helm’s gems of the specified type as a component: daylight
(opal), fireball (fire opal), prismatic spray
(diamond), or wall of fire (ruby). The gem is destroyed when the spell is cast and disappears from the helm.
• As long as it has at least one diamond, the helm emits dim light in a 30-foot radius when at least one undead is within that area. Any undead that starts its turn in that area takes 1d6 radiant damage.
• As long as the helm has at least one ruby, you have resistance to fire damage.
• As long as the helm has at least one fire opal, you can use an action and speak a command word to cause one weapon you are holding to burst into flames. The flames emit bright light in a 10-foot radius and dim light for an additional 10 feet. The flames are harmless to you and the weapon. When you hit with an attack using the blazing weapon,
the target takes an extra 1d6 fire damage. The flames last until you use a bonus action to speak the command word again or until you drop or stow the weapon.
Roll a d20 if you are wearing the helm and take fire damage as a result of failing a saving throw against a spell. On a roll of 1, the helm emits beams of light from its remaining gems. Each creature within 60 feet of the helm other than you must succeed on a DC 17 Dexterity saving throw or be struck by a beam, taking radiant damage equal to the number of gems in the helm. The helm and its gems are then destroyed."
     }{
     name-key "Helm of Comprehending Languages"
     ::type :wondrous-item
     ::rarity :uncommon
     ::description "While wearing this helm, you can use an action to cast the comprehend languages spell from it at will."
     }{
     name-key "Helm of Telepathy"
     ::type :wondrous-item

     ::rarity :uncommon

     ::attunement [:any]
     ::description "While wearing this helm, you can use an action to cast the detect thoughts spell (save DC 13) from it. As long as you maintain concentration on the spell, you can use a bonus action to send a telepathic message to a creature you are focused on. It can reply—using
a bonus action to do so—while your focus on it continues.
While focusing on a creature with detect thoughts, you can use an action to cast the suggestion spell (save DC 13) from the helm on that creature. Once used, the suggestion property can’t be used again until the next dawn."
     }{
     name-key "Helm of Teleportation"
     ::type :wondrous-item

     ::rarity :rare

     ::attunement [:any]
     ::description "This helm has 3 charges. While wearing it, you can use an action and expend 1 charge to cast the teleport spell from it. The helm regains 1d3 expended charges daily at dawn."
     }{
     name-key "Holy Avenger"
     ::type :weapon
     ::item-subtype sword?

     ::rarity :legendary

     ::attunement [:paladin]
     ::magical-attack-bonus 3
     ::magical-damage-bonus 3
     ::description "You gain a +3 bonus to attack and damage rolls made with this magic weapon. When you hit a fiend or an undead with it, that creature takes an extra 2d10 radiant damage.
While you hold the drawn sword, it creates an aura in a 10-foot radius around you. You and all creatures friendly to you in the aura have advantage
on saving throws against spells and other magical effects. If you have 17 or more levels in the paladin class, the radius of the aura increases to 30 feet."
     }{
     name-key "Horn of Blasting"
     ::type :wondrous-item
     ::rarity :rare
     ::description "You can use an action to speak the horn’s command word and then blow the horn, which emits a thunderous blast in a 30-foot cone that is audible 600 feet away. Each creature in the cone must make a DC 15 Constitution saving throw. On a failed save, a creature takes 5d6 thunder damage and is deafened for 1 minute. On a successful save, a creature takes half as much damage and isn’t deafened. Creatures and objects made of glass or crystal have disadvantage on the saving throw and take 10d6 thunder damage instead of 5d6.
Each use of the horn’s magic has a 20 percent chance of causing the horn to explode. The explosion deals 10d6 fire damage to the blower and destroys the horn."
       }
    (horn-of-valhalla "Silver" :rare 2)
    (horn-of-valhalla "Brass" :rare 3 "simple weapons")
    (horn-of-valhalla "Bronze" :rare 4 "medium armor")
    (horn-of-valhalla "Iron" :rare 5 "martial weapons")
    {
     name-key "Horseshoes of a Zephyr"
     ::type :wondrous-item
     ::rarity :very-rare
     ::description "These iron horseshoes come in a set of four. While all four shoes are affixed to the hooves of a horse or similar creature, they allow the creature to move normally while floating 4 inches above the ground. This effect means the creature can cross or stand above nonsolid or unstable surfaces, such as water or lava. The creature leaves no tracks and ignores difficult terrain. In addition, the creature can move at normal speed for up to 12 hours a day without suffering exhaustion from a forced march."
     }{
     name-key "Horseshoes of Speed"
     ::type :wondrous-item
     ::rarity :rare
     ::description "These iron horseshoes come in a set of four. While all four shoes are affixed to the hooves of a horse or similar creature, they increase the creature’s walking speed by 30 feet."
     }{
     name-key "Immovable Rod"
     ::type :rod
     ::rarity :uncommon
     ::description "This flat iron rod has a button on one end. You can use an action to press the button, which causes the rod to become magically fixed in place. Until you or another creature uses an action to push the button again, the rod doesn’t move, even if it is defying gravity. The rod can hold up to 8,000 pounds of weight. More weight causes the rod to deactivate and fall. A creature can use an action to make a DC 30 Strength check, moving the fixed rod up to 10 feet on a success."
     }{
     name-key "Instant Fortress"
     ::type :wondrous-item
     ::rarity :rare
     ::description "You can use an action to place this 1-inch metal cube on the ground and speak its command word. The cube rapidly grows into a fortress that remains until you use an action to speak the command word that dismisses it, which works only if the fortress is empty.
The fortress is a square tower, 20 feet on a side and 30 feet high, with arrow slits on all sides and a battlement atop it. Its interior is divided into two floors, with a ladder running along one wall to connect them. The ladder ends at a trapdoor leading to the roof. When activated, the tower has a small door on the side facing you. The door opens only at your command, which you can speak as a bonus action. It is immune to the knock spell and similar magic, such as that of a chime of opening.
Each creature in the area where the fortress appears must make a DC 15 Dexterity saving throw, taking 10d10 bludgeoning damage on a failed save, or half as much damage on a successful one. In either case, the creature is pushed to an unoccupied space outside but next to the fortress. Objects in the area that aren’t being worn or carried take this damage and are pushed automatically.
The tower is made of adamantine, and its magic prevents it from being tipped over. The roof, the door, and the walls each have 100 hit points, immunity to damage from nonmagical weapons excluding siege weapons, and resistance to all other damage. Only a wish spell can repair the fortress (this use of the spell counts as replicating a spell of 8th level or lower). Each casting of wish causes the roof, the door, or one wall to regain 50 hit points."
     }
    (ioun-stone "Absorption"
                :very-rare
                "While this pale lavender ellipsoid orbits your head, you can use your reaction to cancel a spell of 4th level or lower cast by a creature you can see and targeting only you.
Once the stone has canceled 20 levels of spells, it burns out and turns dull gray, losing its magic. If you are targeted by a spell whose level is higher than the number of spell levels the stone has left, the stone can’t cancel it.")
    (ioun-stone "Agility"
                :very-rare
                "Your Dexterity score increases by 2, to a maximum of 20, while this deep red sphere orbits your head."
                (mod5e/ability ::char5e/dex 2))
    (ioun-stone "Awareness"
                :rare
                "You can’t be surprised while this dark blue rhomboid orbits your head.")
    (ioun-stone "Fortitude"
                :very-rare
                "Your Constitution score increases by 2, to a maximum of 20, while this pink rhomboid orbits your head."
                (mod5e/ability ::char5e/con 2))
    (ioun-stone "Greater Absorption"
                :legendary
                "While this marbled lavender and green ellipsoid orbits your head, you can use your reaction to cancel a spell of 8th level or lower cast by a creature you can see and targeting only you.
Once the stone has canceled 50 levels of spells, it burns out and turns dull gray, losing its magic. If you are targeted by a spell whose level is higher than the number of spell levels the stone has left, the stone can’t cancel it.")
    (ioun-stone "Insight"
                :legendary
                "Your Wisdom score increases by 2, to a maximum of 20, while this incandescent blue sphere orbits your head."
                (mod5e/ability ::char5e/wis 2))
    (ioun-stone "Intellect"
                :very-rare
                "Your Intelligence score increases by 2, to a maximum of 20, while this marbled scarlet and blue sphere orbits your head."
                (mod5e/ability ::char5e/int 2))
    (ioun-stone "Leadership"
                :very-rare
                "Your Charisma score increases by 2, to a maximum of 20, while this marbled pink and green sphere orbits your head."
                (mod5e/ability ::char5e/cha 2))
    (ioun-stone "Mastery"
                :legendary
                "Your proficiency bonus increases by 1 while this pale green prism orbits your head."
                (mod5e/proficiency-bonus-increase 1))
    (ioun-stone "Protection"
                :rare
                "You gain a +1 bonus to AC while this dusty rose prism orbits your head."
                (mod5e/ac-bonus-fn (fn [_ _] 1)))
    (ioun-stone "Regeneration"
                :legendary
                "You regain 15 hit points at the end of each hour this pearly white spindle orbits your head, provided that you have at least 1 hit point.")
    (ioun-stone "Reserve"
                :rare
                "This vibrant purple prism stores spells cast into it, holding them until you use them. The stone can store up to 3 levels worth of spells at a time. When found, it contains 1d4 − 1 levels of stored spells chosen by the GM.
Any creature can cast a spell of 1st through 3rd level into the stone by touching it as the spell is cast. The spell has no effect, other than to be stored in the stone. If the stone can’t hold the spell, the spell is expended without effect. The level of the slot used to cast the spell determines how much space it uses.
While this stone orbits your head, you can cast any spell stored in it. The spell uses the slot level, spell save DC, spell attack bonus, and spellcasting ability of the original caster, but is otherwise treated as if you cast the spell. The spell cast from the stone is no longer stored in it, freeing up space.")
    (ioun-stone "Strength"
                :very-rare
                "Your Strength score increases by 2, to a maximum of 20, while this pale blue rhomboid orbits your head."
                (mod5e/ability ::char5e/str 2))
    (ioun-stone "Sustenance"
                :rare
                "You don’t need to eat or drink while this clear spindle orbits your head.")
    {
     name-key "Iron Bands of Binding"
     ::type :wondrous-item
     ::rarity :rare
     ::description "This rusty iron sphere measures 3 inches in diameter and weighs 1 pound. You can use an action to speak the command word and throw the sphere at a Huge or smaller creature you can see within 60 feet of you. As the sphere moves through the air, it opens into a tangle of metal bands.
Make a ranged attack roll with an attack bonus equal to your Dexterity modifier plus your proficiency bonus. On a hit, the target is restrained until you take a bonus action to speak the command word again to release it. Doing so, or missing with the attack, causes the bands to contract and become a sphere once more.
A creature, including the one restrained, can use an action to make a DC 20 Strength check to break the iron bands. On a success, the item is destroyed, and the restrained creature is freed. If the check fails, any further attempts made by that creature automatically fail until 24 hours have elapsed.
Once the bands are used, they can’t be used again until the next dawn."
     }{
     name-key "Iron Flask"
     ::type :wondrous-item
     ::rarity :legendary
     ::description "This iron bottle has a brass stopper. You can use an action to speak the flask’s command word, targeting a creature that you can see within 60 feet of you. If the target is native to a plane of existence other than the one you’re on, the target must succeed on a DC 17 Wisdom saving throw or be trapped in the flask. If the target has been trapped by the flask before, it has advantage on the saving throw. Once trapped, a creature remains in the flask until released. The flask can hold only one creature at a time. A creature trapped in the flask doesn’t need to breathe, eat, or drink and doesn’t age.
You can use an action to remove the flask’s stopper and release the creature the flask contains. The creature is friendly to you and your companions for 1 hour and obeys your commands for that duration. If you give no commands or give it a command that is likely to result in its death, it defends itself but otherwise takes no actions. At the end of the duration, the creature acts in accordance with its normal disposition and alignment.
An identify spell reveals that a creature is inside the flask, but the only way to determine the type of creature is to open the flask. A newly discovered bottle might already contain a creature chosen by the GM or determined randomly."
     }{
     name-key "Javelin of Lightning"
     ::type :weapon
     ::item-subtype javelin?
     ::rarity :uncommon
     ::description "This javelin is a magic weapon. When you hurl it and speak its command word, it transforms into a bolt of lightning, forming a line 5 feet wide that extends out from you to a target within 120 feet. Each creature in the line excluding you and the target must make a DC 13 Dexterity saving throw, taking 4d6 lightning damage on a failed save, and half as much damage on a successful one. The lightning bolt turns back into a javelin when it reaches the target. Make a ranged weapon attack against the target. On a hit, the target takes damage from the javelin plus 4d6 lightning damage.
The javelin’s property can’t be used again until the next dawn. In the meantime, the javelin can still be used as a magic weapon."
     }{
     name-key "Lantern of Revealing"
     ::type :wondrous-item
     ::rarity :uncommon
     ::description "While lit, this hooded lantern burns for 6 hours on 1 pint of oil, shedding bright light in a 30-foot radius and dim light for an additional 30 feet. Invisible creatures and objects are visible as long as they are in the lantern’s bright light. You can use an action to lower the hood, reducing the light to dim light in a 5-
foot radius."
     }{
     name-key "Luck Blade"
     ::type :weapon
     ::item-subtype sword?

     ::rarity :legendary

     ::attunement [:any]
     ::magical-attack-bonus 1
     ::magical-damage-bonus 1
     ::modifiers [(mod5e/saving-throw-bonuses 1)]
     ::description "You gain a +1 bonus to attack and damage rolls made with this magic weapon. While the sword is on your person, you also gain a +1 bonus to saving throws.
Luck. If the sword is on your person, you can call on its luck (no action required) to reroll one attack roll, ability check, or saving throw you dislike. You must use the second roll. This property can’t be used again until the next dawn.
Wish. The sword has 1d4 – 1 charges. While holding it, you can use an action to expend 1 charge and cast the wish spell from it. This property can’t be used again until the next dawn. The sword loses this property if it has no charges."
     }{
     name-key "Mace of Disruption"
     ::type :weapon
     ::item-subtype :mace

     ::rarity :rare

     ::attunement [:any]
     ::description "When you hit a fiend or an undead with this magic weapon, that creature takes an extra 2d6 radiant damage. If the target has 25 hit points or fewer after taking this damage, it must succeed on a DC 15 Wisdom saving throw or be destroyed. On a successful save, the creature becomes frightened of you until the end of your next turn.
While you hold this weapon, it sheds bright light in a 20-foot radius and dim light for an additional 20 feet."
     }{
     name-key "Mace of Smiting"
     ::type :weapon
     ::item-subtype :mace
     ::rarity :rare
     ::magical-attack-bonus 1
     ::magical-damage-bonus 1
     ::description "You gain a +1 bonus to attack and damage rolls made with this magic weapon. The bonus increases to +3 when you use the mace to attack a construct.
When you roll a 20 on an attack roll made with this weapon, the target takes an extra 2d6 bludgeoning damage, or 4d6 bludgeoning damage if it’s a construct. If a construct has 25 hit points or fewer after taking this damage, it is destroyed."
     }{
     name-key "Mace of Terror"
     ::type :weapon
     ::item-subtype :mace

     ::rarity :rare

     ::attunement [:any]
     ::description "This magic weapon has 3 charges. While holding it, you can use an action and expend 1 charge to release a wave of terror. Each creature of your choice in a 30-foot radius extending from you must succeed on a DC 15 Wisdom saving throw or become frightened of you for 1 minute. While it is frightened in this way, a creature must spend its turns trying to move as far
away from you as it can, and it can’t willingly move to a space within 30 feet of you. It also can’t take reactions. For its action, it can use only the Dash action or try to escape from an effect that prevents it from moving. If it has nowhere it can move, the creature can use the Dodge action. At the end of each of its turns, a creature can repeat the saving throw, ending the effect on itself on a success.
The mace regains 1d3 expended charges daily at dawn."
     }{
     name-key "Mantle of Spell Resistance"
     ::type :wondrous-item

     ::rarity :rare

     ::attunement [:any]
     ::modifiers [(mod5e/saving-throw-advantage ["spells"])]
     ::description "You have advantage on saving throws against spells while you wear this cloak."
     }{
     name-key "Manual of Bodily Health"
     ::type :wondrous-item
     ::rarity :very-rare
     ::description "This book contains health and diet tips, and its words are charged with magic. If you spend 48 hours over a period of 6 days or fewer studying the book’s contents and practicing its guidelines, your Constitution score increases by 2, as does your maximum for that score. The manual then loses its magic, but regains it in a century."
     }{
     name-key "Manual of Gainful Exercise"
     ::type :wondrous-item
     ::rarity :very-rare
     ::description "This book describes fitness exercises, and its words are charged with magic. If you spend 48 hours over a period of 6 days or fewer studying the book’s contents and practicing its guidelines, your Strength score increases by 2, as does your maximum for that score. The manual then loses its magic, but regains it in a century."
     }{
     name-key "Manual of Golems"
     ::type :wondrous-item
     ::rarity :very-rare
     ::description "This tome contains information and incantations necessary to make a particular type of golem. The GM chooses the type or determines it randomly. To decipher and use the manual, you must be a spellcaster with at least two 5th-level spell slots. A creature that can’t use a manual of golems and attempts to read it takes 6d6 psychic damage.
o create a golem, you must spend the time shown on the table, working without interruption with the manual at hand and resting no more than 8 hours per day. You must also pay the specified cost to purchase supplies.
Once you finish creating the golem, the book is consumed in eldritch flames. The golem becomes animate when the ashes of the manual are sprinkled on it. It is under your control, and it understands and obeys your spoken commands."
     }{
     name-key "Manual of Quickness of Action"
     ::type :wondrous-item
     ::rarity :very-rare
     ::description "This book contains coordination and balance exercises, and its words are charged with magic. If you spend 48 hours over a period of 6 days or fewer studying the book’s contents and practicing its guidelines, your Dexterity score increases by 2, as does your maximum for that score. The manual then loses its magic, but regains it in a century."
     }{
     name-key "Marvelous Pigments"
     ::type :wondrous-item
     ::rarity :very-rare
     ::description "Typically found in 1d4 pots inside a fine wooden box with a brush (weighing 1 pound in total), these pigments allow you to create three-dimensional objects by painting them in two dimensions. The paint flows from the brush to form the desired object as you concentrate on its image.
Each pot of paint is sufficient to cover 1,000 square feet of a surface, which lets you create inanimate objects or terrain features—such as a door, a pit, flowers, trees, cells, rooms, or weapons—
that are up to 10,000 cubic feet. It takes 10 minutes to cover 100 square feet.
When you complete the painting, the object or terrain feature depicted becomes a real, nonmagical object. Thus, painting a door on a wall creates an actual door that can be opened to whatever is beyond. Painting a pit on a floor creates a real pit, and its depth counts against the total area of objects you create.
Nothing created by the pigments can have a value greater than 25 gp. If you paint an object of greater value (such as a diamond or a pile of gold), the object looks authentic, but close inspection reveals it is made from paste, bone, or some other worthless material.
If you paint a form of energy such as fire or lightning, the energy appears but dissipates as soon as you complete the painting, doing no harm to anything."
     }{
     name-key "Medallion of Thoughts"
     ::type :wondrous-item

     ::rarity :uncommon

     ::attunement [:any]
     ::description "The medallion has 3 charges. While wearing it, you can use an action and expend 1 charge to cast the detect thoughts spell (save DC 13) from it. The medallion regains 1d3 expended charges daily at dawn."
     }{
     name-key "Mirror of Life Trapping"
     ::type :wondrous-item
     ::rarity :very-rare
     ::description "When this 4-foot-tall mirror is viewed indirectly, its surface shows faint images of creatures. The mirror weighs 50 pounds, and it has AC 11, 10 hit points, and vulnerability to bludgeoning damage. It shatters and is destroyed when reduced to 0 hit points.
If the mirror is hanging on a vertical surface and you are within 5 feet of it, you can use an action to speak its command word and activate it. It remains activated until you use an action to speak the command word again.
Any creature other than you that sees its reflection in the activated mirror while within 30 feet of it must succeed on a DC 15 Charisma saving throw or be trapped, along with anything it is wearing or carrying, in one of the mirror’s twelve extradimensional cells. This saving throw is made with advantage if the creature knows the mirror’s nature, and constructs succeed on the saving throw automatically.
An extradimensional cell is an infinite expanse filled with thick fog that reduces visibility to 10 feet. Creatures trapped in the mirror’s cells don’t age, and they don’t need to eat, drink, or sleep. A creature trapped within a cell can escape using magic that permits planar travel. Otherwise, the creature is confined to the cell until freed.
If the mirror traps a creature but its twelve extradimensional cells are already occupied, the mirror frees one trapped creature at random to accommodate the new prisoner. A freed creature appears in an unoccupied space within sight of the mirror but facing away from it. If the mirror is shattered, all creatures it contains are freed and appear in unoccupied spaces near it.
While within 5 feet of the mirror, you can use an action to speak the name of one creature trapped in it or call out a particular cell by number. The creature named or contained in the named cell appears as an image on the mirror’s surface. You and the creature can then communicate normally.
In a similar way, you can use an action to speak a second command word and free one creature
trapped in the mirror. The freed creature appears, along with its possessions, in the unoccupied space nearest to the mirror and facing away from it."
     }{
     name-key "Mithral Armor"
     ::type :armor
     ::item-subtype heavy-metal-armor?
     ::rarity :uncommon
     :stealth-disadvantage? false
     :min-str 1
     ::description "Mithral is a light, flexible metal. A mithral chain shirt or breastplate can be worn under normal clothes. If the armor normally imposes disadvantage on Dexterity (Stealth) checks or has a Strength requirement, the mithral version of the armor doesn’t."
     }{
     name-key "Necklace of Adaptation"
     ::type :wondrous-item

     ::rarity :uncommon

     ::attunement [:any]
     ::description "While wearing this necklace, you can breathe normally in any environment, and you have advantage on saving throws made against harmful gases and vapors (such as cloudkill and stinking cloud effects, inhaled poisons, and the breath weapons of some dragons)."
     }{
     name-key "Necklace of Fireballs"
     ::type :wondrous-item
     ::rarity :rare
     ::description "This necklace has 1d6 + 3 beads hanging from it. You can use an action to detach a bead and throw it up to 60 feet away. When it reaches the end of its trajectory, the bead detonates as a 3rd-level fireball
spell (save DC 15).
You can hurl multiple beads, or even the whole necklace, as one action. When you do so, increase the level of the fireball by 1 for each bead beyond the first."
     }{
     name-key "Necklace of Prayer Beads"
     ::type :wondrous-item

     ::rarity :rare

     ::attunement [:cleric, :druid, :paladin]
     ::description "This necklace has 1d4 + 2 magic beads made from aquamarine, black pearl, or topaz. It also has many nonmagical beads made from stones such as amber, bloodstone, citrine, coral, jade, pearl, or quartz. If a magic bead is removed from the necklace, that bead loses its magic.
Six types of magic beads exist. The GM decides the type of each bead on the necklace or determines it randomly. A necklace can have more than one bead of the same type. To use one, you must be wearing the necklace. Each bead contains a spell that you can cast from it as a bonus action (using your spell save DC if a save is necessary). Once a magic bead’s spell
is cast, that bead can’t be used again until the next dawn."
     }{
     name-key "Nine Lives Stealer"
     ::type :weapon
     ::item-subtype sword?

     ::rarity :very-rare

     ::attunement [:any]
     ::magical-attack-bonus 2
     ::magical-damage-bonus 2
     ::description "You gain a +2 bonus to attack and damage rolls made with this magic weapon.
The sword has 1d8 + 1 charges. If you score a critical hit against a creature that has fewer than 100 hit points, it must succeed on a DC 15 Constitution saving throw or be slain instantly as the sword tears its life force from its body (a construct or an undead is immune). The sword loses 1 charge if the creature is slain. When the sword has no charges remaining, it loses this property."
     }{
     name-key "Oathbow"
     ::type :weapon
     ::item-subtype :longbow

     ::rarity :very-rare

     ::attunement [:any]
     ::description "When you nock an arrow on this bow, it whispers in Elvish, “Swift defeat to my enemies.” When you use this weapon to make a ranged attack, you can, as a command phrase, say, “Swift death to you who have
wronged me.” The target of your attack becomes your sworn enemy until it dies or until dawn seven days later. You can have only one such sworn enemy at a time. When your sworn enemy dies, you can choose a new one after the next dawn.
When you make a ranged attack roll with this weapon against your sworn enemy, you have advantage on the roll. In addition, your target gains no benefit from cover, other than total cover, and you suffer no disadvantage due to long range. If the attack hits, your sworn enemy takes an extra 3d6 piercing damage.
While your sworn enemy lives, you have disadvantage on attack rolls with all other weapons."
     }{
     name-key "Oil of Etherealness"
     ::type :potion

     ::rarity :rare
     ::description "Beads of this cloudy gray oil form on the outside of its container and quickly evaporate. The oil can cover a Medium or smaller creature, along with the equipment it’s wearing and carrying (one additional
vial is required for each size category above Medium). Applying the oil takes 10 minutes. The affected creature then gains the effect of the etherealness spell for 1 hour."
     }{
     name-key "Oil of Sharpness"
     ::type :potion
     ::rarity :very-rare
     ::description "This clear, gelatinous oil sparkles with tiny, ultrathin silver shards. The oil can coat one slashing or piercing weapon or up to 5 pieces of slashing or piercing ammunition. Applying the oil takes 1 minute. For 1 hour, the coated item is magical and has a +3 bonus to attack and damage rolls."
     }{
     name-key "Oil of Slipperiness"
     ::type :potion
     ::rarity :uncommon
     ::description "This sticky black unguent is thick and heavy in the container, but it flows quickly when poured. The oil can cover a Medium or smaller creature, along with the equipment it’s wearing and carrying (one additional vial is required for each size category above Medium). Applying the oil takes 10 minutes. The affected creature then gains the effect of a freedom of movement spell for 8 hours.
Alternatively, the oil can be poured on the ground as an action, where it covers a 10-foot square, duplicating the effect of the grease spell in that area for 8 hours."
     }{
     name-key "Pearl of Power"
     ::type :wondrous-item

     ::rarity :uncommon

     ::attunement [:spellcaster]
     ::description "While this pearl is on your person, you can use an action to speak its command word and regain one expended spell slot. If the expended slot was of 4th level or higher, the new slot is 3rd level. Once you use the pearl, it canʼt be used again until the next dawn."
     }{
     name-key "Periapt of Health"
     ::type :wondrous-item
     ::rarity :uncommon
     ::description "You are immune to contracting any disease while you wear this pendant. If you are already infected with a disease, the effects of the disease are suppressed you while you wear the pendant."
     }{
     name-key "Periapt of Proof against Poison"
     ::type :wondrous-item
     ::rarity :rare
     ::description "This delicate silver chain has a brilliant-cut black gem pendant. While you wear it, poisons have no effect on you. You are immune to the poisoned condition and have immunity to poison damage."
     }{
     name-key "Periapt of Wound Closure"
     ::type :wondrous-item

     ::rarity :uncommon

     ::attunement [:any]
     ::description "While you wear this pendant, you stabilize whenever you are dying at the start of your turn. In addition, whenever you roll a Hit Die to regain hit points, double the number of hit points it restores."
     }{
     name-key "Philter of Love"
     ::type :potion
     ::rarity :uncommon
     ::description "The next time you see a creature within 10 minutes after drinking this philter, you become charmed by that creature for 1 hour. If the creature is of a species and gender you are normally attracted to, you regard it as your true love while you are charmed. This potion’s rose-hued, effervescent liquid contains one easy-to-miss bubble shaped like a heart."
     }{
     name-key "Pipes of Haunting"
     ::type :wondrous-item
     ::rarity :uncommon
     ::description "You must be proficient with wind instruments to use these pipes. They have 3 charges. You can use an action to play them and expend 1 charge to create an eerie, spellbinding tune. Each creature within 30 feet of you that hears you play must succeed on a DC 15 Wisdom saving throw or become frightened of you for 1 minute. If you wish, all creatures in the area that aren’t hostile toward you automatically succeed on the saving throw. A creature that fails the saving throw can repeat it at the end of each of its turns, ending the effect on itself on a success. A creature that succeeds on its saving throw is immune to the effect of these pipes for 24 hours. The pipes regain 1d3 expended charges daily at dawn."
     }{
     name-key "Pipes of the Sewers"
     ::type :wondrous-item

     ::rarity :uncommon

     ::attunement [:any]
     ::description "You must be proficient with wind instruments to use these pipes. While you are attuned to the pipes, ordinary rats and giant rats are indifferent toward you and will not attack you unless you threaten or harm them.
The pipes have 3 charges. If you play the pipes as an action, you can use a bonus action to expend 1 to 3 charges, calling forth one swarm of rats with each expended charge, provided that enough rats are within half a mile of you to be called in this fashion (as determined by the GM). If there aren’t enough rats to form a swarm, the charge is wasted. Called swarms move toward the music by the shortest available route but aren’t under your control otherwise. The pipes regain 1d3 expended charges daily at dawn.
Whenever a swarm of rats that isn’t under another creature’s control comes within 30 feet of you while you are playing the pipes, you can make a Charisma check contested by the swarm’s Wisdom check. If you lose the contest, the swarm behaves as it normally would and can’t be swayed by the pipes’ music for the next 24 hours. If you win the contest, the swarm is swayed by the pipes’ music and becomes friendly to you and your companions for as long as you continue to play the pipes each round as an action. A friendly swarm obeys your commands. If you issue no commands to a friendly swarm, it defends itself but otherwise takes no actions. If a friendly swarm starts its turn and can’t hear the pipes’ music, your control over that swarm ends, and the swarm behaves as it normally would and can’t be swayed by the pipes’ music for the next 24 hours."
     }{
     name-key "Plate Armor of Etherealness"
     ::type :armor
     ::item-subtype :plate

     ::rarity :legendary

     ::attunement [:any]
     ::description "While you’re wearing this armor, you can speak its command word as an action to gain the effect of the etherealness spell, which last for 10 minutes or until you remove the armor or use an action to speak the command word again. This property of the armor can’t be used again until the next dawn."
     }{
     name-key "Portable Hole"
     ::type :wondrous-item
     ::rarity :rare
     ::description "This fine black cloth, soft as silk, is folded up to the dimensions of a handkerchief. It unfolds into a circular sheet 6 feet in diameter.
You can use an action to unfold a portable hole and place it on or against a solid surface, whereupon the portable hole creates an extradimensional hole 10 feet deep. The cylindrical space within the hole exists on a different plane, so it can’t be used to create open passages. Any creature inside an open portable hole can exit the hole by climbing out of it.
You can use an action to close a portable hole by taking hold of the edges of the cloth and folding it up. Folding the cloth closes the hole, and any creatures or objects within remain in the extradimensional space. No matter what’s in it, the hole weighs next to nothing.
If the hole is folded up, a creature within the hole’s extradimensional space can use an action to make a DC 10 Strength check. On a successful check, the creature forces its way out and appears within 5 feet of the portable hole or the creature carrying it. A breathing creature within a closed portable hole can survive for up to 10 minutes, after which time it begins to suffocate.
Placing a portable hole inside an extradimensional space created by a bag of holding, handy haversack, or similar item instantly destroys both items and opens a gate to the Astral Plane. The gate originates where the one item was placed inside the other. Any creature within 10 feet of the gate is sucked through it and deposited in a random location on the Astral Plane. The gate then closes. The gate is one-way only and can’t be reopened."
     }{
     name-key "Potion of Animal Friendship"
     ::type :potion
     ::rarity :uncommon
     ::description "When you drink this potion, you can cast the animal friendship spell (save DC 13) for 1 hour at will. Agitating this muddy liquid brings little bits into view: a fish scale, a hummingbird tongue, a cat claw, or a squirrel hair."
     }{
     name-key "Potion of Clairvoyance"
     ::type :potion
     ::rarity :rare
     ::description "When you drink this potion, you gain the effect of the clairvoyance spell. An eyeball bobs in this yellowish liquid but vanishes when the potion is opened."
     }{
     name-key "Potion of Climbing"
     ::type :potion
     ::rarity :common
     ::description "When you drink this potion, you gain a climbing speed equal to your walking speed for 1 hour. During this time, you have advantage on Strength (Athletics) checks you make to climb. The potion is separated into brown, silver, and gray layers resembling bands of stone. Shaking the bottle fails to mix the colors."
     }{
     name-key "Potion of Diminution"
     ::type :potion
     ::rarity :rare
     ::description "When you drink this potion, you gain the “reduce” effect of the enlarge/reduce spell for 1d4 hours (no concentration required). The red in the potion’s liquid continuously contracts to a tiny bead and then expands to color the clear liquid around it. Shaking the bottle fails to interrupt this process."
     }{
     name-key "Potion of Flying"
     ::type :potion
     ::rarity :very-rare
     ::description "When you drink this potion, you gain a flying speed equal to your walking speed for 1 hour and can hover. If you’re in the air when the potion wears off, you fall unless you have some other means of staying aloft. This potion’s clear liquid floats at the top of its container and has cloudy white impurities drifting in it."
     }{
     name-key "Potion of Gaseous Form"
     ::type :potion
     ::rarity :rare
     ::description "When you drink this potion, you gain the effect of the gaseous form spell for 1 hour (no concentration required) or until you end the effect as a bonus action. This potion’s container seems to hold fog that moves and pours like water."
       }
    (potion-of-giant-strength "Hill Giant" 21 :uncommon)
    (potion-of-giant-strength "Frost Giant" 23 :rare)
    (potion-of-giant-strength "Stone Giant" 23 :rare)
    (potion-of-giant-strength "Fire Giant" 25 :rare)
    (potion-of-giant-strength "Cloud Giant" 27 :very-rare)
    (potion-of-giant-strength "Storm Giant" 29 :legendary)
    {
     name-key "Potion of Growth"
     ::type :potion
     ::rarity :uncommon
     ::description "When you drink this potion, you gain the “enlarge” effect of the enlarge/reduce spell for 1d4 hours (no concentration required). The red in the potion’s liquid continuously expands from a tiny bead to color the clear liquid around it and then contracts. Shaking the bottle fails to interrupt this process."
     }{
     name-key "Potion of Healing"
     ::type :potion
     ::rarity :common
     ::description "You regain 2d4 + 2 hit points when you drink this potion"
     }{
     name-key "Potion of Greater Healing"
     ::type :potion
     ::rarity :uncommon
     ::description "You regain 4d4 + 4 hit points when you drink this potion"
     }{
     name-key "Potion of Superior Healing"
     ::type :potion
     ::rarity :rare
     ::description "You regain 8d4 + 8 hit points when you drink this potion"
     }{
     name-key "Potion of Supreme Healing"
     ::type :potion
     ::rarity :very-rare
     ::description "You regain 10d4 + 20 hit points when you drink this potion"
     }{
     name-key "Potion of Heroism"
     ::type :potion
     ::rarity :rare
     ::description "For 1 hour after drinking it, you gain 10 temporary hit points that last for 1 hour. For the same duration, you are under the effect of the bless spell (no concentration required). This blue potion bubbles and steams as if boiling."
     }{
     name-key "Potion of Invisibility"
     ::type :potion
     ::rarity :very-rare
     ::description "This potion’s container looks empty but feels as though it holds liquid. When you drink it, you become invisible for 1 hour. Anything you wear or carry is invisible with you. The effect ends early if you attack or cast a spell."
     }{
     name-key "Potion of Mind Reading"
     ::type :potion
     ::rarity :rare
     ::description "When you drink this potion, you gain the effect of the detect thoughts spell (save DC 13). The potion’s dense, purple liquid has an ovoid cloud of pink floating in it."
     }{
     name-key "Potion of Poison"
     ::type :potion
     ::rarity :uncommon
     ::description "This concoction looks, smells, and tastes like a potion of healing or other beneficial potion. However, it is actually poison masked by illusion magic. An identify
spell reveals its true nature.
If you drink it, you take 3d6 poison damage, and you must succeed on a DC 13 Constitution saving throw or be poisoned. At the start of each of your turns while you are poisoned in this way, you take 3d6 poison damage. At the end of each of your turns, you can repeat the saving throw. On a successful save, the poison damage you take on your subsequent turns decreases by 1d6. The poison ends when the damage decreases to 0."
     }{
     name-key "Potion of Resistance"
     ::type :potion
     ::rarity :uncommon
     ::description "When you drink this potion, you gain resistance to one type of damage for 1 hour. The GM chooses the type or determines it randomly from the options below."
     }{
     name-key "Potion of Speed"
     ::type :potion
     ::rarity :very-rare
     ::description "When you drink this potion, you gain the effect of the haste spell for 1 minute (no concentration required). The potion’s yellow fluid is streaked with black and swirls on its own."
     }{
     name-key "Potion of Water Breathing"
     ::type :potion
     ::rarity :uncommon
     ::description "You can breathe underwater for 1 hour after drinking this potion. Its cloudy green fluid smells of the sea and has a jellyfish-like bubble floating in it."
     }{
     name-key "Restorative Ointment"
     ::type :wondrous-item
     ::rarity :uncommon
     ::description "This glass jar, 3 inches in diameter, contains 1d4 + 1 doses of a thick mixture that smells faintly of aloe. The jar and its contents weigh 1/2 pound.
As an action, one dose of the ointment can be swallowed or applied to the skin. The creature that receives it regains 2d8 + 2 hit points, ceases to be poisoned, and is cured of any disease."
     }{
     name-key "Ring of Animal Influence"
     ::type :ring
     ::rarity :rare
     ::description "This ring has 3 charges, and it regains 1d3 expended charges daily at dawn. While wearing the ring, you can use an action to expend 1 of its charges to cast one of the following spells:
• Animal friendship (save DC 13)
• Fear (save DC 13), targeting only beasts that have an Intelligence of 3 or lower
• Speak with animals"
     }{
     name-key "Ring of Djinni Summoning"
     ::type :ring

     ::rarity :legendary

     ::attunement [:any]
     ::description "While wearing this ring, you can speak its command word as an action to summon a particular djinni from the Elemental Plane of Air. The djinni appears in an unoccupied space you choose within 120 feet of you. It remains as long as you concentrate (as if concentrating on a spell), to a maximum of 1 hour, or until it drops to 0 hit points. It then returns to its home plane.
While summoned, the djinni is friendly to you and your companions. It obeys any commands you give it, no matter what language you use. If you fail to command it, the djinni defends itself against attackers but takes no other actions.
After the djinni departs, it can’t be summoned again for 24 hours, and the ring becomes nonmagical if the djinni dies."
     }{
     name-key "Ring of Elemental Command"
     ::type :ring

     ::rarity :legendary

     ::attunement [:any]
     ::description "This ring is linked to one of the four Elemental Planes. The GM chooses or randomly determines the linked plane.
While wearing this ring, you have advantage on attack rolls against elementals from the linked plane, and they have disadvantage on attack rolls against you. In addition, you have access to properties based on the linked plane.
The ring has 5 charges. It regains 1d4 + 1 expended charges daily at dawn. Spells cast from the ring have a save DC of 17.
Ring of Air Elemental Command. You can expend 2 of the ring’s charges to cast dominate monster on an air elemental. In addition, when you fall, you descend 60 feet per round and take no damage from falling. You can also speak and understand Auran.
If you help slay an air elemental while attuned to the ring, you gain access to the following additional properties:
• You have resistance to lightning damage.
• You have a flying speed equal to your walking speed and can hover.
• You can cast the following spells from the ring, expending the necessary number of charges: chain lightning (3 charges), gust of wind (2 charges), or wind wall (1 charge).
Ring of Earth Elemental Command. You can expend 2 of the ring’s charges to cast dominate monster on an earth elemental. In addition, you can move in difficult terrain that is composed of rubble, rocks, or dirt as if it were normal terrain. You can also speak and understand Terran.
If you help slay an earth elemental while attuned to the ring, you gain access to the following additional properties:
• You have resistance to acid damage.
• You can move through solid earth or rock as if those areas were difficult terrain. If you end your turn there, you are shunted out to the nearest unoccupied space you last occupied.
• You can cast the following spells from the ring, expending the necessary number of charges: stone shape (2 charges), stoneskin (3 charges), or wall of stone (3 charges).
Ring of Fire Elemental Command. You can expend 2 of the ring’s charges to cast dominate monster on a fire elemental. In addition, you have resistance to fire damage. You can also speak and understand Ignan.
If you help slay a fire elemental while attuned to the ring, you gain access to the following additional properties:
• You are immune to fire damage.
• You can cast the following spells from the ring, expending the necessary number of charges: burning hands (1 charge), fireball (2 charges), and wall of fire (3 charges).
Ring of Water Elemental Command. You can expend 2 of the ring’s charges to cast dominate monster on a water elemental. In addition, you can stand on and walk across liquid surfaces as if they were solid ground. You can also speak and understand Aquan.
If you help slay a water elemental while attuned to the ring, you gain access to the following additional properties:
• You can breathe underwater and have a swimming speed equal to your walking speed.
• You can cast the following spells from the ring, expending the necessary number of charges: create or destroy water (1 charge), control water (3 charges), ice storm (2 charges), or wall of ice (3 charges)."
     }{
     name-key "Ring of Evasion"
     ::type :ring

     ::rarity :rare

     ::attunement [:any]
     ::description "This ring has 3 charges, and it regains 1d3 expended charges daily at dawn. When you fail a Dexterity saving throw while wearing it, you can use your reaction to expend 1 of its charges to succeed on that saving throw instead."
     }{
     name-key "Ring of Feather Falling"
     ::type :ring

     ::rarity :rare

     ::attunement [:any]
     ::description "When you fall while wearing this ring, you descend 60 feet per round and take no damage from falling."
     }{
     name-key "Ring of Free Action"
     ::type :ring

     ::rarity :rare

     ::attunement [:any]
     ::description "While you wear this ring, difficult terrain doesn’t cost you extra movement. In addition, magic can neither reduce your speed nor cause you to be paralyzed or restrained."
     }{
     name-key "Ring of Invisibility"
     ::type :ring

     ::rarity :legendary

     ::attunement [:any]
     ::description "While wearing this ring, you can turn invisible as an action. Anything you are wearing or carrying is invisible with you. You remain invisible until the ring is removed, until you attack or cast a spell, or until you use a bonus action to become visible again."
     }{
     name-key "Ring of Jumping"
     ::type :ring

     ::rarity :uncommon

     ::attunement [:any]
     ::description "While wearing this ring, you can cast the jump spell from it as a bonus action at will, but can target only yourself when you do so."
     }{
     name-key "Ring of Mind Shielding"
     ::type :ring

     ::rarity :uncommon

     ::attunement [:any]
     ::description "While wearing this ring, you are immune to magic that allows other creatures to read your thoughts, determine whether you are lying, know your alignment, or know your creature type. Creatures can telepathically communicate with you only if you allow it.
You can use an action to cause the ring to become invisible until you use another action to make it visible, until you remove the ring, or until you die.
If you die while wearing the ring, your soul enters it, unless it already houses a soul. You can remain in the ring or depart for the afterlife. As long as your soul is in the ring, you can telepathically communicate with any creature wearing it. A wearer can’t prevent this telepathic communication."
     }{
     name-key "Ring of Protection"
     ::type :ring

     ::rarity :rare

     ::attunement [:any]
     ::magical-ac-bonus 1
     ::modifiers (map #(mod5e/saving-throw-bonus % 1) char5e/ability-keys)
     ::description "You gain a +1 bonus to AC and saving throws while wearing this ring."
     }{
     name-key "Ring of Regeneration"
     ::type :ring

     ::rarity :very-rare

     ::attunement [:any]
     ::description "While wearing this ring, you regain 1d6 hit points every 10 minutes, provided that you have at least 1 hit point. If you lose a body part, the ring causes the missing part to regrow and return to full functionality after 1d6 + 1 days if you have at least 1 hit point the whole time."
     }{
     name-key "Ring of Shooting Stars"
       ::type :ring
       ::rarity :very-rare
       ::attunement [:any]
       ::attunement-details "requires attunement outdoors at night"
     ::description "While wearing this ring in dim light or darkness, you can cast dancing lights and light from the ring at will. Casting either spell from the ring requires an action.
The ring has 6 charges for the following other properties. The ring regains 1d6 expended charges daily at dawn.
Faerie Fire. You can expend 1 charge as an action to cast faerie fire from the ring.
Ball Lightning. You can expend 2 charges as an action to create one to four 3-foot-diameter spheres of lightning. The more spheres you create, the less powerful each sphere is individually.
Each sphere appears in an unoccupied space you can see within 120 feet of you. The spheres last as long as you concentrate (as if concentrating on a spell), up to 1 minute. Each sphere sheds dim light in a 30-foot radius.
As a bonus action, you can move each sphere up to 30 feet, but no farther than 120 feet away from you. When a creature other than you comes within 5 feet of a sphere, the sphere discharges lightning at that creature and disappears. That creature must make a DC 15 Dexterity saving throw. On a failed save, the creature takes lightning damage based on the number of spheres you created.
Shooting Stars. You can expend 1 to 3 charges as an action. For every charge you expend, you launch a glowing mote of light from the ring at a point you can see within 60 feet of you. Each creature within a 15-foot cube originating from that point is showered in sparks and must make a DC 15 Dexterity saving throw, taking 5d4 fire damage on a failed save, or half as much damage on a successful one."
     }{
     name-key "Ring of Spell Storing"
     ::type :ring

     ::rarity :rare

     ::attunement [:any]
     ::description "This ring stores spells cast into it, holding them until the attuned wearer uses them. The ring can store up to 5 levels worth of spells at a time. When found, it contains 1d6 − 1 levels of stored spells chosen by the GM.
Any creature can cast a spell of 1st through 5th level into the ring by touching the ring as the spell is cast. The spell has no effect, other than to be stored in the ring. If the ring can’t hold the spell, the spell is expended without effect. The level of the slot used to cast the spell determines how much space it uses.
While wearing this ring, you can cast any spell stored in it. The spell uses the slot level, spell save DC, spell attack bonus, and spellcasting ability of the original caster, but is otherwise treated as if you cast the spell. The spell cast from the ring is no longer stored in it, freeing up space."
     }{
     name-key "Ring of Spell Turning"
     ::type :ring

     ::rarity :legendary

     ::attunement [:any]
     ::description "While wearing this ring, you have advantage on saving throws against any spell that targets only you (not in an area of effect). In addition, if you roll a 20 for the save and the spell is 7th level or lower, the spell has no effect on you and instead targets the caster, using the slot level, spell save DC, attack bonus, and spellcasting ability of the caster."
     }{
     name-key "Ring of Swimming"
     ::type :ring
     ::rarity :uncommon
     ::description "You have a swimming speed of 40 feet while wearing this ring."
     }{
     name-key "Ring of Telekinesis"
     ::type :ring

     ::rarity :very-rare

     ::attunement [:any]
     ::description "While wearing this ring, you can cast the telekinesis
spell at will, but you can target only objects that aren’t being worn or carried."
     }{
     name-key "Ring of the Ram"
     ::type :ring

     ::rarity :rare

     ::attunement [:any]
     ::description "This ring has 3 charges, and it regains 1d3 expended charges daily at dawn. While wearing the ring, you can use an action to expend 1 to 3 of its charges to attack one creature you can see within 60 feet of you. The ring produces a spectral ram’s head and makes its attack roll with a +7 bonus. On a hit, for each charge you spend, the target takes 2d10 force damage and is pushed 5 feet away from you.
Alternatively, you can expend 1 to 3 of the ring’s charges as an action to try to break an object you can see within 60 feet of you that isn’t being worn or carried. The ring makes a Strength check with a +5 bonus for each charge you spend."
     }{
     name-key "Ring of Three Wishes"
     ::type :ring
     ::rarity :legendary
     ::description "While wearing this ring, you can use an action to expend 1 of its 3 charges to cast the wish spell from it. The ring becomes nonmagical when you use the last charge."
     }{
     name-key "Ring of Warmth"
     ::type :ring

     ::rarity :uncommon

     ::attunement [:any]
     ::description "While wearing this ring, you have resistance to cold damage. In addition, you and everything you wear and carry are unharmed by temperatures as low as −50 degrees Fahrenheit."
     }{
     name-key "Ring of Water Walking"
     ::type :ring
     ::rarity :uncommon
     ::description "While wearing this ring, you can stand on and move across any liquid surface as if it were solid ground."
     }{
     name-key "Ring of X-ray Vision"
     ::type :ring

     ::rarity :rare

     ::attunement [:any]
     ::description "While wearing this ring, you can use an action to speak its command word. When you do so, you can see into and through solid matter for 1 minute. This vision has a radius of 30 feet. To you, solid objects within that radius appear transparent and don’t prevent light from passing through them. The vision can penetrate 1 foot of stone, 1 inch of common metal, or up to 3 feet of wood or dirt. Thicker substances block the vision, as does a thin sheet of lead.
Whenever you use the ring again before taking a long rest, you must succeed on a DC 15 Constitution saving throw or gain one level of exhaustion."
     }{
     name-key "Robe of Eyes"
     ::type :wondrous-item

     ::rarity :rare

     ::attunement [:any]
     ::description "This robe is adorned with eyelike patterns. While you wear the robe, you gain the following benefits:
• The robe lets you see in all directions, and you have advantage on Wisdom (Perception) checks that rely on sight.
• You have darkvision out to a range of 120 feet.
• You can see invisible creatures and objects, as well as see into the Ethereal Plane, out to a range of 120 feet.
The eyes on the robe can’t be closed or averted. Although you can close or avert your own eyes, you are never considered to be doing so while wearing this robe.
A light spell cast on the robe or a daylight spell cast within 5 feet of the robe causes you to be blinded for 1 minute. At the end of each of your turns, you can make a Constitution saving throw (DC 11 for light or DC 15 for daylight), ending the blindness on a success."
     }{
     name-key "Robe of Scintillating Colors"
     ::type :wondrous-item

     ::rarity :very-rare

     ::attunement [:any]
     ::description "This robe has 3 charges, and it regains 1d3 expended charges daily at dawn. While you wear it, you can use an action and expend 1 charge to cause the garment to display a shifting pattern of dazzling hues until the end of your next turn. During this time, the robe sheds bright light in a 30-foot radius and dim light for an additional 30 feet. Creatures that can see you have disadvantage on attack rolls against you. In addition, any creature in the bright light that can see you when the robe’s power is activated must succeed on a DC 15 Wisdom saving throw or become stunned until the effect ends."
     }{
     name-key "Robe of Stars"
     ::type :wondrous-item

     ::rarity :very-rare

     ::attunement [:any]
     ::description "This black or dark blue robe is embroidered with small white or silver stars. You gain a +1 bonus to saving throws while you wear it.
Six stars, located on the robe’s upper front portion, are particularly large. While wearing this robe, you can use an action to pull off one of the stars and use it to cast magic missile as a 5th-level spell. Daily at dusk, 1d6 removed stars reappear on the robe.
While you wear the robe, you can use an action to enter the Astral Plane along with everything you are wearing and carrying. You remain there until you use an action to return to the plane you were on. You reappear in the last space you occupied, or if that space is occupied, the nearest unoccupied space."
     }{
     name-key "Robe of the Archmagi"
     ::type :wondrous-item

     ::rarity :legendary

     ::attunement [:sorcerer, :warlock, :wizard]
     ::modifiers [(mod5e/spell-save-dc-bonus 2)
                 (mod5e/spell-attack-modifier-bonus 2)
                 (mod5e/saving-throw-advantage ["Spells and other magical effects"])
                 (mod5e/ac-bonus-fn
                  (fn [armor shield]
                    (if (nil? armor)
                      5
                      0)))]
     ::description "This elegant garment is made from exquisite cloth of white, gray, or black and adorned with silvery runes. The robe’s color corresponds to the alignment for which the item was created. A white robe was made for good, gray for neutral, and black for evil. You can’t attune to a robe of the archmagi that doesn’t correspond to your alignment.
You gain these benefits while wearing the robe:
• If you aren’t wearing armor, your base Armor Class is 15 + your Dexterity modifier.
• You have advantage on saving throws against spells and other magical effects.
• Your spell save DC and spell attack bonus each increase by 2."
     }{
     name-key "Robe of Useful Items"
     ::type :wondrous-item
     ::rarity :uncommon
     ::description "This robe has cloth patches of various shapes and colors covering it. While wearing the robe, you can use an action to detach one of the patches, causing it to become the object or creature it represents. Once the last patch is removed, the robe becomes an ordinary garment.
The robe has two of each of the following patches:
• Dagger
• Bullseye lantern (filled and lit)
• Steel mirror
• 10-foot pole
• Hempen rope (50 feet, coiled)
• Sack
In addition, the robe has 4d4 other patches. The GM chooses the patches or determines them randomly."
     }{
     name-key "Rod of Absorption"
     ::type :rod

     ::rarity :very-rare

     ::attunement [:any]
     ::description "While holding this rod, you can use your reaction to absorb a spell that is targeting only you and not with an area of effect. The absorbed spell’s effect is canceled, and the spell’s energy—not the spell itself—is stored in the rod. The energy has the same level as the spell when it was cast. The rod can absorb and store up to 50 levels of energy over the course of its existence. Once the rod absorbs 50 levels of energy, it can’t absorb more. If you are targeted by a spell that the rod can’t store, the rod has no effect on that spell.
When you become attuned to the rod, you know how many levels of energy the rod has absorbed over the course of its existence, and how many levels of spell energy it currently has stored.
If you are a spellcaster holding the rod, you can convert energy stored in it into spell slots to cast spells you have prepared or know. You can create spell slots only of a level equal to or lower than your own spell slots, up to a maximum of 5th level. You use the stored levels in place of your slots, but otherwise cast the spell as normal. For example, you can use 3 levels stored in the rod as a 3rd-level spell slot.
A newly found rod has 1d10 levels of spell energy stored in it already. A rod that can no longer absorb spell energy and has no energy remaining becomes nonmagical."
     }{
     name-key "Rod of Alertness"
     ::type :rod

     ::rarity :very-rare

     ::attunement [:any]
     ::description "This rod has a flanged head and the following properties.
Alertness. While holding the rod, you have advantage on Wisdom (Perception) checks and on rolls for initiative.
Spells. While holding the rod, you can use an action to cast one of the following spells from it: detect evil and good, detect magic, detect poison and disease, or see invisibility.
Protective Aura. As an action, you can plant the haft end of the rod in the ground, whereupon the rod’s head sheds bright light in a 60-foot radius and dim light for an additional 60 feet. While in that bright light, you and any creature that is friendly to you gain a +1 bonus to AC and saving throws and can sense the location of any invisible hostile creature that is also in the bright light.
The rod’s head stops glowing and the effect ends after 10 minutes, or when a creature uses an action to pull the rod from the ground. This property can’t be used again until the next dawn."
     }{
     name-key "Rod of Lordly Might"
     ::type :rod

     ::rarity :legendary

     ::attunement [:any]
     ::magical-attack-bonus 3
     ::magical-damage-bonus 3
     ::description "This rod has a flanged head, and it functions as a magic mace that grants a +3 bonus to attack and damage rolls made with it. The rod has properties associated with six different buttons that are set in a row along the haft. It has three other properties as well, detailed below.
Six Buttons. You can press one of the rod’s six buttons as a bonus action. A button’s effect lasts until you push a different button or until you push the same button again, which causes the rod to revert to its normal form.
If you press button 1, the rod becomes a flame tongue, as a fiery blade sprouts from the end opposite the rod’s flanged head.
If you press button 2, the rod’s flanged head folds down and two crescent-shaped blades spring out, transforming the rod into a magic battleaxe that grants a +3 bonus to attack and damage rolls made with it.
If you press button 3, the rod’s flanged head folds down, a spear point springs from the rod’s tip, and the rod’s handle lengthens into a 6-foot haft, transforming the rod into a magic spear that grants a +3 bonus to attack and damage rolls made with it.
If you press button 4, the rod transforms into a climbing pole up to 50 feet long, as you specify. In surfaces as hard as granite, a spike at the bottom and three hooks at the top anchor the pole. Horizontal bars 3 inches long fold out from the sides, 1 foot apart, forming a ladder. The pole can bear up to 4,000 pounds. More weight or lack of solid anchoring causes the rod to revert to its normal form.
If you press button 5, the rod transforms into a handheld battering ram and grants its user a +10 bonus to Strength checks made to break through doors, barricades, and other barriers.
If you press button 6, the rod assumes or remains in its normal form and indicates magnetic north. (Nothing happens if this function of the rod is used in a location that has no magnetic north.) The rod also gives you knowledge of your approximate depth beneath the ground or your height above it.
Drain Life. When you hit a creature with a melee attack using the rod, you can force the target to make a DC 17 Constitution saving throw. On a failure, the target takes an extra 4d6 necrotic damage, and you regain a number of hit points equal to half that necrotic damage. This property can’t be used again until the next dawn.
Paralyze. When you hit a creature with a melee attack using the rod, you can force the target to make a DC 17 Strength saving throw. On a failure, the target is paralyzed for 1 minute. The target can repeat the saving throw at the end of each of its turns, ending the effect on a success. This property can’t be used again until the next dawn.
Terrify. While holding the rod, you can use an action to force each creature you can see within 30 feet of you to make a DC 17 Wisdom saving throw. On a failure, a target is frightened of you for 1 minute. A frightened target can repeat the saving throw at the end of each of its turns, ending the effect on itself on a success. This property can’t be used again until the next dawn."
     }
    (rod-of-the-pact-keeper 1)
    (rod-of-the-pact-keeper 2)
    (rod-of-the-pact-keeper 3)
    {
     name-key "Rod of Rulership"
     ::type :rod

     ::rarity :rare

     ::attunement [:any]
     ::description "You can use an action to present the rod and command obedience from each creature of your choice that you can see within 120 feet of you. Each target must succeed on a DC 15 Wisdom saving throw or be charmed by you for 8 hours. While charmed in this way, the creature regards you as its trusted leader. If harmed by you or your companions, or commanded to do something contrary to its nature, a target ceases to be charmed in this way. The rod can’t be used again until the next dawn."
     }{
     name-key "Rod of Security"
     ::type :rod
     ::rarity :very-rare
     ::description "While holding this rod, you can use an action to activate it. The rod then instantly transports you and up to 199 other willing creatures you can see to a paradise that exists in an extraplanar space. You choose the form that the paradise takes. It could be a tranquil garden, lovely glade, cheery tavern, immense palace, tropical island, fantastic carnival, or whatever else you can imagine. Regardless of its nature, the paradise contains enough water and food to sustain its visitors. Everything else that can be interacted with inside the extraplanar space can exist only there. For example, a flower picked from a garden in the paradise disappears if it is taken outside the extraplanar space.
For each hour spent in the paradise, a visitor regains hit points as if it had spent 1 Hit Die. Also, creatures don’t age while in the paradise, although time passes normally. Visitors can remain in the paradise for up to 200 days divided by the number of creatures present (round down).
When the time runs out or you use an action to end it, all visitors reappear in the location they occupied when you activated the rod, or an unoccupied space nearest that location. The rod can’t be used again until ten days have passed."
     }{
     name-key "Rope of Climbing"
     ::type :wondrous-item
     ::rarity :uncommon
     ::description "This 60-foot length of silk rope weighs 3 pounds and can hold up to 3,000 pounds. If you hold one end of the rope and use an action to speak the command word, the rope animates. As a bonus action, you can command the other end to move toward a destination you choose. That end moves 10 feet on your turn when you first command it and 10 feet on each of your turns until reaching its destination, up to its maximum length away, or until you tell it to stop. You can also tell the rope to fasten itself securely to an object or to unfasten itself, to knot or unknot itself, or to coil itself for carrying.
If you tell the rope to knot, large knots appear at 1-
foot intervals along the rope. While knotted, the rope shortens to a 50-foot length and grants advantage on checks made to climb it.
The rope has AC 20 and 20 hit points. It regains 1 hit point every 5 minutes as long as it has at least 1 hit point. If the rope drops to 0 hit points, it is destroyed."
     }{
     name-key "Rope of Entanglement"
     ::type :wondrous-item
     ::rarity :rare
     ::description "This rope is 30 feet long and weighs 3 pounds. If you hold one end of the rope and use an action to speak its command word, the other end darts forward to entangle a creature you can see within 20 feet of you. The target must succeed on a DC 15 Dexterity saving
throw or become restrained.
You can release the creature by using a bonus action to speak a second command word. A target restrained by the rope can use an action to make a DC 15 Strength or Dexterity check (target’s choice). On a success, the creature is no longer restrained by the rope.
The rope has AC 20 and 20 hit points. It regains 1 hit point every 5 minutes as long as it has at least 1 hit point. If the rope drops to 0 hit points, it is destroyed."
     }{
     name-key "Scarab of Protection"
     ::type :wondrous-item

     ::rarity :legendary

     ::attunement [:any]
     ::description "If you hold this beetle-shaped medallion in your hand for 1 round, an inscription appears on its surface revealing its magical nature. It provides two benefits while it is on your person:
• You have advantage on saving throws against spells.
• The scarab has 12 charges. If you fail a saving throw against a necromancy spell or a harmful effect originating from an undead creature, you can use your reaction to expend 1 charge and turn the failed save into a successful one. The scarab crumbles into powder and is destroyed when its last charge is expended."
     }{
     name-key "Scimitar of Speed"
     ::type :weapon
     ::item-subtype :scimitar

     ::rarity :very-rare

     ::attunement [:any]
     ::magical-attack-bonus 2
     ::magical-damage-bonus 2
     ::description "You gain a +2 bonus to attack and damage rolls made with this magic weapon. In addition, you can make one attack with it as a bonus action on each of your turns."
     }
    {
     name-key "Sending Stones"
     ::type :wondrous-item
     ::rarity :uncommon
     ::modifiers [(mod5e/action
                  {:name "Sending Stones"
                   :page 199
                   :source :dmg
                   :frequency units5e/days-1
                   :summary "Cast sending to communicate with the holder of the other stone"})]
     ::summary "Cast sending between stones"}
    {
     name-key "Shield +1"
     :name-fn (plus-1-name :name)
     ::type :armor
     ::item-subtype :shield
     ::rarity :uncommon
     ::magical-ac-bonus 1
     ::description "While holding this shield, you have a +1 bonus to AC. This bonus is in addition to the shield’s normal bonus to AC."
     }{
     name-key "Shield +2"
     :name-fn (plus-2-name :name)
     ::type :armor
     ::item-subtype :shield
     ::rarity :rare
     ::magical-ac-bonus 2
     ::description "While holding this shield, you have a +2 bonus to AC determined by the shield’s rarity."
     }{
     name-key "Shield +3"
     :name-fn (plus-3-name :name)
     ::type :armor
     ::item-subtype :shield
     ::rarity :very-rare
     ::magical-ac-bonus 3
     ::description "While holding this shield, you have a +3 bonus to AC. This bonus is in addition to the shield’s normal bonus to AC."
     }{
     name-key "Shield of Missile Attraction"
     ::type :armor
     ::item-subtype :shield

     ::rarity :rare

     ::attunement [:any]
     ::description "While holding this shield, you have resistance to damage from ranged weapon attacks.
Curse. This shield is cursed. Attuning to it curses you until you are targeted by the remove curse spell or similar magic. Removing the shield fails to end the curse on you. Whenever a ranged weapon attack is made against a target within 10 feet of you, the curse causes you to become the target instead."
     }{
     name-key "Slippers of Spider Climbing"
     ::type :wondrous-item

     ::rarity :uncommon

     ::attunement [:any]
     ::description "While you wear these light shoes, you can move up, down, and across vertical surfaces and upside down along ceilings, while leaving your hands free. You have a climbing speed equal to your walking speed. However, the slippers don’t allow you to move this way on a slippery surface, such as one covered by ice or oil."
     }{
     name-key "Sovereign Glue"
     ::type :wondrous-item
     ::rarity :legendary
     ::description "This viscous, milky-white substance can form a permanent adhesive bond between any two objects. It must be stored in a jar or flask that has been coated inside with oil of slipperiness. When found, a container contains 1d6 + 1 ounces.
One ounce of the glue can cover a 1-foot square surface. The glue takes 1 minute to set. Once it has done so, the bond it creates can be broken only by the application of universal solvent or oil of etherealness, or with a wish spell."
     }{
     name-key "Spell Scroll"

     ::type :scroll

     ::rarity :varies
     ::description "A spell scroll bears the words of a single spell, written in a mystical cipher. If the spell is on your class’s spell list, you can read the scroll and cast its spell without providing any material components. Otherwise, the scroll is unintelligible. Casting the spell by reading the scroll requires the spell’s normal casting time. Once the spell is cast, the words on the
scroll fade, and it crumbles to dust. If the casting is interrupted, the scroll is not lost.
If the spell is on your class’s spell list but of a higher level than you can normally cast, you must make an ability check using your spellcasting ability to determine whether you cast it successfully. The DC equals 10 + the spell’s level. On a failed check, the spell disappears from the scroll with no other effect.
The level of the spell on the scroll determines the spell’s saving throw DC and attack bonus, as well as the scroll’s rarity, as shown in the Spell Scroll table.
A wizard spell on a spell scroll can be copied just as spells in spellbooks can be copied. When a spell is copied from a spell scroll, the copier must succeed on an Intelligence (Arcana) check with a DC equal to 10 + the spell’s level. If the check succeeds, the spell is successfully copied. Whether the check succeeds or fails, the spell scroll is destroyed."
     }{
     name-key "Spellguard Shield"
     ::type :armor
     ::item-subtype :shield

     ::rarity :very-rare

     ::attunement [:any]
     ::description "While holding this shield, you have advantage on saving throws against spells and other magical effects, and spell attacks have disadvantage against you."
     }{
     name-key "Sphere of Annihilation"
     ::type :wondrous-item
     ::rarity :legendary
     ::description "This 2-foot-diameter black sphere is a hole in the multiverse, hovering in space and stabilized by a magical field surrounding it.
The sphere obliterates all matter it passes through and all matter that passes through it. Artifacts are the exception. Unless an artifact is susceptible to damage from a sphere of annihilation, it passes through the sphere unscathed. Anything else that touches the sphere but isn’t wholly engulfed and obliterated by it takes 4d10 force damage.
The sphere is stationary until someone controls it. If you are within 60 feet of an uncontrolled sphere, you can use an action to make a DC 25 Intelligence (Arcana) check. On a success, the sphere levitates in one direction of your choice, up to a number of feet equal to 5 × your Intelligence modifier (minimum 5 feet). On a failure, the sphere moves 10 feet toward you. A creature whose space the sphere enters must succeed on a DC 13 Dexterity saving throw or be touched by it, taking 4d10 force damage.
If you attempt to control a sphere that is under another creature’s control, you make an Intelligence (Arcana) check contested by the other creature’s Intelligence (Arcana) check. The winner of the contest gains control of the sphere and can levitate it as normal.
If the sphere comes into contact with a planar portal, such as that created by the gate spell, or an extradimensional space, such as that within a portable hole, the GM determines randomly what happens, using the following table."
     }{
     name-key "Staff of Charming"
     ::type :weapon
     ::item-subtype :staff
     ::rarity :rare

     ::attunement [:bard, :cleric, :druid, :sorcerer, :warlock, :wizard]
     ::description "While holding this staff, you can use an action to expend 1 of pits 10 charges to cast charm person, command, or comprehend languages from it using your spell save DC. The staff can also be used as a magic quarterstaff.
If you are holding the staff and fail a saving throw against an enchantment spell that targets only you, you can turn your failed save into a successful one. You can’t use this property of the staff again until the next dawn. If you succeed on a save against an enchantment spell that targets only you, with or without the staff’s intervention, you can use your reaction to expend 1 charge from the staff and turn the spell back on its caster as if you had cast the spell.
The staff regains 1d8 + 2 expended charges daily at dawn. If you expend the last charge, roll a d20. On a 1, the staff becomes a nonmagical quarterstaff."
     }{
     name-key "Staff of Fire"
     ::type :weapon
     ::item-subtype :staff
     ::rarity :very-rare

     ::attunement [:druid :sorcerer :warlock :wizard]
     ::modifiers [(mod5e/damage-resistance :fire)]
     ::description "You have resistance to fire damage while you hold this staff.
The staff has 10 charges. While holding it, you can use an action to expend 1 or more of its charges to cast one of the following spells from it, using your spell save DC: burning hands (1 charge), fireball (3 charges), or wall of fire (4 charges).
The staff regains 1d6 + 4 expended charges daily at dawn. If you expend the last charge, roll a d20. On a 1, the staff blackens, crumbles into cinders, and is destroyed."
     }{
     name-key "Staff of Frost"
     ::type :weapon
     ::item-subtype :staff
     ::rarity :very-rare

     ::attunement [:druid :sorcerer :warlock :wizard]
     ::modifiers [(mod5e/damage-resistance :cold)]
     ::description "You have resistance to cold damage while you hold this staff.
The staff has 10 charges. While holding it, you can use an action to expend 1 or more of its charges to cast one of the following spells from it, using your spell save DC: cone of cold (5 charges), fog cloud (1 charge), ice storm (4 charges), or wall of ice (4 charges).
The staff regains 1d6 + 4 expended charges daily at dawn. If you expend the last charge, roll a d20. On a 1, the staff turns to water and is destroyed."
     }{
     name-key "Staff of Healing"
     ::type :weapon
     ::item-subtype :staff
     ::rarity :rare

     ::attunement [:bard, :cleric, :druid]
     ::description "This staff has 10 charges. While holding it, you can use an action to expend 1 or more of its charges to cast one of the following spells from it, using your spell save DC and spellcasting ability modifier: cure wounds (1 charge per spell level, up to 4th), lesser restoration (2 charges), or mass cure wounds (5 charges).
The staff regains 1d6 + 4 expended charges daily at dawn. If you expend the last charge, roll a d20. On a 1, the staff vanishes in a flash of light, lost forever."
     }{
     name-key "Staff of Power"
     ::type :weapon
     ::item-subtype :staff
     ::magical-attack-bonus 2
     ::magical-damage-bonus 2
     ::magical-ac-bonus 2
     ::rarity :very-rare

     ::attunement [:sorcerer, :warlock, :wizard]
     ::modifiers [(mod5e/ac-bonus-fn (fn [_ _] 2))
                  (mod5e/spell-attack-modifier-bonus 2)
                  (mod5e/saving-throw-bonuses 2)]
     ::description "This staff can be wielded as a magic quarterstaff that grants a +2 bonus to attack and damage rolls made with it. While holding it, you gain a +2 bonus to Armor Class, saving throws, and spell attack rolls.
The staff has 20 charges for the following properties. The staff regains 2d8 + 4 expended charges daily at dawn. If you expend the last charge, roll a d20. On a 1, the staff retains its +2 bonus to attack and damage rolls but loses all other properties. On a 20, the staff regains 1d8 + 2 charges.
Power Strike. When you hit with a melee attack using the staff, you can expend 1 charge to deal an extra 1d6 force damage to the target.
Spells. While holding this staff, you can use an action to expend 1 or more of its charges to cast one of the following spells from it, using your spell save DC and spell attack bonus: cone of cold (5 charges), fireball (5th-level version, 5 charges), globe of invulnerability (6 charges), hold monster (5 charges), levitate (2 charges), lightning bolt (5th-level version, 5 charges), magic missile (1 charge), ray of enfeeblement (1 charge), or wall of force (5 charges).
Retributive Strike. You can use an action to break the staff over your knee or against a solid surface, performing a retributive strike. The staff is destroyed and releases its remaining magic in an explosion that expands to fill a 30-foot-radius sphere centered on it.
You have a 50 percent chance to instantly travel to a random plane of existence, avoiding the explosion. If you fail to avoid the effect, you take force damage equal to 16 × the number of charges in the staff. Every other creature in the area must make a DC 17 Dexterity saving throw. On a failed save, a creature takes an amount of damage based on how far away it is from the point of origin, as shown in the following table. On a successful save, a creature takes half as much damage."
     }{
     name-key "Staff of Striking"
       ::type :weapon
       ::item-subtype :staff
       ::rarity :very-rare

       ::attunement [:any]
       ::magical-attack-bonus 3
       ::magical-damage-bonus 3
       ::description "This staff can be wielded as a magic quarterstaff that grants a +3 bonus to attack and damage rolls made with it.
The staff has 10 charges. When you hit with a melee attack using it, you can expend up to 3 of its charges. For each charge you expend, the target takes an extra 1d6 force damage. The staff regains 1d6 + 4 expended charges daily at dawn. If you expend the last charge, roll a d20. On a 1, the staff becomes a nonmagical quarterstaff."
     }{
     name-key "Staff of Swarming Insects"
     ::type :weapon
     ::item-subtype :staff
     ::rarity :rare
     ::attunement [:bard, :cleric, :druid, :sorcerer, :warlock, :wizard]

     ::description "This staff has 10 charges and regains 1d6 + 4 expended charges daily at dawn. If you expend the last charge, roll a d20. On a 1, a swarm of insects consumes and destroys the staff, then disperses.
Spells. While holding the staff, you can use an action to expend some of its charges to cast one of the following spells from it, using your spell save DC: giant insect (4 charges) or insect plague (5 charges).
Insect Cloud. While holding the staff, you can use an action and expend 1 charge to cause a swarm of harmless flying insects to spread out in a 30-foot radius from you. The insects remain for 10 minutes, making the area heavily obscured for creatures other than you. The swarm moves with you, remaining centered on you. A wind of at least 10 miles per hour disperses the swarm and ends the effect."
     }{
     name-key "Staff of the Magi"
     ::type :weapon
     ::item-subtype :staff
     ::rarity :legendary

     ::attunement [:sorcerer, :warlock, :wizard]
     ::magical-attack-bonus 2
     ::magical-damage-bonus 2
     ::modifiers [(mod5e/spell-attack-modifier-bonus 2)
                  (mod5e/saving-throw-advantage ["spells"])
                  (mod5e/reaction
                   {:name "Staff of the Magi"
                    :page 203
                    :source :dmg
                    :frequency units5e/long-rests-1
                    :summary "Absorb spell cast by another creature, targetting only you. Cancel its effect and gain charges equal to absorbed spell's level. Staff explodes, as per Retributive Strike, if brought over 50 charges."})]
     ::description "This staff can be wielded as a magic quarterstaff that grants a +2 bonus to attack and damage rolls made with it. While you hold it, you gain a +2 bonus to spell attack rolls.
The staff has 50 charges for the following properties. It regains 4d6 + 2 expended charges daily at dawn. If you expend the last charge, roll a d20. On a 20, the staff regains 1d12 + 1 charges.
Spell Absorption. While holding the staff, you have advantage on saving throws against spells. In addition, you can use your reaction when another creature casts a spell that targets only you. If you do, the staff absorbs the magic of the spell, canceling its effect and gaining a number of charges equal to the absorbed spell’s level. However, if doing so brings the staff’s total number of charges above 50, the staff explodes as if you activated its retributive strike (see below).
Spells. While holding the staff, you can use an action to expend some of its charges to cast one of the following spells from it, using your spell save DC and spellcasting ability: conjure elemental (7 charges), dispel magic (3 charges), fireball (7th-level version, 7 charges), flaming sphere (2 charges), ice storm (4 charges), invisibility (2 charges), knock (2 charges), lightning bolt (7th-level version, 7 charges), passwall (5 charges), plane shift (7 charges), telekinesis (5 charges), wall of fire (4 charges), or web (2 charges).
You can also use an action to cast one of the following spells from the staff without using any charges: arcane lock, detect magic, enlarge/reduce, light, mage hand, or protection from evil and good.
Retributive Strike. You can use an action to break the staff over your knee or against a solid surface, performing a retributive strike. The staff is destroyed and releases its remaining magic in an explosion that expands to fill a 30-foot-radius sphere centered on it.
You have a 50 percent chance to instantly travel to a random plane of existence, avoiding the explosion. If you fail to avoid the effect, you take force damage equal to 16 × the number of charges in the staff.
Every other creature in the area must make a DC 17 Dexterity saving throw. On a failed save, a creature takes an amount of damage based on how far away it is from the point of origin, as shown in the following table. On a successful save, a creature takes half as much damage."
     }{
     name-key "Staff of the Python"
     ::type :weapon
     ::item-subtype :staff
     ::rarity :uncommon

     ::attunement [:cleric, :druid, :warlock]
     ::description "You can use an action to speak this staff’s command word and throw the staff on the ground within 10 feet of you. The staff becomes a giant constrictor snake under your control and acts on its own initiative count. By using a bonus action to speak the command word again, you return the staff to its normal form in a space formerly occupied by the snake.
On your turn, you can mentally command the snake if it is within 60 feet of you and you aren’t incapacitated. You decide what action the snake takes and where it moves during its next turn, or you can issue it a general command, such as to attack your enemies or guard a location.
If the snake is reduced to 0 hit points, it dies and reverts to its staff form. The staff then shatters and is destroyed. If the snake reverts to staff form before losing all its hit points, it regains all of them."
     }{
     name-key "Staff of the Woodlands"
     ::type :weapon
     ::item-subtype :staff
     ::rarity :rare

     ::attunement [:druid]
     ::magical-attack-bonus 2
     ::magical-damage-bonus 2
     ::modifiers [(mod5e/spell-attack-modifier-bonus 2)]
     ::description "This staff can be wielded as a magic quarterstaff that grants a +2 bonus to attack and damage rolls made with it. While holding it, you have a +2 bonus to spell attack rolls.
The staff has 10 charges for the following properties. It regains 1d6 + 4 expended charges daily at dawn. If you expend the last charge, roll a d20. On a 1, the staff loses its properties and becomes a nonmagical quarterstaff.
Spells. You can use an action to expend 1 or more of the staff’s charges to cast one of the following spells from it, using your spell save DC: animal friendship (1 charge), awaken (5 charges), barkskin
(2 charges), locate animals or plants (2 charges), speak with animals (1 charge), speak with plants (3 charges), or wall of thorns (6 charges).
You can also use an action to cast the pass without trace spell from the staff without using any charges.
Tree Form. You can use an action to plant one end of the staff in fertile earth and expend 1 charge to transform the staff into a healthy tree. The tree is 60 feet tall and has a 5-foot-diameter trunk, and its branches at the top spread out in a 20-foot radius. The tree appears ordinary but radiates a faint aura of transmutation magic if targeted by detect magic. While touching the tree and using another action to speak its command word, you return the staff to its normal form. Any creature in the tree falls when it reverts to a staff."
     }{
     name-key "Staff of Thunder and Lightning"
     ::type :weapon
     ::item-subtype :staff

     ::rarity :very-rare

     ::attunement [:any]
     ::magical-attack-bonus 2
     ::magical-damage-bonus 2
     ::description "This staff can be wielded as a magic quarterstaff that grants a +2 bonus to attack and damage rolls made with it. It also has the following additional properties. When one of these properties is used, it can’t be used again until the next dawn.
Lightning. When you hit with a melee attack using the staff, you can cause the target to take an extra 2d6 lightning damage.
Thunder. When you hit with a melee attack using the staff, you can cause the staff to emit a crack of thunder, audible out to 300 feet. The target you hit must succeed on a DC 17 Constitution saving throw or become stunned until the end of your next turn.
Lightning Strike. You can use an action to cause a bolt of lightning to leap from the staff’s tip in a line that is 5 feet wide and 120 feet long. Each creature in that line must make a DC 17 Dexterity saving throw, taking 9d6 lightning damage on a failed save, or half as much damage on a successful one.
Thunderclap. You can use an action to cause the staff to issue a deafening thunderclap, audible out to 600 feet. Each creature within 60 feet of you (not including you) must make a DC 17 Constitution saving throw. On a failed save, a creature takes 2d6 thunder damage and becomes deafened for 1 minute. On a successful save, a creature takes half damage and isn’t deafened.
Thunder and Lightning. You can use an action to use the Lightning Strike and Thunderclap properties at the same time. Doing so doesn’t expend the daily use of those properties, only the use of this one."
     }{
     name-key "Staff of Withering"
     ::type :weapon
     ::item-subtype :staff
     ::rarity :rare

     ::attunement [:cleric, :druid, :warlock]
     ::description "This staff has 3 charges and regains 1d3 expended charges daily at dawn.
The staff can be wielded as a magic quarterstaff. On a hit, it deals damage as a normal quarterstaff, and you can expend 1 charge to deal an extra 2d10 necrotic damage to the target. In addition, the target must succeed on a DC 15 Constitution saving throw or have disadvantage for 1 hour on any ability check or saving throw that uses Strength or Constitution."
     }{
     name-key "Stone of Controlling Earth Elementals"
     ::type :wondrous-item
     ::rarity :rare
     ::description "If the stone is touching the ground, you can use an action to speak its command word and summon an earth elemental, as if you had cast the conjure elemental spell. The stone can’t be used this way again until the next dawn. The stone weighs 5 pounds."
     }{
     name-key "Stone of Good Luck (Luckstone)"
     ::type :wondrous-item

     ::rarity :uncommon

       ::attunement [:any]
       ::modifiers [(mod5e/all-skills-bonus 1)
                   (mod5e/saving-throw-bonuses 1)]
     ::description "While this polished agate is on your person, you gain a +1 bonus to ability checks and saving throws."
     }{
     name-key "Sun Blade"
     ::type :weapon
     ::item-subtype sword?

     ::rarity :rare

     ::attunement [:any]
     ::magical-attack-bonus 2
     ::magical-damage-bonus 2
     ::magical-damage-type :radiant
     ::magical-finesse? true

     ::description "This item appears to be a longsword hilt. While grasping the hilt, you can use a bonus action to cause a blade of pure radiance to spring into existence, or make the blade disappear. While the blade exists, this magic longsword has the finesse property. If you are proficient with shortswords or longswords, you are proficient with the sun blade.
You gain a +2 bonus to attack and damage rolls made with this weapon, which deals radiant damage instead of slashing damage. When you hit an undead with it, that target takes an extra 1d8 radiant damage.
The sword’s luminous blade emits bright light in a 15-foot radius and dim light for an additional 15 feet. The light is sunlight. While the blade persists, you can use an action to expand or reduce its radius of bright and dim light by 5 feet each, to a maximum of 30 feet each or a minimum of 10 feet each."
     }{
     name-key "Sword of Life Stealing"
     ::type :weapon
     ::item-subtype sword?

     ::rarity :rare

     ::attunement [:any]
     ::description "When you attack a creature with this magic weapon and roll a 20 on the attack roll, that target takes an extra 3d6 necrotic damage, provided that the target isn’t a construct or an undead. You gain temporary hit points equal to the extra damage dealt."
     }{
     name-key "Sword of Sharpness"
     ::type :weapon
     ::item-subtype slashing-sword?
     ::rarity :very-rare

     ::attunement [:any]
     ::description "When you attack an object with this magic sword and hit, maximize your weapon damage dice against the target.
When you attack a creature with this weapon and roll a 20 on the attack roll, that target takes an extra 4d6 slashing damage. Then roll another d20. If you roll a 20, you lop off one of the target’s limbs, with the effect of such loss determined by the GM. If the creature has no limb to sever, you lop off a portion of its body instead.
In addition, you can speak the sword’s command word to cause the blade to shed bright light in a 10-
foot radius and dim light for an additional 10 feet. Speaking the command word again or sheathing the sword puts out the light."
     }{
     name-key "Sword of Wounding"
     ::type :weapon
     ::item-subtype sword?
     ::rarity :rare
     ::attunement [:any]
     ::description "Hit points lost to this weapon’s damage can be regained only through a short or long rest, rather than by regeneration, magic, or any other means.
Once per turn, when you hit a creature with an attack using this magic weapon, you can wound the target. At the start of each of the wounded creature’s turns, it takes 1d4 necrotic damage for each time you’ve wounded it, and it can then make a DC 15 Constitution saving throw, ending the effect of all such wounds on itself on a success. Alternatively, the wounded creature, or a creature within 5 feet of it, can use an action to make a DC 15 Wisdom (Medicine) check, ending the effect of such wounds on it on a success."
     }{
     name-key "Talisman of Pure Good"
       ::type :wondrous-item

       ::rarity :legendary

       ::attunement [:good]
       ::description "This talisman is a mighty symbol of goodness. A creature that is neither good nor evil in alignment takes 6d6 radiant damage upon touching the talisman. An evil creature takes 8d6 radiant damage upon touching the talisman. Either sort of creature takes the damage again each time it ends its turn holding or carrying the talisman.
If you are a good cleric or paladin, you can use the talisman as a holy symbol, and you gain a +2 bonus to spell attack rolls while you wear or hold it.
The talisman has 7 charges. If you are wearing or holding it, you can use an action to expend 1 charge from it and choose one creature you can see on the ground within 120 feet of you. If the target is of evil alignment, a flaming fissure opens under it. The target must succeed on a DC 20 Dexterity saving throw or fall into the fissure and be destroyed, leaving no remains. The fissure then closes, leaving no trace of its existence. When you expend the last charge, the talisman disperses into motes of golden light and is destroyed."
     }{
     name-key "Talisman of the Sphere"
     ::type :wondrous-item

     ::rarity :legendary

     ::attunement [:any]
     ::description "When you make an Intelligence (Arcana) check to control a sphere of annihilation while you are holding this talisman, you double your proficiency bonus on the check. In addition, when you start your turn with control over a sphere of annihilation, you can use an action to levitate it 10 feet plus a number of additional feet equal to 10 × your Intelligence modifier."
     }{
     name-key "Talisman of Ultimate Evil"
     ::type :wondrous-item

     ::rarity :legendary

     ::attunement [:evil]
     ::description "This item symbolizes unrepentant evil. A creature that is neither good nor evil in alignment takes 6d6 necrotic damage upon touching the talisman. A good creature takes 8d6 necrotic damage upon touching the talisman. Either sort of creature takes the damage again each time it ends its turn holding or carrying the talisman.
If you are an evil cleric or paladin, you can use the talisman as a holy symbol, and you gain a +2 bonus to spell attack rolls while you wear or hold it.
The talisman has 6 charges. If you are wearing or holding it, you can use an action to expend 1 charge from the talisman and choose one creature you can see on the ground within 120 feet of you. If the target is of good alignment, a flaming fissure opens under it. The target must succeed on a DC 20 Dexterity saving throw or fall into the fissure and be destroyed, leaving no remains. The fissure then closes, leaving no trace of its existence. When you expend the last charge, the talisman dissolves into foul-smelling slime and is destroyed."
     }{
     name-key "Tome of Clear Thought"
     ::type :wondrous-item
     ::rarity :very-rare
     ::description "This book contains memory and logic exercises, and its words are charged with magic. If you spend 48 hours over a period of 6 days or fewer studying the book’s contents and practicing its guidelines, your Intelligence score increases by 2, as does your maximum for that score. The manual then loses its magic, but regains it in a century. (this doesn't apply modifiers automatically, once you have read the Tome remove this one and add the read version)"
     }{
     name-key "Tome of Clear Thought (read with modifiers)"
     ::type :wondrous-item
     ::rarity :very-rare
     ::description "This book contains memory and logic exercises, and its words are charged with magic. If you spend 48 hours over a period of 6 days or fewer studying the book’s contents and practicing its guidelines, your Intelligence score increases by 2, as does your maximum for that score. The manual then loses its magic, but regains it in a century."
     ::modifiers [(mod5e/ability ::char5e/int 2)]
     }{
     name-key "Tome of Leadership and Influence"
     ::type :wondrous-item
     ::rarity :very-rare
     ::description "This book contains guidelines for influencing and charming others, and its words are charged with magic. If you spend 48 hours over a period of 6 days or fewer studying the book’s contents and practicing its guidelines, your Charisma score increases by 2, as does your maximum for that score. The manual then loses its magic, but regains it in a century. (this doesn't apply modifiers automatically, once you have read the Tome remove this one and add the read version)"
     }{
       name-key "Tome of Leadership and Influence (read with modifiers)"
       ::type :wondrous-item
       ::rarity :very-rare
       ::description "This book contains guidelines for influencing and charming others, and its words are charged with magic. If you spend 48 hours over a period of 6 days or fewer studying the book’s contents and practicing its guidelines, your Charisma score increases by 2, as does your maximum for that score. The manual then loses its magic, but regains it in a century."
       ::modifiers [(mod5e/ability ::char5e/cha 2)]
       }{
     name-key "Tome of Understanding"
     ::type :wondrous-item
     ::rarity :very-rare
     ::description "This book contains intuition and insight exercises, and its words are charged with magic. If you spend 48 hours over a period of 6 days or fewer studying the book’s contents and practicing its guidelines, your Wisdom score increases by 2, as does your maximum for that score. The manual then loses its magic, but regains it in a century. (this doesn't apply modifiers automatically, once you have read the Tome remove this one and add the read version)"
     }{
       name-key "Tome of Understanding (read with modifiers)"
       ::type :wondrous-item
       ::rarity :very-rare
       ::description "This book contains intuition and insight exercises, and its words are charged with magic. If you spend 48 hours over a period of 6 days or fewer studying the book’s contents and practicing its guidelines, your Wisdom score increases by 2, as does your maximum for that score. The manual then loses its magic, but regains it in a century."
       ::modifiers [(mod5e/ability ::char5e/wis 2)]
       }{
     name-key "Trident of Fish Command"
     ::type :weapon
     ::item-subtype :trident
     ::rarity :uncommon
     ::attunement [:any]
     ::description "This trident is a magic weapon. It has 3 charges. While you carry it, you can use an action and expend 1 charge to cast dominate beast (save DC 15) from it
on a beast that has an innate swimming speed. The trident regains 1d3 expended charges daily at dawn."
     }{
     name-key "Universal Solvent"
     ::type :wondrous-item
     ::rarity :legendary
     ::description "This tube holds milky liquid with a strong alcohol smell. You can use an action to pour the contents of the tube onto a surface within reach. The liquid instantly dissolves up to 1 square foot of adhesive it touches, including sovereign glue."
     }{
     name-key "Vicious Weapon"
     ::type :weapon
     ::item-subtype weapon-not-ammunition?
     ::rarity :rare
     ::description "When you roll a 20 on your attack roll with this magic weapon, your critical hit deals an extra 2d6 damage of the weapon’s type."
     }{
     name-key "Vorpal Sword"
     ::type :weapon
     ::item-subtype slashing-sword?
     ::rarity :legendary
     ::attunement [:any]
     ::magical-attack-bonus 3
     ::magical-damage-bonus 3
     ::description "You gain a +3 bonus to attack and damage rolls made with this magic weapon. In addition, the weapon ignores resistance to slashing damage.
When you attack a creature that has at least one head with this weapon and roll a 20 on the attack roll, you cut off one of the creature’s heads. The creature dies if it can’t survive without the lost head. A creature is immune to this effect if it is immune to slashing damage, doesn’t have or need a head, has legendary actions, or the GM decides that the creature is too big for its head to be cut off with this weapon. Such a creature instead takes an extra 6d8 slashing damage from the hit."
     }{
     name-key "Wand of Binding"
     ::type :wand

     ::rarity :rare

     ::attunement [:spellcaster]
     ::description "This wand has 7 charges for the following properties. It regains 1d6 + 1 expended charges daily at dawn. If you expend the wand’s last charge, roll a d20. On a 1, the wand crumbles into ashes and is destroyed.
Spells. While holding the wand, you can use an action to expend some of its charges to cast one of the following spells (save DC 17): hold monster (5 charges) or hold person (2 charges).
Assisted Escape. While holding the wand, you can use your reaction to expend 1 charge and gain advantage on a saving throw you make to avoid being paralyzed or restrained, or you can expend 1 charge and gain advantage on any check you make to escape a grapple."
     }{
     name-key "Wand of Enemy Detection"
     ::type :wand

     ::rarity :rare

     ::attunement [:any]
     ::description "This wand has 7 charges. While holding it, you can use an action and expend 1 charge to speak its command word. For the next minute, you know the direction of the nearest creature hostile to you within 60 feet, but not its distance from you. The wand can sense the presence of hostile creatures that are ethereal, invisible, disguised, or hidden, as well as those in plain sight. The effect ends if you stop holding the wand.
The wand regains 1d6 + 1 expended charges daily at dawn. If you expend the wand’s last charge, roll a d20. On a 1, the wand crumbles into ashes and is destroyed."
     }{
     name-key "Wand of Fear"
     ::type :wand

     ::rarity :rare

     ::attunement [:any]
     ::description "This wand has 7 charges for the following properties. It regains 1d6 + 1 expended charges daily at dawn. If you expend the wand’s last charge, roll a d20. On a 1, the wand crumbles into ashes and is destroyed.
Command. While holding the wand, you can use an action to expend 1 charge and command another creature to flee or grovel, as with the command spell (save DC 15).
Cone of Fear. While holding the wand, you can use an action to expend 2 charges, causing the wand’s tip to emit a 60-foot cone of amber light. Each creature in the cone must succeed on a DC 15 Wisdom saving throw or become frightened of you for 1 minute. While it is frightened in this way, a creature must spend its turns trying to move as far away from you as it can, and it can’t willingly move to a space within 30 feet of you. It also can’t take reactions. For its action, it can use only the Dash action or try to escape from an effect that prevents it from moving. If it has nowhere it can move, the creature can use the Dodge action. At the end of each of its turns, a creature can repeat the saving throw, ending the effect on itself on a success."
     }{
     name-key "Wand of Fireballs"
     ::type :wand

     ::rarity :rare

     ::attunement [:spellcaster]
     ::description "This wand has 7 charges. While holding it, you can use an action to expend 1 or more of its charges to cast the fireball spell (save DC 15) from it. For 1 charge, you cast the 3rd-level version of the spell. You can increase the spell slot level by one for each additional charge you expend.
The wand regains 1d6 + 1 expended charges daily at dawn. If you expend the wand’s last charge, roll a
d20. On a 1, the wand crumbles into ashes and is destroyed."
     }{
     name-key "Wand of Lightning Bolts"
     ::type :wand

     ::rarity :rare

     ::attunement [:spellcaster]
     ::description "This wand has 7 charges. While holding it, you can use an action to expend 1 or more of its charges to cast the lightning bolt spell (save DC 15) from it. For 1 charge, you cast the 3rd-level version of the spell. You can increase the spell slot level by one for each additional charge you expend.
The wand regains 1d6 + 1 expended charges daily at dawn. If you expend the wand’s last charge, roll a d20. On a 1, the wand crumbles into ashes and is destroyed."
     }{
     name-key "Wand of Magic Detection"
     ::type :wand
     ::rarity :uncommon
     ::description "This wand has 3 charges. While holding it, you can expend 1 charge as an action to cast the detect magic
spell from it. The wand regains 1d3 expended charges daily at dawn."
     }{
     name-key "Wand of Magic Missiles"
     ::type :wand
     ::rarity :uncommon
     ::description "This wand has 7 charges. While holding it, you can use an action to expend 1 or more of its charges to cast the magic missile spell from it. For 1 charge, you cast the 1st-level version of the spell. You can increase the spell slot level by one for each additional charge you expend.
The wand regains 1d6 + 1 expended charges daily at dawn. If you expend the wand’s last charge, roll a d20. On a 1, the wand crumbles into ashes and is destroyed."
     }{
     name-key "Wand of Paralysis"
     ::type :wand

     ::rarity :rare


     ::attunement [:spellcaster]
     ::description "This wand has 7 charges. While holding it, you can use an action to expend 1 of its charges to cause a thin blue ray to streak from the tip toward a creature you can see within 60 feet of you. The target must succeed on a DC 15 Constitution saving throw or be paralyzed for 1 minute. At the end of each of the target’s turns, it can repeat the saving throw, ending the effect on itself on a success.
The wand regains 1d6 + 1 expended charges daily at dawn. If you expend the wand’s last charge, roll a d20. On a 1, the wand crumbles into ashes and is destroyed."
     }{
     name-key "Wand of Polymorph"
     ::type :wand
     ::rarity :very-rare
     ::attunement [:spellcaster]
     ::description "This wand has 7 charges. While holding it, you can use an action to expend 1 of its charges to cast the polymorph spell (save DC 15) from it.
The wand regains 1d6 + 1 expended charges daily at dawn. If you expend the wand’s last charge, roll a d20. On a 1, the wand crumbles into ashes and is destroyed."
     }{
     name-key "Wand of Secrets"
     ::type :wand
     ::rarity :uncommon
     ::description "The wand has 3 charges. While holding it, you can use an action to expend 1 of its charges, and if a secret door or trap is within 30 feet of you, the wand pulses and points at the one nearest to you. The wand regains 1d3 expended charges daily at dawn."
     }
     (caster-bonus-item "Wand of the War Mage" 1 :wand :rare [:spellcaster]
                        [:sp-atk-mod]
                        "While holding this wand, you gain a +1 bonus to spell attack rolls. In addition, you ignore half cover when making a spell attack.")
     (caster-bonus-item "Wand of the War Mage" 2 :wand :rare [:spellcaster]
                        [:sp-atk-mod]
                        "While holding this wand, you gain a +2 bonus to spell attack rolls. In addition, you ignore half cover when making a spell attack.")
     (caster-bonus-item "Wand of the War Mage" 3 :wand :rare [:spellcaster]
                        [:sp-atk-mod]
                        "While holding this wand, you gain a +3 bonus to spell attack rolls. In addition, you ignore half cover when making a spell attack.")
     {
     name-key "Wand of Web"
     ::type :wand
     ::rarity :uncommon
     ::attunement [:spellcaster]
     ::description "This wand has 7 charges. While holding it, you can use an action to expend 1 of its charges to cast the web spell (save DC 15) from it.
The wand regains 1d6 + 1 expended charges daily at dawn. If you expend the wand’s last charge, roll a d20. On a 1, the wand crumbles into ashes and is destroyed."
     }{
     name-key "Wand of Wonder"
     ::type :wand
     ::rarity :rare
     ::attunement [:spellcaster]
     ::description "This wand has 7 charges. While holding it, you can use an action to expend 1 of its charges and choose a target within 120 feet of you. The target can be a creature, an object, or a point in space. Roll d100 and consult the following table to discover what happens.
If the effect causes you to cast a spell from the wand, the spell’s save DC is 15. If the spell normally has a range expressed in feet, its range becomes 120 feet if it isn’t already.
If an effect covers an area, you must center the spell on and include the target. If an effect has multiple possible subjects, the GM randomly determines which ones are affected.
The wand regains 1d6 + 1 expended charges daily at dawn. If you expend the wand’s last charge, roll a d20. On a 1, the wand crumbles into dust and is destroyed."
     }{
     name-key "Weapon, +1"
     :name-fn (plus-1-name :name)
     ::type :weapon
     ::item-subtype weapon-not-ammunition?
     ::rarity :uncommon
     ::magical-attack-bonus 1
     ::magical-damage-bonus 1
     ::description "You have a +1 bonus to attack and damage rolls made with this magic weapon."
     }{
     name-key "Weapon, +2"
     :name-fn (plus-2-name :name)
     ::type :weapon
     ::item-subtype weapon-not-ammunition?
     ::rarity :rare
     ::magical-attack-bonus 2
     ::magical-damage-bonus 2
     ::description "You have a +2 bonus to attack and damage rolls made with this magic weapon."
     }{
     name-key "Weapon, +3"
     :name-fn (plus-3-name :name)
     ::type :weapon
     ::item-subtype weapon-not-ammunition?
     ::rarity :very-rare
     ::magical-attack-bonus 3
     ::magical-damage-bonus 3
     ::description "You have a +3 bonus to attack and damage rolls made with this magic weapon."
     }
    {name-key "Weapon of Warning"
     ::type :weapon
     ::item-subtype weapon-not-ammunition?
     ::rarity :uncommon
     ::attunement [:any]
     ::description "Advantage on initiative; you and companions in 30 ft. can't be surprised"}
    {
     name-key "Well of Many Worlds"
     ::type :wondrous-item
     ::rarity :legendary
     ::description "This fine black cloth, soft as silk, is folded up to the dimensions of a handkerchief. It unfolds into a circular sheet 6 feet in diameter.
You can use an action to unfold and place the well of many worlds on a solid surface, whereupon it creates a two-way portal to another world or plane of existence. Each time the item opens a portal, the GM decides where it leads. You can use an action to close an open portal by taking hold of the edges of the cloth and folding it up. Once well of many worlds
has opened a portal, it can’t do so again for 1d8 hours."
     }{
     name-key "Wind Fan"
     ::type :wondrous-item

     ::rarity :uncommon
     ::description "While holding this fan, you can use an action to cast the gust of wind spell (save DC 13) from it. Once used, the fan shouldn’t be used again until the next dawn. Each time it is used again before then, it has a cumulative 20 percent chance of not working and tearing into useless, nonmagical tatters."
     }{
     name-key "Winged Boots"
     ::type :wondrous-item
     ::rarity :uncommon
     ::attunement [:any]
     ::description "While you wear these boots, you have a flying speed equal to your walking speed. You can use the boots to fly for up to 4 hours, all at once or in several shorter flights, each one using a minimum of 1 minute from the duration. If you are flying when the duration expires, you descend at a rate of 30 feet per round until you land.
The boots regain 2 hours of flying capability for every 12 hours they aren’t in use."
     }{
     name-key "Wings of Flying"
     ::type :wondrous-item
     ::rarity :rare
     ::attunement [:any]
     ::description "While wearing this cloak, you can use an action to speak its command word. This turns the cloak into a pair of bat wings or bird wings on your back for 1 hour or until you repeat the command word as an action. The wings give you a flying speed of 60 feet. When they disappear, you can’t use them again for 1d12 hours."
     }]))

(def weapons-and-ammunition
  (concat
   weapons5e/weapons
   weapons5e/ammunition))

(defn add-key [item]
  (assoc item :key (common/name-to-kw (name-key item))))

(def weapon-subtypes
  #{:axe :sword :staff})

(defn any-fn [item]
  true)

(defn types-fn [types]
  (fn [{:keys [type]}]
    (types type)))

(defn subtypes-fn [subtypes]
  (fn [{:keys [::weapons5e/subtype]}]
    (subtypes subtype)))

(defn keys-fn [keys]
  (fn [item]
    (keys (:key item))))

(defn make-base-weapon-fn [item-subtype subtypes]
  (let [subtypes-set (into #{}
                           (if (and item-subtype (not (fn? item-subtype)))
                               (conj subtypes item-subtype)
                               subtypes))
        type-intersection (intersection subtypes-set weapon-subtypes)
        diff (difference subtypes-set weapon-subtypes)]
    (if (subtypes-set :all)
      any-fn
      (apply
       some-fn
       (cond-> []
         (fn? item-subtype) (conj item-subtype)
         (seq type-intersection) (conj (subtypes-fn type-intersection))
         (seq diff) (conj (keys-fn diff)))))))

(defn expand-weapon [{:keys [::item-subtype name-fn ::subtypes] :as item}]
  (if (or name-fn
          item-subtype
          (and (seq subtypes)
               (not ((set subtypes) :other))))
    (let [base-weapon-fn (make-base-weapon-fn item-subtype subtypes)
          of-type (filter base-weapon-fn (concat weapons5e/weapons
                                                 weapons5e/ammunition))]
      #?(:clj (if (empty? of-type)
                 (throw (IllegalArgumentException. (str "No base types matched for weapon item!: " (::name item))))))
      (map
       (fn [weapon]
         (let [name (if name-fn
                      (name-fn weapon)
                      (if (> (count of-type) 1)
                        (str (name-key item) ", " (:name weapon))
                        (name-key item)))
               item-key (common/name-to-kw name)]
           (merge
            weapon
            item
            {name-key (name-key item)
             :name name
             :base-key (:key weapon)
             :key item-key})))
       of-type))
    (add-key item)))

(def armor-types
  #{:light :medium :heavy :shield})

(defn make-base-armor-fn [item-subtype subtypes]
  (let [subtypes-set (into #{}
                           (if (and item-subtype (not (fn? item-subtype)))
                               (conj subtypes item-subtype)
                               subtypes))
        type-intersection (intersection subtypes-set armor-types)
        diff (difference subtypes-set armor-types)]
    (if (subtypes-set :all)
      any-fn
      (apply
       some-fn
       (cond-> []
         (fn? item-subtype) (conj item-subtype)
         (seq type-intersection) (conj (types-fn type-intersection))
         (seq diff) (conj (keys-fn diff)))))))

(defn expand-armor [{:keys [::item-subtype name-fn ::subtypes] :as item}]
  (if (or name-fn
          item-subtype
          (seq subtypes))
    (let [base-armor-fn (make-base-armor-fn item-subtype subtypes)
          of-type (filter
                   base-armor-fn
                   armor5e/armor)]
      #?(:clj (if (empty? of-type)
                 (throw (IllegalArgumentException. "No base types matched for armor item!"))))
      (map
       (fn [armor]
         (let [name (if (> (count of-type) 1)
                      (if name-fn
                        (name-fn armor)
                        (str (name-key item) ", " (:name armor)))
                      (name-key item))
               item-key (common/name-to-kw name)]
           (merge
            armor
            item
            {name-key (name-key item)
             :name name
             :base-armor (:key armor)
             :key item-key})))
       of-type))
    (add-key item)))

(defn expand-magic-items [magic-items]
  (flatten
   (map
    (fn [{:keys [::type] :as item}]
      (case type
        :weapon (expand-weapon item)
        :armor (expand-armor item)
        (add-key item)))
    magic-items)))

(def magic-items
  (expand-magic-items raw-magic-items))

(def magic-item-map
  (into {} (map (fn [i] [(:key i) i])) magic-items))

(def magic-weapon-xform
  (filter
   #(= :weapon (::type %))))

(def magic-weapons
  (sequence
   magic-weapon-xform
   magic-items))

(def magic-weapon-map
  (common/map-by-key magic-weapons))

(def all-weapons-map
  (merge
   weapons5e/weapons-map
   magic-weapon-map))

(def magic-armor-xform
  (filter
   #(= :armor (::type %))))

(def magic-armor
  (sequence
   magic-armor-xform
   magic-items))

(def magic-armor-map
  (common/map-by-key magic-armor))

(def all-armor-map
  (merge
   armor5e/armor-map
   magic-armor-map))

(def other-magic-items-xform
  (remove
   #(#{:armor :weapon} (::type %))))

(def other-magic-items
  (sequence
   other-magic-items-xform
   magic-items))

(def other-magic-item-map
  (common/map-by-key other-magic-items))

(def all-magic-items-map
  (merge
   magic-armor-map
   magic-weapon-map
   other-magic-item-map))

(def all-equipment-map
  (merge
   equip5e/equipment-map
   equip5e/treasure-map
   other-magic-item-map
   all-armor-map
   all-weapons-map))

(defn equipped-items-details [items item-map]
  (filter
   ::char-equip5e/equipped?
   (map
    (fn [[item-kw cfg]]
      (merge
       cfg
       (item-map item-kw)))
    items)))

(defn equipped-armor-details [armor]
  (equipped-items-details armor all-armor-map))

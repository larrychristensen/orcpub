(ns orcpub.dnd.e5.modifiers
  (:require [clojure.spec :as spec]
            [clojure.string :as s]
            [orcpub.modifiers :as mods]
            [orcpub.entity-spec :as es]
            [orcpub.dnd.e5.character :as char5e])
  #?(:cljs (:require-macros [orcpub.entity-spec :as es]
                            [orcpub.modifiers :as mods])))

(defn cls [cls-key]
  (mods/modifier ?classes
                 (if (not ((set ?classes) cls-key))
                   (conj ?classes cls-key)
                   ?classes)))

(defn subclass [cls-key subclass-key]
  (mods/modifier ?levels (assoc-in ?levels [cls-key :subclass] subclass-key)))

(defn race [nm]
  (mods/modifier ?race nm))

(defn background [nm]
  (mods/modifier ?background nm))

(defn subrace [nm]
  (mods/modifier ?subrace nm))

(defn damage-resistance [value]
  (mods/set-mod ?damage-resistances value))

(defn damage-immunity [value]
  (mods/set-mod ?damage-immunities value))

(defn immunity [value]
  (mods/set-mod ?immunities value))

(defn condition-immunity [value & [qualifier-text]]
  (mods/set-mod ?condition-immunities {:condition value
                                       :qualifier qualifier-text}))

(defn darkvision [value & [order-number]]
  (mods/modifier
   ?darkvision
   value
   "Darkvision"
   (str value " feet")
   nil
   order-number))

(defn speed [value]
  (mods/cum-sum-mod ?speed value "Speed" (mods/bonus-str value)))

(defn swimming-speed [value]
  (mods/cum-sum-mod ?swimming-speed value "Swim Speed" (mods/bonus-str value)))

(defn climbing-speed [value]
  (mods/cum-sum-mod ?climbing-speed value "Climb Speed" (mods/bonus-str value)))

(defn unarmored-speed-bonus [value]
  (mods/cum-sum-mod ?unarmored-speed-bonus value "Unarmored Speed" (mods/bonus-str value)))

(defn ability [ability bonus]
  (mods/modifier ?ability-increases
                 (update ?ability-increases ability + bonus)
                 (clojure.string/upper-case (name ability))
                 (mods/bonus-str bonus)))

(defn abilities [abilities]
  (mods/modifier ?base-abilities abilities))

(defn deferred-abilities []
  (mods/deferred-modifier
    ?abilities
    (fn [abilities]
      (es/modifier ?base-abilities abilities))
    {:str 12 :dex 12 :con 12 :int 12 :wis 12 :cha 12}))

(defn deferred-ability-increases []
  (mods/deferred-modifier
    ?ability-increases
    (fn [increases]
      (es/modifier ?ability-increases (merge-with + increases ?ability-increases)))
    {:str 0 :dex 0 :con 0 :int 0 :wis 0 :cha 0}))

(defn saving-throws [cls-kw & abilities]
  (mods/modifier ?saving-throws
                 (apply conj (or ?saving-throws #{}) abilities)
                 nil;;"Saving Throws"
                 nil;;(s/join ", " (map (comp s/upper-case name) abilities))
                 [(or (nil? cls-kw) (= cls-kw (first ?classes)))]))

(defn saving-throw-advantage [types & [abilities]]
  (mods/vec-mod ?saving-throw-advantage {:abilities abilities
                                         :types types}))

(defn initiative [bonus]
  (mods/cum-sum-mod ?initiative bonus "Initiative" (mods/bonus-str bonus)))

(defn level [class-key class-nm level hit-die]
  (mods/modifier ?levels (update ?levels class-key merge {:class-name class-nm
                                                          :class-level level
                                                          :hit-die hit-die})))

(defn spell-slots [level num]
  (mods/map-mod ?spell-slots level num))

(defn spells-known [level spell-key spellcasting-ability class & [min-level qualifier]]
  (mods/modifier
   ?spells-known
   (if (>= ?total-levels (or min-level 0))
     (update
      ?spells-known
      level
      (fn [spells]
        (conj (or spells)
              {:key spell-key
               :ability spellcasting-ability
               :qualifier qualifier
               :class class})))
     ?spells-known)))

(defn trait-cfg [{:keys [name description level summary page conditions] :as cfg}]
  (mods/modifier ?traits
                 (if (or (nil? level) (>= ?total-levels level))
                   (conj
                    ?traits
                    cfg)
                   ?traits)))

(defn trait [name & [description level summary conditions]]
  (trait-cfg {:name name :description description :level level :summary summary :conditions conditions}))

(defmacro dependent-trait [{:keys [level conditions] :or {level 1} :as t}]
  `(mods/modifier ~'?traits
                  (if (or (nil? ~level) (>= ~'?total-levels ~level))
                    (conj
                     ~'?traits
                     ~t)
                    ~'?traits)
                  nil
                  nil
                  ~conditions))

(defn proficiency-bonus [bonus]
  (mods/modifier ?proficiency-bonus bonus))

(defn skill-proficiency [skill-kw]
  (mods/set-mod ?skill-profs skill-kw))

(defn max-hit-points [bonus]
  (mods/cum-sum-mod ?hit-point-level-increases bonus "HP" (mods/bonus-str bonus)))

(defn deferred-max-hit-points []
  (mods/deferred-modifier
    ?hit-point-level-increases
    (fn [v] (es/cum-sum-mod ?hit-point-level-increases v))
    1
    "HP"
    mods/bonus-str))

(defn skill-expertise [key]
  (mods/set-mod ?skill-expertise key))

(defn tool-proficiency [key & [first-class? cls-kw]]
  (if first-class?
    (mods/set-mod ?tool-profs
                  key
                  nil
                  nil
                  [(= cls-kw (first ?classes))])
    (mods/set-mod ?tool-profs
                  key)))

(defn language [key]
  (mods/set-mod ?languages key))

(defn weapon-proficiency [key & [first-class? cls-kw]]
  (if first-class?
    (mods/set-mod ?weapon-profs
                  key
                  nil
                  nil
                  [(= cls-kw (first ?classes))])
    (mods/set-mod ?weapon-profs
                  key)))

(defn armor-proficiency [key & [first-class? cls-kw]]
  (if first-class?
    (mods/set-mod ?armor-profs
                  key
                  nil
                  nil
                  [(= cls-kw (first ?classes))])
    (mods/set-mod ?armor-profs
                  key)))

(defn light-armor-proficiency [& [first-class? cls-kw]]
  (armor-proficiency "light" :light first-class? cls-kw))

(defn medium-armor-proficiency [& [first-class? cls-kw]]
  (armor-proficiency "medium" :medium first-class? cls-kw))

(defn heavy-armor-proficiency [& [first-class? cls-kw]]
  (armor-proficiency "heavy" :heavy first-class? cls-kw))

(defn shield-armor-proficiency [& [first-class? cls-kw]]
  (armor-proficiency "shields" :shields first-class? cls-kw))

(defn passive-perception [bonus]
  (mods/cum-sum-mod ?passive-perception bonus))

(defn passive-investigation [bonus]
  (mods/cum-sum-mod ?passive-investigation bonus))

(defn size [size]
  (mods/modifier ?size size))

(defn weapon [weapon-kw num]
  (mods/map-mod ?weapons weapon-kw num))

(defn magic-weapon [weapon-kw num]
  (mods/map-mod ?magic-weapons weapon-kw num))

(defn deferred-weapon [weapon-kw]
  (mods/deferred-modifier
    ?weapons
    (fn [num] (es/map-mod ?weapons weapon-kw num))
    1))

(defn deferred-magic-weapon [weapon-kw]
  (mods/deferred-modifier
    ?magic-weapons
    (fn [num] (es/map-mod ?magic-weapons weapon-kw num))
    1))

(defn armor [armor-kw num]
  (mods/map-mod ?armor armor-kw num))

(defn deferred-armor [armor-kw]
  (mods/deferred-modifier
    ?armor
    (fn [num] (es/map-mod ?armor armor-kw num))
    1))

(defn deferred-magic-armor [armor-kw]
  (mods/deferred-modifier
    ?armor
    (fn [num] (es/map-mod ?magic-armor armor-kw num))
    1))

(defn equipment [equipment-kw num]
  (mods/map-mod ?equipment equipment-kw num))

(defn deferred-equipment [equipment-kw]
  (mods/deferred-modifier
    ?equipment
    (fn [num] (es/map-mod ?equipment equipment-kw num))
    1))

(defn deferred-magic-item [item-kw]
  (mods/deferred-modifier
    ?magic-items
    (fn [num] (es/map-mod ?magic-items item-kw num))
    1))

(defn extra-attack []
  (mods/cum-sum-mod ?num-attacks 1))

(defn ranged-attack-bonus [bonus]
  (mods/cum-sum-mod ?ranged-attack-bonus bonus))

(defn armored-ac-bonus [bonus]
  (mods/cum-sum-mod ?armored-ac-bonus bonus))

(defn unarmored-ac-bonus [bonus]
  (mods/cum-sum-mod ?unarmored-ac-bonus bonus))

(defn unarmored-with-shield-ac-bonus [bonus]
  (mods/cum-sum-mod ?unarmored-with-shield-ac-bonus bonus))

(defmacro attack [atk]
  `(mods/modifier ~'?attacks
                  (conj
                   ~'?attacks
                   ~atk)))

(defn critical [roll-value]
  (mods/set-mod ?critical roll-value))


(defmacro action [action]
  `(mods/modifier ~'?actions
                  (conj
                   ~'?actions
                   ~action)))

(defmacro bonus-action [action]
  `(mods/modifier ~'?bonus-actions
                  (conj
                   ~'?bonus-actions
                   ~action)))

(defmacro reaction [action]
  `(mods/modifier ~'?reactions
                 (conj
                  ~'?reactions
                  ~action)))

(defmacro level-val [level mappings]
  (let [flat-mappings (conj (vec (apply concat (sort-by first > (dissoc mappings :default)))) (:default mappings))]
    `(condp <= ~level ~@flat-mappings)))

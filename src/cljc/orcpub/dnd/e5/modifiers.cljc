(ns orcpub.dnd.e5.modifiers
  (:require [clojure.spec :as spec]
            [clojure.string :as s]
            [orcpub.modifiers :as mods]
            [orcpub.entity-spec :as es]
            [orcpub.dnd.e5.character :as char5e])
  #?(:cljs (:require-macros [orcpub.entity-spec :as es]
                            [orcpub.modifiers :as mods])))

(defn class [cls-key]
  (mods/vec-mod ?classes cls-key))

(defn subclass [cls-key nm]
  (mods/modifier ?levels (assoc-in ?levels [cls-key :subclass] nm)))

(defn race [nm]
  (mods/modifier ?race nm))

(defn background [nm]
  (mods/modifier ?background nm))

(defn subrace [nm]
  (mods/modifier ?subrace nm))

(defn resistance [value]
  (mods/set-mod ?resistances value))

(defn immunity [value]
  (mods/set-mod ?immunities value))

(defn condition-immunity [value]
  (mods/set-mod ?condition-immunities value))

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

(defn saving-throws [cls-kw & abilities]
  (mods/modifier ?saving-throws
                 (apply conj (or ?saving-throws #{}) abilities)
                 nil;;"Saving Throws"
                 nil;;(s/join ", " (map (comp s/upper-case name) abilities))
                 [(= cls-kw (first ?classes))]))

(defn saving-throw-type-advantage [type-nm type-kw]
  (mods/vec-mod ?saving-throw-type-advantage {:name type-nm
                                              :key type-kw}))

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

(defmacro dependent-trait [name description level summary conditions]
  `(mods/modifier ~'?traits
                  (if (or (nil? ~level) (>= ~'?total-levels ~level))
                    (conj
                     ~'?traits
                     {:name ~name
                      :description ~description
                      :summary ~summary})
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

(defn tool-proficiency [name key & [first-class? cls-kw]]
  (if first-class?
    (mods/set-mod ?tool-profs
                  {:name name
                   :key key}
                  nil
                  nil
                  [(= cls-kw (first ?classes))])
    (mods/set-mod ?tool-profs
                  {:name name
                   :key key})))

(defn language [name key]
  (mods/set-mod ?languages {:name name
                            :key key}))

(defn weapon-proficiency [name key & [first-class? cls-kw]]
  (if first-class?
    (mods/set-mod ?weapon-profs
                  {:name name
                   :key key}
                  nil
                  nil
                  [(= cls-kw (first ?classes))])
    (mods/set-mod ?weapon-profs
                  {:name name
                   :key key})))

(defn armor-proficiency [name key & [first-class? cls-kw]]
  (if first-class?
    (mods/set-mod ?armor-profs
                  {:name name
                   :key key}
                  nil
                  nil
                  [(= cls-kw (first ?classes))])
    (mods/set-mod ?armor-profs
                  {:name name
                   :key key})))

(defn light-armor-proficiency [& [first-class? cls-kw]]
  (armor-proficiency "light" :light first-class? cls-kw))

(defn medium-armor-proficiency [& [first-class? cls-kw]]
  (armor-proficiency "medium" :medium first-class? cls-kw))

(defn heavy-armor-proficiency [& [first-class? cls-kw]]
  (armor-proficiency "heavy" :heavy first-class? cls-kw))

(defn shield-armor-proficiency [& [first-class? cls-kw]]
  (armor-proficiency "shields" :shields first-class? cls-kw))

(defn action [name & [desc]]
  (mods/vec-mod ?actions {:name name
                          :description desc}))

(defn passive-perception [bonus]
  (mods/cum-sum-mod ?passive-perception bonus))

(defn passive-investigation [bonus]
  (mods/cum-sum-mod ?passive-investigation bonus))

(defn size [size]
  (mods/modifier ?size size))

(defn weapon [weapon-kw num]
  (mods/map-mod ?weapons weapon-kw num))

(defn deferred-weapon [weapon-kw]
  (mods/deferred-modifier
    ?weapons
    (fn [num] (es/map-mod ?weapons weapon-kw num))
    1))

(defn armor [armor-kw num]
  (mods/map-mod ?armor armor-kw num))

(defn deferred-armor [armor-kw]
  (mods/deferred-modifier
    ?armor
    (fn [num] (es/map-mod ?armor armor-kw num))
    1))

(defn equipment [equipment-kw num]
  (mods/map-mod ?equipment equipment-kw num))

(defn deferred-equipment [equipment-kw]
  (mods/deferred-modifier
    ?equipment
    (fn [num] (es/map-mod ?equipment equipment-kw num))
    1))

(defn extra-attack []
  (mods/cum-sum-mod ?num-attacks 1))

(defn ranged-attack-bonus [bonus]
  (mods/cum-sum-mod ?ranged-attack-bonus bonus))

(defn armored-ac-bonus [bonus]
  (mods/modifier ?armor-class-with-armor (fn [armor]
                                           (+ bonus (?armor-class-with-armor armor)))))

(defn critical [roll-value]
  (mods/set-mod ?critical roll-value))

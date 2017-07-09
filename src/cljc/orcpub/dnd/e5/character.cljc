(ns orcpub.dnd.e5.character
  (:require #?(:clj [clojure.spec.alpha :as spec])
            #?(:cljs [cljs.spec.alpha :as spec])
            #?(:clj [clojure.spec.test.alpha :as stest])
            #?(:cljs [cljs.spec.test.alpha :as stest])
            #?(:clj [clojure.edn :refer [read-string]])
            #?(:cljs [cljs.reader :refer [read-string]])
            [clojure.string :as s]
            [orcpub.entity-spec :as es]
            [orcpub.dice :as dice]
            [orcpub.common :as common]
            [orcpub.template :as t]
            [orcpub.entity :as entity]
            [orcpub.entity.strict :as se]
            [orcpub.dnd.e5.skills :as skills]
            [orcpub.dnd.e5.character.equipment :as equip]))

(spec/def ::armor-class nat-int?)
(spec/def ::subrace string?)
(spec/def ::race string?)
(spec/def ::darkvision boolean?)
(spec/def ::speed nat-int?)
(spec/def ::character-ability (spec/int-in 1 21))
(spec/def ::initiative int?)
(spec/def ::savings-throw keyword?)
(spec/def ::savings-throws (spec/* ::savings-throw))
(spec/def ::max-hit-points nat-int?)

(spec/def ::str ::character-ability)
(spec/def ::dex ::character-ability)
(spec/def ::con ::character-ability)
(spec/def ::int ::character-ability)
(spec/def ::wis ::character-ability)
(spec/def ::cha ::character-ability)

(spec/def ::abilities (spec/keys :req-un [::str ::dex ::con ::int ::wis ::cha]))

(spec/def ::custom-equipment ::equip/equipment-items)
(spec/def ::custom-treasure ::equip/equipment-items)

(spec/def ::values (spec/and (spec/map-of qualified-keyword? any?)
                             (spec/keys :opt [::custom-equipment
                                              ::custom-treasure])))

(spec/def ::raw-character ::entity/raw-entity)

(defn has-simple-keywords? [values]
  (some
   (fn [[k v]] (simple-keyword? k))
   (if (map? values)
     values
     ;; some weird spec issue causes values to look like [:map values]
     (if (and (vector? values) (= (first values) :map))
       (second values)))))

(defn equipment-has-simple-keywords? [equipment]
  (some
   (fn [e] (-> e ::entity/value has-simple-keywords?))
   (let [[m e] equipment]
     (if (= :multiple m)
       e
       equipment))))

(spec/def ::unnamespaced-values
  #(has-simple-keywords? (::entity/values %)))

(def equipment-keys [:equipment
                     :weapons
                     :armor
                     :treasure
                     :other-magic-items
                     :magic-weapons
                     :magic-armor])

(spec/def ::unnamespaced-equipment
  (fn [c]
    (some
     #(equipment-has-simple-keywords? (-> c ::entity/options %))
     equipment-keys)))

(spec/def ::unnamespaced-abilities
  (fn [c]
    (let [abilities-selection (-> c ::entity/options :ability-scores)
          abilities (if (vector? abilities-selection) (second abilities-selection) abilities-selection)]
      (has-simple-keywords? (::entity/value abilities)))))

(spec/def ::unnamespaced-keywords
  (spec/or :values ::unnamespaced-values
           :equipment ::unnamespaced-equipment
           :abilities ::unnamespaced-abilities))

(spec/def ::unnamespaced-character
  (spec/and ::raw-character
            ::unnamespaced-keywords))

(spec/def ::strict-character ::se/entity)

(defn add-equipment-namespace-to-option [{:keys [::entity/value] :as option}]
  (if value
    (assoc
     option
     ::entity/value
     (common/add-namespaces-to-keys
      "orcpub.dnd.e5.character.equipment"
      value))
    option))

(defn add-equipment-namespace [raw-character equipment-key]
  (let [path [::entity/options equipment-key]]
    (if (get-in raw-character path)
      (update-in raw-character
                 path
                 (fn [eq]
                   (with-meta
                     (mapv
                      add-equipment-namespace-to-option
                      eq)
                     (meta eq))))
      raw-character)))

(defn add-custom-equipment-namespaces [raw-character]
  (if (get-in raw-character [::entity/values :custom-equipment])
    (assoc-in raw-character [::entity/values ::custom-equipment]
               #(mapv (partial common/add-namespaces-to-keys
                              "orcpub.dnd.e5.character.equipment")
                     %))
    raw-character))

(defn add-equipment-namespaces [raw-character]
  (-> (reduce
       add-equipment-namespace
       raw-character
       equipment-keys)
      add-custom-equipment-namespaces))

(defn add-ability-namespaces [raw-character]
  (update-in raw-character
             [::entity/options :ability-scores ::entity/value]
             (fn [{:keys [::str ::dex ::con ::int ::wis ::cha]
                   n-str :str n-dex :dex n-con :con n-int :int n-wis :wis n-cha :cha}]
               {::str (or n-str str)
                ::dex (or n-dex dex)
                ::con (or n-con con)
                ::int (or n-int int)
                ::wis (or n-wis wis)
                ::cha (or n-cha cha)})))

(defn add-namespaces-to-values [raw-character]
  (if (seq (::entity/values raw-character))
    (update raw-character
            ::entity/values
            (fn [values]
              (common/add-namespaces-to-keys
               "orcpub.dnd.e5.character"
               values)))
    raw-character))

(defn add-namespaces [raw-character]
  (-> raw-character
      add-equipment-namespaces
      add-ability-namespaces
      add-namespaces-to-values))

(defn fix-quantities [raw-character]
  (reduce
   (fn [char equipment-key]
     (let [path [::entity/options equipment-key]]
       (if (seq (get-in raw-character path))
         (update-in char
                    path
                    (fn [equipment-vec]
                      (with-meta
                        (mapv
                         (fn [equipment]
                           (update-in
                            equipment
                            [::entity/value ::equip/quantity]
                            (fn [qty]
                              (if (string? qty)
                                (if (s/blank? qty)
                                  0
                                  (read-string qty))
                                qty))))
                         equipment-vec)
                        (meta equipment-vec))))
         char)))
   raw-character
   equipment-keys))

(defn fix-custom-quantities [raw-character]
  (reduce
   (fn [char equipment-key]
     (let [path [::entity/values equipment-key]]
       (if (seq (get-in raw-character path))
         (update-in char
                    path
                    (fn [equipment-vec]
                      (with-meta
                        (mapv
                         (fn [equipment]
                           (update
                            equipment
                            ::equip/quantity
                            (fn [qty]
                              (if (string? qty)
                                (if (s/blank? qty)
                                  0
                                  (read-string qty))
                                qty))))
                         equipment-vec)
                        (meta equipment-vec))))
         char)))
   raw-character
   [::custom-equipment ::custom-treasure]))

(defn to-strict-prepared-spells [[class-nm spell-keys]]
  {::class-name class-nm
   ::prepared-spells spell-keys})

(defn remove-image-failed-flags [values]
  (dissoc values ::image-url-failed ::faction-image-url-failed))

(defn to-strict-prepared-spells-list [values]
  (update
   values
   ::prepared-spells-by-class
   #(map
    to-strict-prepared-spells
    %)))

(defn remove-nans [values]
  (let [current-hit-points (::current-hit-points values)]
    (if (and current-hit-points
             (not (int? current-hit-points)))
      (dissoc values ::current-hit-points)
      values)))

(defn clean-values [raw-character]
  (cond-> raw-character
    (-> raw-character ::entity/values seq)
    (update ::entity/values
            #(cond-> %
               true remove-image-failed-flags
               true remove-nans
               (::prepared-spells-by-class %) to-strict-prepared-spells-list))
    
    true fix-quantities
    true fix-custom-quantities))

(defn to-strict [raw-character]
  (let [cleaned (clean-values raw-character)]
    (entity/to-strict cleaned)))

(spec/fdef to-strict
           :args ::raw-character
           :ret ::strict-character)

(defn vectorize-equipment [raw-character]
  (reduce
   (fn [char equipment-key]
     (let [path [::entity/options equipment-key]]
       (if (seq (get-in char path))
         (update-in
          char
          path
          (fn [e-map]
            (if (vector? e-map)
              e-map
              (mapv
               (fn [[k v]]
                 {::entity/key k
                  ::entity/value v})
               e-map))))
         char)))
   raw-character
   equipment-keys))

(defn update-values-from-strict [character]
  (-> character
    (update-in
     [::entity/values
      ::prepared-spells-by-class]
     (fn [prepared-spells]
       (into
        {}
        (map
         (fn [{:keys [::class-name ::prepared-spells]}]
           [class-name (set prepared-spells)])
         prepared-spells))))
    (update-in
     [::entity/values
      ::features-used]
     (fn [features-used]
       (into
        {}
        (map
         (fn [[k v]]
           [k (into #{} v)])
         (dissoc features-used :db/id)))))))

(defn from-strict [raw-character]
  (-> (entity/from-strict raw-character)
      vectorize-equipment
      update-values-from-strict))

(defn standard-ability-roll []
  (dice/dice-roll {:num 4 :sides 6 :drop-num 1}))

(spec/fdef
 standard-ability-roll
 :args nil?
 :ret ::character-ability
 :fn (spec/and (partial <= 3) (partial >= 18)))

(def ability-keys [::str ::dex ::con ::int ::wis ::cha])

(defn standard-ability-rolls []
  (zipmap
   ability-keys
   (take 6 (repeatedly standard-ability-roll))))

(spec/fdef
 standard-ability-rolls
 :args nil?
 :ret ::abilities)

(defn abilities [& as]
  (zipmap
   ability-keys
   as))

(defn get-prop [built-char prop]
  (es/entity-val built-char prop))

(defn alignment [built-char]
  (get-prop built-char :alignment))

(defn levels [built-char]
  (get-prop built-char :levels))

(defn total-levels [built-char]
  (get-prop built-char :total-levels))

(defn class-level-fn [built-char]
  (get-prop built-char :class-level))

(defn background [built-char]
  (get-prop built-char :background))

(defn ability-increases [built-char]
  (get-prop built-char :ability-increases))

(defn race-ability-increases [built-char]
  (get-prop built-char :race-ability-increases))

(defn subrace-ability-increases [built-char]
  (get-prop built-char :subrace-ability-increases))

(defn ability-values [built-char]
  (get-prop built-char :abilities))

(defn ability-bonuses [built-char]
  (get-prop built-char :ability-bonuses))

(defn base-land-speed [built-char]
  (get-prop built-char :total-speed))

(defn base-swimming-speed [built-char]
  (get-prop built-char :swimming-speed))

(defn base-flying-speed [built-char]
  (get-prop built-char :flying-speed))

(defn land-speed-with-armor [built-char]
  (get-prop built-char :speed-with-armor))

(defn unarmored-speed-bonus [built-char]
  (get-prop built-char :unarmored-speed-bonus))

(defn race [built-char]
  (get-prop built-char :race))

(defn subrace [built-char]
  (get-prop built-char :subrace))

(defn classes [built-char]
  (get-prop built-char :classes))

(defn darkvision [built-char]
  (get-prop built-char :total-darkvision))

(defn skill-proficiencies [built-char]
  (get-prop built-char :skill-profs))

(defn skill-bonuses [built-char]
  (get-prop built-char :skill-bonuses))

(defn skill-expertise [built-char]
  (get-prop built-char :skill-expertise))

(defn tool-proficiencies [built-char]
  (get-prop built-char :tool-profs))

(defn tool-expertise [built-char]
  (get-prop built-char :tool-expertise))

(defn tool-bonus-fn [built-char]
  (get-prop built-char :tool-bonus-fn))

(defn weapon-proficiencies [built-char]
  (let [proficiencies (get-prop built-char :weapon-profs)]
    (if (and proficiencies (proficiencies :martial))
      [:simple :martial]
      proficiencies)))

(defn has-weapon-prof [built-char]
  (get-prop built-char :has-weapon-prof?))

(defn armor-proficiencies [built-char]
  (get-prop built-char :armor-profs))

(defn damage-resistances [built-char]
  (get-prop built-char :damage-resistances))

(defn damage-vulnerabilities [built-char]
  (get-prop built-char :damage-vulnerabilities))

(defn damage-immunities [built-char]
  (get-prop built-char :damage-immunities))

(defn immunities [built-char]
  (get-prop built-char :immunities))

(defn condition-immunities [built-char]
  (get-prop built-char :condition-immunities))

(defn languages [built-char]
  (get-prop built-char :languages))

(defn base-armor-class [built-char]
  (get-prop built-char :armor-class))

(defn armor-class-with-armor [built-char]
  (get-prop built-char :armor-class-with-armor))

(defn normal-armor-inventory [built-char]
  (get-prop built-char :armor))

(defn magic-armor-inventory [built-char]
  (get-prop built-char :magic-armor))

(defn all-armor-inventory [built-char]
  (merge (normal-armor-inventory built-char)
         (magic-armor-inventory built-char)))

(defn normal-weapons-inventory [built-char]
  (get-prop built-char :weapons))

(defn magic-weapons-inventory [built-char]
  (get-prop built-char :magic-weapons))

(defn all-weapons-inventory [built-char]
  (merge (normal-weapons-inventory built-char)
         (magic-weapons-inventory built-char)))

(defn custom-equipment [built-char]
  (::custom-equipment built-char))

(defn custom-treasure [built-char]
  (::custom-treasure built-char))

(defn treasure [built-char]
  (get-prop built-char :treasure))

(defn normal-equipment-inventory [built-char]
  (get-prop built-char :equipment))

(defn magical-equipment-inventory [built-char]
  (get-prop built-char :magic-items))

(defn spells-known [built-char]
  (get-prop built-char :spells-known))

(defn flat-spells [spells-known]
  (->> spells-known vals (map vals) flatten))

(defn spells-known-modes [built-char]
  (get-prop built-char :spells-known-modes))

(defn spell-slots [built-char]
  (get-prop built-char :spell-slots))

(defn spell-slot-factors [built-char]
  (get-prop built-char :spell-slot-factors))

(defn total-spellcaster-levels [built-char]
  (get-prop built-char :total-spellcaster-levels))

(defn spell-modifiers [built-char]
  (get-prop built-char :spell-modifiers))

(defn prepares-spells [built-char]
  (get-prop built-char :prepares-spells))

(defn prepare-spell-count-fn [built-char]
  (get-prop built-char :prepare-spell-count))

(defn spell-attack-modifier-fn [built-char]
  (get-prop built-char :spell-attack-modifier))

(defn spell-save-dc-fn [built-char]
  (get-prop built-char :spell-save-dc))

(defn traits [built-char]
  (get-prop built-char :traits))

(defn attacks [built-char]
  (get-prop built-char :attacks))

(defn bonus-actions [built-char]
  (get-prop built-char :bonus-actions))

(defn reactions [built-char]
  (get-prop built-char :reactions))

(defn actions [built-char]
  (get-prop built-char :actions))

(defn max-hit-points [built-char]
  (get-prop built-char :max-hit-points))

(defn current-hit-points [built-char]
  (get-prop built-char ::current-hit-points))

(defn hit-point-level-bonus [built-char]
  (get-prop built-char :hit-point-level-bonus))

(defn initiative [built-char]
  (get-prop built-char :initiative))

(defn proficiency-bonus [built-char]
  (get-prop built-char :prof-bonus))

(defn passive-perception [built-char]
  (get-prop built-char :passive-perception))

(defn number-of-attacks [built-char]
  (get-prop built-char :num-attacks))

(defn critical-hit-values [built-char]
  (get-prop built-char :critical))

(defn crit-values-str [built-char]
  (let [critical-hit-values (critical-hit-values built-char)]
    (if (> (count critical-hit-values) 1)
      (str (apply min critical-hit-values)
           "-"
           (apply max critical-hit-values))
      (first critical-hit-values))))

(defn saving-throws [built-char]
  (get-prop built-char :saving-throws))

(defn save-bonuses [built-char]
  (get-prop built-char :save-bonuses))

(defn saving-throw-advantages [built-char]
  (get-prop built-char :saving-throw-advantage))

(defn weapon-attack-modifier-fn [built-char]
  (get-prop built-char :weapon-attack-modifier))

(defn weapon-damage-modifier-fn [built-char]
  (get-prop built-char :weapon-damage-modifier))

(defn option-sources [built-char]
  (get-prop built-char :option-sources))

(defn feats [built-char]
  (get-prop built-char :feats))

(defn weapon-attack-modifier [built-char weapon finesse?]
  ((weapon-attack-modifier-fn built-char)
   weapon
   finesse?))

(defn weapon-damage-modifier [built-char weapon finesse?]
  ((get-prop built-char :weapon-damage-modifier)
   weapon
   finesse?))

(defn age [built-char]
  (get-prop built-char ::age))

(defn sex [built-char]
  (get-prop built-char ::sex))

(defn height [built-char]
  (get-prop built-char ::height))

(defn weight [built-char]
  (get-prop built-char ::weight))

(defn skin [built-char]
  (get-prop built-char ::skin))

(defn eyes [built-char]
  (get-prop built-char ::eyes))

(defn hair [built-char]
  (get-prop built-char ::hair))

(defn image-url [built-char]
  (get-prop built-char ::image-url))

(defn faction-image-url [built-char]
  (get-prop built-char ::faction-image-url))

(defn faction-name [built-char]
  (get-prop built-char ::faction-name))

(defn character-name [built-char]
  (get-prop built-char ::character-name))

(defn player-name [built-char]
  (get-prop built-char ::player-name))

(defn personality-trait-1 [built-char]
  (get-prop built-char ::personality-trait-1))

(defn personality-trait-2 [built-char]
  (get-prop built-char ::personality-trait-2))

(defn ideals [built-char]
  (get-prop built-char ::ideals))

(defn xps [built-char]
  (get-prop built-char ::xps))

(defn bonds [built-char]
  (get-prop built-char ::bonds))

(defn flaws [built-char]
  (get-prop built-char ::flaws))

(defn description [built-char]
  (get-prop built-char ::description))

(defn notes [built-char]
  (get-prop built-char ::notes))

(defn features-used [built-char]
  (get-prop built-char ::features-used))

(defn image-url-failed [built-char]
  (get-prop built-char ::image-url-failed))

(defn faction-image-url-failed [built-char]
  (get-prop built-char ::faction-image-url-failed))

(defn public? [built-char]
  (get-prop built-char ::share?))

(defn used-resources [built-char]
  (get-prop built-char :used-resources))

(defn al-illegal-reasons [built-char]
  (get-prop built-char :al-illegal-reasons))

(defn max-armor-class [unarmored-armor-class
                       ac-with-armor-fn
                       all-armor-inventory
                       equipped-armor
                       equipped-shields]
  (let [all-armor-classes (for [armor (conj equipped-armor nil)
                                shield (conj equipped-shields nil)]
                            (ac-with-armor-fn armor shield))]
    (apply max all-armor-classes)))

(defn remove-custom-starting-equipment [character equipment-indicator path]
  (update-in
   character
   path
   (fn [equipment]
     (with-meta
       (vec
        (remove
         equipment-indicator
         equipment))
       (meta equipment)))))

(defn remove-starting-equipment [character equipment-indicator]
  (update-in
   character
   [::entity/options]
   (fn [options]
     (reduce
      (fn [os equipment-key]
        (if-let [equipment (os equipment-key)]
          (let [equip-meta (meta equipment)
                new-equipment (with-meta
                                (vec
                                 (remove
                                  (comp equipment-indicator ::entity/value)
                                  equipment))
                                equip-meta)]
            (if (or (:db/id equip-meta) (seq new-equipment))
              (assoc os equipment-key new-equipment)
              (dissoc os equipment-key)))
          os))
      options
      equipment-keys))))

(defn add-associated-options [character associated-options]
  (reduce
   (fn [new-char associated-option]
     (update-in
      new-char
      [::entity/options]
      (fn [options]
        (merge-with
         (fn [o1 o2]
           (let [ks (into #{} (map ::entity/key o1))]
             (with-meta
               (vec
                (concat
                 o1
                 (remove (comp ks ::entity/key) o2)))
               (meta o1))))
         options
         associated-option))))
   character
   associated-options))

(defn add-custom-equipment [character custom-equipment path]
  (update-in
   character
   path
   (fn [equipment]
     (let [current-names (into #{} (map :name equipment))]
       (with-meta
         (vec
          (concat
           equipment
           (remove
            (comp current-names :name)
            (map
             (fn [[nm num]]
               {::equip/name nm
                ::equip/quantity num
                ::equip/equipped? true
                ::equip/background-starting-equipment? true})
             custom-equipment))))
         (meta equipment))))))

(defn set-class [character class-key class-index new-class-option]
  (let [associated-options (::t/associated-options new-class-option)
        with-new-class (assoc-in
                        character
                        [::entity/options :class class-index ::entity/key]
                        class-key)
        without-starting-equipment (remove-starting-equipment with-new-class ::equip/class-starting-equipment?)
        with-new-starting-equipment (add-associated-options without-starting-equipment associated-options)]
    (if (zero? class-index)
      with-new-starting-equipment
      with-new-class)))

(defn skill-prof-bonuses [prof-bonus skill-profs skill-expertise default-skill-bonus-fns]
  (reduce
   (fn [m {k :key}]
     (let [skill-ability (skills/skill-abilities k)]
       (assoc m k (if (k skill-profs)
                    (if (k skill-expertise)
                      (* 2 prof-bonus)
                      prof-bonus)
                    (if (seq default-skill-bonus-fns)
                      (apply max
                             (map
                              #(% skill-ability)
                              default-skill-bonus-fns))
                      0)))))
   {}
   skills/skills))

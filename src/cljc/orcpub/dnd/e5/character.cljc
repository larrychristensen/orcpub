(ns orcpub.dnd.e5.character
  (:require #?(:clj [clojure.spec :as spec])
            #?(:cljs [cljs.spec :as spec])
            #?(:clj [clojure.spec.test :as stest])
            #?(:cljs [cljs.spec.test :as stest])
            #?(:clj [clojure.edn :refer [read-string]])
            #?(:cljs [cljs.reader :refer [read-string]])
            [clojure.string :as s]
            [orcpub.entity-spec :as es]
            [orcpub.dice :as dice]
            [orcpub.common :as common]
            [orcpub.template :as t]
            [orcpub.entity :as entity]
            [orcpub.entity.strict :as se]
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

(defn clean-values [raw-character]
  (cond-> raw-character
    (-> raw-character ::entity-values seq)
    (update ::entity/values
            (fn [vs]
              (dissoc vs ::image-url-failed ::faction-image-url-failed)))
    
    true fix-quantities
    true fix-custom-quantities))

(defn to-strict [raw-character]
  (entity/to-strict (clean-values raw-character)))

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

(defn from-strict [raw-character]
  (vectorize-equipment
   (entity/from-strict raw-character)))

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

(defn alignment [built-char]
  (es/entity-val built-char :alignment))

(defn levels [built-char]
  (es/entity-val built-char :levels))

(defn background [built-char]
  (es/entity-val built-char :background))

(defn ability-values [built-char]
  (es/entity-val built-char :abilities))

(defn ability-bonuses [built-char]
  (es/entity-val built-char :ability-bonuses))

(defn base-land-speed [built-char]
  (es/entity-val built-char :total-speed))

(defn base-swimming-speed [built-char]
  (es/entity-val built-char :swimming-speed))

(defn base-flying-speed [built-char]
  (es/entity-val built-char :flying-speed))

(defn land-speed-with-armor [built-char]
  (es/entity-val built-char :speed-with-armor))

(defn unarmored-speed-bonus [built-char]
  (es/entity-val built-char :unarmored-speed-bonus))

(defn race [built-char]
  (es/entity-val built-char :race))

(defn subrace [built-char]
  (es/entity-val built-char :subrace))

(defn classes [built-char]
  (es/entity-val built-char :classes))

(defn darkvision [built-char]
  (es/entity-val built-char :total-darkvision))

(defn skill-proficiencies [built-char]
  (es/entity-val built-char :skill-profs))

(defn skill-bonuses [built-char]
  (es/entity-val built-char :skill-bonuses))

(defn tool-proficiencies [built-char]
  (es/entity-val built-char :tool-profs))

(defn weapon-proficiencies [built-char]
  (let [proficiencies (es/entity-val built-char :weapon-profs)]
    (if (and proficiencies (proficiencies :martial))
      [:simple :martial]
      proficiencies)))

(defn armor-proficiencies [built-char]
  (es/entity-val built-char :armor-profs))

(defn damage-resistances [built-char]
  (es/entity-val built-char :damage-resistances))

(defn damage-immunities [built-char]
  (es/entity-val built-char :damage-immunities))

(defn immunities [built-char]
  (es/entity-val built-char :immunities))

(defn condition-immunities [built-char]
  (es/entity-val built-char :condition-immunities))

(defn languages [built-char]
  (es/entity-val built-char :languages))

(defn base-armor-class [built-char]
  (es/entity-val built-char :armor-class))

(defn armor-class-with-armor [built-char]
  (es/entity-val built-char :armor-class-with-armor))

(defn normal-armor-inventory [built-char]
  (es/entity-val built-char :armor))

(defn magic-armor-inventory [built-char]
  (es/entity-val built-char :magic-armor))

(defn all-armor-inventory [built-char]
  (merge (normal-armor-inventory built-char)
         (magic-armor-inventory built-char)))

(defn normal-weapons-inventory [built-char]
  (es/entity-val built-char :weapons))

(defn magic-weapons-inventory [built-char]
  (es/entity-val built-char :magic-weapons))

(defn all-weapons-inventory [built-char]
  (merge (normal-weapons-inventory built-char)
         (magic-weapons-inventory built-char)))

(defn custom-equipment [built-char]
  (::custom-equipment built-char))

(defn custom-treasure [built-char]
  (::custom-treasure built-char))

(defn normal-equipment-inventory [built-char]
  (es/entity-val built-char :equipment))

(defn magical-equipment-inventory [built-char]
  (es/entity-val built-char :magic-items))

(defn spells-known [built-char]
  (es/entity-val built-char :spells-known))

(defn spell-slots [built-char]
  (es/entity-val built-char :spell-slots))

(defn spell-modifiers [built-char]
  (es/entity-val built-char :spell-modifiers))

(defn traits [built-char]
  (es/entity-val built-char :traits))

(defn attacks [built-char]
  (es/entity-val built-char :attacks))

(defn bonus-actions [built-char]
  (es/entity-val built-char :bonus-actions))

(defn reactions [built-char]
  (es/entity-val built-char :reactions))

(defn actions [built-char]
  (es/entity-val built-char :actions))

(defn max-hit-points [built-char]
  (es/entity-val built-char :max-hit-points))

(defn initiative [built-char]
  (es/entity-val built-char :initiative))

(defn proficiency-bonus [built-char]
  (es/entity-val built-char :prof-bonus))

(defn passive-perception [built-char]
  (es/entity-val built-char :passive-perception))

(defn number-of-attacks [built-char]
  (es/entity-val built-char :num-attacks))

(defn critical-hit-values [built-char]
  (es/entity-val built-char :critical))

(defn saving-throws [built-char]
  (es/entity-val built-char :saving-throws))

(defn save-bonuses [built-char]
  (es/entity-val built-char :save-bonuses))

(defn saving-throw-advantages [built-char]
  (es/entity-val built-char :saving-throw-advantage))

(defn weapon-attack-modifier [built-char weapon finesse?]
  ((es/entity-val built-char :weapon-attack-modifier)
   weapon
   finesse?))

(defn weapon-damage-modifier [built-char weapon finesse?]
  ((es/entity-val built-char :weapon-damage-modifier)
   weapon
   finesse?))

(defn age [built-char]
  (es/entity-val built-char ::age))

(defn sex [built-char]
  (es/entity-val built-char ::sex))

(defn height [built-char]
  (es/entity-val built-char ::height))

(defn weight [built-char]
  (es/entity-val built-char ::weight))

(defn skin [built-char]
  (es/entity-val built-char ::skin))

(defn eyes [built-char]
  (es/entity-val built-char ::eyes))

(defn hair [built-char]
  (es/entity-val built-char ::hair))

(defn image-url [built-char]
  (es/entity-val built-char ::image-url))

(defn faction-image-url [built-char]
  (es/entity-val built-char ::faction-image-url))

(defn faction-name [built-char]
  (es/entity-val built-char ::faction-name))

(defn character-name [built-char]
  (es/entity-val built-char ::character-name))

(defn player-name [built-char]
  (es/entity-val built-char ::player-name))

(defn personality-trait-1 [built-char]
  (es/entity-val built-char ::personality-trait-1))

(defn personality-trait-2 [built-char]
  (es/entity-val built-char ::personality-trait-2))

(defn ideals [built-char]
  (es/entity-val built-char ::ideals))

(defn bonds [built-char]
  (es/entity-val built-char ::bonds))

(defn flaws [built-char]
  (es/entity-val built-char ::flaws))

(defn description [built-char]
  (es/entity-val built-char ::description))

(defn image-url-failed [built-char]
  (es/entity-val built-char ::image-url-failed))

(defn faction-image-url-failed [built-char]
  (es/entity-val built-char ::faction-image-url-failed))

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

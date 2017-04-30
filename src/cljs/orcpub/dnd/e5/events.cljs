(ns orcpub.dnd.e5.events
  (:require [orcpub.entity :as entity]
            [orcpub.template :as t]
            [orcpub.common :as common]
            [orcpub.dice :as dice]
            [orcpub.dnd.e5.template :as t5e]
            [orcpub.dnd.e5.db :refer [default-value character->local-store tab-path]]

            [re-frame.core :refer [reg-event-db reg-event-fx inject-cofx path trim-v
                                   after debug]]
            [cljs.spec :as spec]))

(defn check-and-throw
  "throw an exception if db doesn't match the spec"
  [a-spec db]
  (when-not (spec/valid? a-spec db)
    (throw (ex-info (str "spec check failed: " (spec/explain-str a-spec db)) {}))))

(def check-spec-interceptor (after (partial check-and-throw ::entity/raw-entity)))

(def ->local-store (after character->local-store))

(def character-interceptors [check-spec-interceptor
                             (path :character)
                             ->local-store])


;; -- Event Handlers --------------------------------------------------

(reg-event-fx
 :initialize-db
 [(inject-cofx :local-store-character)
  check-spec-interceptor]
 (fn [{:keys [db local-store-character]} _]
   {:db (if (seq db)
          db
          (if local-store-character
            (assoc default-value :character local-store-character)
            default-value))}))

(defn reset-character [character [_]]
  t5e/character)

(reg-event-db
 :reset-character
 character-interceptors
 reset-character)

(defn set-character [db [_ character]]
  (character->local-store character)
  (assoc db :character character :loading false))

(reg-event-db
 :set-character
 set-character)

(def character-values-path
  [::entity/values])

(defn character-value-path [prop-name]
  (conj character-values-path prop-name))

(defn update-value-field [character [_ prop-name value]]
  (assoc-in character (character-value-path prop-name) value))

(reg-event-db
 :update-value-field
 character-interceptors
 update-value-field)

(defn add-class [character [_ first-unselected]]
  (update-in
   character
   [::entity/options :class]
   conj
   {::entity/key first-unselected ::entity/options {:levels [{::entity/key :level-1}]}}))

(reg-event-db
 :add-class
 character-interceptors
 add-class)

(def custom-equipment-path [::entity/values :custom-equipment])

(def custom-treasure-path [::entity/values :custom-treasure])

(defn remove-custom-starting-equipment [character equipment-indicator path]
  (update-in
   character
   path
   (fn [equipment]
     (vec
      (remove
       :background-starting-equipment?
       equipment)))))

(defn remove-starting-equipment [character equipment-indicator]
  (update-in
   character
   [::entity/options]
   (fn [options]
     (into {}
           (map
            (fn [[k v]]
              [k
               (if (sequential? v)
                 (vec
                  (remove
                   (comp equipment-indicator ::entity/value)
                   v))
                 v)])
            options)))))

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
             (vec
              (concat
               o1
               (remove (comp ks ::entity/key) o2)))))
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
       (vec
        (concat
         equipment
         (remove
          (comp current-names :name)
          (map
           (fn [[nm num]]
             {:name nm
              :quantity num
              :equipped? true
              :background-starting-equipment? true})
           custom-equipment))))))))

(defn add-starting-equipment [character [_ equipment-options custom-treasure custom-equipment]]
  (-> character
      (remove-starting-equipment :background-starting-equipment?)
      (add-associated-options equipment-options)
      (remove-custom-starting-equipment :background-starting-equipment? custom-treasure-path)
      (add-custom-equipment custom-treasure custom-treasure-path)
      (remove-custom-starting-equipment :background-starting-equipment? custom-equipment-path)
      (add-custom-equipment custom-equipment custom-equipment-path)))

(reg-event-db
 :add-starting-equipment
 character-interceptors
 add-starting-equipment)

(defn set-class [character [_ class-key class-index options-map]]
  (let [new-class-option (options-map class-key)
        associated-options (::t/associated-options new-class-option)
        with-new-class (assoc-in
                        character
                        [::entity/options :class class-index]
                        {::entity/key class-key
                         ::entity/options
                         {:levels [{::entity/key :level-1}]}})
        without-starting-equipment (remove-starting-equipment with-new-class :class-starting-equipment)]
    (if (zero? class-index)
      (add-associated-options without-starting-equipment associated-options)
      with-new-class)))

(reg-event-db
 :set-class
 character-interceptors
 set-class)

(defn set-class-level [character [_ class-index new-highest-level]]
  (update-in
   character
   [::entity/options :class class-index ::entity/options :levels]
   (fn [levels]
     (let [current-highest-level (count levels)]
       (cond
         (> new-highest-level current-highest-level)
         (vec (concat levels (map
                              (fn [lvl] {::entity/key (keyword (str "level-" (inc lvl)))})
                              (range current-highest-level new-highest-level))))
         
         (< new-highest-level current-highest-level)
         (vec (take new-highest-level levels))
         
         :else levels)))))

(reg-event-db
 :set-class-level
 character-interceptors
 set-class-level)

(defn delete-class [character [_ class-key]]
  (update-in
   character
   [::entity/options :class]
   (fn [classes] (vec (remove #(= class-key (::entity/key %)) classes)))))

(reg-event-db
 :delete-class
 character-interceptors
 delete-class)

(defn add-inventory-item [character [_ selection-key item-key]]
  (update-in
   character
   [::entity/options selection-key]
   (fn [items]
     (vec
      (conj
       items
       {::entity/key item-key
        ::entity/value {:quantity 1 :equipped? true}})))))

(reg-event-db
 :add-inventory-item
 character-interceptors
 add-inventory-item)

(defn toggle-inventory-item-equipped [character [_ selection-key item-index]]
  (update-in
   character
   [::entity/options selection-key item-index ::entity/value :equipped?]
   not))

(reg-event-db
 :toggle-inventory-item-equipped
 character-interceptors
 toggle-inventory-item-equipped)

(defn toggle-custom-inventory-item-equipped [character [_ custom-equipment-key item-index]]
  (update-in
   character
   [::entity/values custom-equipment-key item-index :equipped?]
   not))

(reg-event-db
 :toggle-custom-inventory-item-equipped
 character-interceptors
 toggle-custom-inventory-item-equipped)

(defn change-inventory-item-quantity [character [_ selection-key item-index quantity]]
  (update-in
   character
   [::entity/options selection-key item-index ::entity/value]
   (fn [item-cfg]
     ;; the select keys here is to keep :equipped while wiping out the starting-equipment indicators
     (assoc (select-keys item-cfg [:equipped?]) :quantity quantity))))

(reg-event-db
 :change-inventory-item-quantity
 character-interceptors
 change-inventory-item-quantity)

(defn change-custom-inventory-item-quantity [character [_ custom-equipment-key item-index quantity]]
  (update-in
   character
   [::entity/values custom-equipment-key item-index]
   (fn [item-cfg]
     ;; the select keys here is to keep :equipped and :name while wiping out the starting-equipment indicators
     (assoc
      (select-keys item-cfg [:name :equipped?])
      :quantity
      quantity))))

(reg-event-db
 :change-custom-inventory-item-quantity
 character-interceptors
 change-custom-inventory-item-quantity)

(defn remove-inventory-item [character [_ selection-key item-key]]
  (update-in
   character
   [::entity/options selection-key]
   (fn [items] (vec (remove #(= item-key (::entity/key %)) items)))))

(reg-event-db
 :remove-inventory-item
 character-interceptors
 remove-inventory-item)

(defn remove-custom-inventory-item [character [_ custom-equipment-key name]]
  (update-in
   character
   [::entity/values custom-equipment-key]
   (fn [items]
     (vec (remove #(= name (:name %)) items)))))

(reg-event-db
 :remove-custom-inventory-item
 character-interceptors
 remove-custom-inventory-item)

(defn set-abilities [character [_ abilities]]
  (assoc-in character [::entity/options :ability-scores ::entity/value] abilities))

(reg-event-db
 :set-abilities
 character-interceptors
 set-abilities)

(defn swap-ability-values [character [_ i other-i k v]]
  (update-in
   character
   [::entity/options :ability-scores ::entity/value]
   (fn [a]
     (let [a-vec (vec a)
           other-index (mod other-i (count a-vec))
           [other-k other-v] (a-vec other-index)]
       (assoc a k other-v other-k v)))))

(reg-event-db
 :swap-ability-values
 character-interceptors
 swap-ability-values)

(defn decrease-ability-value [character [_ full-path k]]
  (update-in
   character
   full-path
   (fn [incs]
     (common/remove-first
      (fn [{inc-key ::entity/key}]
        (= inc-key k))
      incs))))

(reg-event-db
 :decrease-ability-value
 character-interceptors
 decrease-ability-value)

(defn increase-ability-value [character [_ full-path k]]
  (update-in
   character
   full-path
   conj
   {::entity/key k}))

(reg-event-db
 :increase-ability-value
 character-interceptors
 increase-ability-value)

(defn set-ability-score [character [_ ability-kw v]]
  (assoc-in character [::entity/options :ability-scores ::entity/value ability-kw] v))

(reg-event-db
 :set-ability-score
 character-interceptors
 set-ability-score)

(defn set-ability-score-variant [character [_ variant-key]]
  (assoc-in character [::entity/options :ability-scores ::entity/key] variant-key))

(reg-event-db
 :set-ability-score-variant
 character-interceptors
 set-ability-score-variant)

(defn select-skill [character [_ path selected? skill-key]]
  (update-in
   character
   path
   (fn [skills]
     (if selected?                                             
       (vec (remove (fn [s] (= skill-key (::entity/key s))) skills))
       (vec (conj skills {::entity/key skill-key}))))))

(reg-event-db
 :select-skill
 character-interceptors
 select-skill)

(defn set-total-hps [character [_ full-path first-selection selection average-value remainder]]
  (assoc-in
   character
   full-path
   {::entity/key :manual-entry
    ::entity/value (if (= first-selection selection)
                     (+ average-value remainder)
                     average-value)}))

(reg-event-db
 :set-total-hps
 character-interceptors
 set-total-hps)

(defn random-hit-points-option [levels class-kw]
  {::entity/key :roll
   ::entity/value (dice/die-roll (-> levels class-kw :hit-die))})

(defn randomize-hit-points [character [_ built-template path levels class-kw]]
  (assoc-in
   character
   (entity/get-entity-path built-template character path)
   (random-hit-points-option levels class-kw)))

(reg-event-db
 :randomize-hit-points
 character-interceptors
 randomize-hit-points)

(defn set-hit-points-to-average [character [_ built-template path levels class-kw]]
  (assoc-in
   character
   (entity/get-entity-path built-template character path)
   {::entity/key :average
    ::entity/value (dice/die-mean (-> levels class-kw :hit-die))}))

(reg-event-db
 :set-hit-points-to-average
 character-interceptors
 set-hit-points-to-average)

(defn set-level-hit-points [character [_ built-template character level-value value]]
  (assoc-in
   character
   (entity/get-entity-path built-template character (:path level-value))
   {::entity/key :manual-entry
    ::entity/value (if (not (js/isNaN value)) value)}))

(reg-event-db
 :set-level-hit-points
 character-interceptors
 set-level-hit-points)

(defn set-page [db [_ page-index]]
  (assoc db :page page-index))

(reg-event-db
 :set-page
 set-page)

(defn set-active-tabs [db [_ active-tabs]]
  (assoc-in db tab-path active-tabs))

(reg-event-db
 :set-active-tabs
 set-active-tabs)

(defn set-loading [db [_ v]]
  (assoc db :loading v))

(reg-event-db
 :set-loading
 set-loading)

(reg-event-db
 :toggle-locked
 (fn [db [_ path]]
   (update db :locked-components (fn [comps]
                                   (if (comps path)
                                     (disj comps path)
                                     (conj comps path))))))

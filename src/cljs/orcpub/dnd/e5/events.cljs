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

(def character-interceptors [(path :character)
                             ->local-store])


;; -- Event Handlers --------------------------------------------------

(reg-event-fx
 :initialize-db
 [(inject-cofx :local-store-character)
  check-spec-interceptor]
 (fn [{:keys [db local-store-character]} _]
   {:db (assoc default-value :character local-store-character)}))

(defn reset-character [db [_]]
  (assoc db :character t5e/character :page 0))

(reg-event-db
 :reset-character
 reset-character)

(defn set-character [db [_ character]]
  (js/console.log "SET CHARACTER" db character)
  (assoc db :character character))

(reg-event-db
 :set-character
 set-character)

(def character-values-path
  [:character ::entity/values])

(defn character-value-path [prop-name]
  (conj character-values-path prop-name))

(defn update-value-field [db [_ prop-name value]]
  (assoc-in db (character-value-path prop-name) value))

(reg-event-db
 :update-value-field
 update-value-field)

(defn add-class [db [_ first-unselected]]
  (update-in
   db
   [:character ::entity/options :class]
   conj
   {::entity/key first-unselected ::entity/options {:levels [{::entity/key :level-1}]}}))

(reg-event-db
 :add-class
 add-class)

(def custom-equipment-path [:character ::entity/values :custom-equipment])

(def custom-treasure-path [:character ::entity/values :custom-treasure])

(defn remove-custom-starting-equipment [state equipment-indicator path]
  (update-in
   state
   path
   (fn [equipment]
     (vec
      (remove
       :background-starting-equipment?
       equipment)))))

(defn remove-starting-equipment [state equipment-indicator]
  (update-in
   state
   [:character ::entity/options]
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

(defn add-associated-options [state associated-options]
  (reduce
   (fn [new-s associated-option]
     (update-in
      new-s
      [:character ::entity/options]
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
   state
   associated-options))

(defn add-custom-equipment [state custom-equipment path]
  (update-in
      state
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

(defn add-starting-equipment [db [_ equipment-options custom-treasure custom-equipment]]
  (-> db
      (remove-starting-equipment :background-starting-equipment?)
      (add-associated-options equipment-options)
      (remove-custom-starting-equipment :background-starting-equipment? custom-treasure-path)
      (add-custom-equipment custom-treasure custom-treasure-path)
      (remove-custom-starting-equipment :background-starting-equipment? custom-equipment-path)
      (add-custom-equipment custom-equipment custom-equipment-path)))

(reg-event-db
 :add-starting-equipment
 add-starting-equipment)

(defn set-class [db [_ class-key class-index options-map]]
  (let [new-class-option (options-map class-key)
        associated-options (::t/associated-options new-class-option)
        with-new-class (assoc-in
                        db
                        [:character ::entity/options :class class-index]
                        {::entity/key class-key
                         ::entity/options
                         {:levels [{::entity/key :level-1}]}})
        without-starting-equipment (remove-starting-equipment with-new-class :class-starting-equipment)]
    (if (zero? class-index)
      (add-associated-options without-starting-equipment associated-options)
      with-new-class)))

(reg-event-db
 :set-class
 set-class)

(defn set-class-level [db [_ class-index new-highest-level]]
  (update-in
   db
   [:character ::entity/options :class class-index ::entity/options :levels]
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
 set-class-level)

(defn delete-class [db [_ class-key]]
  (update-in
   db
   [:character ::entity/options :class]
   (fn [classes] (vec (remove #(= class-key (::entity/key %)) classes)))))

(reg-event-db
 :delete-class
 delete-class)

(defn add-inventory-item [db [_ selection-key item-key]]
  (update-in
   db
   [:character ::entity/options selection-key]
   (fn [items]
     (vec
      (conj
       items
       {::entity/key item-key
        ::entity/value {:quantity 1 :equipped? true}})))))

(reg-event-db
 :add-inventory-item
 add-inventory-item)

(defn toggle-inventory-item-equipped [db [_ selection-key item-index]]
  (update-in
   db
   [:character ::entity/options selection-key item-index ::entity/value :equipped?]
   not))

(reg-event-db
 :toggle-inventory-item-equipped
 toggle-inventory-item-equipped)

(defn toggle-custom-inventory-item-equipped [db [_ custom-equipment-key item-index]]
  (update-in
   db
   [:character ::entity/values custom-equipment-key item-index :equipped?]
   not))

(reg-event-db
 :toggle-custom-inventory-item-equipped
 toggle-custom-inventory-item-equipped)

(defn change-inventory-item-quantity [db [_ selection-key item-index quantity]]
  (update-in
   db
   [:character ::entity/options selection-key item-index ::entity/value]
   (fn [item-cfg]
     ;; the select keys here is to keep :equipped while wiping out the starting-equipment indicators
     (assoc (select-keys item-cfg [:equipped?]) :quantity quantity))))

(reg-event-db
 :change-inventory-item-quantity
 change-inventory-item-quantity)

(defn change-custom-inventory-item-quantity [db [_ custom-equipment-key item-index quantity]]
  (update-in
   db
   [:character ::entity/values custom-equipment-key item-index]
   (fn [item-cfg]
     ;; the select keys here is to keep :equipped and :name while wiping out the starting-equipment indicators
     (assoc
      (select-keys item-cfg [:name :equipped?])
      :quantity
      quantity))))

(reg-event-db
 :change-custom-inventory-item-quantity
 change-custom-inventory-item-quantity)

(defn remove-inventory-item [db [_ selection-key item-key]]
  (update-in
   db
   [:character ::entity/options selection-key]
   (fn [items] (vec (remove #(= item-key (::entity/key %)) items)))))

(reg-event-db
 :remove-inventory-item
 remove-inventory-item)

(defn remove-custom-inventory-item [db [_ custom-equipment-key name]]
  (update-in
   db
   [:character ::entity/values custom-equipment-key]
   (fn [items]
     (vec (remove #(= name (:name %)) items)))))

(reg-event-db
 :remove-custom-inventory-item
 remove-custom-inventory-item)

(defn set-abilities [db [_ abilities]]
  (assoc-in db [:character ::entity/options :ability-scores ::entity/value] abilities))

(reg-event-db
 :set-abilities
 set-abilities)

(defn swap-ability-values [db [_ i other-i k v]]
  (update-in
   db
   [:character ::entity/options :ability-scores ::entity/value]
   (fn [a]
     (let [a-vec (vec a)
           other-index (mod other-i (count a-vec))
           [other-k other-v] (a-vec other-index)]
       (assoc a k other-v other-k v)))))

(reg-event-db
 :swap-ability-values
 swap-ability-values)

(defn decrease-ability-value [db [_ full-path k]]
  (update-in
   db
   full-path
   (fn [incs]
     (common/remove-first
      (fn [{inc-key ::entity/key}]
        (= inc-key k))
      incs))))

(reg-event-db
 :decrease-ability-value
 decrease-ability-value)

(defn increase-ability-value [db [_ full-path k]]
  (update-in
   db
   full-path
   conj
   {::entity/key k}))

(reg-event-db
 :increase-ability-value
 increase-ability-value)

(defn set-ability-score [db [_ ability-kw v]]
  (assoc-in db [:character ::entity/options :ability-scores ::entity/value ability-kw] v))

(reg-event-db
 :set-ability-score
 set-ability-score)

(defn set-ability-score-variant [db [_ variant-key]]
  (assoc-in db [:character ::entity/options :ability-scores ::entity/key] variant-key))

(reg-event-db
 :set-ability-score-variant
 set-ability-score-variant)

(defn select-skill [db [_ path selected? skill-key]]
  (js/console.log "SELECT SKILL" path selected? skill-key)
  (update-in
   db
   (concat [:character] path)
   (fn [skills]
     (if selected?                                             
       (vec (remove (fn [s] (= skill-key (::entity/key s))) skills))
       (vec (conj skills {::entity/key skill-key}))))))

(reg-event-db
 :select-skill
 select-skill)

(defn set-total-hps [db [_ full-path first-selection selection average-value remainder]]
  (assoc-in
   db
   full-path
   {::entity/key :manual-entry
    ::entity/value (if (= first-selection selection)
                     (+ average-value remainder)
                     average-value)}))

(reg-event-db
 :set-total-hps
 set-total-hps)

(defn randomize-hit-points [db [_ built-template character path levels class-kw]]
  (assoc-in
   db
   (concat [:character] (entity/get-entity-path built-template character path))
   {::entity/key :roll
    ::entity/value (dice/die-roll (-> levels class-kw :hit-die))}))

(reg-event-db
 :randomize-hit-points
 randomize-hit-points)

(defn set-hit-points-to-average [db [_ built-template character path levels class-kw]]
  (assoc-in
   db
   (concat [:character] (entity/get-entity-path built-template character path))
   {::entity/key :average
    ::entity/value (dice/die-mean (-> levels class-kw :hit-die))}))

(reg-event-db
 :set-hit-points-to-average
 set-hit-points-to-average)

(defn set-level-hit-points [db [_ built-template character level-value value]]
  (assoc-in
   db
   (concat [:character] (entity/get-entity-path built-template character (:path level-value)))
   {::entity/key :manual-entry
    ::entity/value (if (not (js/isNaN value)) value)}))

(reg-event-db
 :set-level-hit-points
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

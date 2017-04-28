(ns orcpub.dnd.e5.events
  (:require [orcpub.entity :as entity]
            [orcpub.template :as t]
            [orcpub.common :as common]
            [orcpub.dice :as dice]
            [orcpub.dnd.e5.template :as t5e]))

(defn reset-character [db []]
  (assoc db :character t5e/character :page 0))

(defn set-character [db [character]]
  (assoc db :character character))

(def character-values-path
  [:character ::entity/values])

(defn character-value-path [prop-name]
  (conj character-values-path prop-name))

(defn update-value-field [db [prop-name value]]
  (assoc-in db (character-value-path prop-name) value))

(defn add-class [db [first-unselected]]
  (update-in
   db
   [:character ::entity/options :class]
   conj
   {::entity/key first-unselected ::entity/options {:levels [{::entity/key :level-1}]}}))

(defn set-class [db [class-key class-index options-map]]
  (let [new-class-option (options-map class-key)
        associated-options (::t/associated-options new-class-option)
        with-new-class (assoc-in
                        db
                        [:character ::entity/options :class class-index]
                        {::entity/key class-key
                         ::entity/options
                         {:levels [{::entity/key :level-1}]}})
        without-starting-equipment (t5e/remove-starting-equipment with-new-class :class-starting-equipment)]
    (if (zero? class-index)
      (t5e/add-associated-options without-starting-equipment associated-options)
      with-new-class)))

(defn set-class-level [db [class-index new-highest-level]]
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

(defn delete-class [db [class-key]]
  (update-in
   db
   [:character ::entity/options :class]
   (fn [classes] (vec (remove #(= class-key (::entity/key %)) classes)))))

(defn add-inventory-item [db [selection-key item-key]]
  (update-in
   db
   [:character ::entity/options selection-key]
   (fn [items]
     (vec
      (conj
       items
       {::entity/key item-key
        ::entity/value {:quantity 1 :equipped? true}})))))

(defn toggle-inventory-item-equipped [db [selection-key item-index]]
  (update-in
   db
   [:character ::entity/options selection-key item-index ::entity/value :equipped?]
   not))

(defn toggle-custom-inventory-item-equipped [db [custom-equipment-key item-index]]
  (update-in
   db
   [:character ::entity/values custom-equipment-key item-index :equipped?]
   not))

(defn change-inventory-item-quantity [db [selection-key item-index quantity]]
  (update-in
   db
   [:character ::entity/options selection-key item-index ::entity/value]
   (fn [item-cfg]
     ;; the select keys here is to keep :equipped while wiping out the starting-equipment indicators
     (assoc (select-keys item-cfg [:equipped?]) :quantity quantity))))

(defn change-custom-inventory-item-quantity [db [custom-equipment-key item-index quantity]]
  (update-in
   db
   [:character ::entity/values custom-equipment-key item-index]
   (fn [item-cfg]
     ;; the select keys here is to keep :equipped and :name while wiping out the starting-equipment indicators
     (assoc
      (select-keys item-cfg [:name :equipped?])
      :quantity
      quantity))))

(defn remove-inventory-item [db [selection-key item-key]]
  (update-in
   db
   [:character ::entity/options selection-key]
   (fn [items] (vec (remove #(= item-key (::entity/key %)) items)))))

(defn remove-custom-inventory-item [db [custom-equipment-key name]]
  (update-in
   db
   [:character ::entity/values custom-equipment-key]
   (fn [items]
     (vec (remove #(= name (:name %)) items)))))

(defn set-abilities [db [abilities]]
  (assoc-in db [:character ::entity/options :ability-scores ::entity/value] abilities))

(defn swap-ability-values [db [i other-i k v]]
  (update-in
   db
   [:character ::entity/options :ability-scores ::entity/value]
   (fn [a]
     (let [a-vec (vec a)
           other-index (mod other-i (count a-vec))
           [other-k other-v] (a-vec other-index)]
       (assoc a k other-v other-k v)))))

(defn decrease-ability-value [db [full-path k]]
  (update-in
   db
   full-path
   (fn [incs]
     (common/remove-first
      (fn [{inc-key ::entity/key}]
        (= inc-key k))
      incs))))

(defn increase-ability-value [db [full-path k]]
  (update-in
   db
   full-path
   conj
   {::entity/key k}))

(defn set-ability-score [db [ability-kw v]]
  (assoc-in db [:character ::entity/options :ability-scores ::entity/value ability-kw] v))

(defn set-ability-score-variant [db [variant-key]]
  (assoc-in db [:character ::entity/options :ability-scores ::entity/key] variant-key))

(defn select-skill [db [path selected? skill-key]]
  (update-in
   db
   path
   (fn [skills]
     (if selected?                                             
       (vec (remove (fn [s] (= skill-key (::entity/key s))) skills))
       (vec (conj skills {::entity/key skill-key}))))))

(defn set-total-hps [db [full-path first-selection selection average-value remainder]]
  (assoc-in
   db
   full-path
   {::entity/key :manual-entry
    ::entity/value (if (= first-selection selection)
                     (+ average-value remainder)
                     average-value)}))

(defn randomize-hit-points [db [built-template character path levels class-kw]]
  (assoc-in
   db
   (concat [:character] (entity/get-entity-path built-template character path))
   {::entity/key :roll
    ::entity/value (dice/die-roll (-> levels class-kw :hit-die))}))

(defn set-hit-points-to-average [db [built-template character path levels class-kw]]
  (assoc-in
   db
   (concat [:character] (entity/get-entity-path built-template character path))
   {::entity/key :average
    ::entity/value (dice/die-mean (-> levels class-kw :hit-die))}))

(defn set-level-hit-points [db [built-template character level-value value]]
  (assoc-in
   db
   (concat [:character] (entity/get-entity-path built-template character (:path level-value)))
   {::entity/key :manual-entry
    ::entity/value (if (not (js/isNaN value)) value)}))

(defn set-page [db [page-index]]
  (assoc db :page page-index))

(def tab-path [:builder :character :tab])

(defn set-active-tabs [db [active-tabs]]
  (assoc-in db tab-path active-tabs))

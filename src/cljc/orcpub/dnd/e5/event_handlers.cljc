(ns orcpub.dnd.e5.event-handlers
  (:require [orcpub.entity :as entity]
            [orcpub.dnd.e5.character :as char5e]
            [orcpub.dnd.e5.character.equipment :as char-equip5e]))

(defn set-class-level [character [_ class-index new-highest-level]]
  (update-in
   character
   [::entity/options :class class-index ::entity/options :levels]
   (fn [levels]
     (let [current-highest-level (count levels)]
       (with-meta ;; ensure that db/id meta gets copied over
         (cond
           (> new-highest-level current-highest-level)
           (vec (concat levels (map
                                (fn [lvl] {::entity/key (keyword (str "level-" (inc lvl)))})
                                (range current-highest-level new-highest-level))))
         
           (< new-highest-level current-highest-level)
           (vec (take new-highest-level levels))
         
           :else levels)
         (meta levels))))))

(defn set-class [character [_ class-key class-index options-map]]
  (char5e/set-class character class-key class-index (options-map class-key)))

(def custom-equipment-path [::entity/values ::char5e/custom-equipment])

(def custom-treasure-path [::entity/values ::char5e/custom-treasure])

(defn starting-equipment-entity-option [indicator-key [k num]]
  {::entity/key k
   ::entity/value {::char-equip5e/quantity num
                   ::char-equip5e/equipped? true
                   indicator-key true}})

(defn starting-equipment-entity-options [indicator-key key items]
  (if items
    {key
     (mapv
      (partial starting-equipment-entity-option indicator-key)
      items)}))

(defn background-starting-equipment-entity-options [key items]
  (starting-equipment-entity-options ::char-equip5e/background-starting-equipment? key items))

(defn add-background-starting-equipment [character [_ background-cfg]]
  (let [{:keys [weapons armor equipment treasure custom-treasure custom-equipment]} background-cfg
        equipment-options (remove
                           nil?
                           [(background-starting-equipment-entity-options :weapons weapons)
                            (background-starting-equipment-entity-options :armor armor)
                            (background-starting-equipment-entity-options :equipment equipment)
                            (background-starting-equipment-entity-options :treasure treasure)])]
    (-> character
        (char5e/remove-starting-equipment ::char-equip5e/background-starting-equipment?)
        (char5e/add-associated-options equipment-options)
        (char5e/remove-custom-starting-equipment ::char-equip5e/background-starting-equipment? custom-treasure-path)
        (char5e/add-custom-equipment custom-treasure custom-treasure-path)
        (char5e/remove-custom-starting-equipment ::char-equip5e/background-starting-equipment? custom-equipment-path)
        (char5e/add-custom-equipment custom-equipment custom-equipment-path))))

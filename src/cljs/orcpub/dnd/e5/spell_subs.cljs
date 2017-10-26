(ns orcpub.dnd.e5.spell-subs
  (:require [re-frame.core :refer [reg-sub reg-sub-raw dispatch subscribe]]
            [orcpub.common :as common]
            [orcpub.template :as t]
            [orcpub.dnd.e5 :as e5]
            [orcpub.dnd.e5.modifiers :as mod5e]
            [orcpub.dnd.e5.magic-items :as mi5e]
            [orcpub.dnd.e5.character :as char5e]
            [orcpub.dnd.e5.weapons :as weapon5e]
            [orcpub.dnd.e5.spells :as spells5e]
            [orcpub.dnd.e5.spell-lists :as sl5e]
            [orcpub.dnd.e5.armor :as armor5e]
            [orcpub.dnd.e5.template :as t5e]
            [orcpub.dnd.e5.equipment :as equipment5e]
            [orcpub.route-map :as routes]
            [orcpub.dnd.e5.events :as events]
            [reagent.ratom :as ra]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(reg-sub
 ::e5/plugins
 (fn [db _]
   (get db :plugins)))

(reg-sub
 ::e5/plugin-vals
 :<- [::e5/plugins]
 (fn [plugins]
   (vals plugins)))

(reg-sub
 ::spells5e/plugin-spells
 :<- [::e5/plugin-vals]
 (fn [plugins _]
   (apply concat (map (comp vals ::e5/spells) plugins))))

(reg-sub
 ::spells5e/spells
 :<- [::spells5e/plugin-spells]
 (fn [plugin-spells]
   (concat
    spells5e/spells
    plugin-spells)))

(reg-sub
 ::spells5e/spells-map
 :<- [::spells5e/spells]
 (fn [spells]
   (reduce
    (fn [m {:keys [name key level] :as spell}]
      (assoc m (or key (common/name-to-kw name)) spell))
    {}
    spells)))

(defn merge-spell-lists [& spell-lists]
  (apply
   merge-with
   concat
   spell-lists))

(reg-sub
 ::spells5e/plugin-spell-lists
 :<- [::spells5e/plugin-spells]
 (fn [plugin-spells _]
   (reduce
    (fn [lists {:keys [key level spell-lists]}]
      (reduce-kv
       (fn [l k v]
         (update-in l [k level] conj key))
       lists
       spell-lists))
    {}
    plugin-spells)))

(reg-sub
 ::spells5e/spell-lists
 :<- [::spells5e/plugin-spell-lists]
 (fn [plugin-spell-lists]
   (merge-with
    merge-spell-lists
    sl5e/spell-lists
    plugin-spell-lists)))

(reg-sub
 ::spells5e/spellcasting-classes
 (fn []
   (map
    (fn [kw]
      {:key kw
       :name (common/kw-to-name kw)})
    [:bard :cleric :druid :paladin :ranger :sorcerer :warlock :wizard])))

(defn spell-option [spells-map [_ spell-key ability-key class-name]]
   (let [spell (spells-map spell-key)
         level (:level spell)]
     (t/option-cfg
      {:name (str level " - " (:name spell))
       :key spell-key
       :modifiers [(mod5e/spells-known
                    (:level spell)
                    spell-key
                    ability-key
                    class-name)]})))

(reg-sub
 ::spells5e/spell-option
 :<- [::spells5e/spells-map]
 spell-option)

(reg-sub
 ::spells5e/spell-options
 :<- [::spells5e/spells-map]
 :<- [::spells5e/spell-lists]
 (fn [[spells-map spell-lists] [_ ability-key class-name levels]]
   (apply concat
          (sequence
           (comp
            (map spell-lists)
            (map (fn [spell-key]
                   (spell-option spells-map [nil spell-key ability-key class-name]))))
           levels))))

(reg-sub
 ::spells5e/builder-item
 (fn [db _]
   (::spells5e/builder-item db)))

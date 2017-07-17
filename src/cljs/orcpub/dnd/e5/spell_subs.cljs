(ns orcpub.dnd.e5.spell-subs
  (:require [re-frame.core :refer [reg-sub reg-sub-raw dispatch subscribe]]
            [orcpub.common :as common]
            [orcpub.template :as t]
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
 ::spells5e/spells
 :<- [:selected-plugins]
 (fn [selected-plugins]
   (apply
    concat
    spells5e/spells
    (map
     :spells
     selected-plugins))))

(reg-sub
 ::spells5e/spells-map
 :<- [::spells5e/spells]
 (fn [spells]
   (reduce
    (fn [m {:keys [name key level] :as spell}]
      (assoc m (or key (common/name-to-kw name)) spell))
    {}
    spells)))

(defn merge-class-lists [class-list-1 class-list-2]
  (merge-with
   concat
   class-list-1
   class-list-2))

(reg-sub
 ::spells5e/spell-lists
 :<- [:selected-plugins]
 (fn [selected-plugins]
   (apply
    merge-with
    merge-class-lists
    sl5e/spell-lists
    (map
     :spell-lists
     selected-plugins))))

(ns orcpub.dnd.e5.equipment-subs
  (:require [re-frame.core :refer [reg-sub reg-sub-raw dispatch subscribe]]
            [orcpub.common :as common]
            [orcpub.template :as t]
            [orcpub.dnd.e5.spell-subs]
            [orcpub.dnd.e5.modifiers :as mod5e]
            [orcpub.dnd.e5.magic-items :as mi5e]
            [orcpub.dnd.e5.character :as char5e]
            [orcpub.dnd.e5.backgrounds :as bg5e]
            [orcpub.dnd.e5.languages :as langs5e]
            [orcpub.dnd.e5.feats :as feats5e]
            [orcpub.dnd.e5.races :as races5e]
            [orcpub.dnd.e5.classes :as classes5e]
            [orcpub.dnd.e5.weapons :as weapon5e]
            [orcpub.dnd.e5.armor :as armor5e]
            [orcpub.dnd.e5.template :as t5e]
            [orcpub.dnd.e5.equipment :as equipment5e]
            [orcpub.dnd.e5.spells :as spells5e]
            [orcpub.route-map :as routes]
            [orcpub.dnd.e5.events :refer [url-for-route] :as events]
            [reagent.ratom :as ra]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def sorted-items
  (delay (sort-by mi5e/name-key mi5e/magic-items)))

(defn auth-headers [db]
  (let [token (-> db :user-data :token)]
    (if token
      {"Authorization" (str "Token " token)}
      {})))

(if js/window.location
  (reg-sub-raw
   ::mi5e/custom-items
   (fn [app-db [_ user-data]]
     (go (dispatch [:set-loading true])
         (let [response (<! (http/get (url-for-route routes/dnd-e5-items-route)
                                      {:headers (auth-headers @app-db)}))]
           (dispatch [:set-loading false])
           (case (:status response)
             200 (dispatch [::mi5e/set-custom-items (:body response)])
             401 nil ;;(dispatch [:route routes/login-page-route {:secure? true}])
             500 (dispatch (events/show-generic-error)))))
     (ra/make-reaction
      (fn [] (get @app-db ::mi5e/custom-items [])))))
  (reg-sub
   ::mi5e/custom-items
   (fn [_ _] [])))

(reg-sub
 ::mi5e/expanded-custom-items
 :<- [::mi5e/custom-items]
 (fn [custom-items _]
   (mi5e/expand-magic-items custom-items)))

(reg-sub
 ::mi5e/custom-item-map
 :<- [::mi5e/expanded-custom-items]
 (fn [custom-items _]
   (common/map-by-id custom-items)))

(reg-sub
 ::mi5e/custom-item
 :<- [::mi5e/custom-item-map]
 (fn [custom-item-map [_ id]]
   (get custom-item-map id)))

(reg-sub
 ::char5e/sorted-items
 :<- [::mi5e/expanded-custom-items]
 (fn [custom-items _]
   (concat
    custom-items
    @sorted-items)))

(reg-sub
 ::mi5e/custom-weapons
 :<- [::mi5e/expanded-custom-items]
 (fn [custom-items _]
   (sequence
    mi5e/magic-weapon-xform
    custom-items)))

(reg-sub
 ::mi5e/custom-and-standard-weapons
 :<- [::mi5e/custom-weapons]
 (fn [custom-weapons _]
   (concat
    (map
     (fn [{:keys [::mi5e/name] :as i}]
       (assoc i :name name))
     custom-weapons) weapon5e/weapons)))

(reg-sub
 ::mi5e/custom-and-standard-weapons-map
 :<- [::mi5e/custom-and-standard-weapons]
 (fn [custom-and-standard-weapons _]
   (common/map-by-key custom-and-standard-weapons)))

(reg-sub
 ::mi5e/magic-weapons
 :<- [::char5e/sorted-items]
 (fn [sorted-items _]
   (sequence
    mi5e/magic-weapon-xform
    sorted-items)))

(reg-sub
 ::mi5e/all-weapons
 :<- [::mi5e/magic-weapons]
 (fn [magic-weapons]
   (concat magic-weapons weapon5e/weapons)))

(reg-sub
 ::mi5e/all-melee-weapons
 :<- [::mi5e/all-weapons]
 (fn [all-weapons]
   (filter
    ::weapon5e/melee?
    all-weapons)))

(defn map-by-key-or-id [items]
  (reduce
   (fn [m {:keys [:db/id key] :as item}]
     (assoc m
            key item
            id item))
   {}
   items))

(reg-sub
 ::mi5e/magic-weapon-map
 :<- [::mi5e/magic-weapons]
 (fn [magic-weapons _]
   (map-by-key-or-id magic-weapons)))

(defn magic-item-options [modifier-fn nm]
  (fn [items _]
    (map
     (fn [{:keys [:db/id
                  ::mi5e/name
                  key
                  ::mi5e/description
                  ::mi5e/page
                  ::mi5e/modifiers
                  ::mi5e/source] :as item}]
       (let [item-key (or key (keyword (str "id-" id)))
             full-item (update item
                               ::mi5e/modifiers
                               mod5e/build-modifiers)]
         (t/option-cfg
          {:name (or (:name item) name)
           :key item-key
           :help (if (or description
                         page)
                   (t5e/inventory-help description page source))
           :modifiers [(modifier-fn
                        item-key
                        full-item)]})))
     items)))

(reg-sub
 ::mi5e/magic-weapon-options
 :<- [::mi5e/magic-weapons]
 (magic-item-options mod5e/deferred-magic-weapon "Magic Weapon"))

(reg-sub
 ::mi5e/magic-armor-options
 :<- [::mi5e/magic-armor]
 (magic-item-options mod5e/deferred-magic-armor "Magic Armor"))

(reg-sub
 ::mi5e/other-magic-item-options
 :<- [::mi5e/other-magic-items]
 (magic-item-options mod5e/deferred-magic-item "Magic Item"))

(reg-sub
 ::mi5e/magic-armor
 :<- [::char5e/sorted-items]
 (fn [sorted-items _]
   (sequence
    mi5e/magic-armor-xform
    sorted-items)))

(reg-sub
 ::mi5e/magic-armor-map
 :<- [::mi5e/magic-armor]
 (fn [magic-armor _]
   (map-by-key-or-id magic-armor)))

(reg-sub
 ::mi5e/other-magic-items
 :<- [::char5e/sorted-items]
 (fn [sorted-items _]
   (sequence
    mi5e/other-magic-items-xform
    sorted-items)))

(reg-sub
 ::mi5e/all-armor-map
 :<- [::mi5e/magic-armor-map]
 (fn [magic-armor-map]
   (merge
    magic-armor-map
    armor5e/armor-map)))

(reg-sub
 ::mi5e/other-magic-items-map
 :<- [::mi5e/other-magic-items]
 (fn [magic-items _]
   (map-by-key-or-id magic-items)))

(reg-sub
 ::equipment5e/weapons-map
 (fn [_ _]
   weapon5e/weapons-map))

(reg-sub
 ::mi5e/all-weapons-map
 :<- [::mi5e/magic-weapon-map]
 (fn [magic-weapons-map]
   (merge
    magic-weapons-map
    weapon5e/weapons-map)))

(reg-sub
 ::mi5e/all-magic-items-map
 :<- [::mi5e/magic-weapon-map]
 :<- [::mi5e/magic-armor-map]
 :<- [::mi5e/other-magic-items-map]
 (fn [maps _]
   (apply merge
          mi5e/all-magic-items-map
          maps)))

(reg-sub
 ::mi5e/remote-items
 (fn [db _]
   (::mi5e/remote-items db)))

(reg-sub-raw
 ::mi5e/remote-item
 (fn [app-db [_ id]]
   (go (dispatch [:set-loading true])
       (let [response (<! (http/get (url-for-route
                                      routes/dnd-e5-item-route
                                      :id id)
                                    {:headers (auth-headers @app-db)}))]
         (dispatch [:set-loading false])
         (case (:status response)
           200 (dispatch [::mi5e/add-remote-item (:body response)])
           500 (dispatch (events/show-generic-error)))))
   (ra/make-reaction
    (fn [] (get-in @app-db [::mi5e/remote-items id] {})))))

(reg-sub
 ::mi5e/item
 (fn [item [_ key]]
   (if (int? key)
     @(subscribe [::mi5e/remote-item key])
     (get mi5e/all-equipment-map key))))

(reg-sub
 ::equipment5e/armor-map
 (fn [_ _]
   armor5e/armor-map))

(reg-sub
 ::equipment5e/equipment-map
 (fn [_ _]
   equipment5e/equipment-map))

(reg-sub
 ::equipment5e/treasure-map
 (fn [_ _]
   equipment5e/treasure-map))

(reg-sub
 ::char5e/template-selections
 :<- [::mi5e/magic-weapon-options]
 :<- [::mi5e/magic-armor-options]
 :<- [::mi5e/other-magic-item-options]
 :<- [::mi5e/all-weapons-map]
 :<- [::mi5e/custom-and-standard-weapons]
 :<- [::spells5e/spell-lists]
 :<- [::spells5e/spells-map]
 :<- [::bg5e/backgrounds]
 :<- [::races5e/races]
 :<- [::classes5e/classes]
 :<- [::feats5e/feats]
 :<- [::langs5e/language-map]
 (fn [[magic-weapon-options
       magic-armor-options
       other-magic-item-options
       weapons-map
       custom-and-standard-weapons
       spell-lists
       spells-map
       backgrounds
       races
       classes
       feats
       language-map] _]
   (t5e/template-selections magic-weapon-options
                            magic-armor-options
                            other-magic-item-options
                            weapons-map
                            custom-and-standard-weapons
                            spell-lists
                            spells-map
                            backgrounds
                            races
                            classes
                            feats
                            language-map)))

(reg-sub
 ::char5e/template
 :<- [::char5e/template-selections]
 (fn [template-selections _]
   (t5e/template template-selections)))

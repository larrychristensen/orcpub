(ns orcpub.dnd.e5.equipment-subs
  (:require [re-frame.core :refer [reg-sub reg-sub-raw dispatch subscribe]]
            [orcpub.common :as common]
            [orcpub.dnd.e5.magic-items :as mi5e]
            [orcpub.dnd.e5.character :as char5e]
            [orcpub.dnd.e5.weapons :as weapon5e]
            [orcpub.dnd.e5.armor :as armor5e]
            [orcpub.dnd.e5.equipment :as equipment5e]
            [orcpub.route-map :as routes]
            [orcpub.dnd.e5.events :as events]
            [reagent.ratom :as ra]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def sorted-items
  (delay (sort-by mi5e/name-key mi5e/magic-items)))

(defn auth-headers [db]
  {"Authorization" (str "Token " (-> db :user-data :token))})

(reg-sub-raw
 ::mi5e/custom-items
 (fn [app-db [_ user-data]]
   (go (dispatch [:set-loading true])
       (let [response (<! (http/get (routes/path-for routes/dnd-e5-items-route)
                                    {:accept :transit
                                     :headers (auth-headers @app-db)}))]
         (dispatch [:set-loading false])
         (case (:status response)
           200 (dispatch [::mi5e/set-custom-items (-> response :body)])
           401 (dispatch [:route routes/login-page-route {:secure? true}])
           500 (dispatch (events/show-generic-error)))))
   (ra/make-reaction
    (fn [] (get @app-db ::mi5e/custom-items [])))))

(reg-sub
 ::mi5e/custom-item-map
 (fn [db _]
   (::mi5e/custom-item-map db)))

(reg-sub
 ::mi5e/custom-item
 :<- [::mi5e/custom-item-map]
 (fn [custom-item-map [_ id]]
   (get custom-item-map id)))

(reg-sub
 ::char5e/sorted-items
 (fn [_ _]
   (subscribe [::mi5e/custom-items (subscribe [:user-data])]))
 (fn [custom-items _]
   (concat
    custom-items
    @sorted-items)))

(reg-sub
 ::mi5e/magic-weapons
 :<- [::char5e/sorted-items]
 (fn [sorted-items _]
   (sequence
    mi5e/magic-weapon-xform
    sorted-items)))

(defn map-by-key-or-id [items]
  (reduce
   (fn [m {:keys [:db/id key] :as item}]
     (assoc m (or key (keyword (str "id-" id))) item))
   {}
   items))

(reg-sub
 ::mi5e/magic-weapon-map
 :<- [::mi5e/magic-weapons]
 (fn [magic-weapons _]
   (map-by-key-or-id magic-weapons)))

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
 ::mi5e/other-magic-items-map
 :<- [::mi5e/other-magic-items]
 (fn [magic-items _]
   (map-by-key-or-id magic-items)))

(reg-sub
 ::equipment5e/weapons-map
 (fn [_ _]
   weapon5e/weapons-map))

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

(ns orcpub.dnd.e5.equipment-subs
  (:require [re-frame.core :refer [reg-sub reg-sub-raw dispatch]]
            [orcpub.dnd.e5.magic-items :as mi5e]
            [orcpub.dnd.e5.character :as char5e]
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
  (fn [app-db [_]]
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
 ::char5e/sorted-items
 :<- [::mi5e/custom-items]
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

(reg-sub
 ::mi5e/magic-armor
 :<- [::char5e/sorted-items]
 (fn [sorted-items _]
   (sequence
    mi5e/magic-armor-xform
    sorted-items)))

(reg-sub
 ::mi5e/other-magic-items
 :<- [::char5e/sorted-items]
 (fn [sorted-items _]
   (sequence
    mi5e/other-magic-items-xform
    sorted-items)))

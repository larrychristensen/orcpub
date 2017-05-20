(ns orcpub.dnd.e5.subs
  (:require [re-frame.core :refer [reg-sub reg-sub-raw subscribe dispatch]]
            [orcpub.entity :as entity]
            [orcpub.template :as t]
            [orcpub.registration :as registration]
            [orcpub.dnd.e5.template :as t5e]
            [orcpub.dnd.e5.db :refer [tab-path]]
            [orcpub.route-map :as routes]
            [clojure.string :as s]
            [reagent.ratom :as ra]
            [cljs.core.async :refer [<!]]
            [cljs-http.client :as http])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(reg-sub
 :registration-form
 (fn [db [_]]
   (get db :registration-form)))

(reg-sub
 :username-taken?
 (fn [db [_]]
   (get db :username-taken?)))

(reg-sub
 :email-taken?
 (fn [db [_]]
   (get db :email-taken?)))

(reg-sub
 :registration-validation
 :<- [:registration-form]
 :<- [:email-taken?]
 :<- [:username-taken?]
 (fn [args [_]]
   (apply registration/validate-registration args)))

(reg-sub
 :temp-email
 (fn [db [_]]
   (get db :temp-email)))

(reg-sub
 :locked
 (fn [db [_ path]]
   (get-in db [:locked-components path])))

(reg-sub
 :locked-components
 (fn [db []]
   (get db :locked-components)))

(reg-sub
 :loading
 (fn [db _]
   (get db :loading)))

(reg-sub
 :active-tabs
 (fn [db _]
   (get-in db tab-path)))

(reg-sub
 :character
 (fn [db _]
   (:character db)))

(reg-sub
 :entity-values
 :<- [:character]
 (fn [character _]
   (get-in character [::entity/values])))

(reg-sub
 :option-paths
 :<- [:character]
 (fn [character _]
   (entity/make-path-map character)))

(defn selected-plugin-options [character]
  (into #{}
        (comp (map ::entity/key)
              (remove nil?))
        (get-in character [::entity/options :optional-content])))

(reg-sub
 :selected-plugin-options
 :<- [:character]
 (fn [character _]
   (selected-plugin-options character)))

(reg-sub
 :available-selections
 :<- [:character]
 :<- [:built-character]
 :<- [:built-template]
 (fn [[character built-character built-template]]
   (entity/available-selections character built-character built-template)))

(reg-sub
 :template
 (fn [db _]
   (:template db)))

(reg-sub
 :plugins
 (fn [db _]
   (:plugins db)))

(reg-sub
 :page
 (fn [db _]
   (:page db)))

(reg-sub
 :route
 (fn [db _]
   (:route db)))

(reg-sub
 :previous-route
 (fn [db _]
   (-> db :route-history peek)))

(reg-sub
 :user-data
 (fn [db _]
   (:user-data db)))

(reg-sub
 :username
 (fn [db _]
   (-> db :user-data :user-data :username)))

(defn built-template [selected-plugin-options]
  (let [selected-plugins (map
                          :selections
                          (filter
                           (fn [{:keys [key]}]
                             (selected-plugin-options key))
                           t5e/plugins))]
    (if (seq selected-plugins)
      (update t5e/template
              ::t/selections
              (fn [s]
                (apply
                 entity/merge-multiple-selections
                 s
                 selected-plugins)))
      t5e/template)))

(reg-sub
 :built-template
 :<- [:selected-plugin-options]
 (fn [selected-plugin-options _]
   (built-template selected-plugin-options)))

(defn built-character [character built-template]
  (entity/build character built-template))

(reg-sub
 :built-character
 :<- [:character]
 :<- [:built-template]
 (fn [[character built-template] _]
   (built-character character built-template)))

(reg-sub
 :expanded-characters
 (fn [db _]
   (:expanded-characters db)))

(reg-sub-raw
  :dnd-5e-characters
  (fn [app-db [_]]
    (go (let [response (<! (http/get (routes/path-for routes/dnd-e5-char-list-route)
                                     {:accept :transit
                                      :headers {"Authorization" (str "Token " (-> @app-db :user-data :token))}}))]
          (dispatch [:set-dnd-5e-characters (-> response :body)])))
    (ra/make-reaction
     (fn [] (get-in @app-db [:dnd :e5 :characters] [])))))

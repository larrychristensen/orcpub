(ns orcpub.dnd.e5.subs
  (:require [re-frame.core :refer [reg-sub reg-sub-raw subscribe dispatch]]
            [orcpub.entity :as entity]
            [orcpub.template :as t]
            [orcpub.registration :as registration]
            [orcpub.dnd.e5.template :as t5e]
            [orcpub.dnd.e5.db :refer [tab-path]]
            [orcpub.dnd.e5.events :as events]
            [orcpub.dnd.e5.character :as char5e]
            [orcpub.route-map :as routes]
            [clojure.string :as s]
            [reagent.ratom :as ra]
            [cljs.core.async :refer [<!]]
            [cljs-http.client :as http])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(reg-sub
 :db
 (fn [db _]
   db))

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
  ::char5e/characters
  (fn [app-db [_]]
    (go (dispatch [:set-loading true])
        (let [response (<! (http/get (routes/path-for routes/dnd-e5-char-list-route)
                                     {:accept :transit
                                      :headers {"Authorization" (str "Token " (-> @app-db :user-data :token))}}))]
          (dispatch [:set-loading false])
          (case (:status response)
            200 (dispatch [::char5e/set-characters (-> response :body)])
            401 (dispatch [:route routes/login-page-route {:secure? true}])
            500 (dispatch (events/show-generic-error)))))
    (ra/make-reaction
     (fn [] (get @app-db ::char5e/characters [])))))

(reg-sub
 ::char5e/character-map
 (fn [db _]
   (::char5e/character-map db)))

(reg-sub-raw
  ::char5e/character
  (fn [app-db [_ id :as args]]
    (if (nil? (get-in @app-db [::char5e/character-map id]))
      (go (dispatch [:set-loading true])
          (let [response (<! (http/get (routes/path-for routes/dnd-e5-char-route :id id)
                                       {:accept :transit}))]
            (dispatch [:set-loading false])
            (case (:status response)
              200 (dispatch [::char5e/set-character id (-> response :body)])
              401 (dispatch [:route routes/login-page-route {:secure? true}])
              500 (dispatch (events/show-generic-error))))))
    (ra/make-reaction
     (fn [] (get-in @app-db [::char5e/character-map id] [])))))

(reg-sub
 ::char5e/internal-character
 (fn [[_ id] _]
   (subscribe [::char5e/character id])) 
 (fn [character _ _]
   (char5e/from-strict character)))

(reg-sub
 ::char5e/selected-plugin-options
 (fn [[_ id] _]
   (subscribe [::char5e/internal-character id]))
 (fn [internal-character _ _]
   (selected-plugin-options internal-character)))

(reg-sub
 ::char5e/template
 (fn [db _]
   (:template db)))

(reg-sub
 ::char5e/built-template
 (fn [[_ id] _]
   (subscribe [::char5e/selected-plugin-options id]))
 (fn [selected-plugin-options _]
   (built-template selected-plugin-options)))

(reg-sub
 ::char5e/built-character
 (fn [[_ id] _]
   [(subscribe [::char5e/internal-character id])
    (subscribe [::char5e/built-template id])])
 (fn [[character built-template] _ _]
   (built-character character built-template)))

(reg-sub
 :message-shown?
 (fn [db _]
   (:message-shown? db)))

(reg-sub
 :login-message-shown?
 (fn [db _]
   (:login-message-shown? db)))

(reg-sub
 :message
 (fn [db _]
   (:message db)))

(reg-sub
 :login-message
 (fn [db _]
   (:login-message db)))

(reg-sub
 :message-type
 (fn [db _]
   (:message-type db)))

(reg-sub
 :device-type
 (fn [db _]
   (:device-type db)))

(reg-sub
 :warning-hidden
 (fn [db _]
   (:warning-hidden db)))

(def character-subs
  {::char5e/base-swimming-speed char5e/base-swimming-speed
   ::char5e/base-flying-speed char5e/base-flying-speed
   ::char5e/base-land-speed char5e/base-land-speed
   ::char5e/speed-with-armor char5e/land-speed-with-armor
   ::char5e/unarmored-speed-bonus char5e/unarmored-speed-bonus
   ::char5e/max-hit-points char5e/max-hit-points 
   ::char5e/initiative char5e/initiative 
   ::char5e/passive-perception char5e/passive-perception 
   ::char5e/character-name char5e/character-name 
   ::char5e/proficiency-bonus char5e/proficiency-bonus 
   ::char5e/save-bonuses char5e/save-bonuses 
   ::char5e/saving-throws char5e/saving-throws 
   ::char5e/race char5e/race 
   ::char5e/subrace char5e/subrace 
   ::char5e/alignment char5e/alignment 
   ::char5e/background char5e/background 
   ::char5e/classes char5e/classes 
   ::char5e/levels char5e/levels 
   ::char5e/darkvision char5e/darkvision 
   ::char5e/skill-profs char5e/skill-proficiencies 
   ::char5e/skill-bonuses char5e/skill-bonuses
   ::char5e/skill-expertise char5e/skill-expertise
   ::char5e/tool-profs char5e/tool-proficiencies 
   ::char5e/weapon-profs char5e/weapon-proficiencies 
   ::char5e/armor-profs char5e/armor-proficiencies 
   ::char5e/resistances char5e/damage-resistances 
   ::char5e/damage-immunities char5e/damage-immunities 
   ::char5e/immunities char5e/immunities 
   ::char5e/condition-immunities char5e/condition-immunities 
   ::char5e/languages char5e/languages 
   ::char5e/abilities char5e/ability-values 
   ::char5e/ability-bonuses char5e/ability-bonuses 
   ::char5e/armor-class char5e/base-armor-class 
   ::char5e/armor-class-with-armor char5e/armor-class-with-armor 
   ::char5e/armor char5e/normal-armor-inventory 
   ::char5e/magic-armor char5e/magic-armor-inventory 
   ::char5e/all-armor-inventory char5e/all-armor-inventory 
   ::char5e/spells-known char5e/spells-known
   ::char5e/spells-known-modes char5e/spells-known-modes
   ::char5e/spell-slots char5e/spell-slots 
   ::char5e/spell-modifiers char5e/spell-modifiers 
   ::char5e/weapons char5e/normal-weapons-inventory 
   ::char5e/magic-weapons char5e/magic-weapons-inventory
   ::char5e/equipment char5e/normal-equipment-inventory
   ::char5e/magic-items char5e/magical-equipment-inventory
   ::char5e/traits char5e/traits
   ::char5e/attacks char5e/attacks
   ::char5e/bonus-actions char5e/bonus-actions
   ::char5e/reactions char5e/reactions
   ::char5e/actions char5e/actions
   ::char5e/image-url char5e/image-url
   ::char5e/image-url-failed char5e/image-url-failed
   ::char5e/faction-image-url char5e/faction-image-url
   ::char5e/faction-image-url-failed char5e/faction-image-url-failed})

(doseq [[sub-key char-fn] character-subs]
  (reg-sub
   sub-key
   (fn [[_ id]]
     (if id
       (subscribe [::char5e/built-character id])
       (subscribe [:built-character])))
   (fn [built-char _]
     (char-fn built-char))))

(reg-sub
 ::char5e/all-armor
 (fn [[_ id]]
   [(subscribe [::char5e/magic-armor id])
    (subscribe [::char5e/armor id])])
 (fn [[magic-armor armor] _]
   (merge magic-armor armor)))

(reg-sub
 ::char5e/all-weapons
 (fn [[_ id]]
   [(subscribe [::char5e/magic-weapons id])
    (subscribe [::char5e/weapons id])])
 (fn [[magic-weapons weapons] _]
   (merge magic-weapons weapons)))

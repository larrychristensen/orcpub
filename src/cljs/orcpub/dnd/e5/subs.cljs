(ns orcpub.dnd.e5.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]
            [orcpub.entity :as entity]
            [orcpub.template :as t]
            [orcpub.dnd.e5.template :as t5e]
            [orcpub.dnd.e5.db :refer [tab-path]]
            [clojure.string :as s]))

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

(defn fails-match? [regex value]
  (or (nil? value)
      (nil? (re-matches regex value))))

(defn validate-registration [{:keys [email username password first-and-last-name]} email-taken? username-taken?]
  (let [bad-email-format? (fails-match? #"[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,4}" email)
        password-missing-special-character? (fails-match? #".*[!@#\$%\^&\*].*" password)
        password-missing-number? (fails-match? #".*[0-9].*" password)
        password-missing-uppercase? (fails-match? #".*[A-Z].*" password)
        password-missing-lowercase? (fails-match? #".*[a-z].*" password)
        password-too-short? (or (nil? password) (< (count password) 8))
        username-too-short? (or (nil? username) (< (count username) 3))]
    (cond-> {}
      (s/blank? first-and-last-name) (update :first-and-last-name conj "Name is required")
      email-taken? (update :email conj "Email address is already associated with another account")
      bad-email-format? (update :email conj (if (s/blank? email)
                                              "Email is required"
                                              "Email is not a valid email format"))
      username-taken? (update :username conj "Username is already taken by another user")
      username-too-short? (update :username conj (if (s/blank? username)
                                                   "Username is required"
                                                   "Username must be at least 3 characters"))
      password-missing-lowercase? (update :password conj "Password must have a least one lowercase character")
      password-missing-uppercase? (update :password conj "Password must have a least one uppercase character")
      password-missing-number? (update :password conj "Password must have a least one numeric character")
      password-missing-special-character? (update :password conj "Password must have a least one of the following characters: !, @, #, $, %, ^, &, or *")
      password-too-short? (update :password conj "Password must be at least 8 characters"))))

(reg-sub
 :registration-validation
 :<- [:registration-form]
 :<- [:email-taken?]
 :<- [:username-taken?]
 (fn [args [_]]
   (apply validate-registration args)))

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

(reg-sub
 :selected-plugin-options
 :<- [:character]
 (fn [character _]
   (into #{}
         (comp (map ::entity/key)
               (remove nil?))
         (get-in character [::entity/options :optional-content]))))

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
 :built-template
 :<- [:selected-plugin-options]
 (fn [selected-plugin-options _]
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
       t5e/template))))

(reg-sub
 :built-character
 :<- [:character]
 :<- [:built-template]
 (fn [[character built-template] _]
   (entity/build character built-template)))

(ns orcpub.dnd.e5.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]
            [orcpub.entity :as entity]
            [orcpub.template :as t]
            [orcpub.dnd.e5.template :as t5e]
            [orcpub.dnd.e5.db :refer [tab-path]]))

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
 :built-template
 :<- [:template]
 :<- [:selected-plugin-options]
 (fn [[template selected-plugin-options] _]
   (let [selected-plugins (map
                           :selections
                           (filter
                            (fn [{:keys [key]}]
                              (selected-plugin-options key))
                            t5e/plugins))]
     (if (seq selected-plugins)
       (update template
               ::t/selections
               (fn [s]
                 (apply
                  entity/merge-multiple-selections
                  s
                  selected-plugins)))
       template))))

(reg-sub
 :built-character
 :<- [:character]
 :<- [:built-template]
 (fn [[character built-template] _]
   (entity/build character built-template)))

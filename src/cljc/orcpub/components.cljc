(ns orcpub.components
  (:require [re-frame.core :refer [dispatch]]
            #?(:cljs [reagent.core :refer [atom]])))

(defn checkbox [selected? disable?]
  [:i.fa.fa-check.f-s-14.bg-white.b-color-gray.orange-shadow.m-r-10.pointer.b-1
   {:class-name (str (if selected? "black slight-text-shadow" "white transparent")
                     " "
                     (if disable?
                       "opacity-5"))}])

(defn selection-adder [values on-change]
  [:select.builder-option.builder-option-dropdown
   {:value ""
    :on-change on-change}
   [:option.builder-dropdown-item
    {:value ""
     :disabled true}
    "<select to add>"]
   (doall
    (map
     (fn [{:keys [key name]}]
       ^{:key key}
       [:option.builder-dropdown-item
        {:value key}
        name])
     values))])

(defn input-field []
  (let [state (atom {:timeout nil
                     :temp-val nil})]
    (fn [type value on-change attrs]
      [type
       (merge
        attrs
        {:value (or (:temp-val @state) value "")
         :on-change (fn [e] #?(:cljs
                               (swap! state
                                      (fn [{:keys [timeout temp-val] :as s}]
                                        (if timeout
                                          (js/clearTimeout timeout))
                                        (let [v (.. e -target -value)]
                                          (assoc s
                                                 :timeout (js/setTimeout
                                                           (fn []
                                                             (on-change v)
                                                             (swap! state dissoc :temp-val))
                                                           500)
                                                 :temp-val v))))))})])))

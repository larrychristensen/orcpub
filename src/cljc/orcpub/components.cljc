(ns orcpub.components
  (:require [re-frame.core :refer [dispatch]]))

(defn checkbox [selected? disable?]
  [:i.fa.fa-check.f-s-14.bg-white.orange-shadow.m-r-10.pointer
   {:class-name (str (if selected? "black slight-text-shadow" "transparent")
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

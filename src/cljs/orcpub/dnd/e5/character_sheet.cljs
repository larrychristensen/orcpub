(ns orcpub.dnd.e5.character-sheet
  (:require [goog.dom :as gdom]
            [goog.labs.userAgent.device :as device]
            [cljs.pprint :as pprint]
            [clojure.string :as s]
            
            [orcpub.template :as t]
            [orcpub.entity :as entity]
            [orcpub.entity-spec :as es]
            [orcpub.dice :as dice]
            [orcpub.modifiers :as mod]
            [orcpub.dnd.e5.character :as char5e]
            [orcpub.dnd.e5.modifiers :as mod5e]
            [orcpub.dnd.e5.options :as opt5e]
            [orcpub.dnd.e5.template :as t5e]
            [orcpub.dnd.e5.spells :as spells]

            [clojure.spec :as spec]
            [clojure.spec.test :as stest]

            [reagent.core :as r]))

(defn character-sheet []
  [:div
   {:style {:background-color :gray
            :display :flex
            :justify-content :center
            :height "100%"
            :overflow :auto}}
   [:div
    {:style {:margin-top "30px"
             :margin-bottom "30px"
             :width "8.5in"
             :height "11in"
             :background-color :white
             :box-shadow "5px 5px 5px rgba(0,0,0,0.2)"}}
    [:div
     {:style {:display :flex
              :height "100%"
              :flex-direction :column
              :justify-content :space-between}}
     [:div
      {:style {:display :flex
               :justify-content :space-between}}
      [:div "A"]
      [:div "B"]
      [:div "C"]
      [:div "D"]]
     [:div
      {:style {:display :flex
               :justify-content :space-between}}
      [:div "A"]
      [:div "B"]
      [:div "C"]
      [:div "D"]]]]])

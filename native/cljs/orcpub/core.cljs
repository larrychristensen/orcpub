(ns orcpub.core
    (:require [reagent.core :as r :refer [atom]]
              [re-frame.core :refer [subscribe dispatch dispatch-sync]]
              [orcpub.dnd.e5.events]
              [orcpub.dnd.e5.equipment-subs]
              [orcpub.dnd.e5.subs]
              [orcpub.dnd.e5.db]
              [orcpub.views :refer [text view image touchable-without-feedback app-registry Alert]]
              [orcpub.dnd.e5.native-views :as v5e]))

(defn alert [title]
  (.alert Alert title))

(defn hello-button []
  [touchable-without-feedback {:on-press #(alert "HELLO!")}
   [view {:style {:border-width 1 :border-color "#f0a100" :padding 10 :border-radius 5}}
    [text {:style {:color "white" :text-align "center" :font-weight "bold"}} "press me"]]])

(defn app-root []
  (prn "APP ROOT")
  [view {:style {:flex 1}}
   [view {:style {:background-color "#313a4d"
                  :padding-top 20
                  :padding-bottom 5
                  :padding-left 5
                  :padding-right 5}}
    [image {:source (js/require "./assets/images/orcpub-logo.png")}]]
   [v5e/character-builder]])

(defn init []
  (dispatch-sync [:initialize-db])
  (.registerComponent app-registry "main" #(r/reactify-component app-root)))

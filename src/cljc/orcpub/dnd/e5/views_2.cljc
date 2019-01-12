(ns orcpub.dnd.e5.views-2
  (:require [orcpub.route-map :as routes]
            [clojure.string :as s]
            #?(:cljs [re-frame.core :refer [subscribe dispatch dispatch-sync]])))

(defn style [style]
  #?(:cljs style)
  #?(:clj (s/join
           "; "
           (map
            (fn [[k v]]
              (str (name k) ": " (if (keyword? v) (name v) v)))
            style))))

(defn svg-icon-2 [icon-name & [theme]]
  [:img.svg-icon
   {:src (str "/image/" icon-name ".svg")}])

(defn splash-page-button [title icon route & [handler]]
  [:a.splash-button
   (let [cfg {:style (style {:text-decoration :none
                             :color "#f0a100"})}]
     (if handler
       (assoc cfg :on-click handler)
       (assoc cfg :href (routes/path-for route))))
   [:div.splash-button-content
    {:style (style {:box-shadow "0 2px 6px 0 rgba(0, 0, 0, 0.5)"
                    :margin "5px"
                    :text-align "center"
                    :padding "10px"
                    :cursor :pointer
                    :display :flex
                    :align-items :center
                    :justify-content :space-around
                    :font-weight :bold})}
    [:div
     (svg-icon-2 icon 64 "dark")
     [:div
      [:span.splash-button-title-prefix "DnD 5e "] [:span title]]]]])

(defn legal-footer []
  [:div.m-l-15.m-b-10.m-t-10.t-a-l
   [:span "© 2019 OrcPub"]
   [:a.m-l-5 {:href "/terms-of-use" :target :_blank} "Terms of Use"]
   [:a.m-l-5 {:href "/privacy-policy" :target :_blank} "Privacy Policy"]])

(def orange-style
  {:color :orange})

(defn legal-footer-2 []
  [:div
   {:style (style {:display :flex
                   :justify-content :space-between
                   :align-items :center
                   :flex-wrap :wrap
                   :padding "10px"})}
   [:div
    [:div {:style (style {:margin-bottom "5px"})} "Icons made by Lorc, Caduceus, and Delapouite. Available on " [:a.orange {:href "http://game-icons.net"} "http://game-icons.net"]]]
   [:div.m-l-10 {:style (style {:margin-left "10px"})}
    [:a {:style (style orange-style) :href "https://github.com/larrychristensen/orcpub/issues" :target :_blank} "Feedback/Bug Reports"]]
   [:div.m-l-10.m-r-10.p-10
    {:style (style {:margin-left "10px"
                    :margin-right "10px"
                    :padding "10px"})}
    [:a {:style (style orange-style) :href "/privacy-policy" :target :_blank} "Privacy Policy"]
    [:a.m-l-5 {:style (orange-style {:margin-left "5px"}) :href "/terms-of-use" :target :_blank} "Terms of Use"]]
   [:div.legal-footer
    [:p "© 2019 OrcPub" [:span {:style (style {:margin-left "20px"})} "Contact: " [:a {:href "mailto:redorc@orcpub.com"} "redorc@orcpub.com"]]]
    [:p "Wizards of the Coast, Dungeons & Dragons, D&D, and their logos are trademarks of Wizards of the Coast LLC in the United States and other countries. © 2019 Wizards. All Rights Reserved. OrcPub.com is not affiliated with, endorsed, sponsored, or specifically approved by Wizards of the Coast LLC."]]])

(defn splash-page []
  [:div.app
   {:style (style {:display :flex
                   :flex-direction :column})}
   [:div
    {:style (style
             {:display :flex
              :flex-grow 1
              :color :white
              :align-items :center
              :justify-content :space-around})}
    [:div.main-text-color.splash-page-content
     {:style (style {:font-family "sans-serif"})}
     [:div
      {:style (style {:display :flex
                      :justify-content :space-around})}
      [:img
       {:src "/image/orcpub-logo.svg"
        :style (style {:height "70px"})}]]
     [:div
      {:style (style {:text-align :center
                      :text-shadow "1px 2px 1px black"
                      :font-weight :bold
                      :font-size "14px"
                      :height "48px"})}
      "version 2.0 - community edition"]
     [:div
      {:style (style
               {:display :flex
                :flex-wrap :wrap
                :justify-content :center
                :margin-top "10px"})}
      (splash-page-button
       "Character Builder / Sheet"
       "anvil-impact"
       routes/dnd-e5-char-builder-route)
      (splash-page-button
       "Character Builder for Newbs"
       "baby-face"
       routes/dnd-e5-newb-char-builder-route)
      (splash-page-button
       "Spells"
       "spell-book"
       routes/dnd-e5-spell-list-page-route)
      (splash-page-button
       "Monsters"
       "hydra"
       routes/dnd-e5-monster-list-page-route)
      (splash-page-button
       "Items"
       "all-for-one"
       routes/dnd-e5-item-list-page-route)
      (splash-page-button
       "Combat Tracker"
       "sword-clash"
       routes/dnd-e5-combat-tracker-page-route)
      (splash-page-button
       "Homebrew Content"
       "beer-stein"
       routes/dnd-e5-my-content-route)
      (splash-page-button
       "Encounter Builder"
       "minions"
       routes/dnd-e5-encounter-builder-page-route)
      (splash-page-button
       "Monster Builder"
       "ifrit"
       routes/dnd-e5-monster-builder-page-route)
      (splash-page-button
       "Spell Builder"
       "gift-of-knowledge"
       routes/dnd-e5-spell-builder-page-route)
      (splash-page-button
       "Feat Builder"
       "vitruvian-man"
       routes/dnd-e5-feat-builder-page-route)
      (splash-page-button
       "Class Builder"
       "mounted-knight"
       routes/dnd-e5-class-builder-page-route)
      (splash-page-button
       "Race Builder"
       "woman-elf-face"
       routes/dnd-e5-race-builder-page-route)
      (splash-page-button
       "Background Builder"
       "ages"
       routes/dnd-e5-background-builder-page-route)]]]
   [:div.legal-footer-parent
    {:style (style {:font-size "12px"
                    :color :white
                    :padding "10px"})}
    (legal-footer-2)]])

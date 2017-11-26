(ns orcpub.dnd.e5.views-2
  (:require [orcpub.route-map :as routes]
            [clojure.string :as s]))

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

(defn splash-page-button [title icon route]
  [:a.no-text-decoration.splash-button {:href (routes/path-for route)}
   [:div.shadow.m-5.t-a-c.p-10.pointer.flex.align-items-c.justify-cont-s-a.splash-button-content
    [:div
     (svg-icon-2 icon 64 "dark")
     [:div.f-w-b [:span.splash-button-title-prefix "DnD 5e "] [:span title]]]]])


(defn splash-page []
  [:div.app.flex.align-items-c.justify-cont-s-a
   [:div.main-text-color.splash-page-content
    {:style (style {:font-family "sans-serif"})}
    [:div.flex.justify-cont-s-a
     [:img.orcpub-logo.h-48.pointer
      {:src "/image/orcpub-logo.svg"}]]
    [:div.f-w-b.f-s-12.t-a-c.text-shadow "version 2.0"]
    [:div.flex.flex-wrap.m-t-10.justify-cont-c
     (splash-page-button
      "Character Builder / Sheet"
      "anvil-impact"
      routes/dnd-e5-char-builder-route)
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
      routes/dnd-e5-item-list-page-route)
     (splash-page-button
      "Encounter Builder"
      "minions"
      routes/dnd-e5-encounter-builder-page-route)
     (splash-page-button
      "Monster Builder"
      "ifrit"
      routes/dnd-e5-encounter-builder-page-route)
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
      routes/dnd-e5-background-builder-page-route)]]])

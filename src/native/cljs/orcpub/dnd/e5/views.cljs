(ns orcpub.dnd.e5.views
  (:require [orcpub.views :refer [view
                                  scroll-view
                                  text
                                  touchable-without-feedback
                                  main-text-color
                                  light-text-color]]
            [orcpub.entity :as entity]
            [orcpub.template :as t]
            [clojure.string :as s]
            [reagent.core :as r]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]))

(def tab-style
  {:padding 10
   :border-bottom-color main-text-color
   :opacity 0.4})

(def builder-tab-style
  (merge
   tab-style
   {:border-bottom-width 5}))

(def selected-tab-style
  {:opacity 1})

(def selected-builder-tab-style
  (merge
   builder-tab-style
   selected-tab-style))

(defn builder-tab [title key selected-tab]
  [touchable-without-feedback {:on-press #(reset! selected-tab key)}
   [view {:style (if (= key @selected-tab)
                   selected-builder-tab-style
                   builder-tab-style)}
    [text {:style {:font-weight :bold
                   :color main-text-color
                   :font-size 18}}
     title]]])

(def pages
  [{:name "Race"
    :icon "woman-elf-face"
    :tags #{:race :subrace}}
   {:name "Background"
    :icon "ages"
    :tags #{:background}}
   {:name "Proficiencies"
    :icon "juggler"
    :tags #{:profs}}])

(def options-tab-style
  (merge
   tab-style
   {:border-bottom-width 2}))

(def selected-options-tab-style
  (merge
   options-tab-style
   selected-tab-style))

(defn options-tab [title i selected-tab]
  [touchable-without-feedback {:on-press #(do (prn "I" i) (reset! selected-tab i))}
   [view {:style (if (= i @selected-tab)
                   selected-options-tab-style
                   options-tab-style)}
    [text {:style {:font-weight :bold
                   :color main-text-color
                   :font-size 12}}
     title]]])

(defn option-view [key name]
  [view {:style {:padding 10
                 :margin 2
                 :border-width 2
                 :border-color light-text-color}}
   [text name]])

(defn selection-view [{:keys [::t/key ::t/name ::t/options]}]
  [view
   [text name]
   (doall
    (map
     (fn [{:keys [::t/key ::t/name]}]
       ^{:key key}
       [option-view key name])
     options))])

(defn options-view []
  (let [selected-tab-index (r/atom 0)]
    (fn []
      (let [character @(subscribe [:character])
            built-template @(subscribe [:built-template])
            available-selections @(subscribe [:available-selections])
            built-char @(subscribe [:built-character])
            {:keys [tags ui-fns components] :as page} (pages @selected-tab-index)
            selections (entity/tagged-selections available-selections tags)
            final-selections (entity/combine-selections selections)]
        [view
         [view {:style {:flex-direction :row
                        :justify-content :space-around}}
          (doall
           (map-indexed
            (fn [i {:keys [name icon tags]}]
              ^{:key name}
              [options-tab name i selected-tab-index])
            pages))]
         [scroll-view
          (doall
           (map
            (fn [{:keys [::t/key] :as selection}]
              ^{:key key}
              [selection-view selection])
            final-selections))]]))))


(defn character-builder []
  (let [selected-tab (r/atom :options)]
    [view {:style {:align-items :center}}
     [view {:style {:flex-direction :row
                    :margin-top 10
                    :padding 10}}
      [builder-tab "OPTIONS" :options selected-tab]
      [builder-tab "DESCRIPTION" :description selected-tab]
      [builder-tab "SHEET" :sheet selected-tab]]
     [options-view]]))

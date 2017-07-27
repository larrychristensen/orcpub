(ns orcpub.dnd.e5.native-views
  (:require [orcpub.views :refer [view
                                  scroll-view
                                  text
                                  touchable-without-feedback
                                  main-text-color
                                  light-text-color]]
            [orcpub.entity :as entity]
            [orcpub.views-aux :as views-aux]
            [orcpub.template :as t]
            [clojure.string :as s]
            [reagent.core :as r]
            [clojure.pprint :refer [pprint]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]))

(def tab-style
  {:padding 10
   :flex 1
   :border-bottom-color main-text-color
   :opacity 0.4
   :height 45
   :flex-direction :row
   :justify-content :space-around})

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
   {:border-bottom-width 2
    :height 40}))

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

(def unselected-option-style
  {:padding 12
   :margin 2
   :border-width 2
   :border-radius 5
   :border-color light-text-color})

(def selected-option-style
  (assoc
   unselected-option-style
   :border-width 5
   :border-color main-text-color))

(defn option-view [option-path
                   selection
                   disable-select-new?
                   homebrew?
                   option]
  (let [{:keys [name
                key
                selected?
                selectable?
                multiselect?
                option-path
                select-fn
                help
                has-named-mods?
                modifiers-str
                failed-prereqs] :as data}
        (views-aux/option-selector-data option-path
                                        selection
                                        disable-select-new?
                                        homebrew?
                                        option)]
    [touchable-without-feedback
     {:on-press select-fn}
     [view {:style (if selected?
                     selected-option-style
                     unselected-option-style)}
      [text name]]]))

(defn selection-view [{:keys [::t/key ::t/name ::t/options] :as selection} path]
  [view
   [text {:style {:margin-left 5
                  :font-size 16
                  :font-weight :bold}}
    name]
   [view {:style {:margin-bottom 10}}
    (doall
     (map
      (fn [{:keys [::t/key ::t/name] :as option}]
        ^{:key key}
        [option-view
         path
         selection
         false
         false
         option])
      options))]])

(defn build-view []
  (let [selected-tab-index (r/atom 0)]
    (fn []
      (let [character @(subscribe [:character])
            built-template @(subscribe [:built-template])
            available-selections @(subscribe [:available-selections])
            built-char @(subscribe [:built-character])
            {:keys [tags ui-fns components] :as page} (pages @selected-tab-index)
            selections (entity/tagged-selections available-selections tags)
            final-selections (entity/combine-selections selections)]
        (pprint character)
        [view {:style {:flex 1}}
         [view {:style {:flex-direction :row}}
          [view {:style {:flex-direction :row
                         :flex 1
                         :padding 10
                         :justify-content :space-around}}
           (doall
            (map-indexed
             (fn [i {:keys [name icon tags]}]
               ^{:key name}
               [options-tab name i selected-tab-index])
             pages))]]
         [scroll-view {:style {:padding 10}}
          (doall
           (map
            (fn [{:keys [::t/key] :as selection}]
              (let [path (entity/actual-path selection)]
                ^{:key (s/join "," path)}
                [selection-view selection path]))
            final-selections))]]))))


(defn character-builder []
  (let [selected-tab (r/atom :options)]
    [view {:style {:align-items :center
                   :flex 1}}
     [view {:style {:flex-direction :row
                    :margin-top 10
                    :padding 10}}
      [builder-tab "BUILD" :options selected-tab]
      [builder-tab "DESCRIBE" :description selected-tab]
      [builder-tab "VIEW" :sheet selected-tab]]
     [view {:style {:flex 1
                    :flex-direction :row}}
      [build-view]]]))

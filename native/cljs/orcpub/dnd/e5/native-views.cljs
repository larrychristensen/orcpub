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

(defn selection-section-title [title]
  (prn "SELETCION SECTION TITLE" title)
  [view {:style {:margin-left 5}}
   [text {:style {:font-size 16
                  :font-weight :bold}}
    title]])

(defn selection-section-parent-title [title]
  (prn "SELECTION SECTION PARENT TITLE" title)
  [view {:style {:margin-left 5
                 :margin-bottom 2}}
   [text {:style {:font-style :italic
                  :font-size 14
                  :color light-text-color}}
    title]])

(defn align-items-c [s]
  (assoc s :align-items :center))

(defn h [s v]
  (assoc s :height v))

(defn w [s v]
  (assoc s :width v))

(defn i [s]
  (assoc s :font-style :italic))

(defn remaining-bubble [value color left-offset top-offset]
  [view {:style {:background-color color
                 :border-color color
                 :border-radius 12
                 :border-width 12}}
   [text
    {:style {:position :absolute
             :left left-offset
             :top top-offset
             :font-weight :bold
             :font-size (or font-size 14)
             :color :white}}
    value]])

(defn remaining-indicator [remaining & [size font-size]]
  (remaining-bubble remaining :red -4 -8))

(def remaining-text-style
  {:margin-left 5
   :font-style :italic})

(def remaining-view-style
  {:align-items :center
   :flex-direction :row})

(defn remaining-component [max remaining]
  [view {:style {:margin-left 10}}
   (cond
     (pos? remaining)
     [view {:style remaining-view-style}
      (remaining-indicator remaining)
      [text {:style remaining-text-style}
       "remaining"]]

     (or (zero? remaining)
         (and (nil? max)
              (neg? remaining)))
     [view {:style remaining-view-style}
      (remaining-bubble "\u2713" :green -6 -9)
      [text {:style remaining-text-style}
       "complete"]]

     (neg? remaining)
     [view {:style remaining-view-style}
      [text {:style {:font-style :italic
                     :margin-right 5}}
       "remove"]
      (remaining-bubble (Math/abs remaining) :red -4 -8)])])

(defn selection-section-base []
  (let [expanded? (r/atom false)]
    (fn [{:keys [title path parent-title name icon help max min remaining body hide-lock? hide-homebrew?]}]
      (let [locked? @(subscribe [:locked path])
            homebrew? @(subscribe [:homebrew? path])]
        [view {:style {:padding 5
                       :margin-bottom 20}}
         (if (and (or title name) parent-title)
           (selection-section-parent-title parent-title))
         [view
          #_(if icon (views5e/svg-icon icon 24))
          (if (or title name)
            (selection-section-title (or title name))
            (if parent-title
              (selection-section-parent-title parent-title)))
          #_(if (and path help)
            [show-info-button expanded?])
          #_(if (not hide-lock?)
            [:i.fa.f-s-16.m-l-10.m-r-5.pointer
             {:class-name (if locked? "fa-lock" "fa-unlock-alt opacity-5 hover-opacity-full")
              :on-click #(dispatch [:toggle-locked path])}])
          #_(if (not hide-homebrew?)
            [:span.pointer
             {:class-name (if (not homebrew?) "opacity-5 hover-opacity-full")
              :on-click #(dispatch [:toggle-homebrew path])}
             (views5e/svg-icon "beer-stein" 18)])]
         #_(if (and help path @expanded?)
           [help-section help])
         (if (int? min)
           [view {:style {:flex-direction :row
                          :align-items :center
                          :padding-horizontal 5
                          :justify-content :space-between}}
            [text {:style {:font-style :italic}}
             (str "select " (cond
                              (= min max) min
                              (zero? min) (if (nil? max)
                                            "any number"
                                            (str "up to " max))
                              :else (str "at least " min)))]
            (remaining-component max remaining)])
         body]))))

(defn selection-view [path {:keys [::t/key ::t/name ::t/options] :as selection} _ _ _]
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
     options))])

(defn selection-section [title built-template option-paths ui-fns selection num-columns remaining & [hide-homebrew?]]
  (let [path (entity/actual-path selection)
        {:keys [disable-select-new? homebrew?] :as data}
        (views-aux/selection-section-data
         title
         built-template
         option-paths
         ui-fns
         selection-view
         selection
         num-columns
         remaining
         hide-homebrew?)]
    [selection-section-base data]))

(defn build-view []
  (let [selected-tab-index (r/atom 0)]
    (fn []
      (let [character @(subscribe [:character])
            built-template @(subscribe [:built-template])
            available-selections @(subscribe [:available-selections])
            option-paths @(subscribe [:option-paths])
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
            (fn [{:keys [::t/key ::t/name] :as selection}]
              (let [path (entity/actual-path selection)]
                ^{:key (s/join "," path)}
                [selection-section
                 name
                 built-template
                 option-paths
                 ui-fns
                 selection
                 1
                 (entity/count-remaining built-template character selection)
                 false]))
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

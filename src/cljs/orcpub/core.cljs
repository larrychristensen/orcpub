(ns orcpub.core
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [cljs.pprint :as pprint]
            
            [orcpub.template :as t]
            [orcpub.entity :as entity]
            [orcpub.dnd.e5.character :as char5e]
            [orcpub.dnd.e5.modifiers :as modifiers]

            [reagent.core :as r]))

(enable-console-print!)

(def wizard-cantrips
  [:acid-splash :blade-ward :light :true-strike])

(def wizard-cantrip-options
  (map
   (fn [key]
     {::t/key key
      ::t/name (name key)
      ::t/modifiers [(modifiers/spells-known 0 key)]})
   wizard-cantrips))

(def arcane-tradition-options
  [(t/option
    "School of Evocation"
    nil
    [(modifiers/trait "Evocation Savant")
     (modifiers/trait "Sculpt Spells")])])

(declare app-state)

(defn abilities [character]
  [:div
   [:div
    {:style {:display :flex
             :justify-content :space-between}}
    (for [[k v] (get-in character [::entity/options :ability-scores ::entity/value])]
      ^{:key k} [:div {:style {:margin-top "10px"
                               :margin-bottom "10px"
                               :text-align :center}}
                 [:div {:style {:text-transform :uppercase}} (name k)]
                 [:div {:style {:font-size "18px"}} v]
                 [:div
                  [:i.fa.fa-chevron-circle-left {:style {:font-size "16px"}}]
                  [:i.fa.fa-chevron-circle-right {:style {:margin-left "5px" :font-size "16px"}}]]])]
   [:button.form-button
    {:on-click (fn []
                 (swap! app-state update ::character #(assoc-in % [::entity/options :ability-scores ::entity/value] (char5e/standard-ability-rolls))))}
    "Re-Roll"]])

(def template
  {::count 0
   ::t/selections
   [(t/selection
     "Ability Scores"
     [{::t/name "Standard Roll"
       ::t/key :standard-roll
       ::t/ui-fn #(abilities (::character @app-state))
       ::t/modifiers [(modifiers/abilities nil)]}
      (t/option
       "Standard Scores"
       []
       [(modifiers/abilities (char5e/abilities 15 14 13 12 10 8))])])
    (t/selection
     "Race"
     [(t/option
       "Elf"
       [(t/selection
         "Subrace"
         [(t/option
           "High Elf"
           [(t/selection?
             "Cantrip"
              wizard-cantrip-options)]
           [(modifiers/ability ::char5e/int 1)])
          (t/option
           "Wood Elf"
           []
           [(modifiers/ability ::char5e/wis 1)])])]
       [(modifiers/ability ::char5e/dex 2)])
      (t/option
       "Dwarf"
       [(t/selection
         "Subrace"
         [(t/option
           "Hill Dwarf"
           [(t/selection
             "Tool Proficiency"
             wizard-cantrip-options)]
           [(modifiers/ability ::char5e/int 1)])
          (t/option
           "Mountain Dwarf"
           []
           [(modifiers/ability ::char5e/wis 1)])])]
       [(modifiers/ability ::char5e/dex 2)])])
    (t/selection+
     "Class"
     [(t/option
       "Wizard"
       [(t/sequential-selection
         "Levels"
         [(t/option
           "1"
           [(t/selection "Cantrip" wizard-cantrip-options 0 3)]
           [(modifiers/saving-throws ::char5e/int ::char5e/wis)
            (modifiers/level :wizard)
            (modifiers/max-hit-points 6)])
          (t/option
           "2"
           [(t/selection
             "Arcane Tradition"
             arcane-tradition-options)
            (t/selection
             "Hit Points"
             [(t/option
               "Roll"
               []
               [(modifiers/max-hit-points nil)])
              (t/option
               "Average"
               []
               [(modifiers/max-hit-points 4)])])]
           [(modifiers/level :wizard)])
          (t/option
           "3"
           [(t/selection "Cantrip" wizard-cantrip-options 0 3)]
           [(modifiers/saving-throws ::char5e/int ::char5e/wis)
            (modifiers/level :wizard)
            (modifiers/max-hit-points 6)])])])])]})

(def character
  {::entity/options {:ability-scores {::entity/key :standard-roll
                                      ::entity/value (char5e/abilities 12 13 14 15 16 17)}
                     :race {::entity/key :elf
                            ::entity/options {:subrace {::entity/key :high-elf
                                                        ::entity/options {:cantrip {::entity/key :light}}}}}
                     :class [{::entity/key :wizard
                              ::entity/options {:levels [{::entity/key :1
                                                          ::entity/options {:cantrips-known [{::entity/key :acid-splash}]}}
                                                         {::entity/key :2
                                                          ::entity/options {:arcane-tradition {::entity/key :school-of-evocation}
                                                                            :hit-points {::entity/key :roll
                                                                                         ::entity/value 3}}}]}}]}})

(def text-color
  {:color :white})

(def field-font-size
  {:font-size "14px"})

(defonce app-state
  (r/atom
   {::template template
    ::character character}))

(defn index-of-option [selection option-key]
  (first
   (keep-indexed
    (fn [i v]
      (if (= option-key (::entity/key v))
        i))
    selection)))

(defn update-option [obj current-path [k & ks] update-fn]
  (if k
    (let [selection-path (vec (concat current-path [::entity/options k]))
          selection (get-in obj selection-path)
          next-key (first ks)
          next-path (if (and (vector? selection)
                             next-key)
                      (conj selection-path
                            (index-of-option selection next-key))
                      selection-path)]
      (update-option obj next-path (rest ks) update-fn))
    (update-in obj current-path update-fn)))

(defn remove-list-option [obj path index]
  (update-option obj [] path
                 (fn [list]
                   (keep-indexed
                    (fn [i v] (if (not= i index) v))
                    list))))

(declare builder-selector)

(defn option [path option-paths selectable? {:keys [::t/key ::t/name ::t/selections ::t/ui-fn]}]
  (let [new-path (conj path key)
        selected? (boolean (get-in option-paths new-path))]
    [:div.builder-option
     {:class-name (clojure.string/join
                   " "
                   [(if selected? "selected-builder-option")
                    (if selectable? "selectable-builder-option")])
      :on-click (fn [e]
                  (if selectable? (swap! app-state update ::character #(update-option % [] path (fn [o] (assoc o ::entity/key key)))))
                  (.stopPropagation e))}
     [:h2 name]
     (if selected?
       [:div
        (if ui-fn (ui-fn))
        (into [:div]
              (map (partial builder-selector new-path option-paths))
              selections)])]))

(def builder-selector-style)

(defn dropdown-option [option]
  ^{:key (name (::t/key option))} [:option.builder-dropdown-item
                            (::t/name option)])

(defn dropdown [options change-fn]
  (into [:select.builder-option.builder-option-dropdown
         {:on-change change-fn}]
        (map dropdown-option)
        options))

(defn dropdown-selector [{:keys [::t/options ::t/min ::t/max ::t/key ::t/name ::t/sequential?]}]
  (if max
    (for [i (range max)]
      ^{:key i} [:div (dropdown options (fn [] (prn i)))])
    (dropdown options (fn []))))

(defn remove-option-button [path index]
  [:i.fa.fa-minus-circle.remove-item-button
   {:on-click
    (fn [e]
      (swap! app-state update ::character #(remove-list-option % path index)))}])

(defn add-option-button [selection-name]
  [:div.add-item-button
   [:i.fa.fa-plus-circle]
   [:span {:style {:margin-left "5px"}} (str "Add " selection-name)]])

(defn filter-selected [path key option-paths options]
  (filter
   (fn [opt]
     (let [option-path (concat path [key (::t/key opt)])]
       (get-in option-paths option-path)))
   options))

(defn list-selector-option [removeable? path option-paths multiple-select? i opt]
  [:div.list-selector-option
   [:div {:style {:flex-grow 1}} (option path option-paths (not multiple-select?) opt)]
   (if (removeable? i)
     (remove-option-button path i))])

(defn list-selector [path option-paths {:keys [::t/options ::t/min ::t/max ::t/key ::t/name ::t/sequential?]}]
  (let [no-max? (nil? max)
        multiple-select? (or no-max? (> max 1))
        selected-options (filter-selected path key option-paths options)
        addable? (and multiple-select?
                      (or no-max?
                          (< (count selected-options) max)))
        more-than-min? (> (count selected-options) min)
        next-path (conj path key)]
    [:div
     (into
      [:div]
      (map-indexed
       (partial
        list-selector-option
        #(and multiple-select?
              more-than-min?
              (or (not sequential?)
                  (= % (dec (count selected-options)))))
        next-path
        option-paths
        multiple-select?))
      (if multiple-select?
        selected-options
        options))
     (if addable?
       (add-option-button name))]))

(defn builder-selector [path option-paths selection]
  [:div.builder-selector
   [:h2.builder-selector-header (::t/name selection)]
   (if (not-any? ::t/selections (::t/options selection))
     (dropdown-selector selection)
     (list-selector path option-paths selection))])


(def content-style
  {:width 1440})

(def character-builder-style
  (merge
   content-style
   text-color
   field-font-size))

(def container-style
  {:display :flex
   :justify-content :center})

(def tabs-style
  (merge
   {:height 82
    :text-transform :uppercase
    :font-weight 600}
   content-style
   text-color
   field-font-size))

(def tab-style
  {:padding-top 48
   :padding-left 25
   :padding-right 25
   :padding-bottom 13
   :display :inline-block
   :border-bottom "5px solid rgba(255, 255, 255, 0.05)"})

(def selected-tab-style
  (merge
   tab-style
   {:border-bottom-color "#f1a20f"}))

(def page-header-style
  (merge
   {:font-size 36
    :font-weight :bold
    :margin-top 21
    :margin-bottom 19}))

(defn make-path-map [character]
  (let [flat-options (entity/flatten-options (::entity/options character))]
    (reduce
     (fn [m v]
       (update-in m (::t/path v) (fn [c] (or c {}))))
     {}
     flat-options)))

(defn character-builder []
  (let [option-paths (make-path-map (::character @app-state))]
    [:div.app
     [:div.app-header
      [:div.app-header-bar.container
       [:div.content
        [:img {:src "image/orcpub-logo.svg"}]]]]
     [:div.container
      [:div {:style tabs-style}
       [:span {:style selected-tab-style} "Character"]
       [:span {:style tab-style} "Monster"]]]
     [:div
      {:style container-style}
      [:div
       {:style character-builder-style}
       [:h1 {:style page-header-style} "Character Builder"]
       (into [:div {:style {:width "300px"}}]
             (map (partial builder-selector [] option-paths))
             (::t/selections (::template @app-state)))]]]))

(r/render [character-builder]
          (js/document.getElementById "app"))

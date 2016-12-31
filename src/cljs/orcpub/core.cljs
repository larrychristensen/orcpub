(ns orcpub.core
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [cljs.pprint :as pprint]
            [clojure.string :as s]
            
            [orcpub.template :as t]
            [orcpub.entity :as entity]
            [orcpub.dnd.e5.character :as char5e]
            [orcpub.dnd.e5.modifiers :as modifiers]

            [clojure.spec.test :as stest]

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

(defn get-raw-abilities [character]
  (get-in character [::entity/options :ability-scores ::entity/value]))

(defn abilities-standard [character]
  [:div
   [:div
    {:style {:display :flex
             :justify-content :space-between}}
    (for [[k v] (get-raw-abilities character)]
      ^{:key k} [:div {:style {:margin-top "10px"
                               :margin-bottom "10px"
                               :text-align :center}}
                 [:div {:style {:text-transform :uppercase}} (name k)]
                 [:div {:style {:font-size "18px"}} v]
                 [:div
                  [:i.fa.fa-chevron-circle-left {:style {:font-size "16px"}}]
                  [:i.fa.fa-chevron-circle-right {:style {:margin-left "5px" :font-size "16px"}}]]])]])

(defn abilities-roller [character]
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
       ::t/ui-fn #(abilities-roller (::character @app-state))
       ::t/modifiers [(modifiers/abilities nil)]}
      {::t/name "Standard Scores"
       ::t/key :standard-scores
       ::t/ui-fn #(abilities-standard (::character @app-state))
       ::t/select-fn #(swap! app-state update ::character (fn [c] (assoc-in c [::entity/options :ability-scores] {::entity/key :standard-scores
                                                                                                                 ::entity/value (char5e/abilities 15 14 13 12 10 8)})))
       ::t/modifiers [(modifiers/abilities nil)]}])
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
           [(modifiers/subrace "High Elf")
            (modifiers/ability ::char5e/int 1)])
          (t/option
           "Wood Elf"
           []
           [(modifiers/subrace "Wood Elf")
            (modifiers/ability ::char5e/wis 1)])])]
       [(modifiers/race "Elf")
        (modifiers/ability ::char5e/dex 2)])
      (t/option
       "Dwarf"
       [(t/selection
         "Subrace"
         [(t/option
           "Hill Dwarf"
           [(t/selection
             "Tool Proficiency"
             wizard-cantrip-options)]
           [(modifiers/subrace "Hill Dwarf")
            (modifiers/ability ::char5e/wis 1)])
          (t/option
           "Mountain Dwarf"
           []
           [(modifiers/subrace "Mountain Dwarf")
            (modifiers/ability ::char5e/str 2)])])]
       [(modifiers/race "Dwarf")
        (modifiers/ability ::char5e/con 2)])])
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
            (modifiers/level :wizard "Wizard" 1)
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
           [(modifiers/level :wizard "Wizard" 2)])
          (t/option
           "3"
           [(t/selection "Cantrip" wizard-cantrip-options 0 3)]
           [(modifiers/saving-throws ::char5e/int ::char5e/wis)
            (modifiers/level :wizard "Wizard" 3)
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

(add-watch app-state :log (fn [k r os ns]
                            (js/console.log "OLD" (clj->js os))
                            (js/console.log "NEW" (clj->js ns))))

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

(defn option [path option-paths selectable? {:keys [::t/key ::t/name ::t/selections ::t/ui-fn ::t/select-fn]}]
  (let [new-path (conj path key)
        selected? (boolean (get-in option-paths new-path))]
    [:div.builder-option
     {:class-name (clojure.string/join
                   " "
                   [(if selected? "selected-builder-option")
                    (if selectable? "selectable-builder-option")])
      :on-click (fn [e]
                  (if selectable?
                    (do
                      (if select-fn
                        (select-fn)
                        (swap! app-state update ::character #(update-option % [] path (fn [o] (assoc o ::entity/key key)))))))
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
   (if (not-any? #(or (::t/selections %)
                      (::t/ui-fn %)) (::t/options selection))
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

(defn abilities-radar [size abilities]
  (let [d size
        stroke 1.5
        point-offset 10
        double-point-offset (* point-offset 2)
        double-stroke (* 2 stroke)
        alpha (/ d 4)
        triple-alpha (* alpha 3)
        beta (* alpha (Math/sqrt 3))
        double-beta (* beta 2)
        points [[0 (- (* alpha 2))]
                [beta (- alpha)]
                [beta alpha]
                [0 (* alpha 2)]
                [(- beta) alpha]
                [(- beta) (- alpha)]]
        offset-abilities (take 6 (drop 1 (cycle abilities)))
        text-points [[0 55] [106 0] [206 55] [206 190] [106 240] [0 190]]
        abilities-points (map
                          (fn [[_ av] [x y]]
                            (let [ratio (double (/ av 20))]
                              [(* x ratio) (* y ratio)]))
                          offset-abilities
                          points)
        colors (map-indexed
                (fn [i c]
                  {:key i
                   :color c})
                ["#f4692a" "#f32e50" "#b35c95" "#47eaf8" "#bbe289" "#f9b747"])]
    [:div {:style {:position :relative}}
     (map
      (fn [[ak av] [x y] {:keys [color]}]
        [:div {:style {:width 50 :text-align :center :position :absolute :left x :top y}}
         [:div
          [:span (s/upper-case (name ak))]
          [:span {:style {:margin-left 5 :color color}} av]]
         [:div {:style {:color color}} (let [bonus (int (/ (- av 10) 2))]
                                         (str "(" (if (pos? bonus) "+") bonus ")"))]])
      offset-abilities
      (take 6 (drop 1 (cycle text-points)))
      colors)
     [:svg {:width (+ 80 double-beta double-point-offset) :height (+ 60 d double-point-offset)}
      [:defs
       (map
        (fn [[x1 y1] [x2 y2] c1 c2]
          [:g
           [:linearGradient {:id (str "lg-" (:key c1) "-o")
                             :x1 x1 :y1 y1 :x2 0 :y2 0
                             :gradientUnits :userSpaceOnUse}
            [:stop {:offset "0%" :stop-color (:color c1)}]
            [:stop {:offset "70%" :stop-color (:color c2) :stop-opacity 0}]]
           [:linearGradient {:id (str "lg-" (:key c1) "-" (:key c2))
                             :x1 x1 :y1 y1 :x2 x2 :y2 y2
                             :gradientUnits :userSpaceOnUse}
            [:stop {:offset "0%" :stop-color (:color c1)}]
            [:stop {:offset "100%" :stop-color (:color c2)}]]])
        points
        (drop 1 (cycle points))
        colors
        (drop 1 (cycle colors)))]
      [:g {:transform (str "translate(" (+ 40 beta point-offset) "," (+ 30 (* alpha 2) point-offset) ")")}
       [:polygon.abilities-polygon
        {:stroke "#31bef8"
         :fill "rgba(48, 189, 248, 0.2)"
         :points (s/join " " (map (partial s/join ",") abilities-points))}]
       (map
        (fn [[x1 y1] [x2 y2] c1 c2]
          [:g
           [:line {:x1 x1 :y1 y1 :x2 0 :y2 0 :stroke (str "url(#lg-" (:key c1) "-o)")}]
           [:line {:x1 x1 :y1 y1 :x2 x2 :y2 y2 :stroke (str "url(#lg-" (:key c1) "-" (:key c2) ")") :stroke-width 1.5}]
           [:circle {:cx (* x1 1.05) :cy (* y1 1.05) :r 1 :fill (:color c1)}]])
        points
        (drop 1 (cycle points))
        colors
        (drop 1 (cycle colors)))]]]))

;;(stest/instrument `entity/build)
;;(stest/instrument `t/make-modifier-map)

(defn character-builder []
  (let [option-paths (make-path-map (::character @app-state))
        modifier-map (t/make-modifier-map (::template @app-state))
        built-char (entity/build (::character @app-state) modifier-map)
        _ (prn "BUILT" built-char)]
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
       [:div
        {:style {:display :flex}}
        (into [:div {:style {:width "300px"}}]
              (map (partial builder-selector [] option-paths))
              (::t/selections (::template @app-state)))
        [:div {:style {:flex-grow 1}}]
        [:div
         (let [race (::char5e/race built-char)
               subrace (::char5e/subrace built-char)
               levels (::char5e/levels built-char)]
           [:div {:style {:font-size "24px"
                          :font-weight 600
                          :margin-bottom "16px"
                          :text-align :center
                          :text-shadow "1px 2px 1px black"}}
            [:div (str race
                       (if (and race subrace) " / ")
                       subrace)]
            (if (seq levels)
              [:div
               (map
                (fn [[cls-k {:keys [::char5e/class-name ::char5e/class-level]}]]
                  [:span (str class-name " (" class-level ")")])
                levels)])
            ])
         (abilities-radar 187 (::char5e/abilities built-char))]]]]]))

(r/render [character-builder]
          (js/document.getElementById "app"))

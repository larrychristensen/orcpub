(ns orcpub.core
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
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

            [clojure.spec :as spec]
            [clojure.spec.test :as stest]

            [reagent.core :as r])
  (:require-macros [orcpub.entity-spec :refer [make-entity]]))

(enable-console-print!)

(def arcane-tradition-options
  [(t/option
    "School of Evocation"
    :school-of-evocation
    nil
    [(mod5e/subclass :wizard "School of Evocation")
     (mod5e/trait2 "Evocation Savant")
     (mod5e/trait2 "Sculpt Spells")])])

(declare app-state)

(defn index-of-option [selection option-key]
  (first
   (keep-indexed
    (fn [i v]
      (if (= option-key (::entity/key v))
        i))
    selection)))

(defn get-option-value-path [template path]
  (conj (entity/get-entity-path template path) ::entity/value))

(defn get-option-value [template entity path]
  (get-in entity (get-option-value-path template path)))

(defn update-option [template entity path update-fn]
  (update-in entity (entity/get-entity-path template [] path) update-fn))

(defn remove-list-option [template entity path index]
  (update-option template
                 entity
                 path
                 (fn [list]
                   (keep-indexed
                    (fn [i v] (if (not= i index) v))
                    list))))

(defn get-raw-abilities [character]
  (get-in character [::entity/options :ability-scores ::entity/value]))

(defn abilities-standard [character]
  [:div
   [:div
    {:style {:display :flex
             :justify-content :space-between}}
    (for [[k v] (get-raw-abilities character)]
      ^{:key k} [:div {:style {:margin-top "10px"
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
      ^{:key k}
      [:div {:style {:margin-top "10px"
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

(defn hit-points-roller [die character path]
  [:div
   [:button.form-button
    {:style {:margin-top "10px"}
     :on-click
     (fn []
       (swap! app-state update ::character #(assoc-in % (get-option-value-path (::template @app-state) path) (dice/die-roll die))))}
    "Re-Roll"]])

(def template
  {::t/base
   (es/make-entity
    {?armor-class (+ 10 (?ability-bonuses :dex))
     ?ability-bonuses (reduce-kv
                       (fn [m k v]
                         (assoc m k (int (/ (- v 10) 2))))
                       {}
                       ?abilities)
     ?total-levels (apply + (map (fn [[k {l :class-level}]] l) ?levels))
     ?prof-bonus (+ (int (/ (dec ?total-levels) 4)) 2)
     ?skill-prof-bonuses (reduce
                          (fn [m {k :key}]
                            (assoc m k (if (k ?skill-profs)
                                         (if (k ?skill-expertise)
                                           (* 2 ?prof-bonus)
                                           ?prof-bonus) 0)))
                          {}
                          opt5e/skills)
     ?skill-bonuses (reduce-kv
                     (fn [m k v]
                       (assoc m k (+ v (?ability-bonuses (opt5e/skill-abilities k)))))
                     {}
                     ?skill-prof-bonuses)
     ?max-hit-points (* ?total-levels (?ability-bonuses :con))})
   ::t/selections
   [(t/selection
     "Ability Scores"
     [{::t/name "Standard Roll"
       ::t/key :standard-roll
       ::t/ui-fn #(abilities-roller (::character @app-state))
       ::t/modifiers [(mod5e/deferred-abilities)]}
      {::t/name "Standard Scores"
       ::t/key :standard-scores
       ::t/ui-fn #(abilities-standard (::character @app-state))
       ::t/select-fn #(swap! app-state update ::character (fn [c] (assoc-in c [::entity/options :ability-scores] {::entity/key :standard-scores
                                                                                                                  ::entity/value (char5e/abilities 15 14 13 12 10 8)})))
       ::t/modifiers [(mod5e/deferred-abilities)]}])
    (t/selection
     "Race"
     [(t/option
       "Elf"
       :elf
       [(t/selection
         "Subrace"
         [(t/option
           "High Elf"
           :high-elf
           [(opt5e/wizard-cantrip-selection 1)]
           [(mod5e/subrace2 "High Elf")
            (mod5e/ability2 :int 1)])
          (t/option
           "Wood Elf"
           :wood-elf
           []
           [(mod5e/subrace2 "Wood Elf")
            (mod5e/ability2 :wis 1)])])]
       [(mod5e/race2 "Elf")
        (mod5e/ability2 :dex 2)])
      (t/option
       "Dwarf"
       :dwarf
       [(t/selection
         "Subrace"
         [(t/option
           "Hill Dwarf"
           :hill-dwarf
           [(t/selection
             "Tool Proficiency"
             [])]
           [(mod5e/subrace2 "Hill Dwarf")
            (mod5e/ability2 :wis 1)])
          (t/option
           "Mountain Dwarf"
           :mountain-dwarf
           []
           [(mod5e/subrace2 "Mountain Dwarf")
            (mod5e/ability2 :str 2)])])]
       [(mod5e/race2 "Dwarf")
        (mod5e/ability2 :con 2)
        (mod5e/speed2 25)])])
    (t/selection+
     "Class"
     (fn [selection classes]
       (let [current-classes (into #{}
                                   (map ::entity/key)
                                   (get-in (::character @app-state)
                                           (entity/get-entity-path (::template @app-state) [:class])))]
         (prn "CURRENT_CLASSES" current-classes classes)
         {::entity/key (->> selection
                            ::t/options
                            (map ::t/key)
                            (some #(if (-> % current-classes not) %)))
          ::entity/options {:levels [{::entity/key :1}]}}))
     [(t/option
       "Wizard"
       :wizard
       [(opt5e/skill-selection [:arcana :history :insight :investigation :medicine :religion] 2)
        (t/sequential-selection
         "Levels"
         (fn [selection levels]
           {::entity/key (-> levels count inc str keyword)})
         [(t/option
           "1"
           :1
           [(opt5e/wizard-cantrip-selection 3)
            (opt5e/wizard-spell-selection-1)]
           [(mod5e/saving-throws2 :int :wis)
            (mod5e/level2 :wizard "Wizard" 1)
            (mod5e/max-hit-points2 6)])
          (t/option
           "2"
           :2
           [(t/selection
             "Arcane Tradition"
             arcane-tradition-options)
            (t/selection
             "Hit Points"
             [{::t/name "Roll"
               ::t/key :roll
               ::t/ui-fn #(hit-points-roller 6 (::character @app-state) %)
               ::t/modifiers [(mod5e/deferred-max-hit-points)]}
              (t/option
               "Average"
               :average
               nil
               [(mod5e/max-hit-points2 4)])])]
           [(mod5e/level2 :wizard "Wizard" 2)])
          (t/option
           "3"
           :3
           [(opt5e/wizard-spell-selection-1)]
           [(mod5e/level2 :wizard "Wizard" 3)])
          (t/option
           "4"
           :4
           [(opt5e/ability-score-improvement-selection)]
           [(mod5e/level2 :wizard "Wizard" 3)])])]
       [])
      (t/option
       "Rogue"
       :rogue
       [(t/sequential-selection
         "Levels"
         (fn [selection levels]
           {::entity/key (-> levels count str keyword)})
         [(t/option
           "1"
           :1
           [(opt5e/expertise-selection)]
           [(mod5e/saving-throws2 :dex :int)
            (mod5e/level2 :rogue "Rogue" 1)
            (mod5e/max-hit-points2 8)])
          (t/option
           "2"
           :2
           [(t/selection
             "Roguish Archetype"
             arcane-tradition-options)
            (t/selection
             "Hit Points"
             [(t/option
               "Average"
               :average
               []
               [(mod5e/max-hit-points2 5)])])]
           [(mod5e/level2 :rogue "Rogue" 2)])
          (t/option
           "3"
           :3
           []
           [(mod5e/level2 :rogue "rogue" 3)])])]
       [])])]})

(def character
  {::entity/options {:ability-scores {::entity/key :standard-roll
                                      ::entity/value (char5e/abilities 12 13 14 15 16 17)}
                     :race {::entity/key :elf
                            ::entity/options {:subrace {::entity/key :high-elf
                                                        ::entity/options {:cantrip {::entity/key :light}}}}}
                     :class [{::entity/key :wizard
                              ::entity/options {:levels [{::entity/key :1
                                                          ::entity/options {:cantrips-known [{::entity/key :acid-splash}]
                                                                            :spells-known [{::entity/key :mage-armor} {::entity/key :magic-missile}]}}
                                                         {::entity/key :2
                                                          ::entity/options {:arcane-tradition {::entity/key :school-of-evocation}
                                                                            :hit-points {::entity/key :roll
                                                                                         ::entity/value 3}}}
                                                         {::entity/key :3}
                                                         {::entity/key :4
                                                          ::entity/options {:ability-score-improvement-feat {::entity/key :ability-score-improvement
                                                                                                             ::entity/options {:abilities [{::entity/key :cha}]}}}}]}}]}})

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

(declare builder-selector)

(defn option [path option-paths selectable? {:keys [::t/key ::t/name ::t/selections ::t/modifiers ::t/ui-fn ::t/select-fn]}]
  (let [new-path (conj path key)
        selected? (boolean (get-in option-paths new-path))
        named-mods (filter ::mod/name modifiers)]
    ^{:key key}
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
                        (swap! app-state update ::character #(update-option (::template @app-state) % path (fn [o] (assoc o ::entity/key key)))))))
                  (.stopPropagation e))}
     [:span {:style {:font-weight :bold}} name]
     (if (seq named-mods)
       [:span {:style {:font-style :italic
                       :font-size "12px"
                       :margin-left "10px"
                       :font-weight :normal}}
        (s/join
         ", "
         (map
          (fn [m]
            (str
             (::mod/name m)
             " "
             (let [v (or (::mod/value m) (get-option-value (::template @app-state) (::character @app-state) path))]
               v)))
          named-mods))])
     (if selected?
       [:div
        (if ui-fn (ui-fn path))
        [:div
         (map
          (fn [selection]
            ^{:key (::t/key selection)}
            [builder-selector new-path option-paths selection])
          selections)]])]))

(def builder-selector-style)

(defn dropdown-option [option]
  [:option.builder-dropdown-item
   {:value (str (::t/key option))}
   (::t/name option)])

(defn dropdown [options selected-value change-fn]
  [:select.builder-option.builder-option-dropdown
   {:on-change change-fn
    :value (or (str selected-value) "")}
   [:option.builder-dropdown-item]
   (doall
    (map
     (fn [option]
       ^{:key (::t/key option)} [dropdown-option option])
     options))])

(defn add-option-button [{:keys [::t/key ::t/name ::t/options] :as selection} path new-item-fn]
  [:div.add-item-button
   [:i.fa.fa-plus-circle]
   [:span
    {:on-click
     (fn []
       (let [new-item (new-item-fn selection options)]
         (swap! app-state update ::character
                #(update-option (::template @app-state) % path
                                (fn [options] (conj (vec options) new-item))))))
     :style {:margin-left "5px"}} (str "Add " name)]])

(defn set-option-value [char path value]
  (let [number-indices (keep-indexed (fn [i v] (if (number? v) i))
                                     path)
        subpaths (map #(subvec path 0 %) number-indices)]
    (assoc-in
     (reduce
      (fn [c p]
        (if (nil? (get-in c p))
          (assoc-in c p [])
          c))
      char
      subpaths)
     path
     value)))

(defn dropdown-selector [path option-paths {:keys [::t/options ::t/min ::t/max ::t/key ::t/name ::t/sequential? ::t/new-item-fn] :as selection}]
  (let [change-fn (fn [i]
                    (fn [e]
                      (let [new-path (concat path [key i])
                            option-path (entity/get-entity-path (::template @app-state) new-path)
                            new-value (cljs.reader/read-string (.. e -target -value))]
                        (swap! app-state update ::character
                               #(set-option-value % (conj option-path ::entity/key) new-value)))))]
    [:div
     (if max
       (if (= min max)
         (doall
          (for [i (range max)]
            (let [option-path (conj path key i)
                  entity-path (entity/get-entity-path (::template @app-state) option-path)
                  key-path (conj entity-path ::entity/key)
                  value (get-in (::character @app-state) key-path)]
              ^{:key i} [:div (dropdown options value (change-fn i))]))))
       [:div
        (doall
         (map-indexed
          (fn [i {value ::entity/key}]
            ^{:key value}
            [:div (dropdown options value (change-fn i))])
          (get-in (::character @app-state) (entity/get-entity-path (::template @app-state) (conj path key)))))
        (add-option-button selection (conj path key) new-item-fn)])]))

(defn remove-option-button [path index]
  [:i.fa.fa-minus-circle.remove-item-button
   {:on-click
    (fn [e]
      (swap! app-state update ::character #(remove-list-option (::template @app-state) % path index)))}])

(defn filter-selected [path key option-paths options]
  (filter
   (fn [opt]
     (let [option-path (concat path [key (::t/key opt)])]
       (get-in option-paths option-path)))
   options))

(defn list-selector-option [removeable? path option-paths multiple-select? i opt]
  [:div.list-selector-option
   [:div {:style {:flex-grow 1}}
    [option path option-paths (not multiple-select?) opt]]
   (if (removeable? i)
     [remove-option-button path i])])

(defn list-selector [path option-paths {:keys [::t/options ::t/min ::t/max ::t/key ::t/name ::t/sequential? ::t/new-item-fn] :as selection}]
  (let [no-max? (nil? max)
        multiple-select? (or no-max? (> max 1))
        selected-options (filter-selected path key option-paths options)
        addable? (and multiple-select?
                      (or no-max?
                          (< (count selected-options) max)))
        more-than-min? (> (count selected-options) min)
        next-path (conj path key)]
    [:div
     (doall
      (map-indexed
       (fn [i option]
         ^{:key i}
         [list-selector-option
          #(and multiple-select?
                more-than-min?
                (or (not sequential?)
                    (= % (dec (count selected-options)))))
          next-path
          option-paths
          multiple-select?
          i
          option])
       (if multiple-select?
         selected-options
         options)))
     (if (and addable? new-item-fn)
       (add-option-button selection (conj path key) new-item-fn))]))

(defn builder-selector [path option-paths selection]
  ^{:key (::t/name selection)}
  [:div.builder-selector
   [:h2.builder-selector-header (::t/name selection)]
   [:div
    (let [simple-options?
          (not-any? #(or (seq (::t/selections %))
                         (::t/ui-fn %))
                    (::t/options selection))]
      (if simple-options?
        [dropdown-selector path option-paths selection]
        [list-selector path option-paths selection]))]])


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
   :opacity 0.2
   :border-bottom "5px solid rgba(255, 255, 255, 0.05)"})

(def selected-tab-style
  (merge
   tab-style
   {:border-bottom-color "#f1a20f"
    :opacity 1}))

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

(defn abilities-radar [size abilities ability-bonuses]
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
        ^{:key color}
        [:div {:style {:width 50 :text-align :center :position :absolute :left x :top y}}
         [:div
          [:span (s/upper-case (name ak))]
          [:span {:style {:margin-left 5 :color color}} av]]
         [:div {:style {:color color}} (let [bonus (int (/ (- av 10) 2))]
                                         (str "(" (mod/bonus-str (get ability-bonuses ak)) ")"))]])
      offset-abilities
      (take 6 (drop 1 (cycle text-points)))
      colors)
     [:svg {:width (+ 80 double-beta double-point-offset) :height (+ 60 d double-point-offset)}
      [:defs
       (map
        (fn [[x1 y1] [x2 y2] c1 c2]
          ^{:key (:key c1)}
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
          ^{:key (:key c1)}
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

;;(prn "MODIFIER MAP" (cljs.pprint/pprint (t/make-modifier-map (::template @app-state))))
;;(spec/explain ::t/modifier-map (t/make-modifier-map (::template @app-state)))

(defn print-char [built-char]
  (cljs.pprint/pprint
   (reduce-kv
    (fn [m k v]
      (assoc m k (es/entity-val built-char k)))
    {}
    built-char)))

(defn character-builder []
  (cljs.pprint/pprint (::character @app-state))
  (let [option-paths (make-path-map (::character @app-state))
        built-char (entity/build (::character @app-state) (::template @app-state))]
    (print-char built-char)
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
        [:div {:style {:width "300px"}}
         (map
          (fn [selection]
            ^{:key (::t/key selection)}
            [builder-selector [] option-paths selection])
          (::t/selections (::template @app-state)))]
        [:div {:style {:flex-grow 1
                       :margin-top "10px"
                       :margin-left "30px"
                       :margin-right "80px"}}
         [:div {:style {:margin-top "5px"}}
          [:span.personality-label {:style {:font-size "18px"}} "Character Name"]
          [:input.input {:type :text}]]
         [:div.field
          [:span.personality-label {:style {:font-size "18px"}} "Personality Trait 1"]
          [:select.builder-option.builder-option-dropdown]]
         [:div.field
          [:span.personality-label {:style {}} "Personality Trait 2"]
          [:select.builder-option.builder-option-dropdown]]
         [:div.field
          [:span.personality-label {:style {}} "Ideals"]
          [:select.builder-option.builder-option-dropdown]]
         [:div.field
          [:span.personality-label {:style {}} "Bonds"]
          [:select.builder-option.builder-option-dropdown]]
         [:div.field
          [:span.personality-label {:style {}} "Flaws"]
          [:select.builder-option.builder-option-dropdown]]]
        [:div
         (let [race (es/entity-val built-char :race)
               subrace (es/entity-val built-char :subrace)
               levels (es/entity-val built-char :levels)]
           [:div {:style {:font-size "24px"
                          :font-weight 600
                          :margin-bottom "16px"
                          :text-shadow "1px 2px 1px black"}}
            [:span race]
            (if (seq levels)
              [:span
               {:style {:margin-left "10px"}}
               (apply
                str
                (interpose
                 " / "
                 (map
                  (fn [[cls-k {:keys [class-name class-level subclass]}]]
                    (str class-name " (" class-level ")"))
                  levels)))])])
         [:div {:style {:display :flex}}
          [:div
           [:img {:src "image/barbarian-girl.png"
                  :style {:width "267px"}}]
           (abilities-radar 187 (es/entity-val built-char :abilities) (es/entity-val built-char :ability-bonuses))]
          [:div {:style {:width "250px"}}
           [:div {:style {:margin-left "25px" :margin-top "20px"}}
            [:span {:style {:font-size "16px" :font-weight 600}} "Armor Class"]
            [:div {:style {:margin-top "4px"}}
             [:i.fa.fa-shield {:style {:font-size "32px" :color :white}}]
             [:span {:style {:font-size "24px" :font-weight 600 :margin-left "18px"}} (es/entity-val built-char :armor-class)]
             [:span {:style {:margin-left "5px"}} "(padded armor)"]]]
           [:div {:style {:margin-left "25px" :margin-top "20px"}}
            [:span {:style {:font-size "16px" :font-weight 600}} "Hit Points"]
            [:div {:style {:margin-top "4px"}}
             [:i.fa.fa-crosshairs {:style {:font-size "32px" :color :white}}]
             [:span {:style {:font-size "24px" :font-weight 600 :margin-left "18px"}} (es/entity-val built-char :max-hit-points)]]]
           [:div {:style {:margin-left "25px" :margin-top "20px"}}
            [:span {:style {:font-size "16px" :font-weight 600}} "Skills"]
            [:div {:style {:margin-top "4px"}}
             [:span {:style {:font-size "14px" :font-weight 600}}
              (s/join
               ", "
               (let [skill-bonuses (es/entity-val built-char :skill-bonuses)]
                 (map
                  (fn [skill-kw]
                    (str (s/capitalize (name skill-kw)) " " (mod/bonus-str (skill-bonuses skill-kw))))
                  (es/entity-val built-char :skill-profs))))]]]]]]]]]]))

(r/render [character-builder]
          (js/document.getElementById "app"))

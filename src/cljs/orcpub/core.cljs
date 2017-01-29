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
            [orcpub.dnd.e5.template :as t5e]
            [orcpub.dnd.e5.spells :as spells]

            [clojure.spec :as spec]
            [clojure.spec.test :as stest]

            [reagent.core :as r])
  (:require-macros [orcpub.entity-spec :refer [make-entity]]))

(enable-console-print!)

(declare app-state)

(defn index-of-option [selection option-key]
  (first
   (keep-indexed
    (fn [i v]
      (if (= option-key (::entity/key v))
        i))
    selection)))

(defn get-option-value [template entity path]
  (get-in entity (entity/get-option-value-path template entity path)))


(defn update-option [template entity path update-fn]
  (update-in entity (entity/get-entity-path template entity [] path) update-fn))

(defn remove-list-option [template entity path index]
  (update-option template
                 entity
                 path
                 (fn [list]
                   (keep-indexed
                    (fn [i v] (if (not= i index) v))
                    list))))

(defonce character-ref (r/atom t5e/character))

(def text-color
  {:color :white})

(def field-font-size
  {:font-size "14px"})

(def template (t5e/template character-ref))

(defonce app-state
  (r/atom
   {::template template}))

(add-watch app-state :log (fn [k r os ns]
                            (js/console.log "OLD" (clj->js os))
                            (js/console.log "NEW" (clj->js ns))))

(declare builder-selector)

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
    (map-indexed
     (fn [i option]
       ^{:key i} [dropdown-option option])
     options))])

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

(defn make-dropdown-change-fn [path key template raw-char character-ref i]
  (fn [e]
    (let [new-path (concat path [key i])
          option-path (entity/get-entity-path template raw-char new-path)
          new-value (cljs.reader/read-string (.. e -target -value))]
      (swap! character-ref #(set-option-value % (conj option-path ::entity/key) new-value)))))

(defn option [path option-paths selectable? {:keys [::t/key ::t/name ::t/selections ::t/modifiers ::t/prereqs ::t/ui-fn ::t/select-fn]} built-char raw-char changeable? options change-fn]
  (let [new-path (conj path key)
        selected? (boolean (get-in option-paths new-path))
        named-mods (filter ::mod/name modifiers)
        failed-prereqs (reduce
                        (fn [failures {:keys [::t/prereq-fn ::t/label]}]
                          (if (not (prereq-fn built-char))
                            (conj failures label)))
                        []
                        prereqs)
        meets-prereqs? (empty? failed-prereqs)]
    ^{:key key}
    [:div.builder-option
     {:class-name (clojure.string/join
                   " "
                   [(if selected? "selected-builder-option")
                    (if (and meets-prereqs? selectable?) "selectable-builder-option")
                    (if (not meets-prereqs?) "disabled-builder-option")])
      :on-click (fn [e]
                  (if (and meets-prereqs? selectable?)
                    (do
                      (if select-fn
                        (select-fn path))
                      (swap! character-ref #(update-option template % path (fn [o] (assoc o ::entity/key key))))))
                  (.stopPropagation e))}
     (if changeable? (dropdown options key change-fn) [:span {:style {:font-weight :bold}} name])
     (if (not meets-prereqs?)
       [:div {:style {:font-style :italic
                      :font-size "12px"
                      :font-weight :normal}} (str "Requires " (s/join ", " failed-prereqs))])
     (if (and meets-prereqs? (seq named-mods))
       [:span {:style {:font-style :italic
                       :font-size "12px"
                       :margin-left "10px"
                       :font-weight :normal}}
        (s/join
         ", "
         (map
          (fn [{:keys [::mod/value ::mod/val-fn] :as m}]
            (let []
              (str
               (::mod/name m)
               " "
               (let [v (or value (get-option-value template @character-ref path))]
                 (if val-fn
                   (val-fn v)
                   v)))))
          named-mods))])
     (if selected?
       [:div
        (if ui-fn (ui-fn path))
        [:div
         (doall
          (map
           (fn [selection]
             ^{:key (::t/key selection)}
             [builder-selector new-path option-paths selection built-char raw-char])
           selections))]])]))

(def builder-selector-style)

(defn add-option-button [{:keys [::t/key ::t/name ::t/options] :as selection} entity path new-item-fn]
  [:div.add-item-button
   [:i.fa.fa-plus-circle]
   [:span
    {:on-click
     (fn []
       (let [template template
             value-path (entity/get-entity-path template entity path)
             new-item (new-item-fn
                       selection
                       options
                       (get-in @character-ref value-path))]
         (swap! character-ref #(update-option template % path
                                (fn [options] (conj (vec options) new-item))))))
     :style {:margin-left "5px"}} (str "Add " name)]])

(defn dropdown-selector [path option-paths {:keys [::t/options ::t/min ::t/max ::t/key ::t/name ::t/sequential? ::t/new-item-fn] :as selection} raw-char]
  (let [change-fn (partial make-dropdown-change-fn path key template raw-char character-ref)]
    [:div
     (if max
       (if (= min max)
         (doall
          (for [i (range max)]
            (let [option-path (conj path key i)
                  entity-path (entity/get-entity-path template raw-char option-path)
                  key-path (conj entity-path ::entity/key)
                  value (get-in @character-ref key-path)]
              ^{:key i} [:div (dropdown options value (change-fn i))]))))
       [:div
        (doall
         (map-indexed
          (fn [i {value ::entity/key}]
            ^{:key i}
            [:div (dropdown options value (change-fn i))])
          (get-in @character-ref (entity/get-entity-path template raw-char (conj path key)))))
        (add-option-button selection raw-char (conj path key) new-item-fn)])]))

(defn remove-option-button [path index]
  [:i.fa.fa-minus-circle.remove-item-button
   {:on-click
    (fn [e]
      (swap! character-ref #(remove-list-option template % path index)))}])

(defn filter-selected [path key option-paths options raw-char]
  (let [options-path (conj path key)
        entity-opt-path (entity/get-entity-path template raw-char options-path)
        selected (get-in raw-char entity-opt-path)]
    (if (sequential? selected)
      (let [options-map (into {} (map (juxt ::t/key identity) options))]
        (map
         (fn [{k ::entity/key}]
           (options-map k))
         selected))
      (filter
       (fn [opt]
         (let [option-path (concat path [key (::t/key opt)])]
           (get-in option-paths option-path)))
       options))))

(defn list-selector-option [removeable? path option-paths multiple-select? i opt built-char raw-char changeable? options change-fn]
  [:div.list-selector-option
   [:div {:style {:flex-grow 1}}
    [option path option-paths (not multiple-select?) opt built-char raw-char changeable? options change-fn]]
   (if (removeable? i)
     [remove-option-button path i])])

(defn list-selector [path option-paths {:keys [::t/options ::t/min ::t/max ::t/key ::t/name ::t/sequential? ::t/new-item-fn] :as selection} built-char raw-char]
  (let [no-max? (nil? max)
        multiple-select? (or no-max? (> max 1))
        selected-options (filter-selected path key option-paths options raw-char)
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
          option
          built-char
          raw-char
          (and addable? (not sequential?))
          options
          (make-dropdown-change-fn path key template raw-char character-ref i)])
       (if multiple-select?
         selected-options
         options)))
     (if (and addable? new-item-fn)
       (add-option-button selection raw-char (conj path key) new-item-fn))]))

(defn builder-selector [path option-paths selection built-char raw-char]
  ^{:key (::t/name selection)}
  [:div.builder-selector
   [:h2.builder-selector-header (::t/name selection)]
   [:div
    (let [simple-options? 
          (or (::t/simple? selection)
              (not-any? #(or (seq (::t/selections %))
                             (some ::mod/name (::t/modifiers %))
                             (::t/ui-fn %))
                        (::t/options selection)))]
      (if simple-options?
        [dropdown-selector path option-paths selection raw-char]
        [list-selector path option-paths selection built-char raw-char]))]])

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

;;(prn "MODIFIER MAP" (cljs.pprint/pprint (t/make-modifier-map template)))
;;(spec/explain ::t/modifier-map (t/make-modifier-map template))

(defn print-char [built-char]
  (cljs.pprint/pprint
   (reduce-kv
    (fn [m k v]
      (assoc m k (es/entity-val built-char k)))
    {}
    built-char)))

(defn display-section [title icon-cls value & [list?]]
  [:div {:style {:margin-left "25px" :margin-top "20px"}}
   [:span {:style {:font-size "16px" :font-weight 600}} title]
   [:div {:style {:margin-top (if list? "0px" "4px")}}
    (if icon-cls [:i.fa {:class-name icon-cls :style {:font-size "32px" :margin-right "18px" :color :white}}])
    [:span {:style {:font-size "24px" :font-weight 600}}
     value]]])

(defn list-display-section [title icon-cls values]
  (if (seq values)
    (display-section title icon-cls
                     [:span
                      {:style {:margin-top "5px" :font-size "14px" :font-weight :normal :font-style :italic}}
                      (s/join
                       ", "
                       values)]
                     true)))

(defn character-builder []
  (cljs.pprint/pprint @character-ref)
  (let [option-paths (make-path-map @character-ref)
        built-char (entity/build @character-ref template)]
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
         (doall
          (map
           (fn [selection]
             ^{:key (::t/key selection)}
             [builder-selector [] option-paths selection built-char @character-ref])
           (::t/selections template)))]
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
        (let [race (es/entity-val built-char :race)
              subrace (es/entity-val built-char :subrace)
              levels (es/entity-val built-char :levels)
              darkvision (es/entity-val built-char :darkvision)
              skill-profs (es/entity-val built-char :skill-profs)
              tool-profs (es/entity-val built-char :tool-profs)
              weapon-profs (es/entity-val built-char :weapon-profs)
              armor-profs (es/entity-val built-char :armor-profs)
              resistances (es/entity-val built-char :resistances)
              languages (es/entity-val built-char :languages)]
          [:div {:style {:width "500px"}}
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
                  levels)))])]
           [:div {:style {:display :flex}}
            [:div
             [:img {:src "image/barbarian-girl.png"
                    :style {:width "267px"}}]
             (abilities-radar 187 (es/entity-val built-char :abilities) (es/entity-val built-char :ability-bonuses))]
            [:div {:style {:width "250px"}}
             (display-section "Armor Class" "fa-shield" (es/entity-val built-char :armor-class))
             (display-section "Hit Points" "fa-crosshairs" (es/entity-val built-char :max-hit-points))
             (display-section "Speed" nil (es/entity-val built-char :speed))
             (display-section "Darkvision" "fa-low-vision" (if darkvision (str darkvision " ft.") "--"))
             (display-section "Initiative" nil (mod/bonus-str (es/entity-val built-char :initiative)))
             (display-section "Passive Perception" nil (es/entity-val built-char :passive-perception))
             (list-display-section "Skill Proficiencies" nil
                                   (let [skill-bonuses (es/entity-val built-char :skill-bonuses)]
                                     (map
                                      (fn [skill-kw]
                                        (str (s/capitalize (name skill-kw)) " " (mod/bonus-str (skill-bonuses skill-kw))))
                                      skill-profs)))
             (list-display-section "Languages" nil
                                   (map
                                    (fn [lang]
                                      (:name lang))
                                    languages))
             (list-display-section "Tool Proficiencies" nil
                                   (map
                                    (fn [tool]
                                      (:name tool))
                                    tool-profs))
             (list-display-section "Weapon Proficiencies" nil
                                   (map
                                    (fn [tool]
                                      (:name tool))
                                    weapon-profs))
             (list-display-section "Armor Proficiencies" nil
                                   (map
                                    (fn [armor]
                                      (:name armor))
                                    armor-profs))
             (list-display-section "Resistances" nil
                                   (map
                                    (fn [resistance-kw]
                                      (name resistance-kw))
                                    resistances))
             (display-section "Spells Known" nil
                                   [:div {:style {:font-size "14px"}}
                                    (map
                                     (fn [[level spells]]
                                       ^{:key level}
                                       [:div {:style {:margin-top "10px"}}
                                        [:span {:font-weight 600} (if (zero? level) "Cantrip" (str "Level " level))]
                                        [:div {:style {:font-style :italic
                                                       :font-weight :normal}}
                                         (map
                                          (fn [spell]
                                            (let [spell-data (spells/spell-map (:key spell))]
                                              ^{:key (:key spell)}
                                              [:div
                                               (str
                                                (:name (spells/spell-map (:key spell)))
                                                " ("
                                                (s/upper-case (name (:ability spell))) ")")]))
                                          (filter (fn [{k :key}] (spells/spell-map k)) spells))]])
                                     (es/entity-val built-char :spells-known))])
             (list-display-section "Weapons" nil
                                   (map
                                    (fn [[weapon-kw num]]
                                      (str (name weapon-kw) " (" num ")"))
                                    (es/entity-val built-char :weapons)))
             (list-display-section "Armor" nil
                                   (map
                                    (fn [[armor-kw num]]
                                      (str (name armor-kw) " (" num ")"))
                                    (es/entity-val built-char :armor)))
             (list-display-section "Equipment" nil
                                   (map
                                    (fn [[equipment-kw num]]
                                      (str (name equipment-kw) " (" num ")"))
                                    (es/entity-val built-char :equipment)))]]
           (display-section
            "Features, Traits, & Feats" nil
            [:div
             {:style {:font-size "14px"}}
             (map
              (fn [{:keys [name description]}]
                ^{:key name}
                [:p {:style {:margin-top "10px"}}
                 [:span {:style {:font-weight 600 :font-style :italic}} name "."]
                 [:span {:style {:font-weight :normal :margin-left "10px"}} description]])
              (es/entity-val built-char :traits))])])]]]]))


(r/render [character-builder]
          (js/document.getElementById "app"))

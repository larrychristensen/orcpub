(ns orcpub.core
  (:require [goog.dom :as gdom]
            [goog.labs.userAgent.device :as device]
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

(defn update-in-entity [m [k & ks] f & args]
  (let [current (get m k)
         val (if (and (int? k)
                      (>= k (count current)))
               (vec (concat current (repeat (inc (- k (count current))))))
               current)]
     (if ks
       (assoc m k (apply update-in val ks f args))
       (assoc m k (apply f val args)))))

(defn update-option [template entity path update-fn]
  (update-in entity (entity/get-entity-path template entity [] path) update-fn))

(defn remove-list-option [template entity path index]
  (update-option template
                 entity
                 path
                 (fn [list]
                   (vec
                    (keep-indexed
                     (fn [i v] (if (not= i index) v))
                     list)))))

(defonce character-ref (r/atom t5e/character))

(def text-color
  {:color :white})

(def field-font-size
  {:font-size "14px"})

(def template (t5e/template character-ref))

(defonce app-state
  (r/atom
   {:collapsed-paths #{[:ability-scores]
                       [:background]
                       [:race]}
    :builder {:character {:tab 0}}}))

#_(add-watch app-state :log (fn [k r os ns]
                            (js/console.log "OLD" (clj->js os))
                            (js/console.log "NEW" (clj->js ns))))

(declare builder-selector)

(defn dropdown-option [option]
  [:option.builder-dropdown-item
   {:value (str (::t/key option))}
   (::t/name option)])

(defn dropdown [options selected-value change-fn built-char]
  [:select.builder-option.builder-option-dropdown
   {:on-change change-fn
    :value (or (str selected-value) "")}
   [:option.builder-dropdown-item]
   (doall
    (map-indexed
     (fn [i option]
       ^{:key i} [dropdown-option option])
     (filter (fn [{:keys [::t/prereqs]}]
               (or (nil? prereqs)
                   (every? #(% built-char) prereqs)))
             options)))])

(defn set-option-value [char path value]
  (let [number-indices (keep-indexed (fn [i v] (if (number? v) i))
                                     path)
        subpaths (map #(subvec path 0 (inc %)) number-indices)]
    (assoc-in
     (reduce
      (fn [c p]
        (let [vec-path (butlast p)
              v (get-in c vec-path)
              remaining (inc (- (last p) (count v)))]
          (if (nil? v)
            (assoc-in c vec-path (vec (repeat remaining {})))
            c)))
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


(defn option [path option-paths selectable? list-collapsed? {:keys [::t/key ::t/name ::t/selections ::t/modifiers ::t/prereqs ::t/ui-fn ::t/select-fn]} built-char raw-char changeable? options change-fn built-template]
  (let [new-path (conj path key)
        selected? (boolean (get-in option-paths new-path))
        collapsed? (get (:collapsed-paths @app-state) new-path)
        named-mods (filter ::mod/name modifiers)
        failed-prereqs (reduce
                        (fn [failures {:keys [::t/prereq-fn ::t/label]}]
                          (if (not (prereq-fn built-char))
                            (conj failures label)))
                        []
                        prereqs)
        meets-prereqs? (empty? failed-prereqs)]
    ^{:key key}
    (if (or selected? (not list-collapsed?))
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
                        (swap! character-ref #(update-option built-template % path (fn [o] (assoc o ::entity/key key))))))
                    (.stopPropagation e))}
       [:div
        {:style {:display :flex
                 :justify-content :space-between
                 :align-items :center}}
        [:div
         {:style {:flex-grow 1}}
         (if changeable?
           [dropdown options key change-fn built-char]
           [:span {:style {:font-weight :bold}} name])
         (if (not meets-prereqs?)
           [:div {:style {:font-style :italic
                          :font-size "12px"
                          :font-weight :normal}}
            (str "Requires " (s/join ", " failed-prereqs))])
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
                   (let [v (or value (get-option-value built-template @character-ref path))]
                     (if val-fn
                       (val-fn v)
                       v)))))
              named-mods))])]
        (if (and selected?
                 (seq selections))
          (if collapsed?
            [:span.expand-collapse-button
             {:on-click (fn [_]
                          (swap! app-state update :collapsed-paths disj new-path))}
             "Expand"]
            [:span.expand-collapse-button
             {:on-click (fn [_]
                          (swap! app-state update :collapsed-paths conj new-path))}
             "Collapse"]))]
       (if (and selected? (not collapsed?))
         [:div
          (if ui-fn (ui-fn path))
          [:div
           (doall
            (map
             (fn [{:keys [::t/prereq-fn ::t/key] :as selection}]
               (if (or (not prereq-fn) (prereq-fn built-char))
                 ^{:key key}
                 [builder-selector new-path option-paths selection built-char raw-char built-template]))
             selections))]]
         (if collapsed? [:div.builder-option.collapsed-list-builder-option]))])))

(def builder-selector-style)

(defn add-option-button [{:keys [::t/key ::t/name ::t/options] :as selection} entity path new-item-fn built-template]
  [:div.add-item-button
   [:i.fa.fa-plus-circle]
   [:span
    {:on-click
     (fn []
       (let [value-path (entity/get-entity-path built-template entity path)
             new-item (new-item-fn
                       selection
                       options
                       (get-in @character-ref value-path))]
         (swap! character-ref #(update-option built-template % path
                                (fn [options] (conj (vec options) new-item))))))
     :style {:margin-left "5px"}} (str "Add " name)]])


(defn dropdown-selector [path option-paths {:keys [::t/options ::t/min ::t/max ::t/key ::t/name ::t/sequential? ::t/new-item-fn] :as selection} built-char raw-char built-template]
  (let [change-fn (partial make-dropdown-change-fn path key built-template raw-char character-ref)
        options (filter (fn [{:keys [::t/prereq-fn]}]
                          (or (not prereq-fn) (prereq-fn built-char)))
                        options)]
    [:div
     (if max
       (if (= min max)
         (doall
          (for [i (range max)]
            (let [option-path (conj path key i)
                  entity-path (entity/get-entity-path built-template raw-char option-path)
                  key-path (conj entity-path ::entity/key)
                  value (get-in @character-ref key-path)]
              ^{:key i} [:div (dropdown options value (change-fn i) built-char)]))))
       [:div
        (let [full-path (conj path key)
              entity-path (entity/get-entity-path built-template raw-char full-path)
              selected (get-in @character-ref entity-path)
              remaining (- min (count selected))
              final-options (if (pos? remaining)
                              (vec (concat selected (repeat remaining {::entity/key :wish})))
                              selected)]
          (doall
           (map-indexed
            (fn [i {value ::entity/key}]
              ^{:key i}
              [:div [dropdown options value (change-fn i) built-char]])
            final-options)))
        (add-option-button selection raw-char (conj path key) new-item-fn built-template)])]))

(defn remove-option-button [path built-template index]
  [:i.fa.fa-minus-circle.remove-item-button
   {:on-click
    (fn [e]
      (swap! character-ref #(remove-list-option built-template % path index)))}])

(defn filter-selected [path key option-paths options raw-char built-template]
  (let [options-path (conj path key)
        entity-opt-path (entity/get-entity-path built-template raw-char options-path)
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

(defn list-selector-option [removeable? path option-paths multiple-select? list-collapsed? i opt built-char raw-char changeable? options change-fn built-template]
  [:div.list-selector-option
   [:div {:style {:flex-grow 1}}
    [option path option-paths (not multiple-select?) list-collapsed? opt built-char raw-char changeable? options change-fn built-template]]
   (if (removeable? i)
     [remove-option-button path built-template i])])

(defn list-selector [path option-paths {:keys [::t/options ::t/min ::t/max ::t/key ::t/name ::t/sequential? ::t/new-item-fn] :as selection} collapsed? built-char raw-char built-template]
  (let [no-max? (nil? max)
        multiple-select? (or no-max? (> max 1))
        selected-options (filter-selected path key option-paths options raw-char built-template)
        addable? (and multiple-select?
                      (or no-max?
                          (< (count selected-options) max)))
        more-than-min? (> (count selected-options) min)
        next-path (conj path key)]
    [:div
     (if collapsed? [:div.builder-option.collapsed-list-builder-option])
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
           collapsed?
           i
           option
           built-char
           raw-char
           (and addable? (not sequential?))
           options
           (make-dropdown-change-fn path key built-template raw-char character-ref i)
           built-template])
        (if multiple-select?
          selected-options
          options)))]
     (if collapsed? [:div.builder-option.collapsed-list-builder-option])
     (if (and addable? new-item-fn)
       (add-option-button selection raw-char (conj path key) new-item-fn built-template))]))

(defn builder-selector [path option-paths {:keys [::t/name ::t/key ::t/min ::t/max] :as selection} built-char raw-char built-template]
  (let [new-path (conj path key)
        collapsed? (get (:collapsed-paths @app-state) new-path)]
    ^{:key key}
    [:div.builder-selector
     [:div {:style {:display :flex
                    :justify-content :space-between
                    :align-items :center}}
      (if (zero? (count path))
        [:h1 {:style {:font-size "24px"}} (::t/name selection)]
        [:h2.builder-selector-header (::t/name selection)])
      (if (and (not (or (nil? max) (> max min))) (zero? (count path)))
        (if collapsed?
          [:span.expand-collapse-button
           {:on-click (fn [_]
                        (swap! app-state update :collapsed-paths disj new-path))}
           "Show All Options"]
          [:span.expand-collapse-button
           {:on-click (fn [_]
                        (swap! app-state update :collapsed-paths conj new-path))}
           "Hide Unselected Options"]))]
     [:div
      (let [simple-options? 
            (or (::t/simple? selection)
                (not-any? #(or (seq (::t/selections %))
                               (some ::mod/name (::t/modifiers %))
                               (::t/ui-fn %))
                          (::t/options selection)))]
        (if simple-options?
          [dropdown-selector path option-paths selection built-char raw-char built-template]
          [list-selector path option-paths selection collapsed? built-char raw-char built-template]))]]))

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
   {;;:height 82
    :text-transform :uppercase
    :font-weight 600
    :padding "15px"}
   ;;content-style
   text-color
   field-font-size))

(def base-tab-style
  {;;:padding-top 48
   :display :inline-block
   :opacity 0.2
   :border-bottom "5px solid rgba(255, 255, 255, 0.3)"})

(def tab-style
  (merge
   base-tab-style
   { ;;:padding-top 48
    :padding-left 25
    :padding-right 25
    :padding-bottom 13}))

(def tab-spacer-style
  {:width "10px"})

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
    :margin-bottom 19
    :margin-left 10}))

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
    [:div {:style {:position :relative
                   :width "100%"}}
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
  [:div {:style {:margin-top "20px"}}
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

(defn armor-class-section [armor-class armor-class-with-armor equipped-armor]
  [display-section
   "Armor Class"
   "fa-shield"
   [:span
    [:span
     [:span armor-class]
     [:span {:style {:font-size "12px"
                     :margin-left "5px"}} "(unarmored)"]]
    [:div
     {:style {:margin-left "40px"}}
     (let [has-shield? (:shield equipped-armor)]
      (doall
       (map
        (fn [[armor-kw _]]
          (let [armor (opt5e/armor-map armor-kw)
                ac-fn armor-class-with-armor
                ac (ac-fn armor)]
            ^{:key armor-kw}
            [:div
             [:div
              [:span ac]
              [:span {:style {:font-size "12px"
                              :margin-left "5px"}} (str "(" (:name armor) ")")]]
             (if has-shield?
               [:div
                [:span (+ 2 ac)]
                [:span {:style {:font-size "12px"
                                :margin-left "5px"}} (str "(" (:name armor) " + shield)")]])]))
        (dissoc equipped-armor :shield))))]]])

(defn list-item-section [list-name items & [name-fn]]
  [list-display-section list-name nil
   (map
    (fn [item]
      ((or name-fn :name) item))
    items)])

(defn spells-known-section [spells-known]
  [display-section "Spells Known" nil
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
     spells-known)]])

(defn equipment-section [title equipment equipment-map]
  [list-display-section title nil
   (map
    (fn [[equipment-kw num]]
      (str (:name (equipment-map equipment-kw)) " (" num ")"))
    equipment)])

(defn traits-section [traits]
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
     traits)]))

(defn character-display [built-char mobile? tablet? desktop?]
  (let [race (es/entity-val built-char :race)
        subrace (es/entity-val built-char :subrace)
        levels (es/entity-val built-char :levels)
        darkvision (es/entity-val built-char :darkvision)
        skill-profs (es/entity-val built-char :skill-profs)
        tool-profs (es/entity-val built-char :tool-profs)
        weapon-profs (es/entity-val built-char :weapon-profs)
        armor-profs (es/entity-val built-char :armor-profs)
        resistances (es/entity-val built-char :resistances)
        immunities (es/entity-val built-char :immunities)
        languages (es/entity-val built-char :languages)
        ability-bonuses (es/entity-val built-char :ability-bonuses)
        armor-class (es/entity-val built-char :armor-class)
        armor-class-with-armor (es/entity-val built-char :armor-class-with-armor)
        armor (es/entity-val built-char :armor)
        spells-known (es/entity-val built-char :spells-known)
        weapons (es/entity-val built-char :weapons)
        equipment (es/entity-val built-char :equipment)
        traits (es/entity-val built-char :traits)]
    [:div (if desktop?
            {:style {:width "500px"}}
            {:style {:margin-left "20px" :margin-right "20px"}})
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
      [:div {:style {:width (cond mobile? "60%" tablet? "40%" :else "50%")}}
       [:img.character-image {:src (or (get-in @character-ref [::entity/values :image-url]) "image/barbarian-girl.png")
              :style {:width "100%"
                      :margin-bottom "20px"}}]]
      [:div (if desktop? {:style {:width "250px"}})
       [armor-class-section armor-class armor-class-with-armor armor]
       [display-section "Hit Points" "fa-crosshairs" (es/entity-val built-char :max-hit-points)]
       [display-section "Speed" nil (es/entity-val built-char :speed)]
       [display-section "Darkvision" "fa-low-vision" (if darkvision (str darkvision " ft.") "--")]
       [display-section "Initiative" nil (mod/bonus-str (es/entity-val built-char :initiative))]
       [display-section "Proficiency Bonus" nil (mod/bonus-str (es/entity-val built-char :prof-bonus))]
       [display-section "Passive Perception" nil (es/entity-val built-char :passive-perception)]
       (let [criticals (es/entity-val built-char :critical)
             min-crit (apply min criticals)
             max-crit (apply max criticals)]
         (if (not= min-crit max-crit)
           (display-section "Critical Hit" nil (str min-crit "-" max-crit))))
       [list-display-section "Save Proficiencies" nil (map (comp s/upper-case name) (es/entity-val built-char :saving-throws))]]]
     [:div {:style {:width "100%"}}
      [abilities-radar 187 (es/entity-val built-char :abilities) ability-bonuses]]
     [list-display-section "Skill Proficiencies" nil
      (let [skill-bonuses (es/entity-val built-char :skill-bonuses)]
        (map
         (fn [[skill-kw bonus]]
           (str (s/capitalize (name skill-kw)) " " (mod/bonus-str bonus)))
         (filter (fn [[k bonus]]
                   (not= bonus (ability-bonuses (:ability (opt5e/skills-map k)))))
                 skill-bonuses)))]
     [list-item-section "Languages" languages]
     [list-item-section "Tool Proficiencies" tool-profs]
     [list-item-section "Weapon Proficiencies" weapon-profs]
     [list-item-section "Armor Proficiencies"]
     [list-item-section "Resistances" resistances name]
     [list-item-section "Immunities" immunities name]
     [spells-known-section spells-known]
     [equipment-section "Weapons" weapons opt5e/weapons-map]
     [equipment-section "Armor" armor opt5e/armor-map]
     [equipment-section "Equipment" equipment opt5e/equipment-map]
     [traits-section traits]]))

(def tab-path [:builder :character :tab])

(defn character-builder []
  ;;(cljs.pprint/pprint @character-ref)
  ;;(cljs.pprint/pprint @app-state)
  (let [merged-template (update template
                                ::t/selections
                                (fn [s]
                                  (entity/merge-multiple-selections
                                   s
                                   (t5e/sword-coast-adventurers-guide-selections @character-ref)
                                   (t5e/volos-guide-to-monsters-selections @character-ref))))
        option-paths (make-path-map @character-ref)
        built-template (entity/build-template @character-ref merged-template)
        built-char (entity/build @character-ref built-template)
        active-tab (get-in @app-state tab-path)
        view-width (.-width (gdom/getViewportSize js/window))
        mobile? (device/isMobile)
        tablet? (device/isTablet)
        desktop? (device/isDesktop)]
    ;;(js/console.log "BUILT TEMPLAT" built-template)
    ;;(print-char built-char)
    [:div.app
     {:class-name (cond mobile? "mobile" tablet? "tablet" :else nil)}
     [:div.app-header
      [:div.app-header-bar.container
       [:div.content
        [:img.orcpub-logo {:src "image/orcpub-logo.svg"}]]]]
     #_[:div.container
        [:div {:style tabs-style}
         [:span {:style selected-tab-style} "Character"]
         [:span {:style tab-style} "Monster"]]]
     [:div
      {:style container-style}
      [:div
       {:style character-builder-style}
       [:h1 {:style page-header-style} "Character Builder"]
       (if (not desktop?)
         [:div
          [:div.builder-tabs
           (if mobile? [:span.builder-tab {:class-name (if (= active-tab 0) "selected-builder-tab")
                               :on-click (fn [_] (swap! app-state assoc-in tab-path 0))} "Options"])
           (if tablet? [:span.builder-tab {:class-name (if (= active-tab 0) "selected-builder-tab")
                               :on-click (fn [_] (swap! app-state assoc-in tab-path 0))} "Build"])
           (if mobile? [:span.builder-tab {:class-name (if (= active-tab 1) "selected-builder-tab")
                               :on-click (fn [_] (swap! app-state assoc-in tab-path 1))} "Personality"])
           [:span.builder-tab {:class-name (if (or (and mobile? (= active-tab 2))
                                  (and tablet? (= active-tab 1))) "selected-builder-tab")
                   :on-click (fn [_] (swap! app-state assoc-in tab-path (if mobile? 2 1)))} "Details"]]])
       [:div
        {:style {:display :flex}}
        (if (or desktop?
                (= 0 active-tab))
          [:div {:style (if mobile? {:width "100%"} {:width "300px"})}
           (doall
            (map
             (fn [selection]
               ^{:key (::t/key selection)}
               [builder-selector [] option-paths selection built-char @character-ref built-template])
             (::t/selections built-template)))])
        (if (or desktop?
                (and tablet? (= 0 active-tab))
                (and mobile? (= 1 active-tab)))
          [:div {:style {:flex-grow 1
                         :margin-top "10px"
                         :margin-left (if desktop? "30px" "5px")
                         :margin-right (if desktop? "80px" "5px")}}
           [:div {:style {:margin-top "5px"}}
            [:span.personality-label {:style {:font-size "18px"}} "Character Name"]
            [:input.input {:type :text}]]
           [:div.field
            [:span.personality-label {:style {:font-size "18px"}} "Personality Trait 1"]
            [:input.input {:type :text}]]
           [:div.field
            [:span.personality-label {:style {:font-size "18px"}} "Personality Trait 2"]
            [:input.input {:type :text}]]
           [:div.field
            [:span.personality-label {:style {:font-size "18px"}} "Ideals"]
            [:input.input {:type :text}]]
           [:div.field
            [:span.personality-label {:style {:font-size "18px"}} "Bonds"]
            [:input.input {:type :text}]]
           [:div.field
            [:span.personality-label {:style {:font-size "18px"}} "Flaws"]
            [:input.input {:type :text}]]
           [:div.field
            [:span.personality-label {:style {:font-size "18px"}} "Image URL"]
            [:input.input
             {:type :text
              :on-change (fn [e] (swap! character-ref assoc-in [::entity/values :image-url] (.-value (.-target e))))}]]
           [:div.field
            [:span.personality-label {:style {:font-size "18px"}} "Description/Backstory"]
            [:textarea.input {:style {:height "800px"}}]]])
        (if (or desktop?
                (and mobile? (= 2 active-tab))
                (and tablet? (= 1 active-tab)))
          [character-display built-char mobile? tablet? desktop?])]]]]))


(r/render [character-builder]
          (js/document.getElementById "app"))

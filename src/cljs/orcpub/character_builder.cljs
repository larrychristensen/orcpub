(ns orcpub.character-builder
  (:require [goog.dom :as gdom]
            [goog.labs.userAgent.device :as device]
            [cljs.pprint :as pprint]
            [cljs.reader :as reader]
            [clojure.string :as s]
            [clojure.set :as sets]

            [orcpub.common :as common]
            [orcpub.constants :as const]
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
            [orcpub.dnd.e5.weapons :as weapon5e]
            [orcpub.dnd.e5.armor :as armor5e]
            [orcpub.dnd.e5.magic-items :as mi5e]
            [orcpub.dnd.e5.display :as disp5e]
            [orcpub.pdf-spec :as pdf-spec]

            [clojure.spec :as spec]
            [clojure.spec.test :as stest]

            [reagent.core :as r]))

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

(def stored-char-str (.getItem js/window.localStorage "char-meta"))
(defn remove-stored-char [stored-char-str & [more-info]]
  (js/console.warn (str "Invalid char-meta: " stored-char-str more-info))
  (.removeItem js/window.localStorage "char-meta"))
(def stored-char (if stored-char-str (try (let [v (reader/read-string stored-char-str)]
                                            (if (spec/valid? ::entity/raw-entity v)
                                              v
                                              (remove-stored-char stored-char-str (str (spec/explain-data ::entity/raw-entity v)))))
                                          (catch js/Object
                                              e
                                            (remove-stored-char stored-char-str)))))

(def text-color
  {:color :white})

(def field-font-size
  {:font-size "14px"})

(defonce app-state
  (r/atom
   {:collapsed-paths #{[:ability-scores]
                       [:background]
                       [:race]
                       [:sources]
                       [:class :barbarian]}
    :stepper-selection-path nil
    :mouseover-option nil
    :builder {:character {:tab #{:build :options}}}
    :character (if stored-char stored-char t5e/character)}))

(def template (entity/sort-selections
               (t5e/template app-state)))

#_(add-watch app-state :log (fn [k r os ns]
                            (js/console.log "OLD" (clj->js os))
                              (js/console.log "NEW" (clj->js ns))))

(add-watch app-state
           :local-storage
           (fn [k r os ns]
             (.setItem js/window.localStorage "char-meta" (str (:character ns)))))

(declare builder-selector)

(defn dropdown-option [option]
  [:option.builder-dropdown-item
   {:value (str (::t/key option))}
   (::t/name option)])

(defn hide-mouseover-option! []
  (let [mouseover-option (js/document.getElementById "mouseover-option")]
    (if mouseover-option (set! (.-display (.-style mouseover-option)) "none"))))

(defn show-mouseover-option! []
  (let [mouseover-option (js/document.getElementById "mouseover-option")]
    (if mouseover-option (set! (.-display (.-style mouseover-option)) "block"))))

(defn set-mouseover-option! [opt]
  (show-mouseover-option!)
  (let [title-el (js/document.getElementById "mouseover-option-title")]
    (if title-el
      (do (set! (.-innerHTML title-el) (::t/name opt))
          (set! (.-innerHTML (js/document.getElementById "mouseover-option-help")) (or (::t/help opt) ""))))))

(defn dropdown [options selected-value change-fn built-char]
  [:select.builder-option.builder-option-dropdown
   {:on-change change-fn
    :value (or (str selected-value) "")
    :on-mouse-over (fn [_]
                     (if selected-value
                       (set-mouseover-option!
                        (first
                         (filter
                          #(= selected-value (::t/key %))
                          options)))))}
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

(defn make-dropdown-change-fn [path key template raw-char app-state i]
  (fn [e]
    (let [new-path (concat path [key i])
          option-path (entity/get-entity-path template raw-char new-path)
          new-value (reader/read-string (.. e -target -value))]
      (swap! app-state #(update % :character (fn [c] (set-option-value c (conj option-path ::entity/key) new-value)))))))

(defn make-quantity-change-fn [path key template raw-char app-state i]
  (fn [e]
    (let [new-path (concat path [key i])
          option-path (entity/get-entity-path template raw-char new-path)
          raw-value (.. e -target -value)
          new-value (if (not (s/blank? raw-value)) (js/parseInt raw-value) 1)]
      (swap! app-state #(update % :character (fn [c] (set-option-value c (conj option-path ::entity/value) new-value)))))))

(defn to-option-path
  ([template-path template]
   (to-option-path template-path template []))
  ([template-path template current-option-path]
   (let [path-len (count template-path)
         key (::t/key (get-in template template-path))
         next-option-path (if key (conj current-option-path key) current-option-path)]
     (if (and key (> path-len 2))
       (recur (subvec template-path 0 (- path-len 2))
              template
              next-option-path)
       (vec (reverse next-option-path))))))

(defn option [path option-paths selectable? list-collapsed? {:keys [::t/key ::t/name ::t/selections ::t/modifiers ::t/prereqs ::t/ui-fn ::t/select-fn] :as opt} built-char raw-char changeable? options change-fn built-template collapsed-paths stepper-selection-path]
  (let [new-path (conj path key)
        selected? (boolean (get-in option-paths new-path))
        collapsed? (get collapsed-paths new-path)
        named-mods (filter ::mod/name modifiers)
        failed-prereqs (reduce
                        (fn [failures {:keys [::t/prereq-fn ::t/label]}]
                          (if (and prereq-fn (not (prereq-fn built-char)))
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
                        (swap! app-state #(update % :character (fn [c] (update-option built-template c path (fn [o] (assoc o ::entity/key key))))))))
                    (.stopPropagation e))
        :on-mouse-enter (fn [e]
                         (let [stepper-selection-path stepper-selection-path
                               selection-path (to-option-path stepper-selection-path built-template)]
                           (set-mouseover-option! opt))
                        (.stopPropagation e))}
       [:div.option-header
        [:div.flex-grow-1
         (if changeable?
           [dropdown options key change-fn built-char]
           [:span.f-w-b name])
         (if (not meets-prereqs?)
           [:div.i.f-s-12.f-w-n 
            (str "Requires " (s/join ", " failed-prereqs))])
         (if (and meets-prereqs? (seq named-mods))
           [:span.m-l-10.i.f-s-12.f-w-n
            (s/join
             ", "
             (map
              (fn [{:keys [::mod/value ::mod/val-fn] :as m}]
                (let []
                  (str
                   (::mod/name m)
                   " "
                   (let [v (or value (get-option-value built-template (:character @app-state) path))]
                     (if val-fn
                       (val-fn v)
                       v)))))
              named-mods))])]
        (if (and selected?
                 (or (seq selections)
                     ui-fn))
          (if collapsed?
            [:div.flex
             {:on-click (fn [_]
                          (swap! app-state update :collapsed-paths disj new-path))}
             [:span.expand-collapse-button
              "Expand"]
             [:i.fa.fa-caret-down.m-l-5.orange.pointer]]
            [:div.flex
             {:on-click (fn [_]
                          (swap! app-state update :collapsed-paths conj new-path))}
             [:span.expand-collapse-button
              "Collapse"]
             [:i.fa.fa-caret-up.m-l-5.orange.pointer]]))]
       (if (and selected? (not collapsed?))
         [:div
          (if ui-fn (ui-fn path))
          [:div
           (doall
            (map
             (fn [{:keys [::t/prereq-fn ::t/key] :as selection}]
               (if (or (not prereq-fn) (prereq-fn built-char))
                 ^{:key key}
                 [builder-selector new-path option-paths selection built-char raw-char built-template collapsed-paths stepper-selection-path]))
             selections))]]
         (if (and (seq selections) collapsed?)
           [:div.builder-option.collapsed-list-builder-option]))])))

(def builder-selector-style)

(defn add-option-button [{:keys [::t/key ::t/name ::t/options ::t/new-item-text] :as selection} entity path new-item-fn built-template]
  [:div.add-item-button
   [:i.fa.fa-plus-circle.orange]
   [:span.m-l-5
    {:on-click
     (fn []
       (let [value-path (entity/get-entity-path built-template entity path)
             new-item (new-item-fn
                       selection
                       options
                       (get-in @app-state (concat [:character] value-path)))]
         (swap! app-state #(update % :character (fn [c] (update-option built-template c path
                                                                      (fn [options] (conj (vec options) new-item))))))))}
    (or new-item-text (str "Add " name))]])

(defn remove-option-button [path built-template index]
  [:i.fa.fa-minus-circle.remove-item-button.orange
   {:on-click
    (fn [e]
      (swap! app-state #(update % :character (remove-list-option built-template % path index))))}])

(defn dropdown-selector [path option-paths {:keys [::t/options ::t/min ::t/max ::t/key ::t/name ::t/sequential? ::t/new-item-fn ::t/quantity?] :as selection} built-char raw-char built-template collapsed?]
  (if (not collapsed?)
    (let [change-fn (partial make-dropdown-change-fn path key built-template raw-char app-state)
          qty-change-fn (partial make-quantity-change-fn path key built-template raw-char app-state)
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
                    value (get-in @app-state (concat [:character] key-path))]
                ^{:key i} [:div [dropdown options value (change-fn i) built-char]]))))
         [:div
          (let [full-path (conj path key)
                entity-path (entity/get-entity-path built-template raw-char full-path)
                selected (get-in @app-state (concat [:character] entity-path))
                remaining (- min (count selected))
                final-options (if (pos? remaining)
                                (vec (concat selected (repeat remaining {::entity/key nil})))
                                selected)]
            (doall
             (map-indexed
              (fn [i {value ::entity/key
                      qty-value ::entity/value
                      :as option}]
                ^{:key i}
                [:div.flex.align-items-c
                 [dropdown options value (change-fn i) built-char]
                 (if quantity?
                   [:input.input.m-l-5.w-70
                    {:type :number
                     :placeholder "QTY"
                     :value qty-value
                     :on-change (qty-change-fn i)}])
                 [remove-option-button full-path built-template i]])
              final-options)))
          (add-option-button selection raw-char (conj path key) new-item-fn built-template)])])
    [:div
     [:div.builder-option.collapsed-list-builder-option]
     [:div.builder-option.collapsed-list-builder-option]]))

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

(defn list-selector-option [removeable? path option-paths multiple-select? list-collapsed? i opt built-char raw-char changeable? options change-fn built-template collapsed-paths stepper-selection-path]
  [:div.list-selector-option
   [:div.flex-grow-1
    [option path option-paths (not multiple-select?) list-collapsed? opt built-char raw-char changeable? options change-fn built-template collapsed-paths stepper-selection-path]]
   (if (removeable? i)
     [remove-option-button path built-template i])])

(defn list-selector [path option-paths {:keys [::t/options ::t/min ::t/max ::t/key ::t/name ::t/sequential? ::t/new-item-fn] :as selection} collapsed? built-char raw-char built-template collapsed-paths stepper-selection-path]
  (let [no-max? (nil? max)
        multiple-select? (or no-max? (> max 1))
        selected-options (filter-selected path key option-paths options raw-char built-template)
        addable? (and multiple-select?
                      (or no-max?
                          (< (count selected-options) max)))
        more-than-min? (> (count selected-options) min)
        next-path (conj path key)]
    (assert (or (not multiple-select?)
                new-item-fn)
            (str "MULTIPLE SELECT LIST SELECTOR REQUIRES UI-FN! Offending selection: " next-path))
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
           (make-dropdown-change-fn path key built-template raw-char app-state i)
           built-template
           collapsed-paths
           stepper-selection-path])
        (if multiple-select?
          selected-options
          options)))]
     (if collapsed? [:div.builder-option.collapsed-list-builder-option])
     (if (and addable? new-item-fn)
       (add-option-button selection raw-char (conj path key) new-item-fn built-template))]))

(defn selector-id [path]
  (s/join "--" (map name path)))

(defn builder-selector [path option-paths {:keys [::t/name ::t/key ::t/min ::t/max ::t/ui-fn ::t/collapsible? ::t/options] :as selection} built-char raw-char built-template collapsed-paths stepper-selection-path]
  (let [new-path (conj path key)
        collapsed? (get collapsed-paths new-path)
        simple-options? 
        (or (::t/simple? selection)
            (not-any? #(or (seq (::t/selections %))
                           (some ::mod/name (::t/modifiers %))
                           (::t/ui-fn %))
                      options))
        collapsible? (or collapsible?
                         (and (not (or (nil? max) (> max min)))
                              (not simple-options?)))]
    ^{:key key}
    [:div.builder-selector
     {:id (selector-id new-path)}
     [:div.flex.justify-cont-s-b.align-items-c
      (if (zero? (count path))
        [:h1.f-s-24 (::t/name selection)]
        [:h2.builder-selector-header (::t/name selection)])
      (if collapsible?
        (if collapsed?
          [:div.flex
           {:on-click (fn [_]
                        (swap! app-state update :collapsed-paths disj new-path))}
           [:div.expand-collapse-button
            (if simple-options? "Expand" "Show All Options")]
           [:i.fa.fa-caret-down.m-l-5.orange.pointer]]
          [:div.flex
           {:on-click (fn [_]
                        (swap! app-state update :collapsed-paths conj new-path))}
           [:span.expand-collapse-button
            (if simple-options? "Collapse" "Hide Unselected Options")]
           [:i.fa.fa-caret-up.m-l-5.orange.pointer]]))]
     [:div
      (cond
        ui-fn (ui-fn selection)
        simple-options? [dropdown-selector path option-paths selection built-char raw-char built-template (and collapsible? collapsed?)]
        :else [list-selector path option-paths selection (and collapsible? collapsed?) built-char raw-char built-template collapsed-paths stepper-selection-path])]]))

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
        offset-ability-keys (take 6 (drop 1 (cycle char5e/ability-keys)))
        text-points [[0 55] [106 0] [206 55] [206 190] [106 240] [0 190]]
        abilities-points (map
                          (fn [k [x y]]
                            (let [ratio (double (/ (k abilities) 20))]
                              [(* x ratio) (* y ratio)]))
                          offset-ability-keys
                          points)
        color-names (map-indexed
                     (fn [i c]
                       {:key i
                        :color c})
                     ["f4692a" "f32e50" "b35c95" "47eaf8" "bbe289" "f9b747"])
        colors (map
                (fn [{:keys [key color]}]
                  {:key key
                   :color (str "#" color)})
                color-names)]
    [:div.posn-rel.w-100-p
     (map
      (fn [k [x y] {:keys [color]}]
        (let [color-class (str "c-" color)]
          ^{:key color}
          [:div.t-a-c.w-50.posn-abs {:style {:left x :top y}}
           [:div
            [:span (s/upper-case (name k))]
            [:span.m-l-5 {:class-name color-class} (k abilities)]]
           [:div {:class-name color-class}
            (let [bonus (int (/ (- (k abilities) 10) 2))]
              (str "(" (mod/bonus-str (k ability-bonuses)) ")"))]]))
      offset-ability-keys
      (take 6 (drop 1 (cycle text-points)))
      color-names)
     [:svg {:width 220 :height (+ 60 d double-point-offset)}
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
       (doall
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
         (drop 1 (cycle colors))))]]]))

;;(stest/instrument `entity/build)
;;(stest/instrument `t/make-modifier-map)

;;(prn "MODIFIER MAP" (cljs.pprint/pprint (t/make-modifier-map template)))
;;(spec/explain ::t/modifier-map (t/make-modifier-map template))

(defn realize-char [built-char]
  (reduce-kv
   (fn [m k v]
     (let [realized-value (es/entity-val built-char k)]
       (if (fn? realized-value)
         m
         (assoc m k realized-value))))
   (sorted-map)
   built-char))

(defn print-char [built-char]
  (cljs.pprint/pprint
   (realize-char built-char)))

(defn display-section [title icon-cls value & [list?]]
  [:div.m-t-20
   [:span.f-s-16.f-w-600 title]
   [:div {:class-name (if list? "m-t-0" "m-t-4")}
    (if icon-cls [:i.fa.m-r-18.white {:class-name icon-cls}])
    [:span.f-s-24.f-w-600
     value]]])

(defn list-display-section [title icon-cls values]
  (if (seq values)
    (display-section title icon-cls
                     [:span.m-t-5.f-s-14.f-w-n.i
                      (s/join
                       ", "
                       values)]
                     true)))

(defn armor-class-section [armor-class armor-class-with-armor equipped-armor]
  [display-section
   "Armor Class"
   "fa-shield f-s-32"
   [:span
    [:span
     [:span armor-class]
     [:span.display-section-qualifier-text "(unarmored)"]]
    (let [has-shield? (:shield equipped-armor)]
      [:div.m-l-40
       (if has-shield?
         [:span
          [:span (armor-class-with-armor nil has-shield?)]
          [:span.display-section-qualifier-text "(unarmored + shield)"]])
       (doall
        (map
         (fn [[armor-kw _]]
           (let [armor ((merge armor5e/armor-map
                               mi5e/magic-armor-map) armor-kw)
                 ac (armor-class-with-armor armor)]
             ^{:key armor-kw}
             [:div
              [:div
               [:span ac]
               [:span.display-section-qualifier-text (str "(" (:name armor) ")")]]
              (if has-shield?
                [:div
                 [:span (+ 2 ac)]
                 [:span.display-section-qualifier-text (str "(" (:name armor) " + shield)")]])]))
         (dissoc equipped-armor :shield)))])]])

(defn speed-section [built-char]
  (let [speed (es/entity-val built-char :speed)
        speed-with-armor (es/entity-val built-char :speed-with-armor)
        unarmored-speed-bonus (es/entity-val built-char :unarmored-speed-bonus)
        equipped-armor (es/entity-val built-char :armor)]
    [display-section
     "Speed"
     "fa-tachometer f-s-24"
     [:span
      [:span
       [:span (+ (or unarmored-speed-bonus 0)
                 (if speed-with-armor
                   (speed-with-armor nil)
                   speed))]
       (if (or unarmored-speed-bonus
               speed-with-armor)
         [:span.display-section-qualifier-text "(unarmored)"])]
      (if speed-with-armor
        [:div.m-l-40
         (doall
          (map
           (fn [[armor-kw _]]
             (let [armor ((merge armor5e/armor-map
                                 mi5e/magic-armor-map) armor-kw)
                   speed (speed-with-armor armor)]
               ^{:key armor-kw}
               [:div
                [:div
                 [:span speed]
                 [:span.display-section-qualifier-text (str "(" (:name armor) " armor)")]]]))
           (dissoc equipped-armor :shield)))]
        (if unarmored-speed-bonus
          [:div.m-l-40
           [:span
            [:span speed]
            [:span.display-section-qualifier-text "(armored)"]]]))]
     (let [swim-speed (es/entity-val built-char :swimming-speed)]
       (if swim-speed
         [:div [:span swim-speed] [:span.display-section-qualifier-text "(swim)"]]))]))

(defn list-item-section [list-name items & [name-fn]]
  [list-display-section list-name nil
   (map
    (fn [item]
      ((or name-fn :name) item))
    items)])

(defn compare-spell [spell-1 spell-2]
  (let [key-fn (juxt :key :ability)]
    (compare (key-fn spell-1) (key-fn spell-2))))

(defn spells-known-section [spells-known]
  [display-section "Spells Known" nil
   [:div.f-s-14
    (map
     (fn [[level spells]]
       ^{:key level}
       [:div.m-t-10
        [:span.f-w-600 (if (zero? level) "Cantrip" (str "Level " level))]
        [:div.i.f-w-n
         (map-indexed
          (fn [i spell]
            (let [spell-data (spells/spell-map (:key spell))]
              ^{:key i}
              [:div
               (str
                (:name (spells/spell-map (:key spell)))
                " ("
                (s/join
                 ", "
                 (remove
                  nil?
                  [(if (:ability spell) (s/upper-case (name (:ability spell))))
                   (if (:qualifier spell) (:qualifier spell))]))
                
                ")")]))
          (into
           (sorted-set-by compare-spell)
           (filter
            (fn [{k :key}]
              (spells/spell-map k))
            spells)))]])
     spells-known)]])

(defn equipment-section [title equipment equipment-map]
  [list-display-section title nil
   (map
    (fn [[equipment-kw num]]
      (str (:name (equipment-map equipment-kw)) " (" num ")"))
    equipment)])

(defn attacks-section [attacks]
  (if (seq attacks)
    (display-section
     "Attacks" nil
     [:div.f-s-14
      (map
       (fn [{:keys [name area-type description damage-die damage-die-count damage-type save save-dc] :as attack}]
         ^{:key name}
         [:p.m-t-10
          [:span.f-w-600.i name "."]
          [:span.f-w-n.m-l-10 (common/sentensize (disp5e/attack-description attack))]])
       attacks)])))

(defn actions-section [title actions]
  (if (seq actions)
    (display-section
     title nil
     [:div.f-s-14
      (map
       (fn [action]
         ^{:key action}
         [:p.m-t-10
          [:span.f-w-600.i (:name action) "."]
          [:span.f-w-n.m-l-10 (common/sentensize (disp5e/action-description action))]])
       actions)])))

(defn prof-name [prof-map prof-kw]
  (or (-> prof-kw prof-map :name) (common/safe-name prof-kw)))

(defn character-display [built-char]
  (let [race (es/entity-val built-char :race)
        subrace (es/entity-val built-char :subrace)
        levels (char5e/levels built-char)
        darkvision (es/entity-val built-char :darkvision)
        skill-profs (es/entity-val built-char :skill-profs)
        tool-profs (es/entity-val built-char :tool-profs)
        weapon-profs (es/entity-val built-char :weapon-profs)
        armor-profs (es/entity-val built-char :armor-profs)
        resistances (es/entity-val built-char :damage-resistances)
        immunities (es/entity-val built-char :damage-immunities)
        condition-immunities (es/entity-val built-char :condition-immunities)
        languages (es/entity-val built-char :languages)
        ability-bonuses (es/entity-val built-char :ability-bonuses)
        armor-class (es/entity-val built-char :armor-class)
        armor-class-with-armor (es/entity-val built-char :armor-class-with-armor)
        armor (es/entity-val built-char :armor)
        magic-armor (es/entity-val built-char :magic-armor)
        spells-known (es/entity-val built-char :spells-known)
        weapons (es/entity-val built-char :weapons)
        magic-weapons (es/entity-val built-char :magic-weapons)
        equipment (es/entity-val built-char :equipment)
        magic-items (es/entity-val built-char :magic-items)
        traits (es/entity-val built-char :traits)
        attacks (es/entity-val built-char :attacks)
        bonus-actions (es/entity-val built-char :bonus-actions)
        reactions (es/entity-val built-char :reactions)
        actions (es/entity-val built-char :actions)]
    [:div
     [:div.f-s-24.f-w-600.m-b-16.text-shadow
      [:span race]
      (if (seq levels)
        [:span.m-l-10
         (apply
          str
          (interpose
           " / "
           (map
            (fn [[cls-k {:keys [class-name class-level subclass]}]]
              (str class-name " (" class-level ")"))
            levels)))])]
     [:div.details-columns
      [:div.flex-grow-1.flex-basis-50-p
       [:div.flex
        [:div.w-50-p
         [:img.character-image.w-100-p.m-b-20 {:src (or (get-in @app-state [:character ::entity/values :image-url]) "image/barbarian-girl.png")}]]
        [:div.w-50-p
         [armor-class-section armor-class armor-class-with-armor armor]
         [display-section "Hit Points" "fa-heart-o f-s-24" (es/entity-val built-char :max-hit-points)]
         [speed-section built-char]
         #_[display-section "Speed" nil
          (let [unarmored-speed-bonus (es/entity-val built-char :unarmored-speed-bonus)
                speed (es/entity-val built-char :speed)
                swim-speed (es/entity-val built-char :swimming-speed)
                speed-with-armor (es/entity-val built-char :speed-with-armor)]
            [:div
             [:div
              (if speed-with-armor
                (speed-section speed-with-armor armor)
                speed)]
             (if swim-speed
               [:div [:span swim-speed] [:span.display-section-qualifier-text "(swim)"]])])]
         [display-section "Darkvision" "fa-low-vision f-s-24" (if darkvision (str darkvision " ft.") "--")]
         [display-section "Initiative" nil (mod/bonus-str (es/entity-val built-char :initiative))]
         [display-section "Proficiency Bonus" nil (mod/bonus-str (es/entity-val built-char :prof-bonus))]
         [display-section "Passive Perception" nil (es/entity-val built-char :passive-perception)]
         (let [num-attacks (es/entity-val built-char :num-attacks)]
           (if (> num-attacks 1)
             [display-section "Number of Attacks" nil num-attacks]))
         (let [criticals (es/entity-val built-char :critical)
               min-crit (apply min criticals)
               max-crit (apply max criticals)]
           (if (not= min-crit max-crit)
             (display-section "Critical Hit" nil (str min-crit "-" max-crit))))
         [:div
          [list-display-section
           "Save Proficiencies" nil
           (map (comp s/upper-case name) (es/entity-val built-char :saving-throws))]
          (let [save-advantage (es/entity-val built-char :saving-throw-advantage)]
            [:ul.list-style-disc.m-t-5
             (doall
              (map-indexed
               (fn [i {:keys [abilities types]}]
                 ^{:key i}
                 [:li (str "advantage on "
                           (common/list-print (map (comp s/lower-case :name opt5e/abilities-map) abilities))
                           " saves against "
                           (common/list-print
                            (map #(let [condition (opt5e/conditions-map %)]
                                    (cond
                                      condition (str "being " (s/lower-case (:name condition)))
                                      (keyword? %) (name %)
                                      :else %))
                                 types)))])
               save-advantage))])]]]
       [:div.w-100-p
        [abilities-radar 187 (es/entity-val built-char :abilities) ability-bonuses]]]
      [:div.flex-grow-1.flex-basis-50-p
       [list-display-section "Skill Proficiencies" nil
        (let [skill-bonuses (es/entity-val built-char :skill-bonuses)]
          (map
           (fn [[skill-kw bonus]]
             (str (s/capitalize (name skill-kw)) " " (mod/bonus-str bonus)))
           (filter (fn [[k bonus]]
                     (not= bonus (ability-bonuses (:ability (opt5e/skills-map k)))))
                   skill-bonuses)))]
       [list-item-section "Languages" languages (partial prof-name opt5e/language-map)]
       [list-item-section "Tool Proficiencies" tool-profs (partial prof-name opt5e/tools-map)]
       [list-item-section "Weapon Proficiencies" weapon-profs (partial prof-name weapon5e/weapons-map)]
       [list-item-section "Armor Proficiencies" armor-profs (partial prof-name armor5e/armor-map)]
       [list-item-section "Damage Resistances" resistances name]
       [list-item-section "Damage Immunities" immunities name]
       [list-item-section "Condition Immunities" condition-immunities (fn [{:keys [condition qualifier]}]
                                                                        (str (name condition)
                                                                             (if qualifier (str " (" qualifier ")"))))]
       [spells-known-section spells-known]
       [equipment-section "Weapons" (concat magic-weapons weapons) (merge weapon5e/weapons-map mi5e/magic-weapon-map)]
       [equipment-section "Armor" (concat magic-armor armor) (merge armor5e/armor-map mi5e/magic-armor-map)]
       [equipment-section "Equipment" (concat magic-items equipment) (merge opt5e/equipment-map mi5e/magic-item-map)]
       [attacks-section attacks]
       [actions-section "Bonus Actions" bonus-actions]
       [actions-section "Reactions" reactions]
       [actions-section "Actions" actions]
       [actions-section "Features, Traits, and Feats" traits]]]]))


(def tab-path [:builder :character :tab])

(def plugins
  [{:name "Sword Coast Adventurer's Guide"
    :key :sword-coast-adventurers-guide
    :selections (t5e/sword-coast-adventurers-guide-selections (:character @app-state))}
   {:name "Volo's Guide to Monsters"
    :key :volos-guide-to-monsters
    :selections (t5e/volos-guide-to-monsters-selections (:character @app-state))}])

(def plugins-map
  (zipmap (map :key plugins) plugins))

(def option-sources-selection
  {::t/name "Option Sources"
   ::t/optional? true
   ::t/help "Select the sources you want to use for races, classes, etc. Click the 'Show All Options' button to make additional selections. If you are new to the game we recommend just moving on to the next step."})

(defn get-all-selections-aux [path {:keys [::t/key ::t/selections ::t/options] :as obj} selected-option-paths]
  (let [children (map
                  (fn [{:keys [::t/key] :as s}]
                    (get-all-selections-aux (conj path key) s selected-option-paths))
                  (or selections options))]
    (cond
      selections
      (if (get-in selected-option-paths path) children)
    
      options
      (if key
        (concat
         [(assoc obj ::path path)]
         children)
        children))))

(defn get-all-selections [path obj selected-option-paths]
  (remove nil? (flatten (get-all-selections-aux path obj selected-option-paths))))

(defn selection-made? [built-template selected-option-paths character selection]
  (let [option (get-in selected-option-paths (::path selection))
        entity-path (entity/get-entity-path built-template character (::path selection))
        selections (get-in character entity-path)
        min-count (::t/min selection)]
    (and option (or (= min-count 1) (and (vector? selections) (>= (count (filter ::entity/key selections)) min-count))))))

(defn drop-selected [built-template selected-option-paths character selections]
  (remove
   (partial selection-made? built-template selected-option-paths character)
   selections))

(defn selection-after [current-path all-selections]
  (let [up-to-current (drop-while
                       (fn [s]
                         (prn "PATH" (::path s) current-path)
                         (not= (::path s) current-path))
                       all-selections)
        _ (js/console.log "UP TO CURRENT" up-to-current)
        up-to-next (drop 1 up-to-current)
        next (first up-to-next)]
    (if next
      [(::path next) next]
      (let [first-selection (first all-selections)]
        [(::path first-selection) first-selection]))))

(defn next-selection [current-template-path built-template selected-option-paths character]
  (let [current-path (to-option-path current-template-path built-template)
        all-selections (get-all-selections [] built-template selected-option-paths)]
    (selection-after current-path all-selections)))

(defn prev-selection [current-template-path built-template selected-option-paths character]
  (let [current-path (to-option-path current-template-path built-template)
        all-selections (get-all-selections [] built-template selected-option-paths)
        up-to-current (take-while
                       (fn [s]
                         (not= (::path s) current-path))
                       all-selections)
        prev (last up-to-current)]
    (if prev
      [(::path prev) prev]
      (let [last-selection (last all-selections)]
        [(::path last-selection) last-selection]))))

(defn collapse-paths [state paths]
  (let [all-paths (mapcat #(reductions conj [] %) paths)]
    (reduce
     (fn [s path]
       (update s :collapsed-paths conj path))
     state
     all-paths)))

(defn open-path-and-subpaths [state path]
  (reduce
   (fn [s subpath]
     (update s :collapsed-paths disj subpath))
   state
   (reductions conj [] path)))

(defn set-stepper-top! [top]
  (let [stepper-element (js/document.getElementById "selection-stepper")]
    (set! (.-top (.-style stepper-element)) (str top "px"))))

(defn to-selection-path [entity-path entity]
  (vec
   (remove
    nil?
    (map (fn [path]
           (let [last-key (last path)]
             (cond
               (= last-key ::entity/options) nil
               (number? last-key) (::entity/key (get-in entity path))
               :else last-key)))
         (reductions conj [] entity-path)))))

(defn set-next-template-path! [built-template next-path next-template-path character]
  (let [;;flat-options (entity/flatten-options (::entity/options character))       
        ;;root-paths (concat [[:ability-scores] [:race] [:background] [:weapons] [:armor] [:equipment]] (map ::t/path flat-options))
        ]
    (swap!
     app-state
     (fn [as]
       (-> as
           (assoc :stepper-selection-path next-template-path)
           ;;(collapse-paths root-paths)
           ;;(open-path-and-subpaths next-path)
           )))))

(defn set-next-selection! [built-template option-paths character stepper-selection-path all-selections]
  (let [[next-path {:keys [::t/name]}]
        (if (nil? stepper-selection-path)
          (let [s (second all-selections)]
            [(::path s) s])
          (next-selection
           stepper-selection-path
           built-template
           option-paths
           character))
        next-template-path (if next-path (entity/get-template-selection-path built-template next-path []))]
    #_(hide-mouseover-option!)
    #_(if (nil? next-path)
      (set-stepper-top! 0))
    (set-next-template-path! built-template next-path next-template-path character)))

(defn set-prev-selection! [built-template option-paths character stepper-selection-path all-selections]
  (let [[prev-path {:keys [::t/name]}]
        (if (nil? stepper-selection-path)
          (let [s (last all-selections)]
            [(::path s) s])
          (prev-selection
           stepper-selection-path
           built-template
           option-paths
           character))
        prev-template-path (if prev-path (entity/get-template-selection-path built-template prev-path []))]
    #_(hide-mouseover-option!)
    #_(if (nil? next-path)
      (set-stepper-top! 0))
    (set-next-template-path! built-template prev-path prev-template-path character)))

(defn level-up! [built-template character]
  (let [entity-path [::entity/options :class 0 ::entity/options :levels]
        lvls (get-in @app-state (concat [:character] entity-path))
        next-lvl (-> lvls count inc str keyword)
        selection-path (to-selection-path entity-path (:character @app-state))
        next-lvl-path (conj selection-path next-lvl)
        template-path (entity/get-template-selection-path built-template next-lvl-path [])
        option (get-in built-template template-path)
        first-selection (first (::t/selections option))
        selection-key (::t/key first-selection)
        next-path (conj next-lvl-path selection-key)
        next-template-path (conj template-path ::t/selections 0)]
    (swap!
     app-state
     update-in
     (concat [:character] entity-path)
     conj
     {::entity/key next-lvl})
    (set-next-template-path! built-template next-path next-template-path character)))

(defn selection-stepper [built-template option-paths character stepper-selection-path]
  (let [selection (if stepper-selection-path (get-in built-template stepper-selection-path))
        all-selections (get-all-selections [] built-template option-paths)
        unselected-selections (drop-selected built-template option-paths character all-selections)
        unselected-selections? (pos? (count unselected-selections))
        level-up? (not (or selection unselected-selections?))
        complete? (not unselected-selections?)]
    [:div.flex.selection-stepper-inner
     {:id "selection-stepper"}
     [:div.selection-stepper-main
      [:h1.f-w-bold.selection-stepper-title "Step-By-Step"]
      (if selection
        [:div
         [:h1.f-w-bold.m-t-10 "Step: " (::t/name selection)
          (if (::t/optional? selection)
            [:span.m-l-5.f-s-10 "(optional)"])]
         (let [help (::t/help selection)
               help-vec (if (vector? help) help [help])]
           (if (string? help)
             [:p.m-t-5.selection-stepper-help help]
             help))
         [:div#mouseover-option.b-1.b-rad-5.b-color-gray.p-10.m-t-10.hidden
          [:span#mouseover-option-title.f-w-b]
          [:p#mouseover-option-help]]])
      (if (or (not selection) level-up? complete?)
        [:div.m-t-10
         (if unselected-selections?
           "Click 'Get Started' to step through the build process."
           (str "All selections complete. Click "
                (if level-up?
                  "'Level Up' to your advance character level"
                  "'Finish' to complete")
                " or 'Dismiss' to hide this guide."))])
      [:div.flex.m-t-10.selection-stepper-footer
       [:span.link-button.m-r-5
        {:on-click (fn [_]
                     (swap! app-state assoc :stepper-dismissed true))}
        "Dismiss"]
       [:button.form-button.selection-stepper-button
        {:on-click
         (fn [_] (if level-up?
                   (level-up! built-template character)
                   (set-next-selection! built-template option-paths character stepper-selection-path unselected-selections)))}
        (cond
          level-up? "Level Up"
          complete? "Finish"
          selection "Next Step"
          unselected-selections? "Get Started")]]]
     (if selection
       [:svg.m-l--1.m-t-10 {:width "20" :height "24"}
        [:path 
         {:d "M-2 1.5 L13 14 L-2 22.5"
          :stroke :white
          :fill "#1a1e28"
          :stroke-width "1px"}]])]))

(defn option-sources [collapsed-paths selected-plugins]
  (let [path [:sources]
         collapsed? (collapsed-paths path)]
     [:div
      [:div.flex.just-cont-s-b.align-items-c
       [:h1.f-s-24 "Option Sources"]
       (if collapsed?
         [:div.flex
          {:on-click (fn [_]
                       (swap! app-state update :collapsed-paths disj path))}
          [:span.expand-collapse-button
           "Show All Options"]
          [:i.fa.fa-caret-down.m-l-5.orange.pointer]]
         [:div.flex
          {:on-click (fn [_]
                       (swap! app-state update :collapsed-paths conj path))}
          [:span.expand-collapse-button
           "Hide Unselected Options"]
          [:i.fa.fa-caret-up.m-l-5.orange.pointer]])]
      [:div.builder-option.selected-builder-option
       (if collapsed?
         [:span (s/join ", " (conj (map :name (filter #((:key %) selected-plugins) plugins)) "Player's Handbook"))]
         [:div
          [:div.checkbox-parent
           [:span.checkbox.checked.disabled
            [:i.fa.fa-check.orange]]
           [:span.checkbox-text "Player's Handbook"]]
          (doall
           (map
            (fn [{:keys [name key]}]
              (let [checked? (and selected-plugins (selected-plugins key))]
                ^{:key key}
                [:div.checkbox-parent
                 {:on-click (fn [_] (swap! app-state assoc-in [:plugins key] (not checked?)))}
                 [:span.checkbox
                  {:class-name (if checked? "checked")}
                  (if checked? [:i.fa.fa-check.orange])]
                 [:span.checkbox-text.pointer name]]))
            plugins))])]]))

(defn get-template-from-props [x]
  (get (.-argv (.-props x)) 6))


(defn on-builder-selector-update [x & args]
  (let [built-template ((vec (first args)) 6)
        stepper-selection-path (get @app-state :stepper-selection-path)
        selection (get-in built-template stepper-selection-path)
        selection-path (to-option-path stepper-selection-path built-template)
        selection-id (selector-id selection-path)
        element (js/document.getElementById selection-id)
        top (if element (.-offsetTop element) 0)
        stepper-element (js/document.getElementById "selection-stepper")
        options-top (.-offsetTop (js/document.getElementById "options-column"))]
    (if (pos? top) (set-stepper-top! (- top options-top)))))

(def builder-selector-component
  (with-meta
    builder-selector
    {:component-did-update on-builder-selector-update}))

(defn options-column [character built-char built-template option-paths collapsed-paths stepper-selection-path plugins]
  (prn "STEPPER SELECTION PATH" stepper-selection-path)
  (let [all-selections (get-all-selections [] built-template option-paths)
        _ (js/console.log "ALL SELECTIONS" all-selections)
        {:keys [::t/name ::t/options]} (if stepper-selection-path
                                         (get-in built-template stepper-selection-path)
                                         (first all-selections))
        option-path (to-option-path stepper-selection-path built-template)]
    (prn "OPTION_PATHS" option-path stepper-selection-path option-paths)
    [:div#options-column.w-100-p.b-1.b-rad-5
     [:div.flex.justify-cont-s-b.p-10.align-items-c
      [:button.form-button.p-5-10.m-r-5
       {:on-click
        (fn [_] (set-prev-selection!
                 built-template
                 option-paths
                 character
                 stepper-selection-path
                 all-selections))} "Back"]
      [:h3.f-w-b.f-s-18.t-a-c name]
      [:button.form-button.p-5-10.m-l-5
       {:on-click
        (fn [_] (set-next-selection!
                 built-template
                 option-paths
                 character
                 stepper-selection-path
                 all-selections))}
       "Next"]]
     [:div.p-5
      (doall
       (map
        (fn [{:keys [::t/key ::t/name ::t/path ::t/help ::t/selections ::t/prereqs ::t/select-fn]}]
          (let [new-option-path (conj option-path key)
                selected? (get-in option-paths new-option-path)
                failed-prereqs (reduce
                                (fn [failures {:keys [::t/prereq-fn ::t/label]}]
                                  (if (and prereq-fn (not (prereq-fn built-char)))
                                    (conj failures label)))
                                []
                                prereqs)
                meets-prereqs? (empty? failed-prereqs)
                selectable? meets-prereqs?]
            (prn "OPTIONPATH" new-option-path path selected?)
            ^{:key key}
            [:div.p-10.b-1.b-rad-5.m-5.pointer.b-orange.hover-shadow
             {:class-name (s/join " " (remove nil? [(if selected? "b-w-3") (if (not meets-prereqs?) "opacity-5")]))
              :on-click (fn [e]
                          (prn "NEW OPTION PATH" new-option-path)
                          (if (and meets-prereqs? selectable?)
                            (do
                              (if select-fn
                                (select-fn new-option-path))
                              (let [updated-char (update-option built-template character new-option-path (fn [o] (assoc o ::entity/key key)))
                                    next-option-paths (make-path-map updated-char)
                                    _ (js/console.log "NEXT_OPTON_PATHS" next-option-paths)
                                    next-all-selections (get-all-selections [] built-template next-option-paths)
                                    _ (js/console.log "NEXT ALL SELECTIONS" next-all-selections)
                                    [next-selection-path _] (selection-after option-path next-all-selections)
                                    _ (prn "NEXTSELECTIONS" (::path next-selection))
                                    next-template-path (entity/get-template-selection-path built-template next-selection-path [])]
                                (swap! app-state (fn [as]
                                                   (-> as
                                                       (assoc :character updated-char)
                                                       (assoc :stepper-selection-path next-template-path)))))))
                          (.stopPropagation e))}
             [:div.flex.align-items-c
              [:div.flex-grow-1
               [:div.flex
                [:span.f-w-b.f-s-16.flex-grow-1 name]
                (if help
                  [:span
                   [:span.underline.orange.p-0.m-r-2 "more"]
                   [:i.fa.fa-angle-down.orange]])]
               (if help
                 [:div.m-t-5
                  (doall
                   (map-indexed
                    (fn [i para]
                      ^{:key i}
                      [:p.m-b-5 para])
                    (s/split help #"\n")))])
               (if (not meets-prereqs?)
                 [:div.i.f-s-12.f-w-n 
                  (str "Requires " (s/join ", " failed-prereqs))])]
              (if (seq selections)
                [:i.fa.fa-caret-right])]]))
        options))]]
    #_[:div#options-column
       [option-sources collapsed-paths plugins]
       [:div
        (doall
         (map
          (fn [selection]
            ^{:key (::t/key selection)}
            [builder-selector-component [] option-paths selection built-char @character-ref built-template collapsed-paths stepper-selection-path])
          (::t/selections built-template)))]]))

(defn get-event-value [e]
  (.-value (.-target e)))

(defn character-field [app-state prop-name type & [cls-str]]
  (let [path [::entity/values prop-name]]
    [type {:class-name (str "input " cls-str)
           :type :text
           :value (get-in @app-state (concat [:character] path))
           :on-change (fn [e]
                        (swap! app-state
                               assoc-in
                               (concat [:character] path)
                               (get-event-value e)))}]))

(defn character-input [app-state prop-name & [cls-str]]
  (character-field app-state prop-name :input cls-str))

(defn character-textarea [app-state prop-name & [cls-str]]
  (character-field app-state prop-name :textarea cls-str))

(defn builder-columns [built-template built-char option-paths collapsed-paths stepper-selection-path plugins active-tabs stepper-dismissed?]
  [:div.flex-grow-1.flex
   {:class-name (s/join " " (map #(str (name %) "-tab-active") active-tabs))}
   #_[:div.builder-column.stepper-column
    (if (not stepper-dismissed?)
      [selection-stepper
       built-template
       option-paths
       @character-ref
       stepper-selection-path])]
   [:div.builder-column.options-column
    [options-column (:character @app-state) built-char built-template option-paths collapsed-paths stepper-selection-path plugins]]
   [:div.flex-grow-1.builder-column.personality-column
    [:div.m-t-5
     [:span.personality-label.f-s-18 "Character Name"]
     [character-input app-state :character-name]]
    [:div.field
     [:span.personality-label.f-s-18 "Personality Trait 1"]
     [character-textarea app-state :personality-trait-1]]
    [:div.field
     [:span.personality-label.f-s-18 "Personality Trait 2"]
     [character-textarea app-state :personality-trait-2]]
    [:div.field
     [:span.personality-label.f-s-18 "Ideals"]
     [character-textarea app-state :ideals]]
    [:div.field
     [:span.personality-label.f-s-18 "Bonds"]
     [character-textarea app-state :bonds]]
    [:div.field
     [:span.personality-label.f-s-18 "Flaws"]
     [character-textarea app-state :flaws]]
    [:div.field
     [:span.personality-label.f-s-18 "Image URL"]
     [character-input app-state :image-url]]
    [:div.field
     [:span.personality-label.f-s-18 "Description/Backstory"]
     [character-textarea app-state :description "h-800"]]]
   [:div.builder-column.details-column
    [character-display built-char]]])

(defn builder-tabs [active-tabs]
  [:div.hidden-lg.w-100-p
   [:div.builder-tabs
    [:span.builder-tab.options-tab
     {:class-name (if (active-tabs :options) "selected-builder-tab")
      :on-click (fn [_] (swap! app-state assoc-in tab-path #{:build :options}))} "Options"]
    [:span.builder-tab.personality-tab
     {:class-name (if (active-tabs :personality) "selected-builder-tab")
      :on-click (fn [_] (swap! app-state assoc-in tab-path #{:build :personality}))} "Personality"]
    [:span.builder-tab.build-tab
     {:class-name (if (active-tabs :build) "selected-builder-tab")
      :on-click (fn [_] (swap! app-state assoc-in tab-path #{:build :options}))} "Build"]
    [:span.builder-tab.details-tab
     {:class-name (if (active-tabs :details) "selected-builder-tab")
      :on-click (fn [_] (swap! app-state assoc-in tab-path #{:details}))} "Details"]]])

(defn export-pdf [built-char]
  (fn [_]
    (let [field (.getElementById js/document "fields-input")]
      (aset field "value" (str (pdf-spec/make-spec built-char)))
      (.submit (.getElementById js/document "download-form")))))

(defn download-form [built-char]
  (let [spec (pdf-spec/make-spec built-char)]
    [:form.download-form
     {:id "download-form"
      :action (if (.startsWith js/window.location.href "http://localhost")
                "http://localhost:8890/character.pdf"
                "/character.pdf")
      :method "POST"
      :target "_blank"}
     [:input {:type "hidden" :name "body" :id "fields-input"}]]))

(defn header [built-char]
  [:div.flex.align-items-c.justify-cont-s-b.w-100-p
   [:h1.f-s-36.f-w-b.m-t-21.m-b-19.m-l-10 "Character Builder"]
   [:button.form-button
    {:on-click (export-pdf built-char)
     :style {:height "40px"}}
    [:span "Print"]]])


(defn character-builder []
  (cljs.pprint/pprint (:character @app-state))
  ;;(cljs.pprint/pprint @app-state)
  (let [selected-plugins (map
                          :selections
                          (filter
                           (fn [{:keys [key]}]
                             (get-in @app-state [:plugins key]))
                           plugins))
        merged-template (update template
                                ::t/selections
                                (fn [s]
                                  (apply
                                   entity/merge-multiple-selections
                                   s
                                   selected-plugins)))
        option-paths (make-path-map (:character @app-state))
        built-template (entity/build-template (:character @app-state) merged-template)
        built-char (entity/build (:character @app-state) built-template)
        active-tab (get-in @app-state tab-path)
        view-width (.-width (gdom/getViewportSize js/window))
        stepper-selection-path (:stepper-selection-path @app-state)
        collapsed-paths (:collapsed-paths @app-state)
        mouseover-option (:mouseover-option @app-state)
        plugins (:plugins @app-state)
        stepper-dismissed? (:stepper-dismissed @app-state)]
    ;(js/console.log "BUILT TEMPLAT" built-template)
    (print-char built-char)
    [:div.app
     {:on-scroll (fn [e]
                   (let [app-header (js/document.getElementById "app-header")
                         header-height (.-offsetHeight app-header)
                         scroll-top (.-scrollTop (.-target e))
                         sticky-header (js/document.getElementById "sticky-header")
                         app-main (js/document.getElementById "app-main")
                         scrollbar-width (- js/window.innerWidth (.-offsetWidth app-main))
                         header-container (js/document.getElementById "header-container")]
                     (set! (.-paddingRight (.-style header-container)) (str scrollbar-width "px"))
                     (if (>= scroll-top header-height)
                       (set! (.-display (.-style sticky-header)) "block")
                       (set! (.-display (.-style sticky-header)) "none"))))}
     [download-form built-char]
     [:div#app-header.app-header
      [:div.app-header-bar.container
       [:div.content
        [:img.orcpub-logo {:src "image/orcpub-logo.svg"}]]]]
     [:div#sticky-header.sticky-header.w-100-p.posn-fixed
      [:div.flex.justify-cont-c.bg-light
       [:div#header-container.f-s-14.white.content
        (header built-char)]]]
     [:div.flex.justify-cont-c.white
      [:div.content (header built-char)]]
     [:div.flex.justify-cont-c.white
      [:div.content [builder-tabs active-tab]]]
     [:div#app-main.flex.justify-cont-c.p-b-40
      [:div.f-s-14.white.content
       [:div.flex.w-100-p
        [builder-columns
         built-template
         built-char
         option-paths
         collapsed-paths
         stepper-selection-path
         plugins
         active-tab
         stepper-dismissed?]]]]]))

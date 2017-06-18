(ns orcpub.character-builder
  (:require [goog.dom :as gdom]
            [goog.string :as gs]
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
            [orcpub.components :as comps]
            [orcpub.dnd.e5.character :as char5e]
            [orcpub.dnd.e5.character.equipment :as char-equip5e]
            [orcpub.dnd.e5.modifiers :as mod5e]
            [orcpub.dnd.e5.options :as opt5e]
            [orcpub.dnd.e5.template :as t5e]
            [orcpub.dnd.e5.spells :as spells]
            [orcpub.dnd.e5.weapons :as weapon5e]
            [orcpub.dnd.e5.armor :as armor5e]
            [orcpub.dnd.e5.magic-items :as mi5e]
            [orcpub.dnd.e5.display :as disp5e]
            [orcpub.dnd.e5.equipment :as equip5e]
            [orcpub.dnd.e5.skills :as skill5e]
            [orcpub.dnd.e5.events :as events5e]
            [orcpub.dnd.e5.db :as db5e]
            [orcpub.dnd.e5.views :as views5e]
            [orcpub.route-map :as routes]
            [orcpub.pdf-spec :as pdf-spec]
            [orcpub.user-agent :as user-agent]
            [orcpub.dnd.e5.db :as db]

            [clojure.spec :as spec]
            [clojure.spec.test :as stest]
            [cljs.core.async :refer [<!]]

            [reagent.core :as r]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def print-disabled? false)

(def print-enabled? (and (not print-disabled?)
                         (s/starts-with? js/window.location.href "http://localhost")))

(defn stop-propagation [e]
  (.stopPropagation e))

(defn stop-prop-fn [func]
  (fn [e]
    (func e)
    (stop-propagation e)))

(defn selector-id [path]
  (s/join "--" (map name path)))

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
   (dissoc (views5e/realize-char built-char) ::es/deps)))



(defn get-template-from-props [x]
  (get (.-argv (.-props x)) 6))

(defn help-section [help]
  [:div.m-t-10.f-w-n
   (if (string? help)
     (doall
      (map-indexed
       (fn [i para]
         ^{:key i}
         [:p.m-b-5.m-b-0-last para])
       (s/split help #"\n")))
     help)])

(defn expand-button [expand-text collapse-text expanded?]
  [:span.flex.pointer.align-items-c.justify-cont-end
   {:on-click (fn [e]
                (swap! expanded? not)
                (stop-propagation e))}
   [:span.underline.orange.p-0.m-r-2 (if @expanded? expand-text collapse-text)]
   [:i.fa.orange
    {:class-name (if @expanded? "fa-caret-up" "fa-caret-down")}]])

(defn show-info-button [expanded?]
  [:div.f-w-n.m-l-5 [expand-button "hide info" "show info" expanded?]])

(defn multiselection? [{:keys [::t/multiselect? ::t/min ::t/max]}]
  (or multiselect?
      (and (> min 1)
           (= min max))
      (nil? max)))

(defn get-event-value [e]
  (.-value (.-target e)))

(defn character-state-path [path]
  (concat [:character] path))


(defn character-field []
  (let [state (r/atom
               {:focused false
                :temp-val ""})]
    (fn [entity-values prop-name type & [cls-str handler]]
      (let [value (get entity-values prop-name)
            leave-handler (fn [_]
                            (if (:focused @state)
                              (if (not (and (s/blank? (:temp-val @state))
                                            (s/blank? value)))
                                (if handler
                                  (handler (:temp-val @state))
                                  (dispatch-sync [:update-value-field prop-name (:temp-val @state)])))
                              (swap! state
                                     assoc
                                     :focused false)))]
        [type {:class-name (str "input " cls-str)
               :type :text
               :value (if (:focused @state)
                        (:temp-val @state)
                        (or value (:temp-val @state)))
               :on-focus (fn [_]
                           (swap! state
                                  assoc
                                  :focused true
                                  :temp-val value))
               :on-mouse-out leave-handler
               :on-blur leave-handler
               :on-change #(let [v (get-event-value %)]
                             (swap! state
                                    assoc
                                    :temp-val v))}]))))

(defn character-input [entity-values prop-name & [cls-str handler]]
  [character-field entity-values prop-name :input cls-str handler])

(defn character-textarea [entity-values prop-name & [cls-str]]
  [character-field entity-values prop-name :textarea cls-str])

(defn prereq-failures [option]
  (remove
   nil?
   (map
    (fn [{:keys [::t/prereq-fn ::t/label] :as prereq}]
      (if prereq-fn
        (if (not (prereq-fn))
          label)
        (js/console.warn "NO PREREQ_FN" (::t/name option) prereq)))
    (::t/prereqs option))))

(defn class-level-selector []
  (let [expanded? (r/atom false)]
    (fn [i key selected-class options unselected-classes-set]
      (let [options-map (zipmap (map ::t/key options) options)
            class-template-option (options-map key)
            path [:class-levels key]]
        [:div.m-b-5
         {:class-name (if @expanded? "b-1 b-rad-5 p-5")}
         [:div.flex.align-items-c
          [:select.builder-option.builder-option-dropdown.flex-grow-1.m-t-0
           {:value key
            :on-change (fn [e] (let [new-key (keyword (.. e -target -value))]
                                 (dispatch [:set-class new-key i options-map])))}
           (doall
            (map
             (fn [{:keys [::t/key ::t/name] :as option}]
               (let [failed-prereqs (if (pos? i) (prereq-failures option))]
                 ^{:key key}
                 [:option.builder-dropdown-item
                  {:value key
                   :disabled (seq failed-prereqs)}
                  (str name (if (seq failed-prereqs) (str " (" (s/join ", " failed-prereqs) ")")))]))
             (filter
              #(or (= key (::t/key %))
                   (unselected-classes-set (::t/key %)))
              options)))]
          (if (::t/help class-template-option)
            [show-info-button expanded?])
          (let [levels-selection (some #(if (= :levels (::t/key %)) %) (::t/selections class-template-option))
                available-levels (::t/options levels-selection)
                last-level-key (str "level-" (:class-level selected-class))]
            [:select.builder-option.builder-option-dropdown.m-t-0.m-l-5.w-100
             {:value last-level-key
              :on-change
              (fn [e]
                (let [new-highest-level-str (.. e -target -value)
                      new-highest-level (js/parseInt (last (s/split new-highest-level-str #"-")))]
                  (dispatch [:set-class-level i new-highest-level])))}
             (doall
              (map-indexed
               (fn [i {level-key ::t/key}]
                 ^{:key level-key}
                 [:option.builder-dropdown-item
                  {:value level-key}
                  (inc i)])
               available-levels))])
          [:i.fa.fa-minus-circle.orange.f-s-16.m-l-5.pointer
           {:on-click (fn [_] (dispatch [:delete-class key i options-map]))}]]
         (if @expanded?
           [:div.m-t-5.m-b-10 (::t/help class-template-option)])]))))

(defn class-level-data [option]
  (let [levels (some
                (fn [s]
                  (if (= :levels (::t/key s))
                    s))
                (::t/selections option))]
    (assoc
     (select-keys option [::t/key ::t/prereqs ::t/name ::t/help ::t/associated-options])
     ::t/selections
     [{::t/key (::t/key levels)
       ::t/options (map #(select-keys % [::t/key]) (::t/options levels))}])))

(defn class-levels-selector [{:keys [selection]}]
  (let [options (::t/options selection)
        selected-classes @(subscribe [::char5e/levels])
        unselected-classes (remove
                            (into #{} (keys selected-classes))
                            (map ::t/key options))
        unselected-classes-set (set unselected-classes)
        remaining-classes (filter
                           (fn [option]
                             (and
                              (unselected-classes-set (::t/key option))
                              (entity/meets-prereqs? option)))
                           options)]
    [:div
     [:div
      (doall
       (map-indexed
        (fn [i [key selected-class]]
          ^{:key key}
          [class-level-selector i key selected-class (map class-level-data options) unselected-classes-set])
        selected-classes))]
     (if (seq remaining-classes)
       [:div.orange.p-5.underline.pointer
        [:i.fa.fa-plus-circle.orange.f-s-16]
        [:span.m-l-5
         {:on-click
          (fn [_]
            (let [first-unselected (::t/key (first remaining-classes))]
              (dispatch [:add-class first-unselected])))}
         "Add Class"]])]))

(defn inventory-item []
  (let [expanded? (r/atom false)]
    (fn [{:keys [selection-key
                 item-key
                 item-name
                 item-qty
                 item-description
                 equipped?
                 i
                 qty-input-width
                 check-fn
                 qty-change-fn
                 remove-fn]}]
      [:div.p-5
       [:div.f-w-b.flex.align-items-c
        [:div.pointer.m-l-5
         {:on-click check-fn}
         (comps/checkbox equipped? false)]
        [:div.flex-grow-1 item-name]
        (if item-description [:div.w-60 [show-info-button expanded?]])
        [:input.input.m-l-5.m-t-0.
         {:class-name (str "w-" (or qty-input-width 60))
          :type :number
          :value item-qty
          :on-change qty-change-fn}]
        [:i.fa.fa-minus-circle.orange.f-s-16.m-l-5.pointer
         {:on-click remove-fn}]]
       (if @expanded? [:div.m-t-5 item-description])])))

(defn inventory-selector [item-map qty-input-width {:keys [selection]} & [custom-equipment-key]]
  (let [{:keys [::t/key ::t/options]} selection
        selected-items @(subscribe [:entity-option key])
        selected-keys (into #{} (map ::entity/key selected-items))]
    [:div
     [comps/selection-adder
      (sort-by
         :name
         (sequence
          (comp
           (remove
            #(selected-keys (::t/key %)))
           (map
            (fn [{:keys [::t/name ::t/key]}]
              {:name name
               :key key})))
          options))
      (fn [e]
         (let [kw (keyword (.. e -target -value))]
           (dispatch [:add-inventory-item key kw])))]
     (if (seq selected-items)
       [:div.flex.f-s-12.opacity-5.m-t-10.justify-cont-s-b
        [:div.m-r-10 "Equipped?"]
        [:div.m-r-30 "Quantity"]])
     [:div
      (doall
       (map-indexed
        (fn [i {item-key ::entity/key {item-qty ::char-equip5e/quantity
                                       equipped? ::char-equip5e/equipped?
                                       item-name ::char-equip5e/name} ::entity/value}]
          (let [item (item-map item-key)
                item-name (or item-name (:name item))
                item-description (:description item)]
            ^{:key item-key}
            [inventory-item {:selection-key key
                             :item-key item-key
                             :item-name item-name
                             :item-qty item-qty
                             :item-description item-description
                             :equipped? equipped?
                             :i i
                             :qty-input-width qty-input-width
                             :check-fn (fn [_]
                                         (dispatch [:toggle-inventory-item-equipped key i]))
                             :qty-change-fn (fn [e]
                                              (let [qty (.. e -target -value)]
                                                (dispatch [:change-inventory-item-quantity key i qty])))
                             :remove-fn (fn [_]
                                          (dispatch [:remove-inventory-item key item-key]))}]))
        selected-items))]
     (if custom-equipment-key
       [:div
        (doall
         (map-indexed
          (fn [i {:keys [::char-equip5e/name ::char-equip5e/quantity ::char-equip5e/equipped?] :as item}]
            (let [item-key (common/name-to-kw name)]
              ^{:key item-key}
              [inventory-item {:selection-key custom-equipment-key
                               :item-key item-key
                               :item-name name
                               :item-qty quantity
                               :equipped? equipped?
                               :i i
                               :qty-input-width qty-input-width
                               :check-fn (fn [_]
                                           (dispatch [:toggle-custom-inventory-item-equipped custom-equipment-key i]))
                               :qty-change-fn (fn [e]
                                                (let [qty (.. e -target -value)]
                                                  (dispatch [:change-custom-inventory-item-quantity custom-equipment-key i qty])))
                               :remove-fn (fn [_]
                                            (dispatch [:remove-custom-inventory-item custom-equipment-key name]))}]))
          @(subscribe [:entity-value custom-equipment-key])))])]))

(defn option-selector-base []
  (let [expanded? (r/atom false)]
    (fn [{:keys [name key help selected? selectable? option-path select-fn content explanation-text icon classes multiselect? disable-checkbox?]}]
      [:div.p-10.b-1.b-rad-5.m-5.b-orange.hover-shadow
       {:class-name (s/join " " (conj
                                 (remove nil? [(if selected? "b-w-5")
                                               (if selectable? "pointer")
                                               (if (not selectable?) "opacity-5")])
                                 classes))
        :on-click select-fn}
       [:div.flex.align-items-c
        [:div.flex-grow-1
         [:div.flex.align-items-c
          (if multiselect?
            (comps/checkbox selected? disable-checkbox?))
          (if icon [:div.m-r-5 (views5e/svg-icon icon 24)])
          [:span.f-w-b.f-s-1.flex-grow-1 name]
          (if help
            [show-info-button expanded?])]
         (if (and help @expanded?)
           [help-section help])
         (if (and content selected?)
           content)
         (if explanation-text
           [:div.i.f-s-12.f-w-n 
            explanation-text])]]])))

(defn skill-help [name key ability icon description]
  [:div
   [:div.flex.align-items-c
    (views5e/svg-icon icon 48)
    [:div.f-s-18.f-w-b.m-l-5
     [:div name]
     [:div.i (str "(" (:name (opt5e/abilities-map ability)) ")")]]]
   [:p description]])

(defn remaining-indicator [remaining & [size font-size]]
  [:span.bg-red.t-a-c.p-t-4.b-rad-50-p.inline-block.f-w-b
   (let [size (or size 18)
         font-size (or font-size 14)]
     {:class-name (str "h-" size " w-" size " f-s-" font-size)})
   remaining])

(defn validate-selections [built-template character selections]
  (mapcat
   (fn [{:keys [::t/name ::t/tags] :as selection}]
     (if (not (get tags :starting-equipment))
       (let [remaining (entity/count-remaining built-template character selection)]
         (cond
           (pos? remaining) [(str "You have " remaining " more '" name "' selection" (if (> remaining 1) "s") " to make.")]
           (neg? remaining) [(str "You must remove " (Math/abs remaining) " '" name "' selection" (if (< remaining -1) "s") ".")]
           :else nil))))
   (entity/combine-selections selections)))


(defn new-option-selector [option-path
                           {:keys [::t/min ::t/max ::t/options ::t/multiselect? ::t/ref] :as selection}
                           disable-select-new?
                           homebrew?
                           {:keys [::t/key ::t/name ::t/path ::t/help ::t/selections ::t/prereqs
                                   ::t/modifiers ::t/select-fn ::t/ui-fn ::t/icon] :as option}]
  (let [built-template @(subscribe [:built-template])
        option-paths @(subscribe [:option-paths])
        new-option-path (conj (vec option-path) key)
        selected? (get-in option-paths new-option-path)
        failed-prereqs (reduce
                        (fn [failures {:keys [::t/prereq-fn ::t/label ::t/hide-if-fail?] :as prereq}]
                          (if (and prereq-fn (not (prereq-fn)))
                            (conj failures prereq)
                            failures))
                        []
                        prereqs)
        meets-prereqs? (empty? failed-prereqs)
        selectable? (or homebrew?
                        (and (or selected?
                                 meets-prereqs?)
                             (or (not disable-select-new?)
                                 selected?)))
        has-selections? (seq selections)
        named-modifiers (map (fn [{:keys [::mod/name ::mod/value]}]
                               (str name " " value))
                             (filter ::mod/name (flatten modifiers)))
        has-named-mods? (seq named-modifiers)
        modifiers-str (s/join ", " named-modifiers)
        multiselect? (or multiselect?
                         ref
                         (> min 1)
                         (nil? max))]
    (if (not-any? ::t/hide-if-fail? failed-prereqs)
      ^{:key key}
      [option-selector-base {:name name
                             :key key
                             :help (if (or help has-named-mods?)
                                     [:div
                                      (if has-named-mods? [:div.i modifiers-str])
                                      [:div {:class-name (if has-named-mods? "m-t-5")} help]])
                             :selected? selected?
                             :selectable? selectable?
                             :option-path new-option-path
                             :multiselect? multiselect?
                             :select-fn (fn [e]
                                          (if (or multiselect?
                                                  (not selected?))
                                            (dispatch [:select-option {:option-path option-path
                                                                       :selected? selected?
                                                                       :selectable? selectable?
                                                                       :homebrew? homebrew?
                                                                       :meets-prereqs? meets-prereqs?
                                                                       :selection selection
                                                                       :option option
                                                                       :has-selections? has-selections?
                                                                       :built-template built-template
                                                                       :new-option-path new-option-path}]))
                                          (.stopPropagation e))
                             :content (if (and (or selected? (= 1 (count options))) ui-fn)
                                        (ui-fn new-option-path built-template))
                             :explanation-text (let [explanation-text (if (and (not meets-prereqs?)
                                                                               (not selected?))
                                                                        (s/join ", " (map ::t/label failed-prereqs)))]                      
                                                 explanation-text)
                             :icon icon}])))

(defn selection-section-title [title]
  [:span.m-l-5.f-s-18.f-w-b.flex-grow-1 title])

(defn selection-section-parent-title [title]
  [:span.i.f-s-14.f-w-n.m-l-5.m-b-2 title])

(defn remaining-component [max remaining]
  [:div.m-l-10
   (cond
     (pos? remaining)
     [:div.flex.align-items-c
      (remaining-indicator remaining)
      [:span.i.m-l-5 "remaining"]]

     (or (zero? remaining)
         (and (nil? max)
              (neg? remaining)))
     [:div.flex.align-items-c
      [:span.bg-green.t-a-c.w-18.h-18.p-t-2.b-rad-50-p.inline-block.f-w-b
       [:i.fa.fa-check.f-s-12]]
      [:span.i.m-l-5 "complete"]]

     (neg? remaining)
     [:div.flex.align-items-c
      [:span.i.m-r-5 "remove"]
      [:span.bg-red.t-a-c.w-18.h-18.p-t-4.b-rad-50-p.inline-block.f-w-b (Math/abs remaining)]])])

(defn selection-section-base []
  (let [expanded? (r/atom false)]
    (fn [{:keys [path parent-title name icon help max min remaining body hide-lock? hide-homebrew?]}]
      (let [locked? @(subscribe [:locked path])
            homebrew? @(subscribe [:homebrew? path])]
        [:div.p-5.m-b-20.m-b-0-last
         (if parent-title
           (selection-section-parent-title parent-title))
         [:div.flex.align-items-c
          (if icon (views5e/svg-icon icon 24))
          (selection-section-title name)
          (if (and path help)
            [show-info-button expanded?])
          (if (not hide-lock?)
            [:i.fa.f-s-16.m-l-10.m-r-5.pointer
             {:class-name (if locked? "fa-lock" "fa-unlock-alt opacity-5 hover-opacity-full")
              :on-click #(dispatch [:toggle-locked path])}])
          (if (not hide-homebrew?)
            [:span.pointer
             {:class-name (if (not homebrew?) "opacity-5 hover-opacity-full")
              :on-click #(dispatch [:toggle-homebrew path])}
             (views5e/svg-icon "beer-stein" 18)])]
         (if (and help path @expanded?)
           [help-section help])
         (if (int? min)
           [:div.p-5.f-s-16
            [:div.flex.align-items-c.justify-cont-s-b
             [:span.i.m-r-10 (str "select " (cond
                                              (= min max) min
                                              (zero? min) (if (nil? max)
                                                            "any number"
                                                            (str "up to " max))
                                              :else (str "at least " min)))]
             (remaining-component max remaining)]])
         body]))))

(def ignore-paths-ending-with #{:class :levels :asi-or-feat :ability-score-improvement})

(defn ancestor-names-string [built-template path]
  (let [ancestor-paths (map
                        (fn [p]
                          (if (ignore-paths-ending-with (last p))
                            []
                            p))
                        (reductions conj [] path))
        ancestors (map (fn [a-p]
                         (entity/get-in-lazy built-template
                                 (entity/get-template-selection-path built-template a-p [])))
                       (butlast ancestor-paths))
        ancestor-names (map ::t/name (remove nil? ancestors))]
    (s/join " - " ancestor-names)))

(defn selection-section-column [option-selectors]
  (doall
   (map-indexed
    (fn [i selector]
      ^{:key i}
      [:div selector])
    option-selectors)))

(defn selection-section [built-template option-paths ui-fns {:keys [::t/key ::t/name ::t/help ::t/options ::t/min ::t/max ::t/ref ::t/icon ::t/multiselect? ::entity/path ::entity/parent] :as selection} num-columns & [hide-homebrew?]]
  (let [actual-path (entity/actual-path selection)
        character @(subscribe [:character])
        remaining (entity/count-remaining built-template character selection)
        expanded? (r/atom false)
        ancestor-names (ancestor-names-string built-template actual-path)
        homebrew? @(subscribe [:homebrew? (or ref path)])
        has-custom-item? (some #(= :custom (::t/key %)) options)
        disable-select-new? (and multiselect?
                                 (not (pos? remaining))
                                 (some? max))]
    [selection-section-base {:path actual-path
                             :parent-title (if (not (s/blank? ancestor-names)) ancestor-names)
                             :name name
                             :icon icon
                             :help help
                             :max max
                             :min min
                             :num-columns num-columns
                             :remaining remaining
                             :hide-homebrew? (or hide-homebrew? (not (or multiselect? has-custom-item?)))
                             :body (if (and ui-fns (or (ui-fns ref)
                                                       (ui-fns key)))
                                     (let [ui-fn (or (ui-fns ref)
                                                       (ui-fns key))]                                     
                                       (ui-fn
                                        {:character character
                                         :selection selection}))
                                     (let [option-selectors
                                           (remove
                                            nil?
                                            (map
                                             (fn [option]
                                               (new-option-selector
                                                actual-path
                                                selection
                                                disable-select-new?
                                                homebrew?
                                                option))
                                             (sort-by (juxt ::t/order ::t/name) options)))]
                                       [:div.flex
                                        (doall
                                         (map-indexed
                                          (fn [i part]
                                            ^{:key i}
                                            [:div.flex-grow-1
                                             {:class-name (str "w-" (int (/ 100 num-columns)) "-p")}
                                             (doall
                                              (map-indexed
                                               (fn [j selector]
                                                 ^{:key j}
                                                 [:div selector])
                                               part))])
                                          (partition-all
                                           (common/round-up (/ (count option-selectors)
                                                               num-columns))
                                           option-selectors)))]))}]))

(defn set-abilities! [abilities]
  (dispatch [:set-abilities abilities]))

(defn reroll-abilities []
  (dispatch [:set-abilities (char5e/standard-ability-rolls)]))

(defn set-standard-abilities []
  (fn []
    (dispatch [:set-abilities (char5e/abilities 15 14 13 12 10 8)])))

(defn reset-point-buy-abilities []
  (fn []
    (dispatch [:set-abilities (char5e/abilities 8 8 8 8 8 8)])))

(defn swap-abilities [i other-i k v]
  (stop-prop-fn
   (fn []
     (dispatch [:swap-ability-values i other-i k v]))))

(defn ability-subtitle [title]
  [:div.t-a-c.f-s-10.opacity-5 title])


(defn ability-modifier [v]
  [:div.f-6-12.f-w-n.h-24
   (ability-subtitle "mod")
   [:div.m-t--1
    (opt5e/ability-bonus-str v)]])

(defn ability-component [k v i controls]
  [:div.t-a-c
   [:div.f-s-18.f-w-b v]
   controls])

(def score-costs
  {8 0
   9 1
   10 2
   11 3
   12 4
   13 5
   14 7
   15 9})

(def point-buy-points 27)

(defn ability-value [v]
  [:div.f-s-18.f-w-b v])

(defn ability-increases-component [built-template asi-selections ability-keys]
  (let [total-abilities @(subscribe [::char5e/abilities])
        character @(subscribe [:character])]
    [:div
     (doall
      (map-indexed
       (fn [i {:keys [::t/name ::t/key ::t/min ::t/max ::t/options ::t/different? ::entity/path] :as selection}]
         (let [increases-path (entity/get-entity-path built-template character path)
               selected-options (get-in character increases-path)
               ability-increases (frequencies (map ::entity/key selected-options))
               num-increased (apply + (vals ability-increases))
               num-remaining (if (or (nil? max)
                                     (<= min num-increased max))
                               0
                               (- min num-increased))
               allowed-abilities (into #{} (map ::t/key options))
               ancestors-title (ancestor-names-string built-template path)]
           ^{:key i}
           [:div
            [:div.flex.justify-cont-s-a
             (doall (for [i (range 6)] ^{:key i} [:div.m-t-10 "+"]))]
            [:div.flex.justify-cont-s-b.m-t-10.align-items-c
             [:div
              [:div.m-l-5.i (str "Improvement: " ancestors-title)]]
             (remaining-component 2 num-remaining)]
            [:div.flex.justify-cont-s-a.m-t-5
             (doall
              (map-indexed
               (fn [i k]
                 (let [ability-disabled? (not (allowed-abilities k))
                       increase-disabled? (or ability-disabled?
                                              (and (some? max)
                                                   (zero? (- max num-increased)))
                                              (and different? (pos? (ability-increases k)))
                                              (>= (total-abilities k) 20))
                       decrease-disabled? (or ability-disabled?
                                              (not (pos? (ability-increases k))))]
                   ^{:key k}
                   [:div.t-a-c
                    {:class-name (if ability-disabled? "opacity-5 cursor-disabled")}
                    [:div
                     {:class-name (if (and (not ability-disabled?)
                                           (zero? (ability-increases k 0)))
                                    "opacity-5")}
                     (ability-value (ability-increases k 0))] 
                    [:div.f-s-16
                     [:i.fa.fa-minus-circle.orange
                      {:class-name (if decrease-disabled? "opacity-5 cursor-disabled")
                       :on-click (stop-prop-fn
                                  (fn []
                                    (if (not decrease-disabled?)
                                      (dispatch [:decrease-ability-value increases-path k]))))}]
                     [:i.fa.fa-plus-circle.orange.m-l-5
                      {:class-name (if increase-disabled? "opacity-5 cursor-disabled")
                       :on-click (stop-prop-fn
                                  (fn []
                                    (if (not increase-disabled?)
                                      (dispatch [:increase-ability-value increases-path k]))))}]]]))
               ability-keys))]]))
       asi-selections))]))

(defn race-abilities-component [ability-keys]
  (let [race-ability-increases @(subscribe [::char5e/race-ability-increases])
        subrace-ability-increases @(subscribe [::char5e/subrace-ability-increases])
        ability-increases @(subscribe [::char5e/ability-increases])
        total-abilities @(subscribe [::char5e/abilities])]
    [:div.flex.justify-cont-s-a
     (doall
      (map-indexed
       (fn [i k]
         ^{:key k}
         [:div.t-a-c
          (if (seq race-ability-increases)
            [:div
             [:div.m-t-10.m-b-10 "+"]
             (ability-subtitle "race")
             (let [race-v (get race-ability-increases k 0)]
               [:div
                {:class-name (if (zero? race-v)
                               "opacity-5")}
                (ability-value race-v)])])
          (if (seq subrace-ability-increases)
            [:div
             [:div.m-t-10.m-b-10 "+"]
             (ability-subtitle "subrace")
             (let [subrace-v (get subrace-ability-increases k 0)]
               [:div
                {:class-name (if (zero? subrace-v)
                               "opacity-5")}
                (ability-value subrace-v)])])
          (if (seq ability-increases)
            [:div
             [:div.m-t-10.m-b-10 "+"]
             (ability-subtitle "other")
             (let [other-v (- (get ability-increases k 0)
                              (get race-ability-increases k 0)
                              (get subrace-ability-increases k 0))]
               [:div
                {:class-name (if (zero? other-v)
                               "opacity-5")}
                (ability-value other-v)])])])
       ability-keys))]))

(defn abilities-matrix-footer [ability-keys]
  (let [total-abilities @(subscribe [::char5e/abilities])]
    [:div
     (race-abilities-component ability-keys)
     [:div.flex.justify-cont-s-a
      (doall
       (map-indexed
        (fn [i k]
          ^{:key k}
          [:div.t-a-c
           [:div.m-t-10.m-b-10 "="]
           (ability-subtitle "total")
           [:div.f-s-24.f-w-b (total-abilities k)]
           (ability-modifier (total-abilities k))])
        ability-keys))]]))

(defn abilities-header [ability-keys]
  [:div.flex.justify-cont-s-a
   (doall
    (map-indexed
     (fn [i k]
       ^{:key k}
       [:div.m-t-10.t-a-c
        (t5e/ability-icon k 24)
        [:div.uppercase (name k)]
        (ability-subtitle "base")])
     ability-keys))])

(defn abilities-component [built-template
                           asi-selections
                           content]
  (let [total-abilities @(subscribe [::char5e/abilities])]
    [:div
     (abilities-header char5e/ability-keys)
     content
     (ability-increases-component built-template asi-selections char5e/ability-keys)
     (abilities-matrix-footer char5e/ability-keys)]))


(defn point-buy-abilities [built-template asi-selections]
  (let [default-base-abilities (char5e/abilities 8 8 8 8 8 8)
        abilities (or @(subscribe [::char5e/ability-scores-option-value])
                      default-base-abilities)
        points-used (apply + (map (comp score-costs second) abilities))
        points-remaining (- point-buy-points points-used)
        total-abilities @(subscribe [::char5e/abilities])]
    (abilities-component
     built-template
     asi-selections
     [:div
      [:div.flex.justify-cont-s-a
       (for [i (range 6)]
         ^{:key i}
         [:div
          (ability-value 8)
          [:div.m-t-10 "+"]])]
      [:div.flex.justify-cont-s-b.m-t-10.align-items-c
       [:div.m-l-5 "Point Buys"]
       (remaining-component 27 points-remaining)]
      [:div.flex.justify-cont-s-a
       (doall
        (map
         (fn [k]
           (let [v (abilities k)
                 increase-disabled? (or (zero? points-remaining)
                                        (= 15 v)
                                        (>= (total-abilities k) 20)
                                        (> (- (score-costs (inc v))
                                              (score-costs v))
                                           points-remaining))
                 decrease-disabled? (or (<= v 8) (>= points-remaining point-buy-points))]
             ^{:key k}
             [:div.t-a-c
              (ability-subtitle "bought")
              (ability-value (- v 8))
              [:div.f-s-11.f-w-b (str "(" (score-costs v) " pts)")]
              [:div.f-s-16
               [:i.fa.fa-minus-circle.orange
                {:class-name (if decrease-disabled? "opacity-5 cursor-disabled")
                 :on-click (stop-prop-fn
                            (fn [e]
                              (if (not decrease-disabled?)
                                (set-abilities! (update abilities k dec)))))}]
               [:i.fa.fa-plus-circle.orange.m-l-5
                {:class-name (if increase-disabled? "opacity-5 cursor-disabled")
                 :on-click (stop-prop-fn
                            (fn [_]
                              (if (not increase-disabled?) (set-abilities! (update abilities k inc)))))}]]]))
         char5e/ability-keys))]])))

(defn abilities-standard []
  [:div.flex.justify-cont-s-a
   (let [abilities (or @(subscribe [::char5e/ability-scores-option-value])
                       (char5e/abilities 15 14 13 12 10 8))
          abilities-vec (map (fn [k] [k (abilities k)]) char5e/ability-keys)]
      (doall
       (map-indexed
        (fn [i [k v]]
          ^{:key k}
          [ability-component k v i
           [:div.f-s-16
            [:i.fa.fa-chevron-circle-left.orange
             {:on-click (swap-abilities i (dec i) k v)}]
            [:i.fa.fa-chevron-circle-right.orange.m-l-5
             {:on-click (swap-abilities i (inc i) k v)}]]])
        abilities-vec)))])

(defn abilities-roller [built-template asi-selections]
  (abilities-component
   built-template
   asi-selections
   [:div
    (abilities-standard)
    [:button.form-button.m-t-5
     {:on-click (stop-prop-fn
                 (fn [e]
                   (reroll-abilities)))}
     "Re-Roll"]]))

(defn abilities-standard-editor [built-template asi-selections]
  (abilities-component
   built-template
   asi-selections
   (abilities-standard)))

(defn abilities-entry [built-template asi-selections]
  (let [abilities (or @(subscribe [::char5e/ability-scores-option-value])
                      (char5e/abilities 15 14 13 12 10 8))
        total-abilities @(subscribe [::char5e/abilities])]
    [:div
     (abilities-header char5e/ability-keys)
     [:div.flex.justify-cont-s-a
      (doall
       (map
        (fn [k]
          ^{:key k}
          [:div.p-1
           [:input.input.f-s-18.m-b-5.p-l-0
            {:value (k abilities)
             :on-change (fn [e] (let [value (.-value (.-target e))
                                      new-v (if (not (s/blank? value))
                                              (js/parseInt value))]
                                  (dispatch [:set-ability-score k new-v])))}]])
        char5e/ability-keys))]
     (ability-increases-component built-template asi-selections char5e/ability-keys)
     (race-abilities-component char5e/ability-keys)
     [:div.flex.justify-cont-s-a
      (doall
       (map
        (fn [[k v]]
          ^{:key k}
          [:div.t-a-c.p-1
           [:div.m-t-10.m-b-10 "="]
           [:div.f-w-b "total"]
           [:input.input.b-3.f-s-18.m-b-5.p-l-0
            {:value (if (abilities k)
                      (total-abilities k))
             :on-change (fn [e] (let [total (total-abilities k)                                     
                                      value (.-value (.-target e))
                                      diff (- total
                                              (abilities k))
                                      new-v (if (not (s/blank? value))
                                              (- (js/parseInt value) (or diff 0)))]
                                  (dispatch [:set-ability-score k new-v])))}]])
        total-abilities))]]))

(defn ability-variant-option-selector [name key selected-key content & [select-fn]]
  [option-selector-base
   {:name name
    :key key
    :selected? (= selected-key key)
    :selectable? true
    :option-path [:ability-scores key]
    :content content
    :select-fn (fn [_]
                 (when (not= selected-key key)
                   (if select-fn (select-fn))
                   (dispatch [:set-ability-score-variant key])))}])

(defn abilities-editor [{:keys [built-template option-paths selections]}]
  [:div
   (let [asi-or-feat-selections (filter
                                 (fn [s]
                                   (= :asi-or-feat (::t/key s)))
                                 selections)
         character @(subscribe [:character])]
     (if (seq asi-or-feat-selections)
       [:div
        [:div.m-l-5 (selection-section-title "Ability Score Improvements")]
        (doall
         (map-indexed
          (fn [i {:keys [::t/key ::t/min ::t/max ::t/options ::entity/path] :as selection}]
            (let [remaining (entity/count-remaining built-template character selection)]
              ^{:key i}
              [selection-section-base
               {:path path
                :parent-title (ancestor-names-string built-template path)
                :max 1
                :min 1
                :remaining remaining
                :hide-lock? true
                :body (doall
                       (map
                        (fn [option]
                          ^{:key (::t/key option)}
                          (new-option-selector path
                                               selection
                                               (and max (> min 1) (zero? remaining))
                                               false
                                               option))
                        (sort-by ::t/name options)))}]))
          asi-or-feat-selections))]))
   (let [asi-selections (filter (fn [s] (= :asi (::t/key s))) selections)
         selected-variant @(subscribe [::char5e/ability-scores-option-key])]
     [selection-section-base
      {:path [:ability-scores]
       :name "Abilities Variant"
       :min 1
       :max 1
       :hide-homebrew? true
       :body [:div
              (ability-variant-option-selector
               "Point Buy"
               :point-buy
               selected-variant
               (point-buy-abilities built-template asi-selections)
               #(set-abilities! (char5e/abilities 8 8 8 8 8 8)))
              (ability-variant-option-selector
               "Dice Roll"
               :standard-roll
               selected-variant
               (abilities-roller built-template asi-selections)
               #(reroll-abilities))
              (ability-variant-option-selector
               "Standard Scores"
               :standard-scores
               selected-variant
               (abilities-standard-editor built-template asi-selections)
               #(set-abilities! (char5e/abilities 15 14 13 12 10 8)))
              (ability-variant-option-selector
               "Manual Entry"
               :manual-entry
               selected-variant
               (abilities-entry built-template asi-selections))]}])])


(defn skills-selector [{:keys [selection]}]
  (let [{:keys [::t/ref ::t/max ::t/options]} selection
        character @(subscribe [:character])
        path (concat [::entity/options] ref)
        selected-skills (get-in character path)
        selected-count (count selected-skills)
        remaining (- max selected-count)
        available-skills (into #{} (map ::t/key options))
        selected-skill-keys (into #{} (map ::entity/key selected-skills))]
    (doall
     (map
      (fn [{:keys [name key ability icon description]}]
        (let [skill-profs @(subscribe [::char5e/skill-profs])
              has-prof? (and skill-profs (skill-profs key))
              selected? (selected-skill-keys key)
              selectable? (available-skills key)
              bad-selection? (and selected? (not selectable?))
              homebrew? @(subscribe [:homebrew? ref])
              allow-select? (or homebrew?
                                selected?
                                (and (not selected?)
                                     (or (pos? remaining)
                                         (nil? max))
                                     selectable?
                                     (not has-prof?))
                                bad-selection?)]
          ^{:key key}
          [option-selector-base {:name name
                                 :key key
                                 :help (skill-help name key ability icon description)
                                 :selected? selected?
                                 :selectable? allow-select?
                                 :option-path [:skill-profs key]
                                 :select-fn (fn [_]
                                              (if allow-select?
                                                (dispatch [:select-skill path selected? key])))
                                 :explanation-text (if (and has-prof?
                                                            (not selected?))
                                                     "You already have this skill proficiency")
                                 :icon icon
                                 :classes (if bad-selection? "b-red")
                                 :multiselect? true}]))
      skill5e/skills))))


(def hit-points-headers
  [:tr.f-w-b.t-a-l
   [:th.p-5 "Level"]
   [:th.p-5 "Base"]
   [:th.p-5 "Con"]
   [:th.p-5 "Misc"]
   [:th.p-5 "Total"]])

(defn hp-selection-name-level [selection]
  (let [[_ class-kw _ level-kw _] (::entity/path selection)
        class-name (s/capitalize (name class-kw))]
    {:name class-name
     :level (js/parseInt (last (s/split (name level-kw) #"-")))
     :key class-kw}))

(defn hit-points-entry [character selections built-template]
  (let [classes @(subscribe [::char5e/classes])
        levels @(subscribe [::char5e/levels])
        first-class (if levels (levels (first classes)))
        first-class-hit-die (:hit-die first-class)
        level-bonus @(subscribe [::char5e/hit-point-level-bonus])
        con-bonus (::char5e/con @(subscribe [::char5e/ability-bonuses]))
        con-bonus-str (common/bonus-str con-bonus)
        misc-bonus (- level-bonus con-bonus)
        misc-bonus-str (common/bonus-str misc-bonus)
        all-level-values (map
                          (fn [selection]
                            (let [name-level (hp-selection-name-level selection)
                                  value (get-in character (entity/get-option-value-path built-template character (::entity/path selection)))]
                              {:name (str (:name name-level) (:level name-level))
                               :level (:level name-level)
                               :class (:key name-level)
                               :class-name (:name name-level)
                               :value value
                               :path (::entity/path selection)}))
                          (sort-by (fn [s] ((juxt :name :level)
                                            (hp-selection-name-level s))) selections))
        total-base-hps (apply + (:hit-die first-class) (map :value all-level-values))
        total-con-bonus (* con-bonus (inc (count selections)))
        total-misc-bonus (* misc-bonus (inc (count selections)))
        total-level-bonus (+ total-con-bonus total-misc-bonus)
        by-class (group-by :class all-level-values)
        total-hps (+ total-con-bonus total-misc-bonus total-base-hps)]
    [:div.m-t-5.p-5
     [:div.flex.align-items-c.justify-cont-s-b
      [:div.f-s-16.m-b-5
       [:span.f-w-b "Total:"]
       [:span.m-l-5
        (if (seq selections)
          [:input.input.w-70.b-3.f-w-b.f-s-16
           {:type :number
            :value total-hps
            :on-change (fn [e]
                         (let [value (js/parseInt (.. e -target -value))
                               total-value (if (js/isNaN value) 0 (- value first-class-hit-die total-level-bonus))
                               average-value (int (/ total-value (count selections)))
                               remainder (rem total-value (count selections))
                               first-selection (first selections)]
                           (doseq [selection selections]
                             (let [entity-path (entity/get-entity-path built-template character (::entity/path selection))]
                               (dispatch [:set-total-hps entity-path first-selection selection average-value remainder])))))}]
          total-hps)]]
      (if (seq selections)
        [:button.form-button.p-10
         {:on-click (fn [_]
                      (doseq [selection selections]
                        (let [[_ class-kw :as path] (::entity/path selection)]
                          (dispatch [:randomize-hit-points built-template path levels class-kw]))))}
         "Random"])
      (if (seq selections)
        [:button.form-button.p-10
         {:on-click (fn [_]
                      (doseq [selection selections]
                        (let [[_ class-kw :as path] (::entity/path selection)]
                          (dispatch [:set-hit-points-to-average built-template path levels class-kw]))))}
         "Average"])]
     (doall
      (map-indexed
       (fn [i cls]
         (let [level-values (by-class cls)
               total-base-hps (apply + (if (zero? i)
                                         (:hit-die first-class)
                                         0)
                                     (map :value level-values))
               num-level-values (count level-values)
               level-num (if (zero? i) (inc num-level-values) num-level-values)
               total-con-bonus (* con-bonus level-num)
               total-misc-bonus (* misc-bonus level-num)]
           ^{:key i}
           [:div.m-b-20
            [:div.f-s-16.m-l-5.f-w-b
             (str (s/capitalize (name cls))
                  " ("
                  "D"
                  (-> levels cls :hit-die) ")")]
            [:table.w-100-p.striped
             [:tbody
              hit-points-headers
              (if (zero? i)
                [:tr
                 [:td.p-5 1]
                 [:td.p-5 first-class-hit-die]
                 [:td.p-5 con-bonus-str]
                 [:td.p-5 misc-bonus-str]
                 [:td.p-5 (+ (:hit-die first-class) level-bonus)]])
              (doall
               (map-indexed
                (fn [j level-value]
                  ^{:key (:name level-value)}
                  [:tr
                   [:td.p-5 (:level level-value)]
                   [:td.p-5 [:input.input.m-t-0
                             {:type :number
                              :class-name (if (or (nil? (:value level-value))
                                                  (not (pos? (:value level-value))))
                                            "b-red b-3")
                              :on-change (fn [e]
                                           (let [value (js/parseInt (.. e -target -value))]
                                             (dispatch [:set-level-hit-points built-template character level-value value])))
                              :value (:value level-value)}]]
                   [:td.p-5 con-bonus-str]
                   [:td.p-5 misc-bonus-str]
                   [:td.p-5 (+ (:value level-value) level-bonus)]])
                level-values))
              [:tr
               [:td.p-5 "Total"]
               [:td.p-5 total-base-hps]
               [:td.p-5 (common/bonus-str total-con-bonus)]
               [:td.p-5 (common/bonus-str total-misc-bonus)]
               [:td.p-5 (+ total-base-hps total-con-bonus total-misc-bonus)]]]]]))
       classes))
     (if (> (count classes) 1)
       [:div.m-t-20
        [:div.f-s-16.m-l-5.f-w-b "Total"]
        [:table.w-100-p.striped
         [:tbody
          hit-points-headers
          [:tr
           [:td.p-5 "Total"]
           [:td.p-5 total-base-hps]
           [:td.p-5 (common/bonus-str total-con-bonus)]
           [:td.p-5 (common/bonus-str total-misc-bonus)]
           [:td.p-5 total-hps]]]]])]))

(defn sum-remaining [built-template character selections]
  (apply + (map #(Math/abs (entity/count-remaining built-template character %)) selections)))

(defn hit-points-editor [{:keys [character built-template option-paths selections]}]
  (let [num-selections (count selections)]
    (if @(subscribe [::char5e/levels])
      [selection-section-base
       {:name "Hit Points"
        :hide-lock? true
        :hide-homebrew? true
        :min (if (pos? num-selections) num-selections)
        :max (if (pos? num-selections) num-selections)
        :remaining (if (pos? num-selections) (sum-remaining built-template character selections)) 
        :body (hit-points-entry character selections built-template)}])))

(defn info-block [text]
  [:div.bg-light.b-rad-5.p-10.f-w-b.m-l-5.m-r-5.m-b-5
   text])

(defn known-mode-info []
  (let [spells-known-modes @(subscribe [::char5e/spells-known-modes])
        any-mode-class-names (into
                              []
                              (comp
                               (filter (fn [[_ mode]]
                                         (= mode :all)))
                               (map key)
                               (map (fn [nm] (str nm "s"))))
                              spells-known-modes)]
    (if (seq any-mode-class-names)
      (info-block
       (str "Except for cantrips, "
            (common/list-print any-mode-class-names)
            " do not need to select known spells since they can prepare any spell available in their class spell lists.")))))

(defn more-selection-info [key name]
  (let [selected-plugin-options @(subscribe [:selected-plugin-options])
        unselected-plugins (remove
                            (comp selected-plugin-options :key)
                            t5e/plugins)]
    (if (some
         key
         unselected-plugins)
      (info-block (str "There are more " name " options available if you click 'select sources' above and add more sources.")))))

(def pages
  [{:name "Race"
    :icon "woman-elf-face"
    :tags #{:race :subrace}
    :components [#(more-selection-info :race-options? "race")]}
   {:name "Ability Scores / Feats"
    :icon "strong"
    :tags #{:ability-scores :feats}
    :ui-fns [{:key :ability-scores :group? true :ui-fn abilities-editor}]
    :components [#(more-selection-info :feat-options? "feat")]}
   {:name "Background"
    :icon "ages"
    :tags #{:background}
    :components [#(more-selection-info :background-options? "background")]}
   {:name "Class"
    :icon "mounted-knight"
    :tags #{:class :subclass}
    :ui-fns [{:key :class :ui-fn class-levels-selector}
             {:key :hit-points :group? true :ui-fn hit-points-editor}]
    :components [#(more-selection-info :class-options? "class")]}
   {:name "Spells"
    :icon "spell-book"
    :tags #{:spells}
    :components [#(more-selection-info :spell-options? "spell")
                 known-mode-info]}
   {:name "Proficiencies"
    :icon "juggler"
    :tags #{:profs}
    ;;:ui-fns [{:key :skill-proficiency :ui-fn skills-selector}]
    }
   {:name "Equipment"
    :icon "backpack"
    :tags #{:equipment :starting-equipment}
    :ui-fns [{:key :weapons
              :hide-homebrew? true
              :ui-fn (partial inventory-selector weapon5e/weapons-map 60)}
             {:key :magic-weapons
              :hide-homebrew? true
              :ui-fn (partial inventory-selector mi5e/magic-weapon-map 60)}
             {:key :armor
              :hide-homebrew? true
              :ui-fn (partial inventory-selector armor5e/armor-map 60)}
             {:key :magic-armor
              :hide-homebrew? true
              :ui-fn (partial inventory-selector mi5e/magic-armor-map 60)}
             {:key :equipment
              :hide-homebrew? true
              :ui-fn #(inventory-selector equip5e/equipment-map 60 % ::char5e/custom-equipment)}
             {:key :other-magic-items
              :hide-homebrew? true
              :ui-fn (partial inventory-selector mi5e/other-magic-item-map 60)}
             {:key :treasure
              :hide-homebrew? true
              :ui-fn #(inventory-selector equip5e/treasure-map 100 % ::char5e/custom-treasure)}]}])

(defn section-tabs [available-selections built-template character page-index]
  (let [device-type @(subscribe [:device-type])]
    [:div.flex.justify-cont-s-a
     (doall
      (map-indexed
       (fn [i {:keys [name icon tags]}]
         (let [selections (entity/tagged-selections available-selections tags)
               combined-selections (entity/combine-selections selections)
               total-remaining (sum-remaining built-template character combined-selections)
               class-name (if (= i page-index) "selected-tab" "opacity-5 hover-opacity-full")]
           ^{:key name}
           [:div.p-5.hover-opacity-full.pointer.flex.flex-column.align-items-c
            {:class-name (if (= i page-index) "b-b-2 b-orange" "")
             :on-click (fn [_] (dispatch [:set-page i]))}
            [:div
             {:class-name class-name}
             (if (= :desktop device-type)
               [:div.f-s-10.m-b-2
                name])
             [:div.t-a-c
              (views5e/svg-icon icon 32)]]
            (if (not (= total-remaining 0))
              [:div.flex.justify-cont-end.m-t--10.p-l-20 (remaining-indicator total-remaining 12 11)])]))
       pages))]))

(defn matches-group-fn [key]
  (fn [{s-key ::t/key tags ::t/tags}]
    (let [v (or (= key s-key)
                (get tags key))]
      v)))

(defn matches-non-group-fn [key]
  (fn [{s-key ::t/key ref ::t/ref :as s}]
    (let [v (if (or (= s-key key)
                    (= ref [key]))
              s)]
      v)))

(defn option-sources []
  (let [expanded? (r/atom false)]
    (fn []
      [:div.m-b-20
       [:div.flex.align-items-c
        (views5e/svg-icon "bookshelf")
        (selection-section-title "Option Sources")
        [expand-button "collapse" "select sources" expanded?]]
       (if @expanded?
         [:div
          [option-selector-base {:name "Player's Handbook"
                                 :help (t5e/amazon-frame-help t5e/phb-amazon-frame
                                                              [:span
                                                               "Base options are from the Player's Handbook, although descriptions are either from the "
                                                               t5e/srd-link
                                                               " or are OrcPub summaries. See the Player's Handbook for in-depth, official rules and descriptions."])
                                 :selected? true
                                 :selectable? true
                                 :multiselect? true
                                 :disable-checkbox? true}]
          (doall
           (map
            (fn [option]
              (new-option-selector [(::t/key t5e/optional-content-selection)]
                                   t5e/optional-content-selection
                                   false
                                   false
                                   option))
            (::t/options t5e/optional-content-selection)))]
         [:div
          (doall
           (map-indexed
            (fn [i el]
              (with-meta el {:key i}))
            (interpose
             [:span.orange ", "]
             (map
              (fn [{:keys [name url]}]
                [:a.orange {:href url :target :_blank} name])
              (let [option-sources @(subscribe [::char5e/option-sources])]
                (cons {:name "Player's Handbook"
                       :url disp5e/phb-url}
                      (map t5e/plugin-map option-sources)))))))])])))

(def selection-order-title
  (juxt ::t/order ::t/name ::entity/path))

(defn compare-selections [s1 s2]
  (< (selection-order-title s1)
     (selection-order-title s2)))

(defn compare-paths [s1 s2]
  (< (::entity/path s1)
     (::entity/path s2)))

(defn sorted-selection-set [selections]
  (into (sorted-set-by compare-paths) selections))

(defn new-options-column [num-columns]
  (let [character @(subscribe [:character])
        built-template @(subscribe [:built-template])
        available-selections @(subscribe [:available-selections])
        _ (if print-enabled? (js/console.log "AVAILABLE SELECTIONS" available-selections))
        page @(subscribe [:page])
        page-index (or page 0)
        option-paths @(subscribe [:option-paths])
        {:keys [tags ui-fns components] :as page} (pages page-index)
        selections (entity/tagged-selections available-selections tags)
        combined-selections (entity/combine-selections selections)
        final-selections combined-selections #_(remove #(and (zero? (::t/min %))
                                       (zero? (::t/max %))
                                       (zero? (entity/count-remaining built-template character %)))
                                 combined-selections)]
    (if print-enabled? (js/console.log "FINAL SELECTIONS" final-selections))
    [:div.w-100-p
     [option-sources]
     [:div#options-column.b-1.b-rad-5
      [section-tabs available-selections built-template character page-index]
      [:div.flex.justify-cont-s-b.p-t-5.p-10.align-items-t
       [:button.form-button.p-5-10.m-r-5
        {:on-click
         (fn [_]
           (dispatch [:set-page (let [prev (dec page-index)]
                                  (if (neg? prev)
                                    (dec (count pages))
                                    prev))]))}
        "Back"]
       [:div.flex-grow-1
        [:h3.f-w-b.f-s-20.t-a-c (:name page)]]
       [:button.form-button.p-5-10.m-l-5
        {:on-click
         (fn [_]
           (dispatch [:set-page (let [next (inc page-index)]
                                  (if (>= next (count pages))
                                    0
                                    next))]))}
        "Next"]]
      (let [ui-fn-selections (mapcat
                              (fn [{:keys [key group? ui-fn]}]
                                (if group?
                                  (filter
                                   (matches-group-fn key)
                                   final-selections)
                                  [(some
                                    (matches-non-group-fn key)
                                    final-selections)]))
                              ui-fns)
            non-ui-fn-selections (sets/difference (sorted-selection-set final-selections)
                                                  (sorted-selection-set ui-fn-selections))]
        [:div.p-5
         [:div
          (doall
           (map-indexed
            (fn [i component-fn]
              ^{:key i}
              [component-fn])
            components))]
         [:div
          (doall
           (map
            (fn [{:keys [key group? ui-fn hide-homebrew?]}]
              ^{:key key}
              [:div.m-t-20
               (if group?
                 (let [group (filter
                              (matches-group-fn key)
                              final-selections)]
                   (ui-fn {:character character
                           :built-template built-template
                           :option-paths option-paths
                           :selections group}))
                 (let [selection (some
                                  (matches-non-group-fn key)
                                  final-selections)]
                   (selection-section built-template option-paths {key ui-fn} selection num-columns hide-homebrew?)))])
            ui-fns))]
         (when (seq non-ui-fn-selections)
           [:div.m-t-20
            (let [sorted-selections (into (sorted-set-by compare-selections) non-ui-fn-selections)]
              (doall
               (map
                (fn [selection]
                  ^{:key (::entity/path selection)}
                  [:div (selection-section built-template option-paths nil selection num-columns)])
                sorted-selections)))])])]]))

(def image-style
  {:max-height "100px"
   :max-width "200px"})

(defn description-fields []
  (let [entity-values @(subscribe [:entity-values])
        image-url @(subscribe [::char5e/image-url])
        image-url-failed @(subscribe [::char5e/image-url-failed])
        faction-image-url @(subscribe [::char5e/faction-image-url])
        faction-image-url-failed @(subscribe [::char5e/faction-image-url-failed])]
    [:div.flex-grow-1
     [:div.m-t-5
      [:span.personality-label.f-s-18 "Character Name"]
      [character-input entity-values ::char5e/character-name]]
     [:div.field
      [:span.personality-label.f-s-18 "Player Name"]
      [character-input entity-values ::char5e/player-name]]
     [:div.flex.justify-cont-s-b
      [:div.field.flex-grow-1.m-r-2
       [:span.personality-label.f-s-18 "Age"]
       [character-input entity-values ::char5e/age]]
      [:div.field.flex-grow-1.m-l-2.m-r-2
       [:span.personality-label.f-s-18 "Sex"]
       [character-input entity-values ::char5e/sex]]
      [:div.field.flex-grow-1.m-l-2.m-r-2
       [:span.personality-label.f-s-18 "Height"]
       [character-input entity-values ::char5e/height]]
      [:div.field.flex-grow-1.m-l-2
       [:span.personality-label.f-s-18 "Weight"]
       [character-input entity-values ::char5e/weight]]]
     [:div.flex.justify-cont-s-b
      [:div.field.flex-grow-1.m-r-2
       [:span.personality-label.f-s-18 "Hair Color"]
       [character-input entity-values ::char5e/hair]]
      [:div.field.flex-grow-1.m-1-2.m-r-2
       [:span.personality-label.f-s-18 "Eye Color"]
       [character-input entity-values ::char5e/eyes]]
      [:div.field.flex-grow-1.m-1-2
       [:span.personality-label.f-s-18 "Skin Color"]
       [character-input entity-values ::char5e/skin]]]
     [:div.field
      [:span.personality-label.f-s-18 "Personality Trait 1"]
      [character-textarea entity-values ::char5e/personality-trait-1]]
     [:div.field
      [:span.personality-label.f-s-18 "Personality Trait 2"]
      [character-textarea entity-values ::char5e/personality-trait-2]]
     [:div.field
      [:span.personality-label.f-s-18 "Ideals"]
      [character-textarea entity-values ::char5e/ideals]]
     [:div.field
      [:span.personality-label.f-s-18 "Bonds"]
      [character-textarea entity-values ::char5e/bonds]]
     [:div.field
      [:span.personality-label.f-s-18 "Flaws"]
      [character-textarea entity-values ::char5e/flaws]]
     [:div.flex.align-items-c.w-100-p.m-t-30
      (if (and image-url
               (not image-url-failed))
        [:img.m-r-10 {:src image-url
                      :on-error (fn [_] (dispatch [:failed-loading-image image-url]))
                      :on-load (fn [_] (if image-url-failed (dispatch [:loaded-image])))
               :style image-style}])
      [:div.flex-grow-1
       [:span.personality-label.f-s-18 "Image URL"]
       [character-input entity-values ::char5e/image-url nil #(dispatch [:set-image-url %])]
       (if image-url-failed
         [:div.red.m-t-5 "Image failed to load, please check the URL"])]]
     [:div.field
      [:span.personality-label.f-s-18 "Faction Name"]
      [character-input entity-values ::char5e/faction-name]]
     [:div.flex.align-items-c.w-100-p.m-t-30
      (if (and faction-image-url
               (not faction-image-url-failed))
        [:img.m-r-10 {:src faction-image-url
                      :on-error (fn [_] (dispatch [:failed-loading-faction-image faction-image-url]))
                      :on-load (fn [_] (if faction-image-url-failed
                                         (dispatch [:loaded-faction-image])))
               :style image-style}])
      [:div.flex-grow-1
       [:span.personality-label.f-s-18 "Faction Image URL"]
       [character-input entity-values ::char5e/faction-image-url nil #(dispatch [:set-faction-image-url %])]
       (if faction-image-url-failed
         [:div.red.m-t-5 "Image failed to load, please check the URL"])]]
     [:div.field
      [:span.personality-label.f-s-18 "Description/Backstory"]
      [character-textarea entity-values ::char5e/description "h-800"]]]))

#_(if image-url-failed
          [:div.p-10.red.f-s-18 (str (if (= :https image-url-failed)
                                       no-https-images
                                       "Image could not be loaded, please check the URL and try again"))]
          (let [default-image-url (default-image race classes)
                image-url? (not (s/blank? image-url))]
            (if (or default-image-url image-url?)
              [:img.character-image.w-100-p.m-b-20 {:src (if image-url?
                                                           image-url
                                                           default-image-url)
                                                    :on-error (fn [_] (dispatch [:failed-loading-image image-url]))
                                                    :on-load (fn [_] (if image-url-failed (dispatch [:loaded-image])))}]
              [:div.p-20.m-r-10.m-t-10.bg-gray.b-rad-5.t-a-c
               {:style {:border "2px solid white"
                        :background-color "rgba(255,255,255,0.1)"}}
               [:div (svg-icon "orc-head" 72 72)]
               [:div "No image set, you can set one using the 'Image URL' field in the 'Description' tab."]])))
        #_(if faction-image-url-failed
          [:div.p-10.red.f-s-18 (str (if (= :https faction-image-url-failed)
                                       no-https-images
                                       "Faction image could not be loaded, please check the URL and try again"))]
          (if (not (s/blank? faction-image-url))
            [:div.p-30 [:img.character-image.w-100-p.m-b-20 {:src faction-image-url
                                                             :on-error (fn [_] (dispatch [:failed-loading-faction-image faction-image-url]))
                                                             :on-load (fn [_] (if faction-image-url-failed (dispatch [:loaded-faction-image])))}]]))

(defn builder-tab [title key current-tab]
  [:span.builder-tab
   {:class-name (if (= current-tab key) "selected-builder-tab")
    :on-click #(dispatch [::char5e/set-builder-tab key])} title])

(defn mobile-columns []
  (let [current-tab (or @(subscribe [::char5e/builder-tab]) :options)]
    [:div.p-r-10.w-100-p
     [:div.flex-grow-1.flex.p-l-10.p-t-10
      [:div.w-100-p
       [:div.builder-tabs
        [builder-tab "Options" :options current-tab]
        [builder-tab "Description" :description current-tab]
        [builder-tab "Details" :details current-tab]]
       (case current-tab
         :options [new-options-column 1]
         :description [description-fields]
         [views5e/character-display nil true 1])]]]))


(defn desktop-or-tablet-columns [device-type]
  (let [current-tab (or @(subscribe [::char5e/builder-tab]) :options)]
    [:div.w-100-p
     [:div.flex-grow-1.flex.p-l-10.p-t-10
      [:div.w-50-p
       [:div.builder-tabs
        [builder-tab "Options" :options current-tab]
        [builder-tab "Description" :details current-tab]]
       (if (= current-tab :options)
         [new-options-column (if (= device-type :desktop) 2 1)]
         [description-fields])]
      [:div.w-50-p.m-l-20.m-r-10
       [views5e/character-display nil true 1]]]]))

(defn builder-columns []
  (let [device-type @(subscribe [:device-type])]
    (case device-type
      :mobile [mobile-columns]
      [desktop-or-tablet-columns device-type])))

(defn builder-tabs [active-tabs]
  [:div.hidden-lg.w-100-p
   [:div.builder-tabs
    [:span.builder-tab.options-tab
     {:class-name (if (active-tabs :options) "selected-builder-tab")
      :on-click (fn [_]
                  (dispatch [:set-active-tabs #{:build :options}]))} "Options"]
    [:span.builder-tab.personality-tab
     {:class-name (if (active-tabs :personality) "selected-builder-tab")
      :on-click (fn [_]
                  (dispatch [:set-active-tabs #{:build :personality}]))} "Description"]
    [:span.builder-tab.build-tab
     {:class-name (if (active-tabs :build) "selected-builder-tab")
      :on-click (fn [_]
                  (dispatch [:set-active-tabs #{:build :options}]))} "Build"]
    [:span.builder-tab.details-tab
     {:class-name (if (active-tabs :details) "selected-builder-tab")
      :on-click (fn [_]
                  (dispatch [:set-active-tabs #{:details}]))} "Details"]]])

(defn export-pdf [built-char]
  (fn [_]
    (let [field (.getElementById js/document "fields-input")]
      (aset field "value" (str (pdf-spec/make-spec built-char)))
      (.submit (.getElementById js/document "download-form")))))

(defn download-form [built-char]
  [:form.download-form
   {:id "download-form"
    :action (if (s/starts-with? js/window.location.href "http://localhost")
              "http://localhost:8890/character.pdf"
              "/character.pdf")
    :method "POST"
    :target "_blank"}
   [:input {:type "hidden" :name "body" :id "fields-input"}]])

(def patreon-link-props
  {:href "https://www.patreon.com/user?u=5892323" :target "_blank"})

(defn al-legality []
  (let [expanded? (r/atom false)]
    (fn [al-illegal-reasons used-resources]
      (let [num-resources (count (into #{} (map :resource-key used-resources)))
            multiple-resources? (> num-resources 1)
            has-homebrew? @(subscribe [:has-homebrew?])
            mobile? @(subscribe [:mobile?])
            al-legal? (and (empty? al-illegal-reasons)
                           (not multiple-resources?)
                           (not has-homebrew?))]
        [:div
         {:class-name (if (not mobile?)
                        "m-l-20 m-b-20"
                        "m-l-10")}
         [:div.flex.align-items-c
          [:div.i
           {:class-name
            (if al-legal?
              "green"
              "red")}
           [:i.fa.f-s-18
            {:class-name
             (if al-legal?
               "fa-check"
               "fa-times")}]
           [:a.m-l-5.f-w-b
            {:href "https://media.wizards.com/2016/dnd/downloads/AL_PH_SKT.pdf" :target :_blank}
            (if mobile?
              "AL"
              (str "Adventurer's League "
                   (if al-legal?
                     "Legal"
                     "Illegal")))]]
          (if (not al-legal?)
            [:span.m-l-10.f-s-14
             [expand-button "hide reasons" "show reasons" expanded?]])]
         (if (and @expanded?
                  (not al-legal?))
           [:div.i.red.m-t-5
            (map-indexed
             (fn [i reason]
               ^{:key i} [:div (str common/dot-char " " reason)])
             (cond-> al-illegal-reasons
               multiple-resources?
               (conj (str "You are only allowed to use content from one resource beyond the Player's Handbook, you are using "
                          num-resources
                          ": "
                          (s/join
                           ", "
                           (map
                            (fn [{:keys [resource-key option-name]}]
                              (str option-name " from " (:abbr (disp5e/sources resource-key))))
                            used-resources))))
               has-homebrew?
               (conj "Homebrew is not allowed")))])]))))

(defn app-header []
  [:div#app-header.app-header.flex.flex-column.justify-cont-s-b
   [:div.app-header-bar.container
    [:div.content
     [:img.orcpub-logo {:src "image/orcpub-logo.svg"}]]]
   [:div.container.header-links
    [:div.content
     [:div
      [:div.m-l-10.white.hidden-xs.hidden-sm
       [:span "Questions? Comments? Issues? Feature Requests? We'd love to hear them, "]
       [:a {:href "https://muut.com/orcpub" :target :_blank} "report them here."]]
      [:div.hidden-xs.hidden-sm
       [:div.flex.align-items-c.f-w-b.f-s-18.m-t-10.m-l-10.white
        [:span.hidden-xs "Please support continuing development on "]
        [:a.m-l-5 patreon-link-props [:span "Patreon"]]
        [:a.m-l-5 patreon-link-props
         [:img.h-32.w-32 {:src "https://www.patreon.com/images/patreon_navigation_logo_mini_orange.png"}]]]]]]]])

(def loading-style
  {:position :absolute
   :height "100%"
   :width "100%"
   :top 0
   :bottom 0
   :right 0
   :left 0
   :z-index 100
   :background-color "rgba(0,0,0,0.6)"})

(def unsaved-button-style {:background "#9a031e"})

(defn confirm-handler [character-changed? {:keys [event] :as cfg}]
  (fn [_]
    (if character-changed?
      (dispatch [:show-confirmation cfg])
      (dispatch event))))

(defn character-builder []
  (let [character @(subscribe [:character])
        _  (if print-enabled? (cljs.pprint/pprint character))
        option-paths @(subscribe [:option-paths])
        built-template @(subscribe [:built-template])
        built-char @(subscribe [:built-character])
        active-tab @(subscribe [:active-tabs])
        view-width (.-width (gdom/getViewportSize js/window))
        plugins @(subscribe [:plugins])
        mobile? @(subscribe [:mobile?])
        all-selections (entity/available-selections character built-char built-template)
        selection-validation-messages (validate-selections built-template character all-selections)
        al-illegal-reasons (concat @(subscribe [::char5e/al-illegal-reasons])
                                   selection-validation-messages)
        used-resources @(subscribe [::char5e/used-resources])
        loading @(subscribe [:loading])
        locked-components @(subscribe [:locked-components])
        character-map @(subscribe [::char5e/character-map])
        character-id (:db/id character)
        saved-character (if (and character-id
                                 character-map)
                          (character-map character-id))
        character-changed? (if character-id
                             @(subscribe [::char5e/character-changed? character-id])
                             (not= db/default-character character))]
    (if print-enabled? (print-char built-char))
    [views5e/content-page
     "Character Builder"
     [{:title "Random"
       :icon "random"
       :on-click (confirm-handler
                  character-changed?
                  {:confirm-button-text "GENERATE RANDOM CHARACTER"
                   :question "You have unsaved changes, are you sure you want to discard them and generate a random character?"
                   :event [:random-character character built-template locked-components]})}
      {:title "New"
       :icon "plus"
       :on-click (confirm-handler
                  character-changed?
                  {:confirm-button-text "CREATE NEW CHARACTER"
                   :question "You have unsaved changes, are you sure you want to discard them and create a new character?"
                   :event [:reset-character]})}
      {:title "Print"
       :icon "print"
       :on-click (export-pdf built-char)}
      {:title (if (:db/id character)
                "Update Existing Character"
                "Save New Character")
       :icon "save"
       :style (if character-changed? unsaved-button-style) 
       :on-click #(dispatch [:save-character])}]
     [:div
      [download-form]
      [:div.container
       [:div.content
        [:div.flex.justify-cont-s-b.align-items-c.flex-wrap
         [al-legality al-illegal-reasons used-resources]
         (if character-changed? [:div.red.f-w-b.m-r-10.m-l-10.flex.align-items-c
                                 (views5e/svg-icon "thunder-skull" 24 24)
                                 (if (not mobile?)
                                   [:span "You have unsaved changes"])])]]]
      [:div.flex.justify-cont-c.p-b-40
       [:div.f-s-14.white.content
        [:div.flex.w-100-p
         [builder-columns]]]]]]))

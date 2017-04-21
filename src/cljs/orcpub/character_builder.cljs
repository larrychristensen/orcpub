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
            [orcpub.dnd.e5.equipment :as equip5e]
            [orcpub.dnd.e5.skills :as skill5e]
            [orcpub.pdf-spec :as pdf-spec]

            [clojure.spec :as spec]
            [clojure.spec.test :as stest]

            [reagent.core :as r]))

(def print-enabled? false)

(declare app-state)

(defn stop-propagation [e]
  (.stopPropagation e))

(defn stop-prop-fn [func]
  (fn [e]
    (func e)
    (stop-propagation e)))

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
  (let [entity-path (entity/get-entity-path template entity path)]
    (update-in entity entity-path update-fn)))


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
    :expanded-paths {}
    :stepper-selection-path nil
    :stepper-selection nil
    :mouseover-option nil
    :builder {:character {:tab #{:build :options}}}
    :character (if stored-char stored-char t5e/character)}))

(defonce history
  (r/atom (list @app-state)))

(def template t5e/template)

(defn undo! []
  (when (> (count @history) 1)
    (swap! history pop)
    (swap! app-state #(peek @history))))

#_(add-watch app-state :log (fn [k r os ns]
                            (js/console.log "OLD" (clj->js os))
                              (js/console.log "NEW" (clj->js ns))))

(add-watch app-state
           :local-storage
           (fn [k r os ns]
             (.setItem js/window.localStorage "char-meta" (str (:character ns)))))

(add-watch app-state
           :history
           (fn [k r os ns]
             (if (not= ns (peek @history))
               (swap! history conj ns))))

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

(def builder-selector-style)

(defn add-option-button [{:keys [::t/key ::t/name ::t/options ::t/new-item-fn ::t/new-item-text] :as selection} entity path built-template]
  [:div.orange.p-5.underline.pointer
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
      (swap! app-state #(update % :character (fn [c] (remove-list-option built-template c path index)))))}])

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
          (add-option-button selection raw-char (conj path key) built-template)])])
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

(defn selector-id [path]
  (s/join "--" (map name path)))

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
   (dissoc (realize-char built-char) ::es/deps)))

(defn svg-icon [icon-name & [size]]
  (let [size (or size 32)]
    [:img
     {:class-name (str "h-" size " w-" size)
      :src (str "image/" icon-name ".svg")}]))

(defn display-section [title icon-name value & [list?]]
  [:div.m-t-20
   [:div.flex.align-items-c
    (if icon-name (svg-icon icon-name))
    [:span.m-l-5.f-s-16.f-w-600 title]]
   [:div {:class-name (if list? "m-t-0" "m-t-4")}
    [:span.f-s-24.f-w-600
     value]]])

(defn list-display-section [title image-name values]
  (if (seq values)
    (display-section
     title
     image-name
     [:span.m-t-5.f-s-14.f-w-n.i
      (s/join
       ", "
       values)]
     true)))

(defn svg-icon-section [title icon-name content]
  [:div.m-t-20
   [:span.f-s-16.f-w-600 title]
   [:div.flex.align-items-c
    (svg-icon icon-name)
    [:div.f-s-24.m-l-10.f-w-b content]]])

(defn armor-class-section [armor-class armor-class-with-armor equipped-armor]
  (let [equipped-armor-full (mi5e/equipped-armor-details equipped-armor)
        shields (filter #(= :shield (:type %)) equipped-armor-full)
        armor (filter #(not= :shield (:type %)) equipped-armor-full)
        display-rows (for [a (conj armor nil)
                           shield (conj shields nil)]
                       (let [el (if (= nil a shield) :span :div)]
                         ^{:key (common/name-to-kw (str (:name a) (:name shield)))}
                        [el
                         [el
                          [:span.f-s-24.f-w-b (armor-class-with-armor a shield)]
                          [:span.display-section-qualifier-text (str "("
                                                                     (if a (:name a) "unarmored")
                                                                     (if shield (str " & " (:name shield)))
                                                                     ")")]]]))]
    (svg-icon-section
     "Armor Class"
     "checked-shield"
     [:span
       (first display-rows)
       [:div
        (doall (rest display-rows))]])))

(defn speed-section [built-char]
  (let [speed (char5e/base-land-speed built-char)
        speed-with-armor (char5e/land-speed-with-armor built-char)
        unarmored-speed-bonus (char5e/unarmored-speed-bonus built-char)
        equipped-armor (char5e/normal-armor-inventory built-char)]
    [svg-icon-section
     "Speed"
     "walking-boot"
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
        [:div
         (doall
          (map
           (fn [[armor-kw _]]
             (let [armor (mi5e/all-armor-map armor-kw)
                   speed (speed-with-armor armor)]
               ^{:key armor-kw}
               [:div
                [:div
                 [:span speed]
                 [:span.display-section-qualifier-text (str "(" (:name armor) " armor)")]]]))
           (dissoc equipped-armor :shield)))]
        (if unarmored-speed-bonus
          [:div
           [:span
            [:span speed]
            [:span.display-section-qualifier-text "(armored)"]]]))
      (let [swim-speed (char5e/base-swimming-speed built-char)]
        (if swim-speed
          [:div [:span swim-speed] [:span.display-section-qualifier-text "(swim)"]]))
      (let [flying-speed (char5e/base-flying-speed built-char)]
        (if flying-speed
          [:div [:span flying-speed] [:span.display-section-qualifier-text "(fly)"]]))]]))

(defn list-item-section [list-name icon-name items & [name-fn]]
  [list-display-section list-name icon-name
   (map
    (fn [item]
      ((or name-fn :name) item))
    items)])

(defn compare-spell [spell-1 spell-2]
  (let [key-fn (juxt :key :ability)]
    (compare (key-fn spell-1) (key-fn spell-2))))

(defn spells-known-section [spells-known]
  [display-section "Spells Known" "spell-book"
   [:div.f-s-14
    (doall
     (map
      (fn [[level spells]]
        ^{:key level}
        [:div.m-t-10
         [:span.f-w-600 (if (zero? level) "Cantrip" (str "Level " level))]
         [:div.i.f-w-n
          (doall
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
              spells))))]])
      (filter
       (comp seq second)
       spells-known)))]])

(defn equipment-section [title icon-name equipment equipment-map]
  [list-display-section title icon-name
   (map
    (fn [[equipment-kw {item-qty :quantity equipped? :equipped? :as num}]]
      (str (disp5e/equipment-name equipment-map equipment-kw)
           " (" (or item-qty num) ")"))
    equipment)])

(defn add-links [desc]
  desc
  (let [{:keys [abbr url]} (some (fn [[_ source]]
                                   (if (and (:abbr source)
                                            (re-matches (re-pattern (str ".*" (:abbr source) ".*")) desc))
                             source))
                 disp5e/sources)
        [before after] (if abbr (s/split desc (re-pattern abbr)))]
    (if abbr
      [:span
       [:span before]
       [:a {:href url} abbr]
       [:span after]]
      desc)))

(defn attacks-section [attacks]
  (if (seq attacks)
    (display-section
     "Attacks"
     "pointy-sword"
     [:div.f-s-14
      (doall
       (map
        (fn [{:keys [name area-type description damage-die damage-die-count damage-type save save-dc] :as attack}]
          ^{:key name}
          [:p.m-t-10
           [:span.f-w-600.i name "."]
           [:span.f-w-n.m-l-10 (add-links (common/sentensize (disp5e/attack-description attack)))]])
        attacks))])))

(defn actions-section [title icon-name actions]
  (if (seq actions)
    (display-section
     title icon-name
     [:div.f-s-14
      (doall
       (map
        (fn [action]
          ^{:key action}
          [:p.m-t-10
           [:span.f-w-600.i (:name action) "."]
           [:span.f-w-n.m-l-10 (add-links (common/sentensize (disp5e/action-description action)))]])
        actions))])))

(defn prof-name [prof-map prof-kw]
  (or (-> prof-kw prof-map :name) (common/kw-to-name prof-kw)))

(defn character-display [built-char]
  (let [race (char5e/race built-char)
        subrace (char5e/subrace built-char)
        alignment (char5e/alignment built-char)
        background (char5e/background built-char)
        classes (char5e/classes built-char)
        levels (char5e/levels built-char)
        darkvision (char5e/darkvision built-char)
        skill-profs (char5e/skill-proficiencies built-char)
        tool-profs (char5e/tool-proficiencies built-char)
        weapon-profs (char5e/weapon-proficiencies built-char)
        armor-profs (char5e/armor-proficiencies built-char)
        resistances (char5e/damage-resistances built-char)
        immunities (char5e/damage-immunities built-char)
        condition-immunities (char5e/condition-immunities built-char)
        languages (char5e/languages built-char)
        abilities (char5e/ability-values built-char)
        ability-bonuses (char5e/ability-bonuses built-char)
        armor-class (char5e/base-armor-class built-char)
        armor-class-with-armor (char5e/armor-class-with-armor built-char)
        armor (char5e/normal-armor-inventory built-char)
        magic-armor (char5e/magic-armor-inventory built-char)
        spells-known (char5e/spells-known built-char)
        weapons (char5e/normal-weapons-inventory built-char)
        magic-weapons (char5e/magic-weapons-inventory built-char)
        equipment (char5e/normal-equipment-inventory built-char)
        magic-items (char5e/magical-equipment-inventory built-char)
        traits (char5e/traits built-char)
        attacks (char5e/attacks built-char)
        bonus-actions (char5e/bonus-actions built-char)
        reactions (char5e/reactions built-char)
        actions (char5e/actions built-char)]
    [:div
     [:div.f-s-24.f-w-600.m-b-16.text-shadow.flex
      [:span
       [:span race]
       [:div.f-s-12.m-t-5.opacity-6 subrace]]
      (if (seq levels)
        [:span.m-l-10.flex
         (map-indexed
          (fn [i v]
            (with-meta v {:key i}))
          (interpose
           [:span.m-l-5.m-r-5 "/"]
           (map
            (fn [cls-key]
              (let [{:keys [class-name class-level subclass]} (levels cls-key)]
                [:span
                 [:span (str class-name " (" class-level ")")]
                 [:div.f-s-12.m-t-5.opacity-6 (if subclass (common/kw-to-name subclass true))]]))
            classes)))])]
     [:div.details-columns
      [:div.flex-grow-1.flex-basis-50-p
       [:div.w-100-p.t-a-c
        [:div.flex.justify-cont-s-b.p-10
         (doall
          (map
           (fn [k]
             ^{:key k}
             [:div
              (t5e/ability-icon k 32)
              [:div.f-s-20.uppercase (name k)]
              [:div.f-s-24.f-w-b (abilities k)]
              [:div.f-s-12.opacity-5.m-b--2.m-t-2 "mod"]
              [:div.f-s-18 (common/bonus-str (ability-bonuses k))]])
           char5e/ability-keys))]
        #_[abilities-radar 187 (char5e/ability-values built-char) ability-bonuses]]
       [:div.flex
        [:div.w-50-p
         [:img.character-image.w-100-p.m-b-20 {:src (or (get-in @app-state [:character ::entity/values :image-url]) "image/barbarian-girl.png")}]]
        [:div.w-50-p
         (if background [svg-icon-section "Background" "ages" [:span.f-s-18.f-w-n background]])
         (if alignment [svg-icon-section "Alignment" "yin-yang" [:span.f-s-18.f-w-n alignment]])
         [armor-class-section armor-class armor-class-with-armor (merge magic-armor armor)]
         [svg-icon-section "Hit Points" "health-normal" (char5e/max-hit-points built-char)]
         [speed-section built-char]
         #_[display-section "Speed" nil
          (let [unarmored-speed-bonus (char5e/unarmored-speed-bonus built-char)
                speed (char5e/base-land-speed built-char)
                swim-speed (char5e/base-swimming-speed built-char)
                speed-with-armor (char5e/speed-with-armor built-char)]
            [:div
             [:div
              (if speed-with-armor
                (speed-section speed-with-armor armor)
                speed)]
             (if swim-speed
               [:div [:span swim-speed] [:span.display-section-qualifier-text "(swim)"]])])]
         [svg-icon-section "Darkvision" "night-vision" (if darkvision (str darkvision " ft.") "--")]
         [svg-icon-section "Initiative" "sprint" (mod/bonus-str (char5e/initiative built-char))]
         [display-section "Proficiency Bonus" nil (mod/bonus-str (char5e/proficiency-bonus built-char))]
         [svg-icon-section "Passive Perception" "awareness" (char5e/passive-perception built-char)]
         (let [num-attacks (char5e/number-of-attacks built-char)]
           (if (> num-attacks 1)
             [display-section "Number of Attacks" nil num-attacks]))
         (let [criticals (char5e/critical-hit-values built-char)
               min-crit (apply min criticals)
               max-crit (apply max criticals)]
           (if (not= min-crit max-crit)
             (display-section "Critical Hit" nil (str min-crit "-" max-crit))))
         [:div
          [list-display-section
           "Save Proficiencies" "dodging"
           (map (comp s/upper-case name) (char5e/saving-throws built-char))]
          (let [save-advantage (char5e/saving-throw-advantages built-char)]
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
               save-advantage))])]]]]
      [:div.flex-grow-1.flex-basis-50-p
       [list-display-section "Skill Proficiencies" "juggler"
        (let [skill-bonuses (char5e/skill-bonuses built-char)]
          (map
           (fn [[skill-kw bonus]]
             (str (s/capitalize (name skill-kw)) " " (mod/bonus-str bonus)))
           (filter (fn [[k bonus]]
                     (not= bonus (ability-bonuses (:ability (skill5e/skills-map k)))))
                   skill-bonuses)))]
       [list-item-section "Languages" "lips" languages (partial prof-name opt5e/language-map)]
       [list-item-section "Tool Proficiencies" "stone-crafting" tool-profs (partial prof-name equip5e/tools-map)]
       [list-item-section "Weapon Proficiencies" "bowman" weapon-profs (partial prof-name weapon5e/weapons-map)]
       [list-item-section "Armor Proficiencies" "mailed-fist" armor-profs (partial prof-name armor5e/armor-map)]
       [list-item-section "Damage Resistances" "surrounded-shield" resistances name]
       [list-item-section "Damage Immunities" nil immunities name]
       [list-item-section "Condition Immunities" nil condition-immunities (fn [{:keys [condition qualifier]}]
                                                                        (str (name condition)
                                                                             (if qualifier (str " (" qualifier ")"))))]
       (if (seq spells-known) [spells-known-section spells-known])
       [equipment-section "Weapons" "plain-dagger" (concat magic-weapons weapons) mi5e/all-weapons-map]
       [equipment-section "Armor" "breastplate" (merge magic-armor armor) mi5e/all-armor-map]
       [equipment-section "Equipment" "backpack" (concat magic-items
                                                         equipment
                                                         (map
                                                          (juxt :name identity)
                                                          (es/entity-val built-char :custom-equipment))) mi5e/all-equipment-map]
       [attacks-section attacks]
       [actions-section "Actions" "beams-aura" actions]
       [actions-section "Bonus Actions" "run" bonus-actions]
       [actions-section "Reactions" "van-damme-split" reactions]
       [actions-section "Features, Traits, and Feats" "vitruvian-man" traits]]]]))

(def tab-path [:builder :character :tab])

(def plugins
  [{:name "Sword Coast Adventurer's Guide"
    :key :scag
    :selections (t5e/sword-coast-adventurers-guide-selections (:character @app-state))}
   {:name "Volo's Guide to Monsters"
    :key :vgm
    :selections (t5e/volos-guide-to-monsters-selections (:character @app-state))}
   {:name "Elemental Evil Player's Companion"
    :key :ee
    :selections (t5e/elemental-evil-selections (:character @app-state))}])

(def plugins-map
  (zipmap (map :key plugins) plugins))

(def option-sources-selection
  {::t/name "Option Sources"
   ::t/optional? true
   ::t/help "Select the sources you want to use for races, classes, etc. Click the 'Show All Options' button to make additional selections. If you are new to the game we recommend just moving on to the next step."})

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
                         (not= (::path s) current-path))
                       all-selections)
        up-to-next (drop 1 up-to-current)
        next (first up-to-next)]
    (if next
      [(::path next) next]
      (let [first-selection (first all-selections)]
        [(::path first-selection) first-selection]))))

(defn next-selection [current-template-path built-template selected-option-paths character built-char]
  (let [current-path (to-option-path current-template-path built-template)
        all-selections (entity/get-all-selections [] built-template selected-option-paths built-char)]
    (selection-after current-path all-selections)))

(defn prev-selection [current-template-path built-template selected-option-paths character built-char]
  (let [current-path (to-option-path current-template-path built-template)
        all-selections (entity/get-all-selections [] built-template selected-option-paths built-char)
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

(defn set-next-template-path! [built-template next-path next-template-path next-selection character]
  (swap!
   app-state
   (fn [as]
     (-> as
         (assoc :stepper-selection-path next-template-path)
         (assoc :stepper-selection next-selection)))))

(defn set-next-selection! [built-template option-paths character stepper-selection-path all-selections built-char]
  (let [[next-path {:keys [::t/name] :as next-selection}]
        (if (nil? stepper-selection-path)
          (let [s (second all-selections)]
            [(::path s) s])
          (next-selection
           stepper-selection-path
           built-template
           option-paths
           character
           built-char))
        next-template-path (if next-path (entity/get-template-selection-path built-template next-path []))]
    (set-next-template-path! built-template next-path next-template-path next-selection character)))

(defn set-prev-selection! [built-template option-paths character stepper-selection-path all-selections built-char]
  (let [[prev-path {:keys [::t/name] :as next-selection}]
        (if (nil? stepper-selection-path)
          (let [s (last all-selections)]
            [(::path s) s])
          (prev-selection
           stepper-selection-path
           built-template
           option-paths
           character
           built-char))
        prev-template-path (if prev-path (entity/get-template-selection-path built-template prev-path []))]
    (set-next-template-path! built-template prev-path prev-template-path next-selection character)))

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
    (set-next-template-path! built-template next-path next-template-path nil character)))

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

(defn expand-button [option-path expand-text collapse-text & [handler]]
  (let [full-path [:expanded-paths option-path]
        expanded? (get-in @app-state full-path)]
    [:span.flex.pointer.align-items-c.justify-cont-end
     {:on-click (fn [e]
                  (swap! app-state update-in full-path not)
                  (if handler (handler e))
                  (.stopPropagation e))}
     [:span.underline.orange.p-0.m-r-2 (if expanded? expand-text collapse-text)]
     [:i.fa.orange
      {:class-name (if expanded? "fa-angle-up" "fa-angle-down")}]]))

(defn show-info-button [expanded? option-path]
  [:div.f-w-n.m-l-5 (expand-button option-path "hide info" "show info")])

(defn set-next! [char next-selection next-selection-path]
  (swap! app-state
         (fn [as]
           (cond-> as
             char (assoc :character char)
             next-selection-path (assoc :stepper-selection-path next-selection-path)
             next-selection (assoc :stepper-selection next-selection)))))

(defn multiselection? [{:keys [::t/multiselect? ::t/min ::t/max]}]
  (or multiselect?
      (and (> min 1)
           (= min max))
      (nil? max)))

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

(defn class-levels-selector [character {:keys [::t/options] :as selection} built-char]
  (let [selected-classes (get-in character [::entity/options :class])
        unselected-classes (remove
                            (into #{} (map ::entity/key selected-classes))
                            (map ::t/key options))
        unselected-classes-set (set unselected-classes)
        remaining-classes (filter
                           (fn [option]
                             (and
                              (unselected-classes-set (::t/key option))
                              (every?
                               (fn [prereq]
                                 ((::t/prereq-fn prereq) built-char))
                               (::t/prereqs option))))
                           options)]
    [:div
     [:div
      (doall
       (map-indexed
        (fn [i {:keys [::entity/key] :as selected-class}]
          (let [options-map (zipmap (map ::t/key options) options)
                class-template-option (options-map key)
                expanded-path [:class-levels key]
                expanded? (get-in @app-state [:expanded-paths expanded-path])]
            ^{:key key}
            [:div.m-b-5
             {:class-name (if expanded? "b-1 b-rad-5 p-5")}
             [:div.flex.align-items-c
              [:select.builder-option.builder-option-dropdown.flex-grow-1.m-t-0
               {:value key
                :on-change (fn [e] (let [new-key (keyword (.. e -target -value))]
                                     (swap! app-state
                                            (fn [s]
                                              (let [new-class-option (options-map new-key)
                                                    associated-options (::t/associated-options new-class-option)
                                                    with-new-class (assoc-in
                                                                    s
                                                                    [:character ::entity/options :class i]
                                                                    {::entity/key new-key
                                                                     ::entity/options
                                                                     {:levels [{::entity/key :level-1}]}})
                                                    without-starting-equipment (t5e/remove-starting-equipment with-new-class :class-starting-equipment)]
                                                (if (zero? i)
                                                  (t5e/add-associated-options without-starting-equipment associated-options)
                                                  with-new-class))))))}
               (doall
                (map
                 (fn [{:keys [::t/key ::t/name]}]
                   ^{:key key}
                   [:option.builder-dropdown-item
                    {:value key}
                    name])
                 (filter
                  #(and
                    (or (= key (::t/key %))
                        (unselected-classes-set (::t/key %)))
                    (or (zero? i)
                        (every? (fn [prereq] ((::t/prereq-fn prereq) built-char)) (::t/prereqs %))))
                  options)))]
              (if (::t/help class-template-option)
                (show-info-button expanded? expanded-path))
              (let [selected-levels (get-in selected-class [::entity/options :levels])
                    levels-selection (some #(if (= :levels (::t/key %)) %) (::t/selections class-template-option))
                    available-levels (::t/options levels-selection)
                    last-level-key (::entity/key (last selected-levels))]
                [:select.builder-option.builder-option-dropdown.m-t-0.m-l-5.w-80
                 {:value last-level-key
                  :on-change
                  (fn [e]
                    (let [new-highest-level-str (.. e -target -value)
                          new-highest-level (js/parseInt (last (s/split new-highest-level-str #"-")))]
                      (swap! app-state
                             update-in
                             [:character ::entity/options :class i ::entity/options :levels]
                             (fn [levels]
                               (let [current-highest-level (count levels)]
                                 (cond
                                   (> new-highest-level current-highest-level)
                                   (vec (concat levels (map
                                                        (fn [lvl] {::entity/key (keyword (str "level-" (inc lvl)))})
                                                        (range current-highest-level new-highest-level))))
                                
                                   (< new-highest-level current-highest-level)
                                   (vec (take new-highest-level levels))
                                
                                   :else levels))))))}
                 (doall
                  (map-indexed
                   (fn [i {level-key ::t/key}]
                     ^{:key level-key}
                     [:option.builder-dropdown-item
                      {:value level-key}
                      (inc i)])
                   available-levels))])
              [:i.fa.fa-minus-circle.orange.f-s-16.m-l-5.pointer
               {:on-click (fn [_] (swap! app-state
                                         update-in
                                         [:character ::entity/options :class]
                                         (fn [classes] (vec (remove #(= key (::entity/key %)) classes)))))}]]
             (if expanded?
              [:div.m-t-5.m-b-10 (::t/help class-template-option)])]))
        selected-classes))]
     (if (seq remaining-classes)
       [:div.orange.p-5.underline.pointer
        [:i.fa.fa-plus-circle.orange.f-s-16]
        [:span.m-l-5
         {:on-click
          (fn [_]
            (let [first-unselected (::t/key (first remaining-classes))]
              (swap! app-state update-in [:character ::entity/options :class] conj {::entity/key first-unselected ::entity/options {:levels [{::entity/key :level-1}]}})))}
         "Add Class"]])]))

(defn checkbox [selected? disable?]
  [:i.fa.fa-check.f-s-14.bg-white.orange-shadow.m-r-10
   {:class-name (str (if selected? "black slight-text-shadow" "transparent")
                     " "
                     (if disable?
                       "opacity-5"))}])

(defn inventory-item [{:keys [selection-key
                              item-key
                              item-name
                              item-qty
                              item-description
                              equipped?
                              expanded?
                              i
                              qty-input-width
                              check-fn
                              qty-change-fn
                              remove-fn]}]
  ^{:key item-key}
  [:div.p-5
   [:div.f-w-b.flex.align-items-c
    [:div.pointer.m-l-5
     {:on-click check-fn}
     (checkbox equipped? false)]
    [:div.flex-grow-1 item-name]
    (if item-description [:div.w-60 [show-info-button expanded? [selection-key item-key]]])
    [:input.input.m-l-5.m-t-0.
     {:class-name (str "w-" (or qty-input-width 60))
      :type :number
      :value item-qty
      :on-change qty-change-fn}]
    [:i.fa.fa-minus-circle.orange.f-s-16.m-l-5.pointer
     {:on-click remove-fn}]]
   (if expanded? [:div.m-t-5 item-description])])

(defn inventory-selector [item-map qty-input-width character {:keys [::t/key ::t/options]} & [custom-equipment-key]]
  (let [selected-items (get-in @app-state [:character ::entity/options key])
        selected-keys (into #{} (map ::entity/key selected-items))]
    [:div
     [:select.builder-option.builder-option-dropdown
      {:value ""
       :on-change
       (fn [e]
         (let [kw (keyword (.. e -target -value))]
           (swap! app-state
                  update-in
                  [:character ::entity/options key]
                  (fn [items]
                    (vec
                     (conj
                      items
                      {::entity/key kw
                       ::entity/value {:quantity 1 :equipped? false}}))))))}
      [:option.builder-dropdown-item
       {:value ""
        :disabled true}
       "<select to add>"]
      (doall
       (map
        (fn [{:keys [::t/key ::t/name]}]
          ^{:key key}
          [:option.builder-dropdown-item
           {:value key}
           name])
        (sort-by ::t/name (remove #(selected-keys (::t/key %)) options))))]
     (if (seq selected-items)
       [:div.flex.f-s-12.opacity-5.m-t-10.justify-cont-s-b
        [:div.m-r-10 "Equipped?"]
        [:div.m-r-30 "Quantity"]])
     [:div
      (doall
       (map-indexed
        (fn [i {item-key ::entity/key {item-qty :quantity equipped? :equipped? item-name :name} ::entity/value}]
          (let [item (item-map item-key)
                item-name (or item-name (:name item))
                item-description (:description item)
                expanded? (get-in @app-state [:expanded-paths [key item-key]])]
            (inventory-item {:selection-key key
                             :item-key item-key
                             :item-name item-name
                             :item-qty item-qty
                             :item-description item-description
                             :equipped? equipped?
                             :expanded? expanded?
                             :i i
                             :qty-input-width qty-input-width
                             :check-fn (fn [_]
                                         (swap! app-state
                                                update-in
                                                [:character ::entity/options key i ::entity/value :equipped?]
                                                not))
                             :qty-change-fn (fn [e] (swap! app-state
                                                           assoc-in
                                                           [:character ::entity/options key i ::entity/value]
                                                           {:equipped? equipped? :quantity (.. e -target -value)}))
                             :remove-fn (fn [_] (swap! app-state
                                                       update-in
                                                       [:character ::entity/options key]
                                                       (fn [items] (vec (remove #(= item-key (::entity/key %)) items)))))})))
        selected-items))]
     (if custom-equipment-key
       [:div
        (doall
         (map-indexed
          (fn [i {:keys [name quantity equipped?]}]
            (let [item-key (common/name-to-kw name)]
              (inventory-item {:selection-key custom-equipment-key
                               :item-key item-key
                               :item-name name
                               :item-qty quantity
                               :equipped? equipped?
                               :i i
                               :qty-input-width qty-input-width
                               :check-fn (fn [_]
                                           (swap! app-state
                                                  update-in
                                                  [:character ::entity/values custom-equipment-key i :equipped?]
                                                  not))
                               :qty-change-fn (fn [e]
                                                (swap! app-state
                                                       assoc-in
                                                       [:character ::entity/values custom-equipment-key i]
                                                       {:name name :quantity (.. e -target -value) :equipped? equipped?}))
                               :remove-fn (fn [_]
                                            (swap! app-state
                                                   update-in
                                                   [:character ::entity/values custom-equipment-key]
                                                   (fn [items]
                                                     (vec (remove #(= name (:name %)) items)))))})))
          (get-in @app-state [:character ::entity/values custom-equipment-key])))])]))

(defn option-selector-base [{:keys [name key help selected? selectable? option-path select-fn content explanation-text icon classes multiselect? disable-checkbox?]}]
  (let [expanded? (get-in @app-state [:expanded-paths option-path])]
    ^{:key key}
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
          (checkbox selected? disable-checkbox?))
        (if icon [:div.m-r-5 (svg-icon icon 24)])
        [:span.f-w-b.f-s-1.flex-grow-1 name]
        (if help
          [show-info-button expanded? option-path])]
       (if (and help expanded?)
         [help-section help])
       (if (and content selected?)
         content)
       (if explanation-text
         [:div.i.f-s-12.f-w-n 
          explanation-text])]]]))

(defn skill-help [name key ability icon description]
  [:div
   [:div.flex.align-items-c
    (svg-icon icon 48)
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

(defn actual-path [{:keys [::t/ref ::entity/path] :as selection}]
  (if ref (if (sequential? ref) ref [ref]) path))

(defn count-remaining [built-template character {:keys [::t/min ::t/max ::t/require-value?] :as selection}]
  (let [actual-path (actual-path selection)
        entity-path (entity/get-entity-path built-template character actual-path)
        selected-options (get-in character entity-path)
        selected-count (cond
                         (sequential? selected-options)
                         (count (if require-value?
                                  (filter ::entity/value selected-options)
                                  selected-options))
                         
                         (map? selected-options)
                         (if (or (::entity/value selected-options)
                                 (not require-value?))
                           1
                           0)
                         
                         :else 0)]
    (cond (< selected-count min)
          (- min selected-count)

          (and max (> selected-count max))
          (- max selected-count)

          :else
          0)))


(defn new-option-selector [character built-char built-template option-paths option-path
                           {:keys [::t/min ::t/max ::t/options ::t/multiselect?] :as selection}
                           disable-select-new?
                           {:keys [::t/key ::t/name ::t/path ::t/help ::t/selections ::t/prereqs
                                   ::t/modifiers ::t/select-fn ::t/ui-fn ::t/icon] :as option}]
  (let [new-option-path (conj option-path key)
        selected? (get-in option-paths new-option-path)
        failed-prereqs (reduce
                        (fn [failures {:keys [::t/prereq-fn ::t/label ::t/hide-if-fail?] :as prereq}]
                          (if (and prereq-fn (not (prereq-fn built-char)))
                            (conj failures prereq)
                            failures))
                        []
                        prereqs)
        meets-prereqs? (empty? failed-prereqs)
        selectable? (and (or selected?
                             meets-prereqs?)
                         (or (not disable-select-new?)
                             selected?))
        expanded? (get-in @app-state [:expanded-paths new-option-path])
        has-selections? (seq selections)
        named-modifiers (map (fn [{:keys [::mod/name ::mod/value]}]
                               (str name " " value))
                             (filter ::mod/name (flatten modifiers)))
        has-named-mods? (seq named-modifiers)
        modifiers-str (s/join ", " named-modifiers)]
    (if (not-any? ::t/hide-if-fail? failed-prereqs)
      (option-selector-base {:name name
                             :key key
                             :help (if (or help has-named-mods?)
                                     [:div
                                      (if has-named-mods? [:div.i modifiers-str])
                                      [:div {:class-name (if has-named-mods? "m-t-5")} help]])
                             :selected? selected?
                             :selectable? selectable?
                             :option-path new-option-path
                             :multiselect? (or multiselect?
                                               (> min 1)
                                               (nil? max))
                             :select-fn (fn [e]
                                          (when (and (or (> max 1)
                                                         (nil? max)
                                                         multiselect?
                                                         (not selected?)
                                                         has-selections?)
                                                     (or selected?
                                                         meets-prereqs?)
                                                     selectable?)
                                            (let [updated-char (let [new-option {::entity/key key}]
                                                                 (if (or
                                                                      multiselect?
                                                                      (nil? max)
                                                                      (and
                                                                       (> min 1)
                                                                       (= min max)))
                                                                   (update-option
                                                                    built-template
                                                                    character
                                                                    option-path
                                                                    (fn [parent-vec]
                                                                      (if (or (nil? parent-vec) (map? parent-vec))
                                                                        [new-option]
                                                                        (let [parent-keys (into #{} (map ::entity/key) parent-vec)]
                                                                          (if (parent-keys key)
                                                                            (vec (remove #(= key (::entity/key %)) parent-vec))
                                                                            (conj parent-vec new-option))))))
                                                                   (update-option
                                                                    built-template
                                                                    character
                                                                    new-option-path
                                                                    (fn [o] (if multiselect? [new-option] new-option)))))]
                                              (swap! app-state assoc :character updated-char))
                                            (if select-fn
                                              (select-fn (entity/get-option-value-path
                                                          built-template
                                                          (:character @app-state)
                                                          new-option-path)
                                                         app-state)))
                                          (.stopPropagation e))
                             :content (if (and (or selected? (= 1 (count options))) ui-fn)
                                        (ui-fn new-option-path built-template app-state built-char))
                             :explanation-text (let [explanation-text (if (and (not meets-prereqs?)
                                                                               (not selected?))
                                                                        (s/join ", " (map ::t/label failed-prereqs)))]                      
                                                 explanation-text)
                             :icon icon}))))

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

(defn selection-section-base [{:keys [path parent-title name icon help max min remaining body]}]
  (let [expanded? (and path (get-in @app-state [:expanded-paths path]))]
    ^{:key (keyword (s/join "-" (map clojure.core/name path)))}
    [:div.p-5.m-b-20.m-b-0-last
     (if parent-title
       (selection-section-parent-title parent-title))
     [:div.flex.align-items-c
      (if icon (svg-icon icon 24))
      (selection-section-title name)
      (if (and path help)
        [show-info-button expanded? path])]
     (if (and help expanded?)
       [help-section help])
     (if (or (and (pos? min)
                  (nil? max))
             (= min max))
       (if (int? min)
         [:div.p-5.f-s-16
          [:div.flex.align-items-c.justify-cont-s-b
           [:span.i.m-r-10 (str "select " (if (= min max)
                                            min
                                            (str "at least " min)))]
           (remaining-component max remaining)]]))
     body]))

(defn ancestor-names-string [built-template path]
  (let [ancestor-paths (reductions conj [] path)
        ancestors (map (fn [a-p]
                         (get-in built-template
                                 (entity/get-template-selection-path built-template a-p [])))
                       (take-nth 2 (butlast ancestor-paths)))
        ancestor-names (map ::t/name (remove nil? ancestors))]
    (s/join " - " ancestor-names)))

(defn selection-section [character built-char built-template option-paths ui-fns {:keys [::t/key ::t/name ::t/help ::t/options ::t/min ::t/max ::t/ref ::t/icon ::t/multiselect? ::entity/path ::entity/parent] :as selection}]
  (let [actual-path (actual-path selection)
        remaining (count-remaining built-template character selection)
        expanded? (get-in @app-state [:expanded-paths actual-path])]
    (selection-section-base {:path actual-path
                             :parent-title (if parent (ancestor-names-string built-template actual-path))
                             :name name
                             :icon icon
                             :help help
                             :max max
                             :min min
                             :remaining remaining
                             :body (if (and ui-fns (ui-fns (or ref key)))
                                     ((ui-fns (or ref key)) character selection built-char)
                                     (doall
                                      (map
                                       (fn [option]
                                         (new-option-selector character
                                                              built-char
                                                              built-template
                                                              option-paths
                                                              actual-path
                                                              selection
                                                              (and (or (and max (> min 1))
                                                                       multiselect?)
                                                                   (not (pos? remaining))) option))
                                       (sort-by ::t/name options))))})))

(defn set-ability! [app-state ability-key ability-value]
  (swap! app-state
         assoc-in
         [:character ::entity/options :ability-scores ::entity/value ability-key]
         ability-value))

(defn set-abilities! [app-state abilities]
  (swap! app-state assoc-in [:character ::entity/options :ability-scores ::entity/value] abilities))

(defn reroll-abilities [app-state]
  (set-abilities! app-state (char5e/standard-ability-rolls)))

(defn set-standard-abilities [app-state]
  (fn []
    (set-abilities! app-state (char5e/abilities 15 14 13 12 10 8))))

(defn reset-point-buy-abilities [app-state]
  (fn []
    (set-abilities! app-state (char5e/abilities 8 8 8 8 8 8))))

(defn swap-abilities [app-state i other-i k v]
  (stop-prop-fn
   (fn []
     (swap! app-state
            update-in
            [:character ::entity/options :ability-scores ::entity/value]
            (fn [a]
              (let [a-vec (vec a)
                    other-index (mod other-i (count a-vec))
                    [other-k other-v] (a-vec other-index)]
                (assoc a k other-v other-k v)))))))

(def ability-icons
  {:str "strong"
   :con "caduceus"
   :dex "body-balance"
   :int "read"
   :wis "meditation"
   :cha "aura"})

(defn ability-icon [k size]
  [:img {:class-name (str "h-" size " w-" size)
         :src (str "image/" (ability-icons k) ".svg")}])

(defn ability-subtitle [title]
  [:div.t-a-c.f-s-10.opacity-5 title])

(defn ability-modifier [v]
  [:div.f-6-12.f-w-n.h-24
   (ability-subtitle "mod")
   [:div.m-t--1
    (opt5e/ability-bonus-str v)]])

(defn ability-component [k v i app-state controls]
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

(defn ability-increases-component [app-state built-char built-template asi-selections ability-keys]
  (let [total-abilities (es/entity-val built-char :abilities)]
    [:div
     (doall
      (map-indexed
       (fn [i {:keys [::t/name ::t/key ::t/min ::t/options ::t/different? ::entity/path] :as selection}]
         (let [increases-path (entity/get-entity-path built-template (:character @app-state) path)
               full-path (concat [:character] increases-path)
               selected-options (get-in @app-state full-path)
               ability-increases (frequencies (map ::entity/key selected-options))
               num-increased (apply + (vals ability-increases))
               num-remaining (- min num-increased)
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
                                              (zero? num-remaining)
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
                                      (swap! app-state
                                             update-in
                                             full-path
                                             (fn [incs]
                                               (common/remove-first
                                                (fn [{inc-key ::entity/key}]
                                                  (= inc-key k))
                                                incs))))))}]
                     [:i.fa.fa-plus-circle.orange.m-l-5
                      {:class-name (if increase-disabled? "opacity-5 cursor-disabled")
                       :on-click (stop-prop-fn
                                  (fn []
                                    (if (not increase-disabled?)
                                      (swap! app-state
                                             update-in
                                             full-path
                                             conj
                                             {::entity/key k}))))}]]]))
               ability-keys))]]))
       asi-selections))]))

(defn race-abilities-component [built-char ability-keys]
  (let [race-ability-increases (es/entity-val built-char :race-ability-increases)
        subrace-ability-increases (es/entity-val built-char :subrace-ability-increases)
        total-abilities (es/entity-val built-char :abilities)]
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
                (ability-value subrace-v)])])])
       ability-keys))]))

(defn abilities-matrix-footer [built-char ability-keys]
  (let [total-abilities (es/entity-val built-char :abilities)]
    [:div
     (race-abilities-component built-char ability-keys)
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
        (ability-icon k 24)
        [:div.uppercase (name k)]
        (ability-subtitle "base")])
     ability-keys))])

(defn abilities-component [app-state
                           built-char
                           built-template
                           asi-selections
                           content]
  (let [total-abilities (es/entity-val built-char :abilities)]
    [:div
     (abilities-header char5e/ability-keys)
     content
     (ability-increases-component app-state built-char built-template asi-selections char5e/ability-keys)
     (abilities-matrix-footer built-char char5e/ability-keys)]))


(defn point-buy-abilities [app-state built-char built-template asi-selections]
  (let [default-base-abilities (char5e/abilities 8 8 8 8 8 8)
        abilities (or (opt5e/get-raw-abilities app-state)
                      default-base-abilities)
        points-used (apply + (map (comp score-costs second) abilities))
        points-remaining (- point-buy-points points-used)
        total-abilities (es/entity-val built-char :abilities)]
    (abilities-component
     app-state
     built-char
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
        (map-indexed
         (fn [i [k v]]
           (let [increase-disabled? (or (zero? points-remaining)
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
                                (set-abilities! app-state (update abilities k dec)))))}]
               [:i.fa.fa-plus-circle.orange.m-l-5
                {:class-name (if increase-disabled? "opacity-5 cursor-disabled")
                 :on-click (stop-prop-fn
                            (fn [_]
                              (if (not increase-disabled?) (set-abilities! app-state (update abilities k inc)))))}]]]))
         abilities))]])))

(defn abilities-standard [app-state]
  [:div.flex.justify-cont-s-a
   (let [abilities (or (opt5e/get-raw-abilities app-state)
                       (char5e/abilities 15 14 13 12 10 8))
          abilities-vec (vec abilities)]
      (doall
       (map-indexed
        (fn [i [k v]]
          ^{:key k}
          [ability-component k v i app-state
           [:div.f-s-16
            [:i.fa.fa-chevron-circle-left.orange
             {:on-click (swap-abilities app-state i (dec i) k v)}]
            [:i.fa.fa-chevron-circle-right.orange.m-l-5
             {:on-click (swap-abilities app-state i (inc i) k v)}]]])
        abilities-vec)))])

(defn abilities-roller [app-state built-char built-template asi-selections]
  (abilities-component
   app-state
   built-char
   built-template
   asi-selections
   [:div
    (abilities-standard app-state)
    [:button.form-button.m-t-5
     {:on-click (stop-prop-fn
                 (fn [e]
                   (reroll-abilities app-state)))}
     "Re-Roll"]]))

(defn abilities-standard-editor [app-state built-char built-template asi-selections]
  (abilities-component
   app-state
   built-char
   built-template
   asi-selections
   (abilities-standard app-state)))

(defn abilities-entry [app-state built-char built-template asi-selections]
  (let [abilities (or (opt5e/get-raw-abilities app-state)
                      (char5e/abilities 15 14 13 12 10 8))
        total-abilities (es/entity-val built-char :abilities)]
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
                                  (swap! app-state assoc-in [:character ::entity/options :ability-scores ::entity/value k] new-v)))}]])
        char5e/ability-keys))]
     (ability-increases-component app-state built-char built-template asi-selections char5e/ability-keys)
     (race-abilities-component built-char char5e/ability-keys)
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
                                  (swap! app-state assoc-in [:character ::entity/options :ability-scores ::entity/value k] new-v)))}]])
        total-abilities))]]))

(defn ability-variant-option-selector [name key selected-key content & [select-fn]]
  (option-selector-base
   {:name name
    :key key
    :selected? (= selected-key key)
    :selectable? true
    :option-path [:ability-scores key]
    :content content
    :select-fn (fn [_]
                 (when (not= selected-key key)
                   (if select-fn (select-fn))
                   (swap! app-state assoc-in [:character ::entity/options :ability-scores ::entity/key] key)))}))

(defn abilities-editor [character built-char built-template option-paths selections]
  [:div
   [:div.m-l-5 (selection-section-title "Ability Score Improvements")]
   (doall
    (map
     (fn [{:keys [::t/key ::t/min ::t/max ::t/options ::entity/path] :as selection}]
       (let [remaining (count-remaining built-template character selection)]
         (selection-section-base
          {:path path
           :parent-title (ancestor-names-string built-template path)
           :max 1
           :min 1
           :remaining remaining
           :body (doall
                  (map
                   (fn [option]
                     (new-option-selector character built-char built-template option-paths path selection (and max (> min 1) (zero? remaining)) option))
                   (sort-by ::t/name options)))})))
     (filter
      (fn [s]
        (= :asi-or-feat (::t/key s)))
      selections)))
   (let [asi-selections (filter (fn [s] (= :asi (::t/key s))) selections)
         selected-variant (get-in @app-state [:character ::entity/options :ability-scores ::entity/key])]
     (selection-section-base
      {:path [:ability-scores]
       :name "Abilities Variant"
       :min 1
       :max 1
       :body [:div
              (ability-variant-option-selector
               "Point Buy"
               :point-buy
               selected-variant
               (point-buy-abilities app-state built-char built-template asi-selections)
               #(set-abilities! app-state (char5e/abilities 8 8 8 8 8 8)))
              (ability-variant-option-selector
               "Dice Roll"
               :standard-roll
               selected-variant
               (abilities-roller app-state built-char built-template asi-selections)
               #(reroll-abilities app-state))
              (ability-variant-option-selector
               "Standard Scores"
               :standard-scores
               selected-variant
               (abilities-standard-editor app-state built-char built-template asi-selections)
               #(set-abilities! app-state (char5e/abilities 15 14 13 12 10 8)))
              (ability-variant-option-selector
               "Manual Entry"
               :manual-entry
               selected-variant
               (abilities-entry app-state built-char built-template asi-selections))]}))])

(defn skills-selector [character {:keys [::t/ref ::t/max ::t/options]} built-char]
  (let [path [:character ::entity/options ref]
        selected-skills (get-in @app-state path)
        selected-count (count selected-skills)
        remaining (- max selected-count)
        available-skills (into #{} (map ::t/key options))
        selected-skill-keys (into #{} (map ::entity/key selected-skills))]
    (doall
     (map
      (fn [{:keys [name key ability icon description]}]
        (let [skill-profs (es/entity-val built-char :skill-profs)
              has-prof? (and skill-profs (skill-profs key))
              selected? (selected-skill-keys key)
              selectable? (available-skills key)
              bad-selection? (and selected? (not selectable?))
              allow-select? (or selected?
                                (and (not selected?)
                                     (pos? remaining)
                                     selectable?
                                     (not has-prof?))
                                bad-selection?)]
          (option-selector-base {:name name
                                 :key key
                                 :help (skill-help name key ability icon description)
                                 :selected? selected?
                                 :selectable? allow-select?
                                 :option-path [:skill-profs key]
                                 :select-fn (fn [_]
                                              (if allow-select?
                                                (swap! app-state
                                                       update-in
                                                       path
                                                       (fn [skills]
                                                         (if selected?                                             
                                                           (vec (remove (fn [s] (= key (::entity/key s))) skills))
                                                           (vec (conj skills {::entity/key key})))))))
                                 :explanation-text (if (and has-prof?
                                                            (not selected?))
                                                     "You already have this skill proficiency")
                                 :icon icon
                                 :classes (if bad-selection? "b-red")
                                 :multiselect? true})))
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

(defn hit-points-entry [character selections built-char built-template]
  (let [classes (es/entity-val built-char :classes)
        levels (es/entity-val built-char :levels)
        first-class (if levels (levels (first classes)))
        first-class-hit-die (:hit-die first-class)
        level-bonus (es/entity-val built-char :hit-point-level-bonus)
        con-bonus (:con (es/entity-val built-char :ability-bonuses))
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
                             (let [entity-path (entity/get-entity-path built-template character (::entity/path selection))
                                   full-path (concat [:character] entity-path)]                                         
                               (swap! app-state
                                      assoc-in
                                      full-path
                                      {::entity/key :manual-entry
                                       ::entity/value (if (= first-selection selection)
                                                        (+ average-value remainder)
                                                        average-value)})))))}]
          total-hps)]]
      [:button.form-button.p-10
       {:on-click (fn [_]
                    (doseq [selection selections]
                      (let [[_ class-kw :as path] (::entity/path selection)]
                        (swap! app-state
                               assoc-in
                               (concat [:character] (entity/get-entity-path built-template character path))
                               {::entity/key :roll
                                ::entity/value (dice/die-roll (-> levels class-kw :hit-die))}))))}
       "Random"]
      [:button.form-button.p-10
       {:on-click (fn [_]
                    (doseq [selection selections]
                      (let [[_ class-kw :as path] (::entity/path selection)]
                        (swap! app-state
                               assoc-in
                               (concat [:character] (entity/get-entity-path built-template character path))
                               {::entity/key :average
                                ::entity/value (dice/die-mean (-> levels class-kw :hit-die))}))))}
       "Average"]]
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
                                             (swap! app-state
                                                    assoc-in
                                                    (concat [:character] (entity/get-entity-path built-template character (:path level-value)))
                                                    {::entity/key :manual-entry
                                                     ::entity/value (if (not (js/isNaN value)) value)})))
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
  (apply + (map #(Math/abs (count-remaining built-template character %)) selections)))

(defn hit-points-editor [character built-char built-template option-paths selections]
  (let [num-selections (count selections)]
    (if (es/entity-val built-char :levels)
      (selection-section-base
       {:name "Hit Points"
        :min num-selections
        :max num-selections
        :remaining (sum-remaining built-template character selections) 
        :body (hit-points-entry character selections built-char built-template)}))))

(def pages
  [{:name "Race"
    :icon "woman-elf-face"
    :tags #{:race :subrace}}
   {:name "Ability Scores / Feats"
    :icon "strong"
    :tags #{:ability-scores :feats}
    :ui-fns [{:key :ability-scores :group? true :ui-fn abilities-editor}]
    ;;:group-ui-fns {:ability-scores abilities-editor}
    }
   {:name "Background"
    :icon "ages"
    :tags #{:background}}
   {:name "Class"
    :icon "mounted-knight"
    :tags #{:class :subclass}
    :ui-fns [{:key :class :ui-fn class-levels-selector}
             {:key :hit-points :group? true :ui-fn hit-points-editor}]
    ;;:ui-fns {:class class-levels-selector}
    ;;:group-ui-fns {:hit-points hit-points-editor}
    }
   {:name "Spells"
    :icon "spell-book"
    :tags #{:spells}}
   {:name "Proficiencies"
    :icon "juggler"
    :tags #{:profs}
    :ui-fns [{:key :skill-profs :ui-fn skills-selector}]}
   {:name "Equipment"
    :icon "backpack"
    :tags #{:equipment :starting-equipment}
    :ui-fns [{:key :weapons :ui-fn (partial inventory-selector weapon5e/weapons-map 60)}
             {:key :magic-weapons :ui-fn (partial inventory-selector mi5e/magic-weapon-map 60)}
             {:key :armor :ui-fn (partial inventory-selector armor5e/armor-map 60)}
             {:key :magic-armor :ui-fn (partial inventory-selector mi5e/magic-armor-map 60)}
             {:key :equipment :ui-fn #(inventory-selector equip5e/equipment-map 60 % %2 :custom-equipment)}
             {:key :other-magic-items :ui-fn (partial inventory-selector mi5e/other-magic-item-map 60)}
             {:key :treasure :ui-fn (partial inventory-selector equip5e/treasure-map 100)}]}])

(defn section-tabs [available-selections built-template character page-index]
  [:div.flex.justify-cont-s-a
   (doall
    (map-indexed
     (fn [i {:keys [name icon tags]}]
       (let [selections (entity/tagged-selections available-selections tags)
             combined-selections (entity/combine-selections selections)
             total-remaining (sum-remaining built-template character combined-selections)]
         ^{:key name}
         [:div.p-5.hover-opacity-full.pointer
          {:class-name (if (= i page-index) "b-b-2 b-orange" "")
           :on-click (fn [_] (swap! app-state assoc :page i))}
          [:div
           {:class-name (if (= i page-index) "selected-tab" "opacity-5 hover-opacity-full")}
           (svg-icon icon 32)]
          (if (not (= total-remaining 0))
            [:div.flex.justify-cont-end.m-t--10 (remaining-indicator total-remaining 12 11)])]))
     pages))])

(defn matches-group-fn [key]
  (fn [{s-key ::t/key tags ::t/tags}]
    (or (= key s-key)
        (get tags key))))

(defn matches-non-group-fn [key]
  #(if (or (= (::t/key %) key)
           (= (::t/ref %) key)) %))

(defn get-selected-plugin-options [app-state]
  (into #{}
        (comp (map ::entity/key)
              (remove nil?))
        (get-in @app-state [:character ::entity/options :optional-content])))


(defn new-options-column [character built-char built-template available-selections page-index option-paths stepper-selection-path]
  (if print-enabled? (js/console.log "AVAILABLE SELECTIONS" available-selections))
  (let [{:keys [tags ui-fns] :as page} (pages page-index)
        selections (entity/tagged-selections available-selections tags)
        combined-selections (entity/combine-selections selections)
        final-selections (remove #(and (zero? (::t/min %))
                                       (zero? (::t/max %))
                                       (zero? (count-remaining built-template character %)))
                                 combined-selections)]
    (if print-enabled? (js/console.log "FINAL SELECTIONS" (vec final-selections) (map ::t/key final-selections)))
    [:div.w-100-p
     [:div.m-b-20
      [:div.flex.align-items-c
       (svg-icon "bookshelf")
       (selection-section-title "Option Sources")
       (expand-button [:option-sources] "collapse" "select sources")]
      (if (get-in @app-state [:expanded-paths [:option-sources]])
        [:div
         (option-selector-base {:name "Player's Handbook"
                                :help (t5e/amazon-frame-help t5e/phb-amazon-frame
                                                         [:span
                                                          "Base options are from the Player's Handbook, although descriptions are either from the "
                                                          t5e/srd-link
                                                          " or are OrcPub summaries. See the Player's Handbook for in-depth, official rules and descriptions."])
                                :selected? true
                                :selectable? true
                                :multiselect? true
                                :disable-checkbox? true})
         (doall
          (map
           (fn [option]
             (new-option-selector character built-char built-template option-paths [(::t/key t5e/optional-content-selection)]
                                  t5e/optional-content-selection
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
               [:a.orange {:href url} name])
             (cons {:name "Player's Handbook"
                    :url disp5e/phb-url}
                   (map t5e/plugin-map (get-selected-plugin-options app-state)))))))])]
     
     [:div#options-column.b-1.b-rad-5
      [section-tabs available-selections built-template character page-index]
      [:div.flex.justify-cont-s-b.p-t-5.p-10.align-items-t
       [:button.form-button.p-5-10.m-r-5
        {:on-click
         (fn [_] (swap! app-state assoc :page (let [prev (dec page-index)]
                                                (if (neg? prev)
                                                  (dec (count pages))
                                                  prev))))}
        "Back"]
       [:div.flex-grow-1
        [:h3.f-w-b.f-s-20.t-a-c (:name page)]]
       [:button.form-button.p-5-10.m-l-5
        {:on-click
         (fn [_] (swap! app-state assoc :page (let [next (inc page-index)]
                                                (if (>= next (count pages))
                                                  0
                                                  next))))}
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
            non-ui-fn-selections (sets/difference (set final-selections) (set ui-fn-selections))]
        [:div.p-5
         [:div
          (doall
           (map
            (fn [{:keys [key group? ui-fn]}]
              ^{:key key}
              [:div.m-t-20
               (if group?
                 (let [group (filter
                              (matches-group-fn key)
                              final-selections)]
                   (ui-fn character built-char built-template option-paths group))
                 (let [selection (some
                                  (matches-non-group-fn key)
                                  final-selections)]
                   (selection-section character built-char built-template option-paths {key ui-fn} selection)))])
            ui-fns))]
         (if (seq non-ui-fn-selections)
           [:div.m-t-20
            (doall
             (map
              (fn [selection]
                (selection-section character built-char built-template option-paths nil selection))
              non-ui-fn-selections))])])]]))


(defn builder-columns [built-template built-char option-paths collapsed-paths stepper-selection-path stepper-selection plugins active-tabs stepper-dismissed? available-selections]
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
    [new-options-column (:character @app-state) built-char built-template available-selections (or (:page @app-state) 0) option-paths stepper-selection-path]]
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
      :on-click (fn [_] (swap! app-state assoc-in tab-path #{:build :personality}))} "Description"]
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
  [:form.download-form
   {:id "download-form"
    :action (if (.startsWith js/window.location.href "http://localhost")
              "http://localhost:8890/character.pdf"
              "/character.pdf")
    :method "POST"
    :target "_blank"}
   [:input {:type "hidden" :name "body" :id "fields-input"}]])

(defn header [built-char]
  [:div.w-100-p
   [:div.flex.align-items-c.justify-cont-s-b
    [:h1.f-s-36.f-w-b.m-t-21.m-b-19.m-l-10 "Character Builder"]
    [:div.flex.align-items-c.justify-cont-end.flex-wrap.m-r-10
     [:button.form-button.h-40.m-l-5.m-t-5.m-b-5
      {:class-name (if (<= (count @history) 1) "opacity-5")
       :on-click undo!}
      [:i.fa.fa-undo.f-s-18]
      [:span.m-l-5.hidden-sm.hidden-xs.hidden-md "Undo"]]
     [:button.form-button.h-40.m-l-5.m-t-5.m-b-5
      {:on-click (fn [_] (swap! app-state assoc :character t5e/character :page 0))}
      [:span
       [:i.fa.fa-undo.f-s-18]
       [:i.fa.fa-undo.f-s-18]]
      [:span.m-l-5.hidden-sm.hidden-xs.hidden-md "Reset"]]
     [:button.form-button.h-40.m-l-5.m-t-5.m-b-5
      {:on-click (export-pdf built-char)}
      [:i.fa.fa-print.f-s-18]
      [:span.m-l-5.hidden-sm.hidden-xs.hidden-md "Print"]]
     [:button.form-button.h-40.m-l-5.m-t-5.m-b-5
      [:i.fa.fa-floppy-o.f-s-18]
      [:span.m-l-5.hidden-sm.hidden-xs.hidden-md "Browser Save"]]
     [:button.form-button.h-40.m-l-5.opacity-5.m-t-5.m-b-5
      [:i.fa.fa-cloud-upload.f-s-18]
      [:span.m-l-5.hidden-sm.hidden-xs.hidden-md "Save" [:span.i.m-l-5 "(Coming Soon)"]]]]]])


(defn character-builder []
  (if print-enabled? (cljs.pprint/pprint (:character @app-state)))
  ;;(js/console.log "APP STATE" @app-state)
  (let [selected-plugin-options (get-selected-plugin-options app-state)
        selected-plugins (map
                          :selections
                          (filter
                           (fn [{:keys [key]}]
                             (selected-plugin-options key))
                           plugins))
        merged-template (if (seq selected-plugins)
                          (update template
                                  ::t/selections
                                  (fn [s]
                                    (apply
                                     entity/merge-multiple-selections
                                     s
                                     selected-plugins)))
                          template)
        option-paths (entity/make-path-map (:character @app-state))
        built-template merged-template ;;(entity/build-template (:character @app-state) merged-template)
        built-char (entity/build (:character @app-state) built-template)
        active-tab (get-in @app-state tab-path)
        view-width (.-width (gdom/getViewportSize js/window))
        stepper-selection-path (:stepper-selection-path @app-state)
        stepper-selection (:stepper-selection @app-state)
        collapsed-paths (:collapsed-paths @app-state)
        mouseover-option (:mouseover-option @app-state)
        plugins (:plugins @app-state)
        stepper-dismissed? (:stepper-dismissed @app-state)
        all-selections (entity/available-selections (:character @app-state) built-char built-template)
        al-illegal-reasons (es/entity-val built-char :al-illegal-reasons)
        used-resources (es/entity-val built-char :used-resources)
        num-resources (count used-resources)
        multiple-resources? (> num-resources 1)
        al-legal? (and (empty? al-illegal-reasons)
                       (not multiple-resources?))
        al-illegal-reasons-path [:al-illegal-reasons]]
    (if print-enabled? (print-char built-char))
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
     [:div.container
      [:div.content
       [:div.m-l-20.m-b-20
        [:div.flex
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
           {:href "https://media.wizards.com/2016/dnd/downloads/AL_PH_SKT.pdf"}
           (str "Adventurer's League "
                (if al-legal?
                  "Legal"
                  "Illegal"))]]
         (if (not al-legal?)
           [:span.m-l-10.f-s-14
            (expand-button al-illegal-reasons-path "hide reasons" "show reasons")])]
        (if (and (get-in @app-state [:expanded-paths al-illegal-reasons-path])
                 (not al-legal?))
          [:div.i.red.m-t-5
           (map-indexed
            (fn [i reason]
              ^{:key i} [:div (str common/dot-char " " reason)])
            (if multiple-resources?
              (conj al-illegal-reasons
                    (str "You are only allowed to use content from one resource beyond the Player's Handbook, you are using "
                         num-resources
                         ": "
                         (s/join
                          ", "
                          (map
                           (fn [{:keys [resource-key option-name]}]
                             (str option-name " from " (:abbr (disp5e/sources resource-key))))
                           used-resources))))
              al-illegal-reasons))])]]]
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
         stepper-selection
         plugins
         active-tab
         stepper-dismissed?
         all-selections]]]]
     [:div.white.flex.justify-cont-c
      [:div.content.f-w-n.f-s-12
       [:div.p-10
        [:div.m-b-5 "Icons made by Lorc, Caduceus, and Delapouite. Available on " [:a.orange {:href "http://game-icons.net"} "http://game-icons.net"]]]]]]))

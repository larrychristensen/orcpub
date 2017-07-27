(ns orcpub.views-aux
  (:require [orcpub.template :as t]
            [orcpub.modifiers :as mod]
            [clojure.string :as s]
            [re-frame.core :refer [subscribe dispatch]]))


(defn option-selector-data
  [option-path
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
      {:name name
       :key key
       :has-named-mods? has-named-mods?
       :modifiers-str modifiers-str
       :failed-prereqs failed-prereqs
       :help help
       #_:help #_(if (or help has-named-mods?)
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
       :icon icon})))

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

(defn selection-section-data [title
                              built-template
                              option-paths
                              ui-fns
                              default-body
                              {:keys [::t/key ::t/name ::t/help ::t/options ::t/min ::t/max ::t/ref ::t/icon ::t/multiselect? ::entity/path ::entity/parent] :as selection}
                              num-columns
                              remaining
                              & [hide-homebrew?]]
  (let [actual-path (entity/actual-path selection)
        character @(subscribe [:character])
        ancestor-names (ancestor-names-string built-template actual-path)
        homebrew? @(subscribe [:homebrew? (or ref path)])
        has-custom-item? (some #(= :custom (::t/key %)) options)
        disable-select-new? (and multiselect?
                                 (not (pos? remaining))
                                 (some? max))]
    {:title title
     :path actual-path
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
             default-body)}))

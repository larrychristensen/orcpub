(ns orcpub.template
  (:require [clojure.spec :as spec]
            [orcpub.modifiers :as modifiers]
            [orcpub.common :as common]))

(spec/def ::name string?)
(spec/def ::key keyword?)
(spec/def ::min (spec/nilable (spec/int-in 0 100)))
(spec/def ::max (spec/nilable (spec/int-in 1 100)))
(spec/def ::attribute (spec/keys :req [::name ::key]))
(spec/def ::attributes (spec/+ ::attribute))
(spec/def ::derived-value (spec/or :func (spec/fspec :args (spec/cat :entity map?))
                                   :keyword keyword?))
(spec/def ::derived-attribute (spec/keys :req [::name ::key ::derived-value]))
(spec/def ::derived-attributes (spec/+ ::derived-attribute))
(spec/def ::modifiers (spec/+ ::modifiers/modifier))
(spec/def ::option (spec/keys :req [::name ::key]
                              :opt [::modifiers ::selections]))
(spec/def ::options (spec/+ ::option))
(spec/def ::selection (spec/keys :req [::name ::key ::options]
                                 :opt [::min ::max]))
(spec/def ::selections (spec/* ::selection))
(spec/def ::template (spec/keys :opt [::attributes ::derived-attributes ::selections]))

(spec/def ::modifier-map-value (spec/or :modifiers ::modifiers
                                        :modifier-map ::modifier-map))
(spec/def ::modifier-map-entry (spec/tuple keyword? ::modifier-map-value))
(spec/def ::modifier-map (spec/map-of keyword? (spec/or :modifier-map-value ::modifier-map-value
                                                        :min ::min
                                                        :max ::max)))

(defn selection-cfg [{:keys [name key source page order options help min sequential? multiselect? quantity? collapsible? ui-fn new-item-text new-item-fn prereq-fn simple? tags ref icon different? require-value?] :as cfg}]
  (let [max (if (find cfg :max) (:max cfg) 1)]
    {::name name
     ::key (or key (common/name-to-kw name))
     ::source (or source :phb)
     ::page page
     ::order order
     ::options (vec options)
     ::help help
     ::min (or min 1)
     ::max max
     ::sequential? (boolean sequential?)
     ::collapsible? collapsible?
     ::quantity? quantity?
     ::multiselect? multiselect?
     ::ui-fn ui-fn
     ::tags tags
     ::ref ref
     ::icon icon
     ::new-item-text new-item-text
     ::new-item-fn new-item-fn
     ::prereq-fn prereq-fn
     ::simple? simple?
     ::different? different?
     ::require-value? require-value?}))

(defn selection-with-key
  ([name key options]
   (selection-with-key name key options 1 1))
  ([name key options min max &[sequential? new-item-fn]]
   {::name name
    ::key key
    ::options (vec options)
    ::min min
    ::max max
    ::sequential? (boolean sequential?)
    ::new-item-fn new-item-fn}))

(defn selection
  ([name options]
   (selection name options 1 1))
  ([name options min max &[sequential? new-item-fn]]
   (selection-with-key name (common/name-to-kw name) options min max sequential? new-item-fn)))

(defn selection? [name options]
  (selection name options 0 1))

(defn selection+ [name new-item-fn options]
  (selection name options 1 nil false new-item-fn))

(defn selection* [name new-item-fn options]
  (selection name options 0 nil false new-item-fn))

(defn sequential-selection [name new-item-fn options]
  (selection name options 1 nil true new-item-fn))

(defn option-prereq [explanation func]
  {::label explanation
   ::prereq-fn func})

(defn option-cfg [{:keys [name key help selections modifiers prereqs order ui-fn icon] :as cfg}]
  {::name name
   ::key (or key (common/name-to-kw name))
   ::help help
   ::order order
   ::selections selections
   ::modifiers modifiers
   ::prereqs prereqs
   ::ui-fn ui-fn
   ::icon icon})

(defn option [name key selections modifiers & [prereqs]]
  (cond-> {::name name
           ::key key}
    selections (assoc ::selections (vec selections))
    modifiers (assoc ::modifiers modifiers)
    prereqs (assoc ::prereqs prereqs)))

(defn select-option [name selections & [prereqs]]
  (option name (common/name-to-kw name) selections nil prereqs))

(defn mod-option [name modifiers & [prereqs]]
  (option name (common/name-to-kw name) nil modifiers prereqs))

(declare make-modifier-map-from-selections)
(declare make-plugin-map-from-selections)

(defn make-modifier-map-entry-from-option [option]
  [(::key option)
   (let [modifiers option
         selections (::selections option)]
     (if selections
       (merge (make-modifier-map-from-selections selections) modifiers)
       modifiers))])

(spec/fdef
 make-modifier-map-entry-from-option
 :args ::option
 :ret ::modifier-map-entry)

#_(defn make-modifier-map-entry-from-selection [{:keys [ref key min max options]}]
  (let [path (or ref key)]
    (assoc-in {} ))
  [(or ref key)
   (into (select-keys selection [::min ::max])
         (map make-modifier-map-entry-from-option (::options selection)))])

#_(spec/fdef
 make-modifier-map-entry-from-selection
 :args ::selection
 :ret ::modifier-map-entry)

(defn selections-or-options [item]
  (or (::selections item)
      (::options item)))

(defn get-ref-selections [template]
  (filter
   ::ref
   (tree-seq selections-or-options
             selections-or-options
             template)))

(defn make-modifier-map-from-selections [selections]
  (reduce
   (fn [m {:keys [::ref ::key ::min ::max ::options] :as selection}]
     (let [path (if ref (if (sequential? ref) ref [ref]) [key])]
       (assoc-in m
                 path
                 (into (select-keys selection [::min ::max])
                       (map make-modifier-map-entry-from-option options)))))
   {}
   selections)
  #_(into {} (map make-modifier-map-entry-from-selection selections)))

(spec/fdef
 make-modifier-map-entry-from-selections
 :args ::selections
 :ret ::modifier-map)

(defn make-modifier-map [template]
  (let [ref-selections (get-ref-selections template)]
    (make-modifier-map-from-selections (concat (::selections template) ref-selections))))

(spec/fdef
 make-modifier-map
 :args ::template
 :ret ::modifier-map)

(spec/fdef make-modifier-map
           :args (spec/cat :template ::template)
           :ret ::modifier-map)

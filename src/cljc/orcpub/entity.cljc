(ns orcpub.entity
  (:require [clojure.spec :as spec]
            [orcpub.common :as common]
            [orcpub.modifiers :as mods]
            [orcpub.entity-spec :as es]
            [orcpub.template :as t]
            [clojure.set :refer [difference union intersection]]
            #?(:cljs [cljs.pprint :as pp])))

(spec/def ::key keyword?)
(spec/def ::option (spec/keys :req [::key]
                              :opt [::options]))
(spec/def ::option-vec (spec/+ ::option))
(spec/def ::options (spec/map-of keyword? (spec/or :single ::option
                                                   :multiple ::option-vec)))
(spec/def ::raw-entity (spec/keys :opt [::options]))

(spec/def ::flat-option (spec/keys :req [::t/path]
                                   :opt [::value]))
(spec/def ::flat-options (spec/+ ::flat-option))


;;============== topo sort ===============

(defn without
  "Returns set s with x removed."
  [s x] (difference s #{x}))

(defn take-1
  "Returns the pair [element, s'] where s' is set s with element removed."
  [s] {:pre [(not (empty? s))]}
  (let [item (first s)]
    [item (without s item)]))

(defn no-incoming
  "Returns the set of nodes in graph g for which there are no incoming
  edges, where g is a map of nodes to sets of nodes."
  [g]
  (let [nodes (set (keys g))
        have-incoming (apply union (vals g))]
    (difference nodes have-incoming)))

(defn normalize
  "Returns g with empty outgoing edges added for nodes with incoming
  edges only.  Example: {:a #{:b}} => {:a #{:b}, :b #{}}"
  [g]
  (let [have-incoming (apply union (vals g))]
    (reduce #(if (get % %2) % (assoc % %2 #{})) g have-incoming)))

(defn kahn-sort
  "Proposes a topological sort for directed graph g using Kahn's
   algorithm, where g is a map of nodes to sets of nodes. If g is
   cyclic, returns nil."
  ([g]
     (kahn-sort (normalize g) [] (no-incoming g)))
  ([g l s]
     (if (empty? s)
       (when (every? empty? (vals g)) l)
       (let [[n s'] (take-1 s)
             m (g n)
             g' (reduce #(update-in % [n] without %2) g m)]
         (recur g' (conj l n) (union s' (intersection (no-incoming g') m)))))))

;;==========================================



(declare build-options-paths)

(defn build-option-paths [path option]
  (let [new-path (conj path (::key option))
        child-options (::options option)
        option-value (::value option)
        result (cond-> {::t/path new-path}
                 option-value (assoc ::value option-value))]
    (if (seq child-options)
      (conj (build-options-paths new-path child-options)
            result)
      result)))

(defn build-options-entry-value-paths [path value]
  (if (sequential? value)
    (map (partial build-option-paths path) value)
    [(build-option-paths path value)]))

(defn build-options-entry-paths [path [option-key value]]
  (let [new-path (conj path option-key)]
    (build-options-entry-value-paths new-path value)))

(defn build-options-paths [path options]
  (map (partial build-options-entry-paths path) options))

(defn flatten-options [options]
  (flatten (build-options-paths [] options)))

(defn collect-modifiers [flat-options modifier-map]
  (mapcat
   (fn [{path ::t/path
         option-value ::value
         :as option}]
     (let [modifiers (::t/modifiers (get-in modifier-map path))]
       (map
        (fn [{:keys [::mods/name ::mods/value ::mods/fn ::mods/deferred-fn ::mods/default-value ::mods/deps] :as mod}]
          (if deferred-fn
            (assoc mod ::mods/value (or option-value default-value))
            mod))
        (flatten modifiers))))
   flat-options))

(defn collect-plugins [flat-options plugin-map]
  (mapcat
   (fn [{path ::t/path
         option-value ::value
         :as option}]
     (::t/plugins (get-in plugin-map path)))
   flat-options))

(defn modifier-functions [modifiers]
  (map
   (fn [{:keys [::mods/name ::mods/value ::mods/fn ::mods/deferred-fn ::mods/deps]}]
     (if (and deferred-fn value)
       (deferred-fn value)
       fn))
   modifiers))

(defn index-of-option [selection option-key]
  (first
   (keep-indexed
    (fn [i v]
      (if (= option-key (::key v))
        i))
    selection)))

(defn template-item-with-key [items item-key]
  (first
   (keep-indexed
    (fn [i s]
      (if (= (::t/key s) item-key)
        [i s]))
    items)))

(defn entity-item-with-key [items item-key]
  (first
   (keep-indexed
    (fn [i s]
      (if (and item-key
               (= (::key s) item-key))
        [i s]))
    items)))

(defn get-entity-path
  ([template entity option-path]
   (get-entity-path template entity [] option-path))
  ([template entity current-path [selection-k option-k & ks :as option-path]]
   (if selection-k
     (let [[selection-i selection] (template-item-with-key (::t/selections template) selection-k)
           {:keys [::t/min ::t/max ::t/options]} selection
           [option-i option] (template-item-with-key options option-k)
           selection-path (vec (concat current-path [::options selection-k]))
           entity-items (get-in entity selection-path)
           [entity-i _] (entity-item-with-key entity-items option-k)
           path-i (if (and (or option-k entity-i)
                           (or (nil? max) (> max 1)))
                    (if (nat-int? option-k)
                      option-k
                      entity-i))
           full-path (if path-i (conj selection-path path-i) selection-path)]
       (get-entity-path
        option
        entity
        full-path
        ks))
     (vec current-path))))

(defn order-modifiers [modifiers order]
  (let [order-map (zipmap order (range (count order)))]
    (sort-by (comp order-map ::mods/key) modifiers)))

(def memoized-make-modifier-map (memoize t/make-modifier-map))

(defn apply-options [raw-entity template]
  (let [modifier-map (memoized-make-modifier-map template)
        options (flatten-options (::options raw-entity))
        modifiers (collect-modifiers options modifier-map)
        deps (reduce
              (fn [m {:keys [::mods/key ::mods/deps]}]
                (if (seq deps)
                  (update m key union deps)
                  m))
              {}
              modifiers)
        mod-fns (modifier-functions modifiers)
        base (merge (::t/base template)
                    (::values raw-entity))
        base-deps (::es/deps base)
        all-deps (merge-with union deps base-deps)
        mod-order (rseq (kahn-sort all-deps))
        ordered-mods (order-modifiers modifiers mod-order)
        mod-fns (modifier-functions ordered-mods)]
    (es/apply-modifiers base mod-fns)))

(defn build [raw-entity template]
  (apply-options raw-entity template))

(declare get-template-selection-path)

(defn get-template-option-path [selection [f & r] current-path]
  (let [[option option-i]
        (first (keep-indexed
                (fn [i s]
                  (if (= (::t/key s) f)
                    [s i]))
                (::t/options selection)))
        next-path (vec (concat current-path [::t/options option-i]))]
    (if (seq r)
      (get-template-selection-path option r next-path)
      next-path)))

(defn get-template-selection-path [template [f & r] current-path]
  (let [[selection selection-i]
        (first (keep-indexed
                (fn [i s]
                  (if (= (::t/key s) f)
                    [s i]))
                (::t/selections template)))
        next-path (vec (concat current-path [::t/selections selection-i]))]
    (if (seq r)
      (get-template-option-path selection r next-path)
      next-path)))

(declare merge-selections)

(defn merge-options [options other-options]
  (if (or options other-options)
    (let [opt-map (zipmap (map ::t/key options) options)
          other-opt-map (zipmap (map ::t/key other-options) other-options)
          merged (merge-with
                  (fn [o1 o2]
                    (assoc
                     o1
                     ::t/selections (merge-selections (::t/selections o1) (::t/selections o2))
                     ::t/modifiers (vec (concat (::t/modifiers o1) (::t/modifiers o2)))))
                  opt-map
                  other-opt-map)]
      (vec
       (concat
        (map
         (fn [{key ::t/key}]
           (merged key))
         options)
        (vals (apply dissoc merged (map ::t/key options))))))))

(defn merge-selections [selections other-selections]
  (if (or selections other-selections)
    (let [sel-map (zipmap (map ::t/key selections) selections)
          other-sel-map (zipmap (map ::t/key other-selections) other-selections)
          merged (merge-with
                  (fn [s1 s2]
                    (assoc
                     s1
                     ::t/options
                     (merge-options (::t/options s1) (::t/options s2))))
                  sel-map
                  other-sel-map)]
      (vec
       (concat
        (map
         (fn [{key ::t/key}]
           (merged key))
         selections)
        (vals (apply dissoc merged (map ::t/key selections))))))))

(defn merge-multiple-selections [& selections]
  (reduce
   merge-selections
   selections))

(declare sort-selections)

(defn sort-options [s]
  (update
   s
   ::t/options
   (fn [options]
     (vec
      (sort-by
       ::t/name
       (map
        sort-selections
        options))))))

(defn sort-selections [o]
  (update
   o
   ::t/selections
   (fn [selections]
     (vec
      (sort-by
       (fn [selection]
         (or (::t/order selection) 1000))
       (map
        sort-options
        selections))))))

(defn build-template-aux [plugins template]
  (reduce
   (fn [templ {:keys [::t/path ::t/selections ::t/modifiers] :as plugin}]
     (let [template-path (get-template-selection-path templ path [])]
       (update-in
        templ
        template-path
        #(assoc
          %
          ::t/selections (merge-selections (::t/selections %) selections)
          ::t/modifiers (concat (::t/modifiers %) modifiers)))))
   template
   plugins))

(def memoized-build-template-aux (memoize build-template-aux))

(defn build-template [raw-entity template]
  (let [plugin-map (memoized-make-modifier-map template)
        options (flatten-options (::options raw-entity))
        plugins (collect-plugins options plugin-map)]
    (memoized-build-template-aux plugins template)))

(spec/fdef
 build
 :args (spec/cat :raw-entity ::raw-entity :modifier-map ::t/template)
 :ret any?)

(defn name-to-kw [name]
  (-> name
      clojure.string/lower-case
      (clojure.string/replace #"\W" "-")
      keyword))

(defn selection [name options]
  {::t/name name
   ::t/key (name-to-kw name)
   ::t/options options})

(defn option [name & [selections modifiers]]
  (cond-> {::t/name name
           ::t/key (name-to-kw name)}
    selections (assoc ::t/selections selections)
    modifiers (assoc ::t/modifiers modifiers)))

(defn get-option-value-path [template entity path]
  (conj (get-entity-path template entity path) ::value))

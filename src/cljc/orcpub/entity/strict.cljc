(ns orcpub.entity.strict
  (:require #?(:clj [clojure.spec.alpha :as spec])
            #?(:cljs [cljs.spec.alpha :as spec])))

(spec/def ::int-value int?)
(spec/def ::map-value map?)

(spec/def ::keyword-key (fn [k] (and (keyword? k)
                             (not (re-matches #"^[0-9].*" (name k))))))
(spec/def ::key (spec/or :keyword ::keyword-key
                         :int int?))
(spec/def ::option (spec/keys :req [::key]
                              :opt [::int-value
                                    ::map-value
                                    ::selections]))

(spec/def ::options (spec/coll-of ::option))

(spec/def ::selection (spec/keys :req [::key]
                                 :opt [::option ::options]))

(spec/def ::selections (spec/coll-of ::selection))

(spec/def ::values (spec/map-of qualified-keyword? some?))

(defn has-duplicate-selections? [{:keys [::selections]}]
  (let [key-set (into #{} (map ::key selections))]
    (or (not= (count key-set)
              (count selections))
        (some
         (fn [{:keys [::options ::option] :as selection}]
           (some
            has-duplicate-selections?
            (or options [option])))
         selections))))

(spec/def ::entity (spec/and (spec/keys :opt [::selections
                                              ::values])
                             (complement has-duplicate-selections?)))

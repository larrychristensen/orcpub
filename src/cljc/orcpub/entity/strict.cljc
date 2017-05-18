(ns orcpub.entity.strict
  (:require #?(:clj [clojure.spec :as spec])
            #?(:cljs [cljs.spec :as spec])))

(spec/def ::int-value int?)
(spec/def ::map-value map?)

(spec/def ::key keyword?)
(spec/def ::option (spec/keys :req [::key]
                              :opt [::int-value
                                    ::map-value
                                    ::selections]))

(spec/def ::options (spec/coll-of ::option))

(spec/def ::selection (spec/keys :req [::key]
                                 :opt [::option ::options]))

(spec/def ::selections (spec/coll-of ::selection))

(spec/def ::values (spec/map-of qualified-keyword? some?))

(spec/def ::entity (spec/keys :opt [::selections
                                    ::values]))

(ns orcpub.dnd.e5.combat
  (:require #?(:clj [clojure.spec.alpha :as spec])
            #?(:cljs [cljs.spec.alpha :as spec])
            [orcpub.common :as common]))

(spec/def ::characters (spec/coll-of nat-int? :kind vector?))
(spec/def ::parties (spec/coll-of nat-int? :kind vector?))
(spec/def ::encounters (spec/coll-of keyword? :kind vector?))
(spec/def ::monster keyword?)
(spec/def ::num nat-int?)
(spec/def ::monster-spec (spec/keys :opt-un [::monster ::num]))
(spec/def ::monsters (spec/coll-of ::monster-spec :kind vector?))
(spec/def ::combat (spec/keys :opt-un [::characters ::parties ::encounters ::monsters]))

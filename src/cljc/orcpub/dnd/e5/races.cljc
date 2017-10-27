(ns orcpub.dnd.e5.races
  (:require #?(:clj [clojure.spec.alpha :as spec])
            #?(:cljs [cljs.spec.alpha :as spec])
            [orcpub.common :as common]))

(spec/def ::name (spec/and string? common/starts-with-letter?))
(spec/def ::key (spec/and keyword? common/keyword-starts-with-letter?))
(spec/def ::option-pack string?)
(spec/def ::languages (spec/and set?
                                (spec/coll-of string?)))
(spec/def ::homebrew-race (spec/keys :req-un [::name ::key ::option-pack]
                                     :opt-un [::languages]))

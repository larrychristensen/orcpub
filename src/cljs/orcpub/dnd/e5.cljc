(ns orcpub.dnd.e5
  (:require #?(:cljs [cljs.spec.alpha :as spec])
            #?(:clj [clojure.spec.alpha :as spec])
            [orcpub.dnd.e5.spells :as spells]
            [orcpub.common :as common]))

(spec/def ::spells (spec/map-of common/keyword-starts-with-letter?
                                ::spells/homebrew-spell))
(spec/def ::plugin (spec/keys :opt [::spell-lists
                                    ::spells]))
(spec/def ::plugins (spec/map-of string? ::plugin))


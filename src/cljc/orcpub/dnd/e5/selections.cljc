(ns orcpub.dnd.e5.selections
  (:require #?(:clj [clojure.spec.alpha :as spec])
            #?(:cljs [cljs.spec.alpha :as spec])
            [orcpub.common :as common]
            [orcpub.template :as t]
            [orcpub.modifiers :as mod]
            [orcpub.dnd.e5.options :as opt5e]
            [orcpub.dnd.e5.modifiers :as mod5e]
            [orcpub.dnd.e5.weapons :as weapon5e]
            [orcpub.dnd.e5.equipment :as equipment5e]
            [orcpub.dnd.e5.character :as char5e]
            [orcpub.dnd.e5.units :as units5e]
            [orcpub.dnd.e5.spells :as spells5e]
            [orcpub.dnd.e5.spell-lists :as sl5e]
            [orcpub.dnd.e5.template-base :as t-base]
            [re-frame.core :refer [reg-sub reg-sub-raw dispatch subscribe]]
            [clojure.string :as s]))

(spec/def ::name (spec/and string? common/starts-with-letter?))
(spec/def ::description string?)

(spec/def ::key (spec/and keyword? common/keyword-starts-with-letter?))
(spec/def ::option-pack string?)

(spec/def ::option (spec/keys :req-un [::name]
                              :opt-un [::description]))

(spec/def ::options (spec/coll-of ::option :kind vector?))

(spec/def ::homebrew-selection (spec/keys :req-un [::name ::key ::option-pack]
                                          :opt-un [::options]))

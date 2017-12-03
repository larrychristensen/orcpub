(ns orcpub.dnd.e5
  (:require #?(:cljs [cljs.spec.alpha :as spec])
            #?(:clj [clojure.spec.alpha :as spec])
            [orcpub.dnd.e5.spells :as spells]
            [orcpub.dnd.e5.languages :as languages]
            [orcpub.common :as common]))

(spec/def ::spells (spec/map-of common/keyword-starts-with-letter?
                                ::spells/homebrew-spell))

(spec/def ::content-keyword (fn [v] (and (qualified-keyword? v)
                                         (common/keyword-starts-with-letter? v)
                                         (= (namespace v) "orcpub.dnd.e5"))))

(spec/def ::option-pack string?)

(spec/def ::homebrew-item (spec/keys :req-un [::option-pack]))

(spec/def ::homebrew-items (spec/map-of common/keyword-starts-with-letter?
                                        ::homebrew-item))

(spec/def ::plugin (spec/map-of ::content-keyword
                                ::homebrew-items))

(spec/def ::plugins (spec/map-of string? ::plugin))

(defn merge-plugins [plugin-1 plugin-2]
  (merge-with
   merge
   plugin-1
   plugin-2))

(defn merge-all-plugins [all-plugins-1 all-plugins-2]
  (merge-with
   merge-plugins
   all-plugins-1
   all-plugins-2))


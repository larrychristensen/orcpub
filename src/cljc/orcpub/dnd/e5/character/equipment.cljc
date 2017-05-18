(ns orcpub.dnd.e5.character.equipment
  (:require [clojure.spec :as spec]
            [orcpub.common :as common]))

(spec/def ::name string?)
(spec/def ::quantity nat-int?)
(spec/def ::equipped? boolean?)
(spec/def ::background-starting-equipment? boolean?)
(spec/def ::class-starting-equipment? boolean?)

(def equipment-item-req [::name ::quantity ::equipped?])
(def equipment-item-opt [::background-starting-equipment?
                         ::class-starting-equipment?])

(spec/def ::equipment-item (spec/keys :req [::name ::quantity ::equipped?]
                                      :opt [::background-starting-equipment?
                                            ::class-starting-equipment?]))

(spec/def ::equipment-items (spec/coll-of ::equipment-item))

(spec/def ::equipment-item-un (spec/keys :req-un [::name ::quantity ::equipped?]
                                         :opt-un [::background-starting-equipment?
                                                  ::class-starting-equipment?]))

(spec/def ::equipment-items-un (spec/coll-of ::equipment-item-un))

(defn to-namespaced [item]
  (common/add-namespaces-to-keys item "orcpub.dnd.e5.character.equipment"))

(spec/fdef to-namespaced
           :args ::equipment-item-un
           :ret ::equipment-item)

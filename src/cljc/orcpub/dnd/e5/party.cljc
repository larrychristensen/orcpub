(ns orcpub.dnd.e5.party
  (:require [clojure.spec.alpha :as spec]))

(spec/def ::name string?)

(spec/def ::character-id int?)

(spec/def ::character-ids (spec/coll-of ::character-id))

(spec/def ::party (spec/keys :req [::name ::character-ids]))

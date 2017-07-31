(ns orcpub.dnd.e5.character-props
  (:require [orcpub.entity-spec :as es]))

(defn get-prop [built-char prop]
  (es/entity-val built-char prop))

(defmacro defprop [kw]
  `(defn ~(symbol (name kw)) [built-char#]
     (get-prop built-char# ~kw)))


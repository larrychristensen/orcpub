(ns orcpub.dnd.e5.common)

(defn slot-level-key [level]
  (keyword "orcpub.dnd.e5.spells" (str "slots-" level)))

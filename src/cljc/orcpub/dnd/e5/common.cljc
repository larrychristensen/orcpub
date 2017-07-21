(ns orcpub.dnd.e5.common)

(defn slot-level-key [level]
  (keyword "orcpub.dnd.e5.character" (str "slots-" level)))

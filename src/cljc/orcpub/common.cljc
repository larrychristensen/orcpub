(ns orcpub.common)

(defn name-to-kw [name]
  (-> name
      clojure.string/lower-case
      (clojure.string/replace #"'" "")
      (clojure.string/replace #"\W" "-")
      keyword))

(defn map-by-key [values]
  (zipmap (map :key values) values))

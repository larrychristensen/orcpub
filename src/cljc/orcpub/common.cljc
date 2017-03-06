(ns orcpub.common
  (:require [clojure.string :as s]))

(defn name-to-kw [name]
  (-> name
      clojure.string/lower-case
      (clojure.string/replace #"'" "")
      (clojure.string/replace #"\W" "-")
      keyword))

(defn map-by-key [values]
  (zipmap (map :key values) values))

(defmacro ptime [message body]
  `(do (prn ~message)
       (time ~body)))

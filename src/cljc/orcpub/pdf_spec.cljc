(ns orcpub.pdf-spec
  (:require [orcpub.entity-spec :as es]))

(defn entity-vals [built-char kws]
  (reduce
   (fn [vs kw]
     (let [[to from] (if (keyword? kw) [kw kw] kw)]
       (assoc vs to (es/entity-val built-char from))))
   {}
   kws))

(defn make-spec [built-char]
  (let [race (es/entity-val built-char :race)
        subrace (es/entity-val built-char :subrace)]
    {:race (str race "/" subrace)}))

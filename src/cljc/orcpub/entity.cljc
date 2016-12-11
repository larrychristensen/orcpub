(ns orcpub.entity
  (:require [orcpub.modifiers :as modifiers]))

(defn all-modifiers [entity options]
  (mapcat
   (fn [option-path]
     (:modifiers (get-in options option-path)))
   (:options entity)))

(defn build [entity options]
  (reduce
   (fn [accum-entity {path :path :as modifier}]
     (update-in
      accum-entity
      (if (sequential? path)
        path
        [path])
      (partial modifiers/modify modifier)))
   entity
   (all-modifiers entity options)))

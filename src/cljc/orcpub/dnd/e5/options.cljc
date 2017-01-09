(ns orcpub.dnd.e5.options
  (:require [orcpub.template :as t]
            [orcpub.dnd.e5.character :as character]
            [orcpub.dnd.e5.modifiers :as modifiers]))

(defn skill-option [skill]
  (t/option
   (:name skill)
   (:key skill)
   nil
   [(modifiers/skill-proficiency (:key skill))]))

(defn skill-options [skills]
  (map
   skill-option
   skills))

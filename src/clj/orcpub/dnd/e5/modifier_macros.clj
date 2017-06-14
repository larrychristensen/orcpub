(ns orcpub.dnd.e5.modifier-macros
  (:require [orcpub.modifiers :as mods]
            [orcpub.dnd.e5.skills :as skills]
            [clojure.string :as s]))

(defmacro skill-proficiency-2 [skill-kw source conditions]
  `(mods/modifier ~'?skill-profs
                 (assoc-in ~'?skill-profs
                           [~skill-kw
                            ~source]
                           true)
                 (s/capitalize (-> ~skill-kw skill5e/skills-map :name))
                 "proficiency"
                 nil))

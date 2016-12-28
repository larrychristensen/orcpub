(ns orcpub.dnd.e5.modifiers_test
  (:require [orcpub.dnd.e5.modifiers :as dnd5-mods]
            [clojure.spec.test :as stest]))

(stest/instrument `dnd5-mods/standard-ability-rolls)
(dnd5-mods/standard-ability-rolls)
(stest/summarize-results (stest/check `dnd5-mods/standard-ability-rolls))

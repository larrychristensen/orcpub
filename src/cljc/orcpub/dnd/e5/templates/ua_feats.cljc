(ns orcpub.dnd.e5.templates.ua-feats
  (:require [orcpub.dnd.e5.modifiers :as mod5e]
            [orcpub.dnd.e5.options :as opt5e]
            [orcpub.dnd.e5.character :as char5e]
            [orcpub.template :as t]
            [orcpub.common :as common]
            [re-frame.core :refer [subscribe]]
            [clojure.string :as s]))

(def ua-feats-plugin
  {:name "Unearthed Arcana: Feats"
   :key :ua-feats
   :feat-options? true
   :selections [(opt5e/feat-selection-2
                 {:options (map
                            (fn [o]
                              (update o ::t/modifiers conj opt5e/ua-al-illegal))
                            [(t/option-cfg
                              {:name "Warhammer Master"
                               :modifiers [(mod5e/dependent-trait
                                            {:name "Warhammer Master Feat"
                                             :page 1
                                             :source :ua-feats
                                             :summary (str "When you hit with a warhammer, the creature is knocked prone if it fails a DC " (?spell-save-dc ::char5e/str) ". Also, if you hit a creature with a shield you can knock the shield away instead of do damage.")})]})])})]})

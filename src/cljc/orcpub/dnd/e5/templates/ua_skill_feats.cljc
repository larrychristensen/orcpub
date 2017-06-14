(ns orcpub.dnd.e5.templates.ua-skill-feats
  (:require [orcpub.dnd.e5.modifiers :as mod5e]
            [orcpub.dnd.e5.options :as opt5e]
            [orcpub.dnd.e5.character :as char5e]
            [orcpub.template :as t]
            [orcpub.common :as common]
            [re-frame.core :refer [subscribe]]
            [clojure.string :as s]))

(defn skill-feat [nm page ability-kw skill-kw {:keys [trait-desc trait-type modifiers]}]
  (opt5e/feat-option
   {:name nm
    :exclude-trait? (and trait-desc trait-type)
    :summary (str "gain proficiency in " (s/capitalize (common/kw-to-name skill-kw)) " or double proficiency bonus if you already have it" (if trait-desc (str "; " trait-desc)))
    :modifiers (concat
                modifiers
                (remove
                 nil?
                 [(mod5e/ability ability-kw 1)
                  (if trait-type
                    (trait-type
                     {:name (str nm " Feat")
                      :page page
                      :source :ua-skill-feats
                      :summary trait-desc}))
                  (opt5e/skill-prof-or-expertise skill-kw nm)]))}))

(def ua-skill-feats-plugin
  {:name "Unearthed Arcana: Feats for Skills"
   :key :ua-skill-feats
   :feat-options? true
   :selections [(opt5e/feat-selection-2
                 {:options (map
                            (fn [o]
                              (update o ::t/modifiers conj opt5e/ua-al-illegal))
                            [(skill-feat
                              "Acrobat" 1 ::char5e/dex :acrobatics
                              {:trait-desc "as a bonus action, make a DC 15 Acrobatics check, on success ignore difficult terrain til end of turn"
                               :trait-type #(mod5e/bonus-action %)})
                             (skill-feat
                              "Animal Handler" 1 ::char5e/wis :animal-handling
                              {:trait-desc "as a bonus action, command a friendly beast's actions on it's next"
                               :trait-type #(mod5e/bonus-action %)})
                             (skill-feat
                              "Arcanist" 1 ::char5e/int :arcana
                              {:trait-desc "learn 'prestidigitation' and 'detect magic' spells"
                               :modifiers [(mod5e/spells-known 0 :prestidigitation ::char5e/int "Arcanist" 0 "at will")
                                           (mod5e/spells-known 1 :detect-magic ::char5e/int "Arcanist" 0 "once per long rest")]})
                             (skill-feat
                              "Brawny" 1 ::char5e/str :athletics
                              {:trait-desc "carrying capacity of one size larger creature"})
                             (skill-feat
                              "Diplomat" 1 ::char5e/cha :persuasion
                              {:trait-desc "charm a creature you spend 1 minute talking to"})])})]})

(ns orcpub.dnd.e5.templates.ua-skill-feats
  (:require [orcpub.dnd.e5.modifiers :as mod5e]
            [orcpub.dnd.e5.options :as opt5e]
            [orcpub.dnd.e5.character :as char5e]
            [orcpub.template :as t]
            [orcpub.common :as common]
            [re-frame.core :refer [subscribe]]
            [clojure.string :as s]))

(def action #(mod5e/action %))

(def bonus-action #(mod5e/bonus-action %))

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
                               :trait-type bonus-action})
                             (skill-feat
                              "Animal Handler" 1 ::char5e/wis :animal-handling
                              {:trait-desc "as a bonus action, command a friendly beast's actions on it's next"
                               :trait-type bonus-action})
                             (skill-feat
                              "Arcanist" 1 ::char5e/int :arcana
                              {:trait-desc "learn 'prestidigitation' and 'detect magic' spells"
                               :modifiers [(mod5e/spells-known 0 :prestidigitation ::char5e/int "Arcanist" 0 "at will")
                                           (mod5e/spells-known 1 :detect-magic ::char5e/int "Arcanist" 0 "once per long rest")]})
                             (skill-feat
                              "Brawny" 1 ::char5e/str :athletics
                              {:trait-desc "carrying capacity of one size larger creature"})
                             (skill-feat
                              "Diplomat" 2 ::char5e/cha :persuasion
                              {:trait-desc "charm a creature you spend 1 minute talking to"})
                             (skill-feat
                              "Ephatic" 2 ::char5e/wis :insight
                              {:trait-desc "make an Insight check contested by an opponents Deception check, on success, gain advantage on attack rolls and ability checks against it until end of next turn"
                               :trait-type action})
                             (skill-feat
                              "Historian" 2 ::char5e/int :history
                              {:trait-desc "When you use Help action, make a DC 15 History check. If successful, the creature gains a bonus equal to your proficiency bonus"})
                             (skill-feat
                              "Investigator" 2 ::char5e/int :investigation
                              {:trait-desc "take the Search action"
                               :trait-type bonus-action})
                             (skill-feat
                              "Medic" 2 ::char5e/wis :medicine
                              {:trait-desc "During a short rest, make a DC 15 WIS check. If successful, a creature that spends a HD can forego the roll and gain max HPs from the die."})
                             (skill-feat
                              "Menacing" 2 ::char5e/cha :intimidation
                              {:trait-desc "When you use the Attack action, replace an attack with an Intimidation check contested by the target's Insight check, if successful the target is frightened of you"})
                             (skill-feat
                              "Naturalist" 3 ::char5e/int :nature
                              {:trait-desc "Learn 'druidcraft' and 'detect poison and disease' spells"
                               :modifiers [(mod5e/spells-known 0 :druidcraft ::char5e/int "Naturalist" 0 "at will")
                                           (mod5e/spells-known 1 :detect-poison-and-disease ::char5e/int "Naturalist" 0 "once per long rest")]})
                             (skill-feat
                              "Perceptive" 3 ::char5e/wis :perception
                              {:trait-desc "Lightly obscured area don't give disadvantage on Perception checks to see or hear"})
                             (skill-feat
                              "Performer" 3 ::char5e/cha :performance
                              {:trait-desc "When performing, make a Performance check contented by a humanoid's Insight check. If successful, the target makes Perception and Investigation checks with disadvantage until your performance ends."})
                             (skill-feat
                              "Quick-Fingered" 3 ::char5e/dex :sleight-of-hand
                              {:trait-desc "Pickpocket or similar feats as a bonus action"
                               :trait-type bonus-action})
                             (skill-feat
                              "Silver-Tongued" 3 ::char5e/cha :deception
                              {:trait-desc "When you take an Attack action, replace an attack with a Deception check contested by the target's Insight check. On success, your movement doesn't provoke opportunity attacks from target and attacks against it have advantage."})
                             (skill-feat
                              "Stealthy" 3 ::char5e/dex :stealth
                              {:trait-desc "If you hidden, you can move up to 10 ft. in the open without revealing your position if your move ends where you're not in the open."})
                             (skill-feat
                              "Survivalist" 3 ::char5e/wis :survival
                              {:modifiers [(mod5e/spells-known 1 :alarm ::char5e/wis "Survivalist" 0 "once per long rest")]})
                             (skill-feat
                              "Theologian" 3 ::char5e/int :religion
                             {:modifiers [(mod5e/spells-known 0 :thaumaturgy ::char5e/int "Theologian" 0 "at will")
                                           (mod5e/spells-known 1 :detect-evil-and-good ::char5e/int "Theologian" 0 "once per long rest")]})])})]})

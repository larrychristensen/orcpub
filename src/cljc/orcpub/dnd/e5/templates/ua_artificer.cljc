(ns orcpub.dnd.e5.templates.ua-artificer
  (:require [orcpub.dnd.e5.character :as char5e]
            [orcpub.dnd.e5.options :as opt5e]
            [orcpub.dnd.e5.modifiers :as mod5e]
            [orcpub.dnd.e5.equipment :as equip5e]
            [orcpub.modifiers :as mod]
            [orcpub.template :as t]
            [orcpub.common :as common]))

(def artificer-option
  (opt5e/class-option
   {:name "Artificer"
    :hit-die 8
    :ability-increase-levels [4 8 12 16 19]
    :multiclass-prereqs [(opt5e/ability-prereq ::char5e/cha 13)]
    :profs {:armor {:light false :medium false}
            :weapon {:simple true}
            :save {::char5e/con true ::char5e/int true}
            :tool {:thieves-tools false}
            :skill-options {:choose 3 :options {:arcana true
                                                :deception true
                                                :history true
                                                :investigation true
                                                :medicine true
                                                :nature true
                                                :religion true
                                                :sleight-of-hand true}}}
    :weapon-choices [{:name "Weapon"
                      :options {:handaxe 1
                                :light-hammer 1
                                :simple 1}}]
    :weapons {:light-crossbow 1
              :bolt 20}
    :equipment {:thieves-tools 1
                :dungeoneers-pack 1}
    :armor-choices [{:name "Armor"
                     :options {:scale-mail 1
                               :studded-leather 1}}]
    :armor {:leather 1}
    :spellcaster true
    :spellcasting {:level-factor 1
                   :cantrips-known {1 2 4 1 10 1}
                   :spells-known {1 4
                                  2 1
                                  3 1
                                  4 1
                                  5 1
                                  6 1
                                  7 1
                                  8 1
                                  9 1
                                  10 2
                                  11 1
                                  13 1
                                  14 2
                                  15 1
                                  17 1
                                  18 2}
                   :known-mode :schedule
                   :ability ::char5e/cha}
    :traits [{:name "Magic Item Analysis"
              :page 3
              :summary "You know detect magic and identify and can cast them as rituals."}]
    :selections [(opt5e/tool-proficiency-selection
                  {:num 2
                   :options (map
                             (fn [{:keys [name key]}]
                               (t/option-cfg
                                {:name name
                                 :key key
                                 :modifiers [(mod5e/tool-proficiency key)
                                             (mod5e/tool-expertise key)]}))
                             equip5e/tools)})]
    :modifiers [(mod5e/tool-proficiency :thieves-tools)
                (mod5e/tool-expertise :thieves-tools)]
    :subclass-level 1
    :subclass-title "Artificer Specialty"
    :subclasses [{:name "Alchemist"}
                 {:name "Gunsmith"}]}))

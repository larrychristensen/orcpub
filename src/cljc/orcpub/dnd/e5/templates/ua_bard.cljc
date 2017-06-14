(ns orcpub.dnd.e5.templates.ua-bard
  (:require [orcpub.dnd.e5.modifiers :as mod5e]
            [orcpub.dnd.e5.options :as opt5e]))

(def bard-option
  (opt5e/class-option
   {:name "Bard"
    :subclass-level 3
    :subclass-title "Bard College"
    :source :ua-bard
    :plugin? true
    :subclasses [{:name "College of Glamour"
                  :source :ua-bard
                  :modifiers [opt5e/ua-al-illegal]
                  :traits [{:name "Mantle of Inspiration"
                            :page 1
                            :source :ua-bard
                            :level 3}
                           {:name "Enthralling Performance"
                            :page 1
                            :source :ua-bard
                            :level 2}
                           {:name "Mantle of Magesty"
                            :page 1
                            :source :ua-bard
                            :level 6}
                           {:name "Unbreakable Majesty"
                            :page 2
                            :source :ua-bard
                            :level 14}]}
                 {:name "College of Whispers"
                  :source :ua-bard
                  :modifiers [opt5e/ua-al-illegal]
                  :traits [{:name "Venomous Blades"
                            :page 2
                            :source :ua-bard
                            :level 3}
                           {:name "Venomous Words"
                            :page 2
                            :source :ua-bard
                            :level 3}
                           {:name "Mantle of Whispers"
                            :page 2
                            :source :ua-bard
                            :level 6}
                           {:name "Shadow Lore"
                            :page 2
                            :source :ua-bard
                            :level 14}]}]}))

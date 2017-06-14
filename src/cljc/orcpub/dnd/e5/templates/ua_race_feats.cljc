(ns orcpub.dnd.e5.templates.ua-race-feats
  (:require [orcpub.dnd.e5.modifiers :as mod5e]
            [orcpub.dnd.e5.options :as opt5e]
            [orcpub.dnd.e5.character :as char5e]
            [orcpub.template :as t]
            [orcpub.common :as common]
            [re-frame.core :refer [subscribe]]))

(defn race-prereq [race-nm]
  (t/option-prereq
   (str race-nm " Only")
   (fn [c] (= race-nm @(subscribe [::char5e/race nil c])))))

(defn subrace-prereq [race-nm subrace-nm]
  (t/option-prereq
   (str subrace-nm " Only")
   (fn [c] (and (= race-nm @(subscribe [::char5e/race nil c]))
                (= subrace-nm @(subscribe [::char5e/subrace nil c]))))))

(def ua-race-feats-plugin
  {:name "Unearthed Arcana: Feats for Races"
   :key :ua-race-feats
   :feat-options? true
   :selections [(opt5e/feat-selection-2
                 {:options [(opt5e/feat-option
                             {:name "Barbed Hide"
                              :page 1
                              :source :ua-race-feats
                              :prereqs [(race-prereq "Tiefling")]
                              :selections [(opt5e/ability-increase-selection [::char5e/con ::char5e/cha] 1 false)]
                              :modifiers [(mod5e/bonus-action
                                           {:name "Barbed Hide"
                                            :page 1
                                            :source :ua-race-feats
                                            :summary "Protrude or retract barbs from your skin that deal 1d6 piercing while grappling."})
                                          (opt5e/skill-prof-or-expertise :intimidation :ua-barbed-hide)]})
                            (opt5e/feat-option
                             {:name "Bountiful Luck"
                              :page 1
                              :source :ua-race-feats
                              :prereqs [(race-prereq "Halfling")]
                              :modifiers [(mod5e/dependent-trait
                                           {:name "Bountiful Luck"
                                            :page 1
                                            :source :ua-race-feats
                                            :summary "Allow and ally within 30 ft. to reroll a 1 on an attack roll, ability check, or save."})]})
                            (opt5e/feat-option
                             {:name "Critter Friend"
                              :page 1
                              :source :ua-race-feats
                              :prereqs [(subrace-prereq "Gnome" "Forest Gnome")]
                              :modifiers [(opt5e/skill-prof-or-expertise :animal-handling :critter-friend)
                                          (mod5e/spells-known 1 :speak-with-animals :int "Forest Gnome" 0 "at will")
                                          (mod5e/spells-known 1 :animal-friendship :int "Forest Gnome" 0 "once per long rest")]})
                            (opt5e/feat-option
                             {:name "Dragon Fear"
                              :page 1
                              :source :ua-race-feats
                              :prereqs [(race-prereq "Dragonborn")]
                              :selections [(opt5e/ability-increase-selection [::char5e/str ::char5e/cha] 1 false)]
                              :modifiers [
                                          (mod5e/dependent-trait
                                           {:name "Dragon Fear"
                                            :page 2
                                            :source :ua-race-feats
                                            :summary (str "Expend a breath weapon use and roar, causing creatures of your choice within 30 ft. and can hear you to make a DC " (?spell-save-dc ::char5e/cha) " WIS save. On failed save they become frightened of you for 1 min. Targets that take damage can repeat the save.")})]})
                            (opt5e/feat-option
                             {:name "Dragon Hide"
                              :page 1
                              :source :ua-race-feats
                              :prereqs [(race-prereq "Dragonborn")]
                              :selections [(opt5e/ability-increase-selection [::char5e/str ::char5e/cha] 1 false)]
                              :modifiers [(mod5e/dependent-trait
                                           {:name "Dragon Hide"
                                            :page 2
                                            :source :ua-race-feats
                                            :summary (str "Deal 1d4 " (common/bonus-str (?ability-bonuses ::char5e/str)) " slashing damage with an unarmed strike")})
                                          (mod5e/ac-bonus-fn
                                           (fn [armor & [shield]]
                                             (if (and (nil? armor)
                                                      (nil? shield))
                                               1)))]})]})]})


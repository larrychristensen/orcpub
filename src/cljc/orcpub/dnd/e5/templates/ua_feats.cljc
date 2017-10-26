(ns orcpub.dnd.e5.templates.ua-feats
  (:require [orcpub.dnd.e5.modifiers :as mod5e]
            [orcpub.dnd.e5.options :as opt5e]
            [orcpub.dnd.e5.character :as char5e]
            [orcpub.template :as t]
            [orcpub.common :as common]
            [re-frame.core :refer [subscribe]]
            [clojure.string :as s]))

#_(defn weapon-attack-bonus-mod [weapons bonus]
  (mod5e/attack-modifier-fn
   (fn [{kw :key base-kw :base-key}]
     (if (or (weapons kw))
       bonus
       0))))

#_(def ua-feats-plugin
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
                                             :summary (str "When you hit with a warhammer, the creature is knocked prone if it fails a DC " (?spell-save-dc ::char5e/str) ". Also, if you hit a creature with a shield you can knock the shield away instead of do damage.")})]})
                             (t/option-cfg
                              {:name "Fell Handed"
                               :modifiers [(weapon-attack-bonus-mod #{:handaxe :battleaxe :greataxe :warhammer :maul} 1)
                                           (mod5e/dependent-trait
                                            {:name "Fell Handed Feat"
                                             :page 2
                                             :source :ua-feats
                                             :summary (str "You have mastered the battleaxe, greataxe, handaxe, warhammer, and maul. You gain a +1 bonus to attack rolls with these weapons. When you have advantage on melee attack and hit with one of these weapons, you knock target prone if both rolls would hit. If you have disadvantage on an attack with one of these weapons and miss, but the higher roll would have hit, the target takes " ?str-mod " bludgeoning damage. When you use Help action with one of these weapons the ally gains a +2 bonus to the attack roll if the target is using a shield.")})]})
                             (t/option-cfg
                              {:name "Blade Mastery"
                               :modifiers [(weapon-attack-bonus-mod #{:shortsword :longsword :scimitar :rapier :greatsword} 1)
                                           (mod5e/trait-cfg
                                            {:name "Blade Mastery Feat"
                                             :page 2
                                             :source :ua-feats
                                             :summary "You have mastered the greatsword, longsword,rapier, scimitar, and shortsword. With them you gain a +1 to attack bonus. You have advantage on opportunity attacks with the weapon."})
                                           (mod5e/reaction
                                            {:name "Blade Mastery Feat Reaction"
                                             :page 2
                                             :source :ua-feats
                                             :summary "On your turn, gain a +1 bonus to your AC until the start of your next turn."})]})
                             (t/option-cfg
                              {:name "Flail Mastery"
                               :modifiers [(weapon-attack-bonus-mod #{:flail} 1)
                                           (mod5e/trait-cfg
                                            {:name "Flail Mastery Feat"
                                             :page 3
                                             :source :ua-feats})]})
                             (t/option-cfg
                              {:name "Spear Mastery"
                               :modifiers [(weapon-attack-bonus-mod #{:spear} 1)
                                           (mod5e/trait-cfg
                                            {:name "Spear Mastery Feat"
                                             :page 3
                                             :source :ua-feats})]})
                             (t/option-cfg
                              {:name "Alchemist"
                               :modifiers [(mod5e/ability ::char5e/int 1)
                                           (opt5e/tool-prof-or-expertise :alchemists-supplies
                                                                         :ua-feats-alchemist)
                                           (mod5e/trait-cfg
                                            {:name "Alchemist Feat"
                                             :page 4
                                             :source :ua-feats})]})
                             (t/option-cfg
                              {:name "Burglar"
                               :modifiers [(mod5e/ability ::char5e/dex 1)
                                           (opt5e/tool-prof-or-expertise :thieves-tools
                                                                         :ua-feats-burglar)
                                           (mod5e/trait-cfg
                                            {:name "Burglar Feat"
                                             :page 4
                                             :source :ua-feats})]})
                             (t/option-cfg
                              {:name "Gourmand"
                               :modifiers [(mod5e/ability ::char5e/con 1)
                                           (opt5e/tool-prof-or-expertise :cooks-utensils
                                                                         :ua-feats-gourmand)
                                           (mod5e/trait-cfg
                                            {:name "Gourmand Feat"
                                             :page 4
                                             :source :ua-feats})]})
                             (t/option-cfg
                              {:name "Master of Disguise"
                               :modifiers [(mod5e/ability ::char5e/cha 1)
                                           (opt5e/tool-prof-or-expertise :disguise-kit
                                                                         :ua-feats-master-of-disguise)
                                           (mod5e/trait-cfg
                                            {:name "Master of Disguise Feat"
                                             :page 4
                                             :source :ua-feats})]})])})]})

(ns orcpub.dnd.e5.templates.ua-artificer
  (:require [orcpub.dnd.e5.character :as char5e]
            [orcpub.dnd.e5.character.equipment :as char-equip5e]
            [orcpub.dnd.e5.options :as opt5e]
            [orcpub.dnd.e5.modifiers :as mod5e]
            [orcpub.dnd.e5.equipment :as equip5e]
            [orcpub.dnd.e5.magic-items :as mi5e]
            [orcpub.modifiers :as mod]
            [orcpub.template :as t]
            [orcpub.common :as common]))

(defn wonderous-invention-selection [item-kws]
  (t/selection-cfg
   {:name "Wonderous Inventions"
    :min 1
    :max 1
    :ref [:class :artificer :wonderous-inventions]
    :multiselect? true
    :tags #{:equipment}
    :options (map
              (fn [item-kw]
                (let [{:keys [name summary description] :as item} (mi5e/other-magic-item-map item-kw)]
                  (if (nil? item)
                    (do #?(:cljs (js/console.warn "MAGIC ITEM NOT FOUND" item-kw))))
                  (t/option-cfg
                   {:name name
                    :help (or summary description)
                    :modifiers [(mod5e/magic-item item-kw item {::char-equip5e/quantity 1
                                                                ::char-equip5e/equipped? true})]})))
              item-kws)}))

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
    :weapons {:crossbow-light 1}
    :equipment {:crossbow-bolt 20
                :thieves-tools 1
                :dungeoneers-pack 1}
    :armor-choices [{:name "Armor"
                     :options {:scale-mail 1
                               :studded 1}}]
    :armor {:leather 1}
    :spellcaster true
    :spellcasting {:level-factor 3       
                   :spells-known {3 3
                                  4 1
                                  7 1
                                  8 1
                                  10 1
                                  11 1
                                  14 1
                                  15 1
                                  19 1
                                  20 2}
                   :known-mode :schedule
                   :ability ::char5e/int}
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
                                             (mod/set-mod ?tool-expertise
                                                          key
                                                          nil
                                                          nil
                                                          [(>= (?class-level :artificer) 2)])]}))
                             equip5e/tools)})]
    :modifiers [(mod5e/tool-proficiency :thieves-tools)]
    :levels {2 {:modifiers [(mod5e/tool-expertise :thieves-tools)]
                :selections [(wonderous-invention-selection
                              [:bag-of-holding
                               :cap-of-water-breathing
                               :driftglobe
                               :goggles-of-night
                               :sending-stones])]}
             5 {:selections [(wonderous-invention-selection
                              [:alchemy-jug
                               :helm-of-comprehending-languages
                               :lantern-of-revealing
                               :ring-of-swimming
                               :robe-of-useful-items
                               :rope-of-climbing
                               :wand-of-magic-detection
                               :wand-of-secrets])]}
             10 {:selections [(wonderous-invention-selection
                               [:bag-of-beans
                                :chime-of-opening
                                :decanter-of-endless-water
                                :eyes-of-minute-seeing
                                :folding-boat
                                :handy-haversack])]}
             15 {:selections [(wonderous-invention-selection
                               [:boots-of-striding-and-springing
                                :bracers-of-archery
                                :brooch-of-shielding
                                :broom-of-flying
                                :hat-of-disguise
                                :slippers-of-spider-climbing])]}
             20 {:selections [(wonderous-invention-selection
                               [:eyes-of-the-eagle
                                :gem-of-brightness
                                :gloves-of-missile-snaring
                                :gloves-of-swimming-and-climbing
                                :ring-of-jumping
                                :ring-of-mind-shielding
                                :wings-of-flying])]}}
    :subclass-level 1
    :subclass-title "Artificer Specialty"
    :subclasses [{:name "Alchemist"}
                 {:name "Gunsmith"}]}))

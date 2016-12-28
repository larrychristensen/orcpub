(ns orcpub.core
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [cljs.pprint :as pprint]
            
            [orcpub.template :as t]))

(enable-console-print!)

(def template
  {::t/selections
   [(selection
     "Ability Score Variant"
     [(option
       "Standard Rolls"
       []
       [(modifiers/abilities nil)])])
    (selection
     "Race"
     [(option
       "Elf"
       [(selection
         "Subrace"
         [(option
           "High Elf"
           [(selection
             "Cantrip"
             wizard-cantrip-options)]
           [(modifiers/ability ::char5e/int 1)])])]
       [(modifiers/ability ::char5e/dex 2)])])
    (selection
     "Levels"
     [(option
       "Wizard 1"
       [(selection
         "Cantrips Known"
         wizard-cantrip-options)
        (selection
         "Hit Points"
         [(option
           "Max"
           []
           [(modifiers/max-hit-points 6)])])]
       [(modifiers/saving-throws ::char5e/int ::char5e/wis)
        (modifiers/level :wizard)])
      (option
       "Wizard 2"
       [(selection
         "Arcane Tradition"
         arcane-tradition-options)
        (selection
         "Hit Points"
         [(option
           "Roll"
           []
           [(modifiers/max-hit-points nil)])])]
       [(modifiers/level :wizard)])])]})

(def character
  {::entity/options {:ability-score-variant {::entity/key :standard-rolls
                                             ::entity/value (char5e/abilities 12 13 14 15 16 17)}
                     :race {::entity/key :elf
                            ::entity/options {:subrace {::entity/key :high-elf
                                                        ::entity/options {:cantrip {::entity/key :light}}}}}
                     :levels [{::entity/key :wizard-1
                               ::entity/options {:cantrips-known {::entity/key :acid-splash}
                                                 :hit-points {::entity/key :max}}}
                              {::entity/key :wizard-2
                               ::entity/options {:arcane-tradition {::entity/key :school-of-evocation}
                                                 :hit-points {::entity/key :roll
                                                              ::entity/value 3}}}]}})

(def app-state
  (atom
   {::template template
    ::character character}))

(defmulti read (fn [env key params] key))

(defmethod read :default
  [{:keys [state] :as env} key params]
  (let [st @state]
    (if-let [[_ value] (find st key)]
      {:value value}
      {:value not-found})))

(defmethod read ::template)

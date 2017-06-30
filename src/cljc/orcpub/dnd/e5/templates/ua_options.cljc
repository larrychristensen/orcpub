(ns orcpub.dnd.e5.templates.ua-options
  (:require [orcpub.dnd.e5.modifiers :as mod5e]
            [orcpub.dnd.e5.character :as char5e]
            [orcpub.dnd.e5.options :as opt5e]
            [orcpub.dnd.e5.units :as units5e]))

(def ua-revised-subclasses-kw :ua-revised-subclasses)

(def arcane-archer-option-cfg
  {:name "Arcane Archer"
   :levels {3 {:modifiers [opt5e/ua-al-illegal
                           (mod5e/trait-cfg
                            {:name "Magic Arrow"
                             :page 3
                             :source ua-revised-subclasses-kw
                             :summary "when you fire a non-magical arrow, you can temporarily make it magic with +1 bonus"})
                           (mod5e/dependent-trait
                            {:name "Arcane Shot"
                             :page 3
                             :source ua-revised-subclasses-kw
                             :frequency (units5e/rests
                                         (mod5e/level-val
                                          (?class-level :fighter)
                                          {7 3
                                           10 4
                                           15 5
                                           18 6
                                           :default 2}))
                             :summary "when you fire a magic arrow as part of an Attack, you can use an Arcane Shot Option"})
                           (mod5e/dependent-trait
                            {:name "Arcane Shot: Banishing Arrow"
                             :page 3
                             :source ua-revised-subclasses-kw
                             :summary (str "If the arrow hits, banish the creature until the end of it's next turn, it's speed becomes 0 and it is incapacitated unless it succeeds on a DC " (?spell-save-dc ::char5e/int) " CHA save. " (if (>= (?class-level :fighter) 18) " The target also takes 2d6 force damage."))})
                           (mod5e/dependent-trait
                            {:name "Arcane Shot: Brute Bane Arrow"
                             :page 3
                             :source ua-revised-subclasses-kw
                             :summary (str "If the arrow hits, deal " (if (>= (?class-level :fighter) 18) 4 2) "d6 extra necrotic damage to the target, if it fails a DC " (?spell-save-dc ::char5e/int) " CON save, it's attacks deal half damage until start of your next turn")})
                           (mod5e/dependent-trait
                            {:name "Arcane Shot: Bursting Arrow"
                             :page 3
                             :source ua-revised-subclasses-kw
                             :summary (str "if the arrow hits, deal and extra " (if (>= (?class-level :fighter) 18) 4 2) "d6 force damage to creatures within 10 ft. of the target")})
                           (mod5e/dependent-trait
                            {:name "Arcane Shot: Grasping Arrow"
                             :page 3
                             :source ua-revised-subclasses-kw
                             :summary (let [die-count (if (>= (?class-level :fighter) 18) 4 2)]
                                        (str "if the arrow hits, grasping brambles deal an extra " die-count "d6 poison damage, target's speed is reduced by 10 ft. and takes " die-count "d6 slashing damage the first time it moves on each of it's turns. The brambles can be removed with a DC " (?spell-save-dc ::char5e/int) " STR save."))})
                           (mod5e/dependent-trait
                            {:name "Arcane Shot: Mind Scrambling Arrow"
                             :page 4
                             :source ua-revised-subclasses-kw
                             :summary (str "if the arrow hits, deal an extra " (if (>= (?class-level :fighter) 18) 4 2) "d6 psychic damage, and the target must succeed on a DC " (?spell-save-dc ::char5e/int) " WIS save or cannot harm an ally of your choosing")})
                           (mod5e/dependent-trait
                            {:name "Arcane Shot: Piercing Arrow"
                             :page 4
                             :source ua-revised-subclasses-kw
                             :summary (str "instead of an attack roll, you deal the arrows damage to all creatures within a 1 ft. X 30 ft. line plus an extra " (if (>= (?class-level :fighter) 18) 2 1) "d6 piercing damage, half damage on successful DC " (?spell-save-dc ::char5e/int) " DEX save")})
                           (mod5e/dependent-trait
                            {:name "Arcane Shot: Seeking Arrow"
                             :page 4
                             :source ua-revised-subclasses-kw
                             :summary (str "instead of an attack roll, choose 1 creature you have seen in the past minute, it must succeed on a DC " (?spell-save-dc ::char5e/int) " DEX save or be hit by the arrow, which can move around corners, taking the arrow's damage plus an extra " (if (>= (?class-level :fighter) 18) 2 1) "d6 force damage, half on a successful save")})
                           (mod5e/dependent-trait
                            {:name "Arcane Shot: Shadow Arrow"
                             :page 4
                             :source ua-revised-subclasses-kw
                             :duration units5e/rounds-1
                             :summary (str "if the arrow hits, deal an extra " (if (>= (?class-level :fighter) 18) 4 2) "d6 psychic damage and target must succeed on a DC " (?spell-save-dc ::char5e/int) " WIS save or cannot see beyond 5 ft")})]
               :selections [(opt5e/skill-selection [:arcana :nature] 1)]}
            7 {:modifiers [(mod5e/bonus-action
                            {:name "Curving Shot"
                             :page 3
                             :source ua-revised-subclasses-kw
                             :summary "when you miss with a magic arrow, you may reroll the attack against another target within 60 ft. of the original"})]}
            15 {:modifiers [(mod5e/trait-cfg
                             {:page 3
                              :source ua-revised-subclasses-kw
                              :name "Ever-Ready Shot"
                              :summary "Regain a use of Arcane Shot if you roll initiative and have none"})]}}})

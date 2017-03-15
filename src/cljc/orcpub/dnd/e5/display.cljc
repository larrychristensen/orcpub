(ns orcpub.dnd.e5.display
  (:require [clojure.string :as s]
            [orcpub.common :as common]))

(def sources
  {:phb "PHB"
   :vgm "VGM"
   :scag "SCAG"})

(defn unit-amount-description [{:keys [units amount singular plural] :or {amount 1}}]
  (str amount " " (if (not= 1 amount)
                    (if plural
                      (common/safe-name plural)
                      (str (common/safe-name units) "s"))
                    (if singular
                      (common/safe-name singular)
                      (str (common/safe-name units))))))

(defn source-description [source page]
  (str "see " (sources (or source :phb)) " " page))

(defn frequency-description [{:keys [units amount] :or {amount 1}}]
  (str
   (case amount
     1 "once"
     2 "twice"
     (str amount " times"))
   "/"
   (s/replace (common/safe-name units) #"-" " ")))

(defn attack-description [{:keys [description attack-type area-type damage-type damage-die damage-die-count damage-modifier save save-dc page source] :as attack}]
  (prn "SAVE" attack)
  (str
   (if description (str description ", "))
   (case attack-type
     :area (case area-type
             :line (str (:line-width attack) " x " (:line-length attack) " ft. line, ")
             :cone (str (:length attack) " ft. cone, ")
             nil)
     "melee, ")
   damage-die-count "d" damage-die (if damage-modifier (common/mod-str damage-modifier))
   " "
   (if damage-type (common/safe-name damage-type))
   " damage"
   (if save (str ", DC" save-dc " " (common/safe-name save) " save"))
   (if source (str " (" (source-description source page) ")"))))

(defn action-description [{:keys [description summary source page duration range frequency]}]
  (str
   (or summary description)
   (if (or range duration frequency page)
     (str
      " ("
      (s/join ", "
              (remove
               nil?
               [(if range (str "range " (unit-amount-description range)))
                (if duration (str "lasts " (unit-amount-description duration)))
                (if frequency (str "use " (frequency-description frequency)))
                (if page (source-description source page))]))
      ")"))))

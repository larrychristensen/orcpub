(ns orcpub.dnd.e5.display)

(def sources
  {:phb "PHB"
   :vgm "Volo's Guide to Monsters"
   :scag "Sword Coast Adventurer's Guide"})

(defn unit-amount-description [units amount]
  (str amount " " (name units) (if (not= 1 amount) "s")))

(defn source-description [source page]
  (if page (str " (" (or (sources source) :phb) " " page ")")))

(defn attack-description [{:keys [description area-type damage-type damage-die damage-die-count save save-dc page source] :as attack}]
  (str
   (if description (str description ", "))
   (case area-type
     :line (str (:line-width attack) " x " (:line-length attack) " ft. line, ")
     :cone (str (:length attack) " ft. cone, ")
     nil)
   damage-die-count "d" damage-die
   " "
   (clojure.core/name damage-type)
   " damage"
   (if save (str ", DC" save-dc " " (clojure.core/name save) " save"))
   (source-description source page)))

(defn action-description [{:keys [description source page] {:keys [units amount]} :duration}]
  (str
   description
   " for " (unit-amount-description units (or amount 1))
   (source-description source page)))

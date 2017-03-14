(ns orcpub.dnd.e5.display)

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
   (if page (str " (" (or source "PHB ") page ")"))))

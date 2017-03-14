(ns orcpub.dnd.e5.display)

(def sources
  {:phb "PHB"
   :vgm "VGM"
   :scag "SCAG"})

(defn unit-amount-description [{:keys [units amount] :or {amount 1}}]
  (str amount " " (name units) (if (not= 1 amount) "s")))

(defn source-description [source page]
  (if page (str "see " (or (sources source) :phb) " " page)))

(defn frequency-description [{:keys [units amount] :or {amount 1}}]
  (str
   (case amount
     1 "once"
     2 "twice"
     (str amount " times"))
   "/"
   (name units)))

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

(defn action-description [{:keys [description source page duration frequency]}]
  (str
   description
   " ("
   (if duration (str "lasts " (unit-amount-description duration) ", "))
   (if frequency (str "use " (frequency-description frequency) ", "))
   (source-description source page)
   ")."))

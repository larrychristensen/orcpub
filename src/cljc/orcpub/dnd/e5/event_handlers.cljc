(ns orcpub.dnd.e5.event-handlers
  (:require [orcpub.entity :as entity]))

(defn set-class-level [character [_ class-index new-highest-level]]
  (update-in
   character
   [::entity/options :class class-index ::entity/options :levels]
   (fn [levels]
     (let [current-highest-level (count levels)]
       (prn "LEVELS META" (meta levels) new-highest-level levels)
       (with-meta ;; ensure that db/id meta gets copied over
         (cond
           (> new-highest-level current-highest-level)
           (vec (concat levels (map
                                (fn [lvl] {::entity/key (keyword (str "level-" (inc lvl)))})
                                (range current-highest-level new-highest-level))))
         
           (< new-highest-level current-highest-level)
           (vec (take new-highest-level levels))
         
           :else levels)
         (meta levels))))))

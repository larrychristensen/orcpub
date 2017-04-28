(ns orcpub.dnd.e5.db
  (:require [orcpub.dnd.e5.template :as t5e]
            [re-frame.core :as re-frame]))

(def local-storage-character-key "char-meta")

(def default-value
  {:builder {:character {:tab #{:build :options}}}
   :character t5e/character
   :template t5e/template
   :plugins t5e/plugins})

(defn character->local-store [character]
  (.setItem js/window.localStorage local-storage-character-key (str character)))

(def tab-path [:builder :character :tab])

(re-frame/reg-cofx
  :local-store-character
  (fn [cofx _]
      "Read in character from localstore, and process into a map we can merge into app-db."
      (assoc cofx :local-store-character
             (some->> (.getItem js/window.localStorage local-storage-character-key)
                      (cljs.reader/read-string)))))

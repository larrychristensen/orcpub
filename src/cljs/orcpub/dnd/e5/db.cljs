(ns orcpub.dnd.e5.db
  (:require [orcpub.dnd.e5.template :as t5e]
            [re-frame.core :as re-frame]
            [orcpub.entity :as entity]
            [cljs.spec :as spec]))

(def local-storage-character-key "char-meta")

(def default-value
  {:builder {:character {:tab #{:build :options}}}
   :character t5e/character
   :template t5e/template
   :plugins t5e/plugins})

(defn character->local-store [character]
  (.setItem js/window.localStorage local-storage-character-key (str character)))

(def tab-path [:builder :character :tab])

(defn get-stored-character []
  (let [stored-str (.getItem js/window.localStorage local-storage-character-key)]
    (if stored-str
      (try (cljs.reader/read-string stored-str)
           (catch js/Object e (js/console.warn "UNREADABLE CHARACTER FOUND" stored-str))))))

(re-frame/reg-cofx
  :local-store-character
  (fn [cofx _]
      "Read in character from localstore, and process into a map we can merge into app-db."
      (assoc cofx :local-store-character
             (let [stored-character (get-stored-character)]
               (if stored-character
                 (if (spec/valid? ::entity/raw-entity stored-character)
                   stored-character
                   (js/console.warn "INVALID CHARACTER FOUND, IGNORING" (spec/explain-data ::entity/raw-entity stored-character))))))))

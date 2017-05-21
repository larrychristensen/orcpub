(ns orcpub.dnd.e5.db
  (:require [orcpub.route-map :as route-map]
            [orcpub.user-agent :as user-agent]
            [orcpub.dnd.e5.template :as t5e]
            [orcpub.dnd.e5.character :as char5e]
            [re-frame.core :as re-frame]
            [orcpub.entity :as entity]
            [cljs.spec :as spec]
            [cljs.reader :as reader]
            [bidi.bidi :as bidi]
            [cljs-http.client :as http]))

(def local-storage-character-key "char-meta")
(def local-storage-user-key "user")

(def default-route route-map/dnd-e5-char-builder-route)

(defn parse-route []
  (let [{:keys [handler] :as parsed} (bidi/match-route route-map/routes js/window.location.pathname)]
    (if handler
      handler
      default-route)))

(def default-character (char5e/set-class t5e/character :barbarian 0 t5e/barbarian-option))

(def default-value
  {:builder {:character {:tab #{:build :options}}}
   :character default-character
   :template t5e/template
   :plugins t5e/plugins
   :locked-components #{}
   :route (parse-route)
   :route-history (list default-route)
   :return-route default-route
   :registration-form {:send-updates? true}
   :device-type (user-agent/device-type)})

(defn character->local-store [character]
  (.setItem js/window.localStorage local-storage-character-key (str character)))

(defn user->local-store [user-data]
  (.setItem js/window.localStorage local-storage-user-key (str user-data)))

(def tab-path [:builder :character :tab])

(defn get-local-storage-item [local-storage-key]
  (if-let [stored-str (.getItem js/window.localStorage local-storage-key)]
    (try (reader/read-string stored-str)
         (catch js/Object e (js/console.warn "UNREADABLE ITEM FOUND" local-storage-key stored-str)))))

(defn reg-local-store-cofx [key local-storage-key item-spec & [item-fn]]
  (re-frame/reg-cofx
   key
   (fn [cofx _]
     (assoc cofx
            key
            (if-let [stored-item (get-local-storage-item local-storage-key)]
              (if (spec/valid? item-spec stored-item)
                (if item-fn (item-fn stored-item) stored-item)
                (js/console.warn "INVALID ITEM FOUND, IGNORING" key (spec/explain-data item-spec stored-item))))))))

(reg-local-store-cofx
 :local-store-character
 local-storage-character-key
 ::entity/raw-entity
 (fn [char]
   (if (spec/valid? ::char5e/unnamespaced-character char)
     (char5e/add-namespaces char)
     char)))

(spec/def ::username string?)
(spec/def ::email string?)
(spec/def ::token string?)
(spec/def ::user-data (spec/keys :req-un [::username ::email]))
(spec/def ::user (spec/keys :req-un [::user-data ::token]))

(reg-local-store-cofx
 :local-store-user
 local-storage-user-key
 ::user)

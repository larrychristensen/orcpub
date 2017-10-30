(ns orcpub.dnd.e5.db
  (:require [orcpub.route-map :as route-map]
            [orcpub.user-agent :as user-agent]
            [orcpub.dnd.e5 :as e5]
            [orcpub.dnd.e5.template :as t5e]
            [orcpub.dnd.e5.character :as char5e]
            [orcpub.dnd.e5.backgrounds :as bg5e]
            [orcpub.dnd.e5.feats :as feats5e]
            [orcpub.dnd.e5.races :as race5e]
            [orcpub.dnd.e5.magic-items :as mi5e]
            [orcpub.dnd.e5.spells :as spells5e]
            [orcpub.dnd.e5.equipment :as equip5e]
            [re-frame.core :as re-frame]
            [orcpub.entity :as entity]
            [orcpub.entity.strict :as se]
            [cljs.spec.alpha :as spec]
            [cljs.reader :as reader]
            [bidi.bidi :as bidi]
            [cljs-http.client :as http]
            [cljs.pprint :refer [pprint]]))

(def local-storage-character-key "character")
(def local-storage-user-key "user")
(def local-storage-magic-item-key "magic-item")
(def local-storage-spell-key "spell")
(def local-storage-background-key "background")
(def local-storage-feat-key "feat")
(def local-storage-race-key "race")
(def local-storage-subrace-key "subrace")
(def local-storage-plugins-key "plugins")

(def default-route route-map/dnd-e5-char-builder-route)

(defn parse-route []
  (let [route (if js/window.location
                (bidi/match-route route-map/routes js/window.location.pathname))]
    (if route
      route
      default-route)))

(def default-character (char5e/set-class t5e/character :barbarian 0 t5e/barbarian-option))

(def default-spell {:level 0
                    :school "abjuration"})

(def default-background {:traits []})

(def default-feat {:ability-increases #{}
                   :prereqs #{}})

(def default-race {:size :medium
                   :speed 30
                   :languages #{}
                   :traits []})

(def default-subrace {:traits []})

(def default-value
  {:builder {:character {:tab #{:build :options}}}
   :character default-character
   :template t5e/template
   #_:plugins #_t5e/plugins
   :locked-components #{}
   :route (parse-route)
   :route-history (list default-route)
   :return-route default-route
   :registration-form {:send-updates? true}
   :device-type (user-agent/device-type)
   ::spells5e/builder-item default-spell
   ::bg5e/builder-item default-background
   ::feats5e/builder-item default-feat
   ::race5e/builder-item default-race
   ::race5e/subrace-builder-itme default-subrace})

(defn set-item [key value]
  (try
    (.setItem js/window.localStorage key value)
    (catch js/Object e (prn "FAILED SETTING LOCALSTORAGE ITEM"))))

(defn character->local-store [character]
  (if js/window.localStorage
    (set-item local-storage-character-key
              (str (assoc (char5e/to-strict character)
                          :changed
                          (:changed character))))))

(defn user->local-store [user-data]
  (if js/window.localStorage
    (set-item local-storage-user-key (str user-data))))

(defn magic-item->local-store [magic-item]
  (if js/window.localStorage
    (set-item local-storage-magic-item-key (str magic-item))))

(defn spell->local-store [spell]
  (if js/window.localStorage
    (set-item local-storage-spell-key (str spell))))

(defn background->local-store [background]
  (if js/window.localStorage
    (set-item local-storage-background-key (str background))))

(defn feat->local-store [feat]
  (if js/window.localStorage
    (set-item local-storage-feat-key (str feat))))

(defn race->local-store [race]
  (if js/window.localStorage
    (set-item local-storage-race-key (str race))))

(defn subrace->local-store [subrace]
  (if js/window.localStorage
    (set-item local-storage-subrace-key (str subrace))))

(defn plugins->local-store [plugins]
  (if js/window.localStorage
    (set-item local-storage-plugins-key (str plugins))))

(def tab-path [:builder :character :tab])

(defn get-local-storage-item [local-storage-key]
  (if-let [stored-str (if js/window.localStorage
                        (.getItem js/window.localStorage local-storage-key))]
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
                (if item-fn
                  (item-fn stored-item)
                  stored-item)
                (do
                  (js/console.warn "INVALID ITEM FOUND, IGNORING")
                  (pprint (spec/explain-data item-spec stored-item)))))))))

(reg-local-store-cofx
 :local-store-character
 local-storage-character-key
 ::se/entity
 (fn [char]
   (assoc
    (char5e/from-strict char)
    :changed
    (:changed char))))

(spec/def ::username string?)
(spec/def ::email string?)
(spec/def ::token string?)
(spec/def ::theme string?)
(spec/def ::user-data (spec/keys :req-un [::username ::email]))
(spec/def ::user (spec/keys :opt-un [::user-data ::token ::theme]))

(reg-local-store-cofx
 :local-store-user
 local-storage-user-key
 ::user)

(reg-local-store-cofx
 :local-store-magic-item
 local-storage-magic-item-key
 ::mi5e/internal-magic-item)

(def musical-instrument-choice-cfg
  {:name "Musical Instrument"
   :options (zipmap (map :key equip5e/musical-instruments) (repeat 1))})

#_(def test-plugins
  {"EE" {:orcpub.dnd.e5/spell-lists {:bard
                                     {2 [:dust-devil]}}
         :orcpub.dnd.e5/backgrounds {:charlatan {:name "Charlatan"
                                                 :help "You have a history of being able to work people to your advantage."
                                                 :traits [{:name "False Identity"
                                                           :page 128
                                                           :summary "you have a false identity; you can forge documents"}]
                                                 :profs {:skill {:deception true :sleight-of-hand true}
                                                         :tool {:disguise-kit true :forgery-kit true}}
                                                 :equipment {:clothes-fine 1
                                                             :disguise-kit 1
                                                             :pouch 1}
                                                 :treasure {:gp 15}}}
         :orcpub.dnd.e5/spells {:dust-devil {:name "Dust Devil"
                                             :option-pack "EE"
                                             :key :dust-devil
                                             :school "conjuration"
                                             :level 2
                                             :casting-time "actions-1"
                                             :range "60 feet"
                                             :duration "conc-1-min"
                                             :spell-lists {:bard true}
                                             :components {:verbal true :somatic true :material true :material-component "fur wrapped in cloth"}
                                             :page 17
                                             :source :ee
                                             :summary "Conjure dust devil"}}}})

(reg-local-store-cofx
 ::e5/plugins
 local-storage-plugins-key
 ::e5/plugins)


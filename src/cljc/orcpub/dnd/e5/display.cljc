(ns orcpub.dnd.e5.display
  (:require [clojure.string :as s]
            [orcpub.common :as common]
            [orcpub.dnd.e5.character.equipment :as char-equip]))

(def phb-url "https://www.amazon.com/gp/product/0786965606/ref=as_li_tl?ie=UTF8&tag=orcpub-20&camp=1789&creative=9325&linkCode=as2&creativeASIN=0786965606&linkId=9cd9647802c714f226bd591d61058143")

(def scag-url "https://www.amazon.com/gp/product/0786965800/ref=as_li_tl?ie=UTF8&tag=orcpub-20&camp=1789&creative=9325&linkCode=as2&creativeASIN=0786965800&linkId=9b93efa0fc7239ebbf005d0b17367233")

(def vgm-url "https://www.amazon.com/gp/product/0786966017/ref=as_li_tl?ie=UTF8&tag=orcpub-20&camp=1789&creative=9325&linkCode=as2&creativeASIN=0786966017&linkId=506a1b33174f884dcec5db8c6c07ad31")

(def dmg-url "https://www.amazon.com/gp/product/0786965622/ref=as_li_tl?ie=UTF8&camp=1789&creative=9325&creativeASIN=0786965622&linkCode=as2&tag=orcpub-20&linkId=7d3e39946045872d4da58bd9d14a7a31")

(def sources
  {:phb {:abbr "PHB"
         :url phb-url}
   :vgm {:abbr "VGM"
         :url vgm-url}
   :scag {:abbr "SCAG"
          :url scag-url}
   :dmg {:abbr "DMG"
         :url dmg-url}
   :ee {:abbr "EE"
        :url "https://media.wizards.com/2015/downloads/dnd/EE_PlayersCompanion.pdf"}
   :cos {:abbr "COS"
         :url "https://www.amazon.com/gp/product/0786965983/ref=as_li_qf_sp_asin_il_tl?ie=UTF8&tag=orcpub-20&camp=1789&creative=9325&linkCode=as2&creativeASIN=0786965983&linkId=80c8baab14ecdb5c31f29d9c5b594c83"}
   :ua-revised-ranger {:abbr "UA-Revised-Ranger"
                       :url "http://media.wizards.com/2016/dnd/downloads/UA_RevisedRanger.pdf"}
   :ua-artificer {:abbr "UA-Artificer"
                  :url "http://www.dmsguild.com/product/213032/Unearthed-Arcana-The-Artificer-Class-5e"}
   :ua-eberron {:abbr "UA-Eberron"
                :url "http://media.wizards.com/2015/downloads/dnd/UA_Eberron_v1.1.pdf"}
   :ua-waterborne {:abbr "UA-Waterborne"
                   :url "http://media.wizards.com/2015/downloads/dnd/UA_Waterborne_v3.pdf"}
   :ua-mystic {:abbr "UA-Mystic"
               :url "http://media.wizards.com/2017/dnd/downloads/UAMystic3.pdf"}
   :ua-revised-subclasses {:abbr "UA-Revised-Subclasses"
                           :url "http://media.wizards.com/2017/dnd/downloads/UA-RevisedSubclasses.pdf"}
   :ua-trio-of-subclasses {:abbr "UA-Trio-of-Subclasses"
                           :url "http://media.wizards.com/2017/dnd/downloads/UAThreeSubclasses.pdf"}})

(def plural-map
  {:feet :feet})

(defn equipment-name [equipment-map equipment-kw]
  (or (:name (equipment-map equipment-kw))
      (if (string? equipment-kw)
        equipment-kw
        (common/kw-to-name equipment-kw))))

(defn unit-amount-description [{:keys [units amount singular plural] :or {amount 1 plural (plural-map units)}}]
  (str amount " " (if (not= 1 amount)
                    (if plural
                      (common/safe-name plural)
                      (str (common/safe-name units) "s"))
                    (if singular
                      (common/safe-name singular)
                      (str (common/safe-name units))))))

(defn duration-description [{:keys [concentration] :as duration}]
  (str (if concentration "conc. ") (unit-amount-description duration)))

(defn get-source [source]
  (sources (or source :phb)))

(defn source-description [source page]
  (str "see " (:abbr (get-source source)) " " page))

(defn frequency-description [{:keys [units amount] :or {amount 1}}]
  (str
   (case amount
     1 "once"
     2 "twice"
     (str amount " times"))
   "/"
   (s/replace (common/safe-name units) #"-" " ")))

(defn attack-description [{:keys [description summary attack-type area-type damage-type damage-die damage-die-count damage-modifier save save-dc page source] :as attack}]
  (let [summary (or summary description)]
    (str
     (if summary (str summary ", "))
     (case attack-type
       :area (case area-type
               :line (str (:line-width attack) " x " (:line-length attack) " ft. line, ")
               :cone (str (:length attack) " ft. cone, ")
               nil)
       :ranged "ranged, "
       "melee, ")
     damage-die-count "d" damage-die (if damage-modifier (common/mod-str damage-modifier))
     " "
     (if damage-type (common/safe-name damage-type))
     " damage"
     (if save (str ", DC" save-dc " " (common/safe-name save) " save"))
     (if page (str " (" (source-description source page) ")")))))

(defn action-description [{:keys [description summary source page duration range frequency qualifier]}]
  (str
   (or summary description)
   (if (or range duration frequency page)
     (str
      " ("
      (s/join ", "
              (remove
               nil?
               [qualifier
                (if range (str "range " (unit-amount-description range)))
                (if duration (str "lasts " (duration-description duration)))
                (if frequency (str "use " (frequency-description frequency)))
                (if page (source-description source page))]))
      ")"))))

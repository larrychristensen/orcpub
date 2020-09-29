(ns orcpub.dnd.e5.display
  (:require [clojure.string :as s]
            [orcpub.common :as common]
            [orcpub.dnd.e5.weapons :as weapons]
            [orcpub.dnd.e5.character.equipment :as char-equip]))

#_(def phb-url "https://www.amazon.com/gp/product/0786965606/ref=as_li_tl?ie=UTF8&tag=orcpub-20&camp=1789&creative=9325&linkCode=as2&creativeASIN=0786965606&linkId=9cd9647802c714f226bd591d61058143")

#_(def scag-url "https://www.amazon.com/gp/product/0786965800/ref=as_li_tl?ie=UTF8&tag=orcpub-20&camp=1789&creative=9325&linkCode=as2&creativeASIN=0786965800&linkId=9b93efa0fc7239ebbf005d0b17367233")

#_(def vgm-url "https://www.amazon.com/gp/product/0786966017/ref=as_li_tl?ie=UTF8&tag=orcpub-20&camp=1789&creative=9325&linkCode=as2&creativeASIN=0786966017&linkId=506a1b33174f884dcec5db8c6c07ad31")

#_(def dmg-url "https://www.amazon.com/gp/product/0786965622/ref=as_li_tl?ie=UTF8&camp=1789&creative=9325&creativeASIN=0786965622&linkCode=as2&tag=orcpub-20&linkId=7d3e39946045872d4da58bd9d14a7a31")

#_(def sources
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
   :ua-cleric {:abbr "UA-Cleric"
               :url "http://media.wizards.com/2016/dnd/downloads/UA_Cleric.pdf"}
   :ua-artificer {:abbr "UA-Artificer"
                  :url "http://www.dmsguild.com/product/213032/Unearthed-Arcana-The-Artificer-Class-5e"}
   :ua-eberron {:abbr "UA-Eberron"
                :url "http://media.wizards.com/2015/downloads/dnd/UA_Eberron_v1.1.pdf"}
   :ua-race-feats {:abbr "UA-Race-Feats"
                   :url "http://media.wizards.com/2017/dnd/downloads/RJSJC2017_04UASkillFeats_24v10.pdf"}
   :ua-skill-feats {:abbr "UA-Skill-Feats"
                    :url "http://media.wizards.com/2017/dnd/downloads/UA-SkillFeats.pdf"}
   :ua-waterborne {:abbr "UA-Waterborne"
                   :url "http://media.wizards.com/2015/downloads/dnd/UA_Waterborne_v3.pdf"}
   :ua-mystic {:abbr "UA-Mystic"
               :url "http://media.wizards.com/2017/dnd/downloads/UAMystic3.pdf"}
   :ua-bard {:abbr "UA-Bard"
             :url "https://media.wizards.com/2016/dnd/downloads/UA_Bard.pdf"}
   :ua-revised-subclasses {:abbr "UA-Revised-Subclasses"
                           :url "http://media.wizards.com/2017/dnd/downloads/UA-RevisedSubclasses.pdf"}
   :ua-trio-of-subclasses {:abbr "UA-Trio-of-Subclasses"
                           :url "http://media.wizards.com/2017/dnd/downloads/UAThreeSubclasses.pdf"}
   :ua-revised-class-options {:abbr "UA-Revised-Class-Options"
                              :url "http://media.wizards.com/2017/dnd/downloads/June5UA_RevisedClassOptv1.pdf"}
   :ua-feats {:abbr "UA-Feats"
              :url "https://media.wizards.com/2016/downloads/DND/UA-Feats-V1.pdf"}
   :ua-warlock-and-wizard {:abbr "UA-Warlock-and-Wizard"
                           :url "http://media.wizards.com/2017/dnd/downloads/20170213_Wizrd_Wrlck_UAv2_i48nf.pdf"}
   :ua-fighter {:abbr "UA-Fighter"
                :url "http://media.wizards.com/2016/dnd/downloads/2016_Fighter_UA_1205_1.pdf"}
   :ua-sorcerer {:abbr "UA-Sorcerer"
                 :url "https://media.wizards.com/2017/dnd/downloads/26_UASorcererUA020617s.pdf"}
   :ua-gothic-heroes {:abbr "UA-Gothic-Heroes"
                      :url "https://dnd.wizards.com/sites/default/files/media/upload/articles/UA%20Gothic%20Characters.pdf"}
   :ua-starter-spells {:abbr "UA-Starter-Spells"
                       :url "http://media.wizards.com/2017/dnd/downloads/UA-Starter-Spells.pdf"}})

(def plural-map
  {:feet :feet})

(defn equipment-name [equipment-map equipment-kw]
  (or (:name (equipment-map equipment-kw))
      (if (string? equipment-kw)
        equipment-kw
        (common/kw-to-name equipment-kw true))))

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

#_(defn get-source [source]
  (sources (or source :phb)))

#_(defn source-description [source page]
  (str "see " (:abbr (get-source source)) " " page))

(defn frequency-description [{:keys [units amount] :or {amount 1}}]
  (str
   (case amount
     1 "once"
     2 "twice"
     (str amount " times"))
   "/"
   (s/replace (common/safe-name units) #"-" " ")))

(defn attack-description-short [{:keys [description summary attack-type area-type] :as attack}]
  (let [summary (or summary description)]
    (str
     (when summary (str summary ", "))
     (case attack-type
       :area (case area-type
               :line (str (:line-width attack) " x " (:line-length attack) " ft. line")
               :cone (str (:length attack) " ft. cone")
               nil)
       :ranged "ranged"
       "melee"))))

(defn attack-description [{:keys [description summary attack-type area-type damage-type damage-die damage-die-count damage-modifier attack-modifier save save-dc page source] :as attack}]
  (let [attack-mod-str (when attack-modifier (str (common/bonus-str attack-modifier) " to hit, "))]
    (str
     (attack-description-short attack)
     (case attack-type
       :area ", "
       :ranged (str ", " attack-mod-str)
       (str ", " attack-mod-str))
     (or damage-die-count
         (::weapons/damage-die-count attack))
     "d"
     (or damage-die
         (::weapons/damage-die attack))
     (when damage-modifier (common/mod-str damage-modifier))
     " "
     (when damage-type (common/safe-name damage-type))
     " damage"
     (when save (str ", DC" save-dc " " (common/safe-name save) " save"))
     #_(when page (str " (" (source-description source page) ")")))))

(defn action-description [{:keys [description summary source page duration range frequency qualifier]}]
  (str
   (or summary description)
   (if (or range duration frequency)
     (str
      " ("
      (s/join ", "
              (remove
               nil?
               [qualifier
                (if range (str "range " (unit-amount-description range)))
                (if duration (str "lasts " (duration-description duration)))
                (if frequency (str "use " (frequency-description frequency)))
                #_(if page (source-description source page))]))
      ")"))))

(ns orcpub.dnd.e5.warlock-test
  (:require [clojure.test :refer :all]
            [orcpub.entity :as entity]
            [orcpub.entity-spec :as es]
            [orcpub.template :as t]
            [orcpub.dnd.e5.template :as t5e]
            [orcpub.dnd.e5.character :as char5e]
            [clojure.set :refer [union]]))

(def base-abilities
  {:orcpub.dnd.e5.character/str 10,
   :orcpub.dnd.e5.character/dex 11,
   :orcpub.dnd.e5.character/con 11,
   :orcpub.dnd.e5.character/int 15,
   :orcpub.dnd.e5.character/wis 14,
   :orcpub.dnd.e5.character/cha 15})

(def selected-skill-profs
  #{:intimidation :history})

(def warlock-with-book-of-ancient-secrets
  {:orcpub.entity/options
   {:race
    {:orcpub.entity/key :elf,
     :orcpub.entity/options
     {:subrace {:orcpub.entity/key :dark-elf-drow-}}},
    :ability-scores
    {:orcpub.entity/key :standard-roll,
     :orcpub.entity/value
     base-abilities},
    :alignment {:orcpub.entity/key :chaotic-neutral},
    :background
    {:orcpub.entity/key :spy,
     :orcpub.entity/options
     {:tool-proficiency-gaming-set {:orcpub.entity/key :dice-set}}},
    :feats
    [{:orcpub.entity/key :great-weapon-master}
     {:orcpub.entity/key :keen-mind}],
    :class
    [{:orcpub.entity/key :warlock,
      :orcpub.entity/options
      {:skill-proficiency
       (mapv
        (fn [kw] {:orcpub.entity/key kw})
        selected-skill-profs)
       :levels
       [{:orcpub.entity/key :level-1,
         :orcpub.entity/options
         {:otherworldly-patron {:orcpub.entity/key :the-archfey}}}
        {:orcpub.entity/key :level-2,
         :orcpub.entity/options
         {:hit-points
          {:orcpub.entity/key :average, :orcpub.entity/value 5}}}
        {:orcpub.entity/key :level-3,
         :orcpub.entity/options
         {:pact-boon
          {:orcpub.entity/key :pact-of-the-tome,
           :orcpub.entity/options
           {:book-of-shadows-cantrips
            [{:orcpub.entity/key :blade-ward}
             {:orcpub.entity/key :spare-the-dying}
             {:orcpub.entity/key :thorn-whip}]}},
          :hit-points
          {:orcpub.entity/key :average, :orcpub.entity/value 5}}}
        {:orcpub.entity/key :level-4,
         :orcpub.entity/options
         {:asi-or-feat {:orcpub.entity/key :feat},
          :hit-points
          {:orcpub.entity/key :average, :orcpub.entity/value 5}}}
        {:orcpub.entity/key :level-5,
         :orcpub.entity/options
         {:hit-points
          {:orcpub.entity/key :average, :orcpub.entity/value 5}}}
        {:orcpub.entity/key :level-6,
         :orcpub.entity/options
         {:hit-points
          {:orcpub.entity/key :average, :orcpub.entity/value 5}}}
        {:orcpub.entity/key :level-7,
         :orcpub.entity/options
         {:hit-points
          {:orcpub.entity/key :average, :orcpub.entity/value 5}}}
        {:orcpub.entity/key :level-8,
         :orcpub.entity/options
         {:asi-or-feat {:orcpub.entity/key :feat},
          :hit-points
          {:orcpub.entity/key :average, :orcpub.entity/value 5}}}
        {:orcpub.entity/key :level-9,
         :orcpub.entity/options
         {:hit-points
          {:orcpub.entity/key :average, :orcpub.entity/value 5}}}
        {:orcpub.entity/key :level-10,
         :orcpub.entity/options
         {:hit-points
          {:orcpub.entity/key :average, :orcpub.entity/value 5}}}],
       :eldritch-invocations
       [{:orcpub.entity/key :book-of-ancient-secrets,
         :orcpub.entity/options
         {:book-of-ancient-secrets-rituals
          [{:orcpub.entity/key :detect-poison-and-disease}
           {:orcpub.entity/key :illusory-script}]}}
        {:orcpub.entity/key :agonizing-blast}
        {:orcpub.entity/key :beast-speech}
        {:orcpub.entity/key :eyes-of-the-rune-keeper}
        {:orcpub.entity/key :mire-the-mind}],
       :warlock-cantrips-known
       [{:orcpub.entity/key :friends}
        {:orcpub.entity/key :minor-illusion}
        {:orcpub.entity/key :prestidigitation}
        {:orcpub.entity/key :eldritch-blast}],
       :warlock-spells-known
       [{:orcpub.entity/key :charm-person}
        {:orcpub.entity/key :hellish-rebuke}
        {:orcpub.entity/key :comprehend-languages}
        {:orcpub.entity/key :ray-of-enfeeblement}
        {:orcpub.entity/key :sleep}
        {:orcpub.entity/key :hold-monster}
        {:orcpub.entity/key :dominate-person}
        {:orcpub.entity/key :crown-of-madness}
        {:orcpub.entity/key :fear}
        {:orcpub.entity/key :faerie-fire}],
       :starting-equipment-equipment-pack
       {:orcpub.entity/key :dungeoneers-pack},
       :starting-equipment-simple-weapon {:orcpub.entity/key :dagger},
       :starting-equipment-spellcasting-equipment
       {:orcpub.entity/key :component-pouch},
       :starting-equipment-weapon
       {:orcpub.entity/key :any-simple-weapon,
        :orcpub.entity/options
        {:starting-equipment-simple-weapon
         {:orcpub.entity/key :shortbow}}}}}], 
    :optional-content nil}})

(defn has-spell? [built-char level class-nm spell-key]
  (let [spells-known (char5e/spells-known built-char)]
    (get-in spells-known [level [class-nm spell-key]])))

(def elf-dex-bonus 2)
(def drow-cha-bonus 1)
(def keen-mind-int-bonus 1)
(def elf-skill-profs #{:perception})
(def spy-skill-profs #{:deception :stealth})

(deftest book-of-ancient-secrets
  (let [built-char (entity/build
                    warlock-with-book-of-ancient-secrets
                    (t5e/template
                     (t5e/template-selections nil nil nil)))
        {:keys [::char5e/str
                ::char5e/dex
                ::char5e/con
                ::char5e/int
                ::char5e/wis
                ::char5e/cha]} (char5e/ability-values built-char)
        skill-profs (char5e/skill-proficiencies built-char)]
    (is (has-spell? built-char
                    1
                    "Warlock"
                    :illusory-script))
    (is (= str (base-abilities ::char5e/str)))
    (is (= dex (+ elf-dex-bonus (base-abilities ::char5e/dex))))
    (is (= con (base-abilities ::char5e/con)))
    (is (= int (+ keen-mind-int-bonus (base-abilities ::char5e/int))))
    (is (= wis (base-abilities ::char5e/wis)))
    (is (= cha (+ drow-cha-bonus (base-abilities ::char5e/cha))))
    (is (= (set (keys skill-profs)) (union elf-skill-profs spy-skill-profs selected-skill-profs)))))

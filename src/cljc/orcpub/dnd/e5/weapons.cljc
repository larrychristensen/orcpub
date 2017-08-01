(ns orcpub.dnd.e5.weapons
  (:require [orcpub.common :as common]))

(def weapons
  [{:name "Crossbow, light",
    ::damage-type :piercing,
    ::damage-die 8,
    ::type :simple,
    ::damage-die-count 1,
    ::ranged? true,
    ::range {::min 80, ::max 320},
    :key :crossbow-light
    ::two-handed? true
    ::ammunition? true
    ::link "https://en.wikipedia.org/wiki/Crossbow"}
   {::ranged? true,
    :key :dart,
    :name "Dart",
    ::damage-die-count 1,
    ::type :simple,
    ::damage-type :piercing,
    ::thrown true,
    ::finesse? true,
    ::damage-die 4,
    ::range {::min 20, ::max 60}
    ::link "https://en.wikipedia.org/wiki/Dart_(missile)"}
   {:name "Shortbow",
    ::damage-type :piercing,
    ::damage-die 6,
    ::type :simple,
    ::damage-die-count 1,
    ::ranged? true,
    ::range {::min 80, ::max 320},
    :key :shortbow
    ::two-handed? true
    ::ammunition? true
    ::link "https://en.wikipedia.org/wiki/Bow_and_arrow"}
   {:name "Sling",
    ::damage-type :bludgeoning,
    ::damage-die 4,
    ::type :simple,
    ::damage-die-count 1,
    ::ranged? true,
    ::range {::min 30, ::max 120},
    :key :sling
    ::ammunition? true
    ::link "https://en.wikipedia.org/wiki/Sling_(weapon)"}
   {:name "Club",
    ::damage-type :bludgeoning,
    ::damage-die 4,
    ::damage-die-coun 1,
    ::type :simple,
    ::melee? true,
    ::light? true
    :key :club
    ::link "https://en.wikipedia.org/wiki/Club_(weapon)"}
   {::melee? true,
    :key :dagger,
    :name "Dagger",
    ::damage-die-count 1,
    ::type :simple,
    ::damage-type :piercing,
    ::thrown true,
    ::finesse? true,
    ::damage-die 4,
    ::light? true
    ::range {::min 20, ::max 60}
    ::link "https://en.wikipedia.org/wiki/Dagger"}
   {:name "Greatclub",
    ::damage-type :bludgeoning,
    ::damage-die 8,
    ::damage-die-count 1,
    ::type :simple,
    ::melee? true,
    :key :greatclub
    ::two-handed? true
    ::link "https://en.wikipedia.org/wiki/Club_(weapon)"}
   {:name "Handaxe",
    ::damage-type :slashing,
    ::damage-die 6,
    ::damage-die-count 1,
    ::type :simple,
    ::melee? true,
    ::thrown true,
    ::range {::min 20, ::max 60},
    :key :handaxe
    ::light? true
    ::link "https://en.wikipedia.org/wiki/Battle_axe"}
   {:name "Javelin",
    ::damage-type :piercing,
    ::damage-die 6,
    ::damage-die-count 1,
    ::type :simple,
    ::melee? true,
    ::thrown true,
    ::range {::min 30, ::max 120},
    :key :javelin
    ::link "https://en.wikipedia.org/wiki/Javelin"}
   {:name "Light hammer",
    ::damage-type :bludgeoning,
    ::damage-die 4,
    ::damage-die-count 1,
    ::type :simple,
    ::melee? true,
    ::thrown true,
    ::range {::min 20, ::max 60},
    :key :light-hammer
    ::light? true
    ::link "https://en.wikipedia.org/wiki/War_hammer"}
   {:name "Mace",
    ::damage-type :bludgeoning,
    ::type :simple,
    ::damage-die 6,
    ::damage-die-count 1,
    ::melee? true,
    :key :mace
    ::link "https://en.wikipedia.org/wiki/Mace_(weapon)"}
   {:name "Quarterstaff",
    ::damage-type :bludgeoning,
    ::type :simple,
    ::damage-die 6,
    ::damage-die-count 1,
    ::versatile {::damage-die 8, ::damage-die-count 1},
    ::melee? true,
    :key :quarterstaff
    ::link "https://en.wikipedia.org/wiki/Quarterstaff"}
   {:name "Sickle",
    ::damage-type :slashing,
    ::damage-die 4,
    ::type :simple,
    ::damage-die-count 1,
    ::melee? true,
    :key :sickle
    ::link "https://en.wikipedia.org/wiki/Sickle"}
   {::melee? true,
    ::versatile {::damage-die 8, ::damage-die-count 1},
    :key :spear,
    :name "Spear",
    ::damage-die-count 1,
    ::type :simple,
    ::damage-type :piercing,
    ::thrown true,
    ::damage-die 6,
    ::range {::min 20, ::max 60}
    ::link "https://en.wikipedia.org/wiki/Spear"}
   {:name "Battleaxe",
    ::damage-type :slashing,
    ::damage-die 8,
    ::type :martial,
    ::damage-die-count 1,
    ::versatile {::damage-die 10, ::damage-die-count 1},
    ::melee? true,
    :key :battleaxe
    ::link "https://en.wikipedia.org/wiki/Battle_axe"}
   {:name "Flail",
    ::damage-type :bludgeoning,
    ::damage-die 8,
    ::type :martial,
    ::damage-die-count 1,
    ::melee? true,
    :key :flail
    ::link "https://en.wikipedia.org/wiki/Flail_(weapon)"}
   {:name "Glaive",
    ::damage-type :slashing,
    ::damage-die 10,
    ::type :martial,
    ::damage-die-count 1,
    ::melee? true,
    ::heavy? true,
    ::reach true,
    :key :glaive
    ::two-handed? true
    ::link "https://en.wikipedia.org/wiki/Glaive"}
   {:name "Greataxe",
    ::damage-type :slashing,
    ::damage-die 12,
    ::type :martial,
    ::subtype :axe
    ::damage-die-count 1,
    ::heavy? true,
    ::melee? true,
    :key :greataxe
    ::two-handed? true
    ::link "https://en.wikipedia.org/wiki/Battle_axe"}
   {:name "Greatsword",
    ::subtype :sword
    ::damage-type :slashing,
    ::damage-die 6,
    ::type :martial,
    ::damage-die-count 2,
    ::heavy? true,
    ::melee? true,
    :key :greatsword
    ::two-handed? true
    ::link "https://en.wikipedia.org/wiki/Sword"}
   {:name "Halberd",
    ::damage-type :slashing,
    ::damage-die 10,
    ::type :martial,
    ::damage-die-count 1,
    ::melee? true,
    ::heavy? true,
    ::reach true,
    :key :halberd
    ::two-handed? true
    ::link "https://en.wikipedia.org/wiki/Halberd"}
   {:name "Lance",
    ::damage-type :piercing,
    ::damage-die 12,
    ::type :martial,
    ::damage-die-count 1,
    ::melee? true,
    ::reach true,
    :key :lance
    ::special? true
    ::link "https://en.wikipedia.org/wiki/Lance"}
   {:name "Longsword",
    ::damage-type :slashing,
    ::damage-die 8,
    ::type :martial,
    ::subtype :sword
    ::damage-die-count 1,
    ::versatile {::damage-die 10, ::damage-die-count 1},
    ::melee? true,
    :key :longsword
    ::link "https://en.wikipedia.org/wiki/Sword"}
   {:name "Maul",
    ::damage-type :bludgeoning,
    ::damage-die 6,
    ::type :martial,
    ::damage-die-count 2,
    ::heavy? true,
    ::melee? true,
    :key :maul
    ::two-handed? true
    ::link "https://en.wikipedia.org/wiki/War_hammer"}
   {:name "Morningstar",
    ::damage-type :piercing,
    ::damage-die 8,
    ::type :martial,
    ::damage-die-count 1,
    ::melee? true,
    :key :morningstar
    ::link "https://en.wikipedia.org/wiki/Morning_star_(weapon)"}
   {:name "Pike",
    ::damage-type :piercing,
    ::damage-die 10,
    ::type :martial,
    ::damage-die-count 1,
    ::melee? true,
    ::heavy? true,
    ::reach true,
    :key :pike
    ::two-handed? true
    ::link "https://en.wikipedia.org/wiki/Pike_(weapon)"}
   {:name "Rapier",
    ::damage-type :piercing,
    ::damage-die 8,
    ::type :martial,
    ::subtype :sword
    ::damage-die-count 1,
    ::finesse? true,
    ::melee? true,
    :key :rapier
    ::link "https://en.wikipedia.org/wiki/Rapier"}
   {:name "Scimitar",
    ::damage-type :slashing,
    ::damage-die 6,
    ::type :martial,
    ::subtype :sword
    ::damage-die-count 1,
    ::finesse? true,
    ::melee? true,
    :key :scimitar
    ::light? true
    ::link "https://en.wikipedia.org/wiki/Scimitar"}
   {:name "Shortsword",
    ::damage-type :piercing,
    ::damage-die 6,
    ::type :martial,
    ::subtype :sword
    ::finesse? true,
    ::damage-die-count 1,
    ::melee? true,
    :key :shortsword
    ::light? true
    ::link "https://en.wikipedia.org/wiki/Sword"}
   {::melee? true,
    ::versatile {::damage-die 8, ::damage-die-count 1},
    :key :trident,
    :name "Trident",
    ::damage-die-count 1,
    ::type :martial,
    ::damage-type :piercing,
    ::thrown true,
    ::damage-die 6,
    ::range {::min 20, ::max 60}
    ::link "https://en.wikipedia.org/wiki/Trident"}
   {:name "War pick",
    ::damage-type :piercing,
    ::damage-die 8,
    ::type :martial,
    ::damage-die-count 1,
    ::melee? true,
    :key :war-pick}
   {:name "Warhammer",
    ::damage-type :bludgeoning,
    ::damage-die 8,
    ::type :martial,
    ::damage-die-count 1,
    ::versatile {::damage-die 10, ::damage-die-count 1},
    ::melee? true,
    :key :warhammer
    ::link "https://en.wikipedia.org/wiki/War_hammer"}
   {:name "Whip",
    ::damage-type :slashing,
    ::damage-die 4,
    ::type :martial,
    ::damage-die-count 1,
    ::melee? true,
    ::finesse? true,
    ::reach true,
    :key :whip}
   {:name "Blowgun",
    ::damage-type :piercing,
    ::damage-die 1,
    ::type :martial,
    ::damage-die-count 1,
    ::ranged? true,
    ::range {::min 25, ::max 100},
    ::ammunition? true
    :key :blowgun}
   {:name "Crossbow, hand",
    ::damage-type :piercing,
    ::damage-die 6,
    ::type :martial,
    ::damage-die-count 1,
    ::ranged? true,
    ::range {::min 30, ::max 120},
    ::ammunition? true
    ::light? true
    :key :crossbow-hand}
   {:name "Crossbow, heavy",
    ::damage-type :piercing,
    ::damage-die 10,
    ::type :martial,
    ::damage-die-count 1,
    ::ranged? true,
    ::heavy? true,
    ::range {::min 100, ::max 400},
    :key :crossbow-heavy
    ::ammunition? true
    ::two-handed? true}
   {:name "Thunder Cannon",
    ::damage-type :piercing,
    ::damage-die 6,
    ::type ::special
    ::damage-die-count 2,
    ::ranged? true,
    ::range {::min 150, ::max 500},
    :key :thunder-cannon
    ::two-handed? true}
   {:name "Longbow",
    ::damage-type :piercing,
    ::damage-die 8,
    ::type :martial,
    ::damage-die-count 1,
    ::ranged? true,
    ::heavy? true,
    ::range {::min 150, ::max 600},
    :key :longbow
    ::two-handed? true}
   {:name "Net",
    ::type :martial,
    ::ranged? true,
    ::thrown true,
    ::range {::min 5, ::max 15},
    :key :net
    ::special? true}])

(def ammunition
  (common/add-keys
   [{:name "Arrow" ::type :ammunition :sell-qty 20 :cost {:num 1 :type :gp} :weight "1 lb."}
    {:name "Blowgun needle" ::type :ammunition :sell-qty 50 :cost {:num 1 :type :gp} :weight "1 lb."}
    {:name "Crossbow bolt" ::type :ammunition :sell-qty 20 :cost {:num 1 :type :gp} :weight "1½ lb."}
    {:name "Sling bullet" ::type :ammunition :sell-qty 20 :cost {:num 4 :type :cp} :weight "1½ lb."}]))

(def weapons-map
  (zipmap (map :key weapons) weapons))

(defn weapons-of-type [weapons type]
  (filter #(= type (::type %)) weapons))

(defn martial-weapons [weapons]
  (weapons-of-type weapons :martial))

(defn simple-weapons [weapons]
  (weapons-of-type weapons :simple))

(defn light-melee-weapon? [{:keys [::light? ::melee?]}]
  (and melee? light?))

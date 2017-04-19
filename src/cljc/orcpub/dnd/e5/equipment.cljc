(ns orcpub.dnd.e5.equipment
  (:require [orcpub.common :as common]
            [orcpub.dnd.e5.weapons :as weapons]))

(def musical-instruments
  [{:key :bagpipes
    :name "Bagpipes"}
   {:key :drum
    :name "Drum"}
   {:key :dulcimer
    :name "Dulcimer"}
   {:key :flute
    :name "Flute"}
   {:key :lute
    :name "Lute"}
   {:key :lyre
    :name "Lyre"}
   {:key :horn
    :name "Horn"}
   {:key :pan-flute
    :name "Pan Flute"}
   {:key :shawm
    :name "Shawm"}
   {:key :viol
    :name "Viol"}])

(def artisans-tools
  [{:name "Alchemist's Supplies", :key :alchemists-supplies :icon "fire-bottle"}
   {:name "Brewer's Supplies", :key :brewers-supplies :icon "beer-stein"}
   {:name "Calligrapher's Supplies", :key :calligraphers-supplies :icon "quill-ink"}
   {:name "Carpenter's Tools", :key :carpenters-tools :icon "hand-saw"}
   {:name "Cartographer's Tools", :key :cartographers-tools :icon "compass"}
   {:name "Cobbler's Tools", :key :cobblers-tools :icon "leather-boot"}
   {:name "Cook's Utensils", :key :cooks-utensils :icon "kitchen-knives"}
   {:name "Glassblower's Tools", :key :glassblowers-tools :icon "potion-ball"}
   {:name "Jeweler's Tools", :key :jewelers-tools :icon "cut-diamond"}
   {:name "Leatherworker's Tools", :key :leatherworkers-tools :icon "animal-hide"}
   {:name "Mason's Tools", :key :masons-tools :icon "freemasonry"}
   {:name "Painter's Supplies", :key :painters-supplies :icon "paint-brush"}
   {:name "Potter's Tools", :key :potters-tools :icon "amphora"}
   {:name "Smith's Tools", :key :smiths-tools :icon "anvil-impact"}
   {:name "Tinker's Tools", :key :tinkers-tools :icon "tinker"}
   {:name "Weaver's Tools", :key :weavers-tools :icon "wool"}
   {:name "Woodcarver's Tools", :key :woodcarvers-tools :icon "wood-axe"}])

(def misc-tools
  [{:name "Disguise Kit"
    :key :disguise-kit}
   {:name "Forgery Kit"
    :key :forgery-kit}
   {:name "Herbalism Kit"
    :key :herbalism-kit}
   {:name "Navigator's Tools"
    :key :navigators-tools}
   {:name "Poisoner's Tools"
    :key :poisoners-tools}
   {:name "Thieves' Tools"
    :key :thieves-tools}])

(def gaming-sets
  [{:name "Dice Set"
    :key :dice-set}
   {:name "Dragonchess Set"
    :key :dragonchess-set}
   {:name "Playing Card Set"
    :key :playing-card-set}
   {:name "Three-Dragon Ante Set"
    :key :three-dragon-ante-set}])

(def vehicles
  [{:name "Water Vehicles"
    :key :water-vehicles}
   {:name "Land Vehicles"
    :key :land-vehicles}])

(def tools
  (concat
   musical-instruments
   artisans-tools
   misc-tools
   gaming-sets
   vehicles))

(def tools-map
  (merge
   {:artisans-tool {:name "Artisans Tools"
                    :values artisans-tools}
    :musical-instrument {:name "Musical Instruments"
                         :values musical-instruments}
    :gaming-set {:name "Gaming Set"
                 :values gaming-sets}}
   (zipmap (map :key tools) tools)))

(def arcane-focuses
  (common/add-keys
   [{:name "Crystal" :cost {:num 10 :type :gp} :weight "1 lb."}
    {:name "Orb" :cost {:num 20 :type :gp} :weight "3 lb."}
    {:name "Rod" :cost {:num 10 :type :gp} :weight "2 lb."}
    {:name "Staff" :cost {:num 5 :type :gp} :weight "4 lb."}
    {:name "Wand" :cost {:num 10 :type :gp} :weight "1 lb."}]))

(def druidic-focuses
  (common/add-keys
   [{:name "Sprig of mistletoe" :cost {:num 1 :type :gp} :weight "—"}
    {:name "Totem" :cost {:num 1 :type :gp} :weight "—"}
    {:name "Wooden staff" :cost {:num 5 :type :gp} :weight "4 lb."}
    {:name "Yew wand" :cost {:num 10 :type :gp} :weight "1 lb."}]))

(def holy-symbols
  (common/add-keys
   [{:name "Amulet" :cost {:num 5 :type :gp} :weight "1 lb."}
    {:name "Emblem" :cost {:num 5 :type :gp} :weight "—"}
    {:name "Reliquary" :cost {:num 5 :type :gp} :weight "2 lb."}]))


(def adventuring-gear
  (concat
   weapons/ammunition
   arcane-focuses
   druidic-focuses
   holy-symbols
   (common/add-keys
    [{:name "Abacus" :cost {:num 2 :type :gp} :weight "2 lb."}
     {:name "Acid" :sell-container :vial :cost {:num 25 :type :gp} :weight "1 lb."}
     {:name "Alchemist’s fire" :sell-container :flask :cost {:num 50 :type :gp} :weight "1 lb."}
     {:name "Alms Box"}
     {:name "Antitoxin" :sell-container :vial :cost {:num 50 :type :gp} :weight "—"}
     {:name "Backpack" :cost {:num 2 :type :gp} :weight "5 lb."}
     {:name "Bag of Sand"}
     {:name "Ball bearings" :sell-qty 1000 :sell-contiainer "bag" :cost {:num 1 :type :gp} :weight "2 lb."}
     {:name "Barrel" :cost {:num 2 :type :gp} :weight "70 lb."}
     {:name "Basket" :cost {:num 4 :type :sp} :weight "2 lb."}
     {:name "Bedroll" :cost {:num 1 :type :gp} :weight "7 lb."}
     {:name "Bell" :cost {:num 1 :type :gp} :weight "—"}
     {:name "Blanket" :cost {:num 5 :type :sp} :weight "3 lb."}
     {:name "Block and tackle" :cost {:num 1 :type :gp} :weight "5 lb."}
     {:name "Book" :cost {:num 25 :type :gp} :weight "5 lb."}
     {:name "Bottle, glass" :cost {:num 2 :type :gp} :weight "2 lb."}
     {:name "Bucket" :cost {:num 5 :type :cp} :weight "2 lb."}
     {:name "Caltrops" :sell-qty 20 :sell-container :bag :cost {:num 1 :type :gp} :weight "2 lb."}
     {:name "Candle" :cost {:num 1 :type :cp} :weight "—"}
     {:name "Case, crossbow bolt" :cost {:num 1 :type :gp} :weight "1 lb."}
     {:name "Case, map or scroll" :cost {:num 1 :type :gp} :weight "1 lb."}
     {:name "Censer"}
     {:name "Chain" :sell-qty 10 :sell-container :feet :cost {:num 5 :type :gp} :weight "10 lb."}
     {:name "Chalk" :sell-container :piece :cost {:num 1 :type :cp} :weight "—"}
     {:name "Chest" :cost {:num 5 :type :gp} :weight "25 lb."}
     {:name "Climber’s kit" :cost {:num 25 :type :gp} :weight "12 lb."}
     {:name "Clothes, common" :cost {:num 5 :type :sp} :weight "3 lb."}
     {:name "Clothes, costume" :cost {:num 5 :type :gp} :weight "4 lb."}
     {:name "Clothes, fine" :cost {:num 15 :type :gp} :weight "6 lb."}
     {:name "Clothes, traveler’s" :cost {:num 2 :type :gp} :weight "4 lb."}
     {:name "Component pouch" :cost {:num 25 :type :gp} :weight "2 lb."}
     {:name "Costume"}
     {:name "Crowbar" :cost {:num 2 :type :gp} :weight "5 lb."}
     {:name "Fishing tackle" :cost {:num 1 :type :gp} :weight "4 lb."}
     {:name "Flask or tankard" :cost {:num 2 :type :cp} :weight "1 lb."}
     {:name "Grappling hook" :cost {:num 2 :type :gp} :weight "4 lb."}
     {:name "Hammer" :cost {:num 1 :type :gp} :weight "3 lb."}
     {:name "Hammer, sledge" :cost {:num 2 :type :gp} :weight "10 lb."}
     {:name "Healer’s kit" :cost {:num 5 :type :gp} :weight "3 lb."}
     {:name "Holy water" :sell-container :flask :cost {:num 25 :type :gp} :weight "1 lb."}
     {:name "Hourglass" :cost {:num 25 :type :gp} :weight "1 lb."}
     {:name "Hunting trap" :cost {:num 5 :type :gp} :weight "25 lb."}
     {:name "Ink" :sell-container "ounce bottle" :cost {:num 10 :type :gp} :weight "—"}
     {:name "Ink pen" :cost {:num 2 :type :cp} :weight "—"}
     {:name "Incense" :sell-container :block}
     {:name "Jug or pitcher" :cost {:num 2 :type :cp} :weight "4 lb."}
     {:name "Knife, Small"}
     {:name "Ladder (10-foot)" :cost {:num 1 :type :sp} :weight "25 lb."}
     {:name "Lamp" :cost {:num 5 :type :sp} :weight "1 lb."}
     {:name "Lantern, bullseye" :cost {:num 10 :type :gp} :weight "2 lb."}
     {:name "Lantern, hooded" :cost {:num 5 :type :gp} :weight "2 lb."}
     {:name "Lock" :cost {:num 10 :type :gp} :weight "1 lb."}
     {:name "Magnifying glass" :cost {:num 100 :type :gp} :weight "—"}
     {:name "Manacles" :cost {:num 2 :type :gp} :weight "6 lb."}
     {:name "Mess kit" :cost {:num 2 :type :sp} :weight "1 lb."}
     {:name "Mirror, steel" :cost {:num 5 :type :gp} :weight "1/2 lb."}
     {:name "Oil" :sell-container :flask :cost {:num 1 :type :sp} :weight "1 lb."}
     {:name "Paper" :sell-container :sheet :cost {:num 2 :type :sp} :weight "—"}
     {:name "Parchment" :sell-container :sheet :cost {:num 1 :type :sp} :weight "—"}
     {:name "Perfume" :sell-container :vial :cost {:num 5 :type :gp} :weight "—"}
     {:name "Pick, miner’s" :cost {:num 2 :type :gp} :weight "10 lb."}
     {:name "Piton" :cost {:num 5 :type :cp} :weight "1/4 lb."}
     {:name "Poison, basic" :sell-container :vial :cost {:num 100 :type :gp} :weight "—"}
     {:name "Pole (10-foot)" :cost {:num 5 :type :cp} :weight "7 lb."}
     {:name "Pot, iron" :cost {:num 2 :type :gp} :weight "10 lb."}
     {:name "Potion of healing" :cost {:num 50 :type :gp} :weight "1/2 lb."}
     {:name "Pouch" :cost {:num 5 :type :sp} :weight "1 lb."}
     {:name "Quiver" :cost {:num 1 :type :gp} :weight "1 lb."}
     {:name "Ram, portable" :cost {:num 4 :type :gp} :weight "35 lb."}
     {:name "Rations (1 day)" :cost {:num 5 :type :sp} :weight "2 lb."}
     {:name "Robes" :cost {:num 1 :type :gp} :weight "4 lb."}
     {:name "Rope, hempen" :sell-container :feet :sell-qty 50 :cost {:num 1 :type :gp} :weight "10 lb."}
     {:name "Rope, silk" :sell-container :feet :sell-qty 50 :cost {:num 10 :type :gp} :weight "5 lb."}
     {:name "Sack" :cost {:num 1 :type :cp} :weight "1/2 lb."}
     {:name "Scale, merchant’s" :cost {:num 5 :type :gp} :weight "3 lb."}
     {:name "Sealing wax" :cost {:num 5 :type :sp} :weight "—"}
     {:name "Shovel" :cost {:num 2 :type :gp} :weight "5 lb."}
     {:name "Signal whistle" :cost {:num 5 :type :cp} :weight "—"}
     {:name "Signet ring" :cost {:num 5 :type :gp} :weight "—"}
     {:name "Soap" :cost {:num 2 :type :cp} :weight "—"}
     {:name "Spellbook" :cost {:num 50 :type :gp} :weight "3 lb."}
     {:name "Spikes, iron" :sell-qty 10 :cost {:num 1 :type :gp} :weight "5 lb."}
     {:name "Spyglass" :cost {:num 1000 :type :gp} :weight "1 lb."}
     {:name "String" :sell-container :feet :sell-qty 10}
     {:name "Tent, two-person" :cost {:num 2 :type :gp} :weight "20 lb."}
     {:name "Tinderbox" :cost {:num 5 :type :sp} :weight "1 lb."}
     {:name "Torch" :cost {:num 1 :type :cp} :weight "1 lb."}
     {:name "Vial" :cost {:num 1 :type :gp} :weight "—"}
     {:name "Vestements"}
     {:name "Waterskin" :cost {:num 2 :type :sp} :weight "5 lb. (full)"}
     {:name "Whetstone" :cost {:num 1 :type :cp} :weight "1 lb."}])))

(def packs
  (into
   []
   common/add-keys-xform
   [{:name "Burgler's Pack"
     :items {:backpack 1
             :ball-bearings 1
             :string 1
             :bell 1
             :candle 5
             :crowbar 1
             :hammer 1
             :piton 10
             :lantern-hooded 1
             :oil 2
             :rations-1-day- 5
             :tinderbox 1
             :waterskin 1
             :rope-hempen 1}}
    {:name "Diplomat's Pack"
     :items {:chest 1
             :case-map-or-scroll 1
             :clothes-fine 1
             :ink 1
             :ink-pen 1
             :lamp 1
             :oil 2
             :paper 5
             :perfume 1
             :sealing-wax 1
             :soap 1}}
    {:name "Dungeoneer's Pack"
     :items {:backpack 1
             :crowbar 1
             :hammer 1
             :piton 10
             :torch 10
             :tinderbox 1
             :rations-1-day- 10
             :waterskin 1
             :rope-hempen 1}}
    {:name "Entertainer's Pack"
     :items {:backpack 1
             :bedroll 1
             :costume 2
             :candle 5
             :rations-1-day- 5
             :waterskin 1
             :disguise-kit 1}}
    {:name "Explorer's Pack"
     :items {:backpack 1
             :bedroll 1
             :mess-kit 1
             :tinderbox 1
             :torch 10
             :rations-1-day- 10
             :waterskin 1
             :rope-hempen 1}}
    {:name "Priest's Pack"
     :items {:backpack 1
             :blanket 1
             :candle 10
             :tinderbox 1
             :alms-box 1
             :incense 2
             :censer 1
             :vestements 1
             :rations-1-day- 2
             :waterskin 1}}
    {:name "Scholar's Pack"
     :items {:backpack 1
             :book 1
             :ink 1
             :ink-pen 1
             :parchment 10
             :bag-of-sand 1
             :knife-small 1}}]))

(def equipment
  (concat
   packs
   tools
   adventuring-gear))

(def equipment-map
  (merge
   {:holy-symbol {:name "Holy Symbol"
                  :values holy-symbols}
    :druidic-focus {:name "Druidic Focus"
                    :values druidic-focuses}
    :arcane-focus {:name "Arcane Focus"
                   :values arcane-focuses}
    :pack {:name "Equipment Packs"
           :values packs}
    :musical-instrument {:name "Musical Instruments"
                         :values musical-instruments}}
   (zipmap (map :key equipment) equipment)))

(def treasure
  [{:key :cp
    :name "Copper Pieces (CP)"}
   {:key :sp
    :name "Silver Pieces (SP)"}
   {:key :ep
    :name "Electrum Pieces (EP)"}
   {:key :gp
    :name "Gold Pieces (GP)"}
   {:key :pp
    :name "Platinum Pieces (PP)"}])

(def treasure-map
  (zipmap (map :key treasure) treasure))


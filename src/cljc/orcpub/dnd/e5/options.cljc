(ns orcpub.dnd.e5.options
  (:require [clojure.string :as s]
            [orcpub.common :as common]
            [orcpub.template :as t]
            [orcpub.entity :as entity]
            [orcpub.entity-spec :as es]
            [orcpub.modifiers :as mods]
            [orcpub.dnd.e5.character :as character]
            [orcpub.dnd.e5.modifiers :as modifiers]
            [orcpub.dnd.e5.spells :as spells]
            [orcpub.dnd.e5.spell-lists :as sl]))

(def add-keys-xform
  (map
   #(assoc % :key (common/name-to-kw (:name %)))))

(defn add-keys [vals]
  (into [] add-keys-xform vals))

(def abilities
  [{:key :str
    :name "Strength"}
   {:key :con
    :name "Constitution"}
   {:key :dex
    :name "Dexterity"}
   {:key :int
    :name "Intelligence"}
   {:key :wis
    :name "Wisdom"}
   {:key :cha
    :name "Charisma"}])

(def abilities-map
  (common/map-by-key abilities))

(def conditions
  [{:name "Blinded"}
   {:name "Charmed"}
   {:name "Deafened"}
   {:name "Frightened"}
   {:name "Grappled"}
   {:name "Incapacitated"}
   {:name "Invisible"}
   {:name "Paralyzed"}
   {:name "Petrified"}
   {:name "Poisoned"}
   {:name "Prone"}
   {:name "Restrained"}
   {:name "Stunned"}
   {:name "Unconscious"}])

(def conditions-map
  (common/map-by-key (add-keys conditions)))

(def skills [{:name "Acrobatics"
              :key :acrobatics
              :ability :dex
              :description "Your Dexterity (Acrobatics) check 
covers your attempt to stay on your feet in a tricky 
situation, such as when you’re trying to run across a 
sheet of ice, balance on a tightrope, or stay upright 
on a rocking ship’s deck. The GM might also call for a 
Dexterity (Acrobatics) check to see if you can 
perform acrobatic stunts, including dives, rolls, 
somersaults, and flips."}
             {:name "Animal Handling"
              :key :animal-handling
              :ability :wis
              :description "When there is any question 
whether you can calm down a domesticated animal, 
keep a mount from getting spooked, or intuit an 
animal’s intentions, the GM might call for a Wisdom 
(Animal Handling) check. You also make a Wisdom 
(Animal Handling) check to control your mount 
when you attempt a risky maneuver"}
             {:name "Arcana"
              :key :arcana
              :ability :int
              :description "Your Intelligence (Arcana) check 
measures your ability to recall lore about spells, 
magic items, eldritch symbols, magical traditions, 
the planes of existence, and the inhabitants of those 
planes."}
             {:name "Athletics"
              :key :athletics
              :ability :str
              :description "Your Strength (Athletics) check covers 
difficult situations you encounter while climbing, 
jumping, or swimming."}
             {:name "Deception"
              :key :deception
              :ability :cha
              :description "Your Charisma (Deception) check 
determines whether you can convincingly hide the 
truth, either verbally or through your actions. This 
deception can encompass everything from 
misleading others through ambiguity to telling 
outright lies. Typical situations include trying to fasttalk
a guard, con a merchant, earn money through 
gambling, pass yourself off in a disguise, dull 
someone’s suspicions with false assurances, or 
maintain a straight face while telling a blatant lie."}
             {:name "History"
              :key :history
              :ability :int
              :description "Your Intelligence (History) check 
measures your ability to recall lore about historical 
events, legendary people, ancient kingdoms, past 
disputes, recent wars, and lost civilizations"}
             {:name "Insight"
              :key :insight
              :ability :wis
              :description "Your Wisdom (Insight) check decides 
whether you can determine the true intentions of a 
creature, such as when searching out a lie or 
predicting someone’s next move. Doing so involves
gleaning clues from body language, speech habits, 
and changes in mannerisms."}
             {:name "Intimidation"
              :key :intimidation
              :ability :cha
              :description "When you attempt to influence 
someone through overt threats, hostile actions, and 
physical violence, the GM might ask you to make a 
Charisma (Intimidation) check. Examples include 
trying to pry information out of a prisoner, 
convincing street thugs to back down from a 
confrontation, or using the edge of a broken bottle to 
convince a sneering vizier to reconsider a decision."}
             {:name "Investigation"
              :key :investigation
              :ability :int
              :description "When you look around for clues 
and make deductions based on those clues, you 
make an Intelligence (Investigation) check. You 
might deduce the location of a hidden object, discern 
from the appearance of a wound what kind of 
weapon dealt it, or determine the weakest point in a 
tunnel that could cause it to collapse. Poring through 
ancient scrolls in search of a hidden fragment of 
knowledge might also call for an Intelligence 
(Investigation) check."}
             {:name "Medicine"
              :key :medicine
              :ability :wis
              :description "A Wisdom (Medicine) check lets you try 
to stabilize a dying companion or diagnose an illness."}
             {:name "Nature"
              :key :nature
              :ability :int
              :description "Your Intelligence (Nature) check 
measures your ability to recall lore about terrain, 
plants and animals, the weather, and natural cycles."}
             {:name "Perception"
              :key :perception
              :ability :wis
              :description "Your Wisdom (Perception) check lets 
you spot, hear, or otherwise detect the presence of 
something. It measures your general awareness of 
your surroundings and the keenness of your senses. 
For example, you might try to hear a conversation 
through a closed door, eavesdrop under an open 
window, or hear monsters moving stealthily in the 
forest. Or you might try to spot things that are 
obscured or easy to miss, whether they are orcs 
lying in ambush on a road, thugs hiding in the 
shadows of an alley, or candlelight under a closed 
secret door."}
             {:name "Performance"
              :key :performance
              :ability :cha
              :description "Your Charisma (Performance) 
check determines how well you can delight an 
audience with music, dance, acting, storytelling, or 
some other form of entertainment."}
             {:name "Persuasion"
              :key :persuasion
              :ability :cha
              :description "When you attempt to influence 
someone or a group of people with tact, social graces, 
or good nature, the GM might ask you to make a 
Charisma (Persuasion) check. Typically, you use 
persuasion when acting in good faith, to foster 
friendships, make cordial requests, or exhibit proper 
etiquette. Examples of persuading others include 
convincing a chamberlain to let your party see the 
king, negotiating peace between warring tribes, or 
inspiring a crowd of townsfolk."}
             {:name "Religion"
              :key :religion
              :ability :int
              :description "Your Intelligence (Religion) check 
measures your ability to recall lore about deities, 
rites and prayers, religious hierarchies, holy symbols, 
and the practices of secret cults"}
             {:name "Sleight of Hand"
              :key :sleight-of-hand
              :ability :dex
              :description "Whenever you attempt an act of 
legerdemain or manual trickery, such as planting 
something on someone else or concealing an object 
on your person, make a Dexterity (Sleight of Hand)
check. The GM might also call for a Dexterity (Sleight 
of Hand) check to determine whether you can lift a 
coin purse off another person or slip something out 
of another person’s pocket"}
             {:name "Stealth"
              :key :stealth
              :ability :dex
              :description "Make a Dexterity (Stealth) check when 
you attempt to conceal yourself from enemies, slink 
past guards, slip away without being noticed, or 
sneak up on someone without being seen or heard."}
             {:name "Survival"
              :key :survival
              :ability :wis
              :description "The GM might ask you to make a 
Wisdom (Survival) check to follow tracks, hunt wild 
game, guide your group through frozen wastelands, 
identify signs that owlbears live nearby, predict the 
weather, or avoid quicksand and other natural 
hazards."}])

(def skills-map (common/map-by-key skills))

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
  [{:name "Alchemist's Supplies", :key :alchemists-supplies}
   {:name "Brewer's Supplies", :key :brewers-supplies}
   {:name "Calligrapher's Supplies", :key :calligraphers-supplies}
   {:name "Carpenter's Tools", :key :carpenters-tools}
   {:name "Cartographer's Tools", :key :cartographers-tools}
   {:name "Cobbler's Tools", :key :cobblers-tools}
   {:name "Cook's Utensils", :key :cooks-utensils}
   {:name "Glassblower's Tools", :key :glassblowers-tools}
   {:name "Jeweler's Tools", :key :jewelers-tools}
   {:name "Leatherworker's Tools", :key :leatherworkers-tools}
   {:name "Mason's Tools", :key :masons-tools}
   {:name "Painter's Supplies", :key :painters-supplies}
   {:name "Potter's Tools", :key :potters-tools}
   {:name "Smith's Tools", :key :smiths-tools}
   {:name "Tinker's Tools", :key :tinkers-tools}
   {:name "Weaver's Tools", :key :weavers-tools}
   {:name "Woodcarver's Tools", :key :woodcarvers-tools}])

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

(def ammunition
  (add-keys
   [{:name "Arrow" :sell-qty 20 :cost {:num 1 :type :gp} :weight "1 lb."}
    {:name "Blowgun needle" :sell-qty 50 :cost {:num 1 :type :gp} :weight "1 lb."}
    {:name "Crossbow bolt" :sell-qty 20 :cost {:num 1 :type :gp} :weight "1½ lb."}
    {:name "Sling bullet" :sell-qty 20 :cost {:num 4 :type :cp} :weight "1½ lb."}]))

(def arcane-focuses
  (add-keys
   [{:name "Crystal" :cost {:num 10 :type :gp} :weight "1 lb."}
    {:name "Orb" :cost {:num 20 :type :gp} :weight "3 lb."}
    {:name "Rod" :cost {:num 10 :type :gp} :weight "2 lb."}
    {:name "Staff" :cost {:num 5 :type :gp} :weight "4 lb."}
    {:name "Wand" :cost {:num 10 :type :gp} :weight "1 lb."}]))

(def druidic-focuses
  (add-keys
   [{:name "Sprig of mistletoe" :cost {:num 1 :type :gp} :weight "—"}
    {:name "Totem" :cost {:num 1 :type :gp} :weight "—"}
    {:name "Wooden staff" :cost {:num 5 :type :gp} :weight "4 lb."}
    {:name "Yew wand" :cost {:num 10 :type :gp} :weight "1 lb."}]))

(def holy-symbols
  (add-keys
   [{:name "Amulet" :cost {:num 5 :type :gp} :weight "1 lb."}
    {:name "Emblem" :cost {:num 5 :type :gp} :weight "—"}
    {:name "Reliquary" :cost {:num 5 :type :gp} :weight "2 lb."}]))


(def adventuring-gear
  (concat
   ammunition
   arcane-focuses
   druidic-focuses
   holy-symbols
   (add-keys
    [{:name "Abacus" :cost {:num 2 :type :gp} :weight "2 lb."}
     {:name "Acid" :sell-container :vial :cost {:num 25 :type :gp} :weight "1 lb."}
     {:name "Alchemist’s fire" :sell-container :flask :cost {:num 50 :type :gp} :weight "1 lb."}
     {:name "Antitoxin" :sell-container :vial :cost {:num 50 :type :gp} :weight "—"}
     {:name "Backpack" :cost {:num 2 :type :gp} :weight "5 lb."}
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
     {:name "Chain" :sell-qty 10 :sell-container :feet :cost {:num 5 :type :gp} :weight "10 lb."}
     {:name "Chalk" :sell-container :piece :cost {:num 1 :type :cp} :weight "—"}
     {:name "Chest" :cost {:num 5 :type :gp} :weight "25 lb."}
     {:name "Climber’s kit" :cost {:num 25 :type :gp} :weight "12 lb."}
     {:name "Clothes, common" :cost {:num 5 :type :sp} :weight "3 lb."}
     {:name "Clothes, costume" :cost {:num 5 :type :gp} :weight "4 lb."}
     {:name "Clothes, fine" :cost {:num 15 :type :gp} :weight "6 lb."}
     {:name "Clothes, traveler’s" :cost {:num 2 :type :gp} :weight "4 lb."}
     {:name "Component pouch" :cost {:num 25 :type :gp} :weight "2 lb."}
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
     {:name "Jug or pitcher" :cost {:num 2 :type :cp} :weight "4 lb."}
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
     {:name "Tent, two-person" :cost {:num 2 :type :gp} :weight "20 lb."}
     {:name "Tinderbox" :cost {:num 5 :type :sp} :weight "1 lb."}
     {:name "Torch" :cost {:num 1 :type :cp} :weight "1 lb."}
     {:name "Vial" :cost {:num 1 :type :gp} :weight "—"}
     {:name "Waterskin" :cost {:num 2 :type :sp} :weight "5 lb. (full)"}
     {:name "Whetstone" :cost {:num 1 :type :cp} :weight "1 lb."}])))

(def packs
  (into
   []
   add-keys-xform
   [{:name "Burgler's Pack"}
    {:name "Diplomat's Pack"}
    {:name "Dungeoneer's Pack"}
    {:name "Entertainer's Pack"}
    {:name "Explorer's Pack"}
    {:name "Priest's Pack"}
    {:name "Scholar's Pack"}]))

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

(def armor
  [{:name "Shield"
    :key :shield}
   {:name "Padded",
    :type :light,
    :base-ac 11,
    :stealth-disadvantage? true,
    :weight 8,
    :key :padded}
   {:name "Leather",
    :type :light,
    :base-ac 11,
    :weight 10,
    :key :leather}
   {:name "Studded",
    :type :light,
    :base-ac 12,
    :weight 13,
    :key :studded}
   {:name "Hide",
    :type :medium,
    :base-ac 12,
    :max-dex-mod 2,
    :weight 12,
    :key :hide}
   {:name "Chain Shirt",
    :type :medium,
    :base-ac 13,
    :max-dex-mod 2,
    :weight 20,
    :key :chain-shirt}
   {:name "Scale mail",
    :type :medium,
    :base-ac 14,
    :max-dex-mod 2,
    :stealth-disadvantage? true,
    :weight 45,
    :key :scale-mail}
   {:name "Breastplate",
    :type :medium,
    :base-ac 14,
    :max-dex-mod 2,
    :weight 20,
    :key :breastplate}
   {:name "Half plate",
    :type :medium,
    :base-ac 15,
    :max-dex-mod 2,
    :stealth-disadvantage? true,
    :weight 40,
    :key :half-plate}
   {:name "Ring mail",
    :type :heavy,
    :base-ac 14,
    :max-dex-mod 0,
    :stealth-disadvantage? true,
    :weight 40,
    :key :ring-mail}
   {:name "Chain mail",
    :type :heavy,
    :base-ac 16,
    :max-dex-mod 0,
    :min-str 13,
    :stealth-disadvantage? true,
    :weight 55,
    :key :chain-mail}
   {:name "Splint",
    :type :heavy,
    :base-ac 17,
    :max-dex-mod 0,
    :min-str 15,
    :stealth-disadvantage? true,
    :weight 60,
    :key :splint}
   {:name "Plate",
    :type :heavy,
    :base-ac 18,
    :max-dex-mod 0,
    :min-str 15,
    :stealth-disadvantage? true,
    :weight 65,
    :key :plate}])

(def armor-map
  (zipmap (map :key armor) armor))

(def weapons
  [{:name "Crossbow, light",
    :damage-type :piercing,
    :damage-die 8,
    :type :simple,
    :damage-die-count 1,
    :ranged true,
    :range {:min 80, :max 320},
    :key :crossbow-light}
   {:ranged true,
    :key :dart,
    :name "Dart",
    :damage-die-count 1,
    :type :simple,
    :damage-type :piercing,
    :thrown true,
    :finesse true,
    :damage-die 4,
    :range {:min 20, :max 60}}
   {:name "Shortbow",
    :damage-type :piercing,
    :damage-die 6,
    :type :simple,
    :damage-die-count 1,
    :ranged true,
    :range {:min 80, :max 320},
    :key :shortbow}
   {:name "Sling",
    :damage-type :bludgeoning,
    :damage-die 4,
    :type :simple,
    :damage-die-count 1,
    :ranged true,
    :range {:min 30, :max 120},
    :key :sling}
   {:name "Club",
    :damage-type :bludgeoning,
    :damage-die 4,
    :damage-die-count 1,
    :type :simple,
    :melee true,
    :key :club}
   {:melee true,
    :key :dagger,
    :name "Dagger",
    :damage-die-count 1,
    :type :simple,
    :damage-type :piercing,
    :thrown true,
    :finesse true,
    :damage-die 4,
    :range {:min 20, :max 60}}
   {:name "Greatclub",
    :damage-type :bludgeoning,
    :damage-die 8,
    :damage-die-count 1,
    :type :simple,
    :melee true,
    :key :greatclub}
   {:name "Handaxe",
    :damage-type :slashing,
    :damage-die 6,
    :damage-die-count 1,
    :type :simple,
    :melee true,
    :thrown true,
    :range {:min 20, :max 60},
    :key :handaxe}
   {:name "Javelin",
    :damage-type :piercing,
    :damage-die 6,
    :damage-die-count 1,
    :type :simple,
    :melee true,
    :thrown true,
    :range {:min 30, :max 120},
    :key :javelin}
   {:name "Light hammer",
    :damage-type :bludgeoning,
    :damage-die 4,
    :damage-die-count 1,
    :type :simple,
    :melee true,
    :thrown true,
    :range {:min 20, :max 60},
    :key :light-hammer}
   {:name "Mace",
    :damage-type :bludgeoning,
    :type :simple,
    :damage-die 6,
    :damage-die-count 1,
    :melee true,
    :key :mace}
   {:name "Quarterstaff",
    :damage-type :bludgeoning,
    :type :simple,
    :damage-die 6,
    :damage-die-count 1,
    :versatile {:damage-die 8, :damage-die-count 1},
    :melee true,
    :key :quarterstaff}
   {:name "Sickle",
    :damage-type :slashing,
    :damage-die 4,
    :type :simple,
    :damage-die-count 1,
    :melee true,
    :key :sickle}
   {:melee true,
    :versatile {:damage-die 8, :damage-die-count 1},
    :key :spear,
    :name "Spear",
    :damage-die-count 1,
    :type :simple,
    :damage-type :piercing,
    :thrown true,
    :damage-die 6,
    :range {:min 20, :max 60}}
   {:name "Unarmed Strike",
    :damage-type :bludgeoning,
    :damage-die 1,
    :type :simple,
    :damage-die-count 1,
    :melee true,
    :unarmed true,
    :key :unarmed-strike}
   {:name "Battleaxe",
    :damage-type :slashing,
    :damage-die 8,
    :type :martial,
    :damage-die-count 1,
    :versatile {:damage-die 10, :damage-die-count 1},
    :melee true,
    :key :battleaxe}
   {:name "Flail",
    :damage-type :bludgeoning,
    :damage-die 8,
    :type :martial,
    :damage-die-count 1,
    :melee true,
    :key :flail}
   {:name "Glaive",
    :damage-type :slashing,
    :damage-die 10,
    :type :martial,
    :damage-die-count 1,
    :melee true,
    :heavy true,
    :reach true,
    :key :glaive}
   {:name "Greataxe",
    :damage-type :slashing,
    :damage-die 12,
    :type :martial,
    :damage-die-count 1,
    :heavy true,
    :melee true,
    :key :greataxe}
   {:name "Greatsword",
    :damage-type :slashing,
    :damage-die 6,
    :type :martial,
    :damage-die-count 2,
    :heavy true,
    :melee true,
    :key :greatsword}
   {:name "Halberd",
    :damage-type :slashing,
    :damage-die 10,
    :type :martial,
    :damage-die-count 1,
    :melee true,
    :heavy true,
    :reach true,
    :key :halberd}
   {:name "Lance",
    :damage-type :piercing,
    :damage-die 12,
    :type :martial,
    :damage-die-count 1,
    :melee true,
    :reach true,
    :key :lance}
   {:name "Longsword",
    :damage-type :slashing,
    :damage-die 8,
    :type :martial,
    :damage-die-count 1,
    :versatile {:damage-die 10, :damage-die-count 1},
    :melee true,
    :key :longsword}
   {:name "Maul",
    :damage-type :bludgeoning,
    :damage-die 6,
    :type :martial,
    :damage-die-count 2,
    :heavy true,
    :melee true,
    :key :maul}
   {:name "Morningstar",
    :damage-type :piercing,
    :damage-die 8,
    :type :martial,
    :damage-die-count 1,
    :melee true,
    :key :morningstar}
   {:name "Pike",
    :damage-type :piercing,
    :damage-die 10,
    :type :martial,
    :damage-die-count 1,
    :melee true,
    :heavy true,
    :reach true,
    :key :pike}
   {:name "Rapier",
    :damage-type :piercing,
    :damage-die 8,
    :type :martial,
    :damage-die-count 1,
    :finesse true,
    :melee true,
    :key :rapier}
   {:name "Scimitar",
    :damage-type :slashing,
    :damage-die 6,
    :type :martial,
    :damage-die-count 1,
    :finesse true,
    :melee true,
    :key :scimitar}
   {:name "Shortsword",
    :damage-type :piercing,
    :damage-die 6,
    :type :martial,
    :finesse true,
    :damage-die-count 1,
    :melee true,
    :key :shortsword}
   {:melee true,
    :versatile {:damage-die 8, :damage-die-count 1},
    :key :trident,
    :name "Trident",
    :damage-die-count 1,
    :type :martial,
    :damage-type :piercing,
    :thrown true,
    :damage-die 6,
    :range {:min 20, :max 60}}
   {:name "War pick",
    :damage-type :piercing,
    :damage-die 8,
    :type :martial,
    :damage-die-count 1,
    :melee true,
    :key :war-pick}
   {:name "Warhammer",
    :damage-type :bludgeoning,
    :damage-die 8,
    :type :martial,
    :damage-die-count 1,
    :versatile {:damage-die 10, :damage-die-count 1},
    :melee true,
    :key :warhammer}
   {:name "Whip",
    :damage-type :slashing,
    :damage-die 4,
    :type :martial,
    :damage-die-count 1,
    :melee true,
    :finesse true,
    :reach true,
    :key :whip}
   {:name "Blowgun",
    :damage-type :piercing,
    :damage-die 1,
    :type :martial,
    :damage-die-count 1,
    :ranged true,
    :range {:min 25, :max 100},
    :key :blowgun}
   {:name "Crossbow, hand",
    :damage-type :piercing,
    :damage-die 6,
    :type :martial,
    :damage-die-count 1,
    :ranged true,
    :range {:min 30, :max 120},
    :key :crossbow-hand}
   {:name "Crossbow, heavy",
    :damage-type :piercing,
    :damage-die 10,
    :type :martial,
    :damage-die-count 1,
    :ranged true,
    :heavy true,
    :range {:min 100, :max 400},
    :key :crossbow-heavy}
   {:name "Longbow",
    :damage-type :piercing,
    :damage-die 8,
    :type :martial,
    :damage-die-count 1,
    :ranged true,
    :heavy true,
    :range {:min 150, :max 600},
    :key :longbow}
   {:name "Net",
    :type :martial,
    :ranged true,
    :thrown true,
    :range {:min 5, :max 15},
    :key :net}])

(def weapons-map
  (zipmap (map :key weapons) weapons))

(defn weapons-of-type [weapons type]
  (filter #(= type (:type %)) weapons))

(defn martial-weapons [weapons]
  (weapons-of-type weapons :martial))

(defn simple-weapons [weapons]
  (weapons-of-type weapons :simple))

(def skill-abilities
  (into {} (map (juxt :key :ability)) skills))

(defn skill-option [skill]
  (t/option-cfg
   {:name (:name skill)
    :key (:key skill)
    :help (:description skill)
    :modifiers [(modifiers/skill-proficiency (:key skill))]}))

(defn weapon-proficiency-option [{:keys [name key]}]
  (t/option
   name
   key
   nil
   [(modifiers/weapon-proficiency name key)]))

(defn tool-option [tool]
  (t/option
   (:name tool)
   (:key tool)
   nil
   [(modifiers/tool-proficiency (:name tool) (:key tool))]))

(defn weapon-option [weapon & [num]]
  (t/option
   (:name weapon)
   (:key weapon)
   nil
   [(modifiers/weapon (:key weapon) (or num 1))]))

(defn weapon-options [weapons & [num]]
  (map
   #(weapon-option % num)
   weapons))

(defn simple-melee-weapon-options [num]
  (weapon-options
   (filter
    #(and (= :simple (:type %)) (:melee %))
    weapons)
   num))

(defn martial-weapon-options [num]
  (weapon-options
   (filter
    #(= :martial (:type %))
    weapons)
   num))

(defn skill-options [skills]
  (map
   skill-option
   skills))

(defn weapon-proficiency-options [weapons]
  (map
   weapon-proficiency-option
   weapons))

(defn tool-options [tools]
  (map
   tool-option
   tools))

(defn ability-increase-selection [abilities num & [different?]]
  (assoc
   (t/selection
    "Ability Score Increase"
    (into
     []
     (map
      (fn [ability]
        (t/option
         (s/upper-case (name ability))
         ability
         []
         [(modifiers/ability ability 1)])))
     abilities)
    num
    num)
   ::t/simple? (> num 1)))

(defn min-ability [ability-kw min-value]
  (fn [c] (>= (ability-kw (es/entity-val c :abilities)) min-value)))

(defn ability-prereq [ability-kw min-value]
  {::t/label (str (s/upper-case (name ability-kw)) " " min-value " or higher")
   ::t/prereq-fn (min-ability ability-kw min-value)})

(defn prereq [label prereq-fn]
  {::t/label label
   ::t/prereq-fn prereq-fn})

(defn armor-prereq [armor-kw]
  (prereq (str "proficiency with " (name armor-kw) " armor")
             (fn [c] (let [prof-keys (set (map :key (:armor-profs c)))]
                       (boolean (prof-keys armor-kw))))))

(def languages
  [{:name "Common"
    :key :common}
   {:name "Dwarvish"
    :key :dwarvish}
   {:name "Elvish"
    :key :elvish}
   {:name "Giant"
    :key :giant}
   {:name "Gnomish"
    :key :gnomish}
   {:name "Goblin"
    :key :goblin}
   {:name "Halfling"
    :key :halfling}
   {:name "Orc"
    :key :orc}
   {:name "Abyssal"
    :key :abyssal}
   {:name "Celestial"
    :key :celestial}
   {:name "Draconic"
    :key :draconic}
   {:name "Deep Speech"
    :key :deep-speech}
   {:name "Infernal"
    :key :infernal}
   {:name "Primordial"
    :key :primordial}
   {:name "Sylvan"
    :key :sylval}
   {:name "Undercommon"
    :key :undercommon}])

(def language-map
  (zipmap (map :key languages) languages))

(def elemental-disciplines
  [{:name "Breath of Winter"
    :level 17}
   {:name "Clench of the North Wind"
    :level 6}
   {:name "Eternal Mountain Defense"
    :level 17}
   {:name "Fangs of the Fire Snake"}
   {:name "Fist of Four Thunders"}
   {:name "Fist of Unbroken Air"}
   {:name "Flames of the Phoenix"
    :level 11}
   {:name "Gong of the Summit"
    :level 6}
   {:name "Mist Stance"
    :level 11}
   {:name "Ride the Wind"
    :level 11}
   {:name "River of Hungry Flame"
    :level 17}
   {:name "Rush of the Gale Spirits"}
   {:name "Shape of the Flowing River"}
   {:name "Sweeping Cinder Strike"}
   {:name "Water Whip"}
   {:name "Wave of Rolling Earth"
    :level 17}])

(defn monk-elemental-disciplines []
  (t/selection
   "Elemental Disciplines"
   (mapv
    (fn [{:keys [name level]}]
      (t/option
       name
       (common/name-to-kw name)
       []
       [(modifiers/trait (str "Elemental Discipline: " name))]
       (if level [(fn [c] (>= (es/entity-val c :total-levels) level))])))
    elemental-disciplines)))

(defn language-option [{:keys [name key]}]
  (t/option
   name
   key
   nil
   [(modifiers/language name key)]))

(def class-names
  {:barbarian "Barbarian"
   :bard "Bard"
   :cleric "Cleric"
   :druid "Druid"
   :fighter "Fighter"
   :monk "Monk"
   :paladin "Paladin"
   :ranger "Ranger"
   :rogue "Rogue"
   :sorcerer "Sorcerer"
   :wizard "Wizard"
   :warlock "Warlock"})

(defn key-to-name [key]
  (s/join " " (map s/capitalize (s/split (name key) #"-"))))

(defn spell-options [spells level spellcasting-ability class-name]
  (map
   (fn [key]
     (t/option
      (:name (spells/spell-map key))
      key
      []
      [(modifiers/spells-known level key spellcasting-ability class-name)]))
   (sort spells)))

(defn spell-level-title [level]
  (if (zero? level) "Cantrip" (str "Level " level " Spell")))

(defn spell-selection
  ([class-key level spellcasting-ability class-name num]
   (spell-selection class-key level (get-in sl/spell-lists [class-key level]) spellcasting-ability class-name num))
  ([class-key level spell-keys spellcasting-ability class-name num]
   (t/selection
    (spell-level-title level)
    (spell-options spell-keys level spellcasting-ability class-name)
    num
    num)))

(defn spell-slot-schedule [level-factor]
  (case level-factor
    1 {1 {1 2}
       2 {1 1}
       3 {1 1
          2 2}
       4 {2 1}
       5 {3 2}
       6 {3 1}
       7 {4 1}
       8 {4 1}
       9 {4 1
          5 1}
       10 {5 1}
       11 {6 1}
       13 {7 1}
       15 {8 1}
       17 {9 1}
       18 {5 1}
       19 {6 1}
       20 {7 1}}
    2 {2 {1 2}
       3 {1 1}
       5 {1 1
          2 2}
       7 {2 1}
       9 {3 1}
       11 {3 1}
       13 {4 1}
       15 {4 1}
       17 {4 1
           5 1}
       19 {5 1}}
    3 {3 {1 2}
       4 {1 1}
       7 {1 1
          2 2}
       10 {2 1}
       13 {3 2}
       16 {3 1}
       19 {4 1}}))

(defn total-slots [level level-factor]
  (let [schedule (spell-slot-schedule level-factor)]
    (reduce
     (fn [m lvl]
       (merge-with + m (schedule lvl)))
     {}
     (range 1 (inc level)))))

(defn raw-bard-magical-secrets [level]
  (let [spell-slots (total-slots level 1)]
    (vec
     (for [i (range 2)]
       (t/selection
        (str "Magical Secrets " (inc i))
        (map
         (fn [[lvl _]]
           (t/option
            (spell-level-title lvl)
            (keyword (str lvl))
            [(spell-selection
              :bard
              lvl
              (reduce-kv
               (fn [s _ lvls]
                 (clojure.set/union s (lvls lvl)))
               #{}
               sl/spell-lists)
              :cha
              (class-names :bard)
              1)]
            []))
         spell-slots))))))

(defn bard-magical-secrets [min-level]
  (map
   (fn [s] (assoc s ::t/prereq-fn
                  (fn [built-char]
                    (let [bard-levels (-> (es/entity-val built-char :levels) :bard :class-level)]
                      (>= bard-levels min-level)))))
   (raw-bard-magical-secrets min-level)))

(defn cantrip-selections [class-key ability cantrips-known]
  (reduce
   (fn [m [k v]]
     (assoc m k [(spell-selection class-key 0 ability (class-names class-key) v)]))
   {}
   cantrips-known))

(defn apply-spell-restriction [spell-keys restriction]
  (if restriction
    (filter
     (fn [spell-key]
       (restriction (spells/spell-map spell-key)))
     spell-keys)
    spell-keys))

(defn spells-known-selections [{:keys [class-key
                                       level-factor
                                       spells-known
                                       known-mode
                                       spell-list
                                       spells
                                       ability] :as cfg}]
  (reduce
   (fn [m [cls-lvl v]]
     (let [[num restriction] (if (number? v) [v] ((juxt :num :restriction) v))
           slots (total-slots cls-lvl level-factor)
           all-spells (select-keys
                       (or spells (sl/spell-lists (or spell-list class-key)))
                       (keys slots))
           acquire? (= :acquire known-mode)]
       (assoc m cls-lvl
              [(t/selection
                "Spells Known"
                (vec
                 (flatten
                  (map
                   (fn [[lvl spell-keys]]
                     (map
                      (fn [spell-key]
                        (let [spell (spells/spell-map spell-key)]
                          (t/option
                           (str lvl " - " (:name spell))
                           spell-key
                           []
                           [(modifiers/spells-known lvl spell-key ability (class-names class-key))])))
                      (apply-spell-restriction spell-keys restriction)))
                   all-spells)))
                num
                (if (not acquire?) num)
                acquire?
                (if acquire?
                  (fn [s o v] {::entity/key nil})))])))
   {}
   spells-known))

(defn spellcasting-template [{:keys [class-key
                                     level-factor
                                     cantrips-known
                                     spells-known
                                     known-mode
                                     ability] :as cfg}]
  (let [spell-selections (spells-known-selections cfg)
        cantrip-selections (cantrip-selections class-key ability cantrips-known)]
    {:selections (merge-with
                  concat
                  cantrip-selections
                  spell-selections)}))

(defn magic-initiate-option [class-key spellcasting-ability spell-lists]
  (t/option
   (name class-key)
   class-key
   [(t/selection
     "Cantrip"
     (spell-options (get-in spell-lists [class-key 0]) 0 spellcasting-ability (class-names class-key))
     2 2)
    (t/selection
     "1st Level Spell"
     (spell-options (get-in spell-lists [class-key 1]) 1 spellcasting-ability (class-names class-key))
     1 1)]
   []))

(defn ritual-caster-option [class-key spellcasting-ability spell-lists]
  (t/option
   (name class-key)
   class-key
   [(t/selection
     "1st Level Ritual"
     (spell-options (filter (fn [spell-kw] (:ritual (spells/spell-map spell-kw))) (get-in spell-lists [class-key 1])) 1 spellcasting-ability (class-names class-key))
     2
     2)]
   []))

(defn language-selection [langs num]
  (t/selection
   "Languages"
   (map
    (fn [lang]
      (language-option lang))
    langs)
   num
   num))

(defn maneuver-option [name]
  (t/option
   name
   (common/name-to-kw name)
   nil
   [(modifiers/trait (str name " Maneuver"))]))

(defn proficiency-help [num singular plural]
  (str "Select additional " (if (> num 1) plural singular) " for which you are proficient."))

(defn skill-selection
  ([num]
   (skill-selection (map :key skills) num))
  ([options num & [order key prereq-fn]]
   (t/selection-cfg
    {:name "Skill Proficiency"
     :key key
     :order order
     :help (proficiency-help num "a skill" "skills")
     :options (skill-options
      (filter
       (comp (set options) :key)
       skills))
     :min num
     :max num
     :prereq-fn prereq-fn})))

(defn tool-selection
  ([num]
   (t/selection-cfg
    {:name "Tool Proficiency"
     :help (proficiency-help num "a tool" "tools")
     :options (tool-options tools)
     :min num
     :max num}))
  ([options num]
   (t/selection-cfg
    {:name "Tool Proficiency"
     :help (proficiency-help num "a tool" "tools")
     :options (tool-options
               (filter
                (comp (set options) :key)
                tools))
     :min num
     :max num})))


(defn weapon-proficiency-selection
  ([num]
   (t/selection-cfg
    {:name "Weapon Proficiency"
     :help (proficiency-help num "a weapon" "weapons")
     :options (weapon-proficiency-options weapons)
     :min num
     :max num}))
  ([options num]
   (t/selection-cfg
    {:name "Tool Proficiency"
     :help (proficiency-help num "a weapon" "weapons")
     :options (weapon-proficiency-options
               (filter
                (comp (set options) :key)
                tools))
     :min num
     :max num})))

(defn skilled-selection [title]
  (t/selection
   title
   [(t/select-option
     "Skill"
     [(skill-selection 1)])
    (t/select-option
     "Tool"
     [(tool-selection 1)])]))

(def maneuver-options
  [(maneuver-option "Commander's Strike")
   (maneuver-option "Disarming Attack")
   (maneuver-option "Distracting Strike")
   (maneuver-option "Evasive Footwork")
   (maneuver-option "Feinting Attack")
   (maneuver-option "Goading Attack")
   (maneuver-option "Lunging Attack")
   (maneuver-option "Manuevering Attack")
   (maneuver-option "Menacing Attack")
   (maneuver-option "Parry")
   (maneuver-option "Precision Attack")
   (maneuver-option "Pushing Attack")
   (maneuver-option "Rally")
   (maneuver-option "Riposte")
   (maneuver-option "Sweeping Attack")
   (maneuver-option "Trip Attack")])

(def can-cast-spell-prereq
  {::t/label "spellcasting ability"
   ::t/prereq-fn (fn [c] (some (fn [[k v]] (seq v)) (:spells-known c)))})

(def feat-options
  [(t/option
    "Alert"
    :alert
    nil
    [(modifiers/initiative 5)
     (modifiers/trait "Alert Feat")])
   (t/option
    "Athlete"
    :athlete
    [(ability-increase-selection [:str :dex] 1 false)]
    [(modifiers/trait "Athlete Feat")])
   (t/option
    "Actor"
    :actor
    []
    [(modifiers/ability :cha 1)
     (modifiers/trait "Actor Feat")])
   (t/option
    "Charger"
    :charger
    []
    [(modifiers/trait "Charger Feat")])
   (t/option
    "Crossbow Expert"
    :crossbow-expert
    []
    [(modifiers/trait "Crossbow Expert Feat")])
   (t/option
    "Defensive Duelist"
    :defensive-duelist
    []
    [(modifiers/trait "Defensive Duelist Feat")]
    [(ability-prereq :dex 13)])
   (t/option
    "Dual Wielder"
    :dual-wielder
    []
    [(modifiers/trait "Dual Wielder Feat")])
   (t/option
    "Dungeon Delver"
    :dungeon-delver
    []
    [(modifiers/trait "Dungeon Delver Feat")
     (modifiers/damage-resistance :trap)])
   (t/option
    "Durable"
    :durable
    []
    [(modifiers/trait "Durable Feat")
     (modifiers/ability :con 1)])
   (t/option
    "Elemental Adept"
    :elemental-adept
    []
    [(modifiers/trait "Elemental Adept Feat")]
    [can-cast-spell-prereq])
   (t/option
    "Grappler"
    :grappler
    []
    [(modifiers/trait "Grappler Feat")]
    [(ability-prereq :str 13)])
   (t/option
    "Great Weapon Master"
    :great-weapon-master
    []
    [(modifiers/trait "Great Weapon Master Feat")])
   (t/option
    "Healer"
    :healer
    []
    [(modifiers/trait "Healer Feat")
     (modifiers/action "Healer Feat Action")])
   (t/option
    "Heavily Armored"
    :heavily-armored
    []
    [(modifiers/heavy-armor-proficiency)
     (modifiers/ability :str 1)]
    [(armor-prereq :medium)])
   (t/option
    "Heavy Armor Master"
    :heavy-armor-master
    []
    [(modifiers/ability :str 1)
     (modifiers/trait "Heavy Armor Master Feat")]
    [(armor-prereq :heavy)])
   (t/option
    "Inspiring Leader"
    :inspiring-leader
    []
    [(modifiers/trait "Inspiring Leader Feat")]
    [(ability-prereq :cha 13)])
   (t/option
    "Keen Mind"
    :keen-mind
    []
    [(modifiers/ability :int 1)
     (modifiers/trait "Keen Mind Feat")])
   (t/option
    "Lightly Armored"
    :lightly-armored
    [(ability-increase-selection [:str :dex] 1 false)]
    [(modifiers/light-armor-proficiency)])
   (t/option
    "Linguist"
    :linguist
    [(language-selection languages 3)]
    [(modifiers/ability :int 1)])
   (t/option
    "Lucky"
    :lucky
    nil
    [(modifiers/trait "Lucky Feat")])
   (t/option
    "Mage Slayer"
    :mage-slayer
    nil
    [(modifiers/trait "Mage Slayer Feat")])
   (t/option
    "Magic Initiate"
    :magic-initiate
    [(t/selection
      "Spell Class"
      [(magic-initiate-option :bard :cha sl/spell-lists)
       (magic-initiate-option :cleric :wis sl/spell-lists)
       (magic-initiate-option :druid :wis sl/spell-lists)
       (magic-initiate-option :sorcerer :cha sl/spell-lists)
       (magic-initiate-option :warlock :cha sl/spell-lists)
       (magic-initiate-option :wizard :int sl/spell-lists)])]
    [])
   (t/option
    "Martial Adept"
    :martial-adept
    [(t/selection
      "Martial Maneuvers"
      maneuver-options
      2 2)]
    [(modifiers/trait "Martial Adept Feat")])
   (t/option
    "Medium Armor Master"
    :medium-armor-master
    []
    [(modifiers/trait "Medium Armor Master Feat")
     (mods/modifier ?max-medium-armor-bonus 3)
     (mods/fn-mod ?armor-stealth-disadvantage?
                  (fn [armor]
                    (if (= :medium (:type armor))
                      false
                      (?armor-stealth-disadvantage? armor))))]
    [(armor-prereq :medium)])
   (t/option
    "Mobile"
    :mobile
    []
    [(modifiers/speed 10)
     (modifiers/trait "Mobile Feat")])
   (t/option
    "Moderately Armored"
    :moderately-armored
    [(ability-increase-selection [:str :dex] 1 false)]
    [(modifiers/medium-armor-proficiency)
     (modifiers/shield-armor-proficiency)]
    [(armor-prereq :light)])
   (t/option
    "Mounted Combatant"
    :mounted-combatant
    []
    [(modifiers/trait "Mounted Combatant Feat")])
   (t/option
    "Observant"
    :observant
    [(ability-increase-selection [:int :wis] 1 false)]
    [(modifiers/trait "Observant Feat")
     (modifiers/passive-perception 5)
     (modifiers/passive-investigation 5)])
   (t/option
    "Polearm Master"
    :polearm-master
    []
    [(modifiers/trait "Polearm Master Feat")])
   (t/option
    "Resilient"
    :resilient
    [(t/selection
      "Ability"
      (map
       (fn [ability-key]
         (t/option
          (s/upper-case (name ability-key))
          ability-key
          []
          [(modifiers/ability ability-key 1)
           (modifiers/saving-throws ability-key)]))
       character/ability-keys))]
    [])
   (t/select-option
    "Ritual Caster"
    [(t/selection
      "Spell Class"
      [(ritual-caster-option :bard :cha sl/spell-lists)
       (ritual-caster-option :cleric :wis sl/spell-lists)
       (ritual-caster-option :druid :wis sl/spell-lists)
       (ritual-caster-option :sorcerer :cha sl/spell-lists)
       (ritual-caster-option :warlock :cha sl/spell-lists)
       (ritual-caster-option :wizard :int sl/spell-lists)])]
    [{::t/label "Intelligence or Wisdom 13 or higher"
      ::t/prereq-fn (fn [{{:keys [wis int]} :abilities}]
                      (or (>= wis 13)
                          (>= int 13)))}])
   (t/option
    "Savage Attacker"
    :savage-attacker
    []
    [(modifiers/trait "Savage Attacker Feat")])
   (t/option
    "Sentinal"
    :sentinal
    []
    [(modifiers/trait "Sentinal Feat")])
   (t/mod-option
    "Sharpshooter"
    [(modifiers/trait "Sharpshooter Feat")])
   (t/mod-option
    "Shield Master"
    [(modifiers/trait "Shield Master Feat")])
   (t/select-option
    "Skilled"
    [(skilled-selection "Skill/Tool 1")
     (skilled-selection "Skill/Tool 2")
     (skilled-selection "Skill/tool 3")])
   (t/mod-option
    "Skulker"
    [(modifiers/trait "Skulker Feat")]
    [(ability-prereq :dex 13)])
   (t/mod-option
    "Spell Sniper"
    [(modifiers/trait "Spell Sniper Feat")]
    [can-cast-spell-prereq])
   (t/option
    "Tavern Brawler"
    :tavern-brawler
    [(ability-increase-selection [:str :dex] 1 false)]
    [(modifiers/weapon-proficiency "Improvised Weapons" :improvised)
     (modifiers/trait "Tavern Brawler Feat")])
   (t/mod-option
    "Tough"
    [(mods/modifier ?hit-point-level-bonus (+ 2 ?hit-point-level-bonus))])
   (t/mod-option
    "War Caster"
    [(modifiers/trait "War Caster Feat")])
   (t/select-option
    "Weapon Master"
    [(ability-increase-selection [:str :dex] 1 false)
     (weapon-proficiency-selection 4)])])


(def fighting-style-options
  [(t/option
    "Archery"
    :archery
    []
    [(modifiers/ranged-attack-bonus 2)
     (modifiers/trait "Archery Fighting Style" "You gain a +2 bonus to attack rolls you make with ranged weapons.")])
   (t/option
    "Defense"
    :defense
    []
    [(modifiers/armored-ac-bonus 1)
     (modifiers/trait "Defense Fighting Style" "While you are wearing armor, you gain a +1 bonus to AC.")])
   (t/option
    "Dueling"
    :dueling
    []
    [(modifiers/trait "Dueling Fighting Style" "When you are wielding a melee weapon in one hand and no other weapons, you gain a +2 bonus to damage rolls with that weapon.")])
   (t/option
    "Great Weapon Fighting"
    :great-weapon-fighting
    []
    [(modifiers/trait "Great Weapon Fighting Style" "When you roll a 1 or 2 on a damage die for an attack you make with a melee weapon that you are wielding with two hands, you can reroll the die and must use the new roll, even if the new roll is a 1 or a 2. The weapon must have the two-handed or versatile property for you to gain this benefit.")])
   (t/option
    "Protection"
    :protection
    []
    [(modifiers/trait "Protection Fighting Style" "When a creature you can see attacks a target other than you that is within 5 feet of you, you can use your reaction to impose disadvantage on the attack roll. You must be wielding a shield.")])
   (t/option
    "Two Weapon Fighting"
    :two-weapon-fighting
    []
    [(modifiers/trait "Two Weapon Fighting" "When you engage in two-weapon fighting, you can add your ability modifier to the damage of the second attack.")])])

(defn fighting-style-selection [character-ref & [restrictions]]
  (t/selection
   "Fighting Style"
   (if restrictions
     (filter
      (fn [o]
        (restrictions (::t/key o)))
      fighting-style-options)
     fighting-style-options)))

(defn feat-selection [num]
  (t/selection
   (if (= 1 num) "Feat" "Feats")
   feat-options
   num
   num))

(defn ability-score-improvement-selection []
  (t/selection
   "Ability Score Improvement or Feat"
   [(t/option
     "Ability Score Improvement"
     :ability-score-improvement
     [(ability-increase-selection character/ability-keys 2 false)]
     [])
    (t/option
     "Feat"
     :feat
     [(t/selection
       "Feat"
       feat-options)]
     [])]))

(defn expertise-selection [num]
  (t/selection
   "Skill Expertise"
   (mapv
    (fn [skill]
      (assoc
       (t/option
        (:name skill)
        (:key skill)
        nil
        [(modifiers/skill-expertise (:key skill))])
       ::t/prereq-fn
       (fn [built-char]
         (let [skill-profs (es/entity-val built-char :skill-profs)]
           (and skill-profs (skill-profs (:key skill)))))))
    skills)
   num
   num))

(def rogue-expertise-selection
  (t/selection
   "Expertise"
   [(t/option
     "Two Skills"
     :two-skills
     [(expertise-selection 2)]
     [])
    (t/option
     "One Skill/Theives Tools"
     :one-skill-thieves-tools
     [(expertise-selection 1)]
     [(modifiers/tool-proficiency "Thieves Tools" :thieves-tools)])]))

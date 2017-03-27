(ns orcpub.dnd.e5.spells
  (:require [orcpub.common :as common]))

(def spells
  [{:name "Acid Splash"}
   {:name "Aid"}
   {:name "Alarm"}
   {:name "Alter Self"}
   {:name "Animal Friendship"}
   {:name "Animal Messenger"}
   {:name "Animal Shapes"}
   {:name "Animate Dead"}
   {:name "Animate Objects"}
   {:name "Antilife Shell"}
   {:name "Antimagic Field"}
   {:name "Antipathy/Sympathy"}
   {:name "Arcane Eye"}
   {:name "Arcane Gate"}
   {:name "Arcane Lock"}
   {:key :armor-of-agathys, :name "Armor of Agath."}
   {:key :arms-of-hadar, :name "Arms of Had."}
   {:name "Astral Projection"}
   {:name "Augury"}
   {:name "Aura of Life"}
   {:name "Aura of Purity"}
   {:name "Awaken"}
   {:name "Bane"}
   {:name "Banishing Smite"}
   {:name "Banishment"}
   {:name "Barkskin"}
   {:name "Beacon of Hope"}
   {:name "Beast Sense"}
   {:name "Bestow Curse"}
   {:key :bigbys-hand, :name "B.'s Hand (Arcane Hand)"}
   {:name "Blade Barrier"}
   {:name "Blade Ward"}
   {:name "Bless"}
   {:name "Blight"}
   {:name "Blinding Smite"}
   {:name "Blindness/Deafness"}
   {:name "Blink"}
   {:name "Blur"}
   {:name "Branding Smite"}
   {:name "Burning Hands"}
   {:name "Call Lightning"}
   {:name "Calm Emotions"}
   {:name "Chain Lightning"}
   {:name "Charm Person"}
   {:name "Chill Touch"}
   {:name "Chromatic Orb"}
   {:name "Circle of Death"}
   {:name "Circle of Power"}
   {:name "Clairvoyance"}
   {:name "Clone"}
   {:name "Cloud of Daggers"}
   {:name "Cloudkill"}
   {:name "Color Spray"}
   {:name "Command"}
   {:name "Commune"}
   {:name "Commune with Nature"}
   {:name "Compelling Duel"}
   {:name "Comprehend Languages"}
   {:name "Compulsion"}
   {:name "Cone of Cold"}
   {:name "Confusion"}
   {:name "Conjure Animals"}
   {:name "Conjure Barrage"}
   {:name "Conjure Celestial"}
   {:name "Conjure Elemental"}
   {:name "Conjure Fey"}
   {:name "Conjure Minor Elementals"}
   {:name "Conjure Volley"}
   {:name "Conjure Woodland Beings"}
   {:name "Contact Other Plane"}
   {:name "Contagion"}
   {:name "Contingency"}
   {:name "Continual Flame"}
   {:name "Control Water"}
   {:name "Control Weather"}
   {:name "Cordon of Arrows"}
   {:name "Counterspell"}
   {:name "Create Food and Water"}
   {:name "Create or Destroy Water"}
   {:name "Create Undead"}
   {:name "Creation"}
   {:name "Crown of Madness"}
   {:name "Crusader's Mantle"}
   {:name "Cure Wounds"}
   {:name "Dancing Lights"}
   {:name "Darkness"}
   {:name "Darkvision"}
   {:name "Daylight"}
   {:name "Death Ward"}
   {:name "Delayed Blast Fireball"}
   {:name "Demiplane"}
   {:name "Destructive Wave"}
   {:name "Detect Evil and Good"}
   {:name "Detect Magic"}
   {:name "Detect Poison and Disease"}
   {:name "Detect Thoughts"}
   {:name "Dimension Door"}
   {:name "Disguise Self"}
   {:name "Disintegrate"}
   {:name "Dispel Evil and Good"}
   {:name "Dispel Magic"}
   {:name "Dissonant Whispers"}
   {:name "Divination"}
   {:name "Divine Favor"}
   {:name "Divine Word"}
   {:name "Dominate Beast"}
   {:name "Dominate Monster"}
   {:name "Dominate Person"}
   {:key :drawmijs-instant-summons, :name "Drawm.'s Instant Summons"}
   {:name "Dream"}
   {:name "Druidcraft"}
   {:name "Earthquake"}
   {:name "Eldritch Blast"}
   {:name "Elemental Weapon"}
   {:name "Enhance Ability"}
   {:name "Enlarge/Reduce"}
   {:name "Ensnaring Strike"}
   {:name "Entangle"}
   {:name "Enthrall"}
   {:name "Etherealness"}
   {:key :evards-black-tentacles, :name "Ev.'s Black Tentacles"}
   {:name "Expeditious Retreat"}
   {:name "Eyebite"}
   {:name "Fabricate"}
   {:name "Faerie Fire"}
   {:name "False Life"}
   {:name "Fear"}
   {:name "Feather Fall"}
   {:name "Feeblemind"}
   {:name "Feign Death"}
   {:name "Find Familiar"}
   {:name "Find Steed"}
   {:name "Find the Path"}
   {:name "Find Traps"}
   {:name "Finger of Death"}
   {:name "Fireball"}
   {:name "Fire Bolt"}
   {:name "Fire Shield"}
   {:name "Fire Storm"}
   {:name "Flame Blade"}
   {:name "Flame Strike"}
   {:name "Flaming Sphere"}
   {:name "Flesh to Stone"}
   {:name "Fly"}
   {:name "Fog Cloud"}
   {:name "Forbiddance"}
   {:name "Forcecage"}
   {:name "Foresight"}
   {:name "Freedom of Movement"}
   {:name "Friends"}
   {:name "Gaseous Form"}
   {:name "Gate"}
   {:name "Geas"}
   {:name "Gentle Repose"}
   {:name "Giant Insect"}
   {:name "Glibness"}
   {:name "Globe of Invulnerability"}
   {:name "Glyph of Warding"}
   {:name "Goodberry"}
   {:name "Grasping Vine"}
   {:name "Grease"}
   {:name "Greater Invisibility"}
   {:name "Greater Restoration"}
   {:name "Guardian of Faith"}
   {:name "Guards and Wards"}
   {:name "Guidance"}
   {:name "Guiding Bolt"}
   {:name "Gust of Wind"}
   {:name "Hail of Thorns"}
   {:name "Hallow"}
   {:name "Hallucinatory Terrain"}
   {:name "Harm"}
   {:name "Haste"}
   {:name "Heal"}
   {:name "Healing Word"}
   {:name "Heat Metal"}
   {:name "Hellish Rebuke"}
   {:name "Heroes' Feast"}
   {:name "Heroism"}
   {:name "Hex"}
   {:name "Hold Monster"}
   {:name "Hold Person"}
   {:name "Holy Aura"}
   {:name "Hunger of Had."}
   {:name "Hunter's Mark"}
   {:name "Hypnotic Pattern"}
   {:name "Ice Storm"}
   {:name "Identify"}
   {:name "Illusory Script"}
   {:name "Imprisonment"}
   {:name "Incendiary Cloud"}
   {:name "Inflict Wounds"}
   {:name "Insect Plague"}
   {:name "Invisibility"}
   {:name "Jump"}
   {:name "Knock"}
   {:name "Legend Lore"}
   {:key :leomunds-tiny-hut, :name "Leo.'s Tiny Hut"}
   {:key :leomunds-secret-chest, :name "Leo.'s Secret Chest"}
   {:name "Lesser Restoration"}
   {:name "Levitate"}
   {:name "Light"}
   {:name "Lightning Arrow"}
   {:name "Lightning Bolt"}
   {:name "Locate Animals or Plants"}
   {:name "Locate Creature"}
   {:name "Locate Object"}
   {:name "Longstrider"}
   {:name "Mage Armor"}
   {:name "Mage Hand"}
   {:name "Magic Circle"}
   {:name "Magic Jar"}
   {:name "Magic Missile"}
   {:name "Magic Mouth"}
   {:name "Magic Weapon"}
   {:name "Major Image"}
   {:name "Mass Cure Wounds"}
   {:name "Mass Heal"}
   {:name "Mass Healing Word"}
   {:name "Mass Suggestion"}
   {:name "Maze"}
   {:name "Meld into Stone"}
   {:key :melfs-acid-arrow, :name "M.'s Acid Arrow"}
   {:name "Mending"}
   {:name "Message"}
   {:name "Meteor Swarm"}
   {:name "Mind Blank"}
   {:name "Minor Illusion"}
   {:name "Mirage Arcane"}
   {:name "Mirror Image"}
   {:name "Mislead"}
   {:name "Misty Step"}
   {:name "Modify Memory"}
   {:name "Moonbeam"}
   {:key :mordenkainens-faithful-hound, :name "Mord.'s Faithful Hound"}
   {:key :mordenkainens-magnificent-mansion,
    :name "Mord.'s Magnificent Mansion"}
   {:key :mordenkainens-private-sanctum, :name "Mord.'s Private Sanctum"}
   {:key :mordenkainens-sword, :name "Mord.'s Sword (Arcane Sword)"}
   {:name "Move Earth"}
   {:name "Nondetection"}
   {:key :nystuls-magic-aura,
    :name "Nyst.'s Magic Aura (Arcanist's Magic Aura)"}
   {:name "Pass without Trace"}
   {:name "Passwall"}
   {:name "Phantasmal Force"}
   {:name "Phantasmal Killer"}
   {:name "Phantom Steed"}
   {:name "Planar Ally"}
   {:name "Planar Binding"}
   {:name "Plane Shift"}
   {:name "Plant Growth"}
   {:name "Poison Spray"}
   {:name "Polymorph"}
   {:name "Power Word Heal"}
   {:name "Power Word Kill"}
   {:name "Power Word Stun"}
   {:name "Prayer of Healing"}
   {:name "Prestidigitation"}
   {:name "Prismatic Spray"}
   {:name "Prismatic Wall"}
   {:name "Produce Flame"}
   {:name "Programmed Illusion"}
   {:name "Project Image"}
   {:name "Protection from Energy"}
   {:name "Protection from Evil and Good"}
   {:name "Protection from Poison"}
   {:name "Purify Food and Drink"}
   {:key :otilukes-freezing-sphere, :name "Otil.'s Freezing Sphere"}
   {:key :otilukes-resilient-sphere, :name "Otil.'s Resilient Sphere"}
   {:key :ottos-irresistible-dance, :name "O.'s Irresistible Dance"}
   {:name "Raise Dead"}
   {:name "Rar.'s Telepathic Bond"}
   {:name "Ray of Enfeeblement"}
   {:name "Ray of Frost"}
   {:name "Ray of Sickness"}
   {:name "Regenerate"}
   {:name "Reincarnate"}
   {:name "Remove Curse"}
   {:name "Resistance"}
   {:name "Resurrection"}
   {:name "Reverse Gravity"}
   {:name "Revivify"}
   {:name "Rope Trick"}
   {:name "Sacred Flame"}
   {:name "Sanctuary"}
   {:name "Scorching Ray"}
   {:name "Scrying"}
   {:name "Searing Smite"}
   {:name "See Invisibility"}
   {:name "Seeming"}
   {:name "Sending"}
   {:name "Sequester"}
   {:name "Shapechange"}
   {:name "Shatter"}
   {:name "Shield"}
   {:name "Shield of Faith"}
   {:name "Shillelagh"}
   {:name "Shocking Grasp"}
   {:name "Silence"}
   {:name "Silent Image"}
   {:name "Simulacrum"}
   {:name "Sleep"}
   {:name "Sleet Storm"}
   {:name "Slow"}
   {:name "Spare the Dying"}
   {:name "Speak with Animals"}
   {:name "Speak with Dead"}
   {:name "Speak with Plants"}
   {:name "Spider Climb"}
   {:name "Spike Growth"}
   {:name "Spirit Guardians"}
   {:name "Spiritual Weapon"}
   {:name "Staggering Smite"}
   {:name "Stinking Cloud"}
   {:name "Stone Shape"}
   {:name "Stoneskin"}
   {:name "Storm of Vengeance"}
   {:name "Suggestion"}
   {:name "Sunbeam"}
   {:name "Sunburst"}
   {:name "Swift Quiver"}
   {:name "Symbol"}
   {:key :tashas-hideous-laughter, :name "T.'s Hideous Laughter"}
   {:name "Telekinesis"}
   {:name "Telepathy"}
   {:name "Teleport"}
   {:name "Teleportation Circle"}
   {:key :tensers-floating-disk, :name "Tens.'s Floating Disk"}
   {:name "Thaumaturgy"}
   {:name "Thorn Whip"}
   {:name "Thunderwave"}
   {:name "Time Stop"}
   {:name "Tongues"}
   {:name "Transport via Plants"}
   {:name "Tree Stride"}
   {:name "True Polymorph"}
   {:name "True Resurrection"}
   {:name "True Seeing"}
   {:name "True Strike"}
   {:name "Tsunami"}
   {:name "Unseen Servant"}
   {:name "Vampiric Touch"}
   {:name "Vicious Mockery"}
   {:name "Wall of Fire"}
   {:name "Wall of Force"}
   {:name "Wall of Ice"}
   {:name "Wall of Stone"}
   {:name "Wall of Thorns"}
   {:name "Warding Bond"}
   {:name "Water Breathing"}
   {:name "Water Walk"}
   {:name "Web"}
   {:name "Weird"}
   {:name "Wind Walk"}
   {:name "Wind Wall"}
   {:name "Wish"}
   {:name "Witch Bolt"}
   {:name "Word of Recall"}
   {:name "Wrathful Smite"}
   {:name "Zone of Truth"}])

(def spell-map
  (into
   {}
   (map
    (fn [spell]
      [(or (:key spell)
           (common/name-to-kw (:name spell)))
       spell]))
   spells))

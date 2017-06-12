(ns orcpub.dnd.e5.character.random
  (:require [clojure.string :as s]))

(def calishite-names
  {::male ["Aseir"
          "Bardeid"
          "Haseid"
          "Khemed"
          "Mehmen"
          "Sudeiman"
          "Zashier"
          "Pasha"
          "Faruk"
          "Kwala"
          "Abbas"
          "Ashkan"
          "Ahmad"
          "Ali"
          "Amin"
          "Amir"
          "Ardashir"
          "Afshin"
          "Arshad"
          "Arman"
          "Armin"
          "Amir"
          "Arash"
          "Ardeshir"
          "Arvin"
          "Arwin"
          "Ashem"
          "Astash"
          "Bijan"
          "Babak"
          "Bahrom"
          "Bardia"
          "Basir"
          "Behnam"
          "Dalir"
          "Dariush"
          "Davoud"
          "Darafsh"
          "Ervin"
          "Erwin"
          "Ehsan"
          "Eskandar"
          "Esmail"
          "Izad"
          "Farhad"
          "Farbod"
          "Farrokh"
          "Farhid"
          "Giv"
          "Goshtasp"
          "Hamid"
          "Hassan"
          "Hirbod"
          "Hashem"
          "Hormuzd"
          "Jamshid"
          "Javad"
          "Kamran"
          "Karim"
          "Kasra"
          "Kazem"
          "Khashayr"
          "Khosrow"
          "Kiarash"
          "Kourosh"
          "Mazdak"
          "Mazdan"
          "Maziar"
          "Mehran"
          "Manuchehr"
          "Marduk"
          "Mehrdad"
          "Marzban"
          "Medhi"
          "Meysam"
          "Milad"
          "Mir"
          "Mithradates"
          "Musa"
          "Omid"
          "Papak"
          "Parizad"
          "Parsa"
          "Parviz"
          "Payam"
          "Pedram"
          "Piruz"
          "Pouria"
          "Ramin"
          "Reza"
          "Rostam"
          "Sadegh"
          "Sajad"
          "Saman"
          "Samir"
          "Sassan"
          "Sepehr"
          "Shahin"
          "Shapour"
          "Shahryar"
          "Shervin"
          "Sohrab"
          "Souroush"
          "Tirdad"
          "Turan"
          "Vahid"
          "Vandad"
          "Varshasp"
          "Yaghoub"
          "Yahya"
          "Younes"
          "Yazdan"
          "Zand"
          "Zartosht"
          "Zurvan"]
   ::female ["Atala"
            "Ceidil"
            "Hama"
            "Jasmal"
            "Meilil"
            "Seipora"
            "Yasheira"
            "Zasheida"
            "Oma"
            "Anahita"
            "Anousheh"
            "Arezu"
            "Ashraf"
            "Astar"
            "Atoosa"
            "Azar"
            "Azadeh"
            "Banu"
            "Baharak"
            "Farnaz"
            "Farnzaneh"
            "Fatemeh"
            "Fereshteh"
            "Goli"
            "Jaleh"
            "Katayoun"
            "Kiana"
            "Khorshid"
            "Laleh"
            "Leila"
            "Mandana"
            "Mahshid"
            "Maryam"
            "Mehregan"
            "Mina"
            "Mithra"
            "Mojugan"
            "Nasrin"
            "Nazanin"
            "Niloufar"
            "Parastu"
            "Pardis"
            "Parisa"
            "Parvin"
            "Payvand"
            "Reyhan"
            "Roksaneh"
            "Roya"
            "Roxana"
            "Sepideh"
            "Shirin"
            "Simin"
            "Soraya"
            "Sheila"
            "Sahar"
            "Tahmineh"
            "Taraneh"
            "Tarsa"
            "Tannaz"
            "Yasmin"
            "Yasamin"
            "Zarine"
            "Zhila"
            "Zaynab"]
   ::surname ["Basha"
             "Dumein"
             "Jassan"
             "Khalid"
             "Mostana"
             "Pashar"
             "Rein"
             "Abbasi"
             "Abed"
             "Ahura"
             "Ardavan"
             "Aria"
             "Armand"
             "Avesta"
             "Esfahani"
             "Esfandiari"
             "Farrokhzad"
             "Farahmand"
             "Ghorbani"
             "Gul"
             "Hanifnejad"
             "Hashemi"
             "Homayoun"
             "Hooshang"
             "Jahandar"
             "Jahangir"
             "Jahanshah"
             "Jamshidi"
             "Jang-Ju"
             "Kavoosi"
             "Kazemi"
             "Khadem"
             "Khiabani"
             "Khorasani"
             "Khorram-Din"
             "Lajani"
             "Lahijani"
             "Madani"
             "Mazdaki"
             "Mazdani"
             "Mehregan"
             "Mazandarani"
             "Mokri"
             "Mohsen"
             "Pahlavi"
             "Paria"
             "Parsi"
             "Pouran"
             "Rahbar"
             "Rajavi"
             "Rezaei"
             "Rostamian"
             "Sassani"
             "Shamshiri"
             "Shir-Del"
             "Shirazi"
             "Teymouri"
             "Tir"
             "Turan"
             "Turani"]})

(def chondathan-names
  {::male ["Darvin"
          "Dorn"
          "Evendur"
          "Gorstag"
          "Grim"
          "Helm"
          "Malark"
          "Morn"
          "Mirt"
          "Morn"
          "Kirt"
          "Mort"
          "Randal"
          "Stedd"
          "Reed"]
   ::female ["Arveene"
            "Esvele"
            "Jhessail"
            "Kerri"
            "Lureene"
            "Miri"
            "Niri"
            "Rowan"
            "Shandri"
            "Shandra"
            "Tessele"]
   ::surname-pre ["Amble"
                 "Buck"
                 "Dun"
                 "Even"
                 "Grey"
                 "Tall"
                 "Red"
                 "Green"
                 "Gold"
                 "Silver"
                 "High"]
   ::surname-post ["crown"
                 "man"
                 "dragon"
                 "wood"
                 "castle"
                  "stag"]})

;; macedonian based
(def damaran-names
  {::male ["Bor"
          "Fodel"
          "Glar"
          "Grigor"
          "Igan"
          "Ivor"
          "Kosef"
          "Mival"
          "Orel"
          "Pavel"
          "Sergor"
          "Andon"
          "Bogdan"
          "Bogomil"
          "Bojan"
          "Darko"
          "Dragan"
          "Emil"
          "Kiril"
          "Kliment"
          "Vasko"
          "Dzvonko"
          "Dimitar"]
   ::male-pre ["To"
              "Ho"
              "Ris"
              "Mir"
              "Mar"
              "Jor"
              "Jo"
              "I"
              "Gli"
              "Gri"
              "E"
              "Dar"
              "Bor"]
   ::male-post ["do"
               "dor"
               "to"
               "ko"
               "tej"
               "dan"
               "sif"
               "vo"
               "sim"
               "gor"
               "mil"]
   ::female ["Alethra"
            "Kara"
            "Katernin"
            "Mara"
            "Natali"
            "Olma"
            "Tana"
            "Zora"
            "Bogdana"
            "Dafina"
            "Danica"
            "Dragana"
            "Elena"
            "Gordana"
            "Gorica"
            "Vaska"
            "Vera"
            "Vesna"]
   ::female-pre ["Dan"
                "Daf"
                "Gal"
                "Fros"
                "Filim"
                "Is"
                "Iv"
                "Jasm"
                "Jord"
                "Kal"
                "Kat"
                "Lilj"
                "Lid"
                "Mar"
                "Melan"
                "Mil"
                "Mir"
                "Rum"
                "Ram"
                "Sof"]
   ::female-post ["ica"
                 "ina"
                 "ana"
                 "ena"
                 "ona"
                 "ija"]
   ::surname ["Bersk"
             "Chernin"
             "Dotsk"
             "Kulenov"
             "Marsk"
             "Nemetsk"
             "Shemov"
             "Starag"]
   ::surname-pre ["Mark"
                 "Panch"
                 "Naum"
                 "Nikol"
                 "Lazar"
                 "Koch"
                 "Ilich"
                 "Jan"
                 "Jovan"
                 "Stojon"
                 "Rus"
                 "Rist"
                 "Ris"
                 "Pand"
                 "Petk"
                 "Mitr"]
   ::surname-post ["ev"
                   "ov"
                   "ovsk"
                   "ovski"
                   "evski"
                   "evsk"
                   "esk"
                   "eski"
                   "etsk"]})

(def turami-names
  {::male ["Anton"
          "Diero"
          "Marcon"
          "Pieron"
          "Rimardo"
          "Romero"
          "Salazar"
          "Umbero"]
   ::male-pre ["An"
              "Pie"
              "Ri"
              "Sala"
              "Mar"
              "Die"
              "Ba"
              "Ab"
              "Bar"]
   ::male-post ["ro"
               "dol"
               "tol"
               "ron"
               "con"
               "bero"
               "zar"
               "mardo"
               "kar"]
   ::female ["Balama"
            "Dona"
            "Faila"
            "Jalana"
            "Luisa"
            "Marta"
            "Quara"
            "Selise"
            "Vonda"]
   ::female-pre ["Do"
                "Bala"
                "Aba"
                "Qua"
                "Seli"
                "Von"
                "Lui"
                "Ara"
                "Bibi"]
   ::female-post ["da"
                 "na"
                 "sa"
                 "ma"
                 "se"
                 "rne"
                 "ne"]
   ::surname ["Agosto"
             "Astorio"
             "Calabra"
             "Domine"
             "Falone"
             "Marivaldi"
             "Pisacar"
             "Ramondo"
             "Aroztegi"
             "Bidarte"
             "Bolibar"
             "Elkano"
             "Elizondo"
             "Etxandi"
             "Etxarte"
             "Etxeberri"
             "Ibarra"
             "Lekubarri"
             "Loiola"
             "Mendiluze"
             "Urberoaga"
             "Zabala"
             "Zubiondo"
             "Eneko"]
   ::surname-pre ["Mari"
                 "Asta"
                 "Asto"
                 "Do"
                 "Cala"
                 "Pisa"
                 "I"
                 "Za"]
   ::surname-post ["buri"
                  "buru"
                  "lando"
                  "mondo"
                  "car"
                  "rte"
                  "neko"
                  "gorri"
                  "turri"
                  "luze"
                  "bala"
                  "kano"
                  "xarte"
                  "zondo"
                  "barri"]})

(def shou-names
  {::male ["An"
          "Chen"
          "Chi"
          "Fai"
          "Jiang"
          "Jun"
          "Lian"
          "Long"
          "Meng"
          "On"
          "Shan"
          "Shui"
          "Wen"
          "Li"
          "Ling"
          "Sheng"
          "Si"
          "Song"
          "Zhao"]
   :pre ["Ji"
         "Xi"
         "J"
         "M"
         "Ch"
         "Chi"
         "Sh"
         "Shi"
         "L"
         "Li"
         "F"
         "Fi"]
   :post ["ang"
          "an"
          "eng"
          "ong"
          "en"
          "on"]
   ::surname ["Chien"
             "Huang"
             "Kao"
             "Kung"
             "Lao"
             "Ling"
             "Mei"
             "Pin"
             "Shin"
             "Sum"
             "Tan"
             "Wan"
             "Wu"
             "Kong"
             "Ma"
             "Cheng"
             "Tan"
             "He"
             "Hu"
             "Mao"]
   ::female ["Bai"
            "Chao"
            "Jia"
            "Lie"
            "Mei"
            "Qiao"
            "Shui"
            "Tai"
            "Fen"
            "Fan"
            "Hui"
            "Ju"
            "Jun"
            "Lan"
            "Lei"
            "Liling"
            "Min"
            "Liu"
            "Nuo"
            "Shu"
            "Qiu"
            "Ting"
            "Wei"
             "Wen"]})

(def illuskan-names
  {::male ["Ander"
          "Blath"
          "Bran"
          "Frath"
          "Geth"
          "Lander"
          "Luth"
          "Malcer"
          "Stor"
          "Taman"
           "Urth"
           "Alwyn"
           "Andras"
           "Anwen"
           "Arwyn"
           "Bron"
           "Brin"
           "Delwyn"
           "Drystan"
           "Emlyn"
           "Gareth"
           "Glendower"
           "Gwil"
           "Gwilym"
           "Gwyn"
           "Ilar"
           "Ivor"
           "Madoc"
           "Maldwyn"
           "Meuric"
           "Morgan"
           "Owena"
           "Pryce"
           "Rhodri"
           "Rhydderch"
           "Roderick"
           "Sawyl"
           "Sieffre"
           "Siorus"
           "Talfryn"
           "Taliesin"
           "Trystan"
           "Tudor"
           "Urien"
           "Wynfor"
           "Tomi"]
   ::female ["Amafrey"
             "Betha"
             "Cefrey"
             "Kethra"
             "Mara"
             "Olga"
             "Silifrey"
             "Westra"
             "Carys"
             "Ceri"
             "Delwyn"
             "Delyth"
             "Deryn"
             "Elain"
             "Eurwen"
             "Gaenor"
             "Glenda"
             "Glenys"
             "Glynn"
             "Gwen"
             "Gwenda"
             "Olwyn"
             "Owena"
             "Rhian"
             "Rhiannon"
             "Rhonwen"
             "Rhosyn"
             "Siana"
             "Sian"
             "Siwan"
             "Tegan"
             "Winifrey"
             "Willafrey"]
   ::surname-pre ["Wind"
                  "Storm"
                  "Bright"
                  "Horn"
                  "Lack"
                  "Stead"
                  "Sky"
                  "Green"]
   ::surname-post ["rivver"
                   "wood"
                   "man"
                   "hallow"
                   "harrow"
                   "fforest"
                   "llost"
                   "water"
                   "mor"
                   "mount"
                   "grass"
                   "raven"
                   "hawk"]})

(def rashemi-names
  {::male ["Borivik"
           "Faurgar"
           "Jandar"
           "Kanithar"
           "Madislak"
           "Ralmevik"
           "Shaumar"
           "Vladislak"
           "Bogdan"
           "Bogumir"
           "Milodrag"]
   ::pre ["Madi"
          "Vladi"
          "Ludi"
          "Kani"
          "Brato"
          "Desi"
          "Drago"
          "Kazi"
          "Lyud"
          "Radomir"
          "Rosti"
          "Stanis"
          "Sobe"
          "Tomi"
          "Veli"
          "Vito"
          "Valsti"
          "Vsevo"
          "Yaro"
          "Zvoni"]
   ::male-post ["slak"
                "vik"
                "mar"
                "thar"
                "mir"
                "mil"]
   ::female ["Fyevarra"
             "Hulmarra"
             "Immith"
             "Imzel"
             "Navarra"
             "Shevarra"
             "Tammith"
             "Yuldra"
             "Bogdana"
             "Bozhena"
             "Elena"]
   ::female-post ["slaka"
                  "vika"
                  "mara"
                  "thara"]
   ::surname-pre ["Char"
                  "Iltazy"
                  "Murnye"
                  "Staya"
                  "Ulmo"
                  "Nau"
                  "Ned"
                  "Novo"
                  "Ognya"
                  "Pakul"
                  "Pade"
                  "Pav"
                  "Pavi"
                  "Pavlo"
                  "Pecha"
                  "Pia"
                  "Pulkra"
                  "Rezni"
                  "Rudaw"
                  "Sla"
                  "Soko"
                  "Stol"
                  "Svoba"
                  "Trifo"
                  "Vanche"
                  "Velich"
                  "Wola"
                  "Zabe"
                  "Zori"]
   ::surname-post ["yov"
                   "kov"
                   "mov"
                   "vic"
                   "til"
                   "lek"
                   "goba"
                   "yara"
                   "thara"
                   "noga"
                   "kina"
                   "balek"
                   "vak"
                   "sad"
                   "nov"
                   "lov"
                   "tek"
                   "bek"
                   "zak"
                   "zyk"]})

(defn name-search-match [text]
  (re-matches #".*\bname\b.*" text))

(defn first-last [list sex]
  (str (-> list sex rand-nth)
       " "
       (-> list ::surname rand-nth)))

(defn join-names [first last]
  (str first " " last))

(defn random-item [list key]
  (-> list key rand-nth))

(defn random-sex []
  (rand-nth [::male ::female]))

(defmulti random-name (fn [{:keys [race subrace sex]}]
                        (prn "RANDOM NAME" race subrace sex)
                        [(or subrace race) sex]))

(defmethod random-name [::calishite ::male] [_]
  (first-last calishite-names ::male))

(defmethod random-name [::calishite ::female] [_]
  (first-last calishite-names ::female))

(defmethod random-name [::calishite nil] [_]
  (first-last calishite-names (random-sex)))

(defn chondathan-surname []
  (str (random-item chondathan-names ::surname-pre)
       (random-item chondathan-names ::surname-post)))

(defn chondathan-male-name []
  (str (random-item chondathan-names ::male-pre)
       (random-item chondathan-names ::male-post)))

(defmethod random-name [::chondathan ::male] [_]
  (join-names
   (random-item chondathan-names ::male)
   (chondathan-surname)))

(defmethod random-name [::chondathan ::female] [_]
  (join-names
   (random-item chondathan-names ::female)
   (chondathan-surname)))

(defmethod random-name [::chondathan nil] [_]
  (join-names
   (random-item chondathan-names (random-sex))
   (chondathan-surname)))

(defn set-name [list type]
  (random-item list type))

(defn combined-name [list pre-type post-type]
  (str (random-item list pre-type)
       (random-item list post-type)))

(defn random-set-or-combined [list type pre-type post-type]
  (rand-nth [(set-name list type) (combined-name list pre-type post-type)]))

(defmethod random-name [::turami ::male] [_]
  (join-names
   (random-set-or-combined turami-names ::male ::male-pre ::male-post)
   (random-set-or-combined turami-names ::surname ::surname-pre ::surname-post)))

(defmethod random-name [::turami ::female] [_]
  (join-names
   (random-set-or-combined turami-names ::female ::female-pre ::female-post)
   (random-set-or-combined turami-names ::surname ::surname-pre ::surname-post)))

(defmethod random-name [::turami nil] [_]
  (join-names
   (apply random-set-or-combined turami-names
          (rand-nth [[::female ::female-pre ::female-post]
                     [::male ::male-pre ::male-post]]))
   (random-set-or-combined turami-names ::surname ::surname-pre ::surname-post)))

(defn shou-name [first]
  (join-names
   first
   (random-set-or-combined shou-names ::surname :pre :post)))

(defmethod random-name [::shou ::male] [_]
  (shou-name
   (random-set-or-combined shou-names ::male :pre :post)))

(defmethod random-name [::shou ::female] [_]
  (shou-name
   (random-set-or-combined shou-names ::female :pre :post)))

(defmethod random-name [::shou nil] [_]
  (shou-name
   (random-set-or-combined shou-names (random-sex) :pre :post)))

(defn damaran-name [first]
  (join-names
   first
   (random-set-or-combined damaran-names ::surname ::surname-pre ::surname-post)))

(defmethod random-name [::damaran ::male] [_]
  (damaran-name
   (random-set-or-combined damaran-names ::male ::male-pre ::male-post)))

(defmethod random-name [::damaran ::female] [_]
  (damaran-name
   (random-set-or-combined damaran-names ::female ::female-pre ::female-post)))

(defmethod random-name [::damaran nil] [_]
  (damaran-name
   (apply random-set-or-combined damaran-names (rand-nth [[::female ::female-pre ::female-post]
                                                          [::male ::male-pre ::male-post]]))))

(defn illuskan-name [first]
  (join-names
   first
   (combined-name illuskan-names ::surname-pre ::surname-post)))

(defmethod random-name [::illuskan ::male] [_]
  (illuskan-name
   (set-name illuskan-names ::male)))

(defmethod random-name [::illuskan ::female] [_]
  (illuskan-name
   (set-name illuskan-names ::female)))

(defmethod random-name [::illuskan nil] [_]
  (illuskan-name
   (set-name illuskan-names (rand-nth [::male ::female]))))

(defn rashemi-name [first]
  (join-names
   first
   (combined-name rashemi-names ::surname-pre ::surname-post)))

(defmethod random-name [::rashemi ::male] [_]
  (rashemi-name
   (random-set-or-combined rashemi-names ::male ::pre ::male-post)))

(defmethod random-name [::rashemi ::female] [_]
  (rashemi-name
   (random-set-or-combined rashemi-names ::female ::pre ::female-post)))

(defmethod random-name [::rashemi nil] [_]
  (rashemi-name
   (apply random-set-or-combined rashemi-names (rand-nth
                                                [[::male ::pre ::male-post]
                                                 [::female ::pre ::female-post]]))))


(defn random-human-subrace []
  (rand-nth [::calishite
             ::chondathan
             ::shou
             ::turami
             ::illuskan
             ::damaran]))

(defmethod random-name [nil nil] [_]
  (random-name {:subrace (random-human-subrace)
                :sex (random-sex)}))

(defmethod random-name [nil ::male] [_]
  (random-name {:subrace (random-human-subrace)
                :sex ::male}))

(defmethod random-name [nil ::female] [_]
  (random-name {:subrace (random-human-subrace)
                :sex ::female}))

(defmethod random-name :default [_]
  (random-name {}))

(ns orcpub.dnd.e5.armor)

(def armor
  [{:name "Shield"
    :type :shield
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

(defn non-shields [armor]
  (filter #(not= :shield (:type %)) armor))

(defn shields [armor]
  (filter #(= :shield (:type %)) armor))

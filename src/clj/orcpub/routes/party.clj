(ns orcpub.routes.party
  (:require [clojure.spec :as spec]
            [datomic.api :as d]
            [orcpub.dnd.e5.party :as party]
            [orcpub.entity.strict :as se]))

(defn create-party [{:keys [db conn identity] party :transit-params}]
  (prn "PARTY" party)
  (if (spec/valid? ::party/party party)
    (let [username (:user identity)
          result @(d/transact conn [(assoc party ::party/owner username)])
          new-id (-> result :tempids first val)]
      {:status 200 :body (d/pull (d/db conn) '[*] new-id)})
    {:status 400}))

(def pull-party [:db/id ::party/name {::party/character-ids [:db/id ::se/owner ::se/summary]}])

(defn parties [{:keys [db identity]}]
  (let [username (:user identity)
        result (d/q [:find `(~'pull ~'?e ~pull-party)
                      :in '$ '?username
                      :where ['?e ::party/owner '?username]]
                    db
                    username)
        mapped (map
                (fn [[party]]
                  (update
                   party
                   ::party/character-ids
                   (fn [chars]
                     (map
                      (fn [{:keys [:db/id ::se/owner ::se/summary]}]
                        (assoc summary
                               :db/id id
                               ::se/owner owner))
                      chars))))
                result)]
    (prn "MAPPED" mapped)
    {:status 200
     :body mapped}))

(defn update-party-name [{:keys [db conn identity]
                          party-name :transit-params
                          {:keys [id]} :path-params}]
  @(d/transact conn [{:db/id id
                      ::party/name party-name}])
  {:status 200
   :body (d/pull (d/db conn) pull-party id)})

(defn add-character [{:keys [db conn identity]
                      character-id :transit-params
                      {:keys [id]} :path-params}]
  @(d/transact conn [{:db/id id
                      ::party/character-ids character-id}])
  {:status 200 :body (d/pull db '[*] id)})

(defn remove-character [{:keys [db conn identity]
                         {:keys [id character-id]} :path-params}]
  @(d/transact conn [[:db/retract id ::party/character-ids (Long/parseLong character-id)]])
  {:status 200 :body (d/pull db '[*] id)})

(defn delete-party [{:keys [db conn identity]
                     {:keys [id]} :path-params}]
  @(d/transact conn [[:db/retractEntity id]])
  {:status 200})

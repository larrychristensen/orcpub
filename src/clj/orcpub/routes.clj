(ns orcpub.routes
  (:require [io.pedestal.http :as http]          
            [io.pedestal.http.route :as route]
            [io.pedestal.test :as test]
            [io.pedestal.http.ring-middlewares :as ring]
            [ring.middleware.cookies :only [wrap-cookies]]
            [ring.util.response :as ring-resp]
            [io.pedestal.http.body-params :as body-params]
            [io.pedestal.interceptor.error :as error-int]
            [io.pedestal.interceptor.chain :refer [terminate]]
            #_[com.stuartsierra.component :as component]
            [buddy.auth.protocols :as proto]
            [buddy.auth.backends :as backends]
            [buddy.sign.jwt :as jwt]
            [buddy.hashers :as hashers]
            [buddy.auth.middleware :refer [authentication-request]]
            [clojure.java.io :as io]
            [clj-time.core :as t :refer [hours from-now ago]]
            [clj-time.coerce :as tc :refer [from-date]]
            [clojure.string :as s]
            [clojure.spec :as spec]
            [orcpub.dnd.e5.skills :as skill5e]
            [orcpub.dnd.e5.character :as char5e]
            [datomic.api :as d]
            [bidi.bidi :as bidi]
            [orcpub.route-map :as route-map]
            [orcpub.errors :as errors]
            [orcpub.privacy :as privacy]
            [orcpub.email :as email]
            [orcpub.registration :as registration]
            [orcpub.entity.strict :as se]
            [hiccup.page :as page]
            [environ.core :as environ])
  (:import (org.apache.pdfbox.pdmodel.interactive.form PDCheckBox PDComboBox PDListBox PDRadioButton PDTextField)
           (org.apache.pdfbox.pdmodel PDDocument PDPageContentStream)
           (org.apache.pdfbox.pdmodel.graphics.image PDImageXObject)
           (java.io ByteArrayOutputStream ByteArrayInputStream)
           (org.apache.pdfbox.pdmodel.graphics.image JPEGFactory LosslessFactory)
           (org.eclipse.jetty.server.handler.gzip GzipHandler)
           (javax.imageio ImageIO)
           (java.net URL))
  (:gen-class))

(def backend (backends/jws {:secret (environ/env :signature)}))

(defn first-user-by [db query value]
  (let [result (d/q query
                    db
                    value)
        user-id (ffirst result)]
    (d/pull db '[*] user-id)))

(defn lookup-user [db username password]
  (first-user-by db
                 '{:find [?e]
                   :in [$ [?username ?password]]
                   :where [(or [?e :orcpub.user/username ?username]
                               [?e :orcpub.user/email ?username])
                           [?e :orcpub.user/password ?enc]
                           [(buddy.hashers/check ?password ?enc)]]}
                 [username password]))

(def check-auth
  {:name :check-auth
   :enter (fn [context]
            (let [request (:request context)
                  updated-request (authentication-request request backend)]
              (if (:identity updated-request)
                (assoc context :request updated-request)
                (-> context
                    terminate
                    (assoc :response {:status 401 :body {:message "Unauthorized"}})))))})

(defn redirect [route-key]
  (ring-resp/redirect (route-map/path-for route-key)))

(defn verification-expired? [verification-sent]
  (t/before? (from-date verification-sent) (-> 24 hours ago)))

(defn login-error [error-key & [data]]
  {:status 401 :body (merge
                      data
                      {:error error-key})})

(defn create-token [username exp]
  (jwt/sign {:user username
             :exp exp}
            (environ/env :signature)))

(defn login-response
  [{:keys [json-params db] :as request}]
  (let [{:keys [username password]} json-params
        {:keys [:orcpub.user/verified?
                :orcpub.user/verification-sent
                :orcpub.user/email
                :db/id] :as user} (lookup-user db username password)
        unverified? (not verified?)
        expired? (and verification-sent (verification-expired? verification-sent))]
    (cond
      (nil? id) (login-error errors/bad-credentials)
      (and unverified? expired?) (login-error errors/unverified-expired)
      unverified? (login-error errors/unverified {:email email})
      :else (let [token (create-token username (-> 3 hours from-now))]
              {:status 200 :body {:user-data {:username (:orcpub.user/username user)
                                              :email (:orcpub.user/email user)}
                                  :token token}}))))

(defn login [{:keys [json-params db] :as request}]
  (try
    (login-response request)
    (catch Exception e (prn "E" e))))

(def username-query
  '[:find ?e
    :in $ ?username
    :where [?e :orcpub.user/username ?username]])

(def email-query
  '[:find ?e
    :in $ ?email
    :where [?e :orcpub.user/email ?email]])

(defn base-url [{:keys [scheme headers]}]
  (str (name scheme) "://" (headers "host")))

(defn send-verification-email [request params verification-key]
  (email/send-verification-email
   (base-url request)
   params
   verification-key))

(defn do-verification [request params conn & [tx-data]]
  (let [verification-key (str (java.util.UUID/randomUUID))
        now (java.util.Date.)]
    (do @(d/transact
          conn
          [(merge
            tx-data
            {:orcpub.user/verified? false
             :orcpub.user/verification-key verification-key
             :orcpub.user/verification-sent now})])
        (send-verification-email request params verification-key)
        {:status 200})))

(defn register [{:keys [json-params db conn] :as request}]
  (let [{:keys [username email password first-and-last-name send-updates?]} json-params
        validation (registration/validate-registration
                    json-params
                    (seq (d/q email-query db email))
                    (seq (d/q username-query db username)))]
    (if (seq validation)
      {:status 400
       :body validation}
      (do-verification
       request
       json-params
       conn
       {:orcpub.user/email email
        :orcpub.user/username username
        :orcpub.user/password (hashers/encrypt password)
        :orcpub.user/first-and-last-name first-and-last-name
        :orcpub.user/send-updates? send-updates?
        :orcpub.user/created (java.util.Date.)}))))

(def user-for-verification-key-query
  '[:find ?e
    :in $ ?key
    :where [?e :orcpub.user/verification-key ?key]])

(def user-for-email-query
  '[:find ?e
    :in $ ?email
    :where [?e :orcpub.user/email ?email]])

(defn user-for-verification-key [db key]
  (first-user-by db user-for-verification-key-query key))

(defn user-for-email [db email]
  (first-user-by db user-for-email-query email))

(defn verify [{:keys [query-params db conn] :as request}]
  (let [key (:key query-params)
        {:keys [:orcpub.user/verification-sent
                :orcpub.user/verified?
                :db/id] :as user} (user-for-verification-key (d/db conn) key)]
    (if verified?
      (redirect route-map/verify-success-route)
      (if (or (nil? verification-sent)
              (verification-expired? verification-sent))
        (redirect route-map/verify-failed-route)
        (do (d/transact conn [{:db/id id
                               :orcpub.user/verified? true}])
            (redirect route-map/verify-success-route))))))

(defn re-verify [{:keys [query-params db conn] :as request}]
  (let [email (:email query-params)
        {:keys [:orcpub.user/verification-sent
                :orcpub.user/verified?
                :orcpub.user/first-and-last-name
                :db/id] :as user} (user-for-email db email)]
    (if verified?
      (redirect route-map/verify-success-route)
      (do-verification request
                       (merge query-params
                              {:first-and-last-name first-and-last-name})
                       conn
                       {:db/id id}))))

(defn do-send-password-reset [user-id first-and-last-name email conn request]
  (let [key (str (java.util.UUID/randomUUID))]
    @(d/transact
      conn
      [{:db/id user-id
        :orcpub.user/password-reset-key key
        :orcpub.user/password-reset-sent (java.util.Date.)}])
    (email/send-reset-email
     (base-url request)
     {:first-and-last-name first-and-last-name
      :email email}
     key)
    {:status 200}))

(defn password-reset-expired? [password-reset-sent]
  (and password-reset-sent (t/before? (tc/from-date password-reset-sent) (-> 24 hours ago))))

(defn password-already-reset? [password-reset password-reset-sent]
  (and password-reset (t/before? (tc/from-date password-reset-sent) (tc/from-date password-reset))))

(defn send-password-reset [{:keys [query-params db conn scheme headers] :as request}]
  (let [email (:email query-params)
        {:keys [:orcpub.user/first-and-last-name
                :orcpub.user/password-reset-sent
                :orcpub.user/password-reset
                :db/id] :as user} (user-for-email db email)
        expired? (password-reset-expired? password-reset-sent)
        already-reset? (password-already-reset? password-reset password-reset-sent)]
    (if (or (not password-reset-sent)
            expired?
            already-reset?)
          (do-send-password-reset id first-and-last-name email conn request)
          (redirect route-map/password-reset-sent-route))))

(defn do-password-reset [conn user-id password]
  @(d/transact
    conn
    [{:db/id user-id
      :orcpub.user/password (hashers/encrypt password)
      :orcpub.user/password-reset (java.util.Date.)}])
  {:status 200})

(defn reset-password [{:keys [json-params db conn cookies identity] :as request}]
  (let [{:keys [password verify-password]} json-params
        username (:user identity)
        {:keys [:db/id] :as user} (first-user-by db username-query username)]
    (if (= password verify-password)
      (do-password-reset conn id password)
      {:status 400 :message "Passwords do not match"})))

(def font-sizes
  (merge
   (zipmap (map :key skill5e/skills) (repeat 8))
   (zipmap (map (fn [k] (keyword (str (name k) "-save"))) char5e/ability-keys) (repeat 8))
   {:features-and-traits 8
    :features-and-traits-2 8
    :attacks-and-spellcasting 8
    :backstory 8
    :other-profs 8
    :equipment 8
    :weapon-name-1 8
    :weapon-name-2 8
    :weapon-name-3 8}))

(defn write-fields! [doc fields flatten]
  (let [catalog (.getDocumentCatalog doc)
        form (.getAcroForm catalog)]
    (.setNeedAppearances form true)
    (doseq [[k v] fields]
      (try
        (let [field (.getField form (name k))]
          (when field
            (if (and (font-sizes k) flatten)
              (.setDefaultAppearance field (str "/Helv " (font-sizes k) " Tf 0 0 0 rg")))
            (.setValue
             field
             (cond 
               (instance? PDCheckBox field) (if v "Yes" "Off")
               (instance? PDTextField field) (str v)
               :else nil))))
        (catch Exception e (prn "failed writing field: " k v (clojure.stacktrace/print-stack-trace e)))))
    (when flatten
      (.setNeedAppearances form false)
      (.flatten form))))

(defn content-stream [doc page]
  (PDPageContentStream. doc page true false true))

(defn in-to-sz [inches]
  (float (* 72 inches)))

(defn in-to-coord-x [inches]
  (in-to-sz inches))

(defn in-to-coord-y [inches]
  (in-to-sz (- 11 inches)))

(defn scale [[r-h r-w] [i-h i-w]]
  (let [height-to-width (/ i-h i-w)
        rect-height-to-width (/ r-h r-w)
        height-ratio (/ r-h i-h)]
    (if (> height-to-width rect-height-to-width)
      [r-h (* r-h (/ i-w i-h))]
      [(* r-w (/ i-h i-w)) r-w])))

(defn draw-imagex [c-stream img x y width height]
  (let [[scaled-height scaled-width] (scale [height width] [(.getHeight img) (.getWidth img)])]
    (.drawImage
     c-stream
     img
     (in-to-coord-x (+ x (if (< scaled-width width)
                           (/ (- width scaled-width) 2)
                           0)))
     (in-to-coord-y (+ height y (if (< scaled-height height)
                                  (/ (- scaled-height height) 2)
                                  0)))
     (in-to-sz scaled-width)
     (in-to-sz scaled-height))))

(defn draw-non-jpg [doc page url x y width height]
  (with-open [c-stream (content-stream doc page)]
    (let [img (LosslessFactory/createFromImage doc (ImageIO/read (URL. url)))]
      (draw-imagex c-stream img x y width height))))

(defn draw-jpg [doc page url x y width height]
  (with-open [c-stream (content-stream doc page)
              image-stream (.openStream (URL. url))]
    (let [img (JPEGFactory/createFromStream doc image-stream)]
      (draw-imagex c-stream img x y width height))))

(defn draw-image! [doc page url x y width height]
  (let [lower-case-url (s/lower-case url)
        jpg? (or (s/ends-with? lower-case-url "jpg")
                 (s/ends-with? lower-case-url "jpeg"))
        draw-fn (if jpg? draw-jpg draw-non-jpg)]
    (try
      (draw-fn doc page url x y width height)
      (catch Exception e (prn "failed loading image" (clojure.stacktrace/print-stack-trace e))))))

(defn get-page [doc index]
  (.getPage doc index))

(defn character-pdf-2 [req]
  (let [fields (-> req :form-params :body clojure.edn/read-string)
        {:keys [image-url image-url-failed faction-image-url faction-image-url-failed]} fields
        input (.openStream (io/resource (cond
                                          (find fields :spellcasting-class-6) "fillable-char-sheet-6-spells.pdf"
                                          (find fields :spellcasting-class-5) "fillable-char-sheet-5-spells.pdf"
                                          (find fields :spellcasting-class-4) "fillable-char-sheet-4-spells.pdf"
                                          (find fields :spellcasting-class-3) "fillable-char-sheet-3-spells.pdf"
                                          (find fields :spellcasting-class-2) "fillable-char-sheet-2-spells.pdf"
                                          (find fields :spellcasting-class-1) "fillable-char-sheet-1-spells.pdf"
                                          :else "fillable-char-sheet-0-spells.pdf")))
        output (ByteArrayOutputStream.)
        user-agent (get-in req [:headers "user-agent"])
        chrome? (re-matches #".*Chrome.*" user-agent)]
    (with-open [doc (PDDocument/load input)]
      (write-fields! doc fields (not chrome?))
      (if (and image-url
               (re-matches #"^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]" image-url)
               (not image-url-failed))
        (draw-image! doc (get-page doc 1) image-url 0.45 1.75 2.35 3.15))
      (if (and faction-image-url
               (re-matches #"^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]" faction-image-url)
               (not faction-image-url-failed))
        (draw-image! doc (get-page doc 1) faction-image-url 5.88 2.4 1.905 1.52))
      (.save doc output))
    (let [a (.toByteArray output)]
      {:status 200 :body (ByteArrayInputStream. a)})))

(defn html-response
  [html & [response]]
  (let [merged (merge
                response
                {:status 200 :body html :headers {"Content-Type" "text/html"}})]
    merged))

(defn index [req & [response]]
  (html-response
   (slurp (io/resource "public/index.html"))
   response))

(defn empty-index [req & [response]]
  (html-response
   (slurp (io/resource "public/blank.html"))
   response))

(defn verification-expired [req]
  (index req))

(defn verification-successful [req]
  (index req))

(defn verify-sent [req]
  (index req))

(defn registration-page [req]
  (index req))

(defn login-page [req]
  (index req))

(defn character-list-page [req]
  (index req))

(def user-by-password-reset-key-query
  '[:find ?e
    :in $ ?key
    :where [?e :orcpub.user/password-reset-key ?key]])

(defn reset-password-page [{:keys [query-params db conn] :as req}]
  (let [key (:key query-params)
        {:keys [:db/id
                :orcpub.user/username
                :orcpub.user/password-reset-key
                :orcpub.user/password-reset-sent
                :orcpub.user/password-reset] :as user} (first-user-by db user-by-password-reset-key-query key)
        expired? (password-reset-expired? password-reset-sent) 
        already-reset? (password-already-reset? password-reset password-reset-sent)]
    (cond
      expired? (redirect route-map/password-reset-expired-route)
      already-reset? (redirect route-map/password-reset-used-route)
      :else (let [token (create-token username (-> 1 hours from-now))]
              (index req {:cookies {"token" token}})))))

(defn password-reset-sent-page [req]
  (index req))

(defn password-reset-expired-page [req]
  (index req))

(defn password-reset-used-page [req]
  (index req))

(defn send-password-reset-page [req]
  (index req))

(defn character-builder-page [req]
  (index req))

(defn check-field [query value db]
  {:status 200
   :body (-> (d/q query db value)
             seq
             boolean
             str)})

(defn check-username [{:keys [db query-params]}]
  (check-field username-query (:username query-params) db))

(defn check-email [{:keys [db query-params]}]
  (check-field email-query (:email query-params) db))

(defn save-character [{:keys [db transit-params body conn identity] :as request}]
  (if-let [data (spec/explain-data ::se/entity transit-params)]
    {:status 400 :body data}
    (let [current-id (:db/id transit-params)
          result @(d/transact conn [(if current-id
                                      transit-params
                                      (assoc transit-params
                                             :db/id "tempid"
                                             :orcpub.entity.strict/owner (:user identity)))])]
      {:status 200 :body (if current-id
                           transit-params
                           (assoc transit-params :db/id (-> result :tempids (get "tempid"))))})))

(defn character-list [{:keys [db transit-params body conn identity] :as request}]
  (let [username (:user identity)
        ids (d/q '[:find ?e
                   :in $ ?user
                   :where
                   [?e :orcpub.entity.strict/owner ?user]
                   [?e :orcpub.entity.strict/selections]]
                 db
                 username)
        characters (d/pull-many db '[*] (map first ids))]
    {:status 200 :body characters}))

(defn delete-character [{:keys [db conn identity] {:keys [id]} :path-params}]
  (let [parsed-id (Long/parseLong id)
        username (:user identity)
        {:keys [:orcpub.entity.strict/owner]} (d/pull db '[:orcpub.entity.strict/owner] parsed-id)]
    (if owner
      (do
        @(d/transact conn [[:db/retractEntity parsed-id]])
        {:status 200})
      {:status 401})))

(def header-style
  {:style "color:#2c3445"})

(defn terms-page [body-fn]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (body-fn)})

(defn privacy-policy-page [req]
  (terms-page privacy/privacy-policy))

(defn terms-of-use-page [req]
  (terms-page privacy/terms-of-use))

(defn community-guidelines-page [_]
  (terms-page privacy/community-guidelines))

(defn cookie-policy-page [_]
  (terms-page privacy/cookie-policy))

(defn health-check [_]
  {:status 200 :body "OK"})

(def routes
  (route/expand-routes
   [[["/" {:get `index}]
     [(route-map/path-for route-map/register-route) ^:interceptors [(body-params/body-params)]
      {:post `register}]
     [(route-map/path-for route-map/dnd-e5-char-list-route) ^:interceptors [(body-params/body-params) check-auth]
      {:post `save-character
       :get `character-list}]
     [(route-map/path-for route-map/delete-dnd-e5-char-route :id ":id") ^:interceptors [(body-params/body-params) check-auth]
      {:delete `delete-character}]
     [(route-map/path-for route-map/dnd-e5-char-list-page-route) ^:interceptors [(body-params/body-params)]
      {:get `character-list-page}]
     [(route-map/path-for route-map/dnd-e5-char-builder-route) ^:interceptors [(body-params/body-params)]
      {:get `character-builder-page}]
     [(route-map/path-for route-map/login-route) ^:interceptors [(body-params/body-params)]
      {:post `login}]
     [(route-map/path-for route-map/character-pdf-route) ^:interceptors [(body-params/body-params)]
      {:post `character-pdf-2}]
     [(route-map/path-for route-map/verify-route)
      {:get `verify}]
     [(route-map/path-for route-map/verify-sent-route)
      {:get `verify-sent}]
     [(route-map/path-for route-map/reset-password-page-route) ^:interceptors [ring/cookies]
      {:get `reset-password-page}]
     [(route-map/path-for route-map/send-password-reset-page-route)
      {:get `send-password-reset-page}]
     [(route-map/path-for route-map/password-reset-sent-route)
      {:get `password-reset-sent-page}]
     [(route-map/path-for route-map/password-reset-expired-route)
      {:get `password-reset-expired-page}]
     [(route-map/path-for route-map/password-reset-used-route)
      {:get `password-reset-used-page}]
     [(route-map/path-for route-map/re-verify-route) ^:interceptors [(body-params/body-params)]
      {:get `re-verify}]
     [(route-map/path-for route-map/reset-password-route) ^:interceptors [(body-params/body-params) ring/cookies check-auth]
      {:post `reset-password}]
     [(route-map/path-for route-map/send-password-reset-route) ^:interceptors [(body-params/body-params)]
      {:get `send-password-reset}]
     [(route-map/path-for route-map/verify-failed-route)
      {:get `verification-expired}]
     [(route-map/path-for route-map/verify-success-route)
      {:get `verification-successful}]
     [(route-map/path-for route-map/register-page-route)
      {:get `registration-page}]
     [(route-map/path-for route-map/login-page-route)
      {:get `login-page}]
     [(route-map/path-for route-map/privacy-policy-route)
      {:get `privacy-policy-page}]
     [(route-map/path-for route-map/terms-of-use-route)
      {:get `terms-of-use-page}]
     [(route-map/path-for route-map/community-guidelines-route)
      {:get `community-guidelines-page}]
     [(route-map/path-for route-map/cookies-policy-route)
      {:get `cookie-policy-page}]
     [(route-map/path-for route-map/check-email-route)
      {:get `check-email}]
     [(route-map/path-for route-map/check-username-route)
      {:get `check-username}]
     ["/health"
      {:get `health-check}]]]))

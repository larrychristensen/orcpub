(ns orcpub.server-2
  (:require [io.pedestal.http :as http]          
            [io.pedestal.http.route :as route]
            [io.pedestal.test :as test]
            [io.pedestal.interceptor.error :as error-int]
            [clojure.java.io :as io])
  (:import [org.apache.pdfbox.pdmodel.interactive.form PDCheckBox PDComboBox PDListBox PDRadioButton PDTextField]
           [org.apache.pdfbox.pdmodel PDDocument]
           [java.io ByteArrayOutputStream ByteArrayInputStream]))

(defn response [status body & {:as headers}]
  {:status status :body body :headers headers})

(def ok       (partial response 200))
(def created  (partial response 201))
(def accepted (partial response 202))


(def echo
  {:name :echo
   :enter
   (fn [context]
     (let [request (:request context)
           response (ok context)]
       (assoc context :response response)))})

(defonce database (atom {}))

(def db-interceptor
  {:name :database-interceptor
   :enter
   (fn [context]
     (update context :request assoc :database @database))                               
   :leave
   (fn [context]
     (if-let [[op & args] (:tx-data context)]                                           
       (do
         (apply swap! database op args)                                                 
         (assoc-in context [:request :database] @database))                             
       context))})

(defn make-list [nm]
  {:name  nm
   :items {}})

(defn make-list-item [nm]
  {:name  nm
   :done? false})

(def list-create
  {:name :list-create
   :enter
   (fn [context]
     (let [nm       (get-in context [:request :query-params :name] "Unnamed List")
           new-list (make-list nm)
           db-id    (str (gensym "l"))
           url      (route/url-for :list-view :params {:list-id db-id})]                
       (assoc context
              :response (created new-list "Location" url)
              :tx-data [assoc db-id new-list])))})

(defn find-list-by-id [dbval db-id]
  (get dbval db-id))                                                                    

(def list-view
  {:name :list-view
   :enter
   (fn [context]
     (if-let [db-id (get-in context [:request :params :list-id])]                       
       (if-let [the-list (find-list-by-id (get-in context [:request :database]) db-id)] 
         (assoc context :result the-list)                                               
         context)
       context))})

(def entity-render                                                                      
  {:name :entity-render
   :leave
   (fn [context]
     (if-let [item (:result context)]
       (assoc context :response (ok item))
       context))})

(defn find-list-item-by-ids [dbval list-id item-id]
  (get-in dbval [list-id :items item-id] nil))

(def list-item-view                                                                     
  {:name :list-item-view
   :leave
   (fn [context]
     (if-let [list-id (get-in context [:request :path-params :list-id])]
       (if-let [item-id (get-in context [:request :path-params :item-id])]
         (if-let [item (find-list-item-by-ids (get-in context [:request :database]) list-id item-id)]
           (assoc context :result item)                                                 
           context)
         context)
       context))})

(defn list-item-add
  [dbval list-id item-id new-item]
  (if (contains? dbval list-id)
    (assoc-in dbval [list-id :items item-id] new-item)
    dbval))

(def list-item-create                                                                   
  {:name :list-item-create
   :enter
   (fn [context]
     (if-let [list-id (get-in context [:request :path-params :list-id])]
       (let [nm       (get-in context [:request :query-params :name] "Unnamed Item")
             new-item (make-list-item nm)
             item-id  (str (gensym "i"))]
         (-> context
             (assoc :tx-data  [list-item-add list-id item-id new-item])
             (assoc-in [:request :path-params :item-id] item-id)))                      
       context))})

(defn write-fields! [doc fields]
  (prn "FIELDS" fields)
  (let [catalog (.getDocumentCatalog doc)
        form (.getAcroForm catalog)]
    (doseq [[k v] fields]
      (let [field (.getField form (name k))]
        (prn "FIELD" field k v)
        (do
          (if field
            (.setValue
             field
             (cond 
               (instance? PDCheckBox field) (if v "Yes" "Off")
               (instance? PDTextField field) (str v)
               :else nil)))
          (prn "FIELD AFTER" field k v))))))

(def character-pdf
  {:name :character-pdf
   :enter
   (fn [context]
     (prn "PDF!!!!!!!!!!!!!!!!!!!!")
     (try
       (let [body-map (io.pedestal.http.route/parse-query-string (slurp (get-in context [:request :body])))
             _ (prn "BODY STTR" body-map)
             fields (clojure.edn/read-string (:body body-map))
             _ (prn "REQUEST" (:request context))
             input (.openStream (io/resource "fillable-char-sheet.pdf"))
             output (ByteArrayOutputStream.)]
         (with-open [doc (PDDocument/load input)]
           (write-fields! doc fields)
           (.save doc output))
         (let [a (.toByteArray output)]
           (prn "A" a)
           (assoc context :response {:status 200 :body (ByteArrayInputStream. a)})))
       (catch Throwable e (prn "EXCEPTION!!!!!!!!!!" e))))})

(def service-error-handler
  (error-int/error-dispatch [ctx ex]
    
    [{:exception-type :java.lang.ArithmeticException :interceptor ::another-bad-one}]
    (assoc ctx :response {:status 400 :body "Another bad one"})

    
    [{:exception-type :java.lang.ArithmeticException}]
    (assoc ctx :response {:status 400 :body "A bad one"})

    :else
    (assoc ctx :io.pedestal.interceptor.chain/error ex)))

(def routes
  (route/expand-routes
   #{["/character.pdf"        :post   [service-error-handler character-pdf]]
     ["/todo"                 :post   [db-interceptor list-create]]
     ["/todo"                 :get    echo :route-name :list-query-form]
     ["/todo/:list-id"        :get    [entity-render db-interceptor list-view]]
     ["/todo/:list-id"        :post   [entity-render list-item-view db-interceptor list-item-create]]
     ["/todo/:list-id/:item"  :get    [entity-render list-item-view]]
     ["/todo/:list-id/:item"  :put    echo :route-name :list-item-update]
     ["/todo/:list-id/:item"  :delete echo :route-name :list-item-delete]}))

(def service
  {::http/routes #(deref #'routes)
   ::http/type :jetty
   ::http/port 8890
   ::http/resource-path "/public"})

(defn start []
  (http/start (http/create-server service)))

(defonce server (atom nil))

(defn start-dev
  "The entry-point for 'lein run-dev'"
  [& args]
  (println "\nCreating your [DEV] server...")
  (reset!
   server
   (-> service ;; start with production configuration
       (merge {:env :dev
               ;; do not block thread that starts web server
               ::http/join? false
               ;; Routes can be a function that resolve routes,
               ;;  we can use this to set the routes to be reloadable
               ::http/routes #(deref #'routes)
               ;; all origins are allowed in dev mode
               ::http/allowed-origins {:creds true :allowed-origins (constantly true)}})
       ;; Wire up interceptor chains
       http/default-interceptors
       http/dev-interceptors
       http/create-server
       http/start)))

(defn stop-dev []
  (http/stop @server))

(defn restart []
  (stop-dev)
  (start-dev))

(defn test-request [verb url & [body]]
  (io.pedestal.test/response-for (::http/service-fn @server) verb url :body body))

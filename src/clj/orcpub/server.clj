(ns orcpub.server
  (:require [compojure.core :refer [defroutes GET PUT POST DELETE ANY]]
            [compojure.handler :refer [site]]
            [compojure.route :as route]
            [clojure.java.io :as io]
            [ring.adapter.jetty :as jetty]
            [ring.util.response :as resp]
            [environ.core :refer [env]])
  (:import [org.apache.pdfbox.pdmodel.interactive.form PDCheckBox PDComboBox PDListBox PDRadioButton PDTextField]
           [org.apache.pdfbox.pdmodel PDDocument]
           [java.io ByteArrayOutputStream ByteArrayInputStream]))

(defn splash []
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body "Hello from Heroku"})

(defn write-fields! [doc fields]
  (let [catalog (.getDocumentCatalog doc)
        form (.getAcroForm catalog)]
    (doseq [[k v] fields]
      (let [field (.getField form (name k))]
        (if field
          (.setValue
           field
           (case (type field)
             PDCheckBox (if v "Yes" "No")
             PDTextField v
             nil)))))))

(defroutes app
  (GET "/" [] (resp/file-response "index.html"))
  (POST "/character/download.pdf" [body]
        (let [char (clojure.edn/read-string body)
              input (.openStream (io/resource "char.pdf"))
              output (ByteArrayOutputStream.)]
          (with-open [doc (PDDocument/load input)]
            (write-fields! doc fields)
            (.save doc output))
          (let [a (.toByteArray output)]
            (ByteArrayInputStream. a))))
  (route/resources "/"))

(defn -main [& [port]]
  (let [port (Integer. (or port (env :port) 5000))]
    (jetty/run-jetty (site #'app) {:port port :join? false})))

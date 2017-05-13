(ns orcpub.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [orcpub.character-builder :as ch]
            [orcpub.dnd.e5.subs]
            [orcpub.dnd.e5.events]
            [orcpub.dnd.e5.views :as views]
            [orcpub.route-map :as routes]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            [clojure.string :as s]
            [re-frame.core :refer [dispatch dispatch-sync subscribe]]
            [reagent.core :as r]))

(enable-console-print!)

(dispatch-sync [:initialize-db])

(def register-url (if (s/starts-with? js/window.location.href "http://localhost")
                    "http://localhost:8890/register"
                    "/register"))

(def pages
  {routes/dnd-e5-char-builder-route ch/character-builder
   routes/register-page-route views/register-form
   routes/verify-failed-route views/verify-failed
   routes/verify-success-route views/verify-success
   routes/verify-sent-route views/verify-sent})

(defn main-view []
  (let [route @(subscribe [:route])
        view (pages route)]
    (prn "VIEW" view route)
    [view]))

(r/render (if (let [doc-style js/document.documentElement.style]
                (and js/window.localStorage
                     (or (aget doc-style "flexWrap")
                         (aget doc-style "WebkitFlexWrap")
                         (aget doc-style "msFlexWrap"))))
            [main-view]
            [:div
             [ch/app-header]
             [:div.f-s-24.white.sans
              {:style {:padding "200px"}}
              "Sorry, we are unable to support your browser since it does not support important HTML5 features. Please try a modern browser such as " [:a {:href "https://www.google.com/chrome/browser/desktop/index.html"} "Google Chrome"] " or " [:a {:href "https://www.mozilla.org/en-US/firefox/products/?v=a"} "Mozilla Firefox"]]])
          (js/document.getElementById "app"))

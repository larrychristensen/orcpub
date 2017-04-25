(ns orcpub.core
  (:require [orcpub.character-builder :as ch]
            [reagent.core :as r]))

(enable-console-print!)

(r/render (if js/window.localStorage
            [ch/character-builder]
            [:div
             [ch/app-header]
             [:div.f-s-24.white.sans
              {:style {:padding "200px"}}
              "Sorry, we are unable to support your browser since it does not support important HTML5 features. Please try a modern browser such as " [:a {:href "https://www.google.com/chrome/browser/desktop/index.html"} "Google Chrome"] " or " [:a {:href "https://www.mozilla.org/en-US/firefox/products/?v=a"} "Mozilla Firefox"]]])
          (js/document.getElementById "app"))

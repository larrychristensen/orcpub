(ns orcpub.core
  (:require [orcpub.character-builder :as ch]
            [reagent.core :as r]))

(enable-console-print!)

(r/render [ch/character-builder]
          (js/document.getElementById "app"))

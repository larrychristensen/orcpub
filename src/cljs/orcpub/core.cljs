(ns orcpub.core
  (:require [orcpub.character-builder :as ch]
            [orcpub.dnd.e5.character-sheet :as sheet5e]
            [reagent.core :as r]))

(enable-console-print!)

(r/render [ch/character-builder]
          (js/document.getElementById "app"))

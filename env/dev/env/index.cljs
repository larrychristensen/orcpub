(ns env.index
  (:require [env.dev :as dev]))

;; undo main.js goog preamble hack
(set! js/window.goog js/undefined)

(-> (js/require "figwheel-bridge")
    (.withModules #js {"react" (js/require "react"), "react-native" (js/require "react-native"), "expo" (js/require "expo"), "./assets/icons/app.png" (js/require "../../assets/icons/app.png"), "./assets/icons/loading.png" (js/require "../../assets/icons/loading.png"), "./assets/images/cljs.png" (js/require "../../assets/images/cljs.png"), "./assets/images/orcpub-logo.svg" (js/require "../../assets/images/orcpub-logo.svg"), "./assets/images/orcpub-logo.png" (js/require "../../assets/images/orcpub-logo.png")}
)
    (.start "main"))

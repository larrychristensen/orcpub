(ns env.index
  (:require [env.dev :as dev]))

;; undo main.js goog preamble hack
(set! js/window.goog js/undefined)

(-> (js/require "figwheel-bridge")
    (.withModules #js {"react-native" (js/require "react-native"), "./assets/images/orcpub-logo.png" (js/require "../../assets/images/orcpub-logo.png"), "react" (js/require "react"), "expo" (js/require "expo"), "./assets/icons/app.png" (js/require "../../assets/icons/app.png"), "./assets/icons/loading.png" (js/require "../../assets/icons/loading.png"), "./assets/images/cljs.png" (js/require "../../assets/images/cljs.png"), "./assets/images/orcpub-logo.svg" (js/require "../../assets/images/orcpub-logo.svg")}
)
    (.start "main"))

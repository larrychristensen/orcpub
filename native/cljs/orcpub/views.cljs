(ns orcpub.views
  (:require [reagent.core :as r :refer [atom]]))

(def ReactNative (js/require "react-native"))
#_(def FontAwesome (js/require "react-native-fontawesome"))
#_(def FontAwesomeIcons (.-Icons FontAwesome))

#_(def fa (r/adapt-react-class FontAwesome))
#_(defn fa-icon [icon-name]
  [fa (aget FontAwesomeIcons icon-name)])

(def app-registry (.-AppRegistry ReactNative))
(def text (r/adapt-react-class (.-Text ReactNative)))
(def view (r/adapt-react-class (.-View ReactNative)))
(def scroll-view (r/adapt-react-class (.-ScrollView ReactNative)))
(def image (r/adapt-react-class (.-Image ReactNative)))
(def touchable-highlight (r/adapt-react-class (.-TouchableHighlight ReactNative)))
(def touchable-without-feedback (r/adapt-react-class (.-TouchableWithoutFeedback ReactNative)))
(def Alert (.-Alert ReactNative))

(def main-text-color "#727272")
(def light-text-color "#BBBBBB")

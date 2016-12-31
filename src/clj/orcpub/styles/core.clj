(ns orcpub.styles.core
  (:require [garden.def :refer [defstylesheet defstyles]]
            [garden.units :refer [px]]))

(def container-style
  {:display :flex
   :justify-content :center})

(def content-style
  {:width (px 1440)})

(def text-color
  {:color :white})

(defstyles app
  [:.app
   {:background-image "linear-gradient(182deg, #2c3445, #000000)"
    :font-family "Open Sans, sans-serif"
    :height "100%"
    :overflow :auto}]

  [:.container
   container-style]

  [:.content
   (merge
    content-style
    {:display :flex})]

  [:.app-header
   {:background-color "black"
    :background-image "url(../../image/shutterstock_425039560.jpg)"
    :background-position "0px 900px"
    :background-size "100%"
    :height (px 227)}]

  [:.app-header-bar
   {:height (px 81)
    :-webkit-backdrop-filter "blur(5px)"
    :backdrop-filter "blur(5px)"
    :background-color "rgba(0, 0, 0, 0.15)"}]

  [:.builder-option
   {:border-width (px 1)
    :border-style :solid
    :border-color "rgba(255, 255, 255, 0.5)"
    :border-radius (px 5)
    :padding (px 10)
    :margin-top (px 5)
    :font-weight :normal}]

  [:.selectable-builder-option:hover
   {:border-color "#f1a20f"
    :cursor :pointer}]

  [:.builder-selector
   {:padding (px 5)
   :font-size (px 14)
    :margin-top (px 10)}]

  [:.builder-selector-header
   {:font-size (px 18)
    :font-weight :normal}]

  [:.builder-option-dropdown
   (merge
    {:background-color :transparent
     :width "100%"
     :-webkit-appearance :menulist
     :cursor :pointer}
    text-color)

   [:&:active :&:focus
    {:outline :none}]]

  [:.builder-dropdown-item
   {:-webkit-appearance :none
    :-moz-appearance :none
    :appearance :none
    :background-color :black}]

  [:.selected-builder-option
   {:border-width (px 3)
    :border-color :white
    :font-weight :bold}]

  [:.remove-item-button
   {:color "#f0a100"
    :font-size "16px" 
    :margin-left "5px"
    :cursor :pointer}]

  [:.add-item-button
   {:margin-top "19px"
    :color "#f0a100"
    :font-weight 600
    :text-decoration :underline
    :cursor :pointer}]

  [:.list-selector-option
   {:display :flex
    :align-items :center}]

  [:.fa
   {:color "#f0a100"}]

  [:.abilities-polygon
   {:transition "points 2s"
    :-webkit-transition "points 2s"}]

  [:.form-button
   {:color :white
    :font-weight 600
    :border :none
    :border-radius "5px"
    :text-transform :uppercase
    :padding "10px 15px"
    :cursor :pointer
    :background-image "linear-gradient(to bottom, #f1a20f, #dbab50)"}])

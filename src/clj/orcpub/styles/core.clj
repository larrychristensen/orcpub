(ns orcpub.styles.core
  (:require [garden.def :refer [defstylesheet defstyles]]
            [garden.units :refer [px]]))

(def button-color "#f0a100")

(def container-style
  {:display :flex
   :justify-content :center})

(def content-style
  {:width (px 1440)})

(def text-color
  {:color :white})

(defn px-prop [kw abbr values]
  (map
   (fn [v]
     [(keyword (str "." (name abbr) "-" v))
      {kw (px v)}])
   values))

(def margin-lefts
  (px-prop
   :margin-left
   :m-l
   (concat (range 0 10) (range 10 30 5))))

(def margin-tops
  (px-prop
   :margin-top
   :m-t
   (concat (range 0 10) (range 10 30 5))))

(def props
  [[:.flex
    {:display :flex}]
   [:.f-w-bold
    {:font-weight :bold}]])

(def app
  (concat
   margin-lefts
   margin-tops
   props
   [[:.app
     {:background-image "linear-gradient(182deg, #2c3445, #000000)"
      :font-family "Open Sans, sans-serif"
      :height "100%"
      :overflow :auto
      :padding-bottom "40px"}]

    [:.container
     container-style]

    [:.content
     (merge
      content-style
      {:display :flex})]

    [:.app-header
     {:background-color :black
      :background-image "url(../../image/header-background.jpg)"
      :background-position "center"
      :background-size "cover"
      :height (px 227)}]

    [:.app-header-bar
     {:height (px 81)
      :-webkit-backdrop-filter "blur(5px)"
      :backdrop-filter "blur(5px)"
      :background-color "rgba(0, 0, 0, 0.15)"}]

    [:.app.mobile :.app.tablet
     [:.app-header
      {:height (px 81)
       :background-image :none
       :background-color "rgba(0, 0, 0, 0.3)"}]
     [:.app-header-bar
      {:backdrop-filter :none
       :-webkit-backdrop-filter :none}]
     [:.orcpub-logo
      {:width "220px"
       :margin-left "20px"}]]

    [:.builder-option
     {:border-width (px 1)
      :border-style :solid
      :border-color "rgba(255, 255, 255, 0.5)"
      :border-radius (px 5)
      :padding (px 10)
      :margin-top (px 5)
      :font-weight :normal}]

    [:.builder-tabs
     {:display :flex
      :padding "10px"
      :text-transform :uppercase
      :font-weight 600}]

    [:.builder-tab
     {:opacity 0.2
      :flex-grow 1
      :padding-bottom "13px"
      :text-align :center
      :border-bottom "5px solid rgba(255, 255, 255, 0.3)"}]

    [:.selected-builder-tab
     {:border-bottom-color "#f1a20f"
      :opacity 1}]

    [:.collapsed-list-builder-option
     {:padding "1px"}]

    [:.disabled-builder-option
     {:color "rgba(255, 255, 255, 0.5)"
      :border-color "rgba(255, 255, 255, 0.25)"
      :cursor :auto}]

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
       :cursor :pointer
       :border "1px solid white"}
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
     {:color button-color
      :font-size "16px" 
      :margin-left "5px"
      :cursor :pointer}]

    [:.add-item-button
     {:margin-top "19px"
      :color button-color
      :font-weight 600
      :text-decoration :underline
      :cursor :pointer}]

    [:.list-selector-option
     {:display :flex
      :align-items :center}]

    [:.fa
     {:color button-color}]

    [:.expand-collapse-button
     {:font-size "12px"
      :max-width "100px"
      :margin-left "10px"
      :color "#f0a100"
      :text-decoration :underline
      :cursor :pointer
      :text-align :right}]

    [:.fa-caret-square-o-down
     {:color button-color}]

    [:.expand-collapse-button:hover
     {:color button-color}]

    [:.abilities-polygon
     {:transition "points 2s"
      :-webkit-transition "points 2s"}]

    [:.display-section-qualifier-text
     {:font-size "12px"
      :margin-left "5px"}]

    [:.form-button
     {:color :white
      :font-weight 600
      :border :none
      :border-radius "5px"
      :text-transform :uppercase
      :padding "10px 15px"
      :cursor :pointer
      :background-image "linear-gradient(to bottom, #f1a20f, #dbab50)"}]

    [:.field
     {:margin-top "30px"}]

    [:.field-label
     {:font-size "14px"}]

    [:.personality-label
     {:font-size "18px"}]

    [:.input
     {:background-color :transparent
      :color :white
      :border "1px solid white"
      :border-radius "5px"
      :margin-top "5px"
      :display :block
      :padding "10px"
      :width "100%"
      :box-sizing :border-box
      :font-size "14px"}]

    [:.checkbox-parent
     {:display :flex
      :padding "11px 0"
      :align-items :center}]

    [:.checkbox
     {:width "16px"
      :height "16px"
      :box-shadow "0 1px 0 0 #f0a100"
      :background-color :white
      :cursor :pointer}

     [:.fa-check
      {:font-size "14px"
       :margin "1px"}]]

    [:.checkbox.checked.disabled
     {:background-color "rgba(255, 255, 255, 0.37)"
      :cursor :not-allowed}]

    [:.checkbox-text
     {:margin-left "5px"}]]))

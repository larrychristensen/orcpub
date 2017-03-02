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
   (concat (range -1 10) (range 10 45 5))))

(def margin-tops
  (px-prop
   :margin-top
   :m-t
   (concat (range 0 10) (range 10 30 5))))

(def font-sizes
  [[:.f-s-10
    {:font-size "10px"}]
   [:.f-s-12
    {:font-size "12px"}]
   [:.f-s-14
    {:font-size "14px"}]
   [:.f-s-16
    {:font-size "16px"}]
   [:.f-s-18
    {:font-size "18px"}]
   [:.f-s-24
    {:font-size "24px"}]
   [:.f-s-32
    {:font-size "32px !important"}]
   [:.f-s-36
    {:font-size "36px"}]])

(def props
  [[:.flex
    {:display :flex}]
   
   [:.f-w-bold
    {:font-weight :bold}]
   
   [:.flex-grow-1
    {:flex-grow 1}]
   
   [:.i
    {:font-style :italic}]
   
   [:.f-w-n
    {:font-weight :normal}]
   [:.f-w-b
    {:font-weight :bold}]
   [:.f-w-600
    {:font-weight 600}]

   [:.m-r-5
    {:margin-right "5px"}]
   [:.m-r-18
    {:margin-right "18px"}]
   [:.m-r-20
    {:margin-right "20px"}]
   [:.m-r-80
    {:margin-right "80px"}]

   [:.m-t-21
    {:margin-top "21px"}]
   
   [:.m-b-10
    {:margin-bottom "10px"}]
   [:.m-b-16
    {:margin-bottom "16px"}]
   [:.m-b-19
    {:margin-bottom "19px"}]
   [:.m-b-20
    {:margin-bottom "20px"}]

   [:.m-l-30
    {:margin-left "30px"}]

   [:.text-shadow
    {:text-shadow "1px 2px 1px black"}]
   
   [:.t-a-c
    {:text-align :center}]
   [:.justify-cont-s-b
    {:justify-content :space-between}]
   [:.justify-cont-c
    {:justify-content :center}]
   [:.align-items-c
    {:align-items :center}]

   [:.w-40-p
    {:width "40%"}]
   [:.w-50-p
    {:width "50%"}]
   [:.w-60-p
    {:width "60%"}]
   [:.w-100-p
    {:width "100%"}]
   [:.w-250
    {:width "250px"}]
   [:.w-300
    {:width "300px"}]
   [:.w-500
    {:width "500px"}]
   [:.w-1440
    {:width "1440px"}]

   [:.h-800
    {:height "800px"}]
   
   [:.posn-rel
    {:position :relative}]
   [:.white
    {:color :white}]
   [:.orange
    {:color button-color}]
   [:.uppercase
    {:text-transform :uppercase}]
   [:.bg-trans
    {:background-color :transparent}]
   [:.no-border
    {:border :none}]
   
   [:.p-1
    {:padding "1px"}]
   [:.p-10
    {:padding "10px"}]

   [:.p-l-15
    {:padding-left "15px"}]

   [:.b-rad-5
    {:border-radius "5px"}]

   [:.b-1
    {:border "1px solid"}]

   [:.b-color-gray
    {:border-color "rgba(255,255,255,0.2)"}]

   [:ul.list-style-disc
    {:list-style-type :disc}]

   [:.hidden
    {:display :none}]

   [:.pointer
    {:cursor :pointer}]])

(def app
  (concat
   margin-lefts
   margin-tops
   font-sizes
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
      :font-size "12px"
      :border :none
      :border-radius "5px"
      :text-transform :uppercase
      :padding "10px 15px"
      :cursor :pointer
      :background-image "linear-gradient(to bottom, #f1a20f, #dbab50)"}]

    [:.link-button
     {:color button-color
      :border :none
      :background-color :transparent
      :text-transform :uppercase
      :cursor :pointer
      :font-size "12px"
      :border-radius "5px"
      :padding "10px 15px"}]

    [:.link-button:hover
     {:text-decoration :underline}]

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
     {:margin-left "5px"}]

    [:#selection-stepper
     {:transition "top 2s ease-in-out"
      :width "240px"
      :position :relative
      :top 0}]

    [:.selection-stepper-inner
     {:position :absolute}]

    [:.selection-stepper-main
     {:width "200px"
      :border "1px solid white"
      :border-radius "5px"
      :padding "10px"
      :background-color "#1a1e28"
      :box-shadow "5px 5px 5px rgba(0,0,0,0.6)"}]

    [:.selection-stepper-title
     {:font-size "18px"
      :color "#f0a100"}]

    [:.selection-stepper-help
     {:font-size "14px"
      :font-weight 100}]

    [:.selection-stepper-footer
     {:justify-content :flex-end}]

    [:.option-header
     {:display :flex
      :justify-content :space-between
      :align-items :center}]]))

(ns orcpub.styles.core
  (:require [garden.def :refer [defstylesheet defstyles]]
            [garden.stylesheet :refer [at-media at-keyframes]]
            [garden.units :refer [px]]
            [orcpub.constants :as const]))

(def button-color "#f0a100")
(def red "#9a031e")

(def container-style
  {:display :flex
   :justify-content :center})

(def content-style
  {:max-width (px 1440)
   :width "100%"})

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
   (concat (range -1 10) (range 10 55 5))))

(def margin-tops
  (px-prop
   :margin-top
   :m-t
   (concat (range 0 10) (range 10 30 5))))

(defn handle-browsers [property value]
  {(keyword (str "-webkit-" (name property))) value
   (keyword (str "-moz-" (name property))) value
   property value})

(def font-sizes
  [[:.f-s-10
    {:font-size "10px"}]
   [:.f-s-11
    {:font-size "11px"}]
   [:.f-s-12
    {:font-size "12px !important"}]
   [:.f-s-14
    {:font-size "14px !important"}]
   [:.f-s-16
    {:font-size "16px !important"}]
   [:.f-s-18
    {:font-size "18px !important"}]
   [:.f-s-20
    {:font-size "20px !important"}]
   [:.f-s-24
    {:font-size "24px !important"}]
   [:.f-s-28
    {:font-size "28px"}]
   [:.f-s-32
    {:font-size "32px !important"}]
   [:.f-s-36
    {:font-size "36px"}]])

(def props
  [[:.flex
    {:display :flex}]
   [:.inline-block
    {:display :inline-block}]

   [:.list-style-disc
    {:list-style-type :disc
     :list-style-position :inside}]
   
   [:.f-w-bold
    {:font-weight :bold}]
   
   [:.flex-grow-1
    {:flex-grow 1}]

   [:.flex-basis-50-p
    {:flex-basis "50%"}]
   
   [:.i
    {:font-style :italic}]
   
   [:.f-w-n
    {:font-weight :normal}]
   [:.f-w-b
    {:font-weight :bold}]
   [:.f-w-600
    {:font-weight 600}]

   [:.m-r--10
    {:margin-right "-10px"}]
   [:.m-r--5
    {:margin-right "-5px"}]
   [:.m-r-2
    {:margin-right "2px"}]
   [:.m-r-5
    {:margin-right "5px"}]
   [:.m-r-10
    {:margin-right "10px"}]
   [:.m-r-18
    {:margin-right "18px"}]
   [:.m-r-20
    {:margin-right "20px"}]
   [:.m-r-80
    {:margin-right "80px"}]

   [:.m-t--10
    {:margin-top "-10px"}]
   [:.m-t--5
    {:margin-top "-5px"}]
   [:.m-t-2
    {:margin-top "2px"}]
   [:.m-t-21
    {:margin-top "21px"}]

   [:.opacity-5
    {:opacity "0.5"}]
   [:.opacity-6
    {:opacity "0.6"}]

   [:.m-b--2
    {:margin-bottom "-2px"}]
   [:.m-b--1
    {:margin-bottom "-1px"}]
   [:.m-b-0-last:last-child
    {:margin-bottom "0px"}]
   [:.m-b-5
    {:margin-bottom "5px"}]
   [:.m-b-10
    {:margin-bottom "10px"}]
   [:.m-b-16
    {:margin-bottom "16px"}]
   [:.m-b-19
    {:margin-bottom "19px"}]
   [:.m-b-20
    {:margin-bottom "20px"}]

   [:.m-l--10
    {:margin-left "-10px"}]
   [:.m-l--5
    {:margin-left "-5px"}]
   [:.m-l-30
    {:margin-left "30px"}]

   [:.m-5
    {:margin "5px"}]

   [:.text-shadow
    {:text-shadow "1px 2px 1px black"}]

   [:.hover-shadow:hover
    {:box-shadow "0 2px 6px 0 rgba(0, 0, 0, 0.5)"}]
   
   [:.t-a-c
    {:text-align :center}]
   [:.justify-cont-s-b
    {:justify-content :space-between}]
   [:.justify-cont-s-a
    {:justify-content :space-around}]
   [:.justify-cont-c
    {:justify-content :center}]
   [:.justify-cont-end
    {:justify-content :flex-end}]
   [:.align-items-c
    {:align-items :center}]
   [:.align-items-t
    {:align-items :flex-start}]

   [:.w-auto
    {:width :auto}]
   [:.w-40-p
    {:width "40%"}]
   [:.w-50-p
    {:width "50%"}]
   [:.w-60-p
    {:width "60%"}]
   [:.w-100-p
    {:width "100%"}]

   [:.w-12
    {:width "12px"}]
   [:.w-14
    {:width "14px"}]
   [:.w-15
    {:width "15px"}]
   [:.w-18
    {:width "18px"}]
   [:.w-20
    {:width "20px"}]
   [:.w-24
    {:width "24px"}]
   [:.w-32
    {:width "32px"}]
   [:.w-40
    {:width "40px"}]
   [:.w-48
    {:width "48px"}]
   [:.w-50
    {:width "50px"}]
   [:.w-60
    {:width "60px"}]
   [:.w-70
    {:width "70px"}]
   [:.w-80
    {:width "80px"}]
   [:.w-100
    {:width "100px"}]
   [:.w-200
    {:width "200px"}]
   [:.w-250
    {:width "250px"}]
   [:.w-300
    {:width "300px"}]
   [:.w-500
    {:width "500px"}]
   [:.w-1440
    {:width "1440px"}]

   [:.h-12
    {:height "12px"}]
   [:.h-14
    {:height "14px"}]
   [:.h-15
    {:height "15px"}]
   [:.h-18
    {:height "18px"}]
   [:.h-20
    {:height "20px"}]
   [:.h-24
    {:height "24px"}]
   [:.h-25
    {:height "25px"}]
   [:.h-32
    {:height "32px"}]
   [:.h-40
    {:height "40px"}]
   [:.h-48
    {:height "48px"}]
   [:.h-800
    {:height "800px"}]

   [:.overflow-auto
    {:overflow :auto}]
   
   [:.posn-rel
    {:position :relative}]
   [:.posn-abs
    {:position :absolute}]
   [:.posn-fixed
    {:position :fixed}]
   [:.white
    {:color :white}]
   [:.black
    {:color :black}]
   [:.orange
    {:color button-color}]
   [:.uppercase
    {:text-transform :uppercase}]
   [:.bg-trans
    {:background-color :transparent}]
   [:.no-border
    {:border :none}]

   [:.underline
    {:text-decoration :underline}]

   [:.p-t-2
    {:padding-top "2px"}]
   [:.p-t-3
    {:padding-top "3px"}]
   [:.p-t-4
    {:padding-top "4px"}]
   [:.p-t-5
    {:padding-top "5px"}]
   [:.p-t-10
    {:padding-top "10px"}]

   [:.p-b-5
    {:padding-bottom "5px"}]
   [:.p-b-10
    {:padding-bottom "10px"}]
   [:.p-b-40
    {:padding-bottom "40px"}]
   [:.p-0
    {:padding "0px"}]
   [:.p-1
    {:padding "1px"}]
   [:.p-2
    {:padding "2px"}]
   [:.p-5
    {:padding "5px"}]
   [:.p-10
    {:padding "10px"}]
   [:.p-5-10
    {:padding "5px 10px"}]

   [:.p-l-5
    {:padding-left "5px"}]
   [:.p-l-10
    {:padding-left "10px"}]
   [:.p-l-15
    {:padding-left "15px"}]
   [:.p-l-20
    {:padding-left "20px"}]

   [:.p-r-5
    {:padding-right "5px"}]
   [:.p-r-10
    {:padding-right "10px"}]

   [:.b-rad-50-p
    {:border-radius "50%"}]
   [:.b-rad-5
    {:border-radius "5px"}]

   [:.b-1
    {:border "1px solid"}]

   [:.b-b-2
    {:border-bottom "2px solid"}]
   
   [:.b-w-3
    {:border-width "3px"}]
   [:.b-w-5
    {:border-width "5px"}]

   [:.b-color-gray
    {:border-color "rgba(255,255,255,0.2)"}]

   [:ul.list-style-disc
    {:list-style-type :disc}]

   [:.hidden
    {:display :none}]

   [:.pointer
    {:cursor :pointer}]
   [:.cursor-disabled
    {:cursor :not-allowed}]

   [:.c-f4692a
    {:color "#f4692a"}]
   [:.c-f32e50
    {:color "#f32e50"}]
   [:.c-b35c95
    {:color "#b35c95"}]
   [:.c-47eaf8
    {:color "#47eaf8"}]
   [:.c-bbe289
    {:color "#bbe289"}]
   [:.c-f9b747
    {:color "#f9b747"}]

   [:.b-orange
    {:border-color button-color}]
   [:.b-red
    {:border-color red}]

   [:.hover-slight-white:hover
    {:background-color "#2c3445"
     :opacity 0.2}]

   [:.hover-opacity-full:hover
    {:opacity 1.0}]
   
   [:.bg-light
    {:background-color "#2c3445"}]
   [:.bg-red
    {:background-color red}]
   [:.bg-green
    {:background-color "#70a800"}]

   (at-keyframes
    :bling-animation
    [:from {:text-shadow "0 0 10px black"}]
    [:to {:text-shadow "0 0 10px red"}])

   [:.no-appearance
    (handle-browsers :appearance :none)]])

(def sm-min "768px")
(def md-max "1199px")

(def xs-query
  {:max-width "767px"})

(def sm-query
  {:min-width sm-min :max-width "991px"})

(def md-width "992px")

(def md-query
  {:min-width md-width :max-width md-max})

(def sm-or-md-query
  {:min-width sm-min :max-width md-max})

(def lg-width "1200px")

(def lg-query
  {:min-width lg-width})

(def media-queries
  [[:.visible-xs,
    :.visible-sm,
    :.visible-md,
    :.visible-lg
    {:display "none !important"}]

   [:.visible-xs-block,
    :.visible-xs-inline,
    :.visible-xs-inline-block,
    :.visible-sm-block,
    :.visible-sm-inline,
    :.visible-sm-inline-block,
    :.visible-md-block,
    :.visible-md-inline,
    :.visible-md-inline-block,
    :.visible-lg-block,
    :.visible-lg-inline,
    :.visible-lg-inline-block 
    {:display "none !important"}]

   (at-media xs-query
    [:.visible-xs {:display "block !important"
                   }]
    [:table.visible-xs {:display "table !important"}]
    [:tr.visible-xs {
                     :display "table-row !important"
                     }]
    [:th.visible-xs,
     :td.visible-xs {
                     :display "table-cell !important"
                     }])

   (at-media xs-query
    [:.visible-xs-block
     {:display "block !important"}])
   (at-media xs-query [
                                   :.visible-xs-inline {
                                                        :display "inline !important"
                                                        }
                                   ])
   (at-media xs-query [
                                   :.visible-xs-inline-block {
                                                              :display "inline-block !important"
                                                              }
                                   ])
   (at-media sm-query [
                                                          :.visible-sm {
                                                                        :display "block !important"
                                                                        }
                                                          :table.visible-sm {
                                                                             :display "table !important"
                                                                             }
                                                          :tr.visible-sm {
                                                                          :display "table-row !important"
                                                                          }
                                                          :th.visible-sm,
                                                          :td.visible-sm {
                                                                          :display "table-cell !important"
                                                                          }
                                                          ])
   (at-media sm-query [
                                                          :.visible-sm-block {
                                                                              :display "block !important"
                                                                              }
                                                          ])
   (at-media sm-query [
                                                          :.visible-sm-inline {
                                                                               :display "inline !important"
                                                                               }
                                                          ])
   (at-media sm-query [
                                                          :.visible-sm-inline-block {
                                                                                     :display "inline-block !important"
                                                                                     }
                                                          ])
   (at-media md-query [
                                                           :.visible-md {
                                                                         :display "block !important"
                                                                         }
                                                           :table.visible-md {
                                                                              :display "table !important"
                                                                              }
                                                           :tr.visible-md {
                                                                           :display "table-row !important"
                                                                           }
                                                           :th.visible-md,
                                                           :td.visible-md {
                                                                           :display "table-cell !important"
                                                                           }
                                                           ])
   (at-media md-query [
                                                           :.visible-md-block {
                                                                               :display "block !important"
                                                                               }
                                                           ])
   (at-media md-query [
                                                           :.visible-md-inline {
                                                                                :display "inline !important"
                                                                                }
                                                           ])
   (at-media md-query [
                                                           :.visible-md-inline-block {
                                                                                      :display "inline-block !important"
                                                                                      }
                                                           ])
   (at-media lg-query [
                                    :.visible-lg {
                                                  :display "block !important"
                                                  }
                                    :table.visible-lg {
                                                       :display "table !important"
                                                       }
                                    :tr.visible-lg {
                                                    :display "table-row !important"
                                                    }
                                    :th.visible-lg,
                                    :td.visible-lg {
                                                    :display "table-cell !important"
                                                    }
                                    ])
   (at-media  [
                                    :.visible-lg-block {
                                                        :display "block !important"
                                                        }
                                    ])
   (at-media lg-query [
                                    :.visible-lg-inline {
                                                         :display "inline !important"
                                                         }
                                    ])
   (at-media lg-query [
                                    :.visible-lg-inline-block {
                                                               :display "inline-block !important"
                                                               }
                                    ])
   (at-media xs-query [
                                   :.hidden-xs {
                                                :display "none !important"
                                                }
                                   ])
   (at-media sm-query [
                                                          :.hidden-sm {
                                                                       :display "none !important"
                                                                       }
                                                          ])
   (at-media md-query [
                                                           :.hidden-md {
                                                                        :display "none !important"
                                                                        }
                                                           ])
   (at-media lg-query [
                                    :.hidden-lg {
                                                 :display "none !important"
                                                 }
                                    ])
   [:.visible-print
    :display "none !important"
    ]
   (at-media
    {:print true}
    [:.visible-print
     {:display "block !important"}]
    [:th.visible-print,
     :td.visible-print
     {:display "table-cell !important"}]
    [:table.visible-print
     {:display "table !important"}]
    [:tr.visible-print
     {:display "table-row !important"}])
   [:.visible-print-block
    {:display "none !important"}]
   
   (at-media
    {:print true}
    [:.visible-print-block
     {:display "block !important"}])
   [:.visible-print-inline
    {:display "none !important"}]
   (at-media
    {:print true}
    [:.visible-print-inline
     {:display "inline !important"}
     ])
   [:.visible-print-inline-block
    {:display "none !important"}]
   (at-media
    {:print true}
    [:.visible-print-inline-block
     {:display "inline-block !important"}])
   (at-media
    {:print true}
    [:.hidden-print
     {:display "none !important"}])])

(def font-family "Open Sans, sans-serif !important")

(def app
  (concat
   [[:.app
     {:background-image "linear-gradient(182deg, #2c3445, #000000)"
      :font-family font-family
      :height "100%"
      :overflow :auto}]

    [:select
     {:font-family font-family
      :cursor :pointer}]

    [:*:focus
     {:outline 0}]
    

    [:.sticky-header
     {:top 0
      :box-shadow "0 2px 6px 0 rgba(0, 0, 0, 0.5)"
      :z-index 100
      :display :none}]

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
      :height (px const/header-height)}]

    [:.app-header-bar
     {:height (px 81)
      :-webkit-backdrop-filter "blur(5px)"
      :backdrop-filter "blur(5px)"
      :background-color "rgba(0, 0, 0, 0.15)"}]

    [:.options-column
     {:width "300px"}]

    [:.builder-column
     {:display :none
      :margin "0 5px"}]

    [:.stepper-column
     {:margin-right "-10px"}]

    (at-media
     xs-query
     [:.app-header
      {:height (px 81)
       :background-image :none
       :background-color "rgba(0, 0, 0, 0.3)"}]
     [:.app-header-bar
      {:backdrop-filter :none
       :-webkit-backdrop-filter :none}]
     [:.orcpub-logo
      {:width "220px"
       :margin-left "20px"}]
     [:.content
      {:width "100%"}]
     [:.options-column
      {:width "100%"}])

    (at-media
     xs-query
     [:.build-tab
      {:display :none}]
     [:.options-tab-active
      [:.options-column
       {:display :none}]
      [:.options-column
       {:display :block}]
      [:.personality-column
       {:display :none}]
      [:.details-column
       {:display :none}]]
     [:.personality-tab-active
      [:.options-column
       {:display :none}]
      [:.personality-column
       {:display :block}]
      [:.details-column
       {:display :none}]]
     [:.details-tab-active
      [:.options-column
       {:display :none}]
      [:.personality-column
       {:display :none}]
      [:.details-column
       {:display :block}]])

    (at-media
     sm-or-md-query
     [:.build-tab
      {:display :block}]
     [:.options-tab
      {:display :none}]
     [:.personality-tab
      {:display :none}]
     [:.build-tab-active
      [:.options-column
       {:display :block}]
      [:.stepper-column
       {:display :block}]
      [:.personality-column
       {:display :block}]
      [:.details-column
       {:display :none}]]
     [:.details-tab-active
      [:.options-column
       {:display :none}]
      [:.personality-column
       {:display :none}]
      [:.details-column
       {:display :block}]]
     
     [:.details-columns
      {:display :flex}])

    (at-media
     lg-query
     [:.builder-column
      {:display :block}]
     [:.details-column
      {:max-width "500px"}])

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
      :cursor :pointer
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
      :box-shadow "0 2px 6px 0 rgba(0, 0, 0, 0.5)"
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
       :cursor :pointer
       :border "1px solid white"}
      text-color
      (handle-browsers :appearance :menulist))

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

    [:.form-button:hover
     {:box-shadow "0 2px 6px 0 rgba(0, 0, 0, 0.5)"}]

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
      :box-shadow "0 2px 6px 0 rgba(0, 0, 0, 0.5)"}]

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
      :align-items :center}]]
   margin-lefts
   margin-tops
   font-sizes
   props
   media-queries))

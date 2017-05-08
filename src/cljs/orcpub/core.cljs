(ns orcpub.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [orcpub.character-builder :as ch]
            [orcpub.dnd.e5.subs]
            [orcpub.dnd.e5.events]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            [clojure.string :as s]
            [re-frame.core :refer [dispatch dispatch-sync]]
            [reagent.core :as r]))

(enable-console-print!)

(dispatch-sync [:initialize-db])

(def text-color "#484848")

(def orange "#f0a100")

(def input-style
  {:height "38px" :width "438px"
   :border "solid 1px rgba(72,72,72,0.37)"
   :border-radius "3px"
   :font-size "14px"
   :padding-left "10px"
   :color text-color})

(def register-url (if (s/starts-with? js/window.location.href "http://localhost")
                    "http://localhost:8890/register"
                    "/register"))

(defn login-form []
  (let [params (r/atom {})]
    (fn []
      [:div
       [:input.m-t-20 {:name :email
                       :placeholder "Username or Email"
                       :style (assoc input-style :width "200px")
                       :value (:username @params)
                       :on-change (fn [e] (swap! params assoc :username (.. e -target -value)))}]
       [:input.m-t-20.m-l-5 {:name :password
                             :type :password
                             :placeholder "Password"
                             :value (:password @params)
                             :style (assoc input-style :width "200px")
                             :on-change (fn [e] (swap! params assoc :password (.. e -target -value)))}]
       [:button.form-button.m-l-5
        {:style {:height "42px"
                 :width "100px"
                 :font-size "16px"}
         :on-click (fn [_] (dispatch [:login @params]))}
        "LOGIN"]])))

(defn login-page []
  [:div.sans.h-100-p.flex
   {:style {:flex-direction :column}}
   [:div.container {:style {:height "80px"
                            :background-color "#1a2532"
                            :box-shadow "0 2px 6px 0 rgba(0,0,0,0.5)"}}
    [:div.content
     [:div.flex.justify-cont-s-b.w-100-p
      [:img {:src "image/orcpub-logo.svg"}]
      [login-form]]]]
   [:div.flex.justify-cont-s-a.align-items-c.flex-grow-1
    [:div
     {:style {:width "785px"
              :height "600px"
              :background-color :white
              :border "1px solid white"
              :color text-color}}
     [:div.flex
      [:div {:style {:width "487px"}}
       [:div.flex.justify-cont-s-a.align-items-c
        {:style {:height "65px"
                 :background-color "#1a2532"
                 :border-right "1px solid white"}}
        [:img {:src "image/orcpub-logo.svg"
               :style {:height "25.3px"}}]]
       [:div {:style {:text-align :center}}
        [:div {:style {:color orange
                       :font-weight :bold
                       :font-size "36px"
                       :text-transform :uppercase
                       :text-shadow "1px 2px 1px rgba(0,0,0,0.37)"
                       :margin-top "20px"}}
         "join for free"]
        [:div.f-s-16.m-t-20 "Join now to save your character"]
        [:form {:action (if (s/starts-with? js/window.location.href "http://localhost")
                          "http://localhost:8890/register"
                          "/register")
                :method :post}
         [:input.m-t-20 {:name :first-and-last-name
                         :placeholder "First and Last Name"
                         :style input-style}]
         [:input.m-t-20 {:name :email
                         :placeholder "Email"
                         :style input-style}]
         [:input.m-t-20 {:name :username
                         :placeholder "Username"
                         :style input-style}]
         [:input.m-t-20 {:name :password
                         :placeholder "Password"
                         :style input-style}]
         [:div.m-t-20
          {:style {:text-align :left
                   :margin-left "15px"}}
          [:i.fa.fa-check.f-s-14.orange
           {:style {:margin-top "-3px"
                    :border-color "#f0a100"
                    :border-style :solid
                    :border-width "1px"
                    :border-bottom-width "3px"}}]
          [:span.m-l-5 "Yes! Send me updates about OrcPub."]]
         [:div {:style {:margin-top "40px"}}
          #_[:span "Already have an account?"]
          #_[:span.hover-underline.f-w-b.m-l-10.pointer "LOGIN"]
          [:button.form-button.m-l-20 {:style {:height "40px"
                                               :width "174px"
                                               :font-size "16px"
                                               :font-weight "600"}
                                       :type :submit}
           "JOIN"]]]
        [:div.m-t-5
         [:span.f-s-14
          "By clicking JOIN you agree to our"
          [:a.m-l-5 {:href "" :target :_blank
                     :style {:color text-color}} "Terms of Use"]
          [:span.m-l-5 "and that you've read our"]
          [:a.m-l-5 {:href "" :target :_blank
                     :style {:color text-color}} "Privacy Policy"]]]
        [:div.m-l-15 {:style {:text-align :left
                              :margin-top "15px"}}
         "© 2017 OrcPub"]]]
      [:div {:style {:background-image "url(image/shutterstock_432001912.jpg)"
                     :background-size "900px 600px"
                     :background-position "-260px 0px"
                     :background-clip :content-box
                     :width "308px"
                     :height "600px"}}]]]]])

(r/render (if (let [doc-style js/document.documentElement.style]
                (and js/window.localStorage
                     (or (aget doc-style "flexWrap")
                         (aget doc-style "WebkitFlexWrap")
                         (aget doc-style "msFlexWrap"))))
            [ch/character-builder]
            #_[login-page]
            [:div
             [ch/app-header]
             [:div.f-s-24.white.sans
              {:style {:padding "200px"}}
              "Sorry, we are unable to support your browser since it does not support important HTML5 features. Please try a modern browser such as " [:a {:href "https://www.google.com/chrome/browser/desktop/index.html"} "Google Chrome"] " or " [:a {:href "https://www.mozilla.org/en-US/firefox/products/?v=a"} "Mozilla Firefox"]]])
          (js/document.getElementById "app"))

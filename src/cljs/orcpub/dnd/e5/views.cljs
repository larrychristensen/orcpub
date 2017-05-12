(ns orcpub.dnd.e5.views
  (:require [re-frame.core :refer [dispatch dispatch-sync]]
            [reagent.core :as r]))

(def text-color "#484848")

(def orange "#f0a100")

(def input-style
  {:height "38px" :width "438px"
   :border "solid 1px rgba(72,72,72,0.37)"
   :border-radius "3px"
   :font-size "14px"
   :padding-left "10px"
   :color text-color})

(defn set-value [atom key e]
  (swap! atom assoc key (.. e -target -value)))

(defn login-form []
  (let [params (r/atom {})]
    (fn []
      [:div
       [:input {:name :email
                       :placeholder "Username or Email"
                       :style (assoc input-style :width "150px")
                       :value (:username @params)
                       :on-change (partial set-value params :username)}]
       [:input.m-l-5 {:name :password
                             :type :password
                             :placeholder "Password"
                             :value (:password @params)
                             :style (assoc input-style :width "150px")
                             :on-change (partial set-value params :password)}]
       [:button.form-button.m-l-5
        {:style {:height "42px"
                 :width "100px"
                 :font-size "16px"}
         :on-click #(dispatch [:login @params true])}
        "LOGIN"]])))

(defn register-form []
  (let [params (r/atom {})]
    (fn []
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
          [:div
           [:input.m-t-20 {:name :first-and-last-name
                           :value (:first-and-last-name @params)
                           :placeholder "First and Last Name"
                           :style input-style
                           :on-change (partial set-value params :first-and-last-name)}]
           [:input.m-t-20 {:name :email
                           :value (:email @params)
                           :type :email
                           :placeholder "Email"
                           :style input-style
                           :on-change (partial set-value params :email)}]
           [:input.m-t-20 {:name :username
                           :value (:username @params)
                           :placeholder "Username"
                           :style input-style
                           :on-change (partial set-value params :username)}]
           [:input.m-t-20 {:name :password
                           :type :password
                           :value (:password @params)
                           :placeholder "Password"
                           :style input-style
                           :on-change (partial set-value params :password)}]
           [:div.m-t-20
            {:style {:text-align :left
                     :margin-left "15px"}}
            [:i.fa.fa-check.f-s-14.pointer
             {:class-name (if (:send-updates? @params) "orange" "white")
              :style {:margin-top "-3px"
                      :border-color "#f0a100"
                      :border-style :solid
                      :border-width "1px"
                      :border-bottom-width "3px"}
              :on-click #(swap! params update :send-updates? not)}]
            [:span.m-l-5 "Yes! Send me updates about OrcPub."]]
           [:div {:style {:margin-top "40px"}}
            #_[:span "Already have an account?"]
            #_[:span.hover-underline.f-w-b.m-l-10.pointer "LOGIN"]
            [:button.form-button.m-l-20
             {:style {:height "40px"
                      :width "174px"
                      :font-size "16px"
                      :font-weight "600"}
              :on-click #(dispatch [:register @params true])}
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
                       :height "600px"}}]]])))


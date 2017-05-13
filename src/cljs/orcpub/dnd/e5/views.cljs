(ns orcpub.dnd.e5.views
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [reagent.core :as r]
            [orcpub.route-map :as routes]
            [orcpub.common :as common]
            [bidi.bidi :as bidi]))

(def text-color "#484848")

(def orange "#f0a100")

(def input-style
  {:height "38px" :width "438px"
   :border-style "solid"
   :border-width "1px"
   :border-radius "3px"
   :font-size "14px"
   :padding-left "10px"
   :color text-color})

(defn event-value [e]
  (.. e -target -value))

(defn set-value [atom key e]
  (swap! atom assoc key (event-value e)))

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

(defn registration-page [content]
  [:div.sans.h-100-p.flex
   {:style {:flex-direction :column}}
   [:div.container {:style {:height "80px"
                            :background-color "#1a2532"
                            :box-shadow "0 2px 6px 0 rgba(0,0,0,0.5)"}}
    [:div.content
     [:div.flex.justify-cont-s-b.w-100-p.align-items-c.p-l-20.p-r-20
      [:img {:src "image/orcpub-logo.svg"}]
      [login-form]]]]
   [:div.flex.justify-cont-s-a.align-items-c.flex-grow-1
    [:div
     {:style {:width "785px"
              :min-height "600px"
              :background-color :white
              :border "1px solid white"
              :color text-color}}
     [:div.flex
      [:div.flex {:style {:width "487px"
                          :flex-direction :column}}
       [:div.flex.justify-cont-s-a.align-items-c
        {:style {:height "65px"
                 :background-color "#1a2532"
                 :border-right "1px solid white"}}
        [:img {:src "image/orcpub-logo.svg"
               :style {:height "25.3px"}}]]
       [:div.flex-grow-1 content]
       [:div.m-l-15.m-b-10 {:style {:text-align :left}}
        "© 2017 OrcPub"]]
      [:div {:style {:background-image "url(image/shutterstock_432001912.jpg)"
                     :background-size "1200px 800px"
                     :background-position "-350px 0px"
                     :background-clip :content-box
                     :width "308px"
                     :min-height "600px"}}]]]]])

(defn verify-failed []
  (let [params (r/atom {})]
    (fn []
      (registration-page
       [:div.flex.justify-cont-s-b {:style {:text-align :center
                           :flex-direction :column}}
        [:div.p-20
         [:div.f-w-b.f-s-24.p-b-10
          "That key has expired."]
         [:div "You must verify your email within 24 hours of registering. Send another verification email by submitting you address here:"]
         [:input.m-t-20 {:name :email
                         :value (:email @params)
                         :type :email
                         :placeholder "Email"
                         :style input-style
                         :on-change (partial set-value params :email)}]
         [:button.form-button.m-l-20.m-t-10
          {:style {:height "40px"
                   :width "174px"
                   :font-size "16px"
                   :font-weight "600"}
           :on-click #(dispatch [:re-verify @params])}
          "RESEND"]]]))))

(defn verify-success []
  (registration-page
   [:div {:style {:text-align :center}}
    [:div {:style {:color orange
                   :font-weight :bold
                   :font-size "36px"
                   :text-transform :uppercase
                   :text-shadow "1px 2px 1px rgba(0,0,0,0.37)"
                   :margin-top "100px"}}
     "Success! Registration is complete"]
    [:div.m-t-20 "You can now log in above."]]))

(defn verify-sent []
  (registration-page
   [:div {:style {:text-align :center}}
    [:div {:style {:color orange
                   :font-weight :bold
                   :font-size "36px"
                   :text-transform :uppercase
                   :text-shadow "1px 2px 1px rgba(0,0,0,0.37)"
                   :margin-top "100px"}}
     "Check your email"]
    [:div.p-20
     (str "We sent a verification email to "
          @(subscribe [:temp-email])
          ". You must verify to complete registration and the link we sent will only be valid for 24 hours.")]]))

(defn validation-messages [messages]
  (if messages
    [:ul.t-a-l.p-l-20.p-r-20
     (map-indexed
      (fn [i msg]
        ^{:key i}
        [:li.red (str common/dot-char " " msg)])
      messages)]))

(defn form-input [title key form-data form-validation type]
  (let [value (key form-data)]
    [:div
     [:input.m-t-20
      {:name key
       :type type
       :value value
       :placeholder title
       :style input-style
       :class-name (if (and value (seq (form-validation key)))
                     "b-red"
                     "b-gray")
       :on-change (fn [e] (dispatch [(keyword (str "registration-" (name key))) (event-value e)]))}]
     (if value (validation-messages (form-validation key)))]))

(defn register-form []
  (let [registration-validation @(subscribe [:registration-validation])
        registration-form @(subscribe [:registration-form])
        send-updates? (not= false (:send-updates? registration-form))]
    (prn "VALIDATION" registration-validation)
    (registration-page
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
       (form-input "First and Last Name" :first-and-last-name registration-form registration-validation :text)
       (form-input "Email" :email registration-form registration-validation :email)
       (form-input "Username" :username registration-form registration-validation :username)
       (form-input "Password" :password registration-form registration-validation :password)
       [:div.m-t-20
        {:style {:text-align :left
                 :margin-left "15px"}}
        [:i.fa.fa-check.f-s-14.pointer
         {:class-name (if send-updates? "orange" "white")
          :style {:margin-top "-3px"
                  :border-color "#f0a100"
                  :border-style :solid
                  :border-width "1px"
                  :border-bottom-width "3px"}
          :on-click #(dispatch [:registration-send-updates? (not send-updates?)])}]
        [:span.m-l-5 "Yes! Send me updates about OrcPub."]]
       [:div {:style {:margin-top "40px"}}
        #_[:span "Already have an account?"]
        #_[:span.hover-underline.f-w-b.m-l-10.pointer "LOGIN"]
        [:button.form-button.m-l-20
         {:style {:height "40px"
                  :width "174px"
                  :font-size "16px"
                  :font-weight "600"}
          :on-click #(dispatch [:register])}
         "JOIN"]]]
      [:div.m-t-5
       [:span.f-s-14
        "By clicking JOIN you agree to our"
        [:a.m-l-5 {:href "" :target :_blank
                   :style {:color text-color}} "Terms of Use"]
        [:span.m-l-5 "and that you've read our"]
        [:a.m-l-5 {:href "" :target :_blank
                   :style {:color text-color}} "Privacy Policy"]]]])))


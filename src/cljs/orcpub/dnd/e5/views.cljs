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

(def default-input-style
  (merge
   input-style
   {:border-color "rgba(72,72,72,0.37)"}))

(defn event-value [e]
  (.. e -target -value))

(defn set-value [atom key e]
  (swap! atom assoc key (event-value e)))

(defn registration-page [content]
  [:div.sans.h-100-p.flex
   {:style {:flex-direction :column}}
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
          "Your key has expired."]
         [:div "You must verify your email within 24 hours of registering. Send another verification email by submitting you address here:"]
         [:input.m-t-20 {:name :email
                         :value (:email @params)
                         :type :email
                         :placeholder "Email"
                         :style default-input-style
                         :on-change (partial set-value params :email)}]
         [:button.form-button.m-l-20.m-t-10
          {:style {:height "40px"
                   :width "174px"
                   :font-size "16px"
                   :font-weight "600"}
           :on-click #(dispatch [:re-verify @params])}
          "RESEND"]]]))))

(defn send-password-reset-page []
  (let [params (r/atom {})]
    (fn []
      (registration-page
       [:div.flex.justify-cont-s-b {:style {:text-align :center
                           :flex-direction :column}}
        [:div.p-20
         [:div.f-w-b.f-s-24.p-b-10
          "Reset Password"]
         [:div "Submit your email address here and we will send you an email to reset your password."]
         [:input.m-t-20 {:name :email
                         :value (:email @params)
                         :type :email
                         :placeholder "Email"
                         :style default-input-style
                         :on-change (partial set-value params :email)}]
         [:button.form-button.m-l-20.m-t-10
          {:style {:height "40px"
                   :width "174px"
                   :font-size "16px"
                   :font-weight "600"}
           :on-click #(dispatch [:re-verify @params])}
          "SUBMIT"]]]))))

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

(defn form-input []
  (let [blurred? (r/atom false)]
    (fn [title key form-data form-validation type on-change]
      (let [value (key form-data)]
        [:div
         [:input.m-t-20
          {:name key
           :type type
           :value value
           :placeholder title
           :style input-style
           :class-name (if (and @blurred? (seq (key form-validation)))
                         "b-red"
                         "b-gray")
           :on-change on-change
           :on-blur #(do (prn "BLUR") (swap! blurred? (fn [_] true)))}]
         (if @blurred? (validation-messages (form-validation key)))]))))

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
       [form-input "First and Last Name" :first-and-last-name registration-form registration-validation :text (fn [e] (dispatch [:registration-first-and-last-name (event-value e)]))]
       [form-input "Email" :email registration-form registration-validation :email (fn [e] (dispatch [:registration-email (event-value e)]))]
       [form-input "Username" :username registration-form registration-validation :username (fn [e] (dispatch [:registration-username (event-value e)]))]
       [form-input "Password" :password registration-form registration-validation :password (fn [e] (dispatch [:registration-password (event-value e)]))]
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
       [:div.m-t-30
        [:span "Already have an account?"]
        [:span.underline.f-w-b.m-l-10.pointer.orange
         {:on-click #(dispatch [:route routes/login-page-route])}
         "LOGIN"]
        [:button.form-button.m-l-20
         {:style {:height "40px"
                  :width "174px"
                  :font-size "16px"
                  :font-weight "600"}
          :class-name (if (seq registration-validation) "opacity-5 hover-no-shadow cursor-disabled")
          :on-click #(if (empty? registration-validation)
                       (dispatch [:register]))}
         "JOIN"]]]
      [:div.m-t-5
       [:span.f-s-14
        "By clicking JOIN you agree to our"
        [:a.m-l-5 {:href "" :target :_blank
                   :style {:color text-color}} "Terms of Use"]
        [:span.m-l-5 "and that you've read our"]
        [:a.m-l-5 {:href "" :target :_blank
                   :style {:color text-color}} "Privacy Policy"]]]])))

(defn login-page []
  (let [params (r/atom {})]
    (fn []
      (registration-page
       [:div {:style {:text-align :center}}
        [:div {:style {:color orange
                       :font-weight :bold
                       :font-size "36px"
                       :text-transform :uppercase
                       :text-shadow "1px 2px 1px rgba(0,0,0,0.37)"
                       :margin-top "20px"}}
         "LOGIN"]
        [:div
         {:style {:margin-top "50px"}}
         [form-input "Username or Email" :username @params nil :username #(swap! params assoc :username (event-value %))]
         [form-input "Password" :password @params nil :password #(swap! params assoc :password (event-value %))]
         [:div {:style {:margin-top "40px"}}
          #_[:span "Already have an account?"]
          #_[:span.hover-underline.f-w-b.m-l-10.pointer "LOGIN"]
          [:button.form-button.m-l-20
           {:style {:height "40px"
                    :width "174px"
                    :font-size "16px"
                    :font-weight "600"}
            :on-click #(dispatch [:login @params true])}
           "LOGIN"]
          [:div.m-t-20
           [:span "Don't have a login? "]
           [:span.orange.underline.pointer
            {:on-click #(dispatch [:route routes/register-page-route])}
            "REGISTER NOW"]]
          [:div.m-t-20
           [:span "Forgot your password? "]
           [:span.orange.underline.pointer
            {:on-click #(dispatch [:route routes/reset-password-page-route])}
            "RESET PASSWORD"]]]]]))))


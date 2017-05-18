(ns orcpub.dnd.e5.views
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [reagent.core :as r]
            [orcpub.route-map :as routes]
            [orcpub.common :as common]
            [cljs.pprint :refer [pprint]]
            [orcpub.registration :as registration]
            [bidi.bidi :as bidi]))

(def text-color "#484848")

(def orange "#f0a100")

(def input-style
  {:height "38px"
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

(defn validation-messages [messages]
  (if messages
    [:ul.t-a-l.p-l-20.p-r-20
     (doall
      (map-indexed
       (fn [i msg]
         ^{:key i}
         [:li.red (str common/dot-char " " msg)])
       messages))]))

(defn base-input [attrs]
  [:div.flex.p-l-10.p-l-10.p-r-10.p-t-10
   [:input.flex-grow-1
    attrs]])

(defn form-input []
  (let [blurred? (r/atom false)]
    (fn [{:keys [title key value messages type on-change]}]
      [:div
       [base-input
        {:name key
         :type type
         :value value
         :placeholder title
         :style input-style
         :class-name (if (and @blurred? (seq messages))
                       "b-red"
                       "b-gray")
         :on-change on-change
         :on-blur #(swap! blurred? (fn [_] true))}]
       (if @blurred? (validation-messages messages))])))

(defn svg-icon [icon-name & [size]]
  (let [size (or size 32)]
    [:img
     {:class-name (str "h-" size " w-" size)
      :src (str "/image/" icon-name ".svg")}]))

(defn user-header-view []
  (let [username @(subscribe [:username])]
    (if username
      [:div.white.f-w-b.t-a-r
       [:span.m-r-5 username]
       #_[:i.fa.fa-caret-down]
       [:span.orange.underline.pointer
        {:on-click (fn [] (dispatch [:logout]))}
        "LOG OUT"]]
      [:div.pointer.flex.flex-column.align-items-end
       [:span.orange.underline.f-w-b.m-l-5
        {:on-click #(dispatch [:route routes/login-page-route])}
        [:span "LOGIN"]]])))

(def header-tab-style
  {:width "85px"})

(defn app-header []
  [:div#app-header.app-header.flex.flex-column.justify-cont-s-b
   [:div.app-header-bar.container
    [:div.content
     [:div.flex.justify-cont-s-b.align-items-c.w-100-p.p-l-20.p-r-20
      [:img.orcpub-logo.h-32.w-120 {:src "/image/orcpub-logo.svg"}]
      [user-header-view]]]]
   [:div.container
    [:div.content
     [:div.flex.justify-cont-end.w-100-p
      [:div.flex.m-b-5.m-r-5
       [:div.pointer.white.f-w-b.f-s-14.t-a-c.p-10.header-tab.m-5
        {:style header-tab-style
         :on-click #(dispatch [:route routes/dnd-e5-char-list-page-route])}
        [:div (svg-icon "battle-gear" 48 48)
         [:div "CHARACTERS"]]]
       [:div.white.f-w-b.f-s-14.t-a-c.p-10.header-tab.m-5.disabled
        {:style header-tab-style}
        [:div.opacity-2
         (svg-icon "spell-book" 48 48)
         [:div "SPELLS"]]]
       [:div.white.f-w-b.f-s-14.t-a-c.p-10.header-tab.m-5.disabled
        {:style header-tab-style}
        [:div.opacity-2
         (svg-icon "orc-head" 48 48)
         [:div "MONSTERS"]]]]]]]
   #_[:div.container.header-links
    [:div.content
     [:div.hidden-xs.hidden-sm
      [:div.m-l-10.white
       [:span "Questions? Comments? Issues? Feature Requests? We'd love to hear them, "]
       [:a {:href "https://muut.com/orcpub" :target :_blank} "report them here."]]
      [:div
       [:div.flex.align-items-c.f-w-b.f-s-18.m-t-10.m-l-10.white
        [:span.hidden-xs "Please support continuing development on "]
        [:a.m-l-5 patreon-link-props [:span "Patreon"]]
        [:a.m-l-5 patreon-link-props
         [:img.h-32.w-32 {:src "https://www.patreon.com/images/patreon_navigation_logo_mini_orange.png"}]]]]]]]])

(defn registration-page [content]
  [:div.sans.h-100-p.flex
   {:style {:flex-direction :column}}
   [:div.flex.justify-cont-s-a.align-items-c.flex-grow-1.h-100-p
    [:div.registration-content
     {:style {:background-color :white
              :border "1px solid white"
              :color text-color}}
     [:div.flex.h-100-p
      [:div.flex {:style {:flex-direction :column
                          :width "435px"}}
       [:div.flex.justify-cont-s-a.align-items-c
        {:style {:height "65px"
                 :background-color "#1a2532"
                 :border-right "1px solid white"}}
        [:img.pointer
         {:src "image/orcpub-logo.svg"
          :style {:height "25.3px"}
          :on-click #(dispatch [:route :default])}]]
       [:div.flex-grow-1 content]
       [:div.m-l-15.m-b-10 {:style {:text-align :left}}
        "© 2017 OrcPub"]]
      [:div.registration-image
       {:style {:background-image "url(image/shutterstock_432001912.jpg)"
                :background-size "1200px 800px"
                :background-position "-350px 0px"
                :background-clip :content-box
                :width "350px"
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
         [base-input
          {:name :email
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
    (fn [error-message]
      (registration-page
       [:div.flex.justify-cont-s-b.w-100-p
        {:style {:text-align :center
                 :flex-direction :column}}
        [:div.p-t-10
         (if error-message [:div.red.m-b-20 error-message])
         [:div.f-w-b.f-s-24.p-b-10
          "Send Password Reset Email"]
         [:div.m-b-10 "Submit your email address here and we will send you a link to reset your password."]
         [base-input
          {:name :email
           :value (:email @params)
           :type :email
           :placeholder "Email"
           :style default-input-style
           :on-change (partial set-value params :email)}]
         [:button.form-button.m-t-10
          {:style {:height "40px"
                   :width "174px"
                   :font-size "16px"
                   :font-weight "600"}
           :on-click #(dispatch [:send-password-reset @params])}
          "SUBMIT"]]]))))

(defn password-reset-expired-page []
  [send-password-reset-page "Your reset link has expired, you must complete the reset within 24 hours. Please use the form below to send another reset email."])

(defn password-reset-used-page []
  [send-password-reset-page "Your reset link has already been used. Please use the form below to send another reset email."])

(defn password-validation-messages [password]
  (-> password
      registration/validate-password
      :password))

(defn password-reset-page []
  (let [params (r/atom {})]
    (fn []
      (let [password (:password @params)
            verify-password (:verify-password @params)
            password-messages (password-validation-messages password)
            different? (not= password verify-password)
            invalid? (or (seq password-messages)
                         different?)]
        (registration-page
         [:div.flex.justify-cont-s-b {:style {:text-align :center
                                              :flex-direction :column}}
          [:div.p-20
           [:div.f-w-b.f-s-24.p-b-10
            "Reset Password"]
           [:div "Create a new password."]
           [form-input {:title "Password"
                        :key :password
                        :value password
                        :type :password
                        :messages password-messages
                        :on-change (fn [e] (swap! params assoc :password (event-value e)))}]
           [form-input {:title "Verify Password"
                        :key :verify-password
                        :value verify-password
                        :type :password
                        :messages (if different? ["Passwords do not match"])
                        :on-change (fn [e] (swap! params assoc :verify-password (event-value e)))}]
           [:button.form-button.m-l-20.m-t-10
            {:style {:height "40px"
                     :width "174px"
                     :font-size "16px"
                     :font-weight "600"}
             :class-name (if invalid? "opacity-5 hover-no-shadow cursor-disabled")
             :on-click #(if (not invalid?) (dispatch [:password-reset @params]))}
            "SUBMIT"]]])))))

(defn login-link []
  [:span.underline.f-w-b.m-l-10.pointer.orange
   {:on-click #(dispatch [:route routes/login-page-route])}
   "LOGIN"])

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
    [:div.m-t-20 "You can now"]
    [login-link]]))

(defn password-reset-success []
  (registration-page
   [:div {:style {:text-align :center}}
    [:div {:style {:color orange
                   :font-weight :bold
                   :font-size "36px"
                   :text-transform :uppercase
                   :text-shadow "1px 2px 1px rgba(0,0,0,0.37)"
                   :margin-top "100px"}}
     "Your password has been successfully reset"]
    [:div.m-t-20 "You can now log in"]
    [login-link]]))

(defn email-sent [text]
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
     text]]))

(defn verify-sent []
  (email-sent
   (str "We sent a verification email to "
        @(subscribe [:temp-email])
        ". You must verify to complete registration and the link we sent will only be valid for 24 hours.")))

(defn password-reset-sent []
  (email-sent
   (str "We sent an email to "
        @(subscribe [:temp-email])
        " with a link to reset your password.")))

(defn register-form []
  (let [registration-validation @(subscribe [:registration-validation])
        registration-form @(subscribe [:registration-form])
        send-updates? (not= false (:send-updates? registration-form))]
    (registration-page
     [:div {:style {:text-align :center}}
      [:div {:style {:color orange
                     :font-weight :bold
                     :font-size "36px"
                     :text-transform :uppercase
                     :text-shadow "1px 2px 1px rgba(0,0,0,0.37)"
                     :margin-top "20px"}}
       "join for free"]
      [:div.f-s-16.m-t-20 "Join now to save your characters and more!"]
      [:div.m-t-10
       [form-input {:title "First and Last Name"
                    :key :first-and-last-name
                    :value (:first-and-last-name registration-form)
                    :messages (:first-and-last-name registration-validation)
                    :type :text
                    :on-change (fn [e] (dispatch [:registration-first-and-last-name (event-value e)]))}]
       [form-input {:title "Email"
                    :key :email
                    :value (:email registration-form)
                    :messages (:email registration-validation)
                    :type :email
                    :on-change (fn [e] (dispatch [:registration-email (event-value e)]))}]
       [form-input {:title "Username"
                    :key :username
                    :value (:username registration-form)
                    :messages (:username registration-validation)
                    :type :username
                    :on-change (fn [e] (dispatch [:registration-username (event-value e)]))}]
       [form-input {:title "Password"
                    :key :password
                    :value (:password registration-form)
                    :messages (:password registration-validation)
                    :type :password
                    :on-change (fn [e] (dispatch [:registration-password (event-value e)]))}]
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
        [:div.p-10
         [:span "Already have an account?"]
         (login-link)]
        [:button.form-button
         {:style {:height "40px"
                  :width "174px"
                  :font-size "16px"
                  :font-weight "600"}
          :class-name (if (seq registration-validation) "opacity-5 hover-no-shadow cursor-disabled")
          :on-click #(if (empty? registration-validation)
                       (dispatch [:register]))}
         "JOIN"]]]
      [:div.m-t-5.p-r-10.p-l-10
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
         [form-input {:title "Username or Email"
                      :key :username
                      :value (:username @params)
                      :type :username
                      :on-change #(swap! params assoc :username (event-value %))}]
         [form-input {:title "Password"
                      :key :password
                      :value (:password @params)
                      :type :password
                      :on-change #(swap! params assoc :password (event-value %))}]
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
            {:on-click #(dispatch [:route routes/send-password-reset-page-route])}
            "RESET PASSWORD"]]]]]))))

(def loading-style
  {:position :absolute
   :height "100%"
   :width "100%"
   :top 0
   :bottom 0
   :right 0
   :left 0
   :z-index 100
   :background-color "rgba(0,0,0,0.6)"})

(defn header [title button-cfgs]
  [:div.w-100-p
   [:div.flex.align-items-c.justify-cont-s-b.flex-wrap
    [:h1.f-s-36.f-w-b.m-t-21.m-l-10.character-builder-header title]
    [:div.flex.align-items-c.justify-cont-end.flex-wrap.m-r-10.m-l-10
     (map-indexed
      (fn [i {:keys [title icon on-click]}]
        ^{:key i}
        [:button.form-button.h-40.m-l-5.m-t-5.m-b-5
         {:on-click on-click}
         [:span
          [:i.fa.fa-undo.f-s-18]
          [:i.fa.fa-undo.f-s-18]]
         [:span.m-l-5.header-button-text title]])
      button-cfgs)]]])

(defn content-page [title button-cfgs content]
  [:div.app
   {:on-scroll (fn [e]
                 (let [app-header (js/document.getElementById "app-header")
                       header-height (.-offsetHeight app-header)
                       scroll-top (.-scrollTop (.-target e))
                       sticky-header (js/document.getElementById "sticky-header")
                       app-main (js/document.getElementById "app-main")
                       scrollbar-width (- js/window.innerWidth (.-offsetWidth app-main))
                       header-container (js/document.getElementById "header-container")]
                   (set! (.-paddingRight (.-style header-container)) (str scrollbar-width "px"))
                   (if (>= scroll-top header-height)
                     (set! (.-display (.-style sticky-header)) "block")
                     (set! (.-display (.-style sticky-header)) "none"))))}
   (if @(subscribe [:loading])
     [:div {:style loading-style}
      [:div.flex.justify-cont-s-a.align-items-c.h-100-p
       [:img.h-200.w-200.m-t-200 {:src "/image/spiral.gif"}]]])
   [app-header]
   (let [hdr [header title button-cfgs]]
     [:div
      [:div#sticky-header.sticky-header.w-100-p.posn-fixed
       [:div.flex.justify-cont-c.bg-light
        [:div#header-container.f-s-14.white.content
         hdr]]]
      [:div.flex.justify-cont-c.white
       [:div.content hdr]]
      content
      [:div.white.flex.justify-cont-c
       [:div.content.f-w-n.f-s-12
        [:div.p-10
         [:div.m-b-5 "Icons made by Lorc, Caduceus, and Delapouite. Available on " [:a.orange {:href "http://game-icons.net"} "http://game-icons.net"]]]]]])])

(defn character-list []
  (let [characters @(subscribe [:dnd-5e-characters])]
    (pprint characters)
    [content-page
     "Characters"
     nil
     [:div
      (doall
       (map
        (fn [character]
          [:div (get-in character [:orcpub.entity.strict/values :orcpub.dnd.e5.character/character-name])])
        characters))]]))


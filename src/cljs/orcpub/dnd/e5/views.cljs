(ns orcpub.dnd.e5.views
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [reagent.core :as r]
            [orcpub.route-map :as routes]
            [orcpub.common :as common]
            [orcpub.entity-spec :as es]
            [orcpub.entity.strict :as se]
            [orcpub.dnd.e5.subs :as subs]
            [orcpub.dnd.e5.character :as char]
            [orcpub.dnd.e5.character.equipment :as char-equip]
            [cljs.pprint :refer [pprint]]
            [orcpub.registration :as registration]
            [orcpub.dnd.e5.magic-items :as mi]
            [orcpub.dnd.e5.spells :as spells]
            [orcpub.dnd.e5.skills :as skills]
            [orcpub.dnd.e5.equipment :as equip]
            [orcpub.dnd.e5.weapons :as weapon]
            [orcpub.dnd.e5.armor :as armor]
            [orcpub.dnd.e5.display :as disp]
            [orcpub.dnd.e5.template :as t]
            [orcpub.dnd.e5.options :as opt]
            [clojure.string :as s]
            [orcpub.user-agent :as user-agent]
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
    [:ul.t-a-l.p-l-20.p-r-20.m-b-10
     (doall
      (map-indexed
       (fn [i msg]
         ^{:key i}
         [:li.red (str common/dot-char " " msg)])
       messages))]))

(defn base-input [attrs]
  [:div.m-b-10
   [:div.f-s-10.t-a-l.m-l-10 (:placeholder attrs)]
   [:div.flex.p-l-10.p-l-10.p-r-10
    [:input.flex-grow-1
     (merge
      attrs
      {:auto-complete :off})]]])

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
         :on-focus (fn [_] (reset! blurred? false))
         :on-change on-change
         :on-blur (fn [e] (reset! blurred? true))}]
       (if @blurred? (validation-messages messages))])))

(defn svg-icon [icon-name & [size]]
  (let [size (or size 32)]
    [:img
     {:class-name (str "h-" size " w-" size)
      :src (str "/image/" icon-name ".svg")}]))

(defn user-header-view []
  (let [username @(subscribe [:username])]
    [:div.flex.align-items-c
     [:div.user-icon [svg-icon "orc-head" 40 40]]
     (if username
       [:span.white.f-w-b.t-a-r
        [:span.m-r-5 username]
        [:span.orange.underline.pointer
         {:on-click (fn [] (dispatch [:logout]))}
         "LOG OUT"]]
       [:span.pointer.flex.flex-column.align-items-end
        [:span.orange.underline.f-w-b.m-l-5
         {:on-click #(dispatch [:route routes/login-page-route {:secure? true}])}
         [:span "LOGIN"]]])]))

(def header-tab-style
  {:width "85px"})

(defn header-tab [title icon on-click disabled device-type]
  (let [mobile? (= :mobile device-type)]
    [:div.white.f-w-b.f-s-14.t-a-c.p-10.header-tab.m-5
     {:on-click on-click
      :class-name (str (if disabled "disabled") " " (if (not mobile?) " w-90"))}
     [:div
      {:class-name (if disabled "opacity-2" "pointer")}
      (let [size (if mobile? 24 48)] (svg-icon icon size size))
      (if (not mobile?)
        [:div.title.uppercase title])]]))

(def social-icon-style
  {:color :white
   :font-size "20px"})

(defn social-icon [icon link]
  [:a.p-5.opacity-5.hover-opacity-full.white
   {:style social-icon-style
    :href link :target :_blank}
   [:i.fa
    {:class-name (str "fa-" icon)}]])

(defn app-header []
  (let [device-type @(subscribe [:device-type])]
    [:div#app-header.app-header.flex.flex-column.justify-cont-s-b
     [:div.app-header-bar.container
      [:div.content
       [:div.flex.align-items-c.h-100-p
        [:div.flex.justify-cont-s-b.align-items-c.w-100-p.p-l-20.p-r-20
         [:img.orcpub-logo.h-32.w-120.pointer
          {:src "/image/orcpub-logo.svg"
           :on-click #(dispatch [:route routes/default-route {:return? true}])}]
         [user-header-view]]]]]
     [:div.container
      [:div.content
       [:div.flex.justify-cont-s-b.w-100-p.align-items-end
        [:div.white.p-10
         (social-icon "facebook" "https://www.facebook.com/orcpub")
         (social-icon "twitter" "https://twitter.com/OrcPub")
         (social-icon "reddit" "https://www.reddit.com/r/orcpub/")]
        [:div.flex.m-b-5.m-r-5
         [header-tab
          "characters"
          "battle-gear"
          #(dispatch [:route routes/dnd-e5-char-list-page-route {:return? true}])
          false
          device-type]
         [header-tab
          "spells"
          "spell-book"
          (fn [])
          true
          device-type]
         [header-tab
          "monsters"
          "hydra"
          (fn [])
          true
          device-type]]]]]
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
             [:img.h-32.w-32 {:src "https://www.patreon.com/images/patreon_navigation_logo_mini_orange.png"}]]]]]]]]))

(defn legal-footer []
  [:div.m-l-15.m-b-10.m-t-10 {:style {:text-align :left}}
   [:span "© 2017 OrcPub"]
   [:a.m-l-5 {:href "/terms-of-use" :target :_blank} "Terms of Use"]
   [:a.m-l-5 {:href "/privacy-policy" :target :_blank} "Privacy Policy"]])

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
         {:src "/image/orcpub-logo.svg"
          :style {:height "25.3px"}
          :on-click #(dispatch [:route :default])}]]
       [:div.flex-grow-1 content]
       [legal-footer]]
      [:div.registration-image
       {:style {:background-image "url(/image/shutterstock_432001912.jpg)"
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

(def message-style
  {:padding "10px"
   :border-radius "5px"
   :display :flex
   :justify-content :space-between})

(defn message [message-type message-text close-event]
  [:div.pointer.f-w-b ;;.h-0.opacity-0.fade-out
   {:on-click #(dispatch close-event)}
   [:div.white
    {:style message-style
     :class-name (case message-type
                   :error "bg-red"
                   "bg-green")}
    [:span message-text]
    [:i.fa.fa-times]]])

(defn send-password-reset-page []
  (let [params (r/atom {})]
    (fn [error-message]
      (let [email (:email @params)
            bad-email? (registration/bad-email? email)]
        (registration-page
         [:div.flex.justify-cont-s-b.w-100-p
          {:style {:text-align :center
                   :flex-direction :column}}
          [:div.p-t-10
           (if error-message [:div.red.m-b-20 error-message])
           [:div.f-w-b.f-s-24.p-b-10
            "Send Password Reset Email"]
           [:div.m-b-10 "Submit your email address here and we will send you a link to reset your password."]
           [form-input
            {:title "Email"
             :key :email
             :messages (if bad-email?
                           ["Not a valid email address"]
                           [])
             :type :email
             :value email
             :on-change (partial set-value params :email)}]
           (if @(subscribe [:login-message-shown?])
             [:div.m-t-5.p-r-5.p-l-5
              [message
               :error
               @(subscribe [:login-message])
               [:hide-login-message]]])
           [:button.form-button.m-t-10
            {:style {:height "40px"
                     :width "174px"
                     :font-size "16px"
                     :font-weight "600"}
             :class-name (if bad-email? "disabled opacity-5 hover-no-shadow")
             :on-click #(if (not bad-email?) (dispatch [:send-password-reset @params]))}
            "SUBMIT"]]])))))

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
                        ;;:messages password-messages
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
   {:on-click #(dispatch [:route routes/login-page-route {:secure? true}])}
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
   [:div
    [:span "We sent a verification email to "]
    [:span.f-w-b.red.f-s-18 @(subscribe [:temp-email])]
    [:span ". You must verify to complete registration and the link we sent will only be valid for 24 hours."]]))

(defn password-reset-sent []
  (email-sent
   (str "We sent an email to "
        @(subscribe [:temp-email])
        " with a link to reset your password.")))

(defn register-form []
  (let [registration-validation @(subscribe [:registration-validation])
        registration-form @(subscribe [:registration-form])
        send-updates? (not= false (:send-updates? registration-form))
        password-strength (registration/password-strength (:password registration-form))]
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
       [form-input {:title "Verify Email"
                    :key :verify-email
                    :value (:verify-email registration-form)
                    :messages (:verify-email registration-validation)
                    :type :email
                    :on-change (fn [e] (dispatch [:registration-verify-email (event-value e)]))}]
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
       (let [[color text]
              (cond
                (= 5 password-strength) ["bg-green" "Strong"]
                (< 1 password-strength 5) ["bg-orange" "Moderate"]
                :else ["bg-red" "Weak"])]
         [:div.p-r-10.p-l-10.p-t-5
          [:div
           {:style {:position :relative
                    :height "30px"}}
           [:div.b-rad-5
            {:style {:top 0
                     :left 0
                     :height "30px"
                     :opacity "0.7"
                     :width "100%"
                     :position :absolute}
             :class-name color}]
           [:div.b-rad-5.password-strength-meter
            {:style {:top 0
                     :left 0
                     :position :absolute
                     :height "30px"
                     :transition "width 1s"
                     :width (str (* 100 (float (/ password-strength 5))) "%")}
             :class-name color}]
           [:div.white.p-l-10.b-rad-5
            {:style {:position :absolute
                     :padding-top "6px"}}
             [:span "Password Strength:"]
             [:span.f-w-b.m-l-5 text]]]])
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
        [:a.m-l-5 {:href "/terms-of-use" :target :_blank
                   :style {:color text-color}} "Terms of Use"]
        [:span.m-l-5 "and that you've read our"]
        [:a.m-l-5 {:href "/privacy-policy" :target :_blank
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
         (if @(subscribe [:login-message-shown?])
           [:div.m-t-5.p-r-5.p-l-5 [message
             :error
             @(subscribe [:login-message])
             [:hide-login-message]]])
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
            {:on-click #(dispatch [:route routes/register-page-route {:secure true}])}
            "REGISTER NOW"]]
          [:div.m-t-20
           [:span "Forgot your password? "]
           [:span.orange.underline.pointer
            {:on-click #(dispatch [:route routes/send-password-reset-page-route {:secure? true}])}
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
  (let [device-type @(subscribe [:device-type])]
    [:div.w-100-p
     [:div.flex.align-items-c.justify-cont-s-b.flex-wrap
      [:h1.f-s-36.f-w-b.m-t-5.m-l-10
       {:class-name (if (not= :mobile device-type) "m-t-21 m-b-20")}
       title]
      [:div.flex.align-items-c.justify-cont-end.flex-wrap.m-r-10.m-l-10
       (map-indexed
        (fn [i {:keys [title icon on-click style]}]
          ^{:key i}
          [:button.form-button.h-40.m-l-5.m-t-5.m-b-5
           {:on-click on-click
            :style style}
           [:span
            [:i.fa.f-s-18
             (if icon {:class-name (str "fa-" icon)})]]
           [:span.m-l-5.header-button-text title]])
        button-cfgs)]]
     (if @(subscribe [:confirmation-shown?])
       [:div.flex.justify-cont-end.m-r-10.m-b-20.m-l-10
        (let [cfg @(subscribe [:confirmation-cfg])]
          [:div
           [:div.f-w-b (:question cfg)]
           [:div.flex.justify-cont-end.m-t-5
            [:button.form-button
             {:on-click #(dispatch [:hide-confirmation])}
             "CANCEL"]
            [:button.link-button.underline.f-w-b
             {:on-click #(dispatch [:confirm (:event cfg)])}
             (:confirm-button-text cfg)]]])])
     (if @(subscribe [:message-shown?])
       [:div.p-b-10.p-r-10.p-l-10
        [message
         @(subscribe [:message-type])
         @(subscribe [:message])
         [:hide-message]]])]))

(def debug-data-style {:width "400px" :height "400px"})

(defn debug-data []
  (let [expanded? (r/atom false)]
    (fn []
      [:div.t-a-r
       [:div.orange.pointer.underline
        {:on-click #(swap! expanded? not)}
        [:i.fa.fa-bug {:class-name (if @expanded? "white")}]]
       (if @expanded?
         [:textarea.m-t-5
          {:read-only true
           :style debug-data-style
           :value (str {:browser (user-agent/browser)
                        :browser-version (user-agent/browser-version)
                        :device-type (user-agent/device-type)
                        :platform (user-agent/platform)
                        :platform-version (user-agent/platform-version)
                        :character @(subscribe [:character])})}])])))


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
      #_(if (not @(subscribe [:warning-hidden]))
        [:div.container
         [:div.flex.align-items-c.justify-cont-s-b.white.bg-light.b-rad-5.p-10.f-w-b.m-l-5.m-r-5.m-b-5.pointer.content
          {:on-click #(dispatch [:hide-warning])}
          [:div "This application is not yet officially released and is under heavy development. We welcome you to try the application and report feedback and bugs " [:a {:href "https://muut.com/orcpub" :target :_blank} "here"] " or by emailing " [:a {:href "mailto:redorc@orcpub.com"} "redorc@orcpub.com"] ". Please understand, however, that, until official release we may have to make changes that might cause you to lose some data you enter here."]
          [:i.fa.fa-times]]])
      [:div#app-main.container
       [:div.content.w-100-p content]]
      [:div.white.flex.justify-cont-c
       [:div.content.f-w-n.f-s-12
        [:div.flex.justify-cont-s-b.align-items-c.w-100-p.flex-wrap
         [:div.p-10
          [:div.m-b-5 "Icons made by Lorc, Caduceus, and Delapouite. Available on " [:a.orange {:href "http://game-icons.net"} "http://game-icons.net"]]]
         [:div.m-l-10
          [:a {:href "https://muut.com/orcpub" :target :_blank} "Feedback/Bug Reports"]]
         [:div.m-l-10.m-r-10
          [:a {:href "/privacy-policy" :target :_blank} "Privacy Policy"]
          [:a.m-l-5 {:href "/terms-of-use" :target :_blank} "Terms of Use"]]]
        [debug-data]]]])])

(def row-style
  {:border-bottom "1px solid rgba(255,255,255,0.5)"})

(def list-style
  {:border-top "1px solid rgba(255,255,255,0.5)"})

(def thumbnail-style
  {:height "100px"
   :max-width "200px"})

(defn character-summary [id & [include-name?]]
  (let [character-name @(subscribe [::char/character-name id])
        image-url @(subscribe [::char/image-url id])
        race @(subscribe [::char/race id])
        subrace @(subscribe [::char/subrace id])
        levels @(subscribe [::char/levels id])
        classes @(subscribe [::char/classes id])]
    [:div.flex.align-items-c
     (if image-url
       [:img.m-r-20.m-t-10.m-b-10 {:src image-url
                                   :style thumbnail-style}])
     [:div.flex.character-summary
      (if (and character-name include-name?) [:span.m-r-20.m-b-5 character-name])
      [:span.m-r-10.m-b-5
       [:span race]
       [:div.f-s-12.m-t-5.opacity-6 subrace]]
      (if (seq levels)
        [:span.flex
         (map-indexed
          (fn [i v]
            (with-meta v {:key i}))
          (interpose
           [:span.m-l-5.m-r-5 "/"]
           (map
            (fn [cls-key]
              (let [{:keys [class-name class-level subclass subclass-name]} (levels cls-key)]
                [:span
                 [:span (str class-name " (" class-level ")")]
                 [:div.f-s-12.m-t-5.opacity-6 (if subclass-name subclass-name)]]))
            classes)))])]]))

(defn realize-char [built-char]
  (reduce-kv
   (fn [m k v]
     (let [realized-value (es/entity-val built-char k)]
       (if (fn? realized-value)
         m
         (assoc m k realized-value))))
   (sorted-map)
   built-char))

(def summary-style
  {:padding "33px 0"})

(defn display-section [title icon-name value & [list?]]
  [:div.m-t-20
   [:div.flex.align-items-c
    (if icon-name (svg-icon icon-name))
    [:span.m-l-5.f-s-16.f-w-600 title]]
   [:div {:class-name (if list? "m-t-0" "m-t-4")}
    [:span.f-s-24.f-w-600
     value]]])

(defn list-display-section [title image-name values]
  (if (seq values)
    (display-section
     title
     image-name
     [:span.m-t-5.f-s-14.f-w-n.i
      (s/join
       ", "
       values)]
     true)))

(defn svg-icon-section [title icon-name content]
  [:div.m-t-20
   [:span.f-s-16.f-w-600 title]
   [:div.flex.align-items-c
    (svg-icon icon-name)
    [:div.f-s-24.m-l-10.f-w-b content]]])

(defn armor-class-section [armor-class armor-class-with-armor equipped-armor]
  (let [equipped-armor-full (mi/equipped-armor-details equipped-armor)
        shields (filter #(= :shield (:type %)) equipped-armor-full)
        armor (filter #(not= :shield (:type %)) equipped-armor-full)
        display-rows (for [a (conj armor nil)
                           shield (conj shields nil)]
                       (let [el (if (= nil a shield) :span :div)]
                         ^{:key (common/name-to-kw (str (:name a) (:name shield)))}
                        [el
                         [el
                          [:span.f-s-24.f-w-b (armor-class-with-armor a shield)]
                          [:span.display-section-qualifier-text (str "("
                                                                     (if a (:name a) "unarmored")
                                                                     (if shield (str " & " (:name shield)))
                                                                     ")")]]]))]
    (svg-icon-section
     "Armor Class"
     "checked-shield"
     [:span
       (first display-rows)
       [:div
        (doall (rest display-rows))]])))

(defn speed-section [built-char all-armor]
  (let [speed (char/base-land-speed built-char)
        speed-with-armor (char/land-speed-with-armor built-char)
        unarmored-speed-bonus (char/unarmored-speed-bonus built-char)
        equipped-armor (char/normal-armor-inventory built-char)]
    [svg-icon-section
     "Speed"
     "walking-boot"
     [:span
      [:span
       [:span (+ (or unarmored-speed-bonus 0)
                 (if speed-with-armor
                   (speed-with-armor nil)
                   speed))]
       (if (or unarmored-speed-bonus
               speed-with-armor)
         [:span.display-section-qualifier-text "(unarmored)"])]
      (if speed-with-armor
        [:div
         (doall
          (map
           (fn [[armor-kw _]]
             (let [armor (mi/all-armor-map armor-kw)
                   speed (speed-with-armor armor)]
               ^{:key armor-kw}
               [:div
                [:div
                 [:span speed]
                 [:span.display-section-qualifier-text (str "(" (:name armor) " armor)")]]]))
           (dissoc all-armor :shield)))]
        (if unarmored-speed-bonus
          [:div
           [:span
            [:span speed]
            [:span.display-section-qualifier-text "(armored)"]]]))
      (let [swim-speed (char/base-swimming-speed built-char)]
        (if swim-speed
          [:div [:span swim-speed] [:span.display-section-qualifier-text "(swim)"]]))
      (let [flying-speed (char/base-flying-speed built-char)]
        (if flying-speed
          [:div [:span flying-speed] [:span.display-section-qualifier-text "(fly)"]]))]]))

(defn list-item-section [list-name icon-name items & [name-fn]]
  [list-display-section list-name icon-name
   (map
    (fn [item]
      ((or name-fn :name) item))
    items)])

(defn compare-spell [spell-1 spell-2]
  (let [key-fn (juxt :key :ability)]
    (compare (key-fn spell-1) (key-fn spell-2))))

(defn spells-known-section [spells-known spell-slots spell-modifiers]
  [display-section "Spells" "spell-book"
   [:div
    [:div.f-s-14
     [:span.f-w-b "Slots: "]
     [:span.f-w-n (s/join ", " (map (fn [[level slots]] (str level " - " slots)) spell-slots))]]
    [:div.f-s-14
     [:span.f-w-b "Spell Save DC: "]
     [:span.f-w-n (s/join ", " (map (fn [[class {:keys [spell-save-dc ability]}]]
                                      (str (if spell-save-dc spell-save-dc)
                                           (if (and ability (-> spell-modifiers count (> 1)))
                                             (str " (" class ", " (s/upper-case (name ability)) ")"))))
                                    spell-modifiers))]]
    [:div.f-s-14
     [:span.f-w-b "Spell Attack Bonus: "]
     [:span.f-w-n (s/join ", " (map (fn [[class {:keys [spell-attack-modifier ability]}]]
                                      (str (if spell-attack-modifier
                                             (common/bonus-str spell-attack-modifier))
                                           (if (and ability (-> spell-modifiers count (> 1)))
                                             (str " (" class ", " (s/upper-case (name ability)) ")"))))
                                    spell-modifiers))]]
    [:div.f-s-14.flex.flex-wrap
     (doall
      (map
       (fn [[level spells]]
         ^{:key level}
         [:div.m-t-10.w-200
          [:span.f-w-b (str (if (zero? level) "Cantrip" (str "Level " level)))]
          [:div.i.f-w-n
           (doall
            (map-indexed
             (fn [i spell]
               (let [spell-data (spells/spell-map (:key spell))]
                 ^{:key i}
                 [:div
                  (str
                   (:name (spells/spell-map (:key spell)))
                    (if (or (:ability spell)
                            (:qualifier spell))
                      (str
                       " ("
                       (s/join
                        ", "
                        (remove
                         nil?
                         [(if (:ability spell) (s/upper-case (name (:ability spell))))
                          (if (:qualifier spell) (:qualifier spell))]))
                    
                       ")")))]))
             (into
              (sorted-set-by compare-spell)
              (filter
               (fn [{k :key}]
                 (spells/spell-map k))
               spells))))]])
       (filter
        (comp seq second)
        spells-known)))]]])

(defn equipment-section [title icon-name equipment equipment-map]
  [list-display-section title icon-name
   (map
    (fn [[equipment-kw {item-qty ::char-equip/quantity
                        equipped? ::char-equip/equipped?
                        :as num}]]
      (str (disp/equipment-name equipment-map equipment-kw)
           " (" (or item-qty num) ")"))
    equipment)])

(defn add-links [desc]
  desc
  (let [{:keys [abbr url]} (some (fn [[_ source]]
                                   (if (and (:abbr source)
                                            (re-matches (re-pattern (str ".*" (:abbr source) ".*")) desc))
                             source))
                 disp/sources)
        [before after] (if abbr (s/split desc (re-pattern abbr)))]
    (if abbr
      [:span
       [:span before]
       [:a {:href url :target :_blank} abbr]
       [:span after]]
      desc)))

(defn attacks-section [attacks]
  (if (seq attacks)
    (display-section
     "Attacks"
     "pointy-sword"
     [:div.f-s-14
      (doall
       (map
        (fn [{:keys [name area-type description damage-die damage-die-count damage-type save save-dc] :as attack}]
          ^{:key name}
          [:p.m-t-10
           [:span.f-w-600.i name "."]
           [:span.f-w-n.m-l-10 (add-links (common/sentensize (disp/attack-description attack)))]])
        attacks))])))

(defn actions-section [title icon-name actions]
  (if (seq actions)
    (display-section
     title icon-name
     [:div.f-s-14
      (doall
       (map
        (fn [action]
          ^{:key action}
          [:p.m-t-10
           [:span.f-w-600.i (:name action) "."]
           [:span.f-w-n.m-l-10 (add-links (common/sentensize (disp/action-description action)))]])
        (sort-by :name actions)))])))

(defn prof-name [prof-map prof-kw]
  (or (-> prof-kw prof-map :name) (common/kw-to-name prof-kw)))

(defn resistance-str [{:keys [value qualifier]}]
  (str (name value)
       (if qualifier (str " (" qualifier ")"))))

(def no-https-images "Sorry, we don't currently support images that start with https")

(defn default-image [race classes]
  (if (and (or (= "Human" race)
               (nil? race))
           (= :barbarian (first classes)))
    "/image/barbarian.png"))

(defn section-header-2 [title icon]
  [:div
   (if icon (svg-icon icon 24 24))
   [:div.f-s-18.f-w-b.m-b-5 title]])

(defn armor-class-section-2 [id]
  (let [unarmored-armor-class @(subscribe [::char/armor-class id])
        ac-with-armor-fn @(subscribe [::char/armor-class-with-armor id])
        all-armor-inventory (mi/equipped-armor-details @(subscribe [::char/all-armor-inventory id]))
        equipped-armor (armor/non-shields all-armor-inventory)
        equipped-shields (armor/shields all-armor-inventory)]
    [:div
     [:div.p-10.flex.flex-column.align-items-c
      (section-header-2 "Armor Class" "checked-shield")
      [:div.f-s-24.f-w-b (char/max-armor-class unarmored-armor-class
                                               ac-with-armor-fn
                                               all-armor-inventory
                                               equipped-armor
                                               equipped-shields)]]]))

(defn basic-section [title icon v]
  [:div
   [:div.p-10.flex.flex-column.align-items-c
    (section-header-2 title icon)
    [:div.f-s-24.f-w-b v]]])

(defn hit-points-section-2 [id]
  (basic-section "Max Hit Points" "health-normal" @(subscribe [::char/max-hit-points id])))

(defn initiative-section-2 [id]
  (basic-section "Initiative" "sprint" (common/bonus-str @(subscribe [::char/initiative id]))))

(defn darkvision-section-2 [id]
  (basic-section "Darkvision" "night-vision" (str @(subscribe [::char/darkvision id]) " ft.")))

(defn critical-hits-section-2 [id]
  (let [critical-hit-values @(subscribe [::char/critical-hit-values])]
    (basic-section "Critical Hits" nil (str (apply min critical-hit-values)
                                            "-"
                                            (apply max critical-hit-values)))))

(defn number-of-attacks-section-2 [id]
  (basic-section "Number of Attacks" nil @(subscribe [::char/number-of-attacks])))

(defn passive-perception-section-2 [id]
  (basic-section "Passive Perception" "awareness" @(subscribe [::char/passive-perception id])))

(defn skills-section-2 [id]
  (let [skill-profs (or @(subscribe [::char/skill-profs id]) #{})
        skill-bonuses @(subscribe [::char/skill-bonuses id])]
    [:div
     [passive-perception-section-2 id]
     [:div.p-10.flex.flex-column.align-items-c
      (section-header-2 "Skills" "juggler")
      [:table
       [:tbody
        (doall
         (map
          (fn [{skill-name :name skill-key :key icon :icon :as skill}]
            ^{:key skill-key}
            [:tr.t-a-l
             {:class-name (if (skill-profs skill-key) "f-w-b" "opacity-7")}
             [:td [:div
                   (svg-icon icon 18 18)
                   [:span.m-l-5 skill-name]]]
             [:td [:div.p-5 (common/bonus-str (skill-bonuses skill-key))]]])
          skills/skills))]]]]))

(defn ability-scores-section-2 [id]
  (let [abilities @(subscribe [::char/abilities id])
        ability-bonuses @(subscribe [::char/ability-bonuses id])]
    [:div
     [:div.f-s-18.f-w-b "Ability Scores"]
     [:div.flex.justify-cont-s-a.m-t-10
      (doall
       (map
        (fn [k]
          ^{:key k}
          [:div
           (t/ability-icon k 24)
           [:div
            [:span.f-s-20.uppercase (name k)]]
           [:div.f-s-24.f-w-b (abilities k)]
           [:div.f-s-12.opacity-5.m-b--2.m-t-2 "mod"]
           [:div.f-s-18 (common/bonus-str (ability-bonuses k))]])
        char/ability-keys))]]))

(defn saving-throws-section-2 [id]
  (let [save-bonuses @(subscribe [::char/save-bonuses id])
        saving-throws @(subscribe [::char/saving-throws id])]
    [:div.p-10.flex.flex-column.align-items-c
     (section-header-2 "Saving Throws" "dodging")
     [:table
      [:tbody
       (doall
        (map
         (fn [k]
           ^{:key k}
           [:tr.t-a-l
            {:class-name (if (saving-throws k) "f-w-b" "opacity-7")}
            [:td [:div
                  (t/ability-icon k 18)
                  [:span.m-l-5 (s/upper-case (name k))]]]
            [:td [:div.p-5 (common/bonus-str (save-bonuses k))]]])
         char/ability-keys))]]]))

(defn feet-str [num]
  (str num " ft."))

(defn speed-section-2 [id]
  (let [speed @(subscribe [::char/base-land-speed id])
        swim-speed @(subscribe [::char/base-swimming-speed id])
        flying-speed @(subscribe [::char/base-flying-speed id])
        speed-with-armor @(subscribe [::char/speed-with-armor id])
        unarmored-speed-bonus @(subscribe [::char/unarmored-speed-bonus id])
        equipped-armor @(subscribe [::char/armor id])
        all-armor @(subscribe [::char/all-armor-inventory id])]
    [:div.p-10
     (section-header-2 "Speed" "walking-boot")
     [:span.f-s-24.f-w-b
      [:span
       [:span (feet-str (+ (or unarmored-speed-bonus 0)
                      (if speed-with-armor
                        (speed-with-armor nil)
                        speed)))]
       (if (or unarmored-speed-bonus
               speed-with-armor)
         [:span.display-section-qualifier-text "(unarmored)"])]
      (if speed-with-armor
        [:div.f-s-18
         (doall
          (map
           (fn [[armor-kw _]]
             (let [armor (mi/all-armor-map armor-kw)
                   speed (speed-with-armor armor)]
               ^{:key armor-kw}
               [:div
                [:div
                 [:span (feet-str speed)]
                 [:span.display-section-qualifier-text (str "(" (:name armor) " armor)")]]]))
           (dissoc all-armor :shield)))]
        (if unarmored-speed-bonus
          [:div.f-s-18
           [:span
            [:span (feet-str speed)]
            [:span.display-section-qualifier-text "(armored)"]]]))
      (if swim-speed
        [:div.f-s-18 [:span (feet-str swim-speed)] [:span.display-section-qualifier-text "(swim)"]])
      (if flying-speed
        [:div.f-s-18 [:span (feet-str flying-speed)] [:span.display-section-qualifier-text "(fly)"]])]]))

(defn personality-section [title & descriptions]
  (if (and (seq descriptions)
           (some (complement s/blank?) descriptions))
    [:div.m-t-20.t-a-l
     [:div.f-w-b.f-s-18 title]
     [:div
      (doall
       (map-indexed
        (fn [i description]
          ^{:key i}
          [:div
           (doall
            (map-indexed
             (fn [j p]
               ^{:key j}
               [:p p])
             (s/split
              description
              #"\n")))])
        descriptions))]]))

(defn description-section [id]
  (let [personality-trait-1 @(subscribe [::char/personality-trait-1 id])
        personality-trait-2 @(subscribe [::char/personality-trait-2 id])
        ideals @(subscribe [::char/ideals id])
        bonds @(subscribe [::char/bonds id])
        flaws @(subscribe [::char/flaws id])
        description @(subscribe [::char/description id])]
    [:div.p-5
     (personality-section "Personality Traits" personality-trait-1 personality-trait-2)
     (personality-section "Ideals" ideals)
     (personality-section "Bonds" bonds)
     (personality-section "Flaws" flaws)
     (personality-section "Description" description)]))

(defn summary-details [num-columns id]
  (let [built-char @(subscribe [:built-character id])
        race @(subscribe [::char/race id])
        classes @(subscribe [::char/classes id])
        background @(subscribe [::char/background id])
        alignment @(subscribe [::char/alignment id])
        all-armor @(subscribe [::char/all-armor id])
        image-url-failed @(subscribe [::char/image-url-failed id])
        image-url @(subscribe [::char/image-url id])
        faction-image-url @(subscribe [::char/faction-image-url id])
        faction-image-url-failed @(subscribe [::char/faction-image-url-failed id])
        armor-class @(subscribe [::char/armor-class id])
        armor-class-with-armor @(subscribe [::char/armor-class-with-armor id])]
    [:div
     #_{:class-name (if (= 2 num-columns) "flex")}
     [:div
      #_{:class-name (if (= 2 num-columns) "w-50-p")}
      [:div.w-100-p.t-a-c
       [:div
        [ability-scores-section-2 id]
        [:div.flex.p-10.justify-cont-s-a
         [skills-section-2 id]
         [:div
          [armor-class-section-2 id]
          [hit-points-section-2 id]
          [speed-section-2 id]
          [saving-throws-section-2 id]
          [darkvision-section-2 id]]]
        [description-section id]]]]]))

(defn weapon-details-field [nm value]
  [:div.p-2
   [:span.f-w-b nm ":"]
   [:span.m-l-5 value]])

(defn yes-no [v]
  (if v "yes" "no"))

(defn weapon-details [{:keys [description
                              type
                              damage-type
                              magical-damage-bonus
                              ranged?
                              melee?
                              range
                              two-handed?
                              finesse?
                              link
                              versatile
                              thrown]}]
  [:div.m-t-10.i
   (weapon-details-field "Type" (common/safe-name type))
   (weapon-details-field "Damage Type" (common/safe-name damage-type))
   (weapon-details-field "Melee/Ranged" (if melee? "melee" "ranged"))
   (if range
     (weapon-details-field "Range" (str (:min range) "/" (:max range) " ft.")))
   (weapon-details-field "Finesse?" (yes-no finesse?))
   (weapon-details-field "Two-handed?" (yes-no two-handed?))
   (weapon-details-field "Versatile" (if versatile
                                       (str (:damage-die-count versatile)
                                            "d"
                                            (:damage-die versatile)
                                            (if magical-damage-bonus
                                              (common/mod-str magical-damage-bonus))
                                            " damage")
                                       "no"))
   (if description
     [:div.m-t-10 description])])

(defn armor-details-section [{:keys [type
                                     base-ac
                                     weight
                                     description
                                     max-dex-mod
                                     min-str
                                     magical-ac-bonus
                                     stealth-disadvantage?]
                              :or {magical-ac-bonus 0
                                   base-ac 10}}
                             {shield-magic-bonus :magical-ac-bonus :or {shield-magic-bonus 0} :as shield}
                             expanded?]
  [:div
   [:div (str (if type (str (common/safe-name type) ", ")) "base AC " (+ magical-ac-bonus shield-magic-bonus base-ac (if shield 2 0)) (if stealth-disadvantage? ", stealth disadvantage"))]
   (if expanded?
     [:div
      [:div.m-t-10.i
       (if type
         (weapon-details-field "Type" (common/safe-name type)))
       (weapon-details-field "Base AC" base-ac)
       (if (not= magical-ac-bonus 0)
         (weapon-details-field "Magical AC Bonus" magical-ac-bonus))
       (if shield
         (weapon-details-field "Shield Base AC Bonus" 2))
       (if (and shield
                (not= shield-magic-bonus 0))
         (weapon-details-field "Shield Magical AC Bonus" shield-magic-bonus))
       (if max-dex-mod
         (weapon-details-field "Max DEX AC Bonus" max-dex-mod))
       (if min-str
         (weapon-details-field "Min Strength" min-str))
       (weapon-details-field "Stealth Disadvantage?" (yes-no stealth-disadvantage?))
       (if weight
         (weapon-details-field "Weight" (str weight " lbs.")))
       (if description
         [:div.m-t-10 (str "Armor: " description)])
       (if (:description shield)
         [:div.m-t-10 (str "Shield: " (:description shield))])]])])

(defn boolean-icon [v]
  [:i.fa {:class-name (if v "fa-check green" "fa-times red")}])

(defn armor-section-2 []
  (let [expanded-details (r/atom {})]
    (fn [id]
      (let [all-armor @(subscribe [::char/all-armor id])
            ac-with-armor @(subscribe [::char/armor-class-with-armor id])
            armor-profs (set @(subscribe [::char/armor-profs id]))
            device-type @(subscribe [:device-type])
            mobile? (= :mobile device-type)
            proficiency-bonus @(subscribe [::char/proficiency-bonus id])
            all-armor-details (map mi/all-armor-map (keys all-armor))
            armor-details (armor/non-shields all-armor-details)
            shield-details (armor/shields all-armor-details)]
        [:div
         [:div.flex.align-items-c
          (svg-icon "breastplate" 32 32)
          [:span.m-l-5.f-w-b.f-s-18 "Armor"]]
         [:div
          [:table.w-100-p.t-a-l.striped
           [:tbody
            [:tr.f-w-b
             {:class-name (if mobile? "f-s-12")}
             [:th.p-10 "Name"]
             (if (not mobile?) [:th.p-10 "Proficient?"])
             [:th.p-10 "Details"]
             [:th]
             [:th.p-10 "AC"]]
            (doall
             (for [{:keys [name description type key] :as armor} (conj armor-details nil)
                   shield (conj shield-details nil)]
               (let [k (str key (:key shield))
                     ac (ac-with-armor armor shield)
                     proficient? (and
                                  (or (nil? shield)
                                      (armor-profs :shields))
                                  (or
                                   (nil? armor)
                                   (armor-profs key)
                                   (armor-profs type)))
                     expanded? (@expanded-details k)]
                 ^{:key (str key (:key shield))}
                 [:tr.pointer
                  {:on-click #(swap! expanded-details (fn [d] (update d k not)))}
                  [:td.p-10.f-w-b (str (or (:name armor) "unarmored")
                                       (if shield (str " + " (:name shield))))]
                  (if (not mobile?)
                    [:td.p-10 (boolean-icon proficient?)])
                  [:td.p-10.w-100-p
                   [:div
                    (armor-details-section armor shield expanded?)]]
                  [:td
                   [:div.orange
                    (if (not mobile?)
                      [:span.underline (if expanded? "less" "more")])
                    [:i.fa.m-l-5
                     {:class-name (if expanded? "fa-caret-up" "fa-caret-down")}]]]
                  [:td.p-10.f-w-b.f-s-18 ac]])))]]]]))))

(defn weapons-section-2 []
  (let [expanded-details (r/atom {})]
    (fn [id]
      (let [all-weapons @(subscribe [::char/all-weapons id])
            weapon-profs (set @(subscribe [::char/weapon-profs id]))
            weapon-attack-modifier @(subscribe [::char/weapon-attack-modifier-fn id])
            has-weapon-prof @(subscribe [::char/has-weapon-prof id])
            device-type @(subscribe [:device-type])
            mobile? (= :mobile device-type)
            proficiency-bonus @(subscribe [::char/proficiency-bonus id])]
        [:div
         [:div.flex.align-items-c
          (svg-icon "crossed-swords" 32 32)
          [:span.m-l-5.f-w-b.f-s-18 "Weapons"]]
         [:div
          [:table.w-100-p.t-a-l.striped
           [:tbody
            [:tr.f-w-b
             {:class-name (if mobile? "f-s-12")}
             [:th.p-10 "Name"]
             (if (not mobile?) [:th.p-10 "Proficient?"])
             [:th.p-10 "Details"]
             [:th]
             [:th.p-10 (if mobile? "Atk" [:div.w-40 "Attack Bonus"])]]
            (doall
             (map
              (fn [[weapon-key {:keys [equipped?]}]]
                (let [{:keys [name magical-damage-bonus description ranged?] :as weapon} (mi/all-weapons-map weapon-key)
                      proficient? (has-weapon-prof weapon)
                      expanded? (@expanded-details weapon-key)]
                  ^{:key weapon-key}
                  [:tr.pointer
                   {:on-click #(swap! expanded-details (fn [d] (update d weapon-key not)))}
                   [:td.p-10.f-w-b (:name weapon)]
                   (if (not mobile?)
                     [:td.p-10 (boolean-icon proficient?)])
                   [:td.p-10.w-100-p
                    [:div
                     (disp/attack-description (-> weapon
                                                  (assoc :attack-type (if ranged? :ranged :melee))
                                                  (assoc :damage-modifier magical-damage-bonus)
                                                  (dissoc :description)))]
                    (if expanded?
                      (weapon-details weapon))]
                   [:td
                    [:div.orange
                     (if (not mobile?)
                       [:span.underline (if expanded? "less" "more")])
                     [:i.fa.m-l-5
                      {:class-name (if expanded? "fa-caret-up" "fa-caret-down")}]]]
                   [:td.p-10.f-w-b.f-s-18 (common/bonus-str (max (weapon-attack-modifier weapon true)
                                                                 (weapon-attack-modifier weapon false)))]]))
              all-weapons))]]]]))))

(defn skill-details-section-2 []
  (let [expanded-details (r/atom {})]
    (fn [id]
      (let [skill-profs (or @(subscribe [::char/skill-profs id]) #{})
            skill-bonuses @(subscribe [::char/skill-bonuses id])
            skill-expertise @(subscribe [::char/skill-expertise id])
            device-type @(subscribe [:device-type])
            mobile? (= :mobile device-type)]
        [:div
         [:div.flex.align-items-c
          (svg-icon "juggler" 32 32)
          [:span.m-l-5.f-w-b.f-s-18 "Skills"]]
         [:div
          [:table.w-100-p.t-a-l.striped
           [:tbody
            [:tr.f-w-b
             {:class-name (if mobile? "f-s-12")}
             [:th.p-10 "Name"]
             [:td.p-10 (if mobile? "Prof?" "Proficient?")]
             (if skill-expertise
               [:th.p-10 "Expertise?"])
             [:th.p-10 (if (not mobile?) [:div.w-40 "Bonus"])]]
            (doall
             (map
              (fn [{:keys [key name]}]
                (let [proficient? (key skill-profs)
                      expertise? (key skill-expertise)]
                  ^{:key key}
                  [:tr
                   [:td.p-10.f-w-b name]
                   [:td.p-10 (boolean-icon proficient?)]
                   (if skill-expertise
                     [:td.p-10 (boolean-icon expertise?)])
                   [:td.p-10.f-s-18.f-w-b (common/bonus-str (key skill-bonuses))]]))
              skills/skills))]]]]))))

(defn tool-prof-details-section-2 []
  (let [expanded-details (r/atom {})]
    (fn [id]
      (let [tool-profs (or @(subscribe [::char/tool-profs id]) #{})
            tool-expertise @(subscribe [::char/tool-expertise id])
            tool-bonus-fn @(subscribe [::char/tool-bonus-fn id])
            device-type @(subscribe [:device-type])
            mobile? (= :mobile device-type)]
        (if (seq tool-profs)
          [:div
           [:div.flex.align-items-c
            (svg-icon "stone-crafting" 32 32)
            [:span.m-l-5.f-w-b.f-s-18 "Tools"]]
           [:div
            [:table.w-100-p.t-a-l.striped
             [:tbody
              [:tr.f-w-b
               {:class-name (if mobile? "f-s-12")}
               [:th.p-10 "Name"]
               [:td.p-10 (if mobile? "Prof?" "Proficient?")]
               (if tool-expertise
                 [:th.p-10 "Expertise?"])
               [:th.p-10 (if (not mobile?) [:div.w-40 "Bonus"])]]
              (doall
               (map
                (fn [kw]
                  (let [name (-> equip/tools-map kw :name)
                        proficient? (kw tool-profs)
                        expertise? (kw tool-expertise)]
                    ^{:key kw}
                    [:tr
                     [:td.p-10.f-w-b name]
                     [:td.p-10 (boolean-icon proficient?)]
                     (if tool-expertise
                       [:td.p-10 (boolean-icon expertise?)])
                     [:td.p-10.f-s-18.f-w-b (common/bonus-str (tool-bonus-fn kw))]]))
                tool-profs))]]]])))))


(defn proficiency-details [num-columns id]
  (let [ability-bonuses @(subscribe [::char/ability-bonuses id])]
    [:div.details-columns
     {:class-name (if (= 2 num-columns) "flex")}
     [:div.flex-grow-1.details-column-2
      {:class-name (if (= 2 num-columns) "w-50-p m-l-20")}
      [skill-details-section-2 id]
      [:div.m-t-20
       [tool-prof-details-section-2 id]]
      [list-item-section "Languages" "lips" @(subscribe [::char/languages id]) (partial prof-name opt/language-map)]
      [list-item-section "Tool Proficiencies" "stone-crafting" @(subscribe [::char/tool-profs id]) (partial prof-name equip/tools-map)]
      [list-item-section "Weapon Proficiencies" "bowman" @(subscribe [::char/weapon-profs id]) (partial prof-name weapon/weapons-map)]
      [list-item-section "Armor Proficiencies" "mailed-fist" @(subscribe [::char/armor-profs id]) (partial prof-name armor/armor-map)]]]))

(defn combat-details [num-columns id]
  (let [weapon-profs @(subscribe [::char/weapon-profs id])
        armor-profs @(subscribe [::char/armor-profs id])
        resistances @(subscribe [::char/resistances id])
        damage-immunities @(subscribe [::char/damage-immunities id])
        condition-immunities @(subscribe [::char/condition-immunities id])
        immunities @(subscribe [::char/immunities id])
        weapons @(subscribe [::char/weapons id])
        armor @(subscribe [::char/armor id])
        magic-weapons @(subscribe [::char/magic-weapons id])
        magic-armor @(subscribe [::char/magic-armor id])
        attacks @(subscribe [::char/attacks id])
        critical-hit-values @(subscribe [::char/critical-hit-values id])
        non-standard-crits? (> (count critical-hit-values) 1)
        number-of-attacks @(subscribe [::char/number-of-attacks id])
        non-standard-attack-number? (> number-of-attacks 1)]
    [:div
     [:div.flex.justify-cont-s-a.t-a-c
      [armor-class-section-2 id]
      [hit-points-section-2 id]
      [speed-section-2 id]
      [initiative-section-2 id]]
     (if (or non-standard-crits?
             non-standard-attack-number?)
       [:div.flex.justify-cont-s-a.t-a-c
        [critical-hits-section-2 id]
        [number-of-attacks-section-2 id]])
     [:div.m-t-30
      [list-item-section "Damage Resistances" "surrounded-shield" resistances resistance-str]]
     [:div.m-t-30
      [list-item-section "Damage Immunities" nil damage-immunities resistance-str]]
     [:div.m-t-30
      [list-item-section "Condition Immunities" nil condition-immunities resistance-str]]
     [:div.m-t-30
      [list-item-section "Immunities" nil immunities resistance-str]]
     [:div.m-t-30
      [attacks-section attacks]]
     [:div.m-t-30
      [weapons-section-2 id]]
     [:div.m-t-30
      [armor-section-2 id]]
     [:div
      {:class-name (if (= 2 num-columns) "w-50-p m-l-20")}
      [list-item-section "Weapon Proficiencies" "bowman" weapon-profs (partial prof-name weapon/weapons-map)]
      [list-item-section "Armor Proficiencies" "mailed-fist" armor-profs (partial prof-name armor/armor-map)]]]))

(defn features-details [num-columns id]
  (let [resistances @(subscribe [::char/resistances id])
        damage-immunities @(subscribe [::char/damage-immunities id])
        condition-immunities @(subscribe [::char/condition-immunities id])
        immunities @(subscribe [::char/immunities id])
        actions @(subscribe [::char/actions id])
        bonus-actions @(subscribe [::char/bonus-actions id])
        reactions @(subscribe [::char/reactions id])
        traits @(subscribe [::char/traits id])
        attacks @(subscribe [::char/attacks id])]
    [:div.details-columns
     {:class-name (if (= 2 num-columns) "flex")}
   
     [:div.flex-grow-1.details-column-2
      {:class-name (if (= 2 num-columns) "w-50-p m-l-20")}
      [list-item-section "Damage Resistances" "surrounded-shield" resistances resistance-str]
      [list-item-section "Damage Immunities" nil damage-immunities resistance-str]
      [list-item-section "Condition Immunities" nil condition-immunities resistance-str]
      [list-item-section "Immunities" nil immunities resistance-str]
      [attacks-section attacks]
      [actions-section "Actions" "beams-aura" actions]
      [actions-section "Bonus Actions" "run" bonus-actions]
      [actions-section "Reactions" "van-damme-split" reactions]
      [actions-section "Features, Traits, and Feats" "vitruvian-man" traits]]]))

(defn spell-details [num-columns id]
  (let [spells-known @(subscribe [::char/spells-known id])
        spell-slots @(subscribe [::char/spell-slots id])
        spell-modifiers @(subscribe [::char/spell-modifiers id])]
    [:div.details-columns
     {:class-name (if (= 2 num-columns) "flex")}
     [:div.flex-grow-1.details-column-2
      {:class-name (if (= 2 num-columns) "w-50-p m-l-20")}
      (if (seq spells-known) [spells-known-section spells-known spell-slots spell-modifiers])]]))

(defn details-tab [title icon device-type selected? on-select]
  [:div.b-b-2.f-w-b.pointer.p-10.hover-opacity-full
   {:class-name (if selected? "b-orange" "b-gray")
    :on-click on-select}
   [:div.hover-opacity-full
    {:class-name (if (not selected?) "opacity-5")}
    [:div (svg-icon icon 24 24)]
    (if (= device-type :desktop)
      [:div.uppercase
       title])]])


(def details-tabs
  {"summary" {:icon "stabbed-note"
              :view summary-details}
   "combat" {:icon "sword-clash"
             :view combat-details}
   "proficiencies" {:icon "juggler"
                    :view proficiency-details}
   "spells" {:icon "spell-book"
             :view spell-details}
   "features" {:icon "vitruvian-man"
               :view features-details}})

(defn character-display []
  (let [device-type @(subscribe [:device-type])
        selected-tab (r/atom nil)]
    (fn [id show-summary? num-columns]
      (let [two-columns? (= 2 num-columns)
            tab (if @selected-tab
                  @selected-tab
                  (if two-columns?
                    "combat"
                    "summary"))]
        [:div.w-100-p
         [:div
          (if show-summary?
            [:div.f-s-24.f-w-600.m-b-16.m-l-20.text-shadow.flex
             [character-summary id true]])
          [:div.flex.w-100-p
           (if two-columns?
             [:div.w-50-p
              [summary-details num-columns id]])
           [:div
            {:class-name (if two-columns? "w-50-p" "w-100-p")}
            [:div.flex.p-l-10.m-b-10.m-r-10
             (doall
              (map
               (fn [[title {:keys [view icon]}]]
                 ^{:key title}
                 [:div.flex-grow-1.t-a-c
                  [details-tab
                   title
                   icon
                   device-type
                   (= title tab)
                   #(reset! selected-tab title)]])
               (if two-columns?
                 (rest details-tabs)
                 details-tabs)))]
            [(-> tab details-tabs :view) num-columns id]]]]]))))

(def character-display-style
  {:padding "20px 5px"
   :background-color "rgba(0,0,0,0.15)"})

(defn character-page [{:keys [id] :as arg}]
  (let [{:keys [::se/owner] :as strict-character} @(subscribe [::char/character id])
        character (char/from-strict strict-character)
        built-template (subs/built-template (subs/selected-plugin-options character))
        built-character (subs/built-character character built-template)
        device-type @(subscribe [:device-type])
        username @(subscribe [:username])]
    [content-page
     "Character Page"
     (remove
      nil?
      [(if (= owner username)
         {:title "Edit"
          :icon "pencil"
          :on-click #(dispatch [:edit-character character])})])
     [:div.p-10.white
      [character-display id true (if (= :mobile device-type) 1 2)]]]))

(defn character-list []
  (let [characters @(subscribe [::char/characters])
        expanded-characters @(subscribe [:expanded-characters])
        device-type @(subscribe [:device-type])]
    [content-page
     "Characters"
     [{:title "New"
       :icon "plus"
       :on-click #(dispatch [:new-character])}]
     [:div.p-5
      [:div
       {:style list-style}
       (doall
        (map
         (fn [{:keys [:db/id] :as strict-character}]
           ^{:key id}
           [:div.white
            {:style row-style}
            (let [built-character @(subscribe [::char/built-character id])
                  image-url (char/image-url built-character)
                  expanded? (get expanded-characters id)]
              [:div.pointer
               [:div.flex.justify-cont-s-b.align-items-c
                {:on-click #(dispatch [:toggle-character-expanded id])}
                [:div.m-l-10.flex.align-items-c
                 [:div.f-s-24.f-w-600
                  {:style summary-style}
                  [:div.list-character-summary
                   [character-summary id true]]]]
                [:div.orange.pointer.m-r-10
                 (if (not= device-type :mobile) [:span.underline (if expanded?
                                           "collapse"
                                           "open")])
                 [:i.fa.m-l-5
                  {:class-name (if expanded? "fa-caret-up" "fa-caret-down")}]]]
               (if expanded?
                 [:div
                  {:style character-display-style}
                  [:div.flex.justify-cont-end
                   [:button.form-button
                    {:on-click #(dispatch [:edit-character @(subscribe [::char/internal-character id])])}
                    "EDIT"]
                   [:button.form-button.m-l-5
                    {:on-click #(let [route (routes/match-route (routes/path-for routes/dnd-e5-char-page-route :id id))]
                                  (dispatch [:route route {:return? true}]))}
                    "VIEW"]
                   [:button.form-button.m-l-5
                    {:on-click #(dispatch [:delete-character id])}
                    "DELETE"]]
                  [character-display id false (if (= :mobile device-type) 1 2)]])])])
         characters))]]]))


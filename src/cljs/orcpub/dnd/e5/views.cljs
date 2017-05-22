(ns orcpub.dnd.e5.views
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [reagent.core :as r]
            [orcpub.route-map :as routes]
            [orcpub.common :as common]
            [orcpub.entity-spec :as es]
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
         {:on-click #(dispatch [:route routes/login-page-route])}
         [:span "LOGIN"]]])]))

(def header-tab-style
  {:width "85px"})

(defn app-header []
  [:div#app-header.app-header.flex.flex-column.justify-cont-s-b
   [:div.app-header-bar.container
    [:div.content
     [:div.flex.align-items-c.h-100-p
      [:div.flex.justify-cont-s-b.align-items-c.w-100-p.p-l-20.p-r-20
       [:img.orcpub-logo.h-32.w-120.pointer {:src "/image/orcpub-logo.svg"}]
       [user-header-view]]]]]
   [:div.container
    [:div.content
     [:div.flex.justify-cont-end.w-100-p
      [:div.flex.m-b-5.m-r-5
       [:div.pointer.white.f-w-b.f-s-14.t-a-c.p-10.header-tab.m-5.w-85
        {:on-click #(dispatch [:route routes/dnd-e5-char-list-page-route])}
        (svg-icon "battle-gear" 48 48)
        [:div.title "CHARACTERS"]]
       [:div.white.f-w-b.f-s-14.t-a-c.p-10.header-tab.m-5.disabled.w-85
        [:div.opacity-2
         (svg-icon "spell-book" 48 48)
         [:div.title "SPELLS"]]]
       [:div.white.f-w-b.f-s-14.t-a-c.p-10.header-tab.m-5.disabled.w-85
        [:div.opacity-2
         (svg-icon "hydra" 48 48)
         [:div.title "MONSTERS"]]]]]]]
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

(defn legal-footer []
  [:div.m-l-15.m-b-10 {:style {:text-align :left}}
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
         {:src "image/orcpub-logo.svg"
          :style {:height "25.3px"}
          :on-click #(dispatch [:route :default])}]]
       [:div.flex-grow-1 content]
       [legal-footer]]
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
                    ;:messages (:password registration-validation)
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
        [:a.m-l-5 {:href "/terms-of-use" :target :_blank
                   :style {:color text-color}} "Terms of Use"]
        [:span.m-l-5 "and that you've read our"]
        [:a.m-l-5 {:href "/privacy-policy" :target :_blank
                   :style {:color text-color}} "Privacy Policy"]]]])))

(def message-style
  {:padding "10px"
   :border-radius "5px"
   :display :flex
   :justify-content :space-between})

(defn message [message-type message-text close-event]
  [:div.pointer.f-w-b
   {:on-click #(dispatch close-event)}
   [:div.white
    {:style message-style
     :class-name (case message-type
                   :error "bg-red"
                   "bg-green")}
    [:span message-text]
    [:i.fa.fa-times]]])


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
          [:i.fa.f-s-18
           (if icon {:class-name (str "fa-" icon)})]]
         [:span.m-l-5.header-button-text title]])
      button-cfgs)]]
   (if @(subscribe [:message-shown?])
     [:div.p-b-10.p-r-10.p-l-10
      [message
       @(subscribe [:message-type])
       @(subscribe [:message])
       [:hide-message]]])])

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
      [:div#app-main.container
       [:div.content.w-100-p content]]
      [:div.white.flex.justify-cont-c
       [:div.content.f-w-n.f-s-12
        [:div.flex.justify-cont-s-b.align-items-c.w-100-p.flex-wrap
         [:div.p-10
          [:div.m-b-5 "Icons made by Lorc, Caduceus, and Delapouite. Available on " [:a.orange {:href "http://game-icons.net"} "http://game-icons.net"]]]
         [:div.m-l-10
          [:a {:href "/privacy-policy" :target :_blank} "Privacy Policy"]
          [:a.m-l-5 {:href "/terms-of-use" :target :_blank} "Terms of Use"]]]]]])])

(def row-style
  {:border-bottom "1px solid rgba(255,255,255,0.5)"})

(def list-style
  {:border-top "1px solid rgba(255,255,255,0.5)"})

(defn character-summary [built-char & [include-name?]]
  [:div.flex.character-summary
   (let [nm (char/character-name built-char)]
      (if (and nm include-name?) [:span.m-r-20.m-b-5 nm]))
   [:span.m-r-10.m-b-5
    [:span (char/race built-char)]
    [:div.f-s-12.m-t-5.opacity-6 (char/subrace built-char)]]
   (let [levels (char/levels built-char)]
     (if (seq levels)
       [:span.flex
        (map-indexed
         (fn [i v]
           (with-meta v {:key i}))
         (interpose
          [:span.m-l-5.m-r-5 "/"]
          (map
           (fn [cls-key]
             (let [{:keys [class-name class-level subclass]} (levels cls-key)]
               [:span
                [:span (str class-name " (" class-level ")")]
                [:div.f-s-12.m-t-5.opacity-6 (if subclass (common/kw-to-name subclass true))]]))
           (char/classes built-char))))]))])

(defn realize-char [built-char]
  (reduce-kv
   (fn [m k v]
     (let [realized-value (es/entity-val built-char k)]
       (if (fn? realized-value)
         m
         (assoc m k realized-value))))
   (sorted-map)
   built-char))

(def thumbnail-style
  {:height "100px"})

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
                                      (str spell-save-dc
                                           (if (-> spell-modifiers count (> 1))
                                             (str " (" class ", " (s/upper-case (name ability)) ")"))))
                                    spell-modifiers))]]
    [:div.f-s-14
     [:span.f-w-b "Spell Attack Bonus: "]
     [:span.f-w-n (s/join ", " (map (fn [[class {:keys [spell-attack-modifier ability]}]]
                                      (str (common/bonus-str spell-attack-modifier)
                                           (if (-> spell-modifiers count (> 1))
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
                   " ("
                   (s/join
                    ", "
                    (remove
                     nil?
                     [(if (:ability spell) (s/upper-case (name (:ability spell))))
                      (if (:qualifier spell) (:qualifier spell))]))
                
                   ")")]))
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

(defn character-display [built-char show-summary?]
  (let [race (char/race built-char)
        subrace (char/subrace built-char)
        alignment (char/alignment built-char)
        background (char/background built-char)
        classes (char/classes built-char)
        levels (char/levels built-char)
        darkvision (char/darkvision built-char)
        skill-profs (char/skill-proficiencies built-char)
        tool-profs (char/tool-proficiencies built-char)
        weapon-profs (char/weapon-proficiencies built-char)
        armor-profs (char/armor-proficiencies built-char)
        resistances (char/damage-resistances built-char)
        damage-immunities (char/damage-immunities built-char)
        immunities (char/immunities built-char)
        condition-immunities (char/condition-immunities built-char)
        languages (char/languages built-char)
        abilities (char/ability-values built-char)
        ability-bonuses (char/ability-bonuses built-char)
        armor-class (char/base-armor-class built-char)
        armor-class-with-armor (char/armor-class-with-armor built-char)
        armor (char/normal-armor-inventory built-char)
        magic-armor (char/magic-armor-inventory built-char)
        all-armor (merge magic-armor armor)
        spells-known (char/spells-known built-char)
        spell-slots (char/spell-slots built-char)
        weapons (char/normal-weapons-inventory built-char)
        magic-weapons (char/magic-weapons-inventory built-char)
        equipment (char/normal-equipment-inventory built-char)
        magic-items (char/magical-equipment-inventory built-char)
        traits (char/traits built-char)
        attacks (char/attacks built-char)
        bonus-actions (char/bonus-actions built-char)
        reactions (char/reactions built-char)
        actions (char/actions built-char)
        image-url (char/image-url built-char)
        image-url-failed (es/entity-val built-char :image-url-failed)
        faction-image-url (char/faction-image-url built-char)
        faction-image-url-failed (es/entity-val built-char :faction-image-url-failed)]
    [:div
     (if show-summary?
       [:div.f-s-24.f-w-600.m-b-16.text-shadow.flex
        [character-summary built-char]])
     [:div.details-columns
      [:div.flex-grow-1.flex-basis-50-p
       [:div.w-100-p.t-a-c
        [:div.flex.justify-cont-s-b.p-10
         (doall
          (map
           (fn [k]
             ^{:key k}
             [:div
              (t/ability-icon k 32)
              [:div.f-s-20.uppercase (name k)]
              [:div.f-s-24.f-w-b (abilities k)]
              [:div.f-s-12.opacity-5.m-b--2.m-t-2 "mod"]
              [:div.f-s-18 (common/bonus-str (ability-bonuses k))]])
           char/ability-keys))]]
       [:div.flex
        [:div.w-50-p
         (if image-url-failed
           [:div.p-10.red.f-s-18 (str (if (= :https image-url-failed)
                                        no-https-images
                                        "Image could not be loaded, please check the URL and try again"))]
           [:img.character-image.w-100-p.m-b-20 {:src (if (not (s/blank? image-url)) image-url "/image/barbarian.png")
                                                 :on-error (fn [_] (dispatch [:failed-loading-image image-url]))
                                                 :on-load (fn [_] (if image-url-failed (dispatch [:loaded-image])))}])
         (if faction-image-url-failed
           [:div.p-10.red.f-s-18 (str (if (= :https faction-image-url-failed)
                                        no-https-images
                                        "Faction image could not be loaded, please check the URL and try again"))]
           (if (not (s/blank? faction-image-url))
             [:div.p-30 [:img.character-image.w-100-p.m-b-20 {:src faction-image-url
                                                    :on-error (fn [_] (dispatch [:failed-loading-faction-image faction-image-url]))
                                                    :on-load (fn [_] (if faction-image-url-failed (dispatch [:loaded-faction-image])))}]]))]
        [:div.w-50-p.m-l-10
         (if background [svg-icon-section "Background" "ages" [:span.f-s-18.f-w-n background]])
         (if alignment [svg-icon-section "Alignment" "yin-yang" [:span.f-s-18.f-w-n alignment]])
         [armor-class-section armor-class armor-class-with-armor all-armor]
         [svg-icon-section "Hit Points" "health-normal" (char/max-hit-points built-char)]
         [speed-section built-char all-armor]
         [svg-icon-section "Darkvision" "night-vision" (if (and darkvision (pos? darkvision)) (str darkvision " ft.") "--")]
         [svg-icon-section "Initiative" "sprint" (common/bonus-str (char/initiative built-char))]
         [display-section "Proficiency Bonus" nil (common/bonus-str (char/proficiency-bonus built-char))]
         [svg-icon-section "Passive Perception" "awareness" (char/passive-perception built-char)]
         (let [num-attacks (char/number-of-attacks built-char)]
           (if (> num-attacks 1)
             [display-section "Number of Attacks" nil num-attacks]))
         (let [criticals (char/critical-hit-values built-char)
               min-crit (apply min criticals)
               max-crit (apply max criticals)]
           (if (not= min-crit max-crit)
             (display-section "Critical Hit" nil (str min-crit "-" max-crit))))
         [:div
          [list-display-section
           "Saving Throws" "dodging"
           (map (fn [[k v]] (str (s/upper-case (name k)) (common/bonus-str v))) (char/save-bonuses built-char))]
          (let [save-advantage (char/saving-throw-advantages built-char)]
            [:ul.list-style-disc.m-t-5
             (doall
              (map-indexed
               (fn [i {:keys [abilities types]}]
                 ^{:key i}
                 [:li (str "advantage on "
                           (common/list-print (map (comp s/lower-case :name opt/abilities-map) abilities))
                           " saves against "
                           (common/list-print
                            (map #(let [condition (opt/conditions-map %)]
                                    (cond
                                      condition (str "being " (s/lower-case (:name condition)))
                                      (keyword? %) (name %)
                                      :else %))
                                 types)))])
               save-advantage))])]]]]
      [:div.flex-grow-1.flex-basis-50-p.details-column-2
       [list-display-section "Skill Proficiencies" "juggler"
        (let [skill-bonuses (char/skill-bonuses built-char)]
          (map
           (fn [[skill-kw bonus]]
             (str (s/capitalize (name skill-kw)) " " (common/bonus-str bonus)))
           (filter (fn [[k bonus]]
                     (not= bonus (ability-bonuses (:ability (skills/skills-map k)))))
                   skill-bonuses)))]
       [list-item-section "Languages" "lips" languages (partial prof-name opt/language-map)]
       [list-item-section "Tool Proficiencies" "stone-crafting" tool-profs (partial prof-name equip/tools-map)]
       [list-item-section "Weapon Proficiencies" "bowman" weapon-profs (partial prof-name weapon/weapons-map)]
       [list-item-section "Armor Proficiencies" "mailed-fist" armor-profs (partial prof-name armor/armor-map)]
       [list-item-section "Damage Resistances" "surrounded-shield" resistances resistance-str]
       [list-item-section "Damage Immunities" nil damage-immunities resistance-str]
       [list-item-section "Condition Immunities" nil condition-immunities resistance-str]
       [list-item-section "Immunities" nil immunities resistance-str]
       (if (seq spells-known) [spells-known-section spells-known spell-slots (es/entity-val built-char :spell-modifiers)])
       [equipment-section "Weapons" "plain-dagger" (concat magic-weapons weapons) mi/all-weapons-map]
       [equipment-section "Armor" "breastplate" (merge magic-armor armor) mi/all-armor-map]
       [equipment-section "Equipment" "backpack" (concat magic-items
                                                         equipment
                                                         (map
                                                          (juxt :name identity)
                                                          (es/entity-val built-char :custom-equipment))) mi/all-equipment-map]
       [attacks-section attacks]
       [actions-section "Actions" "beams-aura" actions]
       [actions-section "Bonus Actions" "run" bonus-actions]
       [actions-section "Reactions" "van-damme-split" reactions]
       [actions-section "Features, Traits, and Feats" "vitruvian-man" traits]]]]))

(def character-display-style
  {:padding "20px 5px"
   :background-color "rgba(0,0,0,0.15)"})

(defn character-list []
  (let [characters @(subscribe [:dnd-5e-characters])
        built-template @(subscribe [:built-template])
        expanded-characters @(subscribe [:expanded-characters])]
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
            (let [character (char/from-strict strict-character)
                  built-template (subs/built-template (subs/selected-plugin-options character))
                  built-character (subs/built-character character built-template)
                  image-url (char/image-url built-character)
                  expanded? (get expanded-characters id)]
              [:div.pointer
               {:on-click #(dispatch [:toggle-character-expanded id])}
               [:div.flex.justify-cont-s-b.align-items-c
                [:div.m-l-10.flex.align-items-c
                 (if image-url
                   [:img.m-r-20.m-t-10.m-b-10 {:src image-url
                                               :style thumbnail-style}])
                 [:div.f-s-24.f-w-600
                  {:style summary-style}
                  [:div.list-character-summary
                   [character-summary built-character true]]]]
                [:div.orange.pointer.m-r-10
                 [:span.underline (if expanded?
                                    "collapse"
                                    "open")]
                 [:i.fa.m-l-5
                  {:class-name (if expanded? "fa-caret-up" "fa-caret-down")}]]]
               (if expanded?
                 [:div
                  {:style character-display-style}
                  [:div.flex.justify-cont-end
                   [:button.form-button
                    {:on-click #(dispatch [:edit-character character])}
                    "EDIT"]
                   [:button.form-button.m-l-5
                    {:on-click #(dispatch [:delete-character id])}
                    "DELETE"]]
                  [character-display built-character false]])])])
         characters))]]]))

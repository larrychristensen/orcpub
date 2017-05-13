(ns orcpub.email
  (:require [hiccup.core :as hiccup]
            [postal.core :as postal]
            [environ.core :as environ]))

(defn verification-email-html [first-and-last-name username verification-url]
  [:div
   (str "Dear " first-and-last-name ",")
   [:br]
   [:br]
   "You OrcPub account is almost ready, we just need you to verify your email address going the following URL to confirm that you are authorized to use this email address:"
   [:br]
   [:br]
   [:a {:href verification-url} verification-url]
   [:br]
   [:br]
   "Sincerely,"
   [:br]
   [:br]
   "The OrcPub Team"])

(defn verification-email [first-and-last-name username verification-url]
  [{:type "text/html"
    :content (hiccup/html (verification-email-html first-and-last-name username verification-url))}])

(defn send-verification-email [base-url {:keys [email username first-and-last-name]} verification-key]
  (prn "SENDING_EMAIL!" email verification-key first-and-last-name)
  (postal/send-message {:user (environ/env :email-access-key)
                        :pass (environ/env :email-secret-key)
                        :host "email-smtp.us-west-2.amazonaws.com"
                        :port 587}
                       {:from "OrcPub Team <no-reply@orcpub.com>"
                        :to email
                        :subject "OrcPub Email Verification"
                        :body (verification-email
                               first-and-last-name
                               username
                               (str base-url "/verify?key=" verification-key))}))

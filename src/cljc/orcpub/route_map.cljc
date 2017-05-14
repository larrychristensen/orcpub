(ns orcpub.route-map
  (:require [bidi.bidi :as bidi]))

(def dnd-e5-char-builder-route :char-builder-5e)
(def register-route :register)
(def register-page-route :register-page)
(def verify-route :verify)
(def verify-failed-route :verify-failed)
(def verify-success-route :verify-success)
(def re-verify-route :re-verify)
(def verify-sent-route :verify-sent)
(def login-route :login)
(def login-page-route :login-page)
(def character-pdf-route :character-pdf)
(def check-email-route :check-email)
(def check-username-route :check-username)
(def reset-password-page-route :reset-password-page)
(def reset-password-route :reset-password)
(def send-password-reset-route :send-password-reset)
(def send-password-reset-page-route :send-password-reset-page)

(def routes ["/" {"verify" verify-route
                  "verification-expired" verify-failed-route
                  "verification-successful" verify-success-route
                  "verification-sent" verify-sent-route
                  "re-verify" re-verify-route
                  "register" register-route
                  "register-page" register-page-route
                  "login" login-route
                  "login-page" login-page-route
                  "character.pdf" character-pdf-route
                  "check-email" check-email-route
                  "check-username" check-username-route
                  "reset-password-page" reset-password-page-route
                  "reset-password" reset-password-route
                  "send-password-reset" send-password-reset-route
                  "send-password-reset-page" send-password-reset-page-route}])

(defn path-for [route]
  (bidi/path-for routes route))

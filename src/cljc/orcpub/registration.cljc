(ns orcpub.registration
  (:require [clojure.string :as s]))

(defn fails-match? [regex value]
  (or (nil? value)
      (nil? (re-matches regex value))))

(defn validate-password [password]
  (let [password-missing-special-character? (fails-match? #".*[!@#\$%\^&\*].*" password)
        password-missing-number? (fails-match? #".*[0-9].*" password)
        password-missing-uppercase? (fails-match? #".*[A-Z].*" password)
        password-missing-lowercase? (fails-match? #".*[a-z].*" password)
        password-too-short? (or (nil? password) (< (count password) 8))]
    (cond-> {}
      password-missing-lowercase? (update :password conj "Password must have a least one lowercase character")
      password-missing-uppercase? (update :password conj "Password must have a least one uppercase character")
      password-missing-number? (update :password conj "Password must have a least one numeric character")
      password-missing-special-character? (update :password conj "Password must have a least one of the following characters: !, @, #, $, %, ^, &, or *")
      password-too-short? (update :password conj "Password must be at least 8 characters"))))

(defn validate-registration [{:keys [email username password first-and-last-name]} email-taken? username-taken?]
  (let [bad-email-format? (fails-match? #"[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,4}" email)
        username-too-short? (or (nil? username) (< (count username) 3))]
    (cond-> {}
      (s/blank? first-and-last-name) (update :first-and-last-name conj "Name is required")
      email-taken? (update :email conj "Email address is already associated with another account")
      bad-email-format? (update :email conj (if (s/blank? email)
                                              "Email is required"
                                              "Email is not a valid email format"))
      username-taken? (update :username conj "Username is already taken by another user")
      username-too-short? (update :username conj (if (s/blank? username)
                                                   "Username is required"
                                                   "Username must be at least 3 characters"))
      true (merge (validate-password password)))))

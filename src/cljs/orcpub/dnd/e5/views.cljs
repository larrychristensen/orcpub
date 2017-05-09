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

(defn login-form []
  (let [params (r/atom {})]
    (fn []
      [:div
       [:input {:name :email
                       :placeholder "Username or Email"
                       :style (assoc input-style :width "150px")
                       :value (:username @params)
                       :on-change (fn [e] (swap! params assoc :username (.. e -target -value)))}]
       [:input.m-l-5 {:name :password
                             :type :password
                             :placeholder "Password"
                             :value (:password @params)
                             :style (assoc input-style :width "150px")
                             :on-change (fn [e] (swap! params assoc :password (.. e -target -value)))}]
       [:button.form-button.m-l-5
        {:style {:height "42px"
                 :width "100px"
                 :font-size "16px"}
         :on-click (fn [_]
                     (dispatch [:login @params true]))}
        "LOGIN"]])))


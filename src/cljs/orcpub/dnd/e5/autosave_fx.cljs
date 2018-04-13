(ns ^{:doc "Effects and utils for handling throttled autosave"}
  orcpub.dnd.e5.autosave-fx
  (:require [orcpub.dnd.e5.character :as char5e]
            [re-frame.core :refer [reg-fx dispatch]]))

;; timeout in ms during which we wait for further changes; if
;; none are received, the save will be performed.
(def throttled-save-timeout 7500)

(defonce throttled-save-timer (atom nil))
(defonce throttled-save-queue (atom #{}))

(defn confirm-close-window
  "While a save is pending, this function will be registered as an event listener
   on the window to try to help users not lose data."
  [e]
  (let [confirm-message "You have unsaved changes. Are you sure you want to exit?"]
    (when e
      (set! (.-returnValue e) confirm-message))
    confirm-message))

(defn dispatch-throttled-saves
  []
  (reset! throttled-save-timer nil)

  ; TODO should/can we wait until save is successful?
  (js/window.removeEventListener
    "beforeunload"
    confirm-close-window)

  (let [queued-ids @throttled-save-queue]
    (reset! throttled-save-queue #{})
    (doall
      (for [id queued-ids]
        (dispatch [::char5e/save-character id])))))

;; The primary fx handler; simply return from a -fx event handler
;; as {::char5e/save-character-throttled <characterId>}
(reg-fx
  ::char5e/save-character-throttled
  (fn [id]
    (if-let [timer @throttled-save-timer]
      ; existing timer; clear it
      (js/clearTimeout timer)
      ; no existing, so this is the first; confirm window closing
      (js/window.addEventListener
        "beforeunload"
        confirm-close-window))

    ; enqueue
    (swap! throttled-save-queue conj id)
    (reset! throttled-save-timer
            (js/setTimeout
              dispatch-throttled-saves
              throttled-save-timeout))))


(ns orcpub.user-agent
  (:require [goog.labs.userAgent.browser :as g-browser]
            [goog.labs.userAgent.device :as g-device]
            [goog.labs.userAgent.platform :as g-platform]))

(defn browser []
  (cond
    (g-browser/isChrome) :chrome
    (g-browser/isEdge) :edge
    (g-browser/isFirefox) :firefox
    (g-browser/isIE) :ie
    (g-browser/isSafari) :safari
    :else :not-found))

(defn browser-version []
  (g-browser/getVersion))

(defn device-type []
  (cond
    (g-device/isDesktop) :desktop
    (g-device/isMobile) :mobile
    (g-device/isTablet) :tablet
    :else :not-found))

(defn platform []
  (cond
    (g-platform/isAndroid) :android
    (g-platform/isChromeOS) :chrome-os
    (g-platform/isIos) :ios
    (g-platform/isIpad) :ipad
    (g-platform/isIphone) :iphone
    (g-platform/isIpod) :ipod
    (g-platform/isLinux) :linux
    (g-platform/isMacintosh) :macintosh
    (g-platform/isWindows) :windows
    :else :not-found))

(defn platform-version []
  (g-platform/getVersion))

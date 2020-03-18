(ns orcpub.favicon
  (:require [hiccup.def :refer [defhtml]]))

(defhtml install
         "faviconit.com favicons.
         Filenames default to: icon.ico; browserconfig.xml;
         png - 16,32,57,64,72,76,96,114,120,144,152,160,196,"
         [& {:keys [img xml png-prefix ver]}]
         [:link {:rel "shortcut icon" :href (str img "/favicon.ico?v=" ver)}]
         [:link {:rel "icon" :sizes "16x16 32x32 64x64" :href (str img "/favicon.ico?v=" ver)}]
         [:link {:rel "icon" :type "image/png" :sizes "196x196" :href (str img "/" png-prefix "196.png?v=" ver)}]
         [:link {:rel "icon" :type "image/png" :sizes "160x160" :href (str img "/" png-prefix "160.png?v=" ver)}]
         [:link {:rel "icon" :type "image/png" :sizes "96x96" :href (str img "/" png-prefix "96.png?v=" ver)}]
         [:link {:rel "icon" :type "image/png" :sizes "64x64" :href (str img "/" png-prefix "64.png?v=" ver)}]
         [:link {:rel "icon" :type "image/png" :sizes "32x32" :href (str img "/" png-prefix "32.png?v=" ver)}]
         [:link {:rel "icon" :type "image/png" :sizes "16x16" :href (str img "/" png-prefix "16.png?v=" ver)}]
         [:link {:rel "apple-touch-icon" :sizes "152x152" :href (str img "/" png-prefix "152.png?v=" ver)}]
         [:link {:rel "apple-touch-icon" :sizes "144x144" :href (str img "/" png-prefix "144.png?v=" ver)}]
         [:link {:rel "apple-touch-icon" :sizes "120x120" :href (str img "/" png-prefix "120.png?v=" ver)}]
         [:link {:rel "apple-touch-icon" :sizes "114x114" :href (str img "/" png-prefix "114.png?v=" ver)}]
         [:link {:rel "apple-touch-icon" :sizes "76x76" :href (str img "/" png-prefix "76.png?v=" ver)}]
         [:link {:rel "apple-touch-icon" :sizes "72x72" :href (str img "/" png-prefix "72.png?v=" ver)}]
         [:link {:rel "apple-touch-icon" :href (str img "/" png-prefix "57.png?v=" ver)}]
         [:meta {:name "msapplication-TileColor" :content "#FFFFFF"}]
         [:meta {:name "msapplication-TileImage" :content (str img "/" png-prefix "144.png?v=" ver)}]
         [:meta {:name "msapplication-config" :content (str xml "/browserconfig.xml")}])

(ns orcpub.components)

(defn checkbox [selected? disable?]
  [:i.fa.fa-check.f-s-14.bg-white.orange-shadow.m-r-10
   {:class-name (str (if selected? "black slight-text-shadow" "transparent")
                     " "
                     (if disable?
                       "opacity-5"))}])

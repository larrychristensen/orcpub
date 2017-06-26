(ns orcpub.index
  (require '[hiccup.page :refer :all]))

(defn meta [property content]
  (if content
    [:meta
     {:property property
      :content content}]))

(defn index-page [{:keys [url
                          title
                          description
                          image
                          fb-type]}]
  (html5
   {:lang :en}
   [:head
    (meta "og:url" url)
    (meta "og:type" fb-type)
    (meta "og:title" title)
    (meta "og:description" description)
    (meta "og:image" image)
    [:meta {:charset "UTF-8"}]
    [:meta {:name "viewport"
            :content "width=device-width, initial-scale=1"}]
    [:title title]
    (include-css "/css/style.css"
                 "/css/compiled/styles.css")]))

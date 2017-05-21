(ns orcpub.privacy
  (:require [hiccup.page :as page]))

(defn section [{:keys [title font-size paragraphs subsections]}]
  [:div
   [:div.m-t-20.f-w-b
    {:style (str "color:#2c3445;font-size:" font-size "px")}
    title]
   (map
    (fn [p] [:p p])
    paragraphs)
   (map
    section
    subsections)])

(def privacy-policy-section
  {:title "Privacy Policy"
   :font-size 48
   :subsections
   [{:title "How We Collect Your Information"
     :font-size 32
     :subsections
     [{:title "When you give it to us or give us permission to obtain it"
       :font-size 28
       :paragraphs
       ["When you sign up for or use our products, you voluntarily give us certain information. This can include your name, profile photo, role-playing game characters, comments, likes, the email address or phone number you used to sign up, and any other information you provide us. If you’re using OrcPub on your mobile device, you can also choose to provide us with location data. And if you choose to buy something on OrcPub, you provide us with payment information, contact information (ex., address and phone number), and what you purchased. If you buy something for someone else on OrcPub, you’d also provide us with their shipping details and contact information."
        "You also may give us permission to access your information in other services. For example, you may link your Facebook or Twitter account to OrcPub, which allows us to obtain information from those accounts (like your friends or contacts). The information we get from those services often depends on your settings or their privacy policies, so be sure to check what those are."]}
      {:title "We also get technical information when you use our products"
       :font-size 28
       :paragraphs
       ["These days, whenever you use a website, mobile application, or other internet service, there’s certain information that almost always gets created and recorded automatically. The same is true when you use our products. Here are some of the types of information we collect:"
        "Log data. When you use OrcPub, our servers may automatically record information (“log data”), including information that your browser sends whenever you visit a website or your mobile app sends when you’re using it. This log data may include your Internet Protocol address, the address of the web pages you visited that had OrcPub features, browser type and settings, the date and time of your request, how you used OrcPub, and cookie data."
        "Cookie data. Depending on how you’re accessing our products, we may use “cookies” (small text files sent by your computer each time you visit our website, unique to your OrcPub account or your browser) or similar technologies to record log data. When we use cookies, we may use “session” cookies (that last until you close your browser) or “persistent” cookies (that last until you or your browser delete them). For example, we may use cookies to store your language preferences or other OrcPub settings so you don‘t have to set them up every time you visit OrcPub. Some of the cookies we use are associated with your OrcPub account (including personal information about you, such as the email address you gave us), and other cookies are not."
        "Device information. In addition to log data, we may also collect information about the device you’re using OrcPub on, including what type of device it is, what operating system you’re using, device settings, unique device identifiers, and crash data. Whether we collect some or all of this information often depends on what type of device you’re using and its settings. For example, different types of information are available depending on whether you’re using a Mac or a PC, or an iPhone or an Android phone. To learn more about what information your device makes available to us, please also check the policies of your device manufacturer or software provider."]}
      {:title "Our partners and advertisers may share information with us"
       :font-size 28
       :paragraphs
       ["We may get information about you and your activity off OrcPub from our affiliates, advertisers, partners and other third parties we work with. For example:"
        "Online advertisers typically share information with the websites or apps where they run ads to measure and/or improve those ads. We also receive this information, which may include information like whether clicks on ads led to purchases or a list of criteria to use in targeting ads."]}]}
    {:title "How do we use the information we collect?"
     :font-size 32
     :paragraphs
     ["We use the information we collect to provide our products to you and make them better, develop new products, and protect OrcPub and our users. For example, we may log how often people use two different versions of a product, which can help us understand which version is better. If you make a purchase on OrcPub, we’ll save your payment information and contact information so that you can use them the next time you want to buy something on OrcPub."
      "We also use the information we collect to offer you customized content, including:"
      "Showing you ads you might be interested in. For example, if you purchased a camping tent on OrcPub, we may show you ads for other outdoorsy products."
      "We also use the information we collect to:"
      "Send you updates (such as when certain activity, like shares or comments, happens on OrcPub), newsletters, marketing materials and other information that may be of interest to you. For example, depending on your email notification settings, we may send you weekly updates that include content you may like. You can decide to stop getting these updates by updating your account settings (or through other settings we may provide)."
      "Help your friends and contacts find you on OrcPub. For example, if you sign up using a Facebook account, we may help your Facebook friends find your account on OrcPub when they first sign up for OrcPub. Or, we may allow people to search for your account on OrcPub using your email address."
      "Respond to your questions or comments."]}
    {:title "Transferring your Information"
     :font-size 32
     :paragraphs
     ["OrcPub is headquartered in the United States. By using our products or services, you authorize us to transfer and store your information inside the United States, for the purposes described in this policy. The privacy protections and the rights of authorities to access your personal information in such countries may not be equivalent to those in your home country."]}
    {:title "How and when do we share information"
     :font-size 32
     :paragraphs
     ["Anyone can see the public role-playing game characters and other content you create, and the profile information you give us. We may also make this public information available through what are called “APIs” (basically a technical way to share information quickly). For example, a partner might use a OrcPub API to integrate with other applications our users may be interested in. The other limited instances where we may share your personal information include:"
      "When we have your consent. This includes sharing information with other services (like Facebook or Twitter) when you’ve chosen to link to your OrcPub account to those services or publish your activity on OrcPub to them. For example, you can choose to share your characters on Facebook or Twitter."
      "When you buy something on OrcPub using your credit card, we may share your credit card information, contact information, and other information about the transaction with the merchant you’re buying from. The merchants treat this information just as if you had made a purchase from their website directly, which means their privacy policies and marketing policies apply to the information we share with them."
      "Online advertisers typically use third party companies to audit the delivery and performance of their ads on websites and apps. We also allow these companies to collect this information on OrcPub. To learn more, please see our Help Center."
      "We may employ third party companies or individuals to process personal information on our behalf based on our instructions and in compliance with this Privacy Policy. For example, we share credit card information with the payment companies we use to store your payment information. Or, we may share data with a security consultant to help us get better at identifying spam. In addition, some of the information we request may be collected by third party providers on our behalf."
      "If we believe that disclosure is reasonably necessary to comply with a law, regulation or legal request; to protect the safety, rights, or property of the public, any person, or OrcPub; or to detect, prevent, or otherwise address fraud, security or technical issues. We may share the information described in this Policy with our wholly-owned subsidiaries and affiliates. We may engage in a merger, acquisition, bankruptcy, dissolution, reorganization, or similar transaction or proceeding that involves the transfer of the information described in this Policy. We may also share aggregated or non-personally identifiable information with our partners, advertisers or others."]}
    {:title "What choices do you have about your information?"
     :font-size 32
     :paragraphs
     ["You may close your account at any time by emailing redorc@orcpub.com. We will then inactivate your account and remove your content from OrcPub. We may retain archived copies of you information as required by law or for legitimate business purposes (including to help address fraud and spam). You may remove any content you create from OrcPub at any time, although we may retain archived copies of the information. You may also disable sharing of content you create at any time, whether publicly shared or privately shared with specific users."
      "Also, we support the Do Not Track browser setting."]}
    {:title "Our policy on children’s information"
     :font-size 32
     :paragraphs
     ["OrcPub is not directed to children under 13. If you learn that your minor child has provided us with personal information without your consent, please contact us."]}
    {:title "How do we make changes to this policy?"
     :font-size 32
     :paragraphs
     ["We may change this policy from time to time, and if we do we’ll post any changes on this page. If you continue to use OrcPub after those changes are in effect, you agree to the revised policy. If the changes are significant, we may provide more prominent notice or get your consent as required by law."]}
    {:title "How can you contact us?"
     :font-size 32
     :paragraphs
     ["You can contact us by emailing redorc@orcpub.com"]}]})

(defn terms-page [sections]
  (page/html5
   [:head
    [:link {:rel :stylesheet :href "/css/style.css" :type "text/css"}]
    [:link {:rel :stylesheet :href "/css/compiled/styles.css" :type "text/css"}]]
   [:body.sans
    [:div
     [:div.app-header-bar.container
      {:style "background-color:#2c3445"}
      [:div.content
       [:div.flex.justify-cont-s-b.align-items-c.w-100-p.p-l-20.p-r-20
        [:img.orcpub-logo.h-32.w-120.pointer {:src "/image/orcpub-logo.svg"}]]]]
     [:div.container
      [:div.content
       [:div.f-s-24
        (section sections)]]]]]))

(defn privacy-policy []
  (terms-page privacy-policy-section))

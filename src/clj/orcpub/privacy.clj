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
   [{:title "Thank you for using OrcPub!"
     :font-size 32
     :paragraphs
     ["We wrote this policy to help you understand what information we collect, how we use it, and what choices you have. Because we're an internet company, some of the concepts below are a little technical, but we've tried our best to explain things in a simple and clear way. We welcome your questions and comments on this policy."]}
    {:title "How We Collect Your Information"
     :font-size 32
     :subsections
     [{:title "When you give it to us or give us permission to obtain it"
       :font-size 28
       :paragraphs
       ["When you sign up for or use our products, you voluntarily give us certain information. This can include your name, profile photo, role-playing game characters, comments, likes, the email address or phone number you used to sign up, and any other information you provide us. If you're using OrcPub on your mobile device, you can also choose to provide us with location data. And if you choose to buy something on OrcPub, you provide us with payment information, contact information (ex., address and phone number), and what you purchased. If you buy something for someone else on OrcPub, you'd also provide us with their shipping details and contact information."
        "You also may give us permission to access your information in other services. For example, you may link your Facebook or Twitter account to OrcPub, which allows us to obtain information from those accounts (like your friends or contacts). The information we get from those services often depends on your settings or their privacy policies, so be sure to check what those are."]}
      {:title "We also get technical information when you use our products"
       :font-size 28
       :paragraphs
       ["These days, whenever you use a website, mobile application, or other internet service, there's certain information that almost always gets created and recorded automatically. The same is true when you use our products. Here are some of the types of information we collect:"
        "Log data. When you use OrcPub, our servers may automatically record information (\"log data\"), including information that your browser sends whenever you visit a website or your mobile app sends when you're using it. This log data may include your Internet Protocol address, the address of the web pages you visited that had OrcPub features, browser type and settings, the date and time of your request, how you used OrcPub, and cookie data."
        "Cookie data. Depending on how you're accessing our products, we may use \"cookies\" (small text files sent by your computer each time you visit our website, unique to your OrcPub account or your browser) or similar technologies to record log data. When we use cookies, we may use \"session\" cookies (that last until you close your browser) or \"persistent\" cookies (that last until you or your browser delete them). For example, we may use cookies to store your language preferences or other OrcPub settings so you don‘t have to set them up every time you visit OrcPub. Some of the cookies we use are associated with your OrcPub account (including personal information about you, such as the email address you gave us), and other cookies are not."
        "Device information. In addition to log data, we may also collect information about the device you're using OrcPub on, including what type of device it is, what operating system you're using, device settings, unique device identifiers, and crash data. Whether we collect some or all of this information often depends on what type of device you're using and its settings. For example, different types of information are available depending on whether you're using a Mac or a PC, or an iPhone or an Android phone. To learn more about what information your device makes available to us, please also check the policies of your device manufacturer or software provider."]}
      {:title "Our partners and advertisers may share information with us"
       :font-size 28
       :paragraphs
       ["We may get information about you and your activity off OrcPub from our affiliates, advertisers, partners and other third parties we work with. For example:"
        "Online advertisers typically share information with the websites or apps where they run ads to measure and/or improve those ads. We also receive this information, which may include information like whether clicks on ads led to purchases or a list of criteria to use in targeting ads."]}]}
    {:title "How do we use the information we collect?"
     :font-size 32
     :paragraphs
     ["We use the information we collect to provide our products to you and make them better, develop new products, and protect OrcPub and our users. For example, we may log how often people use two different versions of a product, which can help us understand which version is better. If you make a purchase on OrcPub, we'll save your payment information and contact information so that you can use them the next time you want to buy something on OrcPub."
      "We also use the information we collect to offer you customized content, including:"
      "Showing you ads you might be interested in."
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
     ["Anyone can see the public role-playing game characters and other content you create, and the profile information you give us. We may also make this public information available through what are called \"APIs\" (basically a technical way to share information quickly). For example, a partner might use a OrcPub API to integrate with other applications our users may be interested in. The other limited instances where we may share your personal information include:"
      "When we have your consent. This includes sharing information with other services (like Facebook or Twitter) when you've chosen to link to your OrcPub account to those services or publish your activity on OrcPub to them. For example, you can choose to share your characters on Facebook or Twitter."
      "When you buy something on OrcPub using your credit card, we may share your credit card information, contact information, and other information about the transaction with the merchant you're buying from. The merchants treat this information just as if you had made a purchase from their website directly, which means their privacy policies and marketing policies apply to the information we share with them."
      "Online advertisers typically use third party companies to audit the delivery and performance of their ads on websites and apps. We also allow these companies to collect this information on OrcPub. To learn more, please see our Help Center."
      "We may employ third party companies or individuals to process personal information on our behalf based on our instructions and in compliance with this Privacy Policy. For example, we share credit card information with the payment companies we use to store your payment information. Or, we may share data with a security consultant to help us get better at identifying spam. In addition, some of the information we request may be collected by third party providers on our behalf."
      "If we believe that disclosure is reasonably necessary to comply with a law, regulation or legal request; to protect the safety, rights, or property of the public, any person, or OrcPub; or to detect, prevent, or otherwise address fraud, security or technical issues. We may share the information described in this Policy with our wholly-owned subsidiaries and affiliates. We may engage in a merger, acquisition, bankruptcy, dissolution, reorganization, or similar transaction or proceeding that involves the transfer of the information described in this Policy. We may also share aggregated or non-personally identifiable information with our partners, advertisers or others."]}
    {:title "What choices do you have about your information?"
     :font-size 32
     :paragraphs
     ["You may close your account at any time by emailing redorc@orcpub.com. We will then inactivate your account and remove your content from OrcPub. We may retain archived copies of you information as required by law or for legitimate business purposes (including to help address fraud and spam). You may remove any content you create from OrcPub at any time, although we may retain archived copies of the information. You may also disable sharing of content you create at any time, whether publicly shared or privately shared with specific users."
      "Also, we support the Do Not Track browser setting."]}
    {:title "Our policy on children's information"
     :font-size 32
     :paragraphs
     ["OrcPub is not directed to children under 13. If you learn that your minor child has provided us with personal information without your consent, please contact us."]}
    {:title "How do we make changes to this policy?"
     :font-size 32
     :paragraphs
     ["We may change this policy from time to time, and if we do we'll post any changes on this page. If you continue to use OrcPub after those changes are in effect, you agree to the revised policy. If the changes are significant, we may provide more prominent notice or get your consent as required by law."]}
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

(def terms-section
  {:title "Terms of Service"
   :font-size 48
   :subsections
   [{:title "Thank you for using OrcPub!"
     :font-size 32
     :paragraphs
     [[:div "These Terms of Service (\"Terms\") govern your access to and use of OrcPub's website, products, and services (\"Products\"). Please read these Terms carefully, and contact us if you have any questions. By accessing or using our Products, you agree to be bound by these Terms and by our " [:a {:href "/privacy-policy" :target :_blank} "Privacy Policy"] ". You also confirm you have read and agreed to our " [:a {:href "/community-guidelines" :target :_blank} "Community guidelines"] " and our " [:a {:href "/cookies-policy"} "Cookies policy"] "."]]}
    {:title "1. Using OrcPub"
     :font-size 32
     :subsections
     [{:title "a. Who can use OrcPub"
       :font-size 28
       :paragraphs
       ["You may use our Products only if you can form a binding contract with OrcPub, and only in compliance with these Terms and all applicable laws. When you create your OrcPub account, you must provide us with accurate and complete information. Any use or access by anyone under the age of 13 is prohibited. If you open an account on behalf of a company, organization, or other entity, then (a) \"you\" includes you and that entity, and (b) you represent and warrant that you are authorized to grant all permissions and licenses provided in these Terms and bind the entity to these Terms, and that you agree to these Terms on the entity's behalf. Some of our Products may be software that is downloaded to your computer, phone, tablet, or other device. You agree that we may automatically upgrade those Products, and these Terms will apply to such upgrades."]}
      {:title "b. Our license to you"
       :font-size 28
       :paragraphs
       ["Subject to these Terms and our policies (including our Community guidelines), we grant you a limited, non-exclusive, non-transferable, and revocable license to use our Products."]}]}
    {:title "2. Your content"
     :font-size 32
     :subsections
     [{:title "a. Posting Content"
       :font-size 28
       :paragraphs
       ["OrcPub allows you to post content, including photos, comments, links, and other materials. Anything that you post or otherwise make available on our Products is referred to as \"User Content.\" You retain all rights in, and are solely responsible for, the User Content you post to OrcPub."]}
      {:title "b. How OrcPub and other users can use your content"
       :font-size 28
       :paragraphs
       ["You grant OrcPub and our users a non-exclusive, royalty-free, transferable, sublicensable, worldwide license to use, store, display, reproduce, save, modify, create derivative works, perform, and distribute your User Content on OrcPub solely for the purposes of operating, developing, providing, and using the OrcPub Products. Nothing in these Terms shall restrict other legal rights OrcPub may have to User Content, for example under other licenses. We reserve the right to remove or modify User Content for any reason, including User Content that we believe violates these Terms or our policies."]}
      {:title "c. How long we keep your content"
       :font-size 28
       :paragraphs
       ["Following termination or deactivation of your account, or if you remove any User Content from OrcPub, we may retain your User Content for a commercially reasonable period of time for backup, archival, or audit purposes. Furthermore, OrcPub and its users may retain and continue to use, store, display, reproduce, modify, create derivative works, perform, and distribute any of your User Content that other users have stored or shared through OrcPub."]}
      {:title "d. Feedback you provide"
       :font-size 28
       :paragraphs
       ["We value hearing from our users, and are always interested in learning about ways we can make OrcPub more awesome. If you choose to submit comments, ideas or feedback, you agree that we are free to use them without any restriction or compensation to you. By accepting your submission, OrcPub does not waive any rights to use similar or related Feedback previously known to OrcPub, or developed by its employees, or obtained from sources other than you"]}]}
    {:title "3. Copyright policy"
     :font-size 32
     :paragraphs
     ["OrcPub has adopted and implemented the OrcPub Copyright policy in accordance with the Digital Millennium Copyright Act and other applicable copyright laws. For more information, please read our Copyright policy."]}
    {:title "4. Security"
     :font-size 32
     :paragraphs
     ["We care about the security of our users. While we work to protect the security of your content and account, OrcPub cannot guarantee that unauthorized third parties will not be able to defeat our security measures. We ask that you keep your password secure. Please notify us immediately of any compromise or unauthorized use of your account."]}
    {:title "5. Third-party links, sites, and services"
     :font-size 32
     :paragraphs
     ["Our Products may contain links to third-party websites, advertisers, services, special offers, or other events or activities that are not owned or controlled by OrcPub. We do not endorse or assume any responsibility for any such third-party sites, information, materials, products, or services. If you access any third party website, service, or content from OrcPub, you do so at your own risk and you agree that OrcPub will have no liability arising from your use of or access to any third-party website, service, or content."]}
    {:title "6. Termination"
     :font-size 32
     :paragraphs
     ["OrcPub may terminate or suspend this license at any time, with or without cause or notice to you. Upon termination, you continue to be bound by Sections 2 and 6-12 of these Terms."]}
    {:title "7. Indemnity"
     :font-size 32
     :paragraphs
     ["If you use our Products for commercial purposes without agreeing to our Business Terms as required by Section 1(c), as determined in our sole and absolute discretion, you agree to indemnify and hold harmless OrcPub and its respective officers, directors, employees and agents, from and against any claims, suits, proceedings, disputes, demands, liabilities, damages, losses, costs and expenses, including, without limitation, reasonable legal and accounting fees (including costs of defense of claims, suits or proceedings brought by third parties), in any way related to (a) your access to or use of our Products, (b) your User Content, or (c) your breach of any of these Terms."]}
    {:title "8. Disclaimers"
     :font-size 32
     :paragraphs
     ["The Products and all included content are provided on an \"as is\" basis without warranty of any kind, whether express or implied."
      "ORCPUB SPECIFICALLY DISCLAIMS ANY AND ALL WARRANTIES AND CONDITIONS OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND NON-INFRINGEMENT, AND ANY WARRANTIES ARISING OUT OF COURSE OF DEALING OR USAGE OF TRADE."
      "OrcPub takes no responsibility and assumes no liability for any User Content that you or any other user or third party posts or transmits using our Products. You understand and agree that you may be exposed to User Content that is inaccurate, objectionable, inappropriate for children, or otherwise unsuited to your purpose."]}
    {:title "9. Limitation of liability"
     :font-size 32
     :paragraphs
     ["TO THE MAXIMUM EXTENT PERMITTED BY LAW, ORCPUB SHALL NOT BE LIABLE FOR ANY INDIRECT, INCIDENTAL, SPECIAL, CONSEQUENTIAL OR PUNITIVE DAMAGES, OR ANY LOSS OF PROFITS OR REVENUES, WHETHER INCURRED DIRECTLY OR INDIRECTLY, OR ANY LOSS OF DATA, USE, GOOD-WILL, OR OTHER INTANGIBLE LOSSES, RESULTING FROM (A) YOUR ACCESS TO OR USE OF OR INABILITY TO ACCESS OR USE THE PRODUCTS; (B) ANY CONDUCT OR CONTENT OF ANY THIRD PARTY ON THE PRODUCTS, INCLUDING WITHOUT LIMITATION, ANY DEFAMATORY, OFFENSIVE OR ILLEGAL CONDUCT OF OTHER USERS OR THIRD PARTIES; OR (C) UNAUTHORIZED ACCESS, USE OR ALTERATION OF YOUR TRANSMISSIONS OR CONTENT. IN NO EVENT SHALL ORCPUB'S AGGREGATE LIABILITY FOR ALL CLAIMS RELATING TO THE PRODUCTS EXCEED ONE HUNDRED U.S. DOLLARS (U.S. $100.00)."]}
    :title "10. Arbitration"
    :font-size 32
    :paragraphs
    ["For any dispute you have with OrcPub, you agree to first contact us and attempt to resolve the dispute with us informally. If OrcPub has not been able to resolve the dispute with you informally, we each agree to resolve any claim, dispute, or controversy (excluding claims for injunctive or other equitable relief) arising out of or in connection with or relating to these Terms by binding arbitration by the American Arbitration Association (\"AAA\") under the Commercial Arbitration Rules and Supplementary Procedures for Consumer Related Disputes then in effect for the AAA, except as provided herein. Unless you and OrcPub agree otherwise, the arbitration will be conducted in the county where you reside. Each party will be responsible for paying any AAA filing, administrative and arbitrator fees in accordance with AAA rules, except that OrcPub will pay for your reasonable filing, administrative, and arbitrator fees if your claim for damages does not exceed $75,000 and is non-frivolous (as measured by the standards set forth in Federal Rule of Civil Procedure 11(b)). The award rendered by the arbitrator shall include costs of arbitration, reasonable attorneys' fees and reasonable costs for expert and other witnesses, and any judgment on the award rendered by the arbitrator may be entered in any court of competent jurisdiction. Nothing in this Section shall prevent either party from seeking injunctive or other equitable relief from the courts for matters related to data security, intellectual property or unauthorized access to the Service. ALL CLAIMS MUST BE BROUGHT IN THE PARTIES' INDIVIDUAL CAPACITY, AND NOT AS A PLAINTIFF OR CLASS MEMBER IN ANY PURPORTED CLASS OR REPRESENTATIVE PROCEEDING, AND, UNLESS WE AGREE OTHERWISE, THE ARBITRATOR MAY NOT CONSOLIDATE MORE THAN ONE PERSON'S CLAIMS. YOU AGREE THAT, BY ENTERING INTO THESE TERMS, YOU AND ORCPUB ARE EACH WAIVING THE RIGHT TO A TRIAL BY JURY OR TO PARTICIPATE IN A CLASS ACTION."
     "To the extent any claim, dispute or controversy regarding OrcPub or our Products isn't arbitrable under applicable laws or otherwise: you and OrcPub both agree that any claim or dispute regarding OrcPub will be resolved exclusively in accordance with Clause 11 of these Terms."]
    {:title "11. Governing law and jurisdiction"
     :font-size 32
     :paragraphs
     ["These Terms shall be governed by the laws of the State of Utah, without respect to its conflict of laws principles. We each agree to submit to the personal jurisdiction of a state court located in Salt Lake County, Utah or the United States District Court for the District of Utah, for any actions not subject to Section 10 (Arbitration)."]}
    {:title "12. General terms"
     :font-size 32
     :subsections
     [{:title "Notification procedures and changes to these Terms"
       :font-size 28
       :paragraphs
       ["OrcPub reserves the right to determine the form and means of providing notifications to you, and you agree to receive legal notices electronically if we so choose. We may revise these Terms from time to time and the most current version will always be posted on our website. If a revision, in our sole discretion, is material we will notify you. By continuing to access or use the Products after revisions become effective, you agree to be bound by the revised Terms. If you do not agree to the new terms, please stop using the Products."]}
      {:title "Assignment"
       :font-size 28
       :paragraphs
       ["These Terms, and any rights and licenses granted hereunder, may not be transferred or assigned by you, but may be assigned by OrcPub without restriction. Any attempted transfer or assignment in violation hereof shall be null and void.
"]}
      {:title "Entire agreement/severability"
       :font-size 28
       :paragraphs
       ["These Terms, together with the Privacy policy and any amendments and any additional agreements you may enter into with OrcPub in connection with the Products, shall constitute the entire agreement between you and OrcPub concerning the Products. If any provision of these Terms is deemed invalid, then that provision will be limited or eliminated to the minimum extent necessary, and the remaining provisions of these Terms will remain in full force and effect."]}
      {:title "No waiver"
       :font-size 28
       :paragraphs
       ["No waiver of any term of these Terms shall be deemed a further or continuing waiver of such term or any other term, and OrcPub's failure to assert any right or provision under these Terms shall not constitute a waiver of such right or provision."]}
      {:title "Parties"
       :font-size 28
       :paragraphs
       ["These Terms are a contract between you and OrcPub, 2168 E Vimont Ave., Salt Lake City, UT 84109"
        "Effective May 1, 2017"]}]}]})

(defn terms-of-use []
  (terms-page terms-section))

(def community-guidelines-section
  {:title "Community guidelines"
   :font-size 48
   :subsections
   [{:title "Our Mission"
     :font-size 32
     :paragraphs
     ["At OrcPub, our mission is to help you discover and do what you love. That means showing you ideas that are relevant, interesting and personal to you, and making sure you don't see anything that's inappropriate or spammy."
      "These are guidelines for what we do and don't allow on OrcPub. If you come across content that seems to break these rules, you can report it to us."]}
    {:title "Safety"
     :font-size 32
     :paragraphs
     ["We remove porn. We may hide nudity or erotica."
      "We remove content that physically or sexually exploits people. We work with law enforcement to address the sexualization of minors."
      "We remove images that show gratuitous violence or glorify violence."
      "We remove anything that promotes self-harm, like self mutilation, eating disorders or drug abuse."
      "We remove hate speech and discrimination, or groups and people that advocate either."
      "We remove content used to threaten or organize violence or support violent organizations."
      "We remove attacks on private people or sharing of personally identifiable information."
      "We remove content used to sell or buy regulated goods, like drugs, alcohol, tobacco, firearms and other hazardous materials."
      "We remove accounts that impersonate any person or organization."]}
    {:title "Intellectual property and other rights"
     :font-size 32
     :paragraphs
     ["To respect the rights of people on and off OrcPub, please:"
      "Don't infringe anyone's intellectual property, privacy or other rights."
      "Don't do anything or post any content that violates laws or regulations."
      "Don't use OrcPub's name, logo or trademark in a way that confuses people (check out our brand guidelines for more details)."]}
    {:title "Site security and access"
     :font-size 32
     :paragraphs
     ["To keep OrcPub secure, we ask that you please:"
      "Don't access, use or tamper with our systems or our technical providers' systems."
      "Don't break or circumvent our security measures or test the vulnerability of our systems or networks."
      "Don't use any undocumented or unsupported method to access, search, scrape, download or change any part of OrcPub."
      "Don't try to reverse engineer our software."
      "Don't try to interfere with people on OrcPub or our hosts or networks, like sending a virus, overloading, spamming or mail-bombing."
      "Don't collect or store personally identifiable information from OrcPub or people on OrcPub without permission."
      "Don't share your password, let anyone access your account or do anything that might put your account at risk."
      "Don't sell access to your account, boards, or username, or otherwise transfer account features for compensation."]}
    {:title "Spam"
     :font-size 32
     :paragraphs
     ["Nobody likes spam or other disruptive content. Which is why we remove accounts for stuff like:"
      "Unsolicited commercial messages."
      "Attempts to artificially boost views and other metrics."
      "Repetitive or unwanted posts."
      "Off-domain redirects, cloaking or other ways of obscuring where content leads."
      "Misleading content."]}]})

(defn community-guidelines []
  (terms-page community-guidelines-section))

(def cookie-policy-section
  {:title "Cookies"
   :font-size 48
   :subsections
   [{:title "Cookies on OrcPub"
     :font-size 32
     :paragraphs
     ["Our privacy policy describes how we collect and use information, and what choices you have. One way we collect information is through the use of a technology called \"cookies.\" We use cookies for all kinds of things on OrcPub."]}
    {:title "What's a cookie?"
     :font-size 32
     :paragraphs
     ["When you go online, you use a program called a \"browser\" (like Apple's Safari or Google's Chrome). Most websites store a small amount of text in the browser—and that text is called a \"cookie.\""]}
    {:title "How we use cookies"
     :font-size 32
     :paragraphs
     ["We use cookies for lots of essential things on OrcPub—like helping you log in and tailoring your OrcPub experience. Here are some specifics on how we use cookies."]}
    {:title "What we use cookies for"
     :font-size 32
     :subsections
     [{:title "Personalization"
       :font-size 28
       :paragraphs
       ["Cookies help us remember which content, boards, people or websites you've interacted with so we can show you related content you might like."
        "We also use cookies to help advertisers show you interesting ads."]}
      {:title "Preferences"
       :font-size 28
       :paragraphs
       ["We use cookies to remember your settings and preferences, like the language you prefer and your privacy settings."]}
      {:title "Login"
       :font-size 32
       :paragraphs
       ["Cookies let you log in and out of OrcPub."]}
      {:title "Security"
       :font-size 32
       :paragraphs
       ["Cookies are just one way we protect you from security risks. For example, we use them to detect when someone might be trying to hack your OrcPub account or spam the OrcPub community."]}
      {:title "Analytics"
       :font-size 32
       :paragraphs
       ["We use cookies to make OrcPub better. For example, these cookies tell us how many people use a certain feature and how popular it is, or whether people open an email we send."
        "We also use cookies to help advertisers understand who sees and interacts with their ads, and who visits their website or purchases their products."]}
      {:title "Service providers"
       :font-size 32
       :paragraphs
       ["Sometimes we hire security vendors or use third-party analytics providers to help us understand how people are using OrcPub. Just like we do, these providers may use cookies. Learn more about the third party providers we use."]}]}
    {:title "Where we use cookies"
     :font-size 32
     :paragraphs
     ["We use cookies on orcpub.com, in our mobile applications, and in our products and services (like ads, emails and applications). We also use them on the websites of partners who use OrcPub's Save button, OrcPub widgets, or ad tools like conversion tracking."]}
    {:title "Your options"
     :font-size 32
     :paragraphs
     ["Your browser probably gives you cookie choices. For example, most browsers let you block \"third party cookies,\" which are cookies from sites other than the one you're visiting. Those options vary from browser to browser, so check your browser settings for more info."
      "Some browsers also have a privacy setting called \"Do Not Track,\" which we support. This setting is another way for you to decide whether we use info from our partners and other services to customize OrcPub for you."
      "Effective November 1, 2016"]}]})

(defn cookie-policy []
  (terms-page cookie-policy-section))

(defproject orcpub "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :main orcpub.server

  :min-lein-version "2.7.1"
  
  :repositories [["apache" "http://repository.apache.org/snapshots/"]
                 ["my.datomic.com" {:url "https://my.datomic.com/repo"
                                    :username [:gpg :env]
                                    :password [:gpg :env]}]]
  
  :dependencies [[org.clojure/clojure "1.9.0-alpha16"]
                 [org.clojure/test.check "0.9.0"]
                 [org.clojure/clojurescript "1.9.542"]
                 [org.clojure/core.async "0.3.442"]
                 #_[org.clojure/core.async "0.2.391"
                    :exclusions [org.clojure/tools.reader]]
                 [cljsjs/react "15.3.1-0"]
                 [cljsjs/react-dom "15.3.1-0"]
                 [cljsjs/facebook "v20150729-0"]
                 [cljsjs/google-platformjs-extern "1.0.0-0"]
                 [cljs-http "0.1.43"]
                 [com.andrewmcveigh/cljs-time "0.5.0"]
                 [clj-http "3.6.1"]
                 
                 ;;[org.clojure/core.match "0.3.0-alpha4"]
                 [reagent "0.6.1"]
                 [re-frame "0.9.3"]
                 [react-native-externs "0.0.4"]
                 [garden "1.3.2"]
                 [org.apache.pdfbox/pdfbox "2.1.0-20170324.170253-831"]
                 [io.pedestal/pedestal.service "0.5.1"] 
                 [io.pedestal/pedestal.route "0.5.1"]
                 [io.pedestal/pedestal.jetty "0.5.1"]
                 [org.clojure/data.json "0.2.6"] 
                 [org.slf4j/slf4j-simple "1.7.21"] 
                 [buddy/buddy-auth "1.4.1"]
                 [buddy/buddy-hashers "1.2.0"] 
                 [reloaded.repl "0.2.3"]
                 [bidi "2.0.17"]

                 [com.stuartsierra/component "0.3.2"]
                 [com.google.guava/guava "21.0"]

                 [com.datomic/datomic-pro "0.9.5561"]
                 [com.amazonaws/aws-java-sdk-dynamodb "1.11.6"]
                 [com.fasterxml.jackson.core/jackson-databind "2.7.0"]

                 [binaryage/devtools "0.9.4"]
                 [hiccup "1.0.5"]
                 [com.draines/postal "2.0.2"]
                 [environ "1.1.0"]

                 [figwheel-sidecar "0.5.10"]
                 [pdfkit-clj "0.1.6"]
                 [vvvvalvalval/datomock "0.2.0"]]

  :plugins [[lein-figwheel "0.5.10"]
            [lein-cljsbuild "1.1.6" :exclusions [[org.clojure/clojure]]]
            [lein-garden "0.3.0"]
            [lein-environ "1.1.0"]
            #_[lein-resource "16.9.1"]]

  :aliases {"figwheel-native" ["run" "-m" "user" "--figwheel"]
            "figwheel" ["with-profile" "web-dev" "figwheel"]
            "externs" ["do" "clean"
                       ["run" "-m" "externs"]]
            "rebuild-modules" ["run" "-m" "user" "--rebuild-modules"]
            "prod-build" ^{:doc "Recompile code with prod profile."}
            ["externs"
             ["with-profile" "prod" "cljsbuild" "once" "main"]]}

  :source-paths ["src/clj" "src/cljc" "src/cljs" "env/dev"]

  :test-paths ["test/clj" "test/cljc" "test/cljs"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :resource-paths ["resources" "resources/.ebextensions/*.config"]

  :uberjar-name "orcpub.jar"

  :garden {:builds [{ ;; Optional name of the build:
                     :id "screen"
                     ;; Source paths where the stylesheet source code is
                     :source-paths ["src/clj" "src/cljc"]
                     ;; The var containing your stylesheet:
                     :stylesheet orcpub.styles.core/app
                     ;; Compiler flags passed to `garden.core/css`:
                     :compiler { ;; Where to save the file:
                                :output-to "resources/public/css/compiled/styles.css"
                                ;; Compress the output?
                                :pretty-print? false}}]}

  :cljsbuild {:builds
              [{:id "dev"
                :source-paths ["src/cljc" "src/cljs" "env/dev"]

                ;; the presence of a :figwheel configuration here
                ;; will cause figwheel to inject the figwheel client
                ;; into your build
                :figwheel {:on-jsload "orcpub.core/on-js-reload"
                           ;; :open-urls will pop open your application
                           ;; in the default browser once Figwheel has
                           ;; started and complied your application.
                           ;; Comment this out once it no longer serves you.
                           :open-urls ["http://localhost:3449/index.html"]}

                :compiler {:main orcpub.core
                           :asset-path "/js/compiled/out"
                           :output-to "resources/public/js/compiled/orcpub.js"
                           :output-dir "resources/public/js/compiled/out"
                           :source-map-timestamp true
                           ;; To console.log CLJS data-structures make sure you enable devtools in Chrome
                           ;; https://github.com/binaryage/cljs-devtools
                           :preloads [devtools.preload]}}
               ;; This next build is an compressed minified build for
               ;; production. You can build this with:
               ;; lein cljsbuild once min
               {:id "min"
                :source-paths ["src/cljc" "src/cljs" "env/dev"]
                :compiler {:output-to "resources/public/js/compiled/orcpub.js"
                           :main orcpub.core
                           :optimizations :advanced
                           :pretty-print false}}]}

  :figwheel {:css-dirs ["resources/public/css"]}

  :profiles {:dev {:dependencies [[figwheel-sidecar "0.5.10"]
                                  [com.cemerick/piggieback "0.2.1"]
                                  [org.clojure/test.check "0.9.0"]]
                   :source-paths ["src/cljs" "src/cljc" "env/dev"]
                   :cljsbuild    {:builds [{:id "main"
                                            :source-paths ["src/cljs" "src/native/cljs" "src/cljc" "env/dev"]
                                            :figwheel     true
                                            :compiler     {:output-to     "target/not-used.js"
                                                           :main          "env.main"
                                                           :output-dir    "target"
                                                           :optimizations :none}}]}
                   :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}
             :prod {:cljsbuild {:builds [{:id "main"
                                          :source-paths ["src/cljs" "src/native/cljs" "src/cljc" "env/prod"]
                                          :compiler     {:output-to     "main.js"
                                                         :main          "env.main"
                                                         :output-dir    "target"
                                                         :static-fns    true
                                                         :externs       ["js/externs.js"]
                                                         :parallel-build     true
                                                         :optimize-constants true
                                                         :optimizations :advanced
                                                         :closure-defines {"goog.DEBUG" false}}}]}}
             :web-dev {:dependencies [[binaryage/devtools "0.9.4"]
                                      [figwheel-sidecar "0.5.10"]
                                      [com.cemerick/piggieback "0.2.1"]
                                      [org.clojure/test.check "0.9.0"]]
                       ;; need to add dev source path here to get user.clj loaded
                       :source-paths ["src/web/cljs" "src/cljc" "src/cljs" "dev"]
                       ;; for CIDER
                       ;; :plugins [[cider/cider-nrepl "0.12.0"]]
                       :repl-options { ; for nREPL dev you really need to limit output
                                      :init (set! *print-length* 50)
                                      :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}
             :uberjar {:prep-tasks ["clean" "compile" ["cljsbuild" "once" "prod"]]
                       :env {:production true}
                       :aot :all
                       :omit-source true
                       :cljsbuild {:builds
                                   [{:id "prod"
                                     :source-paths ["src/cljc" "src/web/cljs" "src/cljs"]
                                     :figwheel { :on-jsload "orcpub.core/on-js-reload" }
                                     :compiler {:main orcpub.core
                                                :asset-path "/js/compiled/out"
                                                :output-to "resources/public/js/compiled/orcpub.js"
                                                :optimizations :advanced
                                                :pretty-print false}}]}}})

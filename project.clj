(defproject orcpub "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :main orcpub.server

  :min-lein-version "2.7.1"

  :repositories [["apache" "http://repository.apache.org/snapshots/"]
                 #_["my.datomic.com" {:url "https://my.datomic.com/repo"
                                    :creds :gpg}]]

  :dependencies [[org.clojure/clojure "1.9.0-alpha14"]
                 [org.clojure/test.check "0.9.0"]
                 [org.clojure/clojurescript "1.9.229"]
                 #_[org.clojure/core.async "0.2.391"
                  :exclusions [org.clojure/tools.reader]]
                 [cljsjs/react "15.3.1-0"]
                 [cljsjs/react-dom "15.3.1-0"]
                 [cljs-http "0.1.43"]
                 
                 ;;[org.clojure/core.match "0.3.0-alpha4"]
                 [re-frame "0.9.0"]
                 [reagent "0.6.0"]
                 [garden "1.3.2"]
                 [org.apache.pdfbox/pdfbox "2.1.0-20170316.190223-802"]

                 #_[org.eclipse.jetty/jetty-util "9.4.0.v20161208"]
                 #_[org.eclipse.jetty/jetty-http "9.4.0.v20161208"]
                 #_[org.eclipse.jetty/jetty-client "9.4.0.v20161208"]

                 [io.pedestal/pedestal.service "0.5.1"]
                 [io.pedestal/pedestal.route "0.5.1"]
                 [io.pedestal/pedestal.jetty "0.5.1"]
                 [org.clojure/data.json "0.2.6"]
                 [org.slf4j/slf4j-simple "1.7.21"]
                 [buddy/buddy-auth "1.4.1"]
                 [bidi "2.0.17"]

                 #_[com.datomic/datomic-pro "0.9.5561"]
                 #_[com.datomic/clj-client "0.8.606"]
                 
                 #_[clj-http "2.3.0"]
                 [environ "1.0.0"]]

  :plugins [[lein-figwheel "0.5.8"]
            [lein-cljsbuild "1.1.4" :exclusions [[org.clojure/clojure]]]
            [lein-garden "0.3.0"]]

  :source-paths ["src/clj" "src/cljc" "src/cljs"]

  :test-paths ["test/clj" "test/cljc" "test/cljs"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :uberjar-name "orcpub.jar"

  :garden {:builds [{;; Optional name of the build:
                     :id "screen"
                     ;; Source paths where the stylesheet source code is
                     :source-paths ["src/clj" "src/cljc"]
                     ;; The var containing your stylesheet:
                     :stylesheet orcpub.styles.core/app
                     ;; Compiler flags passed to `garden.core/css`:
                     :compiler {;; Where to save the file:
                                :output-to "resources/public/css/compiled/styles.css"
                                ;; Compress the output?
                                :pretty-print? false}}]}

  :cljsbuild {:builds
              [{:id "dev"
                :source-paths ["src/cljc" "src/cljs"]

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
                           :asset-path "js/compiled/out"
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
                :source-paths ["src/cljc" "src/cljs"]
                :compiler {:output-to "resources/public/js/compiled/orcpub.js"
                           :main orcpub.core
                           :optimizations :advanced
                           :pretty-print false}}]}

  :figwheel {;; :http-server-root "public" ;; default and assumes "resources"
             ;; :server-port 3449 ;; default
             ;; :server-ip "127.0.0.1"

             :css-dirs ["resources/public/css"] ;; watch and update CSS

             ;; Start an nREPL server into the running figwheel process
             ;; :nrepl-port 7888

             ;; Server Ring Handler (optional)
             ;; if you want to embed a ring handler into the figwheel http-kit
             ;; server, this is for simple ring servers, if this

             ;; doesn't work for you just run your own server :) (see lein-ring)

             ;; :ring-handler hello_world.server/handler

             ;; To be able to open files in your editor from the heads up display
             ;; you will need to put a script on your path.
             ;; that script will have to take a file path and a line number
             ;; ie. in  ~/bin/myfile-opener
             ;; #! /bin/sh
             ;; emacsclient -n +$2 $1
             ;;
             ;; :open-file-command "myfile-opener"

             ;; if you are using emacsclient you can just use
             ;; :open-file-command "emacsclient"

             ;; if you want to disable the REPL
             ;; :repl false

             ;; to configure a different figwheel logfile path
             ;; :server-logfile "tmp/logs/figwheel-logfile.log"
             }


  ;; setting up nREPL for Figwheel and ClojureScript dev
  ;; Please see:
  ;; https://github.com/bhauman/lein-figwheel/wiki/Using-the-Figwheel-REPL-within-NRepl


  :profiles {:dev {:dependencies [[binaryage/devtools "0.8.2"]
                                  [figwheel-sidecar "0.5.8"]
                                  [com.cemerick/piggieback "0.2.1"]]
                   ;; need to add dev source path here to get user.clj loaded
                   :source-paths ["src/clj" "src/cljc" "src/cljs" "dev"]
                   ;; for CIDER
                   ;; :plugins [[cider/cider-nrepl "0.12.0"]]
                   :repl-options {; for nREPL dev you really need to limit output
                                  :init (set! *print-length* 50)
                                  :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}
             :uberjar {:prep-tasks ["clean" "compile" ["cljsbuild" "once" "prod"]]
                       :env {:production true}
                       :aot :all
                       :omit-source true
                       :cljsbuild {:builds
                                   [{:id "prod"
                                     :source-paths ["src/cljc" "src/cljs"]
                                     :figwheel { :on-jsload "orcpub.core/on-js-reload" }
                                     :compiler {:main orcpub.core
                                                :asset-path "/js/compiled/out"
                                                :output-to "resources/public/js/compiled/orcpub.js"
                                                :optimizations :advanced
                                                :pretty-print false}}]}}}

  )

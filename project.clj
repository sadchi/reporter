(defproject reporter "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license      {:name "Eclipse Public License"
                 :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2371"]
                 [figwheel "0.1.6-SNAPSHOT"]
                 [reagent "0.4.3"]]

  :jvm-opts     ["-Xmx1G"]

  :plugins      [[lein-cljsbuild "1.0.2"]
                 [lein-figwheel "0.1.6-SNAPSHOT"]
                 [com.cemerick/austin "0.1.5"]]

  :hooks [leiningen.cljsbuild]

  :source-paths ["src"]

  :profiles {
             :prod {
                    :cljsbuild {
                                :builds
                                {:reporter {:compiler
                                            {:optimizations :advanced
                                             :preamble ^:replace ["reagent/react.min.js"]
                                             :pretty-print false}}}}}
             :srcmap {:cljsbuild
                      {:builds
                       {:reporter {:compiler
                                 {:source-map "target/reporter.js.map"
                                  :source-map-path "reporter"}}}}}}

  :cljsbuild    {
                 :builds {
                          :reporter {
                                   :source-paths ["src/reporter" "src/figwheel" "src/brepl"]
                                   :compiler {
                                              :preamble ["reagent/react.js"]
                                              :output-to "target/reporter.js"
                                              :output-dir "target/out"
                                              :optimizations :none
                                              :source-map true
                                              }
                                   }
                          }
                 }

  :figwheel     {
                 :http-server-root "public"
                 :port 3449
                 :css-dirs ["resources/public/css"]
                 }


  :repl-options  {
                  :port 3450
                  :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]
                  }

  )

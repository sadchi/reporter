(defproject reporter "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license      {:name "Eclipse Public License"
                 :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2371"]
                 [figwheel "0.1.6-SNAPSHOT"]]

  :jvm-opts     ["-Xmx1G"]

  :plugins      [[lein-cljsbuild "1.0.3"]
                 [lein-figwheel "0.1.6-SNAPSHOT"]
                 [com.cemerick/austin "0.1.5"]
                 ]

  :cljsbuild    {
                 :builds [
                          {:id "dev"
                           :source-paths ["src/reporter" "src/figwheel" "src/brepl"]
                           :compiler {
                                      :output-to "resources/public/reporter.js"
                                      :output-dir "resources/public/out"
                                      :optimizations :none
                                      :source-map true
                                      }
                           }
                          {:id "release"
                           :source-paths ["src/reporter"]
                           :compiler {
                                      :output-to "resources/public/reporter_prod.js"
                                      :output-dir "resources/public/prod-out"
                                      :optimizations :advanced
                                      :source-map "resources/public/reporter_prod.js.map"
                                      }
                           }]
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

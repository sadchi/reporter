(defproject reporter "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2850"]
                 [reagent "0.4.3"]
                 [hiccup "1.0.5"]
                 [cljs-uuid "0.0.4"]
                 [clj-stacktrace "0.2.7"]
                 [org.clojure/core.match "0.3.0-alpha4"]
                 [org.clojure/core.incubator "0.1.3"]]

  :plugins [[lein-cljsbuild "1.0.4"]
            [lein-haml-sass "0.2.7-SNAPSHOT"]]

  :haml {:src "haml"
         :output-directory "target"}

  :sass {:src "sass"
         :output-directory "target"
         :output-extension "css"
         :delete-output-dir true}

  :cljsbuild {:builds
              {:reporter
               {:source-paths ["src"]
                :compiler {:output-to "target/report.js"
                           :output-dir "target/out"
                           :source-map "target/out.js.map"
                           :optimizations :whitespace
                           :pretty-print true}}}}

  :profiles {:dev
             {:plugins [[com.cemerick/austin "0.1.6"]]
              :source-paths ["src"]
              :cljsbuild {:builds {:reporter
                                   {:compiler
                                    {:preamble ["reagent/react.js"]}}}}}
             :prod
             {:cljsbuild {:builds
                          {:reporter
                           {:compiler {:preamble ["reagent/react.min.js"]
                                       :optimizations :advanced
                                       :pretty-print false}}}}}}

  :repl-options {:caught clj-stacktrace.repl/pst+
                 :host "0.0.0.0"
                 :port 4001}

  :aliases {"bj" ["do" ["cljsbuild" "once"]]
            "bh" ["do" ["haml" "once"]]
            "bc" ["do" ["sass" "once"]]
            "bjprod" ["with-profile" "prod" "cljsbuild" "once"]})

(defproject reporter "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2371"]
                 [reagent "0.4.3"]
                 [hiccup "1.0.5"]
                 [cljs-uuid "0.0.4"]
                 [clj-stacktrace "0.2.7"]]

  :plugins      [[lein-cljsbuild "1.0.3"]
                 [lein-haml-sass "0.2.7-SNAPSHOT"]]


  :haml         {
                 :src "haml"
                 :output-directory "."
                 }
  
  :sass         {
                 :src "sass"
                 :output-directory "css"
                 :output-extension "css"
                 :delete-output-dir true
                 }

  :cljsbuild    {
                 :builds {
                          :reporter {
                                     :source-paths ["src"]
                                     :compiler {
                                                :preamble ["reagent/react.js"]
                                                :output-to "target/reporter.js"
                                                :output-dir "target/out"
                                                :optimizations :simple 
                                                :source-map "target/reporter.js.map"
                                                }
                                     }
                          }
                 }

  :repl-options {
                 :caught clj-stacktrace.repl/pst+
                 :host "0.0.0.0"
                 :port 4001
                 }

  :aliases      {
                 "bj" ["do" "clean" ["cljsbuild" "once"]]
                 "bh" ["do" ["haml" "once"]]
                 "bc" ["do" ["sass" "once"]]
                 }

  )

(defproject reporter "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2371"]
                 [reagent "0.4.3"]]

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
                                                :pretty-print true
                                                :source-map "target/reporter.js.map"
                                                }
                                     }
                          }
                 }

  :aliases      {
                 "bj" ["do" "clean" ["cljsbuild" "once"]]
                 "bh" ["do" "clean" ["haml" "once"]]
                 "bc" ["do" "clean" ["sass" "once"]]
                 }

  )

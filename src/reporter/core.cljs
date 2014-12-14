(ns reporter.core
  (:require [reagent.core :as r :refer [atom]]))

(def counter (atom 0))

(defn header []
  [:div.header "FAILED"])

(defn footer []
  [:div.footer "ahuba production"])

(defn body []
  [:div.container "TEST"])

(defn main []
  [:div [header]
   [body]
   [footer]])
    

(defn ^:export run []
  (r/render-component [main] (.-body js/document)))

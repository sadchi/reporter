(ns reporter.core
  (:require [reagent.core :as r :refer [atom]]))

(def counter (atom 0))
(def data (js->clj js/data :keywordize-keys true))

(defn header []
  [:div.header "FAILED"])

(defn footer []
  [:div.footer "ahuba production"])

(defn body []
  [:div.container "TEST"])

(defn main []
  (let [ [first & rest] data]
    (.log js/console (:status first)))   
  [:div [header]
   [body]
   [footer]])
    

(defn ^:export run []
  (r/render-component [main] (.-body js/document)))

(ns reporter.core
  (:require [reagent.core :as r :refer [atom]]))

(defn home []
  [:div
   [:div 
    [:h1 "Reagent Examle"]]])

(r/render-component [home] (.getElementById js/document "app"))

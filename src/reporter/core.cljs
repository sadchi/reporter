(ns reporter.core
  (:require [reagent.core :as r :refer [atom]]))

(defn main []
  [:div
   [:div 
    [:h1 "Reagent Examle"]]])

(defn ^:export run []
  (r/render-component [main] (.-body js/document)))

(ns reporter.options
  (:require [reagent.core :as r]))


(defn mk-options []
  (r/atom {:status-filter []
           :empty-meta-filter false}))

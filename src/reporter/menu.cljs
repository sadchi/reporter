(ns reporter.menu
  (:require [reporter.state :as state]
            [reagent.core :as r]))


(def options-opened (r/atom false))

(defn report-status [state-map]
  [:div
   (let [state (get state-map [])
         state-atom (deref (state/get-state-atom state))
         status (:status state-atom)]
     [:div.menu__label.menu--grow.heading.heading--h1 
      (when (= status "FAIL") {:class "error"}) 
      (str status "!") ])])


(defn menu [state-map]
  [:div.menu
   [:div.menu__header
    [report-status state-map]]
   (when @options-opened 
     [:div.menu__content
      #_[radio-buttons-group {:group-name "Display options"
                            :values {:failsonly "Only failed tests"
                                     :passonly "Only successful tests"
                                     :both "Both"}
                            :state-to-update options
                            :update-path [:pass-fail-filter]}]])])


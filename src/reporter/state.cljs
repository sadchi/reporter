(ns reporter.state
  (:require [reagent.core :as r]))

(def ^:private id (atom 0))

(defn default-state []
  {:id (swap! id inc) 
   :state (r/atom {
                   :opened false
                   :visible true
                   :reason nil
                   :count 0
                   :fail-count 0
                   :status "SUCCESS"})})


(defn get-state-atom [x]
  (get x :state))

(defn inc-count [state]
  (swap! (get-state-atom state) update-in [:count] inc))

(defn dec-count [state]
  (swap! (get-state-atom state) update-in [:count] dec))

(defn inc-fail-count [state]
  (swap! (get-state-atom state) update-in [:fail-count] inc))

(defn dec-fail-count [state]
  (swap! (get-state-atom state) update-in [:fail-count] dec))

(defn apply-fail-status [state]
  (swap! (get-state-atom state) update-in [:status] #(identity "FAIL")))

(defn flip-opened [state]
  (swap! (get-state-atom state) update-in [:opened] not))


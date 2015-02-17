(ns reporter.state
  (:require [reagent.core :as r]
            [clojure.set :refer [intersection]]))

(def ^:private id-seq (atom 0))

(defn gen-id []
  (swap! id-seq inc))

(defprotocol State
  "Operations for a state"
  (id [this])
  (opened? [this])
  (visible? [this])
  (get-count [this] [this status])
  (reduce-statuses [this f init] "Reduces with f not 0 statuses")
  (update-it! [this f] "Update state atom with a function"))


(extend-type reagent.ratom/RAtom
  State
  (id [state] (:id @state))

  (opened? [state] (:opened? @state))

  (visible? [state] (:visible? @state))

  (get-count [state] (reduce + (vals (:statuses @state))))

  (get-count [state status] (get-in @state [:statuses status] 0))

  (reduce-statuses [state f init]
    (->> (:statuses @state)
         (filter (fn [x] (let [[k v] x] (pos? v))))
         (map first)
         (reduce f init)))

  (update-it! [state f] (swap! state f)))

(defn mk-state []
  (r/atom {:id       (gen-id)
           :statuses {}
           :reasons  #{}
           :visible? true
           :opened?  false}))

(defn- inc-nilable [x]
  (if x
    (inc x)
    1))

(defn inc-status [status state]
  (update-in state [:statuses status] inc-nilable))

(defn flip-opened [state]
  (update-in state [:opened?] not))

(defn set-opened [state]
  (update-in state [:opened?] (fn [_] true)))

(defn set-not-opened [state]
  (update-in state [:opened?] (fn [_] false)))


(defn undo-status-filter [state]
  (let [reasons (get state :reasons)
        new-reasons (disj reasons :status)
        new-visibility (if (= 0 (count new-reasons))
                         true
                         false)]
    (assoc state :reasons new-reasons :visible? new-visibility)))

(defn- get-non-zero-statuses [coll x]
  (let [[k v] x]
    (if (pos? v)
      (conj coll k)
      coll)))

(defn apply-status-filter [expected-statuses state]
  (let [exp-statuses (set expected-statuses)
        act-statuses (reduce get-non-zero-statuses #{} (get state :statuses))
        reasons (get state :reasons)
        intersection-statuses (intersection exp-statuses act-statuses)]
    (if (pos? (count intersection-statuses))
      state
      (assoc state :reasons (conj reasons :status) :visible? false))))


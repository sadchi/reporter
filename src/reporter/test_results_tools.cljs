(ns reporter.test-results-tools
  (:require [reporter.state :as state]))

(def ^:private status-weight-map (clj->js ["FAIL" "ERROR" "UNDEFINED" "SKIPPED" "SUCCESS"]))

(def ^:private bad-statuses #{"FAIL" "ERROR"})

(def ^:private status-class-map
  {"FAIL" "error"
   "ERROR" "error"
   "SKIPPED" ""
   "UNDEFINED" ""
   "SUCCESS" ""})


(defn- status-weight [x]
  (.indexOf status-weight-map x))

(defn- compare-status [fn s1 s2]
  (let [s1-w (status-weight s1)
        s2-w (status-weight s2)]
    (if (fn s1-w s2-w)
      s1
      s2)))

(defn better-status [s1 s2]
  (compare-status > s1 s2))

(defn worse-status [s1 s2]
  (compare-status < s1 s2))

(defn status->class [s]
  (get status-class-map s ""))

(defn bad-status? [s]
  (contains? bad-statuses s))

(defn simple-status-reduce-f [s1 s2]
  (cond
    (= s1 "FAIL") "FAIL"
    (= s2 "FAIL") "FAIL"
    :else s1))


(defn get-status [state]
  (state/reduce-statuses state worse-status "SUCCESS"))

(defn get-test-info [test-results path]
  (->> test-results
       (filter (comp (partial = path) :path))
       (first)))
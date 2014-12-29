(ns reporter.initial-processing
  (:require [reporter.flat-tree :as f-tree]
            [reporter.state :as state]))


(defn build-report-structure [coll path-field content]
  (let [paths (map #(get % path-field) coll)]
    (reduce #(assoc-in %1 %2 content) {} paths)))



(defn- add-content [f coll x]
  (assoc coll x (f)))

(defn build-state-map [{:keys [coll
                               path-field
                               status-field
                               default-atom-fn]}]
  (let [paths (map #(get % path-field) coll)
        branches (map f-tree/create-branch paths)
        branches-comb (set (reduce into [] branches))
        state-map (reduce (partial add-content state/default-state) {} branches-comb)]
    (doseq [item coll]
      (let [path (get item path-field)
            status (get item status-field)
            func-list (if (= status "FAIL")
                        [state/inc-count state/inc-fail-count state/apply-fail-status]
                        [state/inc-count])] 
        (f-tree/update-up-root state-map path func-list)))
    state-map))








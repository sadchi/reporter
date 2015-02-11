(ns reporter.overall-stats
  (:require [reporter.state :as state]
            [reporter.headings :as h]))


(def overview-section-level 4)

(defn calculate-test-quantity [test-results]
  ((comp count set)
   (loop [t-res test-results
          acc nil]
     (let [others (rest t-res)
           item (first t-res)
           path (:path item)
           filename (:filename item)]
       (if (empty? item)
         acc
         (->> path
              (#(if (= filename (peek %))
                  (pop %)
                  %))
              (conj acc)
              (recur others)))))))

(defn calculate-files-quantity [test-results]
  (+ 
   (->> test-results
        (map :filename)
        (filter (complement nil?))
        (set)
        (count))
   (->> test-results
        (map :filecount)
        (filter (complement nil?))
        (reduce +))))

(defn calculate-combinations [test-results]
  (count test-results))

(defn calculate-issues [test-results]
  (->> test-results
       (map :status)
       (filter (partial #{"FAIL" "ERROR"}))
       (count)))



(defn calculate-overall-stats [test-results]
  (let [total-tests (calculate-test-quantity test-results)
        total-files (calculate-files-quantity test-results)
        total-combinations (calculate-combinations test-results)
        total-issues (calculate-issues test-results)
        fail-rate (* 100 (/ total-issues total-combinations))]
    {:total-tests total-tests
     :total-files total-files
     :total-combinations total-combinations
     :total-issues total-issues
     :fail-rate (.toFixed fail-rate 2)}))

(defn overview-content [overall-stats]
  (let [{:keys [total-tests total-files total-combinations total-issues fail-rate]} overall-stats]
   [:div.inner-content
   [:table.simple-table.simple-table--no-borders
    [:tr
     [:td.simple-table__td.simple-table--no-borders.simple-table--20.simple-table--right
      "Total tests:"] 
     [:td.simple-table__td.simple-table--no-borders
      total-tests]]
    
    [:tr.simple-table--odd
     [:td.simple-table__td.simple-table--no-borders.simple-table--20.simple-table--right
      "Total files:"] 
     [:td.simple-table__td.simple-table--no-borders
      total-files]]
    
    [:tr
     [:td.simple-table__td.simple-table--no-borders.simple-table--20.simple-table--right
      "Total combinations:"] 
     [:td.simple-table__td.simple-table--no-borders
      total-combinations]]

    [:tr.simple-table--odd.error
     [:td.simple-table__td.simple-table--no-borders.simple-table--20.simple-table--right
      "Total issues:"]
     [:td.simple-table__td.simple-table--no-borders
      total-issues]]

    [:tr
     [:td.simple-table__td.simple-table--no-borders.simple-table--20.simple-table--right
      "Fail rate:"] 
     [:td.simple-table__td.simple-table--no-borders
      (str fail-rate "%")]]
    
    ]]))

(defn overview-section [overall-stats state-map path]
  (let [state (get state-map path)
        opened (state/opened? state)]
    [:div.section
     [h/section-head {:level overview-section-level
                    :opened opened
                    :status "UNDEFINED"
                    :state state
                    :path path}]
     (when opened [overview-content overall-stats])]))


(defn overview-section-alt [overall-stats state]
  (let [opened (state/opened? state)]
    [:div.section
     [h/section-head {:level overview-section-level
                      :opened opened
                      :status "UNDEFINED"
                      :state state
                      :path ["Overview"]}]
     (when opened [overview-content overall-stats])]))


(ns reporter.overall-stats
  (:require [reporter.state :as state]
            [reporter.test-results-tools :as t-res-t]
            [reporter.headings :as h]))


(def overview-section-level 4)

(defn- calculate-test-quantity [test-results]
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

(defn- calculate-files-quantity [test-results]
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

(defn- calculate-combinations [test-results]
  (count test-results))

(defn- calculate-issues [test-results]
  (->> test-results
       (map :status)
       (filter (partial #{"FAIL" "ERROR"}))
       (count)))



(defn- calculate-per-status [test-results]
  (let [calc-statuses (fn [coll x]
                        (->> (get coll x 0)
                             (inc)
                             (assoc coll x)))]
    (->> (map :status test-results)
         (reduce calc-statuses {}))))

(defn calculate-overall-stats [test-results]
  (let [total-tests (calculate-test-quantity test-results)
        total-files (calculate-files-quantity test-results)
        total-combinations (calculate-combinations test-results)
        total-issues (calculate-issues test-results)
        fail-rate (if-not (zero? total-combinations)
                    (* 100 (/ total-issues total-combinations))
                    0)
        per-status (calculate-per-status test-results)]
    {:total-tests        total-tests
     :total-files        total-files
     :total-combinations total-combinations
     :total-issues       total-issues
     :fail-rate          (.toFixed fail-rate 2)
     :per-status         per-status}))

(defn overview-content [overall-stats]
  (let [{:keys [total-tests total-files total-combinations total-issues fail-rate per-status]} overall-stats
        bad-statuses (filter (fn [x] (let [[k _] x] (t-res-t/bad-status? k))) per-status)
        other-statuses (filter (fn [x] (let [[k _] x] (not (t-res-t/bad-status? k)))) per-status)
        total-issues-caption (conj [:span "Total issues:"] (for [status bad-statuses] ^{:key (state/gen-id)} [:p (first status)]))
        total-issues-counts (conj [:span total-issues] (for [status bad-statuses] ^{:key (state/gen-id)} [:p (second status)]))
        other-statuses-caption (conj [:span "Other statuses:"] (for [status other-statuses] ^{:key (state/gen-id)} [:p (first status)]))
        other-statuses-counts (conj [:span "\u00A0"] (for [status other-statuses] ^{:key (state/gen-id)} [:p (second status)]))]

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
        total-issues-caption]
       [:td.simple-table__td.simple-table--no-borders
        total-issues-counts]]

      [:tr
       [:td.simple-table__td.simple-table--no-borders.simple-table--20.simple-table--right
        "Fail rate:"]
       [:td.simple-table__td.simple-table--no-borders
        (str fail-rate "%")]]

      [:tr.simple-table--odd
       [:td.simple-table__td.simple-table--no-borders.simple-table--20.simple-table--right
        other-statuses-caption]
       [:td.simple-table__td.simple-table--no-borders
        other-statuses-counts]]

      ]]))

(defn overview-section [overall-stats state-map path]
  (let [state (get state-map path)
        opened (state/opened? state)]
    [:div.section
     [h/section-head {:level  overview-section-level
                      :opened opened
                      :status "UNDEFINED"
                      :state  state
                      :path   path}]
     (when opened [overview-content overall-stats])]))


(defn overview-section-alt [overall-stats state]
  (let [opened (state/opened? state)]
    [:div.section
     [h/section-head {:level  overview-section-level
                      :opened opened
                      :status "UNDEFINED"
                      :state  state
                      :path   ["Overview"]}]
     (when opened [overview-content overall-stats])]))


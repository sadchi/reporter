(ns reporter.tree
  (:require [reporter.state :as state]
            [reporter.path :as path]
            [reporter.tools :as tools]
            [reporter.flat-tree :as f-tree]
            [reporter.test-results :as t-res]
            [reporter.test-results-tools :as t]
            [reporter.headings :as h]
            [clojure.string :refer [join lower-case]]))


(def ^:private non-terminal-node-level 4)


(defn sorted-keys-path-aware [coll]
  (let [ks (keys coll)]
    (sort-by (comp lower-case path/name-path-node) ks)))


(defn process-node [tree-node state-map test-results path]
  (if (= tree-node :test)
    [t-res/test-result state-map test-results path]
    (let [state (get state-map (path/flatten-path path))
          opened (state/opened? state)
          status (t/get-status state)
          total (state/get-count state)
          fails (state/get-count state "FAIL")
          errors (state/get-count state "ERROR")
          level (dec (count path))
          level-sec-class (str "section--lvl-" level)
          id (state/id state)]
      ^{:key id} [:div.section {:class level-sec-class}
                  (list
                    ^{:key (str id ".1")}
                    [h/section-head {:level  non-terminal-node-level
                                     :opened opened
                                     :status status
                                     :path   path
                                     :state  state
                                     :extra  (h/heading-fails-stat (+ fails errors) total)
                                     }]
                    (when opened
                      (doall (for [k (sorted-keys-path-aware tree-node)]
                               (let [next-node (get tree-node k)
                                     next-path (conj path k)
                                     next-state (get state-map next-path)
                                     next-id (state/id next-state)]
                                 ^{:key next-id} [process-node next-node state-map test-results next-path])))))])))




(defn root [sub-items]
  (fn []
    (into [:div] (for [item sub-items] [item]))))


(defn node [state path sub-items]
  (fn []
    (let [status (t/get-status state)
          opened (state/opened? state)
          total (state/get-count state)
          fails (state/get-count state "FAIL")
          errors (state/get-count state "ERROR")
          level (dec (count path))
          level-sec-class (str "section--lvl-" level)
          id (state/id state)
          body [:div.section {:class level-sec-class :key id}
                [h/section-head {:level  non-terminal-node-level
                                 :opened opened
                                 :status status
                                 :path   path
                                 :state  state
                                 :extra  (h/heading-fails-stat (+ fails errors) total)}]]
          _ (tools/log-obj "sub-items " sub-items)]
      (if opened
        (into body (for [item sub-items] [item]))
        body))))

(defn- tree-node [{:keys [path structure state-map test-results]}]
  (let [flat-path (path/flatten-path path)
        state (get state-map flat-path)
        sub-tree (get-in structure path)]
    (if (= sub-tree :test)
      (let [test-info (t-res-t/get-test-info test-results flat-path)]
        [t-res/test-result-alt state path test-info])
      (let [opened (state/opened? state)
            status (t-res-t/get-status state)
            total (state/get-count state)
            fails (state/get-count state "FAIL")
            errors (state/get-count state "ERROR")
            level (dec (count path))
            level-sec-class (str "section--lvl-" level)
            id (state/id state)]
        ^{:key id} [:div.section {:class level-sec-class}
                    (list
                      [h/section-head {:level  non-terminal-node-level
                                       :opened opened
                                       :status status
                                       :path   path
                                       :state  state
                                       :extra  (h/heading-fails-stat (+ fails errors) total)}]

                      (when opened
                        (for [k (sorted-keys-path-aware tree-node)]
                          (tree-node {:path         (conj path k)
                                      :structure    structure
                                      :state-map    state-map
                                      :test-results test-results}))))]))))

(defn tree [{:keys [structure state-map test-results]}]
  [:div
   (for [k (keys structure)]
     (tree-node {:path         [k]
                 :structure    structure
                 :state-map    state-map
                 :test-results test-results}))])


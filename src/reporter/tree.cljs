(ns reporter.tree
  (:require [reporter.state :as state]
            [reporter.path :as path]
            [reporter.tools :as tools]
            [reporter.flat-tree :as f-tree]
            [reporter.test-results :as t-res]
            [reporter.test-results-tools :as t-res-t]
            [reporter.headings :as h]
            [clojure.string :refer [join lower-case]]))


(def ^:private non-terminal-node-level 4)


(defn- sort-by-str-val [coll]
  (sort-by (comp lower-case path/name-path-node) coll))

(defn- sorted-keys-path-aware [coll]
  (let [ks (keys coll)]
    (sort-by-str-val ks)))


(defn root [sub-items]
  (fn []
    (into [:div] (for [item sub-items] [item]))))


(defn node [state path sub-items]
  (fn []
    (let [status (t-res-t/get-status state)
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
  (fn []
    ;(tools/log "tree-node rendering")
    (let [flat-path (path/flatten-path path)
          state (get state-map flat-path)
          sub-tree (get-in structure path)]
      (when (state/visible? state)
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
                id (state/id state)
                body [:div.section {:class level-sec-class :key id}
                      [h/section-head {:level  non-terminal-node-level
                                       :status status
                                       :path   path
                                       :state  state
                                       :extra  (h/heading-fails-stat (+ fails errors) total)}]]]

            (if opened
              (into body (for [k (sorted-keys-path-aware sub-tree)]
                           [tree-node {:path         (conj path k)
                                       :structure    structure
                                       :state-map    state-map
                                       :test-results test-results}]))
              body)))))))

(defn- tree [{:keys [structure state-map test-results]}]
  (fn []
    ;(tools/log "tree rendering")
    (into [:div]
          (for [k (sorted-keys-path-aware structure)]
            [tree-node {:path         [k]
                        :structure    structure
                        :state-map    state-map
                        :test-results test-results}]))))



(defn- flat-results-item [{:keys [state path test-info]}]
  (fn []
    (let [visible (state/visible? state)]
      (when visible
        [t-res/test-result-alt state path test-info]))))

(defn- flat-results [{:keys [state-map test-results]}]
  (fn []
    (let [paths (sort-by-str-val (map :path test-results))]
      (into [:div] (for [path paths
                         :let [state (get state-map path)
                               test-info (t-res-t/get-test-info test-results path)]]
                     [flat-results-item {:state     state
                                         :path      path
                                         :test-info test-info}])))))

(defn results-view [{:keys [state-map flat-view-atom expand-all-atom] :as params}]
  (fn []
    (when @expand-all-atom
      (doseq [state (vals state-map)]
        (state/update-it! state state/set-opened))
      (swap! expand-all-atom not))
    [:div
     (if @flat-view-atom
       [flat-results params]
       [tree params])]))
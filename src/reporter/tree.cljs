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


(defn- tree-node [{:keys [path structure state-map test-results]}]
  (fn []
    ;(tools/log "tree-node rendering")
    (let [flat-path (path/flatten-path path)
          state (get state-map flat-path)
          sub-tree (get-in structure path)]
      (when (or (nil? state) (state/visible? state))
        (cond
          (and (vector? sub-tree) (= :test (first sub-tree))) (let [test-info (t-res-t/get-test-info test-results flat-path)]
                                                                [t-res/test-result-alt {:state     state
                                                                                        :path      path
                                                                                        :test-info test-info}])
          (and (vector? sub-tree) (= :external (first sub-tree))) [(second sub-tree)]
          :else (let [opened (state/opened? state)
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
                                             :text   (path/name-path-node (peek path))
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
        [t-res/test-result-alt {:state     state
                                :caption   (path/name-path-node path)
                                :test-info test-info}]))))

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
    [:div
     (if @flat-view-atom
       [flat-results params]
       [tree params])]))
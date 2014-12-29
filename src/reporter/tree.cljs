(ns reporter.tree
  (:require [reporter.state :as state]
            [reporter.test-results :as t-res]
            [reporter.headings :as h]))


(def ^:private non-terminal-node-level 2)


(defn process-node [tree-node state-map test-results path]
  (if (= tree-node :test)
    [t-res/test-result state-map  test-results path]
    (let [state (get state-map path)
          state-atom (deref (state/get-state-atom state))
          opened (get state-atom :opened)
          status (get state-atom :status)
          total (get state-atom :count) 
          fails (get state-atom :fail-count)
          id (get state :id)]
      [:div.section {:key id}
       (list
        ^{:key (str id ".1")} 
        [h/section-head {:level non-terminal-node-level
                       :opened opened
                       :status status
                       :path path
                       :state state
                       :extra (h/heading-fails-stat fails total)
                       }]
        (when opened
          (for [k (sort (keys tree-node))]
            (let [next-node (get tree-node k)
                  next-path (conj path k)
                  next-state (get state-map next-path)
                  next-id (:id next-state)]
              ^{key next-id} [process-node next-node state-map test-results next-path]))))])))



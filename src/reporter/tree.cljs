(ns reporter.tree
  (:require [reporter.state :as state]
            [reporter.test-results :as t-res]
            [clojure.string :refer [join]]))




(defn group-sign [opened]
  (let [class (if opened
                "group-opened-sign"
                "group-closed-sign"
               )]
    [:span {:class class}]))

(defn heading [level s extra-classes]
  (let [class (cond
               (< 0 level 6) (str "heading--h" level)
               :else "heading--h5"
               )]
    [:div {:class (join " " ["heading" class "section__head--grow" extra-classes])} s]))

(defn section-head [{:keys [level opened status path state]}]
  [:div.section__head 
   {:on-click #(state/flip-opened state) }
   [group-sign opened]
   [heading level (peek path) (when (= status "FAIL") "error")]])

(defn process-node [tree-node state-map path]
  (if (= tree-node :test)
    [t-res/test-result state-map path]
    (let [state (get state-map path)
          state-atom (deref (state/get-state-atom state))
          opened (get state-atom :opened)
          status (get state-atom :status)
          id (get state :id)]
      [:div.section {:key id}
       (list
        ^{:key (str id ".1")} 
        [section-head {:level (count path)
                       :opened opened
                       :status status
                       :path path
                       :state state
                       }]
        (when opened
          (for [k (sort (keys tree-node))]
            (let [next-node (get tree-node k)
                  next-path (conj path k)
                  next-state (get state-map next-path)
                  next-id (:id next-state)]
              ^{key next-id} [process-node next-node state-map next-path]))))])))



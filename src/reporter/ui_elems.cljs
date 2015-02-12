(ns reporter.ui-elems
  (:require [clojure.string :refer [join]]
            [reporter.tools :as tools]
            [reporter.state :as state]))

(defn tab [{:keys [id fn-active? fn-activate items]}]
  (fn []
    (let [active (fn-active?)
          class (if active
                  "navbar__item--active"
                  "navbar__item--inactive navbar__item--hoverable")
          on-click-fn (if active nil fn-activate)]
      (->> (for [item items] [item])
           (into ^{:key id} [:div.navbar__item {:class class :on-click on-click-fn}])))))

(defn navbar-item [{:keys [id items classes]}]
  (fn []
    (let [class (when (seq classes) (->> classes
                                         (map name)
                                         (map #(str "navbar__item--" %))
                                         (join " ")))]
      (->> (for [item items] [item])
           (into ^{:key id} [:div.navbar__item {:class class}])))))

(defn text [{:keys [id text extra-classes]}]
  (fn []
    ^{:key id} [:span (when (seq extra-classes) {:class (join " " extra-classes)}) text]))

(defn link [{:keys [id text href extra-classes]}]
  (fn []
    ^{:key id} [:a {:href href :class (when (seq extra-classes) (join " " extra-classes))} text]))

(defn options-btn [{:keys [id fn-on-click]}]
  (fn []
    ^{:key id} [:span.cog-sign {:on-click fn-on-click}]))

(defn only-fail-filter-link [{:keys [id text state-maps filter-state-atom]}]
  (fn []
    (let [filter-active? @filter-state-atom
          class (if filter-active? "active" "inactive")
          f (if filter-active?
              (partial state/apply-status-filter ["FAIL" "ERROR"])
              state/undo-status-filter)
          states (reduce into [] (map vals state-maps))
          ;_ (tools/log-obj "f " f)
          ;_ (tools/log-obj "states " states)
          ]
      (doseq [state states]
        (state/update-it! state f))
      ^ {:key id} [:span {:class class :on-click #(swap! filter-state-atom not)} text])))
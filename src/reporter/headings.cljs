(ns reporter.headings
  (:require [reporter.state :as state]
            [reporter.path :as path]
            [reporter.tools :as tools]
            [reporter.test-results-tools :as t-res-t]
            [clojure.string :refer [join]]))


(defn group-sign [opened]
  (let [class (if opened
                "group-opened-sign"
                "group-closed-sign"
                )]
    [:span {:class class}]))


(defn heading-fails-stat [fails total]
  [:span.heading.heading--h5.heading--no-padding
   [:span.error fails]
   [:span (str "/" total)]])

(defn heading [level s extra-classes]
  (let [class (cond
                (< 0 level 6) (str "heading--h" level)
                :else "heading--h5"
                )]
    [:div {:class (join " " ["heading" class "section__head--grow" extra-classes])} s]))



(defn section-head [{:keys [level status text state extra mark-uncommon-statuses]}]
  (fn []
    (let [opened (state/opened? state)
          id (state/id state)
          status-feature (when-not (t-res-t/common-status? status) (str " [" status "] "))
          final-text (if mark-uncommon-statuses
                       (str text status-feature)
                       text)]
      ^{:key id} [:div.section__head
                  {:on-click #(state/update-it! state state/flip-opened)}
                  (group-sign opened)
                  (heading level final-text (t-res-t/status->class status))
                  extra])))




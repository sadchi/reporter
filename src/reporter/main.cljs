(ns reporter.main
  (:require [reporter.initial-processing :as init-p] 
            [reporter.state :as state]
            [reporter.tree :as tree]
            [reagent.core :as r]
            [reporter.overall-stats :as o-stats]
            [reporter.menu :as m]))


(def inner-data (js->clj js/data :keywordize-keys true))




(defn main [report-structure state-map overall-stats]
  [:div.container
    [o-stats/overview-section overall-stats state-map  ["Overview"]]
     (for [level0 (sort (keys report-structure))] 
       (let [state-key (conj [] level0)
             state (get state-map state-key)
             id (:id state)
             tree-node (get report-structure level0)]
         ^{:key id} [tree/process-node tree-node state-map inner-data (conj [] level0)]))])

(defn header [state-map]
  [m/menu state-map])

(defn footer []
  [:div.footer "FOOTER"])



(defn ^:export start []
  (let [report-structure (init-p/build-report-structure inner-data :path :test)
        state-map (init-p/build-state-map {:coll inner-data
                                    :path-field :path
                                    :status-field :status
                                    :default-atom-fn state/default-state
                                    })
        overall-stats (o-stats/calculate-overall-stats inner-data)
        r-structure-w-overview (assoc-in report-structure ["Overview"] {})
        s-map-w-overview (assoc state-map ["Overview"] (state/default-state))]
    (.log js/console "report-structure: " (clj->js r-structure-w-overview)) 
    (.log js/console "state-map: " (clj->js s-map-w-overview)) 
    (.log js/console "stats: " (clj->js overall-stats)) 
    (def r-struct report-structure)
    (def s-map s-map-w-overview)
    (r/render-component [main report-structure s-map-w-overview overall-stats] (.getElementById js/document "main"))
    (r/render-component [header state-map] (.getElementById js/document "header"))
    (r/render-component [footer] (.getElementById js/document "footer"))
    ))

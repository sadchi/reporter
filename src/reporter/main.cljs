(ns reporter.main
  (:require [reporter.initial-processing :as init-p] 
            [reporter.state :as state]
            [reporter.tree :as tree]
            [reagent.core :as r]))


(def inner-data (js->clj js/data :keywordize-keys true))




(defn main [report-structure state-map]
  [:div.container
     (for [level0 (sort (keys report-structure))] 
       (let [state-key (conj [] level0)
             state (get state-map state-key)
             id (:id state)
             tree-node (get report-structure level0)]
         ^{:key id} [tree/process-node tree-node state-map inner-data (conj [] level0)]))])

(defn header []
  [:div "HEADER"])

(defn footer []
  [:div.footer "FOOTER"])



(defn ^:export start []
  (let [report-structure (init-p/build-report-structure inner-data :path :test)
        state-map (init-p/build-state-map {:coll inner-data
                                    :path-field :path
                                    :status-field :status
                                    :default-atom-fn state/default-state
                                    })]
    #_(.log js/console "report-structure: " (clj->js report-structure)) 
    #_(.log js/console "state-map: " (clj->js state-map)) 
    (def r-struct report-structure)
    (def s-map state-map)
    (r/render-component [main report-structure state-map] (.getElementById js/document "main"))
    (r/render-component [header] (.getElementById js/document "header"))
    (r/render-component [footer] (.getElementById js/document "footer"))
    ))

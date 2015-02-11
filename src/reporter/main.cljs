(ns reporter.main
  (:require [reporter.initial-processing :as init-p]
            [reporter.state :as state]
            [reporter.tree :as tree]
            [reporter.path :as path]
            [reporter.tools :as tools]
            [reporter.test-results-tools :as t]
            [reporter.ui-elems :as ui]
            [reagent.core :as r]
            [reporter.overall-stats :as o-stats]
            [goog.string :as gstring]
            [goog.string.format]))


(defn- main [{:keys [active-tab tabs]}]                     ;report-structure state-map overall-stats data
  (fn []
    (let [tab @active-tab
          items (get tabs tab)
          body [:div.container]]
      (into body (for [item items] [item])))))


(defn- navbar [& items]
  (->> (for [item items] (item))
       (into [:div.navbar])))

(defn- footer []
  [:div.footer "In case of questions about the report functionality contact ahuba@aligntech.com"])


(defn- func-test? [data]
  (if (get data :filename nil)
    true
    false))

(defn- stat-test? [data]
  ((complement func-test?) data))


(defn ^:export start []
  (let [struct-bldr #(init-p/build-report-structure % :path :test)
        state-bldr (fn [tests structure]
                     (init-p/mk-state-map {:coll         tests
                                           :path-field   :path
                                           :status-field :status
                                           :structure    structure}))
        state-builder (fn [tests structure]
                        (init-p/create-state-map {:mk-state-f   state/mk-state
                                                  :structure    structure
                                                  :tests        tests
                                                  :path-field   :path
                                                  :status-field :status}))



        get-status (fn [state-map k]
                     (-> (get state-map k [:root (state/mk-state)])
                         (second)
                         (state/reduce-statuses t/worse-status "SUCCESS")))


        full-inner-data (js->clj js/data :keywordize-keys true)

        func-tests (filter func-test? full-inner-data)
        stat-tests (filter stat-test? full-inner-data)

        func-report-structure (struct-bldr func-tests)
        stat-report-structure (struct-bldr stat-tests)


        func-report-structure (init-p/collapse-poor-branches func-report-structure)
        _ (tools/log-obj "func-report-structure " func-report-structure)
        stat-report-structure (init-p/collapse-poor-branches stat-report-structure)

        func-state-map (state-bldr func-tests func-report-structure)
        _ (tools/log-obj "func-state-map " func-state-map)
        stat-state-map (state-bldr stat-tests stat-report-structure)


        func-root (get func-state-map [])
        stat-root (get stat-state-map [])

        func-root-item (last func-root)
        stat-root-item (last stat-root)

        func-status (get-status func-state-map [])
        stat-status (get-status stat-state-map [])


        common-status (t/worse-status func-status stat-status)

        status-text (fn []
                      (ui/text {:id            (state/gen-id)
                                :text          (gstring/format "%s: " common-status)
                                :extra-classes [(t/status->class common-status)]}))

        func-tab-text (fn []
                        (ui/text {:id            (state/gen-id)
                                  :text          "functional"
                                  :extra-classes [(t/status->class func-status)]}))

        stat-tab-text (fn []
                        (ui/text {:id            (state/gen-id)
                                  :text          "statistical"
                                  :extra-classes [(t/status->class stat-status)]}))

        status-label (fn []
                       (ui/navbar-item {:id    (state/gen-id)
                                        :items [status-text]}))

        filler (fn []
                 (ui/navbar-item {:id      (state/gen-id)
                                  :classes [:grow]}))

        opts (fn []
               (ui/options-btn {:id          (state/gen-id)
                                :fn-on-click (fn [] (identity 1))}))

        opts-btn (fn []
                   (ui/navbar-item {:id      (state/gen-id)
                                    :items   [opts]
                                    :classes [:hoverable]}))


        active-tab (r/atom :func)

        func-tab (fn []
                   (ui/tab {:id          (state/gen-id)
                            :fn-active?  (fn [] (= :func @active-tab))
                            :fn-activate (fn [] (reset! active-tab :func))
                            :items       [func-tab-text]}))


        stat-tab (fn []
                   (ui/tab {:id          (state/gen-id)
                            :fn-active?  (fn [] (= :stat @active-tab))
                            :fn-activate (fn [] (reset! active-tab :stat))
                            :items       [stat-tab-text]}))

        overall-func (o-stats/calculate-overall-stats func-tests)
        overall-stat (o-stats/calculate-overall-stats stat-tests)

        overall-func-sec-state (state/mk-state)
        overall-func-sec (fn []
                           (o-stats/overview-section-alt overall-func overall-func-sec-state))

        overall-stat-sec-state (state/mk-state)
        overall-stat-sec (fn []
                           (o-stats/overview-section-alt overall-stat overall-stat-sec-state))
        ]

    (def f-state func-state-map)
    (def s-state stat-state-map)

    (r/render-component [main {:active-tab active-tab
                               :tabs       {:func [overall-func-sec func-root-item]
                                            :stat [overall-stat-sec stat-root-item]}}] (.getElementById js/document "main"))
    (r/render-component [navbar status-label func-tab stat-tab filler opts-btn] (.getElementById js/document "header"))
    (r/render-component [footer] (.getElementById js/document "footer"))
    ))

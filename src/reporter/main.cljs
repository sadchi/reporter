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
          ;_ (tools/log-obj "main items " items)
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
                     (init-p/mk-state-map {:test-results tests
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
                     (t/get-status (get state-map k)))


        full-inner-data (js->clj js/data :keywordize-keys true)

        func-tests (filter func-test? full-inner-data)
        stat-tests (filter stat-test? full-inner-data)

        func-report-structure (struct-bldr func-tests)
        ;_ (tools/log-obj "func-report-structure " func-report-structure)
        stat-report-structure (struct-bldr stat-tests)
        ;_ (tools/log-obj "stat-report-structure " stat-report-structure)

        func-report-structure (init-p/collapse-poor-branches func-report-structure)
        ;_ (tools/log-obj "func-report-structure " func-report-structure)
        stat-report-structure (init-p/collapse-poor-branches stat-report-structure)
        ;_ (tools/log-obj "stat-report-structure " stat-report-structure)

        func-state-map (state-builder func-tests func-report-structure)
        ;_ (tools/log-obj "func-state-map " func-state-map)
        stat-state-map (state-builder stat-tests stat-report-structure)
        ;_ (tools/log-obj "func-state-map " stat-state-map)

        func-status (get-status func-state-map [])
        ;_ (tools/log-obj "func-status " func-status)
        stat-status (get-status stat-state-map [])
        ;_ (tools/log-obj "stat-status " stat-status)

        common-status (t/worse-status func-status stat-status)
        ;_ (tools/log-obj "common-status " common-status)

        status-text (ui/text {:id            (state/gen-id)
                              :text          (gstring/format "%s: " common-status)
                              :extra-classes [(t/status->class common-status)]})

        func-tab-text (ui/text {:id            (state/gen-id)
                                :text          "functional"
                                :extra-classes [(t/status->class func-status)]})

        stat-tab-text (ui/text {:id            (state/gen-id)
                                :text          "statistical"
                                :extra-classes [(t/status->class stat-status)]})

        status-label (ui/navbar-item {:id    (state/gen-id)
                                      :items [status-text]})

        filler (ui/navbar-item {:id      (state/gen-id)
                                :classes [:grow]})

        only-fails (r/atom true)

        status-filter (ui/only-fail-filter-link {:id                (state/gen-id)
                                                 :text              "only fail/error"
                                                 :state-maps        [func-state-map stat-state-map]
                                                 :filter-state-atom only-fails})

        status-filter-btn (ui/navbar-item {:id    (state/gen-id)
                                           :items [status-filter]})

        flat-view (r/atom false)

        flat-view-trigger (ui/simple-trigger {:id              (state/gen-id)
                                              :text            "flat-view"
                                              :fn-active?      #(deref flat-view)
                                              :fn-change-state #(swap! flat-view not)})

        flat-view-btn (ui/navbar-item {:id    (state/gen-id)
                                       :items [flat-view-trigger]})

        expand-all (r/atom false)

        expand-all-trigger (ui/simple-trigger {:id              (state/gen-id)
                                               :text            "expand-all"
                                               :fn-active?      #(deref expand-all)
                                               :fn-change-state #(swap! expand-all not)})

        expand-all-btn (ui/navbar-item {:id    (state/gen-id)
                                        :items [expand-all-trigger]})


        active-tab (r/atom :func)

        func-tab (ui/tab {:id          (state/gen-id)
                          :fn-active?  (fn [] (= :func @active-tab))
                          :fn-activate (fn [] (reset! active-tab :func))
                          :items       [func-tab-text]})


        stat-tab (ui/tab {:id          (state/gen-id)
                          :fn-active?  (fn [] (= :stat @active-tab))
                          :fn-activate (fn [] (reset! active-tab :stat))
                          :items       [stat-tab-text]})



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
    (def f-struct func-report-structure)
    (def s-state stat-state-map)

    (r/render-component [main {:active-tab active-tab
                               :tabs       {:func [overall-func-sec (tree/results-view {:structure       func-report-structure
                                                                                        :state-map       func-state-map
                                                                                        :test-results    func-tests
                                                                                        :flat-view-atom  flat-view
                                                                                        :expand-all-atom expand-all})]
                                            :stat [overall-stat-sec (tree/results-view {:structure       stat-report-structure
                                                                                        :state-map       stat-state-map
                                                                                        :test-results    stat-tests
                                                                                        :flat-view-atom  flat-view
                                                                                        :expand-all-atom expand-all})]}}] (.getElementById js/document "main"))
    (r/render-component [navbar status-label func-tab stat-tab filler expand-all-btn flat-view-btn status-filter-btn] (.getElementById js/document "header"))
    (r/render-component [footer] (.getElementById js/document "footer"))
    ))

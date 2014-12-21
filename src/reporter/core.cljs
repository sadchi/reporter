(ns reporter.core
  (:require [reagent.core :as r :refer [atom]]
            [clojure.string :refer [join]]
            [cljs-uuid.core :as uuid]))

(def inner-test-results (js->clj js/data :keywordize-keys true))
(def report-structure (atom (sorted-map)))
(def overview-section (atom {}))
(def extra-props  (atom {} ))
(def zero-prop {:visibility :visible
               :status "UNDEFINED"
               :count 0
               :fail-count 0})

(def bottom-level 4)
(def highest-level 1)


(def status-rank-mapping {"UNDEFINED" 0
                    "SUCCESS" 1
                    "FAIL" 2})

(def rank-status-mapping {0 "UNDEFINED"
                    1 "SUCCESS"
                    2 "FAIL"})

(defn status->rank [status]
  (get status-rank-mapping status))

(defn rank->status [rank]
  (get rank-status-mapping rank))

(defn add-sec! [coll path]
  (->> {:state :opened }
       (get-in @coll path)
       (#(assoc % :state :opened))
       (swap! coll assoc-in path )))


(defn add-sec-recur! [coll path]
  (loop [p path]
    (if (empty? p)
      nil
      (do
       (add-sec! coll p)
       (recur (pop p))))))


(defn change-status [new-status-s old-status-s]
  (let [old-rank (status->rank old-status-s)
        new-rank (status->rank new-status-s)]
    (-> (max old-rank new-rank)
        (rank->status))))

(defn update-single! [acoll path attrib update-f]
  (-> @acoll
      (get path zero-prop)
      (update-in [attrib] update-f)
      (#(swap! acoll assoc path %))))

(defn update-up-to-root! [acoll path attrib update-f]
  (loop [p path]
    (update-single! acoll p attrib update-f)
    (when-not (empty? p) (recur (pop p)))))


(defn initial-process-data! [data tree props]
  (doseq [item data]
    (let [path (:path item)
          status (:status item)]
      (update-up-to-root! props path :count inc)
      (update-up-to-root! props path :status (partial change-status status))
      (when (= status "FAIL")
        (update-up-to-root! props path :fail-count inc))
      (add-sec-recur! tree path))))

(defn flip-state [state]
  (cond
   (= state :opened) :closed
   (= state :closed) :opened))

(defn flip-func! [acoll path]
  (swap! acoll update-in (conj path :state) flip-state))


(defn gen-key []
  (str (uuid/make-random)))

(defn group-sign [state]
  (let [class (if (= state :opened)
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



(defn get-test [path]
  (->> inner-test-results
       (filter (comp (partial = path) :path))
       (first)))

(defn is-test? [path]
  (->> inner-test-results
       (map :path)
       (filter (partial = path))
       (seq)
       (boolean)))


(defn heading-fails-stat [props path]
  (let [prop (get props path)
        fails (:fail-count prop)
        total (:count prop)] 
    [:span.heading.heading--h5.heading--no-padding
     [:span.error fails]
     [:span (str "/" total)]]))

(defn section-head [{:keys [level state status path acoll extra]}]
  [:div.section__head 
   {:on-click #(flip-func! acoll path)}
   [group-sign state]
   [heading level (peek path) (when (= status "FAIL") "error")]
   extra])


(defn meta-text [meta-item]
  [:div.text  (:data meta-item)])

(defn is-table-empty? [coll]
  (every? nil? coll))


(defn render-table-row [odd coll]
  ^{:key (gen-key)} 
  [:tr.simple-table__tr (when odd {:class "simple-table--odd"}) 
   (for [value coll]
     ^{:key (gen-key)} [:td.simple-table__td value])])

(defn meta-table [meta-item]
  [:div.sub-section  [:div.simple-table__title (:name meta-item)]
   [:table.simple-table 
    (for [th (:columns meta-item [])]
      ^{:key (gen-key)} [:th.simple-table__th th])
    (loop [data (:data meta-item)
           odd false
           acc nil]
      (if (is-table-empty? data)
        (reverse acc)
        (->> data
             (map first)
             (render-table-row odd)
             (conj acc)
             (recur (map next data) (not odd)))))]])

(defn meta-data-render [meta-data]
  [:div {:key (gen-key)}
   (for [meta-item meta-data]
     (case (:type meta-item)
       "text"  ^{:key (gen-key)}[meta-text meta-item]
       "table" ^{:key (gen-key)}[meta-table meta-item]
       nil))])


(defn fail-record-render [fail odd]
  ^{:key (gen-key)}[:tr (when odd {:class "simple-table--odd"})
   ^{:key (gen-key)}[:td.simple-table__td (:type fail) ]
   ^{:key (gen-key)}[:td.simple-table__td (:msg fail)]])

(defn fail-table [fails]
  (when-not (empty? fails) 
    [:div.sub-section [:div.simple-table__title "Assert errors list"]
     [:table.simple-table
      [:th.simple-table__th.simple-table--20 "Type"]
      [:th.simple-table__th  "Message"]
      (for [[idx fail] (map-indexed vector fails)
            :let [odd (odd? idx)]]
        ^{:key (gen-key)}[fail-record-render fail odd])]]))

(defn test-result-content [test-info]
  (let [fails (:fails test-info [])
        meta-data (:meta test-info [])]
    [:div.inner-content 
     [meta-data-render meta-data]
     [fail-table fails]]))

(defn test-result-special-mark [status]
  [:span.test-mark-sign (when (= status "FAIL") {:class "error"})])

(defn test-result [acoll path]
  (let [state (get-in @acoll (conj path :state))
        test-info (get-test path)
        status (:status test-info)] 
    [:div [section-head {:level bottom-level
                         :state state
                         :status status
                         :path path
                         :acoll acoll
                         :extra (test-result-special-mark status)}]
     (when (= state :opened) [test-result-content test-info])]))

(defn render-tree [tree atree props level path]
  (for [k (keys tree)
        :when (not= k :state)]
    (let [v (get tree k)
          state (:state v)
          p (conj path k)
          status (-> props (get p) (get :status))]
      [:div.section {:key (gen-key)} 
       (if (is-test? p)
         ^{:key (gen-key)}[test-result atree p]
         (list ^{:key (gen-key)}[section-head {:level level
                                               :state state
                                               :status status
                                               :path p
                                               :acoll atree
                                               :extra (heading-fails-stat props p)}]
               (if (= state :opened)
                 (render-tree v atree props (inc level) p))))])))




(defn overall-stat-content []
  [:div.inner-content
   [:table.simple-table.simple-table--no-borders
    [:tr
     [:td.simple-table__td.simple-table--no-borders.simple-table--10.simple-table--right
      "Total tests:"] 
     [:td.simple-table__td.simple-table--no-borders
      "24"]]]])

(defn overall-stat [acoll path]
  (let [state (get-in @acoll (conj path :state))]
    [:div.section
     [section-head {:level highest-level
                    :state state
                    :status "UNDEFINED"
                    :path path
                    :acoll acoll}]
     (when (= state :opened) [overall-stat-content])]))


(defn report-tree []
  [:div.container
   [overall-stat overview-section ["Overview"]]
   (render-tree @report-structure report-structure @extra-props 1 [])])


(defn header []
  [:div.header "FAILED"])


(defn main []
  [report-tree])




(defn ^:export run []
  (initial-process-data! inner-test-results report-structure extra-props)
  (add-sec! overview-section ["Overview"])
  (r/render-component [header] (.getElementById js/document "header"))
  (r/render-component [main] (.getElementById js/document "main")))

(ns reporter.test-results
  (:require [reporter.headings :as h]
            [clojure.string :as string]
            [reporter.path :as path]
            [reporter.state :as state]
            [reporter.common.string-utils :as string-utils]
            [reporter.test-results-tools :refer [get-status status->class]]))


(def ^:private test-heading-level 4)


(defn meta-text [meta-item]
  [:div.text (:data meta-item)])

(defn is-table-empty? [coll]
  (every? nil? coll))


(defn render-table-row [r-idx coll]
  ^{:key r-idx} [:tr.simple-table__tr (when (odd? r-idx) {:class "simple-table--odd"})
                 (for [[idx value] (map-indexed vector coll)]
                   ^{:key idx} [:td.simple-table__td value])])

(defn meta-table [meta-item]
  [:div.sub-section [:div.simple-table__title (:name meta-item)]
   [:div.can-be-wide
    [:table.simple-table
     (for [[idx th] (map-indexed vector (:columns meta-item []))]
       ^{:key idx} [:th.simple-table__th th])
     (loop [data (:data meta-item)
            idx 0
            acc nil]
       (if (is-table-empty? data)
         (reverse acc)
         (->> data
              (map first)
              (render-table-row idx)
              (conj acc)
              (recur (map next data) (inc idx)))))]]])


(defn meta-data-render [meta-data]
  [:div
   (for [[idx meta-item] (map-indexed vector meta-data)]
     (case (:type meta-item)
       "text" ^{:key idx} [meta-text meta-item]
       "table" ^{:key idx} [meta-table meta-item]
       nil))])


(defn fail-record-render [fail odd]
  [:tr (when odd {:class "simple-table--odd"})
   [:td.simple-table__td (:type fail)]
   [:td.simple-table__td (:message fail)]])

(defn error-record-render [error odd]
  (let [message (:message error)
        trace (:trace error)
        trace-lines (string/split trace #"\n")]
    [:tr (when odd {:class "simple-table--odd"})
     [:td.simple-table__td (:type error)]
     [:td.simple-table__td
      (list
        ^{:key 1} [:p message]
        ^{:key 2} [:p (for [trace-line trace-lines]
                        (list trace-line [:br]))])]]))

(defn asset-record-render [asset odd]
  (let [name (:name asset)
        link (:data asset)
        escaped-link (string/escape link {\< "\u003C"
                                          \> "\u003E"
                                          \" "\u0022"
                                          \& "\u0026"
                                          \' "\u0027"})
        link-with-zero-spaces (string-utils/add-zero-spaces escaped-link 10)]
    [:tr (when odd {:class "simple-table--odd"})
     [:td.simple-table__td name]
     [:td.simple-table__td
      [:a {:href escaped-link} link-with-zero-spaces]]]))


(defn fails-table [fails]
  (when-not (empty? fails)
    [:div.sub-section [:div.simple-table__title "Assert errors: "]
     [:div.can-be-wide
      [:table.simple-table
       [:th.simple-table__th.simple-table--20 "Type"]
       [:th.simple-table__th "Message"]
       (for [[idx fail] (map-indexed vector fails)
             :let [odd (odd? idx)]]
         ^{:key idx} [fail-record-render fail odd])]]]))

(defn errors-table [errors]
  (when-not (empty? errors)
    [:div.sub-section [:div.simple-table__title "Internal exceptions: "]
     [:div.can-be-wide
      [:table.simple-table
       [:th.simple-table__th.simple-table--20 "Type"]
       [:th.simple-table__th "Message and trace"]
       (for [[idx error] (map-indexed vector errors)
             :let [odd (odd? idx)]]
         ^{:key idx} [error-record-render error odd])]]]))


(defn assets-table [filename meta-data]
  (let [other-assets (filter #(= "asset" (:type %)) meta-data)
        assets (if filename
                 (conj other-assets {:name "Initial ADF" :data filename})
                 other-assets)]
    (when-not (empty? assets)
      [:div.sub-section [:div.simple-table__title "Assets: "]
       [:div.can-be-wide
        [:table.simple-table
         [:th.simple-table__th.simple-table--20 "Name"]
         [:th.simple-table__th "Link"]
         (for [[idx asset] (map-indexed vector assets)
               :let [odd (odd? idx)]]
           ^{:key idx} [asset-record-render asset odd])]]])))

(defn test-result-content [test-info]
  (let [fails (:fails test-info [])
        errors (:errors test-info [])
        filename (:filename test-info nil)
        meta-data (:meta test-info [])]
    [:div.inner-content
     [meta-data-render meta-data]
     [fails-table fails]
     [errors-table errors]
     [assets-table filename meta-data]]))

(defn test-result-special-mark [status]
  [:span.test-mark-sign {:class (status->class status)}])

(defn test-result-alt [{:keys [state path test-info caption]}]
  (fn []
    (let [id (state/id state)
          status (get-status state)
          opened (state/opened? state)
          level (dec (count path))
          text (if caption caption (path/name-path-node (peek path)))
          level-sec-class (str "section--lvl-" level)]
      [:div {:key id :class level-sec-class}
       [h/section-head {:level                  test-heading-level
                        :opened                 opened
                        :status                 status
                        :text text
                        :state                  state
                        :extra                  (test-result-special-mark status)
                        :mark-uncommon-statuses true}]
       (when opened [test-result-content test-info])])))
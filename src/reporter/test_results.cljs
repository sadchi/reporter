(ns reporter.test-results
  (:require [reporter.headings :as h]
            [reporter.state :as state]))


(def ^:private test-heading-level 4)


(defn meta-text [meta-item]
  [:div.text  (:data meta-item)])

(defn is-table-empty? [coll]
  (every? nil? coll))


(defn render-table-row [r-idx coll]
  ^{:key r-idx} [:tr.simple-table__tr (when (odd? r-idx) {:class "simple-table--odd"}) 
   (for [[idx value] (map-indexed vector coll)]
     ^{:key idx} [:td.simple-table__td value])])

(defn meta-table [meta-item]
  [:div.sub-section  [:div.simple-table__title (:name meta-item)]
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
             (recur (map next data) (inc idx)))))]])


(defn meta-data-render [meta-data]
  [:div
   (for [[idx meta-item] (map-indexed vector meta-data)]
     (case (:type meta-item)
       "text"  ^{:key idx}[meta-text meta-item]
       "table" ^{:key idx}[meta-table meta-item]
       nil))])


(defn fail-record-render [fail odd]
  [:tr (when odd {:class "simple-table--odd"})
   [:td.simple-table__td (:type fail) ]
   [:td.simple-table__td (:message fail)]])

(defn fail-table [fails]
  (when-not (empty? fails) 
    [:div.sub-section [:div.simple-table__title "assert errors list"]
     [:table.simple-table
      [:th.simple-table__th.simple-table--20 "type"]
      [:th.simple-table__th  "message"]
      (for [[idx fail] (map-indexed vector fails)
            :let [odd (odd? idx)]]
        ^{:key idx}[fail-record-render fail odd])]]))



(defn test-result-content [test-info]
  (let [fails (:fails test-info [])
        meta-data (:meta test-info [])]
    [:div.inner-content 
     [meta-data-render meta-data]
     [fail-table fails]]))

(defn test-result-special-mark [status]
  [:span.test-mark-sign (when (= status "FAIL") {:class "error"})])

(defn test-result [state-map test-results path]
  (let [state (get state-map path)
        state-atom (deref (state/get-state-atom state))
        status (get state-atom :status)
        opened (get state-atom :opened)
        test-info (->> test-results
                       (filter (comp (partial = path) :path))
                       (first))
        ]
   [:div
    [h/section-head {:level test-heading-level
                     :opened opened
                     :status status
                     :path path
                     :state state
                     :extra (test-result-special-mark status)}]
    (when opened [test-result-content test-info])]))

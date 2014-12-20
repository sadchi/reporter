(ns reporter.core
  (:require [reagent.core :as r :refer [atom]]
            [clojure.string :refer [join]]
            [cljs-uuid.core :as uuid]))

(def inner-test-results (js->clj js/data :keywordize-keys true))
(def report-structure (atom (sorted-map)))
(def visibility-map  (atom {} ))
(def bottom-level 4)

(defn add-sec! [coll path]
  (->> {:state :opened}
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

(defn update-path [coll path func]
  (->> {:count 0 :visibility :visible}
       (get @coll path)
       ( #(update-in % [:count] func))
       (swap! coll assoc path)))

(defn add-path [coll path]
  (update-path coll path inc))

(defn rem-path [coll path]
  (update-path coll path dec))

(defn update-count [coll path func]
 (loop [p path]
    (if (empty? p)
      nil
      (do
       (update-path coll p func)
       (recur (pop p))))))

(defn inc-count-recur [coll path]
  (update-count coll path inc))

(defn dec-count-recur [coll path]
  (update-count coll path dec))


(defn initial-process-data! [data tree vis-table]
  (doseq [item data]
    (let [path (:path item)]
      (inc-count-recur vis-table path)
      (add-sec-recur! tree path))))



(defn flip-state [state]
  (cond
   (= state :opened) :closed
   (= state :closed) :opened))

(defn flip-func! [path]
  (swap! report-structure update-in (conj path :state) flip-state))


(defn gen-key []
  (str (uuid/make-random)))

(defn group-sign [state]
  (let [class (if (= state :opened)
                "group-opened-sign"
                "group-closed-sign"
               )]
    [:span {:class class}]))

(defn heading [level s]
  (let [class (cond
               (< 0 level 6) (str "heading--h" level)
               :else "heading--h5"
               )]
    [:div {:class (join " " ["heading" class "section__head--grow"])} s]))



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


(defn section-head [level state path]
  [:div.section__head 
   {:on-click #(flip-func! path)}
   [group-sign state]
   [heading level (peek path)]])


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

(defn test-result [path]
  (let [state (get-in @report-structure (conj path :state))
        test-info (get-test path)
        status (:status test-info)] 
    [:div [section-head bottom-level state path]
     (when (= state :opened) [test-result-content test-info])]))


(defn render-tree [tree level path]
  (for [k (keys tree)
        :when (not= k :state)]
    (let [v (get tree k)
          state (:state v)
          p (conj path k)]
      [:div.section {:key (gen-key)} 
       (if (is-test? p)
         ^{:key (gen-key)}[test-result p]
         (list ^{:key (gen-key)}[section-head level state p]
               (if (= state :opened)
                 (render-tree v (inc level) p))))])))



(defn report-tree []
  [:div.container (render-tree @report-structure 1 [])])


(defn header []
  [:div.header "FAILED"])


(defn main []
  [report-tree])


(defn ^:export run []
  (initial-process-data! inner-test-results report-structure visibility-map)
  (r/render-component [header] (.getElementById js/document "header"))
  (r/render-component [main] (.getElementById js/document "main")))

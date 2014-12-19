(ns reporter.core
  (:require [reagent.core :as r :refer [atom]]
            [clojure.string :refer [join]]))

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


(defn gen-key [obj]
  ((comp str hash) obj))

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

(defn meta-table [meta-item]
  [:table 
   (for [th (:columns meta-item [])]
     ^{:key (gen-key th)} [:th th])
   ])

(defn meta-data-render [meta-data]
  [:div {:key (gen-key meta-data)}
   (for [meta-item meta-data]
     (case (:type meta-item)
       "text" ^{:key (gen-key meta-item)}[meta-text meta-item]
       "table" ^{:key (gen-key meta-item)}[meta-table meta-item]
       nil))])


(defn fail-render [fail]
  (let [fail-type (:type fail "")
        fail-msg (:msg fail "")]
    [:div (str "[" fail-type "] " fail-msg)]))


(defn test-result-content [test-info]
  (let [fails (:fails test-info [])
        meta-data (:meta test-info [])]
    [:div.inner-content 
     (for [fail fails]
       ^{:key (gen-key fail)}[fail-render fail])
     [meta-data-render meta-data]]))

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
      [:div.section {:key (gen-key p)} 
       (if (is-test? p)
         ^{:key (gen-key p)}[test-result p]
         (list ^{:key (gen-key p)}[section-head level state p]
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

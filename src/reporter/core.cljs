(ns reporter.core
  (:require [reagent.core :as r :refer [atom]]
            [clojure.string :refer [join]]))

(def inner-test-results (js->clj js/data :keywordize-keys true))
(def report-structure (atom (sorted-map)))
(def visibility-map  (atom {} ))


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


(defn group-sign [state]
  (let [class (if (= state :opened)
                "group-opened-sign"
                "group-closed-sign"
               )]
    [:span {:class class}]))

(defn heading [level name]
  (let [class (cond
               (= level 0) "heading--h1"
               (= level 1) "heading--h2"
               (= level 2) "heading--h3"
               (= level 3) "heading--h4"
               (= level 4) "heading--h5"
               :else "heading--h5"
               )]
    [:div {:class (join " " ["heading" class "section__head--grow"])} name]))



(defn section-head [level name state path]
  [:div.section__head 
   {:on-click #(flip-func! path)}
   [group-sign state]
   [heading level name]])

(defn render-tree [tree level path]
  (for [k (keys tree)
        :when (not= k :state)]
    (let [v (get tree k)
          state (:state v)
          p (conj path k)]
      [:div.section {:key (join "." p)}
       [section-head level k state p]
       (if (= state :opened)
         (render-tree v (inc level) p))])))





(defn report-tree [tree]
  [:div.container (render-tree @report-structure 0 [])])


(defn header []
  [:div.header "FAILED"])


(defn main []
  [report-tree])





(defn ^:export run []
  (initial-process-data! inner-test-results report-structure visibility-map)
  (r/render-component [header] (.getElementById js/document "header"))
  (r/render-component [main] (.getElementById js/document "main")))

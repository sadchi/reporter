(ns reporter.core
  (:require [reagent.core :as r :refer [atom]]
            [clojure.string :refer [join]]))

(def inner-test-results (js->clj js/data :keywordize-keys true))
(def test-data-structure-tree (atom (sorted-map)))
(def visibility-map  (atom {} ))
(def update-func-table (atom {}))

(defn add-sec! [coll path]
  (->> {:state :opened}
       (get-in @coll path)
       (#(assoc % :state :opened))
       (swap! coll assoc-in path )))

(defn add-update-func! [table atomic-tree path]
  (swap! table assoc path (partial swap! atomic-tree path)))

(defn add-sec-recur! [coll table path]
  (loop [p path]
    (if (empty? p)
      nil
      (do
       (add-sec! coll p)
       (add-update-func! table coll path)
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

(defn initial-process-data! [data tree vis-table update-table]
  (doseq [item data]
    (let [path (:path item)]
      (inc-count-recur vis-table path)
      (add-sec-recur! tree update-table path))))


(defn render-test []
  nil)


(defn render-group-sign [state]
  (let [class (if (= state :opened)
                "group-opened-sign"
                "group-closed-sign"
               )]
    [:span {:class class}]))

(defn render-heading [level name]
  (let [class (cond
               (= level 0) "heading--h1"
               (= level 1) "heading--h2"
               (= level 2) "heading--h3"
               (= level 3) "heading--h4"
               (= level 4) "heading--h5"
               :else "heading--h5"
               )]
    [:div {:class (join " " ["heading" class "section__head--grow"])} name]))



(defn render-section-head [level name state path]
  [:div.section__head
   (render-group-sign state)
   (render-heading level name)
   ])

(defn render [tree level path]
  (for [k (keys tree)
        :when (not= k :state)]
    (let [v (get tree k)
          state (:state v)
          p (conj path k)]
      [:div.section {:key (join "." p)}
       (render-section-head level k state p)
       (if (= state :opened)
         (render v (inc level)))])))



(defn header []
  [:div.header "FAILED"])

(defn footer []
  [:div.footer "ahuba production"])

(defn body []
  [:div.container (render @test-data-structure-tree 0 [])])

(defn main []
  (initial-process-data! inner-test-results test-data-structure-tree visibility-map update-func-table)   
  [:div [header]
   [body]
   [footer]])
    

(defn ^:export run []
  (r/render-component [main] (.-body js/document)))

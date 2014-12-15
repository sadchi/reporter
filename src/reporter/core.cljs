(ns reporter.core
  (:require [reagent.core :as r :refer [atom]]))

(def data (js->clj js/data :keywordize-keys true))

(def sec-tree (atom (sorted-map)))
(def sec-visibility (atom {} ))

(defn add-sec [coll path]
  (swap! coll assoc-in path {:state :closed}))


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
  update-count coll path inc)

(defn dec-count-recur [coll path]
  update-count coll path dec)

(defn initial-process-data [d tree vis-table]
  (doseq [item data]
     (.log js/console (clj->js item)))
  )

(defn header []
  [:div.header "FAILED"])

(defn footer []
  [:div.footer "ahuba production"])

(defn body []
  [:div.container "TEST"])

(defn main []
  (initial-process-data data nil nil)   
  [:div [header]
   [body]
   [footer]])
    

(defn ^:export run []
  (r/render-component [main] (.-body js/document)))

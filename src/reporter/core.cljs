(ns reporter.core
  (:require [reagent.core :as r :refer [atom]]))

(def data (js->clj js/data :keywordize-keys true))

(def tree (atom (sorted-map)))
(def vis-map  (atom {} ))

(defn add-sec [coll path]
  (->> {:state :closed}
       (get-in @coll path)
       (#(assoc % :state :closed))
       (swap! coll assoc-in path )))

(defn add-sec-recur [coll path]
  (loop [p path]
    (if (empty? p)
      nil
      (do
       (add-sec coll p)
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
  (update-count coll path inc)
  )

(defn dec-count-recur [coll path]
  (update-count coll path dec)
  )

(defn initial-process-data [d tree vis-table]
  (doseq [item data]
    (let [path (:path item)]
      (inc-count-recur vis-table path)
      (add-sec-recur tree path)
      ))
  )

(defn header []
  [:div.header "FAILED"])

(defn footer []
  [:div.footer "ahuba production"])

(defn body []
  [:div.container "TEST"])

(defn main []
  (initial-process-data data tree vis-map                        )   
  [:div [header]
   [body]
   [footer]])
    

(defn ^:export run []
  (r/render-component [main] (.-body js/document)))

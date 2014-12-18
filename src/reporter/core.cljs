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


(defn group-sign [state]
  (let [class (if (= state :opened)
                "group-opened-sign"
                "group-closed-sign"
               )]
    [:span {:class class}]))

(defn heading [level name]
  (let [class (cond
               (= level 1) "heading--h1"
               (= level 2) "heading--h2"
               (= level 3) "heading--h3"
               (= level 4) "heading--h4"
               (= level 5) "heading--h5"
               :else "heading--h5"
               )]
    [:div {:class (join " " ["heading" class "section__head--grow"])} name]))



(defn get-tests [path]
  (->> inner-test-results
       (filter (comp (partial = path) :path))
       (seq)))

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


; (defn )

(defn test-result [path]
  [:div nil])


(defn render-tree [tree level path]
  (for [k (keys tree)
        :when (not= k :state)]
    (let [v (get tree k)
          state (:state v)
          p (conj path k)]
      [:div.section 
       (list ^{:key (join "." p)}[section-head level state p]
             (if (= state :opened)
               (render-tree v (inc level) p)))])))




; (defn tree-terminal [path]
;   [:div "test"])

; (defn tree-non-terminal [path]
;   (let [tree  (get-in @report-structure path)
;         sub-paths (->> (keys tree) (filter (partial not= :state)))
;         level (count path)
;         state (:state tree)
;         ]
;     [section-head level state path]
;     (when (= state :opened)
;       (for [k sub-paths]
;         ^{:key k} [tree-item (conj path k)]))))

; (defn tree-item [path]
;   [:div.section
;    (if (is-test? path)
;      [tree-terminal path]
;      [tree-non-terminal path])])

; (defn tree-root []
;   [:div.container 
;    (for [k (keys @report-structure)
;          :when (not= k :state)]
;      ^{:key k}[tree-item (vec k)])])

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

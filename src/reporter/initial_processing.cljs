(ns reporter.initial-processing
  (:require [reporter.flat-tree :as f-tree]
            [reporter.state :as state]
            [reporter.path :as path]
            [reporter.test-results :as t-res]
            [reporter.tree :as tree]
            [reporter.tools :as t]
            [clojure.string :refer [join]]))


(defn build-report-structure [coll path-field content]
  (let [paths (map #(get % path-field) coll)]
    (reduce #(assoc-in %1 %2 content) {} paths)))


(defn- mk-path-variants [structure path]
  (if (= (get-in structure path) :test)
    [[:leaf path]]
    (let [sub-tree (get-in structure path)
          coll [[:node path]]
          children (keys sub-tree)]
      (reduce into coll (map #(mk-path-variants structure (conj path %)) children)))))


(defn- mk-all-paths-variants [structure]
  (reduce into #{[:root []]}
          (for [k (keys structure)]
            (mk-path-variants structure [k]))))



(defn- mk-path [structure path]
  (if (= (get-in structure path) :test)
    [path]
    (let [sub-tree (get-in structure path)
          coll [path]
          children (keys sub-tree)]
      (reduce into coll (map #(mk-path structure (conj path %)) children)))))


(defn- mk-all-paths [structure]
  (reduce into #{[]}
          (for [k (keys structure)]
            (mk-path structure [k]))))



(defn create-state-map [{:keys [mk-state-f structure tests path-field status-field]}]
  (let [paths (mk-all-paths structure)
        final-paths (map path/flatten-path paths)

        vec->map (fn [mk-state-f coll x]
                   (assoc coll x (mk-state-f)))

        state-map (reduce (partial vec->map mk-state-f) {} final-paths)]
    (doseq [test tests]
      (let [path (get test path-field)
            status (get test status-field)
            f (fn [s x]
                (when x (state/update-it! x (partial state/inc-status s))))]
        (f-tree/update-up-root-alt state-map path (partial f status))))
    (state/update-it! (get state-map []) state/set-opened)
    state-map))



(defn mk-state-map [{:keys [test-results path-field status-field structure]}]
  (let [vec->map (fn [coll x]
                   (let [[type path state] x]
                     (assoc coll path [type state])))


        mk-flat-key (fn [coll x]
                      (let [[k v] x]
                        (assoc coll (path/flatten-path k) v)))


        path->str (fn [x]
                    (join "." (path/flatten-path x)))


        process-leaf (fn [coll x]
                       (let [[k v] x
                             [type state] v]
                         (if (= type :leaf)
                           (let [test-info (first (filter (fn [x] (= (get x path-field) (path/flatten-path k))) test-results))]
                             (assoc coll k [type state (t-res/test-result-alt state k test-info)]))
                           (assoc coll k v))))

        process-node (fn [coll x]
                       (let [item (get coll x)
                             [type state _] item]
                         (if (= type :node)
                           (let [sub-tree (get-in structure x)
                                 children (keys sub-tree)
                                 children-keys (map #(conj x %) children)
                                 children-sorted (sort-by path->str children-keys)
                                 sub-item-variants (map #(get coll %) children-sorted)
                                 sub-items (map last sub-item-variants)
                                 ;_ (t/log-obj "coll " coll)
                                 ;_ (t/log-obj "path " x)
                                 ;_ (t/log-obj "children-keys " (map path->str children))
                                 ;_ (t/log-obj "children-sorted " (map path->str children-sorted))
                                 ;_ (t/log-obj "sub-item-variants " sub-item-variants)
                                 ;_ (t/log-obj "sub-items " sub-items)
                                 ]
                             (assoc coll x [type state (tree/node state x sub-items)]))
                           coll)))

        process-root (fn [coll]
                       (t/log "\nProcess root")
                       (let [root-item (get coll [])
                             [type state] root-item
                             children (keys structure)
                             children-sorted (sort-by path->str children)
                             ;_ (t/log-obj "children-keys " (map path->str children))
                             ;_ (t/log-obj "children-sorted " (map path->str children-sorted))
                             sub-items (for [child-key children-sorted]
                                         (last (get coll (conj [] child-key))))]
                         (assoc coll [] [type state (tree/root sub-items)])))

        branches-comp (mk-all-paths-variants structure)
        branches-w-state (map #(conj % (state/mk-state)) branches-comp)
        map-type-state (reduce vec->map {} branches-w-state)
        map-type-state-leafs (reduce process-leaf {} map-type-state)
        ;_ (t/log-obj "map-type-state-leafs " map-type-state-leafs)
        down->top-paths (sort-by #(count (path/flatten-path %)) > (keys map-type-state-leafs))
        ;_ (t/log-obj "down->top-paths " down->top-paths)
        map-type-state-leafs-nodes (reduce process-node map-type-state-leafs down->top-paths)
        ;_ (t/log-obj "map-type-state-leafs-nodes " map-type-state-leafs-nodes)
        state-map-before-flatten (process-root map-type-state-leafs-nodes)
        ;_ (t/log-obj "state-map-before-flatten " state-map-before-flatten)
        state-map (reduce mk-flat-key {} state-map-before-flatten)
        ;_ (t/log-obj "state-map " state-map)
        ]
    (doseq [item test-results]
      (let [path (get item path-field)
            status (get item status-field)
            f (fn [s x]
                (when x (state/update-it! x (partial state/inc-status s))))]
        (f-tree/update-up-root-alt state-map path (comp (partial f status) second))))
    state-map))


(defn dissoc-in
  "Dissociates an entry from a nested associative structure returning a new
  nested structure. keys is a sequence of keys. Any empty maps that result
  will not be present in the new structure."
  [m [k & ks :as keys]]
  (if ks
    (if-let [nextmap (get m k)]
      (let [newmap (dissoc-in nextmap ks)]
        (if (seq newmap)
          (assoc m k newmap)
          (dissoc m k)))
      m)
    (dissoc m k)))


(defn- process-node [structure path]
  (if (= (get-in structure path) :test)
    []
    (let [sub-tree (get-in structure path)
          childs (keys sub-tree)
          single-child? (= 1 (count childs))
          coll (if single-child?
                 [path (conj path (first childs))]
                 [])]
      (reduce into coll (map #(process-node structure (conj path %)) childs)))))

(defn- get-single-child-pairs [structure]
  (reduce into []
          (for [k (keys structure)]
            (process-node structure [k]))))


(defn collapse-poor-branches [structure]
  (let [clear-dups (fn [coll x]
                     (let [prev (peek coll)]
                       (cond
                         (= prev nil) [x]
                         (= prev x) (pop coll)
                         :else (conj coll x))))

        path-tail (fn [x y]
                    (subvec y (dec (count x))))

        mk-tail (fn [x]
                  (let [[x1 x2] x]
                    [x1 x2 (path-tail x1 x2)]))


        apply-op (fn [coll x]
                   (let [[dst-path src-path tail] x
                         new-path (pop dst-path)
                         val (get-in coll src-path)]
                     (-> coll
                         (dissoc-in dst-path)
                         (assoc-in (conj new-path tail) val))))


        pairs (get-single-child-pairs structure)
        pairs-dups-cleaned (partition 2 (reduce clear-dups [] pairs))
        ;operations should be reversed to start modifications from the bottom of the tree
        tailed-ops-reversed (reverse (map mk-tail pairs-dups-cleaned))
        new-structure (reduce apply-op structure tailed-ops-reversed)]
    new-structure))





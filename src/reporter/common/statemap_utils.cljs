(ns reporter.common.statemap-utils
  (:require [reporter.state :as state]))

(defn- except-root-vals [state-map]
  (let [val-not-root (fn [coll x]
                       (let [[k v] x]
                         (if (empty? k)
                           coll
                           (conj coll v))))]
    (reduce val-not-root '() state-map)))

(defn- apply-to-all-not-root [state-map f]
  (doseq [val (except-root-vals state-map)]
    (state/update-it! val f)))

(defn expand-all [state-map]
  (apply-to-all-not-root state-map state/set-opened))

(defn collapse-all [state-map]
  (apply-to-all-not-root state-map state/set-not-opened))



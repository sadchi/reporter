(ns reporter.path
  (:require [clojure.string :refer [join]]))

(defn flatten-path [p]
  (into [] (flatten p)))

(defn name-path-node [x]
  (if (vector? x)
    (join " / " x)
    x))



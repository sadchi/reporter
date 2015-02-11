(ns reporter.path
  (:require [clojure.string :refer [join]]))

(defn flatten-path [p]
  (if (vector? p)
    (into [] (flatten p))
    [p]))

(defn name-path-node [x]
  (if (vector? x)
    (join " / " x)
    x))



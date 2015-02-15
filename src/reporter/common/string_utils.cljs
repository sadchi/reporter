(ns reporter.common.string-utils
  (:require [clojure.string :as string]
            [goog.string :as gstring]
            [goog.string.format]))



(defn add-zero-spaces [str every]
  (let [pattern (re-pattern (gstring/format  ".{1,%d}" every))]
    (string/join "\u200B" (re-seq pattern str))))
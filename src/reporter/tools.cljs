(ns reporter.tools)


(defn log-obj [prefix obj]
  (.log js/console prefix (clj->js obj)))

(defn log [& msgs]
  (.log js/console (apply str  msgs)))
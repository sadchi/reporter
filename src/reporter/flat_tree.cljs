(ns reporter.flat-tree
  )


(defn- add-level [v x]
  (let [last-val (last v)
        new-val (conj (into [] last-val) x)]
    (conj v new-val)))

(defn create-branch [path]
  (reduce add-level [[]] path))


(defn- apply-multi [f-list x]
  (doseq [f f-list]
    (f x)))


(defn update-up-root [f-tree path f-list]
  (let [branch (create-branch path)
        states (map (partial get f-tree) branch)]
    (dorun (map #(apply-multi f-list %) states))))

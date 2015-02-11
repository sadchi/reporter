(ns reporter.flat-tree)

(defn- add-level [v x]
  (conj v (conj (peek v) x))
  #_(let [last-val (last v)
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

(defn update-up-root-alt [f-tree path f]
  (let [branch (create-branch path)
        objs (map (partial get f-tree) branch)]
    (dorun (map f objs))))


(defn create-varianted-branch [path]
  (let [branch (create-branch path)
        leaf [:leaf (peek branch)]
        root [:root (first branch)]
        others (map (fn [x] [:node x]) (next (pop branch)))]
    (-> (conj [] root leaf)
        (into others))))
(ns io.allthethings.collections)

(defn indexed [coll]
  (map-indexed (fn [index value] [index value]) coll))

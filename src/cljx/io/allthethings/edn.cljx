(ns io.allthethings.edn)

(defn ->str [object]
  #+clj
  (binding [*print-length* false] (pr-str object)))

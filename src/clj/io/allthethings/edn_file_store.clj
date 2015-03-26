(ns io.allthethings.edn-file-store
  (:require [io.allthethings.store :as store]
            [clojure.java.io :as io]
            [clojure.edn :as edn]))

(defn read-edn-collection! [path]
  (with-open [in (java.io.PushbackReader. (io/reader path))]
    (let [edn-seq (repeatedly (partial edn/read {:eof :theend} in))]
      (doall
       (->> (take-while (partial not= :theend) edn-seq)
            (map (fn [{:keys [url content]}]
                   [url content]))
            (into {}))))))

(defn write-edn-item! [path content]
  (spit path (pr-str content) :append true))

(defrecord EdnFileStore [path]
  store/IAppend
  (append [this value]
    (write-edn-item! path value))

  store/ILoadAll
  (load-all [this]
    (read-edn-collection! path)))

(defn edn-file-store [path]
  (EdnFileStore. path))

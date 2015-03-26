(ns io.allthethings.edn-coder
  (:require #+clj [clojure.tools.reader.edn :as edn]
            #+cljs [cljs.reader]

            [io.allthethings.edn :as io-edn]
            #+clj [clojure.tools.reader.edn :as edn]))

(defprotocol ICoder
  (encode [this body])
  (decode [this body]))

(defrecord EdnCoder [readers]
  ICoder
  (decode [this body]
    #+clj
    (edn/read-string {:readers readers} body)
    #+cljs
    (do
      (doseq [[tag constructor] readers]
        (cljs.reader/register-tag-parser! tag constructor))

      (cljs.reader/read-string body)))

  (encode [this body]
    (io-edn/->str body)))

(defn edn-coder [& {:keys [readers]}]
  (map->EdnCoder {:readers readers}))

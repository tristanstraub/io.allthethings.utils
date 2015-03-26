(ns io.allthethings.paths
  (:require [clojure.java.io :as io]))

(defn get-user-path [] (System/getProperty "user.dir"))
(defn get-user-file-path [name] (io/file (get-user-path) name))

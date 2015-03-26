(ns io.allthethings.store)

(defprotocol IAppend
  (append [this value]))

(defprotocol ILoadAll
  (load-all [this]))

(ns io.allthethings.net.connection
  #+cljs
  (:require-macros [cljs.core.async.macros :refer [go]])

  (:require [#+clj clojure.core.async
             #+cljs cljs.core.async
             :as a
             :refer [#+clj >!!
                     #+clj <!!
                     #+clj go
                     #+clj thread
                     #+clj alts!!
                     put! >! <!
                     chan buffer close!
                     alts!
                     timeout]]))

(defprotocol IIncoming
  (<incoming [this]))

(defprotocol IOutgoing
  (>outgoing [this]))

(defrecord Connection [incoming outgoing]
  IIncoming
  (<incoming [this]
    incoming)

  IOutgoing
  (>outgoing [this]
    outgoing))

(defn connection []
  (map->Connection {:incoming (chan) :outgoing (chan)}))

;; (defrecord EdnConnection [connection readers])

;; (defn edn-connection [connection readers]
;;   (let [coder (edn-coder-connection :readers readers)]
;;     (a/pipe (<incoming connection) (<incoming edn-coder)))

;;   (map->Connection {:incoming (chan) :outgoing (chan)}))

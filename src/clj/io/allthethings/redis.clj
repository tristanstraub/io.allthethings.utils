(ns threed.storage
  (:require [taoensso.carmine :as car :refer (wcar)]))

(def server-connection {;; :pool {:max-active 8}
                        :spec {:host     "localhost"
                               :port     6379
                               ;;:password ""
                               :timeout  4000}})

(defmacro wcar* [& body] `(car/wcar server-connection ~@body))

;; store list of events

(ns io.allthethings.net.websockets
  #+cljs
  (:require-macros [cljs.core.async.macros :refer [go]])

  (:require [#+clj clojure.core.async
             #+cljs cljs.core.async
             :as a
             :refer [put! <! chan close! #+clj go]]

            #+clj [compojure.core :refer [GET]]
            #+clj [org.httpkit.server :as server :refer [with-channel on-receive on-close send!]]

            [io.allthethings.net.connection :as connection :refer [connection]]))

#+cljs
(defn- hook-client-connection! [conn & {:keys [incoming outgoing]}]
  (set! (.-onerror conn) (fn [] (throw (js/Error. "ws error"))))

  (set! (.-onclose conn) (fn [e]
                           (close! outgoing)
                           (close! incoming)))

  ;; Incoming messages
  (set! (.-onmessage conn) (fn [e] (put! incoming (.-data e))))

  ;; Outgoing messages
  (set! (.-onopen conn) (fn [e] (go (loop []
                                      (.send conn (<! outgoing))
                                      (recur))))))

#+clj
(defn- send-message! [conn message]
  (send! conn message))

#+clj
(defn- hook-server-connection! [conn & {:keys [incoming outgoing]}]
  ;; Closing channels
  (on-close conn (fn [status]
                   (close! outgoing)
                   (close! incoming)))

  ;; Incoming messages
  (on-receive conn (fn [msg] (put! incoming msg)))

  ;; Outgoing messages
  (go (loop []
        (send-message! conn (<! outgoing))
        (recur))))

(defn- connection! [conn]
  (let [c (connection)]
    ;; Bind listeners
    (#+clj hook-server-connection! #+cljs hook-client-connection!
           conn :incoming (connection/<incoming c) :outgoing (connection/>outgoing c))

    c))

;; -- Connection feed --

(defprotocol IRoute
  (route [this path]))

(defprotocol IConnections
  (<connections [this]))

(defn strip-leading-wack [path]
  "Remove leading forward slash from url path"
  (if (= \/ (first path))
    (apply str (rest path))
    path))

(defrecord ConnectionFeed [connections]
  IRoute
  (route [this path]
    "Returns a request handler that can be attached to route on the server, or called
directly in the browser"
    #+clj
    (GET path [] (fn [req] (with-channel req conn
                             (put! connections (connection! conn)))))
    #+cljs
    (fn []
      (let [loc (.-location js/window)
            schema (if (= (.-protocol loc) "https:") "wss:" "ws:")
            url (str schema "//" (.-host loc) (.-pathname loc) (strip-leading-wack path))]
        (defonce conn (js/WebSocket. url))
        (println url)
        (put! connections (connection! conn)))))

  IConnections
  (<connections [this]
    "Returns a channel which contains all new connections"
    connections))

(defn connection-feed []
  (map->ConnectionFeed {:connections (chan)}))

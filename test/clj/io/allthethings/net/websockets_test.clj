(ns io.allthethings.net.websockets-test
  (:use midje.sweet)
  (:require ;;clojure.test
   [clojure.core.async
    :as a
    :refer [put! <! chan close! go]]
   [clj-webdriver.core :refer [quit to]]
   [clj-webdriver.remote.server :refer [new-remote-session stop]]
   ;; high level driver for webdriver
   [clj-webdriver.taxi :as taxi]
   ;; run webserver we want to test
   [org.httpkit.server :as http-kit]
   [compojure.core :refer [GET defroutes routes]]
   [net.cgrand.enlive-html :refer [deftemplate]]
   [compojure.route :refer [resources]]
   [clojure.java.io :as io]
   [io.allthethings.net.websockets :as ws]
   [io.allthethings.net.connection :as cn]
   [hiccup.core :refer [html]])
  (:import [java.util.logging Level])
  (:import (java.net NetworkInterface)))

(def PORT 10001)

(def ip
  (let [ifc (NetworkInterface/getNetworkInterfaces)
        ifsq (enumeration-seq ifc)
        ifmp (map #(bean %) ifsq)
        ipsq (filter #(false? (% :loopback)) ifmp)
        ipa (map :interfaceAddresses ipsq)
        ipaf (last ipa)
        ipafs (.split (str ipaf) " ")
        ips (first (nnext ipafs))]

    (str (second (.split ips "/")))))

;;(def ip (.getHostAddress (java.net.InetAddress/getLocalHost)))

(defn local-addresses []
  (->> (java.net.NetworkInterface/getNetworkInterfaces)
       enumeration-seq
       (map bean)
       (filter (complement :loopback))
       (mapcat :interfaceAddresses)
       (map #(.. % (getAddress) (getHostAddress)))))

(deftemplate page
  (io/resource "index.html") [] [:body])

(defn http-handler [feed]
  (routes
   (resources "/")
   (ws/route feed "/ws")
   (GET "/" req {:body (html [:head
                              [:script {:src "/js/out/goog/base.js"}]
                              [:script {:src "/js/app.js"}]
                              [:script "goog.require('io.allthethings.net.websockets-test')"]
                              [:script "console.log('test');"]]
                             [:div "hello"])})))

(defonce httpserver (atom nil))
(defonce connection-feed (atom nil))

(defn stop-http-server! []
  (when @httpserver
    (cn/close-connection! @connection-feed)
    (reset! connection-feed nil)

    (@httpserver)
    (reset! httpserver nil)))


(defn listen-to-messages! [conn message-promise]
  (println "conn:" conn)
  (go (loop []
        (when-let [message (<! (cn/<incoming conn))]
          (deliver message-promise message)
          (println "message:" message)
          (recur)))
      (println "no more messages")))

(defn start-http-server! [message-promise]
  (assert (not @connection-feed))
  (let [feed (ws/connection-feed)]
    (reset! connection-feed feed)

    (when (not @httpserver)
      (reset! httpserver
              (http-kit/run-server (http-handler feed) {:port PORT})))
    (go (loop []
          (when-let [conn (<! (ws/<connections feed))]
            (println "wait for conn")
            (put! (cn/>outgoing conn) "Hello!")
            (listen-to-messages! conn message-promise)
            (recur)))
        (println "no more connections"))))

(defn setup! [message-promise]
  (println "create session")
  (start-http-server! message-promise)
  (let [[server driver]
        (new-remote-session {:port 4444 :host "localhost" :existing true}
                            {:browser :chrome})]

    (taxi/set-driver! driver)))

(defn teardown! []
  (println "end session")
  (taxi/quit)
  (stop-http-server!)
  (println "session ended"))

(let [message-promise (promise)]
  (with-state-changes [(before :facts (setup! message-promise)) (after :facts (teardown!))]
    (fact "Test connection"
          (do
            (println "going to google")
            (taxi/to (str "http://" ip ":" PORT))
            (println "screenshot")
            (taxi/take-screenshot :file "/tmp/google.png")
            (read-string @message-promise)) => {:message "hello"})))

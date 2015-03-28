(ns io.allthethings.net.websockets-test
  (:use midje.sweet)
  (:require ;;clojure.test
   [clj-webdriver.core :refer [quit to]]
   [clj-webdriver.remote.server :refer [new-remote-session stop]]
   ;; high level driver for webdriver
   [clj-webdriver.taxi :as taxi]
   ;; run webserver we want to test
   [org.httpkit.server :as http-kit]
   [compojure.core :refer [GET defroutes routes]]
   [net.cgrand.enlive-html :refer [deftemplate]]
   [clojure.java.io :as io])
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

(deftemplate page
  (io/resource "index.html") [] [:body] identity)

(defn http-handler []
  (routes
   (GET "/" req (page))))

(defonce httpserver (atom nil))

(defn stop-http-server! []
  (when @httpserver
    (@httpserver)
    (reset! httpserver nil)))

(defn start-http-server! []
  (when (not @httpserver)
    (reset! httpserver
            (http-kit/run-server (http-handler) {:port PORT}))))

(defn setup! []
  (println "create session")
  (start-http-server!)
  (let [[server driver]
        (new-remote-session {:port 4444 :host "localhost" :existing true}
                            {:browser :chrome})]

    (taxi/set-driver! driver)))

(defn teardown! []
  (println "end session")
  (taxi/quit)
  (stop-http-server!)
  (println "session ended"))

(with-state-changes [(before :facts (setup!)) (after :facts (teardown!))]
  (fact "Test connection"
        (println "going to google")
        (taxi/to (str "http://" ip ":" PORT))
        (println "screenshot")
        (taxi/take-screenshot :file "/tmp/google.png")))

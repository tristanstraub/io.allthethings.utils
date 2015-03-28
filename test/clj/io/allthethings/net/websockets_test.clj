(ns io.allthethings.net.websockets-test
  (:use midje.sweet)
  (:require ;;clojure.test
        [clj-webdriver.core :refer [quit to]]
        [clj-webdriver.remote.server :refer [new-remote-session stop]]
        [clj-webdriver.taxi :as taxi])
  (:import [java.util.logging Level]))

(defonce global-driver (atom nil))

(defn setup! []
  (println "create session")
  (let [[server driver]
        (new-remote-session {:port 4444 :host "localhost" :existing true}
                            {:browser :chrome})]
    (reset! global-driver driver)
    (taxi/set-driver! driver)))

(defn teardown! []
  (println "end session")
  (quit @global-driver)
  (println "session ended"))

(with-state-changes [(before :facts (setup!)) (after :facts (teardown!))]
  (fact "Test connection"
        (to @global-driver "http://google.com")

        ))

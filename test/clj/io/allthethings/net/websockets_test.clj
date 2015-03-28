(ns io.allthethings.net.websockets-test
  (:use midje.sweet)
  (:require ;;clojure.test
        [clj-webdriver.core :refer [quit to]]
        [clj-webdriver.remote.server :refer [new-remote-session stop]]
        [clj-webdriver.taxi :as taxi])
  (:import [java.util.logging Level]))

(fact "Test connection"
      (println "create session")
      (let [[server driver] (new-remote-session {:port 4444 :host "localhost" :existing true} {:browser :chrome})]
        (taxi/set-driver! driver)
        (println "end session")
        (to driver "http://google.com")
        (quit driver)
        (println "session ended")))

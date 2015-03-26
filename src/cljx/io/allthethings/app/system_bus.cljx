(ns io.allthethings.app.system-bus
  #+cljs
  (:require-macros [cljs.core.async.macros :refer [go]])

  (:require #+clj
            [clojure.core.async
             :as a
             :refer [put! >! <! >!! <!! go chan buffer close! thread
                     alts! alts!! timeout]]

            #+cljs
            [cljs.core.async :refer [put! chan <!]]

            #+cljs
            [cljs.reader]))

;; (declare <get-messages -send-message!)

;; (defprotocol ISendMessage
;;   (send-message! [this message]))

;; (defprotocol ISubscribe
;;   (subscribe! [this topic]))

;; (defn topic-matches? [topic message]
;;   (let [topic-keys (keys topic)]
;;     (= (select-keys message topic-keys)
;;        (select-keys topic topic-keys))))

;; (defprotocol IChannel
;;   (<get-messages [this]))

;; (defrecord SystemBus [subscriptions messages channels clients]
;;   component/Lifecycle
;;   (start [this]
;;     (let [subscriptions (atom [])
;;           messages (<get-messages (async/merge channels))]
;;       (go (loop []
;;             (let [message (<! messages)]
;;               (doseq [{:keys [topic subscription-channel]} @subscriptions]
;;                 (when (topic-matches? topic message)
;;                   (put! subscription-channel message)))
;;               (recur))))
;;       (assoc this :subscriptions subscriptions :messages messages)))

;;   ISendMessage
;;   (send-message! [this client message]
;;     (-send-message! message))

;;   ISubscribe
;;   (subscribe! [this topic]
;;     (let [channel (chan)]
;;       (swap! subscriptions conj {:topic topic :subscription-channel channel})
;;       channel)))

;; (defn system-bus []
;;   (map->SystemBus {}))

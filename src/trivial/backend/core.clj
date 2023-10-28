(ns trivial.backend.core
  (:require
   [org.httpkit.server :as http-kit
    :refer [on-close on-receive with-channel]]
   [trivial.backend.db :refer
    [hexify xtdb-node save!]]
   [trivial.shared.com :as com]
   [trivial.shared.core :refer
    [-game-template- send*]]
   [trivial.shared.spec :as spec]
   [trivial.shared.utils :refer [deep-merge]]
   [xtdb.api :as xt]))

(defonce state
  (atom {}))

;; (set-validator!
;;  state
;;  #(s/valid? ::spec/global-state %))

(defmulti with-channel-for-user
  (fn [by _] by))

(defmethod with-channel-for-user
  ::spec/name
  [_ name]
  (first (first (filter
                 (fn [[_ {{name* ::spec/name}
                          ::spec/user}]]
                   (= name* name))
                 @state))))

(defn send-message
  [{name ::spec/name} text]
  (let [c (with-channel-for-user ::spec/name name)
        messages (@state ::spec/messages)]
    (send* c {::spec/messages
              (vec (conj messages
                         {:message/text text
                          :message/read? false}))})))

(defn send-answer
  [{name ::spec/name} n text]
  (let [c (with-channel-for-user ::spec/name name)
        answers (@state ::spec/user-answers)]
    (send* c {::spec/user-answers
              (assoc answers n text)})))

(defmulti handler
  (fn [handler-type message-type channel old new]
    [handler-type message-type]))

(defmethod handler
  [:server :incoming]
  [_ _ channel state
   {end? ::spec/end?
    :as msg}]
  (when (= true end?) (save! msg))
  (assoc state channel msg))

(defmethod handler
  [:server :outcoming]
  [_ _ channel old new]
  (get new channel))

(defn close!
  [c]
  (swap! state dissoc c))

(defn web-handler [request]
  #_{:clj-kondo/ignore [:unresolved-symbol]}
  (with-channel request channel
    (let [{:keys [on-state-change
                  on-message-received]} (-game-template- :server channel state handler)]
      (swap! state assoc channel
             {})
      (add-watch state
                 []
                 (fn [_ _ old new]
                   (on-state-change old new)))
      (on-close channel
                (fn [_]
                  (close! channel)))
      (on-receive channel
                  (fn [data]
                    (on-message-received data))))))

(defonce server
  (http-kit/run-server
   web-handler {:port com/port}))

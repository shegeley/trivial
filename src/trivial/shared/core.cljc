(ns trivial.shared.core
  (:require
   [trivial.shared.utils :refer [deep-merge]]
   [clojure.edn :as edn]
   #?@(:cljs [[clojure.core.async :as a]
              [cljs.core.async.impl.channels :refer [ManyToManyChannel]]]
       :clj [[org.httpkit.server :refer [send!]]])
   [tick.core :as t])
  (:import #?@(:clj [[org.httpkit.server AsyncChannel]])))

(defprotocol Chan
  (send* [this data])
  (buf-size [this]))

(defn atom? [x]
  #?(:clj  (instance? clojure.lang.Atom x)
     :cljs (satisfies? cljs.core/IAtom x)))

(def edn-opts
  {:readers {'time/instant t/instant}})

(defn str->edn [x]
  (if (string? x)
    (try (edn/read-string edn-opts x)
         (catch #?(:clj Exception
                   :cljs js/Error) e
           (println "error when parsing!")
           x))
    x))

(defn edn->str [x]
  (str x))

#?(:clj
   (extend-type AsyncChannel
     Chan
     (buf-size [_]
       nil)
     (send* [this data]
       (send! this (edn->str data)))))

#?(:cljs
   (extend-type ManyToManyChannel
     Chan
     (buf-size [this]
       (.-n (.-buf this)))
     (send* [this data]
       (a/go
         (a/>! this data)))))

(defn -game-template-
  "Data structure that defines the general online-game pattern. It's the same for the backend and for the frontend.
  `handler` - unpure function that mutates some variables and returns (presumably) the state diff (something that can be `deep-merged` with the current state). IT SHOULD NOT EVER MUTATE THE STATE ITSELF. This will lead to the endless loop
  It has to handle cartesian product `#{:client :server}` x `#{:incoming :outcoming}` except for pair #{:client :outcoming}. We just want to send the client state completely as it is by-default
  The buffer size has to be 1 on a client to prevent desyncronzation of client and server state"
  [type channel state handler]
  {:pre [(or (= type :server)
             (= type :client))
         (if (= type :client)
           (= 1 (buf-size channel))
           true)
         (satisfies? Chan channel)
         (atom? state)]}
  {:on-state-change
   (fn [old new]
     (when (not= old new)
       (let [h (if (= type :client)
                 new
                 (handler type :outcoming channel old new))]
         (send* channel (edn->str h)))))
   :on-message-send
   ;; NOTE: here has to wait and don't sent any messages until recieving an answer
   ;; (on frontend only?)
   (fn [msg] nil)
   :on-message-received
   (fn [msg]
     (let [m (str->edn msg)
           cm (handler type :incoming channel @state m)]
       (reset! state cm)))})

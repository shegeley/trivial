(ns trivial.frontend.core
  (:require
   [clojure.core.async :as a]
   [clojure.spec.alpha :as s]
   [haslett.client :as ws]
   [reagent-dev-tools.core :as dev-tools]
   [reagent.core :as r]
   [reagent.dom :as dom]
   [trivial.frontend.ui.core :as ui]
   [trivial.shared.com :as com]
   [trivial.shared.core :refer [-game-template-]]
   [trivial.shared.seed :as seed]
   [trivial.shared.spec :as spec]
   [trivial.shared.utils :refer [deep-merge]]
   [tick.core :as t]))

(defonce state
  (r/atom {::spec/round seed/round
           ::spec/end? false
           ::spec/messages []
           ::spec/user-answers {}}))

(defonce channel
  (a/chan 1))

(defonce client
  (ws/connect com/url
              {:sink channel
               :source channel}))

(defmulti handler
  (fn [handler-type message-type channel _ _]
    [handler-type message-type]))

(defmethod handler
  [:client :incoming]
  [_ _ _ old-state msg]
  (deep-merge old-state msg))

(when ^boolean goog.DEBUG
  (dev-tools/start! {:state-atom state}))

(defn start! []
  (let [{:keys [on-message-received
                on-state-change]} (-game-template- :client channel state handler)
        start-online-game! (fn []
                             (remove-watch state [::spec/user ::spec/name])
                             (swap! state assoc ::spec/start (t/now))

                             (add-watch
                              state []
                              (fn [_ _ old new]
                                (on-state-change old new)))
                             (a/go-loop []
                               (let [m (a/<! channel)]
                                 (when (some? m)
                                   (on-message-received m)
                                   (recur)))))]
    (add-watch
     state [::spec/user
            ::spec/name]
     (fn [_ state old new]
       (println @state)
       (when (and (= (@state ::spec/mode) :online)
                  (some? new))
         (do
           (println "online game started")
           (start-online-game!)))))))

(defn ^{:dev/after-load true}
  main []
  (a/go
    (start!)
    (dom/render
     [ui/template
      [ui/game channel state]]
     (js/document.getElementById "app"))))

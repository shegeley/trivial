(ns trivial.frontend.ui.core
  (:require
   [clojure.core.async :as a]
   [clojure.spec.alpha :as s]
   [reagent-mui.cljs-time-adapter :refer [cljs-time-adapter]]
   [reagent-mui.colors :as colors]
   [reagent-mui.components :refer
    [alert badge box button button-group card card-actions card-content
     card-header collapse container css-baseline fab form-group form-helper-text
     grid icon-button list-item list-item-text text-field typography]]
   [reagent-mui.icons.close :refer [close]]
   [reagent-mui.lab.loading-button :refer [loading-button]]
   [reagent-mui.styles :as styles]
   [reagent-mui.x.localization-provider :refer [localization-provider]]
   [reagent.core :as r]
   [tick.core :as  t]
   [trivial.shared.spec :as spec])
  (:import
   (goog.i18n DateTimeSymbols_en_US)))

(def custom-theme
  {:palette {:primary colors/green}})

(defn template [content]
  [:<> ;; React  fragment
   [css-baseline]
   [localization-provider {:date-adapter cljs-time-adapter
                           :adapter-locale DateTimeSymbols_en_US}
    [styles/theme-provider (styles/create-theme custom-theme)
     [grid {:container true
            :style {:margin-top "5%"}}
      content]]]])

(defn event-value
  [e]
  (-> e .-target .-value))

(defn user-prompt
  [_ state]
  (let [local-state (r/atom "")]
    (fn []
      [form-group
       [form-helper-text "Introduce yourself to the system"]
       [text-field
        {:variant :outlined
         :value @local-state
         :label "Username"
         :on-change #(reset! local-state (event-value %))}]
       [button {:variant :contained
                :style {:margin-top "2%"}
                :on-click #(reset! state @local-state)}
        "Submit"]])))

(defn trivial
  [{q ::spec/question
    os ::spec/options
    p ::spec/points
    n :number
    oa ::spec/others-answers
    :as p}
   state]
  {:pre (s/valid? p ::spec/trivial)}
  (let [button*
        (fn [{:keys [text n]} state]
          [button
           {:variant (if (= text @state)  :contained :outlined)
            :on-click #(if (= text @state)
                         (reset! state nil)
                         (reset! state text))}
           [badge {:badge-content n
                   :color :secondary
                   :show-zero true}
            [typography text]]])]
    (fn []
      [card
       {:style {:height "100%"}}
       [card-header {:title (if (some? n)
                              (str "Question №" n)
                              (str "100B$ question"))}]
       [card-content q]
       [card-actions
        {:disable-spacing true}
        [button-group
         {:full-width true
          :orientation :vertical}
         (map
          (fn [[text _]]
            (let [n (or (count (filter #(= text %)
                                       (concat oa [@state]))) 0)]
              [button* {:text text
                        :n n} state])) os)]]])))

(defn timer
  [start _]
  {:pre (s/valid? ::spec/timestamp start)}
  (let [t (r/atom (t/now))]
    (fn []
      (js/setTimeout #(reset! t (t/>> @t (t/new-duration 1 :seconds))) 1000)
      [fab {:color :success}
       (str "⏰"
            (t/seconds
             (t/between start @t)))])))

(defn finish
  [props state]
  (fn []
    [fab (merge {:color :success}
                props
                {:on-click #(reset! state true)})
     (str "🏁")]))

(defn round
  [{trivials ::spec/trivials} state]
  [grid {:container true
         :spacing 1}
   (map-indexed
    (fn [i r]
      [grid {:item true
             :xs 12
             :md 6
             :lg 6
             :zero-min-width true}
       [trivial (merge r {:number (inc i)})
        (r/cursor state [i])]])
    trivials)])

(defn message
  [{t :message/text
    r? :message/read?}
   state]
  (fn []
    (when (not @state)
      [box
       {:width "100%"
        :mb 2}
       [collapse {:in (not @state)}
        [alert {:severity :info
                :action (r/as-element
                         [icon-button
                          {:aria-label :close
                           :color :inherit
                           :size :small
                           :on-click #(reset! state true)}
                          [close {:font-size :inherit}]])}
         t]]])))

(defn messages
  [_ state]
  [:<> (map-indexed
        (fn [i msg]
          [message msg
           (r/cursor state [i :message/read?])])
        @state)])

(defn page
  [content]
  [container content])

(defn mode
  [client state]
  [form-group
   [typography "Choose the game mode"]
   (if (some? client)
     [button {:on-click #(reset! state :online)}
      "Online 🌐"]
     [loading-button {:loading true
                      :loading-position :start}
      "Connecting..."])
   [button {:on-click #(reset! state :local)}
    "Local 🚫"]])

(defn endgame
  [_ state]
  (let [answers (get @state ::spec/user-answers)
        round (get @state ::spec/round)
        stats (mapv
               (fn [[k v]]
                 (let [{options ::spec/options
                        points ::spec/points
                        question ::spec/question
                        :as r}
                       (get-in round [::spec/trivials k])
                       points* (if (some? (get (into {} options) v))
                                 points 0)]
                   {:question question
                    :answer v
                    :points points*})) answers)
        total (reduce + (map :points stats))]
    [grid {:container true}
     [typography {:variant :h3} "Game Over"]
     [list
      (map
       (fn [{:keys [question answer points]}]
         (let [text (str "«" question
                         "», your answer «" answer
                         "», points - " points)]
           (r/as-element
            [list-item ^{:key text}
             [list-item-text text]])))
       stats)]
     [typography (str "Total score: " total)]]))

(defn game
  [channel state]
  (fn []
    [page
     (if (get @state ::spec/end?)
       [endgame nil state]
       (if-let [_ (get @state ::spec/mode)]
         (if-let [_ (get @state ::spec/user)]
           [:<>
            [messages
             nil
             (r/cursor state [::spec/messages])]
            [round
             (get @state ::spec/round)
             (r/cursor state [::spec/user-answers])]
            [grid {:container true
                   :style {:margin-top "2%"}}
             [grid {:item :true :xs 1}
              [finish {:style {:position :sticky}}
               (r/cursor state [::spec/end?])]]
             [grid {:item true :xs 10}]]]

           [user-prompt "" (r/cursor state [::spec/user ::spec/name])])
         [mode channel (r/cursor state
                                 [::spec/mode])]))]))

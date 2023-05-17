(ns trivial.frontend.cards
  (:require
   ["highlight.js" :as hljs]
   ["marked" :refer [marked]]
   [devcards.core :as cards
    :include-macros true]
   [reagent.core :as r]
   [tick.core :as t]
   [trivial.frontend.core :refer [client]]
   [trivial.frontend.ui.core :as ui]
   [trivial.shared.seed :as s]
   [trivial.shared.spec :as spec]))

;; https://github.com/bhauman/devcards/blob/master/src/devcards/util/markdown.cljs#L28
(js/goog.exportSymbol "DevcardsSyntaxHighlighter" hljs)
(js/goog.exportSymbol "DevcardsMarked" marked)

(cards/defcard-doc
  "# Dev-Cards
   Integrated isolated frontend development & testing. It came out before [Storybook](https://storybook.js.org/). Supports *markdown*.")

(defonce trivial
  (r/atom nil))

(cards/defcard
  "Base card for trivial"
  (cards/reagent
   (fn [data _]
     [ui/template [ui/trivial s/trivial data]]))

  trivial
  {:inspect-data true
   :history true})

(cards/defcard
  "Base card for timer"
  (cards/reagent
   (fn [_ _]
     [ui/template [ui/timer (t/now) nil]])))

(defonce finish (r/atom false))

(cards/defcard
  "Base card for game end"
  (cards/reagent
   (fn [data _]
     [ui/template [ui/finish nil data]]))
  finish
  {:inspect-data true})

(defonce round (r/atom {}))

(cards/defcard
  "Base card for round"
  (cards/reagent
   (fn [data _]
     [ui/template [ui/round s/round data]]))
  round
  {:inspect-data true
   :history true})

(defonce user (r/atom ""))

(cards/defcard
  "Base card for user prompt"
  (cards/reagent
   (fn [data _]
     [ui/template [ui/user-prompt {} data]]))
  user
  {:inspect-data true})

(defonce mode (r/atom nil))

(cards/defcard
  "Base card for mode choice"
  (cards/reagent
   (fn [data _]
     [ui/template [ui/mode client data]]))
  mode
  {:inspect-data true})

(defonce message
  (r/atom false))

(cards/defcard
  "Base card for the incoming message"
  (cards/reagent
   (fn [data _]
     [ui/template
      [ui/message
       #:message{:text "kek!"
                 :read? false} data]]))
  message
  {:inspect-data true
   :history true})

(defonce endgame*
  (r/atom {::spec/round s/round
           ::spec/user-answers
           {0 "Good"
            1 "No 🧟"
            2 "No ^.^"
            3 "Cats 🐈"
            4 "Thor"}
           ::spec/mode :local
           ::spec/end? true}))

(cards/defcard
  "Base card for the endgame screen"
  (cards/reagent
   (fn [data _]
     [ui/template [ui/endgame nil data]]))
  endgame*
  {:inspect-data true})

(defonce game
  (r/atom {::spec/round s/round
           ::spec/user-answers {}
           ::spec/messages [#:message{:text "Hello"
                                      :read? false}
                            #:message{:text "All good?"
                                      :read? false}]
           ::spec/mode nil
           ::spec/end? false}))

(cards/defcard
  "Base card for the game itself"
  (cards/reagent
   (fn [data _]
     [ui/template [ui/game @data data]]))
  game
  {:inspect-data true
   :history true})

(defn ^{:export true}
  main []
  (cards/start-devcard-ui!))

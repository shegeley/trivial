(ns trivial.shared.spec
  (:require
   #?@(:clj [[clojure.spec.alpha :as s]]
       :cljs [[cljs.spec.alpha :as s]])
   [trivial.shared.core :refer [Chan]]))

(s/def ::id string?)

(s/def ::name string?)

(s/def :event/id keyword?)

(s/def :event/payload vector?)

(s/def ::event
  (s/tuple :event/id :event/payload))

(s/def ::question string?)

(s/def ::timestamp
  #?(:clj java.time.LocalDateTime
     :cljs goog.date.DateTime))

(s/def ::option
  (s/tuple string? boolean?))

(s/def ::options
  (s/coll-of ::option))

(s/def ::answer string?)

(s/def ::user-answers
  (s/map-of number? ::answer))

(s/def ::others-answers
  (s/coll-of ::answer))

(s/def ::points int?)

(s/def ::start ::timestamp)

(s/def ::trivial
  (s/keys :req [::question ::points ::options]
          :opt [::others-answers]))

(s/def :trivial/payload
  ::answer)

(s/def ::trivials
  (s/coll-of ::trivial
             :kind vector?))

(s/def ::round
  (s/keys :req [::trivials
                ::id]))

(s/def :round/payload ::user-answers)

(s/def :message/read? boolean?)

(s/def :message/text string?)

(s/def ::message
  (s/keys :req [:message/read?
                :message/text]))

(s/def ::messages
  (s/coll-of ::message))

(s/def ::user
  (s/keys :req [::name]))

(s/def ::users
  (s/coll-of ::user))

(s/def ::end? boolean?)

(s/def ::mode
  #{:local :online})

(s/def ::local-state
  (s/keys :req
          [::user ::round ::user-answers
           ::start ::messages ::mode ::end?]))

(s/def ::global-state
  (s/map-of ::channel ::local-state))

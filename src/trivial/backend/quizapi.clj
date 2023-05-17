(ns trivial.backend.quizapi
  (:require [clojure.core.async :as a]
            [trivial.shared.spec :as spec]
            [clojure.spec.alpha :as s]
            [trivial.backend.env :as e]
            [clojure.set :as set]
            [clj-http.client :as http]))

(def key*
  (e/get* :quiz-api "quizapi.io key"))

(defn hexify [s]
  (format "%x" (new java.math.BigInteger (.getBytes s))))

(defn quiz!
  ([] (quiz! {:limit 10 :key key*}))
  ([params]
   (let [base "https://quizapi.io/api/v1/questions"]
     (-> (http/get base
                   {:query-params
                    (set/rename-keys params
                                     {:key "apiKey"})
                    :as :json})
         :body))))

(defn quiz->trivial
  "Let's call the single element of the json response [ref:1] (json array's single element) from quiz api 'quizzle'"
  [x]
  {:post [(s/valid? ::spec/trivial %)]}
  (let [options (loop [answers (x :answers)
                       correct-answers (x :correct_answers)
                       acc []]
                  (if (seq answers)
                    (let [[id text :as a] (first answers)
                          cid (keyword (str (name id) "_correct"))
                          correct? (= "true" (get correct-answers cid))]
                      (recur
                       (dissoc answers id)
                       (dissoc correct-answers cid)
                       (if (some? text)
                         (conj acc [text correct?])
                         acc)))
                    acc))]
    #::spec{:question (x :question)
            :options options
            :points 1}))

(defn quiz->round
  [q]
  (let [trivials (map quiz->trivial q)]
    {::spec/trivials trivials
     ::spec/id (hexify (hash trivials))}))

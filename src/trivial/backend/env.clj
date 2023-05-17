(ns trivial.backend.env
  (:require [environ.core :as e]))

(def envs
  (atom {}))

(defn prompt! [what]
  (println (str "Please enter your " what ":"))
  (read-line))

(defn get* [id what]
  (let [e (or (e/env id)
              (prompt! what))]
    (swap! envs assoc id
           {:description what
            :value e})
    e))

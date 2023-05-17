(ns trivial.backend.auth
  (:require [trivial.shared.spec :as spec]))

(defn with-user
  "Simply extracts user from the payload"
  [payload f]
  (let [user (get payload ::spec/user)]
    (if (some? user)
      (f user payload)
      (throw (Exception. "User not introduced")))))

(defn with-auth-user
  "Simply extracts user from the payload and checks if there is a user like that in it's current global state"
  [payload state f]
  (with-user payload
    (fn [user payload]
      (let [user* (get state user)]
        (if (some? user*)
          (f user* payload state)
          (throw (Exception. "User not found")))))))

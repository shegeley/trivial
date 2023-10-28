(ns trivial.backend.db
  (:require
   [clojure.java.io :as io]
   [trivial.shared.utils :refer [deep-merge]]
   [trivial.shared.spec :as spec]
   [xtdb.api :as xt]))

(defn hexify [s]
  (apply str
         (map #(format "%02x" (int %)) s)))

(defn start-xtdb! []
  (letfn [(kv-store [dir]
            {:kv-store {:xtdb/module 'xtdb.rocksdb/->kv-store
                        :db-dir (io/file dir)
                        :sync? true}})]
    (xt/start-node
     {:xtdb/tx-log (kv-store "data/dev/tx-log")
      :xtdb/document-store (kv-store "data/dev/doc-store")
      :xtdb/index-store (kv-store "data/dev/index-store")})))

(defonce xtdb-node
  (start-xtdb!))

(defn save! [x]
  (let [xt-id (keyword (hexify (str (hash x))))
        p (deep-merge {:xt/id xt-id} x)]
    (xt/submit-tx xtdb-node [[::xt/put p]])))

(defn games*
  []
  (xt/q (xt/db xtdb-node)
        '{:find [(pull ?game [*])]
          :where [[?game ::spec/end? ?end]]}))

(defn drop-all!
  []
  (let [res (xt/q (xt/db xtdb-node)
                  '{:find [id]
                    :where [[id :xt/id _]]})
        ids (map first res)]
    (->> ids
         (mapv (fn [id] [::xt/delete id]))
         (xt/submit-tx xtdb-node))))

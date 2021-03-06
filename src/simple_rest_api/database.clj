(ns simple-rest-api.database
  (:require [io.pedestal.interceptor :as pedestal]
            [clojure.java.io :as io]
            [clojure.data.json :as json]
            [com.stuartsierra.component :as component]
            #_[clojure.tools.namespace.repl :refer (refresh)]))



(defrecord Database [connection]
  ;; Implement the Lifecycle protocol
  component/Lifecycle
    (start [component]
      (println ";; Starting database")
      (assoc (assoc component :connection "connected")
        :data (into {}
                    (map (juxt :id identity)
                      (json/read-str (slurp (io/resource "indicators.json"))
                                     :key-fn
                                     keyword)))))
    (stop [component]
      (println ";; Stopping database")
      (assoc component :connection nil)))


(defprotocol QueryMethods
  (lookup-by-id [this id])
  (lookup-by-parameters [this params])
  (select-all-records [this]))

(defrecord DBQueryComponent [database]
  component/Lifecycle
    (start [this]
      (println ";; Starting ExampleComponent")
      (assoc this :database database))
    (stop [this]
      (println ";; Stopping ExampleComponent")
      (dissoc this :database))
  QueryMethods
    (lookup-by-id [this id] (get (:data database) id :no-document-found))
    (lookup-by-parameters [this params]
      (letfn [(has-entries? [m] (= (select-keys m (keys params)) params))]
        (if (seq params)
          (filter has-entries? (select-all-records this))
          (select-all-records this))))
    (select-all-records [this] (vals (:data database)))
  pedestal/IntoInterceptor
    (-interceptor [this]
      (pedestal/map->Interceptor
        {:name :database-interceptor,
         :enter (fn [context]
                  (assoc-in context
                    [:request :db-query]
                    {:lookup-by-id (fn [id] (lookup-by-id this id)),
                     :lookup-by-parameters
                       (fn [params] (lookup-by-parameters this params))})),
         :leave (fn [context]
                  (assoc-in context [:response :database] "database accessed")),
         :error (fn [context]
                  (assoc-in context [:response :database] "database error"))})))


(defn new-database [] (map->Database {}))

(defn db-query-component [] (map->DBQueryComponent {}))

(defn query-system
  []
  (component/system-map :db (new-database)
                        :app (component/using (db-query-component)
                                              {:database :db})))


(def system (atom nil))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;component repl code
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;(def system nil)

#_(defn init [] (alter-var-root #'system (constantly (query-system))))

#_(defn start [] (alter-var-root #'system component/start))

#_(defn stop [] (alter-var-root #'system (fn [s] (when s (component/stop s)))))

#_(defn go [] (init) (start))

#_(defn reset [] (stop) (refresh :after 'simple-rest-api.database/go))

;(go)

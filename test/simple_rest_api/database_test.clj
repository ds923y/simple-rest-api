(ns simple-rest-api.database-test
  (:require [clojure.test :refer :all]
            [clojure.data.json :as json]
            [com.stuartsierra.component :as component]
            [simple-rest-api.database :as database]))

(def test-db-system (atom nil))
(def mock-db-system (atom nil))

(defn with-test-db
  [f]
  (reset! test-db-system (component/start (database/query-system)))
  (f)
  (component/stop @test-db-system))

(use-fixtures :once with-test-db)


(deftest lookup-by-id-test1
  (is (= (database/lookup-by-id (:app @test-db-system)
                                "55b3bbdb409108c74577353e0")
         :no-document-found)))

(deftest lookup-by-id-test2
  (is (= (:id (database/lookup-by-id (:app @test-db-system)
                                     "5b3bbdb409108c74577353e0"))
         "5b3bbdb409108c74577353e0")))

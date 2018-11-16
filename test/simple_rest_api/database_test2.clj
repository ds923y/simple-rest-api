(ns simple-rest-api.database-test2
  (:require [clojure.test :refer :all]
            [clojure.data.json :as json]
            [com.stuartsierra.component :as component]
            [simple-rest-api.database :as database]))

(def mock-db-system (atom nil))

(defn test-system
  []
  (component/system-map :db {:data {:id-1234 {:z 1234},
                                    :id-5678 {:b 5678},
                                    :id-param1 {:a 1, :b 2, :c 3},
                                    :id-param2 {:a 3, :b 2, :c 3},
                                    :id-param3 {:a 4, :b 2, :c 3},
                                    :id-param4 {:a 7, :b 3, :c 3}}}
                        :app (component/using (database/db-query-component)
                                              {:database :db})))

(defn with-mock-db
  [f]
  (reset! mock-db-system (component/start (test-system)))
  (f)
  (component/stop @mock-db-system))

(use-fixtures :once with-mock-db)

(deftest lookup-by-id-test3
  (is (= (database/lookup-by-id (:app @mock-db-system) :id-1234) {:z 1234})))

(deftest lookup-by-id-test4
  (is (= (database/lookup-by-id (:app @mock-db-system)
                                "5b3bbdb409108c74577353e0")
         :no-document-found)))

(deftest lookup-by-param-test5
  (is (= (database/lookup-by-parameters (:app @mock-db-system) {:c 3})
         '({:a 1, :b 2, :c 3}
           {:a 3, :b 2, :c 3}
           {:a 4, :b 2, :c 3}
           {:a 7, :b 3, :c 3}))))


(deftest lookup-by-param-test6
  (is (= (database/lookup-by-parameters (:app @mock-db-system) {:c 3, :b 2})
         '({:a 1, :b 2, :c 3} {:a 3, :b 2, :c 3} {:a 4, :b 2, :c 3}))))

(deftest lookup-by-param-test7
  (is (= (database/lookup-by-parameters (:app @mock-db-system) {:c 3, :b 88})
         '())))

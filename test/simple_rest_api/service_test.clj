(ns simple-rest-api.service-test
  (:require [clojure.test :refer :all]
            [clojure.data.json :as json]
            [io.pedestal.test :refer :all]
            [io.pedestal.http :as bootstrap]
            [simple-rest-api.database :as database]
            [com.stuartsierra.component :as component]
            [simple-rest-api.service :as service]))



(def service (atom nil))

(defn with-test-db
  [f]
  (reset! database/system (component/start (database/query-system)))
  (reset! service
          (::bootstrap/service-fn (bootstrap/create-servlet service/service)))
  (f)
  (component/stop @database/system))

(use-fixtures :once with-test-db)

#_(def service
    (::bootstrap/service-fn (bootstrap/create-servlet service/service)))

(deftest indicator-test1
  (is (= (get-in (json/read-str
                   (:body (response-for @service
                                        :get
                                        "/indicators/5b3bbdb409108c74577353e0"))
                   :key-fn
                   keyword)
                 [:document :id])
         "5b3bbdb409108c74577353e0")))

(deftest indicator-test2
  (is
    (= (get-in (json/read-str (:body (response-for
                                       @service
                                       :get
                                       "/indicators/55b3bbdb409108c74577353e0"))
                              :key-fn
                              keyword)
               [:document])
       "no-document-found")))

(deftest indicator-test3
  (is (= (count (:document (json/read-str
                             (:body (response-for @service :get "/indicators"))
                             :key-fn
                             keyword)))
         100)))

(deftest indicator-test4
  (is (= (count (:document
                  (json/read-str
                    (:body (response-for
                             @service
                             :get
                             "/indicators?adversary=Arid%20Viper&tlp=white"))
                    :key-fn
                    keyword)))
         1)))

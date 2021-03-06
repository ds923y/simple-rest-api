(ns simple-rest-api.service
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [simple-rest-api.database :as db]
            [io.pedestal.http.body-params :as body-params]
            [ring.util.response :as ring-resp]))

(defn home-page
  [request]
  (ring-resp/response
    "example queries: <br> http://localhost:8080/indicators/5b3bbdb409108c74577353e0 <br> http://localhost:8080/indicators?adversary=Arid%20Viper&tlp=white <br> http://localhost:8080/indicators"))

(defn debug-page
  [request]
  (ring-resp/response
    (cheshire.core/generate-string
      (select-keys request [:params :path-params :query-params :form-params]))))

(defn query-by-id-page
  [request]
  (ring-resp/response
    (cheshire.core/generate-string {:document ((-> request
                                                   :db-query
                                                   :lookup-by-id)
                                                (-> request
                                                    :path-params
                                                    :id))})))

(defn query-by-params-page
  [request]
  (ring-resp/response
    (cheshire.core/generate-string {:document ((-> request
                                                   :db-query
                                                   :lookup-by-parameters)
                                                (:query-params request))})))

(def common-interceptors [(body-params/body-params) http/html-body])


;; Tabular routes
(def routes
  #{["/indicators" :get
     (conj common-interceptors `(:app @db/system) `query-by-params-page)]
    ["/indicators/:id" :get
     (conj common-interceptors `(:app @db/system) `query-by-id-page)]
    ["/" :get (conj common-interceptors `home-page)]})

;; Consumed by simple-rest-api.server/create-server
;; See http/default-interceptors for additional options you can configure
(def service
  {:env :prod,
   ;; You can bring your own non-default interceptors. Make
   ;; sure you include routing and set it up right for
   ;; dev-mode. If you do, many other keys for configuring
   ;; default interceptors will be ignored.
   ;; ::http/interceptors []
   ::http/routes routes,
   ;; Uncomment next line to enable CORS support, add
   ;; string(s) specifying scheme, host and port for
   ;; allowed source(s):
   ;;
   ;; "http://localhost:8080"
   ;;
   ;;::http/allowed-origins ["scheme://host:port"]
   ;; Tune the Secure Headers
   ;; and specifically the Content Security Policy appropriate to your
   ;; service/application
   ;; For more information, see: https://content-security-policy.com/
   ;;   See also: https://github.com/pedestal/pedestal/issues/499
   ;;::http/secure-headers {:content-security-policy-settings {:object-src
   ;;"'none'"
   ;;                                                          :script-src
   ;;                                                          "'unsafe-inline'
   ;;                                                          'unsafe-eval'
   ;;                                                          'strict-dynamic'
   ;;                                                          https: http:"
   ;;                                                          :frame-ancestors
   ;;                                                          "'none'"}}
   ;; Root for resource interceptor that is available by default.
   ::http/resource-path "/public",
   ;; Either :jetty, :immutant or :tomcat (see comments in project.clj)
   ;;  This can also be your own chain provider/server-fn --
   ;;  http://pedestal.io/reference/architecture-overview#_chain_provider
   ::http/type :jetty,
   ;;::http/host "localhost"
   ::http/port 8080,
   ;; Options to pass to the container (Jetty)
   ::http/container-options {:h2c? true,
                             :h2? false,
                             ;:keystore "test/hp/keystore.jks"
                             ;:key-password "password"
                             ;:ssl-port 8443
                             :ssl? false}})

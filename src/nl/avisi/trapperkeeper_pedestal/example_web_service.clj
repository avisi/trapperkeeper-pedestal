(ns nl.avisi.trapperkeeper-pedestal.example-web-service
  (:require [clojure.tools.logging :as log]
            [io.pedestal.http :as http]
            [ring.util.response :as ring-resp]
            [nl.avisi.trapperkeeper-pedestal.example-web-core :as core]
            [nl.avisi.trapperkeeper-pedestal.example-service :as hello-svc]
            [io.pedestal.http.route.definition :refer [defroutes]]
            [io.pedestal.interceptor :as interceptor]
            [puppetlabs.trapperkeeper.core :as trapperkeeper]
            [puppetlabs.trapperkeeper.services :as tk-services]))

(defn index [{:keys [create-greeting path-params]}]
  (let [who (:who path-params)]
    (ring-resp/response (str "<h1>" (create-greeting (if (empty? who) "stranger" who)) "</h1>"))))

(interceptor/defbefore get-greeting [{:keys [get-service request] :as ctx}]
  (assoc-in ctx [:request :create-greeting] (partial hello-svc/hello (get-service :HelloService))))

(defn get-service-interceptor [svc]
  (let [get-service (partial tk-services/get-service svc)]
    (interceptor/before ::get-service (fn [ctx] (assoc ctx :get-service get-service)))))

(defn service-interceptor [service svc]
  (assoc service
         ::http/interceptors
         (conj (::http/interceptors service) (get-service-interceptor svc))))

(defroutes routes [[["/*who" {:get index}
                     ^:interceptors [get-greeting http/html-body]]]])

(trapperkeeper/defservice hello-web-service
  [[:ConfigService get-in-config]
   [:WebroutingService add-servlet-handler get-route]
   HelloService]
  (init [this context]
        (log/info "Initializing hello webservice")
        (let [service  (-> {:env (if (get-in-config [:debug]) :dev :prod)
                            ::http/routes routes
                            ::http/resource-path "public"}
                           (http/default-interceptors)
                           (service-interceptor this))]
          (add-servlet-handler this (::http/servlet (http/create-servlet service))
                                            {:redirect-if-no-trailing-slash true}))
        context)

  (start [this context]
         (let [host (get-in-config [:webserver :host])
               port (get-in-config [:webserver :port])]
           (log/infof "Hello web service started; visit http://%s:%s%s/ to check it out!"
                      host port (get-route this)))
         context))

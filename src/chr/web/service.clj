(ns chr.web.service
  (:require [ring.middleware.json :as json]
            [ring.middleware.defaults :as setup]
            [ring.middleware.reload :as reload]
            [ring.util.response :as r]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.adapter.jetty :as jetty]
            [chr.service :as s]
            [cheshire.generate :as json-enc])
  (:import [org.bson.types ObjectId]))

(json-enc/add-encoder ObjectId (fn [oid gen] (.writeString gen (str oid))))

(defn- api*
  [routes]
  (setup/wrap-defaults routes setup/api-defaults))

(defroutes api-routes
  (context "/api" []
           (POST "/save" [:as {body :body}]
                 (r/response (s/save! body)))
           (GET "/changes/:user-id/:t1/:t2" [user-id t1 t2]
                (r/response (s/changes user-id (s/parse-date t1) (s/parse-date t2))))
           (route/not-found {:status 404 :body {:message "Not found"}})))

(def api
  (-> (api* api-routes)
      (reload/wrap-reload)
      (json/wrap-json-body {:keywords? true})
      (json/wrap-json-response)))

(defn run
  []
  (jetty/run-jetty api {:port 3360}))

(def -main run)

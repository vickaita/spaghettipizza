(ns pizza.handler
  (:gen-class) 
  (:require [compojure.core :as c :refer [defroutes OPTIONS GET POST]]
            [compojure.handler :as h :refer [site]]
            [compojure.route :as route :refer [files not-found]]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.reload :as reload] 
            [ring.middleware.multipart-params :as mp]
            [ring.util.response :as response]
            [pizza.pages :as pages]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [amazonica.aws.s3 :as s3]
            [amazonica.core :as aws]
            [digest]
            [environ.core :refer [env]])
  (:import (org.apache.commons.codec.binary Base64)
           (javax.imageio ImageIO)))

;(def ^{:const true} json-header
;  {"Content-Type" "application/json; charset=utf-8"})

(def dev-mode (atom true))

(def bucket-name "spaghettipizza.us")

(defn set-mode
  []
  (when (= "production" (System/getenv "APP_MODE"))
    (reset! dev-mode false)))

(defn set-credentials
  []
  (let [creds (edn/read-string (slurp (io/resource "credentials/aws.clj")))]
    (aws/defcredential
      (:aws-access-key creds)
      (:aws-secret-key creds)
      "us-east-1")))

(defn ensure-bucket
  []
  (println "Checking for the existence of the" bucket-name "bucket.")
  (when (and (s3/does-bucket-exist bucket-name) (not @dev-mode))
    (println "Creating the" bucket-name "bucket.")
    (s3/create-bucket bucket-name)))

(defroutes dev-routes
  (GET "/" [] (if @dev-mode
                (pages/home true)
                (response/redirect "spaghettipizza.us")))
  #_(GET "/pizza/" [] (pages/show @dev-mode))
  (mp/wrap-multipart-params
    (POST "/pizza/" {params :params}
          (let [file (get-in params [:data :tempfile])]
            (with-open [png-stream (io/input-stream file)]
              (let [file-name (str "pizza/" (digest/md5 file) ".png")]
                (s3/put-object
                  :bucket-name bucket-name
                  :key file-name
                  :input-stream png-stream
                  :access-control-list {:grant-permission ["AllUsers" "Read"]}
                  :metadata {:content-length (.length file)
                             :content-type "image/png"})
                (str {:file-name file-name})))))) 
  (route/files "/" {:root "resources/public"})
  (route/not-found #(pages/error-404)))

(defroutes prod-routes
  (OPTIONS "/pizza/" []
           {:status 200
            :headers {:access-control-allow-origin "http://spaghettipizza.us"}
            :body "OK"})
  (mp/wrap-multipart-params
    (POST "/pizza/" {params :params :as req}
          (let [file (get-in params [:data :tempfile])]
            (with-open [png-stream (io/input-stream file)]
              (let [file-name (str "pizza/" (digest/md5 file) ".png")]
                (s3/put-object
                  :bucket-name bucket-name
                  :key file-name
                  :input-stream png-stream
                  :access-control-list {:grant-permission ["AllUsers" "Read"]}
                  :metadata {:content-length (.length file)
                             :content-type "image/png"})
                {:status 200
                 :headers {:access-control-allow-origin "http://spaghettipizza.us"}
                 :body (str {:file-name file-name})}))))) 
  (route/not-found (pages/error-404)))

(defn -main
  [& args]
  (set-mode)
  (set-credentials)
  ;(ensure-bucket)
  (if @dev-mode
    (run-jetty (reload/wrap-reload (site #'dev-routes)) {:port 8080})
    (run-jetty (site #'prod-routes) {:port 8080})))

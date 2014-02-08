(ns pizza.handler
  (:gen-class)
  (:require [compojure.core :as c :refer [defroutes OPTIONS GET POST]]
            [compojure.handler :as h :refer [site]]
            [compojure.route :as route :refer [files not-found]]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.reload :as reload]
            [ring.middleware.multipart-params :as mp]
            [ring.util.response :as response]
            [ring.middleware.cors :as cors]
            [pizza.pages :as pages]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [aws.sdk.s3 :as s3]
            [cemerick.rummage :as sdb]
            [cemerick.rummage.encoding :as enc]
            [digest]
            [environ.core :refer [env]])
  (:import (org.apache.commons.codec.binary Base64)
           (javax.imageio ImageIO)))

(def bucket-name "spaghettipizza.us")

(let [c (edn/read-string (slurp (io/resource "credentials/aws.clj")))]
  (def creds {:access-key (:aws-access-key c)
              :secret-key (:aws-secret-key c)}))

(defn- ensure-bucket
  []
  (println "Checking for the existence of the" bucket-name "bucket.")
  (when-not (s3/bucket-exists? creds bucket-name)
    (println "Creating the" bucket-name "bucket.")
    (s3/create-bucket creds bucket-name)))

(defn- upload-file
  [file]
  (with-open [file-stream (io/input-stream file)]
    (let [file-hash (digest/md5 file)
          file-name (str "pizza/" file-hash ".png")
          client (sdb/create-client (:access-key creds) (:secret-key creds))
          config (assoc enc/keyword-strings :client client)]
      (s3/put-object creds bucket-name file-name file-stream
                     {:content-length (.length file) :content-type "image/png"}
                     (s3/grant :all-users :read))
      (sdb/put-attrs config bucket-name
                     {::sdb/id file-hash :created-at (System/currentTimeMillis)})
      [file-hash file-name])))

(defn handle-file
  [file]
  (if file
    (
     if-let [[file-hash file-name] (upload-file file)]
      {:status 200
       :headers {"Access-Control-Allow-Origin" "http://spaghettipizza.us"}
       :body (str {:file-name file-name :file-hash file-hash})}
      {:status 500
       :body "Error uploading the file to S3."})
    {:status 422
     :body "Missing File."}))

(defroutes dev-routes
  (GET "/" [debug] (pages/home false))
  (mp/wrap-multipart-params
    (POST "/pizza/" {{{file :tempfile} :data} :params} (handle-file file)))
  (route/files "/" {:root "resources/public"})
  (route/not-found (pages/error-404)))

(defroutes prod-routes
  (GET "/" [] (pages/home false))
  (mp/wrap-multipart-params
    (POST "/pizza/" {{{file :tempfile} :data} :params} (handle-file file)))
  (route/files "/" {:root "resources/public"})
  (route/not-found (pages/error-404)))

(defn -main
  [& args]
  (println "Starting server; VERSION =" (:pizza-version env))
  (if (= :dev (:environment env))
    (run-jetty (reload/wrap-reload (site #'dev-routes)) {:port 8080})
    (do (ensure-bucket)
        (run-jetty (site #'prod-routes) {:port 8080}))))

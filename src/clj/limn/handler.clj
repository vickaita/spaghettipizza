(ns limn.handler
  (:gen-class)
  (:require [compojure.core :as c :refer [defroutes OPTIONS GET POST]]
            [compojure.handler :as h :refer [site]]
            [compojure.route :as route :refer [files not-found]]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.reload :as reload]
            [ring.middleware.multipart-params :as mp]
            [ring.util.response :as response]
            [ring.middleware.cors :as cors]
            [limn.pages :as pages]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [aws.sdk.s3 :as s3]
            [cemerick.rummage :as sdb]
            [cemerick.rummage.encoding :as enc]
            [digest]
            [environ.core :refer [env]])
  (:import (org.apache.commons.codec.binary Base64)
           (javax.imageio ImageIO)))

(def ^:private creds (atom nil))
(defn- aws-keys [] (select-keys @creds [:access-key :secret-key]))
(defn- aws-bucket [] (:bucket-name @creds))

(defn- ensure-bucket
  "Checks to ensure that `bucket-name` exits on S3. If absent then it is
  created. TODO: Gracefully handle exceptions here if the bucket cannot be
  created. Also verify write permissions to the bucket."
  []
  (println "Checking for the existence of the" (aws-bucket) "bucket.")
  (when-not (s3/bucket-exists? (aws-keys) (aws-bucket))
    (println "Creating the" (aws-bucket) "bucket.")
    (s3/create-bucket (aws-keys) (aws-bucket))))

(defn- upload-file
  "Upload `file` to `bucket-name`."
  [file]
  (with-open [file-stream (io/input-stream file)]
    (let [file-hash (digest/md5 file)
          file-name (str "pizza/" file-hash ".png")
          client (sdb/create-client (:access-key (aws-keys)) (:secret-key (aws-keys)))
          config (assoc enc/keyword-strings :client client)]
      (s3/put-object (aws-keys) (aws-bucket) file-name file-stream
                     {:content-length (.length file) :content-type "image/png"}
                     (s3/grant :all-users :read))
      (sdb/put-attrs config (aws-bucket)
                     {::sdb/id file-hash :created-at (System/currentTimeMillis)})
      [file-hash file-name])))

(defn handle-file
  [file]
  (if file
    (if-let [[file-hash file-name] (upload-file (aws-bucket) (aws-keys) file)]
      {:status 200
       :headers {"Access-Control-Allow-Origin" "http://spaghettipizza.us"}
       :body (str {:file-name file-name :file-hash file-hash})}
      {:status 500
       :body "Error uploading the file to S3."})
    {:status 422
     :body "Missing File."}))

(defn pizza-list
  [creds]
  (let [client (sdb/create-client (:access-key (aws-keys)) (:secret-key (aws-keys)))
        config (assoc enc/keyword-strings :client client)
        pizzas (sdb/query-all config (str "select * from `" (aws-bucket) "`"))]
    (map #(str "<a href=\"http://spaghettipizza.us/?pizza=" (get % ::sdb/id)
               ".png\"><img src=\"http://spaghettipizza.us/pizza/" (get % ::sdb/id)
                             ".png\"></a>") pizzas)))

(defroutes all-routes
  (GET "/" {{debug :debug} :params}
       (pages/home (and debug (= :dev (:environment env)))))
  ;(GET "/workspace" [] (pages/workspace))
  (mp/wrap-multipart-params
    (POST "/pizza/" {{{file :tempfile} :data} :params} (handle-file file)))
  (GET "/pizza/" [] (pizza-list))
  (GET "/users/new" [] "Create a new user.")
  (route/files "/" {:root "resources/public"})
  (route/not-found (pages/error-404)))

(defn get-creds []
  (let [c (try
            (edn/read-string (slurp (io/resource "credentials/aws.clj")))
            (catch Exception e
              (prn "WARNING: Unable to find valid credentials. Please make sure
                   that you have created a `credentials/aws.clj` file in the
                   resources directory.")
              {:aws-access-key "ACCESS_KEY"
               :aws-secret-key "SECRET_KEY"
               :bucket-name "BUCKET_NAME"}))]
    {:access-key (:aws-access-key c)
     :secret-key (:aws-secret-key c)
     :bucket-name (:bucket-name c)}))

(defn -main
  [& args]
  (println "Starting server; VERSION =" (:pizza-version env))
  (reset! creds (get-creds))
  (if (= :dev (:environment env))
    (run-jetty (reload/wrap-reload (site #'all-routes)) {:port 8080})
    (do (ensure-bucket creds)
        (run-jetty (site #'all-routes) {:port 8080}))))

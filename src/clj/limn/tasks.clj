(ns limn.tasks
  (:require [amazonica.aws.s3 :as s3]
            [amazonica.core :as aws]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.java.shell :refer [sh]]
            [cljs.closure :as cljsc]
            [environ.core :refer [env]]
            [limn.pages :as pages])
  (:import (java.io File FileNotFoundException)))

;; TODO: should probably replace this file with some Rake tasks. It seems that
;; Ruby may just be better suited to this type of work.

(defn push-ui
  "Push the files up to S3."
  []
  (let [bucket-name "spaghettipizza.us"
        creds (edn/read-string (slurp (io/resource "credentials/aws.clj")))
        home-bytes (.getBytes (pages/home false))
        acl {:grant-permission ["AllUsers" "Read"]}]
    (aws/with-credential [(:aws-access-key creds)
                          (:aws-secret-key creds)
                          "us-east-1"]
      (println "Testing for the existence of the" bucket-name "bucket.")
      (when-not (s3/does-bucket-exist bucket-name)
        (println "Bucket not found. Creating a bucket called " bucket-name)
        (s3/create-bucket bucket-name))
      (println "OK.")
      (println "Generating the index page ...")
      (println "Uploading index.html ...")
      (with-open [home (io/input-stream home-bytes)]
        (s3/put-object
          :bucket-name bucket-name
          :key "index.html"
          :input-stream home
          :access-control-list acl
          :metadata {:content-length  (count home-bytes)
                     :content-type "text/html; charset=utf-8"}))
      (let [css (io/file "resources/public/css/main.css")
            js (io/file "resources/public/js/pizza.js")]
        (println "Uploading" (.getName css) "...")
        (s3/put-object
          :bucket-name bucket-name
          :key (str "css/" (.getName css))
          :file css
          :access-control-list acl)
        (println "Building ClojureScript ...")
        (cljsc/build "src/cljs/pizza/core.cljs"
                     {:output-to "resources/public/js/pizza.js"
                      :output-dir "resources/public/js"
                      :preamble ["react/react.min.js"]
                      :externs ["react/externs/react.js"]
                      :optimizations :advanced})
        (println "Uploading" (.getName js) "...")
        (s3/put-object
          :bucket-name bucket-name
          :key (str "js/" (.getName js))
          :file js
          :access-control-list acl)
        (println "Uploading images ...")
        (let [all-files (file-seq (io/file "resources/public/img/"))
              only-files (remove #(.isDirectory %) all-files)]
          (doseq [img only-files]
            (s3/put-object
              :bucket-name bucket-name
              :key (.substring (.getPath img) 17)
              :file (io/file img)
              :access-control-list acl)))))))

(defn push-api
  []
  (println "Building the uberjar.")
  (sh "lein" "with-profile" "prod" "uberjar")
  (println "Copying the jar to the api server.")
  (sh "scp"
      (str "./target/pizza-" (:pizza-version env) "-standalone.jar")
      (str "api.spaghettipizza.us:~/")))

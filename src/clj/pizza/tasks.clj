(ns pizza.tasks
  (:require [amazonica.aws.s3 :as s3]
            [amazonica.core :as aws]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.java.shell :refer [sh]]
            [cljs.closure :as cljsc]
            [pizza.pages :as pages]))

(defn push-ui
  "Push the files up to S3."
  []
  (let [bucket-name "spaghettipizza.us"
        creds (edn/read-string (slurp "resources/credentials/aws.clj"))
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
                     {:optimizations :advanced
                      :output-dir "resources/public/js"
                      :output-to "resources/public/js/pizza.js"})
        (println "Uploading" (.getName js) "...")
        (s3/put-object
          :bucket-name bucket-name
          :key (str "js/" (.getName js))
          :file js
          :access-control-list acl)
        (println "Uploading images ...")
        (doseq [img (-> "resources/public/img" io/file file-seq rest)]
          (s3/put-object
            :bucket-name bucket-name
            :key (str "img/" (.getName img))
            :file (io/file img)
            :access-control-list acl))))))

(defn push-api
  []
  ())

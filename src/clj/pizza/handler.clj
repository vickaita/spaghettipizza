(ns pizza.handler
  (:require [compojure.core :as c :refer [defroutes GET PATCH PUT POST DELETE]]
            [compojure.handler :as h :refer [site]]
            [compojure.route :as route :refer [files not-found]]
            [ring.middleware.reload :as reload] 
            [org.httpkit.server :as s :refer [run-server]]
            [pizza.pages :as pages]))

(def state (atom {:players []}))

(defroutes all-routes
  (GET "/" [] pages/home)
  (GET "/save" [] "save!")
  (POST "/register" [] "reg")
  (route/files "/" {:root "resources/public"})
  (route/not-found "<p>Page not found.</p>"))

(defn in-dev? [args]
  ;; TODO: doesn't work, so for now always return true
  (let [dev? (contains? (vec args) "dev")]
    (println dev?)
    true))

(defn -main [& args]
  (println "Starting server...")
  (let [handler (if (in-dev? args)
                  (reload/wrap-reload (site #'all-routes)) ;; only reload when dev
                  (site all-routes))]
    (run-server handler {:port 8080})))

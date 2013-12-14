(ns pizza.handler
  (:require [compojure.core :as c :refer [defroutes GET PATCH PUT POST DELETE]]
            [compojure.handler :as h :refer [site]]
            [compojure.route :as route :refer [files not-found]]
            [ring.middleware.reload :as reload] 
            [org.httpkit.server :as s :refer [run-server send! on-close on-receive]]
            [pizza.pages :as pages]))

(def ^{:const true} json-header  {"Content-Type" "application/json; charset=utf-8"})

(def state (atom {:kitchens []}))

(defn push!
  [channel message]
  (send! channel {:status 200 :headers json-header :body message}))

(defroutes all-routes
  (GET "/" [] pages/home)

  (POST "/kitchens"
        [kitchen]
        (do (swap! state update-in [:kitchens] conj kitchen)
            {:cookies {"kitchen" kitchen}
             :body (str {:kitchen kitchen})}))

  (GET "/ws" [req] (fn [req]
                     (s/with-channel req channel
                       (println "channel opened")
                       (send! channel
                              {:status 200 :headers json-header :body "connected"})
                       (on-close channel
                                 (fn [status] (println "channel closed: " status)))
                       (on-receive channel
                                   (fn [data]
                                     (println (keyword data))
                                     (condp = (keyword data)
                                       :reset (push! channel "you reset")
                                       :foo (push! channel "bar")))))))
  (route/files "/" {:root "resources/public"})
  (route/not-found pages/error-404))

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

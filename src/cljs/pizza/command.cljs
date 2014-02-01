(ns pizza.command
  (:require [pizza.stroke :as stroke]))

(defmulti exec
  (fn [_ [directive & arguments]] directive)
  :default (fn [_ cmd] (prn (str "Unknown command!" (pr-str cmd)))))

(defmethod exec :clear
  [app [_ tool]]
  (-> app
      (assoc :show-toolbar? false)
      (assoc :strokes [])))

(defmethod exec :save
  [app [_]]
  (-> app
      (assoc :show-toolbar? false)
      (assoc-in [:image :status] :pending))

 ; (let [api-url (if (= "spaghettipizza.us" (.-host (.-location js/document)))
 ;                 "http://api.spaghettipizza.us/pizza/"
 ;                 "/pizza/")
 ;       blob (<! (svg/svg->img-chan svg-elem 612 612))
 ;       data (doto (js/FormData.) (.append "data" blob))
 ;       {fh :file-hash} (reader/read-string (<! (ch/xhr api-url "POST" data)))
 ;       page-url (str #_(.-origin js/location) "?pizza=" fh)]
 ;   (om/transact! app :image-url page-url))
  )

(defmethod exec :select-tool
  [app [_ tool]]
  (-> app
      (assoc :tool tool)
      (assoc-in [:toolbar :tool] tool)
      (assoc :show-toolbar? false)))

(defmethod exec :new-stroke
  [app [_ e]]
  (let [new-stroke (-> (stroke/stroke)
                       (stroke/append e)
                       (assoc :skin (:tool app)))]
  (update-in app [:strokes] #(conj % new-stroke))))

(defmethod exec :extend-stroke
  [app [_ e]]
  (assoc-in app
            [:strokes (-> app :strokes count dec)]
            (stroke/append (-> app :strokes peek) e)))

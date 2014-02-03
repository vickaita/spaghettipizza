(ns pizza.command
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [put! close! chan <! map<]]
            [cljs.reader :as reader]
            [pizza.stroke :as stroke]
            [pizza.svg :as svg]
            [vickaita.channels :as ch]))

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
  (go
    (let [host (.-host (.-location js/document))
          ;; TODO: this should be set via configuration rather than inspecting
          ;; the host
          api-url (if (= "spaghettipizza.us" host)
                    "http://api.spaghettipizza.us/pizza/"
                    "/pizza/")
          svg-elem (doto (.getElementById js/document "main-svg")
                     ;; XXX: There seems to be a bug in either React, om, or
                     ;; sablono that is preventing the xmlns attribute from
                     ;; being set. This line is just to make sure that it is
                     ;; set before we try to create a data url otherwise there
                     ;; will be an error.
                     (.setAttribute "xmlns" "http://www.w3.org/2000/svg"))
          blob (<! (svg/svg->img-chan svg-elem 612 612))
          _ (prn "b")
          data (doto (js/FormData.) (.append "data" blob))
          _ (prn "c")
          {fh :file-hash} (reader/read-string (<! (ch/xhr api-url "POST" data)))
          page-url (str "?pizza=" fh ".png")]
      (prn "d")
      (.pushState js/history nil nil page-url)))
  (-> app
      (assoc :show-toolbar? false)
      (assoc-in [:image :status] :pending)))

(defmethod exec :display-image
  [app [_ url]]
  (-> app
      (assoc-in [:image :status] :ready)
      (assoc :image-url url)))

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

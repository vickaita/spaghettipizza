(ns limn.command
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [put! close! chan <! map<]]
            [cljs.reader :as reader]
            [limn.stroke :as stroke]
            [limn.svg :as svg]
            [vickaita.channels :as ch]))

(defmulti exec (fn [_ [directive & _]] directive))

(defmethod exec :resize
  [app [_ w h]]
   (-> app
       (assoc-in [:easel :width] w)
       (assoc-in [:easel :height] h)
       (assoc-in [:easel :scale-by] (/ (-> app :easel :view-box (nth 2))
                                       (min w h)))))

(defmethod exec :clear
  [app [_ tool]]
  (.setToken (:history app) "/")
  (-> app
      (assoc :image-loading? false)
      (assoc :image-url nil)
      (assoc :show-toolbar? false)
      (assoc-in [:easel :strokes] [])))

(defmethod exec :save
  [app [_]]
  (go
    (let [host (.-host (.-location js/document))
          ;; TODO: this should be set via configuration rather than inspecting
          ;; the host
          api-url (if (= "spaghettipizza.us" host)
                    "http://api.spaghettipizza.us/pizza/"
                    "/pizza/")
          align-svg (.getElementById js/document "align-svg")
          svg-elem (doto (.-firstChild align-svg)
                     ;; XXX: There seems to be a bug in either React, om, or
                     ;; sablono that is preventing the xmlns attribute from
                     ;; being set. This line is just to make sure that it is
                     ;; set before we try to create a data url otherwise there
                     ;; will be an error.
                     (.setAttribute "xmlns" "http://www.w3.org/2000/svg"))
          blob (<! (svg/svg->img-chan svg-elem 612 612))
          data (doto (js/FormData.) (.append "data" blob))
          {fh :file-hash} (reader/read-string (<! (ch/xhr api-url "POST" data)))
          page-url (str "?pizza=" fh ".png")]
      (.setToken (:history app) page-url)))
  (-> app
      (assoc :show-toolbar? false)
      (assoc :image-loading? true)))

(defmethod exec :display-image
  [app [_ url]]
  (-> app
      (assoc-in [:image :status] :ready)
      (assoc :image-loading? false)
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
                       (assoc :skin (:tool app))
                       (assoc :color (:color app)))]
  (update-in app [:easel :strokes] #(conj % new-stroke))))

(defmethod exec :extend-stroke
  [app [_ pt]]
  (assoc-in app
            [:easel :strokes (-> app :easel :strokes count dec)]
            (stroke/append (-> app :easel :strokes peek) pt)))

(defmethod exec :set-color
  [app [_ color]]
  (conj app {:color color :show-toolbar? false}))

(defmethod exec :show-color-wheel
  [app _]
  (assoc app :show-color-wheel? true))

(defmethod exec :hide-color-wheel
  [app _]
  (assoc app :show-color-wheel? false))

(defmethod exec :toggle-color-wheel
  [app _]
  (assoc app :show-color-wheel? (not (:show-color-wheel? app))))

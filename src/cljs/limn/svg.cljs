(ns limn.svg
  (:require [clojure.string :refer [join]]
            [cljs.core.async :refer [put! chan]]
            [goog.dom :as dom]
            [goog.events :as events]))

(defn- M
  [[x y]]
  (str "M" x " " y))

(defn- C
  [[x1 y1] [x2 y2] [x y]]
  (str "C" x1 " " y1 " " x2 " " y2 " " x " " y))

(defn- S
  [[x2 y2] [x y]]
  (str "S" x2 " " y2 " " x " " y))

(defn svg->img-chan
  [svg-elem w h]
  (let [img (doto (js/Image.)
              (.setAttribute "width" w)
              (.setAttribute "height" h))
        canvas (doto (dom/createElement "canvas")
                 (.setAttribute "width" w)
                 (.setAttribute "height" h))
        context (.getContext canvas "2d")
        svg-data (.serializeToString (js/XMLSerializer.) svg-elem)
        svg-blob (js/Blob. #js [svg-data]
                           #js {:type "image/svg+xml;base64"})
        url (.createObjectURL js/URL svg-blob)
        out (chan)]
    (set! (.-onload img)
          (fn [e]
            (.drawImage context img 0 0 w h)
            (.revokeObjectURL js/URL url)
            (let [url (.toDataURL canvas "image/png")
                  binary (js/atob (aget (.split url ",") 1))
                  arr #js []]
              (dotimes [i (alength binary)]
                (aset arr i (.charCodeAt binary i)))
              (put! out (js/Blob. #js [(js/Uint8Array. arr)]
                                  #js {:type "image/png"})))))
    (set! (.-onerror img) #(.log js/console "There was an error:" %))
    (set! (.-src img) url)
    out))

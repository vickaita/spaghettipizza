(ns pizza.core
  (:require-macros [cljs.core.async.macros :refer [go alt!]])
  (:require [goog.dom :as dom]
            [goog.dom.classlist :as cls]
            [goog.events :as evt]
            [cljs.core.async :refer  [put! chan <!]]
            [clojure.browser.repl :as repl]
            [pizza.svg :as svg]
            [pizza.pizza :as pzz]
            [pizza.spaghetti :as spg]))

(defn event-channel
  [event-type element]
  (let [out (chan)]
    (evt/listen element event-type #(put! out %))
    out))

(defn e->pt [e] [(.-offsetX e) (.-offsetY e)])

(def current-noodle (atom nil))
(def current-tool (atom :spaghetti))

(defn enable-spaghetti-drawing
  [svg-elem]
  (let [down (event-channel "mousedown" svg-elem)
        move (event-channel "mousemove" svg-elem)
        up (event-channel "mouseup" js/document)]
    (go (while true
          (alt! down ([e] (let [noodle (spg/create-topping @current-tool (e->pt e))]
                       (reset! current-noodle noodle)
                       (dom/append svg-elem (:element noodle))))
                move ([e] (swap! current-noodle spg/add-point! (e->pt e)))
                up (reset! current-noodle nil))))))

(defn enable-tool-selection
  [toolbar]
  (let [clicks (event-channel "click" toolbar)]
    (go (while true
          (let [elem (.-target (<! clicks))
                tool (keyword (.getAttribute elem "data-tool"))]
            (when tool (do (reset! current-tool tool)
                           (let [elems (dom/getChildren toolbar)]
                             (dotimes [i (alength elems)]
                               (cls/remove (aget elems i) "active")))
                           (cls/add elem "active"))))))))

(defn main
  []
  (let [svg-elem (dom/getElement "main-svg")]
    (evt/listen (dom/getElement "clean") "click"
                #(do (dom/removeChildren svg-elem) (pzz/draw-pizza svg-elem)))
    (pzz/draw-pizza svg-elem)
    (enable-spaghetti-drawing svg-elem)
    (enable-tool-selection (dom/getElement "toolbar"))))

(evt/listen js/document "DOMContentLoaded" main)
;(repl/connect "http://ui:9000/repl")

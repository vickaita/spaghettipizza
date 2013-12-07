(ns pizza.core
  (:require-macros [cljs.core.async.macros :refer [go alt!]])
  (:require [goog.dom :as dom]
            [goog.dom.classlist :as cls]
            [goog.events :as evt]
            [cljs.core.async :refer [put! chan <! map<]]
            [clojure.browser.repl :as repl]
            [pizza.svg :as svg]
            [pizza.pizza :as pzz]
            [pizza.spaghetti :as spag]))

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
  (let [down (map< e->pt (event-channel "mousedown" svg-elem))
        move (map< e->pt (event-channel "mousemove" svg-elem))
        up (event-channel "mouseup" js/document)]
    (go (while true
          (alt! down ([e] (let [n (spag/create-topping @current-tool e)]
                            (reset! current-noodle n)
                            (dom/append svg-elem (:element n))))
                move ([e] (swap! current-noodle spag/add-point! e))
                up (reset! current-noodle nil))))))

(defn- activate!
  "Iterate through all the elements in node-list removing class-name except for
  node which will have class-name added."
  ([node-list node] (activate! node-list node "active"))
  ([node-list node class-name]
  (dotimes [i (alength node-list)]
    (let [n (aget node-list i)]
      (if (= node n)
        (cls/add node class-name)
        (cls/remove n class-name))))))

(defn enable-tool-selection
  [toolbar]
  (let [clicks (map< #(.-target %) (event-channel "click" toolbar))]
    (go (while true
          (let [elem (<! clicks)
                tool (keyword (.getAttribute elem "data-tool"))]
            (when tool
              (reset! current-tool tool)
              (activate! (dom/getChildren toolbar) elem)))))))

(defn main
  []
  (let [svg-elem (dom/getElement "main-svg")]
    (evt/listen (dom/getElement "clean") "click"
                #(do (dom/removeChildren svg-elem) (pzz/draw-pizza svg-elem)))
    (pzz/draw-pizza svg-elem)
    (enable-spaghetti-drawing svg-elem)
    (enable-tool-selection (dom/getElement "toolbar"))))

(evt/listen js/document "DOMContentLoaded" main)
(repl/connect "http://ui:9000/repl")
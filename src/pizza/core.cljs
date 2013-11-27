(ns pizza.core
  (:require-macros [cljs.core.async.macros :refer [go alt!]])
  (:require [goog.dom :as dom]
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

(defn enable-spaghetti-drawing
  [svg-elem]
  (let [down (event-channel "mousedown" svg-elem)
        move (event-channel "mousemove" svg-elem)
        up (event-channel "mouseup" svg-elem)
        current-noodle (atom nil)]
    (go (while true
          (alt! down (let [noodle (spg/create-noodle)]
                       (reset! current-noodle noodle)
                       (dom/append svg-elem (spg/to-svg noodle)))
                move ([e] (when-let [noodle @current-noodle]
                            (if (< (spg/length noodle) spg/max-length)
                              (spg/add-point! noodle (.-offsetX e) (.-offsetY e))
                              (reset! current-noodle nil))))
                up (do #_(spg/smooth @current-noodle)
                       (reset! current-noodle nil)))))))

(defn main
  []
  (let [svg-elem (dom/getElement "main-svg")]
    (evt/listen (dom/getElement "clean") "click"
                #(do (dom/removeChildren svg-elem) (pzz/draw-pizza svg-elem)))
    (pzz/draw-pizza svg-elem)
    (enable-spaghetti-drawing svg-elem)))

(evt/listen js/document "DOMContentLoaded" main)
(repl/connect "http://ui:9000/repl")

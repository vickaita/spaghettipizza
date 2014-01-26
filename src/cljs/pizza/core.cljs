(ns pizza.core
  (:require-macros [cljs.core.async.macros :refer [go alt!]]
                   [dommy.macros :refer [node]])
  (:require [goog.dom :as dom]
            [goog.dom.classlist :as cls]
            [goog.dom.forms :as forms]
            [goog.events :as evt]
            [goog.net.XhrIo :as xhr]
            [goog.net.WebSocket]
            [goog.history.Html5History]
            [cljs.core.async :refer [put! close! chan <! map<]]
            [clojure.browser.repl :as repl]
            [dommy.core]
            [vickaita.channels :refer [event websocket]]
            [vickaita.console :refer [log]]
            [pizza.toolbar :as toolbar]
            [pizza.ajax :as ajax]
            [pizza.svg :as svg]
            [pizza.pizza :as pzz]
            [pizza.easel :as easel]
            [pizza.spaghetti :refer [create-topping add-point!]]))

(enable-console-print!)

(defn normalize-point
  "Convert an event into a point."
  [e]
  (let [elem (.-currentTarget e)
        offset (.getBoundingClientRect elem)
        left (.-left offset)
        top (.-top offset)
        scale-factor (/ 512 (.-width offset))]
    (case (.-type e)
      ("touchstart" "touchmove")
      (let [t (-> e .getBrowserEvent .-touches (aget 0))]
        [(* scale-factor (- (.-pageX t) left))
         (* scale-factor (- (.-pageY t) top))])
      "touchend"
      nil
      (let [b (.getBrowserEvent e)]
        [(* scale-factor (- (.-pageX b) left))
         (* scale-factor (- (.-pageY b) top))]))))

(def current-noodle (atom nil))

(defn- start-noodle
  [e]
  (.preventDefault e)
  (.stopPropagation e)
  (let [pt (normalize-point e)
        n (create-topping @toolbar/current-tool pt)]
    (reset! current-noodle n)
    (dom/append (.-currentTarget e) (:element n))))

(defn- move-noodle
  [e]
  (.preventDefault e)
  (.stopPropagation e)
  (let [pt (normalize-point e)]
    (swap! current-noodle add-point! pt)))

(defn- end-noodle
  [e]
  (.preventDefault e)
  (.stopPropagation e)
  (reset! current-noodle nil))

(defn enable-spaghetti-drawing
  [svg-elem]
  (evt/listen svg-elem "mousedown" start-noodle)
  (evt/listen svg-elem "touchstart" start-noodle)
  (evt/listen svg-elem "mousemove" move-noodle)
  (evt/listen svg-elem "touchmove" move-noodle)
  (evt/listen js/document "mouseup" end-noodle)
  (evt/listen svg-elem "touchup" end-noodle))

(defn- get-pizza-hash
  []
  ;(.. js/document -location -search (split "=") (aget 1))
  (-> (.-search (.-location js/document))
      (.split "=")
      (aget 1)))

(defn -main
  []
  (let [easel (dom/getElement "easel")
        svg-elem (dom/getElement "main-svg")
        history (goog.history.Html5History.)]

    (doto history
      (.setUseFragment false)
      (.setEnabled true))
    (easel/adjust-size! svg-elem)
    (easel/update! easel (get-pizza-hash))
    (evt/listen history "popstate" #(easel/update! easel (get-pizza-hash)))

    ;; Some event handlers for managing toolbar opening/closing.
    (evt/listen (dom/getElement "menu-control") "click"
                #(do (.preventDefault %)
                     (.stopPropagation %)
                     (toolbar/toggle!)))

    (evt/listen (dom/getElement "clean") "click"
                (fn [e]
                  (doto svg-elem
                    dom/removeChildren
                    (dom/append (pzz/fresh-pizza)))
                  (set! (.-search (.-location js/document)) "")
                  (toolbar/hide!)))

    ;; This seems to be breaking noodle drawing on mobile, so disabled until I
    ;; have time to investigate
    #_(evt/listen (dom/getElement "page") "click"
                  #(when (toolbar/visible?)
                     (do (.preventDefault %)
                         (.stopPropagation %)
                         (toolbar/hide!))))

    (enable-spaghetti-drawing svg-elem)
    (toolbar/enable-tool-selection! (dom/getElement "toolbar"))
    (toolbar/enable-save-button! (dom/getElement "photo") svg-elem history)))

(evt/listen js/document "DOMContentLoaded" -main)
#_(repl/connect "http://ui:9000/repl")

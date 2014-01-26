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

;; TODO: consider consolidating all application state into a single atom.
(def current-noodle (atom nil))

;; TODO: Move these *-noodle methods and normalize-point into a separate
;; namespace possibly `pizza.easel` or perhaps a new (pizza.events?).
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

;; TODO: this should almost certainly be in the pizza.easel namespace (or
;; pizza.events if that ends up being a real thing).
(defn enable-spaghetti-drawing
  [svg-elem]
  (doto svg-elem
    (evt/listen "mousedown" start-noodle)
    (evt/listen "touchstart" start-noodle)
    (evt/listen "mousemove" move-noodle)
    (evt/listen "touchmove" move-noodle)
    (evt/listen "touchend" end-noodle))
  ;; Mouseup event triggers on whatever element the pointer is over unlike the
  ;; touchend event which always fires from the origin element.
  (evt/listen js/document "mouseup" end-noodle))

(defn- get-pizza-hash
  []
  ;; TODO: Can I get this information straight from the "navigation" event and
  ;; eliminate this function entirely?
  (-> (.-search (.-location js/document))
      (.split "=")
      (aget 1)))

(defn -main
  []
  (let [easel (dom/getElement "easel")
        svg-elem (dom/getElement "main-svg")
        ;; TODO: goog.history.Html5History is pretty annoying and doesn't fit
        ;; very well with a functional style. I should either use the native
        ;; HTML5 methods or wrap it.
        history (goog.history.Html5History.
                  js/window
                  (let [tt #js {}]
                    (set! (.-createUrl tt)
                          (fn [token _ _] token))
                    (set! (.-retrieveToken tt)
                          (fn [path-prefix location]
                            (.substr (.-pathname location)
                                     (count path-prefix))))
                    tt)
                  ;; XXX: For some reason this tagged literal is breaking in
                  ;; advanced compilation. That's why I'm doing that crazy let
                  ;; block above. Honestly, I don't know why this isn't working.
                  ;#js {:createUrl (fn [token _ _] token)
                  ;     :retrieveToken (fn [path-prefix location]
                  ;                      (.substr (.-pathname location)
                  ;                               (count path-prefix)))}
                  )]

    (doto history
      (evt/listen "navigate" #(easel/update! easel (get-pizza-hash)))
      (.setUseFragment false)
      (.setEnabled true))

    ;; TODO: This should also be fired whenever the viewport is resized.
    (easel/adjust-size! svg-elem)

    ;; Some event handlers for managing toolbar opening/closing.
    (evt/listen (dom/getElement "menu-control") "click"
                #(do (.preventDefault %)
                     (.stopPropagation %)
                     (toolbar/toggle!)))

    (evt/listen (dom/getElement "clean") "click"
                (fn [e]
                  (toolbar/hide!)
                  (.setToken history (str (.-origin js/location) "/"))
                  (easel/update! easel (get-pizza-hash))))

    ;; XXX: This seems to be breaking noodle drawing on mobile, so disabled
    ;; until I have time to investigate.
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

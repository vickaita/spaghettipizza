(ns limn.core
  (:require-macros [cljs.core.async.macros :refer [go alt!]])
  (:require [goog.events :as events]
            [goog.dom]
            [goog.history.Html5History]
            [goog.history.EventType]
            [goog.dom.ViewportSizeMonitor]
            [cljs.core.async :refer [put! close! chan <! map<]]
            [clojure.browser.repl :as repl]
            [clojure.string :refer [join]]
            [om.core :as om :include-macros true]
            [sablono.core :refer-macros [html]]
            [secretary.core :as secretary :include-macros true :refer [defroute]]
            [limn.models.app :refer [default-app-state]]
            [limn.views.app :refer [app-view]]
            [limn.command :refer [exec]]))

(enable-console-print!)
(.initializeTouchEvents js/React true)

(def ^:private commands (chan 100))

(def ^:private history
  (goog.history.Html5History.
    js/window
    (let [transformer #js {}]
      (set! (.-createUrl transformer) (fn [token _ _] token))
      (set! (.-retrieveToken transformer) (fn [path-prefix location]
                                            (.substr (.-pathname location)
                                                     (count path-prefix))))
      transformer)))

(def ^:private sizeMonitor (goog.dom.ViewportSizeMonitor.))

(def ^:private app-state
  (atom (merge default-app-state {:commands commands :history history})))

(defroute "/pizza/:pizza" [pizza]
  (put! commands [:display-image (str "/pizza/" pizza)]))

(defroute "/" [query-params]
  (when-let [pizza-hash (get query-params "pizza")]
    (put! commands [:display-image (str "/pizza/" pizza-hash)])))

(doto history
  (events/listen goog.history.EventType/NAVIGATE
                 #(secretary/dispatch! (str (.-pathname js/location)
                                            (.-search js/location))))
  (.setUseFragment false)
  (.setEnabled true))

(defn- monitor-viewport-size
  "Monitor the size of the window and update accordingly."
  [sizeMonitor commands]
  (events/listen
    sizeMonitor
    "resize"
    (fn [e]
      (when-let [size (.getSize sizeMonitor)]
        (let [w (.-width size)
              h (.-height size)]
          (put! commands [:resize w h]))))))

(defn- monitor-keypress
  [doc commands]
  (events/listen
    doc
    "keypress"
    (fn [e]
      (doto e .preventDefault .stopPropagation)
      (when-let [command (condp = (js/String.fromCharCode (.-keyCode e))
                           "c" [:toggle-color-wheel]
                           nil)]
        (put! commands command)))))

(go (while true
      (let [command (<! commands)]
        (swap! app-state exec command))))
(monitor-viewport-size sizeMonitor commands)
(monitor-keypress js/document commands)
(om/root
  app-view app-state
  {:target (.-body js/document) :shared {:commands commands}})
(.dispatchEvent sizeMonitor "resize")
;(repl/connect "http://ui:9000/repl")

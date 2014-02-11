(ns pizza.core
  (:require-macros [cljs.core.async.macros :refer [go alt!]])
  (:require [goog.events :as events]
            [goog.history.Html5History]
            [goog.dom]
            [goog.dom.ViewportSizeMonitor]
            [cljs.core.async :refer [put! close! chan <! map<]]
            [clojure.browser.repl :as repl]
            [clojure.string :refer [join]]
            [om.core :as om :include-macros true]
            [sablono.core :refer-macros [html]]
            [pizza.toolbar :as toolbar]
            [pizza.easel :as easel]
            [pizza.command :refer [exec]]))

(def ^:private commands (chan))

(def ^:private history (goog.history.Html5History.
                         js/window
                         (let [tt #js {}]
                           (set! (.-createUrl tt)
                                 (fn [token _ _] token))
                           (set! (.-retrieveToken tt)
                                 (fn [path-prefix location]
                                   (.substr (.-pathname location)
                                            (count path-prefix))))
                           tt)))

(defn- monitor-history
  [history commands]
  (doto history
    (.setUseFragment false)
    (.setEnabled true)
    (events/listen
      "navigate"
      (fn [e]
        (.log js/console e)
        (when-let [search (.-search (.-location js/document))]
          (let [pizza-hash (-> search (.split "=") (aget 1))
                img-url (when (> (count pizza-hash) 0)
                          (str "/pizza/" pizza-hash))]
            (put! commands [:display-image img-url])))))))

(def ^:private sizeMonitor (goog.dom.ViewportSizeMonitor.))

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
                           "c" [:show-color-wheel]
                           nil)]
        (put! commands command)))))

(def ^:private app-state
  (atom {:commands commands
         :history history
         :debug true
         :image-url nil
         :image-loading? false
         :easel-width 0
         :easel-height 0
         :viewport-width 512
         :viewport-height 512
         :scale-by 1
         :granularity 5
         :pizza {:crust (easel/create-irregular-circle [256 256] 227)
                 :sauce (easel/create-irregular-circle [256 256] 210)}
         :strokes []
         :show-toolbar? false
         :show-color-wheel? true
         :toolbar {:groups [#_{:name "Test"
                               :tools [{:id :edit :name "Edit"}]}
                            {:name "Pasta"
                             :tools [{:id :spaghetti :name "Spaghetti"}
                                     {:id :linguini :name "Linguini"}
                                     {:id :ziti :name "Ziti"}]}
                            {:name "Cheese"
                             :tools [{:id :ricotta :name "Ricotta"}]}
                            {:name "Herbs"
                             :tools [{:id :basil :name "Basil"}]}]
                   :colors [{:name "Red" :fill "#F86969" :stroke "#F04F4F"}
                            {:name "Orange" :fill "#EF951B" :stroke "#B55F11"}
                            {:name "Yellow" :fill "#FAE265" :stroke "#DDAB0B"}
                            {:name "Green" :fill "#83D874" :stroke "#5FBA52"}
                            {:name "Blue" :fill "#6EB4E1" :stroke "#1E6B92"}
                            {:name "Purple" :fill "#D459A4" :stroke "#AE4173"}
                            {:name "White" :fill "#F2F2F2" :stroke "#D9D9D9"}
                            {:name "Black" :fill "#202020" :stroke "#181818"}]}
         :tool :spaghetti
         :color {:name "Red" :fill "#F86969" :stroke "#F04F4F"}}))

(defn- app-view
  [app owner]
  (om/component
    (html
      [:div#site {:class (join " "
                               [(when (:show-toolbar? app) "show-toolbar")
                                (when (:show-color-wheel? app) "show-color-wheel")])}
       (om/build toolbar/toolbar
                 (om/graft {:commands (:commands app)
                            :tool (:tool app)
                            :color (:color app)
                            :groups (:groups (:toolbar app))
                            :colors (:colors (:toolbar app))}
                           app))
       [:div#page
        [:header#masthead
         [:a#menu-control
          {:on-click (fn [e]
                       (doto e .preventDefault .stopPropagation)
                       (om/transact! app [:show-toolbar?] not))}]
         [:h1 "Spaghetti Pizza"]]
        (om/build easel/easel app)]
       [:div.palettes
        (om/build toolbar/color-wheel
                  (om/graft {:commands (:commands app)
                             :color (:color app)
                             :colors (:colors (:toolbar app))}
                            app))]
       [:footer [:p "Created by Vick Aita"]]])))


(defn -main
  []
  (go (while true
         (let [command (<! commands)]
           ;(when (:debug @app-state) (prn command))
           (swap! app-state exec command))))
   ;; Render the the application and update as the state changes.
   (om/root app-state app-view (.-body js/document))
   ;; Trigger a resize event after the DOM has rendered.
   (.dispatchEvent sizeMonitor "resize"))


(enable-console-print!)
(.initializeTouchEvents js/React true)
(monitor-history history commands)
(monitor-viewport-size sizeMonitor commands)
(monitor-keypress js/document commands)
(events/listen js/document "DOMContentLoaded" -main)
;(repl/connect "http://ui:9000/repl")

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
            [secretary.core :as secretary :include-macros true :refer [defroute]]
            [pizza.toolbar :as toolbar]
            [pizza.easel :as easel]
            [pizza.command :refer [exec]]))

(def ^:private commands (chan))

(defroute "/pizza/:pizza-hash" [pizza-hash]
  (put! commands [:display-image (str "/pizza/" pizza-hash)]))

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
                           "c" [:toggle-color-wheel]
                           nil)]
        (put! commands command)))))

(def ^:private app-state
  (atom {:commands commands
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
         :show-color-wheel? false
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

(enable-console-print!)
(.initializeTouchEvents js/React true)
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
